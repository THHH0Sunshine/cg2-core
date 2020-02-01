package sunshine.cg2.core.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import sunshine.cg2.core.game.event.DeathrattleEvent;
import sunshine.cg2.core.game.event.Event;
import sunshine.cg2.core.game.event.globalevent.DamagedEvent;
import sunshine.cg2.core.game.event.globalevent.GlobalEvent;
import sunshine.cg2.core.game.event.globalevent.HealedEvent;
import sunshine.cg2.core.game.event.globalevent.LeaveTableEvent;
import sunshine.cg2.core.util.JSONObject;

public class Game {

	enum RMsg
	{
		NULL,
		ATTACK,//fromposi,toposiwho,toposi
		CHOOSE,//choice
		CONCEDE,//who
		ENDTURN,
		PLAYHAND,//index,posi,choice,toposiwho,toposi
		USESKILL,//choice,toposiwho,toposi
		SPECIAL,//...
	}
	
	enum Msg
	{
		//BuffObject:{name:string,keywords:[string]}
		//CardDisplayObject:{name:string,cost:int,atk:int,hp:int,canplay:boolean,type:string}
		//CardFullObject:{hash:int,name:string,atk:int,maxhp:int,hp:int,shield:boolean,armor:int,buff:[BuffObject]}
		NULL,
		ASKFORDISCOVER,//{choices:[CardDisplayObject]}
		ASKFORFIRST,
		ATTACK,//{fromhash:int,tohash:int}
		BURN,//{who:int,card:CardDisplayObject}
		CANPLAY,//{minioncan:[{index:int,green:[{pIndex:int,mIndex:int}]}],handcan:[[[{pIndex:int,mIndex:int}]]],skillcan:[[{pIndex:int,mIndex:int}]]}
		CHANGEFIRST,//{index:int,card:CardDisplayObject}
		CHANGEHERO,//{who:int,card:CardFullObject}
		CHANGEPP,//{hash:int,atk:int,maxhp:int,hp:int}
		CHANGESKILL,//{who:int,card:CardDisplayObject}
		CHECKCOINS,//{who:int,num:int}
		CHECKDECK,//{who:int,num:int}
		CHECKHERO,//{who:int,card:CardFullObject}
		CHECKSKILL,//{who:int,card:CardDisplayObject}
		DAMAGE,//{(fromhash):int,tohash:int,num:int}
		DRAW,//{card:CardDisplayObject}
		EQUIP,//{who:int,card:CardFullObject}
		FILLCOINS,//{who:int,num:int}
		FIRSTHAND,//{card:CardDisplayObject}
		GAINARMOR,//{who:int,num:int}
		GAINBUFF,//{hash:int,buff:BuffObject}
		GAINCOINS,//{who:int,num:int,isextra:boolean}
		GAINSHIELD,//{hash:int}
		GAMESTART,
		GET,//{card:CardDisplayObject}
		HEAL,//{(fromhash):int,tohash:int,num:int}
		LOSEBUFF,//{index:int,hash:int}
		LOSECOINS,//{who:int,num:int}
		LOSESHIELD,//{hash:int}
		OCHANGEFIRST,//{who:int,index:int}
		ODRAW,//{who:int}
		OFIRSTHAND,//{who:int,num:int}
		OGET,//{who:int}
		P,//{who:int,num:int}
		REMOVEMINION,//{pIndex:int,mIndex:int}
		SEATS,//{num:int,self:int,first:int}
		SHUFFLE,//{who:int}
		SPENDCOINS,//{who:int,num:int}
		SPELLPOWER,//{who:int,num:int}
		STOPPLAY,
		SUMMON,//{pIndex:int,mIndex:int,card:CardFullObject}
		THROWDECK,//{who:int,num:int}
		THROWHAND,//{who:int,index:int}
		THROWWEAPON,//{who:int}
		TURN,//{who:int}
	}
	
