/**
 * 
 */
package edu.uic.orjala.cyanos;

/**
 * Generic class to wrap any exceptions related to accessing data for CYANOS object.  
 * All methods for the CYANOS objects should use the DataException class to wrap exceptions, e.g. SQLException, thrown by various methods used to 
 * access data stores.
 * 
 * @author George Chlipala
 *
 */
public class DataException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1177645530175587499L;
	
	public DataException() {
		super();
	}
	
	public DataException(Throwable cause) {
		super(cause);
	}
	
	public DataException(String message) { 
		super(message);
	}
	
	public DataException(String message, Throwable cause) { 
		super(message, cause);
	}

	public String getMessage() {
		Throwable cause = this.getCause();
		if ( cause != null ) 
			return cause.getMessage();
		else if ( super.getMessage() != null )
			return super.getMessage();
		else
			return "Data Access Error";
	}
	
	public String getLocalizedMessage() {
		Throwable cause = this.getCause();
		if ( cause != null ) 
			return cause.getLocalizedMessage();
		else if ( super.getLocalizedMessage() != null )
			return super.getLocalizedMessage();
		else
			return "Data Access Error";
	}
}
