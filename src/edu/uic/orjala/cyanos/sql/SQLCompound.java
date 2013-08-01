/**
 * 
 */
package edu.uic.orjala.cyanos.sql;

import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import edu.uic.orjala.cyanos.AccessException;
import edu.uic.orjala.cyanos.Compound;
import edu.uic.orjala.cyanos.CompoundObject;
import edu.uic.orjala.cyanos.Compound_UV;
import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.DataFileObject;
import edu.uic.orjala.cyanos.ExternalFile;
import edu.uic.orjala.cyanos.Material;
import edu.uic.orjala.cyanos.Project;
import edu.uic.orjala.cyanos.Role;
import edu.uic.orjala.cyanos.Sample;
import edu.uic.orjala.cyanos.Separation;
import edu.uic.orjala.cyanos.Strain;
import edu.uic.orjala.cyanos.User;


/**
 * @author George Chlipala
 *
 */
public class SQLCompound extends SQLObject implements Compound_UV, DataFileObject {
	
	private static final int DEREPLICATION_PERM = Role.WRITE;
	// Setup the column names here so that changing is easier.	
	public final static String ID_COLUMN = "compound_id";
	public final static String NAME_COLUMN = "name";
	public final static String FORMULA_COLUMN = "formula";
	public final static String AVERAGE_MASS_COLUMN = "average_wt";
	public final static String MONOISOTOPIC_MASS_COLUMN = "isotopic_wt";
	public final static String NOTES_COLUMN = "notes";
	public final static String PROJECT_COLUMN = "project_id";
	
	public final static String MDL_COLUMN = "mdl_data";
	public final static String SMILES_COLUMN = "smiles";
	public final static String INCHI_STRING_COLUMN = "inchi_string";
	public final static String INCHI_KEY_COLUMN = "inchi_key";
	
	public final static String REMOTE_HOST_COLUMN = "remote_host";

	@Deprecated
	public final static String THUMBNAIL_COLUMN = "thumbnail";
	@Deprecated
	public final static String UV_DATA_COLUMN = "uv_spectrum";

	private final static String DATA_FILE_TABLE = "compound";
	
	private final static String[] ALL_COLUMNS = { ID_COLUMN, NAME_COLUMN, 
		FORMULA_COLUMN, AVERAGE_MASS_COLUMN, MONOISOTOPIC_MASS_COLUMN,
		SMILES_COLUMN, INCHI_STRING_COLUMN, INCHI_KEY_COLUMN, MDL_COLUMN,
		NOTES_COLUMN, PROJECT_COLUMN, REMOTE_HOST_COLUMN
	//	REMOVED_DATE_COLUMN, REMOVED_USER_COLUMN, 
	//	NOTEBOOK_COLUMN, NOTEBOOK_PAGE_COLUMN
		};	
	
	private final static String SQL_BASE = "SELECT " + allColumns("compound.", ALL_COLUMNS) + " FROM compound ";

	
	private final static String SQL_LOAD = SQL_BASE + " WHERE compound_id=?";
	private final static String SQL_LOAD_LIKE = SQL_BASE + " WHERE compound_id LIKE ? OR name LIKE ? OR formula LIKE ?";
	private final static String INSERT_NEW_COMPOUND_SQL = "INSERT INTO compound(compound_id) VALUES(?)";
	
	@Deprecated
	private final static String UV_PEAK_SQL = "SELECT wavelength, rel_intensity FROM uv_data WHERE compound_id=?";
	@Deprecated
	private final static String UV_ADD_PEAK_SQL = "INSERT INTO uv_data(compound_id,wavelength,rel_intensity) VALUES(?,?,?)";
	@Deprecated
	private final static String UV_DEL_PEAK_SQL = "DELETE FROM uv_data WHERE compound_id=? AND wavelength=?";
		
