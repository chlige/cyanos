package edu.uic.orjala.cyanos.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.mail.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.uic.orjala.cyanos.AccessException;
import edu.uic.orjala.cyanos.ConfigException;
import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.User;
import edu.uic.orjala.cyanos.sql.SQLData;
import edu.uic.orjala.cyanos.sql.SQLUser;
import edu.uic.orjala.cyanos.web.MultiPartRequest.FileUpload;
import edu.uic.orjala.cyanos.web.html.Div;
import edu.uic.orjala.cyanos.web.html.Header;
import edu.uic.orjala.cyanos.web.html.Image;
import edu.uic.orjala.cyanos.web.servlet.ServletObject;

/**
 * @author George Chlipala
 *
 */
@Deprecated
public class ServletWrapper implements CyanosWrapper {

	/**
	 * @author George Chlipala
	 *
	 */
	
	private final static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMMMM d, yyyy");
	private final static SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("MMMMM d, yyyy hh:mm a");

//	Google Maps Version 2
//	private static final String GOOGLE_MAPS_JS_URL = "http://maps.google.com/maps?file=api&v=2&key=%s";
//	Google Maps Version 3
	private static final String GOOGLE_MAPS_JS_URL = "https://maps.googleapis.com/maps/api/js?key=%s&sensor=false";
	private static final String OPENLAYERS_JS_URL = "openlayers/OpenLayers.js";
	
	private HttpServletRequest req = null;
	private HttpServletResponse res = null;
	private ServletObject myServlet = null;

	private PrintWriter printer = null;
	private ServletOutputStream outStream = null;
	private HttpSession mySess = null;
	
	private SQLData mySQLData = null;

	private static final String MENU_SEPARATOR = "<HR WIDTH='80%' NOSHADE SIZE='1'/>";
	
	public final static String APP_CONFIG_ATTR = "cyanosAppConfig";
	
	public ServletWrapper(ServletObject aServlet, HttpServletRequest aReq, HttpServletResponse aRes) {
		this.req = aReq;
		this.res = aRes;
		this.myServlet = aServlet;
		this.parseRequest(aReq);
	}
	
