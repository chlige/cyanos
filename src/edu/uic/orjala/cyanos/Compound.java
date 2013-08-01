/**
 * 
 */
package edu.uic.orjala.cyanos;

import java.io.InputStream;
import java.math.BigDecimal;

/**
 * Interface for compound records.
 * 
 * @author George Chlipala
 *
 */
public interface Compound extends DataFileObject, RemoteObject {

	final static String MS_DATA_TYPE = "ms";
	final static String NMR_DATA_TYPE = "nmr";
	final static String IR_DATA_TYPE = "ir";
	final static String UV_DATA_TYPE = "uv";
	final static String CD_DATA_TYPE = "cd";
	
	final static String DATA_FILE_CLASS = "compound";
	
	/**
	 * Returns the ID of the current Compound record.
	 * 
	 * @return Compound ID
	 */
	String getID();
	
	/**
	 * Get the name of the current compound record.
	 * 
	 * @return Name of the compound.
	 * @throws DataException
	 */
	String getName() throws DataException;
	
	/**
	 * Set the name of the current compound.
	 * 
	 * @param newValue New name of the compound.
	 * @throws DataException
	 */
	void setName(String newValue) throws DataException;
	
	/**
	 * Get the formula of the compound as a plaintext string.  For example, C6H12O6
	 * 
	 * @return Formula of the compound.
	 * @throws DataException
	 */
	String getFormula() throws DataException;
	
	/**
	 * Get the formula of the compound formated to display in an HTML page.  For example, C<SUB>6</SUB>H<SUB>12</SUB>O<SUB>6</SUB>
	 * 
	 * @return Formula of the compound.
	 * @throws DataException
	 */
	String getHTMLFormula() throws DataException;
	
	/**
	 * Set the formula of the compound.  Should be a plaintext string, e.g. C6H12O6
	 * 
	 * @param newValue Formula of the compound.
	 * @throws DataException
	 */
	void setFormula(String newValue) throws DataException;
	
	/**
	 * Get the SMILES string of the compound.
	 * 
	 * @return SMILES string of the compound.
	 * @throws DataException
	 */
	String getSmilesString() throws DataException;
	
	/**
	 * Set the SMILES string of the compound.
	 * 
	 * @param newValue New SMILES string of the compound.
	 * @throws DataException
	 */
	void setSmilesString(String newValue) throws DataException;

	/**
	 * Get the InChi string of the compound.
	 * 
	 * @return InChi string of the compound.
	 * @throws DataException
	 */
	String getInChiString() throws DataException;
	
	/**
	 * Set the InChi string of the compound.
	 * 
	 * @param newValue New InChi string of the compound.
	 * @throws DataException
	 */
	void setInChiString(String newValue) throws DataException;
	
	/**
	 * Get the InChi key of the compound.
	 * 
	 * @return InChi key of the compound.
	 * @throws DataException
	 */
	String getInChiKey() throws DataException;
	
	/**
	 * Set the InChi key of the compound.
	 * 
	 * @param newValue New InChi key of the compound.
	 * @throws DataException
	 */
	void setInChiKey(String newValue) throws DataException;
	
	/**
	 * Get the average mass or formula weight of the compound.
	 * 
	 * @return Formula weight of the compound.
	 * @throws DataException
	 */
	BigDecimal getAverageMass() throws DataException;
	
	/**
	 * Set the average mass or formula weight of the compound.
	 * 
	 * @param newValue New formula weight of the compound
	 * @throws DataException
	 */
	void setAverageMass(String newValue) throws DataException;
	
	/**
	 * Set the average mass or formula weight of the compound.
	 * 
	 * @param newValue New formula weight of the compound.
	 * @throws DataException
	 */
	void setAverageMass(BigDecimal newValue) throws DataException;
	
	/**
	 * Get the monoisotopic mass of the compound.
	 * 
	 * @return Monoisotopic mass of the compound.
	 * @throws DataException
	 */
	BigDecimal getMonoisotopicMass() throws DataException;
	
	/**
	 * Set the monoisotopic mass of the compound.
	 * 
	 * @param newValue New monoisotopic mass of the compound.
	 * @throws DataException
	 */
	void setMonoisotopicMass(BigDecimal newValue) throws DataException;
	
