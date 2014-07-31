package scwu.switchboard;

public class Chat {
	User user;
	long time;
	String message;
	
	public Chat(User u,long t,String c) {
		user = u;
		time = t;
		message = c;
	}
}
