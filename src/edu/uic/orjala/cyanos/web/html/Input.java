//
//  Input.java
//  HTMLTools
//
//  Created by George Chlipala on 5/13/06.
//  Copyright 2006 Walnut Computer Services. All rights reserved.
//
package edu.uic.orjala.cyanos.web.html;

import java.lang.String;

/**
 *	Class to generate an HTML input tag (&lt;INPUT&gt;)
 */
public class Input extends HTMLSingleObj {	
/**
 *	Constructor
 */
	public Input() { this.element = new String("INPUT"); }
	
/**
 *	Constructor
 */
	public Input(String newType) { 
		this();
		this.attributes.put("type", newType);
	}
	
/**
 *	Constructor
 */
	public Input(String[] newAttrs) {
		this();
		this.setAttributes(newAttrs);
	}
	
/**
 *	Set type, e.g. text, password, button.
 */
	public void setType(String newType) {
		this.attributes.put("type", newType);
	}
	
/**
 *	Set default value.
 */
	public void setValue(String newValue) {
		this.attributes.put("value", newValue);
	}
	
/**
 *	Set name.
 */
	public void setName(String newName) {
		this.attributes.put("name", newName);
	}
		
}
