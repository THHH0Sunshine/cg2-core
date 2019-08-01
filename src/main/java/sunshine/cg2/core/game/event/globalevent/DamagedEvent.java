package sunshine.cg2.core.game.event.globalevent;

import sunshine.cg2.core.game.Card;

public class DamagedEvent implements GlobalEvent {

	public final Card from;
	public final Card to;
	public final int damage;
	
	public DamagedEvent(Card from,Card to,int damage)
	{
		this.from=from;
		this.to=to;
		this.damage=damage;
	}
}
