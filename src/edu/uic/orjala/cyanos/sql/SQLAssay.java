package edu.uic.orjala.cyanos.sql;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.uic.orjala.cyanos.Assay;
import edu.uic.orjala.cyanos.AssayData;
import edu.uic.orjala.cyanos.AssayPlate;
import edu.uic.orjala.cyanos.BasicObject;
import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.ExternalFile;
import edu.uic.orjala.cyanos.Material;
import edu.uic.orjala.cyanos.Notebook;
import edu.uic.orjala.cyanos.Project;
import edu.uic.orjala.cyanos.Role;
import edu.uic.orjala.cyanos.Sample;
import edu.uic.orjala.cyanos.Strain;
import edu.uic.orjala.cyanos.User;

/**
 * Class to manipulate assay objects
 * 
 * @author George Chlipala
 * @version 1.1
 *
 */

public class SQLAssay extends SQLBoxObject implements Assay {

	// Setup the column names here so that changing is easier.	
	public final static String ID_COLUMN = "assay_id";
	public final static String TARGET_COLUMN = "target";
	public final static String NAME_COLUMN = "name";
	public final static String UNIT_COLUMN = "unit";
	public final static String ACTIVE_LEVEL_COLUMN = "active_level";
	public final static String ACTIVE_OPERATOR_COLUMN = "active_op";
	public final static String LENGTH_COLUMN = "length";
	public final static String WIDTH_COLUMN = "width";
	public final static String NOTES_COLUMN = "notes";
	public final static String PROJECT_COLUMN = "project_id";
	public final static String NOTEBOOK_COLUMN = "notebook_id";
	public final static String NOTEBOOK_PAGE_COLUMN = "notebook_page";
	
	public final static String REMOTE_HOST_COLUMN = "remote_host";

	public final static String SIG_FIGS_COLUMN = "sig_figs";

	public final static String DATE_COLUMN = "date";
	
	private final static String[] ALL_COLUMNS = { ID_COLUMN, TARGET_COLUMN, NAME_COLUMN, 
			UNIT_COLUMN, ACTIVE_LEVEL_COLUMN, ACTIVE_OPERATOR_COLUMN, 
			LENGTH_COLUMN, WIDTH_COLUMN, NOTES_COLUMN, SIG_FIGS_COLUMN,			
			// NOTEBOOK_COLUMN, NOTEBOOK_PAGE_COLUMN, 
			PROJECT_COLUMN, DATE_COLUMN,
			REMOTE_HOST_COLUMN
	};
	
	private final static String DATA_FILE_TABLE = "assay";
	
	private final static String SQL_BASE = sqlBase("assay_info", ALL_COLUMNS);

	private final static String SQL_INSERT_ASSAY = "INSERT INTO assay_info(assay_id,date) VALUES(?,CURRENT_DATE)";
	private final static String SQL_INSERT_WITH_PROJECT = "INSERT INTO assay_info(assay_id,project_id,date) VALUES(?,?,CURRENT_DATE)";

	private final static String SQL_LOAD_ASSAY = SQL_BASE + " WHERE assay_id=?";
	private final static String SQL_LOAD_FOR_TARGET =  SQL_BASE + " WHERE target=? ORDER BY assay_id";
	private final static String SQL_LOAD_FOR_SAMPLE =  SQL_BASE + " JOIN assay a ON (assay_info.assay_id = a.assay_id) WHERE a.sample_id=? ORDER BY a.assay_id";
	private final static String SQL_LOAD_FOR_MATERIAL =  SQL_BASE + " JOIN assay a ON (assay_info.assay_id = a.assay_id) WHERE a.material_id=? ORDER BY a.assay_id";
	private final static String SQL_LOAD_FOR_STRAIN =  SQL_BASE + " JOIN assay a ON (assay_info.assay_id = a.assay_id) WHERE a.culture_id=? ORDER BY a.assay_id";
	private final static String SQL_LOAD_FOR_PROJECT =  SQL_BASE + " WHERE project_id=? ORDER BY assay_id";	

	private static final String SQL_LOAD_ALL =  SQL_BASE + " ORDER BY assay_id";
	private static final String SQL_LOAD_TARGETS =  "SELECT DISTINCT target FROM assay_info ORDER BY target";
		
