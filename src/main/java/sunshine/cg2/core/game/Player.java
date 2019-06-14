package sunshine.cg2.core.game;

import java.util.ArrayList;
import java.util.HashSet;

import sunshine.cg2.core.game.event.NormalLosingEvent;
import sunshine.cg2.core.game.event.globalevent.AfterTurnEndEvent;
import sunshine.cg2.core.game.event.globalevent.GlobalEvent;
import sunshine.cg2.core.game.event.globalevent.MinionEnterEvent;
import sunshine.cg2.core.game.event.globalevent.MinionLeaveEvent;
import sunshine.cg2.core.util.JSONArray;
import sunshine.cg2.core.util.JSONObject;

public class Player {

	private final Game game;
	private int index;
	private Card hero;
	private Card skill;
	private Card weapon;
	private final String[] cardSet;
	private int maxCoins;
	private int coins;
	private int extraCoins;
	private int p=1;
	private final ArrayList<Card> field;
	private final ArrayList<Card> hand;
	private final ArrayList<Card> deck;
	
	private void breakWeapon(Card weapon)
	{
		weapon.kill();
		game.broadcast(Game.Msg.BREAKWEAPON,new JSONObject(new Object[][]{{"who",index}}),-1);
	}
	
	private void changeHero(Card card)
	{
		game.unregisterOnTable(hero);
		game.registerOnTable(card);
		card.replaceOldHero(this,hero);
		hero=card;
		if(skill!=null)game.unregisterOnTable(skill);
		if(card.info.skill==null)skill=null;
		else
		{
			skill=game.createCard(card.info.skill,-1);
			registerAndInitOnTable(skill,Card.Position.SKILL);
		}
		game.broadcast(Game.Msg.CHANGEHERO,new JSONObject(new Object[][]{{"who",index},{"card",card.getFullObject()}}),-1);
	}
	
	private Card draw()
	{
		if(deck.isEmpty())return null;
		Card rt=deck.remove(0);
		hand.add(rt);
		return rt;
	}
	
	private void equip(Card card)
	{
		Card old=weapon;
		weapon=card;
		registerAndInitOnTable(card,Card.Position.EQUIP);
		game.broadcast(Game.Msg.EQUIP,new JSONObject(new Object[][]{{"who",index},{"card",card.getFullObject()}}),-1);
		if(game.getCurrentPlayer()==this)
		{
			int batk=old==null?0:(old.getAtk()<0?0:old.getAtk()),aatk=card.getAtk()<0?0:card.getAtk();
			if(batk!=aatk)hero.pp(aatk-batk,0,false);
		}
		if(old!=null)breakWeapon(old);
	}
	
	private void pdmg()
	{
		game.broadcast(Game.Msg.P,new JSONObject(new Object[][]{{"who",index},{"num",p}}),-1);
		p++;
		hero.takeDamage(null,p);
	}
	
	private void playHand(int index,int posi,int choi,Card target) throws GameOverThrowable
	{
		Card card=hand.get(index);
		spendCoins(card.getCost());
		throwHand(index);
		switch(card.info.type)
		{
		case MINION:
			summon(card,posi);
			break;
		case SPELL:
			break;
		case WEAPON:
			equip(card);
			break;
		case HERO:
			changeHero(card);
			break;
		default:
			return;
		}
		card.info.doBattlecry(card,this,target,choi);
		game.checkForDeath(true);
	}
	
	private void registerAndInitOnTable(Card c,Card.Position pos)
	{
		c.initOnTable(pos,this,game.registerOnTable(c));
	}
	
	private void summon(Card minion,int posi)
	{
		int n=getFieldNum();
		if(n>=game.getRule().maxField)return;
		if(posi>n)posi=n;
		field.add(posi,minion);
		registerAndInitOnTable(minion,Card.Position.MINION);
		game.broadcast(Game.Msg.SUMMON,new JSONObject(new Object[][]{{"pIndex",index},{"mIndex",posi},{"card",minion.getFullObject()}}),-1);
		for(Buff b:minion.getAllBuffs())if(b.info.events!=null)game.registerEvents(b,GlobalEvent.class);
		game.triggerEvent(new MinionEnterEvent(minion));
	}
	
	/*private void useHeroPower(int choi,Card target) throws GameOverThrowable
	{
		spendCoins(skill.getCost());
		skill.incWind();
		skill.info.doBattlecry(skill,this,target,choi);
		game.checkForDeath(true);
	}*/
	
