package sunshine.cg2.core.game.event.globalevent;

import sunshine.cg2.core.game.Card;

public class MinionEnterEvent implements GlobalEvent {

	public final Card minion;
	
	public MinionEnterEvent(Card minion)
	{
		this.minion=minion;
	}
}
