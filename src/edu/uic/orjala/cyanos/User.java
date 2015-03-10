/**
 * 
 */
package edu.uic.orjala.cyanos;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;



/**
 * This User class is used to read data and determine permissions for a user.
 * For write access see {@link edu.uic.orjala.cyanos.MutableUser}
 * 
 * @author George Chlipala
 *
 */

public interface User {

	static final String ADMIN_ROLE = "admin";
	static final String CULTURE_ROLE = "culture";
	static final String SAMPLE_ROLE = "sample";
	static final String BIOASSAY_ROLE = "assay";
	static final String PROJECT_MANAGER_ROLE = "project";
	
	static final String NULL_PROJECT = "";
	static final String GLOBAL_PROJECT = "*";
	
	static final String[] ROLES = { ADMIN_ROLE, CULTURE_ROLE, SAMPLE_ROLE, BIOASSAY_ROLE, PROJECT_MANAGER_ROLE };
		
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
	 * @return User perferences as a String.
	 * @throws DataException
	 */
	String getUserPreferences() throws DataException;
	
	/**
	 * Determine if user has permission for specified role in project.
	 * 
	 * @param projectID ID of the project
	 * @param role Role name, one of {@code User.ADMIN_ROLE}, {@code User.CULTURE_ROLE}, {@code User.SAMPLE_ROLE}, {@code User.BIOASSAY_ROLE} or {@code User.PROJECT_MANAGER_ROLE}.
	 * @param permission Permission bit, one of {@code Role.READ}, {@code Role.WRITE} or {@code Role.CREATE} 
	 * @return true if user has read access for this project.
	 */
	public boolean hasPermissionInProject(String projectID, String role, int permission);
	
	/**
	 * Determine if user is a member of this project, i.e. has any role with any permission.
	 * 
	 * @param projectID ID of the project
	 * @return true if the user is a member of the project.
	 */
	public boolean isMemberOfProject(String projectID);

	/**
	 * Determine if the user has global permission for the specified role.
	 * 
	 * @param role Role name, one of {@code User.ADMIN_ROLE}, {@code User.CULTURE_ROLE}, {@code User.SAMPLE_ROLE}, {@code User.BIOASSAY_ROLE} or {@code User.PROJECT_MANAGER_ROLE}.
	 * @param permission Permission bit, one of {@code Role.READ}, {@code Role.WRITE} or {@code Role.CREATE} 
	 * @return true if the user has global permission.
	 */
	public boolean hasGlobalPermission(String role, int permission);
		
	/**
	 * Determine if the user has permission for the specified role.  
	 * This method will take into consideration if user has global access, which will override project specific permissions.
	 * 
	 * @param projectID ID of the project.
	 * @param role Role name, one of {@code User.ADMIN_ROLE}, {@code User.CULTURE_ROLE}, {@code User.SAMPLE_ROLE}, {@code User.BIOASSAY_ROLE} or {@code User.PROJECT_MANAGER_ROLE}.
	 * @param permission Permission bit, one of {@code Role.READ}, {@code Role.WRITE} or {@code Role.CREATE}.
	 * @return true if the user has permission.
	 */
	boolean isAllowed(String role, String projectID, int permission);
	
	/**
	 * Determine if the user has permission for the specified role in any project.
	 * 
	 * @param role Role name, one of {@code User.ADMIN_ROLE}, {@code User.CULTURE_ROLE}, {@code User.SAMPLE_ROLE}, {@code User.BIOASSAY_ROLE} or {@code User.PROJECT_MANAGER_ROLE}.
	 * @param permission Permission bit, one of {@code Role.READ}, {@code Role.WRITE} or {@code Role.CREATE} 
	 * @return true if the user has permission in any project.
	 */
	boolean couldPerform(String role, int permission);
	
	public List<Role> globalRoles();
	
	public List<Role> rolesForProject(String projectID);

	public abstract boolean checkPassword(String password) throws DataException;

	public abstract void reload() throws DataException;
	
	

}
