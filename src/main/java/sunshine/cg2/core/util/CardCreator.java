package sunshine.cg2.core.util;

import java.util.ArrayList;

import sunshine.cg2.core.game.BuffInfo;
import sunshine.cg2.core.game.Card;
import sunshine.cg2.core.game.CardInfo;
import sunshine.cg2.core.game.GameOverThrowable;
import sunshine.cg2.core.game.Player;
import sunshine.cg2.core.game.CardInfo.Tag;

public class CardCreator {

	private static class Impl extends CardInfo
	{
		private final CardFunction function;
		
		Impl(CardCreator creator)
		{
			super(
				creator.name,
				creator.clz,
				creator.tags(),
				creator.type,
				creator.canPlay,
				creator.cost,
				creator.atk,
				creator.HP,
				creator.shield,
				creator.buffs(),
				creator.choices,
				creator.skill
			);
			function = (creator.function == null ? new CardFunction() : creator.function);
			creator.clear();
		}
		
		@Override
		public boolean canTarget(Card card,Player player,Card target,int choi)
		{
			return function.canTarget(card, player, target, choi);
		}
		
		@Override
		public void doBattlecry(Card card,Player player,Card target,int choi) throws GameOverThrowable
		{
			function.doBattlecry(card, player, target, choi);
		}
	}
	
	public static class CardFunction
	{
		public boolean canTarget(Card card,Player player,Card target,int choi)
		{
			return target == null;
		}
		
		public void doBattlecry(Card card,Player player,Card target,int choi) throws GameOverThrowable
		{
		}
	}
	
	private String packageName;
	private String name;
	private CardInfo.Clz clz;
	private final ArrayList<CardInfo.Tag> tags = new ArrayList<>();
	private CardInfo.Type type;
	private boolean canPlay = true;
	private int cost;
	private int atk;
	private int HP;
	private boolean shield;
	private final ArrayList<BuffInfo> buffs = new ArrayList<>();
	private int choices = 1;
	private CardInfo skill;
	private CardFunction function;
	
	private BuffInfo[] buffs()
	{
		return buffs.toArray(new BuffInfo[0]);
	}
	
	private void clear()
	{
		name = null;
		clz = null;
		tags.clear();
		type = null;
		canPlay = true;
		cost = 0;
		atk = 0;
		HP = 0;
		shield = false;
		buffs.clear();
		choices = 1;
		skill = null;
		function = null;
	}
	
	private CardInfo.Tag[] tags()
	{
		return tags.toArray(new CardInfo.Tag[0]);
	}
	
	public CardCreator(String packageName)
	{
		this.packageName = packageName;
	}
	
	public CardCreator atk(int atk)
	{
		this.atk = atk;
		return this;
	}
	
	public CardCreator battlecry(CardFunction function)
	{
		this.function = function;
		this.tags.add(Tag.BATTLECRY);
		return this;
	}
	
	public CardCreator buffs(BuffInfo... buffs)
	{
		for (BuffInfo b: buffs)
			this.buffs.add(b);
		return this;
	}
	
	public CardCreator cannotPlay()
	{
		canPlay = false;
		return this;
	}
	
	public CardCreator choices(int choices)
	{
		if (choices < 1)
			throw new IllegalArgumentException("choices cannot be fewer than 1");
		this.choices = choices;
		return this;
	}
	
	public CardCreator clz(CardInfo.Clz clz)
	{
		this.clz = clz;
		return this;
	}
	
	public CardCreator cost(int cost)
	{
		this.cost = cost;
		return this;
	}
	
	public CardInfo create()
	{
		if (name == null)
			throw new NullPointerException("name not set");
		if (clz == null)
			throw new NullPointerException("clz not set");
		if (type == null)
			throw new NullPointerException("type not set");
		return new Impl(this);
	}
	
	public CardCreator divineShield()
	{
		this.shield = true;
		return this;
	}
	
	public CardCreator fullName(String name)
	{
		this.name = name;
		return this;
	}
	
	public CardCreator function(CardFunction function)
	{
		this.function = function;
		return this;
	}
	
	public CardCreator hide()
	{
		if (name == null)
			throw new NullPointerException("name not set");
		name = "~" + name;
		return this;
	}
	
	public CardCreator HP(int HP)
	{
		this.HP = HP;
		return this;
	}
	
	public CardCreator keyWords(BuffInfo.KeyWord... keyWords)
	{
		this.buffs.add(new BuffInfo(keyWords, null, false));
		return this;
	}
	
	public CardCreator name(String name)
	{
		this.name = packageName + ":" + name;
		return this;
	}
	
	public CardCreator neutralMinion()
	{
		this.clz = CardInfo.Clz.NONE;
		this.type = CardInfo.Type.MINION;
		return this;
	}
	
	public CardCreator setPackageName(String packageName)
	{
		this.packageName=packageName;
		return this;
	}
	
	public CardCreator skill(CardInfo skill)
	{
		this.skill = skill;
		return this;
	}
	
	public CardCreator stature(int cost, int atk, int HP)
	{
		this.cost = cost;
		this.atk = atk;
		this.HP = HP;
		return this;
	}
	
	public CardCreator tags(CardInfo.Tag... tags)
	{
		for (CardInfo.Tag r: tags)
			this.tags.add(r);
		return this;
	}
	
	public CardCreator type(CardInfo.Type type)
	{
		this.type = type;
		return this;
	}
}
