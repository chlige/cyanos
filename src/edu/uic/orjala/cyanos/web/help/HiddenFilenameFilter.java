/**
 * 
 */
package edu.uic.orjala.cyanos.web.help;

import java.io.File;
import java.io.FilenameFilter;

/**
 * @author George Chlipala
 *
 */
public class HiddenFilenameFilter implements FilenameFilter {

	/* (non-Javadoc)
	 * @see java.io.FilenameFilter#accept(java.io.File, java.lang.String)
	 */
	public boolean accept(File dir, String name) {
		return ! name.startsWith(".");
	}

}
