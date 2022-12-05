package eu.nagygergely.httpclient;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLConnection;

public class Application extends PostMap {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public void appendBody(URLConnection connection) throws IOException {
		connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;");
		StringBuilder sb = new StringBuilder();
		for (String param : this.keySet()) {
			sb.append(param).append("=").append(this.get(param)).append("&");
		}
		sb.deleteCharAt(sb.length() - 1);
		connection.setDoOutput(true);
		OutputStream output = connection.getOutputStream();
		output.write(sb.toString().getBytes());
	}

}
