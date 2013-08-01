//
//  TableHeader.java
//  Cyanos
//
//  Created by George Chlipala on 5/10/06.
//  Copyright 2006 Walnut Computer Services. All rights reserved.
//
package edu.uic.orjala.cyanos.web.html;

import java.lang.String;

/**
 *	Class to generate a HTML table header (&lt;TH&gt;)
 */
public class TableHeader extends TableCell {
	
	public TableHeader() { this.element = new String("TH"); }
	
	public TableHeader(Object[] newData) { 
		this.element = new String("TH");
		this.addItem(newData); 
	}
	
	public TableHeader(Object newData) {	
		this.element = new String("TH");
		this.addItem(newData); 
	}
	
	public TableHeader(String[] newAttrs, Object newData) { 
		this.element = new String("TH");
		this.addItem(newData); 
		this.setAttributes(newAttrs);
	}
	
	public TableHeader(String[] newAttrs, Object[] newData) {
		this.element = new String("TH");
		this.addItem(newData);
		this.setAttributes(newAttrs);
	}
}
