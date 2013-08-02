package edu.uic.orjala.cyanos.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.mail.Session;
import javax.naming.NamingException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.uic.orjala.cyanos.ConfigException;
import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.User;
import edu.uic.orjala.cyanos.sql.SQLData;
import edu.uic.orjala.cyanos.web.MultiPartRequest.FileUpload;
import edu.uic.orjala.cyanos.web.html.Image;

/**
 * Generic interface for CYANOS servlets.
 * 
 * 
 * @author George Chlipala
 *
 */
/**
 * @author George Chlipala
 *
 */
@Deprecated
public interface CyanosWrapper {
	
	SimpleDateFormat dateFormat();

	SimpleDateFormat dateTimeFormat();

	String getFormValue(String key);

	String getFormValue(String key, int index);

	int getFormValueCount(String key);

	String[] getFormValues(String key);

	boolean hasFormValue(String key);

	boolean hasFormValues();

	boolean hasUpload(String key);

	FileUpload getUpload(String key);

	FileUpload getUpload(String key, int index);

	int getUploadCount(String key);

	List<FileUpload> getUploads(String key);

	/**
	 * Get an SQLData object.  This data object will be managed by the servlet and closed upon end of HTTP request processing.
	 * 
	 * @return SQLData object
	 * @throws DataException
	 */
	SQLData getSQLDataSource() throws DataException;

	/**
	 * Get an SQLData object.  The data object can be managed (like {@link #getSQLDataSource()}) or independent, 
	 * where the local routine will need to properly close once finished.
	 * 
	 * @param independent if <CODE>true</CODE> then create an independent data object.
	 * @return SQLData object
	 * @throws DataException
	 * @see #getSQLDataSource()
	 */
	SQLData getSQLDataSource(boolean independent) throws DataException;
	
	User getUser() throws DataException;

	Session getMailSession() throws NamingException;

	AppConfig getAppConfig();

	String getContextPath();

	String getRemoteUser();

	HttpSession getSession();

	String getRequestURI();

	String handleException(Exception e);
	
	HttpServletResponse getResponse();
	
	HttpServletRequest getRequest();
	
	PrintWriter getWriter() throws IOException;
	
	ServletOutputStream getOutputStream() throws IOException;
	
	void print(String aString);
	
	void setContentType(String aType);
	
	public User getUser(String userID) throws DataException;
	
	public PrintWriter startHTMLDoc(String title) throws IOException, ConfigException;
	
	public PrintWriter startHTMLDoc(String title, boolean showMenu) throws IOException, ConfigException;
	
	public PrintWriter startHTMLDoc(String title, boolean showMenu, boolean enableMap) throws IOException, ConfigException;
	
	public PrintWriter startRedirectDoc(String title, String url, int delay) throws IOException, ConfigException;
	
	public PrintWriter startRedirectDoc(String title, boolean showMenu, String url, int delay) throws IOException, ConfigException;
		
	public void finishHTMLDoc() throws IOException;
	
	public void finish() throws IOException;
	
	public String getStyle();

	public String getImagePath(String imageName);

	public Image getImage(String imageName);
	
	public boolean setMyPassword(String newPassword) throws SQLException;
	
}