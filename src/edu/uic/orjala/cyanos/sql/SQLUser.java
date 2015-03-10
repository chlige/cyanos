/**
 * 
 */
package edu.uic.orjala.cyanos.sql;

import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

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

	private final static String SQL_GET_ROLES = "SELECT username,project_id,role,perm FROM roles WHERE username=?";
	private final static String USER_SQL = "SELECT username,fullname,email FROM users WHERE username=?";	
	private final static String SQL_VALIDATE_PASSWORD = "SELECT username FROM users WHERE username=? AND password=SHA(?)";

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

	@Override
	public boolean checkPassword(String password) throws DataException {
		try {
			PreparedStatement sth = this.dbc.prepareStatement(SQL_VALIDATE_PASSWORD);
			sth.setString(1, this.myID);
			sth.setString(2, password);
			ResultSet results = sth.executeQuery();
			boolean check = results.first();
			results.close();
			sth.close();
			return check;
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}
	
	protected void finalize() throws Throwable {
		try {
			this.closeAll();
		} finally {
			super.finalize();
		}
	}
	
	private void loadData() throws DataException, SQLException {
			this.reload();
			this.loadRoles();
	}
	
	@Override
	public void reload() throws DataException {
		try {
			PreparedStatement aPsth = this.dbc.prepareStatement(USER_SQL);
			aPsth.setString(1, this.myID);
			ResultSet results = aPsth.executeQuery();
			boolean exists = results.first();
			if ( exists ) {
				this.fullname = results.getString(2);
				this.email = results.getString(3);
			}
			results.close();
			aPsth.close();
			if ( ! exists ) throw new DataException(String.format("User %s not found.", this.myID));
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}
		
	private void loadRoles() throws SQLException {
		PreparedStatement aPsth = this.dbc.prepareStatement(SQL_GET_ROLES);
		aPsth.setString(1, this.myID);
		ResultSet roleData = aPsth.executeQuery();

		roleData.beforeFirst();

		this.projectRoles.clear();
		
		while ( roleData.next() ) {
			String projectID = roleData.getString(2);
			String roleName = roleData.getString(3);
			Role role = new Role(roleName, roleData.getInt(4));
			if ( ! this.projectRoles.containsKey(projectID) ) 
				this.projectRoles.put(projectID, new HashMap<String,Role>());
			Map<String,Role> thisProj = this.projectRoles.get(projectID);
			thisProj.put(roleName,role);
		}
		roleData.close();
		aPsth.close();
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.User#getUserPreferences()
	 */
	public String getUserPreferences() throws DataException {
		// TODO Auto-generated method stub
		return null;
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

			psth = user.dbc.prepareStatement("UPDATE users SET password=SHA1(?) WHERE username=?");
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

	public void closeAll() throws DataException {
		try {
			if ( this.dbc != null && (! this.dbc.isClosed()) )
//				System.out.format("SQLUser: DB Connection CLOSE: %d\n", dbc.hashCode());
				this.dbc.close();
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}	
}
