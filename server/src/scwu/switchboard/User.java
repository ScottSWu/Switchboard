package scwu.switchboard;
import java.net.InetSocketAddress;
import org.java_websocket.WebSocket;


public class User {
	public SwitchboardServer parent;
	
	public WebSocket socket;
	public InetSocketAddress connection;
	
	public String name;
	public String hash;
	public String color;
	public Dim location;
	public boolean hasroot;
	
	public long lastChatUpdate;
	public long lastItemUpdate;
	public long lockout;
	
	public User(SwitchboardServer server,InetSocketAddress connection) {
		this.parent = server;
		this.connection = connection;
		if (connection==null) {
			hash = "root";
			color = "#000000";
		}
		else {
			byte[] ip = connection.getAddress().getAddress();
			int port = connection.getPort();
			hash = Utils.Base64(new byte[] {
				ip[1],
				ip[2],
				ip[0],
				ip[3],
				(byte) (port/256),
				(byte) (port%256),
			});
			color = String.format("#%02X%02X%02X", (ip[0]+48)%256, (ip[1]+48)%256, (ip[2]+48)%256);
		}
		name = hash;
		lastChatUpdate = 0;
		lastItemUpdate = 0;
		location = new Dim();
		hasroot = false;
		lockout = -5;
	}
	
	public void setProperty(String property,Object arg) {
		if (property.equalsIgnoreCase("name")) {
			name = arg.toString();
		}
	}
}
