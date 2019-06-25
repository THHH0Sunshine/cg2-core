package sunshine.cg2.core.game;

import java.util.HashMap;

public class GamePackage {

	private final HashMap<String,CardInfo> allCards=new HashMap<>();
	
	public GamePackage(CardPackage[] cardPackages)
	{
		for(CardPackage p:cardPackages)
			for(CardInfo c:p.getAllCards())allCards.put(c.name,c);
	}
	
	public CardInfo getCardInfo(String name)
	{
		return allCards.get(name);
	}
}
