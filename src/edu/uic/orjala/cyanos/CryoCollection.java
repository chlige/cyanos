package edu.uic.orjala.cyanos;

import java.util.Date;

public interface CryoCollection extends Box {

	static final String DEWAR = "dewar";
	static final String RACK = "rack";
	static final String BOX = "box";
	
	void create(String newID) throws DataException;
	
	boolean loadDewars() throws DataException;
	
	/**
	 * Returns the ID of the current Cryo collection
	 * 
	 * @return Cryo Collection ID
	 */
	String getID();

	CryoCollection getChildren() throws DataException;
	
	CryoCollection getParent() throws DataException;
	
	String getFormat() throws DataException;
	
	String getName() throws DataException;
	
	String getNotes() throws DataException;
	
	String getDateString() throws DataException;
	
	Date getDate() throws DataException;
	
	void setName(String newValue) throws DataException;
	
	void setLength(int newValue) throws DataException;
	
	void setWidth(int newValue) throws DataException;
	
	void setNotes(String newValue) throws DataException;

	void addNotes(String newNotes) throws DataException;
	
	Cryo getVials() throws DataException;

	Cryo getVial(int row, int col) throws DataException;
	
	String getMaxLocationForStrainID(String strainID) throws DataException;
	
	String getMinLocationForStrainID(String strainID) throws DataException;

	Cryo getCryosForStrainID(String strainID) throws DataException;
	
	Cryo getCryosForInoculation(Inoc anInoc) throws DataException;
	
	Inoc getSourceInocs() throws DataException;
	
	Cryo getCryosForInoculationID(String inocID) throws DataException;

	Cryo getCurrentVial() throws DataException;
	
	Cryo getVialForLocation(int row, int column) throws DataException;
	
	void addCryoForLocation(Cryo aCryo, int row, int column) throws DataException;
	
	void addCryoForLocation(Cryo aCryo, String location) throws DataException;
	
	void addCryoForCurrentLocation(Cryo aCryo) throws DataException;
}
