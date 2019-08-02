package sunshine.cg2.core.library;

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
import sunshine.cg2.core.game.event.DeathrattleEvent;
import sunshine.cg2.core.game.event.Event;
import sunshine.cg2.core.game.event.GainBuffEvent;
import sunshine.cg2.core.game.event.LoseBuffEvent;
import sunshine.cg2.core.game.event.globalevent.AfterTurnEndEvent;
import sunshine.cg2.core.game.event.globalevent.AttackingEvent;
import sunshine.cg2.core.game.event.globalevent.DamagedEvent;
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
		
		public TableBuffInfo(BuffInfo effectInfo,String effectName,KeyWord... keyWords)
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
		public MyMinionBuffInfo(BuffInfo effectInfo,String effectName,KeyWord... keyWords)
		{
			super(effectInfo,effectName,keyWords);
		}
		
		@Override
		protected boolean filter(Buff buff,Card card)
		{
			return card.getPosition()==Position.MINION&&card.getOwner()==buff.toBuff.getOwner();
		}
	}
	
	public static class MyOtherMinionBuffInfo extends MyMinionBuffInfo
	{
		public MyOtherMinionBuffInfo(BuffInfo effectInfo,String effectName,KeyWord... keyWords)
		{
			super(effectInfo,effectName,keyWords);
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
		
		public PPEffectBuffInfo(int atk,int HP,KeyWord... keyWords)
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
		public ThisTurnBuffInfo(KeyWord... keyWords)
		{
			super(keyWords,new Object[]{AfterTurnEndEvent.class},false);
		}
		
		@Override
		public void onTrigger(Buff buff,Event event)
		{
			buff.toBuff.loseBuff(buff);
		}
	}
	
	public static class ThisTurnPPBuffInfo extends BuffInfo
	{
		private final int atk;
		private final int HP;
		
		public ThisTurnPPBuffInfo(int atk,int HP,KeyWord... keyWords)
		{
			super(keyWords,new Object[]{AfterTurnEndEvent.class,GainBuffEvent.class,LoseBuffEvent.class},false);
			this.atk = atk;
			this.HP = HP;
		}
		
		@Override
		public void onTrigger(Buff buff,Event event)
		{
			if(event instanceof AfterTurnEndEvent)buff.toBuff.loseBuff(buff);
			else if(event instanceof GainBuffEvent)buff.toBuff.pp(atk,HP,false);
			else buff.toBuff.pp(-atk,-HP,false);
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
	
	public static abstract class DeathrattleBuffInfo extends BuffInfo
	{
		public DeathrattleBuffInfo()
		{
			super(new KeyWord[]{KeyWord.DEATHRATTLE},new Object[]{DeathrattleEvent.class},false);
		}
		
		@Override
		public void onTrigger(Buff buff,Event event)
		{
			doDeathrattle(buff.toBuff);
		}
		
		protected abstract void doDeathrattle(Card card);
	}
	
	public static class DamageCard extends CardInfoAdapter
	{
		private int damage;
		
		public DamageCard(int damage)
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
	
	public static class HealingCard extends CardInfoAdapter
	{
		private int num;
		
		public HealingCard(int num)
		{
			this.num=num;
		}
		
		@Override
		public boolean canTarget(Card card,Player player,Card target,int choi)
		{
			return target!=null;
		}
		
		@Override
		public void doBattlecry(Card card,Player player,Card target,int choi)
		{
			target.heal(card,num);
		}
	}
	
	private Cards()
	{
	}
	
	private static void register(CardInfo card)
	{
		DEFAULT_LIBRARY.put(card.name,card);
	}
	
	public static final BuffInfo battlecry=new BuffInfo(new KeyWord[]{KeyWord.BATTLECRY},null,false);
	
	public static final BuffInfo poisonous=new BuffInfo(new KeyWord[]{KeyWord.POISONOUS},new Object[]{DamagedEvent.class},false)
	{
		@Override public void onTrigger(Buff buff,Event event)
		{
			DamagedEvent e=(DamagedEvent)event;
			if(e.from==buff.toBuff)e.to.kill();
		}
	};
	
	public static final BuffInfo freezing=new BuffInfo(null,new Object[]{DamagedEvent.class},false)
	{
		@Override public void onTrigger(Buff buff,Event event)
		{
			DamagedEvent e=(DamagedEvent)event;
			if(e.from==buff.toBuff)e.to.freeze();
		}
	};
	
	/*
	hs.basic:albkbhz
	hs.basic:asfd
	hs.basic:assj
	hs.basic:aszh
	hs.basic:bhzs
	hs.basic:bxs
	hs.basic:dcsj
	hs.basic:dwhb
	hs.basic:fnzc
	hs.basic:fx
	hs.basic:hbj
	hs.basic:hqs
	hs.basic:hs
	hs.basic:jh
	hs.basic:jx
	hs.basic:llzf
	hs.basic:lryj
	hs.basic:lwsw
	hs.basic:lyfb
	hs.basic:mbs
	hs.basic:qx
	hs.basic:sgdzy
	hs.basic:sgs
	hs.basic:sll
	hs.basic:slml
	hs.basic:sys
	hs.basic:wzzf
	hs.basic:xhs
	hs.basic:xss
	hs.basic:yhs
	hs.basic:ympx
	hs.basic:yxcz
	hs.basic:yxyj
	hs.basic:zj
	hs.basic:zlzc
	hs.basic:zysj
	hs.basic:zzs
	~hs.basic:byzsxb
	~hs.basic:flgs
	~hs.basic:hdruid
	~hs.basic:hf
	~hs.basic:hhunter
	~hs.basic:hmage
	~hs.basic:hpaladin
	~hs.basic:hpdruid
	~hs.basic:hphunter
	~hs.basic:hpmage
	~hs.basic:hppaladin
	~hs.basic:hppriest
	~hs.basic:hpriest
	~hs.basic:jx
	~hs.basic:lok
	~hs.basic:my
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
					player.gainArmor(1);
					player.getHero().gainBuff(new ThisTurnPPBuffInfo(1,0),"",null);
				}
			}).create();
		register(ci);
		register(cc.name("hdruid").hide().clz(Clz.DRUID).type(Type.HERO).cannotPlay().HP(30)
			.skill(ci).create());
		register(cc.name("yhs").clz(Clz.DRUID).type(Type.SPELL).function(new DamageCard(1)).create());
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
					player.gainArmor(2);
					player.getHero().gainBuff(new ThisTurnPPBuffInfo(2,0),"",null);
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
		register(cc.name("zlzc").clz(Clz.DRUID).type(Type.SPELL).cost(3).function(new HealingCard(8)).create());
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
					BuffInfo bi = new ThisTurnPPBuffInfo(2,0);
					player.getHero().gainBuff(bi,"",null);
					for(Card c:player.getField())if(c.positionIsMinionOrHero())c.gainBuff(bi,"hs.basic:ympx",null);
				}
			}).create());
		register(cc.name("hs").clz(Clz.DRUID).type(Type.SPELL).cost(4)
			.function(new DamageCard(4)
			{
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					Player tarp=target.getOwner();
					super.doBattlecry(card,player,target,choi);
					tarp.getHero().takeDamageWithoutCheck(card,1);
					List<Card> minions=tarp.getAllMinions();
					for (Card c:minions)if(c!=target)c.takeDamageWithoutCheck(card,1);
					player.getGame().checkForDamage();
				}
			}).create());
		register(cc.name("xhs").clz(Clz.DRUID).type(Type.SPELL).cost(6)
			.function(new DamageCard(5)
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
		register(cc.name("assj").clz(Clz.HUNTER).type(Type.SPELL).cost(1).function(new DamageCard(2)).create());
		register(cc.name("sll").clz(Clz.HUNTER).type(Type.MINION).races(Race.BEAST).stature(1,1,1)
			.buffs(new MyOtherMinionBuffInfo(new PPEffectBuffInfo(1,0),"hs.basic:sll")
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
					return target==null&&player.getFieldNum()<player.getGame().getRule().maxField;
				}
				@Override public void doBattlecry(Card card,Player player,Card target,int choi) throws GameOverThrowable
				{
					int who=(int)(Math.random()*3);
					player.summon(player.getGame().createCard(choices[who],-1));
				}
			}).create());
		register(cc.name("hf").hide().clz(Clz.HUNTER).type(Type.MINION).races(Race.BEAST).stature(3,4,2).buffs(new BuffInfo(new KeyWord[]{KeyWord.CHARGE},null,false)).create());
		register(cc.name("ms").hide().clz(Clz.HUNTER).type(Type.MINION).races(Race.BEAST).stature(3,4,4).buffs(new BuffInfo(new KeyWord[]{KeyWord.TAUNT},null,false)).create());
		register(cc.name("lok").hide().clz(Clz.HUNTER).type(Type.MINION).races(Race.BEAST).stature(3,2,4).buffs(new MyOtherMinionBuffInfo(new PPEffectBuffInfo(1,0),"~hs.basic:lok")).create());
		register(cc.name("slml").clz(Clz.HUNTER).type(Type.SPELL).cost(3)
			.function(new DamageCard(3)
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
					if(minions.size()>=2)for(int len=2;len>0;len--)minions.remove((int)(Math.random()*len)).takeDamageWithoutCheck(card,3);
					player.getGame().checkForDamage();
				}
			}).create());
		register(cc.name("xss").clz(Clz.HUNTER).type(Type.MINION).stature(4,4,3).buffs(battlecry)
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
			.buffs(new MyMinionBuffInfo(new BuffInfo(new KeyWord[]{KeyWord.CHARGE},null,true),"hs.basic:tyxn")
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
		ci=cc.name("hpmage").hide().clz(Clz.MAGE).type(Type.SKILL).cost(2).function(new DamageCard(1)).create();
		register(ci);
		register(cc.name("hmage").hide().clz(Clz.MAGE).type(Type.HERO).cannotPlay().HP(30).skill(ci).create());
		register(cc.name("asfd").clz(Clz.MAGE).type(Type.SPELL).cost(1)
			.function(new CardInfoAdapter()
			{
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					Player next=player.getNextPlayer();
					for(int i=0;i<3;i++)
					{
						List<Card> chars=next.getAliveMinions();
						if(!next.getHero().isDying())chars.add(next.getHero());
						int len=chars.size();
						if(len<=0)break;
						int who=(int)(Math.random()*len);
						chars.get(who).takeDamage(card,1);
					}
				}
			}).create());
		register(cc.name("jx").clz(Clz.MAGE).type(Type.SPELL).cost(1)
			.function(new CardInfoAdapter()
			{
				@Override public boolean canTarget(Card card,Player player,Card target,int choi)
				{
					return target==null&&player.getFieldNum()+2<=player.getGame().getRule().maxField;
				}
				@Override public void doBattlecry(Card card,Player player,Card target,int choi) throws GameOverThrowable
				{
					Game game=player.getGame();
					for(int i=0;i<2;i++)player.summon(game.createCard("~hs.basic:jx",-1));
				}
			}).create());
		register(cc.name("jx").hide().clz(Clz.MAGE).type(Type.MINION).stature(1,0,2).buffs(new BuffInfo(new KeyWord[]{KeyWord.TAUNT},null,false)).create());
		register(cc.name("hbj").clz(Clz.MAGE).type(Type.SPELL).cost(2)
			.function(new DamageCard(3)
			{
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					super.doBattlecry(card,player,target,choi);
					target.freeze();
				}
			}).create());
		register(cc.name("mbs").clz(Clz.MAGE).type(Type.SPELL).cost(2)
			.function(new CardInfoAdapter()
			{
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					player.getNextPlayer().getAllMinions().forEach(c->c.takeDamageWithoutCheck(card,1));
					player.getGame().checkForDamage();
				}
			}).create());
		register(cc.name("bsxx").clz(Clz.MAGE).type(Type.SPELL).cost(3)
			.function(new CardInfoAdapter()
			{
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					player.getNextPlayer().getAllMinions().forEach(c->c.freeze());
				}
			}).create());
		register(cc.name("aszh").clz(Clz.MAGE).type(Type.SPELL).cost(3)
			.function(new CardInfoAdapter()
			{
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					player.draw(2);
				}
			}).create());
		register(cc.name("bxs").clz(Clz.MAGE).type(Type.SPELL).cost(4)
			.function(new CardInfoAdapter()
			{
				@Override public boolean canTarget(Card card,Player player,Card target,int choi)
				{
					return target!=null&&target.getPosition()==Position.MINION;
				}
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					target.transformField(player.getGame().createCard("~hs.basic:my",-1),false);
				}
			}).create());
		register(cc.name("my").hide().clz(Clz.NONE).type(Type.MINION).stature(1,1,1).create());
		register(cc.name("sys").clz(Clz.MAGE).type(Type.MINION).races(Race.ELEMENT).stature(4,3,6)
			.buffs(freezing).create());
		register(cc.name("hqs").clz(Clz.MAGE).type(Type.SPELL).cost(4).function(new DamageCard(6)).create());
		register(cc.name("lyfb").clz(Clz.MAGE).type(Type.SPELL).cost(7)
				.function(new CardInfoAdapter()
				{
					@Override public void doBattlecry(Card card,Player player,Card target,int choi)
					{
						player.getNextPlayer().getAllMinions().forEach(c->c.takeDamageWithoutCheck(card,4));
						player.getGame().checkForDamage();
					}
				}).create());
		ci=cc.name("hppaladin").hide().clz(Clz.PALADIN).type(Type.SKILL).cost(2)
			.function(new CardInfoAdapter()
			{
				@Override public boolean canTarget(Card card,Player player,Card target,int choi)
				{
					return target==null&&player.getFieldNum()<player.getGame().getRule().maxField;
				}
				@Override public void doBattlecry(Card card,Player player,Card target,int choi) throws GameOverThrowable
				{
					player.summon(player.getGame().createCard("~hs.basic:byzsxb",-1));
				}
			}).create();
		register(cc.name("byzsxb").hide().clz(Clz.PALADIN).type(Type.MINION).stature(1,1,1).create());
		register(cc.name("hpaladin").hide().clz(Clz.PALADIN).type(Type.HERO).cannotPlay().HP(30).skill(ci).create());
		register(cc.name("bhzs").clz(Clz.PALADIN).type(Type.SPELL).cost(1)
			.function(new CardInfoAdapter()
			{
				@Override public boolean canTarget(Card card,Player player,Card target,int choi)
				{
					return target!=null&&target.getPosition()==Position.MINION;
				}
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					target.gainShield();
				}
			}).create());
		register(cc.name("llzf").clz(Clz.PALADIN).type(Type.SPELL).cost(1)
			.function(new CardInfoAdapter()
			{
				@Override public boolean canTarget(Card card,Player player,Card target,int choi)
				{
					return target!=null&&target.getPosition()==Position.MINION;
				}
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					target.pp(3,0,true);
				}
			}).create());
		register(cc.name("sgdzy").clz(Clz.PALADIN).type(Type.WEAPON).stature(1,1,4).create());
		register(cc.name("qx").clz(Clz.PALADIN).type(Type.SPELL).cost(1)
				.function(new CardInfoAdapter()
				{
					@Override public boolean canTarget(Card card,Player player,Card target,int choi)
					{
						return target!=null&&target.getPosition()==Position.MINION;
					}
					@Override public void doBattlecry(Card card,Player player,Card target,int choi)
					{
						target.setAtk(1);
					}
				}).create());
		register(cc.name("sgs").clz(Clz.PALADIN).type(Type.SPELL).cost(2).function(new HealingCard(6)).create());
		register(cc.name("fx").clz(Clz.PALADIN).type(Type.SPELL).cost(4)
			.function(new CardInfoAdapter()
			{
				@Override public boolean canTarget(Card card,Player player,Card target,int choi)
				{
					return target==null;
				}
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					Player nextPlayer=player.getNextPlayer();
					nextPlayer.getHero().takeDamageWithoutCheck(card,2);
					nextPlayer.getAllMinions().forEach(c->c.takeDamageWithoutCheck(card,2));
					player.getGame().checkForDamage();
				}
			}).create());
		register(cc.name("fnzc").clz(Clz.PALADIN).type(Type.SPELL).cost(4)
			.function(new DamageCard(3)
			{
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					super.doBattlecry(card,player,target,choi);
					player.draw(1);
				}
			}).create());
		register(cc.name("wzzf").clz(Clz.PALADIN).type(Type.SPELL).cost(4)
			.function(new CardInfoAdapter()
			{
				@Override public boolean canTarget(Card card,Player player,Card target,int choi)
				{
					return target!=null&&target.getPosition()==Position.MINION;
				}
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					target.pp(4,4,true);
				}
			}).create());
		register(cc.name("zysj").clz(Clz.PALADIN).type(Type.WEAPON).stature(4,4,2)
			.buffs(new BuffInfo(null,new Object[]{AttackingEvent.class},false)
			{
				@Override public void onTrigger(Buff buff,Event event)
				{
					AttackingEvent e=(AttackingEvent)event;
					if(e.from==buff.toBuff.getOwner().getHero())e.from.heal(buff.toBuff,2);
				}
			}).create());
		register(cc.name("lwsw").clz(Clz.PALADIN).type(Type.MINION).stature(7,5,6).buffs(battlecry)
			.function(new CardInfoAdapter()
			{
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					player.getHero().heal(card,6);
				}
			}).create());
		ci=cc.name("hppriest").hide().clz(Clz.PRIEST).type(Type.SKILL).cost(2).function(new HealingCard(2)).create();
		register(cc.name("hpriest").hide().clz(Clz.PRIEST).type(Type.HERO).cannotPlay().HP(30).skill(ci).create());
	}
}
