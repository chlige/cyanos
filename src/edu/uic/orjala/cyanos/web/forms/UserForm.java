/**
 * 
 */
package edu.uic.orjala.cyanos.web.forms;

import java.sql.SQLException;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.naming.NamingException;

import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.MutableUser;
import edu.uic.orjala.cyanos.Project;
import edu.uic.orjala.cyanos.Role;
import edu.uic.orjala.cyanos.User;
import edu.uic.orjala.cyanos.sql.SQLMutableUser;
import edu.uic.orjala.cyanos.sql.SQLProject;
import edu.uic.orjala.cyanos.sql.SQLUser;
import edu.uic.orjala.cyanos.web.BaseForm;
import edu.uic.orjala.cyanos.web.CyanosWrapper;
import edu.uic.orjala.cyanos.web.html.Div;
import edu.uic.orjala.cyanos.web.html.Form;
import edu.uic.orjala.cyanos.web.html.Image;
import edu.uic.orjala.cyanos.web.html.Paragraph;
import edu.uic.orjala.cyanos.web.html.Table;
import edu.uic.orjala.cyanos.web.html.TableCell;
import edu.uic.orjala.cyanos.web.html.TableHeader;
import edu.uic.orjala.cyanos.web.html.TableRow;

/**
 * @author George Chlipala
 *
 */
public class UserForm extends BaseForm {

	/**
	 * @param aWrapper A CyanosWrapper object
	 */
	public UserForm(CyanosWrapper aWrapper) {
		super(aWrapper);
	}
	
	public String addUserForm() {
		TableRow myRow = new TableRow(this.makeFormTextRow("User ID:", "userID"));
		myRow.addItem(this.makeFormTextRow("FullName:", "name"));
		myRow.addItem(this.makeFormTextRow("Email:", "email"));
		Table myTable = new Table(myRow);
		myTable.setClass("species");
		Form userForm = new Form(myTable);
		
		// Set DIV for global roles.
		Div roleDiv = new Div();
		roleDiv.setClass("role");
		userForm.addItem(roleDiv);
		Image twist = this.getImage("twist-open.png");
		Div formDiv = new Div();
		formDiv.setID("div_::");
		formDiv.setClass("showSection");
		twist.setAttribute("BORDER", "0");
		twist.setAttribute("ID", "twist_::");
		twist.setAttribute("ALIGN", "absmiddle");
		roleDiv.addItem(String.format("<A NAME='::' onClick='loadDiv(\"::\")' CLASS='divSubtitle'>%s Global Roles</A>", twist.toString()));
		roleDiv.addItem(formDiv);

		// Setup form for role information
		myTable = new Table(this.formForRoles(":"));
		myTable.setClass("species");
		formDiv.addItem(myTable);

		// Set DIV for null project roles.
		roleDiv = new Div();
		roleDiv.setClass("role");
		userForm.addItem(roleDiv);
		twist = this.getImage("twist-open.png");
		formDiv = new Div();
		formDiv.setID("div_:");
		formDiv.setClass("showSection");
		twist.setAttribute("BORDER", "0");
		twist.setAttribute("ID", "twist_:");
		twist.setAttribute("ALIGN", "absmiddle");
		roleDiv.addItem(String.format("<A NAME=':' onClick='loadDiv(\":\")' CLASS='divSubtitle'>%s Null Project Roles</A>", twist.toString()));
		roleDiv.addItem(formDiv);

		// Setup form for role information
		myTable = new Table(this.formForRoles(""));
		myTable.setClass("species");
		formDiv.addItem(myTable);
		
		try {
			Project projects = SQLProject.projects(this.getSQLDataSource(), SQLProject.ID_COLUMN, SQLProject.ASCENDING_SORT);
			projects.beforeFirst();
			while ( projects.next() ) {
				// Set DIV for project roles.
				roleDiv = new Div();
				roleDiv.setClass("role");
				userForm.addItem(roleDiv);
				twist = this.getImage("twist-open.png");
				formDiv = new Div();
				formDiv.setID(String.format("div_:%s", projects.getID()));
				formDiv.setClass("showSection");
				twist.setAttribute("BORDER", "0");
				twist.setAttribute("ID", String.format("twist_:%s", projects.getID()));
				twist.setAttribute("ALIGN", "absmiddle");
				roleDiv.addItem(String.format("<A NAME=':%s' onClick='loadDiv(\":%s\")' CLASS='divSubtitle'>%s %s</A>", 
						projects.getID(), projects.getID(), twist.toString(), projects.getName()));
				roleDiv.addItem(formDiv);

				// Setup form for role information
				myTable = new Table(this.formForRoles(projects.getID()));
				myTable.setClass("species");
				formDiv.addItem(myTable);
			}
		} catch (DataException e) {
			formDiv.addItem(this.handleException(e));
		}
		userForm.addItem("<P ALIGN='CENTER'><BUTTON TYPE='submit' NAME='addUser'>Add User</BUTTON><BUTTON TYPE='reset'>Reset Form</BUTTON></P>");
		return userForm.toString();

	}

