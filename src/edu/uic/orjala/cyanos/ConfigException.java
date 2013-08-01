/**
 * 
 */
package edu.uic.orjala.cyanos;

/**
 * @author George Chlipala
 *
 */
public class ConfigException extends DataException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3497708127183178220L;
	private String param = null;
	private String group = null;
	private String key = null;
	/**
	 * 
	 */

	public ConfigException(String param, String group, String key, Throwable cause) {
		super(cause);
		this.param = param;
		this.group = group;
		this.key = key;
	}

	public ConfigException(String param, String group, Throwable cause) {
		super(cause);
		this.param = param;
		this.group = group;
	}
	
	public ConfigException(String param, Throwable cause) {
		super(cause);
		this.param = param;
	}
	
 	/**
	 * @param cause
	 */
	public ConfigException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public String getMessage() {
		Throwable cause = this.getCause();
		String message = null;
		if ( cause != null ) {
			message = cause.getMessage();
		} else if ( param != null ) {
			message = String.format("Error accessing configuration resource PARAM: %s GROUP: %s KEY: %s", param, group, key);
		} else { 
			message = super.getMessage();
		}
		
		if ( message != null ) {
			return message;
		} else {
			return "Configuration Error";
		}
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.DataException#getLocalizedMessage()
	 */
	public String getLocalizedMessage() {
		Throwable cause = this.getCause();
		String message = null;
		if ( cause != null ) {
			message = cause.getLocalizedMessage();
		} else if ( param != null ) {
			message = String.format("Error accessing configuration resource PARAM: %s GROUP: %s KEY: %s", param, group, key);
		} else { 
			message = super.getLocalizedMessage();
		}
		
		if ( message != null ) {
			return message;
		} else {
			return "Configuration Error";
		}
	}

}
