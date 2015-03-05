package edu.uic.orjala.cyanos.web;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.util.Streams;

public class FileUpload {

	private final InputStream inStream;
	private final String name;
	private final String contentType;

	public static FileUpload fromPut(HttpServletRequest req) throws IOException {
		String path = req.getPathInfo();
		if ( path != null ) {
			path = path.substring(1);
			return new FileUpload(path, req.getContentType(), req.getInputStream());
		}
		return null;
	}
	
	private FileUpload(String name, String contentType, InputStream stream) throws IOException {
		this.name = name;
		this.contentType = contentType;

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buffer = new byte[4096];
		int c;
		while ((c= stream.read(buffer)) != -1) {
			out.write(buffer, 0, c);
		}
		this.inStream = new ByteArrayInputStream(out.toByteArray());
	}
	
	/**
	 * @throws IOException 
	 * 
	 */
	protected FileUpload(FileItemStream fileItem) throws IOException {
		this(fileItem.getName(), fileItem.getContentType(), fileItem.openStream());
	}

	/**
	 * Return the name of the uploaded file.
	 * 
	 * @return
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Return an InputStream of the uploaded data.
	 * 
	 * @return
	 * @throws IOException
	 */
	public InputStream getStream() throws IOException {
		inStream.reset();
		return inStream;
	}

	/**
	 * Return the reported content type of the uploaded file
	 * 
	 * @return
	 */
	public String getContentType() {
		return contentType;
	}

	/**
	 * Return the uploaded data as a String.
	 * 
	 * @return
	 * @throws IOException
	 */
	@Deprecated
	public String getString() throws IOException {
		inStream.reset();
		return Streams.asString(inStream);
	}

}