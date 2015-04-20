/**
 * 
 */
package edu.uic.orjala.cyanos.sql;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;

import edu.uic.orjala.cyanos.AssayData;
import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.Material;
import edu.uic.orjala.cyanos.Sample;
import edu.uic.orjala.cyanos.Strain;

/**
 * @author George Chlipala
 *
 */
public class SQLAssayData extends SQLObject implements AssayData {

	public static final String ASSAY_ID_COLUMN = "assay_id";
	public final static String CULTURE_ID_COLUMN = "culture_id";
	public final static String VALUE_COLUMN = "activity";
	public final static String NAME_COLUMN = "name";
	public final static String SAMPLE_ID_COLUMN = "sample_id";
	public final static String MATERIAL_ID_COLUMN = "material_id";
	public final static String VALUE_SIGN_COLUMN = "sign";
	public final static String VALUE_STDEV_COLUMN = "std_dev";
	public final static String CONCENTRATION_COLUMN = "concentration";
	public final static String ROW_COLUMN = "row";
	public final static String COLUMN_COLUMN = "col";
	public final static String IS_ACTIVE_COLUMN = "is_active";
	
	private final static String ACTIVITY_FORMAT = "ai.".concat(SQLAssay.UNIT_COLUMN);
	private final static String ASSAY_DATE_COLUMN = "ai.".concat(SQLAssay.DATE_COLUMN);
	private final static String ASSAY_TARGET_COLUMN = "ai.".concat(SQLAssay.TARGET_COLUMN);
	private final static String ASSAY_NAME_COLUMN = "ai.".concat(SQLAssay.NAME_COLUMN);
	private final static String ASSAY_SIG_FIG_COLUMN = "ai.".concat(SQLAssay.SIG_FIGS_COLUMN);
	
	protected static final String SQL_LOAD_BASE = "SELECT assay.*,ai.*,active(assay.activity + assay.sign,ai.active_level,ai.active_op) AS is_active FROM assay JOIN assay_info ai ON (assay.assay_id = ai.assay_id)";
	
	private static final String SQL_LOAD_ALL_DATA = SQL_LOAD_BASE.concat(" WHERE assay.assay_id=? ORDER BY assay.row,assay.col");
	private static final String SQL_LOAD_FOR_STRAIN = SQL_LOAD_BASE.concat(" WHERE culture_id=? ORDER BY ai.date,assay.row,assay.col");
	private static final String SQL_LOAD_FOR_SAMPLE = SQL_LOAD_BASE.concat(" WHERE sample_id=? ORDER BY ai.date,assay.row,assay.col");
	private static final String SQL_LOAD_FOR_MATERIAL = SQL_LOAD_BASE.concat(" WHERE material_id=? ORDER BY ai.date,assay.row,assay.col");
	private static final String SQL_LOAD_FOR_COMPOUND = SQL_LOAD_BASE.concat(" JOIN compound_peaks ON ( compound_peaks.material_id = assay.material_id) WHERE compound_peaks.compound_id=? ORDER BY ai.date,assay.row,assay.col");

	private static final String SQL_LOAD_FOR_STRAIN_TARGET = SQL_LOAD_BASE.concat(" WHERE culture_id=? AND ai.target = ? ORDER BY ai.date,assay.row,assay.col");
	private static final String SQL_LOAD_FOR_SAMPLE_TARGET = SQL_LOAD_BASE.concat(" WHERE sample_id=? AND ai.target = ? ORDER BY ai.date,assay.row,assay.col");
	private static final String SQL_LOAD_FOR_MATERIAL_TARGET = SQL_LOAD_BASE.concat(" WHERE material_id=? AND ai.target = ? ORDER BY ai.date,assay.row,assay.col");
	private static final String SQL_LOAD_FOR_COMPOUND_TARGET = SQL_LOAD_BASE.concat(" JOIN compound_peaks ON ( compound_peaks.material_id = assay.material_id) WHERE compound_peaks.compound_id=? AND ai.target = ? ORDER BY ai.date,assay.row,assay.col");

