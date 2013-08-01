/**
 * 
 */
package edu.uic.orjala.cyanos.web;

import java.util.HashMap;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.AddressException;

import edu.uic.orjala.cyanos.BasicUser;
import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.Role;

/**
 * @author George Chlipala
 *
 */
public class GuestUser extends BasicUser {

	/**
	 * 
	 */
	public GuestUser() {
		this.myID = "guest";
	}
	
	public GuestUser(String[] allowedRoles) {
		this();
		this.setRoles(allowedRoles);
	}

	public GuestUser(Role[] allowedRoles) {
		this();
		this.setRoles(allowedRoles);
	}

	public GuestUser(String[] allowedRoles, String[] projects) {
		this();
		this.setRoles(allowedRoles);
		for (int i = 0; i < projects.length; i++ ) {
			this.addProject(projects[i]);
		}
	}

	public GuestUser(Role[] allowedRoles, String[] projects) {
		this();
		this.setRoles(allowedRoles);
		for (int i = 0; i < projects.length; i++ ) {
			this.addProject(projects[i]);
		}
	}

	public void setRoles(String[] allowedRoles) {
		this.projectRoles.clear();
		Map<String,Role> roleList = new HashMap<String,Role>(allowedRoles.length);
		for (int i = 0; i < allowedRoles.length; i++) {
			Role aRole = new Role(allowedRoles[i], Role.READ);
			roleList.put(allowedRoles[i], aRole);
		}
		this.projectRoles.put(NULL_PROJECT, roleList);
	}


	protected void setRoles(Role[] allowedRoles) {
		this.projectRoles.clear();
		Map<String,Role> roleList = new HashMap<String,Role>(allowedRoles.length);
		for (int i = 0; i < allowedRoles.length; i++) {
			roleList.put(allowedRoles[i].roleName(), allowedRoles[i]);
		}
		this.projectRoles.put(NULL_PROJECT, roleList);
	}
	
	protected void setRole(String role) {
		Map<String,Role> roleList;
		if ( this.projectRoles.containsKey(NULL_PROJECT) ) {
			roleList = this.projectRoles.get(NULL_PROJECT);
		} else {
			roleList = new HashMap<String,Role>();
			this.projectRoles.put(NULL_PROJECT, roleList);
		}
		Role test = this.projectRoles.get(NULL_PROJECT).get(role);
		if ( test == null ) {
			roleList.put(role, new Role(role, Role.READ));
		}
	}
	
	protected void addProject(String project) {
		if ( ! this.projectRoles.containsKey(project) ) {
			Map<String,Role> roleList = this.projectRoles.get(NULL_PROJECT);
			this.projectRoles.put(project, roleList);
		}
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.User#getUserEmail()
	 */
	public String getUserEmail() throws DataException {
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.User#getUserName()
	 */
	public String getUserName() throws DataException {
		return "Guest User";
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.User#getUserPreferences()
	 */
	public String getUserPreferences() throws DataException {
		return null;
	}

	public void resetPassword(Session mailSession) throws DataException, AddressException, MessagingException {
		// TODO Auto-generated method stub
		
	}
	
}
