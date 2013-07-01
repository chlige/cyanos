/**
 * 
 */
package edu.uic.orjala.cyanos.web;

import edu.uic.orjala.cyanos.CyanosObject;

/**
 * This interface defines the basic methods required to integrate with the web inteface of the 
 * CYANOS data management system. <P>Custom modules should be defined within the Tomcat configuration
 * as a JNDI resource. Please refer to 
 * <A HREF="http://tomcat.apache.org/tomcat-5.5-doc/jndi-resources-howto.html" TARGET='_new'>http://tomcat.apache.org/tomcat-5.5-doc/jndi-resources-howto.html</A>
 * for additional details.  The modules should be defined in the "modules" JNDI container. 
 * For example, the following Tomcat configuration statements would load the module "MyModule", which is 
 * defined by the class edu.state.MyModule.  The code for this module should be the defined classpath of the
 * application server.</P> 
 * <P><CODE>
 * &lt;Resource name="module/MyModule" auth="Container"
 * 		type="edu.state.MyModule" /&gt;
 * </CODE></P>
 * 
 * @author George Chlipala
 *
 */
public interface WebModule {
	
	/**
	 * Returns true if the specified cyanos Class can be used by the module.  
	 * This method is required by the servlets to build a list of custom modules.
	 * 
	 * @param aClass an edu.uic.orjala.cyanos Class
	 * @return YES if extra content is available through this module.
	 */
	boolean isEligible(Class aClass);
	
	/**
	 * Returns the content title.
	 * 
	 * @param anObject an edu.uic.orjala.cyanos Object
	 * @return Title of content for this object.  Null string if object is ineligible.
	 */
	String getTitle(CyanosObject anObject);
	
	/**
	 * Returns the custom content for an object.
	 * 
	 * @param anObject an edu.uic.orjala.cyanos Object
	 * @return Custom content, as a String, for this object. Null string if object is ineligible.
	 */
	String getContent(CyanosObject anObject);
	
}
