/**
 * 
 */
package edu.uic.orjala.cyanos.sql;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import edu.uic.orjala.cyanos.Compound;
import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.ExternalFile;
import edu.uic.orjala.cyanos.Harvest;
import edu.uic.orjala.cyanos.Material;
import edu.uic.orjala.cyanos.Notebook;
import edu.uic.orjala.cyanos.Project;
import edu.uic.orjala.cyanos.Role;
import edu.uic.orjala.cyanos.Sample;
import edu.uic.orjala.cyanos.SampleAccount;
import edu.uic.orjala.cyanos.Separation;
import edu.uic.orjala.cyanos.Strain;
import edu.uic.orjala.cyanos.User;

/**
 * @author George Chlipala
 *
 */
public class SQLMaterial extends SQLObject implements Material {

	// Setup the column names here so that changing is easier.
	public final static String ID_COLUMN = "material_id";
	public final static String LABEL_COLUMN = "label";
	public final static String CULTURE_ID_COLUMN = "culture_id";
	public final static String NOTES_COLUMN = "notes";
	public final static String PROJECT_COLUMN = "project_id";
	public final static String DATE_COLUMN = "date";
	
	public final static String REMOVED_DATE_COLUMN = "removed_date";
	public final static String REMOVED_USER_COLUMN = "removed_by";

	public static final String NOTEBOOK_COLUMN = "notebook_id";
	public static final String NOTEBOOK_PAGE_COLUMN = "notebook_page";
	
	public final static String AMOUNT_VALUE_COLUMN = "amount_value";
	public final static String AMOUNT_SCALE_COLUMN = "amount_scale";
	
	public final static String REMOTE_HOST_COLUMN = "remote_host";
	public final static String REMOTE_ID_COLUMN = "remote_id";
	
	private final static String[] ALL_COLUMNS = { ID_COLUMN, LABEL_COLUMN, CULTURE_ID_COLUMN, 
		NOTES_COLUMN, PROJECT_COLUMN, DATE_COLUMN, 
	//	REMOVED_DATE_COLUMN, REMOVED_USER_COLUMN, 
	//	NOTEBOOK_COLUMN, NOTEBOOK_PAGE_COLUMN, 
		AMOUNT_VALUE_COLUMN, AMOUNT_SCALE_COLUMN, REMOTE_HOST_COLUMN, REMOTE_ID_COLUMN };	
	
	final static String SQL_BASE = sqlBase("material", ALL_COLUMNS);
	
	private final static String SQL_INSERT_MATERIAL = "INSERT INTO material(culture_id,date,remote_id) VALUES(?, CURRENT_DATE,UUID())";
	private final static String SQL_INSERT_WITH_PROJECT = "INSERT INTO material(culture_id,date,project_id,remote_id) VALUES(?,CURRENT_DATE,?,UUID())";

	private final static String SQL_MAKE_EXTRACT = "INSERT INTO extract_info(material_id,harvest_id) VALUES(?,?)";
	private final static String SQL_IS_EXTRACT = "SELECT * FROM extract_info WHERE material_id=?";
	
	private static final String SQL_GET_EXTRACT_SOLVENT = "SELECT solvent FROM extract_info WHERE material_id=?";
	private static final String SQL_GET_EXTRACT_TYPE = "SELECT type FROM extract_info WHERE material_id=?";
	private static final String SQL_GET_EXTRACT_METHOD = "SELECT method FROM extract_info WHERE material_id=?";
	private static final String SQL_GET_EXTRACT_SOURCE = "SELECT harvest_id FROM extract_info WHERE material_id=?";
	
	private static final String SQL_SET_EXTRACT_SOLVENT = "UPDATE extract_info SET solvent=? WHERE material_id=?";
	private static final String SQL_SET_EXTRACT_TYPE = "UPDATE extract_info SET type=? WHERE material_id=?";
	private static final String SQL_SET_EXTRACT_METHOD = "UPDATE extract_info SET method=? WHERE material_id=?";
	private static final String SQL_SET_EXTRACT_SOURCE = "UPDATE extract_info SET harvest_id=? WHERE material_id=?";