	private TableRow formForRoles(String prefix) {
		String[] roleHeaders = { "Role", "Read", "Write", "Delete", "Create" };
		TableCell myCell = new TableHeader(roleHeaders);
		TableRow myRow = new TableRow(myCell);
		for ( int i = 0; i < User.ROLES.length; i++ ) {
				myRow.addItem(this.settingsForRole(prefix, User.ROLES[i]));
		}
		return myRow;
	}
	
	private TableRow formForRoles(Map<String,Role> roleMap) {
		String[] roleHeaders = { "Role", "Read", "Write", "Delete", "Create" };
		TableCell myCell = new TableHeader(roleHeaders);
		TableRow myRow = new TableRow(myCell);
		for ( int i = 0; i < User.ROLES.length; i++ ) {
			if ( roleMap.containsKey(User.ROLES[i])) {
				myRow.addItem(this.settingsForRole(roleMap.get(User.ROLES[i])));
			} else {
				myRow.addItem(this.settingsForRole(User.ROLES[i]));
			}
		}
		myRow.addItem("<TD COLSPAN=5 ALIGN='CENTER'><BUTTON TYPE='submit' NAME='modRoles'>Modify</BUTTON><BUTTON TYPE='reset'>Reset</BUTTON></TD>");		
		return myRow;
	}
	
	private TableCell settingsForRole(Role aRole) {
		TableCell myCell = new TableCell(aRole.roleName());
		int[] roles = { Role.READ, Role.WRITE, Role.DELETE, Role.CREATE };
		for ( int i = 0; i < roles.length; i++ ) {
			if ( aRole.hasPermission(roles[i]) ) {
				myCell.addItem(String.format("<INPUT TYPE='checkbox' NAME='%s' VALUE='%d' CHECKED />", aRole.roleName(), roles[i]));
			} else {
				myCell.addItem(String.format("<INPUT TYPE='checkbox' NAME='%s' VALUE='%d' />", aRole.roleName(), roles[i]));
			}
		}
		return myCell;
	}
	
	private TableCell settingsForRole(String prefix, String aRole) {
		TableCell myCell = new TableCell(aRole);
		int[] roles = { Role.READ, Role.WRITE, Role.DELETE, Role.CREATE };
		for ( int i = 0; i < roles.length; i++ ) {
			myCell.addItem(String.format("<INPUT TYPE='checkbox' NAME='%s:%s' VALUE='%d' />", prefix, aRole, roles[i]));
		}
		return myCell;
	}


	private TableCell settingsForRole(String aRole) {
		TableCell myCell = new TableCell(aRole);
		int[] roles = { Role.READ, Role.WRITE, Role.DELETE, Role.CREATE };
		for ( int i = 0; i < roles.length; i++ ) {
				myCell.addItem(String.format("<INPUT TYPE='checkbox' NAME='%s' VALUE='%d' />", aRole, roles[i]));
		}
		return myCell;
	}
	
