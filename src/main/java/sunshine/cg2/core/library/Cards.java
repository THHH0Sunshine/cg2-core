package sunshine.cg2.core.library;

import java.util.ArrayList;
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
import sunshine.cg2.core.game.event.globalevent.HealedEvent;
import sunshine.cg2.core.game.event.globalevent.LeaveTableEvent;
import sunshine.cg2.core.game.event.globalevent.LeaveTableEvent.Reason;
import sunshine.cg2.core.game.event.globalevent.SummonEvent;
import sunshine.cg2.core.game.event.globalevent.TurnEndEvent;
import sunshine.cg2.core.game.event.globalevent.TurnStartEvent;
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
			else
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
	
	public static class NearbyMinionBuffInfo extends MyMinionBuffInfo
	{
		public NearbyMinionBuffInfo(BuffInfo effectInfo,String effectName,KeyWord... keyWords)
		{
			super(effectInfo,effectName,keyWords);
		}
		
		@Override
		protected boolean filter(Buff buff,Card card)
		{
			List<Card> field=buff.toBuff.getOwner().getField();
			int ind=field.indexOf(buff.toBuff);
			if(ind<0)return false;
			int cind=field.indexOf(card);
			return cind>=0&&(cind==ind-1||cind==ind+1);
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
			if(event instanceof GainBuffEvent)buff.toBuff.ppNoSilence(atk,HP);
			else buff.toBuff.ppNoSilence(-atk,-HP);
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
			else if(event instanceof GainBuffEvent)buff.toBuff.ppNoSilence(atk,HP);
			else buff.toBuff.ppNoSilence(-atk,-HP);
		}
	}
	
	public static class DKBuffInfo extends BuffInfo
	{
		private final int armor;
		
		public DKBuffInfo(int armor)
		{
			super(null,new Object[]{EnterTableEvent.class},false);
			this.armor=armor;
		}
		
		@Override
		public void onTrigger(Buff buff,Event event)
		{
			buff.toBuff.getOwner().gainArmor(armor);
		}
	}
	
	public static class KWBuffInfo extends BuffInfo
	{
		public KWBuffInfo(KeyWord... keyWords)
		{
			super(keyWords,null,false);
		}
	}
	
	public static class KWEffectBuffInfo extends BuffInfo
	{
		public KWEffectBuffInfo(KeyWord... keyWords)
		{
			super(keyWords,null,true);
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
	
	public static class SpellPowerBuffInfo extends BuffInfo
	{
		private final int num;
		
		public SpellPowerBuffInfo(int num)
		{
			super(new KeyWord[]{KeyWord.SPELLPOWER},new Object[]{EnterTableEvent.class,GainBuffEvent.class,LeaveTableEvent.class,LoseBuffEvent.class},false);
			this.num=num;
		}
		
		@Override public void onTrigger(Buff buff,Event event)
		{
			Player player=buff.toBuff.getOwner();
			if(event instanceof EnterTableEvent||event instanceof GainBuffEvent)player.addSpellPower(num);
			else player.addSpellPower(-num);
		}
	};
	
	public static class DamageCard extends CardInfoAdapter
	{
		private final int damage;
		
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
		private final int num;
		
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
	
	public static final BuffInfo battlecry=new KWBuffInfo(KeyWord.BATTLECRY);
	
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
	
	public static final BuffInfo corrupted=new BuffInfo(null,new Object[]{TurnStartEvent.class},false)
	{
		@Override public void onTrigger(Buff buff,Event event)
		{
			TurnStartEvent e=(TurnStartEvent)event;
			if(e.player!=buff.toBuff.getOwner())buff.toBuff.kill();
		}
	};
	
	public static final String[] basicTotems=new String[]{"~hs.basic:zrtt","~hs.basic:kqzntt","~hs.basic:zltt","~hs.basic:sztt"};
	
	/*
	hs.basic:ajf
	hs.basic:albkbhz
	hs.basic:asfd
	hs.basic:assj
	hs.basic:aszh
	hs.basic:ayj
	hs.basic:aysm
	hs.basic:ayst
	hs.basic:bc
	hs.basic:bhzs
	hs.basic:bjms
	hs.basic:bszj
	hs.basic:bxs
	hs.basic:cf
	hs.basic:ckzr
	hs.basic:cs
	hs.basic:cyzf
	hs.basic:dcsj
	hs.basic:dpgd
	hs.basic:dr
	hs.basic:ds
	hs.basic:dwhb
	hs.basic:dyly
	hs.basic:fn
	hs.basic:fnzc
	hs.basic:fss
	hs.basic:fx
	hs.basic:fyz
	hs.basic:hbj
	hs.basic:hqs
	hs.basic:hs
	hs.basic:hstt
	hs.basic:hys
	hs.basic:jh
	hs.basic:jp
	hs.basic:jskz
	hs.basic:jx
	hs.basic:kjdyh
	hs.basic:kkljyws
	hs.basic:lhzh
	hs.basic:llzf
	hs.basic:lryj
	hs.basic:lwsw
	hs.basic:lyfb
	hs.basic:mbs
	hs.basic:mg
	hs.basic:mq
	hs.basic:qx
	hs.basic:sgdzy
	hs.basic:sgs
	hs.basic:sgsy
	hs.basic:shwq
	hs.basic:sll
	hs.basic:slml
	hs.basic:spz
	hs.basic:sscj
	hs.basic:ssxx
	hs.basic:sszl
	hs.basic:swcr
	hs.basic:sx
	hs.basic:sys
	hs.basic:ttzl
	hs.basic:wysz
	hs.basic:wzzf
	hs.basic:xfz
	hs.basic:xhs
	hs.basic:xkxz
	hs.basic:xlsj
	hs.basic:xqsm
	hs.basic:xsqy
	hs.basic:xss
	hs.basic:xzzl
	hs.basic:yhs
	hs.basic:ympx
	hs.basic:ys
	hs.basic:yx
	hs.basic:yxcz
	hs.basic:yxyj
	hs.basic:yydj
	hs.basic:zgzhg
	hs.basic:zj
	hs.basic:zlzc
	hs.basic:zmyg
	hs.basic:zs
	hs.basic:zysd
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
	~hs.basic:hprogue
	~hs.basic:hpshaman
	~hs.basic:hpwarlock
	~hs.basic:hpwarrior
	~hs.basic:hrogue
	~hs.basic:hshaman
	~hs.basic:hwarlock
	~hs.basic:hwarrior
	~hs.basic:jx
	~hs.basic:lok
	~hs.basic:my
	~hs.basic:ms
	~hs.basic:qw
	~hs.basic:xedd
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
					target.gainBuff(new KWBuffInfo(KeyWord.TAUNT),"hs.basic:yxyj",null);
					target.pp(2,2);
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
					player.getHero().gainBuff(bi,"hs.basic:ympx",null);
					for(Card c:player.getField())if(c.getPosition()==Position.MINION)c.gainBuff(bi,"hs.basic:ympx",null);
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
		register(cc.name("albkbhz").clz(Clz.DRUID).type(Type.MINION).stature(8,8,8).buffs(new KWBuffInfo(KeyWord.TAUNT)).create());
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
		register(cc.name("hf").hide().clz(Clz.HUNTER).type(Type.MINION).races(Race.BEAST).stature(3,4,2).buffs(new KWBuffInfo(KeyWord.CHARGE)).create());
		register(cc.name("ms").hide().clz(Clz.HUNTER).type(Type.MINION).races(Race.BEAST).stature(3,4,4).buffs(new KWBuffInfo(KeyWord.TAUNT)).create());
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
						target.pp(2,2);
						target.gainBuff(new KWBuffInfo(KeyWord.TAUNT),"hs.basic:xss",null);
					}
				}
			}).create());
		register(cc.name("tyxn").clz(Clz.HUNTER).type(Type.MINION).races(Race.BEAST).stature(5,2,5)
			.buffs(new MyMinionBuffInfo(new KWEffectBuffInfo(KeyWord.CHARGE),"hs.basic:tyxn")
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
		register(cc.name("jx").hide().clz(Clz.MAGE).type(Type.MINION).stature(1,0,2).buffs(new KWBuffInfo(KeyWord.TAUNT)).create());
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
					target.pp(3,0);
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
					target.pp(4,4);
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
		register(cc.name("bjms").clz(Clz.PRIEST).type(Type.MINION).stature(1,1,3)
			.buffs(new BuffInfo(null,new Object[]{HealedEvent.class},false)
			{
				@Override public void onTrigger(Buff buff,Event event)
				{
					HealedEvent e=(HealedEvent)event;
					if(e.to.getPosition()==Position.MINION)buff.toBuff.getOwner().draw(1);
				}
			}).create());
		register(cc.name("sgsy").clz(Clz.PRIEST).type(Type.SPELL).cost(1)
			.function(new CardInfoAdapter()
			{
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					player.getHero().heal(card,5);
				}
			}).create());
		register(cc.name("xlsj").clz(Clz.PRIEST).type(Type.SPELL).cost(1)
			.function(new CardInfoAdapter()
			{
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					List<Card> hand=player.getNextPlayer().getHand();
					int len=hand.size();
					if(len<=0)return;
					player.obtain(hand.get((int)(Math.random()*len)).getHandCopy());
				}
			}).create());
		register(cc.name("zysd").clz(Clz.PRIEST).type(Type.SPELL).cost(1)
			.function(new CardInfoAdapter()
			{
				@Override public boolean canTarget(Card card,Player player,Card target,int choi)
				{
					return target!=null&&target.getPosition()==Position.MINION;
				}
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					target.pp(0,2);
					player.draw(1);
				}
			}).create());
		register(cc.name("sscj").clz(Clz.PRIEST).type(Type.SPELL).cost(1).function(new DamageCard(2)).create());
		register(cc.name("ayst").clz(Clz.PRIEST).type(Type.SPELL).cost(2)
			.function(new CardInfoAdapter()
			{
				@Override public boolean canTarget(Card card,Player player,Card target,int choi)
				{
					return target!=null&&target.getPosition()==Position.MINION&&target.getAtk()<=3;
				}
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					target.kill();
				}
			}).create());
		register(cc.name("sszl").clz(Clz.PRIEST).type(Type.SPELL).cost(2)
			.function(new CardInfoAdapter()
			{
				@Override public boolean canTarget(Card card,Player player,Card target,int choi)
				{
					return target!=null&&target.getPosition()==Position.MINION;
				}
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					target.setHPWithDmg(target.getHP()*2);
				}
			}).create());
		register(cc.name("aysm").clz(Clz.PRIEST).type(Type.SPELL).cost(3)
			.function(new CardInfoAdapter()
			{
				@Override public boolean canTarget(Card card,Player player,Card target,int choi)
				{
					return target!=null&&target.getPosition()==Position.MINION&&target.getAtk()>=5;
				}
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					target.kill();
				}
			}).create());
		register(cc.name("ssxx").clz(Clz.PRIEST).type(Type.SPELL).cost(5)
			.function(new CardInfoAdapter()
			{
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					Game game=player.getGame();
					Player nextPlayer=player.getNextPlayer();
					nextPlayer.getHero().takeDamageWithoutCheck(card,2);
					nextPlayer.getAllMinions().forEach(c->c.takeDamageWithoutCheck(card,2));
					game.checkForDamage();
					player.getHero().healWithoutCheck(card,2);
					player.getAllMinions().forEach(c->c.healWithoutCheck(card,2));
					game.checkForHeal();
				}
			}).create());
		register(cc.name("jskz").clz(Clz.PRIEST).type(Type.SPELL).cost(10)
			.function(new CardInfoAdapter()
			{
				@Override public boolean canTarget(Card card,Player player,Card target,int choi)
				{
					return target!=null&&target.getOwner()!=player&&target.getPosition()==Position.MINION;
				}
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					player.takeControlOfField(target);
				}
			}).create());
		ci=cc.name("hprogue").hide().clz(Clz.PRIEST).type(Type.SKILL).cost(2)
			.function(new CardInfoAdapter()
			{
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					player.equip(player.getGame().createCard("~hs.basic:bs",-1));
				}
			}).create();
		register(ci);
		register(cc.name("xedd").hide().clz(Clz.ROGUE).type(Type.WEAPON).stature(1,1,2).create());
		register(cc.name("hrogue").hide().clz(Clz.ROGUE).type(Type.HERO).cannotPlay().HP(30).skill(ci).create());
		register(cc.name("bc").clz(Clz.ROGUE).type(Type.SPELL).cost(0)
			.function(new DamageCard(2)
			{
				@Override public boolean canTarget(Card card,Player player,Card target,int choi)
				{
					return super.canTarget(card,player,target,choi)&&target.getPosition()==Position.MINION&&!target.isDamaged();
				}
			}).create());
		register(cc.name("yx").clz(Clz.ROGUE).type(Type.SPELL).cost(1)
			.function(new CardInfoAdapter()
			{
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					player.getNextPlayer().getHero().takeDamage(card,3);
				}
			}).create());
		register(cc.name("zmyg").clz(Clz.ROGUE).type(Type.SPELL).cost(1)
			.function(new CardInfoAdapter()
			{
				@Override public boolean canTarget(Card card,Player player,Card target,int choi)
				{
					return target==null&&player.getWeapon()!=null;
				}
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					player.getWeapon().pp(2,0);
				}
			}).create());
		register(cc.name("dr").clz(Clz.ROGUE).type(Type.SPELL).cost(2)
			.function(new DamageCard(1)
			{
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					super.doBattlecry(card,player,target,choi);
					player.draw(1);
				}
			}).create());
		register(cc.name("mg").clz(Clz.ROGUE).type(Type.SPELL).cost(2)
			.function(new CardInfoAdapter()
			{
				@Override public boolean canTarget(Card card,Player player,Card target,int choi)
				{
					return target!=null&&target.getOwner()!=player&&target.getPosition()==Position.MINION;
				}
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					Game game=player.getGame();
					Player p=target.getOwner();
					if(p.getHandNum()>=game.getRule().maxHand)target.kill();
					else
					{
						p.removeField(target,Reason.MOVE);
						p.obtain(game.createClear(target));
					}
				}
			}).create());
		register(cc.name("ds").clz(Clz.ROGUE).type(Type.SPELL).cost(3)
			.function(new CardInfoAdapter()
			{
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					player.getNextPlayer().getAllMinions().forEach(c->c.takeDamageWithoutCheck(card,1));
					player.getGame().checkForDamage();
					player.draw(1);
				}
			}).create());
		register(cc.name("wysz").clz(Clz.ROGUE).type(Type.MINION).stature(4,3,3).buffs(battlecry)
			.function(new CardInfoAdapter()
			{
				@Override public boolean canTarget(Card card,Player player,Card target,int choi)
				{
					return target!=null&&target.getOwner()==player&&target.getPosition()==Position.MINION;
				}
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					target.gainBuff(poisonous,"hs.basic:wysz",null);
				}
			}).create());
		register(cc.name("ckzr").clz(Clz.ROGUE).type(Type.WEAPON).stature(5,3,4).create());
		register(cc.name("cs").clz(Clz.ROGUE).type(Type.SPELL).cost(5)
			.function(new CardInfoAdapter()
			{
				@Override public boolean canTarget(Card card,Player player,Card target,int choi)
				{
					return target!=null&&target.getOwner()!=player&&target.getPosition()==Position.MINION;
				}
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					target.kill();
				}
			}).create());
		register(cc.name("jp").clz(Clz.ROGUE).type(Type.SPELL).cost(7)
			.function(new CardInfoAdapter()
			{
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					player.draw(4);
				}
			}).create());
		ci=cc.name("hpshaman").hide().clz(Clz.SHAMAN).type(Type.SKILL).cost(2)
			.function(new CardInfoAdapter()
			{
				@Override public boolean canTarget(Card card,Player player,Card target,int choi)
				{
					List<Card> field=player.getField();
					return field.size()<player.getGame().getRule().maxField&&getTotemFlagFromField(field)!=15;
				}
				@Override public void doBattlecry(Card card,Player player,Card target,int choi) throws GameOverThrowable
				{
					Game game=player.getGame();
					if(player.getFieldNum()>=game.getRule().maxField)return;
					int flag=getTotemFlagFromField(player.getField());
					if(flag==15)return;
					ArrayList<String> list=new ArrayList<>(4);
					for(int i=0;i<4;i++)if((flag&(1<<i))==0)list.add(basicTotems[i]);
					int choice=(int)(Math.random()*list.size());
					player.summon(game.createCard(list.get(choice),-1));
				}
			}).create();
		register(ci);
		register(cc.fullName(basicTotems[0]).clz(Clz.SHAMAN).type(Type.MINION).races(Race.TOTEM).stature(1,1,1).create());
		register(cc.fullName(basicTotems[1]).clz(Clz.SHAMAN).type(Type.MINION).races(Race.TOTEM).stature(1,0,2).buffs(new SpellPowerBuffInfo(1)).create());
		register(cc.fullName(basicTotems[2]).clz(Clz.SHAMAN).type(Type.MINION).races(Race.TOTEM).stature(1,0,2)
			.buffs(new BuffInfo(null,new Object[]{TurnEndEvent.class},false)
			{
				@Override public void onTrigger(Buff buff,Event event)
				{
					TurnEndEvent e=(TurnEndEvent)event;
					if(e.player==buff.toBuff.getOwner())
					{
						e.player.getAllMinions().forEach(c->c.healWithoutCheck(buff.toBuff,1));
						e.player.getGame().checkForHeal();
					}
				}
			}).create());
		register(cc.fullName(basicTotems[3]).clz(Clz.SHAMAN).type(Type.MINION).races(Race.TOTEM).stature(1,0,2).buffs(new KWBuffInfo(KeyWord.TAUNT)).create());
		register(cc.name("hshaman").hide().clz(Clz.SHAMAN).type(Type.HERO).cannotPlay().HP(30).skill(ci).create());
		register(cc.name("xzzl").clz(Clz.SHAMAN).type(Type.SPELL).cost(0)
			.function(new CardInfoAdapter()
			{
				@Override public boolean canTarget(Card card,Player player,Card target,int choi)
				{
					return target!=null&&target.getPosition()==Position.MINION;
				}
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					target.heal(card,target.getMaxHP());
					target.gainBuff(new KWBuffInfo(KeyWord.TAUNT),"hs.basic:xzzl",null);
				}
			}).create());
		register(cc.name("ttzl").clz(Clz.SHAMAN).type(Type.SPELL).cost(0)
			.function(new CardInfoAdapter()
			{
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					for(Card c:player.getAllMinions())if(c.hasRace(Race.TOTEM))c.pp(0,2);
				}
			}).create());
		register(cc.name("bszj").clz(Clz.SHAMAN).type(Type.SPELL).cost(1)
			.function(new DamageCard(1)
			{
				@Override public boolean canTarget(Card card,Player player,Card target,int choi)
				{
					return super.canTarget(card,player,target,choi)&&target.getOwner()!=player;
				}
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					super.doBattlecry(card,player,target,choi);
					target.freeze();
				}
			}).create());
		register(cc.name("shwq").clz(Clz.SHAMAN).type(Type.SPELL).cost(2)
			.function(new CardInfoAdapter()
			{
				@Override public boolean canTarget(Card card,Player player,Card target,int choi)
				{
					return target!=null&&target.getOwner()==player&&target.positionIsMinionOrHero();
				}
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					target.gainBuff(new ThisTurnPPBuffInfo(3,0),"hs.basic:shwq",null);
				}
			}).create());
		register(cc.name("fn").clz(Clz.SHAMAN).type(Type.SPELL).cost(2)
			.function(new CardInfoAdapter()
			{
				@Override public boolean canTarget(Card card,Player player,Card target,int choi)
				{
					return target!=null&&target.getPosition()==Position.MINION;
				}
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					target.gainBuff(new KWBuffInfo(KeyWord.WINDFURY),"hs.basic:fn",null);
				}
			}).create());
		register(cc.name("hstt").clz(Clz.SHAMAN).type(Type.MINION).races(Race.TOTEM).stature(3,0,3).buffs(new NearbyMinionBuffInfo(new PPEffectBuffInfo(2,0),"hs.basic:hstt")).create());
		register(cc.name("ys").clz(Clz.SHAMAN).type(Type.SPELL).cost(4)
			.function(new CardInfoAdapter()
			{
				@Override public boolean canTarget(Card card,Player player,Card target,int choi)
				{
					return target!=null&&target.getPosition()==Position.MINION;
				}
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					target.transformField(player.getGame().createCard("~hs.basic:qw",-1),false);
				}
			}).create());
		register(cc.name("qw").hide().clz(Clz.SHAMAN).type(Type.MINION).races(Race.BEAST).stature(1,0,1).buffs(new KWBuffInfo(KeyWord.TAUNT)).create());
		register(cc.name("fyz").clz(Clz.SHAMAN).type(Type.MINION).stature(4,3,3).buffs(battlecry)
			.function(new CardInfoAdapter()
			{
				@Override public boolean canTarget(Card card,Player player,Card target,int choi)
				{
					return target!=null&&target.getOwner()==player&&target.getPosition()==Position.MINION;
				}
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					target.gainBuff(new KWBuffInfo(KeyWord.WINDFURY),"hs.basic:fyz",null);
				}
			}).create());
		register(cc.name("sx").clz(Clz.SHAMAN).type(Type.SPELL).cost(5)
			.function(new CardInfoAdapter()
			{
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					BuffInfo bi = new ThisTurnPPBuffInfo(3,0);
					for(Card c:player.getField())if(c.getPosition()==Position.MINION)c.gainBuff(bi,"hs.basic:sx",null);
				}
			}).create());
		register(cc.name("hys").clz(Clz.SHAMAN).type(Type.MINION).races(Race.ELEMENT).stature(6,6,5).buffs(battlecry).function(new DamageCard(3)).create());
		ci=cc.name("hpwarlock").hide().clz(Clz.WARLOCK).type(Type.SKILL).cost(2)
			.function(new CardInfoAdapter()
			{
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					player.draw(1);
					player.getHero().takeDamage(card,2);
				}
			}).create();
		register(ci);
		register(cc.name("hwarlock").hide().clz(Clz.WARLOCK).type(Type.HERO).cannotPlay().HP(30).skill(ci).create());
		register(cc.name("xsqy").clz(Clz.WARLOCK).type(Type.SPELL).cost(0)
			.function(new CardInfoAdapter()
			{
				@Override public boolean canTarget(Card card,Player player,Card target,int choi)
				{
					return target!=null&&target.hasRace(Race.DEMON);
				}
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					target.kill();
					player.getHero().heal(card,5);
				}
			}).create());
		register(cc.name("swcr").clz(Clz.WARLOCK).type(Type.SPELL).cost(1)
			.function(new DamageCard(1)
			{
				@Override public boolean canTarget(Card card,Player player,Card target,int choi)
				{
					return super.canTarget(card,player,target,choi)&&target.getPosition()==Position.MINION;
				}
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					super.doBattlecry(card,player,target,choi);
					if(target.isDying())player.draw(1);
				}
			}).create());
		register(cc.name("lhzh").clz(Clz.WARLOCK).type(Type.SPELL).cost(1)
			.function(new DamageCard(4)
			{
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					super.doBattlecry(card,player,target,choi);
					player.discardHand(1);
				}
			}).create());
		register(cc.name("fss").clz(Clz.WARLOCK).type(Type.SPELL).cost(1)
			.function(new CardInfoAdapter()
			{
				@Override public boolean canTarget(Card card,Player player,Card target,int choi)
				{
					return target!=null&&target.getOwner()!=player&&target.getPosition()==Position.MINION;
				}
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					target.gainBuff(corrupted,"hs.basic:fss",null);
				}
			}).create());
		register(cc.name("xkxz").clz(Clz.WARLOCK).type(Type.MINION).races(Race.DEMON).stature(1,1,3).buffs(new KWBuffInfo(KeyWord.TAUNT)).create());
		register(cc.name("mq").clz(Clz.WARLOCK).type(Type.MINION).races(Race.DEMON).stature(2,4,3).buffs(battlecry)
			.function(new CardInfoAdapter()
			{
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					player.discardHand(1);
				}
			}).create());
		register(cc.name("xqsm").clz(Clz.WARLOCK).type(Type.SPELL).cost(3)
			.function(new DamageCard(2)
			{
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					super.doBattlecry(card,player,target,choi);
					player.getHero().heal(card,2);
				}
			}).create());
		register(cc.name("ayj").clz(Clz.WARLOCK).type(Type.SPELL).cost(3)
			.function(new DamageCard(4)
			{
				@Override public boolean canTarget(Card card,Player player,Card target,int choi)
				{
					return super.canTarget(card,player,target,choi)&&target.getPosition()==Position.MINION;
				}
			}).create());
		register(cc.name("dyly").clz(Clz.WARLOCK).type(Type.SPELL).cost(4)
			.function(new CardInfoAdapter()
			{
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					Game game=player.getGame();
					game.forEachCardOnTable(c->{if(c.positionIsMinionOrHero())c.takeDamageWithoutCheck(card,3);});
					game.checkForDamage();
				}
			}).create());
		register(cc.name("kjdyh").clz(Clz.WARLOCK).type(Type.MINION).races(Race.DEMON).stature(6,6,6).buffs(battlecry)
			.function(new CardInfoAdapter()
			{
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					Game game=player.getGame();
					game.forEachCardOnTable(c->{if(c!=card&&c.positionIsMinionOrHero())c.takeDamageWithoutCheck(card,1);});
					game.checkForDamage();
				}
			}).create());
		ci=cc.name("hpwarrior").hide().clz(Clz.WARRIOR).type(Type.SKILL).cost(2)
			.function(new CardInfoAdapter()
			{
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					player.gainArmor(2);
				}
			}).create();
		register(ci);
		register(cc.name("hwarrior").hide().clz(Clz.WARRIOR).type(Type.HERO).cannotPlay().HP(30).skill(ci).create());
		register(cc.name("cf").clz(Clz.WARRIOR).type(Type.SPELL).cost(1)
			.function(new CardInfoAdapter()
			{
				@Override public boolean canTarget(Card card,Player player,Card target,int choi)
				{
					return target!=null&&target.getPosition()==Position.MINION&&target.getOwner()==player;
				}
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					target.gainBuff(new KWBuffInfo(KeyWord.CHARGE),"hs.basic:cf",null);
					target.gainBuff(new ThisTurnBuffInfo()
					{
						@Override public boolean canAttack(Card card,Card target)
						{
							return target.getPosition()!=Position.HERO;
						}
					},"hs.basic:cf",null);
				}
			}).create());
		register(cc.name("xfz").clz(Clz.WARRIOR).type(Type.SPELL).cost(1)
			.function(new CardInfoAdapter()
			{
				@Override public boolean canTarget(Card card,Player player,Card target,int choi)
				{
					return target==null;
				}
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					Game game=player.getGame();
					game.forEachCardOnTable(c->{if(c.getPosition()==Position.MINION)c.takeDamageWithoutCheck(card,1);});
					game.checkForDamage();
				}
			}).create());
		register(cc.name("yydj").clz(Clz.WARRIOR).type(Type.SPELL).cost(2)
			.function(new CardInfoAdapter()
			{
				@Override public boolean canTarget(Card card,Player player,Card target,int choi)
				{
					return target==null;
				}
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					player.getHero().gainBuff(new ThisTurnPPBuffInfo(4,0),"hs.basic:yydj",null);
				}
			}).create());
		register(cc.name("zs").clz(Clz.WARRIOR).type(Type.SPELL).cost(2)
			.function(new CardInfoAdapter()
			{
				@Override public boolean canTarget(Card card,Player player,Card target,int choi)
				{
					return target!=null&&target.getPosition()==Position.MINION&&target.getOwner()!=player&&target.isDamaged();
				}
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					target.kill();
				}
			}).create());
		register(cc.name("cyzf").clz(Clz.WARRIOR).type(Type.WEAPON).stature(3,3,2).create());
		register(cc.name("spz").clz(Clz.WARRIOR).type(Type.SPELL).cost(2)
			.function(new CardInfoAdapter()
			{
				@Override public boolean canTarget(Card card,Player player,Card target,int choi)
				{
					return target==null&&player.getNextPlayer().getMinionNum()<2;
				}
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					List<Card> minions=player.getNextPlayer().getAllMinions();
					if(minions.size()>=2)for(int len=2;len>0;len--)minions.remove((int)(Math.random()*len)).takeDamageWithoutCheck(card,2);
					player.getGame().checkForDamage();
				}
			}).create());
		register(cc.name("zgzhg").clz(Clz.WARRIOR).type(Type.MINION).stature(3,2,3)
			.buffs(new MyMinionBuffInfo(new PPEffectBuffInfo(1,0),"hs.basic:sgshg")
			{
				@Override protected boolean filter(Buff buff, Card card)
				{
					return card.hasKW(KeyWord.CHARGE);
				}
			}).create());
		register(cc.name("dpgd").clz(Clz.WARRIOR).type(Type.SPELL).cost(3)
			.function(new CardInfoAdapter()
			{
				@Override public boolean canTarget(Card card,Player player,Card target,int choi)
				{
					return target==null;
				}
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					player.gainArmor(5);
					player.draw(1);
				}
			}).create());
		register(cc.name("kkljyws").clz(Clz.WARRIOR).type(Type.MINION).stature(4,4,3)
			.buffs(new KWBuffInfo(KeyWord.CHARGE)).create());
		register(cc.name("ajf").clz(Clz.WARRIOR).type(Type.WEAPON).stature(5,5,2).create());
	}
	
	public static int getTotemFlagFromField(List<? extends Card> field)
	{
		int rt=0;
		for(Card c:field)
		{
			for(int i=0;i<4;i++)
			{
				if(basicTotems[i]==c.info.name)
				{
					rt|=(1<<i);
					break;
				}
			}
		}
		return rt;
	}
}
