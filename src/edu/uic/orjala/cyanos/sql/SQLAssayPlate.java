/**
 * 
 */
package edu.uic.orjala.cyanos.sql;

import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import edu.uic.orjala.cyanos.Assay;
import edu.uic.orjala.cyanos.AssayPlate;
import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.Material;
import edu.uic.orjala.cyanos.Sample;
import edu.uic.orjala.cyanos.Strain;

/**
 * @author George Chlipala
 *
 */
public class SQLAssayPlate extends SQLBoxObject implements AssayPlate {

	protected Map<String, Integer> locationMap;
	protected SQLAssay myAssay = null;
	protected String actFormat = null;
	protected MathContext sfMC = null;
	
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
	
	public static final String[] ALL_COLUMNS = { ASSAY_ID_COLUMN, CULTURE_ID_COLUMN, VALUE_COLUMN, 
			NAME_COLUMN, SAMPLE_ID_COLUMN, MATERIAL_ID_COLUMN, VALUE_SIGN_COLUMN, VALUE_STDEV_COLUMN, 
			CONCENTRATION_COLUMN, ROW_COLUMN, COLUMN_COLUMN };
	
	private final static String SQL_BASE = sqlBase("assay", ALL_COLUMNS);

	private static final String SQL_LOAD_ALL_DATA = SQL_BASE + " WHERE assay_id=?";
	
	private static final String SQL_INSERT_DATA = "INSERT INTO assay(assay_id,row,col,culture_id) VALUES(?,?,?,?)";

	public static final String SQL_INSERT_XML = sqlInsert("assay", ALL_COLUMNS);
	
	/*
	 * Parameters and SQL for a "cleaned-up" database schema.	
	 * 
	public final static String ROW_COLUMN = "loc_x";
	public final static String COLUMN_COLUMN = "loc_y";
	private static final String SQL_LOAD_ALL_DATA = "SELECT assay_data.* FROM assay_data WHERE assay_id=?";
	private static final String SQL_LOAD_FOR_STRAIN = "SELECT assay_data.* FROM assay_data WHERE assay_id=? AND culture_id=?";
	private static final String SQL_LOAD_FOR_SAMPLE = "SELECT assay_data.* FROM assay_data WHERE assay_id=? AND sample_id=?";
	private static final String SQL_LOAD_ACTIVES = "SELECT assay_data.* FROM assay_data WHERE assay_id=? AND active(activity,?,?) = 1";
	private static final String SQL_INSERT_DATA = "INSERT INTO assay_data(assay_id,row,col) VALUES(?,?,?)";

	 */
	
	protected static AssayPlate dataForAssay(SQLAssay anAssay)  throws DataException {
		SQLAssayPlate assayData = new SQLAssayPlate(anAssay.myData);
		assayData.myAssay = anAssay;
		assayData.setupAssay();
		try {
			PreparedStatement aSth = assayData.myData.prepareStatement(SQL_LOAD_ALL_DATA);
			aSth.setString(1, anAssay.getID());
			assayData.myData.loadUsingPreparedStatement(aSth);
		} catch (SQLException e) {
			throw new DataException(e);
		}
		assayData.buildLocationMap();
		return assayData;
	}
	
	protected SQLAssayPlate(SQLData data) throws DataException {
		super(data);
	}
	
	protected SQLAssayPlate(SQLAssay anAssay) throws DataException {
		super(anAssay.myData);
		this.myAssay = anAssay;
		this.fetchRecord();
	}

