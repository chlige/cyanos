//
//  Strain.java
//  Cyanos
//
//  Created by George Chlipala on 5/7/06.
//  Copyright 2006 Walnut Computer Services. All rights reserved.
//
package edu.uic.orjala.cyanos.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.uic.orjala.cyanos.Assay;
import edu.uic.orjala.cyanos.Collection;
import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.DataFileObject;
import edu.uic.orjala.cyanos.ExternalFile;
import edu.uic.orjala.cyanos.Harvest;
import edu.uic.orjala.cyanos.Inoc;
import edu.uic.orjala.cyanos.Isolation;
import edu.uic.orjala.cyanos.Project;
import edu.uic.orjala.cyanos.Sample;
import edu.uic.orjala.cyanos.SingleURL;
import edu.uic.orjala.cyanos.Strain;
import edu.uic.orjala.cyanos.Taxon;
import edu.uic.orjala.cyanos.User;


public class SQLStrain extends SQLObject implements Strain, DataFileObject {

	// Setup the column names here so that changing is easier.	
	public final static String ID_COLUMN = "culture_id";
	public final static String SOURCE_COLUMN = "culture_source";
	public final static String NAME_COLUMN = "name";
	public final static String GENUS_COLUMN = "genus";
	public final static String NOTES_COLUMN = "notes";
	public final static String REMOVE_DATE_COLUMN = "removed";
	public final static String REMOVE_REASON_COLUMN = "remove_reason";
	public final static String DEFAULT_MEDIA_COLUMN = "media_name";
	public final static String STATUS_COLUMN = "culture_status";
	public final static String PROJECT_COLUMN = "project_id";
	public final static String ISOLATION_COLUMN = "isolation_id";
	public final static String COLLECTION_COLUMN = "collection_id";
	public final static String DATE_COLUMN = "date";
	
	public final static String SORT_ID = "CAST(culture_id AS UNSIGNED)";
	
	public final static String REMOTE_HOST_COLUMN = "remote_host";

	public final static String[] ALL_COLUMNS = {ID_COLUMN, SOURCE_COLUMN, 
		NAME_COLUMN, GENUS_COLUMN, NOTES_COLUMN, 
		REMOVE_DATE_COLUMN, REMOVE_REASON_COLUMN, 
		DEFAULT_MEDIA_COLUMN, STATUS_COLUMN, PROJECT_COLUMN, 
		REMOTE_HOST_COLUMN,	ISOLATION_COLUMN, COLLECTION_COLUMN, DATE_COLUMN};

	private final static String GET_INOC_SQL = "SELECT DISTINCT inoculation.* FROM inoculation WHERE culture_id=? ORDER BY date";
	private final static String GET_FIELD_HARVEST_SQL = SQLHarvest.SQL_BASE + " WHERE culture_id=? AND collection_id IS NOT NULL ORDER BY date";
	private final static String GET_FIELD_COLLECTION_SQL = "SELECT DISTINCT collection.* FROM collection JOIN harvest h ON(h.collection_id = collection.collection_id) WHERE h.culture_id=? ORDER BY date";

	/*
	 * Parameter and SQL for "cleaned-up" database schema.
	 * 
	public final static String DATE_COLUMN = "added";
	private final static String GET_INOC_SQL = "SELECT DISTINCT inoculation.* FROM inoculation WHERE culture_id=? ORDER BY added";
	private final static String GET_FIELD_HARVEST_SQL = "SELECT DISTINCT harvest.* FROM harvest WHERE culture_id=? AND collection_id IS NOT NULL ORDER BY added";
	private final static String GET_FIELD_COLLECTION_SQL = "SELECT DISTINCT collection.* FROM collection JOIN harvest h ON(h.collection_id = collection.collection_id) WHERE h.culture_id=? ORDER BY added";
	 */
	final static String SQL_BASE = sqlBase("species", ALL_COLUMNS);
	
	private final static String INSERT_STRAIN_SQL = "INSERT INTO species(culture_id) VALUES(?)";
	private static final String SQL_INSERT_WITH_PROJECT = "INSERT INTO species(culture_id,project_id) VALUES(?,?)";
	private final static String GET_SAMPLE_SQL = "SELECT DISTINCT sample.* FROM sample WHERE culture_id=?";
	private final static String SQL_LOAD = SQL_BASE + " WHERE culture_id=?";
	private static final String SQL_LOAD_SORTABLE = SQL_BASE + " ORDER BY %s %s";
	private static final String SQL_LOAD_LIKE = SQL_BASE;
	private static final String SQL_LOAD_LIKE_TAXA = SQL_BASE + " JOIN taxon_paths tp ON (species.genus = tp.child) JOIN taxon t ON (tp.parent = t.name) WHERE t.level = ? AND (";
	
