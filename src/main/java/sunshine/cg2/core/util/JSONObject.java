package sunshine.cg2.core.util;

import java.util.HashMap;
import java.util.Map;

public class JSONObject extends HashMap<String,Object> {

	private static final long serialVersionUID = -4582663063438124841L;
	
	public JSONObject()
	{
		super();
	}
	
	public JSONObject(int startLen)
	{
		super(startLen);
	}
	
	public JSONObject(int startLen,float factor)
	{
		super(startLen,factor);
	}
	
	public JSONObject(Object[][] pairs)
	{
		super(pairs.length);
		for(Object[] pair:pairs)put(pair[0].toString(),pair[1]);
	}
	
	public JSONObject(Map<? extends String,?> m)
	{
		super(m);
	}
	
	@Override
	public String toString()
	{
		String[] list=new String[size()];
		String[] keys=keySet().toArray(list);
		for(int i=0;i<list.length;i++)
		{
			Object v=get(keys[i]);
			list[i]="\""+keys[i]+"\":"+(v==null?"null":(v instanceof String?"\""+v+"\"":v.toString()));
		}
		return "{"+String.join(",",list)+"}";
	}
}
