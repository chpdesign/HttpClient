package eu.nagygergely.httpclient;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URLConnection;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class DefaultUrlConnectionFactory extends URLConnectionFactory {

	@Override
	public URLConnection getConnection(HttpClient client, Request request) throws MalformedURLException, IOException, Exception {
		URLConnection conn = null;
		//Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("localhost", 8888));
        /*Authenticator authenticator = new Authenticator() {

            public PasswordAuthentication getPasswordAuthentication() {
                return (new PasswordAuthentication("user",
                        "password".toCharArray()));
            }
        };*/
		Proxy proxy = client.getProxy();
		if (request.getUrl().getProtocol().startsWith("https")) {
			if(proxy != null) {
				conn = (HttpsURLConnection) request.getUrl().openConnection(proxy);
			} else {
				conn = (HttpsURLConnection) request.getUrl().openConnection();
			}
			
			conn.setConnectTimeout(client.getTimeOut());

			TrustManager[] trustAllCerts = { new X509TrustManager() {
				public void checkClientTrusted(X509Certificate[] chain, String authType) {
					// nothing to do
				}

				public void checkServerTrusted(X509Certificate[] chain, String authType) {
					// nothing to do
				}

				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}
			} };
			SSLSocketFactory sslSocketFactory = client.getSSLConnectionType().getSocketFactory(trustAllCerts);
			
			((HttpsURLConnection) conn).setSSLSocketFactory(sslSocketFactory);
			((HttpsURLConnection) conn).setInstanceFollowRedirects(false);
			((HttpsURLConnection) conn).setHostnameVerifier(new HostnameVerifier(){
				@Override
				public boolean verify(String hostname, SSLSession arg1) {
					return true;
				}
			});
			HttpsURLConnection.setFollowRedirects(false);
		} else {
			if(proxy != null) {
				conn = request.getUrl().openConnection(proxy);
			} else {
				conn = request.getUrl().openConnection();
			}
			((HttpURLConnection) conn).setInstanceFollowRedirects(false);
			HttpURLConnection.setFollowRedirects(false);
		}
		((HttpURLConnection) conn).setRequestMethod(request.getMethod());
		/*String password = "username:password";
        String encodedPassword = Base64.encode( password.getBytes() );
        conn.setRequestProperty( "Proxy-Authorization", encodedPassword);*/
		return conn;
	}

}
