package sunshine.cg2.core.game.event.globalevent;

import sunshine.cg2.core.game.Player;

public class TurnStartEvent implements GlobalEvent {

public final Player player;
	
	public TurnStartEvent(Player player)
	{
		this.player = player;
	}
}
