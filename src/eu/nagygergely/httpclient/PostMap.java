package eu.nagygergely.httpclient;

import java.io.IOException;
import java.net.URLConnection;
import java.util.HashMap;

public abstract class PostMap extends HashMap<String, Object> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * application/x-www-form-urlencoded
	 * multipart/form-data
	 * text/plain /!\ NEVER USE /!\
	 * @return
	 */
	public abstract void appendBody(URLConnection connection) throws IOException;

}
