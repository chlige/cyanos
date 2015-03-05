package edu.uic.orjala.cyanos.sql;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.uic.orjala.cyanos.Collection;
import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.ExternalFile;
import edu.uic.orjala.cyanos.Harvest;
import edu.uic.orjala.cyanos.Isolation;
import edu.uic.orjala.cyanos.Notebook;
import edu.uic.orjala.cyanos.Project;
import edu.uic.orjala.cyanos.Strain;
import edu.uic.orjala.cyanos.User;


/**
 * Class to retrieve and manipulate field collection data.
 * 
 * <P>This data is stored in the SQL table "collection".</P>
 * 
 * <TABLE BORDER=1><TR><TD><B>SQL Column</B></TD><TD><B>SQL Data Type</B></TD><TD><B>Java Constant</B></TD></TR>
 * <TR><TD>collection_id</TD><TD>VARCHAR(64)</TD><TD ALIGN='CENTER'> - </TD></TR>
 * <TR><TD>date</TD><TD>DATE</TD><TD>DATE_COLUMN</TD></TR>
 * <TR><TD>location</TD><TD>VARCHAR(256)</TD><TD>LOCATION_COLUMN</TD></TR>
 * <TR><TD>latitude</TD><TD>FLOAT</TD><TD>LATITUDE_COLUMN</TD></TR>
 * <TR><TD>longitude</TD><TD>FLOAT</TD><TD>LONGITUDE_COLUMN</TD></TR>
 * <TR><TD>geo_precision</TD><TD>INT(10) UNSIGNED</TD><TD>PRECISION_COLUMN</TD></TR>
 * <TR><TD>collector</TD><TD>VARCHAR(256)</TD><TD>COLLECTOR_COLUMN</TD></TR>
 * <TR><TD>notes</TD><TD>CLOB(1 k)</TD><TD>NOTES_COLUMN</TD></TR>
 * <TR><TD>project_id</TD><TD>VARCHAR(128)</TD><TD>PROJECT_COLUMN</TD></TR></TABLE>
 * 
 * @author George Chlipala
 * @version 1.0
 *
 */
public class SQLCollection extends SQLObject implements Collection {
	
	public static final String ID_COLUMN = "collection_id";
	public static final String LOCATION_COLUMN = "location";
	public static final String LATITUDE_COLUMN = "latitude";
	public static final String LONGITUDE_COLUMN = "longitude";
	public static final String PRECISION_COLUMN = "geo_precision";
	public static final String COLLECTOR_COLUMN = "collector";
	public static final String NOTES_COLUMN = "notes";
	public static final String PROJECT_COLUMN = "project_id";
	public static final String NOTEBOOK_COLUMN = "notebook_id";
	public static final String NOTEBOOK_PAGE_COLUMN = "notebook_page";
	public static final String DATE_COLUMN = "date";
	
	public final static String REMOTE_HOST_COLUMN = "remote_host";

	private final static String[] ALL_COLUMNS = { ID_COLUMN, DATE_COLUMN, 
		LOCATION_COLUMN, LATITUDE_COLUMN , LONGITUDE_COLUMN,
		COLLECTOR_COLUMN, PRECISION_COLUMN,
//		NOTEBOOK_COLUMN, NOTEBOOK_PAGE_COLUMN, 
		NOTES_COLUMN, PROJECT_COLUMN, REMOTE_HOST_COLUMN };	
	
	private final static String SQL_BASE = sqlBase("collection", ALL_COLUMNS);

	private final static String SQL_BOUNDS_BASE = "SELECT MIN(collection.longitude), MIN(collection.latitude), MAX(collection.longitude), MAX(collection.latitude) FROM collection";
	
	
	private static final String SQL_LOAD_COLLECTION = SQL_BASE + " WHERE collection_id=?";
	private static final String SQL_INSERT_WITH_PROJECT = "INSERT INTO collection(collection_id,project_id) VALUES(?,?)";
	private static final String SQL_GET_HARVESTS = SQLHarvest.SQL_BASE + " WHERE collection_id=?";
	private static final String SQL_GET_STRAINS = SQLStrain.SQL_BASE + " WHERE collection_id=?";
	private static final String SQL_GET_ISOLATIONS = "SELECT isolation.* FROM isolation WHERE collection_id=?";
	private static final String SQL_LOAD_SORTABLE = SQL_BASE + " ORDER BY %s %s";

