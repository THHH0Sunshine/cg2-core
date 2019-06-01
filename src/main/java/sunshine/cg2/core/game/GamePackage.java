package sunshine.cg2.core.game;

import java.util.HashMap;

public class GamePackage {

	private final HashMap<String,CardInfo> cards=new HashMap<>();
	
	public GamePackage(CardPackage[] cardPackages,String[] bannedCards)
	{
		if(cardPackages!=null)
		{
			for(CardPackage p:cardPackages)
			{
				for(CardInfo c:p.getCardList())cards.put(c.name,c);
			}
		}
		if(bannedCards!=null)for(String s:bannedCards)cards.remove(s);
	}
	
	public CardInfo getCardInfo(String name)
	{
		return cards.get(name);
	}
}
