//
//  ListItem.java
//  HTMLTools
//
//  Created by George Chlipala on 5/13/06.
//  Copyright 2006 Walnut Computer Services. All rights reserved.
//
package edu.uic.orjala.cyanos.web.html;

import java.lang.String;

/**
 *	Class to generate a HTML list items (&lt;LI&gt;)
 */
public class ListItem extends HTMLListObj {	
	public ListItem() { this.element = new String("LI"); }
	
	public ListItem(Object[] newData) { 
		this();
		this.addItem(newData); 
	}
	
	public ListItem(Object newData) {	
		this();
		this.addItem(newData); 
	}
	
	public ListItem(String[] newAttrs, Object newData) { 
		this();
		this.addItem(newData); 
		this.setAttributes(newAttrs);
	}
	
	public ListItem(String[] newAttrs, Object[] newData) {
		this();
		this.addItem(newData); 
		this.setAttributes(newAttrs);
	}
}
