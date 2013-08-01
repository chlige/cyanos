/**
 * 
 */
package edu.uic.orjala.cyanos.sql;

import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import edu.uic.orjala.cyanos.BasicUser;
import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.Role;

/**
 * @author George Chlipala
 *
 */
public class SQLUser extends BasicUser {
	
	private Connection dbc = null;
	private ResultSet myData = null;
	private Set<Statement> statments = null;
	
	private final static String SQL_GET_ROLES = "SELECT username,project_id,role,perm FROM roles WHERE username=?";
	private final static String USER_SQL = "SELECT * FROM users WHERE username=?";
	
	private final static String USER_EMAIL = "email";
	private final static String USER_NAME = "fullname";
	
	private final static String ROLE_NAME = "role";
	private final static String ROLE_PROJECT = "project_id";
	private static final String ROLE_PERM = "perm";

	public SQLUser(Connection aDBC, String userID) throws DataException {
		this.dbc = aDBC;
		this.myID = userID;
		try {
			this.loadData();
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}
	
	public SQLUser(SQLData data, String userID) throws DataException {
		this.dbc = data.getDBC();
		this.myID = userID;
		try {
			this.loadData();
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

	
	private PreparedStatement prepareStatement(String sqlString) throws SQLException {
		return this.prepareStatement(sqlString, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
	}
	
	private PreparedStatement prepareStatement(String sqlString, int setType, int setConcurrency) throws SQLException {
		if ( this.dbc != null ) {
			if ( this.statments == null ) this.statments = new HashSet<Statement>();
			PreparedStatement aPsth = this.dbc.prepareStatement(sqlString, setType, setConcurrency);
			this.statments.add(aPsth);
			return aPsth;
		}
		return null;
	}
	
	protected void finalize() throws Throwable {
		try {
			this.close();
		} finally {
			super.finalize();
		}
	}
	
	private void closeData(ResultSet aData) throws SQLException {
		if ( aData != null ) {
			Statement aSth = aData.getStatement();
			this.statments.remove(aSth);
			aData.close();
			aSth.close();
		}
	}	
	
	private void loadData() throws DataException, SQLException {
			this.closeData(this.myData);
			PreparedStatement aPsth = this.prepareStatement(USER_SQL);
			aPsth.setString(1, this.myID);
			this.statments.add(aPsth);
			this.myData = aPsth.executeQuery();
			if ( this.myData.first() ) 
				this.loadRoles();
			else
				throw new DataException(String.format("User %s not found.", this.myID));
	}
		
	private void loadRoles() throws SQLException {
		PreparedStatement aPsth = this.prepareStatement(SQL_GET_ROLES);
		aPsth.setString(1, this.myID);
		ResultSet roleData = aPsth.executeQuery();

		roleData.beforeFirst();

		this.projectRoles.clear();
		
		while ( roleData.next() ) {
			String projectID = roleData.getString(ROLE_PROJECT);
			String roleName = roleData.getString(ROLE_NAME);
			Role role = new Role(roleName, roleData.getInt(ROLE_PERM));
			if ( ! this.projectRoles.containsKey(projectID) ) 
				this.projectRoles.put(projectID, new HashMap<String,Role>());
			Map<String,Role> thisProj = this.projectRoles.get(projectID);
			thisProj.put(roleName,role);
		}
		roleData.close();
		aPsth.close();
	}
	
	private String getString(ResultSet aData, String colName) throws DataException {
		try {
			if ( aData != null ) return aData.getString(colName);
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.User#getUserEmail()
	 */
	public String getUserEmail() throws DataException {
		return this.getString(this.myData, USER_EMAIL);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.User#getUserName()
	 */
	public String getUserName() throws DataException {
		return this.getString(this.myData, USER_NAME);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.User#getUserPreferences()
	 */
	public String getUserPreferences() throws DataException {
		// TODO Auto-generated method stub
		return null;
	}

	private void setString(ResultSet aData, String colName, String newValue) throws DataException {
		try { 
			if ( aData != null ) {
				aData.updateString(colName, newValue);
				aData.refreshRow();
			}
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.User#setUserEmail(java.lang.String)
	 */
	public void setUserEmail(String anEmail) throws DataException {
		this.setString(this.myData, USER_EMAIL, anEmail);

	}

	public void setUserName(String aName) throws DataException {
		this.setString(this.myData, USER_NAME, aName);
	}

	public static void resetPassword(SQLUser user, Session mailSession) throws DataException, AddressException, MessagingException {
		PreparedStatement psth = null;
		try {
			Message aMsg = new MimeMessage(mailSession);
			InternetAddress anEmail = new InternetAddress(user.getUserEmail());
//  Originally set the From address to be the same as the user's email address.
//  Now will utilize properties from definition in context.
/*
 			try {
				anEmail.setPersonal("DO NOT REPLY");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			aMsg.setFrom(anEmail);
*/
			try {
				anEmail.setPersonal(user.getUserName());
			} catch (UnsupportedEncodingException e) {
				// NO BIG DEAL.
			}
			aMsg.setRecipient(Message.RecipientType.TO, anEmail);
			aMsg.setSubject("Database Password Reset");

			psth = user.prepareStatement("UPDATE users SET password=SHA1(?) WHERE username=?");
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
			psth.setString(2, user.myID);
			if ( psth.executeUpdate() > 0 ) {
				String content = String.format("%s -\n\nYour password for the cyanos database has been reset.\n\tYour new password is: %s\nPlease change your password when you login.\n\n.  Do not reply to this message.", 
						user.getUserName(), randPwd);
				aMsg.setContent(content, "text/plain");
				Transport.send(aMsg);
			} else {
				throw new DataException("Could not reset password.");
			}
		} catch (SQLException e) {
			throw new DataException(e);
		}
		
	}


	public void close() throws DataException {
		try {
		if ( this.myData != null ) this.myData.close();
		if ( this.statments != null ) {
			Iterator<Statement> anIter = this.statments.iterator();
			while ( anIter.hasNext() ) {
				anIter.next().close();
			}
			this.statments.clear();
		}
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}
	
	public void closeAll() throws DataException {
		this.close();
		try {
			if ( this.dbc != null && (! this.dbc.isClosed()) )
				this.dbc.close();
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}
	
}
