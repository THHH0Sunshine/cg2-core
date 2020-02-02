package sunshine.cg2.core.game;

public class CardInfo {

	public enum Type
	{
		NONE,
		MINION,
		SPELL,
		WEAPON,
		HERO,
		SKILL,
		SPECIAL,
	}
	
	public enum Clz
	{
		NONE,
		DEATHKNIGHT,
		DRUID,
		HUNTER,
		MAGE,
		PALADIN,
		PRIEST,
		ROGUE,
		SHAMAN,
		WARLOCK,
		WARRIOR,
	}
	
	public enum Tag
	{
		BEAST,
		DRAGON,
		MURLOC,
		ELEMENT,
		TOTEM,
		DEMON,
		MECH,
		BATTLECRY,
		COMBO,
	}
	
	public final String name;
	public final Clz clz;
	public final Tag[] tags;
	public final Type type;
	public final boolean canPlay;
	public final int cost;
	public final int atk;
	public final int HP;
	public final boolean shield;
	public final BuffInfo[] buffs;
	public final int choices;
	public final CardInfo skill;
	
	public CardInfo(String name,Clz clz,Tag[] tags,Type type,boolean canPlay,int cost,int atk,int HP,boolean shield,BuffInfo[] buffs,int choices,CardInfo skill)
	{
		this.name=name;
		this.clz=clz;
		this.tags=tags;
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
	
	public void doBattlecry(Card card,Player player,Card target,int choi) throws GameOverThrowable
	{
	}
}
