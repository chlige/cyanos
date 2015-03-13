/**
 * 
 */
package edu.uic.orjala.cyanos.sql;

import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;

import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.MutableUser;
import edu.uic.orjala.cyanos.Role;
import edu.uic.orjala.cyanos.User;
import edu.uic.orjala.cyanos.web.listener.AppConfigListener;

/**
 * @author George Chlipala
 *
 */
public class SQLMutableUser extends SQLObject implements MutableUser {

	/**
	 * 
	 */
	private static final String SQL_LOAD_ALL_USERS = "SELECT * FROM users ORDER BY username";
	private static final String SQL_LOAD_PROJECT_USERS = "SELECT * FROM users JOIN roles r ON (users.username = r.username) WHERE r.project_id = ? ORDER BY username";
	private final static String INSERT_USER_SQL = "INSERT INTO users(username) VALUES(?)";
	private final static String REVOKE_USER_FROM_PROJECT_SQL = "DELETE FROM roles WHERE username=? AND role=? AND project_id=?";
	private final static String SQL_INSERT_GRANT = "INSERT INTO roles(perm,username,project_id,role) VALUES(?,?,?,?)";
	private final static String SQL_DEL_ROLES = "DELETE FROM roles WHERE username=?";
	private final static String SQL_DEL_USER = "DELETE FROM users WHERE username=?";
	
	private final static String SQL_GET_ROLES = "SELECT username,project_id,role,perm FROM roles WHERE username=? AND project_id=?";
	private final static String USER_SQL = "SELECT * FROM users WHERE username=?";
	
	private final static String USER_EMAIL = "email";
	private final static String USER_NAME = "fullname";
	private final static String USER_ID = "username";
	
	private final static String ROLE_NAME = "role";
	private static final String ROLE_PERM = "perm";

	/**
	 * Load all users.
	 * 
	 * @param data a SQLData object.
	 * @return a SQLMutableUser object with all users.
	 * @throws DataException
	 */
	public static MutableUser users(SQLData data) throws DataException {
		SQLMutableUser userList = new SQLMutableUser(data);
		if ( data.getUser().hasGlobalPermission(User.ADMIN_ROLE, Role.READ) ) {
			userList.loadUsingSQL(SQL_LOAD_ALL_USERS);
		}
		return userList;
	}
	
	/**
	 * Create a new user
	 * 
	 * @param data a SQLData object
	 * @param userID ID of the new user
	 * @return a SQLUser object of the new user.
	 * @throws DataException
	 */
	public static MutableUser createUser(SQLData data, String userID) throws DataException {
		SQLMutableUser newUser = null;
		if ( data.getUser().hasGlobalPermission(User.ADMIN_ROLE, Role.CREATE) ) {
			try {
				PreparedStatement aPsth = data.prepareStatement(INSERT_USER_SQL);
				aPsth.setString(1, userID);
				if ( aPsth.executeUpdate() > 0 ) {
					newUser = new SQLMutableUser(data, userID);
				}
				aPsth.close();
			} catch (SQLException e) {
				throw new DataException(e);
			}
		}
		return newUser;
	}
	
	/**
	 * Load all users for a project.
	 * 
	 * @param data a SQLData object.
	 * @param projectID ID of project.
	 * @return a SQLMutableUser object with all users in selected project
	 * @throws DataException
	 */
	public static MutableUser usersForProject(SQLData data, String projectID) throws DataException {
		SQLMutableUser userList = new SQLMutableUser(data);
		if ( data.getUser().hasGlobalPermission(User.ADMIN_ROLE, Role.READ) ) {
			try {
				PreparedStatement aPsth = data.prepareStatement(SQL_LOAD_PROJECT_USERS);
				aPsth.setString(1, projectID);
				userList.loadUsingPreparedStatement(aPsth);
			} catch (SQLException e) {
				throw new DataException(e);
			}
		}
		return userList;
	}
	
	/**
	 * Delete the specified user.  
	 * NOTE: It will not delete if the user specified is the same as the requesting user {@link SQLData#getUser()}.
	 * 
	 * @param data a SQLData object
	 * @param userID ID of user to delete
	 * @throws DataException
	 */
	public static void deleteUser(SQLData data, String userID) throws DataException {
		if (data.getUser().getUserID().equals(userID) ) {
			return;
		}
		if ( data.getUser().hasGlobalPermission(User.ADMIN_ROLE, Role.DELETE) ) {
			try {
				PreparedStatement aPsth = data.prepareStatement(SQL_DEL_USER);
				aPsth.setString(1, userID);
				if ( aPsth.executeUpdate() > 0 ) {
					aPsth.close();
					aPsth = data.prepareStatement(SQL_DEL_ROLES);
					aPsth.setString(1, userID);
					aPsth.executeUpdate();
				}
				aPsth.close();
			} catch (SQLException e) {
				throw new DataException(e);
			}
		}
	}
	
