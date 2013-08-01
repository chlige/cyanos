//
//  Image.java
//  HTMLTools
//
//  Created by George Chlipala on 5/13/06.
//  Copyright 2006 Walnut Computer Services. All rights reserved.
//
package edu.uic.orjala.cyanos.web.html;

import java.lang.String;

/**
 *	Class to generate an HTML image tag (&lt;IMG&gt;)
 */
public class Image extends HTMLSingleObj {	
/**
 *	Constructor
 */
	public Image() { this.element = new String("IMG"); }
	
/**
 *	Constructor
 */
	public Image(String newData) {	
		this();
		this.attributes.put("SRC", newData); 
	}
	
/**
 *	Constructor
 */
	public Image(String[] newAttrs, String newData) { 
		this();
		this.setAttributes(newAttrs);
		this.attributes.put("SRC", newData); 
	}
	
}
