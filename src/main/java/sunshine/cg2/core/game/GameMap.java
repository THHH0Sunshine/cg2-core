package sunshine.cg2.core.game;

import java.util.function.Consumer;

public class GameMap {

	public int setCard(int pos, Card card)
	{
		return -1;
	}
	
	public Card getCard(int pos)
	{
		return null;
	}
	
	public void removeCard(int pos)
	{
	}
	
	public void moveCardTo(int pos, int newpos)
	{
	}
	
	public void moveCardTo(Card card, int pos)
	{
	}
	
	public void moveCard(int pos, int direction)
	{
	}
	
	public void moveCard(Card card, int direction)
	{
	}
	
	public void removeCard(Card card)
	{
	}
	
	public int getPos(Card card)
	{
		return -1;
	}
	
	public void forEachCardOnMap(Consumer<? super Card> action)
	{
	}
}
