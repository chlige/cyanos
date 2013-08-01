package edu.uic.orjala.cyanos;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Object to manage sample accounts (transaction histories)
 * 
 * @author George Chlipala
 *
 */
public interface SampleAccount extends BasicObject, NotebookObject {

	/**
	 * Returns the Sample ID for this account
	 * 
	 * @return Sample ID
	 */
	String getSampleID();

	/**
	 * Goto a specific line in the account history
	 * 
	 * @param aLine Number for the desired line
	 * @return true if the line exists
	 * @throws DataException
	 */
	boolean gotoAccountLine(int aLine) throws DataException;
	
	/**
	 * Add a new line to the account history.  The object will then move to the new line if created.
	 * 
	 * @return true if a new line was added
	 * @throws DataException
	 */
	boolean addTransaction() throws DataException;
	
	/**
	 * Get the current account line number.
	 * 
	 * @return Number of the current line.
	 * @throws DataException
	 */
	int getAccountLine() throws DataException;
	
	/**
	 * Get the date of the current account line
	 * 
	 * @return Date of the current account line
	 * @throws DataException
	 */
	Date getDate() throws DataException;
	
	/**
	 * Get the date of the current account line
	 * 
	 * @return Date of the current account line as a String
	 * @throws DataException
	 */
	String getDateString() throws DataException;
	
	/**
	 * Set the date of the current account line
	 * 
	 * @param newValue date to set.
	 * @throws DataException
	 */
	void setDate(Date newValue) throws DataException;
	
	/**
	 * Set the date of the current account line
	 * 
	 * @param newValue date to set.
	 * @throws DataException
	 */
	void setDate(String newValue) throws DataException;
	
	/**
	 * Returns the notes of the currently selected account line.
	 * 
	 * @return Note of the current account line
	 * @throws DataException
	 */
	String getNotes() throws DataException;
	
	/**
	 * Set the note/description of the currently selected account line
	 * 
	 * @param newValue String value of the new notes.  Will overwrite.
	 * @throws DataException
	 */
	void setNotes(String newValue) throws DataException;
	
	/**
	 * Append text to the notes/description of the currently selected account line.
	 * 
	 * @param newNotes String value to append to the end of the notes/description.
	 * @throws DataException
	 */
	void addNotes(String newNotes) throws DataException;
	
	/**
	 * Get the transaction amount of the currently selected account line.
	 * 
	 * @return Transaction amount as a BigDecimal.
	 * @throws DataException
	 */
	BigDecimal getAmount() throws DataException;

	/**
	 * Withdraw the specified amount from the account.  
	 * This method will update the currently selected account line and will parse the string for the proper unit prefix, e.g. k, m, or u.
	 * 
	 * @param aValue Withdrawal amount.
	 * @throws DataException
	 */
	void withdrawAmount(String aValue) throws DataException;
	
	/**
	 * Withdraw the specified amount from the account.  
	 * This method will update the currently selected account line and will parse the string for the proper unit prefix, e.g. k, m, or u.  
	 * If the amount string does not end with a unit, it will use the default unit specified.
	 * 
	 * @param amount Withdrawal amount.
	 * @param unit Default unit.
	 * @throws DataException
	 */
	void withdrawAmount(String amount, String unit) throws DataException;
	
	/**
	 * Deposit the specified amount into the account.  
	 * This method will update the currently selected account line and will parse the string for the proper unit prefix, e.g. k, m, or u.
	 * 
	 * @param aValue Deposit amount.
	 * @throws DataException
	 */
	void depositAmount(String aValue) throws DataException;
	
	/**
	 * Deposit the specified amount from the account.  
	 * This method will update the currently selected account line and will parse the string for the proper unit prefix, e.g. k, m, or u.  
	 * If the amount string does not end with a unit, it will use the default unit specified.
	 * 
	 * @param amount Deposit amount.
	 * @param unit Default unit.
	 * @throws DataException
	 */
	void depositAmount(String amount, String unit) throws DataException;

	/**
	 * Transfer the specified amount from source sample the account.  
	 * This method will update the currently selected account line and will parse the string for the proper unit prefix, e.g. k, m, or u.  
	 * If the amount string does not end with a unit, it will use the default unit specified.
	 * 
	 * @param source Source sample.
	 * @param amount Deposit amount.
	 * @param unit Default unit.
	 * @throws DataException
	 */
	SampleAccount transferAmount(Sample source, String amount, String unit) throws DataException;
	
	
	/**
	 * Get the CYANOS object that is referenced by the currently selected account line.
	 * 
	 * @return CYANOS object for the account line.  Will return null if there is no reference for the currently selected account line.
	 * @throws DataException
	 */
	BasicObject getReference() throws DataException;
	
	/**
	 * Set the reference of the currently selected account line.
	 * 
	 * @param aRef Sample to reference.
	 * @throws DataException
	 */
	void setTransactionReference(Sample aRef) throws DataException;

	/**
	 * Set the reference of the currently selected account line.
	 * 
	 * @param aRef Separation to reference
	 * @throws DataException
	 */
	void setTransactionReference(Separation aRef) throws DataException;
	
	/**
	 * Set the reference of the currently selected account line.
	 * 
	 * @param aRef Harvest to reference
	 * @throws DataException
	 */
	void setTransactionReference(Harvest aRef) throws DataException;

	/**
	 * Set the reference of the currently selected account line.
	 * 
	 * @param aRef Assay to reference
	 * @throws DataException
	 */
	void setTransactionReference(Assay aRef) throws DataException;

	/**
	 * Return the object {@link Class} of the reference of the current account line.
	 * 
	 * @return {@link Class} of reference.  One of {@link Sample}, {@link Separation}, {@link Harvest}
	 * @throws DataException
	 */
	Class<?> getTransactionReferenceClass() throws DataException;
	
	/**
	 * Get the reference ID of the current account line.
	 * 
	 * @return ID of the object referenced.
	 * @throws DataException
	 */
	String getTransactionReferenceID() throws DataException;
	
	/**
	 * Update the current accout line.
	 * 
	 * @throws DataException
	 */
	void updateTransaction() throws DataException;
	
	/**
	 * Get current balance of the entire sample account
	 * 
	 * @return Balance of the sample account
	 * @throws DataException
	 */
	BigDecimal accountBalance() throws DataException;

	/**
	 * Remove transaction reference, e.g. harvest, sample, separation.  NOTE: This will only delete the reference not the actual object.
	 * 
	 * @throws DataException
	 */
	void clearReference() throws DataException;

	/**
	 *  Void the transaction.  Requires the user to have the role {@link User#SAMPLE_ROLE} with permission {@link Role#WRITE}. 
	 * 	
	 * @throws DataException
	 * @see #isVoid()
	 * @see #isValid()
	 */
	void voidTransaction() throws DataException;

	/**
	 * Check if the transaction is voided.
	 * 
	 * @return true if the transaction exists and is void.
	 * @throws DataException
	 * @see #voidTransaction()
	 * @see #isValid()
	 */
	boolean isVoid() throws DataException;

	/**
	 * Check if the transaction is valid
	 * 
	 * @return true if the transaction exists and is not void.
	 * @throws DataException
	 * @see #voidTransaction()
	 * @see #isVoid()
	 */
	boolean isValid() throws DataException;

	/**
	 * Get date the transaction was voided.
	 * 
	 * @return date the transaction was voided.
	 * @throws DataException
	 */
	Date getVoidDate() throws DataException;
	
	/**
	 * Get the ID of the user who voided the transaction.
	 * 
	 * @return userID of the admin who voided the transaction.
	 * @throws DataException 
	 */
	String getVoidUserID() throws DataException;
	

}
