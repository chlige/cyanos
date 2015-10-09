//
//  Harvest.java
//  Cyanos
//
//  Created by George Chlipala on 5/7/06.
//  Copyright 2006 Walnut Computer Services. All rights reserved.
//
package edu.uic.orjala.cyanos.sql;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import edu.uic.orjala.cyanos.Collection;
import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.Harvest;
import edu.uic.orjala.cyanos.Inoc;
import edu.uic.orjala.cyanos.Material;
import edu.uic.orjala.cyanos.Notebook;
import edu.uic.orjala.cyanos.Project;
import edu.uic.orjala.cyanos.Strain;
import edu.uic.orjala.cyanos.User;


public class SQLHarvest extends SQLObject implements Harvest {
   
	public static final String ID_COLUMN = "harvest_id";
	public static final String PREP_DATE_COLUMN = "prep_date";
	public static final String PROJECT_COLUMN = "project_id";
	public static final String COLOR_COLUMN = "color";
	public static final String NOTES_COLUMN = "notes";
	public static final String CULTURE_ID_COLUMN = "culture_id";
	public static final String COLLECTION_ID_COLUMN = "collection_id";
//	public static final String NAME_COLUMN = "name";
	
	public static final String NOTEBOOK_COLUMN = "notebook_id";
	public static final String NOTEBOOK_PAGE_COLUMN = "notebook_page";

	public static final String CELL_MASS_VALUE_COLUMN = "cell_mass_value";
	public static final String CELL_MASS_SCALE_COLUMN = "cell_mass_scale";

	public static final String MEDIA_VOLUME_VALUE_COLUMN = "media_volume_value";
	public static final String MEDIA_VOLUME_SCALE_COLUMN = "media_volume_scale";

	public final static String REMOTE_HOST_COLUMN = "remote_host";
	public final static String REMOTE_ID_COLUMN = "remote_id";

	public static final String DATE_COLUMN = "date";
	public static final String TYPE_COLUMN = "type";
	
	/*
	 * Parameters and SQL for "cleaned-up" database schema.
	 * 
	public static final String DATE_COLUMN = "added";
	public static final String TYPE_COLUMN = "harvest_type";
	 */
	
	private final static String[] ALL_COLUMNS = { ID_COLUMN, CULTURE_ID_COLUMN, DATE_COLUMN, 
		PREP_DATE_COLUMN, COLLECTION_ID_COLUMN,
		CELL_MASS_VALUE_COLUMN, CELL_MASS_SCALE_COLUMN, 
		MEDIA_VOLUME_VALUE_COLUMN, MEDIA_VOLUME_SCALE_COLUMN,
		TYPE_COLUMN, COLOR_COLUMN, 
		NOTES_COLUMN, PROJECT_COLUMN, 
	//	REMOVED_DATE_COLUMN, REMOVED_USER_COLUMN, 
	//	NOTEBOOK_COLUMN, NOTEBOOK_PAGE_COLUMN, 
		REMOTE_HOST_COLUMN, REMOTE_ID_COLUMN };	

	final static String SQL_BASE = sqlBase("harvest", ALL_COLUMNS);
	
	private static final String SQL_LOAD = SQL_BASE + " WHERE harvest_id=?";
	private static final String SQL_ADD_HARVEST = "INSERT INTO harvest(culture_id, date, project_id, remote_id) VALUES(?, CURRENT_DATE, ?, UUID())";
	private static final String SQL_ADD_REMOTE_HARVEST = "INSERT INTO harvest(project_id, remote_host, remote_id) VALUES(?, ?, ?)";
	private static final String SQL_LOAD_EXTRACTS = SQLMaterial.SQL_BASE + " JOIN extract_info ON(extract_info.material_id = material.material_id) WHERE extract_info.harvest_id=?";
	private static final String SQL_LOAD_INOCS = "SELECT inoculation.* FROM inoculation WHERE harvest_id=?";
	private static final String SQL_LOAD_TEMPLATE = SQL_BASE + " WHERE %s ORDER BY %s %s";
	
