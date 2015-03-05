/**
 * 
 */
package edu.uic.orjala.cyanos.sql;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import net.sf.jmimemagic.Magic;
import net.sf.jmimemagic.MagicException;
import net.sf.jmimemagic.MagicMatch;
import net.sf.jmimemagic.MagicMatchNotFoundException;
import net.sf.jmimemagic.MagicParseException;
import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.DataFileObject;
import edu.uic.orjala.cyanos.ExternalFile;

/**
 * @author George Chlipala
 *
 */
public class SQLExternalFile extends SQLObject implements ExternalFile {

	protected String rootPath = "";
	
	protected final static String DESCRIPTION_COLUMN = "description";
	protected final static String MIME_COLUMN = "mime_type";
	protected final static String FILE_COLUMN = "file";
	private static final String DATA_TYPE_COLUMN = "type";	
	
	protected static final String SQL_LOAD = "SELECT data.* FROM data WHERE file=?";

	public static ExternalFile load(SQLData data, String root, String path) throws DataException {
		SQLExternalFile aFile = new SQLExternalFile(data, root);
		aFile.myID = path;
		aFile.fetchRecord();
		return aFile;
	}
	
	/**
	 * @param data SQLData object
	 */
	SQLExternalFile(SQLData data, String root) {
		super(data);
		this.idField = FILE_COLUMN;
		this.rootPath = root;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.sql.SQLObject#fetchRecord()
	 */
	
	protected void fetchRecord() throws DataException {
		this.fetchRecord(SQL_LOAD);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.ExternalFile#getDescription()
	 */
	public String getDescription() throws DataException {
		return this.myData.getString(DESCRIPTION_COLUMN);
	}	

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.ExternalFile#getFileObject()
	 */
	public File getFileObject() throws DataException {
		if ( this.rootPath == null ) {
			this.rootPath = myData.config.getFilePath(this.myData.getString("tab"), this.getDataType());
		}
		return new File(this.rootPath, this.getID());
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.ExternalFile#getFilePath()
	 */
	public String getFilePath() throws DataException {
		return this.myData.getString(FILE_COLUMN);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.ExternalFile#getFileStream()
	 */
	public InputStream getInputStream() throws FileNotFoundException, DataException {
		return new FileInputStream(this.getFileObject());
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.ExternalFile#getMimeType()
	 */
	public String getMimeType() throws DataException {
		try {
			MagicMatch aMatch = Magic.getMagicMatch(this.getFileObject(), true);
			return aMatch.getMimeType();
		} catch (MagicParseException e) {
			throw new DataException(e);
		} catch (MagicMatchNotFoundException e) {
			throw new DataException(e);
		} catch (MagicException e) {
			throw new DataException(e);
		}
		// return this.myData.getString(MIME_COLUMN);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.ExternalFile#setDescription(java.lang.String)
	 */
	public void setDescription(String newText) throws DataException {
		this.myData.setString(DESCRIPTION_COLUMN, newText);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.ExternalFile#setMimeType(java.lang.String)
	 */
	public void setMimeType(String newType) throws DataException {
		// this.myData.setString(MIME_COLUMN, newType);
	}

	public void setRootPath(String aPath) {
		this.rootPath = aPath;
	}

	public String getDataType() throws DataException {
		return this.myData.getString(DATA_TYPE_COLUMN);
	}

	public DataFileObject getReference() throws DataException {
		// TODO Auto-generated method stub
		return null;
	}
	

}