	private final Map<String,CardInfo> library;
	private final HashMap<String,CardInfo> standardCards=new HashMap<>();
	private final String[] usingPrefix;
	private final IO io;	
	private final Rule rule;
	private final Player[] players;
	private final EventHandler eventHandler=new EventHandler();
	private final LinkedList<Card> table=new LinkedList<>();
	private int nextNumber;
	private int first;
	private int current;
	private int round;
	
	void addCardToTable(Card card,Card.Position pos,Player owner)
	{
		card.initOnTable(pos,owner,nextNumber++);
		table.add(card);
		for(Buff b:card.getAllBuffs())if(b.info.events!=null)registerEvents(b,GlobalEvent.class);
	}
	
	void removeCardFromTable(Card card)
	{
		for(Buff b:card.getAllBuffs())unregisterEvents(b);
		table.remove(card);
		card.resetPosition();
	}
	
	void broadcast(Msg msg,JSONObject jsonobj,int except)
	{
		for(int i=0;i<players.length;i++)if(i!=except)sendto(i,msg,jsonobj);
	}
	
	RMsg getRMsg(byte index)
	{
		RMsg[] all=RMsg.values();
		if(index<0||index>=all.length)
			return RMsg.NULL;
		return all[index];
	}
	
	IO.Reply recv()
	{
		return io.recv();
	}
	
	byte[] recvfrom(int from) throws GameOverThrowable
	{
		while(true)
		{
			IO.Reply r=io.recv();
			if(r.data.length<=0)players[r.who].leave(false);
			if(getRMsg(r.data[0])==RMsg.CONCEDE)players[r.who].leave(true);
			if(r.who==from)return r.data;
		}
	}
	
	void sendto(int to,Msg msg,JSONObject jsonobj)
	{
		String name=msg.name();
		JSONObject rt=new JSONObject();
		rt.put("message",name);
		if(jsonobj!=null)rt.put("data",jsonobj);
		io.sendTo(to,rt);
	}
	
	public Game(Map<String,CardInfo> library,String[] usingPrefix,Rule rule,CardSet[] sets,IO io)
	{
		int len=sets.length;
		this.library=library;
		this.io=io;
		this.rule=rule;
		this.usingPrefix=usingPrefix;
		library.forEach((s,c)->
		{
			for(String p:usingPrefix)
				if(s.startsWith(p))
					standardCards.put(s,c);
		});
		players=new Player[len];
		for(int i=0;i<len;i++)
		{
			players[i]=new Player(this,i,sets[i].hero,sets[i].cards.clone());
		}
	}
	
	public void checkForDamage()
	{
		ArrayList<DamagedEvent> damaged=new ArrayList<>();
		for(Card c:table)
		{
			DamagedEvent e=c.removeDamagedEvent();
			if(e!=null)damaged.add(e);
		}
		for(DamagedEvent e:damaged)triggerEvent(e);
	}
	
	public int checkForDeath(boolean completely) throws GameOverThrowable
	{
		int rt=0;
		do
		{
			for(Player p:players)if(p.getHero().isDying())throw new GameOverThrowable(GameOverThrowable.Type.NORMAL);
			ArrayList<Card> dying=new ArrayList<>();
			for(Card c:table)if(c.isDying())dying.add(c);
			if(dying.isEmpty())break;
			for(Card c:dying)
			{
				switch(c.getPosition())
				{
				case MINION:
					c.getOwner().removeField(c,LeaveTableEvent.Reason.DEATH);
					//players[c.getMinionOwnerId()].addDeath(c.info.id);
					break;
				default:
					triggerEvent(new LeaveTableEvent(c,LeaveTableEvent.Reason.DEATH));
					removeCardFromTable(c);
				}
			}
			for(Card c:dying)for(Buff b:c.getAllBuffs())b.triggerSelf(new DeathrattleEvent());
			for(Card c:dying)c.getOwner().removeSoul(c);
			rt++;
		}while(completely);
		return rt;
	}
	
