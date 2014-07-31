package scwu.switchboard;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Pattern;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.apache.commons.lang3.StringEscapeUtils;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONArray;
import org.json.JSONObject;

public class SwitchboardServer extends WebSocketServer {
	
	/**
	 * Main method
	 * 
	 * @param args Command line arguments
	 */
	public static void main(String args[]) {
		// Default port
		//int port = 62696;
		int port = 80;
		
		if (args.length > 0) {
			try {
				port = Integer.parseInt(args[0]);
			}
			catch (Exception e) {
				
			}
		}
		
		// Queue any extra commands
		final Queue<String> prec = new LinkedList<String>();
		if (args.length>1) {
			for (int i=1; i<args.length; i++) {
				prec.add(args[i]);
			}
		}
		
		System.out.println("Running on port " + port);
		
		try {
			// Create a server on the specified port
			final SwitchboardServer tts = new SwitchboardServer(new InetSocketAddress(port));
			
			// Server thread
			new Thread(new Runnable() {
				public void run() {
					tts.start();
				}
			}).start();
			
			// Input thread
			new Thread(new Runnable() {
				public void run() {
					Scanner cin = new Scanner(System.in);
					String line;
					while (true) {
						System.out.print("> ");
						if (!prec.isEmpty()) { // Execute queued commands first
							line = prec.poll();
							System.out.println(line);
						}
						else {
							line = cin.nextLine();
						}
						
						String[] parts = line.split(" ");
						if (parts.length > 0) {
							try {
								/*
								if (parts[0].equalsIgnoreCase("exec")) { // Execute a file
									for (int i=1; i<parts.length; i++) {
										tts.cmd_file(parts[i]);
									}
								}
								*/
								if (parts[0].equalsIgnoreCase("stop") || parts[0].equalsIgnoreCase("exit")) { // Stop the server
									cin.close();
									System.exit(0);
								}
								else if (parts[0].equalsIgnoreCase("export")) { // Export the server state
									
								}
								else if (parts[0].equalsIgnoreCase("import")) { // Import the server state
									
								}
								else { // Execute a normal command
									tts.cmd_root(line);
								}
							}
							catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				}
			}).start();
		}
		catch (Exception e) {
			e.printStackTrace();
			System.out.println("Closed");
		}
	}
	
	/**
	 * Instance of this class
	 */
	final private SwitchboardServer server = this;
	/**
	 * Map of current users by connection address
	 */
	private HashMap<InetSocketAddress,User> users;
	/**
	 * Map of current items by hashcode
	 */
	private HashMap<Integer,Item> items;
	/**
	 * List of chat messages
	 */
	private ArrayList<Chat> chats;
	/**
	 * Map of scriptable context menu options by hashcode
	 */
	//private HashMap<Integer,Option> options;
	/**
	 * Javascript Engine
	 */
	private ScriptEngine engine;
	/**
	 * Random number generator object
	 */
	private Random rgen;
	/**
	 * Root user object
	 */
	private User root;
	/**
	 * Unencrypted root key for limited remote access
	 */
	private String rootkey;
	
	public SwitchboardServer(InetSocketAddress arg0) throws Exception {
		super(arg0);
		
		users = new HashMap<InetSocketAddress,User>();
		chats = new ArrayList<Chat>();
		items = new HashMap<Integer,Item>();
		
		rgen = new Random();
		
		root = new User(this,null);
		root.hasroot = true;
		rootkey = rgen.nextLong() + " " + rgen.nextLong(); // No one could guess this
		
		commands = initCommands();
		responses = initResponses();
		engine = getCleanEngine();
	}
	
	// Scripting Engine
	private ScriptEngine getCleanEngine() throws FileNotFoundException, ScriptException, URISyntaxException {
		ScriptEngineManager m = new ScriptEngineManager();
		ScriptEngine se = m.getEngineByName("JavaScript");
		
		se.eval(new InputStreamReader(Utils.class.getResourceAsStream("/scwu/js/json.js")));
		se.eval(new InputStreamReader(Utils.class.getResourceAsStream("/scwu/js/init.js")));
		
		return se;
	}
	
	// Commands
	/**
	 * List of all available commands
	 */
	private HashMap<String,Command> commands;
	
	private abstract class Command {
		private JSONObject defaults;
		private boolean needroot;
		
		public Command(JSONObject d) {
			defaults = d;
			needroot = false;
		}
		
		public Command(JSONObject d,boolean r) {
			this(d);
			needroot = r;
		}

		public void run(User u,String[] sargs) {
			if (needroot && !u.hasroot) return;
			
			JSONObject args = new JSONObject(defaults);
			
			for (int i=1; i<sargs.length; i+=2) {
				args.put(sargs[i],sargs[i+1]);
			}
			
			execute(u,args);
		}
		
		public void run(User u,JSONObject args) {
			if (needroot && !u.hasroot) return;
			
			String[] names = JSONObject.getNames(defaults);
			if (names!=null) {
				for (String n : names) {
					if (!args.has(n)) args.put(n, defaults.get(n));
				}
			}
			
			execute(u,args);
		}
		
		//public abstract void execute(User u,String[] args);
		
		protected abstract void execute(User u,JSONObject args);
	}
	
	public HashMap<String,Command> initCommands() {
		HashMap<String,Command> cl = new HashMap<String,Command>();
		cl.put("list",new Command(
				new JSONObject()
					.put("target","users")
				,true) { // List
			protected void execute(User user,JSONObject args) {
				if ("users".equals(args.optString("target"))) {
					for (InetSocketAddress ia:users.keySet()) {
						User u = users.get(ia);
						System.out.println("\t" + u.hash + "\t" + u.name + "\t" + u.connection.toString() + "\t" + u.location);
					}
				}
				else if ("items".equals(args.optString("target"))) {
					for (int h:items.keySet()) {
						Item i = items.get(h);
						System.out.println("\t" + (i.deleted ? "X" : "O") + (i.publicVisibility ? "P" : "R") + "\t" + String.format("%08x",Integer.valueOf(h)) + "\t" + i.owner + "\t" + i.name + "\t" + i.location + "\t" + i.publicImage + "\t");
					}
				}
				else if ("rootkey".equalsIgnoreCase(args.optString("target"))) {
					System.out.println(rootkey);
				}
			}
		});
		cl.put("add",new Command(
				new JSONObject()
					.put("name", "")
					.put("owner", "")
					.put("publicImage", "")
					.put("privateImage", "")
					.put("visible", true)
					.put("publicVisibility", true)
					.put("draggable", true)
					.put("x", 0)
					.put("y", 0)
					.put("z", 0)
					.put("w", 0)
					.put("h", 0)
					.put("publicMark", false)
					.put("options", new JSONArray())
				,false) { // Add
			protected void execute(User user,JSONObject args) {
				synchronized (items) {
					Item newItem = new Item(
						server,
						args.optString("name"),
						args.optString("owner"),
						args.optString("publicImage"),
						args.optString("privateImage"),
						Boolean.parseBoolean(args.optString("visible")),
						Boolean.parseBoolean(args.optString("publicVisibility")),
						Boolean.parseBoolean(args.optString("draggable")),
						new Dim(
							Float.parseFloat(args.optString("x")),
							Float.parseFloat(args.optString("y")),
							Float.parseFloat(args.optString("z")),
							Float.parseFloat(args.optString("w")),
							Float.parseFloat(args.optString("h"))
						),
						Boolean.parseBoolean(args.optString("publicMark")),
						args.optJSONArray("options")
					);
					
					addItem(newItem);
				}
			}
		});
		cl.put("set",new Command(
				new JSONObject()
			,false) { // Set properties of an item
			protected void execute(User user,JSONObject args) {
				if ("item".equalsIgnoreCase(args.optString("target"))) {
					Item i = null;
					if (args.has("hash")) {
						i = items.get(Integer.parseInt(args.optString("hash")));
					}
					else if (args.has("name")) {
						i = getItemByName(args.optString("name"));
					}
					if (i != null) {
						if (i.owner==null || i.owner.equals("") || i.owner.equals(user.hash) || user.hasroot) {
							i.setProperty(args.optString("property"),args.get("value"));
						}
					}
				}
				else if ("user".equalsIgnoreCase(args.optString("target"))) {
					User u = null;
					if (args.has("hash")) {
						u = getUserByHash(args.optString("hash"));
					}
					else if (args.has("name")) {
						u = getUserByName(args.optString("name"));
					}
					if (u != null) {
						if (u==user || user.hasroot) {
							u.setProperty(args.optString("property"),args.get("value"));
						}
					}
				}
			}
		});
		cl.put("clear",new Command(
			new JSONObject()
			,true) { // Clear items
			protected void execute(User user,JSONObject args) {
				if ("items".equalsIgnoreCase(args.optString("target"))) {
					Pattern re = Pattern.compile(args.optString("names"));
					for (Integer i : items.keySet()) {
						Item it = items.get(i);
						if (re.matcher(it.name).matches()) {
							deleteItem(it);
						}
					}
				}
				else if ("item".equalsIgnoreCase(args.optString("target"))) {
					JSONArray names = args.getJSONArray("names");
					for (int j = 0; j < names.length(); j++) {
						String n = names.optString(j);
						for (Integer i : items.keySet()) {
							Item it = items.get(i);
							if (n.equals(it.name)) {
								deleteItem(it);
							}
						}
					}
				}
			}
		});
		cl.put("reset",new Command(
			new JSONObject()
			,true) { // Reset all
			protected void execute(User user,JSONObject args) {
				long now = System.currentTimeMillis();
				
				for (InetSocketAddress i : users.keySet()) {
					User u = users.get(i);
					u.socket.send(new JSO()
						.add("type","ZZR")
						.toString()
					);
					u.lastItemUpdate = now;
				}
				
				synchronized (items) {
					items.clear();
				}
			}
		});
		cl.put("deal",new Command(
			new JSONObject()
				.put("items",".*")
			,true) { // Deal items to players by shuffling
			protected void execute(User user,JSONObject args) {
				ArrayList<Item> dealFrom = new ArrayList<Item>();
				ArrayList<User> dealTo = new ArrayList<User>();
				int limit = -1;
				
				for (int h : items.keySet()) {
					Item i = items.get(h);
					if (Pattern.matches(args.optString("items"),i.name)) {
						i.publicVisibility = false;
						dealFrom.add(i);
					}
				}
				
				if (args.has("limit")) {
					limit = Integer.parseInt(args.optString("limit"));
				}
				if (args.has("users")) {
					JSONArray ulist = args.getJSONArray("users");
					for (int i = 0; i < ulist.length(); i++) {
						User u = getUserByHash(ulist.optString(i));
						if (u != null) dealTo.add(u);
					}
				}
				else {
					for (InetSocketAddress i:users.keySet()) {
						dealTo.add(users.get(i));
					}
				}
				
				if (dealTo.size() == 0) return;
				
				// Deal out cards to each person
				Collections.shuffle(dealFrom);
				int dti = 0;
				for (int i = 0; i < dealFrom.size() && i < limit * dealTo.size(); i++) {
					Item df = dealFrom.get(i);
					User dt = dealTo.get(dti % dealTo.size());
					
					df.owner = dt.hash;
					df.publicVisibility = false;
					
					dti++;
				}
				
				// Shuffle card positions around
				for (int i = 0; i < 10; i++) {
					for (int j = 0; j < dealFrom.size(); j++) {
						Item curr = dealFrom.get(j);
						Item other = dealFrom.get((int) (Math.random() * dealFrom.size()));
						
						Dim temp = curr.location;
						curr.location = other.location;
						other.location = temp;
					}
				}
				
				long now = System.currentTimeMillis();
				for (Item i:dealFrom) {
					i.lastUpdate = now;
				}
			}
		});
		cl.put("exec",new Command(
			new JSONObject()
				.put("scripts", new JSONArray())
			,true) {
			protected void execute(User user,JSONObject args) {
				if (user!=root) return; // Not for remote execution
				
				JSONArray scripts = args.getJSONArray("scripts");
				for (int i=0; i<scripts.length(); i++) {
					try {
						File scriptFile = new File(scripts.optString(i));
						engine.eval(new FileReader(scriptFile));
						
						JSONArray commands = new JSONArray(engine.eval("SB._pop_commands();").toString());
						for (int j=0; j<commands.length(); j++) {
							cmd(user,commands.getJSONObject(j));
						}
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
		cl.put("eval",new Command(
			new JSONObject()
				.put("code", "")
			,true) {
			protected void execute(User user,JSONObject args) {
				try {
					engine.eval(args.optString("code"));
				}
				catch (ScriptException e) {
					e.printStackTrace();
				}
			}
		});
		cl.put("root",new Command(
			new JSONObject()
			,false) {
			protected void execute(User user,JSONObject args) {
				if (!user.hasroot && user.lockout==0) return;
				
				if (rootkey.equals(args.optString("key"))) {
					user.hasroot = true;
					user.lockout = -10;
				}
				else if (!user.hasroot) {
					user.lockout++;
				}
			}
		});
		cl.put("rootdo",new Command(
			new JSONObject()
			,true) {
			protected void execute(User user,JSONObject args) {
				if (args.has("do")) {
					cmd_root(args.getJSONObject("do"));
				}
			}
		});
		cl.put("remove",new Command(
			new JSONObject()
			,true) {
			protected void execute(User user,JSONObject args) {
				if (!args.has("target")) return;
				
				if ("user".equalsIgnoreCase(args.optString("target"))) {
					
				}
			}
		});
		cl.put("send",new Command(
				new JSONObject()
				,true) {
			protected void execute(User user,JSONObject args) {
				if (!args.has("data")) return;
				
				User u = null;
				if (args.has("name")) {
					u = getUserByName(args.optString("name"));
				}
				else if (args.has("hash")) {
					u = getUserByHash(args.optString("hash"));
				}
				
				if (u!=null) {
					String data = args.getJSONObject("data").toString();
					u.socket.send(data);
				}
			}
		});
		cl.put("sendall",new Command(
				new JSONObject()
				,true) {
			protected void execute(User user,JSONObject args) {
				if (!args.has("data")) return;
				
				String data = args.getJSONObject("data").toString();
				
				User u;
				for (InetSocketAddress i : users.keySet()) {
					u = users.get(i);
					u.socket.send(data.toString());
				}
			}
		});
		/*
		cl.put("",new Command() {
			public void execute(User user,String[] args) {
				
			}
		});
		 */
		return cl;
	}
	
	public void cmd(User u,String argline) {
		try {
			JSONObject args = new JSONObject(argline);
			cmd(u,args);
		}
		catch (Exception e) {
			String[] args = Utils.cmd_parse(argline);
			if (args.length>0) {
				String cmdkey = args[0];
				if (commands.containsKey(cmdkey)) {
					commands.get(cmdkey).run(u, args);
				}
			}
		}
	}
	
	public void cmd(User u,JSONObject args) {
		if (args.has("cmd")) {
			String cmdkey = args.optString("cmd");
			if (commands.containsKey(cmdkey)) {
				commands.get(cmdkey).run(u, args);
			}
		}
		else {
			//System.out.println("\'" + args[0] + "\' not found");
		}
	}

	public void cmd_root(String argline) {
		cmd(root,argline);
	}
	
	public void cmd_root(JSONObject args) {
		cmd(root,args);
	}
	
	public void cmd_file(String filename) {
		File script = new File(filename);
		if (!script.isFile()) {
			try {
				System.out.println("The file '" + script.getCanonicalPath() + "' does not exist.");
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			return;
		}
		
		try {
			Scanner fin = new Scanner(script);
			if (!fin.hasNextLine()) {
				System.out.println("No data found in '" + script.getCanonicalPath() + "'.");
			}
			else {
				String header = fin.nextLine();
				if (!header.startsWith("////")) {
					System.out.println("No header found in '" + script.getCanonicalPath() + "'.");
					header = "";
				}
				else {
					header = header.substring(4);
				}
				if (header.equals("") || header.startsWith("t1")) {
					//cmd_file_t1(fin);
				}
				else if (header.startsWith("t2")) {
					//cmd_file_t2(fin);
				}
			}
			fin.close();
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// Helper functions
	public User getUserByAddr(InetSocketAddress a) {
		return users.get(a);
	}
	
	public User getUserByHash(String h) {
		for (InetSocketAddress i:users.keySet()) {
			User u = users.get(i);
			if (u.hash!=null && u.hash.equals(h)) {
				return u;
			}
		}
		return null;
	}
	
	public User getUserByName(String n) {
		for (InetSocketAddress i:users.keySet()) {
			User u = users.get(i);
			if (u.name!=null && u.name.equals(n)) {
				return u;
			}
		}
		return null;
	}
	
	public Item getItemByHash(int h) {
		return items.get(h);
	}
	
	public Item getItemByName(String n) {
		for (Integer h:items.keySet()) {
			Item i = items.get(h);
			if (i.name.equals(n)) {
				return i;
			}
		}
		return null;
	}
	
	public boolean addUser(User u) {
		if (u == null || u.connection == null) return false;
		users.put(u.connection,u);
		return true;
	}
	
	public boolean removeUser(InetSocketAddress a) {
		if (a == null || !users.containsKey(a)) return false;
		users.remove(a);
		return true;
	}
	
	public boolean removeUser(User u) {
		if (u == null || u.connection == null || !users.containsKey(u.connection)) return false;
		users.remove(u.connection);
		return true;
	}
	
	public boolean addItem(Item i) {
		if (i == null) return false;
		items.put(i.hashCode(),i);
		return true;
	}
	
	public boolean deleteItem(Item i) {
		if (i == null) return false;
		i.deleted = true;
		i.lastUpdate = System.currentTimeMillis();
		return true;
	}
	
	public boolean removeItem(Item i) {
		if (i == null || !items.containsKey(i.hashCode())) return false;
		items.remove(i.hashCode());
		return true;
	}
	
	// Websocket Responses
	private HashMap<String,Response> responses;
	
	public abstract class Response {
		public abstract String respond(long now,User u,String type,String message);
	}
	
	public HashMap<String,Response> initResponses() {
		HashMap<String,Response> rs = new HashMap<String,Response>();
		
		rs.put("CTS", new Response() { // Chat sent
			public String respond(long now,User u,String type,String message) {
				chats.add(new Chat(u,now,message));
				
				return new JSO()
					.add("time", now)
					.add("type","CTS")
					.add("message","sent")
				.toString();
			}
		});
		rs.put("CTU", new Response() { // Chat update
			public String respond(long now,User u,String type,String message) {
				long currentTime = u.lastChatUpdate;
				try {
					currentTime = Long.parseLong(message);
				}
				catch (NumberFormatException e) {
					
				}
				
				JSO res = new JSO()
					.add("time", now)
					.add("type","CTU")
				;
				JSA carr = new JSA();
				
				res.add("data",carr);
				for (int i = 0; i<chats.size(); i++) {
					Chat c = chats.get(i);
					if (c.time >= currentTime) {
						carr.add(new JSO()
							.add("time",c.time)
							.add("hash",c.user.hash)
							.add("name",((c.user == root) ? "svop" : c.user.name))
							.add("message",StringEscapeUtils.escapeHtml4(c.message))
						);
					}
				}
				u.lastChatUpdate = now;
				
				return res.toString();
			}
		});
		
		rs.put("ITC", new Response() { // Item created
			public String respond(long now,User u,String type,String message) {
				return new JSO()
					.add("time", now)
					.add("type","ITC")
					.add("message","Item created")
				.nullify();
			}
		});
		rs.put("ITD", new Response() { // Item deleted
			public String respond(long now,User u,String type,String message) {
				return new JSO()
					.add("time", now)
					.add("type","ITD")
					.add("message","Item deleted")
				.nullify();
			}
		});
		rs.put("ITE", new Response() { // Item edit
			public String respond(long now,User u,String type,String message) {
				String[] parts = message.split(",");
				try {
					int id = Integer.parseInt(parts[0],16);
					float x = Float.parseFloat(parts[1]);
					float y = Float.parseFloat(parts[2]);
					
					Item o = getItemByHash(id);
					if (o!=null && (o.owner.equalsIgnoreCase("") || o.owner.equals(u.hash))) {
						if (o.possession == null || o.possession == u || now - o.lastUpdate >= 1000) {
							o.location.x = x;
							o.location.y = y;
							o.lastUpdate = now;
							o.possession = u;
						}
					}
				}
				catch (Exception e) {
					
				}
				return null;
			}
		});
		rs.put("ITO", new Response() { // Item option
			public String respond(long now,User u,String type,String message) {
				JSO res = new JSO().add("type", "ITO");
				
				if (message.indexOf(",")>=0) {
					String[] parts = message.split(",");
					try {
						int id = Integer.parseInt(parts[0]);
						int option = Integer.parseInt(parts[1]);
						
						Item i = getItemByHash(id);
						if (i!=null && option<i.options.size()) {
							Option o = i.options.get(option);
							String action = o.action
								.replaceAll("%%in", i.name)
								.replaceAll("%%ih", Integer.toString(id))
								.replaceAll("%%un", u.name)
								.replaceAll("%%uh", u.hash);
							engine.eval("(" + action + ")()");
							
							JSONArray commands = new JSONArray(engine.eval("SB._pop_commands();").toString());
							for (int j=0; j<commands.length(); j++) {
								cmd(u,commands.getJSONObject(j));
							}
						}
					}
					catch (Exception e) {
						
					}
				}
				return res.toString();
			}
		});
		rs.put("ITU", new Response() { // Item update
			public String respond(long now,User u,String type,String message) {
				long currentTime = u.lastItemUpdate;
				try {
					currentTime = Long.parseLong(message);
				}
				catch (NumberFormatException e) {
					
				}
				
				JSO res = new JSO()
					.add("time", now)
					.add("type", "ITU")
				;
				
				// Items
				JSO iarr = new JSO();
				res.add("data", iarr);
				
				synchronized (items) {
					for (Integer h:items.keySet()) {
						Item i = items.get(h);
						String id = String.format("%08x", Integer.valueOf(h));
						if (i.visible && i.lastUpdate>=currentTime) {
							JSO it = new JSO()
								.add("id", id)
								.add("name", i.name)
								.add("image", i.isVisible(u) ? i.publicImage : i.privateImage)
								.add("deleted", i.deleted)
								.add("x", i.location.x)
								.add("y", i.location.y)
								.add("z", i.location.z)
								.add("w", i.location.w)
								.add("h", i.location.h)
								.add("color", i.publicMark ? (i.publicVisibility ? "#00FF00" : "#FF0000") : "transparent")
								.add("options", i.getOptions())
							;
							iarr.add(id,it);
						}
					}
				}
				u.lastItemUpdate = now;
				
				return res.toString();
			}
		});

		rs.put("URN", new Response() { // User name
			public String respond(long now,User u,String type,String message) {
				String filteredName = message.replaceAll("[^a-zA-Z0-9 ]","");
				if (filteredName.length() > 16) {
					filteredName = filteredName.substring(0,16);
				}
				
				u.name = filteredName;
				
				return new JSO()
					.add("time", now)
					.add("type","URN")
					.add("name",filteredName)
					.add("hash",u.hash)
				.toString();
			}
		});
		rs.put("URU", new Response() { // User update
			public String respond(long now,User u,String type,String message) {
				// New position
				int delim = message.indexOf(",");
				if (delim>=0) {
					try {
						float x = Float.parseFloat(message.substring(0,delim));
						float y = Float.parseFloat(message.substring(delim+1));
						if (x<0) x = 0; else if (x>640) x = 640;
						if (y<0) y = 0; else if (y>640) y = 640;
						
						u.location.x = x;
						u.location.y = y;
					}
					catch (Exception e) {
						
					}
				}
				
				// Users
				JSO res = new JSO()
					.add("time", now)
					.add("type", "URU")
				;
				JSO uarr = new JSO();
				res.add("data", uarr);
				for (InetSocketAddress i:users.keySet()) {
					User ur = users.get(i);
					if (u != ur) {
						JSO ut = new JSO()
							.add("name", ur.name)
							.add("hash", ur.hash)
							.add("x", ur.location.x)
							.add("y", ur.location.y)
						;
						uarr.add(ur.hash,ut);
					}
				}
				
				try {
					return res.toString();
				}
				catch (Exception e) {
					e.printStackTrace();
					return new JSO()
						.add("time", now)
						.add("type", "URU")
					.toString();
				}
			}
		});
		rs.put("URC", new Response() { // User command
			private String[] allowed = {
				"set item %s public %s",
				"set item %s owner %s",
				"set user %s name %s"
			};
			public String respond(long now,User u,String type,String message) {
				int command = Integer.parseInt(message.substring(0,3));
				if (command<allowed.length) {
					String[] parts = Utils.cmd_parse(message.substring(3));
					cmd(u,String.format(allowed[command],parts[0],parts[1]));
				}
				return new JSO()
					.add("time", now)
					.add("type", "URC")
				.toString();
			}
		});
		/*
		rs.put("",new Response() {
			public String respond(long now,User u,String type,String message) {
				
			}
		});
		 */
		return rs;
	}
	
	// Websocket events
	public void onOpen(WebSocket conn,ClientHandshake handshake) {
		// System.out.println("in  " +
		// conn.getRemoteSocketAddress().toString());
		
		User u = new User(server,conn.getRemoteSocketAddress());
		u.socket = conn;
		
		addUser(u);
		
		conn.send(new JSO()
			.add("type", "INI")
			.add("message", u.hash)
		.toString());
	}
	
	public void onClose(WebSocket conn,int code,String reason,boolean remote) {
		// System.out.println("out " +
		// conn.getRemoteSocketAddress().toString());
		
		removeUser(conn.getRemoteSocketAddress());
	}
	
	public void onMessage(WebSocket conn,String message) {
		if (message.length() < 3) return;
		if (!users.keySet().contains(conn.getRemoteSocketAddress())) return;
		
		User u = users.get(conn.getRemoteSocketAddress());
		u.socket = conn;
		
		String type = message.substring(0,3);
		message = message.substring(3);
		long now = System.currentTimeMillis();
		
		// System.out.println(type + " " +
		// conn.getRemoteSocketAddress().getAddress().toString());
		
		String response = null;
		if (responses.containsKey(type)) {
			response = responses.get(type).respond(now,u,type,message);
		}
		else {
			response = new JSO().add("type","ERR").add("message","No such request type").toString();
		}
		
		if (response!=null) {
			conn.send(response);
		}
	}
	
	public void onError(WebSocket conn,Exception ex) {
		if (conn!=null) System.err.println(conn.getRemoteSocketAddress().toString());
		ex.printStackTrace();
	}
}
