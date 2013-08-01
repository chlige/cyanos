/**
 * 
 */
package edu.uic.orjala.cyanos.web.forms;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.naming.NamingException;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import edu.uic.orjala.cyanos.BasicUser;
import edu.uic.orjala.cyanos.ConfigException;
import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.MutableUser;
import edu.uic.orjala.cyanos.Role;
import edu.uic.orjala.cyanos.Sample;
import edu.uic.orjala.cyanos.Separation;
import edu.uic.orjala.cyanos.Strain;
import edu.uic.orjala.cyanos.User;
import edu.uic.orjala.cyanos.sql.SQLData;
import edu.uic.orjala.cyanos.sql.SQLMutableUser;
import edu.uic.orjala.cyanos.web.AppConfig;
import edu.uic.orjala.cyanos.web.AppConfigXML;
import edu.uic.orjala.cyanos.web.BaseForm;
import edu.uic.orjala.cyanos.web.CyanosWrapper;
import edu.uic.orjala.cyanos.web.html.Div;
import edu.uic.orjala.cyanos.web.html.HtmlList;
import edu.uic.orjala.cyanos.web.html.Popup;
import edu.uic.orjala.cyanos.web.html.Table;
import edu.uic.orjala.cyanos.web.html.TableCell;
import edu.uic.orjala.cyanos.web.html.TableHeader;
import edu.uic.orjala.cyanos.web.html.TableRow;

/**
 * @author George Chlipala
 *
 */
public class SetupForm extends BaseForm {
	
	/**
	 * @author George Chlipala
	 *
	 */
	protected class SetupUser extends BasicUser {

		/**
		 * 
		 */
		public SetupUser() {
			this.myID = "setup";
			Map<String,Role> roleList = new HashMap<String,Role>();
			this.projectRoles.put(NULL_PROJECT, roleList);
			roleList.put(BasicUser.ADMIN_ROLE, new Role(BasicUser.ADMIN_ROLE, Role.READ + Role.WRITE + Role.DELETE + Role.CREATE));
		}

		/* (non-Javadoc)
		 * @see edu.uic.orjala.cyanos.User#getUserName()
		 */
		public String getUserName() throws DataException {
			return "Setup User";
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

		/* (non-Javadoc)
		 * @see edu.uic.orjala.cyanos.User#resetPassword(javax.mail.Session)
		 */
		public void resetPassword(Session mailSession) throws DataException,
				AddressException, MessagingException {
			// NOTHING TO DO.
		}

	}

	/**
	 * @author George Chlipala
	 *
	 */
	public class UserInfo {
		private final String userID;
		private final String userName;
		private final String userEmail;
		private boolean isAdmin;
		/**
		 * 
		 */
		public UserInfo(String id, String name, String email) {
			this.userID = id;
			this.userName = name;
			this.userEmail = email;
		}
		
		public String getID() {
			return this.userID;
		}
		
		public String getName() {
			return this.userName;
		}
		
		public String getEmail() {
			return this.userEmail;
		}
		
		public boolean isAdmin() {
			return this.isAdmin;
		}
		
		public void setAdmin() {
			this.isAdmin = true;
		}
		
		public void unsetAdmin() {
			this.isAdmin = false;
		}

	}

	private final static String PAGE_VALUE = "page";
	private final static String APP_CONFIG_ATTR = "app_config";
	private final static String ADMIN_LOGIN_VALUE = "adminLogin";
	private final static String ADMIN_EMAIL = "adminEmail";
	
	private final static String PREV_PAGE = "prevPage";
	private final static String NEXT_PAGE = "nextPage";
	private final static String FINISH_ACTION = "finish";
	
	private final static String[] PAGE_TITLES = {"Welcome", "Setup Database", "Setup Administrator", "Datafile Configuration", "Finish"};
	
	private final static int PAGE_WELCOME = 0;
	private final static int PAGE_DATABASE = 1;
	private final static int PAGE_ADMIN = 2;
	private final static int PAGE_DATAFILE = 3;
	
	private final static int PAGE_USERS = 3;
	
	private final static int PAGE_FINISH = 4;
	private static final int DATABASE_VERSION = 2;
	
	private File oldConfig;
	
	private Map<String,Object> valueMap = null;

	public SetupForm(CyanosWrapper aWrapper) {
		super(aWrapper);
	}
	
	public void setConfigFile(String file) {
		this.oldConfig = new File(file);
	}
	
