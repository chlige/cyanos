package edu.uic.orjala.cyanos;

import java.math.BigDecimal;
import java.util.Date;

import edu.uic.orjala.cyanos.Project;
import edu.uic.orjala.cyanos.Sample;
import edu.uic.orjala.cyanos.Strain;
import edu.uic.orjala.cyanos.Separation.SeparationTemplate;

/**
 * Interface for Assay objects
 * 
 * @author George Chlipala
 * 
 */
public interface Assay extends DataFileObject, NotebookObject, RemoteObject {
	
	/**
	 * Interface for an Assay Protocol (Template)
	 * 
	 * @author George Chlipala
	 *
	 */
	public interface AssayTemplate {
		
		String getTarget();

		String getActiveOperator();		
		
		BigDecimal getActiveLevel();
		
		String getUnitFormat();
		
		int getLength();
		
		int getWidth();
		
		String getSize();
		
		void setTarget(String value);

		void setActiveOperator(String value);		
		
		void setActiveLevel(BigDecimal value);
		
		void setUnitFormat(String value);
		
		void setLength(int value);
		
		void setWidth(int value);
		
		void setSize(String value);
		
		Assay create(String newID) throws DataException;
		
		Assay createInProject(String newID, String projectID) throws DataException;
		
		void setName(String aName);
		
		String getName();
		
		void setSigFigs(int value);
		
		int getSigFigs();
		
		void save() throws DataException;

	}

	final static String RAW_DATA_TYPE = "raw";

	final static String REPORT_DATA_TYPE = "report";

	final static String DATA_FILE_CLASS = "assay";

	final static String[] DATA_TYPES = { RAW_DATA_TYPE, REPORT_DATA_TYPE };

	static final String OPERATOR_GREATER_THAN = "gt";

	static final String OPERATOR_GREATER_EQUAL = "ge";

	static final String OPERATOR_LESS_THAN = "lt";

	static final String OPERATOR_LESS_EQUAL = "le";

	static final String OPERATOR_EQUAL = "eq";

	static final String OPERATOR_NOT_EQUAL = "ne";

	/**
	 * Get the length of the plate, X dimension, of the assay.
	 * 
	 * @return length of the plate as an int.
	 * @throws DataException
	 * 
	 * @see #setLength(int)
	 * @see #getWidth()
	 */
	int getLength() throws DataException;

	/**
	 * Get the width of the plate, Y dimension, of the assay.
	 * 
	 * @return width of the plate as an int.
	 * @throws DataException
	 * 
	 * @see #setWidth(int)
	 * @see #getLength()
	 */
	int getWidth() throws DataException;

	/**
	 * Get the name, i.e. descriptor, of the assay.
	 * 
	 * @return name of the assay
	 * @throws DataException
	 * 
	 * @see #setName(String)
	 */
	String getName() throws DataException;

	/**
	 * Get the target of the assay, e.g. 20S Proteasome or MCF7
	 * 
	 * @return the assay target
	 * @throws DataException
	 * 
	 * @see #setTarget(String)
	 */
	String getTarget() throws DataException;

	/**
	 * Get the activity operator for this assay, e.g. eq, ne, or lt. It should
	 * be equal to one of the following: {@link #OPERATOR_EQUAL}
	 * {@link #OPERATOR_GREATER_EQUAL} {@link #OPERATOR_GREATER_THAN}
	 * {@link #OPERATOR_LESS_EQUAL} {@link #OPERATOR_LESS_THAN}
	 * {@link #OPERATOR_NOT_EQUAL}
	 * 
	 * @return the activity operator as a {@link String}.
	 * @throws DataException
	 * 
	 * @see #setActiveOperator(String)
	 * @see #getActiveLevel()
	 */
	String getActiveOperator() throws DataException;

