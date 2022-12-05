package eu.nagygergely.httpclient;

import java.net.URLConnection;

public interface AuthInterface {
	public void prepareAuthForConnection(Request request, HttpClient client, URLConnection connection) throws Exception;
}