	/**
	 * Set the monoisotopic mass of the compound.
	 * 
	 * @param newValue New monoisotopic mass of the compound.
	 * @throws DataException
	 */
	void setMonoisotopicMass(String newValue) throws DataException;
	
	/**
	 * Retrieve the notes/description of the compound
	 * 
	 * @return Compound description/notes
	 * @throws DataException
	 */
	String getNotes() throws DataException;

	/**
	 * Set compound description/notes.  This method will overwrite the current description/notes.
	 * 
	 * @param newNotes New decription/notes.
	 * @throws DataException
	 */
	void setNotes(String newNotes) throws DataException;

	/**
	 * Append to the compound description/notes.  This method with append the text to the end of the current description/notes.
	 * 
	 * @param newNotes Text to add to the description/notes.
	 * @throws DataException
	 */
	void addNotes(String newNotes) throws DataException;
	
	/**
	 * Get the structure of the compound in MDL mol format.
	 * 
	 * @return Structure of the compound
	 * @throws DataException
	 */
	String getMDLData() throws DataException;
	
	/**
	 * Set the structure of the compound using the MDL mol data from a {@link InputStream}
	 * 
	 * @return {@link InputStream} for the MDL mol file or other IO stream
	 * @throws DataException
	 */
	InputStream getMDLDataStream() throws DataException;
	
	/**
	 * Set the structure of the compound using the MDL mol data contained in the String.
	 * 
	 * @param newValue New structure in MDL mol format.
	 * @throws DataException
	 */
	void setMDLData(String newValue) throws DataException;
	
	/**
	 * Determines if the compound record contains structure data in MDL mol format.
	 * 
	 * @return true if the compound record contains MDL mol data.
	 * @throws DataException
	 */
	boolean hasMDLData() throws DataException;
	
	/**
	 * Clear the structure data for this compound record.
	 * 
	 * @throws DataException
	 */
	void clearMDLData() throws DataException;
		
	@Deprecated
	byte[] getThumbnail() throws DataException;

	@Deprecated
	InputStream getThumbnailStream() throws DataException;
	
	@Deprecated
	boolean hasThumbnail() throws DataException;
	
	@Deprecated
	void clearThumbnail() throws DataException;

	@Deprecated
	void setTumbnail(byte[] data) throws DataException;
	
	/**
	 * Link the compound record to the specified NMR data file.
	 * 
	 * @param aFile NMR data file to link.
	 * @throws DataException
	 */
	void addNMRDatafile(ExternalFile aFile) throws DataException;
	
	/**
	 * Link the compound record to the specified MS data file.
	 * 
	 * @param aFile MS data file to link.
	 * @throws DataException
	 */
	void addMSDatafile(ExternalFile aFile) throws DataException;
	
	/**
	 * Link the compound record to the specified IR data file.
	 * 
	 * @param aFile IR data file to link.
	 * @throws DataException
	 */
	void addIRDatafile(ExternalFile aFile) throws DataException;
	
	/**
	 * Link the compound record to the specified UV data file.
	 * 
	 * @param aFile UV data file to link.
	 * @throws DataException
	 */
	void addUVDatafile(ExternalFile aFile) throws DataException;
	
	String[] dataTypes();
	
	@Deprecated
	/**
	 * Get samples that are examples of this compound
	 * 
	 * @return Sample 
	 * @throws DataException
	 */
	Sample getSamples() throws DataException;
	
	/**
	 * Get materials that are examples of this compound
	 * 
	 * @return Material 
	 * @throws DataException
	 */
	Material getMaterials() throws DataException;
	
	BigDecimal getRetentionTime(CompoundObject aSample) throws DataException;
	
	BigDecimal getRetentionTimeForSample(String sampleID) throws DataException;
	
	BigDecimal getRetentionTimeForMaterial(String materialID) throws DataException;
	
	BigDecimal getRetentionTimeForSeparation(String separationID) throws DataException;
	
	/**
	 * Returns the associated project ID of the strain.
	 * 
	 * @return project ID as a {@link java.lang.String}.
	 * @throws DataException
	 */
	String getProjectID() throws DataException;
	
	Project getProject() throws DataException;

	void setProjectID(String newValue) throws DataException;
	
	void setProject(Project aProject) throws DataException;
	
}
