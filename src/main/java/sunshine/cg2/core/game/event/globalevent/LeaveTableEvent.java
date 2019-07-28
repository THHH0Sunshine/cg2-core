package sunshine.cg2.core.game.event.globalevent;

import sunshine.cg2.core.game.Card;

public class LeaveTableEvent implements GlobalEvent {

	public enum Reason
	{
		CONTROL,
		DEATH,
		MOVE,
		CHANGEHERO,
		TRANSFORM
	}
	
	public final Card card;
	public final Reason reason;
	
	public LeaveTableEvent(Card card,Reason reason)
	{
		this.card=card;
		this.reason=reason;
	}
}