	@Deprecated
	private static final String SQL_UV_INTENSITY = "SELECT compound.* FROM compound JOIN uv_data u ON(u.compound_id = compound.compound_id) WHERE ABS(wavelength - ?) <= ? AND ABS(rel_intensity - ?) <= ?";
	@Deprecated
	private static final String SQL_UV_BASIC = "SELECT compound.* FROM compound JOIN uv_data u ON(u.compound_id = compound.compound_id)  WHERE ABS(wavelength - ?) <= ?";
	
	private static final String MS_DEFAULT_UNIT = "Da";
	
	/*
	 * SQL statements for proposed ms_data table for MS index.
	 * 
	private static final String SQL_ADD_MS_ION = "INSERT INTO ms_data(compound_id,mz,rel_intensity,adduct) VALUES(?,?,?,?)";
	private static final String SQL_DEL_MS_ION = "DELETE FROM ms_data WHERE compound_id=? AND mz=?";
	private static final String SQL_DEL_MS_ADDUCT = "DELETE FROM ms_data WHERE compound_id=? AND adduct=?";
	private static final String SQL_GET_MS_ADDUCTS = "SELECT DISTINCT adduct FROM ms_data WHERE compound_id=? ORDER BY adduct";
	private static final String SQL_GET_MS_IONS = "SELECT adduct,mz,rel_intensity FROM ms_data WHERE compound_id=? GROUP BY adduct";
	private static final String SQL_GET_MS_IONS_FOR_ADDUCT = "SELECT mz,rel_intensity FROM ms_data WHERE compound_id=? AND adduct=?";
	*/
	
	/* Previous SQL Statements
	 * 

	private static final String SQL_COMPOUNDS_FOR_STRAIN = "SELECT compound.* FROM compound JOIN sample s ON (compound.compound_id = s.compound_id) WHERE s.culture_id = ? ORDER BY compound.compound_id";
	private static final String SQL_GET_SAMPLES = "SELECT sample.* FROM sample WHERE compound_id=? ORDER BY sample_id";
	*/

	private static final String SQL_LOAD_SORTABLE = "SELECT compound.* FROM compound ORDER BY %s %s";
	private static final String SQL_LOAD_WHERE = "SELECT compound.* FROM compound WHERE %s ORDER BY %s %s";
	
	private static final String SQL_GET_SAMPLES = "SELECT sample.* FROM sample JOIN compound_peaks ON (compound_peaks.sample_id = sample.sample_id) WHERE compound_peaks.compound_id=? ORDER BY sample.sample_id";
	private static final String SQL_COMPOUNDS_FOR_STRAIN = "SELECT compound.* FROM compound JOIN compound_peaks ON (compound_peaks.compound_id = compound.compound_id) JOIN material ON (compound_peaks.material_id = material.material_id) WHERE material.culture_id = ? ORDER BY compound.compound_id";
	private static final String SQL_COMPOUNDS_FOR_SAMPLE = "SELECT compound.* FROM compound JOIN compound_peaks ON (compound_peaks.compound_id = compound.compound_id) JOIN sample ON ( sample.material_id = compound_peaks.material_id) WHERE sample.sample_id = ? ORDER BY compound.compound_id";
	private static final String SQL_COMPOUNDS_FOR_MATERIAL = "SELECT compound.* FROM compound JOIN compound_peaks ON (compound_peaks.compound_id = compound.compound_id) WHERE compound_peaks.material_id = ? ORDER BY compound.compound_id";
	private static final String SQL_COMPOUNDS_FOR_SEPARATION = "SELECT DISTINCT compound.* FROM compound JOIN compound_peaks ON (compound_peaks.compound_id = compound.compound_id) WHERE compound_peaks.separation_id = ? ORDER BY compound.compound_id";

	private static final String SQL_COMPOUNDS_FOR_INCHI_KEY = "SELECT compound.* FROM compound WHERE compound.inchi_key = ? ORDER BY compound.compound_id";