	/**
	 * Get the activity level for this assay.
	 * 
	 * @return the activity level as a BigDecimal.
	 * @throws DataException
	 * 
	 * @see #setActiveLevel(BigDecimal)
	 * @see #setActiveLevel(String)
	 * @see #getActiveOperator()
	 */
	BigDecimal getActiveLevel() throws DataException;

	/**
	 * Get notes for this assay.
	 * 
	 * @return assay notes
	 * @throws DataException
	 * 
	 * @see #setNotes(String)
	 * @see #addNotes(String)
	 */
	String getNotes() throws DataException;

	/**
	 * Get the assay date.
	 * 
	 * @return assay date, as a {@link String}
	 * @throws DataException
	 * 
	 * @see #getDate()
	 * @see #setDate(Date)
	 * @see #setDate(String)
	 */
	String getDateString() throws DataException;

	/**
	 * Get the assay date.
	 * 
	 * @return assay date, as a {@link java.util.Date} object
	 * @throws DataException
	 * 
	 * @see #getDateString()
	 * @see #setDate(Date)
	 * @see #setDate(String)
	 */
	Date getDate() throws DataException;

	/**
	 * Get the unit for this assay, e.g. ug/ml or %
	 * 
	 * @return the data format string.
	 * @throws DataException
	 * @see #setUnit(String)
	 */
	String getUnit() throws DataException;

	/**
	 * Set the unit for the assay. 
	 * 
	 * @param newUnit a new unit string.
	 * @throws DataException
	 * @see #getUnit()
	 */
	void setUnit(String newUnit) throws DataException;

	/**
	 * Get the project ID for this assay.
	 * 
	 * @return the project ID
	 * @throws DataException
	 * @see #getProject()
	 * @see #setProjectID(String)
	 * @see #setProject(Project)
	 */
	String getProjectID() throws DataException;

	/**
	 * Get the project for this assay.
	 * 
	 * @return the project
	 * @throws DataException
	 * @see #getProjectID()
	 * @see #setProjectID(String)
	 * @see #setProject(Project)
	 */
	Project getProject() throws DataException;

	/**
	 * Set the project ID for this assay.
	 * 
	 * @param newValue
	 *            the project ID
	 * @throws DataException
	 * @see #setProject(Project)
	 * @see #getProjectID()
	 * @see #getProject()
	 */
	void setProjectID(String newValue) throws DataException;

	/**
	 * Set the project for this assay.
	 * 
	 * @param aProject
	 *            the project
	 * @throws DataException
	 * @see #setProjectID(String)
	 * @see #getProjectID()
	 * @see #getProject()
	 */
	void setProject(Project aProject) throws DataException;

	/**
	 * Set the name of the assay.
	 * 
	 * @param newName
	 *            the name of the assay.
	 * @throws DataException
	 * @see #getName()
	 */
	void setName(String newName) throws DataException;

	/**
	 * Set the assay notes.
	 * 
	 * @param newNotes
	 *            the assay notes
	 * @throws DataException
	 * @see #getNotes()
	 */
	void setNotes(String newNotes) throws DataException;

	/**
	 * Set the date the assay was performed.
	 * 
	 * @param newValue
	 *            the assay date as a {@link String}.
	 * @throws DataException
	 * @see #setDate(Date)
	 * @see #getDate()
	 */
	void setDate(String newValue) throws DataException;

	/**
	 * Set the date the assay was performed.
	 * 
	 * @param newValue
	 *            the assay date as a {@link java.util.Date} object
	 * @throws DataException
	 * @see #setDate(String)
	 * @see #getDate()
	 */
	void setDate(Date newValue) throws DataException;

	/**
	 * Set the assay target, e.g. 20S Proteasome or MCF7
	 * 
	 * @param newValue
	 *            the assay target
	 * @throws DataException
	 * @see #getTarget()
	 */
	void setTarget(String newValue) throws DataException;

	/**
	 * Set the length, i.e. number of wells in the X dimension, of the assay
	 * plate.
	 * 
	 * @param newValue
	 *            the length of the plate
	 * @throws DataException
	 * @see #getLength()
	 */
	void setLength(int newValue) throws DataException;

