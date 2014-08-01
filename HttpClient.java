package com.httpclient;

import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;
import com.ning.http.util.Base64;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class HttpClient {
	
	final static boolean DEBUG = false;
	
	final String url_prefix;

	public HttpClient(String url) {
		if (!url.endsWith("/")) {
			url = url + "/";
		}
		this.url_prefix = url;
	}

	public HttpClient() {
		this.url_prefix = "";
	}

	protected ArrayList<HttpResult> completedes = new ArrayList();

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

	public HashMap<String, String> headers = new HashMap();

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

	private Response lastResponse = null;

	public Response getResponse() {
		return this.lastResponse;
	}

	public void Get(String url, HashMap<String, String> headers, HashMap<String, Object> params, HttpResult result) {
		Get(url, headers, params, true, result);
	}

	public Response Get(String url, HashMap<String, String> headers, HashMap<String, Object> params) {
		final ArrayList<Response> r = new ArrayList<>();
		Get(url, headers, params, false, new HttpResult() {

			@Override
			public void Completed(Response paramResponse) {
				r.add(paramResponse);
			}
		});
		return r.get(0);
	}

	public void Get(final String url, final HashMap<String, String> headers, final HashMap<String, Object> params,
			boolean async, final HttpResult result) {
		final ArrayList<HttpResult> completedes = this.completedes;
		if (async) {
			Thread a = new Thread(new Runnable() {
				public void run() {
					try {
						HttpClient.Response ret = HttpClient.this.doGetRequest(
								new URL(HttpClient.this.url_prefix + url), headers, params, false);
						HttpClient.this.lastResponse = ret;
						if (result != null) {
							result.Completed(ret);
						}
						for (HttpClient.HttpResult com : completedes) {
							com.Completed(ret);
						}
					} catch (Exception exception) {
						exception.printStackTrace();
					}
				}
			});
			a.start();
		} else {
			try {
				Response ret = doGetRequest(new URL(this.url_prefix + url), headers, params, false);
				this.lastResponse = ret;
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
	}

	public void Post(String url, HashMap<String, String> headers, HashMap<String, Object> params, HttpResult result) {
		Post(url, headers, params, true, result);
	}

	public Response Post(String url, HashMap<String, String> headers, HashMap<String, Object> params) {
		final ArrayList<Response> r = new ArrayList<>();
		Post(url, headers, params, false, new HttpResult() {

			@Override
			public void Completed(Response paramResponse) {
				r.add(paramResponse);
			}
		});
		return r.get(0);
	}

	public void Post(final String url, final HashMap<String, String> headers, final HashMap<String, Object> params,
			boolean async, final HttpResult result) {
		final ArrayList<HttpResult> completedes = this.completedes;
		if (async) {
			Thread a = new Thread(new Runnable() {
				public void run() {
					try {
						HttpClient.Response ret = HttpClient.this.doPostRequest(new URL(HttpClient.this.url_prefix
								+ url), headers, params, false);
						HttpClient.this.lastResponse = ret;
						if (result != null) {
							result.Completed(ret);
						}
						for (HttpClient.HttpResult com : completedes) {
							com.Completed(ret);
						}
					} catch (Exception exception) {
						exception.printStackTrace();
					}
				}
			});
			a.start();
		} else {
			try {
				Response ret = doPostRequest(new URL(this.url_prefix + url), headers, params, false);
				this.lastResponse = ret;
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
	}

	String boundary = Long.toHexString(System.currentTimeMillis());

	protected void createParamString(URLConnection connection, HashMap<String, Object> params) throws IOException {
		if ((params != null) && (params.size() > 0)) {
			connection.setDoOutput(true);
			connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + this.boundary);
			OutputStream output = connection.getOutputStream();
			for (String param : params.keySet()) {
				// System.out.println(params.get(param).getClass());
				if (params.get(param) != null) {
					if ((params.get(param) instanceof File)) {
						File file = (File) params.get(param);
						InputStream f = new FileInputStream(file);
						output.write(("--" + this.boundary).getBytes());
						output.write(13);
						output.write(10);
						output.write(("Content-Disposition: form-data; name=\"" + param + "\"; filename=\""
								+ file.getName() + "\"").getBytes());
						output.write(13);
						output.write(10);
						output.write("Content-Type: multipart/mixed; charset=UTF-8".getBytes());
						output.write(13);
						output.write(10);
						output.write(13);
						output.write(10);
						ByteStreams.copy(f, output);
						output.write(13);
						output.write(10);
						f.close();
					} else {
						output.write(("--" + this.boundary).getBytes());
						output.write(13);
						output.write(10);
						output.write(("Content-Disposition: form-data; name=\"" + param + "\"").getBytes());
						output.write(13);
						output.write(10);
						output.write("Content-Type: multipart/mixed; charset=UTF-8".getBytes());
						output.write(13);
						output.write(10);
						output.write(13);
						output.write(10);
						output.write(params.get(param).toString().getBytes());
						output.write(13);
						output.write(10);
					}
				} else
					System.err.println("Param Error: " + param);
			}
			output.write(("--" + this.boundary + "--").getBytes());
		}
		int responseCode = ((HttpURLConnection) connection).getResponseCode();
		if(DEBUG)
			System.out.println(responseCode);
	}

	protected CookieManager cm = new CookieManager();

	public Object getCookie() {
		return this.cm;
	}

	public void setCookie(CookieManager cm) {
		this.cm = cm;
	}

	private String AuthenticatorUser = "";
	private String AuthenticatorPass = "";
	private HashMap<String, String> authFields = null;
	private int AuthenticatorType = -1;
	public static final int AUTHENTICATOR_TYPE_NONE = -1;
	public static final int AUTHENTICATOR_TYPE_BASIC = 1;
	public static final int AUTHENTICATOR_TYPE_DIGEST = 2;

	public void setAuthenticator() {
		this.AuthenticatorUser = "";
		this.AuthenticatorPass = "";
		this.AuthenticatorType = -1;
		HashMap<String, String> authFields = null;
	}

	public void setDigestAuthenticator(String username, String password) {
		this.AuthenticatorUser = username;
		this.AuthenticatorPass = password;
		this.AuthenticatorType = 2;
		HashMap<String, String> authFields = null;
	}

	public void setBasicAuthenticator(String username, String password) {
		this.AuthenticatorUser = username;
		this.AuthenticatorPass = password;
		this.AuthenticatorType = 1;
		HashMap<String, String> authFields = null;
	}

	public boolean autoReferer = false;
	private String lastUrl = "http://google.com/";
	public static final String QUERY_CHAR = "?";
	public static final String ANCHOR_CHAR = "#";
	private static final String HEX_LOOKUP = "0123456789abcdef";

	protected Response doGetRequest(URL url, HashMap<String, String> headers, HashMap<String, Object> params,
			boolean debug) {
		GET get = new GET(url, headers, params);
		return sendRequest(get, debug);
	}

	protected Response doPostRequest(URL url, HashMap<String, String> headers, HashMap<String, Object> body,
			boolean debug) {
		POST post = new POST(url, headers, body);
		return sendRequest(post, debug);
	}

	private Response sendRequest(Request request, boolean debug) {
		URLConnection conn = null;
		Response response = null;
		long time = 0L;
		try {
			SSLContext sslContext;
			if (request.getUrl().getProtocol().startsWith("https")) {
				conn = (HttpsURLConnection) request.getUrl().openConnection();

				TrustManager[] trustAllCerts = { new X509TrustManager() {
					public void checkClientTrusted(X509Certificate[] chain, String authType) {
					}

					public void checkServerTrusted(X509Certificate[] chain, String authType) {
					}

					public X509Certificate[] getAcceptedIssuers() {
						return null;
					}
				} };
				sslContext = SSLContext.getInstance("SSL");
				sslContext.init(null, trustAllCerts, new SecureRandom());

				SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

				((HttpsURLConnection) conn).setSSLSocketFactory(sslSocketFactory);
				((HttpsURLConnection) conn).setInstanceFollowRedirects(false);
				HttpsURLConnection.setFollowRedirects(false);
			} else {
				conn = request.getUrl().openConnection();
				((HttpURLConnection) conn).setInstanceFollowRedirects(false);
				HttpURLConnection.setFollowRedirects(false);
			}
			this.cm.setCookies(conn);
			if (request.headers != null) {
				for (String header : request.headers.keySet()) {
					conn.setRequestProperty(header, (String) request.headers.get(header));
				}
			}
			if (this.headers != null) {
				for (String header : this.headers.keySet()) {
					conn.setRequestProperty(header, (String) this.headers.get(header));
				}
			}
			if (this.autoReferer) {
				conn.setRequestProperty("Referer", this.lastUrl);
				this.lastUrl = request.getUrl().toString();
			}
			if (this.AuthenticatorType != -1) {
				int tAuth = this.AuthenticatorType;
				this.AuthenticatorType = -1;
				response = sendRequest(request, debug);
				this.AuthenticatorType = tAuth;

				String username = this.AuthenticatorUser;
				String password = this.AuthenticatorPass;
				String auth = null;
				try {
					auth = (String) ((List) response.getHeaders().get("WWW-Authenticate")).get(0);
				} catch (NullPointerException localNullPointerException) {
				}
				if (auth == null) {
					auth = "";
				}
				boolean AuthStart = false;
				if (this.AuthenticatorType == 1) {
					String userpass = username + ":" + password;
					String basicAuth = "Basic " + new String(Base64.encode(userpass.getBytes()));
					conn.setRequestProperty("Authorization", basicAuth);
				} else if (this.AuthenticatorType == 2) {
					this.authFields = splitAuthFields(auth.substring(7));
					AuthStart = true;

					MessageDigest md5 = null;
					try {
						md5 = MessageDigest.getInstance("MD5");
					} catch (NoSuchAlgorithmException e) {
						e.printStackTrace();
					}
					Joiner colonJoiner = Joiner.on(':');

					String HA1 = null;
					try {
						md5.reset();
						String ha1str = colonJoiner.join(username, this.authFields.get("realm"),
								new Object[] { password });
						md5.update(ha1str.getBytes("ISO-8859-1"));
						byte[] ha1bytes = md5.digest();
						HA1 = bytesToHexString(ha1bytes);
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					String method = "GET";
					if ((request instanceof POST)) {
						method = "POST";
					}
					String HA2 = null;
					try {
						md5.reset();
						String ha2str = colonJoiner.join(method, conn.getURL().getPath(), new Object[0]);
						md5.update(ha2str.getBytes("ISO-8859-1"));
						HA2 = bytesToHexString(md5.digest());
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					String HA3 = null;
					try {
						md5.reset();
						String ha3str = colonJoiner.join(HA1, this.authFields.get("nonce"), new Object[] { HA2 });
						md5.update(ha3str.getBytes("ISO-8859-1"));
						HA3 = bytesToHexString(md5.digest());
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
					StringBuilder sb = new StringBuilder(128);
					sb.append("Digest ");
					sb.append("username").append("=\"").append(username).append("\",");
					sb.append("realm").append("=\"").append((String) this.authFields.get("realm")).append("\",");
					sb.append("nonce").append("=\"").append((String) this.authFields.get("nonce")).append("\",");
					sb.append("uri").append("=\"").append(conn.getURL().getPath()).append("\",");
					if (!AuthStart) {
						sb.append("qop").append("=").append("auth").append(",");
						sb.append("nc=").append((String) this.authFields.get("nc")).append(",");
						sb.append("cnonce=\"").append((String) this.authFields.get("cnonce")).append("\",");
					}
					sb.append("response").append("=\"").append(HA3).append("\"");
					conn.setRequestProperty("Authorization", sb.toString());
				}
			}
			time = System.currentTimeMillis();
			if ((request instanceof POST)) {
				createParamString(conn, ((POST) request).body);
			}
			int status = ((HttpURLConnection) conn).getResponseCode();
			if (status != 200) {
				Res r = new Res();
				r.html = ((HttpURLConnection) conn).getResponseMessage();
				r.bhtml = r.html.getBytes("UTF-8");
				response = new Response(status, r, conn.getHeaderFields());
			} else {
				response = new Response(status, readInputStream(conn), conn.getHeaderFields());
			}
			response.time = (System.currentTimeMillis() - time);
			if (debug) {
				dumpRequest(request, response);
			}
			this.cm.storeCookies(conn);
			if (status != 200) {
				if ((conn.getHeaderField("Location") != null) && (!conn.getHeaderField("Location").isEmpty())) {
					String newUrl = conn.getHeaderField("Location");
					if (isValidURL(newUrl)) {
						request = new Request(new URL(newUrl), request.headers);
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
						request = new Request(newURL, request.headers);
					}
					response = sendRequest(request, debug);
				}
			}
		} catch (Exception e) {
			e.printStackTrace(System.err);
		} finally {
			if (conn != null) {
				((HttpURLConnection) conn).disconnect();
			}
		}
		return response;
	}

	private static Res readInputStream(URLConnection conn) throws IOException {
		InputStream is = conn.getInputStream();
		Res r = new Res();

		String encoding = "UTF-8";
		if (conn.getContentEncoding() != null) {
			encoding = conn.getContentEncoding();
		}
		if (conn.getContentType() != null && getCharsetFromContentType(conn.getContentType()) != null) {
			encoding = getCharsetFromContentType(conn.getContentType());
		}

		r.bhtml = IOUtils.toByteArray(is);
		r.html = new String(r.bhtml, encoding);
		
		Element meta = Jsoup.parse(r.html).select("meta[charset], meta[http-equiv=Content-Type][content]").last();
		if(meta != null)
		if(meta.hasAttr("charset"))
		{
			encoding = meta.attr("charset").toUpperCase();
		}
		else if(meta.hasAttr("content") && getCharsetFromContentType(meta.attr("content")) != null)
		{
			encoding = getCharsetFromContentType(meta.attr("content"));
		}
		
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
		return null;
	}

	protected void dumpRequest(Request req, Response resp) throws MalformedURLException {
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
		sb.append("\n==> ").append("Response body:\n").append(resp.bodyString);

		sb.append("\n==========================================================");

		System.out.println(sb.toString());
	}

	public class Request {
		public URL baseUrl;
		public HashMap<String, String> headers;

		public Request(URL baseUrl, HashMap<String, String> headers) {
			this.baseUrl = baseUrl;
			this.headers = headers;
		}

		public URL getUrl() throws MalformedURLException {
			return this.baseUrl;
		}
	}

	private class POST extends HttpClient.Request {
		public HashMap<String, Object> body;

		public POST(URL baseUrl, HashMap<String, String> headers, HashMap<String, Object> body) {
			super(baseUrl, headers);
			this.body = body;
		}
	}

	private class GET extends HttpClient.Request {
		public HashMap<String, Object> params;

		public GET(URL baseUrl, HashMap<String, String> headers, HashMap<String, Object> params) {
			super(baseUrl, headers);
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
	}

	public class Response {
		private int status;
		private String bodyString;
		private Document bodyDoc;
		private byte[] bbody;
		private long time;
		private Map<String, List<String>> headers;

		public int getStatus() {
			return this.status;
		}

		public String getBodyString() {
			return this.bodyString;
		}

		public Document getBodyDoc() {
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

		public Response(int status, HttpClient.Res body, Map<String, List<String>> headers) {
			this.status = status;
			this.bodyString = body.html;
			this.bbody = body.bhtml;
			this.bodyDoc = Jsoup.parse(this.bodyString);
			this.headers = headers;
		}
	}

	public boolean isValidURL(String url) {
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

	private static String digest2HexString(byte[] digest) {
		String digestString = "";
		for (int i = 0; i < digest.length; i++) {
			int low = digest[i] & 0xF;
			int hi = (digest[i] & 0xF0) >> 4;
			digestString = digestString + Integer.toHexString(hi);
			digestString = digestString + Integer.toHexString(low);
		}
		return digestString;
	}

	private static String bytesToHexString(byte[] bytes) {
		StringBuilder sb = new StringBuilder(bytes.length * 2);
		for (int i = 0; i < bytes.length; i++) {
			sb.append("0123456789abcdef".charAt((bytes[i] & 0xF0) >> 4));
			sb.append("0123456789abcdef".charAt((bytes[i] & 0xF) >> 0));
		}
		return sb.toString();
	}

	private static HashMap<String, String> splitAuthFields(String authString) {
		HashMap<String, String> fields = Maps.newHashMap();
		CharMatcher trimmer = CharMatcher.anyOf("\"\t ");
		Splitter commas = Splitter.on(',').trimResults().omitEmptyStrings();
		Splitter equals = Splitter.on('=').trimResults(trimmer).limit(2);
		for (String keyPair : commas.split(authString)) {
			String[] valuePair = (String[]) Iterables.toArray(equals.split(keyPair), String.class);
			fields.put(valuePair[0], valuePair[1]);
		}
		return fields;
	}

	public static void copyStream(InputStream input, OutputStream output) throws IOException {
		byte[] buffer = new byte[1024];
		int bytesRead;
		while ((bytesRead = input.read(buffer)) != -1) {
			output.write(buffer, 0, bytesRead);
		}
	}

	public static abstract interface HttpResult {
		public abstract void Completed(HttpClient.Response paramResponse);
	}

	protected static class Res {
		String html;
		byte[] bhtml;
	}
}
