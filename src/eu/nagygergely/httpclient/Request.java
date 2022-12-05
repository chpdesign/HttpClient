package eu.nagygergely.httpclient;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;


public abstract class Request {
	public URL baseUrl;
	public HashMap<String, String> headers;
	public String method = "GET";

	public Request(URL baseUrl, String method, HashMap<String, String> headers) {
		this.baseUrl = baseUrl;
		this.headers = headers;
		this.method = method;
	}

	public URL getUrl() throws MalformedURLException {
		return this.baseUrl;
	}
	
	public HashMap<String, String> getHeaders() {
		return headers;
	}
	
	public String getMethod() {
		return method;
	}
	
	public abstract void prepareConnection(URLConnection connection) throws IOException;
}
