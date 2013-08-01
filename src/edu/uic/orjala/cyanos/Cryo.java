/**
 * 
 */
package edu.uic.orjala.cyanos;

import java.util.Date;

/**
 * Interface for cyropreservation records.
 * 
 * @author George Chlipala
 *
 */
public interface Cryo extends BasicObject {

	/**
	 * Returns the ID of the current Cryopreservation record.
	 * 
	 * @return Cryo ID
	 */
	String getID();

	/**
	 * Retrieve the notes.
	 * 
	 * @return Collection notes
	 * @throws DataException
	 */
	String getNotes() throws DataException;

	/**
	 * Retrieve the cryo preservation date.
	 * 
	 * @return Cryo preservation date as a Date object.
	 * @throws DataException
	 */
	Date getDate() throws DataException;

	/**
	 * Retrieve the cryo preservation date.
	 * 
	 * @return Cryo preservation date as a string.
	 * @throws DataException
	 */
	String getDateString() throws DataException;
	
	String getLocation() throws DataException;
	
	String getRow() throws DataException;
	
	String getColumn() throws DataException;
	
	String getCultureID() throws DataException;

	String getCollectionID() throws DataException;
	
	Inoc getSourceInoc() throws DataException;
	
	Inoc getThawInoc() throws DataException;
	
	void setRow(String newValue) throws DataException;
	
	void setColumn(String newValue) throws DataException;
	
	void setLocation(String aRow, String aCol) throws DataException;
	
	void setLocation(int row, int column) throws DataException;

	void setCultureID(String newCultureID) throws DataException;

	/**
	 * Set cryo preservation date
	 * 
	 * @param newValue cryo preservation date as a Date object.
	 * @throws DataException
	 */
	void setDate(Date newValue) throws DataException;

	/**
	 * Set cryo preservation date
	 * 
	 * @param newValue cryo preservation date as a string.
	 * @throws DataException
	 */
	void setDate(String newValue) throws DataException;

	/**
	 * Set cryo preservation notes.
	 * 
	 * @param newNotes cryo preservation notes.
	 * @throws DataException
	 */
	void setNotes(String newNotes) throws DataException;

	public void setCollection(CryoCollection aCol) throws DataException;
	
	public void setCollection(String aColID) throws DataException;
	
	public void setSourceInoc(Inoc anInoc) throws DataException;
	
	/**
	 * 
	 * Will return true if a thaw ID exists.
	 * 
	 * @return boolean
	 * @throws DataException
	 */
	
	boolean isThawed() throws DataException;
	
	boolean wasRemoved() throws DataException;

	boolean isFrozen() throws DataException;

	Inoc thaw() throws DataException;
	
	void remove() throws DataException;
	
	public boolean loadFromCollection(CryoCollection aCol, int row, int column) throws DataException;
	
	public boolean loadFromCollection(String collectionID, int row, int column) throws DataException;
	
	public boolean loadForStrain(Strain aStrain) throws DataException;
	
	public boolean loadForStrainID(String cultureID) throws DataException;

}
