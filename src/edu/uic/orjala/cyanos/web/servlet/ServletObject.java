//
//  ServletObject.java
//  Cyanos
//
//  Created by George Chlipala on 5/7/06.
//  Copyright 2006 University of Illinois at Chicago. All rights reserved.
//
package edu.uic.orjala.cyanos.web.servlet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.mail.Session;
import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.User;
import edu.uic.orjala.cyanos.sql.SQLData;
import edu.uic.orjala.cyanos.web.AppConfig;
import edu.uic.orjala.cyanos.web.CyanosWrapper;
import edu.uic.orjala.cyanos.web.GuestUser;
import edu.uic.orjala.cyanos.web.JobManager;
import edu.uic.orjala.cyanos.web.ServletWrapper;
import edu.uic.orjala.cyanos.web.SheetWriter;
import edu.uic.orjala.cyanos.web.WebModule;
import edu.uic.orjala.cyanos.web.html.Anchor;
import edu.uic.orjala.cyanos.web.html.Popup;
import edu.uic.orjala.cyanos.web.html.Table;
import edu.uic.orjala.cyanos.web.html.TableCell;
import edu.uic.orjala.cyanos.web.html.TableHeader;
import edu.uic.orjala.cyanos.web.html.TableRow;
import edu.uic.orjala.cyanos.web.listener.AppConfigListener;
import edu.uic.orjala.cyanos.web.listener.CyanosRequestListener;
import edu.uic.orjala.cyanos.web.listener.CyanosSessionListener;
import edu.uic.orjala.cyanos.web.listener.UploadManager.FileUpload;

/**
 * Abstract class for all servlets in application (common methods and attributes)
 */

public abstract class ServletObject extends HttpServlet {
   
	/**
	 * 
	 */
	private static final long serialVersionUID = 2482550398196439329L;
	protected static final long VERSION = 01L;
	protected static final long SUBVERSION = 5L;
	protected static final String RELEASE_DATE = "June 20, 2013";
	protected static final int DATABASE_VERSION = 3;
	
//	protected DataSource dbh = null;
//	protected boolean newInstall = false;
//	protected boolean upgradeInstall = false;
//	protected String configXMLFile;
//	protected int idtype = SQLData.ID_TYPE_SERIAL;

	public final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMMMM d, yyyy");
	public final static SimpleDateFormat CALFIELD_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	public final static SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("MMMMM d, yyyy hh:mm a");

	private final static String MODULE_PATH = "java:comp/env/module";
	
	public static final String SPREADSHEET = "spreadsheet";
	public static final String UPLOAD_FORM = "uploadForm";
	public static final String DATASOURCE = "datasource";
	
	public static final String SESS_ATTR_DATE_FORMAT = "dateFormatter";
	public static final String SESS_ATTR_USER = "cyanosUser";
	public static final String REQ_ATTR_UPLOADS = "uploadFiles";
	public static final String DB_CONN = "datasource_conn";

	public static final String APP_CONFIG_ATTR = "cyanosAppConfig";
	
/*
	public void init(ServletConfig config) throws ServletException {
		try {
			super.init(config);
			Context initCtx = new InitialContext();
			ServletContext srvCtx = getServletContext();
			AppConfig myConf = (AppConfig)srvCtx.getAttribute(APP_CONFIG_ATTR);
			if ( myConf == null ) {
				this.newInstall = true;
//				throw new ServletException("CYANOS application configuration not initialized.");
			}
			if ( myConf != null ) {	
				this.dbh  = (DataSource)initCtx.lookup("java:comp/env/jdbc/" + AppConfig.CYANOS_DB_NAME);
				this.idtype = AppConfigSQL.getSchemaIDType(dbh);
				this.newInstall = ! myConf.configExists();
				if ( this.newInstall ) {
					try {
						Context envCtx = (Context) initCtx.lookup("java:comp/env");
						this.configXMLFile = (String) envCtx.lookup(APP_CONFIG_ATTR);
					} catch (NamingException e) {
						// TODO should report that it is not found.
					}
				} else {
					this.upgradeInstall = ( AppConfig.APP_VERSION > ((AppConfig)myConf).getVersion() );
				}
			}
			initCtx.close();
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}
	*/
	
	protected void doGet ( HttpServletRequest req, HttpServletResponse res ) throws ServletException, IOException {
		CyanosWrapper aWrap = new ServletWrapper(this, req, res);		
		try {
			this.setupSession(req);

			this.display(aWrap);
		} catch (Exception e) {
			throw new ServletException(e);
		}
//		res.flushBuffer();
	}
	
