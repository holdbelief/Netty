package client.mode;

import org.msgpack.annotation.Message;

@Message
public class UserInfo {
	private String username;
	private String age;

	public String getUsername() {
		return username;
	}

	public String getAge() {
		return age;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setAge(String age) {
		this.age = age;
	}

	public UserInfo(String username, String age) {
		super();
		this.username = username;
		this.age = age;
	}

	public UserInfo() {

	}
}
