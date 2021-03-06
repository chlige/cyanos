package edu.uic.orjala.cyanos.web.listener;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.mail.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.sql.DataSource;

import edu.uic.orjala.cyanos.ConfigException;
import edu.uic.orjala.cyanos.sql.SQLData;
import edu.uic.orjala.cyanos.web.AppConfig;
import edu.uic.orjala.cyanos.web.AppConfigSQL;
import edu.uic.orjala.cyanos.web.help.HelpIndex;
import edu.uic.orjala.cyanos.web.servlet.HelpServlet;
import edu.uic.orjala.cyanos.web.servlet.ServletObject;

/**
 * Application Lifecycle Listener implementation class AppConfigListner
 *
 */
public class AppConfigListener implements ServletContextListener {

	private static AppConfigSQL config = null;
	private static DataSource dbh = null;
	private static int idtype = SQLData.ID_TYPE_SERIAL;
	private static String filePath = null;
	private static String instanceName = null;
	
	/**
     * @see ServletContextListener#contextInitialized(ServletContextEvent)
     */
    public void contextInitialized(ServletContextEvent ev) {
        ServletContext context = ev.getServletContext();
		if ( context.getAttribute(ServletObject.APP_CONFIG_ATTR) == null ) {
			try {
				Context initCtx = new InitialContext();
				Context envCtx = (Context) initCtx.lookup("java:comp/env");
				
				if ( filePath == null )
					filePath = (String) envCtx.lookup("filePath");
				
				if ( instanceName == null ) 
					instanceName = (String) envCtx.lookup("name");
				
				if ( dbh == null ) {
					dbh  = (DataSource) initCtx.lookup("java:comp/env/jdbc/" + AppConfig.CYANOS_DB_NAME);
					idtype = AppConfigSQL.getSchemaIDType(dbh);
				}

				if ( config == null ) {
					context.log(String.format("Initializing CYANOS configuration for %s.", instanceName));
					config = new AppConfigSQL();
				}
								
				context.setAttribute(ServletObject.APP_CONFIG_ATTR, config);
				
//				context.setAttribute(UploadServlet.CUSTOM_UPLOAD_MODULES, uploadModules);
//				List<String> addOns = config.classesForUploadModule();
//				this.addModules(addOns, context);

				/* Older XML configuration
				String configLocation = (String) envCtx.lookup(ServletWrapper.APP_CONFIG_ATTR);
				if ( configLocation == null ) this.newInstall = true;
				else {
					myConf = new AppConfig((String) envCtx.lookup(ServletWrapper.APP_CONFIG_ATTR));
					srvCtx.setAttribute(ServletWrapper.APP_CONFIG_ATTR, myConf);
				}
				*/
			} catch (ConfigException e ) {
				context.log("UNABLE TO INITIALIZE CYANOS", e);
			} catch ( NamingException e ) {
				context.log("UNABLE TO INITIALIZE CYANOS", e);
			} catch ( SQLException e) {
				context.log("UNABLE TO INITIALIZE CYANOS", e);				
			}
		}
		
		try {
			context.log("Building CYANOS help index.");
			String path = context.getRealPath(HelpServlet.HELP_PATH);
			HelpIndex.rebuildIndex(path);
		} catch (IOException e) {
			context.log("UNABLE TO BUILD HELP INDEX", e);
		}
		
   }

	/**
     * @see ServletContextListener#contextDestroyed(ServletContextEvent)
     */
    public void contextDestroyed(ServletContextEvent ev) {
        ServletContext context = ev.getServletContext();    
        if ( config != null && config.isUnsaved() ) {
        	try {
				config.writeConfig();
			} catch (ConfigException e) {
				System.err.println("COULD NOT WRITE CONFIGURATION!!");
				e.printStackTrace();
			}
        }
        context.removeAttribute(ServletObject.APP_CONFIG_ATTR);
    }
	
    
    public static void reloadConfig() throws ConfigException {
    	if ( config != null )
    		config.loadConfig();
    }
    
	public static AppConfig getConfig() {
		return config;
	}
	
	public static DataSource getDataSource() {
		return dbh;
	}
	
	public static int getIDType() {
		return idtype;
	}
	
	public static String getFilePath() {
		return filePath;
	}
	
	public static Connection getDBConnection() throws SQLException {
		Connection conn = dbh.getConnection();
//		System.out.format("DB Connection Open: %d\n", conn.hashCode());
		return conn;
	}
	
	public static boolean isNewInstall() {
		return ( config.getVersion() < 0 );
	}

	public static boolean isUpgradeInstall() {
		return ( AppConfig.APP_VERSION > config.getVersion() );
	}

	public static Session getMailSession() throws NamingException {		
		Context initCtx = new InitialContext();
		return (Session) initCtx.lookup("java:comp/env/mail/Session");
	}


}
