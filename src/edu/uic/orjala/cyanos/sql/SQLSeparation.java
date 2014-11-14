package edu.uic.orjala.cyanos.sql;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.uic.orjala.cyanos.AccessException;
import edu.uic.orjala.cyanos.Compound;
import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.ExternalFile;
import edu.uic.orjala.cyanos.Material;
import edu.uic.orjala.cyanos.Notebook;
import edu.uic.orjala.cyanos.Project;
import edu.uic.orjala.cyanos.Role;
import edu.uic.orjala.cyanos.Sample;
import edu.uic.orjala.cyanos.Separation;
import edu.uic.orjala.cyanos.Strain;
import edu.uic.orjala.cyanos.User;

public class SQLSeparation extends SQLObject implements Separation {


	/**
	 * @author George Chlipala
	 *
	 */

	private SQLData fracData = null;
	
	public static final String ID_COLUMN = "separation_id";
	public static final String METHOD_COLUMN = "method";
	public static final String STATIONARY_PHASE_COLUMN = "s_phase";
	public static final String MOBILE_PHASE_COLUMN = "m_phase";
	public static final String NOTES_COLUMN = "notes";
//	public static final String NAME_COLUMN = "name";
	public static final String PROJECT_COLUMN = "project_id";
	public static final String TAG_COLUMN = "tag";
	public static final String REMOVED_DATE_COLUMN = "removed_date";
	public static final String REMOVED_USER_COLUMN = "removed_by";
	
	public static final String NOTEBOOK_COLUMN = "notebook_id";
	public static final String NOTEBOOK_PAGE_COLUMN = "notebook_page";

	public static final String DATE_COLUMN = "date";
	
	public static final String REMOTE_HOST_COLUMN = "remote_host";
	public static final String REMOTE_ID_COLUMN = "remote_id";
	
	private static final String[] ALL_COLUMNS = { ID_COLUMN, TAG_COLUMN, 
		METHOD_COLUMN, STATIONARY_PHASE_COLUMN, MOBILE_PHASE_COLUMN, 
		// NAME_COLUMN, 
		NOTES_COLUMN, PROJECT_COLUMN, DATE_COLUMN, 
		REMOVED_DATE_COLUMN, REMOVED_USER_COLUMN, 
	//	NOTEBOOK_COLUMN, NOTEBOOK_PAGE_COLUMN, 
		REMOTE_HOST_COLUMN, REMOTE_ID_COLUMN };	
	
	static final String SQL_BASE = sqlBase("separation", ALL_COLUMNS);
	
	/*
	 * Parameter and SQL for "cleaned-up" database schema.
	 *
	private static final String DATE_COLUMN = "added";
	private static final String SQL_INSERT_SEP = "INSERT INTO separation(added) VALUES(CURRENT_DATE)";
	private static final String SQL_INSERT_WITH_PROJECT = "INSERT INTO separation(added,project_id) VALUES(CURRENT_DATE,?)";
	 */

	private static final String SQL_INSERT_WITH_PROJECT = "INSERT INTO separation(date,project_id,remote_id) VALUES(CURRENT_DATE,?,UUID())";

	private static final String SQL_LOAD = SQL_BASE + " WHERE separation_id=?";
	private static final String SQL_ADD_SOURCE = "INSERT INTO separation_source(separation_id, material_id, amount_value, amount_scale) VALUES(?,?,?,?)";
	private static final String SQL_REMOVE_SOURCE = "DELETE FROM separation_source WHERE separation_id=? AND material_id=?";
	private static final String SQL_LOAD_FRACTIONS = "SELECT separation_id,material_id,fraction_number FROM separation_product WHERE separation_id=? ORDER BY fraction_number";
	private static final String SQL_ADD_FRACTION = "INSERT INTO separation_product(separation_id,material_id,fraction_number) VALUES(?,?,?)";
	private static final String SQL_GET_FRACTION_MATERIALS = SQLMaterial.SQL_BASE + " JOIN separation_product sep ON(material.material_id = sep.material_id) WHERE separation_id=? ORDER BY fraction_number";
	private static final String SQL_GET_SOURCE_MATERIALS = SQLMaterial.SQL_BASE + " JOIN separation_source sep ON(material.material_id = sep.material_id) WHERE separation_id=? ORDER BY material_id";

	private static final String SQL_LOAD_FOR_SAMPLE = SQL_BASE + " JOIN (separation_source ss, sample sa, material m) ON (ss.material_id = m.material_id AND sa.material_id = m.material_id AND ss.separation_id = separation.separation_id) WHERE sa.sample_id=? ORDER BY separation_id";
	private static final String SQL_LOAD_ALL = SQL_BASE + " ORDER BY separation_id";
	