	Player(Game game,int index,String hero,String[] cardSet)
	{
		this.game=game;
		this.index=index;
		this.hero=game.createCard(hero,-1);
		registerAndInitOnTable(this.hero,Card.Position.HERO);
		if(this.hero.info.skill!=null)
		{
			skill=game.createCard(this.hero.info.skill,-1);
			registerAndInitOnTable(skill,Card.Position.SKILL);
		}
		this.cardSet=cardSet;
		Rule r=game.getRule();
		this.field=new ArrayList<>(r.maxField);
		this.hand=new ArrayList<>(r.maxHand);
		this.deck=new ArrayList<>(r.maxDeck);
	}
	
	void doTurn() throws GameOverThrowable
	{
		Rule rule=game.getRule();
		int pos=game.getPos(index);
		int round=game.getRound();
		game.broadcast(Game.Msg.TURN,new JSONObject(new Object[][]{{"who",index}}),-1);
		gainEmptyCoins(rule.getCoinNum(pos,round),false);
		fillCoins(maxCoins);
		if(weapon!=null&&weapon.getAtk()>0)hero.pp(weapon.getAtk(),0,false);
		hero.resetWind();
		if(skill!=null)skill.resetWind();
		draw(rule.getDrawNum(pos,round));
		game.checkForDeath(true);
		boolean playing=true;
		while(playing)
		{
			Player[] players=game.getAllPlayers();
			int minionNum=field.size();
			JSONArray minionCan=new JSONArray(minionNum+1);
			for(int n=-1;n<minionNum;n++)
			{
				Card c=n<0?hero:field.get(n);
				if(!c.positionIsMinionOrHero())continue;
				JSONObject toAdd=new JSONObject();
				toAdd.put("index",n);
				toAdd.put("green",new JSONArray());
				minionCan.add(toAdd);
				int speed=c.getSpeed();
				if(speed<=0)continue;
				HashSet<JSONObject> targets=new HashSet<>();
				for(Player p:players)
				{
					if(p==this)continue;
					ArrayList<Integer> tt=new ArrayList<>(rule.maxField+1);
					if(speed==2&&p.hero.hasKW(BuffInfo.KeyWord.TAUNT)&&!p.hero.hasKW(BuffInfo.KeyWord.STEALTH))tt.add(-1);
					int pmcount=p.field.size();
					for(int i=0;i<pmcount;i++)
					{
						Card cc=p.field.get(i);
						if(c.positionIsMinionOrHero()&&cc.hasKW(BuffInfo.KeyWord.TAUNT)&&!cc.hasKW(BuffInfo.KeyWord.STEALTH))tt.add(i);
					}
					if(!tt.isEmpty())
					{
						for(int cci:tt)if(p.field.get(cci).hasKW(BuffInfo.KeyWord.IMMUNE)/*||!c.info.canAttack(c,cc)*/)tt.remove(cci);
					}
					else
					{
						if(speed==2&&!p.hero.hasKW(BuffInfo.KeyWord.IMMUNE)&&!p.hero.hasKW(BuffInfo.KeyWord.STEALTH)/*&&c.info.canAttack(c,p.hero)*/)tt.add(-1);
						for(int i=0;i<pmcount;i++)
						{
							Card ccc=p.field.get(i);
							if(c.positionIsMinionOrHero()&&!ccc.hasKW(BuffInfo.KeyWord.IMMUNE)&&!ccc.hasKW(BuffInfo.KeyWord.STEALTH)/*&&c.info.canAttack(c,ccc)*/)tt.add(i);
						}
					}
					for(int cci:tt)targets.add(new JSONObject(new Object[][]{{"pIndex",p.index},{"mIndex",cci}}));
				}
				((JSONArray)toAdd.get("green")).addAll(targets);
			}
			int handNum=hand.size();
			JSONArray handCan=new JSONArray(handNum);
			for(int i=0;i<hand.size();i++)
			{
				Card c=hand.get(i);
				JSONArray toAdd=new JSONArray(c.info.choices);
				for(int j=0;j<c.info.choices;j++)toAdd.add(new JSONArray());
				handCan.add(toAdd);
				if(!c.info.canPlay||c.getCost()>coins||c.info.type==CardInfo.Type.NONE)continue;
				if(c.info.type==CardInfo.Type.MINION&&minionNum>=rule.maxField)continue;
				for(int j=0;j<c.info.choices;j++)
				{
					if(c.info.canTarget(c,this,null,j))((JSONArray)toAdd.get(j)).add(null);
					else
					{
						for(Player p:players)
						{
							if((p==this||!p.hero.hasKW(BuffInfo.KeyWord.STEALTH))&&(c.info.type!=CardInfo.Type.SPELL||!p.hero.hasKW(BuffInfo.KeyWord.MM))&&c.info.canTarget(c,this,p.hero,j))((JSONArray)toAdd.get(j)).add(new JSONObject(new Object[][]{{"pIndex",p.index},{"mIndex",-1}}));
							for(int n=0;n<p.field.size();n++)
							{
								Card cc=p.field.get(n);
								if(cc.positionIsMinionOrHero()&&(p==this||!cc.hasKW(BuffInfo.KeyWord.STEALTH))&&(c.info.type!=CardInfo.Type.SPELL||!cc.hasKW(BuffInfo.KeyWord.MM))&&c.info.canTarget(c,this,cc,j))((JSONArray)toAdd.get(j)).add(new JSONObject(new Object[][]{{"pIndex",p.index},{"mIndex",n}}));
							}
						}
					}
				}
			}
			//skillCan?
			game.sendto(index,Game.Msg.CANPLAY,new JSONObject(new Object[][]{{"minioncan",minionCan},{"handcan",handCan}}));
			byte[] reply=game.recvfrom(index);
			Card target;
			boolean f;
			switch(game.getRMsg(reply[0]))
			{
			case ATTACK:
				if(index==reply[2])leave(false);
				if(reply[1]>=minionNum)leave(false);
				Card from=reply[1]<0?hero:field.get(reply[1]);
				if(!from.positionIsMinionOrHero()||reply[2]<0||reply[2]>=game.getPlayerCount())leave(false);
				Player tarp=game.getPlayer(reply[2]);
				if(reply[3]>=tarp.field.size())leave(false);
				target=reply[3]<0?tarp.hero:tarp.field.get(reply[3]);
				if(!target.positionIsMinionOrHero())leave(false);
				f=true;
				for(Object o:minionCan)
				{
					JSONObject jo=(JSONObject)o;
					if((Integer)jo.get("index")==reply[1])
					{
						for(Object oo:(JSONArray)jo.get("green"))
						{
							JSONObject joo=(JSONObject)oo;
							if((Integer)joo.get("pIndex")==reply[2]&&(Integer)joo.get("mIndex")==reply[3])
							{
								f=false;
								break;
							}
						}
						break;
					}
				}
				if(f)leave(false);
				from.attack(target);
				game.checkForDeath(true);
				break;
			case ENDTURN:
				playing=false;
				break;
			case PLAYHAND:
				if(reply[4]<0||reply[4]>=game.getPlayerCount())target=null;
				else
				{
					Player tarpp=game.getPlayer(reply[4]);
					if(reply[5]>=tarpp.field.size())target=null;
					target=reply[5]<0?tarpp.hero:tarpp.field.get(reply[5]);
					if(!target.positionIsMinionOrHero())target=null;
				}
				if(reply[1]<0||
					reply[1]>=hand.size()||
					reply[2]<0||
					reply[2]>minionNum||
					reply[3]<0||
					reply[3]>=hand.get(reply[1]).info.choices)leave(false);
				f=true;
				for(Object o:(JSONArray)((JSONArray)handCan.get(reply[1])).get(reply[3]))
				{
					if(o==null&&reply[4]<0)
					{
						f=false;
						break;
					}
					JSONObject jo=(JSONObject)o;
					if((Integer)jo.get("pIndex")==reply[4]&&(Integer)jo.get("mIndex")==reply[5])
					{
						f=false;
						break;
					}
				}
				if(f)leave(false);
				playHand(reply[1],reply[2],reply[3],target);
				break;
			/*case USESKILL:
				if(skill==null)leave(false);
				target=game.findChar(reply[2],reply[3]);
				if(!canUseHeroPower(reply[1],target))leave(false);
				useHeroPower(reply[1],target);
				break;*/
			default:
				leave(false);
			}
		}
		game.triggerEvent(new AfterTurnEndEvent(this));
		loseEmptyCoins(extraCoins);
		if(weapon!=null&&weapon.getAtk()>0)hero.pp(-weapon.getAtk(),0,false);
		for(int i=-1;i<field.size();i++)
		{
			Card c=i<0?hero:field.get(i);
			if(!c.positionIsMinionOrHero())continue;
			if((c.getWind()>=0||c.hasKW(BuffInfo.KeyWord.CHARGE)||c.hasKW(BuffInfo.KeyWord.RUSH))&&c.getWind()<c.getMaxWind())
			{
				for(Buff b:c.getAllBuffs())
				{
					if(b.info.keyWords==null||b.info.isEffect)continue;
					for(BuffInfo.KeyWord kw:b.info.keyWords)
					{
						if(kw.equals(BuffInfo.KeyWord.FROZEN))
						{
							NormalLosingEvent e=new NormalLosingEvent();
							b.triggerSelf(game,e);
							if(!e.prevent)c.loseBuff(b);
							break;
						}
					}
				}
			}
			c.resetWind();
		}
	}
	
