package eu.nagygergely.httpclient;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLConnection;

public abstract class URLConnectionFactory {
	public abstract URLConnection getConnection(HttpClient client, Request request) throws MalformedURLException, IOException, Exception;
}
