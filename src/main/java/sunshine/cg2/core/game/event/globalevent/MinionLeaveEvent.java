package sunshine.cg2.core.game.event.globalevent;

import sunshine.cg2.core.game.Card;

public class MinionLeaveEvent implements GlobalEvent {

	public enum Reason
	{
		CONTROL,
		DEATH,
		MOVE,
		TRANSFORM
	}
	
	public final Card minion;
	public final Reason reason;
	
	public MinionLeaveEvent(Card minion,Reason reason)
	{
		this.minion=minion;
		this.reason=reason;
	}
}
