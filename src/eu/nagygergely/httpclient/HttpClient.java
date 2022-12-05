package eu.nagygergely.httpclient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import javax.net.ssl.HttpsURLConnection;
import org.apache.commons.io.IOUtils;

public class HttpClient {
	
	public static boolean DEBUG = false;
	
	protected String url_prefix;
	
	protected URLConnectionFactory connectionFactory = new DefaultUrlConnectionFactory();

	public HttpClient() {
		this("");
	}
	
	public HttpClient(String url) {
		this.url_prefix = url;
	}

	public HttpClient(URLConnectionFactory connectionFactory) {
		this("", connectionFactory);
	}
	
	public HttpClient(String url, URLConnectionFactory connectionFactory) {
		this(url);
		this.connectionFactory = connectionFactory;
	}

	protected ArrayList<HttpResult> completedes = new ArrayList<HttpResult>();

	public void addCompleteEvent(HttpResult ddl) {
		if (ddl == null) {
			return;
		}
		if (!this.completedes.contains(ddl)) {
			this.completedes.add(ddl);
		}
	}

	public void removeCompleteEvent(HttpResult ddl) {
		if (ddl == null) {
			return;
		}
		if (this.completedes.contains(ddl)) {
			this.completedes.remove(ddl);
		}
	}

	protected HashMap<String, String> headers = new HashMap<String, String>();

	public void setUserAgent(String useragent) {
		this.headers.put("User-Agent", useragent);
	}

	public void setReferer(String referer) {
		if ((!referer.startsWith("https://")) && (!referer.startsWith("http://"))) {
			this.headers.put("Referer", this.url_prefix + referer);
		} else {
			this.headers.put("Referer", referer);
		}
	}

	public void setHeader(String key, String value) {
		this.headers.put(key, value);
	}
	
	public HashMap<String, String> getHeaders() {
		return headers;
	}

	private Response lastResponse = null;

	public Response getResponse() {
		return this.lastResponse;
	}

	public void Get(String url, HashMap<String, String> headers, HashMap<String, Object> params, HttpResult result) throws Exception {
		Get(url, headers, params, true, result);
	}
	
	public Response Get(String url) throws Exception {
		return Get(url, null, null);
	}
	
	public Response Get() throws Exception {
		return Get("", null, null);
	}

	public Response Get(String url, HashMap<String, String> headers) throws Exception {
		final ArrayList<Response> r = new ArrayList<Response>();
		Get(url, headers, null, false, new HttpResult() {
			
			public void Completed(Response paramResponse) {
				r.add(paramResponse);
			}
		});
		return r.get(0);
	}
	
	public Response Get(String url, HashMap<String, String> headers, HashMap<String, Object> params) throws Exception {
		final ArrayList<Response> r = new ArrayList<Response>();
		Get(url, headers, params, false, new HttpResult() {

			public void Completed(Response paramResponse) {
				r.add(paramResponse);
			}
		});
		return r.get(0);
	}

	public void Get(final String url, final HashMap<String, String> headers, final HashMap<String, Object> params,
			boolean async, final HttpResult result) throws Exception {
		final ArrayList<HttpResult> completedes = this.completedes;
		if (async) {
			Thread a = new Thread(new Runnable() {
				public void run() {
					try {
						Response ret = HttpClient.this.doGetRequest(
								new URL(HttpClient.this.url_prefix + url), headers, params);
						HttpClient.this.lastResponse = ret;
						if (result != null) {
							result.Completed(ret);
						}
						for (HttpResult com : completedes) {
							com.Completed(ret);
						}
					} catch (Exception exception) {
						exception.printStackTrace();
					}
				}
			});
			a.start();
		} else {
			Response ret = doGetRequest(new URL(this.url_prefix + url), headers, params);
			this.lastResponse = ret;
			if (result != null) {
				result.Completed(ret);
			}
			for (HttpResult com : completedes) {
				com.Completed(ret);
			}
		}
	}

