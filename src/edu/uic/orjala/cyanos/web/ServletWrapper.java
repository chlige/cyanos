package edu.uic.orjala.cyanos.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.mail.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.sql.DataSource;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import edu.uic.orjala.cyanos.AccessException;
import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.User;
import edu.uic.orjala.cyanos.sql.SQLData;
import edu.uic.orjala.cyanos.sql.SQLUser;
import edu.uic.orjala.cyanos.web.html.Div;
import edu.uic.orjala.cyanos.web.html.Header;
import edu.uic.orjala.cyanos.web.html.Image;

/**
 * @author George Chlipala
 *
 */
public class ServletWrapper implements CyanosWrapper {

	private HttpServletRequest req = null;
	private HttpServletResponse res = null;
	private ServletObject myServlet = null;
	protected DataSource dbh = null;
	private PrintWriter printer = null;
	private ServletOutputStream outStream = null;
	private HttpSession mySess = null;
	
	private SQLData mySQLData = null;

	private Map<String, String[]> formValues = null;
	private Map<String, FileItem[]> uploadItems = null;

	private static final String MENU_SEPARATOR = "<HR WIDTH='80%' NOSHADE SIZE='1'/>";
	
	public final static String APP_CONFIG_ATTR = "cyanosAppConfig";
	
	public ServletWrapper(ServletObject aServlet, DataSource aDBH, HttpServletRequest aReq, HttpServletResponse aRes) {
		this.req = aReq;
		this.res = aRes;
		this.dbh = aDBH;
		this.myServlet = aServlet;
		this.parseRequest(aReq);
	}
	