	private static final String SQL_LOAD_FOR_COMPOUND = "SELECT DISTINCT separation.* FROM separation JOIN (compound_peaks c) ON (c.separation_id = separation.separation_id) WHERE compound_id=? ORDER BY separation_id";
	private static final String SQL_LOAD_FOR_COMPOUND_SAMPLE = "SELECT DISTINCT separation.* FROM separation JOIN (compound_peaks c) ON (c.separation_id = separation.separation_id) WHERE c.compound_id=? AND c.sample_id=? ORDER BY separation_id";
	
	private static final String SQL_GET_TAGS = "SELECT DISTINCT tag FROM separation ORDER BY tag";
	
	private static final String SQL_TRASH_SEPARATION = "INSERT INTO %s.separation(separation_id,date_added,method,s_phase,m_phase,notes,name,project_id,tag) (SELECT separation_id,date,method,s_phase,m_phase,notes,name,project_id,tag FROM separation WHERE separation_id=?)";
	private static final String SQL_DELETE_SEPARATION = "DELETE sep, src, fr FROM separation sep JOIN separation_product fr ON (sep.separation_id = fr.separation_id) JOIN separation_source src ON ( sep.separation_id = src.separation_id) WHERE sep.separation_id=?";
	private static final String SQL_CHECK_PROJECT = "SELECT project_id FROM separation WHERE separation_id=?";

	private static final String SQL_REMOVE_KIDS = "UPDATE sample, separation_product SET sample.removed_date=CURRENT_DATE, sample.removed_by=? WHERE sample.sample_id=separation_product.sample_id AND separation_product.separation_id=?";
	private static final String SQL_VOID_KIDS = "UPDATE sample_acct, sample, separation_product SET sample_acct.void_date=CURRENT_DATE, sample_acct.void_user=? WHERE sample.sample_id=separation_product.sample_id AND separation_product.separation_id=? AND sample_acct.ref_table = 'sample' AND sample_acct.ref_id=sample.sample_id";

	private static final String FRACTION_NUMBER_COL = "fraction_number";
	private static final String FRACTION_MATERIAL_COL = "material_id";

	private static final String DATA_FILE_TABLE = "separation";

	public static final String SQL_GET_RT_COMPOUND = "SELECT retention_time FROM compound_peaks WHERE separation_id=? AND compound_id=?";
	
	private static final String SQL_LINK_CMPD = "REPLACE compound_peaks(compound_id,material_id,separation_id,retention_time) VALUES(?,?,?,?)";
	private static final String SQL_UNLINK_CMPD = "DELETE FROM compound_peaks WHERE compound_id = ? AND separation_id = ?";
	
