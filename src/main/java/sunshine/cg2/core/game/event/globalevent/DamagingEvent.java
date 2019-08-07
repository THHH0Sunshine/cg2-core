package sunshine.cg2.core.game.event.globalevent;

import sunshine.cg2.core.game.Card;

public class DamagingEvent implements GlobalEvent {

	public final Card from;
	public final Card to;
	public int damage;
	
	public DamagingEvent(Card from,Card to,int damage)
	{
		this.from=from;
		this.to=to;
		this.damage=damage;
	}
}
