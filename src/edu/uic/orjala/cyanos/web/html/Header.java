//
//  Header.java
//  HTMLTools
//
//  Created by George Chlipala on 5/14/06.
//  Copyright 2006 Walnut Computer Services. All rights reserved.
//
package edu.uic.orjala.cyanos.web.html;

import java.lang.String;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

/**
 *	Class to generate a HTML header (&lt;HEADER1&gt;)
 */
public class Header {
	
	protected final Map<String, String> baseItems = new HashMap<String, String>();
	protected final Map<String, String> metaItems = new HashMap<String, String>();
	protected final List<Map<String,String>> cssItems = new ArrayList<Map<String,String>>();
	protected final List<Map<String,String>> scripts = new ArrayList<Map<String,String>>();
	protected final List<String> tags = new ArrayList<String>();
	
	protected int version = 0;
	protected int dtd = 1;
	
	private static final int VERSION_HTML4 = 0;
	private static final int VERSION_XHTML = 1;
	
	private static final int DTD_STRICT = 0;
	private static final int DTD_TRANSITIONAL = 1;
	private static final int DTD_FRAMESET = 3;
	
	private static final String[][] DTDS = {
		{ "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">", 
			"<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">",
			"<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Frameset//EN\" \"http://www.w3.org/TR/html4/frameset.dtd\">" },
		{ "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">",
			"<!DOCTYPE HTML PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">",
			"<!DOCTYPE HTML PUBLIC \"-//W3C//DTD XHTML 1.0 Frameset//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd\">"	}
	};
	
/**
 *	Constructor
 */
	public Header() { }
	
/**
 *	Set &lt;TITLE&gt; information
 */
	public void setTitle(String newTitle) {
		this.baseItems.put("title", newTitle);
	}
	
/**
 *	Add &lt;META&gt; information
 */
	public void addMeta(String name, String content) {
		this.metaItems.put(name, content);
	}
	
/**
 *	Add &lt;STYLE&gt; file information
 */
	public void addCSSFile(String filename) {
		Map<String,String> styleItem = new HashMap<String,String>();
		styleItem.put("src", filename);
		this.cssItems.add(styleItem);
	}

/**
 *	Add &lt;STYLE&gt; content
 */
	public void addCSSContent(String content) {
		Map<String,String> styleItem = new HashMap<String,String>();
		styleItem.put("content", content);
		this.cssItems.add(styleItem);
	}

/**
 *	Add &lt;SCRIPT&gt; file information
 */
	public void addScriptFile(String lang, String file) {
		Map<String,String> scriptItem = new HashMap<String,String>();
		scriptItem.put("src", file);
		scriptItem.put("language", lang);
		this.scripts.add(scriptItem);
	}

/**
 *	Add &lt;SCRIPT&gt; content
 */
	public void addScriptContent(String lang, String content) {
		Map<String,String> scriptItem = new HashMap<String,String>();
		scriptItem.put("code", content);
		scriptItem.put("language", lang);
		this.scripts.add(scriptItem);
	}

/**
 * Add a Javascript file.
 * Shortcut for <code>addScriptFile("javascript", file)</code>
 */
	public void addJavascriptFile(String file) {
		this.addScriptFile("JAVASCRIPT", file);
	}
	
/**
 * Add Javascript content.
 * Shortcut for <code>addScriptContent("javascript", content)</code>
 */
	public void addJavascriptContent(String content) {
		this.addScriptContent("JAVASCRIPT", content);
	}
	
	public void setVersionXHTML() {
		this.version = VERSION_XHTML;
	}
	
	public void setVersionHTML4() {
		this.version = VERSION_HTML4;
	}
	
	public void setStrictDTD() {
		this.dtd = DTD_STRICT;
	}
	
	public void setTransitionalDTD() {
		this.dtd = DTD_TRANSITIONAL;
	}
	
	public void setFramsetDTD() {
		this.dtd = DTD_FRAMESET;
	}
	
	public void addTag(String tag) {
		this.tags.add(tag);
	}

/**
 * Generate HTML 
 */
	public String toString() {
		StringBuffer output = new StringBuffer(DTDS[this.version][this.dtd]);
		output.append("<HTML><HEAD>");
				
		for (String key : this.baseItems.keySet() ) {
			output.append("<" + key.toString() + ">" + this.baseItems.get(key) + 
				"</" + key.toString() + ">\n");
		}
		
		for (String metaName : this.metaItems.keySet() ) {
			output.append("<META NAME=\"" + metaName.toString() + 
				"\" CONTENT=\"" + this.metaItems.get(metaName) + "\">\n");
		}
		
		for (Map<String, String> script: this.scripts ) {
			if ( script.containsKey("code") ) {
				output.append("<SCRIPT LANGUAGE=\""  + script.get("language") + "\">\n" +
					script.get("code") + "\n</SCRIPT>\n");
			} else {
				output.append("<SCRIPT LANGUAGE=\"" + script.get("language") + "\" SRC=\"" +
					script.get("src") + "\"></SCRIPT>");
			}
		}

		for (Map<String,String> style : this.cssItems ) {
			if ( style.containsKey("code") ) {
				output.append("<STYLE TYPE=\"text/css\">\n" + style.get("code") + "\n</STYLE>\n");
			} else {
				output.append("<LINK REL=\"stylesheet\" TYPE=\"text/css\" HREF=\"" + style.get("src") + "\"/>");
			}
		}
		
		for ( String tag : this.tags ) {
			output.append(tag);
		}
		
		output.append("</HEAD>");
		return output.toString();
	}
}
