/**
 * 
 */
package edu.uic.orjala.cyanos;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Savepoint;

/**
 * @author George Chlipala
 *
 */
@Deprecated
public class SingleFile implements ExternalFile {

	private File thisFile;
	
	protected String rootPath;
	protected String filePath;
	protected String description;
	protected String mimeType;
	
	SingleFile() {
		
	}
	
	public SingleFile(String root, File aFile) {
		this.thisFile = aFile;
		this.rootPath = root;
		this.filePath = this.thisFile.getAbsolutePath();
		if ( this.filePath.startsWith(this.rootPath) ) {
			this.filePath = this.filePath.substring(this.rootPath.length());
		}
	}

	public SingleFile(String root, String aPath) {
		this.thisFile = new File(root, aPath);
		this.rootPath = root;
		this.filePath = aPath;
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.ExternalFile#getDescription()
	 */
	public String getDescription() throws DataException {
		return this.description;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.ExternalFile#getFileObject()
	 */
	public File getFileObject() throws DataException {
		return this.thisFile;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.ExternalFile#getFilePath()
	 */
	public String getFilePath() throws DataException {
		return this.filePath;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.ExternalFile#getFileStream()
	 */
	public InputStream getInputStream() throws DataException {
		try {
			return new FileInputStream(this.thisFile);
		} catch (FileNotFoundException e) {
			throw new DataException(e);
		}
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.ExternalFile#getMimeType()
	 */
	public String getMimeType() throws DataException {
		return this.mimeType;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.ExternalFile#setDescription(java.lang.String)
	 */
	public void setDescription(String newText) throws DataException {
		this.description = newText;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.ExternalFile#setMimeType(java.lang.String)
	 */
	public void setMimeType(String newType) throws DataException {
		this.mimeType = newType;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.ExternalFile#setRootPath(java.lang.String)
	 */
	public void setRootPath(String aPath) {
		this.rootPath = aPath;
		this.filePath = this.thisFile.getAbsolutePath();
		if ( this.filePath.startsWith(this.rootPath) ) {
			this.filePath = this.filePath.substring(this.rootPath.length());
		}	
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.BasicObject#afterLast()
	 */
	public void afterLast() throws DataException {
		
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.BasicObject#beforeFirst()
	 */
	public void beforeFirst() throws DataException {
		
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.BasicObject#count()
	 */
	public int count() throws DataException {
		return 0;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.BasicObject#first()
	 */
	public boolean first() throws DataException {
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.BasicObject#isAllowed(int)
	 */
	public boolean isAllowed(int permission) {
		return true;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.BasicObject#isLoaded()
	 */
	public boolean isLoaded() throws DataException {
		return true;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.BasicObject#last()
	 */
	public boolean last() throws DataException {
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.BasicObject#next()
	 */
	public boolean next() throws DataException {
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.BasicObject#previous()
	 */
	public boolean previous() throws DataException {
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.BasicObject#refresh()
	 */
	public void refresh() throws DataException {
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.BasicObject#setAutoRefresh()
	 */
	public void setAutoRefresh() {
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.BasicObject#setManualRefresh()
	 */
	public void setManualRefresh() {
	}

	public String getDataType() throws DataException {
		return null;
	}

	public DataFileObject getReference() throws DataException {
		return null;
	}

	
	public boolean gotoRow(int row) throws DataException {
		return false;
	}

	
	public Savepoint setSavepoint() throws DataException {
		// TODO Auto-generated method stub
		return null;
	}

	
	public Savepoint setSavepoint(String name) throws DataException {
		// TODO Auto-generated method stub
		return null;
	}

	
	public void setAutoCommit(boolean value) throws DataException {
		// TODO Auto-generated method stub
		
	}

	
	public void commit() throws DataException {
		// TODO Auto-generated method stub
		
	}

	
	public void rollback() throws DataException {
		// TODO Auto-generated method stub
		
	}

	
	public void rollback(Savepoint savepoint) throws DataException {
		// TODO Auto-generated method stub
		
	}

	public String getAttribute(String attribute) throws DataException {
		// TODO Auto-generated method stub
		return null;
	}

	public void setAttribute(String attribute, String value)
			throws DataException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isLast() throws DataException {
		return false;
	}

}