	private static final String SQL_LOAD_FOR_TAXA = SQL_BASE + " JOIN taxon_paths tp ON (species.genus = tp.child) WHERE tp.parent = ? ORDER BY culture_id ASC";

	private static final String SQL_LOAD_BY_TAXA = SQL_BASE + "LEFT OUTER JOIN taxon t ON (species.genus = t.name) ORDER BY %s %s";
	private static final String SELECT_URLS_SQL = String.format("SELECT data.* FROM data WHERE data.id = ? AND data.tab = '%s' AND data.type = '%s' ORDER BY data.file", DATA_FILE_CLASS, URL_DATA_TYPE);

	private static final String SQL_LOAD_WITH_PHOTOS = sqlBase("species", ALL_COLUMNS, true) + "JOIN data ON(species.culture_id = data.id) WHERE data.tab = ? AND data.type = ? ORDER BY " + SORT_ID + " ASC";

	private static final String SQL_LOAD_FOR_PROJECT_UPDATED = SQL_BASE + "WHERE project_id = ? AND last_updated > ?";
	
	/**
	 * Retrieve all strains sorted by strain ID.
	 * 
	 * @param data SQLData object
	 * @return Strain object with all strains.
	 * @throws DataException
	 */
	public static SQLStrain strains(SQLData data) throws DataException {
		SQLStrain newObj = new SQLStrain(data);
		newObj.loadAll(SQL_LOAD_SORTABLE, ID_COLUMN, ASCENDING_SORT);
		return newObj;
	}
	
	/**
	 * Retrieve all strains sorted by specified column
	 * 
	 * @param data SQLData object
	 * @param column Column to sort by (use statics)
	 * @param sortDirection Direction for sort, either {@code SQLObject.ASCENDING_SORT} or {@code SQLObject.DESCENDING_SORT}
	 * @return Strain object with all strains.
	 * @throws DataException
	 */
	public static SQLStrain strains(SQLData data, String column, String sortDirection) throws DataException {
		SQLStrain newObj = new SQLStrain(data);
		newObj.loadAll(SQL_LOAD_SORTABLE, column, sortDirection);
		return newObj;
	}
	
	/**
	 * Retrieve a set of strains sorted by specified column.  LIKE conditions are combined using an OR operation.
	 * 
	 * @param data SQLData object
	 * @param likeColumns columns for LIKE conditions (use statics).
	 * @param likeValues values for LIKE conditions.
	 * @param sortColumn Column to sort by (use statics).
	 * @param sortDirection Direction for sort, either {@code SQLObject.ASCENDING_SORT} or {@code SQLObject.DESCENDING_SORT}.
	 * @return Strain object with all strains.
	 * @throws DataException
	 */
	public static SQLStrain strainsLike(SQLData data, String[] likeColumns, String[] likeValues, String sortColumn, String sortDirection) throws DataException {
		SQLStrain newObj = new SQLStrain(data);
		newObj.loadLike(SQL_LOAD_LIKE, likeColumns, likeValues, sortColumn, sortDirection);
		return newObj;
	}
	