	protected void doPost ( HttpServletRequest req, HttpServletResponse res )  throws ServletException, IOException {
		CyanosWrapper aWrap = new ServletWrapper(this, req, res);		
		try {
			this.setupSession(req);
			
			this.display(aWrap);
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}
	
	@Deprecated
	protected void display(CyanosWrapper aWrapper) throws Exception {
		// throw new ServletException("No display method set for this servlet!");
	}

/**
 * Generate a list of the species.
 */
	
	@Deprecated
	protected String strainJScript() 
		throws SQLException
	{
		StringBuffer script = new StringBuffer("var strains = new Array();\n");
		Connection dbc = AppConfigListener.getDBConnection();
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
		Connection dbc = AppConfigListener.getDBConnection();
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
		Connection dbc = AppConfigListener.getDBConnection();
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
		Connection dbc = AppConfigListener.getDBConnection();
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
		Connection dbc = AppConfigListener.getDBConnection();
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
	protected String plateForm(int cols, int rows, String baseURL, Map<String, String> data) {
		return this.plateForm(cols, rows, "location", baseURL, data);
	}

	@Deprecated
	protected String plateForm(int cols, int rows, String fieldName, String baseURL, Map<String, String> data)  
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
	protected String plateFromForm(int cols, int rows, String fieldName, Map<String, String> data){
		return this.plateFromForm(cols, rows, fieldName, data, null);
	}

	@Deprecated
	protected String plateFromForm(int cols, int rows, String fieldName, Map<String, String> data, Map<String, String> label)  
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
					thisRow.addItem("<IMG SRC='empty.png'>");
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
	protected String plateTable(int cols, int rows, String baseURL, Map<String, String> data)  
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
	protected Table plateTextTable(int cols, int rows, Map<String, String> data)  
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
		
	@Deprecated
	protected String delimOutput (List<List<String>> anArray, String delim) {
		Iterator<List<String>> anIter = anArray.listIterator();
		StringBuffer output = new StringBuffer();
		while ( anIter.hasNext()) {
			List<String> aRow = anIter.next();
			ListIterator<String> idIter = aRow.listIterator();
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

	protected static String quoteString(String value, String delim) {
		if ( value == null ) return "";
		value = value.replaceAll("[\n\r]", " ");
		boolean quote = value.contains(delim);
		if (quote) {
			StringBuffer output = new StringBuffer("\"");
			output.append(value);
			output.append("\"");
			return output.toString();
		}
		return value;
	}
	 
	protected void delimOutput (List<List<String>> anArray, String delim, PrintWriter out) {		
		Iterator<List<String>> anIter = anArray.listIterator();
		while ( anIter.hasNext()) {
			List<String> aRow = anIter.next();
			ListIterator<String> idIter = aRow.listIterator();
			if ( idIter.hasNext() ) {
				out.print(idIter.next().toString());
				while ( idIter.hasNext() ) {
					out.print(delim);
					String myItem = (String)idIter.next();
					if ( myItem != null ) {
						boolean quote = myItem.matches(delim);
						if (quote) out.print("\"");
						out.print(myItem.replaceAll("[\n\r]", " "));
						if (quote) out.print("\"");
					}
				}
				out.println();
			}
		}
	}
	
	protected void delimOutput (List<List<String>> anArray, SheetWriter out) {
		Iterator<List<String>> anIter = anArray.iterator();
		while ( anIter.hasNext() ) {
			List<String> row = anIter.next();
			ListIterator<String> rowIter = row.listIterator();
			int size = row.size();
			while ( rowIter.hasNext() ) {
				if (rowIter.nextIndex() == size)
					out.println(rowIter.next());
				else
					out.print(rowIter.next());
			}
		}
	}

	public String getServletInfo() {
		return servletInfo();
	}
	
	public static String servletInfo() {
		return String.format("Cyanos database v%d.%d By: George Chlipala, %s", VERSION, SUBVERSION, RELEASE_DATE);
	}
	
	public static Session getMailSession() throws NamingException {		
		Context initCtx = new InitialContext();
		return (Session) initCtx.lookup("java:comp/env/mail/Session");
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
			aDBC = AppConfigListener.getDBConnection();
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
	public List<WebModule> getWebModules() throws NamingException {
		ArrayList<WebModule> retList = new ArrayList<WebModule>();
		InitialContext initCtx = new InitialContext();
		NamingEnumeration<Binding> moduleEnum = initCtx.listBindings(MODULE_PATH);
		while ( moduleEnum.hasMore() ) {
			Binding aBinding = moduleEnum.next();
			retList.add((WebModule)aBinding.getObject());
		}
		return retList;
	}
		
	protected AppConfig getAppConfig() {
		ServletContext aCtx = this.getServletContext();
		AppConfig aConfig = (AppConfig)aCtx.getAttribute(APP_CONFIG_ATTR);
		return aConfig;
	}

	public String getHelpModule() {
		return null;
	}
	
	protected static User getGuestUser() {
		String[] roles = { User.CULTURE_ROLE };
		String[] projects = { GuestUser.GLOBAL_PROJECT };
		return new GuestUser(roles, projects);
	}

	protected void setupSession(HttpServletRequest req) throws SQLException, DataException {
		HttpSession session = req.getSession();

//		Connection conn = (Connection) session.getAttribute(DB_CONN);
//		if ( conn == null || conn.isClosed() ) {
//			conn = AppConfigListener.getDBConnection();
//			session.setAttribute(DB_CONN, conn);
//		}
		
//		SQLData data = this.newSQLData(req, conn);
		
//		req.setAttribute(DATASOURCE, data);

		if ( session.getAttribute(SESS_ATTR_DATE_FORMAT) == null ) 
			session.setAttribute(SESS_ATTR_DATE_FORMAT, DATE_FORMAT);

//		if ( session.getAttribute(SESS_ATTR_USER) == null ) {
//			session.setAttribute(SESS_ATTR_USER, data.getUser());
//		}		
	}

	public static SQLData getSQLData(HttpServletRequest req) throws SQLException, DataException {
		return CyanosRequestListener.getSQLData(req);
	}
	
	public static SQLData newSQLData(HttpServletRequest req, Connection conn) throws DataException, SQLException {
		return newSQLData(req);
	}

	public static SQLData newSQLData(HttpServletRequest req) throws DataException, SQLException {
		return CyanosRequestListener.newSQLData(req);
	}

	public static User getUser(HttpServletRequest req) throws SQLException, DataException {
		return getSQLData(req).getUser();
	}
	
	
	public static final String INPUT_LIVESEARCH = "<INPUT ID=\"%s\" TYPE='TEXT' NAME=\"%s\" VALUE=\"%s\" autocomplete='off' onKeyUp=\"livesearch(this, '%s', '%s')\" style='padding-bottom: 0px'/><DIV ID=\"%s\" CLASS='livesearch'></DIV>";

	static String livesearch(String fieldID, String fieldValue, String searchID, String divID) {
		return String.format(INPUT_LIVESEARCH, fieldID, fieldID, fieldValue, searchID, divID, divID);
	}
	
	public static String formatStringHTML(String aString) {
		if ( aString != null ) {
			return aString.replaceAll("(\r\n|\n|\r)", "<BR>");
		} else {
			return "";
		}
	}
	
	protected void forwardRequest(HttpServletRequest req, HttpServletResponse res, String path) throws ServletException, IOException {
		RequestDispatcher disp = getServletContext().getRequestDispatcher(path);
		disp.forward(req, res);					
	}

	protected void includeRequest(HttpServletRequest req, HttpServletResponse res, String path) throws ServletException, IOException {
		RequestDispatcher disp = getServletContext().getRequestDispatcher(path);
		disp.include(req, res);					
	}

	public static String shortenString(String aString, int length) {
		if (aString == null ) 
			return "";
		else 
			aString = aString.split("\n")[0];
			
		if ( aString.length() < length)
			return aString;
		else 
			return aString.substring(0, length);
	}

	public static String getLetterForIndex(int index) {
		if ( index <= 'Z' ) {
			return String.format("%c", 'A' + index);
		} else {
			int first = 1;
			while ( index > 'Z' ) {
				index = index - ('Z' * first);
				first++;
			}
			return String.format("%c%c", ('A' + first - 1), ('A' + index));
		}
		
	}
	
	public static FileUpload getUpload(HttpServletRequest request, String name) throws ServletException, IOException {
		return CyanosRequestListener.getUpload(request, name);
	}
	
	public static boolean hasActiveJobs(HttpSession session) {
		JobManager manager = CyanosSessionListener.getJobManager(session);
		return manager.hasActiveJobs();
	}

}