	private static final String SQL_LOAD_ACTIVES = SQL_LOAD_BASE.concat(" WHERE assay.assay_id=? AND active(assay.activity + assay.sign,ai.active_level,ai.active_op) = 1 ORDER BY assay.row,assay.col");
	
	public static AssayData activeData(SQLAssay anAssay) throws DataException {
		SQLAssayData assayData = new SQLAssayData(anAssay.myData);
		try {
			PreparedStatement aSth = assayData.myData.prepareStatement(SQL_LOAD_ACTIVES);
			aSth.setString(1, anAssay.getID());
			assayData.myData.loadUsingPreparedStatement(aSth);
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return assayData;
	}
	
	public static AssayData dataForStrainID(SQLData data, String strainID) throws DataException {
		SQLAssayData assayData = new SQLAssayData(data);
		try {
			PreparedStatement aSth = assayData.myData.prepareStatement(SQL_LOAD_FOR_STRAIN);
			aSth.setString(1, strainID);
			assayData.myData.loadUsingPreparedStatement(aSth);
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return assayData;
	}
	
	public static AssayData dataForSampleID(SQLData data, String sampleID) throws DataException {
		SQLAssayData assayData = new SQLAssayData(data);
		try {
			PreparedStatement aSth = assayData.myData.prepareStatement(SQL_LOAD_FOR_SAMPLE);
			aSth.setString(1, sampleID);
			assayData.myData.loadUsingPreparedStatement(aSth);
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return assayData;
	}
	
	public static AssayData dataForCompoundID(SQLData data, String compoundID) throws DataException {
		SQLAssayData assayData = new SQLAssayData(data);
		try {
			PreparedStatement aSth = assayData.myData.prepareStatement(SQL_LOAD_FOR_COMPOUND);
			aSth.setString(1, compoundID);
			assayData.myData.loadUsingPreparedStatement(aSth);
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return assayData;
	}
	
	public static AssayData dataForMaterialID(SQLData data, String materialID) throws DataException {
		SQLAssayData assayData = new SQLAssayData(data);
		try {
			PreparedStatement aSth = assayData.myData.prepareStatement(SQL_LOAD_FOR_MATERIAL);
			aSth.setString(1, materialID);
			assayData.myData.loadUsingPreparedStatement(aSth);
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return assayData;
	}
	
	public static AssayData dataForStrainID(SQLData data, String strainID, String target) throws DataException {
		SQLAssayData assayData = new SQLAssayData(data);
		try {
			PreparedStatement aSth = assayData.myData.prepareStatement(SQL_LOAD_FOR_STRAIN_TARGET);
			aSth.setString(1, strainID);
			aSth.setString(2, target);
			assayData.myData.loadUsingPreparedStatement(aSth);
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return assayData;
	}
	
	public static AssayData dataForSampleID(SQLData data, String sampleID, String target) throws DataException {
		SQLAssayData assayData = new SQLAssayData(data);
		try {
			PreparedStatement aSth = assayData.myData.prepareStatement(SQL_LOAD_FOR_SAMPLE_TARGET);
			aSth.setString(1, sampleID);
			aSth.setString(2, target);
			assayData.myData.loadUsingPreparedStatement(aSth);
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return assayData;
	}
	
	public static AssayData dataForMaterialID(SQLData data, String materialID, String target) throws DataException {
		SQLAssayData assayData = new SQLAssayData(data);
		try {
			PreparedStatement aSth = assayData.myData.prepareStatement(SQL_LOAD_FOR_MATERIAL_TARGET);
			aSth.setString(1, materialID);
			aSth.setString(2, target);
			assayData.myData.loadUsingPreparedStatement(aSth);
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return assayData;
	}
	
	public static AssayData dataForCompoundID(SQLData data, String compoundID, String target) throws DataException {
		SQLAssayData assayData = new SQLAssayData(data);
		try {
			PreparedStatement aSth = assayData.myData.prepareStatement(SQL_LOAD_FOR_COMPOUND_TARGET);
			aSth.setString(1, compoundID);
			aSth.setString(2, target);
			assayData.myData.loadUsingPreparedStatement(aSth);
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return assayData;
	}
	
	public static AssayData dataForStrain(SQLData data, Strain aStrain) throws DataException {
		return SQLAssayData.dataForStrainID(data, aStrain.getID());
	}
	
	public static AssayData dataForSample(SQLData data, Sample aSample) throws DataException {
		return SQLAssayData.dataForSampleID(data, aSample.getID());
	}
	
	protected SQLAssayData(SQLData data) throws DataException {
		super(data);
	}
	
	protected SQLAssayData(SQLAssay anAssay) throws DataException {
		super(anAssay.myData);
		this.fetchRecord();
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.sql.SQLObject#fetchRecord()
	 */
	
	protected void fetchRecord() throws DataException {
		this.fetchRecord(SQL_LOAD_ALL_DATA);
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.AssayData#getActivity()
	 */
	public BigDecimal getActivity() throws DataException {
		return this.myData.getBigDecimal(VALUE_COLUMN);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.AssayData#getActivityString()
	 */
	public String getActivityString() throws DataException {
		BigDecimal value = this.getActivity();
		if ( value == null ) {
			return "-";
		}
		int sign = this.myData.getInt(VALUE_SIGN_COLUMN);
		String valueString = ( value.compareTo(BigDecimal.ZERO) == 0 ? "0" : value.round(new MathContext(this.myData.getInt(ASSAY_SIG_FIG_COLUMN))).toPlainString());
		switch (sign) {
			case 1: valueString = "> ".concat(valueString); break;
			case -1: valueString = "< ".concat(valueString); break;
		}
		String unit = this.myData.getString(ACTIVITY_FORMAT);
		if ( unit != null && unit.length() > 0 ) {
			valueString = valueString.concat(" ").concat(unit);
		}
		return valueString;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.AssayData#getAssayID()
	 */
	public String getAssayID() throws DataException {
		return this.myData.getString(ASSAY_ID_COLUMN);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.AssayData#getConcentration()
	 */
	public BigDecimal getConcentration() throws DataException {
		return this.myData.getBigDecimal(CONCENTRATION_COLUMN);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.AssayData#getLabel()
	 */
	public String getLabel() throws DataException {
		return this.myData.getString(NAME_COLUMN);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.AssayData#getSample()
	 */
	public Sample getSample() throws DataException {
		return SQLSample.load(this.myData, this.getSampleID());
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.AssayData#getSampleID()
	 */
	public String getSampleID() throws DataException {
		return this.myData.getString(SAMPLE_ID_COLUMN);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.AssayData#getMaterial()
	 */
	public Material getMaterial() throws DataException {
		return SQLMaterial.load(this.myData, this.getMaterialID());
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.AssayData#getMaterialID()
	 */
	public String getMaterialID() throws DataException {
		return this.myData.getString(MATERIAL_ID_COLUMN);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.AssayData#getStrainID()
	 */
	public String getStrainID() throws DataException {
		return this.myData.getString(CULTURE_ID_COLUMN);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.AssayData#isActive()
	 */
	public boolean isActive() throws DataException {
		if ( this.myData.isNull(VALUE_COLUMN) ) return false;
		return ( this.myData.getInt(IS_ACTIVE_COLUMN) == 1);
	}
	
	public String getLocation() throws DataException {
		return String.format("%s%02d", ALPHABET[this.myData.getInt(ROW_COLUMN)], this.myData.getInt(COLUMN_COLUMN));
	}

	
	public String getAssayName() throws DataException {
		return this.myData.getString(ASSAY_NAME_COLUMN);
	}

	
	public String getAssayTarget() throws DataException {
		return this.myData.getString(ASSAY_TARGET_COLUMN);
	}

	
	public Date getDate() throws DataException {
		return this.myData.getDate(ASSAY_DATE_COLUMN);
	}

	public BigDecimal getActivitySD() throws DataException {
		return this.myData.getBigDecimal(VALUE_STDEV_COLUMN);
	}

	public String getConcentrationString() throws DataException {
		return autoFormatAmount(this.getConcentration(), CONCENTRATION_TYPE);
	}
}
