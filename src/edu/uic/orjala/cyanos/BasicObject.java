/**
 * 
 */
package edu.uic.orjala.cyanos;

import java.sql.Savepoint;

/**
 * @author George Chlipala
 *
 */
public interface BasicObject {
	
	public final static int MASS_TYPE = 1;
	public final static int VOLUME_TYPE = 2;
	public final static int CONCENTRATION_TYPE = 3;


	/**
	 * Move to next record.
	 * 
	 * @return true if another record exists.
	 * @throws DataException
	 */
	boolean next() throws DataException;

	/**
	 * Move to previous record.
	 * 
	 * @return true if a previous record exists.
	 * @throws DataException
	 */
	boolean previous() throws DataException;

	/**
	 * Move to first record in result set.
	 * 
	 * @return true if a record exists.
	 * @throws DataException
	 */
	boolean first() throws DataException;

	/**
	 * Move to last record in result set.
	 * 
	 * @return true if a record exists.
	 * @throws DataException
	 */
	boolean last() throws DataException;
	
	/**
	 * Move to before the first record.
	 * 
	 * @throws DataException
	 */
	void beforeFirst() throws DataException;
	
	/**
	 * Move to after last record.
	 * 
	 * @throws DataException
	 */
	void afterLast() throws DataException;
	
	/**
	 * Set manual refresh.  
	 * Any changes to the current record, i.e. set*() methods, will not be updated in the data store until {@link #refresh()} is invoked. 
	 */
	void setManualRefresh();
	
	/**
	 * Set automatic refresh.  All changes will be updated with the data store upon invocation of set*() method(s).
	 */
	void setAutoRefresh();
	
	/**
	 * Force the current record to update the data store.
	 * 
	 * @throws DataException
	 */
	void refresh() throws DataException;

	/**
	 * Return the number of records in the result set.
	 * 
	 * @return Number of records.
	 * @throws DataException
	 */
	int count() throws DataException;
	
	/**
	 * Check if the current user has the specified permission of this object.  
	 * This will take into account the project of the object, if the object can link project IDs.
	 * 
	 * @param permission one or a combination of {@link Role#READ}, {@link Role#WRITE}, {@link Role#CREATE}, or {@link Role#DELETE}
	 * @return true if the user has the permission.
	 */
	public boolean isAllowed(int permission);
	
	/**
	 * Check if a result set, i.e. data, is loaded for this object.
	 * 
	 * @return true if data has been loaded for this object.
	 * @throws DataException
	 */
	public boolean isLoaded() throws DataException;
	
	public boolean gotoRow(int row) throws DataException;
	
	public Savepoint setSavepoint() throws DataException;
	
	public Savepoint setSavepoint(String name) throws DataException;
	
	public void setAutoCommit(boolean value) throws DataException;
	
	public void commit() throws DataException;
	
	public void rollback() throws DataException;
	
	public void rollback(Savepoint savepoint) throws DataException;
	
	public String getAttribute(String attribute) throws DataException;
	
	public void setAttribute(String attribute, String value) throws DataException;
	
	public boolean isLast() throws DataException;

}
