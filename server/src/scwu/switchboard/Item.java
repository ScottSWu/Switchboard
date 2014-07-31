package scwu.switchboard;
import java.util.ArrayList;
import java.util.HashSet;
import org.json.JSONArray;
import org.json.JSONObject;

public class Item {
	public SwitchboardServer parent;
	public String name;
	public String owner;
	public String publicImage;
	public String privateImage;
	public long lastUpdate;
	public boolean deleted;
	public boolean visible;
	public boolean publicVisibility;
	public boolean draggable;
	public HashSet<User> privateVisibility;
	public User possession;
	public Dim location;
	public ArrayList<Option> options;
	public boolean publicMark;
	
	public Item(SwitchboardServer server,String name,String owner,String publicImage,String privateImage,boolean visible,
		boolean publicVisibility,boolean draggable,Dim location,boolean publicMark,JSONArray options) {
		parent = server;
		this.name = name;
		this.owner = owner;
		this.publicImage = publicImage;
		this.privateImage = (privateImage==null || privateImage.length()==0) ? publicImage : privateImage;
		this.lastUpdate = System.currentTimeMillis();
		this.deleted = false;
		this.visible = visible;
		this.publicVisibility = publicVisibility;
		this.privateVisibility = new HashSet<User>();
		this.draggable = draggable;
		this.possession = null;
		this.location = location;
		this.publicMark = publicMark;
		
		this.options = new ArrayList<Option>(options.length());
		for (int i=0; i<options.length(); i++) {
			try {
				JSONObject op = options.getJSONObject(i);
				this.options.add(new Option(op.getString("label"),op.optString("action")));
			}
			catch (Exception e) {
				
			}
		}
	}
	
	public boolean isVisible(User u) {
		if (u.hash.equals(owner)) return true;
		if (publicVisibility) return true;
		return privateVisibility.contains(u);
	}
	
	public JSA getOptions() {
		JSA oplist = new JSA();
		for (int i=0; i<options.size(); i++) {
			oplist.add(options.get(i).label);
		}
		return oplist;
	}
	
	public void setProperty(String property,Object arg) {
		if (property.equalsIgnoreCase("name")) {
			name = arg.toString();
		}
		else if (property.equalsIgnoreCase("owner")) {
			owner = arg.toString();
		}
		else if (property.equalsIgnoreCase("publicImage")) {
			publicImage = arg.toString();
		}
		else if (property.equalsIgnoreCase("privateImage")) {
			privateImage = arg.toString();
		}
		else if (property.equalsIgnoreCase("deleted")) {
			deleted = Boolean.parseBoolean(arg.toString());
		}
		else if (property.equalsIgnoreCase("visible")) {
			visible = Boolean.parseBoolean(arg.toString());
		}
		else if (property.equalsIgnoreCase("publicVisibility")) {
			publicVisibility = Boolean.parseBoolean(arg.toString());
		}
		else if (property.equalsIgnoreCase("privateVisibility") && arg instanceof JSONArray) {
			JSONArray args = (JSONArray) arg;
			for (int j = 0; j < args.length(); j++) {
				User u = parent.getUserByHash(args.getString(j));
				if (u != null) {
					if (privateVisibility.contains(u)) {
						privateVisibility.remove(u);
					}
					else {
						privateVisibility.add(u);
					}
				}
			}
		}
		
		else if (property.equalsIgnoreCase("location") && arg instanceof JSONObject) {
			JSONObject args = (JSONObject) arg;
			location.x = Float.parseFloat(args.getString("x"));
			location.y = Float.parseFloat(args.getString("y"));
			location.z = Float.parseFloat(args.getString("z"));
			location.w = Float.parseFloat(args.getString("w"));
			location.h = Float.parseFloat(args.getString("h"));
		}
		else if (property.equalsIgnoreCase("location-x")) {
			location.x = Float.parseFloat(arg.toString());
		}
		else if (property.equalsIgnoreCase("location-y")) {
			location.y = Float.parseFloat(arg.toString());
		}
		else if (property.equalsIgnoreCase("location-w")) {
			location.w = Float.parseFloat(arg.toString());
		}
		else if (property.equalsIgnoreCase("location-h")) {
			location.h = Float.parseFloat(arg.toString());
		}
		else if (property.equalsIgnoreCase("publicMark")) {
			publicMark = Boolean.parseBoolean(arg.toString());
		}
		
		lastUpdate = System.currentTimeMillis();
	}
}
