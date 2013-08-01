//
//  Popup.java
//  HTMLTools
//
//  Created by George Chlipala on 5/13/06.
//  Copyright 2006 Walnut Computer Services. All rights reserved.
//
package edu.uic.orjala.cyanos.web.html;

import java.lang.String;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *	Class to generate a HTML select form element (&lt;SELECT&gt;)
 */
public class Popup {	
	
	protected Map<String,String> options = new HashMap<String,String>();
	protected Map<String,String> attributes = new HashMap<String,String>();
	protected List<Object> keys = new ArrayList<Object>();
	protected String defValue;
	
	public Popup() { }
	
	public Popup(String[] newData) { 
		this.addItem(newData); 
	}
	
	public Popup(String newData) {	
		this.addItem(newData); 
	}
	
	public Popup(String[] newAttrs, String newData) { 
		this.addItem(newData); 
		this.setAttributes(newAttrs);
	}
	
	public Popup(String[] newAttrs, String[] newData) {
		this();
		this.addItem(newData); 
		this.setAttributes(newAttrs);
	}
	
/**
 * Set name
 */
	public void setName(String newName) {
		this.attributes.put("name", newName);
	}

/**
 * Set default value
 */
	public void setDefault(String newValue) {
		if ( newValue != null )
			this.defValue = new String(newValue);
		else 
			this.defValue = null;
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

	public void setAttributes(String[] newAttrs) {
		int i,j;
		for ( i = 0; i < newAttrs.length; i = i + 2) {
			j = i + 1;
			this.attributes.put(newAttrs[i], newAttrs[j]);
		}
	}
/**
 * Add an item to the popup (&lt;OPTION&gt;)
 */
	public void addItem(String newValue) {
		if ( newValue != null ) {
			this.options.put(newValue, newValue);
			this.keys.add(newValue);
		}
	}
	
/**
 * Add an array of items to the popup (&lt;OPTION&gt;)
 */
	public void addItem(String[] newItem) {
		for (int i = 0; i < newItem.length; i++) {
			if ( newItem[i] != null ) {
				this.options.put(newItem[i], newItem[i]);
				this.keys.add(newItem[i]);
			}
		}
	}
		
/**
 * Add an item to the popup with a label (&lt;OPTION&gt;)
 */
	public void addItemWithLabel(String newValue, String newLabel) {
		if ( newValue != null ) {
			this.options.put(newValue, newLabel);
			this.keys.add(newValue);
		}
	}
	
/**
 * Add an array of items to the popup with labels (&lt;OPTION&gt;)
 */
	public void addItemWithLabel(String[] newItem, String[] newLabel) {
		for (int i = 0; i < newItem.length; i++) {
			if ( newItem[i] != null ) {
				this.options.put(newItem[i], newLabel[i]);
				this.keys.add(newItem[i]);
			}
		}
	}
/**
 * Add an item to the popup (&lt;OPTION&gt;) at specified index.
 */
	public void addItem(String newValue, int index) {
		if ( newValue != null ) {
			this.options.put(newValue, newValue);
			this.keys.add(index, newValue);
		}
	}
	
/**
 * Add an array of items to the popup (&lt;OPTION&gt;) at specified index.
 */
	public void addItem(String[] newItem, int index) {
		for (int i = newItem.length; i >= 0; i--) {
			if ( newItem[i] != null ) {
				this.options.put(newItem[i], newItem[i]);
				this.keys.add(index, newItem[i]);
			}
		}
	}
		
/**
 * Add an item to the popup with a label (&lt;OPTION&gt;) at specified index.
 */
	public void addItemWithLabel(String newValue, String newLabel, int index) {
		if ( newValue != null ) {
			this.options.put(newValue, newLabel);
			this.keys.add(index, newValue);
		}
	}
	
/**
 * Add an array of items to the popup with labels (&lt;OPTION&gt;) at specified index.
 */
	public void addItemWithLabel(String[] newItem, String[] newLabel, int index) {
		for (int i = newItem.length; i >= 0; i--) {
			if ( newItem[i] != null ) {
				this.options.put(newItem[i], newLabel[i]);
				this.keys.add(index, newItem[i]);
			}
		}
	}
	
	public void addGroup(PopupGroup aGroup) {
		if ( aGroup != null ) this.keys.add(aGroup);
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
		
		output.append("<SELECT " + this.attrString() + ">");
		for (Iterator<Object> key = this.keys.iterator(); key.hasNext();) {
			Object item = key.next();
			if ( item.getClass().isAssignableFrom(PopupGroup.class)) {
				((PopupGroup) item).setDefault(this.defValue);
				output.append(item.toString());
			} else {
				if ( item.equals(defValue) ) {
					output.append("<OPTION VALUE=\"" + item.toString() + "\" SELECTED>" + 
							this.options.get(item) + "</OPTION>");
				} else {
					output.append("<OPTION VALUE=\"" + item.toString() + "\">" + 
							this.options.get(item) + "</OPTION>");				
				}
			}
		}
		output.append("</SELECT>");
		return output.toString();
	}
}
