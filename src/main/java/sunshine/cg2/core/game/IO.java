package sunshine.cg2.core.game;

import sunshine.cg2.core.util.JSONObject;

public interface IO {

	public class Reply
	{
		public int who;
		public byte[] data;
		
		public Reply(int who,byte[] data)
		{
			this.who=who;
			this.data=data;
		}
	}
	
	void sendTo(int who,JSONObject msg);
	Reply recv();
}
