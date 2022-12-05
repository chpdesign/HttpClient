package eu.nagygergely.httpclient.nativecurl;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import sun.net.ProgressMonitor;
import sun.net.ProgressSource;
import sun.net.www.MessageHeader;
import sun.net.www.protocol.http.Handler;

public class UrlConnection extends sun.net.www.protocol.http.HttpURLConnection {
	
	protected UrlConnection(URL paramURL, Handler paramHandler)
		    throws IOException
	  {
	    this(paramURL, null, paramHandler);
	  }
	  
	  public UrlConnection(URL paramURL, String paramString, int paramInt)
	  {
	    this(paramURL, new Proxy(Proxy.Type.HTTP, InetSocketAddress.createUnresolved(paramString, paramInt)));
	  }
	  
	  public UrlConnection(URL paramURL, Proxy paramProxy)
	  {
	    this(paramURL, paramProxy, new Handler());
	  }
	  
	  protected UrlConnection(URL paramURL, Proxy paramProxy, Handler paramHandler)
	  {
		  super(paramURL, paramProxy, paramHandler);
	  }

	protected String curlLocation = "D:\\Downloads\\curl-7.86.0_2-win64-mingw\\bin\\curl.exe";
	
	protected InputStream in;
	protected ByteArrayOutputStream out;
	
	protected HashMap<String, String[]> headers = new HashMap<String, String[]>();
	
	public void setCurlLocation(String curlLocation) {
		this.curlLocation = curlLocation;
	}
	
	public String getCurlLocation() {
		return curlLocation;
	}
	
	public ArrayList<String> toCurlRequest() {
	    ArrayList<String> builder = new ArrayList<String>();
	    builder.add(curlLocation);
	    
	    builder.add("-sSL");
	    builder.add("-D");
	    
	    // URL
	    builder.add("-");
	    builder.add(this.getURL().toString());

	    // Method	
	    builder.add("-X");
	    builder.add(this.getRequestMethod());

	    // Headers
	    for (Entry<String, List<String>> entry : this.getRequestProperties().entrySet()) {
	    	builder.add("-H");
	    	String header = entry.getKey() + ":";
	        for (String value : entry.getValue()) {
	        	header += " " + value;
	        }
	        header += "";
	        builder.add(header);
	    }

	    // Body
	    if (out != null) {
	    	builder.add("-d '" + new String(out.toByteArray()) + "'");
	    }

	    return builder;
	}
	
	@Override
	protected sun.net.www.http.HttpClient getNewHttpClient(URL paramURL,
			Proxy paramProxy, int paramInt) throws IOException {
		System.out.println("OK1");
		return new HttpClient(new Socket() {
        	@Override
        	public InputStream getInputStream() throws IOException {
	    		try {
	        		ProcessBuilder builder = new ProcessBuilder(toCurlRequest());
	    			Process proc = builder.start();
	    			proc.waitFor();
	    			in = proc.getInputStream();
        		} catch (InterruptedException e) {
        			// TODO Auto-generated catch block
        			e.printStackTrace();
        		}
        		return in;
        	}
        	
        	@Override
        	public OutputStream getOutputStream() throws IOException {
        		return out;
        	}
        });
	}
	
	@Override
	protected sun.net.www.http.HttpClient getNewHttpClient(URL paramURL,
			Proxy paramProxy, int paramInt, boolean paramBoolean)
			throws IOException {
		System.out.println("OK2");
		return new HttpClient(new Socket() {
        	@Override
        	public InputStream getInputStream() throws IOException {
        		try {
	        		ProcessBuilder builder = new ProcessBuilder(toCurlRequest());
	    			Process proc = builder.start();
	    			proc.waitFor();
	    			in = proc.getInputStream();
        		} catch (InterruptedException e) {
        			// TODO Auto-generated catch block
        			e.printStackTrace();
        		}
        		return in;
        	}
        	
        	@Override
        	public OutputStream getOutputStream() throws IOException {
        		return out;
        	}
        });
	}
	
	@Override
	protected void setNewClient(URL paramURL) throws IOException {
		http = getNewHttpClient(paramURL, null, 0);
	}
	
	@Override
	protected void setNewClient(URL paramURL, boolean paramBoolean)
			throws IOException {
		http = getNewHttpClient(paramURL, null, 0);
	}
	
	
	@Override
	public InputStream getInputStream() throws IOException {
		Method[] methods = this.getClass().getSuperclass().getDeclaredMethods();
		for(int i= 0; i < methods.length; i++) {
			System.out.println(methods[i].getName());
		}
		try {
			ProcessBuilder builder = new ProcessBuilder(toCurlRequest());
			Process proc = builder.start();
			proc.waitFor();
			
	        in = proc.getInputStream();
	        
			/*BufferedReader stdInput = new BufferedReader(new 
				     InputStreamReader(proc.getInputStream()));
			//System.out.println("Here is the standard output of the command:\n");
			int line = 0;
			do {
				String s = stdInput.readLine();
				if ((s != null) && (!s.isEmpty())) {
				    //System.out.println(s);
					if (line == 0) {
						responseCode = Integer.parseInt(s.split(" ")[1]);
					}
					line++;
				} else {
					break;
				}
			} while(true);*/

			/*BufferedReader stdInput = new BufferedReader(new 
			     InputStreamReader(proc.getInputStream()));

			BufferedReader stdError = new BufferedReader(new 
			     InputStreamReader(proc.getErrorStream()));

			// Read the output from the command
			System.out.println("Here is the standard output of the command:\n");
			String s = null;
			while ((s = stdInput.readLine()) != null) {
			    System.out.println(s);
			}

			// Read any errors from the attempted command
			System.out.println("Here is the standard error of the command (if any):\n");
			while ((s = stdError.readLine()) != null) {
			    System.out.println(s);
			}*/
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return super.getInputStream();
	}
	
	
	@Override
	public OutputStream getOutputStream() throws IOException {
		if (out == null) {
			out = new ByteArrayOutputStream();
		}
		return out;
	}

	@Override
	public void connect() throws IOException {
		
	}

	@Override
	public void disconnect() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean usingProxy() {
		// TODO Auto-generated method stub
		return false;
	}

}
