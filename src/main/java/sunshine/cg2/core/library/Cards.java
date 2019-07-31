package sunshine.cg2.core.library;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import sunshine.cg2.core.game.Buff;
import sunshine.cg2.core.game.BuffInfo;
import sunshine.cg2.core.game.Card;
import sunshine.cg2.core.game.Card.Position;
import sunshine.cg2.core.game.CardInfo;
import sunshine.cg2.core.game.Game;
import sunshine.cg2.core.game.GameOverThrowable;
import sunshine.cg2.core.game.Player;
import sunshine.cg2.core.game.BuffInfo.KeyWord;
import sunshine.cg2.core.game.CardInfo.Clz;
import sunshine.cg2.core.game.CardInfo.Race;
import sunshine.cg2.core.game.CardInfo.Type;
import sunshine.cg2.core.game.event.Event;
import sunshine.cg2.core.game.event.GainBuffEvent;
import sunshine.cg2.core.game.event.LoseBuffEvent;
import sunshine.cg2.core.game.event.globalevent.AfterTurnEndEvent;
import sunshine.cg2.core.game.event.globalevent.EnterTableEvent;
import sunshine.cg2.core.game.event.globalevent.LeaveTableEvent;
import sunshine.cg2.core.game.event.globalevent.SummonEvent;
import sunshine.cg2.core.util.CardCreator;
import sunshine.cg2.core.util.CardCreator.CardInfoAdapter;

public class Cards {

	public static abstract class TableBuffInfo extends BuffInfo
	{
		private final BuffInfo effectInfo;
		private final String effectName;
		
		public TableBuffInfo(KeyWord[] keyWords,BuffInfo effectInfo,String effectName)
		{
			super(keyWords,new Object[]{EnterTableEvent.class,GainBuffEvent.class,LeaveTableEvent.class,LoseBuffEvent.class},false);
			this.effectInfo=effectInfo;
			this.effectName=effectName;
		}
		
		@Override
		public void onTrigger(Buff buff,Event event)
		{
			Game game=buff.toBuff.getGame();
			if(event instanceof EnterTableEvent)
			{
				Card who=((EnterTableEvent)event).card;
				if(who==buff.toBuff)
				{
					game.forEachCardOnTable(c->
					{
						if(filter(buff,c))
						{
							c.gainBuff(effectInfo,effectName,buff);
						}
					});
				}
				else
				{
					if(filter(buff,who))
					{
						who.gainBuff(effectInfo,effectName,buff);
					}
				}
			}
			else if(event instanceof LeaveTableEvent)
			{
				Card who=((LeaveTableEvent)event).card;
				if(who==buff.toBuff)
				{
					game.forEachCardOnTable(c->
					{
						Buff b=c.getEffectBySource(buff);
						if(b!=null)c.loseBuff(b);
					});
				}
				else
				{
					LeaveTableEvent.Reason reason=((LeaveTableEvent)event).reason;
					if(reason==LeaveTableEvent.Reason.CONTROL||reason==LeaveTableEvent.Reason.CHANGEHERO)
					{
						Buff b=who.getEffectBySource(buff);
						if(b!=null)who.loseBuff(b);
					}
				}
			}
			else if(event instanceof GainBuffEvent)
			{
				game.forEachCardOnTable(c->
				{
					if(filter(buff,c))
					{
						c.gainBuff(effectInfo,effectName,buff);
					}
				});
			}
			else if(event instanceof LoseBuffEvent)
			{
				game.forEachCardOnTable(c->
				{
					Buff b=c.getEffectBySource(buff);
					if(b!=null)c.loseBuff(b);
				});
			}
		}
		
		protected abstract boolean filter(Buff buff,Card card);			
	}
	
	public static class MyMinionBuffInfo extends TableBuffInfo
	{
		public MyMinionBuffInfo(KeyWord[] keyWords,BuffInfo effectInfo,String effectName)
		{
			super(keyWords,effectInfo,effectName);
		}
		
		@Override
		protected boolean filter(Buff buff,Card card)
		{
			return card.getPosition()==Position.MINION&&card.getOwner()==buff.toBuff.getOwner();
		}
	}
	
	public static class MyOtherMinionBuffInfo extends MyMinionBuffInfo
	{
		public MyOtherMinionBuffInfo(KeyWord[] keyWords,BuffInfo effectInfo,String effectName)
		{
			super(keyWords,effectInfo,effectName);
		}
		
		@Override
		protected boolean filter(Buff buff,Card card)
		{
			return buff.toBuff!=card&&super.filter(buff,card);
		}
	}
	
	public static class PPEffectBuffInfo extends BuffInfo
	{
		private final int atk;
		private final int HP;
		
		public PPEffectBuffInfo(KeyWord[] keyWords,int atk,int HP)
		{
			super(keyWords,new Object[]{GainBuffEvent.class,LoseBuffEvent.class},true);
			this.atk=atk;
			this.HP=HP;
		}
		