	public String newConfig() {
		
		Div content = new Div();
		content.setID("setupPanel");

		AppConfig myConfig = this.myWrapper.getAppConfig();
		if ( myConfig == null ) {
			content.addItem(this.getSideBar(-1));
			content.addItem(this.warnPage());
			return content.toString();
		}
		
		int page = 0;
		if ( this.hasFormValue(SetupForm.PAGE_VALUE)) {
			page = Integer.parseInt(this.getFormValue(SetupForm.PAGE_VALUE));
		}
		
		int prevPage = -1;
		if ( this.hasFormValue("prev")) {
			prevPage = Integer.parseInt(this.getFormValue("prev"));
		}
		
		HttpSession aSession = this.myWrapper.getSession();
		Object attr = aSession.getAttribute(SetupForm.APP_CONFIG_ATTR);
		if ( attr instanceof Map )
			valueMap = (Map<String,Object>)attr;
		if ( valueMap == null ) {
			valueMap = new HashMap<String,Object>();
			aSession.setAttribute(SetupForm.APP_CONFIG_ATTR, valueMap);
		}


		if ( this.hasFormValue(SetupForm.PREV_PAGE) ) {
			prevPage = page;
			page = page - 1;
		} else if ( this.hasFormValue(SetupForm.NEXT_PAGE) ) {
			prevPage = page;
			page = page + 1;
		} else if ( prevPage < 0 && page > 0 ){
			prevPage = page;
		}

		Div validation = null;
		
		try { 
			switch (prevPage) {
			case 2: if ( ! this.validateAdmin() ) {
				page = prevPage;
				validation = new Div("<P><B><FONT COLOR='red'>Validation error:</FONT> Need to desginate an administrator account.</B></P>");
				validation.setClass("error");
			} 
			break;
			//case 3: ; break;
			//case 4: ; break;
			}
		} catch ( ConfigException e ) {
			validation = new Div("<P><B><FONT COLOR='red'>Configuration ERROR:</FONT> ");
			validation.addItem(e.getLocalizedMessage());
			validation.addItem("</B></P>");
			validation.setClass("error");
			e.printStackTrace();
		} catch (SQLException e) {
			validation = new Div("<P><B><FONT COLOR='red'>SQL ERROR:</FONT> ");
			validation.addItem(e.getLocalizedMessage());
			validation.addItem("</B></P>");
			validation.setClass("error");
			e.printStackTrace();
		}

		if ( this.hasFormValue(SetupForm.FINISH_ACTION) ) {
			page = PAGE_FINISH;
		}
		
		content.addItem(this.getSideBar(page));
		content.addItem("<DIV ID='mainPanel'>");
		
		Div panelDiv = new Div();

		if ( validation != null ) {
			content.addItem(validation);
			panelDiv.setAttribute("style", "position:absolute; top: 10%; left: 0; width: 100%; height: 90%");
		} else {
			panelDiv.setAttribute("style", "position:absolute; top: 0; left: 0; width: 100%; height: 100%");			
		}

		content.addItem(panelDiv);

		switch (page) {
		case PAGE_WELCOME: panelDiv.addItem(this.welcomePage()); break;
		case PAGE_DATABASE: panelDiv.addItem(this.databasePage()); break;
		case PAGE_ADMIN: panelDiv.addItem(this.adminPage()); break;
//		case 3: content.addItem(this.newPage3()); break;
		case PAGE_DATAFILE: panelDiv.addItem(this.datafilePage()); break;
		case PAGE_FINISH: panelDiv.addItem(this.finishPage()); break;
		}
		content.addItem("</DIV>");
		return content.toString();

	}
	
	private boolean validateAdmin() throws ConfigException, SQLException {
		if ( this.hasFormValue(ADMIN_LOGIN_VALUE) && this.getFormValue(ADMIN_LOGIN_VALUE).length() > 0 ) {
			valueMap.put(ADMIN_LOGIN_VALUE, this.getFormValue(ADMIN_LOGIN_VALUE));
			valueMap.put(ADMIN_EMAIL, this.getFormValue(ADMIN_EMAIL));
		} else {
			valueMap.remove(ADMIN_LOGIN_VALUE);
			valueMap.remove(ADMIN_EMAIL);
		}
		
		if ( this.hasFormValue("admins") ) {
			List<String> admins = new ArrayList<String>();
			String[] list = this.getFormValues("admins");
			for ( int i = 0; i < list.length; i++ ) {
				admins.add(list[i]);
			}
			valueMap.put("admins", admins);
		} else {
			valueMap.remove("admins");
		}

		if (valueMap.containsKey(ADMIN_LOGIN_VALUE) || valueMap.containsKey("admins"))
			return true;
		
		return (this.getAdminCount() > 0);
	}
	
	private int getAdminCount() throws ConfigException, SQLException {
		int retVal = 0;
		DataSource aDS = this.myWrapper.getAppConfig().getDataSourceObject();
		Connection aConn = aDS.getConnection();
		PreparedStatement aSth = aConn.prepareStatement("SELECT COUNT(username) FROM roles WHERE role = 'admin'");
		ResultSet dbUsers = aSth.executeQuery();
		while ( dbUsers.next() ) {
			retVal = dbUsers.getInt(1);
		}
		aConn.close();
		return retVal;
	}
	
	private String getSideBar(int level) {
		HtmlList aList = new HtmlList();
		aList.ordered();
		for ( int i = 0; i < PAGE_TITLES.length; i++ ) {
			if ( level == i ) {
				aList.addItem(String.format("<B>%s</B>", PAGE_TITLES[i]));				
			} else {
				aList.addItem(String.format("<A HREF='?prev=%d&page=%d'>%s</A>", level, i, PAGE_TITLES[i]));
			}
		}
		Div content = new Div(aList);
		content.setID("sideNav");
		return content.toString();
	}
	