	GameOverThrowable init()
	{
		Player players[]=game.getAllPlayers();
		Rule rule=game.getRule();
		int pos=game.getPos(index);
		int _coins=rule.getCoins(pos);
		maxCoins=_coins;
		coins=_coins;
		String[] _deck=rule.getDeck(cardSet,pos);
		for(String c:_deck)
		{
			Card card=game.createCard(c,index);
			deck.add(card);
		}
		game.broadcast(Game.Msg.CHECKHERO,new JSONObject(new Object[][]{{"who",index},{"card",hero.getFullObject()}}),-1);
		game.broadcast(Game.Msg.CHECKCOINS,new JSONObject(new Object[][]{{"who",index},{"num",_coins}}),-1);
		game.broadcast(Game.Msg.CHECKDECK,new JSONObject(new Object[][]{{"who",index},{"num",_deck.length}}),-1);
		int fc=rule.getFirstCards(pos,players.length);
		if(fc>_deck.length)fc=_deck.length;
		for(int j=0;j<fc;j++)game.sendto(index,Game.Msg.FIRSTHAND,new JSONObject(new Object[][]{{"card",deck.get(j).getDisplayObject()}}));
		game.broadcast(Game.Msg.OFIRSTHAND,new JSONObject(new Object[][]{{"who",index},{"num",fc}}),index);
		if(_deck.length>=fc*2&&rule.canChangeFirst(pos,players.length))game.sendto(index,Game.Msg.ASKFORFIRST,null);
		return null;
	}
	
