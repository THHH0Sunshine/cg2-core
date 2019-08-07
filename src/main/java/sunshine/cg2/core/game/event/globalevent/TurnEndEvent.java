package sunshine.cg2.core.game.event.globalevent;

import sunshine.cg2.core.game.Player;

public class TurnEndEvent implements GlobalEvent {

	public final Player player;
	
	public TurnEndEvent(Player player)
	{
		this.player = player;
	}
}