	protected void setupAssay() throws DataException {
		if ( this.myAssay != null ) {
			this.myID = this.myAssay.getID();
			this.maxLength = this.myAssay.getLength();
			this.maxWidth = this.myAssay.getWidth();
			this.actFormat = this.myAssay.getUnit();
			this.sfMC = new MathContext(this.myAssay.getSigFigs());
		}
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.sql.SQLObject#fetchRecord()
	 */
	
	protected void fetchRecord() throws DataException {
		this.setupAssay();
		this.fetchRecord(SQL_LOAD_ALL_DATA);
		this.buildLocationMap();
	}
	
	void buildLocationMap() throws DataException {
		if ( this.myData != null ) {
			this.locationMap = new HashMap<String, Integer>();
			this.myData.beforeFirst();
			while ( this.myData.next() ) {
				String key = String.format("%d,%d", this.myData.getInt(ROW_COLUMN), this.myData.getInt(COLUMN_COLUMN));
				this.locationMap.put(key, this.myData.getRow());
			}
		}
	}
	
	public boolean next() throws DataException {
		if ( this.myData.next() ) {
			this.currCol = this.myData.getInt(COLUMN_COLUMN);
			this.currRow = this.myData.getInt(ROW_COLUMN);
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Data#previous()
	 */
	public boolean previous() throws DataException {
		if ( this.myData.previous() ) {
			this.currCol = this.myData.getInt(COLUMN_COLUMN);
			this.currRow = this.myData.getInt(ROW_COLUMN);
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Data#first()
	 */
	public boolean first() throws DataException {
		if ( this.myData.first() ) {
			this.currCol = this.myData.getInt(COLUMN_COLUMN);
			this.currRow = this.myData.getInt(ROW_COLUMN);
			return true;
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Data#last()
	 */
	public boolean last() throws DataException {
		if ( this.myData.last() ) {
			this.currCol = this.myData.getInt(COLUMN_COLUMN);
			this.currRow = this.myData.getInt(ROW_COLUMN);
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Data#beforeFirst()
	 */
	public void beforeFirst() throws DataException {
		this.myData.beforeFirst();
		this.currCol = 0;
		this.currRow = 0;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Data#afterLast()
	 */
	public void afterLast() throws DataException {
		this.myData.afterLast();
		this.currCol = 0;
		this.currRow = 0;
	}


	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.AssayData#addCurrentLocation()
	 */
	public void addCurrentLocation(String strainID) throws DataException {
		this.addLocation(this.currRow, this.currCol, strainID);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.AssayData#addLocation(java.lang.String)
	 */
	public void addLocation(String location, String strainID) throws DataException {
		int[] locs = parseLocation(location);
		this.addLocation(locs[0], locs[1], strainID);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.AssayData#addLocation(int, int)
	 */
	public void addLocation(int row, int col, String strainID) throws DataException {
		if ( row > 0 && col > 0 && row <= this.maxLength && col <= this.maxWidth ) {
			String key = String.format("%d,%d", row, col);
			if ( ! this.locationMap.containsKey(key) ) {
				try {
					PreparedStatement insert = this.myData.prepareStatement(SQL_INSERT_DATA);
					insert.setString(1, this.myID);
					insert.setInt(2, row);
					insert.setInt(3, col);
					insert.setString(4, strainID);
					if ( insert.executeUpdate() == 1 ) {
						this.myData.reload();
						this.buildLocationMap();
					}
					insert.close();
				} catch (SQLException e) {
					throw new DataException(e);
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.AssayData#currentLocationExists()
	 */
	public boolean currentLocationExists() throws DataException {
		return this.locationExists(this.currRow, this.currCol);
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
		int sign = this.myData.getInt(VALUE_SIGN_COLUMN);
		String value = this.getActivity().round(this.sfMC).toPlainString();
		switch (sign) {
			case 1: value = "> ".concat(value); break;
			case -1: value = "< ".concat(value); break;
		}
		if ( this.actFormat != null && this.actFormat.length() > 0 ) {
			value = value.concat(" ").concat(this.actFormat);
		}
		return value;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.AssayData#getAssayID()
	 */
	public String getAssayID() {
		return this.myAssay.getID();
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
		String operator = this.myAssay.getActiveOperator();
		BigDecimal myActivity = this.getActivity().add(this.myData.getBigDecimal(VALUE_SIGN_COLUMN));
		int compare = myActivity.compareTo(this.myAssay.getActiveLevel());
		if ( operator.equals(SQLAssay.OPERATOR_EQUAL) ) {
			return compare == 0;
		} else if ( operator.equals(Assay.OPERATOR_NOT_EQUAL) ) {
			return compare != 0;
		} else if ( operator.equals(Assay.OPERATOR_GREATER_THAN)) {
			return compare > 0;
		} else if ( operator.equals(Assay.OPERATOR_GREATER_EQUAL)) {
			return compare >= 0;
		} else if ( operator.equals(Assay.OPERATOR_LESS_THAN)) {
			return compare < 0;
		} else if ( operator.equals(Assay.OPERATOR_LESS_EQUAL)) {
			return compare <= 0;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.AssayData#locationExists(int, int)
	 */
	public boolean locationExists(int row, int col) throws DataException {
		if ( row > 0 && col > 0 && row <= this.maxLength && col <= this.maxWidth ) {
			String key = String.format("%d,%d", row, col);
			return this.locationMap.containsKey(key);
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.AssayData#locationExists(java.lang.String)
	 */
	public boolean locationExists(String location) throws DataException {
		int[] loc = parseLocation(location);
		return this.locationExists(loc[0], loc[1]);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.AssayData#setActivity(java.lang.String)
	 */
	public void setActivity(String newValue) throws DataException {
		if ( newValue.startsWith(">") ) {
			this.myData.setInt(VALUE_SIGN_COLUMN, 1);
			newValue = newValue.substring(1);
		} else if ( newValue.startsWith("<") ) {
			this.myData.setInt(VALUE_SIGN_COLUMN, -1);
			newValue = newValue.substring(1);
		} else {
			this.myData.setInt(VALUE_SIGN_COLUMN, 0);
		}
		this.myData.setStringNullBlank(VALUE_COLUMN, newValue);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.AssayData#setConcentration(java.lang.String)
	 */
	public void setConcentration(String aConcentration) throws DataException {
		this.myData.setStringNullBlank(CONCENTRATION_COLUMN, aConcentration);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.AssayData#setConcentration(float)
	 */
	public void setConcentration(BigDecimal aConcentration) throws DataException {
		this.myData.setBigDecimal(CONCENTRATION_COLUMN, aConcentration);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.AssayData#setLabel(java.lang.String)
	 */
	public void setLabel(String aLabel) throws DataException {
		this.myData.setString(NAME_COLUMN, aLabel);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.AssayData#setSample(edu.uic.orjala.cyanos.Sample)
	 */
	public void setSample(Sample aSample) throws DataException {
		this.setStrainID(aSample.getCultureID());
		this.setMaterialID(aSample.getParentMaterialID());
		this.myData.setString(SAMPLE_ID_COLUMN, aSample.getID());
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.AssayData#setSampleID(java.lang.String)
	 */
	public void setSampleID(String aSampleID) throws DataException {
		this.myData.setStringNullBlank(SAMPLE_ID_COLUMN, aSampleID);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.AssayData#setMaterial(edu.uic.orjala.cyanos.Material)
	 */
	public void setMaterial(Material aMaterial) throws DataException {
		this.setStrainID(aMaterial.getCultureID());
		this.myData.setString(MATERIAL_ID_COLUMN, aMaterial.getID());
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.AssayData#setMaterialID(java.lang.String)
	 */
	public void setMaterialID(String aMaterialID) throws DataException {
		this.myData.setStringNullBlank(MATERIAL_ID_COLUMN, aMaterialID);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.AssayData#setStdev(float)
	 */
	public void setStdev(BigDecimal newValue) throws DataException {
		this.myData.setBigDecimal(VALUE_STDEV_COLUMN, newValue);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.AssayData#setStdev(java.lang.String)
	 */
	public void setStdev(String newValue) throws DataException {
		this.myData.setStringNullBlank(VALUE_STDEV_COLUMN, newValue);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.AssayData#setStrain(edu.uic.orjala.cyanos.Strain)
	 */
	public void setStrain(Strain aStrain) throws DataException {
		this.myData.setString(CULTURE_ID_COLUMN, aStrain.getID());
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.AssayData#setStrainID(java.lang.String)
	 */
	public void setStrainID(String aStrainID) throws DataException {
		this.myData.setStringNullBlank(CULTURE_ID_COLUMN, aStrainID);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Box#beforeFirstColumn()
	 */
	public void beforeFirstColumn() throws DataException {
		super.beforeFirstColumn();
		this.myData.beforeFirst();
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Box#beforeFirstRow()
	 */
	public void beforeFirstRow() throws DataException {
		super.beforeFirstRow();
		this.myData.beforeFirst();
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Box#firstColumn()
	 */
	public void firstColumn() throws DataException {
		super.firstColumn();
		this.setCurrentLocation();
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Box#firstRow()
	 */
	public void firstRow() throws DataException {
		super.firstRow();
		this.setCurrentLocation();
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Box#getLength()
	 */
	public int getLength() throws DataException {
		return this.maxLength;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Box#getWidth()
	 */
	public int getWidth() throws DataException {
		return this.maxWidth;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Box#gotoLocation(int, int)
	 */
	public boolean gotoLocation(int row, int col) throws DataException {
		if ( super.gotoLocation(row, col) ) {
			String key = String.format("%d,%d", row, col);
			if ( this.locationMap.containsKey(key) ) {
				this.myData.gotoRow(this.locationMap.get(key).intValue());
			}
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Box#gotoLocation(java.lang.String)
	 */
	public boolean gotoLocation(String aLoc) throws DataException {
		int[] vals = parseLocation(aLoc);
		return this.gotoLocation(vals[0], vals[1]);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Box#nextColumn()
	 */
	public boolean nextColumn() throws DataException {
		if ( super.nextColumn() ) {
			this.setCurrentLocation();
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Box#nextLocationByColumn()
	 */
	public boolean nextLocationByColumn() throws DataException {
		if ( super.nextLocationByColumn() ) {
			this.setCurrentLocation();
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Box#nextLocationByRow()
	 */
	public boolean nextLocationByRow() throws DataException {
		if ( super.nextLocationByRow() ) {
			this.setCurrentLocation();
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Box#nextRow()
	 */
	public boolean nextRow() throws DataException {
		if ( super.nextRow() ) {
			this.setCurrentLocation();
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Box#previousColumn()
	 */
	public boolean previousColumn() throws DataException {
		if ( super.previousColumn() ) {
			this.setCurrentLocation();
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Box#previousRow()
	 */
	public boolean previousRow() throws DataException {
		if ( super.previousRow() ) {
			this.setCurrentLocation();
			return true;
		}
		return false;
	}

	private void setCurrentLocation() throws DataException {
		String key = String.format("%d,%d", this.currRow, this.currCol);
		if ( this.locationMap.containsKey(key) ) {
			this.myData.gotoRow(this.locationMap.get(key).intValue());
		}
	}

	
	public String getLocation() throws DataException {
		return String.format("%s%02d", ALPHABET[this.myData.getInt(ROW_COLUMN)], this.myData.getInt(COLUMN_COLUMN));
	}

	
	public String getAssayName() throws DataException {
		return this.myAssay.getName();
	}

	
	public String getAssayTarget() throws DataException {
		return this.myAssay.getTarget();
	}

	
	public Date getDate() throws DataException {
		return this.myAssay.getDate();
	}

	public BigDecimal getActivitySD() throws DataException {
		return this.myData.getBigDecimal(VALUE_STDEV_COLUMN);
	}

	public String getConcentrationString() throws DataException {
		return autoFormatAmount(this.getConcentration(), CONCENTRATION_TYPE);
	}
}
