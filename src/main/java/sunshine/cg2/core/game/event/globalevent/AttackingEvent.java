package sunshine.cg2.core.game.event.globalevent;

import sunshine.cg2.core.game.Card;

public class AttackingEvent implements GlobalEvent {

	public final Card from;
	public final Card to;
	
	public AttackingEvent(Card from, Card to)
	{
		this.from = from;
		this.to = to;
	}
}
