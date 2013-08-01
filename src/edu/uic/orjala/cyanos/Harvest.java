package edu.uic.orjala.cyanos;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Interface for Harvest records
 * 
 * @author George Chlipala
 *
 */
public interface Harvest extends BasicObject, NotebookObject, RemoteObject {

	/**
	 * Returns the ID of the current Harvest record
	 * 
	 * @return Harvest ID
	 */
	String getID();
	
	/**
	 * Get the strain ID for the current harvest record.
	 * 
	 * @return strain ID of the harvest.
	 * @throws DataException
	 */
	String getStrainID() throws DataException;
	
	/**
	 * Get the strain for the current harvest record.
	 * 
	 * @return Strain object for harvest
	 * @throws SQLException 
	 * @see Strain
	 */
	Strain getStrain() throws DataException;
	
//	String getName() throws DataException;
	
	/**
	 * Get the date of the current harvest record.
	 * 
	 * @return Data of the current harvest record.
	 * @throws DataException
	 */
	Date getDate() throws DataException;
	
	/**
	 * Get the color of the harvest.
	 * 
	 * @return Color of the harvest.
	 * @throws DataException
	 */
	String getColor() throws DataException;
	
	/**
	 * Get the type description of the material, e.g. filamentous or planktonic.
	 * 
	 * @return Type description of the material
	 * @throws DataException
	 */
	String getType() throws DataException;
	
	/**
	 * Get the weight of the dried cell mass. 
	 * 
	 * @return Weight of the dired cell mass.  Will return null if the weight has not been set.
	 * @throws DataException
	 */
	BigDecimal getCellMass() throws DataException;
	
	/**
	 * Get the volume of the media harvested.
	 * 
	 * @return Volume of the media harvest.
	 * @throws DataException
	 */
	BigDecimal getMediaVolume() throws DataException;
	
	/**
	 * Get the date the harvest was prepared and thus read for extraction.
	 * 
	 * @return Date the harvest was prepared for extraction.
	 * @throws DataException
	 */
	Date getPrepDate() throws DataException;
	
	/**
	 * Get the date the harvest was prepared for extraction
	 * 
	 * @return Date the harvest was prepared for extraction, as a string.
	 * @throws DataException
	 */
	String getPrepDateString() throws DataException;
	
	/**
	 * Get the associated project ID.
	 * 
	 * @return project ID linked to this harvest record.  Null if the harvest is not linked to a project.
	 * @throws DataException
	 */
	String getProjectID() throws DataException;
	
	/**
	 * Get the associated project.
	 * 
	 * @return Project linked to this harvest record.  Null if the harvest is not linked to a project.
	 * @throws DataException
	 */
	Project getProject() throws DataException;
	
	/**
	 * Link the harvest to the desginated project ID.
	 * 
	 * @param newValue Project ID to link.
	 * @throws DataException
	 */
	void setProjectID(String newValue) throws DataException;
	
	/**
	 * Link the harvest record to the desginated project.
	 * 
	 * @param aProject Project to link.
	 * @throws DataException
	 */
	void setProject(Project aProject) throws DataException;

	/**
	 * Sets source strain to aStrain
	 * 
	 * @param aStrain Strain Object
	 * @throws DataException 
	 */
	void setStrain(Strain aStrain) throws DataException;

	/**
	 * Sets source strain ID to anID
	 * 
	 * @param anID String
	 * @throws DataException 
	 */
	void setStrainID(String anID) throws DataException;

//	void setName(String aName) throws DataException;
	
	/**
	 * Set the date the harvest was prepared for extraction.
	 * 
	 * @param newDate Date the harvest was prepared for extraction.
	 * @throws DataException
	 */
	void setPrepDate(String newDate) throws DataException;
	
	/**
	 * Set the date the harvest was prepared for extraction.
	 * 
	 * @param newDate Date the harvest was prepared for extraction.
	 * @throws DataException
	 */
	void setPrepDate(Date newDate) throws DataException;
	
	/**
	 * Get the notes/description of the harvest.
	 * 
	 * @return Notes/description of the current harvest record.
	 * @throws DataException
	 */
	String getNotes() throws DataException;
	
