package edu.uic.orjala.cyanos;

import java.math.BigDecimal;
import java.util.Date;

public interface Separation extends DataFileObject, NotebookObject, CompoundObject, RemoteObject {
	
	/**
	 * Interface for a Separation Protocol (Template)
	 * 
	 * @author George Chlipala
	 *
	 */
	public interface SeparationTemplate {
		
		String getMobilePhase();

		String getMethod();
		
		String getStationaryPhase();
		
		void setMethod(String newValue);
		
		void setMobilePhase(String newValue);
		
		void setStationaryPhase(String newValue);
		
		Separation create() throws DataException;
		
		Separation createInProject(String projectID) throws DataException;
		
		void setName(String aName);
		
		String getName();
		
		void save() throws DataException;

	}

	final static String LC_DATA_TYPE = "hplc";
	
	final static String[] DATA_TYPES = { LC_DATA_TYPE };
	final static String DATA_FILE_CLASS = "separation";
	
//	String getName() throws DataException;

	Material getSources() throws DataException;
	
	Material getFractions() throws DataException;
	
	boolean addSource(Material source, BigDecimal amount) throws DataException;
	
	boolean removeSource(Material aSample) throws DataException;
	
	Material makeFraction() throws DataException;
	
	Material makeFraction(int frNumber) throws DataException;
	
	Material makeFraction(String label) throws DataException;
	
	Material makeFraction(int frNumber, String label) throws DataException;
	
	Material getCurrentFraction() throws DataException;
	
	int getCurrentFractionNumber() throws DataException;
	
	boolean gotoFraction(int frNumber) throws DataException;
	
	boolean firstFraction() throws DataException;
	
	boolean lastFraction() throws DataException;
	
	boolean nextFraction() throws DataException;
	
	boolean previousFraction() throws DataException;
	
	void beforeFirstFraction() throws DataException;
	
	void afterLastFraction() throws DataException;
	
	String getMobilePhase() throws DataException;

	String getMethod() throws DataException;
	
	String getStationaryPhase() throws DataException;
	
	void setMethod(String newValue) throws DataException;
	
	void setMobilePhase(String newValue) throws DataException;
	
	void setStationaryPhase(String newValue) throws DataException;
	
	Date getDate() throws DataException;
	
	String getDateString() throws DataException;
	
	void setDate(Date newValue) throws DataException;
	
	void setDate(String newValue) throws DataException;
	
	String getNotes() throws DataException;
	
//	void setName(String newValue) throws DataException;
	
	String getTag() throws DataException;
	
	void setTag(String newValue) throws DataException;
	
	void setNotes(String newValue) throws DataException;
	
	void appendNotes(String newNotes) throws DataException;
	
	SeparationTemplate createTemplate() throws DataException;
	
	void setTemplate(SeparationTemplate aTemplate) throws DataException;
	
	ExternalFile getChromatograms() throws DataException;
	
	void addChromatogram(ExternalFile aFile) throws DataException;

	void setProjectID(String projectID) throws DataException;
	
	void setProject(Project aProject) throws DataException;
	
	String getProjectID() throws DataException;
	
	Project getProject() throws DataException;
	
	/**
	 * Get the date the separation record was removed.
	 * 
	 * @return removed date as a {@link java.util.Date}
	 * @throws DataException
	 */
	Date getRemovedDate() throws DataException;
	
	/**
	 * Get the date the separation record was removed.
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

	void addCompoundID(String compoundID, String sampleID, String retentionTime) throws DataException;

	void addCompound(Compound aCompound, String sampleID, String retentionTime) throws DataException;

	void addCompoundID(String newValue, String sampleID, double retentionTime) throws DataException;

	void addCompound(Compound aCompound, String sampleID, double retentionTime)	throws DataException;
}
