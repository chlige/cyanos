package edu.uic.orjala.cyanos.web;

import java.io.File;
import java.io.FileFilter;

public class CustomJarFilter implements FileFilter {

	long lastModified;
	
	/**
	 * 
	 */
	public CustomJarFilter(File parentFile) {
		lastModified = parentFile.lastModified();
	}

	/* (non-Javadoc)
	 * @see java.io.FileFilter#accept(java.io.File)
	 */
	public boolean accept(File arg0) {
		System.out.format("PARENT: %d %s: %d\n", lastModified, arg0.getName(), arg0.lastModified());
		return arg0.lastModified() > lastModified;
	}

}