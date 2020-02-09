package sunshine.cg2.core.library.hearthstone;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import sunshine.cg2.core.game.Buff;
import sunshine.cg2.core.game.BuffInfo;
import sunshine.cg2.core.game.Card;
import sunshine.cg2.core.game.CardInfo;
import sunshine.cg2.core.game.Game;
import sunshine.cg2.core.game.GameOverThrowable;
import sunshine.cg2.core.game.Player;
import sunshine.cg2.core.game.BuffInfo.KeyWord;
import sunshine.cg2.core.game.Card.Position;
import sunshine.cg2.core.game.CardInfo.Clz;
import sunshine.cg2.core.game.CardInfo.Tag;
import sunshine.cg2.core.game.CardInfo.Type;
import sunshine.cg2.core.game.event.Event;
import sunshine.cg2.core.game.event.globalevent.AttackingEvent;
import sunshine.cg2.core.game.event.globalevent.DamagedEvent;
import sunshine.cg2.core.game.event.globalevent.HealedEvent;
import sunshine.cg2.core.game.event.globalevent.SummonEvent;
import sunshine.cg2.core.game.event.globalevent.TurnEndEvent;
import sunshine.cg2.core.library.Cards;
import sunshine.cg2.core.library.Cards.DamageFunction;
import sunshine.cg2.core.library.Cards.HealingFunction;
import sunshine.cg2.core.library.Cards.KWBuffInfo;
import sunshine.cg2.core.library.Cards.KWEffectBuffInfo;
import sunshine.cg2.core.library.Cards.MyMinionBuffInfo;
import sunshine.cg2.core.library.Cards.MyOtherMinionBuffInfo;
import sunshine.cg2.core.library.Cards.NearbyMinionBuffInfo;
import sunshine.cg2.core.library.Cards.OtherMinionBuffInfo;
import sunshine.cg2.core.library.Cards.PPEffectBuffInfo;
import sunshine.cg2.core.library.Cards.SpellPowerBuffInfo;
import sunshine.cg2.core.library.Cards.SummonRightFunction;
import sunshine.cg2.core.library.Cards.ThisTurnBuffInfo;
import sunshine.cg2.core.library.Cards.ThisTurnPPBuffInfo;
import sunshine.cg2.core.util.CardCreator;
import sunshine.cg2.core.util.CardCreator.CardFunction;

public class BasicCards
{
	private BasicCards() {}
	
	private static void register(CardInfo card)
	{
		Cards.DEFAULT_LIBRARY.put(card.name, card);
	}
	
	public static final String[] basicTotems = new String[]
	{
		"~hs.basic:zrtt",
		"~hs.basic:kqzntt",
		"~hs.basic:zltt",
		"~hs.basic:sztt"
	};
	
	public static int getTotemFlagFromField(List<? extends Card> field)
	{
		int rt = 0;
		for (Card c: field)
			for (int i = 0; i < 4; i++)
				if (basicTotems[i] == c.info.name)
				{
					rt |= (1 << i);
					break;
				}
		return rt;
	}
	
