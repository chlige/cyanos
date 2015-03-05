package edu.uic.orjala.cyanos;

import java.util.Date;

/**
 * Interface for field collection records.
 * 
 * @author George Chlipala
 *
 */
public interface Collection extends BasicObject, NotebookObject, RemoteObject, DataFileObject {

	final static String PHOTO_DATA_TYPE = "photo";
	
	final static String[] DATA_TYPES = { PHOTO_DATA_TYPE };
	final static String DATA_FILE_CLASS = "collection";


	/**
	 * Returns the ID of the current Collection
	 * 
	 * @return Collection ID
	 */
	String getID();
	
	/**
	 * Returns the string description of the location.
	 * 
	 * @return location descriptor
	 * @throws DataException
	 */
	String getLocationName() throws DataException;

	/**
	 * Returns the latitude as a floating point number (float).
	 * <BR/>Positive values for North latitudes and negative for South.
	 * 
	 * @return latitude as a float
	 * @throws DataException
	 */
	Float getLatitudeFloat() throws DataException;

	/**
	 * Returns the longitude as a floating point number (float).
	 * <BR/>Positive values for East longitude and negative for West.
	 * 
	 * @return longitude as a float
	 * @throws DataException
	 */
	Float getLongitudeFloat() throws DataException;

	/**
	 * Returns the latitude hemisphere, i.e. N or S.
	 * 
	 * @return Latitude hemipshere string
	 * @throws DataException
	 */
	String getLatitudeHemisphere() throws DataException;

	/**
	 * Returns the latitude of the collection formated as H D&deg; M.M'.
	 * <BR/>For example, a database value of 41.86432 would produce N 41&deg; 51.8597'
	 * 
	 * @return Latitude as a string
	 * @throws DataException
	 */
	String getLatitudeDM() throws DataException;

	/**
	 * Returns the longitude hemisphere, i.e. E or W.
	 * 
	 * @return Longitude hemipshere string
	 * @throws DataException
	 */
	String getLongitudeHemisphere() throws DataException;

	/**
	 * Returns the longitude of the collection formated as H D&deg; M.M'.
	 * <BR/>For example, a database value of -87.609239 would produce a value of W 87&deg; 36.5543'
	 * 
	 * @return Longitude as a string
	 * @throws DataException
	 */
	String getLongitudeDM() throws DataException;

	/**
	 * Retrieve the collection notes.
	 * 
	 * @return Collection notes
	 * @throws DataException
	 */
	String getNotes() throws DataException;

	/**
	 * Retrieve the collection date.
	 * 
	 * @return Collection date as a Date object.
	 * @throws DataException
	 */
	Date getDate() throws DataException;

	/**
	 * Retrieve the collection date.
	 * 
	 * @return Collection date as a string.
	 * @throws DataException
	 */
	String getDateString() throws DataException;

	/**
	 * Retrieve collector information.
	 *  
	 * @return Collector information.
	 * @throws DataException
	 */
	String getCollector() throws DataException;

	/**
	 * Retrieve location precision, in meters.
	 *  
	 * @return location precision as int.
	 * @throws DataException
	 */
	Integer getPrecision() throws DataException;

	/**
	 * Retrieve the project ID.
	 *  
	 * @return Collection project ID
	 * @throws DataException
	 */
	String getProjectID() throws DataException;

	/**
	 * Retrieve project information as a Project object.
	 *  
	 * @return Project object for associated project ID.
	 * @throws DataException
	 */
	Project getProject() throws DataException;

	/**
	 * Retrieve field harvest information.
	 * 
	 * @return Harvest object for harvest or null if non-existant
	 * @throws DataException 
	 * @see Harvest
	 */
	Harvest getHarvest() throws DataException;

	/**
	 * Set project ID for the collection.
	 * 
	 * @param newValue new project ID.
	 * @throws DataException
	 */
	void setProjectID(String newValue) throws DataException;

	/**
	 * Set project information for the collection.
	 * 
	 * @param aProject project object to associate.
	 * @throws DataException
	 */
	void setProject(Project aProject) throws DataException;

	/**
	 * Set collection date
	 * 
	 * @param newValue collection date as a Date object.
	 * @throws DataException
	 */
	void setDate(Date newValue) throws DataException;

