package eu.nagygergely.httpclient;

import java.security.SecureRandom;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

public class SSLConnection extends SSLConnectionType {
	@Override
	public SSLSocketFactory getSocketFactory(TrustManager[] trustManagers) throws Exception {
		SSLContext sslContext = SSLContext.getInstance("SSL");
		sslContext.init(null, trustManagers, new SecureRandom());
		return sslContext.getSocketFactory();
	}
}
