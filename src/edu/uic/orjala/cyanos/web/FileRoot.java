/**
 * 
 */
package edu.uic.orjala.cyanos.web;

import java.io.File;

/**
 * @author George Chlipala
 *
 */
public class FileRoot {

	/**
	 * 
	 */
	private File root = null;
	
	public FileRoot(String rootPath) {
		this.root = new File(rootPath);
	}
	
	public FileRoot(File rootFile) {
		this.root = rootFile;
	}

	public String getPath() {
		return this.root.getAbsolutePath();
	}
		
	public File getRootFile() {
		return this.root;
	}
		
	public String chrootFile(File aFile) {
		String aPath = aFile.getAbsolutePath();
		if ( aPath.startsWith(this.root.getAbsolutePath()))
			return aPath.substring(this.root.getAbsolutePath().length());
		return null;
	}
	
	public String chrootPath(String aPath) {
		if ( aPath.startsWith(this.root.getAbsolutePath()))
			return aPath.substring(this.root.getAbsolutePath().length());
		return "";		
	}
	
	public boolean isRoot(File aFile) {
		return this.root.equals(aFile);
	}
}
