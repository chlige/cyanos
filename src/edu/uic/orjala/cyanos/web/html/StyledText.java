//
//  StyledText.java
//  HTMLTools
//
//  Created by George Chlipala on 5/13/06.
//  Copyright 2006 Walnut Computer Services. All rights reserved.
//
package edu.uic.orjala.cyanos.web.html;

import java.lang.String;
import java.util.Iterator;

/**
 *	Class to generate a HTML font element (&lt;FONT&gt;)
 */
public class StyledText extends HTMLObj {	
	
	protected StringBuffer textString = new StringBuffer();
	
	public StyledText() { }
		
	public StyledText(String newString) {	
		this.textString.append(newString); 
	}
	
	public StyledText(String[] newAttrs, String newString) { 
		this.textString.append(newString); 
		this.setAttributes(newAttrs);
	}
		
/**
 * Add string to the content.
 */
	public void addString(String newString) {
		this.textString.append(newString); 
	}
	
/**
 * Set the font size.
 */
	public void setSize(int newSize) {
		Integer newInt = new Integer(newSize);
		this.attributes.put("size", newInt.toString());
	}

/**
 * Set the font size.
 */
	public void setSize(String newSize) {
		this.attributes.put("size", newSize);
	}
	
/**
 * Set the font color.
 */
	public void setColor(String newColor) {
		this.attributes.put("color", newColor);
	}
	
/**
 * Add a string to the content and make bold (&lt;B&gt;)
 */
	public void addBoldString(String newString) {
		this.textString.append("<B>" + newString + "</B>");
	}
	
/**
 * Add a string to the content and make italic (&lt;I&gt;)
 */
	public void addItalicString(String newString) {
		this.textString.append("<I>" + newString + "</I>");
	}

/**
 * Add a string to the content and make bold and italic (&lt;B&gt;&lt;I&gt;)
 */
	public void addBoldAndItalicString(String newString) {
		this.addBoldString("<I>" + newString + "</I>");
	}

/**
 * Generate HTML
 */
	public String toString() {
		StringBuffer output = new StringBuffer(); 
		StringBuffer attrs = new StringBuffer();
				
		for (Iterator<String> key = this.attributes.keySet().iterator(); key.hasNext(); ) {
			Object keyObj = key.next();
			attrs.append(keyObj.toString() + "=" + '"' + this.attributes.get(keyObj) + '"'+ " ");
		}
		
		output.append("<FONT " + attrs.toString() + ">" + this.textString.toString() + "</FONT>");
		return output.toString();
	}	
	
}
