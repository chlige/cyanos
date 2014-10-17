package edu.uic.orjala.cyanos.web.listener;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.store.LockObtainFailedException;

import edu.uic.orjala.cyanos.ConfigException;
import edu.uic.orjala.cyanos.sql.SQLData;
import edu.uic.orjala.cyanos.web.AppConfig;
import edu.uic.orjala.cyanos.web.AppConfigSQL;
import edu.uic.orjala.cyanos.web.UploadModule;
import edu.uic.orjala.cyanos.web.help.HelpIndex;
import edu.uic.orjala.cyanos.web.servlet.HelpServlet;
import edu.uic.orjala.cyanos.web.servlet.ServletObject;
import edu.uic.orjala.cyanos.web.servlet.UploadServlet;

/**
 * Application Lifecycle Listener implementation class AppConfigListner
 *
 */
public class AppConfigListener implements ServletContextListener {

	private static AppConfigSQL config = null;
	private static DataSource dbh = null;
	private static int idtype = SQLData.ID_TYPE_SERIAL;
	
	private static final Map<String, Class<UploadModule>> uploadModules = new HashMap<String, Class<UploadModule>>();
	
	/**
     * @see ServletContextListener#contextInitialized(ServletContextEvent)
     */
    public void contextInitialized(ServletContextEvent arg0) {
        ServletContext context = arg0.getServletContext();
		if ( context.getAttribute(ServletObject.APP_CONFIG_ATTR) == null ) {
			try {
				Context initCtx = new InitialContext();
				if ( dbh == null ) {
					dbh  = (DataSource) initCtx.lookup("java:comp/env/jdbc/" + AppConfig.CYANOS_DB_NAME);
					idtype = AppConfigSQL.getSchemaIDType(dbh);
				}

				if ( config == null ) {
					context.log("Initializing CYANOS configuration.");
					config = new AppConfigSQL();
				}
								
				context.setAttribute(ServletObject.APP_CONFIG_ATTR, config);
				context.setAttribute(UploadServlet.CUSTOM_UPLOAD_MODULES, uploadModules);
				List<String> addOns = config.classesForUploadModule();
				this.addModules(addOns, context);

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
    public void contextDestroyed(ServletContextEvent arg0) {
        ServletContext context = arg0.getServletContext();    
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
    
	private void addModules(List<String> newModules, ServletContext context) {
		if ( newModules != null && newModules.size() > 0 ) {
			ListIterator<String> anIter = newModules.listIterator();
			while ( anIter.hasNext() ) {
				String className = anIter.next();
				try {
					Class<?> aClass = Class.forName(className, true, this.getClass().getClassLoader());
					if ( aClass != null ) {
						if ( UploadModule.class.isAssignableFrom(aClass) ) {
							uploadModules.put(aClass.getName(), (Class<UploadModule>) aClass);
							context.log(String.format("LOADED upload module: %s", className));
						} else {
							context.log(String.format("Will NOT load module: %s. Does NOT implement UploadModule interface!", className));
						}
					}
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
					context.log(String.format("Could not load upload module: %s via class loader", className));
					context.log(e.getLocalizedMessage());
				}
			}
		}

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
	
	public static Connection getDBConnection() throws SQLException {
		Connection conn = dbh.getConnection();
//		System.out.format("DB Connection Open: %d\n", conn.hashCode());
		return conn;
	}
	
	public static UploadModule getUploadModule(HttpServletRequest req, String module) throws Exception {
		Class<UploadModule> aModule = uploadModules.get(module);
		if ( aModule != null ) {
			Class[] classList = { HttpServletRequest.class };
			Object[] args = { req };
			Constructor aCons = aModule.getConstructor(classList);
			return (UploadModule) aCons.newInstance(args);
		}
		return null;
	}
	
	public static boolean isNewInstall() {
		return ( config.getVersion() < 0 );
	}

	public static boolean isUpgradeInstall() {
		return ( AppConfig.APP_VERSION > config.getVersion() );
	}

}