	/*
	hs.basic:ajf
	hs.basic:albkbhz
	hs.basic:alxz
	hs.basic:alzyz
	hs.basic:asfd
	hs.basic:assj
	hs.basic:aszh
	hs.basic:ayj
	hs.basic:aysm
	hs.basic:ayst
	hs.basic:bc
	hs.basic:bfcqs
	hs.basic:bfcys
	hs.basic:bfxr
	hs.basic:bhzs
	hs.basic:bjms
	hs.basic:bszj
	hs.basic:bxs
	hs.basic:cbhwbb
	hs.basic:cf
	hs.basic:ckzr
	hs.basic:cs
	hs.basic:cyzf
	hs.basic:dcsj
	hs.basic:dfs
	hs.basic:dlrfs
	hs.basic:dpgd
	hs.basic:dr
	hs.basic:ds
	hs.basic:dse
	hs.basic:dwhb
	hs.basic:dyly
	hs.basic:fn
	hs.basic:fnzc
	hs.basic:fss
	hs.basic:fx
	hs.basic:fyz
	hs.basic:gcsxt
	hs.basic:glbskbz
	hs.basic:gtrdbs
	hs.basic:hbj
	hs.basic:hqs
	hs.basic:hs
	hs.basic:hstt
	hs.basic:hys
	hs.basic:jh
	hs.basic:jjczz
	hs.basic:jp
	hs.basic:jskz
	hs.basic:jx
	hs.basic:jxyljg
	hs.basic:kjdyh
	hs.basic:kkljyws
	hs.basic:lhzh
	hs.basic:llzf
	hs.basic:lmhjb
	hs.basic:lmtzb
	hs.basic:lqb
	hs.basic:lryj
	hs.basic:lszs
	hs.basic:lwsw
	hs.basic:lyfb
	hs.basic:lzqzg
	hs.basic:mbs
	hs.basic:mg
	hs.basic:mq
	hs.basic:pscyjs
	hs.basic:qx
	hs.basic:rheq
	hs.basic:sgdzy
	hs.basic:sgs
	hs.basic:sgsy
	hs.basic:shwq
	hs.basic:sjcdws
	hs.basic:sjzbb
	hs.basic:slbb
	hs.basic:sldj
	hs.basic:sll
	hs.basic:slml
	hs.basic:spz
	hs.basic:sqsrm
	hs.basic:srmfs
	hs.basic:sscj
	hs.basic:ssxx
	hs.basic:sszl
	hs.basic:swcr
	hs.basic:sx
	hs.basic:sxzzrng
	hs.basic:sys
	hs.basic:syyz
	hs.basic:tdls
	hs.basic:tdlx
	hs.basic:tlbhqs
	hs.basic:ttzl
	hs.basic:tzhx
	hs.basic:wysz
	hs.basic:wzzf
	hs.basic:xfz
	hs.basic:xhs
	hs.basic:xkxz
	hs.basic:xlsj
	hs.basic:xqsm
	hs.basic:xsqy
	hs.basic:xss
	hs.basic:xzxml
	hs.basic:xzzl
	hs.basic:yhs
	hs.basic:yjbnz
	hs.basic:ympx
	hs.basic:yrck
	hs.basic:yrlcz
	hs.basic:ys
	hs.basic:yx
	hs.basic:yxcz
	hs.basic:yxyj
	hs.basic:yydj
	hs.basic:zgzhg
	hs.basic:zj
	hs.basic:zlzc
	hs.basic:zmyg
	hs.basic:zrfmj
	hs.basic:zs
	hs.basic:zysd
	hs.basic:zysj
	hs.basic:zzkl
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
	~hs.basic:jxyl
	~hs.basic:lok
	~hs.basic:my
	~hs.basic:ms
	~hs.basic:qw
	~hs.basic:xedd
	~hs.basic:xyb
	~hs.basic:yrch
	~hs.basic:yz
	*/
	public static void init()
	{
		CardCreator cc=new CardCreator("hs.basic");
		CardInfo ci;
		register(cc.name("xyb").hide().clz(Clz.NONE).type(Type.SPELL).cost(0)
			.function(new CardFunction()
			{
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					player.gainEmptyCoins(1,true);
					player.fillCoins(1);
				}
			}).create());
		ci=cc.name("hpdruid").hide().clz(Clz.DRUID).type(Type.SKILL).cost(2)
			.function(new CardFunction()
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
		register(cc.name("yhs").clz(Clz.DRUID).type(Type.SPELL).function(new DamageFunction(1)).create());
		register(cc.name("jh").clz(Clz.DRUID).type(Type.SPELL).cost(0)
			.function(new CardFunction()
			{
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					player.gainEmptyCoins(1,true);
					player.fillCoins(1);
				}
			}).create());
		register(cc.name("zj").clz(Clz.DRUID).type(Type.SPELL).cost(1)
			.function(new CardFunction()
			{
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					player.gainArmor(2);
					player.getHero().gainBuff(new ThisTurnPPBuffInfo(2,0),"",null);
				}
			}).create());
		register(cc.name("yxyj").clz(Clz.DRUID).type(Type.SPELL).cost(2)
			.function(new CardFunction()
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
		register(cc.name("zlzc").clz(Clz.DRUID).type(Type.SPELL).cost(3).function(new HealingFunction(8)).create());
		register(cc.name("yxcz").clz(Clz.DRUID).type(Type.SPELL).cost(3)
			.function(new CardFunction()
			{
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					if(!player.gainEmptyCoins(1,false))player.obtain(player.getGame().createCard("~hs.basic:flgs",-1));
				}
			}).create());
		register(cc.name("flgs").hide().clz(Clz.DRUID).type(Type.SPELL)
			.function(new CardFunction()
			{
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					player.draw(1);
				}
			}).create());
		register(cc.name("ympx").clz(Clz.DRUID).type(Type.SPELL).cost(3)
			.function(new CardFunction()
			{
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					BuffInfo bi = new ThisTurnPPBuffInfo(2,0);
					player.getHero().gainBuff(bi,"hs.basic:ympx",null);
					for(Card c:player.getField())if(c.getPosition()==Position.MINION)c.gainBuff(bi,"hs.basic:ympx",null);
				}
			}).create());
		register(cc.name("hs").clz(Clz.DRUID).type(Type.SPELL).cost(4)
			.function(new DamageFunction(4)
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
			.function(new DamageFunction(5)
			{
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					super.doBattlecry(card,player,target,choi);
					player.draw(1);
				}
			}).create());
		register(cc.name("albkbhz").clz(Clz.DRUID).type(Type.MINION).stature(8,8,8).keyWords(KeyWord.TAUNT).create());
		ci=cc.name("hphunter").hide().clz(Clz.HUNTER).type(Type.SKILL).cost(2)
			.function(new CardFunction()
			{
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					player.getNextPlayer().getHero().takeDamage(card,2);
				}
			}).create();
		register(ci);
		register(cc.name("hhunter").hide().clz(Clz.HUNTER).type(Type.HERO).cannotPlay().HP(30).skill(ci).create());
		register(cc.name("assj").clz(Clz.HUNTER).type(Type.SPELL).cost(1).function(new DamageFunction(2)).create());
		register(cc.name("sll").clz(Clz.HUNTER).type(Type.MINION).tags(Tag.BEAST).stature(1,1,1)
			.buffs(new MyOtherMinionBuffInfo(new PPEffectBuffInfo(1,0),"hs.basic:sll")
			{
				@Override protected boolean filter(Buff buff,Card card)
				{
					return super.filter(buff,card)&&card.hasTag(Tag.BEAST);
				}
			}).create());
		register(cc.name("zzs").clz(Clz.HUNTER).type(Type.SPELL).cost(1)
			.function(new CardFunction()
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
			.function(new CardFunction()
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
			.function(new CardFunction()
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
		register(cc.name("hf").hide().clz(Clz.HUNTER).type(Type.MINION).tags(Tag.BEAST).stature(3,4,2).keyWords(KeyWord.CHARGE).create());
		register(cc.name("ms").hide().clz(Clz.HUNTER).type(Type.MINION).tags(Tag.BEAST).stature(3,4,4).keyWords(KeyWord.TAUNT).create());
		register(cc.name("lok").hide().clz(Clz.HUNTER).type(Type.MINION).tags(Tag.BEAST).stature(3,2,4).buffs(new MyOtherMinionBuffInfo(new PPEffectBuffInfo(1,0),"~hs.basic:lok")).create());
		register(cc.name("slml").clz(Clz.HUNTER).type(Type.SPELL).cost(3)
			.function(new DamageFunction(3)
			{
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					for(Card c:player.getField())
						if(c.getPosition()==Position.MINION&&c.hasTag(Tag.BEAST))
						{
							target.takeDamage(card,5);
							return;
						}
					super.doBattlecry(card,player,target,choi);
				}
			}).create());
		register(cc.name("dcsj").clz(Clz.HUNTER).type(Type.SPELL).cost(4)
			.function(new CardFunction()
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
		register(cc.name("xss").clz(Clz.HUNTER).type(Type.MINION).stature(4,4,3)
			.battlecry(new CardFunction()
			{
				@Override public boolean canTarget(Card card,Player player,Card target,int choi)
				{
					HashSet<Card> cs=new HashSet<>();
					for(Card c:player.getField())
						if(c.getPosition()==Position.MINION&&c.hasTag(Tag.BEAST))
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
		register(cc.name("tyxn").clz(Clz.HUNTER).type(Type.MINION).tags(Tag.BEAST).stature(5,2,5)
			.buffs(new MyMinionBuffInfo(new KWEffectBuffInfo(KeyWord.CHARGE),"hs.basic:tyxn")
			{
				@Override protected boolean filter(Buff buff,Card card)
				{
					return super.filter(buff,card)&&card.hasTag(Tag.BEAST);
				}
			}).create());
		register(cc.name("jedtj").clz(Clz.HUNTER).type(Type.MINION).tags(Tag.BEAST).stature(5,3,2)
			.buffs(new BuffInfo(null,new Object[]{SummonEvent.class},false)
			{
				@Override public void onTrigger(Buff buff,Event event)
				{
					Card toSummon=((SummonEvent)event).minion;
					if(toSummon==buff.toBuff)return;
					Player player=buff.toBuff.getOwner();
					if(toSummon.getOwner()==player&&toSummon.hasTag(Tag.BEAST))
						player.draw(1);
				}
			}).create());
		ci=cc.name("hpmage").hide().clz(Clz.MAGE).type(Type.SKILL).cost(2).function(new DamageFunction(1)).create();
		register(ci);
		register(cc.name("hmage").hide().clz(Clz.MAGE).type(Type.HERO).cannotPlay().HP(30).skill(ci).create());
		register(cc.name("asfd").clz(Clz.MAGE).type(Type.SPELL).cost(1)
			.function(new CardFunction()
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
			.function(new CardFunction()
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
		register(cc.name("jx").hide().clz(Clz.MAGE).type(Type.MINION).stature(1,0,2).keyWords(KeyWord.TAUNT).create());
		register(cc.name("hbj").clz(Clz.MAGE).type(Type.SPELL).cost(2)
			.function(new DamageFunction(3)
			{
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					super.doBattlecry(card,player,target,choi);
					target.freeze();
				}
			}).create());
		register(cc.name("mbs").clz(Clz.MAGE).type(Type.SPELL).cost(2)
			.function(new CardFunction()
			{
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					player.getNextPlayer().getAllMinions().forEach(c->c.takeDamageWithoutCheck(card,1));
					player.getGame().checkForDamage();
				}
			}).create());
		register(cc.name("bsxx").clz(Clz.MAGE).type(Type.SPELL).cost(3)
			.function(new CardFunction()
			{
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					player.getNextPlayer().getAllMinions().forEach(c->c.freeze());
				}
			}).create());
		register(cc.name("aszh").clz(Clz.MAGE).type(Type.SPELL).cost(3)
			.function(new CardFunction()
			{
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					player.draw(2);
				}
			}).create());
		register(cc.name("bxs").clz(Clz.MAGE).type(Type.SPELL).cost(4)
			.function(new CardFunction()
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
		register(cc.name("sys").clz(Clz.MAGE).type(Type.MINION).tags(Tag.ELEMENT).stature(4,3,6)
			.buffs(Cards.freezing).create());
		register(cc.name("hqs").clz(Clz.MAGE).type(Type.SPELL).cost(4).function(new DamageFunction(6)).create());
		register(cc.name("lyfb").clz(Clz.MAGE).type(Type.SPELL).cost(7)
			.function(new CardFunction()
			{
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					player.getNextPlayer().getAllMinions().forEach(c->c.takeDamageWithoutCheck(card,4));
					player.getGame().checkForDamage();
				}
			}).create());
		ci=cc.name("hppaladin").hide().clz(Clz.PALADIN).type(Type.SKILL).cost(2)
			.function(new CardFunction()
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
			.function(new CardFunction()
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
			.function(new CardFunction()
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
			.function(new CardFunction()
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
		register(cc.name("sgs").clz(Clz.PALADIN).type(Type.SPELL).cost(2).function(new HealingFunction(6)).create());
		register(cc.name("fx").clz(Clz.PALADIN).type(Type.SPELL).cost(4)
			.function(new CardFunction()
			{
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					Player nextPlayer=player.getNextPlayer();
					nextPlayer.getHero().takeDamageWithoutCheck(card,2);
					nextPlayer.getAllMinions().forEach(c->c.takeDamageWithoutCheck(card,2));
					player.getGame().checkForDamage();
				}
			}).create());
		register(cc.name("fnzc").clz(Clz.PALADIN).type(Type.SPELL).cost(4)
			.function(new DamageFunction(3)
			{
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					super.doBattlecry(card,player,target,choi);
					player.draw(1);
				}
			}).create());
		register(cc.name("wzzf").clz(Clz.PALADIN).type(Type.SPELL).cost(4)
			.function(new CardFunction()
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
		register(cc.name("lwsw").clz(Clz.PALADIN).type(Type.MINION).stature(7,5,6)
			.battlecry(new CardFunction()
			{
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					player.getHero().heal(card,6);
				}
			}).create());
		ci=cc.name("hppriest").hide().clz(Clz.PRIEST).type(Type.SKILL).cost(2).function(new HealingFunction(2)).create();
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
			.function(new CardFunction()
			{
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					player.getHero().heal(card,5);
				}
			}).create());
		register(cc.name("xlsj").clz(Clz.PRIEST).type(Type.SPELL).cost(1)
			.function(new CardFunction()
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
			.function(new CardFunction()
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
		register(cc.name("sscj").clz(Clz.PRIEST).type(Type.SPELL).cost(1).function(new DamageFunction(2)).create());
		register(cc.name("ayst").clz(Clz.PRIEST).type(Type.SPELL).cost(2)
			.function(new CardFunction()
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
			.function(new CardFunction()
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
			.function(new CardFunction()
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
			.function(new CardFunction()
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
			.function(new CardFunction()
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
			.function(new CardFunction()
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
			.function(new DamageFunction(2)
			{
				@Override public boolean canTarget(Card card,Player player,Card target,int choi)
				{
					return super.canTarget(card,player,target,choi)&&target.getPosition()==Position.MINION&&!target.isDamaged();
				}
			}).create());
		register(cc.name("yx").clz(Clz.ROGUE).type(Type.SPELL).cost(1)
			.function(new CardFunction()
			{
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					player.getNextPlayer().getHero().takeDamage(card,3);
				}
			}).create());
		register(cc.name("zmyg").clz(Clz.ROGUE).type(Type.SPELL).cost(1)
			.function(new CardFunction()
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
			.function(new DamageFunction(1)
			{
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					super.doBattlecry(card,player,target,choi);
					player.draw(1);
				}
			}).create());
		register(cc.name("mg").clz(Clz.ROGUE).type(Type.SPELL).cost(2)
			.function(new CardFunction()
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
						p.moveFieldAway(target);
						p.obtain(game.createClear(target));
					}
				}
			}).create());
		register(cc.name("ds").clz(Clz.ROGUE).type(Type.SPELL).cost(3)
			.function(new CardFunction()
			{
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					player.getNextPlayer().getAllMinions().forEach(c->c.takeDamageWithoutCheck(card,1));
					player.getGame().checkForDamage();
					player.draw(1);
				}
			}).create());
		register(cc.name("wysz").clz(Clz.ROGUE).type(Type.MINION).stature(4,3,3)
			.battlecry(new CardFunction()
			{
				@Override public boolean canTarget(Card card,Player player,Card target,int choi)
				{
					return target!=null&&target.getOwner()==player&&target.getPosition()==Position.MINION;
				}
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					target.gainBuff(Cards.poisonous,"hs.basic:wysz",null);
				}
			}).create());
		register(cc.name("ckzr").clz(Clz.ROGUE).type(Type.WEAPON).stature(5,3,4).create());
		register(cc.name("cs").clz(Clz.ROGUE).type(Type.SPELL).cost(5)
			.function(new CardFunction()
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
			.function(new CardFunction()
			{
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					player.draw(4);
				}
			}).create());
		ci=cc.name("hpshaman").hide().clz(Clz.SHAMAN).type(Type.SKILL).cost(2)
			.function(new CardFunction()
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
		register(cc.fullName(basicTotems[0]).clz(Clz.SHAMAN).type(Type.MINION).tags(Tag.TOTEM).stature(1,1,1).create());
		register(cc.fullName(basicTotems[1]).clz(Clz.SHAMAN).type(Type.MINION).tags(Tag.TOTEM).stature(1,0,2).buffs(new SpellPowerBuffInfo(1)).create());
		register(cc.fullName(basicTotems[2]).clz(Clz.SHAMAN).type(Type.MINION).tags(Tag.TOTEM).stature(1,0,2)
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
		register(cc.fullName(basicTotems[3]).clz(Clz.SHAMAN).type(Type.MINION).tags(Tag.TOTEM).stature(1,0,2).keyWords(KeyWord.TAUNT).create());
		register(cc.name("hshaman").hide().clz(Clz.SHAMAN).type(Type.HERO).cannotPlay().HP(30).skill(ci).create());
		register(cc.name("xzzl").clz(Clz.SHAMAN).type(Type.SPELL).cost(0)
			.function(new CardFunction()
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
			.function(new CardFunction()
			{
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					for(Card c:player.getAllMinions())if(c.hasTag(Tag.TOTEM))c.pp(0,2);
				}
			}).create());
		register(cc.name("bszj").clz(Clz.SHAMAN).type(Type.SPELL).cost(1)
			.function(new DamageFunction(1)
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
			.function(new CardFunction()
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
			.function(new CardFunction()
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
		register(cc.name("hstt").clz(Clz.SHAMAN).type(Type.MINION).tags(Tag.TOTEM).stature(3,0,3).buffs(new NearbyMinionBuffInfo(new PPEffectBuffInfo(2,0),"hs.basic:hstt")).create());
		register(cc.name("ys").clz(Clz.SHAMAN).type(Type.SPELL).cost(4)
			.function(new CardFunction()
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
		register(cc.name("qw").hide().clz(Clz.SHAMAN).type(Type.MINION).tags(Tag.BEAST).stature(1,0,1).keyWords(KeyWord.TAUNT).create());
		register(cc.name("fyz").clz(Clz.SHAMAN).type(Type.MINION).stature(4,3,3)
			.battlecry(new CardFunction()
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
			.function(new CardFunction()
			{
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					BuffInfo bi = new ThisTurnPPBuffInfo(3,0);
					for(Card c:player.getField())if(c.getPosition()==Position.MINION)c.gainBuff(bi,"hs.basic:sx",null);
				}
			}).create());
		register(cc.name("hys").clz(Clz.SHAMAN).type(Type.MINION).tags(Tag.ELEMENT).stature(6,6,5)
			.battlecry(new DamageFunction(3)).create());
		ci=cc.name("hpwarlock").hide().clz(Clz.WARLOCK).type(Type.SKILL).cost(2)
			.function(new CardFunction()
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
			.function(new CardFunction()
			{
				@Override public boolean canTarget(Card card,Player player,Card target,int choi)
				{
					return target!=null&&target.hasTag(Tag.DEMON);
				}
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					target.kill();
					player.getHero().heal(card,5);
				}
			}).create());
		register(cc.name("swcr").clz(Clz.WARLOCK).type(Type.SPELL).cost(1)
			.function(new DamageFunction(1)
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
			.function(new DamageFunction(4)
			{
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					super.doBattlecry(card,player,target,choi);
					player.discardHand(1);
				}
			}).create());
		register(cc.name("fss").clz(Clz.WARLOCK).type(Type.SPELL).cost(1)
			.function(new CardFunction()
			{
				@Override public boolean canTarget(Card card,Player player,Card target,int choi)
				{
					return target!=null&&target.getOwner()!=player&&target.getPosition()==Position.MINION;
				}
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					target.gainBuff(Cards.corrupted,"hs.basic:fss",null);
				}
			}).create());
		register(cc.name("xkxz").clz(Clz.WARLOCK).type(Type.MINION).tags(Tag.DEMON).stature(1,1,3).keyWords(KeyWord.TAUNT).create());
		register(cc.name("mq").clz(Clz.WARLOCK).type(Type.MINION).tags(Tag.DEMON).stature(2,4,3)
			.battlecry(new CardFunction()
			{
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					player.discardHand(1);
				}
			}).create());
		register(cc.name("xqsm").clz(Clz.WARLOCK).type(Type.SPELL).cost(3)
			.function(new DamageFunction(2)
			{
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					super.doBattlecry(card,player,target,choi);
					player.getHero().heal(card,2);
				}
			}).create());
		register(cc.name("ayj").clz(Clz.WARLOCK).type(Type.SPELL).cost(3)
			.function(new DamageFunction(4)
			{
				@Override public boolean canTarget(Card card,Player player,Card target,int choi)
				{
					return super.canTarget(card,player,target,choi)&&target.getPosition()==Position.MINION;
				}
			}).create());
		register(cc.name("dyly").clz(Clz.WARLOCK).type(Type.SPELL).cost(4)
			.function(new CardFunction()
			{
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					Game game=player.getGame();
					game.forEachCardOnTable(c->{if(c.positionIsMinionOrHero())c.takeDamageWithoutCheck(card,3);});
					game.checkForDamage();
				}
			}).create());
		register(cc.name("kjdyh").clz(Clz.WARLOCK).type(Type.MINION).tags(Tag.DEMON).stature(6,6,6)
			.battlecry(new CardFunction()
			{
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					Game game=player.getGame();
					game.forEachCardOnTable(c->{if(c!=card&&c.positionIsMinionOrHero())c.takeDamageWithoutCheck(card,1);});
					game.checkForDamage();
				}
			}).create());
		ci=cc.name("hpwarrior").hide().clz(Clz.WARRIOR).type(Type.SKILL).cost(2)
			.function(new CardFunction()
			{
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					player.gainArmor(2);
				}
			}).create();
		register(ci);
		register(cc.name("hwarrior").hide().clz(Clz.WARRIOR).type(Type.HERO).cannotPlay().HP(30).skill(ci).create());
		register(cc.name("cf").clz(Clz.WARRIOR).type(Type.SPELL).cost(1)
			.function(new CardFunction()
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
			.function(new CardFunction()
			{
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					Game game=player.getGame();
					game.forEachCardOnTable(c->{if(c.getPosition()==Position.MINION)c.takeDamageWithoutCheck(card,1);});
					game.checkForDamage();
				}
			}).create());
		register(cc.name("yydj").clz(Clz.WARRIOR).type(Type.SPELL).cost(2)
			.function(new CardFunction()
			{
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					player.getHero().gainBuff(new ThisTurnPPBuffInfo(4,0),"hs.basic:yydj",null);
				}
			}).create());
		register(cc.name("zs").clz(Clz.WARRIOR).type(Type.SPELL).cost(2)
			.function(new CardFunction()
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
			.function(new CardFunction()
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
			.function(new CardFunction()
			{
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					player.gainArmor(5);
					player.draw(1);
				}
			}).create());
		register(cc.name("kkljyws").clz(Clz.WARRIOR).type(Type.MINION).stature(4,4,3)
			.keyWords(KeyWord.CHARGE).create());
		register(cc.name("ajf").clz(Clz.WARRIOR).type(Type.WEAPON).stature(5,5,2).create());
		register(cc.name("syyz").neutralMinion().tags(Tag.BEAST).stature(1,1,1)
			.keyWords(KeyWord.CHARGE).create());
		register(cc.name("wy").neutralMinion().stature(1,2,1)
			.battlecry(new HealingFunction(2)).create());
		register(cc.name("yrxjz").neutralMinion().tags(Tag.MURLOC).stature(1,2,1).create());
		register(cc.name("sjzbb").neutralMinion().stature(1,1,2)
			.keyWords(KeyWord.TAUNT).create());
		register(cc.name("alxz").neutralMinion().tags(Tag.MURLOC).stature(1,1,1)
			.buffs(new OtherMinionBuffInfo(new PPEffectBuffInfo(1,0),"hs.basic:alxz")
			{
				@Override protected boolean filter(Buff buff,Card card)
				{
					return super.filter(buff,card)&&card.hasTag(Tag.MURLOC);
				}
			}).create());
		register(cc.name("jlgjs").neutralMinion().stature(1,1,1)
			.battlecry(new DamageFunction(1)).create());
		register(cc.name("gcsxt").neutralMinion().stature(2,1,1)
			.battlecry(new CardFunction()
			{
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					player.draw(1);
				}
			}).create());
		register(cc.name("dse").neutralMinion().tags(Tag.BEAST).stature(2,2,3).create());
		register(cc.name("xzxml").neutralMinion().tags(Tag.BEAST).stature(2,3,2).create());
		register(cc.name("slbb").neutralMinion().stature(2,2,2)
			.keyWords(KeyWord.TAUNT).create());
		register(cc.name("lszs").neutralMinion().tags(Tag.MURLOC).stature(2,2,1)
			.keyWords(KeyWord.CHARGE).create());
		register(cc.name("yrlcz").neutralMinion().tags(Tag.MURLOC).stature(2,2,1)
			.battlecry(new SummonRightFunction("~hs.basic:yrch")).create());
		register(cc.name("yrch").hide().neutralMinion().tags(Tag.MURLOC).stature(1,1,1).create());
		register(cc.name("gtrdbs").neutralMinion().stature(2,2,2)
			.buffs(new SpellPowerBuffInfo(1)).create());
		register(cc.name("sxzzrng").neutralMinion().stature(2,3,2)
			.battlecry(new CardFunction()
			{
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					player.getNextPlayer().throwWeapon();
				}
			}).create());
		register(cc.name("tdlx").neutralMinion().stature(3,2,2)
			.buffs(new MyOtherMinionBuffInfo(new PPEffectBuffInfo(1,0),"hs.basic:tdlx")).create());
		register(cc.name("yjbnz").neutralMinion().tags(Tag.ELEMENT).stature(3,5,1).create());
		register(cc.name("lqb").neutralMinion().stature(3,3,1)
			.keyWords(KeyWord.CHARGE).create());
		register(cc.name("tdls").neutralMinion().stature(3,2,3)
			.battlecry(new SummonRightFunction("~hs.basic:yz")).create());
		register(cc.name("yz").hide().neutralMinion().tags(Tag.BEAST).stature(1,1,1).create());
		register(cc.name("tzhx").neutralMinion().tags(Tag.BEAST).stature(3,3,3)
			.keyWords(KeyWord.TAUNT).create());
		register(cc.name("dlrfs").neutralMinion().stature(3,1,4)
			.buffs(new SpellPowerBuffInfo(1)).create());
		register(cc.name("tlbhqs").neutralMinion().stature(3,2,2)
			.battlecry(new DamageFunction(1)).create());
		register(cc.name("pscyjs").neutralMinion().stature(3,3,2)
			.battlecry(new CardFunction()
			{
				@Override public boolean canTarget(Card card,Player player,Card target,int choi)
				{
					return target!=null&&target.getOwner()==player&&target.getPosition()==Position.MINION;
				}
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					target.pp(1,1);
				}
			}).create());
		register(cc.name("ybzz").neutralMinion().tags(Tag.BEAST).stature(3,1,4)
			.keyWords(KeyWord.TAUNT).create());
		register(cc.name("lzqzg").neutralMinion().tags(Tag.BEAST).stature(4,2,7).create());
		register(cc.name("sjcdws").neutralMinion().stature(4,3,5)
			.keyWords(KeyWord.TAUNT).create());
		register(cc.name("srmfs").neutralMinion().stature(4,4,4)
			.buffs(new SpellPowerBuffInfo(1)).create());
		register(cc.name("bfxr").neutralMinion().stature(4,4,5).create());
		register(cc.name("jxyljg").neutralMinion().stature(4,2,4)
			.battlecry(new SummonRightFunction("~hs.basic:jxyl")).create());
		register(cc.name("jxyl").hide().neutralMinion().tags(Tag.MECH).stature(1,2,1).create());
		register(cc.name("zrfmj").neutralMinion().stature(4,2,4)
			.battlecry(new CardFunction()
			{
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					player.draw(1);
				}
			}).create());
		register(cc.name("bfcqs").neutralMinion().stature(4,2,5)
			.keyWords(KeyWord.CHARGE).create());
		register(cc.name("yrck").neutralMinion().stature(5,4,4)
			.battlecry(new CardFunction()
			{
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					player.getNextPlayer().getHero().takeDamage(card,3);
				}
			}).create());
		register(cc.name("glbskbz").neutralMinion().stature(5,2,7)
			.buffs(new BuffInfo(null,new Object[]{DamagedEvent.class},false)
			{
				@Override public void onTrigger(Buff buff,Event event)
				{
					if(((DamagedEvent)event).to!=buff.toBuff)return;
					buff.toBuff.pp(3,0);
				}
			}).create());
		register(cc.name("cbhwbb").neutralMinion().stature(5,5,4)
			.keyWords(KeyWord.TAUNT).create());
		register(cc.name("sldj").neutralMinion().stature(5,4,4)
			.battlecry(new CardFunction()
			{
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					int num=player.getMinionNum()-1;
					card.pp(num,num);
				}
			}).create());
		register(cc.name("lmtzb").neutralMinion().stature(5,4,2)
			.battlecry(new DamageFunction(2)).create());
		register(cc.name("alzyz").neutralMinion().stature(5,4,5)
			.battlecry(new CardFunction()
			{
				@Override public void doBattlecry(Card card,Player player,Card target,int choi)
				{
					for(Card c:player.getField())if(c.getPosition()==Position.MINION)c.healWithoutCheck(card,2);
					player.getHero().healWithoutCheck(card,2);
					player.getGame().checkForHeal();
				}
			}).create());
		register(cc.name("sqsrm").neutralMinion().stature(6,6,7).create());
		register(cc.name("lmhjb").neutralMinion().stature(6,5,2)
			.keyWords(KeyWord.CHARGE).create());
		register(cc.name("dfs").neutralMinion().stature(6,4,7)
			.buffs(new SpellPowerBuffInfo(1)).create());
		register(cc.name("jjczz").neutralMinion().stature(6,6,5)
			.keyWords(KeyWord.TAUNT).create());
		register(cc.name("zzkl").neutralMinion().stature(7,7,7).create());
		register(cc.name("rheq").neutralMinion().tags(Tag.BEAST).stature(7,9,5).create());
		register(cc.name("bfcys").neutralMinion().stature(7,6,6)
			.buffs(new MyOtherMinionBuffInfo(new PPEffectBuffInfo(1,1),"hs.basic:bfcys")).create());
		
		register(cc.fullName("cg2:test").clz(Clz.NONE).type(Type.SPELL).cost(1)
			.function(new CardFunction()
			{
				@Override public void doBattlecry(Card card,Player player,Card target,int choi) throws GameOverThrowable
				{
					Game game=player.getGame();
					Clz clz=player.getHero().info.clz;
					ArrayList<CardInfo> pool=game.selectFromStandardWhere(c->c.clz==clz||c.clz==Clz.NONE);
					if(pool.size()<=0)return;
					if(pool.size()==1){player.obtain(game.createCard(pool.get(0),-1));return;}
					ArrayList<CardInfo> discover;
					if(pool.size()>3)
					{
						discover=new ArrayList<>(3);
						for(int i=0;i<3;i++)discover.add(pool.remove((int)(Math.random()*pool.size())));
					}
					else discover=pool;
					Card[] cards=new Card[discover.size()];
					for(int i=0;i<discover.size();i++)cards[i]=game.createCard(discover.get(i),-1);
					int choice=player.askForDiscover(cards);
					player.obtain(cards[choice]);
				}
			}).create());
	}
}
