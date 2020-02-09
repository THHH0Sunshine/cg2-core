package sunshine.cg2.core.library;

import java.util.HashMap;
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
import sunshine.cg2.core.game.event.DeathrattleEvent;
import sunshine.cg2.core.game.event.Event;
import sunshine.cg2.core.game.event.GainBuffEvent;
import sunshine.cg2.core.game.event.LoseBuffEvent;
import sunshine.cg2.core.game.event.globalevent.AfterTurnEndEvent;
import sunshine.cg2.core.game.event.globalevent.DamagedEvent;
import sunshine.cg2.core.game.event.globalevent.EnterTableEvent;
import sunshine.cg2.core.game.event.globalevent.LeaveTableEvent;
import sunshine.cg2.core.game.event.globalevent.TurnStartEvent;
import sunshine.cg2.core.library.hearthstone.BasicCards;
import sunshine.cg2.core.util.CardCreator.CardFunction;

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
	
	public static class OtherMinionBuffInfo extends TableBuffInfo
	{
		public OtherMinionBuffInfo(BuffInfo effectInfo,String effectName,KeyWord... keyWords)
		{
			super(effectInfo,effectName,keyWords);
		}
		
		@Override
		protected boolean filter(Buff buff,Card card)
		{
			return buff.toBuff!=card&&card.getPosition()==Position.MINION;
		}
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
	
	public static class DamageFunction extends CardFunction
	{
		private final int damage;
		
		public DamageFunction(int damage)
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
	
	public static class HealingFunction extends CardFunction
	{
		private final int num;
		
		public HealingFunction(int num)
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
	
	public static class SummonRightFunction extends CardFunction
	{
		private final String toSummon;
		
		public SummonRightFunction(String toSummon)
		{
			this.toSummon=toSummon;
		}
		
		@Override
		public void doBattlecry(Card card,Player player,Card target,int choi) throws GameOverThrowable
		{
			player.summon(player.getGame().createCard(toSummon,-1),card,false);
		}
	}
	
	private Cards()
	{
	}
	
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
	
	public static final Map<String,CardInfo> DEFAULT_LIBRARY=new HashMap<String,CardInfo>();
	
	static
	{
		BasicCards.init();
	}
}
