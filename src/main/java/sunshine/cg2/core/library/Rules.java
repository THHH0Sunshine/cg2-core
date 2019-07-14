package sunshine.cg2.core.library;

import java.util.LinkedList;
import java.util.function.Consumer;

import sunshine.cg2.core.game.Card;
import sunshine.cg2.core.game.Game;
import sunshine.cg2.core.game.GameMap;
import sunshine.cg2.core.game.Rule;

public class Rules {

	private static class HsTable extends GameMap
	{
		private final LinkedList<Card> cards=new LinkedList<>();
		private int nextNumber;
		
		@Override
		public int setCard(int pos, Card card)
		{
			cards.add(card);
			return nextNumber++;
		}
		
		@Override
		public void removeCard(Card card)
		{
			cards.remove(card);
		}
		
		@Override
		public void forEachCardOnMap(Consumer<? super Card> action)
		{
			cards.forEach(action);
		}
	}
	
	public static final Rule TEST=new Rule("cg2:test",10,10,999,10,7)
	{
		public GameMap initMap(Game game)
		{
			return new HsTable();
		}
		public boolean canChangeFirst(int pos,int num)
		{
			return false;
		}
		public int chooseFirst(int num)
		{
			return 0;
		}
		public int getCoins(int pos)
		{
			return 10;
		}
		public String[] getDeck(String[] cardSet,int pos)
		{
			return cardSet.clone();
		}
		public int getCoinNum(int pos,int round)
		{
			return 1;
		}
		public int getDrawNum(int pos,int round)
		{
			return 1;
		}
		public String[] getExtraFirst(int pos,int num)
		{
			return new String[]{"cg2:spell0"};
		}
		public int getFirstCards(int pos,int num)
		{
			return 5;
		}
	};
	
	public static final Rule HEARTHSTONE=new Rule("cg2:hearthstone",60,10,60,10,7)
	{
		public GameMap initMap(Game game)
		{
			return new HsTable();
		}
		public boolean canChangeFirst(int pos,int num)
		{
			return true;
		}
		public int chooseFirst(int num)
		{
			return (int)Math.random()*num;
		}
		public int getCoins(int pos)
		{
			return 0;
		}
		public String[] getDeck(String[] cardSet,int pos)
		{
			int l=cardSet.length;
			String[] rt=new String[l];
			String[] set=cardSet.clone();
			for(int i=0;i<l;i++)
			{
				int n=(int)(Math.random()*(double)(l-i));
				rt[i]=set[n];
				for(int j=n;j<l-i-1;j++)set[j]=set[j+1];
			}
			return rt;
		}
		public int getCoinNum(int pos,int round)
		{
			return 1;
		}
		public int getDrawNum(int pos,int round)
		{
			return 1;
		}
		public String[] getExtraFirst(int pos,int num)
		{
			if(pos==num-1)return new String[]{"cg2:spell0"};
			return null;
		}
		public int getFirstCards(int pos,int num)
		{
			return pos==0?3:4;
		}
	};
}
