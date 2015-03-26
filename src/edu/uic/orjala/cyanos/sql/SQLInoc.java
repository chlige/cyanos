//
//  Inoc.java
//  Cyanos
//
//  Created by George Chlipala on 5/7/06.
//  Copyright 2006 Walnut Computer Services. All rights reserved.
//
package edu.uic.orjala.cyanos.sql;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.uic.orjala.cyanos.Cryo;
import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.Harvest;
import edu.uic.orjala.cyanos.Inoc;
import edu.uic.orjala.cyanos.Notebook;
import edu.uic.orjala.cyanos.Project;
import edu.uic.orjala.cyanos.Strain;
import edu.uic.orjala.cyanos.User;

/**
 * Class to manipulate inoculation objects
 * 
 * @author George Chlipala
 * @version 1.0
 *
 */
public class SQLInoc extends SQLObject implements Inoc {
	
	public static final String ID_COLUMN = "inoculation_id";
	public static final String PROJECT_COLUMN = "project_id";
	public static final String CULTURE_ID_COLUMN = "culture_id";
	public static final String PARENT_ID_COLUMN = "parent_id";
	public static final String HARVEST_ID_COLUMN = "harvest_id";
	public static final String MEDIA_COLUMN = "media";
	public static final String VOLUME_VALUE_COLUMN = "volume_value";
	public static final String VOLUME_SCALE_COLUMN = "volume_scale";
	public static final String FATE_COLUMN = "fate";
	public static final String REMOVED_COLUMN = "removed";
	public static final String NOTES_COLUMN = "notes";
	public static final String NOTEBOOK_COLUMN = "notebook_id";
	public static final String NOTEBOOK_PAGE_COLUMN = "notebook_page";

	public static final String DATE_COLUMN = "date";
	private static final String INSERT_INOC_SQL = "INSERT INTO inoculation(culture_id,date) VALUES(?,CURRENT_DATE)";
	private static final String SQL_INSERT_WITH_PROJECT = "INSERT INTO inoculation(culture_id,date,project_id) VALUES(?,CURRENT_DATE,?)";
	private static final String SQL_INSERT_KID = "INSERT INTO inoculation(culture_id,project_id,parent_id,date) VALUES(?,?,?,CURRENT_DATE)";

	private static final String SQL_INSERT_WITH_PROJECT_VOL_MEDIA = "INSERT INTO inoculation(culture_id,date,project_id,volume_value,volume_scale,media) VALUES(?,CURRENT_DATE,?,?,?,?)";

	/*
	 * Parameters and SQL for "cleaned-up" database schema.
	 * 	
	public static final String DATE_COLUMN = "added";
	private static final String INSERT_INOC_SQL = "INSERT INTO inoculation(added) VALUES(CURRENT_DATE)";
	private static final String SQL_INSERT_WITH_PROJECT = "INSERT INTO inoculation(added,project_id) VALUES(CURRENT_DATE,?)";

	 */
	
	private static final String SQL_LOAD = "SELECT inoculation.* FROM inoculation WHERE inoculation_id=?";
	private static final String SQL_LOAD_CRYO_PARENT = "SELECT cryo.* FROM cryo WHERE thaw_id=?";
	private static final String SQL_LOAD_KIDS = "SELECT inoculation.* FROM inoculation WHERE parent_id=? ORDER BY inoculation_id";
	private static final String SQL_LOAD_TEMPLATE = "SELECT inoculation.* FROM inoculation WHERE %s ORDER BY %s %s";
	private static final String SQL_OPEN_FOR_STRAIN = "SELECT inoculation.* FROM inoculation WHERE culture_id=? AND fate IS NULL ORDER BY date";
	private static final String SQL_VIABLE_FOR_STRAIN = "SELECT inoculation.* FROM inoculation WHERE culture_id=? AND removed IS NULL ORDER BY date";
	private static final String SQL_CURRENT_STOCK_FOR_STRAIN = "SELECT inoculation.* FROM inoculation WHERE culture_id=? AND fate='stock' AND removed IS NULL ORDER BY date";
	private static final String SQL_ALL_STOCK_FOR_STRAIN = "SELECT inoculation.* FROM inoculation WHERE culture_id=? AND fate='stock' ORDER BY date";
	private static final String SQL_LOAD_FOR_STRAIN = "SELECT inoculation.* FROM inoculation WHERE culture_id=? ORDER BY date";
	
