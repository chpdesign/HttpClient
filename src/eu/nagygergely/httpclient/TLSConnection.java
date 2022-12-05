package eu.nagygergely.httpclient;

import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

public class TLSConnection extends SSLConnectionType {
	@Override
	public SSLSocketFactory getSocketFactory(TrustManager[] trustManagers) throws Exception {
		/*SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
		sslContext.init(null, trustManagers, new SecureRandom());
		return sslContext.getSocketFactory();*/
		return new TLSSocketFactory(trustManagers);
	}
}
