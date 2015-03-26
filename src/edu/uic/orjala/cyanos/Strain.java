/**
 * 
 */
package edu.uic.orjala.cyanos;

import java.util.Date;

/**
 * @author George Chlipala
 *
 */
public interface Strain extends DataFileObject, RemoteObject {

	final static String URL_DATA_TYPE = "url";
	final static String PHOTO_DATA_TYPE = "photo";
	
	final static String GOOD_STATUS = "good";
	final static String REMOVED_STATUS = "removed";
	final static String SLOW_GROWTH_STATUS = "slow growth";
	final static String CONTAMINATED_STATUS = "contaminated";
	final static String FIELD_HARVEST_STATUS = "field";
	
	final static String[] DATA_TYPES = { URL_DATA_TYPE, PHOTO_DATA_TYPE };
	final static String DATA_FILE_CLASS = "strain";
		
	/**
	 * Get the source of this strain.
	 * 
	 * @return Source of the strain as a {@link java.lang.String}.
	 * @throws DataException
	 */
	String getCultureSource() throws DataException;
	
	/**
	 * Get the genus of this strain.
	 * 
	 * @return Genus of the strain as a {@link java.lang.String}.
	 * @throws DataException
	 */
	String getGenus() throws DataException;
	
	/**
	 * Get the default media of the strain.
	 * 
	 * @return Default media as a {@link java.lang.String}.
	 * @throws DataException
	 */
	String getDefaultMedia() throws DataException;
	
	/**
	 * Get the notes of the strain.
	 * 
	 * @return Notes as a {@link java.lang.String}.
	 * @throws DataException
	 */
	String getNotes() throws DataException;
	
	/**
	 * Get the scientific name of the strain.
	 * 
	 * @return scientific name as a {@link java.lang.String}.
	 * @throws DataException
	 */
	String getName() throws DataException;
		
	/**
	 * Get the date the strain was added.
	 * 
	 * @return add date as a {@link java.lang.String}.
	 * @throws DataException
	 */
	String getDateString() throws DataException;
	
	/**
	 * Get the date the strain was removed.
	 * 
	 * @return removed date as a {@link java.util.Date}
	 * @throws DataException
	 */
	Date getRemovedDate() throws DataException;
	
	/**
	 * Get the date the strain was removed.
	 * 
	 * @return removed date as a {@link java.lang.String}.
	 * @throws DataException
	 */
	String getRemovedDateString() throws DataException;

	/**
	 * Get the reason the strain was removed.
	 * 
	 * @return removal reason as a {@link java.lang.String}.
	 * @throws DataException
	 */
	String getRemoveReason() throws DataException;
	
	/**
	 * The status of the strain.  Should be one of {@code Strain.GOOD_STATUS}, {@code Strain.DEAD_STATUS}, 
	 * {@code Strain.CONTAMINATED_STATUS} or {@code Strain.FIELD_HARVEST_STATUS}.
	 * 
	 * @return status of the strain as a {@link java.lang.String}
	 * @throws DataException
	 */
	String getStatus() throws DataException;
	
	/**
	 * Returns the associated project ID of the strain.
	 * 
	 * @return project ID as a {@link java.lang.String}.
	 * @throws DataException
	 */
	String getProjectID() throws DataException;
	
	Project getProject() throws DataException;

	void setCultureSource(String newSource) throws DataException;
	
	void setName(String newName) throws DataException;
	
	void setGenus(String newName) throws DataException;
	
	void setDefaultMedia(String newMedia) throws DataException;
	
	void setDate(Date newValue) throws DataException;
	
	void setDate(String newValue) throws DataException;
	
	void setRemoveDate(Date newValue) throws DataException;
	
	void setRemoveDate(String newValue) throws DataException;
	
	void setRemoveReason(String newValue) throws DataException;
	
	void setNotes(String newNotes) throws DataException;
	
	/**
	 * Set the status of the string.
	 * 
	 * @param newStatus one of {@code Strain.GOOD_STATUS}, {@code Strain.DEAD_STATUS}, {@code Strain.CONTAMINATED_STATUS} or {@code Strain.FIELD_HARVEST_STATUS}
	 * @throws DataException
	 */
	void setStatus(String newStatus) throws DataException;
	
	void setProjectID(String newValue) throws DataException;
	
	void setProject(Project aProject) throws DataException;
	
	void addNotes(String newNotes) throws DataException;
	
	Isolation getSourceIsolation() throws DataException;
	
	Collection getSourceCollection() throws DataException;
	
	String getSourceIsolationID() throws DataException;
	
	String getSourceCollectionID() throws DataException;
	
	void setSourceIsolation(Isolation isolation) throws DataException;
	
	void setSourceCollection(Collection collection) throws DataException;
	
	void setSourceIsolationID(String isolationID) throws DataException;
	
	void setSourceCollectionID(String collectionID) throws DataException;
		
	Inoc getInoculations() throws DataException;
	
	Harvest getFieldHarvests() throws DataException;
	
	Collection getFieldCollections() throws DataException;
	
	Sample getSamples() throws DataException;
	
	Assay getAssays() throws DataException;
	
	boolean wasRemoved() throws DataException;
	
	boolean isActive() throws DataException;
	
	Taxon getTaxon() throws DataException;
	
	String getTaxonName(String level) throws DataException;
	
	ExternalFile getURLs() throws DataException;
		
	void addURL(String url, String description) throws DataException;
	
	ExternalFile getPhotos() throws DataException;

	void addPhoto(ExternalFile aFile) throws DataException;
	
	String[] dataTypes();
	
	boolean statusIs(String status) throws DataException;
}
