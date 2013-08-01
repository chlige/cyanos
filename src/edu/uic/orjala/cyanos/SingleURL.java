/**
 * 
 */
package edu.uic.orjala.cyanos;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * @author George Chlipala
 *
 */
public class SingleURL extends SingleFile implements ExternalFile {

	/**
	 * 
	 */
	
	public SingleURL(String aURL) {
		this.filePath = aURL;
	}

	public SingleURL(URI aURL) {
		this.filePath = aURL.toString();
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.ExternalFile#getFileObject()
	 */
	public File getFileObject() throws DataException {
		try {
			return new File(new URI(this.filePath));
		} catch (URISyntaxException e) {
			throw new DataException(e);
		}
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.ExternalFile#getFileStream()
	 */
	public InputStream getInputStream() throws DataException {
		try {
			URL aURL = new URL(this.filePath);
			return aURL.openStream();
		} catch (MalformedURLException e) {
			throw new DataException(e);
		} catch (IOException e) {
			throw new DataException(e);
		}
		
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.ExternalFile#getMimeType()
	 */
	public String getMimeType() throws DataException {
		return "application/x-url";
	}

	public void setMimeType(String newType) throws DataException {
		// DOES NOTHING.
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.ExternalFile#setRootPath(java.lang.String)
	 */
	public void setRootPath(String aPath) {
		// DOES NOTHING

	}
}