	private static final String SQL_LOAD_HARVEST = "SELECT harvest.* FROM harvest JOIN extract_info ON (extract_info.harvest_id = harvest.harvest_id) WHERE extract_info.material_id=?";
	private static final String SQL_LOAD_SEP_DESCEND = "SELECT separation.* FROM separation JOIN separation_source src ON(separation.separation_id = src.separation_id) WHERE src.material_id=?";
	private static final String SQL_LOAD_SEP_PARENT = "SELECT separation.* FROM separation JOIN separation_product src ON(separation.separation_id = src.separation_id) WHERE src.material_id=?";
	
	private static final String SQL_LOAD_EXTRACTS_FOR_STRAIN = SQL_BASE + " JOIN extract_info ON (extract_info.material_id = material.material_id) WHERE material.culture_id=?";
	private static final String SQL_LOAD_MATERIALS_FOR_COMPOUND = SQL_BASE + " JOIN  compound_peaks ON (compound_peaks.material_id = material.material_id) WHERE compound_peaks.compound_id=?";
	
	private static final String SQL_LOAD = SQL_BASE + " WHERE material_id=?";
	private static final String SQL_LOAD_BY_LABEL = SQL_BASE + " WHERE label=?";
	private static final String SQL_LOAD_CULTURE_ID = SQL_BASE + " WHERE culture_id=?";
	private static final String SQL_LOAD_QUERY = SQL_BASE + " WHERE culture_id = ? OR remote_id = ?";
	
	private static final String SQL_GET_AMOUNT_FOR_SEP = "SELECT amount_value, amount_scale FROM separation_source WHERE material_id = ? AND separation_id = ? ";
	
	private static final String SQL_LINK_CMPD = "INSERT INTO compound_peaks(compound_id, material_id, separation_id, retention_time) VALUES(?,?,?,?)";
	private static final String SQL_UNLINK_CMPD = "DELETE FROM compound_peaks WHERE compound_id = ? AND material_id = ?";
	private static final String SQL_GET_RT_COMPOUND = "SELECT retention_time FROM compound_peaks WHERE material_id=? AND compound_id=?";
	
	private static final String DATA_FILE_TABLE = "material";