	void leave(boolean concede) throws GameOverThrowable
	{
		hero.kill();
		throw new GameOverThrowable(concede?GameOverThrowable.Type.CONCEDE:GameOverThrowable.Type.LEFT);
	}
	
	GameOverThrowable prepareFirst(byte[] reply)
	{
		Player players[]=game.getAllPlayers();
		Rule rule=game.getRule();
		int pos=game.getPos(index);
		String[] _deck=rule.getDeck(cardSet,pos);
		int fc=rule.getFirstCards(pos,players.length);
		if(_deck.length>=fc*2&&rule.canChangeFirst(pos,players.length))
		{
			Game.RMsg rmsg=game.getRMsg(reply[0]);
			if(rmsg!=Game.RMsg.CHOOSE)
			{
				try
				{
					leave(false);
				}
				catch(GameOverThrowable e)
				{
					return e;
				}
			}
			HashSet<Card> toshf=new HashSet<>();
			for(int j=0;j<fc;j++)
			{
				if((reply[1]&(1<<j))!=0)
				{
					toshf.add(deck.get(j));
					Card c=deck.get(fc);
					deck.set(j,c);
					deck.remove(fc);
					game.sendto(index,Game.Msg.CHANGEFIRST,new JSONObject(new Object[][]{{"index",j},{"card",c.getDisplayObject()}}));
					game.broadcast(Game.Msg.OCHANGEFIRST,new JSONObject(new Object[][]{{"who",index},{"index",j}}),index);
				}
			}
			for(Card c:toshf)deck.add(fc+(int)(Math.random()*(_deck.length-fc+1)),c);
		}
		for(int j=0;j<fc;j++)draw();
		return null;
	}
	
	public void burn()
	{
		if(deck.isEmpty())return;
		Card c=deck.remove(0);
		game.broadcast(Game.Msg.BURN,new JSONObject(new Object[][]{{"who",index},{"card",c.getDisplayObject()}}),-1);
	}
	