	private static final String SQL_COLLECTIONS_LOCATED = SQL_BASE + " WHERE latitude >= ? AND latitude <= ? AND longitude >= ? AND longitude <= ?";
	
	private static final Pattern PATTERN_D = Pattern.compile("^[NSEWnsew] ([0-9\\.]+)");
	private static final Pattern patternDM = Pattern.compile("^[NSEWnsew] ([0-9]+)[\u00B0]? ([0-9\\.]+)'?");
	private static final Pattern patternDMS = Pattern.compile("^[NSEWnsew] ([0-9]+)[\u00B0]? ([0-9]+)'? ([0-9\\.]+)\"?");

	
	/**
	 * Retrieve all collections sorted by collection ID.
	 * 
	 * @param data SQLData object
	 * @return Collection object with all collections.
	 * @throws DataException
	 */
	public static SQLCollection collections(SQLData data) throws DataException {
		SQLCollection myCols = new SQLCollection(data);
		myCols.loadAll();
		return myCols;
	}
	
	/**
	 * Retrieve a set of collections located in the specified bounding box.
	 * 
	 * @param data SQLData object
	 * @param minLat Mininum latitude
	 * @param maxLat Maximum latitude
	 * @param minLong Minimum longitude
	 * @param maxLong Maximum longitude
	 * @return Collection object with the selected collections
	 * @throws DataException
	 */
	public static SQLCollection collectionsLoacted(SQLData data, float minLat, float maxLat, float minLong, float maxLong) throws DataException {
		SQLCollection myCols = new SQLCollection(data);
		myCols.loadByLoc(minLat, maxLat, minLong, maxLong);
		return myCols;
	}
	
	/**
	 * Retrieve all collections sorted by specified column
	 * 
	 * @param data SQLData object
	 * @param column Column to sort by (use statics)
	 * @param sortDirection Direction for sort @see SQLObject.ASCENDING_SORT and SQLObject.DESCENDING_SORT
	 * @return Collection object with all collections.
	 * @throws DataException
	 */
	public static SQLCollection collections(SQLData data, String column, String sortDirection) throws DataException {
		SQLCollection myCols = new SQLCollection(data);
		myCols.loadAll(column, sortDirection);
		return myCols;
	}
	
	/**
	 * Retrieve a set of collections sorted by specified column.  LIKE conditions are combined using an OR operation.
	 * 
	 * @param data SQLData object
	 * @param likeColumns columns for LIKE conditions (use statics).
	 * @param likeValues values for LIKE conditions.
	 * @param sortColumn Column to sort by (use statics).
	 * @param sortDirection Direction for sort, either {@code SQLObject.ASCENDING_SORT} or {@code SQLObject.DESCENDING_SORT}.
	 * @return Collection object with the specified collections.
	 * @throws DataException
	 */
	public static SQLCollection collectionsLike(SQLData data, String[] likeColumns, String[] likeValues, String sortColumn, String sortDirection) throws DataException {
		SQLCollection newObj = new SQLCollection(data);
		newObj.loadLike(SQL_BASE, likeColumns, likeValues, sortColumn, sortDirection);
		return newObj;
	}

	public static SQLCollection load(SQLData data, String collectionID) throws DataException {
		SQLCollection newObj = new SQLCollection(data);
		newObj.myID = collectionID;
		newObj.fetchRecord();
		return newObj;
	}
	
	public static float[] boundsLike(SQLData data, String[] likeColumns, String[] likeValues, String sortColumn, String sortDirection) throws DataException, SQLException {
		float[] bounds = { -180.0f, -90.0f, 180.0f, 90.0f};
		
		PreparedStatement aSth = data.prepareLikeStatement(SQL_BOUNDS_BASE, likeColumns, likeValues, sortColumn, sortDirection);
		if ( aSth != null ) {
			ResultSet results = aSth.executeQuery();
			if ( results.first() ) {
				for ( int i = 0; i < bounds.length; i++ ) {
					bounds[i] = results.getFloat(i + 1);
				}
			}
			results.close();
			aSth.close();
		}
		return bounds;
	}
	
	private static final String SQL_LOAD_FOR_PROJECT_UPDATED = SQL_BASE + "WHERE project_id = ? AND last_updated > ?";