	public static SQLMaterial create(SQLData data, String strainID) throws DataException {
		SQLMaterial object = new SQLMaterial(data);
		try {
			PreparedStatement aSth = object.myData.prepareStatement(SQL_INSERT_MATERIAL);
			aSth.setString(1, strainID);
			object.makeNewWithAutonumber(aSth);
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return object;
	}
	
	public static SQLMaterial createInProject(SQLData data, String strainID, String projectID) throws DataException {
		SQLMaterial object = new SQLMaterial(data);
		try {
			PreparedStatement aSth = object.myData.prepareStatement(SQL_INSERT_WITH_PROJECT);
			aSth.setString(1, strainID);
			aSth.setString(2, projectID);
			object.makeNewWithAutonumber(aSth);
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return object;
	}
	
	private final static String SQL_INSERT_REMOTE_PROJECT = "INSERT INTO material(project_id,remote_host,remote_id) VALUES(?,?,?)";

	/**
	 * Create a new Material record using update information from a remote host.
	 * 
	 * @param data SQLData object
	 * @param projectID ID of the project
	 * @param hostID ID of the remote host (UUID)
	 * @param materialID material ID of the original object (stored but not used a ID on master system)
	 * @return
	 * @throws DataException
	 */
	
	public static SQLMaterial createInProject(SQLData data, String projectID, String hostID, String materialID) throws DataException {
		SQLMaterial object = new SQLMaterial(data);
		try {
			PreparedStatement aSth = object.myData.prepareStatement(SQL_INSERT_REMOTE_PROJECT);
			aSth.setString(1, projectID);
			aSth.setString(2, hostID);
			aSth.setString(3, materialID);
			object.makeNewWithAutonumber(aSth, projectID);
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return object;
		
	}
	
	/**
	 * Retrieve materials for a compound.
	 * 
	 * @param data SQLData object
	 * @param compoundID ID of the compound
	 * @return Material object with materials associated with the compound.
	 * @throws DataException
	 */
	public static SQLMaterial materialsForCompound(SQLData data, String compoundID) throws DataException {
		try {
			SQLMaterial newObj = new SQLMaterial(data);
			PreparedStatement aSth = data.prepareStatement(SQL_LOAD_MATERIALS_FOR_COMPOUND);
			aSth.setString(1, compoundID);
			newObj.loadUsingPreparedStatement(aSth);
			return newObj;
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

	public static SQLMaterial load(SQLData data, String materialID) throws DataException {
		SQLMaterial object = new SQLMaterial(data);
		object.myID = materialID;
		object.fetchRecord(SQL_LOAD);
		return object;
	}
	
	public static SQLMaterial load(SQLData data, String hostID, String materialID) throws DataException {
		SQLMaterial object = new SQLMaterial(data);
		try { 
			PreparedStatement aSth = object.myData.prepareStatement(SQL_BASE + " WHERE remote_host = ? AND remote_id = ?");
			aSth.setString(1, hostID);
			aSth.setString(2, materialID);
			object.fetchRecord(aSth);
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return object;
	}
	
	public static SQLMaterial find(SQLData data, String query) throws DataException {
		SQLMaterial object = new SQLMaterial(data);
		try {
			PreparedStatement aSth = object.myData.prepareStatement(SQL_LOAD_QUERY);
			aSth.setString(1, query);
			aSth.setString(2, query);
			object.loadUsingPreparedStatement(aSth);
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return object;
	}	

	/**
	 * Load materials by label.
	 * 
	 * @param data
	 * @param label
	 * @return
	 * @throws DataException
	 */
	public static SQLMaterial findByLabel(SQLData data, String label) throws DataException {
		SQLMaterial object = new SQLMaterial(data);
		try {
			PreparedStatement aSth = object.myData.prepareStatement(SQL_LOAD_BY_LABEL);
			aSth.setString(1, label);
			object.loadUsingPreparedStatement(aSth);
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return object;
	}	

	public static SQLMaterial findForStrain(SQLData data, String cultureID) throws DataException {
		SQLMaterial object = new SQLMaterial(data);
		try {
			PreparedStatement aSth = object.myData.prepareStatement(SQL_LOAD_CULTURE_ID);
			aSth.setString(1, cultureID);
			object.loadUsingPreparedStatement(aSth);
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return object;
	}
	
	public static SQLMaterial extractsForStrain(SQLData data, String cultureID) throws DataException {
		SQLMaterial object = new SQLMaterial(data);
		try {
			PreparedStatement aSth = object.myData.prepareStatement(SQL_LOAD_EXTRACTS_FOR_STRAIN);
			aSth.setString(1, cultureID);
			object.loadUsingPreparedStatement(aSth);
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return object;
	}
	
	private static final String SQL_LOAD_FOR_PROJECT_UPDATED = SQL_BASE + " LEFT OUTER JOIN extract_info ON(extract_info.material_id = material.material_id)" +
			" WHERE project_id = ? AND ( material.last_updated > ? OR extract_info.last_updated > ? )";
	
	/**
	 * Load material records for a project that have been updated since the date given.
	 * 
	 * @param data SQLData object
	 * @param projectID ID of the project
	 * @param lastUpdate Date of the last update
	 * @return
	 * @throws SQLException
	 * @throws DataException
	 */
	public static SQLMaterial loadForProjectLastUpdated(SQLData data, String projectID, Date lastUpdate) throws SQLException, DataException {
		SQLMaterial results = new SQLMaterial(data);
		PreparedStatement aPsth = results.myData.prepareStatement(SQL_LOAD_FOR_PROJECT_UPDATED);
		aPsth.setString(1, projectID);
		Timestamp update = new Timestamp(lastUpdate.getTime());
		aPsth.setTimestamp(2, update);
		aPsth.setTimestamp(3, update);
		results.loadUsingPreparedStatement(aPsth);
		return results;
	}
	
	/**
	 * Load material records for a project that have been update since the date given, exclude records from the selected host.
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
	public static SQLMaterial loadForProjectLastUpdated(SQLData data, String projectID, Date lastUpdate, String hostID) throws SQLException, DataException {
		SQLMaterial results = new SQLMaterial(data);
		PreparedStatement aPsth = results.myData.prepareStatement(SQL_LOAD_FOR_PROJECT_UPDATED + " AND " + REMOTE_HOST_COLUMN + " <> ?");
		aPsth.setString(1, projectID);
		Timestamp update = new Timestamp(lastUpdate.getTime());
		aPsth.setTimestamp(2, update);
		aPsth.setTimestamp(3, update);
		aPsth.setString(4, hostID);
		results.loadUsingPreparedStatement(aPsth);
		return results;
	}
	
	protected SQLMaterial(SQLData data) {
		super(data);
		this.initVals();
	}
	
	protected void initVals() {
		this.idField = ID_COLUMN;
		this.myData.setAccessRole(User.SAMPLE_ROLE);
		this.myData.setProjectField(PROJECT_COLUMN);
	}


	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Material#getAccount()
	 */
	
	public SampleAccount getAccount() throws DataException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Material#getLabel()
	 */
	
	public String getLabel() throws DataException {
		return this.myData.getString(LABEL_COLUMN);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Material#getCultureID()
	 */
	
	public String getCultureID() throws DataException {
		return this.myData.getString(CULTURE_ID_COLUMN);
	}

	public Strain getCulture() throws DataException { 
		return SQLStrain.load(myData, this.getCultureID());
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Material#getNotes()
	 */
	
	public String getNotes() throws DataException {
		return this.myData.getString(NOTES_COLUMN);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Material#getDateString()
	 */
	
	public String getDateString() throws DataException {
		return this.myData.getString(DATE_COLUMN);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Material#getDate()
	 */
	
	public Date getDate() throws DataException {
		return this.myData.getDate(DATE_COLUMN);
	}
	
	

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.sql.SQLObject#getProjectID()
	 */
	public String getProjectID() throws DataException {
		return this.myData.getString(PROJECT_COLUMN);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Material#setLabel(java.lang.String)
	 */
	public void setLabel(String newLabel) throws DataException {
		this.myData.setString(LABEL_COLUMN, newLabel);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Material#setCultureID(java.lang.String)
	 */
	
	public void setCultureID(String newCultureID) throws DataException {
		this.myData.setString(CULTURE_ID_COLUMN, newCultureID);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Material#setNotes(java.lang.String)
	 */
	
	public void setNotes(String newNotes) throws DataException {
		this.myData.setString(NOTES_COLUMN, newNotes);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Material#setDate(java.lang.String)
	 */
	
	public void setDate(String newValue) throws DataException {
		this.myData.setString(DATE_COLUMN, newValue);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Material#setDate(java.util.Date)
	 */
	
	public void setDate(Date newValue) throws DataException {
		this.myData.setDate(DATE_COLUMN, newValue);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Material#addNotes(java.lang.String)
	 */
	
	public void addNotes(String newNotes) throws DataException {
		StringBuffer myNotes = new StringBuffer(this.getNotes());
		myNotes.append(" ");
		myNotes.append(newNotes);
		this.myData.setString(NOTES_COLUMN, myNotes.toString());
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Material#setProjectID(java.lang.String)
	 */
	
	public void setProjectID(String newValue) throws DataException {
		this.myData.setStringNullBlank(PROJECT_COLUMN, newValue);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Material#setProject(edu.uic.orjala.cyanos.Project)
	 */
	
	public void setProject(Project aProject) throws DataException {
		if ( aProject != null )
			this.setProjectID(aProject.getID());
		else 
			this.myData.setNull(PROJECT_COLUMN);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Material#isFraction()
	 */
	
	public boolean isFraction() throws DataException {
		if ( this.myID == null ) {
			return false;
		}
		try {
			PreparedStatement aPsth = this.myData.prepareStatement(SQL_LOAD_SEP_PARENT);
			aPsth.setString(1, this.myID);
			ResultSet check = aPsth.executeQuery();
			boolean retval = check.first();
			check.close();
			aPsth.close();
			return retval;
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Material#getParentSeparation()
	 */
	
	public Separation getParentSeparation() throws DataException {
		if ( this.myID != null ) {
			SQLSeparation mySep = new SQLSeparation(this.myData);
			try {
				PreparedStatement aPsth = this.myData.prepareStatement(SQL_LOAD_SEP_PARENT);
				aPsth.setString(1,this.myID);
				aPsth.setString(1, this.myID);
				mySep.loadUsingPreparedStatement(aPsth);
				return mySep;
			} catch (SQLException e) {
				throw new DataException(e);
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Material#getSeparations()
	 */
	
	public Separation getSeparations() throws DataException {
		if ( this.myID != null ) {
			SQLSeparation mySep = new SQLSeparation(this.myData);
			try {
				PreparedStatement aPsth = this.myData.prepareStatement(SQL_LOAD_SEP_DESCEND);
				aPsth.setString(1, this.myID);
				mySep.loadUsingPreparedStatement(aPsth);
				return mySep;
			} catch (SQLException e) {
				throw new DataException(e);
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Material#makeExtract(java.lang.String)
	 */
	
	public boolean makeExtract(String harvestID) throws DataException {
		boolean retval = false;
		if ( this.myID != null ) {
			try {
				PreparedStatement aPsth = this.myData.prepareStatement(SQL_MAKE_EXTRACT);
				aPsth.setString(1,this.myID);
				aPsth.setString(2,harvestID);
				retval = ( aPsth.executeUpdate() > 0 );
				aPsth.close();
			} catch ( SQLException e ) {
				throw new DataException(e);
			} 
		}
		return retval;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Material#isExtract()
	 */
	
	public boolean isExtract() throws DataException {
		boolean retval = false;
		if ( this.myID != null ) {
			try {
				PreparedStatement aPsth = this.myData.prepareStatement(SQL_IS_EXTRACT);
				aPsth.setString(1,this.myID);
				ResultSet aResult = aPsth.executeQuery();
				retval = aResult.first();
				aPsth.close();
			} catch ( SQLException e ) {
				throw new DataException(e);
			} 
		}
		return retval;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Material#getHarvestForExtract()
	 */
	
	public Harvest getHarvestForExtract() throws DataException {
		if ( this.myID != null ) {
			SQLHarvest myHarvest = new SQLHarvest(this.myData);
			try {
				PreparedStatement aPsth = this.myData.prepareStatement(SQL_LOAD_HARVEST);
				aPsth.setString(1, this.myID);
				myHarvest.loadUsingPreparedStatement(aPsth);
				if ( myHarvest.first())
					return myHarvest;
			} catch (SQLException e) {
				throw new DataException(e);
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Material#getExtractSolvent()
	 */
	
	public String getExtractSolvent() throws DataException {
		if ( this.myID != null ) {
			try {
				PreparedStatement aPsth = this.myData.prepareStatement(SQL_GET_EXTRACT_SOLVENT);
				aPsth.setString(1,this.myID);
				ResultSet aResult = aPsth.executeQuery();
				String retval = null;
				if ( aResult.first()) retval = aResult.getString(1);
				aResult.close();
				aPsth.close();
				return retval;
			} catch (SQLException e) {
				throw new DataException(e);
			}
		}	
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Material#getExtractType()
	 */
	
	public String getExtractType() throws DataException {
		if ( this.myID != null ) {
			try {
				PreparedStatement aPsth = this.myData.prepareStatement(SQL_GET_EXTRACT_TYPE);
				aPsth.setString(1,this.myID);
				ResultSet aResult = aPsth.executeQuery();
				String retval = null;
				if ( aResult.first()) retval = aResult.getString(1);
				aResult.close();
				aPsth.close();
				return retval;
			} catch (SQLException e) {
				throw new DataException(e);
			}
		}	
		return null;
	}

	public String getExtractMethod() throws DataException {
		if ( this.myID != null ) {
			try {
				PreparedStatement aPsth = this.myData.prepareStatement(SQL_GET_EXTRACT_METHOD);
				aPsth.setString(1,this.myID);
				ResultSet aResult = aPsth.executeQuery();
				String retval = null;
				if ( aResult.first()) retval = aResult.getString(1);
				aResult.close();
				aPsth.close();
				return retval;
			} catch (SQLException e) {
				throw new DataException(e);
			}
		}	
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Material#getExtractSource()
	 */
	
	public Harvest getExtractSource() throws DataException {
		if ( this.myID != null ) {
			try {
				PreparedStatement aPsth = this.myData.prepareStatement(SQL_GET_EXTRACT_SOURCE);
				aPsth.setString(1,this.myID);
				ResultSet aResult = aPsth.executeQuery();
				Harvest retVal = null;
				if ( aResult.first()) {
					retVal = SQLHarvest.load(this.myData, aResult.getString(1));
				}
				aResult.close();
				aPsth.close();
				return retVal;
			} catch (SQLException e) {
				throw new DataException(e);
			}
		}	
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Material#setExtractSolvent(java.lang.String)
	 */
	
	public void setExtractSolvent(String newSolvent) throws DataException {
		if ( this.isExtract() ) {
			try {
				PreparedStatement aPsth = this.myData.prepareStatement(SQL_SET_EXTRACT_SOLVENT);
				aPsth.setString(1, newSolvent);
				aPsth.setString(2,this.myID);
				aPsth.executeUpdate();
				aPsth.close();
			} catch (SQLException e) {
				throw new DataException(e);
			}
		}	
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Material#setExtractType(java.lang.String)
	 */
	
	public void setExtractType(String newType) throws DataException {
		if ( this.isExtract() ) {
			try {
				PreparedStatement aPsth = this.myData.prepareStatement(SQL_SET_EXTRACT_TYPE);
				aPsth.setString(1, newType);
				aPsth.setString(2,this.myID);
				aPsth.executeUpdate();
				aPsth.close();
			} catch (SQLException e) {
				throw new DataException(e);
			}
		}	
	}

	public void setExtractMethod(String newValue) throws DataException {
		if ( this.isExtract() ) {
			try {
				PreparedStatement aPsth = this.myData.prepareStatement(SQL_SET_EXTRACT_METHOD);
				aPsth.setString(1, newValue);
				aPsth.setString(2,this.myID);
				aPsth.executeUpdate();
				aPsth.close();
			} catch (SQLException e) {
				throw new DataException(e);
			}
		}	
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Material#setExtractSource(java.lang.String)
	 */
	
	public void setExtractSource(String harvestID) throws DataException {
		if ( this.isExtract() ) {
			try {
				PreparedStatement aPsth = this.myData.prepareStatement(SQL_SET_EXTRACT_SOURCE);
				aPsth.setString(1, harvestID);
				aPsth.setString(2,this.myID);
				aPsth.executeUpdate();
				aPsth.close();
			} catch (SQLException e) {
				throw new DataException(e);
			}
		}
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Material#setExtractSource(edu.uic.orjala.cyanos.Harvest)
	 */
	
	public void setExtractSource(Harvest aHarvest) throws DataException {
		this.setExtractSolvent(aHarvest.getID());
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Material#getAmount()
	 */
	
	public BigDecimal getAmount() throws DataException {
		return this.myData.getDecimal(AMOUNT_VALUE_COLUMN, AMOUNT_SCALE_COLUMN);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.sql.SQLObject#fetchRecord()
	 */
	
	protected void fetchRecord() throws DataException {
		this.fetchRecord(SQL_LOAD);
	}

	
	public Sample getSamples() throws DataException {
		return SQLSample.loadForMaterial(myData, this.getID());
	}

	
	public void setAmount(BigDecimal value) throws DataException {
		this.myData.setDecimal(AMOUNT_VALUE_COLUMN, AMOUNT_SCALE_COLUMN, value);
	}	

	public Date getRemovedDate() throws DataException {
		return this.myData.getDate(REMOVED_DATE_COLUMN);
	}


	public String getRemovedDateString() throws DataException {
		return this.myData.getString(REMOVED_DATE_COLUMN);
	}


	public boolean isActive() throws DataException {
		return this.myData.isNull(REMOVED_DATE_COLUMN);
	}


	public boolean isRemoved() throws DataException {
		return (! this.myData.isNull(REMOVED_DATE_COLUMN));
	}


	public User getRemovedBy() throws DataException {
		String userID = this.myData.getString(REMOVED_USER_COLUMN);
		if ( userID != null )
			return new SQLUser(this.myData.getDBC(), userID);
		return null;
	}


	public String getRemovedByID() throws DataException {
		return this.myData.getString(REMOVED_USER_COLUMN);
	}


	public void remove() throws DataException {
		if ( this.isAllowedException(Role.DELETE) ) {
			Date now = new Date();
			this.myData.setDate(REMOVED_DATE_COLUMN, now);
			this.myData.setString(REMOVED_USER_COLUMN, this.myData.getUser().getUserID());
	//		SQLSampleAccount.voidReferences(this.myData, "sample", this.myID);
		}
	}

	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Material#formatAmount(java.lang.String)
	 */
	public String formatAmount(String unit) throws DataException {
		return formatAmount(getAmount(), unit);
	}

	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Material#displayAmount()
	 */
	public String displayAmount() throws DataException {
		return autoFormatAmount(getAmount(), SQLMaterial.MASS_TYPE);
	}

	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Material#getAmountForSeparation(edu.uic.orjala.cyanos.Separation)
	 */
	public BigDecimal getAmountForSeparation(Separation separation)
			throws DataException {
		return this.getAmountForSeparationID(separation.getID());
	}

	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Material#getAmountForSeparationID(java.lang.String)
	 */
	public BigDecimal getAmountForSeparationID(String separationID)
			throws DataException {
		if ( this.myID != null ) {
			try {
				PreparedStatement aPsth = this.myData.prepareStatement(SQL_GET_AMOUNT_FOR_SEP);
				aPsth.setString(1, this.myID);
				aPsth.setString(2, separationID);
				ResultSet aResult = aPsth.executeQuery();
				if ( aResult.first() ) {
					long amount = aResult.getLong(1);
					if ( aResult.wasNull() ) {
						return null;
					}
					int scale = aResult.getInt(2);
					return BigDecimal.valueOf(amount, -1 * scale);
				}
				aResult.close();
				aPsth.close();
			} catch (SQLException e) {
				throw new DataException(e);
			}
		}	
		return null;
	}

	public String getDataFileClass() {
		return DATA_FILE_CLASS;
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

	
	public Compound getCompounds() throws DataException {
		return SQLCompound.compoundsForMaterial(myData, this.myID);
	}

	
	public boolean hasCompounds() throws DataException {
		return getCompounds().first();
	}

	
	public void unlinkCompound(Compound aCompound) throws DataException {
		this.unlinkCompoundID(aCompound.getID());
	}
	
	
	public void unlinkCompoundID(String compoundID) throws DataException {
		try {
			PreparedStatement aSth = this.myData.prepareStatement(SQL_UNLINK_CMPD);
			aSth.setString(1, compoundID);
			aSth.setString(2, this.myID);
			aSth.execute();
			aSth.close();
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}
	
	
	public void addCompoundID(String newValue, String separationID, String retentionTime) throws DataException {
		if ( retentionTime == null || retentionTime.length() < 1 ) {
			this.addCompoundID(newValue, 0.0f);
			return;
		}
		Double time = parseTime(retentionTime);
		if ( time != null ) {
			this.addCompoundID(newValue, time.doubleValue());
			return;
		}
		try {
			PreparedStatement aSth = this.myData.prepareStatement(SQL_LINK_CMPD);
			aSth.setString(1, newValue);
			aSth.setString(2, this.myID);
			aSth.setString(3, separationID);
			aSth.setString(4, retentionTime);
			aSth.execute();
			aSth.close();
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}
	
	
	public void addCompoundID(String newValue, String separationID, double retentionTime)
			throws DataException {
		try {
			PreparedStatement aSth = this.myData.prepareStatement(SQL_LINK_CMPD);
			aSth.setString(1, newValue);
			aSth.setString(2, this.myID);
			aSth.setString(3, separationID);
			aSth.setDouble(4, retentionTime);
			aSth.execute();
			aSth.close();
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}
	
	
	public void addCompoundID(String newValue) throws DataException {
		this.addCompoundID(newValue, null, 0.0);
	}

	
	public void addCompound(Compound aCompound) throws DataException {
		this.addCompoundID(aCompound.getID(), null, 0.0);
	}

	
	public void addCompound(Compound aCompound, String retentionTime) throws DataException {
		this.addCompoundID(aCompound.getID(), null, retentionTime);
	}

	
	public void addCompound(Compound aCompound, double retentionTime) throws DataException {
		this.addCompoundID(aCompound.getID(), null, retentionTime);
	}
	
	
	public void addCompoundID(String compoundID, String retentionTime) throws DataException {
		this.addCompoundID(compoundID, null, retentionTime);
	}

	
	public void addCompoundID(String compoundID, double retentionTime) throws DataException {
		this.addCompoundID(compoundID, null, retentionTime);
	}

	
	public void addCompound(Compound aCompound, Separation separation, String retentionTime) throws DataException {
		this.addCompoundID(aCompound.getID(), separation.getID(), retentionTime);
	}

	
	public void addCompound(Compound aCompound, Separation separation, double retentionTime) throws DataException {
		this.addCompoundID(aCompound.getID(), separation.getID(), retentionTime);
	}

	
	public Double getRetentionTime(Compound aCompound) throws DataException {
		return this.getRetentionTime(aCompound.getID());
	}

	
	public Double getRetentionTime(String compoundID) throws DataException {
		if ( this.myID != null ) {
			try {
				PreparedStatement aPsth = this.myData.prepareStatement(SQL_GET_RT_COMPOUND);
				aPsth.setString(1, this.myID);
				aPsth.setString(2, compoundID);
				ResultSet aResult = aPsth.executeQuery();
				Double retVal = null;
				if ( aResult.first()) {
					retVal = aResult.getDouble(1);
				}
				aResult.close();
				aPsth.close();
				return retVal;
			} catch (SQLException e) {
				throw new DataException(e);
			}
		}	
		return null;
	}

	public ExternalFile getChromatograms() throws DataException {
		return this.getDataFilesForType(LC_DATA_TYPE);
	}
	
	public void addChromatogram(ExternalFile aFile) throws DataException {
		this.linkDataFile(aFile, LC_DATA_TYPE);
	}
	
	public ExternalFile getNMRData() throws DataException {
		return this.getDataFilesForType(NMR_DATA_TYPE);
	}
	
	public void addNMRData(ExternalFile aFile) throws DataException {
		this.linkDataFile(aFile, NMR_DATA_TYPE);
	}
	
	public ExternalFile getMSData() throws DataException {
		return this.getDataFilesForType(MS_DATA_TYPE);
	}
	
	public void addMSData(ExternalFile aFile) throws DataException {
		this.linkDataFile(aFile, MS_DATA_TYPE);
	}
	
	public String[] dataTypes() {
		String[] retVals = { Material.LC_DATA_TYPE, Material.NMR_DATA_TYPE, Material.MS_DATA_TYPE };
		return retVals;
	}

	public ExtractProtocol createProtocol() throws DataException {
		ExtractProtocol aTemplate = new SQLExtractProtocol(this.myData);
		aTemplate.setExtractMethod(this.getExtractMethod());
		aTemplate.setExtractSolvent(this.getExtractSolvent());
		aTemplate.setExtractType(this.getExtractType());
		return aTemplate;
	}
	
	public void setProtocol(ExtractProtocol aTemplate) throws DataException {
		if ( this.myID != null )  {
			this.setExtractMethod(aTemplate.getExtractMethod());
			this.setExtractType(aTemplate.getExtractType());
			this.setExtractSolvent(aTemplate.getExtractSolvent());
		}
	}

	@Override
	public String getRemoteID() throws DataException {
		return this.myData.getString(REMOTE_ID_COLUMN);
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
	public boolean hasDataFile(String path) throws DataException {
		ExternalFile file = this.getDataFile(DATA_FILE_CLASS, this.myID, path);
		return ( file != null && file.first());
	}
	
	@Override
	public ExternalFile getDataFile(String path) throws DataException {
		return this.getDataFile(DATA_FILE_CLASS, this.myID, path);
	}
}