	public static SQLHarvest harvestsForStrain(SQLData data, Strain aStrain) throws DataException {
		return SQLHarvest.harvestsForStrain(data, aStrain.getID());
	}
	
	public static List<String> types(SQLData data) throws DataException, SQLException {
		return data.getPossibles("SHOW COLUMNS FROM harvest LIKE 'type'");
	}

	
	public static SQLHarvest harvestsForStrain(SQLData data, String aStrainID) throws DataException {
		SQLHarvest aHarvest = new SQLHarvest(data);
		String[] columns = { CULTURE_ID_COLUMN };
		String[] values = { aStrainID };
		String[] operators = {};
		aHarvest.loadWhere(SQL_LOAD_TEMPLATE, columns, values, operators, ID_COLUMN, ASCENDING_SORT);
		return aHarvest;
	}
	
	public static SQLHarvest harvestsWhere(SQLData data, String[] columns, String[] values, String[] operators, String sortColumn, String sortDirection) throws DataException {
		SQLHarvest aHarvest = new SQLHarvest(data);
		aHarvest.loadWhere(SQL_LOAD_TEMPLATE, columns, values, operators, sortColumn, sortDirection);
		return aHarvest;
	}
	
	public static SQLHarvest load(SQLData data, String id) throws DataException {
		SQLHarvest object = new SQLHarvest(data);
		object.myID = id;
		object.fetchRecord();
		return object;
	}
	