	public void checkForHeal()
	{
		ArrayList<HealedEvent> healed=new ArrayList<>();
		for(Card c:table)
		{
			HealedEvent e=c.removeHealedEvent();
			if(e!=null)healed.add(e);
		}
		for(HealedEvent e:healed)triggerEvent(e);
	}
	
	public Card createCard(CardInfo info,int from)
	{
		return new Card(this,info,from,info.cost,info.atk,info.HP);
	}
	
	public Card createCard(String name,int from)
	{
		return createCard(library.get(name),from);
	}
	
	public Card createClear(Card card)
	{
		return createCard(card.info,card.from);
	}
	
	public void forEachCardInLibrary(BiConsumer<? super String,? super CardInfo> action)
	{
		library.forEach(action);
	}
	
	public void forEachCardInStandard(BiConsumer<? super String,? super CardInfo> action)
	{
		standardCards.forEach(action);
	}
	
	public void forEachCardOnTable(Consumer<? super Card> action)
	{
		table.forEach(action);
	}
	
	public CardInfo getCardInLibraryFromName(String name)
	{
		return library.get(name);
	}
	
	public Player getCurrentPlayer()
	{
		return players[current];
	}
	
	public int getRound()
	{
		return round;
	}
	
	public Player[] getAllPlayers()
	{
		return players.clone();
	}
	
	public Player getPlayer(int who)
	{
		return players[who];
	}
	
	public int getPlayerCount()
	{
		return players.length;
	}
	
	public int getPos(int who)
	{
		return who>=first?who-first:players.length+who-first;
	}
	
	public Rule getRule()
	{
		return rule;
	}
	
	public String[] getUsingPrefix()
	{
		return usingPrefix.clone();
	}
	
	public void registerEvents(Buff buff,Class<? extends Event> select)
	{
		eventHandler.register(buff,select);
	}
	
	public void run()
	{
		first=rule.chooseFirst(players.length);
		for(int i=0;i<players.length;i++)
		{
			sendto(i,Msg.SEATS,new JSONObject(new Object[][]{{"num",players.length},{"self",i},{"first",first}}));
		}
		GameOverThrowable gameOver=null;
		for(int i=0;i<players.length;i++)
		{
			gameOver=players[i].init();
			if(gameOver!=null)break;
		}
		if(gameOver==null)
		{
			boolean[] fs=new boolean[players.length];
			boolean conc=false;
			for(int i=0;i<players.length;i++)
			{
				IO.Reply rep=recv();
				if(getRMsg(rep.data[0])==RMsg.CONCEDE)conc=true;
				if(fs[rep.who]||conc)
				{
					players[rep.who].getHero().kill();
					gameOver=new GameOverThrowable(conc?GameOverThrowable.Type.CONCEDE:GameOverThrowable.Type.LEFT);
					break;
				}
				else fs[rep.who]=true;
				gameOver=players[rep.who].prepareFirst(rep.data);
				if(gameOver!=null)break;
			}
		}
		if(gameOver==null)
		{
			round=1;
			current=first;
			broadcast(Msg.GAMESTART,null,-1);
			while(round<=rule.maxRounds)
			{
				try
				{
					players[current].doTurn();
				}
				catch(GameOverThrowable e)
				{
					gameOver=e;
					break;
				}
				current++;
				if(current>=players.length)current=0;
				if(current==first)System.out.println("====ROUND "+(++round)+"====");//?
			}
		}
		System.out.println("====GAME OVER====\ntype="+(gameOver==null?"DRAW":gameOver.type));//game over
		for(int i=0;i<players.length;i++)System.out.println("player"+i+":\n\tlose="+players[i].getHero().isDying());//game over
	}
	
	public void triggerEvent(Event e)
	{
		eventHandler.triggerEvent(e);
	}
	
	public void unregisterEvents(Buff buff)
	{
		eventHandler.unregister(buff);
	}
}
