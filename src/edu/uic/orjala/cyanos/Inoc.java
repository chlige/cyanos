package edu.uic.orjala.cyanos;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Interface for inoculation records.
 * 
 * @author George Chlipala
 *
 */
public interface Inoc extends BasicObject, NotebookObject {

	final static String FATE_HARVEST = "harvest";
	final static String FATE_STOCK = "stock";
	final static String FATE_CRYO = "cryo";
	final static String FATE_DEAD = "dead";
	
	/**
	 * Returns the ID of the current Inoc record
	 * 
	 * @return Inoculation ID
	 */
	String getID();

	Strain getStrain() throws DataException;
	
	String getStrainID() throws DataException;
	
	Inoc getParent() throws DataException;
	
	Harvest getHarvest() throws DataException;
	
	String getHarvestID() throws DataException;
	
	Date getDate() throws DataException;
	
	String getMedia() throws DataException;
	
	BigDecimal getVolume() throws DataException;
	
	String getVolumeString(float cutOff) throws DataException;
	
	String getVolumeString() throws DataException;

	String getNotes() throws DataException;
	
	String getFate() throws DataException;
	
	Date getRemoveDate() throws DataException;
	
	String getProjectID() throws DataException;

	Project getProject() throws DataException;

	void setStrain(Strain aStrain) throws DataException;
	
	void setStrainID(String anID) throws DataException;
	
	void setParent(Inoc aParent) throws DataException;
	
	void setParentID(String anID) throws DataException;
	
	void setHarvest(Harvest aHarvest) throws DataException;
	
	void setHarvest(String aHarvest) throws DataException;
	
	Harvest createHarvest() throws DataException;

	Inoc createChild() throws DataException;

	void setMedia(String newMedia) throws DataException;
	
	void setVolume(String newVol) throws DataException;

	void setVolume(BigDecimal newVol) throws DataException;
	
	void setNotes(String newNotes) throws DataException;
	
	void setDate(java.util.Date newDate) throws DataException;
	
	void setDate(String newDate) throws DataException;
	
	void setRemovedDate(java.util.Date newDate) throws DataException;
	
	void setRemovedDate(String newDate) throws DataException;
	
	void setFate(String newFate) throws DataException;
	
	void setProjectID(String newValue) throws DataException;
	
	void setProject(Project aProject) throws DataException;
	
	void addNotes(String newNotes) throws DataException;
	
	Inoc getChildren() throws DataException;
	
	Cryo getCryoParent() throws DataException;

}
