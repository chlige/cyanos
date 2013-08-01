package edu.uic.orjala.cyanos;
/**
 * 
 */


/**
 * This exception class is used to handle exceptions due to insufficient permissions.
 * 
 * @author George Chlipala
 * @version 1.0
 *
 */
public class AccessException extends DataException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1511496553883150967L;
	private User myUser = null;
	private String myRole = null;
	private Integer myPerm = null;
		
	/**
	 * Create a new access exception.
	 * 
	 */
	public AccessException() {
	}

	/**
	 * Create a new access exception with the cause.
	 * 
	 * @param cause
	 */
	public AccessException(Throwable cause) {
		super(cause);
	}

	/**
	 * Create a new access exception with message.
	 * 
	 * @param message
	 */
	public AccessException(String message) {
		super(message);
	}

	/**
	 * Create a new access exception with cause and message.
	 * 
	 * @param message
	 * @param cause
	 */
	public AccessException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Create a new access exception.  
	 * The action was attempted by the specified user and required the stated role and permission.
	 * 
	 * @param aUser User who attempted to perform the action.
	 * @param requiredRole role required to perform the action.
	 * @param requiredPerm permission bit required to perform the action.
	 */
	public AccessException(User aUser, String requiredRole, int requiredPerm) {
		super();
		myUser = aUser;
		myRole = requiredRole;
		myPerm = new Integer(requiredPerm);
	}
	
	/**
	 * Get the user who attempted the action.
	 * 
	 * @return The user who attempted to perform the action.
	 */
	public User getUser() {
		return myUser;
	}
	
	/**
	 * Get the role required to perform the action.
	 * 
	 * @return The role required to perform the action.
	 */
	public String getRequiredRole() {
		return myRole;
	}
	
	/**
	 * Get the permission bit required to perform the action.
	 * 
	 * @return The permission bit required to perform the action.
	 */
	public Integer getPermissionBit() {
		return myPerm;
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.DataException#getMessage()
	 */
	public String getMessage() {
		Throwable cause = this.getCause();
		String message = null;
		if ( cause != null ) {
			message = cause.getMessage();
		} else if ( myUser != null ) {
			message = String.format("Access Denied for user %s using role %s and permission bit %s(%d)", myUser.getUserID(), myRole, Role.labelForBit(myPerm.intValue()), myPerm.intValue());
		} else { 
			message = super.getMessage();
		}
		
		if ( message != null ) {
			return message;
		} else {
			return "Access Denied";
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
		} else if ( myUser != null ) {
			message = String.format("Access Denied for user %s using role %s and permission bit %s(%d)", myUser.getUserID(), myRole, Role.labelForBit(myPerm.intValue()), myPerm.intValue());
		} else { 
			message = super.getLocalizedMessage();
		}
		
		if ( message != null ) {
			return message;
		} else {
			return "Access Denied";
		}
	}

}
