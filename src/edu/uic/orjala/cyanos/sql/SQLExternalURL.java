/**
 * 
 */
package edu.uic.orjala.cyanos.sql;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import edu.uic.orjala.cyanos.DataException;

/**
 * @author gchlip2
 *
 */
public class SQLExternalURL extends SQLExternalFile {

	private static final String URL_MIME_TYPE = "application/x-url";

	/**
	 * @param data SQLData object
	 */
	SQLExternalURL(SQLData data) {
		super(data, null);
	}
	
	public File getFileObject() throws DataException {
		try {
			return new File(new URI(this.getID()));
		} catch (URISyntaxException e) {
			throw new DataException(e);
		}
	}

	public InputStream getInputStream() throws DataException {
		try {
			URL aURL = new URL(this.getID());
			return aURL.openStream();
		} catch (MalformedURLException e) {
			throw new DataException(e);
		} catch (IOException e) {
			throw new DataException(e);
		}
		
	}

	public String getMimeType() throws DataException {
		return URL_MIME_TYPE;
	}

	public void setMimeType(String newType) throws DataException {
		this.myData.setString(MIME_COLUMN, URL_MIME_TYPE);
	}

}
