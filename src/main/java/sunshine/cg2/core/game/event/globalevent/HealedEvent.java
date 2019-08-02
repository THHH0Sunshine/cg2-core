package sunshine.cg2.core.game.event.globalevent;

import sunshine.cg2.core.game.Card;

public class HealedEvent implements GlobalEvent {

	public final Card from;
	public final Card to;
	public final int num;
	
	public HealedEvent(Card from,Card to,int num)
	{
		this.from=from;
		this.to=to;
		this.num=num;
	}
}
