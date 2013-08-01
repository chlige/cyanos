/**
 * 
 */
package edu.uic.orjala.cyanos;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * Interface for records linking files.
 * 
 * @author George Chlipala
 *
 */
public interface ExternalFile extends BasicObject {
	
	/**
	 * Get the MIME type of the file.
	 * 
	 * @return MIME type.
	 * @throws DataException
	 */
	String getMimeType() throws DataException;
	
	/**
	 * Set the MIME type of the file.
	 * 
	 * @param newType New MIME type
	 * @throws DataException
	 */
	void setMimeType(String newType) throws DataException;
	
	/**
	 * Get the description of the file.
	 * 
	 * @return Description of the file.
	 * @throws DataException
	 */
	String getDescription() throws DataException;	
	
	/**
	 * Set the description of the file.
	 * 
	 * @param newText New description of the file.
	 * @throws DataException
	 */
	void setDescription(String newText) throws DataException;
	
	/**
	 * Get the associated data type. This field will be specific for the linked CYANOS record.
	 * 
	 * @return Data type of the external file.
	 * @throws DataException
	 */
	String getDataType() throws DataException;
	
	/**
	 * Get the linked CYANOS record for this file.
	 * 
	 * @return CYANOS object linked to the file.
	 * @throws DataException
	 */
	DataFileObject getReference() throws DataException;

	/**
	 * Get an InputStream for the file.
	 * 
	 * @return IO stream to read the file.
	 * @throws DataException
	 */
	InputStream getInputStream() throws FileNotFoundException, DataException;
	
	/**
	 * Get the path of the file.
	 * 
	 * @return Path of the file.
	 * @throws DataException
	 */
	String getFilePath() throws DataException;
	
	/**
	 * Get the java.io.File object for the file.
	 * 
	 * @return the File 
	 * @throws DataException
	 */
	File getFileObject() throws DataException;
	
	/**
	 * Set the root path of the file.  Used by CYANOS to group files into directory paths on the system.
	 * 
	 * @param aPath Root path for the file.
	 */
	void setRootPath(String aPath);
	
}
