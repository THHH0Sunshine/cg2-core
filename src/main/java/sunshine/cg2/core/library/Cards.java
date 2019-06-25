package sunshine.cg2.core.library;

import java.util.Arrays;

import sunshine.cg2.core.game.Buff;
import sunshine.cg2.core.game.BuffInfo;
import sunshine.cg2.core.game.Card;
import sunshine.cg2.core.game.CardInfo;
import sunshine.cg2.core.game.CardPackage;
import sunshine.cg2.core.game.Game;
import sunshine.cg2.core.game.Player;
import sunshine.cg2.core.game.BuffInfo.KeyWord;
import sunshine.cg2.core.game.CardInfo.Type;
import sunshine.cg2.core.game.event.Event;
import sunshine.cg2.core.game.event.GainBuffEvent;
import sunshine.cg2.core.game.event.LoseBuffEvent;
import sunshine.cg2.core.game.event.globalevent.AfterTurnEndEvent;
import sunshine.cg2.core.game.event.globalevent.MinionEnterEvent;
import sunshine.cg2.core.game.event.globalevent.MinionLeaveEvent;

public class Cards {

	public static abstract class MyMinionBuffInfo extends BuffInfo
	{
		private final BuffInfo effectInfo;
		private final String effectName;
		
		public MyMinionBuffInfo(KeyWord[] keyWords,BuffInfo effectInfo,String effectName)
		{
			super(keyWords,new Object[]{MinionEnterEvent.class,GainBuffEvent.class,MinionLeaveEvent.class,LoseBuffEvent.class},false);
			this.effectInfo=effectInfo;
			this.effectName=effectName;
		}
		
		@Override
		public void onTrigger(Buff buff,Game game,Event event)
		{
			if(event instanceof MinionEnterEvent)
			{
				Card who=((MinionEnterEvent)event).minion;
				if(who==buff.toBuff)
				{
					for(Card c:who.getOwner().getField())
					{
						if(c.positionIsMinionOrHero()&&filter(buff,game,c))
						{
							c.gainBuff(new Buff(effectInfo,effectName,c,who));
						}
					}
				}
				else
				{
					if(who.getOwner()==buff.toBuff.getOwner()&&filter(buff,game,who))
					{
						who.gainBuff(new Buff(effectInfo,effectName,who,buff.toBuff));
					}
				}
			}
			else if(event instanceof MinionLeaveEvent)
			{
				Card who=((MinionLeaveEvent)event).minion;
				if(who==buff.toBuff)
				{
					for(Player p:game.getAllPlayers())
					{
						for(Card c:p.getField())
						{
							Buff b=c.getEffectBySource(who);
							if(b!=null)c.loseBuff(b);
						}
					}
				}
				else
				{
					MinionLeaveEvent.Reason reason=((MinionLeaveEvent)event).reason;
					if(reason!=null&&(reason.equals(MinionLeaveEvent.Reason.CONTROL)))
					{
						Buff b=who.getEffectBySource(buff.toBuff);
						if(b!=null)who.loseBuff(b);
					}
				}
			}
			else if(event instanceof GainBuffEvent)
			{
				for(Card c:buff.toBuff.getOwner().getField())
				{
					if(c.positionIsMinionOrHero()&&filter(buff,game,c))
					{
						c.gainBuff(new Buff(effectInfo,effectName,c,buff.toBuff));
					}
				}
			}
			else if(event instanceof LoseBuffEvent)
			{
				for(Player p:game.getAllPlayers())
				{
					for(Card c:p.getField())
					{
						Buff b=c.getEffectBySource(buff.toBuff);
						if(b!=null)c.loseBuff(b);
					}
				}
			}
		}
		
		abstract boolean filter(Buff buff,Game game,Card card);
	}
	
	public static class MyOtherMinionBuffInfo extends MyMinionBuffInfo
	{
		public MyOtherMinionBuffInfo(KeyWord[] keyWords,BuffInfo effectInfo,String effectName)
		{
			super(keyWords,effectInfo,effectName);
		}
		
		@Override
		protected boolean filter(Buff buff,Game game,Card card)
		{
			return buff.toBuff!=card;
		}
	}
	
	public static class PPEffectBuffInfo extends BuffInfo
	{
		private int atk;
		private int HP;
		
		public PPEffectBuffInfo(KeyWord[] keyWords,int atk,int HP)
		{
			super(keyWords,new Object[]{GainBuffEvent.class,LoseBuffEvent.class},true);
			this.atk=atk;
			this.HP=HP;
		}
		
		@Override
		public void onTrigger(Buff buff,Game game,Event event)
		{
			if(event instanceof GainBuffEvent)buff.toBuff.pp(atk,HP,false);
			else buff.toBuff.pp(-atk,-HP,false);
		}
	}
	