	private static final String SQL_CLEAR_DATA = "DELETE FROM assay WHERE assay_id = ?";

	
//	private static final String SQL_SELECT_ACTIVE_SUBSET = "SELECT assay.* FROM assay WHERE assay_id=? AND isActive(activity + sign, ?, ?) > 0";
//	private static final String SQL_SELECT_DATA_SORTED = "SELECT assay.* FROM assay WHERE assay_id=? ORDER BY %s %s";

	/*
	 *  Parameter fields and SQL for "improve database layout"

	public final static String DATE_COLUMN = "added";
	
	private final static String SQL_INSERT_ASSAY = "INSERT INTO assay(assay_id,added) VALUES(?,CURRENT_DATE)";
	private final static String SQL_INSERT_WITH_PROJECT = "INSERT INTO assay(assay_id,project_id,added) VALUES(?,?,CURRENT_DATE)";
	private final static String SQL_LOAD_ASSAY = "SELECT assay.* FROM assay WHERE assay_id=?";
	private final static String SQL_LOAD_FOR_TARGET = "SELECT assay.* FROM assay WHERE target=? ORDER BY assay_id";
	private final static String SQL_LOAD_FOR_SAMPLE = "SELECT DISTINCT assay.* FROM assay JOIN assay_data a ON (assay.assay_id = a.assay_id) JOIN sample s ON ( a.sample_id = s.sample_id) WHERE a.sample_id=? OR s.source_id=? ORDER BY assay_id";
	private final static String SQL_LOAD_FOR_STRAIN = "SELECT DISTINCT assay.* FROM assay JOIN assay_data a ON (assay.assay_id = a.assay_id) WHERE a.culture_id=? ORDER BY assay_id";
	private final static String SQL_LOAD_FOR_PROJECT = "SELECT DISTINCT assay.* FROM assay WHERE project_id=? ORDER BY assay_id";	

	private static final String SQL_LOAD_ALL = "SELECT assay.* FROM assay ORDER BY assay_id";
	
	*/
	
	public static SQLAssay assays(SQLData aDatasource) throws DataException {
		SQLAssay myAssays = new SQLAssay(aDatasource);
		myAssays.myData.loadUsingSQL(SQL_LOAD_ALL);
		return myAssays;
	}
	
