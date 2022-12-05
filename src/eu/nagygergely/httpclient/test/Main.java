package eu.nagygergely.httpclient.test;

import java.util.HashMap;

import eu.nagygergely.httpclient.Application;
import eu.nagygergely.httpclient.HttpClient;
import eu.nagygergely.httpclient.PostMap;
import eu.nagygergely.httpclient.Response;
import eu.nagygergely.httpclient.nativecurl.NativecUrlConnectionFactory;

public class Main {
	public static void main(String[] args) throws Exception {
		/*testGetHeaders();
		testGet();
		testGetParam();
		testPostHeaders();
		testPost();*/
		testCurlGet();
	}
	
	protected static void testGetHeaders() throws Exception {
		HttpClient client = new HttpClient();
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("getTestHeaderField", "getTestHeaderValue");
		HashMap<String, Object> get = new HashMap<String, Object>();
		get.put("getTestField", "getTestValue");
		Response response = client.Get("https://httpbin.org/get", headers, get);
		System.out.println(response.getBodyString());
	}
	
	protected static void testGet() throws Exception {
		HttpClient client = new HttpClient();
		Response response = client.Get("https://httpbin.org/get");
		System.out.println(response.getBodyString());
	}
	
	protected static void testGetParam() throws Exception {
		HttpClient client = new HttpClient();
		HashMap<String, Object> get = new HashMap<String, Object>();
		get.put("getTestField", "getTestValue");
		Response response = client.Get("https://httpbin.org/get", null, get);
		System.out.println(response.getBodyString());
	}
	
	protected static void testPostHeaders() throws Exception {
		HttpClient client = new HttpClient();
		HashMap<String, String> headers = new HashMap<String, String>();
		headers.put("postTestHeaderField", "postTestHeaderValue");
		PostMap form = new Application();
		form.put("postTestField", "postTestValue");
		Response response = client.Post("https://httpbin.org/post", form, headers);
		System.out.println(response.getBodyString());
	}
	
	protected static void testPost() throws Exception {
		HttpClient client = new HttpClient();
		PostMap form = new Application();
		form.put("postTestField", "postTestValue");
		Response response = client.Post("https://httpbin.org/post", form);
		System.out.println(response.getBodyString());
	}
	
	protected static void testCurlGet() throws Exception {
		HttpClient client = new HttpClient(new NativecUrlConnectionFactory());
		Response response = client.Get("https://httpbin.org/get");
		System.out.println(response.getBodyString());
	}
}
