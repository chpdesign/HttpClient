package eu.nagygergely.httpclient;

import java.net.URLConnection;

import com.ning.http.util.Base64;

public class BasicAuth implements AuthInterface {
	protected String username;
	protected String password;

	public BasicAuth(String username, String password) {
		this.username = username;
		this.password = password;
	}
	
	@Override
	public void prepareAuthForConnection(Request request, HttpClient client, URLConnection conn) {
		String userpass = username + ":" + password;
		String basicAuth = "Basic " + new String(Base64.encode(userpass.getBytes()));
		conn.setRequestProperty("Authorization", basicAuth);	
	}
}