	/**
	 * Set the date of the harvest.
	 * 
	 * @param newDate Date of the harvest.
	 * @throws DataException
	 */
	void setDate(Date newDate) throws DataException;
	
	/**
	 * Set the date of the harvest.
	 * 
	 * @param newDate Date of the harvest.
	 * @throws DataException
	 */
	void setDate(String newDate) throws DataException;
	
	/**
	 * Set the color of the material harvested
	 * 
	 * @param newColor Color of the material harvested.
	 * @throws DataException
	 */
	void setColor(String newColor) throws DataException;
	
	/**
	 * Set the type description of the material, e.g. filamentous or planktonic.
	 * 
	 * @param newType Type decription of the material.
	 * @throws DataException
	 */
	void setType(String newType) throws DataException;
	
	/**
	 * Set the amount of cell mass harvested.  The amount should be in grams.
	 * 
	 * @param newCellmass Amount of the cell mass in grams.
	 * @throws DataException
	 */
	void setCellMass(BigDecimal newCellmass) throws DataException;
	
	/**
	 * Set the amount of cell mass harvested.  This method will parse the string to determine the proper unit scale, e.g. k, m, or u.
	 * 
	 * @param newCellmass Amount of the cell mass.
	 * @throws DataException
	 */
	void setCellMass(String newCellMass) throws DataException;
	
	/**
	 * Set the amount of media harvested.  This method will parse the string to determine proper unit scale, e.g. k, m, or u.
	 * 
	 * @param newVol Amount of media harvested.
	 * @throws DataException
	 */
	void setMediaVolume(String newVol) throws DataException;

	/**
	 * Set the amount of media harvested.  This amount should be in liters.
	 * 
	 * @param newVol Amount of media harvested, in liters.
	 * @throws DataException
	 */
	void setMediaVolume(BigDecimal newVol) throws DataException;
	
	/**
	 * Set the notes/description of the harvest.  This method will overwrite the current notes/description of the harvest record.
	 * 
	 * @param newNotes New harvest notes/description.
	 * @throws DataException
	 */
	void setNotes(String newNotes) throws DataException;
	
	/**
	 * Append new notes/descriptions to the harvest.   This method will add the text to the end of the current harvest notes.
	 * 
	 * @param newNotes Additional text to add to the harvest notes/description.
	 * @throws DataException
	 */
	void addNotes(String newNotes) throws DataException;

	/**
	 * Get the parent inoculations for this harvest record.
	 * 
	 * @return Parent inoculations of the current harvest.
	 * @throws DataException
	 */
	Inoc getInoculations() throws DataException;
	
	/**
	 * Determine if this harvest was from field collected material.
	 * 
	 * @return true if the harvest was from field collected material.
	 * @throws DataException
	 */
	boolean isFieldHarvest() throws DataException;
	
	/**
	 * Get the ID of the parent field collection.
	 * 
	 * @return ID of the parent field collection.
	 * @throws DataException
	 */
	String getCollectionID() throws DataException;
	
	/**
	 * Get the parent field collection
	 * 
	 * @return Parent field collection.
	 * @throws DataException
	 */
	Collection getCollection() throws DataException;
	
	/**
	 * Set the parent field collection of this harvest.
	 * 
	 * @param newValue ID of the parent field collection.
	 * @throws DataException
	 */
	void setCollectionID(String newValue) throws DataException;
	
	/**
	 * Set the parent field collection of this harvest.
	 * 
	 * @param aCol Parent field collection.
	 * @throws DataException
	 */
	void setCollection(Collection aCol) throws DataException;
	
	/**
	 * Unlink the current parent field collection of this harvest.
	 * 
	 * @throws DataException
	 */
	void unlinkCollection() throws DataException;
	
	/**
	 * Get child extract(s) of this harvest. 
	 * 
	 * @return Material containing a result set of all child extracts.
	 * @throws DataException
	 */
	Material getExtract() throws DataException;
	
	/**
	 * Create a new child extract for this harvest.
	 * 
	 * @return Material object of the new child extract.
	 * @throws DataException
	 */
	Material createExtract() throws DataException;
}