		@Override
		public void onTrigger(Buff buff,Event event)
		{
			if(event instanceof GainBuffEvent)buff.toBuff.pp(atk,HP,false);
			else buff.toBuff.pp(-atk,-HP,false);
		}
	}
	
	public static class ThisTurnBuffInfo extends BuffInfo
	{
		private static Object[] events(Object[] c)
		{
			if(c==null)return new Object[]{AfterTurnEndEvent.class};
			Object[] rt=Arrays.copyOf(c,c.length+1);
			rt[c.length]=AfterTurnEndEvent.class;
			return rt;
		}
		
		public ThisTurnBuffInfo(KeyWord[] keyWords,Object[] extraEvents)
		{
			super(keyWords,events(extraEvents),false);
		}
		
		@Override
		public void onTrigger(Buff buff,Event event)
		{
			if(event instanceof AfterTurnEndEvent)buff.toBuff.loseBuff(buff);
			else onExtraEvents(buff,event);
		}
		
		public void onExtraEvents(Buff buff,Event event)
		{
		}
	}
	
	public static class ThisTurnPPBuffInfo extends BuffInfo
	{
		private static Object[] events(Object[] c)
		{
			int l;
			Object[] rt;
			if(c==null)
			{
				l=0;
				rt=new Object[3];
			}
			else
			{
				l=c.length;
				rt=Arrays.copyOf(c,c.length+3);
			}
			rt[l]=AfterTurnEndEvent.class;
			rt[l+1]=GainBuffEvent.class;
			rt[l+2]=LoseBuffEvent.class;
			return rt;
		}
		
		private final int atk;
		private final int HP;
		
		public ThisTurnPPBuffInfo(KeyWord[] keyWords,Object[] extraEvents,int atk,int HP)
		{
			super(keyWords,events(extraEvents),false);
			this.atk = atk;
			this.HP = HP;
		}
		
		@Override
		public void onTrigger(Buff buff,Event event)
		{
			if(event instanceof AfterTurnEndEvent)buff.toBuff.loseBuff(buff);
			else if(event instanceof GainBuffEvent)buff.toBuff.pp(atk,HP,false);
			else if(event instanceof LoseBuffEvent)buff.toBuff.pp(-atk,-HP,false);
			else onExtraEvents(buff,event);
		}
		
		public void onExtraEvents(Buff buff,Event event)
		{
		}
	}
	
	public static class DKBuffInfo extends BuffInfo
	{
		int armor;
		
		public DKBuffInfo(int armor)
		{
			super(null,new Object[]{EnterTableEvent.class},false);
		}
		
		@Override
		public void onTrigger(Buff buff,Event event)
		{
			buff.toBuff.getOwner().gainArmor(armor);
		}
	}
	
	public static class DamageSpellCard extends CardInfoAdapter
	{
		private int damage;
		
		public DamageSpellCard(int damage)
		{
			this.damage=damage;
		}
		
		@Override
		public boolean canTarget(Card card,Player player,Card target,int choi)
		{
			return target!=null&&!target.hasKW(KeyWord.IMMUNE);
		}
		
		@Override
		public void doBattlecry(Card card,Player player,Card target,int choi)
		{
			target.takeDamage(card,damage);
		}
	}
	
	private Cards()
	{
	}
	
	private static void register(CardInfo card)
	{
		DEFAULT_LIBRARY.put(card.name,card);
	}
	
