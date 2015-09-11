/**
 * 
 */
package edu.uic.orjala.cyanos;

import java.util.Date;

/**
 * @author George Chlipala
 *
 */
public class ContentVersion {
	
	private int version;
	private Date versionDate;
	private String content;
	
	public ContentVersion(int version, Date date, String content) {
		this.version = version;
		this.versionDate = date;
		this.content = content;
	}
	
	public int getVersion() {
		return version;
	}
	
	public Date getDate() {
		return this.versionDate;
	}
	
	public String getContent() { 
		return this.content;
	}

}
