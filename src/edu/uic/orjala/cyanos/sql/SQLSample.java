package edu.uic.orjala.cyanos.sql;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import edu.uic.orjala.cyanos.AccessException;
import edu.uic.orjala.cyanos.Assay;
import edu.uic.orjala.cyanos.Compound;
import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.ExternalFile;
import edu.uic.orjala.cyanos.Harvest;
import edu.uic.orjala.cyanos.Material;
import edu.uic.orjala.cyanos.Project;
import edu.uic.orjala.cyanos.Role;
import edu.uic.orjala.cyanos.Sample;
import edu.uic.orjala.cyanos.SampleAccount;
import edu.uic.orjala.cyanos.SampleCollection;
import edu.uic.orjala.cyanos.Separation;
import edu.uic.orjala.cyanos.User;

public class SQLSample extends SQLObject implements Sample {

	// Setup the column names here so that changing is easier.	
	private final static String COLLECTION_COLUMN = "collection";
	private final static String NAME_COLUMN = "name";
	private final static String CULTURE_ID_COLUMN = "culture_id";
	private final static String NOTES_COLUMN = "notes";
	private final static String UNIT_COLUMN = "unit";
	private final static String VIAL_WEIGHT_COLUMN = "vial_wt";
	private final static String LIBRARY_SOURCE_COLUMN = "source_id";
	private final static String CONCENTRATION_COLUMN = "concentration";
	private final static String PROJECT_COLUMN = "project_id";
//	private final static String COMPOUND_ID_COLUMN = "compound_id";
	private final static String REMOVED_DATE_COLUMN = "removed_date";
	private final static String REMOVED_USER_COLUMN = "removed_by";
	private final static String MATERIAL_ID_COLUMN = "material_id";

	private final static String ROW_COLUMN = "row";
	private final static String COLUMN_COLUMN = "col";
	private final static String DATE_COLUMN = "date";
	private final static String SQL_INSERT_SAMPLE = "INSERT INTO sample(material_id, project_id, culture_id, date) (SELECT material_id, project_id, culture_id, CURRENT_DATE FROM material WHERE material_id=?)";
	private final static String SQL_INSERT_WITH_PROJECT = "INSERT INTO sample(material_id, project_id, culture_id, date) (SELECT material_id, ?, culture_id, CURRENT_DATE FROM material WHERE material_id=?)";
	private static final String SQL_LOAD_FROM_COLL_LOC = "SELECT sample.* FROM sample WHERE collection=? AND row=? AND col=?";
	private static final String SQL_LOAD_FOR_MATERIAL = "SELECT sample.* FROM sample WHERE material_id = ?";
	/*
	 * Parameters and SQL for "cleaned-up" database schema.
	 * 
	private final static String ROW_COLUMN = "loc_x";
	private final static String COLUMN_COLUMN = "loc_y";
	private final static String DATE_COLUMN = "added";
	private final static String SQL_INSERT_SAMPLE = "INSERT INTO sample(added) VALUES(CURRENT_DATE)";
	private final static String SQL_INSERT_WITH_PROJECT = "INSERT INTO sample(added,project_id) VALUES(CURRENT_DATE,?)";
	private static final String SQL_LOAD_FROM_COLL_LOC = "SELECT sample.* FROM sample WHERE collection=? AND loc_x=? AND loc_y=?";
	 */
	
//	private final static String SQL_UPDATE_LIBRARY_COMPOUND = "UPDATE sample SET compound_id=? WHERE source_id=?";
	private final static String SQL_GET_PARENT_SEPARATION = "SELECT separation_id FROM separation_product WHERE material_id=?";
	
	private final static String SQL_IS_EXTRACT = "SELECT * FROM extract_info WHERE material_id=?";
	
	protected boolean rowAlpha = true;
	protected boolean colAlpha = false;

/*
	private final static String SQL_MAKE_EXTRACT = "INSERT INTO extract_info(material_id,harvest_id) VALUES(?,?)";
	private static final String SQL_GET_EXTRACT_SOLVENT = "SELECT solvent FROM extract_info WHERE material_id=?";
	private static final String SQL_GET_EXTRACT_TYPE = "SELECT type FROM extract_info WHERE material_id=?";
	private static final String SQL_GET_EXTRACT_SOURCE = "SELECT harvest_id FROM extract_info WHERE material_id=?";
	private static final String SQL_SET_EXTRACT_SOLVENT = "UPDATE extract_info SET solvent=? WHERE material_id=?";
	private static final String SQL_SET_EXTRACT_TYPE = "UPDATE extract_info SET type=? WHERE material_id=?";
	private static final String SQL_SET_EXTRACT_SOURCE = "UPDATE extract_info SET harvest_id=? WHERE material_id=?";
*/	
	
