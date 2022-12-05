package eu.nagygergely.httpclient;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;

import com.google.common.io.ByteStreams;

public class MultiPart extends PostMap {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected String boundary = Long.toHexString(System.currentTimeMillis());

	@Override
	public void appendBody(URLConnection connection) throws IOException {
		connection.setDoOutput(true);
		connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + this.boundary);
		if (this.size() > 0) {
			OutputStream output = connection.getOutputStream();
			for (String param : this.keySet()) {
				// System.out.println(params.get(param).getClass());
				if (this.get(param) != null) {
					if ((this.get(param) instanceof File)) {
						File file = (File) this.get(param);
						InputStream f = new FileInputStream(file);
						output.write(("--" + this.boundary).getBytes());
						output.write(13);
						output.write(10);
						output.write(("Content-Disposition: form-data; name=\"" + param + "\"; filename=\""
								+ file.getName() + "\"").getBytes());
						output.write(13);
						output.write(10);
						output.write("Content-Type: application/octet-stream".getBytes());
						output.write(13);
						output.write(10);
						output.write(13);
						output.write(10);
						ByteStreams.copy(f, output);
						output.write(13);
						output.write(10);
						f.close();
					}else if ((this.get(param) instanceof InputStream)) {
						InputStream f = (InputStream) this.get(param);
						output.write(("--" + this.boundary).getBytes());
						output.write(13);
						output.write(10);
						output.write(("Content-Disposition: form-data; name=\"" + param + "\"; filename=\""
								+ "file_"+System.currentTimeMillis()+".tmp" + "\"").getBytes());
						output.write(13);
						output.write(10);
						output.write("Content-Type: application/octet-stream".getBytes());
						output.write(13);
						output.write(10);
						output.write(13);
						output.write(10);
						ByteStreams.copy(f, output);
						output.write(13);
						output.write(10);
						f.close();
					}
					/*else if (this.get(param) instanceof byte[]) {
						byte[] f = (byte[]) this.get(param);
						output.write(("--" + this.boundary).getBytes());
						output.write(13);
						output.write(10);
						output.write(("Content-Disposition: form-data; name=\"" + param + "\"; filename=\""
								+ randomString() + "\"").getBytes());
						output.write(13);
						output.write(10);
						output.write("Content-Type: multipart/mixed; charset=UTF-8".getBytes());
						output.write(13);
						output.write(10);
						output.write(13);
						output.write(10);
						output.write(f);
						output.write(13);
						output.write(10);
					}*/
					else if(this.get(param) instanceof byte[]) {
						output.write(("--" + this.boundary).getBytes());
						output.write(13);
						output.write(10);
						output.write(("Content-Disposition: form-data; name=\"" + param + "\"; filename=\""
								+ "file_"+System.currentTimeMillis()+".tmp" + "\"").getBytes());
						output.write(13);
						output.write(10);
						output.write(13);
						output.write(10);
						output.write((byte[]) this.get(param));
						output.write(13);
						output.write(10);
					} else {
						output.write(("--" + this.boundary).getBytes());
						output.write(13);
						output.write(10);
						output.write(("Content-Disposition: form-data; name=\"" + param + "\"").getBytes());
						output.write(13);
						output.write(10);
						output.write(13);
						output.write(10);
						output.write(this.get(param).toString().getBytes());
						output.write(13);
						output.write(10);
					}
				} else
					System.err.println("Param Error: " + param);
			}
			output.write(("--" + this.boundary + "--").getBytes());
		}
	}

}
