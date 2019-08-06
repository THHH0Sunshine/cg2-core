package sunshine.cg2.core.game;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import sunshine.cg2.core.game.BuffInfo.KeyWord;
import sunshine.cg2.core.game.event.GainBuffEvent;
import sunshine.cg2.core.game.event.LoseBuffEvent;
import sunshine.cg2.core.game.event.globalevent.AttackingEvent;
import sunshine.cg2.core.game.event.globalevent.DamagedEvent;
import sunshine.cg2.core.game.event.globalevent.GlobalEvent;
import sunshine.cg2.core.game.event.globalevent.HealedEvent;
import sunshine.cg2.core.util.JSONArray;
import sunshine.cg2.core.util.JSONObject;

public class Card {

	public enum Position
	{
		OFFTABLE,
		HERO,
		SKILL,
		MINION,
		ICE,
		EQUIP,
		SECRET,
		QUEST,
		SPECIAL,
	}
	
	private Game game;
	private int cost;
	private int atk;
	private int maxHP;
	private int HP;
	private int datk;
	private int dHP;
	private boolean shield;
	private int armor;
	private boolean dying;
	private boolean sleeping;
	private int wind;
	private boolean forceGreen;
	private final LinkedList<Buff> buffs=new LinkedList<>();
	private Player owner;
	private int number;
	private Position position=Position.OFFTABLE;
	private DamagedEvent damagedEvent;
	private HealedEvent healedEvent;
	public final CardInfo info;
	public final int from;
	public final HashMap<String,Object> tags = new HashMap<>();
	
	Card(Game game,CardInfo info,int from,int cost,int atk,int HP)
	{
		this.game=game;
		this.info=info;
		this.from=from;
		this.cost=cost;
		this.atk=atk;
		maxHP=HP;
		this.HP=HP;
		this.shield=info.shield;
		if(info.buffs!=null)for(BuffInfo bi:info.buffs)buffs.add(new Buff(bi,"",this,null));
	}
	
	void gainArmor(int armor)
	{
		this.armor+=armor;
	}
	
	JSONObject getDisplayObject()
	{
		return new JSONObject(new Object[][]
		{
			{"name",info.name},
			{"cost",cost},
			{"atk",getAtk()},
			{"hp",HP},
			{"canplay",info.canPlay},
			{"type",info.type.name()},
			{"handtags",info.getHandTags(this)}
		});
	}
	
	JSONObject getFullObject()
	{
		JSONArray rtBuff=new JSONArray(buffs.size());
		for(Buff b:buffs)rtBuff.add(b.getObject());
		return new JSONObject(new Object[][]
		{
			{"hash",hashCode()},
			{"name",info.name},
			{"atk",getAtk()},
			{"maxhp",maxHP},
			{"hp",HP},
			{"shield",shield},
			{"armor",armor},
			{"buff",rtBuff},
			{"tabletags",info.getTableTags(this)}
		});
	}
	
	void incWind()
	{
		forceGreen=false;
		wind++;
	}
	
	void initOnTable(Position position,Player owner,int number)
	{
		this.position=position;
		this.owner=owner;
		this.number=number;
		switch(position)
		{
		case MINION:
			sleeping=true;
			break;
		default:
		}
	}
	
	DamagedEvent removeDamagedEvent()
	{
		DamagedEvent rt=damagedEvent;
		damagedEvent=null;
		return rt;
	}
	
	HealedEvent removeHealedEvent()
	{
		HealedEvent rt=healedEvent;
		healedEvent=null;
		return rt;
	}
	
	void replaceOldHero(Card old)
	{
		if(info.HP<=0)
		{
			maxHP=old.maxHP;
			HP=old.HP;
		}
		atk=old.atk;
		armor=old.armor;
		wind=old.wind;
		for(Buff b:old.buffs)buffs.add(b);
		tags.putAll(old.tags);
	}
	
	void resetPosition()
	{
		position=Position.OFFTABLE;
	}
	
	void resetWind()
	{
		sleeping=false;
		forceGreen=false;
		wind=0;
	}
	
	public void attack(Card target)
	{
		if(!positionIsMinionOrHero()||!target.positionIsMinionOrHero())return;
		for(Buff b:buffs)
		{
			if(b.info.keyWords==null||b.info.isEffect)continue;
			for(BuffInfo.KeyWord kw:b.info.keyWords)
			{
				if(kw.equals(BuffInfo.KeyWord.STEALTH))
				{
					loseBuff(b);
					break;
				}
			}
		}
		game.triggerEvent(new AttackingEvent(this,target));
		wind++;
		game.broadcast(Game.Msg.ATTACK,new JSONObject(new Object[][]{{"fromhash",hashCode()},{"tohash",target.hashCode()}}),-1);
		boolean f = this.number < target.number;
		Card finj = f ? this : target;
		Card sinj = f ? target : this;
		finj.takeDamageWithoutCheck(sinj,sinj.atk);
		sinj.takeDamageWithoutCheck(finj,finj.atk);
		game.checkForDamage();
		if(position==Position.HERO)owner.damageWeapon();
	}
	
