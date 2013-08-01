package edu.uic.orjala.cyanos;

import java.math.BigDecimal;
import java.util.Date;

public interface Sample extends DataFileObject, CompoundObject {
	
	public final static String LC_DATA_TYPE = "hplc";
	public final static String MS_DATA_TYPE = "ms";
	public final static String NMR_DATA_TYPE = "nmr";
	
	public final static String[] DATA_TYPES = { LC_DATA_TYPE, MS_DATA_TYPE, NMR_DATA_TYPE };
	final static String DATA_FILE_CLASS = "sample";
	
	SampleAccount getAccount() throws DataException;
	
	String getBaseUnit() throws DataException;
	
	String getCollectionID() throws DataException;
	
	SampleCollection getCollection() throws DataException;
	
	String getLocation() throws DataException;
	
	int getLocationRow() throws DataException;
	
	int getLocationCol() throws DataException;
	
	String getName() throws DataException;
	
	String getCultureID() throws DataException;

	String getNotes() throws DataException;
	
	String getVialWeight() throws DataException;
	
	String getDateString() throws DataException;
	
	Date getDate() throws DataException;
	
	String getProjectID() throws DataException;
	
	Project getProject() throws DataException;
	
	BigDecimal getConcentration() throws DataException;
	
	Material getParentMaterial() throws DataException;
	
	String getParentMaterialID() throws DataException;
	
	void setConcentration(BigDecimal newValue) throws DataException;
	
	void setConcentration(String newValue) throws DataException;
	
	void setBaseUnit(String newUnit) throws DataException;
	
	void setCollectionID(String newColID) throws DataException;
	
	void setLocation(String newRow, String newCol) throws DataException;
	
	void setLocation(int newRow, int newCol) throws DataException;
	
	void setLocation(String newLocation) throws DataException;
	
	void setName(String newName) throws DataException;
	
	void setCultureID(String newCultureID) throws DataException;

	void setNotes(String newNotes) throws DataException;
	
	void setDate(String newValue) throws DataException;
	
	void setDate(Date newValue) throws DataException;
	
	void addNotes(String newNotes) throws DataException;
	
	void setVialWeight(String newValue) throws DataException;
	
	void setProjectID(String newValue) throws DataException;
	
	void setProject(Project aProject) throws DataException;
	
	BigDecimal accountBalance() throws DataException;
	
	boolean isFraction() throws DataException;
	
	Separation getParentSeparation() throws DataException;

	Separation getSeparations() throws DataException;

//	boolean makeExtract(String harvestID) throws DataException;
	
	boolean isExtract() throws DataException;

	Harvest getHarvestForExtract() throws DataException;
	
	String getExtractSolvent() throws DataException;
	
	String getExtractType() throws DataException;

	Harvest getExtractSource() throws DataException;

//	void setExtractSolvent(String newSolvent) throws DataException;
	
//	void setExtractType(String newType) throws DataException;

//	void setExtractSource(String harvestID) throws DataException;
	
//	void setExtractSource(Harvest aHarvest) throws DataException;
	
	BigDecimal getAmountForHarvest(String harvestID) throws DataException;
	
	BigDecimal getAmountForHarvest(Harvest aHarvest) throws DataException;

	BigDecimal getAmountForSeparation(String anID) throws DataException;
	
	BigDecimal getAmountForSeparation(Separation aSep) throws DataException;
	
	BigDecimal getAmountForAssay(Assay assay) throws DataException;
	
	BigDecimal getAmountForAssay(String assayID) throws DataException;
	
	boolean isNeat() throws DataException;
	
	boolean isSolution() throws DataException;

	boolean isLibrarySample() throws DataException;
	
	String getLibrarySourceID() throws DataException;
	
	Sample getLibrarySource() throws DataException;
	
	Sample getLibraryChildren() throws DataException;
	
	void setLibrarySourceID(String newValue) throws DataException;
	
	void setLibrarySource(Sample aSource) throws DataException;

	boolean isSelfLibrarySource() throws DataException;
	
	void setSelfLibrarySource() throws DataException;
	
	ExternalFile getChromatograms() throws DataException;
	
	void addChromatogram(ExternalFile aFile) throws DataException;
	
	ExternalFile getNMRData() throws DataException;
	
	void addNMRData(ExternalFile aFile) throws DataException;
	
	ExternalFile getMSData() throws DataException;
	
	void addMSData(ExternalFile aFile) throws DataException;
	
	String[] dataTypes();

	Assay getAssays() throws DataException ;
	
	String getSeparationID(String compoundID) throws DataException;

	String getSeparationID(Compound aCompound) throws DataException;

	Separation getSeparation(String compoundID) throws DataException;

	Separation getSeparation(Compound aCompound) throws DataException;

	/**
	 * Get the date the sample was removed.
	 * 
	 * @return removed date as a {@link java.util.Date}
	 * @throws DataException
	 */
	Date getRemovedDate() throws DataException;
	
	/**
	 * Get the date the sample was removed.
	 * 
	 * @return removed date as a {@link java.lang.String}.
	 * @throws DataException
	 */
	String getRemovedDateString() throws DataException;

	void remove() throws DataException;
	
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
	
	void addCompoundID(String newValue, String retentionTime) throws DataException;

	void addCompoundID(String newValue, double retentionTime) throws DataException;
	
	void addCompoundID(String newValue) throws DataException;
	
	void addCompound(Compound aCompound) throws DataException;
	
	void addCompound(Compound aCompound, String retentionTime) throws DataException;

	void addCompound(Compound aCompound, double retentionTime) throws DataException;

}
