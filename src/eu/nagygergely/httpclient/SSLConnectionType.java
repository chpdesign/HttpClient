package eu.nagygergely.httpclient;

import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

public abstract class SSLConnectionType {
	
	public abstract SSLSocketFactory getSocketFactory(TrustManager[] trustManagers) throws Exception;

}
