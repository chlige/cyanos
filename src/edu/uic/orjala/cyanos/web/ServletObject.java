//
//  ServletObject.java
//  Cyanos
//
//  Created by George Chlipala on 5/7/06.
//  Copyright 2006 University of Illinois at Chicago. All rights reserved.
//
package edu.uic.orjala.cyanos.web;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.mail.Session;
import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import edu.uic.orjala.cyanos.web.forms.SetupForm;
import edu.uic.orjala.cyanos.web.html.Anchor;
import edu.uic.orjala.cyanos.web.html.Header;
import edu.uic.orjala.cyanos.web.html.Popup;
import edu.uic.orjala.cyanos.web.html.Table;
import edu.uic.orjala.cyanos.web.html.TableCell;
import edu.uic.orjala.cyanos.web.html.TableHeader;
import edu.uic.orjala.cyanos.web.html.TableRow;

/**
 * Abstract class for all servlets in application (common methods and attributes)
 */

public abstract class ServletObject extends HttpServlet {
   
	private static final long VERSION = 03L;
	private static final long SUBVERSION= 0L;
	private static final String RELEASE_DATE = "October 15, 2009";
	
	protected DataSource dbh = null;
	protected boolean newInstall = false;
	
	private final static String MODULE_PATH = "java:comp/env/module";
	
	protected static final String SPREADSHEET = "spreadsheet";
	protected static final String UPLOAD_JOB = "upload";

