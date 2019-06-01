package sunshine.cg2.core.util;

import java.util.ArrayList;
import java.util.Collection;

public class JSONArray extends ArrayList<Object> {

	private static final long serialVersionUID = 3240143072619793222L;
	
	public JSONArray()
	{
		super();
	}
	
	public JSONArray(int startLen)
	{
		super(startLen);
	}
	
	public JSONArray(Collection<?> c)
	{
		super(c);
	}
	
	public JSONArray(Object[] elements)
	{
		super(elements.length);
		for(Object e:elements)add(e);
	}
	
	@Override
	public String toString()
	{
		int len=size();
		String[] list=new String[len];
		for(int i=0;i<len;i++)
		{
			Object ci=get(i);
			list[i]=(ci==null?"null":(ci instanceof String?"\""+ci+"\"":ci.toString()));
		}
		return "["+String.join(",",list)+"]";
	}
}
