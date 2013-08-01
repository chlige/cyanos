//
//  Table.java
//  Cyanos
//
//  Created by George Chlipala on 5/7/06.
//  Copyright 2006 Walnut Computer Services. All rights reserved.
//
package edu.uic.orjala.cyanos.web.html;

import java.lang.String;

/**
 *	Class to generate a HTML table element (&lt;TABLE&gt;)
 */
public class Table extends HTMLObj {
	
	public Table() { this.element = new String("TABLE"); }
	
	public Table(Object[] newData) { 
		this.element = new String("TABLE"); 
		this.addItem(newData); }
	
	public Table(Object newData) {	
		this.element = new String("TABLE"); 
		this.addItem(newData); 
	}
	
	public Table(String[] newAttrs, Object newData) { 
		this.element = new String("TABLE"); 
		this.addItem(newData); 
		this.setAttributes(newAttrs);
	}
	
	public Table(String[] newAttrs, Object[] newData) {
		this.element = new String("TABLE"); 
		this.addItem(newData);
		this.setAttributes(newAttrs);
	}

/**
 * Add an array of rows to the table
 */
	public void addRow(Object[] newItems) {
		for (int i = 0; i < newItems.length; i++) {
			this.data.add(newItems[i]);
		}
	}

/**
 * Add a row to the table
 */
	public void addRow(Object newItem) {
		this.data.add(newItem);
	}

}
