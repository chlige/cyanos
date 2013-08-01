/**
 * 
 */
package edu.uic.orjala.cyanos;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author George Chlipala
 *
 */
public interface Material extends DataFileObject, NotebookObject, CompoundObject, RemoteObject {
	
	public final static String LC_DATA_TYPE = "hplc";
	public final static String MS_DATA_TYPE = "ms";
	public final static String NMR_DATA_TYPE = "nmr";
	
	public final static String[] DATA_TYPES = { LC_DATA_TYPE, MS_DATA_TYPE, NMR_DATA_TYPE };
	final static String DATA_FILE_CLASS = "material";

	public interface ExtractProtocol {
		
		String getExtractSolvent();

		String getExtractType();

		String getExtractMethod();
		
		void setExtractSolvent(String newSolvent);

		void setExtractType(String newType);

		void setExtractMethod(String newType);

		Material create(String harvestID) throws DataException;
		
		Separation createInProject(String harvestID, String projectID) throws DataException;
		
		void setName(String aName);
		
		String getName();
		
		void save() throws DataException;

	}

	
	/**
	 * Returns the ID of the current Material record
	 * 
	 * @return Material ID
	 */
	String getID();
	
	/**
	 * @return
	 * @throws DataException
	 */
	SampleAccount getAccount() throws DataException;

	/**
	 * @return
	 * @throws DataException
	 */
	String getLabel() throws DataException;
	
	Strain getCulture() throws DataException;
	
	/**
	 * @return
	 * @throws DataException
	 */
	String getCultureID() throws DataException;

	/**
	 * @return
	 * @throws DataException
	 */
	String getNotes() throws DataException;
		
	/**
	 * @return
	 * @throws DataException
	 */
	String getDateString() throws DataException;
	
	/**
	 * @return
	 * @throws DataException
	 */
	Date getDate() throws DataException;
	
	/**
	 * @return
	 * @throws DataException
	 */
	String getProjectID() throws DataException;
	
	/**
	 * @return
	 * @throws DataException
	 */
	Project getProject() throws DataException;

	/**
	 * @param newLabel
	 * @throws DataException
	 */
	void setLabel(String newLabel) throws DataException;
	
	/**
	 * @param newCultureID
	 * @throws DataException
	 */
	void setCultureID(String newCultureID) throws DataException;

	/**
	 * @param newNotes
	 * @throws DataException
	 */
	void setNotes(String newNotes) throws DataException;
	
	/**
	 * @param newValue
	 * @throws DataException
	 */
	void setDate(String newValue) throws DataException;
	
	/**
	 * @param newValue
	 * @throws DataException
	 */
	void setDate(Date newValue) throws DataException;
	
	/**
	 * @param newNotes
	 * @throws DataException
	 */
	void addNotes(String newNotes) throws DataException;
	
	/**
	 * @param newValue
	 * @throws DataException
	 */
	void setProjectID(String newValue) throws DataException;
	
	/**
	 * @param aProject
	 * @throws DataException
	 */
	void setProject(Project aProject) throws DataException;

	/**
	 * @return
	 * @throws DataException
	 */
	boolean isFraction() throws DataException;
	
	/**
	 * @return
	 * @throws DataException
	 */
	Separation getParentSeparation() throws DataException;

	/**
	 * @return
	 * @throws DataException
	 */
	Separation getSeparations() throws DataException;

	/**
	 * @param harvestID
	 * @return
	 * @throws DataException
	 */
	boolean makeExtract(String harvestID) throws DataException;
	
	/**
	 * @return
	 * @throws DataException
	 */
	boolean isExtract() throws DataException;

	/**
	 * @return
	 * @throws DataException
	 */
	Harvest getHarvestForExtract() throws DataException;
	
	/**
	 * @return
	 * @throws DataException
	 */
	String getExtractSolvent() throws DataException;
	
	/**
	 * @return
	 * @throws DataException
	 */
	String getExtractType() throws DataException;

	
	/**
	 * @return
	 * @throws DataException
	 */
	String getExtractMethod() throws DataException;

	/**
	 * @return
	 * @throws DataException
	 */
	Harvest getExtractSource() throws DataException;

	/**
	 * @param newSolvent
	 * @throws DataException
	 */
	void setExtractSolvent(String newSolvent) throws DataException;
	
	/**
	 * @param newType
	 * @throws DataException
	 */
	void setExtractType(String newType) throws DataException;

	/**
	 * @param newType
	 * @throws DataException
	 */
	void setExtractMethod(String newType) throws DataException;

	/**
	 * @param harvestID
	 * @throws DataException
	 */
	void setExtractSource(String harvestID) throws DataException;

	/**
	 * @param aHarvest
	 * @throws DataException
	 */
	void setExtractSource(Harvest aHarvest) throws DataException;

	/**
	 * @return
	 * @throws DataException
	 */
	BigDecimal getAmount() throws DataException;
	
	/**
	 * Format the amount to the SI mass unit specified, e.g. kg, g, mg, or ug.
	 * 
	 * @param unit
	 * @return
	 * @throws DataException
	 */
	String formatAmount(String unit) throws DataException;
	
	/**
	 * The amount formated to the nearest SI mass unit, e.g. kg, g, mg, or ug, with unit included in the string.
	 * 
	 * @return
	 * @throws DataException
	 */
	String displayAmount() throws DataException;
	
	/**
	 * @param value
	 * @throws DataException
	 */
	void setAmount(BigDecimal value) throws DataException;
	
	/**
	 * @return
	 * @throws DataException
	 */
	Sample getSamples() throws DataException;
	
	boolean isActive() throws DataException;

	boolean isRemoved() throws DataException;
	
	/**
	 * Get the ID of the user who removed the sample.
	 * 
	 * @return user ID of the removing user.
	 * @throws DataException
	 */
	String getRemovedByID() throws DataException;

	User getRemovedBy() throws DataException;
	
	BigDecimal getAmountForSeparation(Separation separation) throws DataException;
	
	BigDecimal getAmountForSeparationID(String separationID) throws DataException;
	
	ExternalFile getChromatograms() throws DataException;
	
	void addChromatogram(ExternalFile aFile) throws DataException;
	
	ExternalFile getNMRData() throws DataException;
	
	void addNMRData(ExternalFile aFile) throws DataException;
	
	ExternalFile getMSData() throws DataException;
	
	void addMSData(ExternalFile aFile) throws DataException;
	
	String[] dataTypes();
	
	void addCompoundID(String compoundID, String separationID, String retentionTime) throws DataException;

	void addCompoundID(String compoundID, String separationID, double retentionTime) throws DataException;

	void addCompoundID(String compoundID, String retentionTime) throws DataException;

	void addCompoundID(String compoundID, double retentionTime) throws DataException;
	
	void addCompoundID(String compoundID) throws DataException;
	
	void addCompound(Compound aCompound) throws DataException;
	
	void addCompound(Compound aCompound, String retentionTime) throws DataException;

	void addCompound(Compound aCompound, double retentionTime) throws DataException;

	void addCompound(Compound aCompound, Separation separation, String retentionTime) throws DataException;

	void addCompound(Compound aCompound, Separation separation, double retentionTime) throws DataException;

	ExtractProtocol createProtocol() throws DataException;
	
	void setProtocol(ExtractProtocol aTemplate) throws DataException;
	

}
