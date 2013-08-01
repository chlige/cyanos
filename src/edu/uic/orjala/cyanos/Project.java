/**
 * 
 */
package edu.uic.orjala.cyanos;

import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.util.Date;
import java.util.Map;

/**
 * Interface for Project objects.  
 * 
 * @author George Chlipala
 *
 */
public interface Project extends BasicObject {
	
	/**
	 * @author George Chlipala
	 *
	 */
	public interface UpdateHost extends BasicObject {
		
		String getHostName() throws DataException;
		
		String getHostID() throws DataException;
		
		String getPublicKeyString() throws DataException;
		
		PublicKey getPublicKey() throws DataException, GeneralSecurityException;
		
	}

	public final static int UPDATE_SEND = 1;
	public final static int UPDATE_RECEIVE = 2;
	public final static int UPDATE_SEND_RECIEVE = UPDATE_SEND + UPDATE_RECEIVE;
	public final static int UPDATE_RECEIVE_LOCAL_ONLY = 4;

	/**
	 * Returns the ID of the current Project
	 * 
	 * @return Project ID
	 */
	String getID();
	
	/**
	 * Get the name of the project.
	 * 
	 * @return Name of the project.
	 * @throws DataException
	 */
	String getName() throws DataException;
	
	/**
	 * Get project notes/description.
	 * 
	 * @return Notes for the project.
	 * @throws DataException
	 */
	String getNotes() throws DataException;
	
	/**
	 * Set a the name of the project.
	 * 
	 * @param newValue New name for the project.
	 * @throws DataException
	 */
	void setName(String newValue) throws DataException;
	
	/**
	 * Set the notes/description of the project
	 * 
	 * @param newValue New notes/description of the project.
	 * @throws DataException
	 */
	void setNotes(String newValue) throws DataException;
	
	Collection collections() throws DataException;
	
	Strain strains() throws DataException;
	
	Inoc inoculations() throws DataException;

	Material materials() throws DataException;
	
	Assay assays() throws DataException;

	/**
	 * Set the URL of the master server.
	 * 
	 * @param url New URL for master server.
	 * @throws DataException
	 */
	void setMasterURL(String url) throws DataException;
	
	/**
	 * Get the URL of the master server.
	 * 
	 * @return URL for the master server.
	 * @throws DataException
	 */
	String getMasterURL() throws DataException;
	
	/**
	 * Set the certificate of the master server.
	 * 
	 * @param certificate X.509 encoded certificate for update server
	 * @throws DataException
	 */
	void setUpdateCert(String certificate) throws DataException;
	
	/**
	 * Get the certificate of the master server.
	 * 
	 * @return x.509 encoded certificate for the master server.
	 * @throws DataException
	 */
	String getUpdateCert() throws DataException;

	/**
	 * Get the update preferences
	 * 
	 * @return 
	 * @throws DataException
	 */
	Map<String,Integer> getUpdatePrefs() throws DataException;
	
	/**
	 * Set the update preference for a particular object class.
	 * 
	 * @param objectClass
	 * @param updateType
	 * @throws DataException
	 */
	void setUpdatePrefs(String objectClass, int updateType) throws DataException;
	
	/**
	 * Get the update preference for an object class.
	 * 
	 * @param objectClass
	 * @return
	 * @throws DataException
	 */
	int getUpdatePrefs(String objectClass) throws DataException;
	
	/**
	 * Get the date/time of the last update sent to the master server
	 * 
	 * @return
	 * @throws DataException
	 */
	Date getLastUpdateSent() throws DataException;
	
	/**
	 * Set the date/time of the last update sent to the master server.
	 * 
	 * @param time
	 * @throws DataException
	 */
	void setLastUpdateSent(Date time) throws DataException;
	
	String getLastUpdateMessage() throws DataException;
	
	void setLastUpdateMessage(String message) throws DataException;
	
	String getKeyForHost(String hostID) throws DataException;
	
	UpdateHost getHosts() throws DataException;
	
	UpdateHost getUpdateHost(String hostID) throws DataException;
	
	void addUpdateHost(String hostID, String hostname, String publicKey) throws DataException;
	
	void removeHost(String hostsID) throws DataException;
}