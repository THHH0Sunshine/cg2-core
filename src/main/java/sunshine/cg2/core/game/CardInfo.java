package sunshine.cg2.core.game;

import sunshine.cg2.core.util.JSONObject;

public class CardInfo {

	public enum Type
	{
		NONE,
		MINION,
		SPELL,
		WEAPON,
		HERO,
		SPECIAL,
	}
	
	public final String name;
	public final Type type;
	public final boolean canPlay;
	public final int cost;
	public final int atk;
	public final int HP;
	public final boolean shield;
	public final BuffInfo[] buffs;
	public final int choices;
	public final CardInfo skill;
	
	public CardInfo(String name,Type type,boolean canPlay,int cost,int atk,int HP,boolean shield,BuffInfo[] buffs,int choices,CardInfo skill)
	{
		this.name=name;
		this.type=type;
		this.canPlay=canPlay;
		this.cost=cost;
		this.atk=atk;
		this.HP=HP;
		this.shield=shield;
		this.buffs=buffs;
		this.choices=choices<=0?1:choices;
		this.skill=skill;
	}
	
	public boolean canTarget(Card card,Player player,Card target,int choi)
	{
		return false;
	}
	
	public boolean canAttack(Card card,Card target)
	{
		return true;
	}
	
	public boolean changeHero(Player player,Card oldHero,Card newHero)
	{
		return false;
	}
	
	public void doBattlecry(Card card,Player player,Card target,int choi)
	{
	}
	
	public JSONObject getHandTags(Card card)
	{
		return null;
	}
	
	public JSONObject getTableTags(Card card)
	{
		return null;
	}
}
