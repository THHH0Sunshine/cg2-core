package sunshine.cg2.core.game;

import java.util.HashMap;

import sunshine.cg2.core.game.event.Event;

class EventHandler {

	private class BuffList
	{
		private java.util.HashSet<Buff> list=new java.util.HashSet<Buff>();
		
		boolean add(Buff b)
		{
			return list.add(b);
		}
		
		boolean remove(Buff b)
		{
			return list.remove(b);
		}
		
		void triggerAll(Game g,Event e)
		{
			for(Buff b:list)b.info.onTrigger(b,g,e);
		}
	}
	
	private final HashMap<Class<?>,BuffList> registry=new HashMap<>();
	
	void register(Buff buff,Class<? extends Event> select)
	{
		for(Object o:buff.info.events)
		{
			if(o instanceof Class&&select.isAssignableFrom((Class<?>)o))
			{
				BuffList bl=registry.get(o);
				if(bl==null)
				{
					bl=new BuffList();
					bl.add(buff);
					registry.put((Class<?>)o,bl);
				}
				else bl.add(buff);
			}
		}
	}
	
	void unregister(Buff buff)
	{
		registry.forEach((k,v)->{v.remove(buff);});
	}
	
	void triggerEvent(Game game,Event e)
	{
		registry.forEach((c,l)->{if(c.isAssignableFrom(e.getClass()))l.triggerAll(game,e);});
	}
}
