//
//  TextArea.java
//  HTMLTools
//
//  Created by George Chlipala on 5/13/06.
//  Copyright 2006 Walnut Computer Services. All rights reserved.
//
package edu.uic.orjala.cyanos.web.html;

import java.lang.String;

/**
 *	Class to generate a HTML textarea form element (&lt;TEXTAREA&gt;)
 */
public class TextArea extends HTMLObj {	
	
	public TextArea() { this.element = new String("TEXTAREA"); }
	
	public TextArea(Object[] newData) { 
		this();
		this.addItem(newData); 
	}
	
	public TextArea(Object newData) {	
		this();
		this.addItem(newData); 
	}
	
	public TextArea(String[] newAttrs, Object newData) { 
		this();
		this.addItem(newData); 
		this.setAttributes(newAttrs);
	}
	
	public TextArea(String[] newAttrs, Object[] newData) {
		this();
		this.addItem(newData); 
		this.setAttributes(newAttrs);
	}
	
/**
 * Set name of textarea.
 */
	public void setName(String newName) {
		this.attributes.put("name", newName);
	}
		
}