	private static final String SQL_GET_SEP_AMOUNT = "SELECT amount FROM sample_acct WHERE sample_id=? AND ref_id=? AND ref_table='separation'";
	private static final String SQL_GET_HARVEST_AMOUNT = "SELECT amount FROM sample_acct WHERE sample_id=? AND ref_id=? AND ref_table='harvest'";
	private static final String SQL_GET_ASSAY_AMOUNT = "SELECT amount FROM sample_acct WHERE sample_id=? AND ref_id=? AND ref_table='assay'";
	
	private static final String SQL_LOAD_LIBRARY_KIDS = "SELECT sample.* FROM sample WHERE source_id = ? AND sample_id != ?";
//	private static final String SQL_LOAD_HARVEST = "SELECT harvest.* FROM havest JOIN extract_info ON (extract_info.harvest_id = harvest.harvest_id) WHERE extract_info.material_id=?";
//	private static final String SQL_LOAD_SEP_DESCEND = "SELECT separation.* FROM separation JOIN separation_source src USING(separation.separation_id src.separation_id) WHERE src.material_id=?";
	
	private static final String SQL_LOAD = "SELECT sample.* FROM sample WHERE sample_id=?";
	
	private static final String SQL_TRASH_SAMPLE = "INSERT INTO %s.sample(sample_id,collection,name,culture_id,notes,unit,vial_wt,source_id,concentration,loc_x,loc_y,compound_id,project_id,date_added) (SELECT sample_id,collection,name,culture_id,notes,unit,vial_wt,source_id,concentration,row,col,compound_id,project_id,date FROM sample WHERE sample_id=? OR source_id=?)";
	private static final String SQL_TRASH_ACCOUNT = "INSERT INTO %s.sample_acct(acct_id,sample_id,txn_date,ref_table,ref_id,void_date,void_user,amount,notes) (SELECT sa.acct_id,sa.sample_id,sa.date,sa.ref_table,sa.ref_id,sa.void_date,sa.void_user,sa.amount,sa.notes FROM sample_acct sa JOIN sample s ON (sa.sample_id=s.sample_id) WHERE s.sample_id=? OR s.source_id=?)";
	private static final String SQL_DELETE_SAMPLE = "DELETE sample, sample_acct FROM sample JOIN sample_acct ON (sample.sample_id=sample_acct.id) WHERE sample.sample_id=? OR sample.source_id=?";
	private static final String SQL_CHECK_PROJECT = "SELECT project_id FROM sample WHERE sample_id=?";
	private static final String DATA_FILE_TABLE = "sample";

	protected static final String SQL_GET_RT_COMPOUND = "SELECT retention_time FROM compound_peaks WHERE sample_id=? AND compound_id=?";
	protected static final String SQL_GET_SEP_COMPOUND = "SELECT separation_id FROM compound_peaks WHERE sample_id=? AND compound_id=?";
	
	/*
	 * Old SQL Statements

	private final static String SQL_INSERT_CMPD_WITH_PROJECT = "INSERT INTO sample(compound_id,date,project_id) VALUES(?,CURRENT_DATE,?)";
	private final static String SQL_INSERT_CMPD = "INSERT INTO sample(compound_id,date) VALUES(?,CURRENT_DATE)";
	*/
	
	private final static String SQL_LINK_CMPD = "INSERT INTO compound_peaks(compound_id,sample_id,retention_time) VALUES(?,?,?)";
	private final static String SQL_UNLINK_CMPD = "DELETE FROM compound_peaks WHERE compound_id = ? AND sample_id = ?";
	