	private String welcomePage() {
		StringBuffer output = new StringBuffer("<DIV ID='contentPanel'><FORM METHOD='POST' ACTION='main'/>");
		output.append(String.format("<INPUT TYPE='HIDDEN' NAME='page' VALUE='%d'/>", PAGE_WELCOME));
		output.append("<P ALIGN='CENTER'><FONT SIZE='+2'><B>Welcome!</B></FONT></P>");
		output.append("<P CLASS='mainContent'>Welcome to Cyanos, a natural product drug discovery information management system.  ");
		output.append("This system allows one to store and manage data related to:");
		output.append("<UL><LI>Taxonomic data</LI><LI>Culture information, e.g. inoculations, harvest, and cryopreservations.</LI>");
		output.append("<LI>Extraction and fractionation data.</LI><LI>Sample libraries</LI><LI>Bioassay data</LI>");
		output.append("<LI>Datafile management, e.g. LC-UV, MS, and NMR data.</LI></UL></P>");

		if ( this.oldConfig != null && this.oldConfig.exists() ) {
			if ( this.hasFormValue("importXML") ) {
				AppConfig myConf = this.myWrapper.getAppConfig();
				try {
					AppConfigXML.loadConfig((AppConfig)myConf, this.oldConfig);
					output.append("<P><B>XML configuration file successfully imported.</B></P>");
					output.append("<TABLE CLASS='buttons'><TR><TD><BUTTON TYPE='SUBMIT' NAME='finish' VALUE='load'>Finish</BUTTON></TD></TR></TABLE>");		
					output.append("</FORM></DIV>");
					return output.toString();
				} catch (ConfigException e) {
					output.append(this.handleException(e));
				}
			} else {
				output.append("<P><B>An XML configuration file exists.</B> <BUTTON TYPE='SUBMIT' NAME='importXML'>Import Configuration</BUTTON></P>");
			}
		}
		
		output.append("<TABLE CLASS='buttons'><TR><TD><INPUT TYPE='SUBMIT' NAME='nextPage' VALUE='Next &gt;'/></TD></TR></TABLE>");		
		output.append("</FORM></DIV>");
		return output.toString();
	}
	
	private String warnPage() {
		StringBuffer output = new StringBuffer("<DIV ID='contentPanel'><FORM METHOD='POST' ACTION='main'/>");
		output.append("<P ALIGN='CENTER'><FONT SIZE='+2'><B>SETUP INCOMPLETE</B></FONT></P>");
		output.append("<P CLASS='mainContent'>The web appplication container not properly setup.  Ensure that the proper JDBC Datasource is available.</P>");
		output.append("<TABLE CLASS='buttons'><TR><TD><INPUT TYPE='SUBMIT' NAME='go' VALUE='Next &gt;'/></TD></TR></TABLE>");
		
		output.append("</FORM></DIV>");
		return output.toString();
	}
	
	private String databasePage() {
		StringBuffer output = new StringBuffer(this.getSideBar(PAGE_DATABASE));
		output.append("<DIV ID='contentPanel'><FORM METHOD='POST' ACTION='main'/>");
		output.append(String.format("<INPUT TYPE='HIDDEN' NAME='page' VALUE='%d'/>", PAGE_DATABASE));
		output.append("<P ALIGN='CENTER'><FONT SIZE='+2'><B>Database Setup</B></FONT></P>");
		output.append("<P CLASS='mainContent'>Please enter the information for your database. Setup the datasource configurations in the web application configuration.");

		boolean update = this.hasFormValue("updateSchema");

		output.append(this.validateDB());

		if ( update || valueMap.containsKey("dbOK") ) 
			output.append("<TABLE CLASS='buttons'><TR><TD><INPUT TYPE='SUBMIT' NAME='prevPage' VALUE='&lt; Previous'/></TD><TD><INPUT TYPE='SUBMIT' NAME='nextPage' VALUE='Next &gt;'/></TD></TR></TABLE>");		
		else 
			output.append("<TABLE CLASS='buttons'><TR><TD><INPUT TYPE='SUBMIT' NAME='prevPage' VALUE='&lt; Previous'/></TD><TD><INPUT TYPE='SUBMIT' NAME='update1' VALUE='Next &gt;'/></TD></TR></TABLE>");		
		output.append("</FORM></DIV>");
		return output.toString();

	}