	/**
	 * Set collection date
	 * 
	 * @param newValue collection date as a string.
	 * @throws DataException
	 */
	void setDate(String newValue) throws DataException;

	/**
	 * Set location descriptor.
	 * 
	 * @param newValue location descriptor
	 * @throws DataException
	 */
	void setLocationName(String newValue) throws DataException;

	/**
	 * Sets the latitude of the collection. Positive numbers denote North latitude and negative for South.
	 * 
	 * @param newValue latitude as a float
	 * @throws DataException
	 */
	void setLatitude(float newValue) throws DataException;

	/**
	 * Sets the latitude of the collection.  
	 * The method will parse the string to create the float value for storage.  
	 * The following formats are accepted. Please note that degree (&deg;), minute ('), and second (") marks are optional.
	 * <UL><LI>Decimal degree (positive for North, negative for South), e.g. 41.86432</LI>
	 * <LI>Hemisphere letter with decimal degrees, e.g. N 41.86432&deg;</LI>
	 * <LI>Hemisphere letter with degrees and decimal minutes, e.g. N 41&deg; 51.8597'</LI>
	 * <LI>Hemisphere letter with degrees, minutes, and decimal seconds, e.g. N 41&deg; 51' 51.58'</LI>
	 * </UL>
	 * 
	 * @param newValue latitude as a string
	 * @throws DataException
	 */
	void setLatitude(String newValue) throws DataException;

	/**
	 * Sets the longitude of the collection. Positive numbers denote East longitude and negative for West.
	 * 
	 * @param newValue longitude as a float
	 * @throws DataException
	 */
	void setLongitude(float newValue) throws DataException;

	/**
	 * Sets the longitude of the collection.  
	 * The method will parse the string to create the float value for storage.  
	 * The following formats are accepted. Please note that degree (&deg;), minute ('), and second (") marks are optional.
	 * <UL><LI>Decimal degree (positive for East, negative for West), e.g. -87.609239</LI>
	 * <LI>Hemisphere letter with decimal degrees, e.g. W 87.60924&deg;</LI>
	 * <LI>Hemisphere letter with degrees and decimal minutes, e.g. W 87&deg; 36.5543'</LI>
	 * <LI>Hemisphere letter with degrees, minutes, and decimal seconds, e.g. W 87&deg; 36' 33.26'</LI>
	 * </UL>
	 * 
	 * @param newValue latitude as a string
	 * @throws DataException
	 */
	void setLongitude(String newValue) throws DataException;

	/**
	 * Set collector information.
	 * 
	 * @param newValue collector's name
	 * @throws DataException
	 */
	void setCollector(String newValue) throws DataException;

	/**
	 * Set collection notes.
	 * 
	 * @param newNotes collection notes.
	 * @throws DataException
	 */
	void setNotes(String newNotes) throws DataException;

	/**
	 * Set location precision.
	 * <BR/>The method will parse the string to create the numeric value.  A valid format is a decimal number followed by units of length (km or m).
	 * 
	 * @param newValue precision as a string.
	 * @throws DataException
	 */
	void setPrecision(String newValue) throws DataException;

	/**
	 * Set location precision.
	 * 
	 * @param newValue precision as an integer (int)
	 * @throws DataException
	 */
	void setPrecision(int newValue) throws DataException;

	/**
	 * Create a field harvest record for this collection.
	 * 
	 * @return a new Harvest object.
	 * @param strainID ID of the strain
	 * @throws DataException
	 */
	Harvest createHarvest(String strainID) throws DataException;

	/**
	 * Add a string to the collection notes.
	 * 
	 * @param newNotes string to add to collection notes.
	 * @throws DataException
	 */
	void addNotes(String newNotes) throws DataException;

	/**
	 * Retrieve isolations associated with this collection.
	 * 
	 * @return Isolation object
	 * @throws DataException
	 */
	Isolation getIsolations() throws DataException;

	/**
	 * Retrieve stains derived from this collection.
	 * 
	 * @return Strain object
	 * @throws DataException
	 */
	Strain getStrains() throws DataException;
	
	ExternalFile getPhotos() throws DataException;

	String addPhoto(String name, String description, String mimeType) throws DataException;
}