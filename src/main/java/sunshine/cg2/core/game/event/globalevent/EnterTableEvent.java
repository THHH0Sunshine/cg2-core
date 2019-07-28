package sunshine.cg2.core.game.event.globalevent;

import sunshine.cg2.core.game.Card;

public class EnterTableEvent implements GlobalEvent {

	public final Card card;
	
	public EnterTableEvent(Card card)
	{
		this.card=card;
	}
}
