//
//  TableRow.java
//  Cyanos
//
//  Created by George Chlipala on 5/12/06.
//  Copyright 2006 Walnut Computer Services. All rights reserved.
//
package edu.uic.orjala.cyanos.web.html;

import java.lang.String;

/**
 *	Class to generate a HTML table row (&lt;TR&gt;)
 */
public class TableRow extends HTMLListObj {	
	
	public TableRow() { this.element = new String("TR"); }
	
	public TableRow(Object[] newData) { 
		this.element = new String("TR");
		this.addItem(newData); 
	}
	
	public TableRow(Object newData) {	
		this.element = new String("TR");
		this.addItem(newData); 
	}
	
	public TableRow(String[] newAttrs, Object newData) { 
		this.element = new String("TR");
		this.addItem(newData); 
		this.setAttributes(newAttrs);
	}
	
	public TableRow(String[] newAttrs, Object[] newData) {
		this.element = new String("TR");
		this.addItem(newData);
		this.setAttributes(newAttrs);
	}

/**
 * Add an array of cells to the row.
 */
	public void addCell(Object[] newCells) {
		for (int i = 0; i < newCells.length; i++) {
			this.data.add(newCells[i]);
		}
	}

/**
 * Add a cell to the row.
 */
	public void addCell(Object newCell) {
		this.data.add(newCell);
	}

}