	private String adminPage() {
		StringBuffer output = new StringBuffer("<DIV ID='contentPanel'><FORM METHOD='POST' ACTION='main'/>");
		output.append(String.format("<INPUT TYPE='HIDDEN' NAME='page' VALUE='%d'/>", PAGE_ADMIN));
		List<UserInfo> userList = new ArrayList<UserInfo>();
		try { 
			DataSource aDS = this.myWrapper.getAppConfig().getDataSourceObject();
			Connection aConn = aDS.getConnection();
			PreparedStatement aSth = aConn.prepareStatement("SELECT DISTINCT users.username, users.fullname, users.email, (SELECT roles.perm FROM roles WHERE roles.username = users.username AND roles.role = 'admin') FROM users ORDER BY username");
			ResultSet dbUsers = aSth.executeQuery();
			dbUsers.beforeFirst();
			while ( dbUsers.next() ) {
				UserInfo aUser = new UserInfo(dbUsers.getString(1), dbUsers.getString(2), dbUsers.getString(3));
				if ( dbUsers.getInt(4) > 0 ) {
					aUser.setAdmin();
				}
				userList.add(aUser);
			}
			aConn.close();
		} catch (Exception e) {
			output.append(this.handleException(e));
		}
		
		output.append("<P ALIGN='CENTER'><FONT SIZE='+2'><B>Setup Administrator Account</B></FONT></P>");

		output.append("<P CLASS='mainContent'>Setup an administrator account for the management of this application.</P>");

		boolean hasAdmin = false;

		if ( userList.size() > 0 ) {
			output.append("<H3>Select existing user account</H3>");
			ListIterator<UserInfo> iter = userList.listIterator();
			TableCell aCell = new TableHeader("User");
			aCell.addItem("Full Name");
			aCell.addItem("Email");
			aCell.addItem("Admin");
			TableRow aRow = new TableRow(aCell);
			
			List<String> admins = null;
			if ( valueMap.containsKey("admins") ) {
				admins = (List<String>)valueMap.get("admins");
			}
			
			while ( iter.hasNext() ) {
				UserInfo user = iter.next();
				hasAdmin = ( hasAdmin ? hasAdmin : user.isAdmin() );
				aCell = new TableCell(user.getID());
				aCell.addItem(user.getName());
				aCell.addItem(user.getEmail());
				if ( user.isAdmin() ) {
					aCell.addItem("<B>X</B>");
				} else {
					if ( admins != null && admins.contains(user.getID()) ) {
						aCell.addItem(String.format("<INPUT TYPE='CHECKBOX' NAME='admins' VALUE='%s' CHECKED>", user.getID()));						
					} else
						aCell.addItem(String.format("<INPUT TYPE='CHECKBOX' NAME='admins' VALUE='%s'>", user.getID()));
				}
				aRow.addItem(aCell);
			}
			Table aTable = new Table(aRow);
			output.append(aTable.toString());
		}
		
		if ( hasAdmin ) {
			output.append("<INPUT TYPE='HIDDEN' NAME='hasAdmin'>");
		}
		
		output.append("<H3>Create a new user account</H3><P><B>NOTE:</B> This user will only be given right to change the configuration of the application and will not be granted access rights to the data.</P>");
		
		output.append("<P>");
		TableCell myCell = new TableCell("Login:");
		if ( valueMap.containsKey("adminLogin") ) {
			myCell.addItem(String.format("<INPUT TYPE='TEXT' NAME='adminLogin' VALUE=\"%s\"/>", valueMap.get("adminLogin")));
		} else {
			myCell.addItem("<INPUT TYPE='TEXT' NAME='adminLogin'/>");
		}
		
		TableRow aRow = new TableRow(myCell);
		
		myCell = new TableCell("Password:");
		if ( valueMap.containsKey("pass1") ) 
			myCell.addItem(String.format("<INPUT TYPE='PASSWORD' NAME='pass1' VALUE=\"%s\"/>", valueMap.get("pass1")));
		else 
			myCell.addItem("<INPUT TYPE='PASSWORD' NAME='pass1' />");
		aRow.addItem(myCell);
		
		myCell = new TableCell("Confirm Password:");
		if ( valueMap.containsKey("pass2") ) 
			myCell.addItem(String.format("<INPUT TYPE='PASSWORD' NAME='pass2' VALUE=\"%s\"/>", valueMap.get("pass2")));
		else 
			myCell.addItem("<INPUT TYPE='PASSWORD' NAME='pass2' />");
		aRow.addItem(myCell);
		
		Table myTable = new Table(aRow);
		
		output.append(myTable.toString());
		
		output.append("<TABLE CLASS='buttons'><TR><TD><INPUT TYPE='SUBMIT' NAME='prevPage' VALUE='&lt; Previous'/></TD><TD><INPUT TYPE='SUBMIT' NAME='nextPage' VALUE='Next &gt;'/></TD></TR></TABLE>");		
		output.append("</FORM></DIV>");
		return output.toString();
		
	}
	
