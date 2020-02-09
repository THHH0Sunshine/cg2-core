package sunshine.cg2.core.game.event.globalevent;

import sunshine.cg2.core.game.Card;

public class EnterTableEvent implements GlobalEvent {

	public final Card card;
	public final byte[] addition;
	
	public EnterTableEvent(Card card,byte[] addition)
	{
		this.card=card;
		this.addition=addition;
	}
}
