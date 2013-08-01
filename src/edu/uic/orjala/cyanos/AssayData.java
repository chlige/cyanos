package edu.uic.orjala.cyanos;

import java.math.BigDecimal;
import java.util.Date;



/**
 * Interface to wrap assay data points into a single object.
 * 
 * @author George Chlipala
 *
 */
public interface AssayData extends BasicObject {

	/**
	 * Get the ID of the parent assay.
	 * 
	 * @return ID of the parent assay.
	 * @throws DataException 
	 */
	String getAssayID() throws DataException;
	
	/**
	 * Get the name, i.e. descriptor, of the assay.
	 * 
	 * @return name of the assay
	 * @throws DataException
	 */
	String getAssayName() throws DataException;

	/**
	 * Get the target of the assay, e.g. 20S Proteasome or MCF7
	 * 
	 * @return the assay target
	 * @throws DataException
	 */
	String getAssayTarget() throws DataException;

	/**
	 * Get the assay date.
	 * 
	 * @return assay date, as a {@link java.util.Date} object
	 * @throws DataException
	 */
	Date getDate() throws DataException;

	/**
	 * Get the label of the currently selected assay data point.
	 * 
	 * @return Label of the currently selected assay data point.
	 * @throws DataException
	 */
	String getLabel() throws DataException;

	/**
	 * Get the sample associated with the current data point.
	 * 
	 * @return Sample associated with the current data point.  Returns null if no sample is associated.
	 * @throws DataException
	 */
	Sample getSample() throws DataException;
	/**
	 * Get the sample ID associated with the current data point.
	 * 
	 * @return Sample ID associated with the data point.  Returns null if no sample is linked.
	 * @throws DataException
	 */
	String getSampleID() throws DataException;

	/**
	 * Get the sample associated with the current data point.
	 * 
	 * @return Material associated with the current data point.  Returns null if no sample is associated.
	 * @throws DataException
	 */
	Material getMaterial() throws DataException;

	/**
	 * Get the sample ID associated with the current data point.
	 * 
	 * @return Material ID associated with the data point.  Returns null if no sample is linked.
	 * @throws DataException
	 */
	String getMaterialID() throws DataException;

	/**
	 * Get the strain ID associated with the current data point.
	 * 
	 * @return Strain ID linked to the current data point.
	 * @throws DataException
	 */
	String getStrainID() throws DataException;

	/**
	 * Get the final concentration of the sample in the assay
	 * 
	 * @return Concentration of the sample evaluated.
	 * @throws DataException
	 */
	BigDecimal getConcentration() throws DataException;

	/**
	 * Get the activity data as a string. e.g. 100 % or 5.6 ug/mL.
	 * 
	 * @return Activity data as a string, including units.
	 * @throws DataException
	 */
	String getActivityString() throws DataException;

	/**
	 * Get the activity data as a decimal, e.g. 100 or 5.6
	 * 
	 * @return Activity data as a BigDecimal.
	 * @throws DataException
	 */
	BigDecimal getActivity() throws DataException;
	
	/**
	 * Get the activity value standard deviation as a decimal, e.g. 100 or 5.6
	 * 
	 * @return standard deviation data as a BigDecimal.
	 * @throws DataException
	 */
	BigDecimal getActivitySD() throws DataException;

	/**
	 * Determine if the current sample is considered active, i.e. satisfies the activity thresholds of the parent assay.
	 * 
	 * @return true if the current sample is active.
	 * @throws DataException
	 */
	boolean isActive() throws DataException;
	
	String getLocation() throws DataException;
	
	String getConcentrationString() throws DataException;
}