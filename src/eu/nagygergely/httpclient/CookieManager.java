package eu.nagygergely.httpclient;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

public class CookieManager {
	private Map<String, Map> store;
	private static final String SET_COOKIE = "Set-Cookie";
	private static final String COOKIE_VALUE_DELIMITER = ";";
	private static final String PATH = "path";
	private static final String EXPIRES = "expires";
	private static final String DATE_FORMAT = "EEE, dd-MMM-yy HH:mm:ss zzz";
	private static final String SET_COOKIE_SEPARATOR = "; ";
	private static final String COOKIE = "Cookie";
	private static final char NAME_VALUE_SEPARATOR = '=';
	private static final char DOT = '.';
	private DateFormat dateFormat;

	public CookieManager() {
		this.store = new HashMap();
		this.dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.ENGLISH);
	}

	public void storeCookies(URLConnection conn) throws IOException {
		String domain = getDomainFromHost(conn.getURL().getHost());
		Map<String, Map<String, String>> domainStore;
		if (this.store.containsKey(domain)) {
			domainStore = (Map) this.store.get(domain);
		} else {
			domainStore = new HashMap();
			this.store.put(domain, domainStore);
		}
		String headerName = null;
		for (int i = 1; (headerName = conn.getHeaderFieldKey(i)) != null; i++) {
			if (headerName.equalsIgnoreCase(SET_COOKIE)) {
				Map<String, String> cookie = new HashMap();
				StringTokenizer st = new StringTokenizer(conn.getHeaderField(i), COOKIE_VALUE_DELIMITER);
				if (st.hasMoreTokens()) {
					String token = st.nextToken();
					String name = token.substring(0, token.indexOf(NAME_VALUE_SEPARATOR));
					String value = token.substring(token.indexOf(NAME_VALUE_SEPARATOR) + 1, token.length());
					domainStore.put(name, cookie);
					cookie.put(name, value);
				}
				while (st.hasMoreTokens()) {
					String token = st.nextToken();
					try {
						cookie.put(token.substring(0, token.indexOf(NAME_VALUE_SEPARATOR)).toLowerCase().trim(),
								token.substring(token.indexOf(NAME_VALUE_SEPARATOR) + 1, token.length()));
					} catch (Exception localException) {
					}
				}
			}
		}
	}

	public void setCookies(URLConnection conn) throws IOException {
		URL url = conn.getURL();
		String domain = getDomainFromHost(url.getHost());
		String path = url.getPath();

		Map domainStore = (Map) this.store.get(domain);
		if (domainStore == null) {
			return;
		}
		StringBuffer cookieStringBuffer = new StringBuffer();

		Iterator cookieNames = domainStore.keySet().iterator();
		while (cookieNames.hasNext()) {
			String cookieName = (String) cookieNames.next();
			Map<String, String> cookie = (Map) domainStore.get(cookieName);
			if ((comparePaths((String) cookie.get(PATH), path))
					&& (isNotExpired((String) cookie.get(EXPIRES)))) {
				cookieStringBuffer.append(cookieName);
				cookieStringBuffer.append(NAME_VALUE_SEPARATOR);
				cookieStringBuffer.append((String) cookie.get(cookieName));
				if (cookieNames.hasNext()) {
					cookieStringBuffer.append(SET_COOKIE_SEPARATOR);
				}
			}
		}
		try {
			conn.setRequestProperty(COOKIE, cookieStringBuffer.toString());
		} catch (IllegalStateException ise) {
			IOException ioe = new IOException(
					"Illegal State! Cookies cannot be set on a URLConnection that is already connected. Only call setCookies(java.net.URLConnection) AFTER calling java.net.URLConnection.connect().");

			throw ioe;
		}
	}

	private String getDomainFromHost(String host) {
		if (host.indexOf(DOT) != host.lastIndexOf(DOT)) {
			return host.substring(host.indexOf(DOT) + 1);
		}
		return host;
	}

	private boolean isNotExpired(String cookieExpires) {
		if (cookieExpires == null) {
			return true;
		}
		Date now = new Date();
		try {
			return now.compareTo(this.dateFormat.parse(cookieExpires)) <= 0;
		} catch (ParseException pe) {
			pe.printStackTrace();
		}
		return false;
	}

	private boolean comparePaths(String cookiePath, String targetPath) {
		if (cookiePath == null) {
			return true;
		}
		if (cookiePath.equals("/")) {
			return true;
		}
		if (targetPath.regionMatches(0, cookiePath, 0, cookiePath.length())) {
			return true;
		}
		return false;
	}

	public String toString() {
		return this.store.toString();
	}

	public void loadCookie(String data) {
	}

	public static void main(String[] args) {
		CookieManager cm = new CookieManager();
		try {
			URL url = new URL("http://www.hccp.org/test/cookieTest.jsp");
			URLConnection conn = url.openConnection();
			conn.connect();
			cm.storeCookies(conn);
			System.out.println(cm);
			cm.setCookies(url.openConnection());
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
}
