package sunshine.cg2.core.game.event.globalevent;

import sunshine.cg2.core.game.Player;

public class AfterTurnEndEvent implements GlobalEvent {

	public final Player player;
	
	public AfterTurnEndEvent(Player player)
	{
		this.player=player;
	}
}
