package eu.nagygergely.httpclient;

import java.util.List;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class Response {
	int status;
	private String bodyString;
	private Document bodyDoc;
	private byte[] bbody;
	long time;
	private String url;
	private Map<String, List<String>> headers;
	
	public String getUrl()
	{
		return this.url;
	}

	public int getStatus() {
		return this.status;
	}

	public String getBodyString() {
		return this.bodyString;
	}

	public Document getBodyDoc() {
		if(this.bodyDoc == null)
		{
			this.bodyDoc = Jsoup.parse(this.bodyString);
		}
		return this.bodyDoc;
	}

	public byte[] getBody() {
		return this.bbody;
	}

	public long getTime() {
		return this.time;
	}

	public Map<String, List<String>> getHeaders() {
		return this.headers;
	}

	public Response(String url, int status, HttpClient.Res body, Map<String, List<String>> headers) {
		this.status = status;
		this.bodyString = body.html;
		this.bbody = body.bhtml;
		this.headers = headers;
		this.url = url;
	}
}
