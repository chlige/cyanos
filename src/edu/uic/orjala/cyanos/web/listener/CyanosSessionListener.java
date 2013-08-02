/**
 * 
 */
package edu.uic.orjala.cyanos.web.listener;

import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import edu.uic.orjala.cyanos.web.servlet.ServletObject;

/**
 * @author George Chlipala
 *
 */
public class CyanosSessionListener implements HttpSessionListener {

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSessionListener#sessionCreated(javax.servlet.http.HttpSessionEvent)
	 */
	@Override
	public void sessionCreated(HttpSessionEvent event) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSessionListener#sessionDestroyed(javax.servlet.http.HttpSessionEvent)
	 */
	@Override
	public void sessionDestroyed(HttpSessionEvent event) {
		HttpSession session = event.getSession();
		if ( session.getAttribute(ServletObject.DB_CONN) != null ) {
			Connection conn = (Connection) session.getAttribute(ServletObject.DB_CONN);
			try {
				if ( ! conn.isClosed() ) { conn.close(); }
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}		
	}

}
