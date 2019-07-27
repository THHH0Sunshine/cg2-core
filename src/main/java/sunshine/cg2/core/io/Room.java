package sunshine.cg2.core.io;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import sunshine.cg2.core.game.CardPackage;
import sunshine.cg2.core.game.CardSet;
import sunshine.cg2.core.game.Game;
import sunshine.cg2.core.game.GamePackage;
import sunshine.cg2.core.game.IO;
import sunshine.cg2.core.library.Cards;
import sunshine.cg2.core.library.Rules;
import sunshine.cg2.core.util.JSONObject;

public class Room implements IO {

	private class GameThread implements Runnable
	{
		@Override
		public void run()
		{
			for(Client c:clients)c.send("{start:true}");
			CardSet[] ds=new CardSet[clients.size()];
			String[] d=new String[20];
			for(int i=0;i<d.length/2;i++)d[i]="cg2:minion0";
			for(int i=d.length/2;i<d.length;i++)d[i]="hs.basic:zlzc";
			for(int i=0;i<ds.length;i++)ds[i]=new CardSet("cg2:hero0",d);
			Game g=new Game(new GamePackage(new CardPackage[]{Cards.BASIC_CARDS}),Rules.HEARTHSTONE,ds,Room.this);
			g.run();
			for(Client c:clients)c.send("{stop:true}");
			started=false;
		}
	}
	
	private volatile boolean started=false;
	private final ArrayList<Client> clients=new ArrayList<>();
	private int min=2;
	private int max=2;
	private final LinkedBlockingQueue<Reply> queue=new LinkedBlockingQueue<>();
	
	public boolean join(Client client)
	{
		return client!=null&&!started&&clients.size()<max&&!clients.contains(client)&&clients.add(client);
	}
	
	public void leave(Client client)
	{
		if(client==null)return;
		if(started)postMessage(client,new byte[]{0});//if started 'turn into a computer'
		clients.remove(client);
	}
	
	public boolean postMessage(Client client,byte[] message)
	{
		if(client==null||message==null||!started)return false;
		int who=clients.indexOf(client);
		if(who<0)return false;
		queue.add(new Reply(who,message));
		return true;
	}
	
	public boolean start(Client client)
	{
		if(client==null||started||clients.size()<min||!clients.contains(client))return false;
		queue.clear();
		started=true;
		new Thread(new GameThread()).start();
		return true;
	}
	
	@Override
	public void sendTo(int who,JSONObject msg)
	{
		clients.get(who).send(msg.toString());
	}
	
	@Override
	public Reply recv()
	{
		while(true)
		{
			Reply m;
			try
			{
				m=queue.take();
			}
			catch(InterruptedException e)
			{
				Thread.currentThread().interrupt();
				continue;
			}
			if(m.who<0||m.who>=clients.size()||m.data.length<=0)continue;
			return m;
		}
	}
}
