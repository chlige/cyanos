package edu.uic.orjala.cyanos;

public interface CompoundObject extends BasicObject {
	
	
	/**
	 * Returns the ID of the current object
	 * 
	 * @return Object ID
	 */
	String getID();

	void unlinkCompoundID(String compoundID) throws DataException;

	void unlinkCompound(Compound aCompound) throws DataException;

	Compound getCompounds() throws DataException;

	boolean hasCompounds() throws DataException;
	
	Double getRetentionTime(Compound aCompound) throws DataException;
	
	Double getRetentionTime(String compoundID) throws DataException;
	
}