	public static SQLHarvest load(SQLData data, String hostID, String remoteID) throws DataException {
		SQLHarvest object = new SQLHarvest(data);
		try { 
			PreparedStatement aSth = object.myData.prepareStatement(SQL_BASE + " WHERE remote_host = ? AND remote_id = ?");
			aSth.setString(1, hostID);
			aSth.setString(2, remoteID);
			object.fetchRecord(aSth);
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return object;
	}
	
	protected SQLHarvest(SQLData data) {
		super(data);
		this.initVals();
	}

	@Deprecated
	public SQLHarvest(SQLData data, String anID) throws DataException {
		this(data);
		this.myID = anID;
		this.fetchRecord();
	}
	
	protected void initVals() {
		this.idField = "harvest_id";
		this.myData.setProjectField(PROJECT_COLUMN);
		this.myData.setAccessRole(User.CULTURE_ROLE);
	}
	
	protected void fetchRecord() throws DataException {
		this.fetchRecord(SQL_LOAD);
	}
	
	/**
	 * Creates a new Harvest record not associated with a project.
	 * 
	 * @param data SQLData object.
	 * @param strainID ID of the parent strain.
	 * @return Harvest object for the new record.
	 * @throws DataException
	 */
	public static SQLHarvest create(SQLData data, String strainID) throws DataException {
		return SQLHarvest.createInProject(data, strainID, null);
	}
	
	/**
	 * Creates a new Harvest record in the specified project. 
	 * This method should be used to ensure data security of new objects.
	 * 
	 * @param data SQLData object.
	 * @param strainID ID of the parent strain.
	 * @param projectID ID of the project.
	 * @return Harvest object for the new record.
	 * @throws DataException
	 */
	public static SQLHarvest createInProject(SQLData data, String strainID, String projectID) throws DataException {
		SQLHarvest aHarv = new SQLHarvest(data);
		try {
			PreparedStatement aSth = aHarv.myData.prepareStatement(SQL_ADD_HARVEST);
			aSth.setString(1, strainID);
			aSth.setString(2, projectID);
			aHarv.makeNewWithAutonumber(aSth);
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return aHarv;
	}
	
	/**
	 * Creates a new Harvest record in the specified project. 
	 * This method should be used to ensure data security of new objects.
	 * 
	 * @param data SQLData object.
	 * @param strainID ID of the parent strain.
	 * @param projectID ID of the project.
	 * @return Harvest object for the new record.
	 * @throws DataException
	 */
	public static SQLHarvest createInProject(SQLData data, String projectID, String remoteHost, String remoteID) throws DataException {
		SQLHarvest aHarv = new SQLHarvest(data);
		try {
			PreparedStatement aSth = aHarv.myData.prepareStatement(SQL_ADD_REMOTE_HARVEST);
			aSth.setString(1, projectID);
			aSth.setString(2, remoteHost);
			aSth.setString(3, remoteID);
			aHarv.makeNewWithAutonumber(aSth, projectID);
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return aHarv;
	}
	
	private static final String SQL_LOAD_FOR_PROJECT_UPDATED = SQL_BASE + " WHERE project_id = ? AND last_updated > ?";
	
	/**
	 * Load harvest records for a project that have been updated since the date given.
	 * 
	 * @param data SQLData object
	 * @param projectID ID of the project
	 * @param lastUpdate Date of the last update
	 * @return
	 * @throws SQLException
	 * @throws DataException
	 */

	public static SQLHarvest loadForProjectLastUpdated(SQLData myData, String projectID, Date lastUpdate) throws DataException {
		SQLHarvest results = new SQLHarvest(myData);
		try { 
			PreparedStatement aPsth = results.myData.prepareStatement(SQL_LOAD_FOR_PROJECT_UPDATED);
			aPsth.setString(1, projectID);
			Timestamp update = new Timestamp(lastUpdate.getTime());
			aPsth.setTimestamp(2, update);
			results.loadUsingPreparedStatement(aPsth);
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return results;
	}
	
	/**
	 * Load harvest records for a project that have been update since the date given, exclude records from the selected host.
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
	public static SQLHarvest loadForProjectLastUpdated(SQLData data, String projectID, Date lastUpdate, String hostID) throws SQLException, DataException {
		SQLHarvest results = new SQLHarvest(data);
		try {
			PreparedStatement aPsth = results.myData.prepareStatement(SQL_LOAD_FOR_PROJECT_UPDATED + " AND " + REMOTE_HOST_COLUMN + " <> ?");
			aPsth.setString(1, projectID);
			Timestamp update = new Timestamp(lastUpdate.getTime());
			aPsth.setTimestamp(2, update);
			aPsth.setString(3, hostID);
			results.loadUsingPreparedStatement(aPsth);
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return results;
	}


	public String getStrainID() throws DataException {
		return this.myData.getString(CULTURE_ID_COLUMN);
	}
	
	/**
	 * @return Strain object for harvest
	 * @throws DataException 
	 * @see Strain
	 */
	@Override
	public Strain getStrain() throws DataException {
		return SQLStrain.load(this.myData, this.myData.getString(CULTURE_ID_COLUMN));
	}
	
	@Override
	public Date getDate() throws DataException {
		return this.myData.getDate(DATE_COLUMN);
	}
	
	@Override
	public String getColor() throws DataException  {
		return this.myData.getString(COLOR_COLUMN);
	}
	
	@Override
	public String getType() throws DataException  {
		return this.myData.getString(TYPE_COLUMN);
	}
	
	@Override
	public BigDecimal getCellMass() throws DataException  {
		return this.myData.getDecimal(CELL_MASS_VALUE_COLUMN, CELL_MASS_SCALE_COLUMN);
	}
	
	@Override
	public BigDecimal getMediaVolume() throws DataException  {
		return this.myData.getDecimal(MEDIA_VOLUME_VALUE_COLUMN, MEDIA_VOLUME_SCALE_COLUMN);
	}
	
	@Override
	public Date getPrepDate() throws DataException {
		return this.myData.getDate(PREP_DATE_COLUMN);
	}
	
	@Override
	public String getPrepDateString() throws DataException {
		return this.myData.getString(PREP_DATE_COLUMN);
	}
	
	@Override
	public String getProjectID() throws DataException {
		return this.myData.getString(PROJECT_COLUMN);
	}
	
	@Override
	public Project getProject() throws DataException {
		String projID = this.getProjectID();
		if ( projID != null ) {
			Project aProj = SQLProject.load(this.myData, projID);
			if ( aProj.first() )
				return aProj;
		}
		return null;
	}
	
	@Override
	public void setProjectID(String newValue) throws DataException {
		this.myData.setStringNullBlank(PROJECT_COLUMN, newValue);
	}
	
	@Override
	public void setProject(Project aProject) throws DataException {
		if ( aProject != null )
			this.setProjectID(aProject.getID());
		else 
			this.myData.setNull(PROJECT_COLUMN);
	}

	/**
	 * Sets source strain to aStrain
	 * 
	 * @param aStrain Strain Object
	 * @throws DataException 
	 */
	@Override
	public void setStrain(Strain aStrain) throws DataException {
		this.setStrainID(aStrain.getID());
	}

	/**
	 * Sets source strain ID to anID
	 * 
	 * @param anID String
	 * @throws DataException 
	 */
	@Override
	public void setStrainID(String anID) throws DataException {
		this.myData.setString(CULTURE_ID_COLUMN, anID);
	}

	@Override
	public void setPrepDate(String newDate) throws DataException {
		this.myData.setString(PREP_DATE_COLUMN, newDate);
	}
	
	@Override
	public void setPrepDate(Date newDate) throws DataException {
		this.myData.setDate(PREP_DATE_COLUMN, newDate);
	}
	
	@Override
	public String getNotes() throws DataException  {
		return this.myData.getString(NOTES_COLUMN);
	}
	
	@Override
	public void setDate(Date newDate) throws DataException {
		this.myData.setDate(DATE_COLUMN, newDate);
	}
	
	@Override
	public void setDate(String newDate) throws DataException {
		this.myData.setString(DATE_COLUMN, newDate);
	}
	
	@Override
	public void setColor(String newColor) throws DataException  {
		this.myData.setString(COLOR_COLUMN, newColor);
	}
	
	@Override
	public void setType(String newType) throws DataException  {
		this.myData.setString(TYPE_COLUMN, newType);
	}
	
	@Override
	public void setCellMass(BigDecimal newCellmass) throws DataException  {
		this.myData.setDecimal(CELL_MASS_VALUE_COLUMN, CELL_MASS_SCALE_COLUMN, newCellmass);
	}
	
	@Override
	public void setCellMass(String newCellMass) throws DataException {
		this.setCellMass(parseAmount(newCellMass));
	}
	
	@Override
	public void setMediaVolume(BigDecimal newVol) throws DataException  {
		this.myData.setDecimal(MEDIA_VOLUME_VALUE_COLUMN, MEDIA_VOLUME_SCALE_COLUMN, newVol);
	}
	
	@Override
	public void setMediaVolume(String newVol) throws DataException  {
		this.setMediaVolume(parseAmount(newVol));
	}
	
	@Override
	public void setNotes(String newNotes) throws DataException  {
		this.myData.setString(NOTES_COLUMN, newNotes);
	}
	
	@Override
	public void addNotes(String newNotes) throws DataException  {
		StringBuffer curNotes = new StringBuffer(this.myData.getString(NOTES_COLUMN));
		curNotes.append(" ");
		curNotes.append(newNotes);
		this.myData.setString(NOTES_COLUMN, curNotes.toString());
	}

	@Override
	public Inoc getInoculations() throws DataException {
		if ( this.myData.isNull(COLLECTION_ID_COLUMN) ) {
			SQLInoc anInoc = new SQLInoc(this.myData);
			try {
				PreparedStatement aPsth = this.myData.prepareStatement(SQL_LOAD_INOCS);
				aPsth.setString(1, this.myID);
				anInoc.loadUsingPreparedStatement(aPsth);
				if ( anInoc.first() )
					return anInoc;
			} catch (SQLException e) {
				throw new DataException(e);
			}
		}
		return null;
	}

	@Override
	public boolean isFieldHarvest() throws DataException {
		return (! this.myData.isNull(COLLECTION_ID_COLUMN));
	}
	
	@Override
	public String getCollectionID() throws DataException {
		return this.myData.getString(COLLECTION_ID_COLUMN);
	}
	
	@Override
	public Collection getCollection() throws DataException {
		String colID = this.getCollectionID();
		if ( colID != null ) {
			Collection aCol = SQLCollection.load(this.myData, colID);
			if (aCol.first()) return aCol;
		}
		return null;
	}
	
	@Override
	public void setCollectionID(String newValue) throws DataException {
		this.myData.setString(COLLECTION_ID_COLUMN, newValue);
	}
	
	@Override
	public void setCollection(Collection aCol) throws DataException {
		this.setCollectionID(aCol.getID());
	}
	
	@Override
	public void unlinkCollection() throws DataException {
		this.myData.setNull(COLLECTION_ID_COLUMN);
	}
	
	@Override
	public Material getExtract() throws DataException {
		if ( this.myID != null ) {
			SQLMaterial aSample = new SQLMaterial(this.myData);
			try {
				PreparedStatement aPsth = this.myData.prepareStatement(SQL_LOAD_EXTRACTS);
				aPsth.setString(1, this.myID);
				aSample.loadUsingPreparedStatement(aPsth);
				if ( aSample.first() )
					return aSample;
			} catch (SQLException e) {
				throw new DataException(e);
			}
		}
		return null;
	}
	
	@Override
	public Material createExtract() throws DataException {
		if ( this.myID != null ) {
			SQLMaterial aSample = SQLMaterial.createInProject(this.myData, this.getStrainID(), this.getProjectID());
			if ( aSample.makeExtract(this.myID) ) 
				return aSample;
		}
		return null;
	}

	@Override
	public Material createExtract(String label) throws DataException {
		if ( this.myID != null ) {
			SQLMaterial aSample = SQLMaterial.createInProject(this.myData, this.getStrainID(), label, this.getProjectID());
			if ( aSample.makeExtract(this.myID) ) 
				return aSample;
		}
		return null;
	}


//	public String getName() throws DataException {
//		return this.myData.getString(NAME_COLUMN);
//	}

//	public void setName(String aName) throws DataException {
//		this.myData.setString(NAME_COLUMN, aName);
//	}
	
	@Override
	public Notebook getNotebook() throws DataException {
		String notebookID = this.myData.getString(NOTEBOOK_COLUMN);
		if ( notebookID != null ) {
			Notebook aNotebook = new SQLNotebook(this.myData, notebookID);
			return aNotebook;
		}
		return null;
	}

	@Override
	public String getNotebookID() throws DataException {
		return this.myData.getString(NOTEBOOK_COLUMN);
	}

	@Override
	public int getNotebookPage() throws DataException {
		return this.myData.getInt(NOTEBOOK_PAGE_COLUMN);
	}

	@Override
	public void setNotebook(Notebook aNotebook) throws DataException {
		if ( aNotebook != null ) 
			this.myData.setString(NOTEBOOK_COLUMN, aNotebook.getID());
		else 
			this.myData.setNull(NOTEBOOK_COLUMN);
	}

	@Override
	public void setNotebook(Notebook aNotebook, int aPage) throws DataException {
		if ( aNotebook != null ) {
			this.myData.setString(NOTEBOOK_COLUMN, aNotebook.getID());
			this.myData.setInt(NOTEBOOK_PAGE_COLUMN, aPage);
		} else {
			this.myData.setNull(NOTEBOOK_COLUMN);
			this.myData.setNull(NOTEBOOK_PAGE_COLUMN);
		}
	}

	@Override
	public void setNotebookID(String anID) throws DataException {
		this.myData.setStringNullBlank(NOTEBOOK_COLUMN, anID);
	}

	@Override
	public void setNotebookID(String anID, int aPage) throws DataException {
		if ( anID.length() > 0 ) {
			this.myData.setString(NOTEBOOK_COLUMN, anID);
			this.myData.setInt(NOTEBOOK_PAGE_COLUMN, aPage);
		} else {
			this.myData.setNull(NOTEBOOK_COLUMN);
			this.myData.setNull(NOTEBOOK_PAGE_COLUMN);
		}
	}

	@Override
	public void setNotebookPage(int aPage) throws DataException {
		this.myData.setInt(NOTEBOOK_PAGE_COLUMN, aPage);
	}

	@Override
	public String getRemoteID() throws DataException {
		return this.myData.getString(REMOTE_ID_COLUMN);
	}

	@Override
	public String getRemoteHostID() throws DataException {
		return this.myData.getString(REMOTE_HOST_COLUMN);
	}

}
