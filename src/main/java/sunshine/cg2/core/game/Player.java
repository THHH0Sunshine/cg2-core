package sunshine.cg2.core.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import sunshine.cg2.core.game.event.globalevent.AfterTurnEndEvent;
import sunshine.cg2.core.game.event.globalevent.EnterTableEvent;
import sunshine.cg2.core.game.event.globalevent.LeaveTableEvent;
import sunshine.cg2.core.game.event.globalevent.SummonEvent;
import sunshine.cg2.core.game.event.globalevent.TurnEndEvent;
import sunshine.cg2.core.game.event.globalevent.TurnStartEvent;
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
	private int spellPower;
	private int staghelmCount;
	private final ArrayList<Card> soul=new ArrayList<>();
	
	private void addField(Card card,int posi,int poss,Card.Position position,byte[] addition)
	{
		if(field.size()>=game.getRule().maxField)return;
		field.add(posi,card);
		soul.add(poss,card);
		game.addCardToTable(card,position,this);
		game.broadcast(Game.Msg.SUMMON,new JSONObject(new Object[][]{{"pIndex",index},{"mIndex",posi},{"card",card.getFullObject()}}),-1);
		game.triggerEvent(new EnterTableEvent(card,addition));
	}
	
	private void addField(Card card,Card near,boolean left,Card.Position position)
	{
		int posi=getFieldInsertIndex(near,left);
		if(posi<0)return;
		int poss=soul.indexOf(near);
		if(poss<0)poss=soul.size();
		else if(!left)poss++;
		addField(card,posi,poss,position,null);
	}
	
	private void addField(Card card,int posi,Card.Position position,byte[] addition)
	{
		int n=field.size();
		if(posi<0||posi>n)posi=n;
		int poss=posi==n?soul.size():soul.indexOf(field.get(posi));
		if(poss<0)return;//should not reach here
		addField(card,posi,poss,position,addition);
	}
	
	private Card draw()
	{
		if(deck.isEmpty())return null;
		Card rt=deck.remove(0);
		hand.add(rt);
		return rt;
	}
	
	private int getFieldInsertIndex(Card near,boolean left)
	{
		int n=field.size();
		int sn=soul.size();
		int poss=soul.indexOf(near);
		if(poss<0)return n;
		else
		{
			if(left)
			{
				for(int i=poss-1;i>=0;i--)
				{
					Card c=soul.get(i);
					if(c.getPosition()!=Card.Position.OFFTABLE)
					{
						int ind=field.indexOf(c);
						if(ind<0)return -1;
						return ind+1;
					}
				}
				return 0;
			}
			else
			{
				for(int i=++poss;i<sn;i++)
				{
					Card c=soul.get(i);
					if(c.getPosition()!=Card.Position.OFFTABLE)
					{
						return field.indexOf(c);
					}
				}
				return n;
			}
		}
	}
	
	private void pdmg()
	{
		game.broadcast(Game.Msg.P,new JSONObject(new Object[][]{{"who",index},{"num",p}}),-1);
		hero.takeDamage(null,p);
		p++;
	}
	
	private void playHand(int index,int posi,int choi,Card target,byte[] addition) throws GameOverThrowable
	{
		Card card=hand.get(index);
		spendCoins(card.getCost());
		throwHand(index);
		boolean changeOwner=true;
		switch(card.info.type)
		{
		case MINION:
			addField(card,posi,Card.Position.MINION,addition);
			break;
		case SPELL:
			changeOwner=false;
			break;
		case WEAPON:
			equip(card,addition);
			break;
		case HERO:
			changeHero(card,addition);
			break;
		default:
			return;
		}
		card.info.doBattlecry(card,changeOwner?card.getOwner():this,target,choi);
		game.checkForDeath(true);
		if(card.info.type==CardInfo.Type.MINION&&card.getPosition()==Card.Position.MINION)
		{
			game.triggerEvent(new SummonEvent(card));
			game.checkForDeath(true);
		}
	}
	
	private void useHeroPower(int choi,Card target) throws GameOverThrowable
	{
		spendCoins(skill.getCost());
		skill.incWind();
		skill.info.doBattlecry(skill,this,target,choi);
		game.checkForDeath(true);
	}
	
	Player(Game game,int index,String hero,String[] cardSet)
	{
		this.game=game;
		this.index=index;
		this.hero=game.createCard(hero,-1);
		game.addCardToTable(this.hero,Card.Position.HERO,this);
		if(this.hero.info.skill!=null)
		{
			skill=game.createCard(this.hero.info.skill,-1);
			game.addCardToTable(skill,Card.Position.SKILL,this);
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
		if(weapon!=null&&weapon.getAtk()>0)hero.ppNoSilence(weapon.getAtk(),0);
		hero.resetWind();
		for(Card c:field)if(c.positionIsMinionOrHero())c.resetWind();
		if(skill!=null)skill.resetWind();
		game.triggerEvent(new TurnStartEvent(this));
		game.checkForDeath(true);
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
						for(int cci:tt)
						{
							Card cc = p.field.get(cci);
							if(cc.hasKW(BuffInfo.KeyWord.IMMUNE)||!c.canAttack(cc))tt.remove(cci);
						}
					}
					else
					{
						if(speed==2&&!p.hero.hasKW(BuffInfo.KeyWord.IMMUNE)&&!p.hero.hasKW(BuffInfo.KeyWord.STEALTH)&&c.canAttack(p.hero))tt.add(-1);
						for(int i=0;i<pmcount;i++)
						{
							Card ccc=p.field.get(i);
							if(c.positionIsMinionOrHero()&&!ccc.hasKW(BuffInfo.KeyWord.IMMUNE)&&!ccc.hasKW(BuffInfo.KeyWord.STEALTH)&&c.canAttack(ccc))tt.add(i);
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
				int choices=hasStaghelm()?1:c.info.choices;
				JSONArray toAdd=new JSONArray(choices);
				for(int j=0;j<choices;j++)toAdd.add(new JSONArray());
				handCan.add(toAdd);
				if(!c.info.canPlay||c.getCost()>coins||c.info.type==CardInfo.Type.NONE)continue;
				if(c.info.type==CardInfo.Type.MINION&&minionNum>=rule.maxField)continue;
				for(int j=0;j<choices;j++)
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
			JSONArray skillCan=null;
			if(skill!=null&&skill.info.canPlay&&skill.getCost()<=coins&&skill.getSpeed()>0)
			{
				int choices=hasStaghelm()?1:skill.info.choices;
				skillCan=new JSONArray(choices);
				for(int i=0;i<choices;i++)
				{
					JSONArray toAdd=new JSONArray();
					skillCan.add(toAdd);
					if(skill.info.canTarget(skill,this,null,i))toAdd.add(null);
					else
					{
						for(Player p:players)
						{
							if((p==this||!p.hero.hasKW(BuffInfo.KeyWord.STEALTH))&&!p.hero.hasKW(BuffInfo.KeyWord.MM)&&skill.info.canTarget(skill,this,p.hero,i))toAdd.add(new JSONObject(new Object[][]{{"pIndex",p.index},{"mIndex",-1}}));
							for(int n=0;n<p.field.size();n++)
							{
								Card c=p.field.get(n);
								if(c.positionIsMinionOrHero()&&(p==this||!c.hasKW(BuffInfo.KeyWord.STEALTH))&&!c.hasKW(BuffInfo.KeyWord.MM)&&skill.info.canTarget(skill,this,c,i))toAdd.add(new JSONObject(new Object[][]{{"pIndex",p.index},{"mIndex",n}}));
							}
						}
					}
				}
			}
			game.sendto(index,Game.Msg.CANPLAY,new JSONObject(new Object[][]{{"minioncan",minionCan},{"handcan",handCan},{"skillcan",skillCan}}));
			byte[] reply=game.recvfrom(index);
			Card target;
			boolean f;
			switch(game.getRMsg(reply[0]))
			{
			case ATTACK:
				if(reply.length<4||index==reply[2])leave(false);
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
				if(reply.length<6)leave(false);
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
					reply[3]>=(hasStaghelm()?1:hand.get(reply[1]).info.choices))leave(false);
				f=true;
				for(Object o:(JSONArray)((JSONArray)handCan.get(reply[1])).get(reply[3]))
				{
					if(o==null&&target==null)
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
				playHand(reply[1],reply[2],reply[3],target,Arrays.copyOfRange(reply,6,reply.length));
				break;
			case USESKILL:
				if(skillCan==null||reply.length<4)leave(false);
				if(reply[2]<0||reply[2]>game.getPlayerCount())target=null;
				else
				{
					Player tarpp=game.getPlayer(reply[2]);
					if(reply[3]>=tarpp.field.size())target=null;
					target=reply[3]<0?tarpp.hero:tarpp.field.get(reply[3]);
					if(!target.positionIsMinionOrHero())target=null;
				}
				if(reply[1]<0||reply[1]>=(hasStaghelm()?1:skill.info.choices))leave(false);
				f=true;
				for(Object o:(JSONArray)skillCan.get(reply[1]))
				{
					if(o==null&&target==null)
					{
						f=false;
						break;
					}
					JSONObject jo=(JSONObject)o;
					if((Integer)jo.get("pIndex")==reply[2]&&(Integer)jo.get("mIndex")==reply[3])
					{
						f=false;
						break;
					}
				}
				if(f)leave(false);
				useHeroPower(reply[1],target);
				break;
			default:
				leave(false);
			}
		}
		game.triggerEvent(new TurnEndEvent(this));
		game.checkForDeath(true);
		game.triggerEvent(new AfterTurnEndEvent(this));
		loseEmptyCoins(extraCoins);
		if(weapon!=null&&weapon.getAtk()>0)hero.ppNoSilence(-weapon.getAtk(),0);
		for(int i=-1;i<field.size();i++)
		{
			Card c=i<0?hero:field.get(i);
			if(!c.positionIsMinionOrHero())continue;
			if(c.shouldBreakIce())
			{
				for(Buff b:c.getAllBuffs())
				{
					if(b.info.keyWords==null||b.info.isEffect)continue;
					for(BuffInfo.KeyWord kw:b.info.keyWords)
					{
						if(kw.equals(BuffInfo.KeyWord.FROZEN))
						{
							c.loseBuff(b);
							break;
						}
					}
				}
			}
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
		if(skill!=null)game.broadcast(Game.Msg.CHECKSKILL,new JSONObject(new Object[][]{{"who",index},{"card",skill.getDisplayObject()}}),-1);
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
		Player[] players=game.getAllPlayers();
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
			for(Card c:toshf)deck.add(fc+(int)(Math.random()*(deck.size()-fc+1)),c);
		}
		for(int j=0;j<fc;j++)draw();
		return null;
	}
	
	void removeField(Card card,LeaveTableEvent.Reason reason)
	{
		int posi=field.indexOf(card);
		if(posi<0)return;
		game.triggerEvent(new LeaveTableEvent(card,reason));
		field.remove(posi);
		if(reason!=LeaveTableEvent.Reason.DEATH)soul.remove(card);
		game.removeCardFromTable(card);
		game.broadcast(Game.Msg.REMOVEMINION,new JSONObject(new Object[][]{{"pIndex",index},{"mIndex",posi}}),-1);
	}
	
	void removeSoul(Card card)
	{
		soul.remove(card);
	}
	
	public void addSpellPower(int num)
	{
		if(num==0)return;
		spellPower+=num;
		game.broadcast(Game.Msg.SPELLPOWER,new JSONObject(new Object[][]{{"who",index},{"num",spellPower}}),-1);
	}
	
	public int askForDiscover(Card[] choices) throws GameOverThrowable
	{
		JSONArray ja=new JSONArray(choices.length);
		for(Card c:choices)ja.add(c.getDisplayObject());
		game.sendto(index,Game.Msg.ASKFORDISCOVER,new JSONObject(new Object[][]{{"choices",ja}}));
		byte[] reply=game.recvfrom(index);
		if(reply.length<2||game.getRMsg(reply[0])!=Game.RMsg.CHOOSE||reply[1]<0||reply[1]>=choices.length)leave(false);
		return reply[1];
	}
	
	public void burn()
	{
		if(deck.isEmpty())return;
		Card c=deck.remove(0);
		game.broadcast(Game.Msg.BURN,new JSONObject(new Object[][]{{"who",index},{"card",c.getDisplayObject()}}),-1);
	}
	
	public void changeHero(Card card,byte[] addition)
	{
		game.triggerEvent(new LeaveTableEvent(hero,LeaveTableEvent.Reason.CHANGEHERO));
		game.removeCardFromTable(hero);
		card.replaceOldHero(hero);
		hero=card;
		game.addCardToTable(card,Card.Position.HERO,this);
		game.broadcast(Game.Msg.CHANGEHERO,new JSONObject(new Object[][]{{"who",index},{"card",card.getFullObject()}}),-1);
		game.triggerEvent(new EnterTableEvent(card,addition));
		if(card.info.skill!=null)changeSkill(card.info.skill);
	}
	
	public void changeSkill(CardInfo ci)
	{
		if(skill!=null)
		{
			game.triggerEvent(new LeaveTableEvent(skill,LeaveTableEvent.Reason.MOVE));
			game.removeCardFromTable(skill);
		}
		if(ci==null)
		{
			skill=null;
			game.broadcast(Game.Msg.CHANGESKILL,new JSONObject(new Object[][]{{"who",index},{"card",null}}),-1);
		}
		else
		{
			skill=game.createCard(ci,-1);
			game.addCardToTable(skill,Card.Position.SKILL,this);
			game.broadcast(Game.Msg.CHANGESKILL,new JSONObject(new Object[][]{{"who",index},{"card",skill.getDisplayObject()}}),-1);
			game.triggerEvent(new EnterTableEvent(skill,null));
		}
	}
	
	public void damageWeapon()
	{
		if(weapon!=null)
			weapon.takeDamage(null,1);
	}
	
	public void discardHand(int num)
	{
		if(num<=0)return;
		int len=hand.size();
		if(len<=0)return;
		if(num>len)num=len;
		for(;num>0;num--)throwHand((int)(Math.random()*num));
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
	
	public void equip(Card card,byte[] addition)
	{
		Card old=weapon;
		if(old!=null)old.kill();
		weapon=card;
		game.addCardToTable(card,Card.Position.EQUIP,this);
		game.broadcast(Game.Msg.EQUIP,new JSONObject(new Object[][]{{"who",index},{"card",card.getFullObject()}}),-1);
		if(game.getCurrentPlayer()==this)
		{
			int batk=old==null?0:(old.getAtk()<0?0:old.getAtk()),aatk=card.getAtk()<0?0:card.getAtk();
			if(batk!=aatk)hero.ppNoSilence(aatk-batk,0);
		}
		game.triggerEvent(new EnterTableEvent(card,addition));
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
	
	public void gainOneStaghelm()
	{
		staghelmCount++;
	}
	
	public List<Card> getAliveMinions()
	{
		ArrayList<Card> rt=new ArrayList<>(field.size());
		for(Card c:field)if(c.getPosition()==Card.Position.MINION&&!c.isDying())rt.add(c);
		return rt;
	}
	
	public List<Card> getAllMinions()
	{
		ArrayList<Card> rt=new ArrayList<>(field.size());
		for(Card c:field)if(c.getPosition()==Card.Position.MINION)rt.add(c);
		return rt;
	}
	
	@SuppressWarnings("unchecked")
	public List<Card> getField()
	{
		return (List<Card>)field.clone();
	}
	
	public int getFieldNum()
	{
		return field.size();
	}
	
	public Game getGame()
	{
		return game;
	}
	
	@SuppressWarnings("unchecked")
	public List<Card> getHand()
	{
		return (List<Card>)hand.clone();
	}
	
	public int getHandNum()
	{
		return hand.size();
	}
	
	public Card getHero()
	{
		return hero;
	}
	
	public int getIndex()
	{
		return index;
	}
	
	public int getMinionNum()
	{
		int rt=0;
		for(Card c:field)if(c.getPosition()==Card.Position.MINION)rt++;
		return rt;
	}
	
	public Player getNextPlayer(int num)
	{
		int pc=game.getPlayerCount();
		int rt=(index+num)%pc;
		if(rt<0)rt+=pc;
		return game.getPlayer(rt);
	}
	
	public Player getNextPlayer()
	{
		return getNextPlayer(1);
	}
	
	public int getSpellPower()
	{
		return spellPower;
	}
	
	public Card getWeapon()
	{
		return weapon;
	}
	
	public boolean hasStaghelm()
	{
		return staghelmCount>0;
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
	
	public void loseOneStaghelm()
	{
		staghelmCount--;
	}
	
	public void moveFieldAway(Card card)
	{
		removeField(card,LeaveTableEvent.Reason.MOVE);
	}
	
	public void obtain(Card card)
	{
		if(hand.size()>=game.getRule().maxHand)return;
		hand.add(card);
		game.sendto(index,Game.Msg.GET,new JSONObject(new Object[][]{{"card",card.getDisplayObject()}}));
		game.broadcast(Game.Msg.OGET,new JSONObject(new Object[][]{{"who",index}}),index);
	}
	
	public Card[] popDeck(int num)
	{
		if(num<=0)return null;
		int len=deck.size();
		if(len<=0)return null;
		if(num>len)num=len;
		Card[] rt=new Card[num];
		for(int i=0;i<num;i++)rt[i]=deck.remove(0);
		game.broadcast(Game.Msg.THROWDECK,new JSONObject(new Object[][]{{"who",index},{"num",num}}),-1);
		return rt;
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
	
	public void summon(Card minion) throws GameOverThrowable
	{
		summon(minion,null,false);
	}
	
	public void summon(Card minion,Card near,boolean left) throws GameOverThrowable
	{
		addField(minion,near,left,Card.Position.MINION);
		if(minion.getPosition()==Card.Position.MINION)game.triggerEvent(new SummonEvent(minion));
	}
	
	public void takeControlOfField(Card card)
	{
		Player p=card.getOwner();
		if(p==null||!p.field.contains(card))return;
		p.removeField(card,LeaveTableEvent.Reason.CONTROL);
		p.addField(card,field.size(),Card.Position.MINION,null);
	}
	
	public void takeControlOfWeapon(Player player)
	{
		Card card=player.weapon;
		if(card==null)return;
		player.weapon=null;
		game.broadcast(Game.Msg.THROWWEAPON,new JSONObject(new Object[][]{{"who",player.index}}),-1);
		if(game.getCurrentPlayer()==player&&card.getAtk()>0)player.hero.ppNoSilence(-card.getAtk(),0);
		game.triggerEvent(new LeaveTableEvent(card,LeaveTableEvent.Reason.CONTROL));
		game.removeCardFromTable(card);
		equip(card,null);
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
		old.kill();
		weapon=null;
		game.broadcast(Game.Msg.THROWWEAPON,new JSONObject(new Object[][]{{"who",index}}),-1);
		if(game.getCurrentPlayer()==this&&old.getAtk()>0)hero.ppNoSilence(-old.getAtk(),0);
	}
	
	public void transformField(Card from,Card to,boolean ice)
	{
		int posi=field.indexOf(from);
		if(posi<0)return;
		removeField(from,LeaveTableEvent.Reason.TRANSFORM);
		addField(to,posi,ice?Card.Position.ICE:Card.Position.MINION,null);
	}
}
