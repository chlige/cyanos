package edu.uic.orjala.cyanos;

import java.math.BigDecimal;


/**
 * Interface to wrap assay data points into a single object.
 * 
 * @author George Chlipala
 *
 */
public interface AssayPlate extends AssayData, Box {

	
	/**
	 * Set the activity data for the currently selected location.
	 * 
	 * @param newValue Activity value, e.g. > 100 or 7.8
	 * @throws DataException
	 */
	void setActivity(String newValue) throws DataException;

	/**
	 * Set the standard deviation for the currently selected location.
	 * 
	 * @param newValue Value of the standard deviation for the data point.
	 * @throws DataException
	 */
	void setStdev(BigDecimal newValue) throws DataException;

	/**
	 * Set the standard deviation for the currently selected location.
	 * 
	 * @param newValue Value of the standard deviation for the data point.
	 * @throws DataException
	 */
	void setStdev(String newValue) throws DataException;

	/**
	 * Set the sample associated with the current data point.
	 * 
	 * @param aMaterial Material to tie to the data point.
	 * @throws DataException
	 */
	void setMaterial(Material aMaterial) throws DataException;

	/**
	 * Set the sample ID associated with the current data point.
	 * 
	 * @param aMaterialID Material ID to link to the current data point.
	 * @throws DataException
	 */
	void setMaterialID(String aMaterialID) throws DataException;

	/**
	 * Set the sample ID associated with the current data point.
	 * 
	 * @param aSampleID Sample ID to link to the current data point.
	 * @throws DataException
	 */
	void setSampleID(String aSampleID) throws DataException;

	/**
	 * Set the sample associated with the current data point.
	 * 
	 * @param aSample Sample to tie to the data point.
	 * @throws DataException
	 */
	void setSample(Sample aSample) throws DataException;

	/**
	 * Set the strain associated with the current data point.
	 * 
	 * @param aStrain Strain to link to the current data point.
	 * @throws DataException
	 */
	void setStrain(Strain aStrain) throws DataException;
	
	/**
	 * Set the strain ID associated with the current data point.
	 * 
	 * @param aStrainID Strain ID to link to the current data point.
	 * @throws DataException
	 */
	void setStrainID(String aStrainID) throws DataException;

	/**
	 * Set the final concentration of the sample in the assay.  
	 * This method will parse the strain to determine the unit scales, e.g. mg/L or ug/mL. 
	 * 
	 * @param aConcentration Final concentration of the sample.
	 * @throws DataException
	 */
	void setConcentration(String aConcentration) throws DataException;

	/**
	 * Set the final concentration of the sample in the assay.  Number in g/L.
	 * 
	 * @param aConcentration Final concentration of the sample as g/L.
	 * @throws DataException
	 */
	void setConcentration(BigDecimal aConcentration) throws DataException;

	/**
	 * Set the label of the currently selected data point.
	 * 
	 * @param aLabel Sample label for this data point.
	 * @throws DataException
	 */
	void setLabel(String aLabel) throws DataException;
	
	/**
	 * Returns true if the specified location exists.
	 * 
	 * @param row plate row (A=1,B=2,etc.).  For a 96-well plate 1-8 are valid.
	 * @param col plate column (1,2,3,etc.).  For a 96-well plate 1-12 are valid.
	 * @return true if the location exists
	 * @throws DataException
	 */
	boolean locationExists(int row, int col) throws DataException;

	/**
	 * Returns true if the specified location exists.
	 * 
	 * @param location String location, e.g. A1.
	 * @return true if the location exists
	 * @throws DataException
	 */
	boolean locationExists(String location) throws DataException;

	/**
	 * Returns true if the currently selected location exists.
	 * 
	 * @return true if the current location exists
	 * @throws DataException
	 */
	boolean currentLocationExists() throws DataException;

	/**
	 * Add a location to the assay plate
	 * 
	 * @param location String location, e.g. A1.
	 * @param strainID ID of the strain for this data point.
	 * @throws DataException
	 */
	void addLocation(String location, String strainID) throws DataException;

	/**
	 * Add a location to the assay plate
	 * 
	 * @param row plate row (A=1,B=2,etc.).  For a 96-well plate 1-8 are valid.
	 * @param col plate column (1,2,3,etc.).  For a 96-well plate 1-12 are valid.
	 * @param strainID ID of the strain for this data point.
	 * @throws DataException
	 */
	void addLocation(int row, int col, String strainID) throws DataException;

	/**
	 * Add currently selected location to the assay plate.
	 *
	 * @param strainID ID of the strain for this data point.
	 * @throws DataException
	 */
	void addCurrentLocation(String strainID) throws DataException;
}