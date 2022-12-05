package eu.nagygergely.httpclient.nativecurl;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class HttpClient extends sun.net.www.http.HttpClient {
	
	protected Socket s;
	
	protected HttpClient(Socket socket) {
		s = socket;
	}
	
	@Override
	protected Socket doConnect(String paramString, int paramInt)
			throws IOException, UnknownHostException {
		return s;
	}
	
}
