package edu.uic.orjala.cyanos.web.listener;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpServletRequest;

import edu.uic.orjala.cyanos.ConfigException;
import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.User;
import edu.uic.orjala.cyanos.sql.SQLData;
import edu.uic.orjala.cyanos.sql.SQLUser;
import edu.uic.orjala.cyanos.web.FileUpload;
import edu.uic.orjala.cyanos.web.GuestUser;
import edu.uic.orjala.cyanos.web.MultiPartRequest;

/**
 * Application Lifecycle Listener implementation class CyanosRequestListener
 *
 */
public class CyanosRequestListener implements ServletRequestListener {

	private static final String DATASOURCE = "datasource";
	
	private static final String USER = "user";

	private final static Pattern MOBILE_PATTERN = Pattern.compile("iphone|ipad|ipod|android|blackberry|mini|windows\\sce|palm/i");
	
	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequestListener#requestDestroyed(javax.servlet.ServletRequestEvent)
	 */
	@Override
	public void requestDestroyed(ServletRequestEvent event) {
		ServletRequest object = event.getServletRequest();
		if ( object instanceof HttpServletRequest ) {
			HttpServletRequest req = (HttpServletRequest) object;
			Object data = req.getAttribute(DATASOURCE);
			if ( data instanceof SQLData ) {
				try {
					((SQLData)data).close();
					((SQLData)data).closeDBC();
				} catch (DataException e) {
					e.printStackTrace();
				}
			}
			Object user = req.getAttribute(USER);
			if ( user instanceof SQLUser ) {
				try {
					((SQLUser)user).closeAll();
				} catch (DataException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletRequestListener#requestInitialized(javax.servlet.ServletRequestEvent)
	 */
	@Override
	public void requestInitialized(ServletRequestEvent event) {
//		ServletRequest object = event.getServletRequest();
//		if ( object instanceof HttpServletRequest ) {
//
//		}
	}
	

	public static FileUpload getUpload(HttpServletRequest request, String name) throws ServletException, IOException {
		return getUpload(request, name, 0);
	}
	
	public static FileUpload getUpload(HttpServletRequest request, String name, int index) throws ServletException, IOException {
		HttpServletRequest mpReq = MultiPartRequest.parseRequest(request);
		if ( mpReq instanceof MultiPartRequest ) {
			return ((MultiPartRequest)mpReq).getUpload(name, index);
		}
		return null;
	}

	public static int getUploadCount(HttpServletRequest request, String name) throws ServletException, IOException {
		HttpServletRequest mpReq = MultiPartRequest.parseRequest(request);
		if ( mpReq instanceof MultiPartRequest ) {
			return ((MultiPartRequest)mpReq).getUploadCount(name);
		}
		return 0;		
	}
	
	public static SQLData getSQLData(HttpServletRequest req) throws SQLException, ConfigException, DataException {
		Object data = req.getAttribute(DATASOURCE);
		if ( data == null ) {
			data = newSQLData(req);
			req.setAttribute(DATASOURCE, data);
		}
		return (SQLData) data;
	}
	
	public static SQLData newSQLData(HttpServletRequest req) throws SQLException, ConfigException, DataException {
		Connection conn = AppConfigListener.getDBConnection();
		return new SQLData(AppConfigListener.getConfig(), conn, getUser(req), AppConfigListener.getIDType());			
	}
	
	public static User getUser(HttpServletRequest req) throws DataException, SQLException {
		User user = (User) req.getAttribute(USER);
		if ( user == null ) {
			if ( req.getRemoteUser() == null )
				user = getGuestUser();
			else 
				user = new SQLUser(AppConfigListener.getDBConnection(), req.getRemoteUser());
			req.setAttribute(USER, user);
		}
		return user;
	}
	
	public static boolean isMobile(HttpServletRequest req) {
		String userAgent = req.getHeader("User-Agent");	
		if ( userAgent != null ) {
			return MOBILE_PATTERN.matcher(userAgent).matches();
		}
		return false;
	}
	
	protected static User getGuestUser() {
		String[] roles = { User.CULTURE_ROLE };
		String[] projects = { GuestUser.GLOBAL_PROJECT };
		return new GuestUser(roles, projects);
	}


}