	public static List<String> targets(SQLData data) throws DataException {
		SQLData dataCopy = data.duplicate();
		dataCopy.loadUsingSQL(SQL_LOAD_TARGETS);
		List<String> retval = new ArrayList<String>();
		dataCopy.beforeFirst();
		while ( dataCopy.next() ) {
			retval.add(dataCopy.getString(TARGET_COLUMN));
		}
		return retval;
	}
	
	
	/**
	 * Retrieve a set of targets sorted by specified column.  LIKE conditions are combined using an OR operation.
	 * 
	 * @param data SQLData object
	 * @param likeColumns columns for LIKE conditions (use statics).
	 * @param likeValues values for LIKE conditions.
	 * @param sortColumn Column to sort by (use statics).
	 * @param sortDirection Direction for sort, either {@code SQLObject.ASCENDING_SORT} or {@code SQLObject.DESCENDING_SORT}.
	 * @return List<String> of targets.
	 * @throws DataException
	 */
	public static List<String> targetsLike(SQLData data, String[] likeColumns, String[] likeValues) throws DataException {
		SQLData dataCopy = data.duplicate();
		List<String> retval = new ArrayList<String>();		
		if ( likeColumns.length != likeValues.length ) return retval;

		String likeQuery = String.format("%s LIKE ?", likeColumns[0]);

		for ( int i = 1; i < likeColumns.length; i++) {
			likeQuery = likeQuery.concat(String.format(" OR %s LIKE ?", likeColumns[i]));
		}
		StringBuffer sqlString = new StringBuffer("SELECT DISTINCT target FROM assay_info WHERE ");
		sqlString.append(likeQuery);
		sqlString.append(" ORDER BY target");
		try {
			PreparedStatement aSth = dataCopy.prepareStatement(sqlString.toString());
			for ( int i = 0; i < likeValues.length; i++) {
				aSth.setString(i + 1, likeValues[i]);
			}
			dataCopy.loadUsingPreparedStatement(aSth);
			dataCopy.beforeFirst();
			while ( dataCopy.next() ) {
				retval.add(dataCopy.getString(TARGET_COLUMN));
			}
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return retval;
	}
	

	/**
	 * Load assays for the specified target.
	 * 
	 * @param data SQLData object
	 * @param aTarget
	 * @return tAssays
	 * @throws DataException
	 */	
	public static SQLAssay assaysForTarget(SQLData data, String aTarget) throws DataException {
		try {
			SQLAssay assays = new SQLAssay(data);
			PreparedStatement aPsth = assays.myData.prepareStatement(SQL_LOAD_FOR_TARGET);
			aPsth.setString(1, aTarget);
			assays.myData.loadUsingPreparedStatement(aPsth);
			return assays;
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}
	

	/**
	 * Load assays for the specified strain ID.
	 * 
	 * @param data SQLData object
	 * @param cultureID ID of the strain
	 * @return Assays
	 * @throws DataException
	 */
	public static SQLAssay assaysForStrainID(SQLData data, String cultureID) throws DataException {
		try {
			SQLAssay assays = new SQLAssay(data);
			PreparedStatement aPsth = assays.myData.prepareStatement(SQL_LOAD_FOR_STRAIN);
			aPsth.setString(1, cultureID);
			assays.myData.loadUsingPreparedStatement(aPsth);
			return assays;
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

	/**
	 * Load assays for the specified {@link Strain}.
	 * 
	 * @param data SQLData object
	 * @param aStrain
	 * @return Assays
	 * @throws DataException
	 */
	public static SQLAssay assaysForStrain(SQLData data, Strain aStrain) throws DataException {
		return SQLAssay.assaysForStrainID(data, aStrain.getID());
	}
	
	/**
	 * Load assays for the specified sample ID.
	 * 
	 * @param data SQLData object
	 * @param sampleID ID of the sample
	 * @return Assays
	 * @throws DataException
	 */
	public static SQLAssay assaysForSampleID(SQLData data, String sampleID) throws DataException {
		try {
			SQLAssay assays = new SQLAssay(data);
			PreparedStatement aPsth = assays.myData.prepareStatement(SQL_LOAD_FOR_SAMPLE);
			aPsth.setString(1, sampleID);
			assays.myData.loadUsingPreparedStatement(aPsth);
			return assays;
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

	/**
	 * Load assays for the specified {@link Sample}
	 * 
	 * @param data SQLData object
	 * @param aSample
	 * @return Assays
	 * @throws DataException
	 */
	public static SQLAssay assaysForSample(SQLData data, Sample aSample) throws DataException {
		return SQLAssay.assaysForSampleID(data, aSample.getID());
	}
	
	/**
	 * Load assays for the specified sample ID.
	 * 
	 * @param data SQLData object
	 * @param materialID ID of the sample
	 * @return Assays
	 * @throws DataException
	 */
	public static SQLAssay assaysForMaterialID(SQLData data, String materialID) throws DataException {
		try {
			SQLAssay assays = new SQLAssay(data);
			PreparedStatement aPsth = assays.myData.prepareStatement(SQL_LOAD_FOR_MATERIAL);
			aPsth.setString(1, materialID);
			assays.myData.loadUsingPreparedStatement(aPsth);
			return assays;
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

	/**
	 * Load assays for the specified {@link Material}
	 * 
	 * @param data SQLData object
	 * @param aMaterial
	 * @return Assays
	 * @throws DataException
	 */
	public static SQLAssay assaysForMaterial(SQLData data, Material aMaterial) throws DataException {
		return SQLAssay.assaysForMaterialID(data, aMaterial.getID());
	}
	
	/**
	 * Load assays for the specified project ID.
	 * 
	 * @param data SQLData object
	 * @param aProject the Project
	 * @return Assays
	 * @throws DataException
	 */
	public static SQLAssay assaysForProject(SQLData data, Project aProject) throws DataException {
		return SQLAssay.assaysForProjectID(data, aProject.getID());
	}

	/**
	 * Load assays for the specified {@link Project}.
	 * 
	 * @param data SQLData object
	 * @param projectID ID of the project.
	 * @return Assays
	 * @throws DataException
	 */
	public static SQLAssay assaysForProjectID(SQLData data, String projectID) throws DataException {
		try {
			SQLAssay assays = new SQLAssay(data);
			PreparedStatement aPsth = assays.myData.prepareStatement(SQL_LOAD_FOR_PROJECT);
			aPsth.setString(1, projectID);
			assays.myData.loadUsingPreparedStatement(aPsth);
			return assays;
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}
	
	public static SQLAssay create(SQLData data, String newID) throws DataException {
		SQLAssay anAssay = new SQLAssay(data);
		anAssay.makeNewWithValue(SQL_INSERT_ASSAY, newID);
		return anAssay;
	}
	
	public static SQLAssay createInProject(SQLData data, String newID, String projectID) throws DataException {
		SQLAssay anObj = new SQLAssay(data);
		anObj.makeNewInProject(SQL_INSERT_WITH_PROJECT, newID, projectID);
		return anObj;
	}
	
	public static SQLAssay createInProject(SQLData data, String newID, Project aProject) throws DataException {
		return SQLAssay.createInProject(data, newID, aProject.getID());
	}	

	private static final String SQL_LOAD_FOR_PROJECT_UPDATED = "SELECT DISTINCT " + allColumns("assay_info.", ALL_COLUMNS) + 
			" FROM assay_info LEFT OUTER JOIN assay ON (assay_info.assay_id = assay.assay_id) WHERE project_id = ? AND (assay_info.last_updated > ? OR assay.last_updated > ?)";

	/**
	 * Load assay records for a project that have been updated since the date given.
	 * 
	 * @param data SQLData object
	 * @param projectID ID of the project
	 * @param lastUpdate Date of the last update
	 * @return
	 * @throws SQLException
	 * @throws DataException
	 */
	public static SQLAssay loadForProjectLastUpdated(SQLData data, String projectID, Date lastUpdate) throws SQLException, DataException {
		SQLAssay results = new SQLAssay(data);
		PreparedStatement aPsth = results.myData.prepareStatement(SQL_LOAD_FOR_PROJECT_UPDATED);
		aPsth.setString(1, projectID);
		Timestamp update = new Timestamp(lastUpdate.getTime());
		aPsth.setTimestamp(2, update);
		aPsth.setTimestamp(3, update);
		results.loadUsingPreparedStatement(aPsth);
		return results;
	}

	/**
	 * Load assay records for a project that have been update since the date given, exclude records from the selected host.
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
	public static SQLAssay assaysForProjectLastUpdated(SQLData data, String projectID, Date lastUpdate, String hostID) throws SQLException, DataException {
		SQLAssay results = new SQLAssay(data);
		PreparedStatement aPsth = results.myData.prepareStatement(SQL_LOAD_FOR_PROJECT_UPDATED + " AND " + REMOTE_HOST_COLUMN + " <> ?");
		aPsth.setString(1, projectID);
		Timestamp update = new Timestamp(lastUpdate.getTime());
		aPsth.setTimestamp(2, update);
		aPsth.setTimestamp(3, update);
		aPsth.setString(4, hostID);
		results.loadUsingPreparedStatement(aPsth);
		return results;
	}

	private final static String SQL_INSERT_REMOTE_PROJECT = "INSERT INTO assay_info(assay_id,project_id,remote_host) VALUES(?,?,?)";

	/**
	 * Create a new Assay record using update information from a remote host.
	 * 
	 * @param data SQLData object
	 * @param projectID ID of the project
	 * @param hostID ID of the remote host (UUID)
	 * @param assaylID assay ID of the original object (stored but not used a ID on master system)
	 * @return
	 * @throws DataException
	 */
	
	public static SQLAssay createInProject(SQLData data, String projectID, String hostID, String assayID) throws DataException {
		SQLAssay object = new SQLAssay(data);
		try {
			PreparedStatement aSth = object.myData.prepareStatement(SQL_INSERT_REMOTE_PROJECT);
			aSth.setString(1, assayID);
			aSth.setString(2, projectID);
			aSth.setString(3, hostID);
			object.makeNewInProject(aSth,assayID,projectID);
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return object;
		
	}

	public static Assay load(SQLData data, String assayID) throws DataException {
		SQLAssay result = new SQLAssay(data);
		result.myID = assayID;
		result.fetchRecord();
		return result;
	}
	
	
	protected SQLAssay(SQLData data) {
		super(data);
		this.initVals();
	}

	@Deprecated
	public SQLAssay(SQLData data, String anID) throws DataException {
		this(data);
		this.myID = anID;
		this.fetchRecord();
	}
	
	protected void initVals() {
		this.idField = ID_COLUMN;
		this.myData.setProjectField(PROJECT_COLUMN);
		this.myData.setAccessRole(User.BIOASSAY_ROLE);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.sql.Assay#fetchRecord()
	 */
	protected void fetchRecord() throws DataException {
		this.fetchRecord(SQL_LOAD_ASSAY);
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.sql.Assay#create(java.lang.String)
	 */
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.sql.Assay#getName()
	 */
	public String getName() throws DataException {
		return this.myData.getQUIETString(NAME_COLUMN);
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.sql.Assay#getTarget()
	 */
	public String getTarget() throws DataException {
		return this.myData.getString(TARGET_COLUMN);
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.sql.Assay#getActiveOperator()
	 */
	public String getActiveOperator() throws DataException {
		return this.myData.getString(ACTIVE_OPERATOR_COLUMN);
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.sql.Assay#getActiveLevel()
	 */
	public BigDecimal getActiveLevel() throws DataException {
		BigDecimal value = this.myData.getBigDecimal(ACTIVE_LEVEL_COLUMN);
		if ( value != null ) {
			int sf = this.getSigFigs();
			return value.round(new MathContext(sf));
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.sql.Assay#getLength()
	 */
	public int getLength() throws DataException {
		return this.myData.getInt(LENGTH_COLUMN);
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.sql.Assay#getWidth()
	 */
	public int getWidth() throws DataException {
		return this.myData.getInt(WIDTH_COLUMN);
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.sql.Assay#getNotes()
	 */
	public String getNotes() throws DataException {
		return this.myData.getString(NOTES_COLUMN);
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.sql.Assay#getDateString()
	 */
	public String getDateString() throws DataException {
		return this.myData.getString(DATE_COLUMN);
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.sql.Assay#getDate()
	 */
	public Date getDate() throws DataException {
		return this.myData.getDate(DATE_COLUMN);
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.sql.Assay#getUnitFormat()
	 */
	public String getUnit() throws DataException {
		return this.myData.getString(UNIT_COLUMN);
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.sql.Assay#setUnitFormat(java.lang.String)
	 */
	public void setUnit(String newUnit) throws DataException {
		this.myData.setString(UNIT_COLUMN, newUnit);
	}
		
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.sql.Assay#getProjectID()
	 */
	public String getProjectID() throws DataException {
		return this.myData.getString(PROJECT_COLUMN);
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.sql.Assay#getProject()
	 */
	public Project getProject() throws DataException {
		String projID = this.getProjectID();
		if ( projID != null ) {
			Project aProj = SQLProject.load(this.myData.duplicate(), projID);
			if ( aProj.first() )
				return aProj;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.sql.Assay#setProjectID(java.lang.String)
	 */
	public void setProjectID(String newValue) throws DataException {
		this.myData.setStringNullBlank(PROJECT_COLUMN, newValue);
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.sql.Assay#setProject(edu.uic.orjala.cyanos.Project)
	 */
	public void setProject(Project aProject) throws DataException {
		if ( aProject != null )
			this.setProjectID(aProject.getID());
		else 
			this.myData.setNull(PROJECT_COLUMN);
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.sql.Assay#setName(java.lang.String)
	 */
	public void setName(String newName) throws DataException {
		this.myData.setString(NAME_COLUMN, newName);
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.sql.Assay#setNotes(java.lang.String)
	 */
	public void setNotes(String newNotes) throws DataException {
		this.myData.setString(NOTES_COLUMN, newNotes);
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.sql.Assay#setDate(java.lang.String)
	 */
	public void setDate(String newValue) throws DataException {
		this.myData.setString(DATE_COLUMN, newValue);
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.sql.Assay#setDate(java.util.Date)
	 */
	public void setDate(Date newValue) throws DataException {
		this.myData.setDate(DATE_COLUMN, newValue);
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.sql.Assay#setTarget(java.lang.String)
	 */
	public void setTarget(String newValue) throws DataException {
		this.myData.setString(TARGET_COLUMN, newValue);
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.sql.Assay#setLength(int)
	 */
	public void setLength(int newValue) throws DataException {
		this.myData.setInt(LENGTH_COLUMN, newValue);
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.sql.Assay#setWidth(int)
	 */
	public void setWidth(int newValue) throws DataException {
		this.myData.setInt(WIDTH_COLUMN, newValue);
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.sql.Assay#setActiveLevel(float)
	 */
	public void setActiveLevel(BigDecimal newValue) throws DataException {
		this.myData.setBigDecimal(ACTIVE_LEVEL_COLUMN, newValue);
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.sql.Assay#setActiveLevel(java.lang.String)
	 */
	public void setActiveLevel(String newValue) throws DataException {
		this.myData.setString(ACTIVE_LEVEL_COLUMN, newValue);
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Assay#setActiveOperator(java.lang.String)
	 */
	public void setActiveOperator(String newValue) throws DataException {
		this.myData.setString(ACTIVE_OPERATOR_COLUMN, newValue);
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.sql.Assay#addNotes(java.lang.String)
	 */
	public void addNotes(String newNotes) throws DataException {
		StringBuffer myNotes = new StringBuffer(this.getNotes());
		myNotes.append(" ");
		myNotes.append(newNotes);
		this.myData.setString(NOTES_COLUMN, myNotes.toString());
	}
	
	public String getDataFileClass() {
		return DATA_FILE_CLASS;
	}

	public AssayPlate getAssayData() throws DataException {
		return SQLAssayPlate.dataForAssay(this);
	}

	public AssayData getAssayDataForSample(Sample aSample) throws DataException {
		return SQLAssayData.dataForSample(this.myData, aSample);
	}

	public AssayData getAssayDataForSampleID(String aSampleID) throws DataException {
		return SQLAssayData.dataForSampleID(this.myData, aSampleID);
	}

	public AssayData getAssayDataForStrain(Strain aStrain) throws DataException {
		return SQLAssayData.dataForStrain(this.myData, aStrain);
	}

	public AssayData getAssayDataForStrainID(String aStrainID) throws DataException {
		return SQLAssayData.dataForStrainID(this.myData, aStrainID);
	}
	
	public AssayData getActiveData() throws DataException {
		return SQLAssayData.activeData(this);
	}

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

	public ExternalFile getDataFiles() throws DataException {
		return this.getDataFiles(DATA_FILE_TABLE, this.myID);
	}

	public void linkDataFile(ExternalFile aFile, String dataType) throws DataException {
		this.setDataFile(DATA_FILE_TABLE, this.myID, dataType, aFile);
	}
	
	public ExternalFile getDataFilesForType(String dataType) throws DataException {
		return this.getDataFilesForType(DATA_FILE_TABLE, this.myID, dataType);
	}

	public void unlinkDataFile(ExternalFile aFile) throws DataException {
		this.unsetDataFile(DATA_FILE_TABLE, this.myID, aFile);
	}

	
	public AssayData getDataForObject(BasicObject object) throws DataException {
		if ( object instanceof Sample ) {
			return this.getAssayDataForSample((Sample) object);
		} else if ( object instanceof Strain ) {
			return this.getAssayDataForStrain((Strain) object);
		} 
		return null; 
	}

	public void clearData() throws DataException {
		if ( this.myID != null && this.isAllowedException(Role.WRITE) ) {
			PreparedStatement psth = this.myData.prepareStatement(SQL_CLEAR_DATA);
			try {
				psth.setString(1, this.myID);
				psth.executeUpdate();
				psth.close();
			} catch (SQLException e) {
				throw new DataException(e);
			}
		}
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

	@Override
	public AssayTemplate createTemplate() throws DataException {
		SQLAssayTemplate template = new SQLAssayTemplate(this.myData);
		template.setTarget(this.getTarget());
		template.setActiveOperator(this.getActiveOperator());
		template.setActiveLevel(this.getActiveLevel());
		template.setLength(this.getLength());
		template.setWidth(this.getWidth());
		template.setSigFigs(this.getSigFigs());
		return template;
	}

	@Override
	public void setTemplate(AssayTemplate aTemplate) throws DataException {
		if ( this.myID != null )  {
			boolean oldRefresh = this.autorefresh;
			this.setManualRefresh();
			this.setTarget(aTemplate.getTarget());
			this.setActiveOperator(aTemplate.getActiveOperator());
			this.setActiveLevel(aTemplate.getActiveLevel());
			this.setLength(aTemplate.getLength());
			this.setWidth(aTemplate.getWidth());
			this.setSigFigs(aTemplate.getSigFigs());
			this.myData.refresh();
			this.autorefresh = oldRefresh;
		}
	}

	@Override
	public void setSigFigs(int value) throws DataException {
		this.myData.setInt(SIG_FIGS_COLUMN, value);
	}

	@Override
	public void setSigFigs(String value) throws DataException {
		this.myData.setString(SIG_FIGS_COLUMN, value);
	}

	@Override
	public int getSigFigs() throws DataException {
		return this.myData.getInt(SIG_FIGS_COLUMN);
	}

	@Override
	public boolean hasDataFile(String path) throws DataException {
		ExternalFile file = this.getDataFile(DATA_FILE_TABLE, this.myID, path);
		return ( file != null && file.first());
	}
	
	@Override
	public ExternalFile getDataFile(String path) throws DataException {
		return this.getDataFile(DATA_FILE_CLASS, this.myID, path);
	}
}