	/**
	 * Set the width, i.e. number of wells in the Y dimension, of the assay
	 * plate.
	 * 
	 * @param newValue
	 *            the width of the plate
	 * @throws DataException
	 * @see #getWidth()
	 * @see #setLength(int)
	 */
	void setWidth(int newValue) throws DataException;

	/**
	 * Set the activity threshold for this assay.
	 * 
	 * @param newValue
	 *            the activity threshold
	 * @throws DataException
	 * @see #getActiveLevel()
	 * @see #setActiveOperator(String)
	 */
	void setActiveLevel(BigDecimal newValue) throws DataException;

	/**
	 * Set the activity threshold for this assay.
	 * 
	 * @param newValue
	 *            the activity threshold
	 * @throws DataException
	 * @see #setActiveLevel(BigDecimal)
	 * @see #getActiveLevel()
	 * @see #setActiveOperator(String)
	 */
	void setActiveLevel(String newValue) throws DataException;

	/**
	 * Set the activity operator for this assay. It should be one of the
	 * following: {@link #OPERATOR_EQUAL}, {@link #OPERATOR_GREATER_EQUAL},
	 * {@link #OPERATOR_GREATER_THAN}, {@link #OPERATOR_LESS_EQUAL},
	 * {@link #OPERATOR_LESS_THAN}, {@link #OPERATOR_NOT_EQUAL}
	 * 
	 * @param newValue
	 *            the activity operator. Utilize OPERATOR_* constants.
	 * @throws DataException
	 * @see #getActiveOperator()
	 * @see #setActiveLevel(BigDecimal)
	 */
	void setActiveOperator(String newValue) throws DataException;

	/**
	 * Add notes to the current assay notes.
	 * 
	 * @param newNotes
	 *            assay notes to add.
	 * @throws DataException
	 * @see #getNotes()
	 * @see #setNotes(String)
	 */
	void addNotes(String newNotes) throws DataException;

	/**
	 * Get the data for this assay.
	 * 
	 * @return assay data
	 * @throws DataException
	 * @see AssayData
	 */
	AssayPlate getAssayData() throws DataException;

	/**
	 * Get the data from this assay for the {@link Strain} specified
	 * 
	 * @param aStrain
	 * @return assay data
	 * @throws DataException
	 * @see AssayData
	 */
	AssayData getAssayDataForStrain(Strain aStrain) throws DataException;

	/**
	 * Get the data from this assay for the strain ID specified
	 * 
	 * @param aStrainID
	 * @return assay data
	 * @throws DataException
	 * @see AssayData
	 */
	AssayData getAssayDataForStrainID(String aStrainID) throws DataException;

	/**
	 * Get the data from this assay for the {@link Sample} specified
	 * 
	 * @param aSample
	 * @return assay data
	 * @throws DataException
	 * @see AssayData
	 */
	AssayData getAssayDataForSample(Sample aSample) throws DataException;

	/**
	 * Get the data from this assay for the sample ID specified
	 * 
	 * @param aSampleID
	 * @return assay data
	 * @throws DataException
	 * @see AssayData
	 */
	AssayData getAssayDataForSampleID(String aSampleID) throws DataException;

	/**
	 * Get the data from this assay that is considered active. Refer to
	 * {@link #getActiveLevel()} and {@link #getActiveOperator()} "active"
	 * threshold information.
	 * 
	 * @return assay data
	 * @throws DataException
	 * @see AssayData
	 */
	AssayData getActiveData() throws DataException;
	
	AssayData getDataForObject(BasicObject object) throws DataException;
	
	void clearData() throws DataException;
	
	AssayTemplate createTemplate() throws DataException;
	
	void setTemplate(AssayTemplate aTemplate) throws DataException;
	
	void setSigFigs(int value) throws DataException;
	
	void setSigFigs(String value) throws DataException;
	
	int getSigFigs() throws DataException;

}