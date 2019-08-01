package sunshine.cg2.core.game;

import sunshine.cg2.core.game.event.Event;

public class BuffInfo {

	public enum KeyWord
	{
		CHARGE,
		FROZEN,
		IMMUNE,
		MM,
		RUSH,
		STEALTH,
		TAUNT,
		WINDFURY,
		POISONOUS,
		WOOD
	}
	
	public final KeyWord[] keyWords;
	public final Object[] events;
	public final boolean isEffect;
	
	public BuffInfo(KeyWord[] keyWords,Object[] events,boolean isEffect)
	{
		this.keyWords=keyWords;
		this.events=events;
		this.isEffect=isEffect;
	}
	
	public void onTrigger(Buff buff,Event event)
	{
	}
}