	public void forceGreen()
	{
		forceGreen=true;
	}
	
	public void freeze()
	{
		if(!positionIsMinionOrHero())return;
		gainBuff(new BuffInfo(new KeyWord[]{KeyWord.FROZEN},null,false),"",null);
	}
	
	public void gainBuff(BuffInfo buffInfo,String name,Buff effectSource)
	{
		if(position==Position.OFFTABLE)return;
		Buff buff = new Buff(buffInfo,name,this,effectSource);
		buffs.add(buff);
		if(buffInfo.events!=null)game.registerEvents(buff,GlobalEvent.class);
		game.broadcast(Game.Msg.GAINBUFF,new JSONObject(new Object[][]{{"hash",hashCode()},{"buff",buff.getObject()}}),-1);
		buff.triggerSelf(new GainBuffEvent());
	}
	
	public void gainShield()
	{
		if(!positionIsMinionOrHero())return;
		shield=true;
		game.broadcast(Game.Msg.GAINSHIELD,new JSONObject(new Object[][]{{"hash",hashCode()}}),-1);
	}
	
	public Buff[] getAllBuffs()
	{
		return buffs.toArray(new Buff[0]);
	}
	
	public int getAtk()
	{
		return atk<0?0:atk;
	}
	
	public int getCost()
	{
		return cost;
	}
	
	public Buff getEffectBySource(Buff source)
	{
		for(Buff b:buffs)if(b.effectSource==source)return b;
		return null;
	}
	
	public Game getGame()
	{
		return game;
	}
	
	public Card getHandCopy()
	{
		if(position!=Position.OFFTABLE)return null;
		Card rt=new Card(game,info,-1,cost,atk,HP);
		rt.maxHP=maxHP;
		rt.datk=datk;
		rt.dHP=dHP;
		return rt;
	}
	
	public int getHP()
	{
		return HP;
	}
	
	public int getMaxHP()
	{
		return maxHP;
	}
	
	public int getMaxWind()
	{
		return hasKW(BuffInfo.KeyWord.WINDFURY)?2:1;
	}
	
	public int getNumber()
	{
		return number;
	}
	
	public Player getOwner()
	{
		return owner;
	}
	
	public Position getPosition()
	{
		return position;
	}
	
	public int getRealAtk()
	{
		return atk;
	}
	
	public int getSpeed()
	{
		if(forceGreen)return 2;
		HashSet<BuffInfo.KeyWord> kws=new HashSet<>();
		for(Buff b:buffs)if(b.info.keyWords!=null)for(BuffInfo.KeyWord kw:b.info.keyWords)kws.add(kw);
		if(kws.contains(BuffInfo.KeyWord.WOOD)||kws.contains(BuffInfo.KeyWord.FROZEN)||info.type!=CardInfo.Type.SKILL&&atk<=0||wind>=getMaxWind())return 0;
		if(!sleeping)return 2;
		if(kws.contains(BuffInfo.KeyWord.CHARGE))return 2;
		if(kws.contains(BuffInfo.KeyWord.RUSH))return 1;
		return 0;
	}
	
	public boolean hasKW(BuffInfo.KeyWord kW)
	{
		for(Buff b:buffs)
		{
			if(b.info.keyWords!=null)
			{
				for(BuffInfo.KeyWord w:b.info.keyWords)
				{
					if(w.equals(kW))return true;
				}
			}
		}
		return false;
	}
	
	public boolean hasRace(CardInfo.Race race)
	{
		if (info.races==null)return false;
		for(CardInfo.Race r:info.races)if(r==race)return true;
		return false;
	}
	
	public void heal(Card from,int count)
	{
		if(position==Position.OFFTABLE||HP>=maxHP)return;
		healWithoutCheck(from,count);
		game.checkForHeal();
	}
	
	public void healWithoutCheck(Card from,int count)
	{
		if(position==Position.OFFTABLE||HP>=maxHP)return;
		int c=maxHP-HP;
		if(count<c)c=count;
		HP+=count;
		healedEvent=new HealedEvent(from,this,count);
		JSONObject toSend=new JSONObject(new Object[][]{{"tohash",hashCode()},{"num",c}});
		if(from!=null)toSend.put("fromhash",from.hashCode());
		game.broadcast(Game.Msg.HEAL,toSend,-1);
	}
	