	private String newPage3() {
		StringBuffer output = new StringBuffer("<DIV ID='contentPanel'><FORM METHOD='POST' ACTION='main'/><INPUT TYPE='HIDDEN' NAME='page' VALUE='3'/>");
		output.append("<P ALIGN='CENTER'><FONT SIZE='+2'><B>Setup Initial User Account</B></FONT></P>");
		output.append("<P CLASS='mainContent'>Create initial user accounts. <B>NOTE:</B> User accounts can be created after setup of the application.  Login using the adminstrator account created in the previous step and use the user administration interface to add, modify, or remove user accounts.</P>");

		List<Map<String,String>> userList;
		if ( valueMap.containsKey("users") ) {
			userList = (List<Map<String, String>>) valueMap.get("users");
		} else {
			userList = new ArrayList<Map<String,String>>();
			valueMap.put("users", userList);
			if ( valueMap.containsKey("dbOK") ) {
				output.append("<P CLASS='mainContent'>Attempting to find existing user accounts...");
				try { 
					DataSource aDS = this.myWrapper.getAppConfig().getDataSourceObject();
					Connection aConn = aDS.getConnection();
					PreparedStatement aSth = aConn.prepareStatement("SELECT username,fullname,email FROM users WHERE username != ?");
					aSth.setString(1,(String)valueMap.get("adminLogin"));
					ResultSet dbUsers = aSth.executeQuery();
					dbUsers.beforeFirst();
					while ( dbUsers.next() ) {
						Map<String,String> aUser = new HashMap<String, String>();
						aUser.put("login", dbUsers.getString(1));
						aUser.put("name", dbUsers.getString(2));
						aUser.put("email", dbUsers.getString(3));
						userList.add(aUser);
					};
					aConn.close();
					output.append("<FONT COLOR='GREEN'><B>Success</FONT></B></P>");
				} catch (Exception e) {
					output.append("<P ALIGN='CENTER'><B><FONT COLOR='RED'>ERROR:</FONT>" + e.getMessage() + "</B></P>");
					e.printStackTrace();
				}

			}
		}
		
		if ( this.hasFormValue("addUser") ) {
			Map<String,String> aUser = new HashMap<String, String>();
			aUser.put("login", this.getFormValue("login"));
			aUser.put("name", this.getFormValue("name"));
			aUser.put("email", this.getFormValue("email"));
			aUser.put("pwd", "new");
			userList.add(aUser);
		}
		
		output.append("<P CLASS='mainContent'>");
		
		if (userList.size() > 0 ) {
			String headers[] = { "Login", "Name", "Email" };
			TableCell myCell = new TableHeader(headers);
			myCell.setAttribute("ALIGN", "LEFT");
			TableRow aRow = new TableRow(myCell);
			
			ListIterator<Map<String, String>> userIter = userList.listIterator();
			
			while ( userIter.hasNext()) {
				Map<String, String> aUser = userIter.next();
				myCell = new TableCell(aUser.get("login"));
				myCell.addItem(aUser.get("name"));
				myCell.addItem(aUser.get("email"));
				if ( ! aUser.containsKey("pwd") ) {
					myCell.addItem("<B>Pre-existing account</B>");
				}
				aRow.addItem(myCell);
			}
			
			Table myTable = new Table(aRow);
			myTable.setClass("status");
			myTable.setAttribute("WIDTH", "75%");
			output.append(myTable);
		}
		
		output.append("</P><P CLASS='mainContent'>");

		TableRow aRow = new TableRow("<TD>Login:</TD><TD><INPUT TYPE='TEXT' NAME='login'/></TD>");
		aRow.addItem("<TD>Name:</TD><TD><INPUT TYPE='TEXT' NAME='name' SIZE='25'/></TD>");
		aRow.addItem("<TD>Email:</TD><TD><INPUT TYPE='TEXT' NAME='email' SIZE='25'/></TD>");
		aRow.addItem("<TD COLSPAN='2'><INPUT TYPE='SUBMIT' NAME='addUser' VALUE='Add User'/></TD>");
		
		Table myTable = new Table(aRow);
		output.append(myTable.toString());
		
		output.append("<TABLE CLASS='buttons'><TR><TD><INPUT TYPE='SUBMIT' NAME='prevPage' VALUE='&lt; Previous'/></TD><TD><INPUT TYPE='SUBMIT' NAME='nextPage' VALUE='Next &gt;'/></TD></TR></TABLE>");		
		output.append("</FORM></DIV>");
		return output.toString();	
	}
	
	
	private String datafilePage() {
		StringBuffer output = new StringBuffer("<DIV ID='contentPanel'><FORM METHOD='POST' ACTION='main'/>");
		output.append(String.format("<INPUT TYPE='HIDDEN' NAME='page' VALUE='%d'/>", PAGE_DATAFILE));
		output.append("<P ALIGN='CENTER'><FONT SIZE='+2'><B>Configure Datafile Paths</B></FONT></P>");
		output.append("<P CLASS='mainContent'>Define directory paths on the server to store photos and datafiles.  An asterix (*) can be used as a wildcard to class or type values.</P>");

		output.append("<P CLASS='mainContent'>");

		AppConfig myConf = this.myWrapper.getAppConfig();

		if ( this.hasFormValue("addPath") ) {
			myConf.setFilePath(this.getFormValue("class"), this.getFormValue("type"), this.getFormValue("path"));
		}

		Map<String, Map<String, String>> fileMap = myConf.getFilePathMap();
		Iterator<String> classIter = fileMap.keySet().iterator();

		String[] headers = {"Class", "Type", "Path"};
		TableCell aCell = new TableHeader(headers);
		TableRow aRow = new TableRow(aCell);

		while ( classIter.hasNext() ) {
			String aClass = (String)classIter.next();
			Map<String, String> classMap = fileMap.get(aClass);
			Iterator<String> typeIter = classMap.keySet().iterator();
//			myCell.setAttribute("COLSPAN", "3");
//			aRow.addItem(myCell);
			while ( typeIter.hasNext() ) {
				String aType = (String) typeIter.next();
				TableCell myCell = new TableCell(aClass);
				myCell.setAttribute("COLSPAN", "3");
				myCell.addItem(aType);
				myCell.addItem(classMap.get(aType));
				aRow.addItem(myCell);
			}
		}

		Popup classPop = new Popup();
		classPop.addItem("");
		classPop.addItem("*");
		classPop.addItem("strain");
		classPop.addItem("sample");
		classPop.addItem("separation");
		classPop.setName("class");
		classPop.setAttribute("onChange","this.form.submit()");

		Popup typePop = new Popup();
		typePop.addItem("");
		typePop.setName("type");


		if ( this.hasFormValue("class") && (! this.getFormValue("class").equals("")) ) {
			typePop.addItem("*");
			String currClass = this.getFormValue("class");
			classPop.setDefault(currClass);
			if ( currClass.equals("strain") ) {
				typePop.addItem(Strain.PHOTO_DATA_TYPE);
			} else if ( currClass.equals("sample")) {
				typePop.addItem(Sample.LC_DATA_TYPE);
				typePop.addItem(Sample.NMR_DATA_TYPE);
				typePop.addItem(Sample.MS_DATA_TYPE);
			} else if ( currClass.equals("separation") ) {
				typePop.addItem(Separation.LC_DATA_TYPE);
			}
			if ( this.hasFormValue("type"))
				typePop.setDefault(this.getFormValue("type"));
		}

		aCell = new TableCell(classPop);
		aCell.addItem(typePop);
		aCell.addItem("<INPUT TYPE='TEXT' NAME='path' SIZE='35'/>");
		aRow.addItem(aCell);

		if ( this.hasFormValue("class") && (! this.getFormValue("class").equals("")) ) {

			aRow.addItem("<TD COLSPAN=2><TD><INPUT TYPE='SUBMIT' NAME='addPath' VALUE='Add File Path'/></TD>");
		}


		Table myTable = new Table(aRow);
		myTable.setAttribute("WIDTH", "75%");
		output.append(myTable);

		output.append("</P><TABLE CLASS='buttons'><TR><TD><INPUT TYPE='SUBMIT' NAME='prevPage' VALUE='&lt; Previous'/></TD><TD><INPUT TYPE='SUBMIT' NAME='finish' VALUE='Finish'/></TD></TR></TABLE>");		
		output.append("</FORM></DIV>");
		return output.toString();
		
	}

