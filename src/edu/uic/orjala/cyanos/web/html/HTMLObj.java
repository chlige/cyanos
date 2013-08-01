//
//  HTMLObj.java
//  Cyanos
//
//  Created by George Chlipala on 5/10/06.
//  Copyright 2006 Walnut Computer Services. All rights reserved.
//
package edu.uic.orjala.cyanos.web.html;

import java.lang.String;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

abstract class HTMLObj {
	
	protected List<Object> data = new ArrayList<Object>();
	protected Map<String,String> attributes = new HashMap<String,String>();
	protected String element;
	
	protected void init() {
		this.data = new ArrayList<Object>();
		this.attributes = new HashMap<String,String>();
	}

/**
 *	Sets HTML tag element, e.g. A, P, H1, TABLE, etc.
 */
	public void setElement(String newElement) {
		element = newElement;
	}
	
/**
 *	Adds an item to include between the tags.
 */
	public void addItem(Object newItem) {
		this.data.add(newItem);
	}
	
/**
 *	Adds an array of items to include between the tags.
 */
	public void addItem(Object[] newItem) {
		for (int i = 0; i < newItem.length; i++) {
			this.data.add(newItem[i]);
		}
	}
	
	public void setAttributes(String[] newAttrs) {
		int i,j;
		for ( i = 0; i < newAttrs.length; i = i + 2) {
			j = i + 1;
			this.attributes.put(newAttrs[i], newAttrs[j]);
		}
	}
	
/**
 *	Sets HTML class for the object (CSS)
 */
	public void setClass(String newClass) {
		this.attributes.put("CLASS", newClass);
	}
	
/**
 *	Sets an attribute for the object
 */
	public void setAttribute(String key, String value) {
		this.attributes.put(key, value);
	}
	
	protected String attrString() {
		StringBuffer attrs = new StringBuffer();
		
		for (Iterator<String> key = this.attributes.keySet().iterator(); key.hasNext(); ) {
			Object keyObj = key.next();
			attrs.append(" " + keyObj.toString() + "=" + '"' + this.attributes.get(keyObj) + '"');
		}
		return attrs.toString();		
	}
	
/**
 *	Generates HTML for the object
 */
	public String toString() {
		StringBuffer output = new StringBuffer(); 
		
		output.append("<" + this.element + this.attrString() + ">");
		for (ListIterator<Object> i = this.data.listIterator(); i.hasNext(); ) {
				Object item = i.next();
				if ( item != null ) {
					output.append(item.toString());
				}
		}
		output.append("</" + this.element + ">");
		return output.toString();
	}
}