	public String userForm() {
		String selUser = this.getFormValue("user");
		Div mainDiv = new Div();

		mainDiv.addItem(this.processForm());

		try {
			MutableUser myUsers = SQLMutableUser.users(this.getSQLDataSource());
			myUsers.beforeFirst();
			Project projects = SQLProject.projects(this.getSQLDataSource(), SQLProject.ID_COLUMN, SQLProject.ASCENDING_SORT);
			while ( myUsers.next() ) {
				String user = myUsers.getUserID();
				boolean selected = user.equals(selUser);

				// Setup DIV for user information 
				Div userDiv = new Div();
				userDiv.setClass("user");
				mainDiv.addItem(userDiv);
				Image twist = this.getImage("twist-closed.png");
				Div infoDiv = new Div();

				if ( selected ) {
					twist = this.getImage("twist-open.png");
					infoDiv.setClass("showSection");
				} else {
					infoDiv.setClass("hideSection");
				}

				twist.setAttribute("BORDER", "0");
				twist.setAttribute("ALIGN", "absmiddle");
				twist.setAttribute("ID", "twist_" + user);
				infoDiv.setID(String.format("div_%s", user));
				userDiv.addItem(String.format("<A NAME='%s' onClick='loadDiv(\"%s\")' CLASS='divTitle'>%s %s</A>", user, user, twist.toString(), myUsers.getUserName()));
				userDiv.addItem(infoDiv);

				// Setup DIV for user form information

				TableCell myCell = new TableCell("User ID:");
				myCell.addItem(user);
				TableRow myRow = new TableRow(myCell);
				myRow.addItem(this.makeFormTextRow("FullName:", "name", myUsers.getUserName()));
				myRow.addItem(this.makeFormTextRow("Email:", "email", myUsers.getUserEmail()));
				myRow.addItem("<TD COLSPAN=2 ALIGN='CENTER'><BUTTON TYPE='submit' NAME='modUser'>Modify</BUTTON><BUTTON TYPE='reset'>Reset Form</BUTTON></TD>");
				myRow.addItem("<TD COLSPAN=2></TD>");
				myRow.addItem("<TD COLSPAN=2 ALIGN='CENTER'><BUTTON TYPE='submit' NAME='deleteUser'>Delete User</BUTTON><BUTTON TYPE='submit' NAME='resetUser'>Reset Password</BUTTON></TD>");
				Table myTable = new Table(myRow);
				myTable.setClass("species");
				Form userForm = new Form(myTable);
				userForm.addHiddenValue("user", user);
				userForm.setPost();
				infoDiv.addItem(userForm);

				// Set DIV for global roles.
				Div roleDiv = new Div();
				roleDiv.setClass("role");
				infoDiv.addItem(roleDiv);
				twist = this.getImage("twist-closed.png");
				Div formDiv = new Div();
				formDiv.setID(String.format("div_%s::", user));
				formDiv.setClass("hideSection");
				if ( this.hasFormValue("globalProject") ) {
					twist = this.getImage("twist-open.png");
					formDiv.setClass("showSection");
				}
				twist.setAttribute("BORDER", "0");
				twist.setAttribute("ID", String.format("twist_%s::", user));
				twist.setAttribute("ALIGN", "absmiddle");
				roleDiv.addItem(String.format("<A NAME='%s::' onClick='loadDiv(\"%s::\")' CLASS='divSubtitle'>%s Global Roles</A>", user, user, twist.toString()));
				roleDiv.addItem(formDiv);

				// Setup form for role information
				myTable = new Table(this.formForRoles(myUsers.globalRoleMap()));
				myTable.setClass("species");
				Form roleForm = new Form(myTable);
				roleForm.addHiddenValue("user", user);
				roleForm.addHiddenValue("globalProject", "1");
				roleForm.setPost();
				formDiv.addItem(roleForm);
				
				// Set DIV for null project roles.
				roleDiv = new Div();
				roleDiv.setClass("role");
				infoDiv.addItem(roleDiv);
				twist = this.getImage("twist-closed.png");
				formDiv = new Div();
				formDiv.setID(String.format("div_%s:", user));
				formDiv.setClass("hideSection");
				if ( this.hasFormValue("project_code") && this.getFormValue("project_code").equals(SQLUser.NULL_PROJECT) ) {
					twist = this.getImage("twist-open.png");
					formDiv.setClass("showSection");
				}
				twist.setAttribute("BORDER", "0");
				twist.setAttribute("ID", String.format("twist_%s:", user));
				twist.setAttribute("ALIGN", "absmiddle");
				roleDiv.addItem(String.format("<A NAME='%s:' onClick='loadDiv(\"%s:\")' CLASS='divSubtitle'>%s Null Project Roles</A>", user, user, twist.toString()));
				roleDiv.addItem(formDiv);

				// Setup form for role information
				myTable = new Table(this.formForRoles(myUsers.roleMapForProject(SQLUser.NULL_PROJECT)));
				myTable.setClass("species");
				roleForm = new Form(myTable);
				roleForm.addHiddenValue("user", user);
				roleForm.addHiddenValue("project_code", SQLUser.NULL_PROJECT);
				roleForm.setPost();
				formDiv.addItem(roleForm);

				projects.beforeFirst();
				while ( projects.next() ) {
					roleDiv = new Div();
					roleDiv.setClass("role");
					infoDiv.addItem(roleDiv);
					twist = this.getImage("twist-closed.png");
					formDiv = new Div();
					String projectID = projects.getID();

					if ( this.hasFormValue("project_code") && this.getFormValue("project_code").equals(projects.getID()) ) {
						twist = this.getImage("twist-open.png");
						formDiv.setClass("showSection");
					}
					formDiv.setID(String.format("div_%s:%s", user, projectID));
					formDiv.setClass("hideSection");
					twist.setAttribute("BORDER", "0");
					twist.setAttribute("ID", String.format("twist_%s:%s", user, projectID));
					twist.setAttribute("ALIGN", "absmiddle");
					roleDiv.addItem(String.format("<A NAME='%s:%s' onClick='loadDiv(\"%s:%s\")' CLASS='divSubtitle'>%s %s</A>", user, projectID,
							user, projectID, twist.toString(), projects.getName()));
					roleDiv.addItem(formDiv);
					
					// Setup form for role information
					myTable = new Table(this.formForRoles(myUsers.roleMapForProject(projectID)));
					myTable.setClass("species");
					roleForm = new Form(myTable);
					roleForm.addHiddenValue("user", user);
					roleForm.addHiddenValue("project_code", projects.getID());
					roleForm.setPost();
					formDiv.addItem(roleForm);

				}
			}
		} catch ( DataException ex ) {
			mainDiv.addItem("<P ALIGN='CENTER'>" + this.handleException(ex) + "</P>");
		}
		return mainDiv.toString();

	}
	