	public void Post(String url, HashMap<String, String> headers, PostMap params, HttpResult result) throws Exception {
		Post(url, headers, params, true, result);
	}
	
	public Response Post(String url) throws Exception {
		return Post(url, new HashMap<String, String>(), null);
	}
	
	public Response Post() throws Exception {
		return Post("", new HashMap<String, String>(), null);
	}

	public Response Post(String url, HashMap<String, String> headers, PostMap params) throws Exception {
		final ArrayList<Response> r = new ArrayList<Response>();
		Post(url, headers, params, false, new HttpResult() {

			public void Completed(Response paramResponse) {
				r.add(paramResponse);
			}
		});
		return r.get(0);
	}
	
	public Response Post(String url, PostMap params, HashMap<String, String> headers) throws Exception {
		return Post(url, headers, params);
	}
	
	public Response Post(String url, PostMap params) throws Exception {
		return Post(url, null, params);
	}

	public void Post(final String url, final HashMap<String, String> headers, final PostMap params,
			boolean async, final HttpResult result) throws Exception {
		final ArrayList<HttpResult> completedes = this.completedes;
		if (async) {
			Thread a = new Thread(new Runnable() {
				public void run() {
					try {
						Response ret = HttpClient.this.doPostRequest(new URL(HttpClient.this.url_prefix
								+ url), headers, params);
						HttpClient.this.lastResponse = ret;
						if (result != null) {
							result.Completed(ret);
						}
						for (HttpResult com : completedes) {
							com.Completed(ret);
						}
					} catch (Exception exception) {
						exception.printStackTrace();
					}
				}
			});
			a.start();
		} else {
			Response ret = doPostRequest(new URL(this.url_prefix + url), headers, params);
			this.lastResponse = ret;
			if (result != null) {
				result.Completed(ret);
			}
			for (HttpResult com : completedes) {
				com.Completed(ret);
			}
		}
	}

	protected void createParamString(URLConnection connection, PostMap params) throws IOException {
		params.appendBody(connection);
	}

	protected CookieManager cm = new CookieManager();

	public CookieManager getCookie() {
		return this.cm;
	}

	public void setCookie(CookieManager cm) {
		this.cm = cm;
	}

	protected AuthInterface auth;

	public void setAuthenticator() {
		this.auth = null;
	}

	public void setAuthenticator(AuthInterface auth) {
		this.auth = auth;
	}
	
	public AuthInterface getAuthenticator() {
		return auth;
	}

	protected boolean autoReferer = false;
	protected boolean followRedirect = true;
	
	public void setAutoReferer(boolean referer)
	{
		autoReferer = referer;
	}
	
	public boolean getAutoReferer()
	{
		return autoReferer;
	}
	
	public void followRedirect(boolean redirect)
	{
		followRedirect = redirect;
	}
	
	protected String lastUrl = "http://google.com/";
	
	public void setLastUrl(String lastUrl) {
		this.lastUrl = lastUrl;
	}
	
	public String getLastUrl() {
		return lastUrl;
	}
	
	public static final String QUERY_CHAR = "?";
	public static final String ANCHOR_CHAR = "#";

	protected Response doGetRequest(URL url, HashMap<String, String> headers, HashMap<String, Object> params) throws Exception {
		GET get = new GET(url, headers, params);
		return sendRequest(get);
	}

	protected Response doPostRequest(URL url, HashMap<String, String> headers, PostMap body) throws Exception {
		POST post = new POST(url, headers, body);
		return sendRequest(post);
	}
	
	Proxy proxy = null;
	String proxy_host;
	int proxy_port;
	
	public int getProxyPort()
	{
		return proxy_port;
	}
	
	public String getProxyAddress()
	{
		return proxy_host;
	}
	
	public void setProxy(Proxy.Type type,String host,int port)
	{
		proxy_host = host;
		proxy_port = port;
		proxy = new Proxy(type, new InetSocketAddress(host, port));
	}
	
	public void setProxy(Proxy p)
	{
		proxy = p;
	}
	