	public void init(ServletConfig config) throws ServletException {
		try {
			super.init(config);
			Context initCtx = new InitialContext();
			ServletContext srvCtx = getServletContext();
			Context envCtx = (Context) initCtx.lookup("java:comp/env");
			CyanosConfig myConf = (CyanosConfig)srvCtx.getAttribute(ServletWrapper.APP_CONFIG_ATTR);
			if ( myConf == null ) {
				String configLocation = (String) envCtx.lookup(ServletWrapper.APP_CONFIG_ATTR);
				if ( configLocation == null ) this.newInstall = true;
				else {
					myConf = new AppConfig((String) envCtx.lookup(ServletWrapper.APP_CONFIG_ATTR));
					srvCtx.setAttribute(ServletWrapper.APP_CONFIG_ATTR, myConf);
				}
			}
			if ( myConf != null ) {
				this.dbh = myConf.getDataSourceObject();
				this.newInstall = ! myConf.configExists();
			}
			initCtx.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	public void doGet ( HttpServletRequest req, HttpServletResponse res ) throws ServletException, IOException {
		CyanosWrapper aWrap = new ServletWrapper(this, this.dbh, req, res);		
		try {
			if ( this.newInstall ) {
				this.newConfig(aWrap);
				return;
			}
			this.display(aWrap);
		} catch (Exception e) {
			throw new ServletException(e);
		}
//		res.flushBuffer();
	}

	@SuppressWarnings("unchecked")
	public void doPost ( HttpServletRequest req, HttpServletResponse res )  throws ServletException, IOException {
		CyanosWrapper aWrap = new ServletWrapper(this, this.dbh, req, res);		
		try {
			if ( this.newInstall ) {
				this.newConfig(aWrap);
				return;
			}
			this.display(aWrap);
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}
	
	protected void display(CyanosWrapper aWrapper) throws Exception {
		throw new ServletException("No display method set for this servlet!");
	}

	private void newConfig(CyanosWrapper aWrapper) throws IOException {
		PrintWriter out = aWrapper.getWriter();

		aWrapper.setContentType("text/html");
		Header myHeader = new Header();
		myHeader.setTitle("Cyanos Database - Application Setup");
		myHeader.addCSSFile(aWrapper.getContextPath() + "/new.css");
		out.println(myHeader);				
		
		SetupForm myForm = new SetupForm(aWrapper);
		out.println(myForm.newConfig());
	}
	
	
/**
 * Generate a list of the species.
 */
	
	@Deprecated
	protected String strainJScript() 
		throws SQLException
	{
		StringBuffer script = new StringBuffer("var strains = new Array();\n");
		Connection dbc = this.dbh.getConnection();
		Statement sth = dbc.createStatement();
		ResultSet results = sth.executeQuery("SELECT culture_id,name FROM species ORDER BY CAST(culture_id as UNSIGNED)");
		results.beforeFirst();
		while (results.next()) {
			script.append("strains[\"" + results.getString(1) + "\"]=\"" + results.getString(2) + "\";\n");
		}
		sth.close();
		dbc.close();
		script.append("function checkStrain(field) {\n if ( field.value != '' && field.value != null && strains[field.value] == null ) { " +
				"field.value=prompt('Strain not found!\\nPlease enter a valid strain ID.',field.value); \n" +
				"checkStrain(field);\n} \n}\n\n");
		return script.toString();	
	}

	/**
	 * @return Popup
	 * @throws SQLException
	 */
	@Deprecated
	public Popup sqlPopup(String sqlString) 
		throws SQLException
	{
		Popup strainPop = new Popup();
		strainPop.addItem("");
		Connection dbc = this.dbh.getConnection();
		Statement sth = dbc.createStatement();
		ResultSet results = sth.executeQuery(sqlString);
		results.beforeFirst();
		while (results.next()) {
			strainPop.addItem(results.getString(1));
		}
		sth.close();
		dbc.close();
		return strainPop;	
	}

	@Deprecated
	public Popup sqlPopupWithLabel(String sqlString) 
		throws SQLException
	{
		Popup strainPop = new Popup();
		strainPop.addItem("");
		Connection dbc = this.dbh.getConnection();
		Statement sth = dbc.createStatement();
		ResultSet results = sth.executeQuery(sqlString);
		results.beforeFirst();
		while (results.next()) {
			strainPop.addItemWithLabel(results.getString(1),results.getString(2));
		}
		sth.close();
		dbc.close();
		return strainPop;	
	}

	@Deprecated
	protected Popup orderPopup() 
		throws SQLException
	{
		Popup aPop = new Popup();
		aPop.addItemWithLabel("", "UNDEFINED");
		Connection dbc = this.dbh.getConnection();
		Statement sth = dbc.createStatement();
		ResultSet results = sth.executeQuery("SELECT DISTINCT ord FROM taxonomic");
		results.beforeFirst();
		while (results.next()) {
			aPop.addItem(results.getString(1));
		}
		sth.close();
		dbc.close();
		return aPop;	
	}
	
/**
 * Generate the menu bar.
 */

	@Deprecated
	public String fileList(String table, String key)
		throws SQLException
	{
		Connection dbc = this.dbh.getConnection();
		Statement sth = dbc.createStatement();
		String selectSQL = "SELECT file,type,description FROM data WHERE tab='" + 
			table + " AND id='" + key + "'";
		ResultSet results = sth.executeQuery(selectSQL);
		results.beforeFirst();
	
		TableRow myRow = new TableRow();
		TableCell myCell = new TableHeader();
		myCell.addItem("Filename");
		myCell.addItem("Type");	
		myCell.addItem("Description");
		myCell.setAttribute("class","header");
		myRow.addItem(myCell);
		Table myTable = new Table(myRow);
		
		String curClass = "odd";
		while (results.next()) {
			myCell = new TableCell();
			myCell.addItem("<A HREF='" + results.getString(1) + "'>" + results.getString(1) + "</A>");
			myCell.addItem(results.getString(2));
			myCell.addItem(results.getString(3));
			TableRow aRow = new TableRow(myCell);
			aRow.setClass(curClass);
			aRow.setAttribute("align", "center");
			myTable.addItem(aRow);
			if ( curClass.equals("odd") ) {
				curClass = "even";
			} else {
				curClass = "odd";
			}
		}
		sth.close();
		dbc.close();
		return myTable.toString();
	}
	
	@Deprecated
	protected List<File> dirArray(File aDir) {
		File[] files = aDir.listFiles();
		ArrayList<File> myList = new ArrayList<File>();
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				myList.addAll(this.dirArray(files[i]));
			} else if (! files[i].isHidden()){
				myList.add(files[i]);
			}
		}		
		return myList;
	}
	
	@Deprecated
	protected String plateForm(int cols, int rows, String baseURL, Map data) {
		return this.plateForm(cols, rows, "location", baseURL, data);
	}

	@Deprecated
	protected String plateForm(int cols, int rows, String fieldName, String baseURL, Map data)  
	{

		TableCell boxHeader = new TableHeader("");
		String[] alphabet = {"", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", 
				"M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
		for ( int i = 1; i <= cols; i++ ) {
			boxHeader.addItem(Integer.toString(i));
		}

		TableRow row = new TableRow(boxHeader);
		boolean first = true;

		for (int r = 1; r <= rows; r++ ) {
			TableCell thisRow = new TableCell();
			for (int c = 1; c <= cols; c++ ) {
				String location = new String(alphabet[r] + Integer.toString(c));
				if (! data.containsKey(location)) {
					if (first) {
						thisRow.addItem("<INPUT TYPE=RADIO NAME='" + fieldName + "' VALUE='" + location + "' CHECKED />");
						first = false;
					} else {
						thisRow.addItem("<INPUT TYPE=RADIO NAME='" + fieldName + "' VALUE='" + location + "' />");
					}
				} else {
					if (baseURL != null) {
						Anchor myLink = new Anchor("<IMG SRC='filled.png' BORDER=0>");
						myLink.setLink(baseURL + data.get(location));
						thisRow.addItem(myLink);				
					} else {
						thisRow.addItem("<IMG SRC='filled.png'>");
					}
				}
			}
			row.addItem("<TH>" + alphabet[r] + "</TH>" + thisRow.toString());
		}
		Table myTable = new Table(row);
		return myTable.toString();
	}
	
	@Deprecated
	protected String plateFromForm(int cols, int rows, String fieldName, Map data){
		return this.plateFromForm(cols, rows, fieldName, data, null);
	}

	@Deprecated
	protected String plateFromForm(int cols, int rows, String fieldName, Map data, Map label)  
	{

		TableCell boxHeader = new TableHeader("");
		String[] alphabet = {"", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", 
				"M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
		for ( int i = 1; i <= cols; i++ ) {
			boxHeader.addItem(Integer.toString(i));
		}

		TableRow row = new TableRow(boxHeader);
		boolean first = true;

		for (int r = 1; r <= rows; r++ ) {
			TableCell thisRow = new TableCell();
			for (int c = 1; c <= cols; c++ ) {
				String location = new String(alphabet[r] + Integer.toString(c));
				if (! data.containsKey(location)) {
					thisRow.addItem("<IMG SRC='images/empty.png'>");
				} else {
					StringBuffer thisCell = new StringBuffer();
					if (first) {
						thisCell.append("<INPUT TYPE=RADIO NAME='" + fieldName + "' VALUE='" + data.get(location) + "' CHECKED />");
						first = false;
					} else {
						thisCell.append("<INPUT TYPE=RADIO NAME='" + fieldName + "' VALUE='" + data.get(location) + "' />");
					}
					if (label != null ) {
						thisCell.append("<BR>");
						thisCell.append(label.get(location));
					}
					thisRow.addItem(thisCell.toString());
				}
			}
			row.addItem("<TH>" + alphabet[r] + "</TH>" + thisRow.toString());
		}
		Table myTable = new Table(row);
		return myTable.toString();
	}

	@Deprecated
	protected String plateTable(int cols, int rows, String baseURL, Map data)  
	{

		TableCell boxHeader = new TableHeader("");
		String[] alphabet = {"", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", 
				"M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
		for ( int i = 1; i <= cols; i++ ) {
			boxHeader.addItem(Integer.toString(i));
		}

		TableRow row = new TableRow(boxHeader);

		for (int r = 1; r <= rows; r++ ) {
			TableCell thisRow = new TableCell();
			for (int c = 1; c <= cols; c++ ) {
				String location = new String(alphabet[r] + Integer.toString(c));
				if (! data.containsKey(location)) {
					thisRow.addItem("<IMG SRC='empty.png' BORDER=0>");
				} else {
					if (baseURL != null) {
						Anchor myLink = new Anchor("<IMG SRC='filled.png' BORDER=0>");
						myLink.setLink(baseURL + data.get(location));
						thisRow.addItem(myLink);				
					} else {
						thisRow.addItem("<IMG SRC='filled.png'>");
					}
				}
			}
			row.addItem("<TH>" + alphabet[r] + "</TH>" + thisRow.toString());
		}
		Table myTable = new Table(row);
		return myTable.toString();
	}

	@Deprecated
	protected Table plateTextTable(int cols, int rows, Map data)  
	{

		TableCell boxHeader = new TableHeader("");
		String[] alphabet = {"", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", 
				"M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
		for ( int i = 1; i <= cols; i++ ) {
			boxHeader.addItem(Integer.toString(i));
		}

		TableRow row = new TableRow(boxHeader);
		String tdWidth = Float.toString(100.0f / rows) + "%";
		for (int r = 1; r <= rows; r++ ) {
			TableCell thisRow = new TableCell();
			thisRow.setAttribute("WIDTH", tdWidth);
			thisRow.setAttribute("ALIGN", "CENTER");
			thisRow.setAttribute("VALIGN", "TOP");
			for (int c = 1; c <= cols; c++ ) {
				String location = new String(alphabet[r] + Integer.toString(c));
				if (! data.containsKey(location)) {
					thisRow.addItem("");
				} else {
					thisRow.addItem(data.get(location));
				}
			}
			row.addItem("<TH WIDTH=3>" + alphabet[r] + "</TH>" + thisRow.toString());
		}
		Table myTable = new Table(row);
		return myTable;
	}
		
	protected String delimOutput (List anArray, String delim)
	{
		ListIterator anIter = anArray.listIterator();
		StringBuffer output = new StringBuffer();
		while ( anIter.hasNext()) {
			List aRow = (ArrayList)anIter.next();
			ListIterator idIter = aRow.listIterator();
			if ( idIter.hasNext() ) {
				output.append(idIter.next().toString());
				while ( idIter.hasNext() ) {
					output.append(delim);
					String myItem = (String)idIter.next();
					if ( myItem != null ) {
						boolean quote = myItem.matches(delim);
						if (quote) output.append("\"");
						output.append(myItem.replaceAll("[\n\r]", " "));
						if (quote) output.append("\"");
					}
				}
				output.append("\n");
			}
		}
		return output.toString();
	}
		
	public String getServletInfo() {
		return String.format("Cyanos database v%d.%d By: George Chlipala, %s", VERSION, SUBVERSION, RELEASE_DATE);
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.CyanosServlet#getFormValue(java.lang.String)
	 */
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.CyanosServlet#getDBC()
	 */
	@Deprecated
	protected Connection getDBC() throws SQLException {
		return this.dbh.getConnection();
	}
		
	protected Session getMailSession() throws NamingException {		
		Context initCtx = new InitialContext();
		Context envCtx = (Context) initCtx.lookup("java:comp/env");
		Session session = (Session) envCtx.lookup("mail/Session");
		return session;
	}

	
	/*
	@Deprecated
	protected String collectionPopup(String formParameter, boolean reload) {
		try {
			Popup aPop = new Popup();
			aPop.setName(formParameter);
			if ( reload )
				aPop.setAttribute("onChange", "this.form.submit()");
			aPop.addItem("");
			List<String> libs = SQLSampleCollection.libraries(this.getSQLDataSource());
			if ( this.formValues.containsKey(formParameter) ) {
				aPop.setDefault(this.getFormValue(formParameter));
			}
			Iterator<String> anIter = libs.iterator();
			String aLib;
			
			while ( anIter.hasNext() ) {
				aLib = anIter.next();
				PopupGroup aGroup = new PopupGroup(aLib);
				SampleCollection cols = SQLSampleCollection.loadForLibrary(this.getSQLDataSource(), aLib);
				if ( cols != null ) {
					cols.beforeFirst();
					while ( cols.next() ) {
						aGroup.addItemWithLabel(cols.getID(), cols.getName());
					} 
				}
				aPop.addGroup(aGroup);
			}
			return aPop.toString();
		} catch (DataException e) {
			e.printStackTrace();
			return("<B><FONT COLOR='red'>ERROR:</FONT> " + e.getMessage() + "</B>");
		}
	}
	
	@Deprecated
	protected Map<String,String> buildTemplate(String[] tempKeys) {
		Map<String, String> newTemplate = new HashMap<String, String>();
		for (int i = 0; i < tempKeys.length; i++ ) {
			if ( formValues.containsKey(tempKeys[i])) {
				newTemplate.put(tempKeys[i], this.getFormValue(tempKeys[i]));
			}
		}
		return newTemplate;
	}

	@Deprecated
	protected Popup protocolPopup(String protocol) throws SQLException {
		Connection aDBC = null;
		Popup aPop = new Popup();
		SQLException execp = null;

		try {
			aDBC = this.dbh.getConnection();
			Statement sth = aDBC.createStatement();
			ResultSet results = sth.executeQuery(String.format("SELECT name FROM data_templates WHERE data='%s'",protocol));
			results.beforeFirst();
			aPop.addItemWithLabel("", "NONE");
			while (results.next()) {
				aPop.addItem(results.getString(1));
			}
			results.close();
			sth.close();
		} catch (SQLException e) {
			execp = e;
		}
		
		try {
			if ( aDBC != null && (! aDBC.isClosed()) ) 
				aDBC.close();
		} catch (SQLException e) {
			this.log("Failed to close database connection", e);
		}

		if ( execp != null ) throw execp;
		return aPop;
	}
	
	@Deprecated
	protected Popup projectPopup() throws DataException {
		Project allProjs = SQLProject.projects(this.getSQLDataSource(), SQLProject.ID_COLUMN, SQLProject.ASCENDING_SORT);
		allProjs.beforeFirst();
		Popup aPop = new Popup();
		aPop.addItemWithLabel("", "NONE");
		while ( allProjs.next() ) {
			aPop.addItemWithLabel(allProjs.getID(), String.format("%s (%s)", allProjs.getName(), allProjs.getID()));
		}
		return aPop;
	}
	*/
	@SuppressWarnings("unchecked")
	public List getWebModules() throws NamingException {
		List retList = new ArrayList<WebModule>();
		InitialContext initCtx = new InitialContext();
		NamingEnumeration<Binding> moduleEnum = initCtx.listBindings(MODULE_PATH);
		while ( moduleEnum.hasMore() ) {
			Binding aBinding = moduleEnum.next();
			retList.add(aBinding.getObject());
		}
		return retList;
	}
		
	protected CyanosConfig getAppConfig() {
		ServletContext aCtx = this.getServletContext();
		CyanosConfig aConfig = (CyanosConfig)aCtx.getAttribute(ServletWrapper.APP_CONFIG_ATTR);
		return aConfig;
	}

	protected String getHelpModule() {
		return null;
	}

}

