/**
 * 
 */
package edu.uic.orjala.cyanos.web;

import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.servlet.http.HttpServletRequest;

import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.MutableUser;
import edu.uic.orjala.cyanos.Project;
import edu.uic.orjala.cyanos.Role;
import edu.uic.orjala.cyanos.User;
import edu.uic.orjala.cyanos.sql.SQLMutableUser;
import edu.uic.orjala.cyanos.sql.SQLProject;
import edu.uic.orjala.cyanos.sql.SQLUser;
import edu.uic.orjala.cyanos.web.forms.ConfigForm;
import edu.uic.orjala.cyanos.web.forms.UserForm;
import edu.uic.orjala.cyanos.web.html.Form;
import edu.uic.orjala.cyanos.web.html.HtmlList;
import edu.uic.orjala.cyanos.web.html.Paragraph;
import edu.uic.orjala.cyanos.web.html.StyledText;
import edu.uic.orjala.cyanos.web.html.Table;
import edu.uic.orjala.cyanos.web.html.TableCell;
import edu.uic.orjala.cyanos.web.html.TableHeader;
import edu.uic.orjala.cyanos.web.html.TableRow;


/**
 * @author George Chlipala
 * 
 */
public class AdminServlet extends ServletObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1729910187898214608L;
	
	String[] roleList = { "culture", "cryo", "assay", "admin", "sample", "project" };
	
	public void display(CyanosWrapper aWrap) throws Exception {

		PrintWriter out = aWrap.startHTMLDoc("Administration");		
		HttpServletRequest req = aWrap.getRequest();
		String module = req.getPathInfo();

		String servlet = req.getServletPath();

		if ( servlet.equals("/self") ) {
			UserForm aForm = new UserForm(aWrap);
			out.println("<P ALIGN=\"CENTER\"><FONT SIZE=+3>Account Management</FONT><HR WIDTH=\"85%\"/>");
			if ( module == null || module.equals("/") ) {
				HtmlList formList = new HtmlList();
				formList.unordered();
				formList.setAttribute("type", "none");
				formList.addItem("<LI><A HREF='self/profile'>Profile Management</A></LI>");
				formList.addItem("<LI><A HREF='self/password'>Change Password</A></LI>");
				out.println(formList.toString());
			} else if ( module.equals("/profile") ) {
				out.println("");
			} else if ( module.equals("/password") ) {
				out.println(aForm.selfChangePasswordForm());
			}			
		} else if ( servlet.equals("/reset")) {
			UserForm aForm = new UserForm(aWrap);
			if ( aWrap.hasFormValue("resetUser")) {
				SQLUser aUser = new SQLUser(aWrap.getSQLDataSource(), aWrap.getFormValue("user"));
				out.println(aForm.resetUser(aUser));
				aUser.closeAll();
			} else {
				out.println(aForm.resetForm());
				out.println("<P ALIGN='CENTER'><A HREF='main'>Return to main page</A></P>");
			}
		} else {
			if ( module == null || module.equals("/") ) {
				out.println("<P ALIGN=\"CENTER\"><FONT SIZE=+3>Application Administration</FONT><HR WIDTH=\"85%\"/>");
				HtmlList formList = new HtmlList();
				formList.unordered();
				formList.setAttribute("type", "none");
				formList.addItem("<LI><A HREF='admin/user'>User Administration</A></LI>");
				formList.addItem("<LI><A HREF='admin/config'>Configuration Management</A></LI>");
				out.println(formList.toString());
			} else if ( module.equals("/config") ) {
				ConfigForm aForm = new ConfigForm(aWrap);
				out.println(aForm.manageConfig());
			} else if ( module.equals("/user") ) {
				out.println(this.manageUser(aWrap));
			} else if ( module.equals("/user/add") ) {
				out.println(this.addUser(aWrap));
			} else if ( module.equals("/news") ) {
				out.println(this.manageNews(aWrap));
			}
		}
		
		aWrap.finishHTMLDoc();
	}
	
	private String addUser(CyanosWrapper aWrap) {
		StringBuffer output = new StringBuffer();
		Paragraph head = new Paragraph();
		head.setAlign("CENTER");
		StyledText title = new StyledText("User Management");
		title.setSize("+3");
		head.addItem(title);
		head.addItem("<HR WIDTH='85%'/>");
		output.append(head);
		
		UserForm aForm = new UserForm(aWrap);
		
		try {
			if ( ! aWrap.getUser().hasGlobalPermission(User.ADMIN_ROLE, Role.CREATE) ) {
				output.append("<P ALIGN='CENTER'>");
				output.append(BaseForm.FAILED_TAG);
				output.append("Insufficient Permission<BR/><A HREF='../../user'>Return to User Manager</A></P>");
			}
			if ( aWrap.hasFormValue("addUser")) {
				MutableUser aUser = SQLMutableUser.createUser(aWrap.getSQLDataSource(), aWrap.getFormValue("userID"));
				aUser.setUserName(aWrap.getFormValue("name"));
				aUser.setUserEmail(aWrap.getFormValue("email"));
				output.append("<P ALIGN='CENTER'><B><FONT COLOR='green'>SUCCESS: </FONT>New user added</B></P>");
				
				output.append("<P ALIGN='CENTER'><H2>Roles</H2></P>");
				output.append("<HR WIDTH='25%'/><P ALIGN='CENTER'>Global Roles: ");
				output.append(aForm.grantPerms(aUser, SQLUser.GLOBAL_PROJECT, "::"));
				output.append("<P ALIGN='CENTER'>Null Project Roles: ");				
				output.append(aForm.grantPerms(aUser, SQLUser.NULL_PROJECT, ":"));
				
				Project projects = SQLProject.projects(aWrap.getSQLDataSource(), SQLProject.ID_COLUMN, SQLProject.ASCENDING_SORT);
				projects.beforeFirst();
				while ( projects.next() ) {
					output.append("<P ALIGN='CENTER'>");			
					output.append(projects.getID());
					output.append(": ");
					String prefix = new String(projects.getID() + ":");
					output.append(aForm.grantPerms(aUser, projects.getID(), prefix));
				}
				output.append(aForm.resetUser(aWrap.getFormValue("userID")));
			} else {
				output.append(aForm.addUserForm());
			}
		} catch (DataException e) {
			output.append("<P ALIGN='CENTER'>");
			output.append(aWrap.handleException(e));
			output.append("</P>");
		}

		return output.toString();
	}
	
	private String manageUser(CyanosWrapper aWrap) {
		StringBuffer output = new StringBuffer();
		Paragraph head = new Paragraph();
		head.setAlign("CENTER");
		StyledText title = new StyledText("User Management");
		title.setSize("+3");
		head.addItem(title);
		head.addItem("<HR WIDTH='85%'/>");
		output.append(head);

		try {
			if ( aWrap.getUser().isAllowed(User.ADMIN_ROLE, User.GLOBAL_PROJECT, Role.CREATE))
				output.append("<P ALIGN='center'><A HREF='user/add'>Add a New User</A></P>");
		} catch (DataException e) {
			this.log("Failed to retrieve current user.", e);
		}

		UserForm aForm = new UserForm(aWrap);
		output.append(aForm.userForm());
		
		return output.toString();
	}
	
	private String manageNews(CyanosWrapper aWrap) {
		String[] headers = { "", "Date Added", "Expires", "Subject", "Body"};
		TableCell aCell = new TableHeader(headers);
		TableRow aRow = new TableRow(aCell);
		Table newsTable = new Table(aRow);
		
		try {
			if ( aWrap.hasFormValue("addNews") ) {
				News.create(aWrap.getSQLDataSource(), aWrap.getFormValue("expires"), aWrap.getFormValue("subject"), aWrap.getFormValue("content"));
			}

			News newsList = News.allNews(aWrap.getSQLDataSource());
			
			
			newsList.beforeFirst();
			SimpleDateFormat aFormat = aWrap.dateTimeFormat();

			String currentItem = null;
			
			if ( aWrap.hasFormValue("item") && (! aWrap.hasFormValue("returnAction"))) {
				currentItem = aWrap.getFormValue("item");
			}
			while ( newsList.next() ) {
				boolean formRow = false;
				if ( newsList.getID().equals(currentItem) ) {
					if ( aWrap.hasFormValue("changeAction") ) {
						try {
							newsList.setExpiration(aFormat.parse(aWrap.getFormValue("expires")));
						} catch (ParseException e) {
							newsTable.addItem("<TR><TD ALIGN='CENTER' COLSPAN='4'><B><FONT COLOR='red'>Error:</FONT> " + e.getMessage() + "</B></TD></TR>");
						}
						newsList.setContent(aWrap.getFormValue("content"));
						newsList.setSubject(aWrap.getFormValue("subject"));
					} else 
						formRow = true;
				}
				
				if ( formRow ) {
					aCell = new TableCell(String.format("<INPUT TYPE='RADIO' NAME='item' VALUE='%s' CHECKED/>", newsList.getID()));
					aCell.addItem(aFormat.format(newsList.getDateAdded()));
					aCell.addItem(String.format("<INPUT TYPE='TEXT' NAME='expires' VALUE='%s'/>", aFormat.format(newsList.getExpiration())));
					aCell.addItem(String.format("<INPUT TYPE='TEXT' NAME='subject' VALUE='%s'/>", newsList.getSubject()));
					aCell.addItem(String.format("<TEXTAREA NAME='content' ROWS=4 COLS=80>%s</TEXTAREA>", newsList.getContent()));
					aRow.addItem(aCell);
					aRow.addItem("<TD COLSPAN=5 ALIGN='CENTER'><INPUT TYPE='SUBMIT' NAME='returnAction' VALUE='Return'/><INPUT TYPE='SUBMIT' NAME='changeAction' VALUE='Update'/></TD>");
				} else {
					aCell = new TableCell(String.format("<INPUT TYPE='RADIO' NAME='item' VALUE='%s' onClick='this.form.submit()'/>", newsList.getID()));
					aCell.addItem(aFormat.format(newsList.getDateAdded()));
					aCell.addItem(aFormat.format(newsList.getExpiration()));
					aCell.addItem(newsList.getSubject());
					aCell.addItem(newsList.getContent().replaceAll("\n", "<BR>"));
					aRow.addItem(aCell);
				}
			}
		} catch (DataException e) {
			newsTable.addItem("<TR><TD ALIGN='CENTER' COLSPAN='4'><B><FONT COLOR='red'>Error:</FONT> " + e.getMessage() + "</B></TD></TR>");
		}
		Form aForm = new Form("<P ALIGN='CENTER'><FONT SIZE='+2'>News</FONT><HR WIDTH='80%'/>");
		aForm.setAttribute("METHOD", "POST");
		aForm.addItem(newsTable);
		aForm.addItem("</P>");
		
		StringBuffer output = new StringBuffer();
		output.append(aForm.toString());
		if ( aWrap.hasFormValue("addForm")) {
			aForm = new Form("<P ALIGN='CENTER'><FONT SIZE='+1'><B>Create a news item</B></FONT></BR>");
			aForm.setAttribute("METHOD", "POST");
			aCell = new TableCell("Subject:");
			aCell.addItem("<INPUT TYPE='TEXT' NAME='subject'/>");
			aRow = new TableRow(aCell);

			aCell = new TableCell("Expires:");
			aCell.addItem("<INPUT TYPE='TEXT' NAME='expires'/> (YYYY-MM-DD HH:MM:SS)");
			aRow.addItem(aCell);
			
			aRow.addItem("<TD COLSPAN='2' ALIGN='CENTER'><TEXTAREA NAME='content' ROWS=4 COLS=80></TEXTAREA></TD>");
			aRow.addItem("<TD COLSPAN='2' ALIGN='CENTER'><INPUT TYPE='SUBMIT' NAME='addNews' VALUE='Create'/></TD>");			
			
			Table aTable = new Table(aRow);
			aForm.addItem(aTable);
			aForm.addItem("</P>");
			output.append(aForm);
		} else {
			output.append("<FORM METHOD='POST'><P ALIGN='CENTER'><INPUT TYPE='SUBMIT' NAME='addForm' VALUE='Create'/></P></FORM>");			
		}
		
		return output.toString();
		
	}
	
	/*
	private String testDB(String jndiName) {
		StringBuffer output = new StringBuffer();
		output.append("<P ALIGN='CENTER'><B>Testing datasource</B> " + jndiName + "...");

		try { 
			
			InitialContext initCtx = new InitialContext();
			DataSource aDS = (DataSource)initCtx.lookup("java:comp/env/jdbc/" + jndiName);

			Connection aConn = aDS.getConnection();
			output.append("<FONT COLOR='GREEN'><B>Success</FONT></B></P>");
			output.append("<P CLASS='mainContent'><B>Checking Schema...</B>");
			int statusCols = 3;
			TableCell myCell = new TableHeader();

			for ( int i = 0; i < statusCols; i++ ) {
				myCell.addItem("Table");
				myCell.addItem("Status");
			}
			boolean schemaOK = true;
			TableRow aRow = new TableRow(myCell);
 
			DatabaseMetaData dbMeta = aConn.getMetaData();
			String[] types = {"TABLE"};
			ResultSet aResult = dbMeta.getTables(null, null, null, types);
			List<String> foundTables = new ArrayList<String>();
			if ( aResult.getType() != ResultSet.TYPE_FORWARD_ONLY ) aResult.beforeFirst();
			while ( aResult.next() ) {
				foundTables.add(aResult.getString("TABLE_NAME").toLowerCase());
			}


			CyanosConfig myConfig = this.getAppConfig();
			String[] tables = myConfig.tableList();
			myCell = new TableCell();
			for ( int i=0; i < tables.length; i++ ) {
				myCell.addItem(tables[i]);
				if ( foundTables.contains(tables[i]) )
					myCell.addItem("<FONT COLOR='GREEN'><B>OK</B></FONT>");
				else {
					myCell.addItem("<FONT COLOR='RED'><B>NOT FOUND</B></FONT>");
					schemaOK = false;
				}
				int count = (i + 1) % statusCols;
				if ( count == 0 ) {
					aRow.addItem(myCell);
					myCell = new TableCell();
				}
			}

			if ( (tables.length % statusCols) > 0  ) {

			}

			Table myTable = new Table(aRow);
			myTable.setClass("status");
			output.append(myTable.toString());
			if ( ! schemaOK ) {
				output.append("Schema not up to date.");
			}
			output.append("</P>");
			aConn.close();
		} catch (SQLException e) {
			output.append("<BR/><FONT COLOR='RED'>ERROR:</FONT>" + e.getMessage() + "</B>");
			e.printStackTrace();
		} catch (NamingException e) {
			output.append("<BR/><FONT COLOR='RED'>ERROR:</FONT>" + e.getMessage() + "</B>");
			e.printStackTrace();
		}
		output.append("</P>");
		return output.toString();
	}
	*/
}