	private String processForm() {
		try {
			if ( this.hasFormValue("modUser") ) {
				MutableUser aUser = new SQLMutableUser(this.getSQLDataSource(), this.getFormValue("user"));
				if ( aUser.first() ) {
					aUser.setUserName(this.getFormValue("name"));
					aUser.setUserEmail(this.getFormValue("email"));
					return this.message(SUCCESS_TAG, "Updated user information.").toString();
				}
			} else if ( this.hasFormValue("modRoles") ) {
				MutableUser aUser = new SQLMutableUser(this.getSQLDataSource(), this.getFormValue("user"));
				for ( int r = 0; r < User.ROLES.length; r++ ) {
					if ( this.hasFormValue(User.ROLES[r]) ) {
						String[] perms = this.getFormValues(User.ROLES[r]);
						int permBit = 0;
						for ( int p = 0; p < perms.length; p++ ) {
							permBit += Integer.parseInt(perms[p]);
						}
						if ( this.hasFormValue("globalProject") ) {
							aUser.grantGlobalPermission(User.ROLES[r], permBit);
						} else {
							aUser.grantPermissionForProject(this.getFormValue("project_code"), User.ROLES[r], permBit);
						}
					} else {
						if ( this.hasFormValue("globalProject") ) {
							aUser.removeGlobalRole(User.ROLES[r]);
						} else {
							aUser.removeFromProject(this.getFormValue("project_code"), User.ROLES[r]);
						}
					}
				}
				return this.message(SUCCESS_TAG, "Updated user information.").toString();
			} else if ( this.hasFormValue("deleteUser") ) {
				if ( this.getUser().hasGlobalPermission(User.ADMIN_ROLE, Role.DELETE)) {
					if ( this.hasFormValue("confirmDelete") ) {
						String userName = this.getFormValue("user");
						SQLMutableUser.deleteUser(this.getSQLDataSource(), userName);
						Div messageDiv = this.messageDiv(SUCCESS_TAG, String.format("Deleted user ID: %s", userName));
						return messageDiv.toString();
					} else if ( this.hasFormValue("cancelDelete") ) {
						return "";
					} else {
						MutableUser aUser = new SQLMutableUser(this.getSQLDataSource(), this.getFormValue("user"));
						aUser.first();
						Form delForm = new Form("<P ALIGN='CENTER'>");
						delForm.addItem(WARNING_TAG);
						delForm.addItem(String.format("Confirm user deletion: %s (%s)<BR/>", aUser.getUserName(), aUser.getUserID() ));
						delForm.addItem("<BUTTON TYPE='submit' NAME='confirmDelete'>Delete</BUTTON><BUTTON TYPE='submit' NAME='cancelDelete'>Cancel</BUTTON></P>");
						delForm.addHiddenValue("deleteUser", "1");
						delForm.addHiddenValue("user", aUser.getUserID());
						delForm.setPost();
						Div messageDiv = new Div(delForm);
						messageDiv.setClass("messages");
						return messageDiv.toString();
					}
				} else {
					return this.message(FAILED_TAG, "Insufficient permission to delete user.").toString();
				}
			} else if ( this.hasFormValue("resetUser") ) {
				MutableUser aUser = new SQLMutableUser(this.getSQLDataSource(), this.getFormValue("user"));
				return this.resetUser(aUser.asUser());
			}
		} catch ( DataException ex ) {
			return this.handleException(ex);
		}	
		return "";
	}

