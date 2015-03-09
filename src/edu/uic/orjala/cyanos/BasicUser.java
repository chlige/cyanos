/**
 * 
 */
package edu.uic.orjala.cyanos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.uic.orjala.cyanos.sql.SQLUser;

/**
 * This abstract class contains the methods to check permissions of a user.
 * 
 * @author George Chlipala
 *
 */
public abstract class BasicUser implements User {

	protected String myID = null;
	protected final Map<String, Map<String, Role>> projectRoles = new HashMap<String, Map<String, Role>>();
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.User#getUserID()
	 */
	public String getUserID() {
		return this.myID;
	}

	public boolean isAllowed(String role, String projectID, int permission) {
		if ( role == null ) return true;
		boolean globalPerm = this.hasGlobalPermission(role, permission);
		return ( globalPerm || this.hasPermissionInProject(projectID, role, permission));
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.User#isMemberOfProject(java.lang.String)
	 */
	public boolean isMemberOfProject(String projectID) {
		if ( this.projectRoles != null ) {
			if ( projectID == null ) projectID = NULL_PROJECT;
			return this.projectRoles.containsKey(projectID);
		}
		return false;
	}

	public boolean hasGlobalPermission(String role, int permission) {
		return this.hasPermissionInProject(GLOBAL_PROJECT, role, permission);
	}

	public boolean hasPermissionInProject(String projectID, String role, int permission) {
		if ( this.projectRoles != null ) {
			if ( projectID == null ) projectID = NULL_PROJECT;
			if ( this.projectRoles.containsKey(projectID) ) {
				Role myRole = this.projectRoles.get(projectID).get(role);
				if ( myRole != null ) return myRole.hasPermission(permission);
			}
		}
		return false;
	}

	public boolean couldPerform(String role, int permission) {
		if ( this.projectRoles != null ) {
			for ( Map<String,Role> project : this.projectRoles.values() ) {
				Role myRole = project.get(role);
				if ( myRole != null && myRole.hasPermission(permission) ) {
					return true;
				}
			}
		}
		return false;
	}
	
	public List<Role> globalRoles() {
		return this.rolesForProject(SQLUser.GLOBAL_PROJECT);
	}

	public List<Role> rolesForProject(String projectID) {
		List<Role> roleList = new ArrayList<Role>();
		if ( this.projectRoles.containsKey(projectID) ) {
			roleList.addAll(this.projectRoles.get(projectID).values());
		}
		return roleList;
	}


	
}
