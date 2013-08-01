/**
 * 
 */
package edu.uic.orjala.cyanos;

/**
 * @author George Chlipala
 *
 */
public interface DataFileObject extends BasicObject {

	String getID();
	
	String getDataFileClass();
	
	void linkDataFile(ExternalFile aFile, String dataType) throws DataException;
	
	void linkDataFile(String path, String dataType, String description, String mimeType) throws DataException;
	
	void updateDataFile(String path, String dataType, String description, String mimeType) throws DataException;
	
	ExternalFile getDataFiles() throws DataException;
	
	ExternalFile getDataFilesForType(String dataType) throws DataException;
	
	void unlinkDataFile(ExternalFile aFile) throws DataException;

	void unlinkDataFile(String path) throws DataException;
}
