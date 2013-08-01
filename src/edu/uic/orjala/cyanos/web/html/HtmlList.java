//
//  HtmlList.java
//  HTMLTools
//
//  Created by George Chlipala on 5/13/06.
//  Copyright 2006 Walnut Computer Services. All rights reserved.
//
package edu.uic.orjala.cyanos.web.html;

import java.lang.String;
import java.util.Iterator;
import java.util.ListIterator;

/**
 *	Class to generate a list in HTML (&lt;OL&gt; or &lt;UL&gt;)
 */
public class HtmlList extends HTMLListObj {	
/**
 *	Constructor
 */
	public HtmlList() { this.element = new String("OL"); }
	
/**
 *	Constructor
 */
	public HtmlList(Object[] newData) { 
		this();
		this.addItem(newData); 
	}
	
/**
 *	Constructor
 */
	public HtmlList(Object newData) {	
		this();
		this.addItem(newData); 
	}
	
/**
 *	Constructor
 */
	public HtmlList(String[] newAttrs, Object newData) { 
		this();
		this.addItem(newData); 
		this.setAttributes(newAttrs);
	}
	
/**
 *	Constructor
 */
	public HtmlList(String[] newAttrs, Object[] newData) {
		this();
		this.addItem(newData); 
		this.setAttributes(newAttrs);
	}
	
/**
 *	Set list as ordered (&lt;OL&gt;)
 */
	public void ordered() {
		this.element = new String("OL");
	}

/**
 *	Set list as unordered (&lt;UL&gt;)
 */
	public void unordered() {
		this.element = new String("UL");
	}

/**
 *	Generate HTML
 */
	public String toString() {
		StringBuffer output = new StringBuffer(); 
		StringBuffer attrs = new StringBuffer();
				
		for (Iterator<String> key = this.attributes.keySet().iterator(); key.hasNext(); ) {
			Object keyObj = key.next();
			attrs.append(keyObj.toString() + "=" + '"' + this.attributes.get(keyObj) + '"'+ " ");
		}
		output.append("<" + element + " " + attrs + ">");
		
		for (ListIterator<Object> i = this.data.listIterator(); i.hasNext(); ) {
				Object item = i.next();
				output.append("<LI>" + item.toString() + "</LI>");
		}
		output.append("</" + element + ">");
		return output.toString();
	}
}
