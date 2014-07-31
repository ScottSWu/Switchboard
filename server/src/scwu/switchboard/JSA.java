package scwu.switchboard;
import java.util.LinkedList;


public class JSA {
	private LinkedList<Object> elements;
	
	public JSA() {
		elements = new LinkedList<Object>();
	}
	
	public JSA add(Object...e) {
		for (int i=0; i<e.length; i++) {
			elements.add(e[i]);
		}
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
	
	public String toString() {
		StringBuilder sb = new StringBuilder("[");
		boolean first = true;
		
		for (Object k : elements) {
			if (first) first = false;
			else sb.append(",");
			
			char kq = isLiteral(k)?' ':'\"';
			
			if (k==this) k = "this";
			if (k==null) k = "null";
			sb.append(kq + k.toString() + kq);
		}
		
		sb.append("]");
		
		return sb.toString();
	}
	
	public String nullify() {
		return null;
	}
}
