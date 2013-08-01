/**
 * 
 */
package edu.uic.orjala.cyanos;

/**
 * This is a EXPERIMENTAL interface for storing assays using raw data.
 * 
 * @author George Chlipala
 *
 */
public interface Assay_RawData extends Assay {

	float getZPrime() throws DataException;

	float getControlMean() throws DataException;

	float getBlankMean() throws DataException;

	float getControlSTDEV() throws DataException;

	float getBlankSTDEV() throws DataException;

	float getSampleMean(String sampleID) throws DataException;

	float getCultureIDMean(String cultureID) throws DataException;

	float getSampleSTDEV(String sampleID) throws DataException;

}
