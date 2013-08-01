/**
 * 
 */
package edu.uic.orjala.cyanos.web.servlet;

import java.io.PrintWriter;
import java.security.KeyPair;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.binary.Base64;

import edu.uic.orjala.cyanos.Assay;
import edu.uic.orjala.cyanos.Compound;
import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.Material;
import edu.uic.orjala.cyanos.MutableUser;
import edu.uic.orjala.cyanos.Project;
import edu.uic.orjala.cyanos.Role;
import edu.uic.orjala.cyanos.Separation;
import edu.uic.orjala.cyanos.Strain;
import edu.uic.orjala.cyanos.User;
import edu.uic.orjala.cyanos.sql.SQLMutableUser;
import edu.uic.orjala.cyanos.sql.SQLProject;
import edu.uic.orjala.cyanos.sql.SQLUser;
import edu.uic.orjala.cyanos.web.AppConfig;
import edu.uic.orjala.cyanos.web.AppConfigSQL;
import edu.uic.orjala.cyanos.web.AppConfigXML;
import edu.uic.orjala.cyanos.web.BaseForm;
import edu.uic.orjala.cyanos.web.CyanosWrapper;
import edu.uic.orjala.cyanos.web.News;
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

	public static final String PARAM_CONFIG_NEW_MAP_URL = "mapURL";
	public static final String PARAM_CONFIG_MAP_GOOGLE_KEY = "googleMapKey";
	public static final String PARAM_CONFIG_MAP_ENABLE_NASA = "enableNASA";
	public static final String PARAM_CONFIG_MAP_ENABLE_OSM = "enableOSM";
	/**
	 * 
	 */
	private static final long serialVersionUID = -1729910187898214608L;
	public static final String APP_CONFIG_ATTR = ConfigForm.APP_CONFIG_ATTR;
	
	public static final String PARAM_CONFIG_GEN_KEY_PAIR = "genKeyPair";
	public static final String PARAM_CONFIG_UPLOAD_FILE = "xmlFile";
	public static final String PARAM_CONFIG_DOWNLOAD_FILE = "downloadConfig";
	public static final String PARAM_CONFIG_REVERT = "reloadConfig";
	public static final String PARAM_CONFIG_SAVE = "saveFile";
	public static final String PARAM_CONFIG_UPDATE = "updateConfig";
	public static final String PARAM_CONFIG_NEW_MAP_NAME = "mapName";
	public static final String PARAM_CONFIG_MAP_DEL_LAYER = "delLayer";
	
	public static final String FORM_CONFIG_FILEPATHS = "filepaths";
	public static final String FORM_CONFIG_DATATYPES = "datatypes";
	public static final String FORM_CONFIG_MAPS = "maps";
	public static final String FORM_CONFIG_QUEUES = "queues";
	public static final String FORM_CONFIG_MODULES = "modules";
	public static final String FORM_CONFIG_KEYS = "keys";
	public static final String FORM_CONFIG_XML = "savereload";
	
	public static final String[] CONFIG_FORMS = { AdminServlet.FORM_CONFIG_XML, AdminServlet.FORM_CONFIG_FILEPATHS, AdminServlet.FORM_CONFIG_DATATYPES, AdminServlet.FORM_CONFIG_QUEUES, 
			AdminServlet.FORM_CONFIG_MAPS, AdminServlet.FORM_CONFIG_MODULES, AdminServlet.FORM_CONFIG_KEYS }; 
	public static final String[] CONFIG_TITLES = { "Load/Save", "File Paths", "File Types", "Work Queues", "Maps", "Custom Modules", "Update Keys" };
	
	
	public static final String[] FILE_PATH_CLASSES = { "*", Strain.DATA_FILE_CLASS, Assay.DATA_FILE_CLASS, 
		Material.DATA_FILE_CLASS, Compound.DATA_FILE_CLASS, Separation.DATA_FILE_CLASS }; 
	
	public static final String[] roleList = { "culture", "cryo", "assay", "admin", "sample", "project" };
	
	static final String[] DATA_TYPE_DEF_CLASS = { Material.DATA_FILE_CLASS, Material.DATA_FILE_CLASS, Material.DATA_FILE_CLASS,
		Separation.DATA_FILE_CLASS, 
		Assay.DATA_FILE_CLASS, Assay.DATA_FILE_CLASS,
		Compound.DATA_FILE_CLASS, Compound.DATA_FILE_CLASS, Compound.DATA_FILE_CLASS, Compound.DATA_FILE_CLASS };
	static final String[] DATA_TYPE_DEF_TYPE =  { Material.LC_DATA_TYPE, Material.MS_DATA_TYPE, Material.NMR_DATA_TYPE,
		Separation.LC_DATA_TYPE, 
		Assay.RAW_DATA_TYPE, Assay.REPORT_DATA_TYPE,
		Compound.IR_DATA_TYPE, Compound.MS_DATA_TYPE, Compound.NMR_DATA_TYPE, Compound.UV_DATA_TYPE};
	static final String[] DATA_TYPE_DEF_DESC = { "LC Chromatogram", "Mass Spectrum", "NMR Spectrum", 
		"LC Chromatogram", 
		"Raw Data", "Report", 
		"IR Spectrum", "Mass Spectrum", "NMR Spectrum", "UV Spectrum"};
	
	public void display(CyanosWrapper aWrap) throws Exception {

		HttpServletRequest req = aWrap.getRequest();
		String module = req.getPathInfo();

		if ( module != null && module.equals("/get-config") ) {
			aWrap.setContentType("text/xml");
			aWrap.getResponse().reset();				
			HttpSession sess = req.getSession();
			AppConfig config = (AppConfig) sess.getAttribute(ConfigForm.APP_CONFIG_ATTR);
			if ( config == null ) {
				config = (AppConfig) aWrap.getAppConfig();
			}
			AppConfigXML.writeConfig(config, aWrap.getOutputStream());
			return;
		}
		
		PrintWriter out = aWrap.startHTMLDoc("Administration");		
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
		} else if ( servlet.equals("/admin/config") ) {
			HttpSession sess = req.getSession();
			if ( sess.getAttribute(ConfigForm.APP_CONFIG_ATTR) == null || aWrap.hasFormValue(ConfigForm.REVERT_CONFIG_ACTION) ) {
				sess.setAttribute(ConfigForm.APP_CONFIG_ATTR, new AppConfigSQL()); 
			}
			AppConfig appConfig = (AppConfig) sess.getAttribute(APP_CONFIG_ATTR);
			String pubKeyString = appConfig.getUpdateCert();
			
			if ( aWrap.hasFormValue(PARAM_CONFIG_GEN_KEY_PAIR) && ( pubKeyString == null || pubKeyString.length() == 0 ) ) {
				KeyPair keyPair = SQLProject.generateKeyPair();
				appConfig.setUpdateKey(SQLProject.encodePrivateKey(keyPair.getPrivate()));
				appConfig.setUpdateCert(SQLProject.encodePublicKey(keyPair.getPublic()));
				pubKeyString = appConfig.getUpdateCert();
			}
			
			if ( aWrap.hasFormValue(PARAM_CONFIG_UPDATE) ) {
				String form = aWrap.getFormValue("form");
				if ( form.equalsIgnoreCase(FORM_CONFIG_MAPS) ) 
					this.updateConfigMaps(aWrap, appConfig);
				else if ( form.equalsIgnoreCase(FORM_CONFIG_FILEPATHS) )
					this.updateConfigFilePaths(aWrap, appConfig);
				else if ( form.equalsIgnoreCase(FORM_CONFIG_QUEUES) )
					this.updateConfigQueues(aWrap, appConfig);
				else if ( form.equalsIgnoreCase(FORM_CONFIG_MODULES) )
					this.updateConfigModules(aWrap, appConfig);
				else if ( form.equalsIgnoreCase(FORM_CONFIG_DATATYPES) )
					this.updateConfigDataTypes(aWrap, appConfig);
			}
			
			if ( req.getParameter(ConfigForm.DOWNLOAD_CONFIG_ACTION) != null ) {
				ServletContext sc = getServletContext();
				RequestDispatcher rd = sc.getRequestDispatcher("/admin/get-config");
				rd.forward(req, aWrap.getResponse());
			}
			ServletContext sc = getServletContext();
			RequestDispatcher rd = sc.getRequestDispatcher("/config.jsp");
			rd.forward(req, aWrap.getResponse());		
		} else if ( servlet.equals("/config/user") ) {
			if ( "/add".equals(module) ) {
				out.println(this.addUser(aWrap));				
			} else {
				out.print(this.manageUser(aWrap));
			}
		} else if ( servlet.equals("/config/news") ) {
			out.println(this.manageNews(aWrap));
		} else {
			if ( module == null || module.equals("/") ) {
				out.println("<P ALIGN=\"CENTER\"><FONT SIZE=+3>Application Administration</FONT><HR WIDTH=\"85%\"/>");
				HtmlList formList = new HtmlList();
				formList.unordered();
				formList.setAttribute("type", "none");
				formList.addItem("<LI><A HREF='admin/user'>User Administration</A></LI>");
				formList.addItem("<LI><A HREF='admin/config'>Configuration Management</A></LI>");
				out.println(formList.toString());
			}
		}
		
		aWrap.finishHTMLDoc();
	}
	
	private void updateConfigModules(CyanosWrapper aWrap, AppConfig myConfig) {
		updateConfigModules(aWrap.getRequest(), myConfig);
	}
	
	static void updateConfigModules(HttpServletRequest req, AppConfig myConfig) {
		if ( req.getParameter("new_class") != null ) {
			String[] classes = req.getParameterValues("new_class");
			for ( int i = 0; i < classes.length; i++ ) {					
				String fieldName = String.format("class:%s", classes[i]);
				String classType = req.getParameter(fieldName);
				if ( classType.length() > 0 ) {
					myConfig.addClassForModuleType(classType, classes[i]);
					myConfig.addClassForJar(classes[i], req.getParameter("lib-jar"));
				}
			}
		} 
		
		if ( req.getParameter("delDerepClass") != null ) {
			String[] classes = req.getParameterValues("delDerepClass");
			for ( String aClass : classes ) {					
				myConfig.removeClassForModuleType(AppConfig.DEREPLICATION_MODULE, aClass);
			}
		}

		if ( req.getParameter("delUploadClass") != null ) {
			String[] classes = req.getParameterValues("delUploadClass");
			for ( String aClass : classes ) {					
				myConfig.removeClassForModuleType(AppConfig.UPLOAD_MODULE, aClass);
			}
		}
	}
	
	static void updateConfigQueues(HttpServletRequest req, AppConfig myConfig) {
		for ( int i = 0; i < AppConfig.QUEUE_TYPES.length; i++ ) {
			String mySource = req.getParameter(String.format("queuetype-%02d", i));
			myConfig.setQueueSource(AppConfig.QUEUE_TYPES[i], mySource);
			if ( mySource.equals("static") ) {
				String queueNames = req.getParameter(String.format("queues-%02d", i));
				if ( queueNames.length() > 0 ) {
					String[] queues = queueNames.split(" *, *");
					myConfig.setQueues(AppConfig.QUEUE_TYPES[i], queues);
				}
			}
		}
	}
	
	private void updateConfigQueues(CyanosWrapper aWrap, AppConfig myConfig) {
		updateConfigQueues(aWrap.getRequest(), myConfig);
	}
	
	static void updateConfigDataTypes(HttpServletRequest req, AppConfig myConfig) {
		myConfig.clearDataTypes();
		if ( req.getParameter(PARAM_CONFIG_UPDATE).equals("defaults") ) {
			for ( int i = 0; i < DATA_TYPE_DEF_CLASS.length; i++ ) {
				myConfig.setDataType(DATA_TYPE_DEF_CLASS[i], DATA_TYPE_DEF_TYPE[i], DATA_TYPE_DEF_DESC[i]);
			}
		} else if ( req.getParameter("row") != null ) {
			String[] rows = req.getParameterValues("row");
			for ( int i = 0; i < rows.length; i++ ) {
				String type = req.getParameter(String.format("%s_type", rows[i]));
				if ( type.length() > 0 && ( req.getParameter(String.format("%s_rm",rows[i])) == null) ) {
					String desc = req.getParameter(String.format("%s_desc", rows[i]));
					if ( desc.length() < 1 ) { desc = type; }
					myConfig.setDataType(req.getParameter(String.format("%s_class", rows[i])), type, desc);
				}
			}
		}
	}

	private void updateConfigDataTypes(CyanosWrapper aWrap, AppConfig myConfig) {
		updateConfigDataTypes(aWrap.getRequest(), myConfig);
	}
	
	static void updateConfigFilePaths(HttpServletRequest req, AppConfig myConfig) {
		myConfig.clearFilePaths();
		if ( req.getParameter("new_path") != null && (req.getParameter("new_path").length() > 0) ) {
			myConfig.setFilePath(req.getParameter("new_class"), req.getParameter("new_type"), req.getParameter("new_path"));
		}
		if ( req.getParameter("row") != null ) {
			String[] rows = req.getParameterValues("row");
			for ( int i = 0; i < rows.length; i++ ) {
				String path = req.getParameter(String.format("%s_path", rows[i]));
				if ( path.length() > 0 && ( req.getParameter(String.format("%s_rm",rows[i])) == null) ) {
					String type = req.getParameter(String.format("%s_type", rows[i]));
					if ( type.length() < 1 ) { type = "*"; }
					myConfig.setFilePath(req.getParameter(String.format("%s_class", rows[i])), type, path);
				}
			}
		}

	}
	
	
	private void updateConfigFilePaths(CyanosWrapper aWrap, AppConfig myConfig) {
		updateConfigFilePaths(aWrap.getRequest(), myConfig);
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
	
	private void updateConfigMaps(CyanosWrapper aWrap, AppConfig myConfig) {
		updateConfigMaps(aWrap.getRequest(), myConfig);
	}
	
	static void updateConfigMaps(HttpServletRequest req, AppConfig myConfig) {
		if ( req.getParameter(PARAM_CONFIG_MAP_DEL_LAYER) != null ) {
			String[] layers = req.getParameterValues(PARAM_CONFIG_MAP_DEL_LAYER);
			for ( int i = 0; i <  layers.length; i++ ) {
				myConfig.deleteMapServerLayer(layers[i]);
			}
		}
		if ( req.getParameter(PARAM_CONFIG_NEW_MAP_NAME) != null && req.getParameter(PARAM_CONFIG_NEW_MAP_NAME).length() > 0 ) {
			myConfig.addMapServerLayer(req.getParameter(PARAM_CONFIG_NEW_MAP_NAME) , req.getParameter(PARAM_CONFIG_NEW_MAP_URL));
		}
		if ( req.getParameter(PARAM_CONFIG_MAP_ENABLE_OSM) != null ) {
			myConfig.setMapParameter(AppConfig.MAP_OSM_LAYER, "1");
		} else {
			myConfig.removeMapParameter(AppConfig.MAP_OSM_LAYER);
		}

		if ( req.getParameter(PARAM_CONFIG_MAP_ENABLE_NASA) != null ) {
			myConfig.setMapParameter(AppConfig.MAP_NASA_LAYER, "1");
		} else {
			myConfig.removeMapParameter(AppConfig.MAP_NASA_LAYER);
		}

		String googleMapKey = req.getParameter(PARAM_CONFIG_MAP_GOOGLE_KEY);
		if ( googleMapKey != null && googleMapKey.length() > 0 ) {
			myConfig.setGoogleMapKey(googleMapKey);
		} else {
			myConfig.removeMapParameter(AppConfig.PARAM_GOOGLE_MAP_KEY);
		}
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


			CyanosConfig myConfig = aWrap.getAppConfig();
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
