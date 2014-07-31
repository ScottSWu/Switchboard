package scwu.switchboard;
import java.util.HashMap;


public class JSO {
	private HashMap<Object,Object> pairs;
	
	public JSO() {
		pairs = new HashMap<Object,Object>();
	}
	
	public JSO add(Object key,Object value) {
		pairs.put(key, value);
		return this;
	}
	
	private boolean isLiteral(Object o) {
		return	o == null ||
				o == this ||
				o instanceof JSO ||
				o instanceof JSA ||
				o instanceof Integer ||
				o instanceof Double ||
				o instanceof Long ||
				o instanceof Float ||
				o instanceof Short ||
				o instanceof Byte ||
				o instanceof Boolean;
	}
	
	public boolean hasKey(Object key) {
		return pairs.containsKey(key);
	}
	
	public Object get(Object key) {
		return pairs.get(key);
	}
	
	public void set(Object key,Object value) {
		pairs.put(key,value);
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder("{");
		boolean first = true;
		
		for (Object k : pairs.keySet()) {
			Object v = pairs.get(k);
			
			if (first) first = false;
			else sb.append(",");
			char kq = isLiteral(k)?' ':'\"';
			char vq = isLiteral(v)?' ':'\"';
			
			if (v==this) v = "this";
			if (k==null) continue;
			if (v==null) v = "null";
			
			sb.append(kq + k.toString() + kq + ":" + vq + v.toString() + vq);
		}
		
		sb.append("}");
		
		return sb.toString();
	}
	
	public String nullify() {
		return null;
	}
}
