package sunshine.cg2.core.game;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import sunshine.cg2.core.game.event.GainBuffEvent;
import sunshine.cg2.core.game.event.LoseBuffEvent;
import sunshine.cg2.core.game.event.NormalLosingEvent;
import sunshine.cg2.core.game.event.globalevent.GlobalEvent;
import sunshine.cg2.core.util.JSONArray;
import sunshine.cg2.core.util.JSONObject;

public class Card {

	public enum Position
	{
		NONE,
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
	private int wind;
	private final LinkedList<Buff> buffs=new LinkedList<>();
	private Player owner;
	private int number;
	private Position position=Position.NONE;
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
			{"choices",info.choices},
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
			{"tabletags",info.getHandTags(this)}
		});
	}
	
	void incWind()
	{
		wind++;
	}
	
	void replaceOldHero(Player player,Card old)
	{
		initOnTable(Position.HERO,player,-1);
		if(!info.changeHero(player,old,this))
		{
			maxHP=old.maxHP;
			HP=old.HP;
		}
		atk=old.atk;
		wind=old.wind;
		for(Buff b:old.buffs)if(b.info.isEffect)buffs.add(b);
	}
	
	void resetWind()
	{
		wind=0;
	}
	
	void initOnTable(Position position,Player owner,int number)
	{
		this.position=position;
		this.owner=owner;
		this.number=number;
		switch(position)
		{
		case MINION:
			wind=-1;
			break;
		default:
		}
	}
	
	public void attack(Card target,byte[] extra)
	{
		for(Buff b:buffs)
		{
			if(b.info.keyWords==null||b.info.isEffect)continue;
			for(BuffInfo.KeyWord kw:b.info.keyWords)
			{
				if(kw.equals(BuffInfo.KeyWord.STEALTH))
				{
					NormalLosingEvent e=new NormalLosingEvent();
					b.triggerSelf(game,e);
					if(!e.prevent)loseBuff(b);
					break;
				}
			}
		}
		if(wind>=0)wind++;
		game.broadcast(Game.Msg.ATTACK,new JSONObject(new Object[][]{{"fromhash",hashCode()},{"tohash",target.hashCode()}}),-1);
		boolean f = this.number < target.number;
		Card finj = f ? this : target;
		Card sinj = f ? target : this;
		finj.takeDamage(sinj,sinj.atk);
		sinj.takeDamage(finj,finj.atk);
	}
	
	public void takeDamage(Card from,int damage)
	{
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
				if(HP<=0)dying=true;
				armor=0;
			}
			JSONObject toSend=new JSONObject(new Object[][]{{"tohash",hashCode()},{"num",damage}});
			if(from!=null)toSend.put("fromhash",from.hashCode());
			game.broadcast(Game.Msg.DAMAGE,toSend,-1);
		}
	}
	
	public void gainBuff(BuffInfo buffInfo,String name,Card effectSource)
	{
		Buff buff = new Buff(buffInfo,name,this,effectSource);
		buffs.add(buff);
		if(buffInfo.events!=null)game.registerEvents(buff,GlobalEvent.class);
		game.broadcast(Game.Msg.GAINBUFF,new JSONObject(new Object[][]{{"hash",hashCode()},{"buff",buff.getObject()}}),-1);
		buff.triggerSelf(game,new GainBuffEvent());
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
	
	public Buff getEffectBySource(Card source)
	{
		for(Buff b:buffs)if(b.effectSource==source)return b;
		return null;
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
		HashSet<BuffInfo.KeyWord> kws=new HashSet<>();
		for(Buff b:buffs)if(b.info.keyWords!=null)for(BuffInfo.KeyWord kw:b.info.keyWords)kws.add(kw);
		if(kws.contains(BuffInfo.KeyWord.WOOD)||kws.contains(BuffInfo.KeyWord.FROZEN)||atk<=0||wind>=getMaxWind())return 0;
		if(wind>=0)return 2;
		if(kws.contains(BuffInfo.KeyWord.CHARGE))return 2;
		if(kws.contains(BuffInfo.KeyWord.RUSH))return 1;
		return 0;
	}
	
	public int getWind()
	{
		return wind;
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
	
	public boolean isDying()
	{
		return dying;
	}
	
	public void kill()
	{
		dying=true;
	}
	
	public void loseBuff(Buff buff)
	{
		int ind=buffs.indexOf(buff);
		if(ind<0)return;
		buff.triggerSelf(game,new LoseBuffEvent());
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
		if(this.HP>maxHP)
		{
			this.HP=maxHP;
			if(this.HP<=0)dying=true;
		}
		game.broadcast(Game.Msg.CHANGEPP,new JSONObject(new Object[][]{{"hash",hashCode()},{"atk",getAtk()},{"maxhp",maxHP},{"hp",this.HP}}),-1);
	}
	
	public void restoreHealth(Card from,int count)
	{
		if(HP>=maxHP)return;
		int c=maxHP-HP;
		if(count<c)c=count;
		HP+=count;
		JSONObject toSend=new JSONObject(new Object[][]{{"tohash",hashCode()},{"num",c}});
		if(from!=null)toSend.put("fromhash",from.hashCode());
		game.broadcast(Game.Msg.HEAL,toSend,-1);
	}
	
	public void silence()
	{
		Iterator<Buff> it = buffs.iterator();
		while (it.hasNext())
		{
			Buff b = it.next();
			if (!b.info.isEffect)it.remove();
		}
		pp(-datk,-dHP,true);
	}
}
