package sunshine.cg2.core.io;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import sunshine.cg2.core.game.CardSet;
import sunshine.cg2.core.game.Game;
import sunshine.cg2.core.game.IO;
import sunshine.cg2.core.library.Cards;
import sunshine.cg2.core.library.Rules;
import sunshine.cg2.core.util.JSONObject;

public class Room implements IO {

	private class LeftClient implements Client
	{
		@Override
		public void send(String s)
		{
		}
	}
	
	private class GameThread implements Runnable
	{
		@Override
		public void run()
		{
			clients.forEach(c->c.send("{start:true}"));
			CardSet[] ds=new CardSet[clients.size()];
			String[] d=new String[20];
			for(int i=0;i<d.length/2;i++)d[i]="hs.basic:dwhb";
			for(int i=d.length/2;i<d.length;i++)d[i]="hs.basic:jedtj";
			for(int i=0;i<ds.length;i++)ds[i]=new CardSet("~hs.basic:hmage",d);
			Game g=new Game(Cards.DEFAULT_LIBRARY,new String[]{"hs.basic"},Rules.HEARTHSTONE,ds,Room.this);
			g.run();
			synchronized(clients)
			{
				for(int i=0;i<clients.size();i++)
				{
					Client c=clients.get(i);
					if(c instanceof LeftClient)clients.remove(i--);
					else c.send("{stop:true}");
				}
				started=false;
			}
		}
	}
	
	private boolean started=false;
	private final List<Client> clients=Collections.synchronizedList(new ArrayList<Client>());
	private int min=2;
	private int max=2;
	private final LinkedBlockingQueue<Reply> queue=new LinkedBlockingQueue<>();
	
	public boolean join(Client client)
	{
		synchronized(clients)
		{
			return client!=null&&!started&&clients.size()<max&&!clients.contains(client)&&clients.add(client);
		}
	}
	
	public void leave(Client client)
	{
		synchronized(clients)
		{
			if(client==null)return;
			if(started)
			{
				int index=clients.indexOf(client);
				if(index<0)return;
				client=new LeftClient();
				clients.set(index,client);
				postMessage(client,new byte[]{0});
			}
			else clients.remove(client);
		}
	}
	
	public boolean postMessage(Client client,byte[] message)
	{
		synchronized(clients)
		{
			if(client==null||message==null||!started)return false;
			int who=clients.indexOf(client);
			if(who<0)return false;
			queue.add(new Reply(who,message));
			return true;
		}
	}
	
	public boolean start(Client client)
	{
		synchronized(clients)
		{
			if(client==null||started||clients.size()<min||!clients.contains(client))return false;
			queue.clear();
			started=true;
			new Thread(new GameThread()).start();
			return true;
		}
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