	/**
	 * Retrieve a set of strains sorted by specified column.  LIKE conditions are combined using an OR operation.
	 * 
	 * @param data SQLData object
	 * @param likeColumns columns for LIKE conditions (use statics).
	 * @param likeValues values for LIKE conditions.
	 * @param sortTaxon Taxonomic column to sort by, one of @{code SQLTaxon.LEVEL_KINGDOM}, @{code SQLTaxon.LEVEL_PHYLUM}, @{code SQLTaxon.LEVEL_CLASS}, 
	 * 	@{code SQLTaxon.LEVEL_ORDER}, @{code SQLTaxon.LEVEL_FAMILY} or @{code SQLTaxon.LEVEL_GENUS}.
	 * @param sortDirection Direction for sort, either {@code SQLObject.ASCENDING_SORT} or {@code SQLObject.DESCENDING_SORT}.
	 * @return Strain object with all strains.
	 * @throws DataException
	 */
	public static SQLStrain strainsLikeByTaxa(SQLData data, String[] likeColumns, String[] likeValues, String sortTaxon, String sortDirection) throws DataException {
		SQLStrain newObj = new SQLStrain(data);
		if ( likeColumns.length != likeValues.length ) return newObj;
		
		String likeQuery = String.format("%s LIKE ?", likeColumns[0]);
		
		for ( int i = 1; i < likeColumns.length; i++) {
			likeQuery = likeQuery.concat(String.format(" OR %s LIKE ?", likeColumns[i]));
		}
		
		StringBuffer sqlString = new StringBuffer(SQL_LOAD_LIKE_TAXA);
		sqlString.append(likeQuery);
		sqlString.append(") ORDER BY t.name ");
		sqlString.append(sortDirection);

		try {
			PreparedStatement aSth = newObj.myData.prepareStatement(sqlString.toString());
			aSth.setString(1, sortTaxon);
			for ( int i = 1; i < likeValues.length; i++) {
				aSth.setString(i + 1, likeValues[i]);
			}
			newObj.loadUsingPreparedStatement(aSth);
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return newObj;
	}
	
	protected void loadTaxaLike(String sqlTemplate, String[] likeColumns, String[] likeValues, String sortColumn, String sortDirection ) throws DataException {	
		if ( likeColumns.length != likeValues.length ) return;
		
		String likeQuery = String.format("%s LIKE ?", likeColumns[0]);
		
		for ( int i = 1; i < likeColumns.length; i++) {
			likeQuery = likeQuery.concat(String.format(" OR %s LIKE ?", likeColumns[i]));
		}
		
		StringBuffer sqlString = new StringBuffer(sqlTemplate);
		sqlString.append(likeQuery);
		sqlString.append(" ORDER BY t.name ");
		sqlString.append(sortDirection);

		try {
			PreparedStatement aSth = this.myData.prepareStatement(sqlString.toString());
			aSth.setString(1, sortColumn);
			for ( int i = 1; i < likeValues.length; i++) {
				aSth.setString(i + 1, likeValues[i]);
			}
			this.loadUsingPreparedStatement(aSth);
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

	/**
	 * Retrieve all strains sorted by specified taxon.
	 * 
	 * @param data SQLData object
	 * @param sortTaxon Taxonomic column to sort by, one of @{code SQLTaxon.LEVEL_KINGDOM}, @{code SQLTaxon.LEVEL_PHYLUM}, @{code SQLTaxon.LEVEL_CLASS}, 
	 * 	@{code SQLTaxon.LEVEL_ORDER}, @{code SQLTaxon.LEVEL_FAMILY} or @{code SQLTaxon.LEVEL_GENUS}.
	 * @param sortDirection Direction for sort, either {@code SQLStrain.ASCENDING_SORT} or {@code SQLStrain.DESCENDING_SORT}
	 * @return Strain object with all strains.
	 * @throws DataException
	 */
	public static SQLStrain strainsByTaxa(SQLData data, String sortTaxon, String sortDirection) throws DataException {
		SQLStrain newObj = new SQLStrain(data);
		newObj.loadAll(SQL_LOAD_BY_TAXA, sortTaxon, sortDirection);
		return newObj;
	}
	
	/**
	 * Create a new strain record.
	 * 
	 * @param data SQLData object
	 * @param newID ID of the new strain
	 * @return Strain object for the new strain.
	 * @throws DataException
	 */
	public static SQLStrain create(SQLData data, String newID) throws DataException {
		SQLStrain aStrain = new SQLStrain(data);
		aStrain.makeNewWithValue(INSERT_STRAIN_SQL, newID);
		return aStrain;
	}
	
	/**
	 * Creates a new strain record in the specified project. 
	 * This method should be used to ensure data security of new objects.
	 * 
	 * @param data SQLData object
	 * @param newID ID for the new strain.
	 * @param projectID ID of the project.
	 * @return Strain object for the new strain.
	 * @throws DataException
	 */
	public static SQLStrain createInProject(SQLData data, String newID, String projectID) throws DataException {
		SQLStrain aStrain = new SQLStrain(data);
		aStrain.makeNewInProject(SQL_INSERT_WITH_PROJECT, newID, projectID);
		return aStrain;
	}
	
	/**
	 * Load strain records for a project that have been updated since the date given.
	 * 
	 * @param data SQLData object
	 * @param projectID ID of the project
	 * @param lastUpdate Date of the last update
	 * @return
	 * @throws SQLException
	 * @throws DataException
	 */
	public static SQLStrain loadForProjectLastUpdated(SQLData data, String projectID, Date lastUpdate) throws SQLException, DataException {
		SQLStrain results = new SQLStrain(data);
		PreparedStatement aPsth = results.myData.prepareStatement(SQL_LOAD_FOR_PROJECT_UPDATED);
		aPsth.setString(1, projectID);
		aPsth.setTimestamp(2, new Timestamp(lastUpdate.getTime()));
		results.loadUsingPreparedStatement(aPsth);
		return results;
	}
	
	public static SQLStrain loadWithPhotos(SQLData data) throws SQLException, DataException {
		SQLStrain results = new SQLStrain(data);
		PreparedStatement aPsth = results.myData.prepareStatement(SQL_LOAD_WITH_PHOTOS);
		aPsth.setString(1, DATA_FILE_CLASS);
		aPsth.setString(2, PHOTO_DATA_TYPE);
		results.loadUsingPreparedStatement(aPsth);
		return results;
	}
	
	/**
	 * Load strain records for a project that have been update since the date given, exclude records from the selected host.
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
	public static SQLStrain loadForProjectLastUpdated(SQLData data, String projectID, Date lastUpdate, String hostID) throws SQLException, DataException {
		SQLStrain results = new SQLStrain(data);
		PreparedStatement aPsth = results.myData.prepareStatement(SQL_LOAD_FOR_PROJECT_UPDATED + " AND " + REMOTE_HOST_COLUMN + " <> ?");
		aPsth.setString(1, projectID);
		aPsth.setTimestamp(2, new Timestamp(lastUpdate.getTime()));
		aPsth.setString(3, hostID);
		results.loadUsingPreparedStatement(aPsth);
		return results;
	}

	private final static String SQL_INSERT_REMOTE_PROJECT = "INSERT INTO species(culture_id,project_id,remote_host) VALUES(?,?,?)";

	/**
	 * Create a new Strain record using update information from a remote host.
	 * 
	 * @param data SQLData object
	 * @param projectID ID of the project
	 * @param hostID ID of the remote host (UUID)
	 * @param cultureID culture ID of the original object (stored but not used a ID on master system)
	 * @return
	 * @throws DataException
	 */
	
	public static SQLStrain createInProject(SQLData data, String projectID, String hostID, String cultureID) throws DataException {
		SQLStrain object = new SQLStrain(data);
		try {
			PreparedStatement aSth = object.myData.prepareStatement(SQL_INSERT_REMOTE_PROJECT);
			aSth.setString(1, cultureID);
			aSth.setString(2, projectID);
			aSth.setString(3, hostID);
			object.makeNewInProject(aSth, cultureID, projectID);
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return object;
		
	}


	
	/**
	 * Get the number of valid strains (strains not removed) in the database.
	 * 
	 * @param data SQLData object
	 * @return number of strains in the database.
	 * @throws DataException
	 */
	public static int numberOfStrains(SQLData data) throws DataException {
		try {
			PreparedStatement sth = data.prepareStatement("SELECT COUNT(DISTINCT culture_id) FROM species WHERE removed IS NULL");
			ResultSet results = sth.executeQuery();
			results.last();
			int retVal = results.getInt(1);
			results.close();
			sth.close();
			return retVal;
		} catch (SQLException e ) {
			throw new DataException(e);
		}
	}
	
	private static final String SQL_TAXA_NUMBER = "SELECT COUNT(DISTINCT t.name) FROM species s JOIN taxon_paths tp ON (s.genus = tp.child) JOIN taxon t ON (tp.parent = t.name) WHERE t.level = ? AND removed IS NULL";
	
	/**
	 * Returns the number of taxa represented be the strains in the database.
	 * 
	 * @param data SQLData object
	 * @param level Taxonomic level, e.g. kingdom, class, order, etc.  
	 * @return
	 * @throws DataException
	 */
	public static int numberOfTaxa(SQLData data, String level) throws DataException {
		try {
			PreparedStatement sth = data.prepareStatement(SQL_TAXA_NUMBER);
			sth.setString(1, level);
			ResultSet results = sth.executeQuery();
			results.last();
			int retVal = results.getInt(1);
			results.close();
			sth.close();
			return retVal;
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

	private static final String SQL_TAXA = "SELECT DISTINCT t.name FROM species s JOIN taxon_paths tp ON (s.genus = tp.child) JOIN taxon t ON (tp.parent = t.name) WHERE t.level = ? AND removed IS NULL ORDER BY t.name";

	/**
	 * Returns a list of taxonomic names that are represented by strains in the database.
	 * 
	 * @param data SQLData object
	 * @param level
	 * @return
	 * @throws DataException
	 */
	public static List<String> validTaxa(SQLData data, String level) throws DataException {
		List<String> retVal = new ArrayList<String>();
		try {
			PreparedStatement sth = data.prepareStatement(SQL_TAXA);
			sth.setString(1, level);
			ResultSet results = sth.executeQuery();
			results.beforeFirst();
			while ( results.next() ) {
				retVal.add(results.getString(1));
			}
			results.close();
			sth.close();
			return retVal;
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}
	
	private static final String SQL_TAXA_COUNT = "SELECT t.name, COUNT(s.culture_id) FROM species s JOIN taxon_paths tp ON (s.genus = tp.child) JOIN taxon t ON (tp.parent = t.name) WHERE t.level = ? AND removed IS NULL GROUP BY t.name ORDER BY t.name";
	
	public static Map<String, Integer> countForTaxa(SQLData data, String level) throws DataException {
		Map<String, Integer> retVal = new HashMap<String, Integer>();
		try {
			PreparedStatement sth = data.prepareStatement(SQL_TAXA_COUNT);
			sth.setString(1, level);
			ResultSet results = sth.executeQuery();
			results.beforeFirst();
			while ( results.next() ) {
				retVal.put(results.getString(1), results.getInt(2));
			}
			results.close();
			sth.close();
			return retVal;
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

	public static SQLStrain load(SQLData data, String cultureID) throws DataException {
		SQLStrain object = new SQLStrain(data);
		object.myID = cultureID;
		object.fetchRecord(SQL_LOAD);
		return object;
	}
	
	protected SQLStrain(SQLData data) {
		super(data);
		this.initVals();
	}
	
	@Deprecated
	public SQLStrain(SQLData data, String anID) throws DataException {
		this(data);
		this.myID = anID;
		this.fetchRecord();
	}
	
	protected void initVals() {
		this.idField = ID_COLUMN;
		this.myData.setProjectField(PROJECT_COLUMN);
		this.myData.setAccessRole(User.CULTURE_ROLE);
	}
	
	protected void fetchRecord() throws DataException {
		this.fetchRecord(SQL_LOAD);
	}
		
	public String getCultureSource() throws DataException {
		return this.myData.getString(SOURCE_COLUMN);
	}
	
	public String getGenus() throws DataException {
		return this.myData.getQUIETString(GENUS_COLUMN);
	}
	
	public String getDefaultMedia() throws DataException {
		return this.myData.getString(DEFAULT_MEDIA_COLUMN);
	}
	
	public String getNotes() throws DataException {
		return this.myData.getString(NOTES_COLUMN);
	}
	
	public String getName() throws DataException {
		return this.myData.getQUIETString(NAME_COLUMN);
	}
	
	public Date getDate() throws DataException {
		return this.myData.getDate(DATE_COLUMN);
	}
	
	public String getDateString() throws DataException {
		return this.myData.getString(DATE_COLUMN);
	}
	
	public Date getRemovedDate() throws DataException {
		return this.myData.getDate(REMOVE_DATE_COLUMN);
	}
	
	public String getRemovedDateString() throws DataException {
		return this.myData.getString(REMOVE_DATE_COLUMN);
	}

	public String getRemoveReason() throws DataException {
		return this.myData.getString(REMOVE_REASON_COLUMN);
	}
	
	public String getStatus() throws DataException {
		return this.myData.getString(STATUS_COLUMN);
	}
	
	public String getProjectID() throws DataException {
/*		String retVal = this.myData.getString(PROJECT_COLUMN);
		if ( retVal == null ) {
			Isolation myIso = this.getSourceIsolation();
			if ( myIso != null ) retVal = myIso.getProjectID();
		} */
		return this.myData.getString(PROJECT_COLUMN);
	}
	
	public Project getProject() throws DataException {
		String projID = this.getProjectID();
		if ( projID != null ) {
			Project aProj = SQLProject.load(this.myData, projID);
			if ( aProj.first() )
				return aProj;
		}
		return null;
	}

	public void setCultureSource(String newSource) throws DataException {
		this.myData.setString(SOURCE_COLUMN, newSource);
	}
	
	public void setName(String newName) throws DataException {
		this.myData.setString(NAME_COLUMN, newName);
	}
	
	public void setGenus(String newName) throws DataException {
		this.myData.setString(GENUS_COLUMN, newName);
	}
	
	public void setDefaultMedia(String newMedia) throws DataException {
		this.myData.setString(DEFAULT_MEDIA_COLUMN, newMedia);
	}
	
	public void setDate(Date newValue) throws DataException {
		this.myData.setDate(DATE_COLUMN, newValue);
	}
	
	public void setDate(String newValue) throws DataException {
		this.myData.setString(DATE_COLUMN, newValue);
	}
	
	public void setRemoveDate(Date newValue) throws DataException {
		this.myData.setDate(REMOVE_DATE_COLUMN, newValue);
	}
	
	public void setRemoveDate(String newValue) throws DataException {
		this.myData.setString(REMOVE_DATE_COLUMN, newValue);
	}
	
	public void setRemoveReason(String newValue) throws DataException {
		this.myData.setString(REMOVE_REASON_COLUMN, newValue);
	}
	
	public void setNotes(String newNotes) throws DataException {
		this.myData.setString(NOTES_COLUMN, newNotes);
	}
	
	public void setStatus(String newStatus) throws DataException {
		this.myData.setString(STATUS_COLUMN, newStatus);
	}
	
	public void setProjectID(String newValue) throws DataException {
		this.myData.setStringNullBlank(PROJECT_COLUMN, newValue);
	}
	
	public void setProject(Project aProject) throws DataException {
		if ( aProject != null )
			this.setProjectID(aProject.getID());
		else 
			this.myData.setNull(PROJECT_COLUMN);
	}
	
	public void addNotes(String newNotes) throws DataException {
		StringBuffer curNotes = new StringBuffer(this.myData.getString(NOTES_COLUMN));
		curNotes.append(" ");
		curNotes.append(newNotes);
		this.myData.setString(NOTES_COLUMN, curNotes.toString());
	}
	
	public Inoc getInoculations() throws DataException {
		try {
			SQLInoc myInocs = new SQLInoc(this.myData);
			PreparedStatement aPsth = this.myData.prepareStatement(GET_INOC_SQL);
			aPsth.setString(1, this.myID);
			myInocs.loadUsingPreparedStatement(aPsth);
			if ( myInocs.first() )
				return myInocs;
			return null;
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}
	
	public Harvest getFieldHarvests() throws DataException {
		try {
			SQLHarvest aHarv = new SQLHarvest(this.myData);
			PreparedStatement aPsth = this.myData.prepareStatement(GET_FIELD_HARVEST_SQL);
			aPsth.setString(1, this.myID);
			aHarv.loadUsingPreparedStatement(aPsth);
			if ( aHarv.first() )
				return aHarv;
			return null;
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}
	
	public Collection getFieldCollections() throws DataException {
		try {
			SQLCollection myCols = new SQLCollection(this.myData);
			PreparedStatement aPsth = this.myData.prepareStatement(GET_FIELD_COLLECTION_SQL);
			aPsth.setString(1, this.myID);
			myCols.loadUsingPreparedStatement(aPsth);
			if ( myCols.first())
				return myCols;
			return null;
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}
	
	public Sample getSamples() throws DataException {
		try {
		SQLSample mySamples = new SQLSample(this.myData);
		PreparedStatement aPsth = this.myData.prepareStatement(GET_SAMPLE_SQL);
		aPsth.setString(1, this.myID);
		mySamples.loadUsingPreparedStatement(aPsth);
		if ( mySamples.first() )
			return mySamples;
		return null;
		} catch (SQLException e) {
			throw new DataException(e);
		}

	}
	
	public Assay getAssays() throws DataException {
		return SQLAssay.assaysForStrainID(this.myData, this.myID);
	}
	
	public boolean wasRemoved() throws DataException {
		if ( this.myData.getString(REMOVE_DATE_COLUMN) != null) 
			return true;
		return false;
	}
	
	public boolean isActive() throws DataException {
		if ( this.myData.getString(REMOVE_DATE_COLUMN) == null )
			return true;
		return false;
	}
	
	public Taxon getTaxon() throws DataException {
		String myGenus = this.getGenus();
		if ( myGenus != null ) {
			Taxon myTaxon = SQLTaxon.load(myData, myGenus);
			return myTaxon;
		}
		return null;
	}
		
	public String[] dataTypes() {
		String[] retVals = { Strain.PHOTO_DATA_TYPE };
		return retVals;
	}

	protected void loadAll(String sqlTemplate, String column, String sortDirection) throws DataException {
		String sqlString = String.format(sqlTemplate, column, sortDirection);
		this.myData.loadUsingSQL(sqlString);
	}

	public boolean statusIs(String status) throws DataException {
		String myStatus = this.getStatus();
		return ( myStatus != null && myStatus.equals(status));
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

	public void addPhoto(ExternalFile aFile) throws DataException {
		this.linkDataFile(aFile, Strain.PHOTO_DATA_TYPE);
	}

	public ExternalFile getPhotos() throws DataException {
		return this.getDataFilesForType(Strain.PHOTO_DATA_TYPE);
	}

	public ExternalFile getURLs() throws DataException {
		SQLExternalURL someFiles = new SQLExternalURL(this.myData);
		try {
			PreparedStatement aPsth = this.myData.prepareStatement(SELECT_URLS_SQL);
			aPsth.setString(1, this.myID);
			someFiles.loadUsingPreparedStatement(aPsth);
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return someFiles;
	}

	public void addURL(String url, String description) throws DataException {
		SingleURL aURL = new SingleURL(url);
		aURL.setDescription(description);
		this.linkDataFile(aURL, URL_DATA_TYPE);
	}

	public void unlinkDataFile(ExternalFile aFile) throws DataException {
		this.unsetDataFile(DATA_FILE_CLASS, this.myID, aFile);
	}

	public Isolation getSourceIsolation() throws DataException {
		String anID = this.getSourceIsolationID();
		if ( anID != null ) {
			Isolation myIso = new SQLIsolation(this.myData, anID);
			if ( myIso.first() ) return myIso;
		}
		return null;
	}
	
	
	public Collection getSourceCollection() throws DataException {
		String anID = this.getSourceCollectionID();
		if ( anID != null ) {
			Collection myColl = SQLCollection.load(this.myData, anID);
			if ( myColl.first() ) return myColl;
		}
		return null;
	}

	
	public String getSourceIsolationID() throws DataException {
		return this.myData.getString(ISOLATION_COLUMN);
	}

	
	public String getSourceCollectionID() throws DataException {
		return this.myData.getString(COLLECTION_COLUMN);
	}

	
	public void setSourceIsolation(Isolation isolation) throws DataException {
		this.setSourceIsolationID(isolation.getID());
		this.setSourceCollectionID(isolation.getCollectionID());
	}

	
	public void setSourceCollection(Collection collection) throws DataException {
		this.setSourceCollectionID(collection.getID());
	}

	
	public void setSourceIsolationID(String isolationID) throws DataException {
		this.myData.setStringNullBlank(ISOLATION_COLUMN, isolationID);
	}

	
	public void setSourceCollectionID(String collectionID) throws DataException {
		this.myData.setStringNullBlank(COLLECTION_COLUMN, collectionID);
	}

	@Override
	public String getRemoteID() throws DataException {
		return this.getID();
	}

	@Override
	public String getRemoteHostID() throws DataException {
		return this.myData.getString(REMOTE_HOST_COLUMN);
	}

	@Override
	public String getTaxonName(String level) throws DataException {
		return SQLTaxon.nameForLevel(myData, this.getGenus(), level);
	}

	@Override
	public void linkDataFile(String path, String dataType, String description,
			String mimeType) throws DataException {
		this.setDataFile(DATA_FILE_CLASS, this.myID, dataType, path, description, mimeType);
	}

	@Override
	public void updateDataFile(String path, String dataType,
			String description, String mimeType) throws DataException {
		this.setDataFile(DATA_FILE_CLASS, this.myID, dataType, path, description, mimeType);
	}

	@Override
	public void unlinkDataFile(String path) throws DataException {
		this.unsetDataFile(DATA_FILE_CLASS, this.myID, path);
	}

}