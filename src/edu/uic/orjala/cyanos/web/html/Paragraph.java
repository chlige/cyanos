//
//  Paragraph.java
//  HTMLTools
//
//  Created by George Chlipala on 5/14/06.
//  Copyright 2006 Walnut Computer Services. All rights reserved.
//
package edu.uic.orjala.cyanos.web.html;

import java.lang.String;

/**
 *	Class to generate a HTML paragraph (&lt;P&gt;)
 */
public class Paragraph extends HTMLObj {
	
	public Paragraph() { this.element = new String("P"); }
	
	public Paragraph(Object[] newData) { 
		this();
		this.addItem(newData); 
	}
	
	public Paragraph(Object newData) {	
		this();
		this.addItem(newData); 
	}
	
	public Paragraph(String[] newAttrs, Object newData) { 
		this();
		this.addItem(newData); 
		this.setAttributes(newAttrs);
	}
	
	public Paragraph(String[] newAttrs, Object[] newData) {
		this();
		this.addItem(newData); 
		this.setAttributes(newAttrs);
	}

/**
 * Set paragraph alignment, e.g. left, center, or right.
 */
	public void setAlign(String newAlign) {
		this.attributes.put("ALIGN", newAlign);
	}
}
