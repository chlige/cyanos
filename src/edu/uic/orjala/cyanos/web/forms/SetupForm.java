/**
 * 
 */
package edu.uic.orjala.cyanos.web.forms;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import edu.uic.orjala.cyanos.Sample;
import edu.uic.orjala.cyanos.Separation;
import edu.uic.orjala.cyanos.Strain;
import edu.uic.orjala.cyanos.web.BaseForm;
import edu.uic.orjala.cyanos.web.CyanosConfig;
import edu.uic.orjala.cyanos.web.CyanosWrapper;
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
	
	private final static String PAGE_VALUE = "page";
	private final static String APP_CONFIG_ATTR = "app_config";
	private final static String DATA_DS_VALUE = "dataDS";
	private final static String ADMIN_LOGIN_VALUE = "adminLogin";
	private final static String PASSWD1_VALUE = "pass1";
	private final static String PASSWD2_VALUE = "pass2";
	
	private final static String PREV_PAGE = "prevPage";
	private final static String NEXT_PAGE = "nextPage";
	private final static String FINISH_ACTION = "finish";
	
	private final static String[] PAGE_1_FIELDS = { SetupForm.DATA_DS_VALUE };
	private final static String[] PAGE_2_FIELDS = { SetupForm.ADMIN_LOGIN_VALUE, SetupForm.PASSWD1_VALUE, SetupForm.PASSWD2_VALUE };
	
	private Map<String,Object> valueMap = null;
	
	public SetupForm(CyanosWrapper aWrapper) {
		super(aWrapper);
	}
	
	@SuppressWarnings("unchecked")
	public String newConfig() {
		
		CyanosConfig myConfig = this.myWrapper.getAppConfig();
		if ( myConfig == null ) {
			return this.warnPage();
		}
		
		int page = 0;
		if ( this.hasFormValue(SetupForm.PAGE_VALUE)) {
			page = Integer.parseInt(this.getFormValue(SetupForm.PAGE_VALUE));
		}
		
		HttpSession aSession = this.myWrapper.getSession();
		valueMap = (Map)aSession.getAttribute(SetupForm.APP_CONFIG_ATTR);
		if ( valueMap == null ) {
			valueMap = new HashMap<String,Object>();
			aSession.setAttribute(SetupForm.APP_CONFIG_ATTR, valueMap);
		}
		String[] params = {};
		
		switch (page) {
		case 1: params = SetupForm.PAGE_1_FIELDS; break;
		case 2: params = SetupForm.PAGE_2_FIELDS; break;
	//	case 3: ; break;
		//case 4: ; break;
		}
		
		for ( int i = 0 ; i < params.length; i++ ) {
			if ( this.hasFormValue(params[i]) )
			valueMap.put(params[i], this.getFormValue(params[i]));
		}

		if ( this.hasFormValue(SetupForm.PREV_PAGE) ) {
			page = page - 1;
		} 
		if ( this.hasFormValue(SetupForm.NEXT_PAGE) ) {
			page = page + 1;
		}
		
		if ( this.hasFormValue(SetupForm.FINISH_ACTION) ) {
			return (this.newPage5());
		}
		
		switch (page) {
		case 0: return this.newPage0();
		case 1: return this.newPage1(); 
		case 2: return this.newPage2();
		case 3: return this.newPage3();
		case 4: return this.newPage4();
		}
		return "";
	}
	
	private String newPage0() {
		StringBuffer output = new StringBuffer("<DIV ID='sideNav'>");
		HtmlList aList = new HtmlList();
		aList.ordered();
		aList.addItem("<B>Welcome</B>");
		aList.addItem("<A HREF='?page=1'>Setup Database</A>");
		aList.addItem("<A HREF='?page=2'>Setup Administrator</A>");
		aList.addItem("<A HREF='?page=3'>Setup User Accounts</A>");
		aList.addItem("<A HREF='?page=4'>Datafile Configuration</A>");
		
		output.append(aList.toString());
		output.append("</DIV><DIV ID='mainPanel'><FORM METHOD='POST' ACTION='main'/><INPUT TYPE='HIDDEN' NAME='page' VALUE='0'/>");
		output.append("<P ALIGN='CENTER'><FONT SIZE='+2'><B>Welcome!</B></FONT></P>");
		output.append("<P CLASS='mainContent'>Welcome to Cyanos, a natural product drug discovery information management system.  ");
		output.append("This system allows one to store and manage data related to:");
		output.append("<UL><LI>Taxonomic data</LI><LI>Culture information, e.g. inoculations, harvest, and cryopreservations.</LI>");
		output.append("<LI>Extraction and fractionation data.</LI><LI>Sample libraries</LI><LI>Bioassay data</LI>");
		output.append("<LI>Datafile management, e.g. LC-UV, MS, and NMR data.</LI></UL></P>");
		output.append("<TABLE CLASS='buttons'><TR><TD><INPUT TYPE='SUBMIT' NAME='nextPage' VALUE='Next &gt;'/></TD></TR></TABLE>");
		
		output.append("</FORM></DIV>");
		return output.toString();
	}
	
	private String warnPage() {
		StringBuffer output = new StringBuffer("<DIV ID='sideNav'>");
		HtmlList aList = new HtmlList();
		aList.ordered();
		aList.addItem("<B>Welcome</B>");
		aList.addItem("<A HREF='?page=1'>Setup Database</A>");
		aList.addItem("<A HREF='?page=2'>Setup Administrator</A>");
		aList.addItem("<A HREF='?page=3'>Setup User Accounts</A>");
		aList.addItem("<A HREF='?page=4'>Datafile Configuration</A>");
		
		output.append(aList.toString());
		output.append("</DIV><DIV ID='mainPanel'><FORM METHOD='POST' ACTION='main'/>");
		output.append("<P ALIGN='CENTER'><FONT SIZE='+2'><B>SETUP INCOMPLETE</B></FONT></P>");
		output.append("<P CLASS='mainContent'>The web appplication container not properly setup.  Ensure the <CODE>cyanosAppConfig</CODE> ");
		output.append("Environment Entry is setup in the Web Application Server context for this application.");
		output.append("Also ensure that the proper JDBC Datasource(s) are available.</P>");
		output.append("<P CLASS='mainContent'><DL><DT>cyanosAppConfig</DT><DD>Type: java.lang.String<BR/>");
		output.append("Value: Location for applicaiton configuration file, e.g. /etc/tomcat5/cyanos-app.conf</DD></DL></P>");
		output.append("<TABLE CLASS='buttons'><TR><TD><INPUT TYPE='SUBMIT' NAME='go' VALUE='Next &gt;'/></TD></TR></TABLE>");
		
		output.append("</FORM></DIV>");
		return output.toString();
	}
	
	private String newPage1() {
		StringBuffer output = new StringBuffer("<DIV ID='sideNav'>");
		
		HtmlList aList = new HtmlList();
		aList.ordered();
		aList.addItem("<A HREF='?page=0'>Welcome</A>");
		aList.addItem("<B>Setup Database</B>");
		aList.addItem("<A HREF='?page=2'>Setup Administrator</A>");
		aList.addItem("<A HREF='?page=3'>Setup User Accounts</A>");
		aList.addItem("<A HREF='?page=4'>Datafile Configuration</A>");

		output.append(aList.toString());
		output.append("</DIV><DIV ID='mainPanel'><FORM METHOD='POST' ACTION='main'/><INPUT TYPE='HIDDEN' NAME='page' VALUE='1'/>");
		output.append("<P ALIGN='CENTER'><FONT SIZE='+2'><B>Database Setup</B></FONT></P>");
		output.append("<P CLASS='mainContent'>Please enter the information for your database. Setup the datasource configurations in the web application configuration.");

		boolean update = this.hasFormValue("update1");
		
		TableCell myCell = new TableCell("Data Store:");
		Popup aPop = new Popup();
		try {
			CyanosConfig myConfig = this.myWrapper.getAppConfig();
			Context initCtx = new InitialContext();
			NamingEnumeration jdbcEnum = initCtx.list("java:comp/env/jdbc");
			while ( jdbcEnum.hasMore() ) {
				NameClassPair aName = (NameClassPair) jdbcEnum.next();
				aPop.addItemWithLabel(aName.getName(), aName.getName());
			}
			aPop.setName("dataDS");
			if ( valueMap.containsKey("dataDS") ) {
				aPop.setDefault((String)valueMap.get("dataDS"));
			}
			myCell.addItem(aPop.toString());

			TableRow aRow = new TableRow();
			aRow.addItem(myCell);
			Table myTable = new Table(aRow);

			output.append("<P CLASS='mainContent'>");
			output.append(myTable.toString());
			output.append("</P>");
			output.append("<P CLASS='mainContent'><INPUT TYPE='SUBMIT' NAME='update1' VALUE='Update Configuration'/></P>");

			if ( update ) {
				DataSource aDS = (DataSource)initCtx.lookup("java:comp/env/jdbc/" + (String)valueMap.get("dataDS"));
				output.append("<P CLASS='mainContent'><B>Connecting to database</B>...");
				try { 
					Connection aConn = aDS.getConnection();
					output.append("<FONT COLOR='GREEN'><B>Success</FONT></B></P>");
					output.append("<P CLASS='mainContent'><B>Checking Schema...</B>");
					int statusCols = 3;
					myCell = new TableHeader();

					for ( int i = 0; i < statusCols; i++ ) {
						myCell.addItem("Table");
						myCell.addItem("Status");
					}
					boolean schemaOK = true;
					aRow = new TableRow(myCell);
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

						myTable = new Table(aRow);
						myTable.setClass("status");
						output.append(myTable.toString());
						if ( ! schemaOK ) {
							output.append("Schema not up to date. <INPUT TYPE='SUBMIT' NAME='createSchema' VALUE='Create Database Schema'/>");
							valueMap.remove("dbOK");
						} else {
							valueMap.put("dbOK", "TRUE");
						}
					} catch (SQLException ex) {
						output.append("<BR/><FONT COLOR='RED'>ERROR:</FONT>" + ex.getMessage() + "</B>");
					}
					output.append("</P>");
					aConn.close();
				} catch (SQLException ex) {
					output.append("<B><FONT COLOR='RED'>ERROR:</FONT>" + ex.getMessage() + "</B></P>");
				}

			}

		} catch (Exception e) {
			output.append("<P ALIGN='CENTER'><B><FONT COLOR='RED'>ERROR:</FONT>" + e.getMessage() + "</B></P>");
			e.printStackTrace();
		}

		if ( update || valueMap.containsKey("dbOK") ) 
			output.append("<TABLE CLASS='buttons'><TR><TD><INPUT TYPE='SUBMIT' NAME='prevPage' VALUE='&lt; Previous'/></TD><TD><INPUT TYPE='SUBMIT' NAME='nextPage' VALUE='Next &gt;'/></TD></TR></TABLE>");		
		else 
			output.append("<TABLE CLASS='buttons'><TR><TD><INPUT TYPE='SUBMIT' NAME='prevPage' VALUE='&lt; Previous'/></TD><TD><INPUT TYPE='SUBMIT' NAME='update1' VALUE='Next &gt;'/></TD></TR></TABLE>");		
		output.append("</FORM></DIV>");
		return output.toString();
		
	}
	
	private String newPage2() {
		StringBuffer output = new StringBuffer("<DIV ID='sideNav'>");
		HtmlList aList = new HtmlList();
		aList.ordered();
		aList.addItem("<A HREF='?page=0'>Welcome</A>");
		aList.addItem("<A HREF='?page=1'>Setup Database</A>");
		aList.addItem("<B>Setup Administrator</B>");
		aList.addItem("<A HREF='?page=3'>Setup User Accounts</A>");
		aList.addItem("<A HREF='?page=4'>Datafile Configuration</A>");

		output.append(aList.toString());
		output.append("</DIV><DIV ID='mainPanel'><FORM METHOD='POST' ACTION='main'/><INPUT TYPE='HIDDEN' NAME='page' VALUE='2'/>");
		output.append("<P ALIGN='CENTER'><FONT SIZE='+2'><B>Setup Administrator Account</B></FONT></P>");
		output.append("<P CLASS='mainContent'>Create an account for the management of this application.  <B>NOTE:</B> This user will only be given right to change the configuration of the application and will not be granted access rights to the data.");
		output.append("</P><P CLASS='mainContent'>");
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
	
	@SuppressWarnings("unchecked")
	private String newPage3() {
		StringBuffer output = new StringBuffer("<DIV ID='sideNav'>");

		HtmlList aList = new HtmlList();
		aList.ordered();
		aList.addItem("<A HREF='?page=0'>Welcome</A>");
		aList.addItem("<A HREF='?page=1'>Setup Database</A>");
		aList.addItem("<A HREF='?page=2'>Setup Administrator</A>");
		aList.addItem("<B>Setup User Accounts</B>");
		aList.addItem("<A HREF='?page=4'>Datafile Configuration</A>");

		output.append(aList.toString());
		output.append("</DIV><DIV ID='mainPanel'><FORM METHOD='POST' ACTION='main'/><INPUT TYPE='HIDDEN' NAME='page' VALUE='3'/>");
		output.append("<P ALIGN='CENTER'><FONT SIZE='+2'><B>Setup Initial User Account</B></FONT></P>");
		output.append("<P CLASS='mainContent'>Create initial user accounts. <B>NOTE:</B> User accounts can be created after setup of the application.  Login using the adminstrator account created in the previous step and use the user administration interface to add, modify, or remove user accounts.</P>");

		List<Map<String,String>> userList;
		if ( valueMap.containsKey("users") ) {
			userList = (ArrayList)valueMap.get("users");
		} else {
			userList = new ArrayList<Map<String,String>>();
			valueMap.put("users", userList);
			if ( valueMap.containsKey("dbOK") ) {
				output.append("<P CLASS='mainContent'>Attempting to find existing user accounts...");
				try { 
					Context initCtx = new InitialContext();
					DataSource aDS = (DataSource)initCtx.lookup("java:comp/env/jdbc/" + (String)valueMap.get("dataDS"));
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
			String pass1 = this.getFormValue("upass1");
			String pass2 = this.getFormValue("upass2");
			if ( pass1 == null || (! pass1.equals(pass2)) ) {
				output.append("<P CLASS='mainContent'><FONT COLOR='RED'><B>Password Mismatch!</FONT> User not added</B></P>");
			} else {
				Map<String,String> aUser = new HashMap<String, String>();
				aUser.put("login", this.getFormValue("login"));
				aUser.put("name", this.getFormValue("name"));
				aUser.put("email", this.getFormValue("email"));
				aUser.put("pwd", this.getFormValue("pass1"));
				userList.add(aUser);
			}
		}
		
		output.append("<P CLASS='mainContent'>");
		
		if (userList.size() > 0 ) {
			String headers[] = { "Login", "Name", "Email" };
			TableCell myCell = new TableHeader(headers);
			myCell.setAttribute("ALIGN", "LEFT");
			TableRow aRow = new TableRow(myCell);
			
			ListIterator userIter = userList.listIterator();
			
			while ( userIter.hasNext()) {
				Map aUser = (HashMap)userIter.next();
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
		aRow.addItem("<TD>Email:</TD><TD><INPUT TYPE='TEXT' NAME='name' SIZE='25'/></TD>");
		aRow.addItem("<TD>Password:</TD><TD><INPUT TYPE='PASSWORD' NAME='upass1'/></TD>");
		aRow.addItem("<TD>Confirm Password:</TD><TD><INPUT TYPE='PASSWORD' NAME='upass2'/></TD>");
		aRow.addItem("<TD COLSPAN='2'><INPUT TYPE='SUBMIT' NAME='addUser' VALUE='Add User'/></TD>");
		
		Table myTable = new Table(aRow);
		output.append(myTable.toString());
		
		output.append("<TABLE CLASS='buttons'><TR><TD><INPUT TYPE='SUBMIT' NAME='prevPage' VALUE='&lt; Previous'/></TD><TD><INPUT TYPE='SUBMIT' NAME='nextPage' VALUE='Next &gt;'/></TD></TR></TABLE>");		
		output.append("</FORM></DIV>");
		return output.toString();
		
	}
	
	private String newPage4() {
		StringBuffer output = new StringBuffer("<DIV ID='sideNav'>");
		HtmlList aList = new HtmlList();
		aList.ordered();
		aList.addItem("<A HREF='?page=0'>Welcome</A>");
		aList.addItem("<A HREF='?page=1'>Setup Database</A>");
		aList.addItem("<A HREF='?page=2'>Setup Administrator</A>");
		aList.addItem("<A HREF='?page=3'>Setup User Accounts</A>");
		aList.addItem("<B>Datafile Configuration</B>");

		output.append(aList.toString());
		output.append("</DIV><DIV ID='mainPanel'><FORM METHOD='POST' ACTION='main'/><INPUT TYPE='HIDDEN' NAME='page' VALUE='4'/>");
		output.append("<P ALIGN='CENTER'><FONT SIZE='+2'><B>Configure Datafile Paths</B></FONT></P>");
		output.append("<P CLASS='mainContent'>Define directory paths on the server to store photos and datafiles.  An asterix (*) can be used as a wildcard to class or type values.</P>");

		output.append("<P CLASS='mainContent'>");

		CyanosConfig myConf = this.myWrapper.getAppConfig();
		
		if ( this.hasFormValue("addPath") ) {
			myConf.setFilePath(this.getFormValue("class"), this.getFormValue("type"), this.getFormValue("path"));
		}
		
		Map fileMap = myConf.getFilePathMap();
		
		Iterator classIter = fileMap.keySet().iterator();
		
		String[] headers = {"Class", "Type", "Path"};
		TableCell aCell = new TableHeader(headers);
		TableRow aRow = new TableRow(aCell);
		
		while ( classIter.hasNext() ) {
			String aClass = (String)classIter.next();
			Map classMap = (Map)fileMap.get(aClass);
			Iterator typeIter = classMap.keySet().iterator();
			TableCell myCell = new TableCell(aClass);
			myCell.setAttribute("COLSPAN", "3");
			aRow.addItem(myCell);
			while ( typeIter.hasNext() ) {
				String aType = (String) typeIter.next();
				myCell = new TableCell(" ");
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

	private String newPage5() {
		StringBuffer output = new StringBuffer("<DIV ID='sideNav'>");
		
		HtmlList aList = new HtmlList();
		aList.ordered();
		aList.addItem("<A HREF='?page=0'>Welcome</A>");
		aList.addItem("<A HREF='?page=1'>Setup Database</A>");
		aList.addItem("<A HREF='?page=2'>Setup Administrator</A>");
		aList.addItem("<A HREF='?page=3'>Setup User Accounts</A>");
		aList.addItem("<A HREF='?page=4'>Datafile Configuration</A>");

		output.append(aList.toString());
		output.append("</DIV><DIV ID='mainPanel'><FORM METHOD='POST' ACTION='main'/><INPUT TYPE='HIDDEN' NAME='finish' VALUE='4'/>");
		
		if ( this.hasFormValue("commit") ) {
			output.append("<P ALIGN='CENTER'><FONT SIZE='+2'><B>Saving Configuration...</B></FONT>");
			CyanosConfig myConf = this.myWrapper.getAppConfig();
			
			myConf.setDataSource((String)valueMap.get("dataDS"));
			try {
				myConf.writeConfig();
				output.append("<FONT SIZE='+1' COLOR='GREEN'><B>SUCCESS</B></FONT></P>");
				output.append("<P CLASS='mainContent'>The application is now configured for use!<BR/>Please restart the servlet container!<BR/>");
				output.append(String.format("<A HREF='/manager/html/reload?path=%s'>Tomcat Restart</A></P>",this.myWrapper.getContextPath()));
				return output.toString();
			} catch (Exception e) {
				output.append("<FONT SIZE='+1' COLOR='RED'><B>WRITE FAILED</B></FONT></BR>");
				output.append(e.getMessage());
				output.append("</P>");
				e.printStackTrace();
			} 

		} else {
			output.append("<P ALIGN='CENTER'><FONT SIZE='+2'><B>Confirm and Commit Configuration</B></FONT></P>");
			output.append("<P CLASS='mainContent'>Confirm the settings for the application then click the \"Commit\" button to save the changes.</P>");
		}
		TableRow aRow = new TableRow("<TH COLSPAN=2>Database</TH>");
		TableCell myCell = new TableCell("Datasource Name:");
		myCell.addItem(valueMap.get("dataDS"));
		aRow.addItem(myCell);

		aRow.addItem("<TH COLSPAN=2>Administrator Account</TH>");
		myCell = new TableCell("Login:");
		myCell.addItem(valueMap.get("adminLogin"));
		aRow.addItem(myCell);

		aRow.addItem("<TH COLSPAN=2>User Accounts</TH>");
		int newAcct = 0;
		int oldAcct = 0;

		List userList = (ArrayList)valueMap.get("users");
		ListIterator userIter = userList.listIterator();

		while ( userIter.hasNext() ) {
			Map aUser = (Map)userIter.next();
			if (aUser.containsKey("pwd")) newAcct++;
			else oldAcct++;
		}

		myCell = new TableCell("New Accounts:");
		myCell.addItem(String.format("%d", newAcct));
		aRow.addItem(myCell);

		myCell = new TableCell("Existing Accounts:");
		myCell.addItem(String.format("%d", oldAcct));
		aRow.addItem(myCell);

		aRow.addItem("<TH COLSPAN=2>Data File Paths</TH>");

		Table myTable = new Table(aRow);

		output.append("<P CLASS='mainContent'>");
		output.append(myTable);
		output.append("</P><TABLE CLASS='buttons'><TR><TD><INPUT TYPE='SUBMIT' NAME='commit' VALUE='Commit'/></TD></TR></TABLE>");	

		output.append("</FORM></DIV>");

		return output.toString();
	}
	
}