	public static SQLMutableUser load(SQLData data, String userID) throws DataException {
		return new SQLMutableUser(data, userID);
	}
	
	public SQLMutableUser(SQLData data) {
		super(data);
		this.initVals();
	}
	
	public SQLMutableUser(SQLData data, String userID) throws DataException {
		this(data);
		this.myID = userID;
		this.fetchRecord();
	}

	protected void initVals() {
		this.idField = USER_ID;
		this.myData.setAccessRole(User.ADMIN_ROLE);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.sql.SQLObject#fetchRecord()
	 */
	
	protected void fetchRecord() throws DataException {
		this.fetchRecord(USER_SQL);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.User#getUserEmail()
	 */
	public String getUserEmail() throws DataException {
		return this.myData.getString(USER_EMAIL);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.User#getUserID()
	 */
	public String getUserID() {
		return this.getID();
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.User#getUserName()
	 */
	public String getUserName() throws DataException {
		return this.myData.getString(USER_NAME);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.User#getUserPreferences()
	 */
	public String getUserPreferences() throws DataException {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Grant global permission to the user for the specified role.
	 * 
	 * @param role Role name, one of {@code User.ADMIN_ROLE}, {@code User.CULTURE_ROLE}, {@code User.SAMPLE_ROLE}, {@code User.BIOASSAY_ROLE} or {@code User.PROJECT_MANAGER_ROLE}.
	 * @param permission Permission bit, one of {@code Role.READ}, {@code Role.WRITE} or {@code Role.CREATE} 
	 * @throws DataException
	 */
	public void grantGlobalPermission(String role, int permission) throws DataException {
		this.grantPermissionForProject(SQLUser.GLOBAL_PROJECT, role, permission);
	}

	/**
	 * Grant the user read/write permission for the specified role in the project.
	 * 
	 * @param projectID ID of the project
	 * @param role Role name, one of {@code User.ADMIN_ROLE}, {@code User.CULTURE_ROLE}, {@code User.SAMPLE_ROLE}, {@code User.BIOASSAY_ROLE} or {@code User.PROJECT_MANAGER_ROLE}.
	 * @param permission Permission bit to grant, one of {@code Role.READ}, {@code Role.WRITE} or {@code Role.CREATE}.
	 * @throws DataException
	 */
	public void grantPermissionForProject(String projectID, String role, int permission) throws DataException {
		boolean allowed = false;
		if ( SQLUser.GLOBAL_PROJECT.equals(projectID) ||  SQLUser.NULL_PROJECT.equals(projectID) ) {
			allowed = this.isAllowedException(Role.WRITE);
		} else {
			allowed = this.myData.isAllowedForProject(Role.WRITE, projectID);
		}
		if ( allowed ) {
			this.removeFromProject(projectID, role);
			try {
				PreparedStatement aPsth = this.myData.prepareStatement(SQL_INSERT_GRANT);
				aPsth.setInt(1, permission);
				aPsth.setString(2, this.myID);
				aPsth.setString(3, projectID);
				aPsth.setString(4, role);
				aPsth.executeUpdate();
				aPsth.close();
			} catch (SQLException e) {
				throw new DataException(e);
			}
		}
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.User#setUserEmail(java.lang.String)
	 */
	public void setUserEmail(String anEmail) throws DataException {
		if ( this.myData.getUser().getUserID().equals(this.getID()) ) {
			this.myData.setString(USER_EMAIL, anEmail);
		} else {
			this.myData.setString(USER_EMAIL, anEmail);
		}
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.User#setUserName(java.lang.String)
	 */
	public void setUserName(String aName) throws DataException {
		if ( this.myData.getUser().getUserID().equals(this.getID()) ) {
			this.myData.setString(USER_NAME, aName);
		} else {
			this.myData.setString(USER_NAME, aName);
		}
	}
		
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.MutableUser#removeApplicationRole(java.lang.String)
	 */
	public void removeGlobalRole(String role) throws DataException {
		this.removeFromProject(SQLUser.GLOBAL_PROJECT, role);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.MutableUser#removeFromProject(java.lang.String, java.lang.String)
	 */
	public void removeFromProject(String projectID, String role) throws DataException {
		boolean allowed = false;
		if ( SQLUser.GLOBAL_PROJECT.equals(projectID) || SQLUser.NULL_PROJECT.equals(projectID) ) {
			allowed = this.isAllowedException(Role.WRITE);
		} else {
			allowed = this.myData.isAllowedForProject(Role.WRITE, projectID);
		}
		if ( allowed ) {
			try {
				PreparedStatement aPsth = this.myData.prepareStatement(REVOKE_USER_FROM_PROJECT_SQL);
				aPsth.setString(1, this.myID);
				aPsth.setString(2, role);
				aPsth.setString(3, projectID);
				aPsth.executeUpdate();
				aPsth.close();
			} catch (SQLException e) {
				throw new DataException(e);
			}
		}
	}

	public List<Role> globalRoles() throws DataException {
		return this.rolesForProject(SQLUser.GLOBAL_PROJECT);
	}

	public List<Role> rolesForProject(String projectID) throws DataException {
		List<Role> roleList = new ArrayList<Role>();
		try {
			PreparedStatement aPsth = this.myData.prepareStatement(SQL_GET_ROLES);
			aPsth.setString(1, this.myID);
			aPsth.setString(2, projectID);
			ResultSet aResult = aPsth.executeQuery();
			aResult.beforeFirst();
			while ( aResult.next() ) {
				Role role = new Role(aResult.getString(ROLE_NAME), aResult.getInt(ROLE_PERM));
				roleList.add(role);
			}
			aResult.close();
			aPsth.close();
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return roleList;
	}

	public Map<String, Role> roleMapForProject(String projectID) throws DataException {
		Map<String, Role> roleMap = new HashMap<String, Role>();
		try {
			PreparedStatement aPsth = this.myData.prepareStatement(SQL_GET_ROLES);
			aPsth.setString(1, this.myID);
			aPsth.setString(2, projectID);
			ResultSet aResult = aPsth.executeQuery();
			aResult.beforeFirst();
			while ( aResult.next() ) {
				Role role = new Role(aResult.getString(ROLE_NAME), aResult.getInt(ROLE_PERM));
				roleMap.put(role.roleName(), role);
			}
			aResult.close();
			aPsth.close();
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return roleMap;
	}

	public Map<String, Role> globalRoleMap() throws DataException {
		return this.roleMapForProject(SQLUser.GLOBAL_PROJECT);
	}
	
	public User asUser() throws DataException {
		return new SQLUser(this.myData.getDBC(), this.myID);
	}
	
	public void resetPassword(Session mailSession) throws DataException, AddressException, MessagingException {
		PreparedStatement psth = null;
		try {
			Message aMsg = new MimeMessage(mailSession);
			InternetAddress anEmail = new InternetAddress(this.getUserEmail());
// Originally set the From address as the same as user email.  
//  Now will utilized properties from context definition.
/*			try {
				anEmail.setPersonal("DO NOT REPLY");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			aMsg.setFrom(anEmail);
*/
			try {
				anEmail.setPersonal(this.getUserName());
			} catch (UnsupportedEncodingException e) {
				// NO BIG DEAL.
			}
			aMsg.setRecipient(Message.RecipientType.TO, anEmail);
			aMsg.setSubject("Database Password Reset");

			psth = this.prepareStatement("UPDATE users SET password=SHA1(?) WHERE username=?");
			Random random = new Random();
			int len = random.nextInt(4) + 6;
			char[] pwd = new char[len];
			for ( int i = 0; i < pwd.length; i++ ) {
				switch ( random.nextInt(3) ) {
				case 0: pwd[i] = (char)('a' + random.nextInt(27)); break;
				case 1: pwd[i] = (char)('A' + random.nextInt(27)); break;
				case 2: pwd[i] = (char)('0' + random.nextInt(10)); break;
				}
			}
			String randPwd = new String(pwd);
			psth.setString(1, randPwd);
			psth.setString(2, this.myID);
			if ( psth.executeUpdate() > 0 ) {
				String content = String.format("%s -\n\nYour password for the cyanos database has been reset.\n\tYour new password is: %s\nPlease change your password when you login.\n\n.  Do not reply to this message.", 
						this.getUserName(), randPwd);
				aMsg.setContent(content, "text/plain");
				Transport.send(aMsg);
			} else {
				throw new DataException("Could not reset password.");
			}
		} catch (SQLException e) {
			throw new DataException(e);
		}
		
	}

	private final static String SQL_CHECK_EMAIL = "SELECT fullname FROM users WHERE username=? AND email=?";
	
	public static void resetPassword(String urlBase, String userID, String email) throws SQLException, NamingException, MessagingException, DataException {
		PreparedStatement psth = null;
		Connection dbc = AppConfigListener.getDBConnection();
		Session mailSession = AppConfigListener.getMailSession();
		
		psth = dbc.prepareStatement(SQL_CHECK_EMAIL);
		psth.setString(1, userID);
		psth.setString(2, email);

		ResultSet results = psth.executeQuery();
		if ( ! results.first() ) {
			throw new DataException("Email does not match user account specified.");
		}
		String fullname = results.getString(1);
		results.close();
		psth.close();
		
		Message aMsg = new MimeMessage(mailSession);
		InternetAddress anEmail = new InternetAddress(email);

		try {
			anEmail.setPersonal(fullname);
		} catch (UnsupportedEncodingException e) {
			// NO BIG DEAL.
		}
		aMsg.setRecipient(Message.RecipientType.TO, anEmail);
		aMsg.setSubject("CYANOS Password Reset");

		psth = dbc.prepareStatement("UPDATE users SET password=MD5(?) WHERE username=?");
		Random random = new Random();
		int len = random.nextInt(4) + 6;
		char[] pwd = new char[len];
		for ( int i = 0; i < pwd.length; i++ ) {
			switch ( random.nextInt(3) ) {
			case 0: pwd[i] = (char)('a' + random.nextInt(27)); break;
			case 1: pwd[i] = (char)('A' + random.nextInt(27)); break;
			case 2: pwd[i] = (char)('0' + random.nextInt(10)); break;
			}
		}
		String randPwd = new String(pwd);
		psth.setString(1, randPwd);
		psth.setString(2, userID);
		if ( psth.executeUpdate() > 0 ) {
			StringBuffer content = new StringBuffer(fullname);
			content.append(" -\n\nA password reset has been requested for your user account.\n\tAccess the reset password page and use the following link to change your password.\n");
			content.append(urlBase);
			content.append("?username=");
			content.append(userID);
			content.append("&token=");
			content.append(randPwd);
			content.append("\n\n.  Do not reply to this message.");
			aMsg.setContent(content.toString(), "text/plain");
			Transport.send(aMsg);
		} else {
			psth.close();
			dbc.close();
			throw new DataException("Could not reset password.");
		}
		psth.close();
		dbc.close();
	}

	private final static String SQL_CHECK_TOKEN = "SELECT username FROM users WHERE username=? AND password=MD5(?)";

	public static void finishReset(String userID, String token, String password) throws SQLException, DataException {
		PreparedStatement psth = null;
		Connection dbc = AppConfigListener.getDBConnection();
		
		psth = dbc.prepareStatement(SQL_CHECK_TOKEN);
		psth.setString(1, userID);
		psth.setString(2, token);

		ResultSet results = psth.executeQuery();
		if ( ! results.first() ) {
			throw new DataException("Update token does not match user account specified.");
		}
		results.close();
		psth.close();
		
		psth = dbc.prepareStatement("UPDATE users SET password=SHA1(?) WHERE username=?");
		psth.setString(1, password);
		psth.setString(2, userID);
		if ( psth.executeUpdate() == 0 ) {
			psth.close();
			dbc.close();
			throw new DataException("Could not set password.");
		}
		psth.close();
		dbc.close();
	}
	
	private final static String SQL_SET_PASSWORD = "UPDATE users SET password=SHA1(?) WHERE username=?";
	private final static String SQL_SET_EMAIL = "UPDATE users SET email=? WHERE username=?";
	
	public static boolean updatePassword(HttpServletRequest req, String password) throws SQLException {
		if ( req.getRemoteUser() != null ) {
			Connection dbc = AppConfigListener.getDBConnection();
			PreparedStatement psth = dbc.prepareStatement(SQL_SET_PASSWORD, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			psth.setString(1, password);
			psth.setString(2, req.getRemoteUser());
			boolean update = psth.executeUpdate() == 1;
			psth.close();
			dbc.close();
			return update;
		} 
		return false;
	}
	
	public static void updateEmail(HttpServletRequest req, String email) throws SQLException {
		if ( req.getRemoteUser() != null ) {
			Connection dbc = AppConfigListener.getDBConnection();
			PreparedStatement psth = dbc.prepareStatement(SQL_SET_EMAIL, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			psth.setString(1, email);
			psth.setString(2, req.getRemoteUser());
			psth.executeUpdate();
			psth.close();
			dbc.close();
		} 
	}


}
