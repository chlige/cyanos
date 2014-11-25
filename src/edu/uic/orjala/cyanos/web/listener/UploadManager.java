/**
 * 
 */
package edu.uic.orjala.cyanos.web.listener;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;

/**
 * @author George Chlipala
 *
 */
public class UploadManager {

	private final Map<String, List<FileUpload>> uploadItems = new Hashtable<String, List<FileUpload>>();
	private final Map<String, String[]> parameters = new Hashtable<String, String[]>();
	
	public class FileUpload {

		private final InputStream inStream;
		private final String name;
		private final String contentType;

		/**
		 * @throws IOException 
		 * 
		 */
		protected FileUpload(FileItemStream fileItem) throws IOException {
			this.name = fileItem.getName();

			InputStream fileIn = fileItem.openStream();
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			int c;
			while ((c= fileIn.read()) != -1) {
				out.write((char) c);
			}
			this.inStream = new ByteArrayInputStream(out.toByteArray());
			this.contentType = fileItem.getContentType();
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
	
	static UploadManager parseMultipartReq(HttpServletRequest req) throws ServletException, IOException {		
		UploadManager uploads = new UploadManager();
		try {
			ServletFileUpload upload = new ServletFileUpload();
			FileItemIterator iter = upload.getItemIterator(req);
			while (iter.hasNext() ) {
				FileItemStream anItem = iter.next();
				if ( ! anItem.isFormField() ) {
					uploads.addItem(anItem);
				} else {
					String[] current = uploads.parameters.get(anItem.getFieldName());
					String[] values;
					if ( current != null ) {
						values = new String[current.length + 1];
						System.arraycopy(current, 0, values, 0, current.length);
						values[values.length] = anItem.toString();
						
					} else {
						values = new String[1];
						values[0] = anItem.toString();
					}
					uploads.parameters.put(anItem.getFieldName(), values);
				}
			}
		} catch (FileUploadException e) {
			throw new ServletException("COULD NOT PARSE UPLOAD", e);
		}
		return uploads;
	}
	
	
	/**
	 * 
	 */
	protected UploadManager() {
	}

	
	protected void addItem(FileItemStream anItem) throws IOException {
		String thisField = anItem.getFieldName();
		if ( ! anItem.isFormField() ) {
			FileUpload file = new FileUpload(anItem);
			if ( file.getStream().available() > 0 ) {
				if ( ! uploadItems.containsKey(thisField) ) {
					uploadItems.put(thisField, new ArrayList<FileUpload>());
				}
				uploadItems.get(thisField).add(file);
			}
		}
	}
	
	public boolean hasFiles() {
		return (! this.uploadItems.isEmpty() );
	}
	
	public FileUpload getFile(String name, int index) {
		List<FileUpload> list = this.uploadItems.get(name);
		if ( list != null && list.size() > index ) {
			return list.get(index);
		}
		return null;
	}
	
	public int getFileCount(String name) {
		List<FileUpload> list = this.uploadItems.get(name);
		if ( list != null ) {
			return list.size();
		}
		return 0;
	}
	
	public String[] getParameterValues(String parameter) {
		return this.parameters.get(parameter);
	}
	
	public String getParameter(String parameter) {
		String[] values = this.getParameterValues(parameter);
		if ( values != null && values.length > 1 ) {
			return values[0];
		}
		return null;
	}



}