	/**
	 * Retrieve all compounds sorted by specified column
	 * 
	 * @param data SQLData object
	 * @param column Column to sort by (use statics)
	 * @param sortDirection Direction for sort, either {@code SQLObject.ASCENDING_SORT} or {@code SQLObject.DESCENDING_SORT}
	 * @return Compound object with all compounds.
	 * @throws DataException
	 */
	public static SQLCompound compounds(SQLData data, String column, String sortDirection) throws DataException {
		SQLCompound newObj = new SQLCompound(data);
		String sqlString = String.format(SQL_LOAD_SORTABLE, column, sortDirection);
		newObj.loadSQL(sqlString);
		return newObj;
	}
	
	/**
	 * Retrieve selected compounds sorted by specified column 
	 * 
	 * @param data SQLData object
	 * @param column Column to sort by (use statics)
	 * @param sortDirection Direction for sort, either {@code SQLObject.ASCENDING_SORT} or {@code SQLObject.DESCENDING_SORT}
	 * @return Compound object with all compounds.
	 * @throws DataException
	 */
	public static SQLCompound compoundsWhere(SQLData data, String whereString, String column, String sortDirection) throws DataException {
		SQLCompound newObj = new SQLCompound(data);
		String sqlString = String.format(SQL_LOAD_WHERE, whereString, column, sortDirection);
		newObj.loadSQL(sqlString);
		return newObj;
	}
	
