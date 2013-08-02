package edu.uic.orjala.cyanos.web.listener;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpServletRequest;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.store.LockObtainFailedException;

import edu.uic.orjala.cyanos.ConfigException;
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
	private static final Map<String, Class<UploadModule>> uploadModules = new HashMap<String, Class<UploadModule>>();
	
	/**
     * @see ServletContextListener#contextInitialized(ServletContextEvent)
     */
    public void contextInitialized(ServletContextEvent arg0) {
        ServletContext context = arg0.getServletContext();
		if ( context.getAttribute(ServletObject.APP_CONFIG_ATTR) == null ) {
			try {
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
			} catch (ConfigException e) {
				context.log("UNABLE TO INITIALIZE CYANOS CONFIG", e);
			}
		}
		
		try {
			context.log("Building CYANOS help index.");
			String path = context.getRealPath(HelpServlet.HELP_PATH);
			HelpIndex.rebuildIndex(path);
		} catch (CorruptIndexException e) {
			context.log("UNABLE TO BUILD HELP INDEX", e);
		} catch (LockObtainFailedException e) {
			context.log("UNABLE TO BUILD HELP INDEX", e);
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


}
