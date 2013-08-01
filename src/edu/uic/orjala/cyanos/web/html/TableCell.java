//
//  TableCell.java
//  HTMLTools
//
//  Created by George Chlipala on 5/13/06.
//  Copyright 2006 Walnut Computer Services. All rights reserved.
//
package edu.uic.orjala.cyanos.web.html;

import java.lang.String;

/**
 *	Class to generate a HTML table cell (&lt;TD&gt;)
 */
public class TableCell extends HTMLListObj {	
	
	public TableCell() { this.element = new String("TD"); }
	
	public TableCell(Object[] newData) { 
		this.element = new String("TD");
		this.addItem(newData); 
	}
	
	public TableCell(Object newData) {	
		this.element = new String("TD");
		this.addItem(newData); 
	}
	
	public TableCell(String[] newAttrs, Object newData) { 
		this.element = new String("TD");
		this.addItem(newData); 
		this.setAttributes(newAttrs);
	}
	
	public TableCell(String[] newAttrs, Object[] newData) {
		this.element = new String("TD");
		this.addItem(newData);
		this.setAttributes(newAttrs);
	}
	
/**
 * Set the cell as a table header (&lt;TH&gt;) or regular table cell (&lt;TD&gt;)
 */
	public void setHeader(boolean header) {
		if (header) {
			this.element = new String("TH");
		} else {
			this.element = new String("TD");
		}
	}
}