	public void draw(int num)
	{
		for(int i=0;i<num;i++)
		{
			if(deck.isEmpty())pdmg();
			else if(hand.size()>=game.getRule().maxHand)burn();
			else
			{
				Card c=draw();
				game.sendto(index,Game.Msg.DRAW,new JSONObject(new Object[][]{{"card",c.getDisplayObject()}}));
				game.broadcast(Game.Msg.ODRAW,new JSONObject(new Object[][]{{"who",index}}),index);
			}
		}
	}
	
	public void fillCoins(int num)
	{
		if(num<=0)return;
		int delta=maxCoins-coins;
		if(delta<=0)return;
		if(num>delta)num=delta;
		coins+=num;
		game.broadcast(Game.Msg.FILLCOINS,new JSONObject(new Object[][]{{"who",index},{"num",num}}),-1);
	}
	
	public void gainArmor(int armor)
	{
		if(armor<=0)return;
		hero.gainArmor(armor);
		game.broadcast(Game.Msg.GAINARMOR,new JSONObject(new Object[][]{{"who",index},{"num",armor}}),-1);
	}
	
	public boolean gainEmptyCoins(int num,boolean extra)
	{
		if(num<=0)return true;
		int delta=game.getRule().maxCoins-maxCoins;
		if(delta<=0)return false;
		else
		{
			if(num>delta)num=delta;
			maxCoins+=num;
			if(extra)extraCoins+=num;
			game.broadcast(Game.Msg.GAINCOINS,new JSONObject(new Object[][]{{"who",index},{"num",num},{"isextra",extra}}),-1);
		}
		return true;
	}
	
	public Card[] getField()
	{
		return field.toArray(new Card[0]);
	}
	
	public int getFieldNum()
	{
		return field.size();
	}
	
	public Game getGame()
	{
		return game;
	}
	
	public Card getHero()
	{
		return hero;
	}
	
	public int getIndex()
	{
		return index;
	}
	
	public void loseEmptyCoins(int num)
	{
		if(num<=0)return;
		if(num>maxCoins)num=maxCoins;
		maxCoins-=num;
		if(extraCoins>num)extraCoins-=num;
		else extraCoins=0;
		if(coins>maxCoins)coins=maxCoins;
		game.broadcast(Game.Msg.LOSECOINS,new JSONObject(new Object[][]{{"who",index},{"num",num}}),-1);
	}
	
	public void obtain(Card card)
	{
		if(hand.size()>=game.getRule().maxHand)return;
		hand.add(card);
		game.sendto(index,Game.Msg.GET,new JSONObject(new Object[][]{{"card",card.getDisplayObject()}}));
		game.broadcast(Game.Msg.OGET,new JSONObject(new Object[][]{{"who",index}}),index);
	}
	
	public void removeMinion(Card minion,MinionLeaveEvent.Reason reason)
	{
		game.triggerEvent(new MinionLeaveEvent(minion,reason));
		for(Buff b:minion.getAllBuffs())game.unregisterEvents(b);
		int posi=field.indexOf(minion);
		field.remove(posi);
		game.unregisterOnTable(minion);
		game.broadcast(Game.Msg.REMOVEMINION,new JSONObject(new Object[][]{{"pIndex",index},{"mIndex",posi}}),-1);
	}
	
	public void shuffle(Card card)
	{
		int len=deck.size();
		if(len>=game.getRule().maxDeck)return;
		deck.add((int)(Math.random()*(deck.size()+1)),card);
		game.broadcast(Game.Msg.SHUFFLE,new JSONObject(new Object[][]{{"who",index}}),-1);
	}
	
	public void spendCoins(int num)
	{
		if(coins<num)num=coins;
		coins-=num;
		game.broadcast(Game.Msg.SPENDCOINS,new JSONObject(new Object[][]{{"who",index},{"num",num}}),-1);
	}
	
	public void throwDeck(int index)
	{
		deck.remove(index);
		game.broadcast(Game.Msg.THROWDECK,new JSONObject(new Object[][]{{"who",index}}),-1);
	}
	
	public void throwHand(int index)
	{
		hand.remove(index);
		game.broadcast(Game.Msg.THROWHAND,new JSONObject(new Object[][]{{"who",this.index},{"index",index}}),-1);
	}
	
	public void throwWeapon()
	{
		if(weapon==null)return;
		Card old=weapon;
		weapon=null;
		if(game.getCurrentPlayer()==this&&old.getAtk()>0)hero.pp(-old.getAtk(),0,false);
		breakWeapon(old);
	}
}