	public boolean isDying()
	{
		return HP<=0||dying;
	}
	
	public void kill()
	{
		dying=true;
	}
	
	public void loseBuff(Buff buff)
	{
		if(position==Position.OFFTABLE)return;
		int ind=buffs.indexOf(buff);
		if(ind<0)return;
		buff.triggerSelf(new LoseBuffEvent());
		game.unregisterEvents(buff);
		buffs.remove(buff);
		game.broadcast(Game.Msg.LOSEBUFF,new JSONObject(new Object[][]{{"index",ind},{"hash",hashCode()}}),-1);
	}
	
	public boolean positionIsMinionOrHero()
	{
		return position==Position.MINION||position==Position.HERO;
	}
	
	public void pp(int atk,int HP,boolean decOnSilence)
	{
		if(decOnSilence)
		{
			datk+=atk;
			dHP+=HP;
		}
		int batk=this.atk<0?0:this.atk;
		this.atk+=atk;
		if(position==Position.EQUIP&&game.getCurrentPlayer()==owner)
		{
			int aatk=this.atk<0?0:this.atk;
			if(batk!=aatk)owner.getHero().pp(aatk-batk,0,false);
		}
		maxHP+=HP;
		if(HP>0)this.HP+=HP;
		if(this.HP>maxHP)this.HP=maxHP;
		if(position!=Position.OFFTABLE)game.broadcast(Game.Msg.CHANGEPP,new JSONObject(new Object[][]{{"hash",hashCode()},{"atk",getAtk()},{"maxhp",maxHP},{"hp",this.HP}}),-1);
	}
	
	public void setAtk(int atk)
	{
		pp(atk-this.atk-datk,0,true);
	}
	
	public void setAtkAndHP(int atk,int HP)
	{
		this.HP=maxHP;
		int patk=atk-this.atk-datk;
		int pHP=HP-this.HP-dHP;
		pp(patk,pHP,true);
	}
	
	public void setAtkAndHPWithDmg(int atk,int HP)
	{
		int dmg=maxHP-this.HP;
		this.HP=maxHP;
		maxHP+=dmg;
		int patk=atk-this.atk-datk;
		int pHP=HP-this.HP-dHP;
		pp(patk,pHP,true);
	}
	
	public void setHP(int HP)
	{
		this.HP=maxHP;
		pp(0,HP-this.HP-dHP,true);
	}
	
	public void setHPWithDmg(int HP)
	{
		int dmg=maxHP-this.HP;
		this.HP=maxHP;
		maxHP+=dmg;
		pp(0,HP-this.HP-dHP,true);
	}
	
	boolean shouldBreakIce()
	{
		boolean charge=false;
		for(Buff b:buffs)
		{
			if(b.info.keyWords!=null)
			{
				for(BuffInfo.KeyWord w:b.info.keyWords)
				{
					if(w==BuffInfo.KeyWord.CHARGE||w==BuffInfo.KeyWord.RUSH)
					{
						charge=true;
						break;
					}
				}
			}
		}
		return (charge||!sleeping)&&wind<getMaxWind();
	}
	
	public void silence()
	{
		if(position==Position.OFFTABLE)return;
		Iterator<Buff> it = buffs.iterator();
		while (it.hasNext())
		{
			Buff b = it.next();
			if (!b.info.isEffect)it.remove();
		}
		pp(-datk,-dHP,true);
	}
	
	public void takeDamage(Card from,int damage)
	{
		if(position==Position.OFFTABLE)return;
		takeDamageWithoutCheck(from,damage);
		game.checkForDamage();
	}
	
	public void takeDamageWithoutCheck(Card from,int damage)
	{
		if(position==Position.OFFTABLE)return;
		if(shield)
		{
			shield=false;
			game.broadcast(Game.Msg.LOSESHIELD,new JSONObject(new Object[][]{{"hash",hashCode()}}),-1);
		}
		else
		{
			if(armor>=damage)armor-=damage;
			else
			{
				HP-=damage-armor;
				armor=0;
			}
			damagedEvent=new DamagedEvent(from,this,damage);
			JSONObject toSend=new JSONObject(new Object[][]{{"tohash",hashCode()},{"num",damage}});
			if(from!=null)toSend.put("fromhash",from.hashCode());
			game.broadcast(Game.Msg.DAMAGE,toSend,-1);
		}
	}
	
	public void transformField(Card to,boolean ice)
	{
		if(position!=Position.MINION)return;
		owner.transformField(this,to,ice);
	}
}