	public static void delete(SQLData data, String sampleID) throws DataException {
		try {
			PreparedStatement aSth = data.prepareStatement(SQL_CHECK_PROJECT);
			aSth.setString(1, sampleID);
			ResultSet aResult = aSth.executeQuery();
			if ( aResult.first() ) {
				String projectID = aResult.getString(1);
				aSth.close();
				if ( data.getUser().isAllowed(User.SAMPLE_ROLE, projectID, Role.DELETE) ) {
					boolean deleteObj = false;
					if ( data.hasTrash() ) {
						aSth = data.prepareStatement(String.format(SQL_TRASH_ACCOUNT, data.getTrashCatalog()));
						aSth.setString(1, sampleID);
						aSth.setString(2, sampleID);
						if ( (aSth.executeUpdate() > 0) ) {
							aSth.close();
							aSth = data.prepareStatement(String.format(SQL_TRASH_SAMPLE, data.getTrashCatalog()));
							aSth.setString(1, sampleID);
							aSth.setString(2, sampleID);
							deleteObj= (aSth.executeUpdate() > 0);
						}
					} else {
						deleteObj = true;
					}
					aSth.close();
					if ( deleteObj ) {
						aSth = data.prepareStatement(SQL_DELETE_SAMPLE);
						aSth.setString(1, sampleID);
						aSth.setString(2, sampleID);
						if ( aSth.executeUpdate() > 0 ) {
							aSth.close();
							SQLSampleAccount.voidReferences(data, "sample", sampleID);
						}
					}
				} else {
					throw new AccessException(data.getUser(), User.SAMPLE_ROLE, Role.DELETE);
				}
			}
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

/*
	public static Sample createForCompound(SQLData data, String compoundID) throws DataException {
		SQLSample aSample = new SQLSample(data);
		try {
			PreparedStatement aSth = aSample.myData.prepareStatement(SQL_LINK_CMPD);
			aSth.setString(1, compoundID);
			aSample.makeNewWithAutonumber(aSth);
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return aSample;
	}
	
	public static Sample createForCompoundInProject(SQLData data, String compoundID, String projectID) throws DataException {
		SQLSample aSample = new SQLSample(data);
		try {
			PreparedStatement aSth = aSample.myData.prepareStatement(SQL_INSERT_CMPD_WITH_PROJECT);
			aSth.setString(1, compoundID);
			aSth.setString(2, projectID);
			aSample.makeNewWithAutonumber(aSth);
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return aSample;
	}
	*/
	
	public static Sample create(SQLData data, String materialID) throws DataException {
		SQLSample aSample = new SQLSample(data);
		try {
			PreparedStatement aSth = aSample.myData.prepareStatement(SQL_INSERT_SAMPLE);
			aSth.setString(1, materialID);
			aSample.makeNewWithAutonumber(aSth);
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return aSample;
	}
	
	public static Sample createInProject(SQLData data, String materialID, String projectID) throws DataException {
		SQLSample aSample = new SQLSample(data);
		try {
			PreparedStatement aSth = aSample.myData.prepareStatement(SQL_INSERT_WITH_PROJECT);
			aSth.setString(2, materialID);
			aSth.setString(1, projectID);
			aSample.makeNewWithAutonumber(aSth);
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return aSample;
	}
	
	protected SQLSample(SQLData data) {
		super(data);
		this.initVals();
	}
	
	public static Sample loadForMaterial(SQLData data, String materialID) throws DataException {
		try {
			SQLSample aSample = new SQLSample(data);
			PreparedStatement aPsth = aSample.myData.prepareStatement(SQL_LOAD_FOR_MATERIAL);
			aPsth.setString(1, materialID);
			aSample.loadUsingPreparedStatement(aPsth);
			if ( aSample.first() ) 
				return aSample;
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return null;
	}
	
	public static Sample load(SQLData data, String anID) throws DataException {
		SQLSample object = new SQLSample(data);
		object.myID = anID;
		object.fetchRecord();
		return object;
	}

	public SQLSample(SQLData data, String anID) throws DataException {
		super(data);
		this.myID = anID;
		this.initVals();
		this.fetchRecord();
	}
	
	protected void initVals() {
		this.idField = "sample_id";
		this.myData.setAccessRole(User.SAMPLE_ROLE);
		this.myData.setProjectField(PROJECT_COLUMN);
	}
	
	protected void fetchRecord() throws DataException {
		this.fetchRecord(SQL_LOAD);
	}
	
	
	public static Sample loadFromCollection(SQLData data, SampleCollection aCol, int row, int column) throws DataException {
		return SQLSample.loadFromCollection(data, aCol.getID(), row, column);
	}
	
	public static Sample loadFromCollection(SQLData data, String collectionID, String location) throws DataException {
		try {
			SQLSample aSample = new SQLSample(data);
			int[] loc = parseLocation(location);
			PreparedStatement aPsth = aSample.myData.prepareStatement(SQL_LOAD_FROM_COLL_LOC);
			aPsth.setString(1, collectionID);
			aPsth.setInt(2, loc[0]);
			aPsth.setInt(3, loc[1]);
			aSample.loadUsingPreparedStatement(aPsth);
			if ( aSample.first() ) 
				return aSample;
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return null;	}

	public static Sample loadFromCollection(SQLData data, String collectionID, int row, int column) throws DataException {
		try {
			SQLSample aSample = new SQLSample(data);
			PreparedStatement aPsth = aSample.myData.prepareStatement(SQL_LOAD_FROM_COLL_LOC);
			aPsth.setString(1, collectionID);
			aPsth.setInt(2, row);
			aPsth.setInt(3, column);
			aSample.loadUsingPreparedStatement(aPsth);
			if ( aSample.first() ) 
				return aSample;
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return null;
	}

	public String getBaseUnit() throws DataException {
		return this.myData.getString(UNIT_COLUMN);
	}
	
	public String getCollectionID() throws DataException {
		return this.myData.getString(COLLECTION_COLUMN);
	}
	
	public SampleCollection getCollection() throws DataException {
		String colID = this.myData.getString(COLLECTION_COLUMN);
		if ( colID != null ) {
			SampleCollection aCol = SQLSampleCollection.load(this.myData, colID);
			if ( aCol.first() ) return aCol;
		}
		return null;
	}
	
	public String getLocation() throws DataException {
		int myRow = this.myData.getInt(ROW_COLUMN);
		int myCol = this.myData.getInt(COLUMN_COLUMN);
		if ( myRow > 0 & myCol > 0 ) {
			String row = ( this.rowAlpha ? ALPHABET[myRow] : String.valueOf(myRow) );
			String col = ( this.colAlpha ? ALPHABET[myCol] : String.valueOf(myCol) );
			return row + col;
		} else 
			return null;
	}
	
	public int getLocationRow() throws DataException {
		return this.myData.getInt(ROW_COLUMN);
	}
	
	public int getLocationCol() throws DataException {
		return this.myData.getInt(COLUMN_COLUMN);
	}
	
	public String getName() throws DataException {
		return this.myData.getQUIETString(NAME_COLUMN);
	}
	
	public String getCultureID() throws DataException {
		return this.myData.getString(CULTURE_ID_COLUMN);
	}

	public String getNotes() throws DataException {
		return this.myData.getString(NOTES_COLUMN);
	}
	
	public String getVialWeight() throws DataException {
		return this.myData.getString(VIAL_WEIGHT_COLUMN);
	}
	
	public String getDateString() throws DataException {
		return this.myData.getString(DATE_COLUMN);
	}
	
	public Date getDate() throws DataException {
		return this.myData.getDate(DATE_COLUMN);
	}
	
	public String getProjectID() throws DataException {
		String retVal = this.myData.getString(PROJECT_COLUMN);
/*		if ( retVal == null ) {
			String sourceID = this.getLibrarySourceID();
			if ( sourceID == null || sourceID.equals(this.myID) ) {
				Separation aSep = this.getParentSeparation();
				if ( aSep != null ) {
					Sample source = aSep.getSources();
					source.beforeFirst();
					List<String> projects = new ArrayList<String>();
					while ( source.next() ) {
						String thisProj = source.getProjectID();
						if ( thisProj != null && (! projects.contains(thisProj)) )
							projects.add(thisProj);
					}
					if ( projects.size() > 1 ) {
						StringBuffer projString = new StringBuffer();
						projString.append(projects.get(0));
						ListIterator<String> anIter = projects.listIterator(1);
						while (anIter.hasNext() ) {
							String aProj = anIter.next();
							projString.append(", ");
							projString.append(aProj);
						}
						retVal = projString.toString();
					} else if ( projects.size() > 0 ){
						retVal = projects.get(0);
					}
				} else {
					Strain aStrain = new SQLStrain(this.myData, this.getCultureID());
					retVal = aStrain.getProjectID();
				}
			} else {
				Sample aSample = new SQLSample(this.myData, sourceID);
				retVal = aSample.getProjectID();
			}
		}
*/		return retVal;
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
	
	public BigDecimal getConcentration() throws DataException {
		return this.myData.getBigDecimal(CONCENTRATION_COLUMN);
	}
	
	public void setConcentration(BigDecimal newValue) throws DataException {
		this.myData.setBigDecimal(CONCENTRATION_COLUMN, newValue);
	}
	
	public void setConcentration(String newValue) throws DataException {
		this.setConcentration(parseAmount(newValue));
	}
	
	public void setBaseUnit(String newUnit) throws DataException {
		this.myData.setString(UNIT_COLUMN, newUnit);
	}
	
	public void setCollectionID(String newColID) throws DataException {
		this.myData.setString(COLLECTION_COLUMN, newColID);
	}
	
	public void setLocation(String newRow, String newCol) throws DataException {
		this.myData.setString(ROW_COLUMN, newRow);
		this.myData.setString(COLUMN_COLUMN, newCol);
	}
	
	public void setLocation(int newRow, int newCol) throws DataException {
		this.myData.setInt(ROW_COLUMN, newRow);
		this.myData.setInt(COLUMN_COLUMN, newCol);
	}
	
	public void setLocation(String newLocation) throws DataException {
		int[] vals = parseLocation(newLocation);
		if ( vals[0] > 0 && vals[1] > 0 ) {
			this.myData.setInt(ROW_COLUMN, vals[0]);
			this.myData.setInt(COLUMN_COLUMN, vals[1]);
		}
	}
	
	public void setName(String newName) throws DataException {
		this.myData.setString(NAME_COLUMN, newName);
	}
	
	public void setCultureID(String newCultureID) throws DataException {
		this.myData.setString(CULTURE_ID_COLUMN, newCultureID);
	}

	public void setNotes(String newNotes) throws DataException {
		this.myData.setString(NOTES_COLUMN, newNotes);
	}
	
	public void setDate(String newValue) throws DataException {
		this.myData.setString(DATE_COLUMN, newValue);
	}
	
	public void setDate(Date newValue) throws DataException {
		this.myData.setDate(DATE_COLUMN, newValue);
	}
	
	public void addNotes(String newNotes) throws DataException {
		StringBuffer myNotes = new StringBuffer(this.getNotes());
		myNotes.append(" ");
		myNotes.append(newNotes);
		this.myData.setString(NOTES_COLUMN, myNotes.toString());
	}
	
	public void setVialWeight(String newValue) throws DataException {
		this.myData.setString(VIAL_WEIGHT_COLUMN, newValue);
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
	
	public BigDecimal accountBalance() throws DataException {
		return SQLSampleAccount.balance(this.myData, this.myID);
	}

	public boolean isFraction() throws DataException {
		String parentID = this.getParentMaterialID();
		if ( parentID != null ) {
			try {
				PreparedStatement aPsth = this.myData.prepareStatement(SQL_GET_PARENT_SEPARATION);
				aPsth.setString(1, parentID);
				ResultSet check = aPsth.executeQuery();
				boolean retval = check.first();
				check.close();
				aPsth.close();
				return retval;
			} catch (SQLException e) {
				throw new DataException(e);
			}
		}
		return false;
	}

	public Separation getParentSeparation() throws DataException {
		Material parent = this.getParentMaterial();
		if ( parent != null )
			return parent.getParentSeparation();
		return null;
	}

	public Separation getSeparations() throws DataException {
		Material parent = this.getParentMaterial();
		if ( parent != null )
			return parent.getSeparations();
		return null;
	}
	
/*
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
	
	*/
	
	public boolean isExtract() throws DataException {
		boolean retval = false;
		String parentID = this.getParentMaterialID();
		if ( parentID != null ) {
			try {
				PreparedStatement aPsth = this.myData.prepareStatement(SQL_IS_EXTRACT);
				aPsth.setString(1, parentID);
				ResultSet aResult = aPsth.executeQuery();
				retval = aResult.first();
				aPsth.close();
			} catch ( SQLException e ) {
				throw new DataException(e);
			} 
		}
		return retval;
	}

	public Harvest getHarvestForExtract() throws DataException {
		Material parent = this.getParentMaterial();
		if ( parent != null )
			return parent.getHarvestForExtract();
		return null;
	}
	
	public String getExtractSolvent() throws DataException {
		Material parent = this.getParentMaterial();
		if ( parent != null )
			return parent.getExtractSolvent();
		return null;
	}
	
	public String getExtractType() throws DataException {
		Material parent = this.getParentMaterial();
		if ( parent != null )
			return parent.getExtractType();
		return null;
	}

	public Harvest getExtractSource() throws DataException {
		Material parent = this.getParentMaterial();
		if ( parent != null )
			parent.getExtractSource();
		return null;
	}

	/*
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
	
	public void setExtractType(String newType) throws DataException {
		if ( this.isExtract() ) {
			try {
				PreparedStatement aPsth = this.myData.prepareStatement(SQL_SET_EXTRACT_TYPE);
				aPsth.setString(1, newType);
				aPsth.setString(2, this.myID);
				aPsth.executeUpdate();
				aPsth.close();
			} catch (SQLException e) {
				throw new DataException(e);
			}
		}	
	}
	 */

	/*
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
	
	public void setExtractSource(Harvest aHarvest) throws DataException {
		this.setExtractSource(aHarvest.getID());
	}
	*/
	
	public BigDecimal getAmountForHarvest(String harvestID) throws DataException {
		if ( this.myID != null ) {
			try {
				PreparedStatement aPsth = this.myData.prepareStatement(SQL_GET_HARVEST_AMOUNT);
				aPsth.setString(1, this.myID);
				aPsth.setString(2, harvestID);
				ResultSet aResult = aPsth.executeQuery();
				BigDecimal retval = null;
				if ( aResult.first()) retval = aResult.getBigDecimal(1);
				aResult.close();
				aPsth.close();
				return retval;
			} catch (SQLException e) {
				throw new DataException(e);
			}
		}
		return null;
	}
	
	public BigDecimal getAmountForAssay(String assayID) throws DataException {
		if ( this.myID != null ) {
			try {
				PreparedStatement aPsth = this.myData.prepareStatement(SQL_GET_ASSAY_AMOUNT);
				aPsth.setString(1, this.myID);
				aPsth.setString(2, assayID);
				ResultSet aResult = aPsth.executeQuery();
				BigDecimal retval = null;
				if ( aResult.first()) retval = aResult.getBigDecimal(1);
				aResult.close();
				aPsth.close();
				return retval;
			} catch (SQLException e) {
				throw new DataException(e);
			}
		}
		return null;
	}
	
	public BigDecimal getAmountForAssay(Assay assay) throws DataException {
		return this.getAmountForAssay(assay.getID());
	}
	
	public BigDecimal getAmountForHarvest(Harvest aHarvest) throws DataException {
		return this.getAmountForHarvest(aHarvest.getID());
	}

	public BigDecimal getAmountForSeparation(String anID) throws DataException {
		if ( this.myID != null ) {
			try {
				PreparedStatement aPsth = this.myData.prepareStatement(SQL_GET_SEP_AMOUNT);
				aPsth.setString(1, this.myID);
				aPsth.setString(2, anID);
				ResultSet aResult = aPsth.executeQuery();
				BigDecimal retval = null;
				if ( aResult.first()) retval = aResult.getBigDecimal(1);
				aResult.close();
				aPsth.close();
				return retval;
			} catch (SQLException e) {
				throw new DataException(e);
			}
		}
		return null;
	}
	
	public BigDecimal getAmountForSeparation(Separation aSep) throws DataException {
		return this.getAmountForSeparation(aSep.getID());
	}
	
	public boolean isNeat() throws DataException {
		boolean retVal = false;
		if ( this.myID != null )
			retVal = ( this.myData.getFloat(CONCENTRATION_COLUMN) == 0.0f );
		return retVal;
	}
	
	public boolean isSolution() throws DataException {
		boolean retVal = false;
		if ( this.myID != null ) 
			retVal = ( this.myData.getFloat(CONCENTRATION_COLUMN) > 0 );
		return retVal;
	}

	public boolean isLibrarySample() throws DataException {
		if ( this.myData != null ) {
			String myParent = this.myData.getString(LIBRARY_SOURCE_COLUMN);
			if ( myParent != null )
				return true;
		}	
		return false;
	}
	
	public String getLibrarySourceID() throws DataException {
		return this.myData.getString(LIBRARY_SOURCE_COLUMN);
	}
	
	public Sample getLibrarySource() throws DataException {
		String sourceID = this.myData.getString(LIBRARY_SOURCE_COLUMN);
		if ( sourceID != null ) {
			Sample source = new SQLSample(this.myData, sourceID);
			if ( source.first() ) return source;
		}
		return null;
	}
	
	public Sample getLibraryChildren() throws DataException {
		SQLSample kids = new SQLSample(this.myData);
		try {
			PreparedStatement aPsth = this.myData.prepareStatement(SQL_LOAD_LIBRARY_KIDS);
			aPsth.setString(1, this.myID);
			aPsth.setString(2, this.myID);
			kids.loadUsingPreparedStatement(aPsth);
			if ( kids.first())
				return kids;
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return null;
	}
	
	public void setLibrarySourceID(String newValue) throws DataException {
		this.myData.setString(LIBRARY_SOURCE_COLUMN, newValue);
	}
	
	public void setLibrarySource(Sample aSource) throws DataException {
		String mySrcID = aSource.getLibrarySourceID();
		if ( mySrcID != null ) 
			this.setLibrarySourceID(mySrcID);
		else 
			this.setLibrarySourceID(aSource.getID());
	}

	public boolean isSelfLibrarySource() throws DataException {
		if ( this.myID != null ) {
			String sourceID = this.getLibrarySourceID();
			return this.myID.equals(sourceID);
		}
		return false;
	}
	
	public void setSelfLibrarySource() throws DataException {
		this.myData.setString(LIBRARY_SOURCE_COLUMN, this.myID);
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
		String[] retVals = { Sample.LC_DATA_TYPE, Sample.NMR_DATA_TYPE, Sample.MS_DATA_TYPE };
		return retVals;
	}

	public Assay getAssays() throws DataException {
		return SQLAssay.assaysForSampleID(this.myData, this.myID);
	}

	public SampleAccount getAccount() throws DataException {
		return new SQLSampleAccount(this);
	}

	public String getDataFileClass() {
		return DATA_FILE_CLASS;
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
			SQLSampleAccount.voidReferences(this.myData, "sample", this.myID);
		}
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
		return SQLCompound.compoundsForSample(myData, this.myID);
	}

	
	public boolean hasCompounds() throws DataException {
		return getCompounds().first();
	}

	
	public void addCompoundID(String newValue, String retentionTime) throws DataException {
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
			aSth.setString(3, retentionTime);
			aSth.execute();
			aSth.close();
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

	
	public void addCompoundID(String newValue) throws DataException {
		this.addCompoundID(newValue, "0");
	}

	
	public void addCompound(Compound aCompound) throws DataException {
		this.addCompoundID(aCompound.getID());
	}

	
	public void addCompound(Compound aCompound, String retentionTime)
			throws DataException {
		this.addCompoundID(aCompound.getID(), retentionTime);
	}

	
	public void addCompoundID(String newValue, double retentionTime)
			throws DataException {
		try {
			PreparedStatement aSth = this.myData.prepareStatement(SQL_LINK_CMPD);
			aSth.setString(1, newValue);
			aSth.setString(2, this.myID);
			aSth.setDouble(3, retentionTime);
			aSth.execute();
			aSth.close();
		} catch (SQLException e) {
			throw new DataException(e);
		}
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

	
	public void addCompound(Compound aCompound, double retentionTime)
			throws DataException {
		this.addCompoundID(aCompound.getID(), retentionTime);
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

	
	public String getSeparationID(String compoundID) throws DataException {
		if ( this.myID != null ) {
			try {
				PreparedStatement aPsth = this.myData.prepareStatement(SQL_GET_SEP_COMPOUND);
				aPsth.setString(1, this.myID);
				aPsth.setString(2, compoundID);
				ResultSet aResult = aPsth.executeQuery();
				String retVal = null;
				if ( aResult.first()) {
					retVal = aResult.getString(1);
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

	
	public String getSeparationID(Compound aCompound) throws DataException {
		return this.getSeparationID(aCompound.getID());
	}

	
	public Separation getSeparation(String compoundID) throws DataException {
		if ( this.myID != null ) {
			return SQLSeparation.separationsForCompoundID(myData, compoundID, this.myID);
		} 
		return null;
	}

	
	public Separation getSeparation(Compound aCompound) throws DataException {
		return this.getSeparation(aCompound.getID());
	}

	
	public Material getParentMaterial() throws DataException {
		String parent = this.getParentMaterialID();
		if ( parent != null ) {
			return SQLMaterial.load(myData, parent);
		}
		return null;
	}

	
	public String getParentMaterialID() throws DataException {
		return this.myData.getString(MATERIAL_ID_COLUMN);
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