	/**
	 * Load collection records for a project that have been updated since the date given.
	 * 
	 * @param myData SQLData object
	 * @param id ID of the project
	 * @param lastUpdate Date of the last update
	 * @return
	 * @throws DataException
	 */
	public static SQLCollection loadForProjectLastUpdated(SQLData myData, String projectID, Date lastUpdate) throws DataException {
		SQLCollection results = new SQLCollection(myData);
		try {
			PreparedStatement aPsth = results.myData.prepareStatement(SQL_LOAD_FOR_PROJECT_UPDATED);
			aPsth.setString(1, projectID);
			aPsth.setTimestamp(2, new Timestamp(lastUpdate.getTime()));
			results.loadUsingPreparedStatement(aPsth);
			return results;
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

	/**
	 * Load collection records for a project that have been updated since the date given, exclude records from the selected host.
	 * Used by a master server to find project updates to send to the slave instance.
	 * 
	 * @param data SQLData object
	 * @param projectID ID of the project
	 * @param lastUpdate Date of the last update
	 * @param hostID UUID of the host to ignore.
	 * @return
	 * @throws SQLException
	 * @throws DataException
	 */
	
	public static SQLCollection loadForProjectLastUpdated(SQLData data, String projectID, Date lastUpdate, String hostID) throws SQLException, DataException {
		SQLCollection results = new SQLCollection(data);
		PreparedStatement aPsth = results.myData.prepareStatement(SQL_LOAD_FOR_PROJECT_UPDATED + " AND " + REMOTE_HOST_COLUMN + " <> ?");
		aPsth.setString(1, projectID);
		aPsth.setTimestamp(2, new Timestamp(lastUpdate.getTime()));
		aPsth.setString(3, hostID);
		results.loadUsingPreparedStatement(aPsth);
		return results;
	}

	private final static String SQL_INSERT_REMOTE_PROJECT = "INSERT INTO collection(collection_id,project_id,remote_host) VALUES(?,?,?)";

	/**
	 * Create a new Strain record using update information from a remote host.
	 * 
	 * @param data SQLData object
	 * @param projectID ID of the project
	 * @param hostID ID of the remote host (UUID)
	 * @param collectionID collection ID of the original object
	 * @return
	 * @throws DataException
	 */
	
	public static SQLCollection createInProject(SQLData data, String projectID, String hostID, String collectionID) throws DataException {
		SQLCollection object = new SQLCollection(data);
		try {
			PreparedStatement aSth = object.myData.prepareStatement(SQL_INSERT_REMOTE_PROJECT);
			aSth.setString(1, collectionID);
			aSth.setString(2, projectID);
			aSth.setString(3, hostID);
			object.makeNewInProject(aSth, collectionID, projectID);
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return object;
		
	}



	protected SQLCollection(SQLData data) {
		super(data);
		this.initVals();
	}

	@Deprecated
	public SQLCollection(SQLData data, String anID) throws DataException {
		this(data);
		this.myID = anID;
		this.fetchRecord();
	}
	
	protected void initVals() {
		this.idField = "collection_id";
		this.myData.setProjectField(PROJECT_COLUMN);
		this.myData.setAccessRole(User.CULTURE_ROLE);
	}
	
	protected void fetchRecord() throws DataException {
		this.fetchRecord(SQL_LOAD_COLLECTION);
	}
	
	/**
	 * Creates a new Collection record.
	 * 
	 * @param data SQLData object
	 * @param newID ID for the new collection record.
	 * @return Collection object for the new collection;
	 * @throws DataException
	 */

	private static final String INSERT_NEW_COLLECTION_SQL = "INSERT INTO collection(collection_id) VALUES(?)";

	public static SQLCollection create(SQLData data, String newID) throws DataException {
		SQLCollection aCol = new SQLCollection(data);
		aCol.makeNewWithValue(INSERT_NEW_COLLECTION_SQL, newID);
		return aCol;
	}
	
	/**
	 * Creates a new Collection record in the specified project. 
	 * This method should be used to ensure data security of new objects.
	 * 
	 * @param data SQLData object
	 * @param newID ID for the new collection record.
	 * @param projectID ID of the project.
	 * @return Collection object for the new collection;
	 * @throws DataException
	 */
	public static SQLCollection createInProject(SQLData data, String newID, String projectID) throws DataException {
		SQLCollection aCol = new SQLCollection(data);
		aCol.makeNewInProject(SQL_INSERT_WITH_PROJECT, newID, projectID);
		return aCol;
	}
	
	public static float parseCoordinate(String value) {
		if ( value == null ) return 1000;
		float retVal = 1000;
		Matcher mD = PATTERN_D.matcher(value);
		Matcher mDM = patternDM.matcher(value);
		Matcher mDMS = patternDMS.matcher(value);
		try {
			if ( mDMS.matches() ) {
				float deg = Float.parseFloat(mDMS.group(1));
				float min = Float.parseFloat(mDMS.group(2)) / 60;
				float sec = Float.parseFloat(mDMS.group(3)) / 3600;
				retVal = deg + min + sec;
			} else if ( mDM.matches() ) {
				float deg = Float.parseFloat(mDM.group(1));
				float min = Float.parseFloat(mDM.group(2)) / 60;
				retVal = deg + min;
			} else if ( mD.matches() ) {
				retVal = Float.parseFloat(mD.group(1));
			} else if ( value.matches("-?[0-9\\.]+") ) {
				retVal = Float.parseFloat(value);
			} else {
				return retVal;
			}
		} catch (NumberFormatException e) {
			return 1000;
		}
		if ( value.matches("^[SWsw]{1} .+") )
			return (-1.0f * retVal);
		else 
			return retVal;
	}

	private static int dist2decimal(int precision, int distance) {
		float relative = Double.valueOf(Math.log10(distance / precision)).floatValue();
		if ( relative < 0 ) 
			return 0;
		return Math.round(relative);
	}
	
	public static String float2DM(float value, int precision) {
		float absValue = Math.abs(value);
		if ( precision < 1) precision = 1825;
		int sigFigs = dist2decimal(precision, 1825);
		String formatString = "%d%s %.0" + String.valueOf(sigFigs) + "f'";
		float min = (absValue * 60) % 60;
		return String.format(formatString, Float.valueOf(absValue).intValue(), DEGREE_SIGN, min);
	}
		
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.sql.Collection#getLocationName()
	 */
	public String getLocationName() throws DataException {
		return this.myData.getString(LOCATION_COLUMN);
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.sql.Collection#getLatitudeFloat()
	 */
	public Float getLatitudeFloat() throws DataException {
		float retval = this.myData.getFloat(LATITUDE_COLUMN);
		if ( this.myData.lastWasNull() ) return null;
		return new Float(retval);
	} 
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.sql.Collection#getLongitudeFloat()
	 */
	public Float getLongitudeFloat() throws DataException {
		float retval = this.myData.getFloat(LONGITUDE_COLUMN);
		if ( this.myData.lastWasNull() ) return null;
		return new Float(retval);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.sql.Collection#getLatitudeHemisphere()
	 */
	public String getLatitudeHemisphere() throws DataException {
		if ( this.myData != null ) {
			Float latFloat = this.getLatitudeFloat();
			if ( latFloat == null ) return null;
			if ( latFloat < 0 ) return "S";
			return "N";
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.sql.Collection#getLatitudeDM()
	 */
	public String getLatitudeDM() throws DataException {
		if ( this.myData != null ) {
			int precision = this.myData.getInt(PRECISION_COLUMN);
			Float latitude = this.getLatitudeFloat();
			if ( latitude == null )
				return "";
			String hemisphere = "N ";
			if ( latitude < 0 ) hemisphere = "S ";
			return hemisphere + float2DM(latitude, precision);
		}
		return "";
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.sql.Collection#getLongitudeHemisphere()
	 */
	public String getLongitudeHemisphere() throws DataException {
		if ( this.myData != null ) {
			Float longValue = this.getLongitudeFloat();
			if ( longValue == null ) return null;
			if ( longValue < 0 ) return "W";
			return "E";
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.sql.Collection#getLongitudeDM()
	 */
	public String getLongitudeDM() throws DataException {
		if ( this.myData != null ) {
			int precision = this.myData.getInt(PRECISION_COLUMN);
			Float value = this.getLongitudeFloat();
			if ( value == null )
				return "";
			String hemisphere = "E ";
			if ( value < 0 ) hemisphere = "W ";
			return hemisphere + float2DM(value, precision);
		}
		return "";
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.sql.Collection#getNotes()
	 */
	public String getNotes() throws DataException {
		return this.myData.getString(NOTES_COLUMN);
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.sql.Collection#getDate()
	 */
	public Date getDate() throws DataException {
		return this.myData.getDate(DATE_COLUMN);
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.sql.Collection#getDateString()
	 */
	public String getDateString() throws DataException {
		return this.myData.getString(DATE_COLUMN);
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.sql.Collection#getCollector()
	 */
	public String getCollector() throws DataException {
		return this.myData.getString(COLLECTOR_COLUMN);
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.sql.Collection#getPrecision()
	 */
	public Integer getPrecision() throws DataException {
		Integer retval = new Integer(this.myData.getInt(PRECISION_COLUMN));
		if ( this.myData.lastWasNull() ) return null;
		return retval;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.sql.Collection#getProjectID()
	 */
	public String getProjectID() throws DataException {
		return this.myData.getString(PROJECT_COLUMN);
	}
	
	/**
	 * Retrieve project information as a Project object.
	 *  
	 * @return Project object for associated project ID.
	 * @throws DataException
	 */
	public Project getProject() throws DataException {
		String projID = this.getProjectID();
		if ( projID != null ) {
			Project aProj = SQLProject.load(this.myData, projID);
			if ( aProj.first() )
				return aProj;
		}
		return null;
	}
	
	/**
	 * Retrieve field harvest information.
	 * 
	 * @return Harvest object for harvest or null if non-existant
	 * @throws DataException 
	 * @see Harvest
	 */
	public Harvest getHarvest() throws DataException {
		if ( this.myID != null ) {
			SQLHarvest myHarvest = new SQLHarvest(this.myData);
			try {
				PreparedStatement aSth = this.myData.prepareStatement(SQL_GET_HARVESTS);
				aSth.setString(1, this.myID);
				myHarvest.loadUsingPreparedStatement(aSth);
				if ( myHarvest.first() )
					return myHarvest;
			} catch (SQLException e) {
				throw new DataException(e);
			}
		} 
		return null;
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.sql.Collection#setProjectID(java.lang.String)
	 */
	public void setProjectID(String newValue) throws DataException {
		this.myData.setStringNullBlank(PROJECT_COLUMN, newValue);
	}
	
	/**
	 * Set project information for the collection.
	 * 
	 * @param aProject project object to associate.
	 * @throws SQLException
	 */
	public void setProject(Project aProject) throws DataException {
		if ( aProject != null )
			this.setProjectID(aProject.getID());
		else 
			this.myData.setNull(PROJECT_COLUMN);
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.sql.Collection#setDate(java.util.Date)
	 */
	public void setDate(Date newValue) throws DataException {
		this.myData.setDate(DATE_COLUMN, newValue);
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.sql.Collection#setDate(java.lang.String)
	 */
	public void setDate(String newValue) throws DataException {
		this.myData.setString(DATE_COLUMN, newValue);
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.sql.Collection#setLocationName(java.lang.String)
	 */
	public void setLocationName(String newValue) throws DataException {
		this.myData.setString(LOCATION_COLUMN, newValue);
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.sql.Collection#setLatitude(float)
	 */
	public void setLatitude(float newValue) throws DataException {
		if ( newValue <= 90 && newValue >= -90 ) 
			this.myData.setFloat(LATITUDE_COLUMN, newValue);
		else 
			this.myData.setNull(LATITUDE_COLUMN);
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.sql.Collection#setLatitude(java.lang.String)
	 */
	public void setLatitude(String newValue) throws DataException {
		this.setLatitude(parseCoordinate(newValue));
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.sql.Collection#setLongitude(float)
	 */
	public void setLongitude(float newValue) throws DataException {
		if ( newValue <= 180 && newValue >= -180) 
			this.myData.setFloat(LONGITUDE_COLUMN, newValue);
		else 
			this.myData.setNull(LONGITUDE_COLUMN);
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.sql.Collection#setLongitude(java.lang.String)
	 */
	public void setLongitude(String newValue) throws DataException {
		this.setLongitude(parseCoordinate(newValue));
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.sql.Collection#setCollector(java.lang.String)
	 */
	public void setCollector(String newValue) throws DataException {
		this.myData.setString(COLLECTOR_COLUMN, newValue);
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.sql.Collection#setNotes(java.lang.String)
	 */
	public void setNotes(String newNotes) throws DataException {
		this.myData.setString(NOTES_COLUMN, newNotes);
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.sql.Collection#setPrecision(java.lang.String)
	 */
	public void setPrecision(String newValue) throws DataException {
		BigDecimal precision = parseAmount(newValue, "m");
		try { 
			this.setPrecision(precision.intValueExact());
		} catch (ArithmeticException e) {
			throw new DataException(e);
		}
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.sql.Collection#setPrecision(int)
	 */
	public void setPrecision(int newValue) throws DataException {
		this.myData.setInt(PRECISION_COLUMN, newValue);
	}
	
	/**
	 * Create a field harvest record for this collection.
	 * 
	 * @return a new Harvest object.
	 * @throws SQLException
	 */
	public Harvest createHarvest(String strainID) throws DataException {
		Harvest aHarv = SQLHarvest.createInProject(this.myData, strainID, this.getProjectID());
		if ( aHarv.first() ) {
			aHarv.setCollectionID(this.myID);
			return aHarv;
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.sql.Collection#addNotes(java.lang.String)
	 */
	public void addNotes(String newNotes) throws DataException {
		StringBuffer curNotes = new StringBuffer(this.myData.getString(NOTES_COLUMN));
		curNotes.append(" ");
		curNotes.append(newNotes);
		this.myData.setString(NOTES_COLUMN, curNotes.toString());
	}
	
	/**
	 * Retrieve isolations associated with this collection.
	 * 
	 * @return Isolation object
	 * @throws SQLException
	 */
	
	public Isolation getIsolations() throws DataException {
		if ( this.myID != null ) {
			SQLIsolation anIso = new SQLIsolation(this.myData);
			try {
				PreparedStatement aSth = this.myData.prepareStatement(SQL_GET_ISOLATIONS);
				aSth.setString(1, this.myID);
				anIso.loadUsingPreparedStatement(aSth);
				if ( anIso.first() )
					return anIso;
			} catch (SQLException e) {
				throw new DataException(e);
			}
		}
		return null;
	}

	/**
	 * Retrieve stains derived from this collection.
	 * 
	 * @return Strain object
	 * @throws SQLException
	 */
	public Strain getStrains() throws DataException {
		if ( this.myID != null )  {
			SQLStrain myStrains = new SQLStrain(this.myData);
			try {
				PreparedStatement aSth = this.myData.prepareStatement(SQL_GET_STRAINS);
				aSth.setString(1, this.myID);
				myStrains.loadUsingPreparedStatement(aSth);
				if ( myStrains.first() )
					return myStrains;
			} catch (SQLException e) {
				throw new DataException(e);
			}
		}
		return null;
	}

	protected void loadAll() throws DataException {
		this.loadAll(ID_COLUMN, ASCENDING_SORT);
	}

	protected void loadAll(String column, String sortDirection) throws DataException {
		String sqlString = String.format(SQL_LOAD_SORTABLE, column, sortDirection);
		this.myData.loadUsingSQL(sqlString);
	}

	protected void loadByLoc(float minLat, float maxLat, float minLong, float maxLong) throws DataException {
		try {
			PreparedStatement aPsth = this.myData.prepareStatement(SQL_COLLECTIONS_LOCATED);
			aPsth.setFloat(1, minLat);
			aPsth.setFloat(2, maxLat);
			aPsth.setFloat(3, minLong);
			aPsth.setFloat(4, maxLong);
			this.myData.loadUsingPreparedStatement(aPsth);
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}
	
	/*	
	private String formatCoordinate(String colName, String format) throws SQLException {
		String retVal = "";
		float value = this.getFloat(colName);
		if ( this.lastWasNull() )
			return "";
		if ( format.matches("^H.*") ) {
			if ( colName.equals(this.LATITUDE_COLUMN) ) {
				if ( value < 0 ) retVal = "S " + retVal;
				else retVal = "N " + retVal;
			} else if ( colName.equals(this.LONGITUDE_COLUMN) ){
				if ( value < 0 ) retVal = "W " + retVal;
				else retVal = "E " + retVal;
			} else {
				retVal = "? " + retVal;
			}
		} else {
			if ( value < 0 ) retVal = "-" + retVal;
		}

		int precision = this.getInt(this.PRECISION_COLUMN);
		return this.float2DM(value, precision);
	}


	private String float2DMS(float value) {
		float absValue = Math.abs(value);
		String formatString = "%d° %d' %d\"";
		int min = Float.valueOf((absValue * 60) % 60).intValue();
		float sec = (absValue * 3600) % 3600;
		return String.format(formatString, Float.valueOf(absValue).intValue(), min, sec);
	}
	 */

	public Notebook getNotebook() throws DataException {
		String notebookID = this.myData.getString(NOTEBOOK_COLUMN);
		if ( notebookID != null ) {
			Notebook aNotebook = new SQLNotebook(this.myData, notebookID);
			return aNotebook;
		}
		return null;
	}

	public String getNotebookID() throws DataException {
		return this.myData.getString(NOTEBOOK_COLUMN);
	}

	public int getNotebookPage() throws DataException {
		return this.myData.getInt(NOTEBOOK_PAGE_COLUMN);
	}

	public void setNotebook(Notebook aNotebook) throws DataException {
		if ( aNotebook != null ) 
			this.myData.setString(NOTEBOOK_COLUMN, aNotebook.getID());
		else 
			this.myData.setNull(NOTEBOOK_COLUMN);
	}

	public void setNotebook(Notebook aNotebook, int aPage) throws DataException {
		if ( aNotebook != null ) {
			this.myData.setString(NOTEBOOK_COLUMN, aNotebook.getID());
			this.myData.setInt(NOTEBOOK_PAGE_COLUMN, aPage);
		} else {
			this.myData.setNull(NOTEBOOK_COLUMN);
			this.myData.setNull(NOTEBOOK_PAGE_COLUMN);
		}
	}

	public void setNotebookID(String anID) throws DataException {
		this.myData.setStringNullBlank(NOTEBOOK_COLUMN, anID);
	}

	public void setNotebookID(String anID, int aPage) throws DataException {
		if ( anID.length() > 0 ) {
			this.myData.setString(NOTEBOOK_COLUMN, anID);
			this.myData.setInt(NOTEBOOK_PAGE_COLUMN, aPage);
		} else {
			this.myData.setNull(NOTEBOOK_COLUMN);
			this.myData.setNull(NOTEBOOK_PAGE_COLUMN);
		}
	}

	public void setNotebookPage(int aPage) throws DataException {
		this.myData.setInt(NOTEBOOK_PAGE_COLUMN, aPage);
	}

	@Override
	public String getRemoteID() throws DataException {
		return this.getID();
	}

	@Override
	public String getRemoteHostID() throws DataException {
		return this.myData.getString(REMOTE_HOST_COLUMN);
	}	

	public void unlinkDataFile(ExternalFile aFile) throws DataException {
		this.unsetDataFile(DATA_FILE_CLASS, this.myID, aFile);
	}

	public String getDataFileClass() {
		return DATA_FILE_CLASS;
	}
	
	public ExternalFile getDataFiles() throws DataException {
		return this.getDataFiles(DATA_FILE_CLASS, this.myID);
	}

	public void linkDataFile(ExternalFile aFile, String dataType) throws DataException {
		this.setDataFile(DATA_FILE_CLASS, this.myID, dataType, aFile);
	}

	public ExternalFile getDataFilesForType(String dataType) throws DataException {
		return this.getDataFilesForType(DATA_FILE_CLASS, this.myID, dataType);
	}

	@Override
	public String addPhoto(String name, String description, String mimeType) throws DataException {
		String path = String.format("/collection/%s/%s", this.myID, name);
		this.linkDataFile(path, Strain.PHOTO_DATA_TYPE, description, mimeType);
		return path;
	}

	public ExternalFile getPhotos() throws DataException {
		return this.getDataFilesForType(Strain.PHOTO_DATA_TYPE);
	}

	@Override
	public void linkDataFile(String path, String dataType, String description, String mimeType) throws DataException {
		this.setDataFile(DATA_FILE_CLASS, this.myID, dataType, path, description, mimeType);
	}

	@Override
	public void updateDataFile(String path, String dataType, String description, String mimeType) throws DataException {
		this.setDataFile(DATA_FILE_CLASS, this.myID, dataType, path, description, mimeType);
	}

	@Override
	public void unlinkDataFile(String path) throws DataException {
		this.unsetDataFile(DATA_FILE_CLASS, this.myID, path);
	}

}
