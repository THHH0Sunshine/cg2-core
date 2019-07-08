package sunshine.cg2.core.game;

import java.util.function.Consumer;

public interface GameMap {

	public static class NoMap implements GameMap
	{
		@Override
		public int setCard(int pos, Card card)
		{
			throw new UnsupportedOperationException();
		}
		
		@Override
		public Card getCard(int pos)
		{
			throw new UnsupportedOperationException();
		}
		
		@Override
		public void removeCard(int pos)
		{
			throw new UnsupportedOperationException();
		}
		
		@Override
		public void moveCardTo(int pos, int newpos)
		{
			throw new UnsupportedOperationException();
		}
		
		@Override
		public void moveCardTo(Card card, int pos)
		{
			throw new UnsupportedOperationException();
		}
		
		@Override
		public void moveCard(int pos, int direction)
		{
			throw new UnsupportedOperationException();
		}
		
		@Override
		public void moveCard(Card card, int direction)
		{
			throw new UnsupportedOperationException();
		}
		
		@Override
		public void removeCard(Card card)
		{
			throw new UnsupportedOperationException();
		}
		
		@Override
		public int getPos(Card card)
		{
			throw new UnsupportedOperationException();
		}
		
		@Override
		public void forEachCardOnMap(Consumer<? super Card> action)
		{
			throw new UnsupportedOperationException();
		}
	}
	
	int setCard(int pos, Card card);
	Card getCard(int pos);
	void removeCard(int pos);
	void removeCard(Card card);
	void moveCardTo(int pos, int newpos);
	void moveCardTo(Card card, int pos);
	void moveCard(int pos, int direction);
	void moveCard(Card card, int direction);
	int getPos(Card card);
	void forEachCardOnMap(Consumer<? super Card> action);
}