	public static class ThisTurnBuffInfo extends BuffInfo
	{
		private static Object[] events(Object[] c)
		{
			Object[] rt=Arrays.copyOf(c,c.length+1);
			rt[c.length]=AfterTurnEndEvent.class;
			return rt;
		}
		
		public ThisTurnBuffInfo(KeyWord[] keyWords,Object[] extraEvents)
		{
			super(keyWords,events(extraEvents),false);
		}
		
		@Override
		public void onTrigger(Buff buff,Game game,Event event)
		{
			if(event instanceof AfterTurnEndEvent)buff.toBuff.loseBuff(buff);
			else onExtraEvents(buff,game,event);
		}
		
		public void onExtraEvents(Buff buff,Game game,Event event)
		{
		}
	}
	
	public static class NullTargetCardInfo extends CardInfo
	{
		public NullTargetCardInfo(String name,Type type,int cost,int atk,int HP,boolean shield,BuffInfo[] buffs,int choices,CardInfo skill)
		{
			super(name,type,true,cost,atk,HP,shield,buffs,choices,skill);
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
		
		public DamageSpellCardInfo(String name,int cost,int choices,int damage)
		{
			super(name,Type.SPELL,true,cost,0,0,false,null,choices,null);
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
		private final int armor;
		
		public DKCardInfo(String name,int cost,int choices,CardInfo skill,int armor)
		{
			super(name,Type.HERO,true,cost,0,0,false,null,choices,skill);
			this.armor=armor;
		}
		
		@Override
		public boolean changeHero(Player player,Card oldHero,Card newHero)
		{
			player.gainArmor(armor);
			return false;
		}
	}
	
	private static final CardInfo[] basicCards=
	{
		new CardInfo("cg2:dummy",Type.NONE,false,0,0,0,false,null,1,null),
		new NullTargetCardInfo("cg2:minion0",Type.MINION,0,1,1,true,new BuffInfo[]{new MyOtherMinionBuffInfo(null,new PPEffectBuffInfo(null,1,1),"cg2:minion0")},1,null),
		new CardInfo("cg2:spell0",Type.SPELL,true,0,0,0,false,null,1,null),
		new CardInfo("cg2:hero0",Type.HERO,false,0,0,30,false,new BuffInfo[]{new BuffInfo(new KeyWord[]{KeyWord.WINDFURY},null,false)},1,null),
		new DamageSpellCardInfo("hs.basic:yhs",0,1,1),
		new NullTargetCardInfo("hs.basic:jh",Type.SPELL,0,0,0,false,null,1,null)
		{
			@Override public void doBattlecry(Card card,Player player,Card target,int choi)
			{
				player.gainEmptyCoins(2,true);
				player.fillCoins(2);
			}
		},
		new NullTargetCardInfo("hs.basic:zj",Type.SPELL,1,0,0,false,null,1,null)
		{
			@Override public void doBattlecry(Card card,Player player,Card target,int choi)
			{
				Card hero=player.getHero();
				player.gainArmor(3);
				hero.gainBuff(new Buff(new ThisTurnBuffInfo(null,new Object[]{GainBuffEvent.class,LoseBuffEvent.class})
				{
					@Override public void onExtraEvents(Buff buff,Game game,Event event)
					{
						if(event instanceof GainBuffEvent)buff.toBuff.pp(2,0,false);
						else buff.toBuff.pp(-2,0,false);
					}
				},name,hero,null));
			}
		},
		new CardInfo("hs.basic:yxyj",Type.SPELL,true,2,0,0,false,null,1,null)
		{
			@Override public void doBattlecry(Card card,Player player,Card target,int choi)
			{
				target.gainBuff(new Buff(new BuffInfo(new KeyWord[]{KeyWord.TAUNT},null,false),name,target,null));
				target.pp(2,2,true);
			}
			@Override public boolean canTarget(Card card,Player player,Card target,int choi)
			{
				return target!=null&&target.getPosition()==Card.Position.MINION;
			}
		},
		new CardInfo("hs.basic:zlzc",Type.SPELL,true,3,0,0,false,null,1,null)
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
		new NullTargetCardInfo("hs.basic:yxcz",Type.SPELL,2,0,0,false,null,1,null)
		{
			@Override public void doBattlecry(Card card,Player player,Card target,int choi)
			{
				if(!player.gainEmptyCoins(1,false))player.obtain(player.getGame().createCard("hs.basic:~flgs",-1));
			}
		},
		new NullTargetCardInfo("hs.basic:~flgs",Type.SPELL,0,0,0,false,null,1,null)
		{
			@Override public void doBattlecry(Card card,Player player,Card target,int choi)
			{
				player.draw(1);
			}
		}
	};
	
	public static final CardPackage BASIC_CARDS=new CardPackage(){public CardInfo[] getAllCards(){return basicCards.clone();}};
}
