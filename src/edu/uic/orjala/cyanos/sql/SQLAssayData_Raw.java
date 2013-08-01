/**
 * 
 */
package edu.uic.orjala.cyanos.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import edu.uic.orjala.cyanos.DataException;

/**
 * @author gchlip2
 *
 */
public class SQLAssayData_Raw extends SQLAssayPlate {


	protected static final String SQL_LOAD_BASE = "SELECT assay.*, ai.unit, active(assay.activity + assay.sign,ai.active_level,ai.active_op) AS is_active, AVG(assay.active) AS mean_activity, STDDEV(assay.active) AS sd_activity FROM assay JOIN assay_info ai ON (assay.assay_id = ai.assay_id) GROUP BY assay.assay_id,assay.material_id,assay.concentration";
	
	private static final String SQL_LOAD_ALL_DATA = SQL_LOAD_BASE.concat("  WHERE assay.assay_id=?");
	private static final String SQL_LOAD_FOR_STRAIN = SQL_LOAD_BASE.concat(" WHERE culture_id=?");
	private static final String SQL_LOAD_FOR_SAMPLE = SQL_LOAD_BASE.concat(" WHERE sample_id=?");
	private static final String SQL_LOAD_FOR_MATERIAL = SQL_LOAD_BASE.concat(" WHERE material_id=?");
	private static final String SQL_LOAD_ACTIVES = SQL_LOAD_BASE.concat(" WHERE assay.assay_id=? AND active(assay.activity + assay.sign,ai.active_level,ai.active_op) = 1");

	// TODO need to setup SQL for a raw data assay.
	private static final String SAMPLE_STDEV_SQL = "SELECT 0";

	protected SQLAssayData_Raw(SQLData data) throws DataException {
		super(data);
	}
	
	protected SQLAssayData_Raw(SQLAssay anAssay) throws DataException {
		super(anAssay);
	}

	// THIS IS FOR A FUTURE IDEA.
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.sql.Assay#getZPrime()
	 */
	public float getZPrime() throws DataException {
		float zPrime = 0;
		try {
			PreparedStatement aSth = this.myData.prepareStatement("SELECT type,AVG(activity),STDEV(activity) " +
			"FROM assay WHERE assay_id=? AND (type='blank' || type='control') GROUP BY type");
			aSth.setString(1,this.myID);
			ResultSet aResult = aSth.executeQuery();
			if ( aResult.first() ) {
				float barC = 0, barB = 0, sigmaC = 0, sigmaB = 0;
				aResult.beforeFirst();
				while ( aResult.next() ) {
					if ( aResult.getString(1).equals("control") ) {
						barC = aResult.getFloat(2);
						sigmaC = aResult.getFloat(3);
					} else {
						barB = aResult.getFloat(2);
						sigmaB = aResult.getFloat(3);
					}
				}
				float meanDiff = ( barC > barB ? barC - barB : barB - barC );
				float sigmaSum = 3 * (sigmaC + sigmaB);
				if ( meanDiff > 0 )
					zPrime = 1 - ( sigmaSum / meanDiff );
			}
			return zPrime;
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.sql.Assay#getControlMean()
	 */
	public float getControlMean() throws DataException {
		try {
			PreparedStatement aSth = this.myData.prepareStatement("SELECT AVG(activity) " +
			"FROM assay WHERE assay_id=? AND type='control' GROUP BY type");
			aSth.setString(1,this.myID);
			ResultSet aResult = aSth.executeQuery();
			float retval = 0.0f;
			if ( aResult.first() ) retval = aResult.getFloat(1);
			aResult.close();
			aSth.close();
			return retval;
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.sql.Assay#getBlankMean()
	 */
	public float getBlankMean() throws DataException {
		try {
			PreparedStatement aSth = this.myData.prepareStatement("SELECT AVG(activity) " +
			"FROM assay WHERE assay_id=? AND type='blank' GROUP BY type");
			aSth.setString(1,this.myID);
			ResultSet aResult = aSth.executeQuery();
			float retval = 0.0f;
			if ( aResult.first() ) retval = aResult.getFloat(1);
			aResult.close();
			aSth.close();
			return retval;
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.sql.Assay#getControlSTDEV()
	 */
	public float getControlSTDEV() throws DataException {
		try {
			PreparedStatement aSth = this.myData.prepareStatement("SELECT STDEV(activity) " +
				"FROM assay WHERE assay_id=? AND type='control' GROUP BY type");
			aSth.setString(1,this.myID);
			ResultSet aResult = aSth.executeQuery();
			float retval = 0.0f;
			if ( aResult.first() ) retval = aResult.getFloat(1);
			aResult.close();
			aSth.close();
			return retval;
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.sql.Assay#getBlankSTDEV()
	 */
	public float getBlankSTDEV() throws DataException {
		try {
			PreparedStatement aSth = this.myData.prepareStatement("SELECT STDEV(activity) " +
				"FROM assay WHERE assay_id=? AND type='blank' GROUP BY type");
			aSth.setString(1,this.myID);
			ResultSet aResult = aSth.executeQuery();
			float retval = 0.0f;
			if ( aResult.first() ) retval = aResult.getFloat(1);
			aResult.close();
			aSth.close();
			return retval;
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.sql.Assay#getSampleMean(java.lang.String)
	 */
	public float getSampleMean(String sampleID) throws DataException {
		try {
			PreparedStatement aSth = this.myData.prepareStatement("SELECT AVG(activity) " +
				"FROM assay WHERE assay_id=? AND sample_id=? GROUP BY sample_id,concentration");
			aSth.setString(1,this.myID);
			aSth.setString(2, sampleID);
			ResultSet aResult = aSth.executeQuery();
			float retval = 0.0f;
			if ( aResult.first() ) retval = aResult.getFloat(1);
			aResult.close();
			aSth.close();
			return retval;
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.sql.Assay#getCultureIDMean(java.lang.String)

	public float getCultureIDMean(String cultureID) throws DataException {
		if ( this.loadSubSetForCultureID(cultureID) ) 
			return this.getFloat("AVG(activity)");
		return 0;
	}
	
	private float getActivityForValue(float testValue) throws DataException {
		if ( this.isRawdata ) {
			float controlValue = this.getControlMean();
			float blankValue = this.getBlankMean();
			if ( (controlValue - blankValue) != 0 ) 
				return (testValue - blankValue) / (controlValue - blankValue);
		}
		return testValue;
	}
	 */
}
