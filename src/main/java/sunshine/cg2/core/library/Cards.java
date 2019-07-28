package sunshine.cg2.core.library;

import java.util.Arrays;

import sunshine.cg2.core.game.Buff;
import sunshine.cg2.core.game.BuffInfo;
import sunshine.cg2.core.game.Card;
import sunshine.cg2.core.game.Card.Position;
import sunshine.cg2.core.game.CardInfo;
import sunshine.cg2.core.game.CardPackage;
import sunshine.cg2.core.game.Game;
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
	
	public static class MyOtherMinionBuffInfo extends TableBuffInfo
	{
		public MyOtherMinionBuffInfo(KeyWord[] keyWords,BuffInfo effectInfo,String effectName)
		{
			super(keyWords,effectInfo,effectName);
		}
		
		@Override
		protected boolean filter(Buff buff,Card card)
		{
			return buff.toBuff!=card&&card.getPosition()==Position.MINION&&card.getOwner()==buff.toBuff.getOwner();
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
	
	public static class NullTargetCardInfo extends CardInfo
	{
		public NullTargetCardInfo(String name,Clz clz,Race[] races,Type type,int cost,int atk,int HP,boolean shield,BuffInfo[] buffs,int choices,CardInfo skill)
		{
			super(name,clz,races,type,true,cost,atk,HP,shield,buffs,choices,skill);
		}
		
		@Override
		public boolean canTarget(Card card,Player player,Card target,int choi)
		{
			return target==null;
		}
	}
	
	public static class DamageSpellCardInfo extends CardInfo
	{
		private int damage;
		
		public DamageSpellCardInfo(String name,Clz clz,Race[] races,int cost,int choices,int damage)
		{
			super(name,clz,races,Type.SPELL,true,cost,0,0,false,null,choices,null);
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
	
	public static class DKCardInfo extends CardInfo
	{
		private static class Impl extends BuffInfo
		{
			int armor;
			
			Impl(int armor)
			{
				super(null,new Object[]{EnterTableEvent.class},false);
			}
			
			@Override
			public void onTrigger(Buff buff,Event event)
			{
				buff.toBuff.getOwner().gainArmor(armor);
			}
		}
		
		public DKCardInfo(String name,Clz clz,Race[] races,int cost,int choices,CardInfo skill,int armor)
		{
			super(name,clz,races,Type.HERO,true,cost,0,0,false,new BuffInfo[]{new Impl(armor)},choices,skill);
		}
	}
	
	/*
	cg2:dummy
	cg2:hero0
	cg2:minion0
	cg2:spell0
	hs.basic:albkbhz
	hs.basic:assj
	hs.basic:hs
	hs.basic:jh
	hs.basic:sll
	hs.basic:xhs
	hs.basic:yhs
	hs.basic:ympx
	hs.basic:yxcz
	hs.basic:yxyj
	hs.basic:zj
	hs.basic:zlzc
	hs.basic:zzs
	hs.basic:~flgs
	*/
	private static final CardInfo[] basicCards=
	{
		new CardInfo("cg2:dummy",Clz.NONE,null,Type.NONE,false,0,0,0,false,null,1,null),
		new NullTargetCardInfo("cg2:minion0",Clz.NONE,new Race[]{Race.BEAST},Type.MINION,0,1,1,true,new BuffInfo[]{new MyOtherMinionBuffInfo(null,new PPEffectBuffInfo(null,1,1),"cg2:minion0")},1,null),
		new CardInfo("cg2:spell0",Clz.NONE,null,Type.SPELL,true,0,0,0,false,null,1,null),
		new CardInfo("cg2:hero0",Clz.NONE,null,Type.HERO,false,0,0,30,false,new BuffInfo[]{new BuffInfo(new KeyWord[]{KeyWord.WINDFURY},null,false)},1,null),
		new DamageSpellCardInfo("hs.basic:yhs",Clz.DRUID,null,0,1,1),
		new NullTargetCardInfo("hs.basic:jh",Clz.DRUID,null,Type.SPELL,0,0,0,false,null,1,null)
		{
			@Override public void doBattlecry(Card card,Player player,Card target,int choi)
			{
				player.gainEmptyCoins(2,true);
				player.fillCoins(2);
			}
		},
		new NullTargetCardInfo("hs.basic:zj",Clz.DRUID,null,Type.SPELL,1,0,0,false,null,1,null)
		{
			@Override public void doBattlecry(Card card,Player player,Card target,int choi)
			{
				Card hero=player.getHero();
				player.gainArmor(3);
				hero.gainBuff(new ThisTurnPPBuffInfo(null,null,2,0),name,null);
			}
		},
		new CardInfo("hs.basic:yxyj",Clz.DRUID,null,Type.SPELL,true,2,0,0,false,null,1,null)
		{
			@Override public void doBattlecry(Card card,Player player,Card target,int choi)
			{
				target.gainBuff(new BuffInfo(new KeyWord[]{KeyWord.TAUNT},null,false),name,null);
				target.pp(2,2,true);
			}
			@Override public boolean canTarget(Card card,Player player,Card target,int choi)
			{
				return target!=null&&target.getPosition()==Card.Position.MINION;
			}
		},
		new CardInfo("hs.basic:zlzc",Clz.DRUID,null,Type.SPELL,true,3,0,0,false,null,1,null)
		{
			@Override public void doBattlecry(Card card,Player player,Card target,int choi)
			{
				target.restoreHealth(card,8);
			}
			@Override public boolean canTarget(Card card,Player player,Card target,int choi)
			{
				return target!=null;
			}
		},
		new NullTargetCardInfo("hs.basic:yxcz",Clz.DRUID,null,Type.SPELL,2,0,0,false,null,1,null)
		{
			@Override public void doBattlecry(Card card,Player player,Card target,int choi)
			{
				if(!player.gainEmptyCoins(1,false))player.obtain(player.getGame().createCard("hs.basic:~flgs",-1));
			}
		},
		new NullTargetCardInfo("hs.basic:~flgs",Clz.DRUID,null,Type.SPELL,0,0,0,false,null,1,null)
		{
			@Override public void doBattlecry(Card card,Player player,Card target,int choi)
			{
				player.draw(1);
			}
		},
		new NullTargetCardInfo("hs.basic:ympx",Clz.DRUID,null,Type.SPELL,3,0,0,false,null,1,null)
		{
			@Override public void doBattlecry(Card card,Player player,Card target,int choi)
			{
				Card hero=player.getHero();
				BuffInfo bi = new ThisTurnPPBuffInfo(null,null,2,0);
				hero.gainBuff(bi,name,null);
				for(Card c:player.getField())if(c.positionIsMinionOrHero())c.gainBuff(bi,name,null);
			}
		},
		new DamageSpellCardInfo("hs.basic:hs",Clz.DRUID,null,4,1,4)
		{
			@Override
			public void doBattlecry(Card card,Player player,Card target,int choi)
			{
				Player tarp=target.getOwner();
				super.doBattlecry(card,player,target,choi);
				player.getGame().forEachCardOnTable(c->{if(c!=target&&c.getOwner()==tarp&&c.positionIsMinionOrHero())c.takeDamage(card,1);});
			}
		},
		new DamageSpellCardInfo("hs.basic:xhs",Clz.DRUID,null,6,1,5)
		{
			@Override
			public void doBattlecry(Card card,Player player,Card target,int choi)
			{
				super.doBattlecry(card,player,target,choi);
				player.draw(1);
			}
		},
		new NullTargetCardInfo("hs.basic:albkbhz",Clz.DRUID,null,Type.MINION,8,8,8,false,new BuffInfo[]{new BuffInfo(new KeyWord[]{KeyWord.TAUNT},null,false)},1,null),
		new DamageSpellCardInfo("hs.basic:assj",Clz.HUNTER,null,1,1,2),
		new NullTargetCardInfo("hs.basic:sll",Clz.HUNTER,new Race[]{Race.BEAST},Type.MINION,1,1,1,false,new BuffInfo[]{new MyOtherMinionBuffInfo(null,new PPEffectBuffInfo(null,1,0),"hs.basic:sll")
			{
				@Override protected boolean filter(Buff buff,Card card)
				{
					return super.filter(buff,card)&&card.hasRace(Race.BEAST);
				}
			}},1,null),
		new NullTargetCardInfo("hs.basic:zzs",Clz.HUNTER,null,Type.SPELL,1,0,0,false,null,1,null)
		{
			@Override
			public void doBattlecry(Card card,Player player,Card target,int choi)
			{
				//
			}
		},
	};
	
	public static final CardPackage BASIC_CARDS=new CardPackage(){public CardInfo[] getAllCards(){return basicCards.clone();}};
}
