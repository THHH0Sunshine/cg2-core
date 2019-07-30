package sunshine.cg2.core.game.event.globalevent;

import sunshine.cg2.core.game.Card;

public class SummonEvent implements GlobalEvent {

	public final Card minion;
	
	public SummonEvent(Card minion)
	{
		this.minion = minion;
	}
}
