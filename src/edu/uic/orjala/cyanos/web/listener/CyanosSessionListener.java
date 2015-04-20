/**
 * 
 */
package edu.uic.orjala.cyanos.web.listener;

import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import edu.uic.orjala.cyanos.web.Job;
import edu.uic.orjala.cyanos.web.JobManager;
import edu.uic.orjala.cyanos.web.servlet.ServletObject;

/**
 * @author George Chlipala
 *
 */
public class CyanosSessionListener implements HttpSessionListener {
	
	private static final String JOB_MANAGER = "jobs";

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSessionListener#sessionCreated(javax.servlet.http.HttpSessionEvent)
	 */
	@Override
	public void sessionCreated(HttpSessionEvent event) {
//		HttpSession session = event.getSession();
//		session.setAttribute(JOB_MANAGER, new JobManager());
	}

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpSessionListener#sessionDestroyed(javax.servlet.http.HttpSessionEvent)
	 */
	@Override
	public void sessionDestroyed(HttpSessionEvent event) {
		HttpSession session = event.getSession();
		if ( session.getAttribute(JOB_MANAGER) != null ) {
			session.removeAttribute(JOB_MANAGER);
		}
		if ( session.getAttribute(ServletObject.DB_CONN) != null ) {
			Connection conn = (Connection) session.getAttribute(ServletObject.DB_CONN);
			try {
				if ( ! conn.isClosed() ) { conn.close(); }
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}		
	}
	
	public static JobManager getJobManager(HttpSession session) {
		Object boss = session.getAttribute(JOB_MANAGER);
		if ( boss instanceof JobManager ) {
			return (JobManager)boss;
		} else {
			boss = new JobManager();
			session.setAttribute(JOB_MANAGER, boss);
			return (JobManager)boss;
		}
	}

	public static JobManager getJobManager(HttpServletRequest request) {
		return getJobManager(request.getSession());
	}
	
	public static void addJob(HttpSession session, Job job) {
		JobManager boss = getJobManager(session);
		boss.addJob(job);
	}
}