	private static final String SQL_LOAD_CULTURE_ID = SQL_BASE + " JOIN separation_source src ON(separation.separation_id = src.separation_id) JOIN material mat ON (mat.material_id = src.material_id) WHERE mat.culture_id=?";

		
	public static void delete(SQLData data, String separationID) throws DataException {
		try {
			PreparedStatement aSth = data.prepareStatement(SQL_CHECK_PROJECT);
			aSth.setString(1, separationID);
			ResultSet aResult = aSth.executeQuery();
			if ( aResult.first() ) {
				String projectID = aResult.getString(1);
				aSth.close();
				if ( data.getUser().isAllowed(User.SAMPLE_ROLE, projectID, Role.DELETE) ) {
					boolean deleteSep = false;
					if ( data.hasTrash() ) {
						aSth = data.prepareStatement(String.format(SQL_TRASH_SEPARATION, data.getTrashCatalog()));
						aSth.setString(1, separationID);
						deleteSep = (aSth.executeUpdate() > 0);
					} else {
						deleteSep = true;
					}
					aSth.close();
					if ( deleteSep ) {
						aSth = data.prepareStatement(SQL_DELETE_SEPARATION);
						aSth.setString(1, separationID);
						if ( aSth.executeUpdate() > 0 ) {
							aSth.close();
							SQLSampleAccount.voidReferences(data, "separation", separationID);
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
	
	public static Separation separationsForStrain(SQLData data, Strain aStrain) throws DataException {
		return SQLSeparation.findForStrain(data, aStrain.getID());
	}
	
	public static Separation separationsForCompound(SQLData data, Compound aCompound) throws DataException {
		return separationsForCompoundID(data, aCompound.getID());
	}
	
	public static SQLSeparation findForStrain(SQLData data, String cultureID) throws DataException {
		SQLSeparation object = new SQLSeparation(data);
		try {
			PreparedStatement aSth = object.myData.prepareStatement(SQL_LOAD_CULTURE_ID);
			aSth.setString(1, cultureID);
			object.loadUsingPreparedStatement(aSth);
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return object;
	}
	
	
	public static Separation separationsForCompoundID(SQLData data, String compoundID) throws DataException {
		SQLSeparation aSep = new SQLSeparation(data);
		try {
			PreparedStatement aSth = aSep.myData.prepareStatement(SQL_LOAD_FOR_COMPOUND);
			aSth.setString(1, compoundID);
			aSep.myData.loadUsingPreparedStatement(aSth);
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return aSep;
	}
	
	public static Separation separationsForCompound(SQLData data, Compound aCompound, Sample aSample) throws DataException {
		return separationsForCompoundID(data, aCompound.getID(), aSample.getID());
	}
	
	public static Separation separationsForCompoundID(SQLData data, String compoundID, String sampleID) throws DataException {
		SQLSeparation aSep = new SQLSeparation(data);
		try {
			PreparedStatement aSth = aSep.myData.prepareStatement(SQL_LOAD_FOR_COMPOUND_SAMPLE);
			aSth.setString(1, compoundID);
			aSth.setString(2, sampleID);
			aSep.myData.loadUsingPreparedStatement(aSth);
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return aSep;
	}
	
	public static Separation separationsForSample(SQLData data, Sample aSample) throws DataException {
		SQLSeparation aSep = new SQLSeparation(data);
		try {
			PreparedStatement aSth = aSep.myData.prepareStatement(SQL_LOAD_FOR_SAMPLE);
			aSth.setString(1, aSample.getID());
			aSep.myData.loadUsingPreparedStatement(aSth);
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return aSep;
	}

	public static Separation separations(SQLData data) throws DataException {
		SQLSeparation aSep = new SQLSeparation(data);
		aSep.myData.loadUsingSQL(SQL_LOAD_ALL);
		return aSep;
	}

	public static List<String> tags(SQLData data) throws DataException {
		SQLData aData = data.duplicate();
		List<String> tags = new ArrayList<String>();
		aData.loadUsingSQL(SQL_GET_TAGS);
		aData.beforeFirst();
		while ( aData.next() ) tags.add(aData.getString(TAG_COLUMN));
		aData.close();
		return tags;
	}
	
	public static SQLSeparation load(SQLData data, String id) throws DataException {
		return new SQLSeparation(data, id);
	}

	public SQLSeparation(SQLData data) {
		super(data);
		this.fracData = data.duplicate();
		this.initVals();
	}
	
	public SQLSeparation(SQLData data, String anID) throws DataException {
		super(data);
		this.fracData = data.duplicate();
		this.myID = anID;
		this.initVals();
		this.fetchRecord();
	}
	
	protected void fetchRecord() throws DataException {
		this.fetchRecord(SQL_LOAD);
		if ( this.myData != null ) this.loadFractionData();
	}
	
	protected void initVals() {
		this.idField = ID_COLUMN;
		this.myData.setProjectField(PROJECT_COLUMN);
		this.myData.setAccessRole(User.SAMPLE_ROLE);

	}
	
	public static Separation createInProject(SQLData data, String projectID) throws DataException {
		SQLSeparation aSep = new SQLSeparation(data);
		try {
			PreparedStatement aPsth = aSep.myData.prepareStatement(SQL_INSERT_WITH_PROJECT);
			aPsth.setString(1, projectID);
			aSep.makeNewWithAutonumber(aPsth);
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return aSep;
	}
	
	public void setTemplate(SeparationTemplate aTemplate) throws DataException {
		if ( this.myID != null )  {
			boolean oldRefresh = this.autorefresh;
			this.setManualRefresh();
			this.myData.setString(STATIONARY_PHASE_COLUMN, aTemplate.getStationaryPhase());
			this.myData.setString(MOBILE_PHASE_COLUMN, aTemplate.getMobilePhase());
			this.myData.setString(METHOD_COLUMN, aTemplate.getMethod());
			this.myData.refresh();
			this.autorefresh = oldRefresh;
		}
	}
	
	public static Separation create(SQLData data) throws DataException {
		return SQLSeparation.createInProject(data, null);
	}
	
	private void loadFractionData() throws DataException {
		try {
			PreparedStatement fracSth = this.myData.prepareStatement(SQL_LOAD_FRACTIONS);
			fracSth.setString(1, this.myID);
			this.fracData.loadUsingPreparedStatement(fracSth);
			this.fracData.first();
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

	public Material getSources() throws DataException {
		if ( this.myID != null ) {
			SQLMaterial newSample = new SQLMaterial(this.myData);
			try {
				PreparedStatement aSth = this.myData.prepareStatement(SQL_GET_SOURCE_MATERIALS);
				aSth.setString(1, this.myID);
				newSample.loadUsingPreparedStatement(aSth);
				if ( newSample.first())
					return newSample;
			} catch (SQLException e) {
				throw new DataException(e);
			}
		}
		return null;
	}
	
	public Material getFractions() throws DataException {
		if ( this.myID != null ) {
			SQLMaterial newSample = new SQLMaterial(this.myData);
			try {
				PreparedStatement aSth = this.myData.prepareStatement(SQL_GET_FRACTION_MATERIALS);
				aSth.setString(1, this.myID);
				newSample.loadUsingPreparedStatement(aSth);
				if ( newSample.first())
					return newSample;
			} catch (SQLException e) {
				throw new DataException(e);
			}
		}
		return null;
	}

	public boolean addSource(Material source, BigDecimal amount) throws DataException {
		if ( this.myID != null && this.isAllowedException(Role.WRITE) ) {
			try {
				PreparedStatement addParent = this.myData.prepareStatement(SQL_ADD_SOURCE);
				addParent.setString(1,this.myID);
				addParent.setString(2,source.getID());
				addParent.setLong(3, amount.unscaledValue().longValue());
				addParent.setInt(4, -1 * amount.scale());
				if ( addParent.executeUpdate() > 0 ) {
					String aProject = source.getProjectID();
					if ( this.getProjectID() != null && aProject != null ) {
						this.setProjectID(aProject);
					}
					return true;
				} else {
					return false;
				}
			} catch (SQLException e) {
				throw new DataException(e);
			}
		}
		return false;
	}
	
	public void setProjectID(String projectID) throws DataException{
		this.myData.setStringNullBlank(PROJECT_COLUMN, projectID);
	}
	
	public void setProject(Project aProject) throws DataException {
		if ( aProject != null )
			this.setProjectID(aProject.getID());
		else 
			this.myData.setNull(PROJECT_COLUMN);
	}
	
	public String getProjectID() throws DataException {
		return this.myData.getString(PROJECT_COLUMN);
	}
	
	public Project getProject() throws DataException {
		return SQLProject.load(this.myData, this.getProjectID());
	}

	public boolean removeSource(Material source) throws DataException {
		if ( this.myID != null && this.isAllowedException(Role.WRITE) ) {
			try {
				PreparedStatement addParent = this.myData.prepareStatement(SQL_REMOVE_SOURCE);
				addParent.setString(1,this.myID);
				addParent.setString(2,source.getID());
				if ( addParent.executeUpdate() > 0 ) {
					return true;
				} else {
					return false;
				}
			} catch (SQLException e) {
				throw new DataException(e);
			}
		}
		return false;
	}
	
	public Material makeFraction() throws DataException {
		Material mySource = this.getSources();
		if ( mySource.first() ) {
			Material newFrac = SQLMaterial.create(this.myData, mySource.getCultureID());
			this.addFraction(newFrac);
			return newFrac;
		}
		return null;
	}
	
	public Material makeFraction(int frNumber) throws DataException {
		Material mySource = this.getSources();
		if ( mySource.first() ) {
			Material newFrac = SQLMaterial.create(this.myData, mySource.getCultureID());
			if ( newFrac.first() ) {
				this.addFraction(frNumber, newFrac);
				return newFrac;
			} else {
				return null;
			}
		} 
		return null;
	}
	
	protected void addFraction(Material aSample) throws DataException {
		boolean onRow = false;
		int currRow = -1;
		if ( this.fracData.getRow() > 0 ) { 
			onRow = true;
			currRow = this.fracData.getInt(FRACTION_NUMBER_COL);
		}
		int frNumber = 1;
		if ( this.fracData.last() ) 
			frNumber = this.fracData.getInt(FRACTION_NUMBER_COL) + 1;
		this.addFraction(frNumber, aSample);
		if (onRow) this.gotoFraction(currRow);
	}

	protected void addFraction(int frNumber, Material fraction) throws DataException {
		if ( this.myID != null && this.isAllowedException(Role.WRITE) ) {
			boolean onRow = false;
			int currRow = -1;
			if ( this.fracData.getRow() > 0 ) { 
				onRow = true;
				currRow = this.fracData.getInt(FRACTION_NUMBER_COL);
			}
			try {
				PreparedStatement aPsth = this.myData.prepareStatement(SQL_ADD_FRACTION);
				aPsth.setString(1, this.myID);
				aPsth.setString(2, fraction.getID());
				aPsth.setInt(3, frNumber);
				aPsth.executeUpdate();
				aPsth.close();
			} catch (SQLException e) {
				throw new DataException(e);
			}
			this.loadFractionData();
			if (onRow) this.gotoFraction(currRow);
		}
	}
	
	public Material getCurrentFraction() throws DataException {
		Material aFrac = SQLMaterial.load(this.myData, this.fracData.getString(FRACTION_MATERIAL_COL));
		return aFrac;
	}	
	
	public int getCurrentFractionNumber() throws DataException {
		if ( this.fracData != null ) 
			return this.fracData.getInt(FRACTION_NUMBER_COL);
		return 0;
	}

	public boolean gotoFraction(int frNumber) throws DataException {
		boolean retval = false;
		boolean onRow = false;
		int currRow = -1;
		if ( this.fracData == null ) 
			this.loadFractionData();
		if ( this.fracData.getRow() > 0 ) { 
			onRow = true;
			currRow = this.fracData.getInt(FRACTION_NUMBER_COL);
		}
		this.fracData.beforeFirst();
		FIND_LINE: while ( this.fracData.next() ) {
			if ( this.fracData.getInt(FRACTION_NUMBER_COL) == frNumber ) {
				retval = true;
				break FIND_LINE;
			}
		}
		if ( onRow && ! retval ) {
			this.gotoFraction(currRow);
		}
		return retval;
	}
	
	public boolean firstFraction() throws DataException {
		if ( this.fracData != null ) 
			return this.fracData.first();
		return false;
	}
	
	public boolean lastFraction() throws DataException {
		if ( this.fracData != null )
			return this.fracData.last();
		return false;
	}
	
	public boolean nextFraction() throws DataException {
		if ( this.fracData != null )
			return this.fracData.next();
		return false;
	}
	
	public boolean previousFraction() throws DataException {
		if ( this.fracData != null ) 
			return this.fracData.previous();
		return false;
	}
	
	public void beforeFirstFraction() throws DataException {
		if ( this.fracData != null ) 
			this.fracData.beforeFirst();
	}
	
	public void afterLastFraction() throws DataException {
		if ( this.fracData != null ) 
			this.fracData.afterLast();
	}
	
	public String getMobilePhase() throws DataException {
		return this.myData.getString(MOBILE_PHASE_COLUMN);
	}

	public String getMethod() throws DataException {
		return this.myData.getString(METHOD_COLUMN);
	}
	
	public String getStationaryPhase() throws DataException {
		return this.myData.getString(STATIONARY_PHASE_COLUMN);
	}
	
	public void setMethod(String newValue) throws DataException {
		this.myData.setString(METHOD_COLUMN, newValue);
	}
	
	public void setMobilePhase(String newValue) throws DataException {
		this.myData.setString(MOBILE_PHASE_COLUMN, newValue);
	}
	
	public void setStationaryPhase(String newValue) throws DataException {
		this.myData.setString(STATIONARY_PHASE_COLUMN, newValue);
	}
	
	public Date getDate() throws DataException {
		return this.myData.getDate(DATE_COLUMN);
	}
	
	public String getDateString() throws DataException {
		return this.myData.getString(DATE_COLUMN);
	}
	
	public void setDate(Date newValue) throws DataException {
		this.myData.setDate(DATE_COLUMN, newValue);
	}
	
	public void setDate(String newValue) throws DataException {
		this.myData.setString(DATE_COLUMN, newValue);
	}
	
	public String getNotes() throws DataException {
		return this.myData.getString(NOTES_COLUMN);
	}
	
	public void setNotes(String newValue) throws DataException {
		this.myData.setString(NOTES_COLUMN, newValue);
	}
	
	public void appendNotes(String newNotes) throws DataException {
		StringBuffer newValue = new StringBuffer(this.myData.getString(NOTES_COLUMN));
		newValue.append(newNotes);
		this.myData.setString(NOTES_COLUMN, newValue.toString());
	}
	
	public SeparationTemplate createTemplate() throws DataException {
		SeparationTemplate aTemplate = new SQLSeparationTemplate(this.myData);
		aTemplate.setMethod(this.myData.getString(METHOD_COLUMN));
		aTemplate.setMobilePhase(this.myData.getString(MOBILE_PHASE_COLUMN));
		aTemplate.setStationaryPhase(this.myData.getString(STATIONARY_PHASE_COLUMN));
		return aTemplate;
	}
	
	public String getProtocolMobilePhase() throws DataException {
		if ( this.protocol != null ) 
			return this.protocol.get(MOBILE_PHASE_COLUMN);
		return null;
	}

	public String getProtocolMethod() throws DataException {
		if ( this.protocol != null )
			return this.protocol.get(METHOD_COLUMN);
		return null;
	}
	
	public String getProtocolStationaryPhase() throws DataException {
		if ( this.protocol != null )
			return this.protocol.get(STATIONARY_PHASE_COLUMN);
		return null;
	}
	
	public void setProtocolMethod(String newValue) throws DataException {
		if ( this.protocol != null )
			this.protocol.put(METHOD_COLUMN, newValue);
	}
	
	public void setProtocolMobilePhase(String newValue) throws DataException {
		if ( this.protocol != null )
			this.protocol.put(MOBILE_PHASE_COLUMN, newValue);
	}
	
	public void setProtocolStationaryPhase(String newValue) throws DataException {
		if ( this.protocol != null )
			this.protocol.put(STATIONARY_PHASE_COLUMN, newValue);
	}
	
	public ExternalFile getChromatograms() throws DataException {
		return this.getDataFilesForType(LC_DATA_TYPE);
	}
	
	public void addChromatogram(ExternalFile aFile) throws DataException {
		this.linkDataFile(aFile, LC_DATA_TYPE);
	}

	public String[] dataTypes() {
		String[] retVals = { Separation.LC_DATA_TYPE };
		return retVals;
	}

	public String getDataFileClass() {
		return DATA_FILE_CLASS;
	}

	public String getTag() throws DataException {
		return this.myData.getString(TAG_COLUMN);
	}

	public void setTag(String newValue) throws DataException {
		this.myData.setString(TAG_COLUMN, newValue);
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
			SQLSampleAccount.voidReferences(this.myData, "separation", this.myID);
			
			try {
				PreparedStatement aSth = this.myData.prepareStatement(SQL_REMOVE_KIDS);
				aSth.setString(1, this.myData.getUser().getUserID());
				aSth.setString(2, this.myID);
				if ( aSth.executeUpdate() > 0 ) {
					aSth.close();
					aSth = this.myData.prepareStatement(SQL_VOID_KIDS);
					aSth.setString(1, this.myData.getUser().getUserID());
					aSth.setString(2, this.myID);
					aSth.execute();
				}
				aSth.close();
			} catch (SQLException e) {
				throw new DataException(e);
			}
		
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
		return SQLCompound.compoundsForSeparation(myData, this.myID);
	}

	
	public boolean hasCompounds() throws DataException {
		return getCompounds().first();
	}

	
	public void addCompoundID(String newValue, String sampleID, String retentionTime) throws DataException {
		if ( retentionTime == null || retentionTime.length() < 1 ) {
			this.addCompoundID(newValue, sampleID, 0.0f);
			return;
		}
		Double time = parseTime(retentionTime);
		if ( time != null ) {
			this.addCompoundID(newValue, sampleID, time.doubleValue());
			return;
		}
		try {
			PreparedStatement aSth = this.myData.prepareStatement(SQL_LINK_CMPD);
			aSth.setString(1, newValue);
			aSth.setString(2, sampleID);
			aSth.setString(3, this.myID);
			aSth.setString(4, retentionTime);
			aSth.execute();
			aSth.close();
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

	
	public void addCompound(Compound aCompound,  String sampleID, String retentionTime)
			throws DataException {
		this.addCompoundID(aCompound.getID(), sampleID, retentionTime);
	}

	
	public void addCompoundID(String newValue,  String sampleID, double retentionTime)
			throws DataException {
		try {
			PreparedStatement aSth = this.myData.prepareStatement(SQL_LINK_CMPD);
			aSth.setString(1, newValue);
			aSth.setString(2, sampleID);
			aSth.setString(3, this.myID);
			aSth.setDouble(4, retentionTime);
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

	
	public void addCompound(Compound aCompound,  String sampleID, double retentionTime)
			throws DataException {
		this.addCompoundID(aCompound.getID(), sampleID, retentionTime);
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


}
