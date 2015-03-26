/**
 * 
 */
package edu.uic.orjala.cyanos;

import java.util.Date;

/**
 * @author George Chlipala
 *
 */
public interface DataFileObject extends BasicObject {

	String getID();
	
	/**
	 * Get the date the object was added.
	 * 
	 * @return add date as a {@link java.util.Date}
	 * @throws DataException
	 */
	Date getDate() throws DataException;
	
	String getDataFileClass();
	
	void linkDataFile(ExternalFile aFile, String dataType) throws DataException;
	
	void linkDataFile(String path, String dataType, String description, String mimeType) throws DataException;
	
	void updateDataFile(String path, String dataType, String description, String mimeType) throws DataException;
	
	ExternalFile getDataFiles() throws DataException;
	
	ExternalFile getDataFilesForType(String dataType) throws DataException;
	
	void unlinkDataFile(ExternalFile aFile) throws DataException;

	void unlinkDataFile(String path) throws DataException;
	
	boolean hasDataFile(String path) throws DataException;

	ExternalFile getDataFile(String path) throws DataException;
}
