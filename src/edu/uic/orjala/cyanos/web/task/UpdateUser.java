package edu.uic.orjala.cyanos.web.task;

import java.util.HashMap;
import java.util.Map;

import edu.uic.orjala.cyanos.BasicUser;
import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.Role;
import edu.uic.orjala.cyanos.User;

public class UpdateUser extends BasicUser {

	private static final int PERMS = Role.READ + Role.WRITE + Role.CREATE;
	private static final String[] ROLE_LIST = {User.BIOASSAY_ROLE, User.CULTURE_ROLE, User.SAMPLE_ROLE, User.PROJECT_MANAGER_ROLE};

	/**
	 * 
	 */
	public UpdateUser(String projectID) {
		super();
		Map<String, Role> roles = new HashMap<String, Role>();
		Map<String, Role> globalRoles = new HashMap<String,Role>();
		globalRoles.put(User.PROJECT_MANAGER_ROLE, new Role(User.PROJECT_MANAGER_ROLE, Role.READ + Role.WRITE));
		for ( String role : ROLE_LIST ) {
			roles.put(role, new Role(role, PERMS));
			globalRoles.put(role, new Role(role, Role.READ));
		}
		this.projectRoles.put(projectID, roles);		
		this.projectRoles.put(GLOBAL_PROJECT, globalRoles);
		this.myID = "updateUser";
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.User#getUserName()
	 */
	public String getUserName() throws DataException {
		return this.myID;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.User#getUserEmail()
	 */
	public String getUserEmail() throws DataException {
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.User#getUserPreferences()
	 */
	public String getUserPreferences() throws DataException {
		return null;
	}

}