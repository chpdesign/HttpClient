package eu.nagygergely.httpclient;

import java.io.UnsupportedEncodingException;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;

import com.google.common.base.CharMatcher;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

public class DigestAuth implements AuthInterface {
	protected String username;
	protected String password;
	
	protected HashMap<String, String> authFields = null;

	public DigestAuth(String username, String password) {
		this.username = username;
		this.password = password;
	}
	
	@Override
	public void prepareAuthForConnection(Request request, HttpClient client, URLConnection connection) throws Exception {
		AuthInterface tAuth = client.getAuthenticator();
		client.setAuthenticator();
		Response response = client.sendRequest(request);
		client.setAuthenticator(tAuth);

		String auth = null;
		try {
			auth = (String) ((List<?>) response.getHeaders().get("WWW-Authenticate")).get(0);
		} catch (NullPointerException localNullPointerException) {
		}
		if (auth == null) {
			auth = "";
		}
		boolean AuthStart = false;
		
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
		String method = request.getMethod();
		String HA2 = null;
		try {
			md5.reset();
			String ha2str = colonJoiner.join(method, connection.getURL().getPath(), new Object[0]);
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
		sb.append("uri").append("=\"").append(connection.getURL().getPath()).append("\",");
		if (!AuthStart) {
			sb.append("qop").append("=").append("auth").append(",");
			sb.append("nc=").append((String) this.authFields.get("nc")).append(",");
			sb.append("cnonce=\"").append((String) this.authFields.get("cnonce")).append("\",");
		}
		sb.append("response").append("=\"").append(HA3).append("\"");
		connection.setRequestProperty("Authorization", sb.toString());
	}
	
	protected static HashMap<String, String> splitAuthFields(String authString) {
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
	
	protected static String bytesToHexString(byte[] bytes) {
		StringBuilder sb = new StringBuilder(bytes.length * 2);
		for (int i = 0; i < bytes.length; i++) {
			sb.append("0123456789abcdef".charAt((bytes[i] & 0xF0) >> 4));
			sb.append("0123456789abcdef".charAt((bytes[i] & 0xF) >> 0));
		}
		return sb.toString();
	}
}