	public static List<String> fates(SQLData data) throws DataException, SQLException {
		return data.getPossibles("SHOW COLUMNS FROM inoculation LIKE 'fate'");
	}
	
	public static Inoc inocsWhere(SQLData data, String[] columns, String[] values, String[] operators, String sortColumn, String sortDirection) throws DataException {
		SQLInoc anInoc = new SQLInoc(data);
		anInoc.loadWhere(SQL_LOAD_TEMPLATE, columns, values, operators, sortColumn, sortDirection);
		return anInoc;
	}
	
	public static Inoc inocsForStrain(SQLData data, String strainID) throws DataException {
		SQLInoc anInoc = new SQLInoc(data);
		try {
			PreparedStatement aPsth = anInoc.myData.prepareStatement(SQL_LOAD_FOR_STRAIN);
			aPsth.setString(1, strainID);
			anInoc.myData.loadUsingPreparedStatement(aPsth);
			if ( anInoc.first() ) 
				return anInoc;
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return null;		
	}
	
	public static Inoc openInocsForStrain(SQLData data, String strainID) throws DataException {
		SQLInoc anInoc = new SQLInoc(data);
		try {
			PreparedStatement aPsth = anInoc.myData.prepareStatement(SQL_OPEN_FOR_STRAIN);
			aPsth.setString(1, strainID);
			anInoc.myData.loadUsingPreparedStatement(aPsth);
			if ( anInoc.first() ) 
				return anInoc;
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return null;
	}
	
	public static Inoc viableInocsForStrain(SQLData data, String strainID) throws DataException {
		SQLInoc anInoc = new SQLInoc(data);
		try {
			PreparedStatement aPsth = anInoc.myData.prepareStatement(SQL_VIABLE_FOR_STRAIN);
			aPsth.setString(1, strainID);
			anInoc.myData.loadUsingPreparedStatement(aPsth);
			if ( anInoc.first() ) 
				return anInoc;
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return null;
	}
	
	public static Inoc currentStocksForStrain(SQLData data, String strainID) throws DataException {
		SQLInoc anInoc = new SQLInoc(data);
		try {
			PreparedStatement aPsth = anInoc.myData.prepareStatement(SQL_CURRENT_STOCK_FOR_STRAIN);
			aPsth.setString(1, strainID);
			anInoc.myData.loadUsingPreparedStatement(aPsth);
			if ( anInoc.first() ) 
				return anInoc;
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return null;
	}
	
	public static Inoc allStocksForStrain(SQLData data, String strainID) throws DataException {
		SQLInoc anInoc = new SQLInoc(data);
		try {
			PreparedStatement aPsth = anInoc.myData.prepareStatement(SQL_ALL_STOCK_FOR_STRAIN);
			aPsth.setString(1, strainID);
			anInoc.myData.loadUsingPreparedStatement(aPsth);
			if ( anInoc.first() ) 
				return anInoc;
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return null;
	}
	
	public static SQLInoc load(SQLData data, String id) throws DataException {
		SQLInoc object = new SQLInoc(data);
		object.myID = id;
		object.fetchRecord();
		return object;
	}
	
	protected SQLInoc(SQLData data) {
		super(data);
		this.initVals();
	}

	@Deprecated
	public SQLInoc(SQLData data, String anID) throws DataException {
		this(data);
		this.myID = anID;
		this.fetchRecord();
	}
	
	protected void initVals() {
		this.idField = "inoculation_id";
		this.myData.setProjectField(PROJECT_COLUMN);
		this.myData.setAccessRole(User.CULTURE_ROLE);
	}
	
	public static Inoc create(SQLData data, String strainID) throws DataException {
		SQLInoc anObj = new SQLInoc(data);
		try { 
			PreparedStatement aSth = anObj.myData.prepareStatement(INSERT_INOC_SQL);
			aSth.setString(1, strainID);
			anObj.makeNewWithAutonumber(aSth);
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return anObj;
	}
	
	/**
	 * Creates a new inoculation record in the specified project. 
	 * This method should be used to ensure data security of new objects.
	 * 
	 * @param projectID ID of the project.
	 * @throws DataException
	 */
	public static Inoc createInProject(SQLData data, String strainID, String projectID) throws DataException {
		SQLInoc anObj = new SQLInoc(data);
		try { 
			PreparedStatement aSth = anObj.myData.prepareStatement(SQL_INSERT_WITH_PROJECT);
			aSth.setString(1, strainID);
			aSth.setString(2, projectID);
			anObj.makeNewWithAutonumber(aSth);
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return anObj;
	}

	/**
	 * Creates a new inoculation record in the specified project. 
	 * This method should be used to ensure data security of new objects.
	 * 
	 * @param projectID ID of the project.
	 * @throws DataException
	 */
	public static Inoc createInProject(SQLData data, String strainID, String projectID, BigDecimal volume, String media) throws DataException {
		SQLInoc anObj = new SQLInoc(data);
		try { 
			PreparedStatement aSth = anObj.myData.prepareStatement(SQL_INSERT_WITH_PROJECT_VOL_MEDIA);
			aSth.setString(1, strainID);
			aSth.setString(2, projectID);
			BigInteger unscaledValue = volume.unscaledValue();
			aSth.setLong(3, unscaledValue.longValue());
			aSth.setInt(4, -1 * volume.scale());
			aSth.setString(5, media);
			anObj.makeNewWithAutonumber(aSth);
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return anObj;
	}

	
	protected void fetchRecord() throws DataException {
		this.fetchRecord(SQL_LOAD);
	}
	
	/**
	 * @return Strain object for inoculation
	 * @throws DataException 
	 * @see Strain
	 */
	public Strain getStrain() throws DataException {
		Strain myStrain = SQLStrain.load(this.myData, this.myData.getString(CULTURE_ID_COLUMN));
		return myStrain;
	}
	
	public String getStrainID() throws DataException {
		return this.myData.getString(CULTURE_ID_COLUMN);
	}
	
	/**
	 * @return Inoc object for parent inoculation
	 * @throws DataException 
	 */
	public Inoc getParent() throws DataException {
		String parentID = this.myData.getString(PARENT_ID_COLUMN);
		if ( parentID != null ) {
			Inoc parent = SQLInoc.load(this.myData, parentID);
			return parent;
		} 
		return null;
	}

	/**
	 * @return Harvest object for harvest or null if non-existant
	 * @throws DataException 
	 * @see Harvest
	 */
	public Harvest getHarvest() throws DataException {
		String harvID = this.myData.getString(HARVEST_ID_COLUMN);
		if ( harvID != null ) {
			Harvest myHarvest = SQLHarvest.load(myData, harvID);
			return myHarvest;
		} 
		return null;
	}

	public String getHarvestID() throws DataException {
		return this.myData.getString(HARVEST_ID_COLUMN);
	}
	
	/**
	 * @return Date object
	 * @throws DataException 
	 */
	public Date getDate() throws DataException {
		return this.myData.getDate(DATE_COLUMN);
	}
	
	/**
	 * @return String 
	 */
	public String getMedia() throws DataException {
		return this.myData.getString(MEDIA_COLUMN);
	}
	
	public BigDecimal getVolume() throws DataException {
		return this.myData.getDecimal(VOLUME_VALUE_COLUMN, VOLUME_SCALE_COLUMN);
	}
	
	public String getVolumeString(float cutOff) throws DataException {
		BigDecimal myVol = this.getVolume();
		if ( myVol != null && myVol.floatValue() < cutOff ) {
			return formatAmount(myVol, "mL");					
		} else {
			return formatAmount(myVol, "L");									
		}

	}
	
	public String getVolumeString() throws DataException {
		BigDecimal myVol = this.getVolume();
		return autoFormatAmount(myVol, VOLUME_TYPE);
	}
	
	public String getNotes() throws DataException {
		return this.myData.getString(NOTES_COLUMN);
	}
	
	public String getFate() throws DataException {
		return this.myData.getString(FATE_COLUMN);
	}
	
	public Date getRemoveDate() throws DataException {
		return this.myData.getDate(REMOVED_COLUMN);
	}
	
	public String getProjectID() throws DataException {
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
		/**
	 * Sets source strain to aStrain
	 * 
	 * @param aStrain Strain Object
	 * @throws DataException 
	 */
	public void setStrain(Strain aStrain) throws DataException {
		this.setStrainID(aStrain.getID());
	}

	/**
	 * Sets source strain ID to anID
	 * 
	 * @param anID String
	 * @throws DataException 
	 */
	public void setStrainID(String anID) throws DataException {
		this.myData.setString(CULTURE_ID_COLUMN, anID);
	}


	/**
	 * Sets parent to aParent
	 * 
	 * @param aParent Inoc Object
	 * @throws DataException 
	 */
	public void setParent(Inoc aParent) throws DataException {
		this.setParentID(aParent.getID());
	}
	
	/**
	 * Sets parent ID to anID
	 * 
	 * @param anID String
	 * @throws DataException 
	 */
	public void setParentID(String anID) throws DataException {
		this.myData.setString(PARENT_ID_COLUMN, anID);
	}
	

	/**
	 * Sets harvest to aHarvest
	 * 
	 * @param aHarvest Harvest Object
	 * @throws DataException 
	 */
	public void setHarvest(Harvest aHarvest) throws DataException {
		this.myData.setString(HARVEST_ID_COLUMN, aHarvest.getID());
		this.myData.setString(FATE_COLUMN, FATE_HARVEST);
		this.setRemovedDate(aHarvest.getDate());
	}
	
	public void setHarvest(String aHarvest) throws DataException {
		this.setHarvest(SQLHarvest.load(myData, aHarvest));
	}
	
	public Harvest createHarvest() throws DataException {
		Harvest aHarv = SQLHarvest.createInProject(this.myData, this.getStrainID(), this.getProjectID());
		if ( aHarv.first() ) {
			this.myData.setString(HARVEST_ID_COLUMN, aHarv.getID());
			aHarv.setStrainID(this.getStrainID());
			return aHarv;
		}
		return null;
	}

	public Inoc createChild() throws DataException {
		SQLInoc aKid = new SQLInoc(this.myData);
		try { 
			PreparedStatement aSth = aKid.myData.prepareStatement(SQL_INSERT_KID);
			aSth.setString(1, this.getStrainID());
			aSth.setString(2, this.getProjectID());
			aSth.setString(3, this.myID);
			aKid.makeNewWithAutonumber(aSth);
		} catch (SQLException e) {
			throw new DataException(e);
		}		
		if ( aKid.first() ) {
			return aKid;
		}
		return null;
	}

	public void setMedia(String newMedia) throws DataException {
		this.myData.setString(MEDIA_COLUMN, newMedia);
	}

	public void setVolume(String newVol) throws DataException  {
		this.setVolume(parseAmount(newVol, "L"));
	}

	public void setVolume(BigDecimal newVol) throws DataException  {
		this.myData.setDecimal(VOLUME_VALUE_COLUMN, VOLUME_SCALE_COLUMN, newVol);
	}
	
	public void setNotes(String newNotes) throws DataException  {
		this.myData.setString(NOTES_COLUMN, newNotes);
	}
	
	public void setDate(java.util.Date newDate) throws DataException  {
		this.myData.setDate(DATE_COLUMN, (java.sql.Date)newDate);
	}
	
	public void setDate(String newDate) throws DataException {
		this.myData.setString(DATE_COLUMN, newDate);
	}
	
	public void setRemovedDate(java.util.Date newDate) throws DataException  {
		this.myData.setDate(REMOVED_COLUMN, (java.sql.Date)newDate);
	}
	
	public void setRemovedDate(String newDate) throws DataException  {
		if ( newDate.equals("") )
			this.myData.setNull(REMOVED_COLUMN);
		else 
			this.myData.setString(REMOVED_COLUMN, newDate);
	}
	
	public void setFate(String newFate) throws DataException  {
		if ( newFate.equals("") )
			this.myData.setNull(FATE_COLUMN);
		else
			this.myData.setString(FATE_COLUMN, newFate);
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

	/**
	 * @return Inoc object for children inoculations
	 * @throws DataException 
	 */
	public Inoc getChildren() throws DataException {
		SQLInoc kids = new SQLInoc(this.myData);
		try {
			PreparedStatement aPsth = this.myData.prepareStatement(SQL_LOAD_KIDS);
			aPsth.setString(1, this.myID);
			kids.loadUsingPreparedStatement(aPsth);
			if ( kids.first() ) 
				return kids;
		} catch ( SQLException e) {
			throw new DataException(e);
		}
		return null;
	}

	/**
	 * @return Cryo object for parent cryo.
	 * @throws DataException 
	 */
	public Cryo getCryoParent() throws DataException {
		SQLCryo aCryo = new SQLCryo(this.myData);
		try {
			PreparedStatement aPsth = this.myData.prepareStatement(SQL_LOAD_CRYO_PARENT);
			aPsth.setString(1, this.myID);
			aCryo.loadUsingPreparedStatement(aPsth);
			if ( aCryo.first() ) 
				return aCryo;
		} catch ( SQLException e) {
			throw new DataException(e);
		}
		return null;
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

}