	/*
	hs.basic:albkbhz
	hs.basic:assj
	hs.basic:dcsj
	hs.basic:dwhb
	hs.basic:hs
	hs.basic:jh
	hs.basic:lryj
	hs.basic:sll
	hs.basic:slml
	hs.basic:xhs
	hs.basic:xss
	hs.basic:yhs
	hs.basic:ympx
	hs.basic:yxcz
	hs.basic:yxyj
	hs.basic:zj
	hs.basic:zlzc
	hs.basic:zzs
	~hs.basic:flgs
	~hs.basic:hf
	~hs.basic:lok
	~hs.basic:ms
	*/
	public static final Map<String,CardInfo> DEFAULT_LIBRARY;
	static
	{
		DEFAULT_LIBRARY=new HashMap<String,CardInfo>();
		CardCreator cc=new CardCreator("hs.basic");
		CardInfo ci;
		ci=cc.name("hpdruid").hide().clz(Clz.DRUID).type(Type.SKILL).cost(2)
			.function(new CardInfoAdapter()
			{
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					Card hero=player.getHero();
					player.gainArmor(1);
					hero.gainBuff(new ThisTurnPPBuffInfo(null,null,1,0),"",null);
				}
			}).create();
		register(ci);
		register(cc.name("hdruid").hide().clz(Clz.DRUID).type(Type.HERO).cannotPlay().HP(30)
			.skill(ci).create());
		register(cc.name("yhs").clz(Clz.DRUID).type(Type.SPELL).function(new DamageSpellCard(1)).create());
		register(cc.name("jh").clz(Clz.DRUID).type(Type.SPELL)
			.function(new CardInfoAdapter()
			{
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					player.gainEmptyCoins(1,true);
					player.fillCoins(1);
				}
			}).create());
		register(cc.name("zj").clz(Clz.DRUID).type(Type.SPELL).cost(1)
			.function(new CardInfoAdapter()
			{
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					Card hero=player.getHero();
					player.gainArmor(2);
					hero.gainBuff(new ThisTurnPPBuffInfo(null,null,2,0),"",null);
				}
			}).create());
		register(cc.name("yxyj").clz(Clz.DRUID).type(Type.SPELL).cost(2)
			.function(new CardInfoAdapter()
			{
				@Override public boolean canTarget(Card card,Player player,Card target,int choi)
				{
					return target!=null&&target.getPosition()==Card.Position.MINION;
				}
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					target.gainBuff(new BuffInfo(new KeyWord[]{KeyWord.TAUNT},null,false),"hs.basic:yxyj",null);
					target.pp(2,2,true);
				}
			}).create());
		register(cc.name("zlzc").clz(Clz.DRUID).type(Type.SPELL).cost(3)
			.function(new CardInfoAdapter()
			{
				@Override public boolean canTarget(Card card,Player player,Card target,int choi)
				{
					return target!=null;
				}
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					target.restoreHealth(card,8);
				}
			}).create());
		register(cc.name("yxcz").clz(Clz.DRUID).type(Type.SPELL).cost(3)
			.function(new CardInfoAdapter()
			{
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					if(!player.gainEmptyCoins(1,false))player.obtain(player.getGame().createCard("~hs.basic:flgs",-1));
				}
			}).create());
		register(cc.name("flgs").hide().clz(Clz.DRUID).type(Type.SPELL)
			.function(new CardInfoAdapter()
			{
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					player.draw(1);
				}
			}).create());
		register(cc.name("ympx").clz(Clz.DRUID).type(Type.SPELL).cost(3)
			.function(new CardInfoAdapter()
			{
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					Card hero=player.getHero();
					BuffInfo bi = new ThisTurnPPBuffInfo(null,null,2,0);
					hero.gainBuff(bi,"",null);
					for(Card c:player.getField())if(c.positionIsMinionOrHero())c.gainBuff(bi,"hs.basic:ympx",null);
				}
			}).create());
		register(cc.name("hs").clz(Clz.DRUID).type(Type.SPELL).cost(4)
			.function(new DamageSpellCard(4)
			{
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					Player tarp=target.getOwner();
					super.doBattlecry(card,player,target,choi);
					player.getGame().forEachCardOnTable(c->{if(c!=target&&c.getOwner()==tarp&&c.positionIsMinionOrHero())c.takeDamage(card,1);});
				}
			}).create());
		register(cc.name("xhs").clz(Clz.DRUID).type(Type.SPELL).cost(6)
			.function(new DamageSpellCard(5)
			{
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					super.doBattlecry(card,player,target,choi);
					player.draw(1);
				}
			}).create());
		register(cc.name("albkbhz").clz(Clz.DRUID).type(Type.MINION).stature(8,8,8).buffs(new BuffInfo(new KeyWord[]{KeyWord.TAUNT},null,false)).create());
		ci=cc.name("hphunter").hide().clz(Clz.HUNTER).type(Type.SKILL).cost(2)
			.function(new CardInfoAdapter()
			{
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					player.getNextPlayer().getHero().takeDamage(card,2);
				}
			}).create();
		register(ci);
		register(cc.name("hhunter").hide().clz(Clz.HUNTER).type(Type.HERO).cannotPlay().HP(30).skill(ci).create());
		register(cc.name("assj").clz(Clz.HUNTER).type(Type.SPELL).cost(1).function(new DamageSpellCard(2)).create());
		register(cc.name("sll").clz(Clz.HUNTER).type(Type.MINION).races(Race.BEAST).stature(1,1,1)
			.buffs(new MyOtherMinionBuffInfo(null,new PPEffectBuffInfo(null,1,0),"hs.basic:sll")
			{
				@Override protected boolean filter(Buff buff,Card card)
				{
					return super.filter(buff,card)&&card.hasRace(Race.BEAST);
				}
			}).create());
		register(cc.name("zzs").clz(Clz.HUNTER).type(Type.SPELL).cost(1)
			.function(new CardInfoAdapter()
			{
				@Override public void doBattlecry(Card card,Player player,Card target,int choi) throws GameOverThrowable
				{
					Card[] top=player.popDeck(3);
					if(top!=null)
					{
						int ind=player.askForDiscover(top);
						player.obtain(top[ind]);
					}
				}
			}).create());
		register(cc.name("lryj").clz(Clz.HUNTER).type(Type.SPELL).cost(2)
			.function(new CardInfoAdapter()
			{
				@Override public boolean canTarget(Card card,Player player,Card target,int choi)
				{
					return target!=null&&target.getPosition()==Position.MINION;
				}
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					target.setHP(1);
				}
			}).create());
		register(cc.name("dwhb").clz(Clz.HUNTER).type(Type.SPELL).cost(3)
			.function(new CardInfoAdapter()
			{
				final String[] choices=new String[]{"~hs.basic:hf","~hs.basic:ms","~hs.basic:lok"};
				@Override public boolean canTarget(Card card,Player player,Card target,int choi)
				{
					return player.getFieldNum()<player.getGame().getRule().maxField;
				}
				@Override public void doBattlecry(Card card,Player player,Card target,int choi) throws GameOverThrowable
				{
					Game game=player.getGame();
					int who=(int)(Math.random()*3);
					player.summon(game.createCard(choices[who],-1));
				}
			}).create());
		register(cc.name("hf").hide().clz(Clz.HUNTER).type(Type.MINION).races(Race.BEAST).stature(3,4,2).buffs(new BuffInfo(new KeyWord[]{KeyWord.CHARGE},null,false)).create());
		register(cc.name("ms").hide().clz(Clz.HUNTER).type(Type.MINION).races(Race.BEAST).stature(3,4,4).buffs(new BuffInfo(new KeyWord[]{KeyWord.TAUNT},null,false)).create());
		register(cc.name("lok").hide().clz(Clz.HUNTER).type(Type.MINION).races(Race.BEAST).stature(3,2,4).buffs(new MyOtherMinionBuffInfo(null,new PPEffectBuffInfo(null,1,0),"~hs.basic:lok")).create());
		register(cc.name("slml").clz(Clz.HUNTER).type(Type.SPELL).cost(3)
			.function(new DamageSpellCard(3)
			{
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					for(Card c:player.getField())
						if(c.getPosition()==Position.MINION&&c.hasRace(Race.BEAST))
						{
							target.takeDamage(card,5);
							return;
						}
					super.doBattlecry(card,player,target,choi);
				}
			}).create());
		register(cc.name("dcsj").clz(Clz.HUNTER).type(Type.SPELL).cost(4)
			.function(new CardInfoAdapter()
			{
				@Override public boolean canTarget(Card card,Player player,Card target,int choi)
				{
					return target==null&&player.getNextPlayer().getMinionNum()<2;
				}
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					List<Card> minions=player.getNextPlayer().getAllMinions();
					int len=minions.size();
					if(len>2)len=2;
					for(;len>0;len--)minions.remove((int)(Math.random()*len)).takeDamage(card,3);
				}
			}).create());
		register(cc.name("xss").clz(Clz.HUNTER).type(Type.MINION).stature(4,4,3)
			.function(new CardInfoAdapter()
			{
				@Override public boolean canTarget(Card card,Player player,Card target,int choi)
				{
					HashSet<Card> cs=new HashSet<>();
					for(Card c:player.getField())
						if(c.getPosition()==Position.MINION&&c.hasRace(Race.BEAST))
							cs.add(c);
					return cs.isEmpty()?target==null:cs.contains(target);
				}
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					if(target!=null)
					{
						target.pp(2,2,true);
						target.gainBuff(new BuffInfo(new KeyWord[]{KeyWord.TAUNT},null,false),"hs.basic:xss",null);
					}
				}
			}).create());
		register(cc.name("tyxn").clz(Clz.HUNTER).type(Type.MINION).races(Race.BEAST).stature(5,2,5)
			.buffs(new MyMinionBuffInfo(null,new BuffInfo(new KeyWord[]{KeyWord.CHARGE},null,true),"hs.basic:tyxn")
			{
				@Override protected boolean filter(Buff buff,Card card)
				{
					return super.filter(buff,card)&&card.hasRace(Race.BEAST);
				}
			}).create());
		register(cc.name("jedtj").clz(Clz.HUNTER).type(Type.MINION).races(Race.BEAST).stature(5,3,2)
			.buffs(new BuffInfo(null,new Object[]{SummonEvent.class},false)
			{
				@Override public void onTrigger(Buff buff,Event event)
				{
					Card toSummon=((SummonEvent)event).minion;
					if(toSummon==buff.toBuff)return;
					Player player=buff.toBuff.getOwner();
					if(toSummon.getOwner()==player&&toSummon.hasRace(Race.BEAST))
						player.draw(1);
				}
			}).create());
		register(cc.name("hmage").hide().clz(Clz.MAGE).type(Type.HERO).cannotPlay().HP(30).create()); //? skill
	}
}