	public String resetUser(User aUser) {
		String output = null;
		try {
			Div messageDiv = new Div();
			SQLUser.resetPassword((SQLUser)aUser, this.myWrapper.getMailSession());
			messageDiv.addItem("<P ALIGN='CENTER'><B><FONT COLOR='green'>SUCCESS:</FONT> Password reset.</B></P>");
			messageDiv.setClass("messages");	
			output = messageDiv.toString();
		} catch (DataException e) {
			output = this.handleException(e);
		} catch (NamingException e) {
			output = this.handleException(e);
		} catch (AddressException e) {
			output = this.handleException(e);
		} catch (MessagingException e) {
			output = this.handleException(e);
		}		
		return output;
	}

	public String resetForm() {
		TableCell myCell = new TableHeader("<FONT SIZE='+1'>Reset Password</FONT>");
		myCell.setAttribute("align","center");
		myCell.setAttribute("colspan","2");
		TableRow tableRow = new TableRow(myCell);
		tableRow.addItem(this.makeFormTextRow("User ID:", "user"));
		tableRow.addItem("<TD COLSPAN=2 ALIGN='CENTER'><INPUT TYPE='SUBMIT' NAME='resetUser' VALUE='Reset Password'></TD>");
		Table myTable = new Table(tableRow);
		Paragraph aPar = new Paragraph(myTable);
		aPar.setAttribute("ALIGN", "CENTER");
		Form myForm = new Form(aPar);
		myForm.setAttribute("METHOD", "POST");
		return myForm.toString();
	}
	