	@SuppressWarnings("unchecked")
	private void parseRequest(HttpServletRequest aReq) {

	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.CyanosWrapper#dateFormat()
	 */
	public SimpleDateFormat dateFormat()
	{
		return DATE_FORMAT;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.CyanosWrapper#dateTimeFormat()
	 */
	public SimpleDateFormat dateTimeFormat()
	{
		return DATETIME_FORMAT;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.CyanosWrapper#getContextPath()
	 */
	public String getContextPath() {
		return this.req.getContextPath();
	}


	private Connection getConnection() {
		return (Connection) this.getSession().getAttribute(ServletObject.DB_CONN);
	}

	@Deprecated
	public SQLData getSQLDataSource(boolean independent) throws DataException {
		try {
			if ( independent )
				return this.myServlet.newSQLData(req, this.getConnection());
			else 
				return this.getSQLDataSource();
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.CyanosServlet#getSQLDataSource()
	 */
	public SQLData getSQLDataSource() throws DataException {
		try {
			return this.myServlet.getSQLData(this.req);
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.CyanosServlet#getUser()
	 */
	public User getUser() throws DataException {
		return this.getSQLDataSource().getUser();
	}
	
	public User getUser(String userID) throws DataException {
		return new SQLUser(this.getConnection(), userID);
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.CyanosServlet#getMailSession()
	 */
	public Session getMailSession() throws NamingException {		
		Context initCtx = new InitialContext();
		Context envCtx = (Context) initCtx.lookup("java:comp/env");
		Session session = (Session) envCtx.lookup("mail/Session");
		return session;
	}

	
	public String getFormValue(String key) {
		return this.getFormValue(key, 0);
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.CyanosServlet#getFormValue(java.lang.String, int)
	 */
	public String getFormValue(String key, int index) {
		if ( req.getParameterValues(key) != null )
			return req.getParameterValues(key)[index];
		else 
			return "";
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.CyanosServlet#getFormValueCount(java.lang.String)
	 */
	public int getFormValueCount(String key) {
		if ( req.getParameterValues(key) != null )
			return req.getParameterValues(key).length;
		else 
			return 0;
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.CyanosServlet#getFormValues(java.lang.String)
	 */
	public String[] getFormValues(String key) {
		return req.getParameterValues(key);
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.CyanosServlet#hasFormValue(java.lang.String)
	 */
	public boolean hasFormValue(String key) {
		return (req.getParameter(key) != null );
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.CyanosServlet#hasFormValues()
	 */
	public boolean hasFormValues() {
		return (! req.getParameterMap().isEmpty() );
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.CyanosServlet#hasUpload(java.lang.String)
	 */
	public boolean hasUpload(String key) {
		return ( req instanceof MultiPartRequest );
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.CyanosServlet#getUpload(java.lang.String)
	 */
	public FileUpload getUpload(String key) {
		if ( req instanceof MultiPartRequest ) 
			return ((MultiPartRequest) req).getUpload(key);
		else 
			return null;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.CyanosServlet#getUpload(java.lang.String, int)
	 */
	public FileUpload getUpload(String key, int index) {
		if ( req instanceof MultiPartRequest ) 
			return ((MultiPartRequest) req).getUpload(key, index);
		else
			return null;
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.CyanosServlet#getUploadCount(java.lang.String)
	 */
	public int getUploadCount(String key) {
		if ( req instanceof MultiPartRequest ) 
			return ((MultiPartRequest) req).getUploadCount(key);
		else
			return 0;
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.CyanosServlet#getUploads(java.lang.String)
	 */
	public List<FileUpload> getUploads(String key) {
		if ( req instanceof MultiPartRequest ) 
			return ((MultiPartRequest)req).getUploads(key);
		else
			return null;
	}


	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.CyanosWrapper#handleException(java.lang.Exception)
	 */
	public String handleException(Exception e) {
		if ( AccessException.class.isAssignableFrom(e.getClass()))
			this.myServlet.log(String.format("[%s %s] %s", this.req.getMethod(), this.req.getRequestURI(), e.getLocalizedMessage()));
		else
			e.printStackTrace();
		return String.format("<DIV CLASS='error'><P><B><FONT COLOR='red'>ERROR:</FONT> %s</B></P></DIV>", e.getLocalizedMessage());
	}
	
	public void finalize() {
		try {
			if ( this.mySQLData != null ) {
				this.mySQLData.close();
	//			this.mySQLData.closeDBC();
				this.mySQLData = null;
			}
		} catch (DataException e) {
			this.myServlet.log("FAILED TO CLOSE DB CONNECTION!", e);
		}
	}

	public HttpServletRequest getRequest() {
		return this.req;
	}

	public HttpServletResponse getResponse() {
		return this.res;
	}

	public ServletOutputStream getOutputStream() throws IOException {
			if ( this.outStream == null && this.printer == null )
				this.outStream = this.res.getOutputStream();
		return this.outStream;
	}

	public PrintWriter getWriter() throws IOException {
		if ( this.printer == null && this.outStream == null ) 
			this.printer = this.res.getWriter();
		return this.printer;
	}

	public void print(String aString) {
		try {
			if ( this.getWriter() != null )
				this.printer.print(aString);
		} catch (IOException e) {
			this.myServlet.log("COULD NOT PRINT", e);
		}
	}

	public void setContentType(String aType) {
		this.res.setContentType(aType);
	}
	
	public String getStyle() {
		String cssFile = "cyanos.css";
		return String.format("%s/%s", this.getContextPath(), cssFile);
	}

	public String getImagePath(String imageName) {
		String imagePath = this.req.getContextPath() + "/images/" + imageName;
		return imagePath;
	}

	public Image getImage(String imageName) {
		String imagePath = this.getImagePath(imageName);
		Image aImage = new Image(imagePath);
		return aImage;
	}

	public AppConfig getAppConfig() {
		ServletContext aCtx = this.myServlet.getServletContext();
		AppConfig aConfig = (AppConfig)aCtx.getAttribute(APP_CONFIG_ATTR);
		return aConfig;
	}

	public Image getIconForMIMEType(String mimeType) {
		Image anIcon = null;
		if ( mimeType == null ) {
			anIcon = this.getImage("icons/binary.png");
		} else if ( mimeType.equals("application/pdf") ||  mimeType.equals("text/html") ) {
			anIcon = this.getImage("icons/layout.png");
		} else if ( mimeType.startsWith("image") ) {
			anIcon = this.getImage("icons/image.png");
		} else if ( mimeType.equals("text/plain") ) {
			anIcon = this.getImage("icons/text.png");
		} else if ( mimeType.startsWith("application/vnd.ms") ) {
			anIcon = this.getImage("icons/quill.png");
		} else if ( mimeType.equals("application/zip") ) {
			anIcon = this.getImage("icons/compressed.png");
		} else {
			anIcon = this.getImage("icons/binary.png");
		}
		
		return anIcon;
	}

	public HttpSession getSession() {
		if ( this.mySess == null ) {
			this.mySess = this.req.getSession();
		}
		return this.mySess;
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.CyanosServlet#getRequestURI()
	 */
	public String getRequestURI() {
		return this.req.getRequestURI();
	}

	private Header getHTMLHeader(String title) {
		Header myHeader = new Header();
		myHeader.setTitle(String.format("Cyanos Database - %s", title));
		myHeader.addCSSFile(this.getStyle());
		myHeader.addJavascriptFile(this.getContextPath() + "/cyanos.js");
		return myHeader;
	}
	
	public PrintWriter startHTMLDoc(String title) throws IOException, ConfigException {
		return this.startHTMLDoc(title, true);
	}
	
	public PrintWriter startHTMLDoc(String title, boolean showMenu) throws IOException, ConfigException {
		return this.startHTMLDoc(title, showMenu, false);
	}
	
	public void finishHTMLDoc() throws IOException {
		if ( this.printer != null ) {
			this.print("</DIV></BODY></HTML>");
			this.printer.close();
		}
	}

	public void finish() throws IOException {
		if ( this.printer != null )
			this.printer.close();
		else if ( this.outStream != null )
			this.outStream.close();
	}
	
	private String menu() {
		Div menuBar = new Div();
		menuBar.setClass("menubar");
		
		String urlFormat = "<A HREF=\"" + this.getContextPath() + "%s\">%s</A><BR/>";
		
		// Create Cyanos Menu
		menuBar.addItem("<SPAN CLASS='menu' ID='cyanosMenu'><A NAME='cyanos' onClick='toggleMenu(\"cyanosMenu\")'>Cyanos</A><BR>");

		Div subMenu = new Div("<P>");
		subMenu.addItem(String.format(urlFormat, "/main","Main Page"));
		if ( req.isUserInRole("project")) {
			subMenu.addItem(String.format(urlFormat, "/project", "Manage Projects"));
		}

		if ( this.getRemoteUser() != null) {
			subMenu.addItem(String.format(urlFormat, "/upload", "Upload Data"));
			subMenu.addItem(String.format(urlFormat, "/logout.jsp", "Logout"));
		} else {
			subMenu.addItem(String.format(urlFormat, "/login.jsp", "Login"));
		}
		
		if (req.isUserInRole("admin")) {
			// Create management menu
			subMenu.addItem(MENU_SEPARATOR);
			subMenu.addItem(String.format(urlFormat, "/admin/user","Manage Users"));
			subMenu.addItem(String.format(urlFormat, "/admin/news","Manage News"));
			subMenu.addItem(String.format(urlFormat, "/admin/config","Manage Config"));
		}

		
		subMenu.setClass("submenu");
		subMenu.addItem("</P>");
		menuBar.addItem(subMenu);
		menuBar.addItem("</SPAN>");

		// Create Strain Menu
		menuBar.addItem("<SPAN CLASS='menu' ID='strainMenu'><A NAME='strains' onClick='toggleMenu(\"strainMenu\")'>Strains</A><BR>");
		
		subMenu = new Div("<P>");
		subMenu.setClass("submenu");
 		subMenu.addItem(String.format(urlFormat, "/strain","Search"));
		subMenu.addItem(String.format(urlFormat, "/taxabrowser","Taxa Browser"));

		if (req.isUserInRole("culture")) {
			subMenu.addItem(String.format(urlFormat, "/strain?action=add","Add New"));
			subMenu.addItem(String.format(urlFormat, "/file","Photo Manager"));		
		}
		subMenu.addItem("</P>");
		menuBar.addItem(subMenu);
		menuBar.addItem("</SPAN>");
		

		if (req.isUserInRole("culture")) {
			// Create culture menu
			menuBar.addItem("<SPAN CLASS='menu' ID='cultureMenu'><A NAME='cultures' onClick='toggleMenu(\"cultureMenu\")'>Cultures</A><BR>");
			subMenu = new Div("<P>");
			subMenu.setClass("submenu");
			subMenu.addItem(String.format(urlFormat, "/inoc/add","Add Inoculation(s)"));
			subMenu.addItem(MENU_SEPARATOR);
			subMenu.addItem(String.format(urlFormat, "/cryo","Browse Cryopreservations"));
			subMenu.addItem(String.format(urlFormat, "/cryo/add","Add Cryopreservation(s)"));
			subMenu.addItem("</P>");
			menuBar.addItem(subMenu);
			menuBar.addItem("</SPAN>");
			
			// Create culture menu
			menuBar.addItem("<SPAN CLASS='menu' ID='collectionMenu'><A NAME='collections' onClick='toggleMenu(\"collectionMenu\")'>Collections</A><BR>");
			subMenu = new Div("<P>");
			subMenu.setClass("submenu");
			subMenu.addItem(String.format(urlFormat, "/collection","Search Collections"));
			subMenu.addItem(String.format(urlFormat, "/collection/add", "Add New Collection"));
			subMenu.addItem(MENU_SEPARATOR);
			subMenu.addItem(String.format(urlFormat, "/upload/collection","Upload Collections"));
			subMenu.addItem(String.format(urlFormat, "/upload/isolation","Upload Isolations"));
			subMenu.addItem("</P>");
			menuBar.addItem(subMenu);
			menuBar.addItem("</SPAN>");
		} 
        
		if (req.isUserInRole("sample")) {
			// Create sample menu
			menuBar.addItem("<SPAN CLASS='menu' ID='sampleMenu'><A NAME='samples' onClick='toggleMenu(\"sampleMenu\")'>Samples</A><BR>");
			subMenu = new Div("<P>");
			subMenu.setClass("submenu");
			subMenu.addItem(String.format(urlFormat, "/material", "Browse Materials"));
			subMenu.addItem(String.format(urlFormat, "/sample/protocol", "Manage Extract Protocols"));
			subMenu.addItem(MENU_SEPARATOR);
			subMenu.addItem(String.format(urlFormat, "/sample/newCollection","Add a New Collection"));		
			subMenu.addItem(String.format(urlFormat, "/sample","Browse"));
			subMenu.addItem(String.format(urlFormat, "/upload/sample","Upload Data"));
			subMenu.addItem(MENU_SEPARATOR);
			subMenu.addItem(String.format(urlFormat, "/compound", "View Compounds"));
			subMenu.addItem(String.format(urlFormat, "/compound/add", "Add Compound"));
			subMenu.addItem(String.format(urlFormat, "/dereplication","Dereplication"));
			menuBar.addItem(subMenu);
			menuBar.addItem("</SPAN>");
		
			// Create sample menu
			menuBar.addItem("<SPAN CLASS='menu' ID='sepMenu'><A NAME='seps' onClick='toggleMenu(\"sepMenu\")'>Separations</A><BR>");
			subMenu = new Div("<P>");
			subMenu.setClass("submenu");
			subMenu.addItem(String.format(urlFormat, "/separation/protocol", "Manage Protocols"));
			subMenu.addItem(String.format(urlFormat, "/upload/fraction", "Upload New"));
			subMenu.addItem("</P>");
			menuBar.addItem(subMenu);
			menuBar.addItem("</SPAN>");
		}
		
		if (req.isUserInRole("assay")) {
			// Create assay menu
			menuBar.addItem("<SPAN CLASS='menu' ID='assayMenu'><A NAME='assays' onClick='toggleMenu(\"assayMenu\")'>Bioassays</A><BR>");
			subMenu = new Div("<P>");
			subMenu.setClass("submenu");
			subMenu.addItem(String.format(urlFormat, "/assay/add","Add New"));
			subMenu.addItem(String.format(urlFormat, "/assay","Browse Data"));
			subMenu.addItem(MENU_SEPARATOR);
			subMenu.addItem(String.format(urlFormat, "/assay/protocol","Manage Protocols"));
			subMenu.addItem(String.format(urlFormat, "/upload/assay","Upload Data"));
			subMenu.addItem("</P>");
			menuBar.addItem(subMenu);
			menuBar.addItem("</SPAN>");
		}

		menuBar.addItem("<SPAN CLASS='helpmenu' ID='helpMenu'><A NAME='help' onClick='toggleMenu(\"helpMenu\")'>Help</A><BR>");
		subMenu = new Div("<P>");
		subMenu.setClass("submenu");
		String helpModule = this.myServlet.getHelpModule();
		if ( helpModule != null ) {
			String url = String.format("/help/toc?module=%s", helpModule);
			subMenu.addItem(String.format(urlFormat, url, "Quick Help"));	
			subMenu.addItem(MENU_SEPARATOR);
		}
		subMenu.addItem(String.format(urlFormat, "/help/toc","Help Contents"));
		subMenu.addItem(String.format(urlFormat, "/help/find","Find a Topic"));
		subMenu.addItem(String.format(urlFormat, "/help/search","Search Help"));
		
		subMenu.addItem("</P>");
		menuBar.addItem(subMenu);
		menuBar.addItem("</SPAN>");

		
        return menuBar.toString() + "<DIV STYLE='height:20px'></DIV>";
   }

	public String getRemoteUser() {
		return this.req.getRemoteUser();
	}

	public boolean setMyPassword(String newPassword) throws SQLException {
		boolean retVal = false;
		Connection aDBC = null;
		SQLException execp = null;
		try {
			aDBC = this.getConnection();
			PreparedStatement psth = aDBC.prepareStatement("UPDATE users SET password=SHA1(?) WHERE username=?");
			psth.setString(1, newPassword);
			psth.setString(2, this.getRemoteUser());
			retVal = ( psth.executeUpdate() > 0 ); 
			psth.close();
		} catch (SQLException e) {
			execp = e;
		}
		
		try {
			if ( aDBC != null && (! aDBC.isClosed()) )
				aDBC.close();
		} catch (SQLException e) {
			this.myServlet.log("FAILED TO CLOSE SQL CONNECTION", e);
		}
		if ( execp != null )
			throw execp;
		return retVal;
	}

	public PrintWriter startHTMLDoc(String title, boolean showMenu, boolean enableMap) throws IOException, ConfigException {
		PrintWriter out = this.getWriter();
		if ( out != null ) {
			this.setContentType("text/html");
			Header myHeader = this.getHTMLHeader(title);
			AppConfig myConf = this.getAppConfig();
			if ( enableMap ) {
				myHeader.addJavascriptFile(String.format("%s/%s", this.getContextPath(), OPENLAYERS_JS_URL));
				//					myHeader.addJavascriptFile("http://maps.google.com/maps/api/js?v=3&sensor=false");
				myHeader.addJavascriptFile(String.format("%s/cyanos-map.js", this.getContextPath()));
				//					myHeader.addJavascriptFile(String.format("%s/openlayers/OpenLayers.js", this.getContextPath()));
				String googleMapKey = myConf.getGoogleMapKey();
				if ( googleMapKey != null && googleMapKey.length() > 0 )  {
					String googleMapURL = String.format(GOOGLE_MAPS_JS_URL, googleMapKey);
					myHeader.addJavascriptFile(googleMapURL);
				}
			}
			out.println(myHeader);
			out.println("<BODY>");
			if ( showMenu )	out.println( this.menu() );
			out.println("<DIV class='content'>");
		}
		return out;
	}

	public PrintWriter startRedirectDoc(String title, String url, int delay)
			throws IOException, ConfigException {
		// TODO Auto-generated method stub
		return null;
	}

	public PrintWriter startRedirectDoc(String title, boolean showMenu,
			String url, int delay) throws IOException, ConfigException {
		// TODO Auto-generated method stub
		return null;
	}
	
}