	@SuppressWarnings("unchecked")
	private void parseRequest(HttpServletRequest aReq) {
		if ( ServletFileUpload.isMultipartContent(this.req)) {
			this.parseMultipartReq();
		} else {
			this.formValues = aReq.getParameterMap();
		}
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.CyanosWrapper#dateFormat()
	 */
	public SimpleDateFormat dateFormat()
	{
		SimpleDateFormat myDate = new SimpleDateFormat("MMMMM d, yyyy");
		return myDate;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.CyanosWrapper#dateTimeFormat()
	 */
	public SimpleDateFormat dateTimeFormat()
	{
		SimpleDateFormat myDate = new SimpleDateFormat("MMMMM d, yyyy hh:mm a");
		return myDate;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.CyanosWrapper#getContextPath()
	 */
	public String getContextPath() {
		return this.req.getContextPath();
	}


	private SQLData newSQLDataSource() throws DataException {
		try {
			if ( this.getRemoteUser() == null )
				return new SQLData(this.dbh.getConnection(), this.getGuestUser());				
			else 
				return new SQLData(this.dbh.getConnection(), this.getRemoteUser());
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}
	
	public SQLData getSQLDataSource(boolean independent) throws DataException {
		if ( independent )
			return this.newSQLDataSource();
		else 
			return this.getSQLDataSource();
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.CyanosServlet#getSQLDataSource()
	 */
	public SQLData getSQLDataSource() throws DataException {
		if ( this.mySQLData == null )
				this.mySQLData = this.newSQLDataSource();
		return this.mySQLData;
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.CyanosServlet#getUser()
	 */
	public User getUser() throws DataException {
		return this.getSQLDataSource().getUser();
	}
	
	public User getUser(String userID) throws DataException {
		try {
			return new SQLUser(this.dbh.getConnection(), userID);
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}
	
	private User getGuestUser() {
		String[] roles = { User.CULTURE_ROLE };
		String[] projects = { GuestUser.GLOBAL_PROJECT };
		return new GuestUser(roles, projects);
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
		if ( this.formValues.containsKey(key) )
			return ((String[])formValues.get(key))[index];
		else 
			return "";
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.CyanosServlet#getFormValueCount(java.lang.String)
	 */
	public int getFormValueCount(String key) {
		return ((String[])formValues.get(key)).length;
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.CyanosServlet#getFormValues(java.lang.String)
	 */
	public String[] getFormValues(String key) {
		return (String[])formValues.get(key);
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.CyanosServlet#hasFormValue(java.lang.String)
	 */
	public boolean hasFormValue(String key) {
		return this.formValues.containsKey(key);
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.CyanosServlet#hasFormValues()
	 */
	public boolean hasFormValues() {
		return (! this.formValues.isEmpty());
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.CyanosServlet#hasUpload(java.lang.String)
	 */
	public boolean hasUpload(String key) {
		return (this.uploadItems != null && this.uploadItems.containsKey(key));
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.CyanosServlet#getUpload(java.lang.String)
	 */
	public FileItem getUpload(String key) {
		return this.getUpload(key, 0);
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.CyanosServlet#getUpload(java.lang.String, int)
	 */
	public FileItem getUpload(String key, int index) {
		if ( this.uploadItems != null && this.uploadItems.containsKey(key) )
			return (uploadItems.get(key))[index];
		else 
			return null;
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.CyanosServlet#getUploadCount(java.lang.String)
	 */
	public int getUploadCount(String key) {
		if ( this.uploadItems != null )
			return (this.uploadItems.get(key)).length;
		else
			return 0;
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.CyanosServlet#getUploads(java.lang.String)
	 */
	public FileItem[] getUploads(String key) {
		if ( this.uploadItems != null && this.uploadItems.containsKey(key))
			return this.uploadItems.get(key);
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
				this.mySQLData.closeDBC();
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
	
	private void parseMultipartReq() {		
		this.formValues = new Hashtable<String, String[]>();
		this.uploadItems = new Hashtable<String, FileItem[]>();
		try {
			FileItemFactory factory = new DiskFileItemFactory();
			ServletFileUpload upload = new ServletFileUpload(factory);
			List items = upload.parseRequest(this.req);
			Iterator iter = items.iterator();
			while (iter.hasNext() ) {
				FileItem anItem = (FileItem) iter.next();
				String thisField = anItem.getFieldName();
				if ( anItem.isFormField()) {
					if ( this.formValues.containsKey(thisField) ) {
						String[] vals = this.formValues.get(thisField);
						int next = vals.length;
						vals[next] = anItem.getString();
						this.formValues.put(thisField, vals);
					} else {
						String[] vals = { anItem.getString() };
						this.formValues.put(thisField, vals);
					}
				} else if (anItem.getSize() > 0) {
					if ( this.uploadItems.containsKey(thisField) ) {
						FileItem[] vals = this.uploadItems.get(thisField);
						int next = vals.length;
						vals[next] = anItem;
						this.uploadItems.put(thisField, vals);
					} else {
						FileItem[] vals = { anItem };
						this.uploadItems.put(thisField, vals);
					}
				}
			}
		} catch (FileUploadException e) {
			this.myServlet.log("COULD NOT PARSE UPLOAD", e);
		}
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

	public CyanosConfig getAppConfig() {
		ServletContext aCtx = this.myServlet.getServletContext();
		CyanosConfig aConfig = (CyanosConfig)aCtx.getAttribute(APP_CONFIG_ATTR);
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
	
	public PrintWriter startHTMLDoc(String title) throws IOException {
		return this.startHTMLDoc(title, true);
	}
	
	public PrintWriter startHTMLDoc(String title, boolean showMenu) throws IOException {
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
			subMenu.addItem(String.format(urlFormat, "/sample/newCollection","Add a New Collection"));		
			subMenu.addItem(String.format(urlFormat, "/sample","Browse"));
			subMenu.addItem(MENU_SEPARATOR);
			subMenu.addItem(String.format(urlFormat, "/sample/protocol", "Manage Protocols"));
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
			aDBC = this.dbh.getConnection();
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

	public PrintWriter startHTMLDoc(String title, boolean showMenu, boolean enableMap) throws IOException {
		PrintWriter out = this.getWriter();
		if ( out != null ) {
			this.setContentType("text/html");
			Header myHeader = this.getHTMLHeader(title);
			CyanosConfig myConf = this.getAppConfig();
			if ( enableMap && myConf.canMap() ) {
				Map<String,String> layers = myConf.getMapServerLayers();
				if ( ! layers.isEmpty() ) {
					myHeader.addJavascriptFile(String.format("%s/OpenLayers.js", this.getContextPath()));
					out.println(myHeader);
					out.println("<BODY>");
				} else {
					String googleMapKey = myConf.getGoogleMapKey();
					if ( googleMapKey != null )  {
						String googleMapURL = String.format("http://maps.google.com/maps?file=api&v=2&key=%s", googleMapKey);
						myHeader.addJavascriptFile(googleMapURL);
						myHeader.addJavascriptFile("http://gmaps-utility-library.googlecode.com/svn/trunk/markermanager/release/src/markermanager.js");
						out.println(myHeader);
						out.println("<BODY onUnload='GUnload()'>");
					}
				} 
			} else {
				out.println(myHeader);
				out.println("<BODY>");
			}
			if ( showMenu )	out.println( this.menu() );
			out.println("<DIV class='content'>");
		}
		return out;
	}
	
}
