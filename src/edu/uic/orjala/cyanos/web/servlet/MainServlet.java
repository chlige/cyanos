package edu.uic.orjala.cyanos.web.servlet;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.uic.orjala.cyanos.ConfigException;
import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.Role;
import edu.uic.orjala.cyanos.User;
import edu.uic.orjala.cyanos.web.AppConfig;
import edu.uic.orjala.cyanos.web.AppConfigSQL;
import edu.uic.orjala.cyanos.web.AppConfigXML;
import edu.uic.orjala.cyanos.web.FileUpload;
import edu.uic.orjala.cyanos.web.MultiPartRequest;
import edu.uic.orjala.cyanos.web.listener.AppConfigListener;
import edu.uic.orjala.cyanos.web.News;

public class MainServlet extends ServletObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5699551419430618192L;
	public static final String HELP_MODULE = "main";
	
	public static final String ATTR_NEWS = "news";

	public static final String APP_CONFIG_ATTR = AdminServlet.APP_CONFIG_ATTR;
	public static final String SESS_ATTR_UPLOAD_CONFIG = "xmlConfig";
	
	public static final String[] SETUP_TITLES = { "Welcome", "Validate Database", "Setup Administrator", "Finish" };
	
	public static final String ATTR_SETUP_VALUES = "setupValues";
	
	public static final String SETUP_DB_VALID = "dbValid";
	public static final String SETUP_ADMIN_ID = "adminID";
	public static final String SETUP_ADMIN_PWD = "adminPWD";
	public static final String SETUP_ADMIN_NAME = "adminName";
	public static final String SETUP_ADMIN_EMAIL = "adminEmail";
	public static final String SETUP_ADMIN_ROLES = "adminRoles";
	public static final String SETUP_HAS_ADMIN = "adminCount";
	public static final String SETUP_DEFAULT_PATH = "filePath";
	
	public static final String SETUP_XML_CONFIG = "xmlConfig";
	
	public static final String SETUP_ACTION_FINISH = "finish";
	public static final String SETUP_ACTION_PARSE = "parseXML";
	public static final String SETUP_ACTION_UPLOAD = "uploadXML";
	
	public static final String SETUP_UPLOAD = "xmlFile";
	
	public static final String ATTR_FILE_COMPLETE = "fileUploaded";
	
	public void doPost ( HttpServletRequest req, HttpServletResponse res ) throws ServletException, IOException {
		if ( AppConfigListener.isUpgradeInstall() ) {
			req = MultiPartRequest.parseRequest(req);
			this.setupInstall(req);
			if ( req.getParameter(SETUP_ACTION_FINISH) != null ) {
				HttpSession session = req.getSession();
				Map<String,String> setupValues = (Map<String,String>) session.getAttribute(ATTR_SETUP_VALUES);
				if ( setupValues != null ) {
					if ( this.validateSetup(setupValues) ) {
						AppConfig config = (AppConfig) session.getAttribute(APP_CONFIG_ATTR);
						RequestDispatcher disp;
						if ( this.commitSetup(setupValues)) {
							try {
								config.writeConfig();
								AppConfigListener.reloadConfig();
							} catch (ConfigException e) {
								throw new ServletException(e);
							}
							session.removeAttribute(APP_CONFIG_ATTR);
							session.removeAttribute(ATTR_SETUP_VALUES);
							disp = getServletContext().getRequestDispatcher("/setup/complete.jsp");
						} else {
							disp = getServletContext().getRequestDispatcher("/setup/failed.jsp");							
						}
						disp.forward(req, res);	
						return;
					}
				}
			} else if ( req.getParameter(SETUP_ACTION_PARSE) != null ) {
				HttpSession session = req.getSession();
				Map<String,String> setupValues = (Map<String,String>) session.getAttribute(ATTR_SETUP_VALUES);
				if ( setupValues != null && setupValues.containsKey(SETUP_XML_CONFIG) ) {
					try {
						session.setAttribute(SESS_ATTR_UPLOAD_CONFIG, this.loadXMLFile(setupValues.get(SETUP_XML_CONFIG)));
						setupValues.remove(SETUP_XML_CONFIG);
					} catch (ConfigException e) {
						throw new ServletException(e);
					}
				}
			} else if ( req.getParameter(SETUP_ACTION_UPLOAD) != null && req instanceof MultiPartRequest ) {
				HttpSession session = req.getSession();
				FileUpload upload = getUpload(req, SETUP_UPLOAD);
				if ( upload != null ) {
					try {
						session.setAttribute(SESS_ATTR_UPLOAD_CONFIG, this.loadXMLFile(upload.getStream()));
					} catch (ConfigException e) {
						throw new ServletException(e);
					}
				}
			} else if ( req.getParameter("useConfig") != null ) {
				HttpSession session = req.getSession();
				String useConfig = req.getParameter("useConfig");
				if ( useConfig.equals("xml") ) {
					session.setAttribute(APP_CONFIG_ATTR, session.getAttribute(SESS_ATTR_UPLOAD_CONFIG));					
				}
				session.removeAttribute(SESS_ATTR_UPLOAD_CONFIG);
				req.setAttribute(ATTR_FILE_COMPLETE, Boolean.TRUE);
			} else if ( req.getParameter("form") != null ) {
				if ( req.getParameter(AdminServlet.PARAM_CONFIG_UPDATE) != null ) {
					String form = req.getParameter("form");
					HttpSession session = req.getSession();
					AppConfig config = (AppConfig) session.getAttribute(APP_CONFIG_ATTR);
					if ( form.equalsIgnoreCase(AdminServlet.FORM_CONFIG_MAPS) ) 
						AdminServlet.updateConfigMaps(req, config);
					else if ( form.equalsIgnoreCase(AdminServlet.FORM_CONFIG_FILEPATHS) )
						AdminServlet.updateConfigFilePaths(req, config);
					else if ( form.equalsIgnoreCase(AdminServlet.FORM_CONFIG_QUEUES) )
						AdminServlet.updateConfigQueues(req, config);
					else if ( form.equalsIgnoreCase(AdminServlet.FORM_CONFIG_MODULES) )
						AdminServlet.updateConfigModules(req, config);
					else if ( form.equalsIgnoreCase(AdminServlet.FORM_CONFIG_DATATYPES) )
						AdminServlet.updateConfigDataTypes(req, config);
				}
			} else if ( req.getParameter("setup-map") != null ) {
				HttpSession session = req.getSession();
				AppConfig config = (AppConfig) session.getAttribute(APP_CONFIG_ATTR);
				AdminServlet.updateConfigMaps(req, config);
			}

			
			RequestDispatcher disp = getServletContext().getRequestDispatcher("/install.jsp");
			disp.forward(req, res);	
		} else {
			try {
				this.setupSession(req);
				req.setAttribute(ATTR_NEWS, News.currentNews(this.getSQLData(req)));
			} catch (DataException e) {
				throw new ServletException(e);
			} catch (SQLException e) {
				throw new ServletException(e);
			}
			RequestDispatcher disp = getServletContext().getRequestDispatcher("/main.jsp");
			disp.forward(req, res);				
		}
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.servlet.ServletObject#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		if ( AppConfigListener.isNewInstall() ) {
			this.setupInstall(req);
			
			if ( req.getParameter("showConfig") != null ) {
				HttpSession session = req.getSession();
				req.setAttribute(APP_CONFIG_ATTR, session.getAttribute(APP_CONFIG_ATTR));
				RequestDispatcher disp = getServletContext().getRequestDispatcher("/setup/display.jsp");
				disp.forward(req, res);	
				return;
			}
			this.forwardRequest(req, res, "/install.jsp");
		} else {
			try {
				this.setupSession(req);
				req.setAttribute(ATTR_NEWS, News.currentNews(this.getSQLData(req)));
			} catch (DataException e) {
				throw new ServletException(e);
			} catch (SQLException e) {
				throw new ServletException(e);
			}
			RequestDispatcher disp = getServletContext().getRequestDispatcher("/main.jsp");
			disp.forward(req, res);				
		}
	}
	
	private AppConfig loadXMLFile(String xmlFile) throws ConfigException {
		AppConfig config = new AppConfigSQL();
		File configFile = new File(xmlFile);
		if ( configFile.exists() ) {
			AppConfigXML.loadConfig(config, configFile);
		}
		return config;
	}
	
	
	private AppConfig loadXMLFile(InputStream xmlStream) throws ConfigException {
		AppConfig config = new AppConfigSQL();
		AppConfigXML.loadConfig(config, xmlStream);
		return config;
	}
	
	private boolean commitSetup(Map<String,String> setupValues) throws ServletException {
		boolean success = setupValues.containsKey(SETUP_HAS_ADMIN);
		if ( ! success )
			success = this.createAdmin(setupValues);
		return success;
	}
	
	private boolean createAdmin(Map<String,String> setupValues) throws ServletException {
		boolean success = false;
		try {
			Connection conn = AppConfigListener.getDBConnection();
			PreparedStatement psth = conn.prepareStatement("INSERT INTO users(username,password,fullname,email) VALUES(?,SHA1(?),?,?);");
			conn.setAutoCommit(false);
			Savepoint savepoint = conn.setSavepoint();

			String userID = setupValues.get(SETUP_ADMIN_ID);
			String userName = setupValues.get(SETUP_ADMIN_NAME);
			String userEmail = setupValues.get(SETUP_ADMIN_EMAIL);
			String userPwd = setupValues.get(SETUP_ADMIN_PWD);

			psth.setString(1, userID);
			psth.setString(2, userPwd);
			psth.setString(3, userName);
			psth.setString(4, userEmail);
			
			if ( psth.executeUpdate() > 0 ) {
				psth = conn.prepareStatement("INSERT INTO roles(perm,username,project_id,role) VALUES(?,?,?,?)");
				psth.setString(2, userID);
				psth.setString(3, User.GLOBAL_PROJECT);
				psth.setString(4, User.ADMIN_ROLE);
				psth.setInt(1, Role.CREATE + Role.DELETE + Role.READ + Role.WRITE);
				success = ( psth.executeUpdate() == 1 );
				
				if ( success ) {
					String roleString = setupValues.get(SETUP_ADMIN_ROLES);
					if ( roleString != null ) {
						for ( String role : roleString.split("\\s*,\\s*") ) {
							psth.setString(4, role);
							success = ( psth.executeUpdate() == 1 );
						}
					}
				}
			}
			if ( success ) {
				conn.commit();
			} else {
				conn.rollback(savepoint);
			}
			psth.close();
			conn.close();
		} catch (SQLException e) {
			throw new ServletException(e);
		}
		return success;
	}

	
	private void setupInstall(HttpServletRequest req) throws ServletException {
		HttpSession session = req.getSession();
		if ( session.getAttribute(ATTR_SETUP_VALUES) == null ) {
			Map<String,String> setupValues = new HashMap<String,String>();
			try {
				Context initCtx = new InitialContext();
				Context envCtx = (Context) initCtx.lookup("java:comp/env");
				if ( envCtx != null ) {
					Object configFile = envCtx.lookup(AppConfigXML.APP_CONFIG_ATTR);
					if ( configFile != null && configFile instanceof String) {
						File cFile = new File((String) configFile);
						if ( cFile.exists() ) {
							setupValues.put(SETUP_XML_CONFIG, (String) configFile);
							try {
								session.setAttribute(SESS_ATTR_UPLOAD_CONFIG, this.loadXMLFile(setupValues.get(SETUP_XML_CONFIG)));
							} catch (ConfigException e) {							
								e.printStackTrace();
							}
						}
					}
				}
			} catch (NamingException e) {
				e.printStackTrace();
			}
			
			try {
					Connection conn = AppConfigListener.getDBConnection();
					PreparedStatement psth = conn.prepareStatement("SELECT users.username,fullname FROM users JOIN roles ON (users.username = roles.username) " +
							"WHERE roles.perm = ? AND project_id = ? AND role = ? ORDER BY users.username;");
					psth.setInt(1, Role.CREATE + Role.DELETE + Role.READ + Role.WRITE);
					psth.setString(2, User.GLOBAL_PROJECT);
					psth.setString(3, User.ADMIN_ROLE);

					ResultSet results = psth.executeQuery();

					if ( results.first() ) {
						setupValues.put(SETUP_HAS_ADMIN, "true");
						Map<String,String> adminMap = new Hashtable<String,String>();
						results.beforeFirst();
						while ( results.next() ) {
							adminMap.put(results.getString(1), results.getString(2));
						}
						session.setAttribute(SETUP_HAS_ADMIN, adminMap);
					}
					results.close();
					psth.close();
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			session.setAttribute(ATTR_SETUP_VALUES, setupValues);
		}
		try { 
			if ( session.getAttribute(APP_CONFIG_ATTR) == null ) {
				AppConfig config = new AppConfigSQL(true);
				if ( ! config.canMap() ) {
					config.setMapParameter(AppConfig.MAP_OSM_LAYER, "1");
				}
				session.setAttribute(APP_CONFIG_ATTR, config);
			}
		} catch (ConfigException e) {
			throw new ServletException(e);
		}
	}
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		try {
			File install = new File(config.getServletContext().getRealPath(".install"));			
			if ( ! install.exists() ) {
				install.createNewFile();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean validateSetup(Map<String,String> setupValues) {
		boolean valid = setupValues.containsKey(SETUP_DB_VALID);
		if ( valid && setupValues.containsKey(SETUP_HAS_ADMIN) ) 
			return valid;
		if ( valid && setupValues.containsKey(SETUP_ADMIN_ID) )
			valid = valid && setupValues.get(SETUP_ADMIN_ID).length() > 0;
		if ( valid && setupValues.containsKey(SETUP_ADMIN_PWD) )
			valid = valid && setupValues.get(SETUP_ADMIN_PWD).length() > 0;
		return valid;
	}

/*
	protected void display(CyanosWrapper aWrap) throws Exception {
		
		aWrap.getRequest().setAttribute(ATTR_NEWS, News.currentNews(aWrap.getSQLDataSource()));
		RequestDispatcher disp = getServletContext().getRequestDispatcher("/main.jsp");
		disp.forward(aWrap.getRequest(), aWrap.getResponse());				

				
		PrintWriter out = aWrap.startHTMLDoc("Main Page");
		
		Div sideDiv = new Div();
		sideDiv.setClass("left25");
		sideDiv.addItem("<DIV STYLE='height:100px'></DIV>");
		Div mainDiv = new Div();
		mainDiv.setClass("right75");
		
		Paragraph head = new Paragraph();
		head.setAlign("CENTER");
		StyledText title = new StyledText("Cyanos Database");
		title.setSize("+3");
		head.addItem(title);
		mainDiv.addItem(head);
		mainDiv.addItem("<HR WIDTH='85%'/>");
		
		Table newsTable = new Table("<TR><TD ALIGN='CENTER'><B><FONT SIZE='+2'>News</FONT></B></TD></TR>");
		try {
			News news = News.currentNews(aWrap.getSQLDataSource());
			Definition newsItems = new Definition();
			if ( news.first() )  {
				news.beforeFirst();
				SimpleDateFormat myFormat = aWrap.dateTimeFormat();
				while (news.next()) {
					String header = String.format("<B>%s</B> - <I>%s</I>", news.getSubject(), myFormat.format(news.getDateAdded()));
					newsItems.addDefinition(header, news.getContent().replaceAll("\n", "<BR>"));
				}
				newsTable.addItem(String.format("<TR><TD><DL WIDTH='80%%'>%s</DL></TD></TR>", newsItems.toString()));
			} else {
				newsTable.addItem("<TR><TD ALIGN='CENTER'><B>No News</B></TD></TR>");
			}
		} catch (DataException e) {
			newsTable.addItem("<TR><TD ALIGN='CENTER'><B><FONT COLOR='red'>SQL ERROR:</FONT> " + e.getMessage() + "</B></TD></TR>");
		}
		newsTable.setAttribute("ALIGN", "CENTER");
		newsTable.setAttribute("WIDTH", "80%");
		mainDiv.addItem(newsTable);
		
		StrainForm aForm = new StrainForm(aWrap);
		mainDiv.addItem("<HR WIDTH='75%'/><P ALIGN='CENTER'><B><FONT SIZE='+2'>Strain Query</FONT></B></P>");
		mainDiv.addItem(aForm.queryForm());
		
		if ( aWrap.getRemoteUser() != null ) {
			Div aModule = new Div(this.userModule(aWrap));
			aModule.setClass("sideModule");
			sideDiv.addItem(aModule);
			QueueForm qForm = new QueueForm(aWrap);
			aModule = new Div(qForm.queueModule(aWrap.getRemoteUser()));
			aModule.setClass("sideModule");
			sideDiv.addItem(aModule);
		} else {
			sideDiv.addItem("<DIV CLASS='sideModule'><IFRAME SRC='login.jsp' scrolling=NO FRAMEBORDER=0 HEIGHT=190 WIDTH='100%'></IFRAME></DIV>");
		}
		
		Div strainMod = new Div(this.strainModule(aWrap));
		strainMod.setClass("sideModule");
		sideDiv.addItem(strainMod);
		
		mainDiv.addItem("<HR WIDTH='85%'/><P ALIGN=CENTER><I>" + this.getServletInfo() + "</I></P>");		
		out.println(sideDiv.toString());
		out.println(mainDiv.toString());
		aWrap.finishHTMLDoc();

	}

	protected String userModule(CyanosWrapper aWrap) throws DataException {
		StringBuffer output = new StringBuffer();
		SQLData myData = aWrap.getSQLDataSource();
		User aUser = myData.getUser();
		output.append("<P><B>Welcome, " + aUser.getUserName() + "</B></P>");
		output.append("<P><A HREF=\"self\">Update Profile</A><BR>");
		output.append("<A HREF=\"logout.jsp\">Logout</A></P>");
		return output.toString();
	}

	protected String strainModule(CyanosWrapper aWrap) throws SQLException {
		StrainForm aForm = new StrainForm(aWrap);
		
		Div moduleDiv = new Div(aForm.summaryTable());
		moduleDiv.setClass("hideSection");
		moduleDiv.setID("div_strainModule");

		Form queryForm = new Form();
		queryForm.setName("query");
		queryForm.setAttribute("action", "strain");
		Input searchField = new Input("text");
		searchField.setName("query");
		queryForm.addItem(searchField);
		queryForm.addSubmit("Search");
		
		moduleDiv.addItem(queryForm);
		
		Image anImage = aWrap.getImage("module-twist-closed.png");
		anImage.setAttribute("ID", "twist_strainModule");
		anImage.setAttribute("ALIGN", "absmiddle");
		return "<P CLASS='moduleTitle'><A NAME='strainModule' onClick='twistModule(\"strainModule\")'>" + anImage.toString() +
			"  Strain Information</A></P>" + moduleDiv.toString();
	}
*/

	public static String versionString() {
		return String.format("%d.%d", VERSION, SUBVERSION);
	}
	
/*
	private String validateDB() {
		CyanosConfig myConfig = this.myWrapper.getAppConfig();
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
	*/
}
