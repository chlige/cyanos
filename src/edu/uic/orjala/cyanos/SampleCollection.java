package edu.uic.orjala.cyanos;

public interface SampleCollection extends Box {

	/**
	 * Returns the ID of the current Sample Collection record.
	 * 
	 * @return Sample Collection ID
	 */
	String getID();

	Sample getCurrentSample() throws DataException;
	
	Sample getSampleForLocation(int row, int column) throws DataException;
	
	String getLibrary() throws DataException;
	
	void setLibrary(String newValue) throws DataException;
	
	String getName() throws DataException;

	String getNotes() throws DataException;
		
	void setName(String newValue) throws DataException;
	
	void setLength(int newValue) throws DataException;
	
	void setWidth(int newValue) throws DataException;
	
	void setNotes(String newValue) throws DataException;

	void addNotes(String newNotes) throws DataException;
	
	Sample getSamples() throws DataException;
	
	void addSampleToCurrentLocation(Sample aSample) throws DataException;
}