	private String finishPage() {
		StringBuffer output = new StringBuffer("<DIV ID='contentPanel'><FORM METHOD='POST' ACTION='main'/><INPUT TYPE='HIDDEN' NAME='finish' VALUE='4'/>");

		AppConfig myConf = this.myWrapper.getAppConfig();

		if ( this.hasFormValue("commit") ) {
			output.append("<P ALIGN='CENTER'><FONT SIZE='+2'><B>Saving Configuration...</B></FONT>");
			
//			myConf.setDataSource((String)valueMap.get("dataDS"));
			try {
				myConf.writeConfig();
				output.append("<FONT SIZE='+1' COLOR='GREEN'><B>SUCCESS</B></FONT></P>");

				TableRow aRow = new TableRow("<TH COLSPAN=3>Administrator Account(s)</TH>");

				SQLData dataSource = new SQLData((AppConfig)myConf, myConf.getDataSourceObject().getConnection(), new SetupUser());

				if ( valueMap.containsKey("admins") ) {
					List<String> admins = (List<String>)valueMap.get("admins");
					ListIterator<String> iter = admins.listIterator();
					while ( iter.hasNext() ) {
						TableCell myCell = new TableCell("Existing Login:");
						String userID = iter.next();
						myCell.addItem(userID);
						try {
							MutableUser aUser = new SQLMutableUser(dataSource, userID);
							aUser.grantGlobalPermission(User.ADMIN_ROLE, Role.READ + Role.WRITE + Role.DELETE + Role.CREATE);
							myCell.addItem("<FONT COLOR='GREEN'><B>SUCCESS</B></FONT>");
						} catch (DataException e) {
							myCell.addItem(this.handleException(e));
						}
						aRow.addItem(myCell);
					}
				}
				
				if ( valueMap.containsKey(ADMIN_LOGIN_VALUE) ) {
					TableCell myCell = new TableCell("New Login:");
					String userID = (String) valueMap.get(ADMIN_LOGIN_VALUE);
					String email = (String) valueMap.get(ADMIN_EMAIL);

					try {
						MutableUser aUser = SQLMutableUser.createUser(dataSource, userID);
						aUser.setUserEmail(email);
						aUser.grantGlobalPermission(User.ADMIN_ROLE, Role.READ + Role.WRITE + Role.DELETE + Role.CREATE);
						aUser.resetPassword(this.myWrapper.getMailSession());
						myCell.addItem("<FONT COLOR='GREEN'><B>SUCCESS</B></FONT>");
					} catch (DataException e) {
						myCell.addItem(this.handleException(e));
					} catch (AddressException e) {
						myCell.addItem(this.handleException(e));
					} catch (MessagingException e) {
						myCell.addItem(this.handleException(e));
					} catch (NamingException e) {
						myCell.addItem(this.handleException(e));
					}
					aRow.addItem(myCell);
				}
				
				Table myTable = new Table(aRow);

				output.append(myTable.toString());
				
				output.append("<P CLASS='mainContent'>The application is now configured for use!<BR/>Please restart the servlet container!<BR/>");
				output.append(String.format("<A HREF='/manager/html/reload?path=%s'>Tomcat Restart</A></P>",this.myWrapper.getContextPath()));
				return output.toString();
			} catch ( ConfigException e) {
				output.append("<FONT SIZE='+1' COLOR='RED'><B>WRITE FAILED</B></FONT></BR>");
				output.append(e.getMessage());
				output.append("</P>");
				e.printStackTrace();
			} catch (SQLException e) {
				output.append(this.handleException(e));
			} 

		} else {
			output.append("<P ALIGN='CENTER'><FONT SIZE='+2'><B>Confirm and Commit Configuration</B></FONT></P>");
			output.append("<P CLASS='mainContent'>Confirm the settings for the application then click the \"Commit\" button to save the changes.</P>");
		}

		boolean setupOK = false;
		
		try { 
			output.append("<P><B>Database</B>...");
			if ( this.isDBValid() ) {
				output.append("<FONT COLOR='GREEN'><B>OK</FONT></B></P>");
				setupOK = true;
			} else if ( this.isDBCurrent() ) {
				output.append("<FONT COLOR='RED'><B>Incomplete</FONT></B></P>");
				output.append("<DIV class='suberror'><P>Schema incomplete.  Recreate the schema using the included <A HREF='cyanos-schema.sql'>SQL file</A>.</P></DIV>");					
			} else {
				output.append("<FONT COLOR='RED'><B>Need to update</FONT></B></P>");
				output.append("<DIV class='suberror'><P>Incompatible schema version.  Please upgrade using the appropriate SQL file.<BR>See installation instructions for further details.</P></DIV>");					
			}
		} catch (SQLException e) {
			output.append(this.handleException(e));
		} catch (ConfigException e) {
			output.append(this.handleException(e));
		}
		
		TableRow aRow = new TableRow("<TH COLSPAN=2>Administrator Account(s)</TH>");

		boolean hasAdmin = false;

		if ( valueMap.containsKey("admins") ) {
			List<String> admins = (List<String>)valueMap.get("admins");
			ListIterator<String> iter = admins.listIterator();
			while ( iter.hasNext() ) {
				TableCell myCell = new TableCell("Existing Login:");		
				myCell.addItem(iter.next());
				aRow.addItem(myCell);
			}
			hasAdmin = true;
		}

		if ( valueMap.containsKey(ADMIN_LOGIN_VALUE) ) {
			TableCell myCell = new TableCell("New Login:");
			myCell.addItem(String.format("%s &lt;%s&gt;", valueMap.get(ADMIN_LOGIN_VALUE), valueMap.get(ADMIN_EMAIL)));
			aRow.addItem(myCell);
			hasAdmin = true;
		}

		try { 
			if ( ! hasAdmin ) {
				int admins = this.getAdminCount();
				hasAdmin = ( admins > 0 );
				aRow.addItem(String.format("<TD COLSPAN='2'>%d administrator account(s) currently exist(s)</TD>", admins));
			}
		} catch ( SQLException e ) {
			output.append(this.handleException(e));
		} catch (ConfigException e) {
			output.append(this.handleException(e));
		}

		/*
			aRow.addItem("<TH COLSPAN=2>User Accounts</TH>");
			int newAcct = 0;
			int oldAcct = 0;

			List<?> userList = (ArrayList<?>)valueMap.get("users");
			ListIterator<?> userIter = userList.listIterator();

			while ( userIter.hasNext() ) {
				Map<String, ?> aUser = (Map<String, ?>)userIter.next();
				if (aUser.containsKey("pwd")) newAcct++;
				else oldAcct++;
			}

			myCell = new TableCell("New Accounts:");
			myCell.addItem(String.format("%d", newAcct));
			aRow.addItem(myCell);

			myCell = new TableCell("Existing Accounts:");
			myCell.addItem(String.format("%d", oldAcct));
			aRow.addItem(myCell);
		 */

		aRow.addItem("<TH COLSPAN=2>Data File Paths</TH>");

		Map<String, Map<String, String>> fileMap = myConf.getFilePathMap();
		Iterator<String> classIter = fileMap.keySet().iterator();

		String[] headers = {"Class", "Type", "Path"};
		TableCell aCell = new TableHeader(headers);
		TableRow dfRow = new TableRow(aCell);

		while ( classIter.hasNext() ) {
			String aClass = (String)classIter.next();
			Map<String, String> classMap = fileMap.get(aClass);
			Iterator<String> typeIter = classMap.keySet().iterator();
			//			myCell.setAttribute("COLSPAN", "3");
			//			aRow.addItem(myCell);
			while ( typeIter.hasNext() ) {
				String aType = (String) typeIter.next();
				TableCell myCell = new TableCell(aClass);
				myCell.addItem(aType);
				myCell.addItem(classMap.get(aType));
				dfRow.addItem(myCell);
			}
		}

		aCell = new TableCell(new Table(dfRow));
		aCell.setAttribute("COLSPAN", "2");
		aRow.addItem(aCell);

		Table myTable = new Table(aRow);

		if ( ! hasAdmin ) 
			output.append("<P>No administrator account specified.  Please designate one using the <A HREF='?page=2'>Administrator Account Page</A>.</P>");

		output.append("<P CLASS='mainContent'>");
		output.append(myTable);
		if ( hasAdmin && setupOK )
			output.append("</P><TABLE CLASS='buttons'><TR><TD><INPUT TYPE='SUBMIT' NAME='commit' VALUE='Commit'/></TD></TR></TABLE>");	

		output.append("</FORM></DIV>");

		return output.toString();
	}

