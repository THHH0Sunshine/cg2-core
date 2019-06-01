package sunshine.cg2.core.game;

public class GameManager {

	private Game game;
	
	public GameManager(GamePackage pack,Rule rule,CardSet[] sets,IO io)
	{
		game=new Game(pack,rule,sets,io);
	}
	
	public void run()
	{
		game.run();
	}
}
