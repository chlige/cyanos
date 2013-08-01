/**
 * 
 */
package edu.uic.orjala.cyanos;

import java.util.Map;

/**
 * This interface extends the Compound interface by adding methods for thre retrival and manipulation of indexed UV data.
 * 
 * @author George Chlipala
 *
 */
@Deprecated
public interface Compound_UV extends Compound {

	/**
	 * Retrieves UV spectrum of the compound.  Should be JCAMP-DX format.
	 * 
	 * @return UV spectrum as JCAMP-DX String
	 * @throws SQLException
	 */
	String getUVData() throws DataException;
	
	/**
	 * Sets UV spectrum of the compound.  Should be JCAMP-DX format.
	 * 
	 * @param newData UV spectrum as JCAMP-DX String
	 * @throws SQLException
	 */
	void setUVData(String newData) throws DataException;
	
	Map<Float, Float> getUVPeaks() throws DataException;
	
	void addUVPeak(float wavelength, float relIntensity) throws DataException;
	
	void deleteUVPeak(float wavelength) throws DataException;
	
	boolean hasUVData() throws DataException;
}