	public String selfChangePasswordForm() {
		TableCell myCell = new TableHeader("<FONT SIZE=\"+1\">Change Password</FONT>");
		myCell.setAttribute("align","center");
		myCell.setAttribute("colspan","2");
		TableRow tableRow = new TableRow(myCell);
		
		if ( this.hasFormValue("changePwd") ) {
			String passwd = this.getFormValue("newPwd1");
			if ( passwd.equals(this.getFormValue("newPwd2"))) {
				try {
					if ( this.myWrapper.setMyPassword(passwd) ) {
						tableRow.addItem("<TD COLSPAN=2><B><FONT COLOR='green'>SUCCESS:</FONT> Password changed!</B></TD>");
						Table myTable = new Table(tableRow);
						myTable.setClass("species");
						myTable.setAttribute("align", "center");
						return myTable.toString();
					} else {
						tableRow.addItem("<TD COLSPAN=2><B><FONT COLOR='red'>FAILED:</FONT> Cannot change password!</B></TD>");
					}
				} catch (SQLException e) {
					tableRow.addItem(String.format("<TD COLSPAN=2>%s</TD>", this.handleException(e)));
				}
			}
		}

		myCell = new TableCell("User ID:");
		myCell.addItem(this.myWrapper.getRemoteUser());
		tableRow.addItem(myCell);

		myCell = new TableCell("Old Password:");
		myCell.addItem("<INPUT TYPE=PASSWORD NAME=\"curPwd\" SIZE=20 />");
		tableRow.addItem(myCell);

		myCell = new TableCell("New Password:");
		myCell.addItem("<INPUT TYPE=PASSWORD NAME=\"newPwd1\" SIZE=20 />");
		tableRow.addItem(myCell);

		myCell = new TableCell("Retype New Password:");
		myCell.addItem("<INPUT TYPE=PASSWORD NAME=\"newPwd2\" SIZE=20 />");
		tableRow.addItem(myCell);

		myCell = new TableCell("<INPUT TYPE=SUBMIT NAME=\"changePwd\" VALUE=\"Update\"/><INPUT TYPE=\"RESET\"/>");
		myCell.setAttribute("colspan","2");
		myCell.setAttribute("align","center");
		tableRow.addItem(myCell);

		Table myTable = new Table(tableRow);
		myTable.setClass("species");
		myTable.setAttribute("align", "center");

		Form pwdForm = new Form(myTable);
		pwdForm.setAttribute("METHOD","POST");
		return pwdForm.toString();
	}

	public String resetUser(String userID) {
		String output = null;
		try {
			Div messageDiv = new Div();
			SQLUser.resetPassword(new SQLUser(this.getSQLDataSource(), userID), this.myWrapper.getMailSession());
			messageDiv.addItem("<P ALIGN='CENTER'><B><FONT COLOR='green'>SUCCESS:</FONT> Password reset.</B></P>");
			messageDiv.setClass("messages");		
		} catch (DataException e) {
			output = this.handleException(e);
		} catch (NamingException e) {
			output = this.handleException(e);
		} catch (AddressException e) {
			output = this.handleException(e);
		} catch (MessagingException e) {
			output = this.handleException(e);
		}		
		return output;
	}

	public String grantPerms(MutableUser aUser, String project, String prefix) throws DataException {
		StringBuffer output = new StringBuffer();
		for ( int r = 0; r < User.ROLES.length; r++ ) {
			if ( this.hasFormValue(prefix + User.ROLES[r]) ) {
				String[] perms = this.getFormValues(prefix + User.ROLES[r]);
				int permBit = 0;
				for ( int p = 0; p < perms.length; p++ ) {
					permBit += Integer.parseInt(perms[p]);
				}
				aUser.grantPermissionForProject(project, User.ROLES[r], permBit);
				output.append(String.format("%s (%d) ", User.ROLES[r], permBit));
			} else {
				aUser.removeFromProject(project, User.ROLES[r]);
				output.append(String.format("%s (NONE) ", User.ROLES[r]));
			}
		}
		return output.toString();
	}

}