	/**
	 * Retrieve compounds for a Strain.
	 * 
	 * @param data SQLData object
	 * @param strainID ID of the strain.
	 * @return Compound object with compounds associated with the strain.
	 * @throws DataException
	 */
	public static SQLCompound compoundsForStrain(SQLData data, String strainID) throws DataException {
		try {
			SQLCompound newObj = new SQLCompound(data);
			PreparedStatement aSth = data.prepareStatement(SQL_COMPOUNDS_FOR_STRAIN);
			aSth.setString(1, strainID);
			newObj.loadUsingPreparedStatement(aSth);
			return newObj;
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

	/**
	 * Retrieve compounds for a Sample.
	 * 
	 * @param data SQLData object
	 * @param strainID ID of the sample.
	 * @return Compound object with compounds associated with the sample.
	 * @throws DataException
	 */
	public static SQLCompound compoundsForSample(SQLData data, String sampleID) throws DataException {
		try {
			SQLCompound newObj = new SQLCompound(data);
			PreparedStatement aSth = data.prepareStatement(SQL_COMPOUNDS_FOR_SAMPLE);
			aSth.setString(1, sampleID);
			newObj.loadUsingPreparedStatement(aSth);
			return newObj;
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

	/**
	 * Retrieve compounds for a Material.
	 * 
	 * @param data SQLData object
	 * @param strainID ID of the material.
	 * @return Compound object with compounds associated with the material.
	 * @throws DataException
	 */
	public static SQLCompound compoundsForMaterial(SQLData data, String materialID) throws DataException {
		try {
			SQLCompound newObj = new SQLCompound(data);
			PreparedStatement aSth = data.prepareStatement(SQL_COMPOUNDS_FOR_MATERIAL);
			aSth.setString(1, materialID);
			newObj.loadUsingPreparedStatement(aSth);
			return newObj;
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

	/**
	 * Retrieve compounds for a Separation.
	 * 
	 * @param data SQLData object
	 * @param seprationID ID of the Separation.
	 * @return Compound object with compounds associated with the sample.
	 * @throws DataException
	 */
	public static SQLCompound compoundsForSeparation(SQLData data, String separationID) throws DataException {
		try {
			SQLCompound newObj = new SQLCompound(data);
			PreparedStatement aSth = data.prepareStatement(SQL_COMPOUNDS_FOR_SEPARATION);
			aSth.setString(1, separationID);
			newObj.loadUsingPreparedStatement(aSth);
			return newObj;
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

	/**
	 * Retrieve compounds for InChI key.
	 * 
	 * @param data SQLData object
	 * @param seprationID ID of the Separation.
	 * @return Compound object with compounds associated with the sample.
	 * @throws DataException
	 */
	public static SQLCompound compoundsForInChiKey(SQLData data, String inchiKey) throws DataException {
		try {
			SQLCompound newObj = new SQLCompound(data);
			PreparedStatement aSth = data.prepareStatement(SQL_COMPOUNDS_FOR_INCHI_KEY);
			aSth.setString(1, inchiKey);
			newObj.loadUsingPreparedStatement(aSth);
			return newObj;
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

	/**
	 * Retrieve compounds for a Strain.
	 * 
	 * @param data SQLData object
	 * @param aStrain a Strain object.
	 * @return Compound object with compounds associated with the strain.
	 * @throws DataException
	 */
	public static SQLCompound compoundsForStrain(SQLData data, Strain aStrain) throws DataException {
		return SQLCompound.compoundsForStrain(data, aStrain.getID());
	}
	/**
	 * Creates a new Compound record.
	 * 
	 * @param data SQLData object
	 * @param newID ID for the new compound record.
	 * @return Compound object for the new compound.
	 * @throws DataException
	 */
	public static SQLCompound create(SQLData data, String newID) throws DataException {
		SQLCompound aCompound = new SQLCompound(data);
		aCompound.makeNewWithValue(INSERT_NEW_COMPOUND_SQL, newID);
		return aCompound;
	}
	
	public static SQLCompound load(SQLData data, String compoundID) throws DataException {
		SQLCompound aCompound = new SQLCompound(data);
		aCompound.myID = compoundID;
		aCompound.fetchRecord();
		return aCompound;
	}
	
	public static SQLCompound loadLike(SQLData data, String query) throws DataException {
		try {
			if ( query.contains("*") )
				query = query.replace("*", "%");
			else 
				query = "%".concat(query).concat("%");
			
			SQLCompound newObj = new SQLCompound(data);
			PreparedStatement aSth = data.prepareStatement(SQL_LOAD_LIKE);
			aSth.setString(1, query);
			aSth.setString(2, query);
			aSth.setString(3, query);
			newObj.loadUsingPreparedStatement(aSth);
			return newObj;
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}
	

	protected SQLCompound(SQLData data) {
		super(data);
		this.initVals();
	}

	@Deprecated
	public SQLCompound(SQLData data, String anID) throws DataException {
		this(data);
		this.myID = anID;
		this.fetchRecord();
	}
	
	protected void initVals() {
		this.myData.setProjectField(PROJECT_COLUMN);
		this.idField = SQLCompound.ID_COLUMN;
		this.myData.setAccessRole(User.SAMPLE_ROLE);
	}
	
	protected void fetchRecord() throws DataException {
		this.fetchRecord(SQL_LOAD);
	}

	public String getName() throws DataException {
		return this.myData.getQUIETString(NAME_COLUMN);
	}
	
	public void setName(String newValue) throws DataException {
		this.myData.setString(NAME_COLUMN, newValue);
	}
	
	public String getFormula() throws DataException {
		return this.myData.getString(FORMULA_COLUMN);
	}
	
	public String getHTMLFormula() throws DataException {
		return getHTMLFormula(this.getFormula());
	}
	
	public static String getHTMLFormula(String formula) {
		if ( formula != null ) {
			return formula.replaceAll("(\\d+)", "<SUB>$1</SUB>");
		}	
		return null;
	}
	
	public void setFormula(String newValue) throws DataException {
		this.myData.setString(FORMULA_COLUMN, newValue);
	}
	
	public String getSmilesString() throws DataException {
		return this.myData.getString(SMILES_COLUMN);
	}
	
	public void setSmilesString(String newValue) throws DataException {
		this.myData.setString(SMILES_COLUMN, newValue);
	}
	
	public BigDecimal getAverageMass() throws DataException {
		return this.myData.getBigDecimal(AVERAGE_MASS_COLUMN);
	}
	
	public void setAverageMass(String newValue) throws DataException {
		this.setAverageMass(parseAmount(newValue, MS_DEFAULT_UNIT));
	}
	
	public void setAverageMass(BigDecimal newValue) throws DataException {
		// TODO should set to proper precision of average mass column DECIMAL(10,4)
		this.myData.setString(AVERAGE_MASS_COLUMN, newValue.toPlainString());
	}
	
	public BigDecimal getMonoisotopicMass() throws DataException {
		return this.myData.getBigDecimal(MONOISOTOPIC_MASS_COLUMN);
	}
	
	public void setMonoisotopicMass(BigDecimal newValue) throws DataException {
		// TODO should set to proper precision of MI mass column DECIMAL(11,5)
		this.myData.setString(MONOISOTOPIC_MASS_COLUMN, newValue.toPlainString());
	}
	
	public void setMonoisotopicMass(String newValue) throws DataException {
		this.setMonoisotopicMass(parseAmount(newValue, MS_DEFAULT_UNIT));
	}
	
	public String getNotes() throws DataException {
		return this.myData.getString(NOTES_COLUMN);
	}
	
	public void setNotes(String newValue) throws DataException {
		this.myData.setString(NOTES_COLUMN, newValue);
	}
	
	public void addNotes(String newNotes) throws DataException {
		StringBuffer myNotes = new StringBuffer(this.getNotes());
		myNotes.append(" ");
		myNotes.append(newNotes);
		this.myData.setString(NOTES_COLUMN, myNotes.toString());
	}
	
	public String getMDLData() throws DataException {
		return this.myData.getString(MDL_COLUMN);
	}
	
	public InputStream getMDLDataStream() throws DataException {
		return this.myData.getBinaryStream(MDL_COLUMN);
	}
	
	public void setMDLData(String newValue) throws DataException {
		this.myData.setString(MDL_COLUMN, newValue);
	}
	
	public boolean hasMDLData() throws DataException {
		return ! this.myData.isNull(MDL_COLUMN);
	}
	
	public void clearMDLData() throws DataException {
		this.myData.setNull(MDL_COLUMN);
	}
	
	public void clearThumbnail() throws DataException {
		this.myData.setNull(THUMBNAIL_COLUMN);	
	}

	public byte[] getThumbnail() throws DataException {
		return this.myData.getByteArray(THUMBNAIL_COLUMN);
	}

	public InputStream getThumbnailStream() throws DataException {
		return this.myData.getBinaryStream(THUMBNAIL_COLUMN);
	}

	public boolean hasThumbnail() throws DataException {
		return ! this.myData.isNull(MDL_COLUMN);
	}

	public void setTumbnail(byte[] data) throws DataException {
		this.myData.setByteArray(THUMBNAIL_COLUMN, data);
	}

	public Sample getSamples() throws DataException {
		SQLSample mySamples = new SQLSample(this.myData);
		try {
			PreparedStatement aSth = this.myData.prepareStatement(SQL_GET_SAMPLES);
			aSth.setString(1, this.myID);
			mySamples.loadUsingPreparedStatement(aSth);
			if ( mySamples.first())
				return mySamples;
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return null;
	}
	
	public ExternalFile getNMRDatafiles() throws DataException {
		return this.getDataFilesForType(NMR_DATA_TYPE);
	}
	
	public void addNMRDatafile(ExternalFile aFile) throws DataException {
		this.linkDataFile(aFile, NMR_DATA_TYPE);
	}
	
	public ExternalFile getMSDatafiles() throws DataException {
		return this.getDataFilesForType(MS_DATA_TYPE);
	}
	
	public void addMSDatafile(ExternalFile aFile) throws DataException {
		this.linkDataFile(aFile, MS_DATA_TYPE);
	}
	
	public ExternalFile getIRDatafiles() throws DataException {
		return this.getDataFilesForType(IR_DATA_TYPE);
	}
	
	public void addIRDatafile(ExternalFile aFile) throws DataException {
		this.linkDataFile(aFile, IR_DATA_TYPE);
	}

	public ExternalFile getUVDatafiles() throws DataException {
		return this.getDataFilesForType(UV_DATA_TYPE);
	}
	
	public void addUVDatafile(ExternalFile aFile) throws DataException {
		this.linkDataFile(aFile, UV_DATA_TYPE);
	}
	
	/**
	 * Retrieves UV spectrum of the compound.  Should be JCAMP-DX format.
	 * 
	 * @return UV spectrum as JCAMP-DX String
	 * @throws SQLException
	 */
	public String getUVData() throws DataException {
		return this.myData.getString(UV_DATA_COLUMN);
	}
	
	/**
	 * Sets UV spectrum of the compound.  Should be JCAMP-DX format.
	 * 
	 * @param newData UV spectrum as JCAMP-DX String
	 * @throws SQLException
	 */
	public void setUVData(String newData) throws DataException {
		this.myData.setString(UV_DATA_COLUMN, newData);
	}

	public Map<Float, Float> getUVPeaks() throws DataException {
		try {
			PreparedStatement aPsth = this.myData.prepareStatement(UV_PEAK_SQL);
			aPsth.setString(1, this.myID);
			ResultSet results = aPsth.executeQuery();
			results.beforeFirst();
			Map<Float,Float> retVals = new HashMap<Float,Float>();
			while ( results.next() ) {
				retVals.put(results.getFloat(1), results.getFloat(2));
			}
			aPsth.close();
			return retVals;
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}
	
	public void addUVPeak(float wavelength, float relIntensity) throws DataException {
		if ( this.isAllowed(DEREPLICATION_PERM) ) {
			try {
				PreparedStatement aPsth = this.myData.prepareStatement(UV_ADD_PEAK_SQL);
				aPsth.setString(1, this.myID);
				aPsth.setFloat(2, wavelength);
				aPsth.setFloat(3, relIntensity);
				aPsth.executeUpdate();
				aPsth.close();
			} catch (SQLException e) {
				throw new DataException(e);
			}
		} else if ( this.myData.willThrowAccessExeceptions() ) { 
			throw new AccessException(this.myData.getUser(), this.myData.getAccessRole(), DEREPLICATION_PERM); 
		}
	}
	
	public void deleteUVPeak(float wavelength) throws DataException {
		if ( this.isAllowed(DEREPLICATION_PERM) ) {
			try {
				PreparedStatement aPsth = this.myData.prepareStatement(UV_DEL_PEAK_SQL);
				aPsth.setString(1, this.myID);
				aPsth.setFloat(2, wavelength);
				aPsth.executeUpdate();
				aPsth.close();
			} catch (SQLException e) {
				throw new DataException(e);
			}
		} else if ( this.myData.willThrowAccessExeceptions() ) { 
			throw new AccessException(this.myData.getUser(), this.myData.getAccessRole(), DEREPLICATION_PERM); 
		}
	}
	
	public boolean hasUVData() throws DataException {
		try {
			PreparedStatement aPsth = this.myData.prepareStatement("SELECT DISTINCT compound_id FROM uv_data WHERE compound_id=?");
			aPsth.setString(1, this.myID);
			ResultSet aResult = aPsth.executeQuery();
			return aResult.first();
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}
		
	@Deprecated
	public static SQLCompound findCompoundsByUVMax(SQLData data, String wavelength, String difference) throws DataException {
		SQLCompound newObj = new SQLCompound(data);
		try {
			PreparedStatement aPsth = newObj.myData.prepareStatement(SQL_UV_BASIC);
			aPsth.setString(1, wavelength);
			aPsth.setString(2, difference);
			newObj.myData.loadUsingPreparedStatement(aPsth);
			return newObj;
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

	@Deprecated
	public static SQLCompound findCompoundsByUVMax(SQLData data, float wavelength, float difference) throws DataException {
		SQLCompound newObj = new SQLCompound(data);
		try {
			PreparedStatement aPsth = newObj.myData.prepareStatement(SQL_UV_BASIC);
			aPsth.setFloat(1, wavelength);
			aPsth.setFloat(2, difference);
			newObj.myData.loadUsingPreparedStatement(aPsth);
			return newObj;
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

	@Deprecated
	public static SQLCompound findCompoundsByUVMax(SQLData data, float wavelength) throws DataException {
		return findCompoundsByUVMax(data, wavelength, 0.5f);
	}
	
	@Deprecated
	public static SQLCompound findCompoundsByUVMax(SQLData data, String wavelength) throws DataException {
		return findCompoundsByUVMax(data, wavelength, "0.5");
	}
	
	@Deprecated
	public static SQLCompound findCompoundsByUVMax(SQLData data, String wavelength, String waveDiff, String intensity, String intDiff) throws DataException {
		SQLCompound newObj = new SQLCompound(data);
		try {
			PreparedStatement aPsth = newObj.myData.prepareStatement(SQL_UV_INTENSITY);
			aPsth.setString(1, wavelength);
			aPsth.setString(2, waveDiff);
			aPsth.setString(3, intensity);
			aPsth.setString(4, intDiff);
			newObj.myData.loadUsingPreparedStatement(aPsth);
			return newObj;
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

	@Deprecated
	public static SQLCompound findCompoundsByUVMax(SQLData data, float wavelength, float waveDiff, float intensity, float intDiff) throws DataException {
		SQLCompound newObj = new SQLCompound(data);
		try {
			PreparedStatement aPsth = newObj.myData.prepareStatement(SQL_UV_INTENSITY);
			aPsth.setFloat(1, wavelength);
			aPsth.setFloat(2, waveDiff);
			aPsth.setFloat(3, intensity);
			aPsth.setFloat(4, intDiff);
			newObj.myData.loadUsingPreparedStatement(aPsth);
			return newObj;
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}
	
	public String[] dataTypes() {
		String[] retVals = { Compound.MS_DATA_TYPE, Compound.NMR_DATA_TYPE, Compound.IR_DATA_TYPE, Compound.UV_DATA_TYPE };
		return retVals;
	}
	
	public String getDataFileClass() {
		return DATA_FILE_CLASS;
	}

	public void setProjectID(String projectID) throws DataException {
		this.myData.setString(PROJECT_COLUMN, projectID);
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

	
	public BigDecimal getRetentionTime(CompoundObject source) throws DataException {
		if ( source instanceof Sample )
			return this.getRetentionTimeForSample(((Sample)source).getID());
		else if ( source instanceof Separation )
			return this.getRetentionTimeForSeparation(((Separation)source).getID());
		else
			return null;
	}

	
	public BigDecimal getRetentionTimeForSample(String sampleID) throws DataException {
		if ( this.myID != null ) {
			try {
				PreparedStatement aPsth = this.myData.prepareStatement(SQLSample.SQL_GET_RT_COMPOUND);
				aPsth.setString(1, sampleID);
				aPsth.setString(2, this.myID);
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

	
	public BigDecimal getRetentionTimeForSeparation(String separationID) throws DataException {
		if ( this.myID != null ) {
			try {
				PreparedStatement aPsth = this.myData.prepareStatement(SQLSeparation.SQL_GET_RT_COMPOUND);
				aPsth.setString(1, separationID);
				aPsth.setString(2, this.myID);
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

	
	public String getInChiString() throws DataException {
		return this.myData.getString(INCHI_STRING_COLUMN);
	}

	
	public void setInChiString(String newValue) throws DataException {
		this.myData.setString(INCHI_STRING_COLUMN, newValue);
	}

	
	public String getInChiKey() throws DataException {
		return this.myData.getString(INCHI_KEY_COLUMN);
	}

	
	public void setInChiKey(String newValue) throws DataException {
		this.myData.setString(INCHI_KEY_COLUMN, newValue);
	}

	@Override
	public Material getMaterials() throws DataException {
		return SQLMaterial.materialsForCompound(myData, this.getID());
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

	@Override
	public BigDecimal getRetentionTimeForMaterial(String materialID)
			throws DataException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRemoteID() throws DataException {
		return this.getID();
	}

	@Override
	public String getRemoteHostID() throws DataException {
		return this.myData.getString(REMOTE_HOST_COLUMN);
	}

/*
	public boolean hasMSData() throws DataException {
		try {
			PreparedStatement aPsth = this.myData.prepareStatement("SELECT DISTINCT compound_id FROM ms_data WHERE compound_id=?");
			aPsth.setString(1, this.myID);
			ResultSet aResult = aPsth.executeQuery();
			return aResult.first();
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}
	

	public void addMSIon(float mz, float relIntensity, String adduct) throws DataException {
		if ( this.isAllowed(DEREPLICATION_PERM) ) {
			try {
				PreparedStatement aPsth = this.myData.prepareStatement(SQL_ADD_MS_ION);
				aPsth.setString(1, this.myID);
				aPsth.setFloat(2, mz);
				aPsth.setFloat(3, relIntensity);
				aPsth.setString(4, adduct);
				aPsth.executeUpdate();
				aPsth.close();
			} catch (SQLException e) {
				throw new DataException(e);
			}
		}
	}

	public void deleteMSIon(float mz) throws DataException {
		if ( this.isAllowed(DEREPLICATION_PERM) ) {
			try {
				PreparedStatement aPsth = this.myData.prepareStatement(SQL_DEL_MS_ION);
				aPsth.setString(1, this.myID);
				aPsth.setFloat(2, mz);
				aPsth.executeUpdate();
				aPsth.close();
			} catch (SQLException e) {
				throw new DataException(e);
			}
		}
	}

	public void deleteMsAdduct(String adduct) throws DataException {
		if ( this.isAllowed(DEREPLICATION_PERM) ) {
			try {
				PreparedStatement aPsth = this.myData.prepareStatement(SQL_DEL_MS_ADDUCT);
				aPsth.setString(1, this.myID);
				aPsth.setString(2, adduct);
				aPsth.executeUpdate();
				aPsth.close();
			} catch (SQLException e) {
				throw new DataException(e);
			}
		}
	}

	public List<String> msAdducts() throws DataException {
		try {
			PreparedStatement aPsth = this.myData.prepareStatement(SQL_GET_MS_ADDUCTS);
			aPsth.setString(1, this.myID);
			List<String> retVals = new ArrayList<String>();
			ResultSet res = aPsth.executeQuery();
			res.beforeFirst();
			while ( res.next() ) {
				retVals.add(res.getString(1));
			}
			res.close();
			aPsth.close();
			return retVals;
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

	public Map<String, Map<Float, Float>> msIons() throws DataException {
		try {
			PreparedStatement aPsth = this.myData.prepareStatement(SQL_GET_MS_IONS);
			aPsth.setString(1, this.myID);
			Map<String, Map<Float, Float>> retVals = new HashMap<String, Map<Float, Float>>();
			ResultSet res = aPsth.executeQuery();
			res.beforeFirst();
			while ( res.next() ) {
				String anAdduct = res.getString(1);
				Map<Float, Float> addMap;
				if ( retVals.containsKey(anAdduct) ) {
					addMap = retVals.get(anAdduct);
				} else {
					addMap = new HashMap<Float, Float>();
					retVals.put(anAdduct, addMap);
				}
				addMap.put(res.getFloat(2), res.getFloat(3));
			}
			res.close();
			aPsth.close();
			return retVals;
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

	public Map<Float, Float> msIonsForAdduct(String adduct) throws DataException {
		try {
			PreparedStatement aPsth = this.myData.prepareStatement(SQL_GET_MS_IONS_FOR_ADDUCT);
			aPsth.setString(1, this.myID);
			aPsth.setString(2, adduct);
			Map<Float, Float> retVals = new HashMap<Float, Float>();
			ResultSet res = aPsth.executeQuery();
			res.beforeFirst();
			while ( res.next() ) {
				retVals.put(res.getFloat(1), res.getFloat(2));
			}
			res.close();
			aPsth.close();
			return retVals;
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}
 */
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