	private boolean isDBValid() throws ConfigException, SQLException {
		if ( ! this.valueMap.containsKey("dbOK") ) {
			this.valueMap.put("dbOK", new Boolean(this.checkSchema()));
		}
		return ((Boolean) this.valueMap.get("dbOK")) && this.isDBCurrent();
	}
	
	private boolean isDBCurrent() throws ConfigException, SQLException {
		if ( ! this.valueMap.containsKey("dbVer") ) {
			this.valueMap.put("dbVer", new Integer(this.getDBVersion()));
		}
		Integer version = (Integer) valueMap.get("dbVer");
		return ( version.intValue() == DATABASE_VERSION );
	}
	
	private boolean dbChecked() {
		return this.valueMap.containsKey("dbOK");
	}
	
	private int getDBVersion() throws ConfigException, SQLException {
		AppConfig myConfig = this.myWrapper.getAppConfig();
		int version = 0;
		DataSource aDS = myConfig.getDataSourceObject();
		Connection aConn = aDS.getConnection();
		SQLException ex = null;
		try { 
			Statement sth = aConn.createStatement();
			ResultSet result = sth.executeQuery("SELECT value FROM config WHERE element = 'database' AND param = 'version'");
			if ( result.first() ) {
				version = result.getInt(1);
			}
		} catch (SQLException e) {
			ex = e;
		} finally {
			aConn.close();
			if ( ex != null ) throw ex;
		}
		return version;
	}
	
