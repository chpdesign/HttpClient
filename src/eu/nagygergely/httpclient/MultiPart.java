package eu.nagygergely.httpclient;

public class MultiPart extends PostMap {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	public formType formType() {
		return PostMap.formType.MultiPart;
	}

}
