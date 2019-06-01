package sunshine.cg2.core.game;

public abstract class Rule {

	public final String name;
	public final int maxRounds;
	public final int maxHand;
	public final int maxDeck;
	public final int maxCoins;
	public final int maxField;
	
	public Rule(String name,int maxRounds,int maxHand,int maxDeck,int maxCoins,int maxField)
	{
		this.name=name;
		this.maxRounds=maxRounds;
		this.maxHand=maxHand;
		this.maxDeck=maxDeck;
		this.maxCoins=maxCoins;
		this.maxField=maxField;
	}
	
	public abstract boolean canChangeFirst(int pos,int num);
	public abstract int chooseFirst(int num);
	public abstract int getCoins(int pos);
	public abstract String[] getDeck(String[] cardSet,int pos);
	public abstract int getCoinNum(int pos,int round);
	public abstract int getDrawNum(int pos,int round);
	public abstract String[] getExtraFirst(int pos,int num);
	public abstract int getFirstCards(int pos,int num);
}
