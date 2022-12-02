package eu.nagygergely.httpclient;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;


public class Request {
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
}
