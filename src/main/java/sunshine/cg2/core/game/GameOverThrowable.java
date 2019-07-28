package sunshine.cg2.core.game;

public class GameOverThrowable extends Throwable {

	private static final long serialVersionUID = 7881776143142507181L;
	
	enum Type
	{
		NORMAL,
		CONCEDE,
		LEFT,
	}
	
	final Type type;
	
	GameOverThrowable(Type type)
	{
		this.type=type;
	}
}