	public void setProxy()
	{
		proxy_host = null;
		proxy_port = 0;
		proxy = null;
	}
	
	public Proxy getProxy()
	{
		return proxy;
	}
	
	protected SSLConnectionType connectionType = new TLSConnection();
	
	public void setSSLConnectionType(SSLConnectionType type)
	{
		connectionType = type;
	}
	
	public SSLConnectionType getSSLConnectionType()
	{
		return connectionType;
	}
	
	private int timeout = 5000;
	
	public void setTimeOut(int timeout)
	{
		this.timeout = timeout;
	}
	
	public int getTimeOut()
	{
		return this.timeout;
	}
	
	public void StreamGet(final String url, final HashMap<String, String> headers, final HashMap<String, Object> params, OutputStream out, InputStream in) throws Exception
	{
		GET get = new GET(new URL(HttpClient.this.url_prefix + url), headers, params);
		StreamRequest(get, out, in);
	}
	
	public void StreamPost(final String url, final HashMap<String, String> headers, final PostMap params, OutputStream out, InputStream in) throws Exception
	{
		POST post = new POST(new URL(HttpClient.this.url_prefix + url), headers, params);
		StreamRequest(post, out, in);
	}
	
	private void StreamRequest(Request request, OutputStream out, InputStream in)
	{
		try {
			URLConnection conn = createConnection(request);
			conn.setDoOutput(true);
			out = conn.getOutputStream();
			in = conn.getInputStream();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/*private Socket removeSSLv2v3(Socket socket) {

	    if (!(socket instanceof SSLSocket)) {
	        return socket;
	    }

	    SSLSocket sslSocket = (SSLSocket) socket;

	    String[] protocols = sslSocket.getEnabledProtocols();
	    Set<String> set = new HashSet<String>();
	    for (String s : protocols) {
	        if (s.equals("SSLv3") || s.equals("SSLv2Hello")) {
	            continue;
	        }
	        set.add(s);
	    }
	    sslSocket.setEnabledProtocols(set.toArray(new String[0]));

	    return sslSocket;
	}*/
	
	private URLConnection createConnection(Request request) throws Exception
	{
		URLConnection connection = this.connectionFactory.getConnection(this, request);
		this.cm.setCookies(connection);
		if (request.headers != null) {
			for (String header : request.headers.keySet()) {
				connection.setRequestProperty(header, (String) request.headers.get(header));
			}
		}
		if (this.headers != null) {
			for (String header : this.headers.keySet()) {
				connection.setRequestProperty(header, (String) this.headers.get(header));
			}
		}
		if (this.autoReferer) {
			connection.setRequestProperty("Referer", this.lastUrl);
			this.lastUrl = request.getUrl().toString();
		}
		if (this.auth != null) {
			this.auth.prepareAuthForConnection(request, this, connection);
		}
		request.prepareConnection(connection);
		return connection;
	}

	protected Response sendRequest(Request request) throws Exception {
		URLConnection connection = null;
		Response response = null;
		long time = 0L;
		try{
			connection = createConnection(request);
			int status = 0;
			try{
				status = ((HttpURLConnection) connection).getResponseCode();
			}
			catch (Exception e)
			{
				throw e;
			}
			/*if (status != 200) {
				Res r = new Res();
				r.html = ((HttpURLConnection) connection).getResponseMessage();
				r.bhtml = r.html.getBytes("UTF-8");
				response = new Response(request.getUrl().toString(), status, readInputStream(connection), connection.getHeaderFields());
			} else {
				response = new Response(request.getUrl().toString(), status, readInputStream(connection), connection.getHeaderFields());
			}*/
			response = new Response(request.getUrl().toString(), status, readInputStream(connection), connection.getHeaderFields());
			response.time = (System.currentTimeMillis() - time);
			if (DEBUG) {
				dumpRequest(request, response);
			}
			this.cm.storeCookies(connection);
			if (status != 200) {
				if (followRedirect && (connection.getHeaderField("Location") != null) && (!connection.getHeaderField("Location").isEmpty())) {
					String newUrl = connection.getHeaderField("Location");
					if (isValidURL(newUrl)) {
						request = new GET(new URL(newUrl), request.headers, null);
					} else {
						String path = request.getUrl().getPath();
						if (path.length() - request.getUrl().getFile().length() >= 0) {
							path = path.substring(0, path.length() - request.getUrl().getFile().length());
						} else {
							path = "";
						}
						if (newUrl.charAt(0) == '/') {
							newUrl = newUrl.substring(1, newUrl.length());
						}
						URL newURL = new URL(request.getUrl().getProtocol() + "://" + request.getUrl().getHost()
								+ (request.getUrl().getPort() != -1 ? ":" + request.getUrl().getPort() : "") + path
								+ "/" + newUrl);
						request = new GET(newURL, request.headers, null);
					}
					response = sendRequest(request);
				}
			}
		} catch (Exception e) {
			//e.printStackTrace(System.err);
			throw e;
		} finally {
			if (connection != null) {
				((HttpURLConnection) connection).disconnect();
			}
		}
		return response;
	}

	private static Res readInputStream(URLConnection conn) throws IOException {
		InputStream is;
		if (200 <= ((HttpURLConnection) conn).getResponseCode() && ((HttpURLConnection) conn).getResponseCode() <= 299) {
		    is = conn.getInputStream();
		} else {
		    is = (conn instanceof HttpsURLConnection ? ((HttpsURLConnection) conn) : ((HttpURLConnection) conn)).getErrorStream();
		    if(is == null)
		    {
		    	is = conn.getInputStream();		    	
		    }
		}
		// http://archive.oreilly.com/pub/post/optimizing_http_downloads_in_j.html
		// http://www.rgagnon.com/javadetails/java-HttpUrlConnection-with-GZIP-encoding.html
		// https://stackoverflow.com/questions/3932117/handling-http-contentencoding-deflate
		// http://thushw.blogspot.hu/2014/05/decoding-html-pages-with-content.html
		if(is != null)
		{
			String encoding = conn.getContentEncoding();
			if (encoding != null && encoding.equalsIgnoreCase("gzip"))
			{
				is = new GZIPInputStream(is);
			}
			else if (encoding != null && encoding.equalsIgnoreCase("deflate"))
			{
				is = new InflaterInputStream(is, new Inflater(true));
			}
			//if ("gzip".equals(conn.getContentEncoding())) {
			//	is = new GZIPInputStream(conn.getInputStream()));
			//}
		}
		//InputStream is = conn.getInputStream();
		Res r = new Res();

		String encoding = "UTF-8";
		if (conn.getContentEncoding() != null) {
			encoding = conn.getContentEncoding();
		}
		if (conn.getContentType() != null && getCharsetFromContentType(conn.getContentType()) != null) {
			encoding = getCharsetFromContentType(conn.getContentType());
		}

		r.bhtml = IOUtils.toByteArray(is);
		/*r.html = new String(r.bhtml, encoding);
		
		Element meta = Jsoup.parse(r.html).select("meta[charset], meta[http-equiv=Content-Type][content]").last();
		if(meta != null)
		if(meta.hasAttr("charset"))
		{
			encoding = meta.attr("charset").toUpperCase();
		}
		else if(meta.hasAttr("content") && getCharsetFromContentType(meta.attr("content")) != null)
		{
			encoding = getCharsetFromContentType(meta.attr("content"));
		}*/
		
		if(DEBUG)
		{
			System.out.println(conn.getURL());
			System.out.println("encoding: "+encoding);
		}
		
		r.html = new String(r.bhtml, encoding);
		
		return r;
	}

	private static final Pattern charsetPattern = Pattern.compile("(?i)\\bcharset=\\s*\"?([^\\s;\"]*)");

	static String getCharsetFromContentType(String contentType) {
		if (contentType == null)
			return null;
		
		String r = "";

		Matcher m = charsetPattern.matcher(contentType);
		if (m.find()) {
			r = m.group(1).trim().toUpperCase();
		}
		if(r != null && !r.isEmpty())
			return r;
		return null;
	}

	protected static void dumpRequest(Request req, Response resp) throws MalformedURLException {
		StringBuilder sb = new StringBuilder();
		sb.append("=> Dumping request information:");
		sb.append("\n").append("======================= REQUEST ==========================");
		sb.append("\n==> ").append("URL: ").append(req.getUrl());
		sb.append("\n==> ").append("Method: ").append((req instanceof POST) ? "POST" : "GET");
		if (req.headers != null) {
			for (String header : req.headers.keySet()) {
				sb.append("\n===> ").append("Header: ").append(header).append(": ")
						.append((String) req.headers.get(header));
			}
		}
		if (((req instanceof GET)) && (((GET) req).params != null)) {
			for (String param : ((GET) req).params.keySet()) {
				sb.append("\n===> ").append("Param: ").append(param).append("=").append(((GET) req).params.get(param));
			}
		}
		if ((req instanceof POST)) {
			sb.append("\n==> ").append("Request body: ").append(((POST) req).body);
		}
		sb.append("\n").append("======================= RESPONSE =========================");

		sb.append("\n==> ").append("Round trip time: ").append(resp.time).append(" ms");
		sb.append("\n==> ").append("Response status: ").append(resp.status);
		sb.append("\n==> ").append("Response body:\n").append(resp.getBodyString());

		sb.append("\n==========================================================");

		System.out.println(sb.toString());
	}

	private class POST extends Request {
		public PostMap body;

		public POST(URL baseUrl, HashMap<String, String> headers, PostMap body) {
			super(baseUrl, "POST", headers);
			this.body = body;
		}
		
		@Override
		public void prepareConnection(URLConnection connection) throws IOException {
			this.body.appendBody(connection);
		}
	}

	private class GET extends Request {
		public HashMap<String, Object> params;

		public GET(URL baseUrl, HashMap<String, String> headers, HashMap<String, Object> params) {
			super(baseUrl, "GET", headers);
			this.params = params;
		}

		public URL getUrl() throws MalformedURLException {
			StringBuilder sb = new StringBuilder(this.baseUrl.toString());
			if ((this.params != null) && (this.params.size() > 0)) {
				sb.append(createParamString());
			}
			return new URL(sb.toString());
		}

		private String createParamString() {
			StringBuilder sb = new StringBuilder();
			if ((this.params != null) && (this.params.size() > 0)) {
				sb.append("?");
				for (String param : this.params.keySet()) {
					sb.append(param).append("=").append(this.params.get(param)).append("&");
				}
				sb.deleteCharAt(sb.length() - 1);
			}
			return sb.toString();
		}
		
		@Override
		public void prepareConnection(URLConnection connection)
				throws IOException {
			// nothing to do	
		}
	}

	public static boolean isValidURL(String url) {
		URL u = null;
		try {
			u = new URL(url);
		} catch (MalformedURLException e) {
			return false;
		}
		try {
			u.toURI();
		} catch (URISyntaxException e) {
			return false;
		}
		return true;
	}

	public static String extractBaseUrl(String url) {
		if (url != null) {
			int queryPosition = url.indexOf("?");
			if (queryPosition <= 0) {
				queryPosition = url.indexOf("#");
			}
			if (queryPosition >= 0) {
				url = url.substring(0, queryPosition);
			}
		}
		return url;
	}

	public static void copyStream(InputStream input, OutputStream output) throws IOException {
		byte[] buffer = new byte[1024];
		int bytesRead;
		while ((bytesRead = input.read(buffer)) != -1) {
			output.write(buffer, 0, bytesRead);
		}
	}

	protected static class Res {
		String html;
		byte[] bhtml;
	}

	public String getPrefix() {
		return url_prefix;
	}
	
	public void setPrefix(String prefix) {
		url_prefix = prefix;
	}
}
