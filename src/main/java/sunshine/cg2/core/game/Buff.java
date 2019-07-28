package sunshine.cg2.core.game;

import sunshine.cg2.core.game.event.Event;
import sunshine.cg2.core.util.JSONArray;
import sunshine.cg2.core.util.JSONObject;

public class Buff {

	public final Card toBuff;
	public final Buff effectSource;
	public final String name;
	public final BuffInfo info;
	
	Buff(BuffInfo info,String name,Card toBuff,Buff effectSource)
	{
		this.info=info;
		this.name=name;
		this.toBuff=toBuff;
		this.effectSource=effectSource;
	}
	
	JSONObject getObject()
	{
		JSONArray kws;
		if(info.keyWords==null)kws=new JSONArray();
		else
		{
			kws=new JSONArray(info.keyWords.length);
			for(BuffInfo.KeyWord kw:info.keyWords)kws.add(kw.name());
		}
		return new JSONObject(new Object[][]{{"name",name},{"keywords",kws}});
	}
	
	void triggerSelf(Event event)
	{
		if(info.events==null)return;
		for(Object o:info.events)
		{
			if(o==event.getClass())
			{
				info.onTrigger(this,event);
				break;
			}
		}
	}
}
