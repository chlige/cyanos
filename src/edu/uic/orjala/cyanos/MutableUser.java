/**
 * 
 */
package edu.uic.orjala.cyanos;

import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.AddressException;

/**
 * @author George Chlipala
 *
 */
public interface MutableUser {

	/**
	 * Get ID of current user, e.g. bsmith01.
	 * 
	 * @return User ID of current user.
	 */
	String getUserID();
	
	/**
	 * Get name of user, e.g. Bob Smith.
	 * 
	 * @return Name of current user.
	 * @throws DataException
	 */
	String getUserName() throws DataException;
		
	/**
	 * Get email of user, e.g. bob.smith@nowhere.com
	 * 
	 * @return Email address of current user.
	 * @throws DataException
	 */
	String getUserEmail() throws DataException;
	
	/**
	 * Get preference data of user.  NOT YET IMPLEMENTED.
	 * 
	 * @return User preferences as a String.
	 * @throws DataException
	 */
	String getUserPreferences() throws DataException;
	
	/**
	 * Set name of user.  
	 * 
	 * @param aName New name.
	 * @throws DataException
	 */
	void setUserName(String aName) throws DataException;
		
	/**
	 * Set email of user.
	 * 
	 * @param anEmail New email address.
	 * @throws DataException
	 */
	void setUserEmail(String anEmail) throws DataException;

	/**
	 * Move to the next user loaded.  For user administration.
	 * 
	 * @return true if there is a next object.
	 * @throws DataException
	 */
	boolean next() throws DataException;

	/**
	 * Move to the previous user loaded.  For user administration.
	 * 
	 * @return true if there is a previous object.
	 * @throws DataException
	 */
	boolean previous() throws DataException;

	/**
	 * Move to the first user loaded.  
	 * 
	 * @return true if there is a user.
	 * @throws DataException
	 */
	boolean first() throws DataException;

	/**
	 * Move to the last user loaded.
	 * 
	 * @return true if there is a user.
	 * @throws DataException
	 */
	boolean last() throws DataException;
	
	/**
	 * Move to before the first user loaded.  For user administration.
	 * 
	 * @throws DataException
	 */
	void beforeFirst() throws DataException;
	
	/**
	 * Move to after the last user loaded. For user administration.
	 * 
	 * @throws DataException
	 */
	void afterLast() throws DataException;

	/**
	 * Grant global permission to the user for the specified role.
	 * 
	 * @param role Role name, one of {@code User.ADMIN_ROLE}, {@code User.CULTURE_ROLE}, {@code User.SAMPLE_ROLE}, {@code User.BIOASSAY_ROLE} or {@code User.PROJECT_MANAGER_ROLE}.
	 * @param permission Permission bit, one of {@code Role.READ}, {@code Role.WRITE} or {@code Role.CREATE} 
	 * @throws DataException
	 */
	public void grantGlobalPermission(String role, int permission) throws DataException;
	
	/**
	 * Grant the user read/write permission for the specified role in the project.
	 * 
	 * @param projectID ID of the project
	 * @param role Role name, one of {@code User.ADMIN_ROLE}, {@code User.CULTURE_ROLE}, {@code User.SAMPLE_ROLE}, {@code User.BIOASSAY_ROLE} or {@code User.PROJECT_MANAGER_ROLE}.
	 * @param permission Permission bit to grant, one of {@code Role.READ}, {@code Role.WRITE} or {@code Role.CREATE}.
	 * @throws DataException
	 */
	public void grantPermissionForProject(String projectID, String role, int permission) throws DataException;

	/**
	 * Revoke the specified role in the project from the current user.
	 * 
	 * @param projectID ID of the project
	 * @param role Role name, one of {@code User.ADMIN_ROLE}, {@code User.CULTURE_ROLE}, {@code User.SAMPLE_ROLE}, {@code User.BIOASSAY_ROLE} or {@code User.PROJECT_MANAGER_ROLE}.
	 * @throws DataException
	 */
	public void removeFromProject(String projectID, String role) throws DataException;
	
	/**
	 * Revoke the specified global role from the current user.  NOTE: This will not revoke roles at the project level.
	 * 
	 * @param role Role name, one of {@code User.ADMIN_ROLE}, {@code User.CULTURE_ROLE}, {@code User.SAMPLE_ROLE}, {@code User.BIOASSAY_ROLE} or {@code User.PROJECT_MANAGER_ROLE}.
	 * @throws DataException
	 */
	public void removeGlobalRole(String role) throws DataException;
	
	public List<Role> rolesForProject(String projectID) throws DataException;
	
	public List<Role> globalRoles() throws DataException;
	
	public Map<String, Role> roleMapForProject(String projectID) throws DataException;
	
	public Map<String, Role> globalRoleMap() throws DataException;
	
	public User asUser() throws DataException;
	
	public void resetPassword(Session mailSession) throws DataException, AddressException, MessagingException;
}
