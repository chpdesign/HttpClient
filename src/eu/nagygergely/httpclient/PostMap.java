package eu.nagygergely.httpclient;

import java.util.HashMap;

public abstract class PostMap extends HashMap<String, Object> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public enum formType{
		Application("application/x-www-form-urlencoded"),
		MultiPart("multipart/form-data"),
		@Deprecated
		TextPlain("text/plain");
		private String type;
		private formType(String type)
		{
			this.type = type;
		}
		public String type()
		{
			return type;
		}
	}
	
	/**
	 * application/x-www-form-urlencoded
	 * multipart/form-data
	 * text/plain /!\ NEVER USE /!\
	 * @return
	 */
	public abstract formType formType();

}
