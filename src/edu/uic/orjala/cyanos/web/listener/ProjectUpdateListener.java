/**
 * 
 */
package edu.uic.orjala.cyanos.web.listener;

import java.net.CookieHandler;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Timer;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.sql.DataSource;

import edu.uic.orjala.cyanos.ConfigException;
import edu.uic.orjala.cyanos.sql.SQLProject;
import edu.uic.orjala.cyanos.web.AppConfigSQL;
import edu.uic.orjala.cyanos.web.task.ProjectUpdateTask;

/**
 * @author George Chlipala
 *
 */
public class ProjectUpdateListener implements ServletContextListener {

	/**
	 * @author George Chlipala
	 *
	 */
	public class UpdateAllProjectsTask extends ProjectUpdateTask {
		
		protected UpdateAllProjectsTask() {
			
		}
		
		@Override
		public void run() {
			Connection aConn = null;
				try {
					config = new AppConfigSQL();
					this.setupKeypair();

					if ( this.privKey != null ) {
						DataSource myDS = config.getDataSourceObject();
						aConn = myDS.getConnection();
						Statement aSth = aConn.createStatement();
						ResultSet projects = aSth.executeQuery(SQLProject.SQL_UPDATABLE_PROJECTS);

						CookieHandler.setDefault(cookies);
						
						projects.beforeFirst();
						while ( projects.next() ) {
							this.updateProject(aConn, projects.getString(1));
						}
						projects.close();
						aSth.close();
					}
				} catch (SQLException e) {
					e.printStackTrace();
				} catch (ConfigException e) {
					e.printStackTrace();
				} catch (GeneralSecurityException e) {
					e.printStackTrace();
				} finally {
					try {
						if ( aConn != null && ! aConn.isClosed() )
							aConn.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
					this.clearFields();
					this.myData = null;
					this.masterPubKey = null;
					this.masterURL = null;
					this.config = null;
					this.privKey = null;
				}
		}

	}


	protected static KeyFactory keyFactory;
	
	// Set the delay to 1 day.
	private static final long DELAY = 24 * 3600 * 1000;
	
	// Run at 2 in the morning
	private static final int UPDATE_HOUR = 8;
	
	private UpdateAllProjectsTask updateTask = null;
	private Timer updateTimer = null;
	
	public static final String UPDATE_TIMER_ATTR = "updateTimer";

	/* (non-Javadoc)
	 * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
	 */
	public void contextDestroyed(ServletContextEvent context) {
		this.updateTask.cancel();
		this.updateTimer.cancel();
	}

	/* (non-Javadoc)
	 * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
	 */
	public void contextInitialized(ServletContextEvent context) {
		updateTask = new UpdateAllProjectsTask();
		Calendar updateTime = Calendar.getInstance();
		updateTime.set(Calendar.HOUR_OF_DAY, UPDATE_HOUR);
		updateTime.set(Calendar.MINUTE, 35);
		updateTime.set(Calendar.SECOND, 0);
//		updateTime.add(Calendar.DATE, 1);
		updateTimer = new Timer();
		updateTimer.schedule(updateTask, updateTime.getTime(), DELAY);
	}	
}