	private boolean checkSchema() throws ConfigException, SQLException {
		AppConfig myConfig = this.myWrapper.getAppConfig();
		boolean found = true;
		DataSource aDS = myConfig.getDataSourceObject();
		Connection aConn = aDS.getConnection();
		try { 
			DatabaseMetaData dbMeta = aConn.getMetaData();
			String[] types = {"TABLE"};
			ResultSet aResult = dbMeta.getTables(null, null, null, types);
			List<String> foundTables = new ArrayList<String>();
			aResult.beforeFirst();
			while ( aResult.next() ) {
				foundTables.add(aResult.getString("TABLE_NAME"));
			}
			
			String[] tables = myConfig.tableList();

			for ( int i=0; i < tables.length; i++ ) {
				found = ( found && foundTables.contains(tables[i]) );
			}

		} catch (SQLException ex) {
			throw ex;
		} finally {
			aConn.close();				
		}
		return found;
	}
	
	private String validateDB() {
		AppConfig myConfig = this.myWrapper.getAppConfig();
		StringBuffer output = new StringBuffer();
		output.append("<P CLASS='mainContent'><B>Connecting to database</B>...");
		try { 
			DataSource aDS = myConfig.getDataSourceObject();
			Connection aConn = aDS.getConnection();
			output.append("<FONT COLOR='GREEN'><B>Success</FONT></B></P>");
			
			output.append("<P CLASS='mainContent'><B>Checking Schema</B></P>");
			int statusCols = 3;
			TableCell myCell = new TableHeader();

			for ( int i = 0; i < statusCols; i++ ) {
				myCell.addItem("Table");
				myCell.addItem("Status");
			}
			boolean schemaOK = true;
			int version = 0;
			TableRow aRow = new TableRow(myCell);
			try { 
				Statement sth = aConn.createStatement();
				ResultSet result = sth.executeQuery("SELECT value FROM config WHERE element = 'database' AND param ='version'");
				if ( result.first() ) {
					version = result.getInt(1);
				}
				output.append(String.format("<P CLASS='mainContent'><B>Schema version</B>...%d ", version));
				if ( version == DATABASE_VERSION ) {
					output.append("(<FONT COLOR='GREEN'><B>Current</FONT></B>)</P>");
				} else {
					output.append("(<FONT COLOR='RED'><B>Need to update</FONT></B>)</P>");											
				}
				
				DatabaseMetaData dbMeta = aConn.getMetaData();
				String[] types = {"TABLE"};
				ResultSet aResult = dbMeta.getTables(null, null, null, types);
				List<String> foundTables = new ArrayList<String>();
				aResult.beforeFirst();
				while ( aResult.next() ) {
					foundTables.add(aResult.getString("TABLE_NAME"));
				}

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
					aRow.addItem(myCell);
				}

				Table myTable = new Table(aRow);
				myTable.setClass("status");
				output.append(myTable.toString());
				valueMap.put("dbVer", new Integer(version));

				if ( version != DATABASE_VERSION ) {
					output.append("<DIV class='suberror'><P>Incompatible schema version.  Please upgrade using the appropriate SQL file.<BR>See installation instructions for further details.</P></DIV>");					
					valueMap.put("dbOK", Boolean.FALSE);
				} else if ( ! schemaOK ) {
					output.append("<DIV class='suberror'><P>Schema incomplete.  Recreate the schema using the included <A HREF='cyanos-schema.sql'>SQL file</A>.</P></DIV>");					
					valueMap.put("dbOK", Boolean.FALSE);
				} else {
					valueMap.put("dbOK", Boolean.TRUE);
				}
			} catch (SQLException ex) {
				output.append("<BR/><FONT COLOR='RED'>ERROR:</FONT>" + ex.getMessage() + "</B>");
			}
			output.append("</P>");
			aConn.close();
		} catch (ConfigException ex) {
			output.append("<B><FONT COLOR='RED'>ERROR:</FONT>" + ex.getMessage() + "</B></P>");
		} catch (SQLException ex) {
			output.append("<B><FONT COLOR='RED'>ERROR:</FONT>" + ex.getMessage() + "</B></P>");
		}
		return output.toString();
	}
	
}
