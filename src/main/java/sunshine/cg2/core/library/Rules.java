package sunshine.cg2.core.library;

import sunshine.cg2.core.game.Rule;

public class Rules {

	public static final Rule TEST=new Rule("cg2:test",10,10,999,10,7)
	{
		@Override public boolean canChangeFirst(int pos,int num)
		{
			return false;
		}
		@Override public int chooseFirst(int num)
		{
			return 0;
		}
		@Override public int getCoins(int pos)
		{
			return 10;
		}
		@Override public String[] getDeck(String[] cardSet,int pos)
		{
			return cardSet.clone();
		}
		@Override public int getCoinNum(int pos,int round)
		{
			return 1;
		}
		@Override public int getDrawNum(int pos,int round)
		{
			return 1;
		}
		@Override public String[] getExtraFirst(int pos,int num)
		{
			return new String[]{"cg2:spell0"};
		}
		@Override public int getFirstCards(int pos,int num)
		{
			return 5;
		}
	};
	
	public static final Rule HEARTHSTONE=new Rule("cg2:hearthstone",60,10,60,10,7)
	{
		@Override public boolean canChangeFirst(int pos,int num)
		{
			return true;
		}
		@Override public int chooseFirst(int num)
		{
			return (int)Math.random()*num;
		}
		@Override public int getCoins(int pos)
		{
			return 0;
		}
		@Override public String[] getDeck(String[] cardSet,int pos)
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
		@Override public int getCoinNum(int pos,int round)
		{
			return 1;
		}
		@Override public int getDrawNum(int pos,int round)
		{
			return 1;
		}
		@Override public String[] getExtraFirst(int pos,int num)
		{
			if(pos==num-1)return new String[]{"cg2:spell0"};
			return null;
		}
		@Override public int getFirstCards(int pos,int num)
		{
			return pos==0?3:4;
		}
	};
}
