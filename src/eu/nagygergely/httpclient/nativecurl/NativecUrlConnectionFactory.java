package eu.nagygergely.httpclient.nativecurl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URLConnection;

import sun.net.www.protocol.http.Handler;
import eu.nagygergely.httpclient.HttpClient;
import eu.nagygergely.httpclient.Request;
import eu.nagygergely.httpclient.URLConnectionFactory;

public class NativecUrlConnectionFactory extends URLConnectionFactory {

	@Override
	public URLConnection getConnection(HttpClient client, Request request) throws MalformedURLException, IOException, Exception {
		URLConnection conn = new UrlConnection(request.getUrl(), new Handler());
		return conn;
	}

}
