//
//  HTMLListObj.java
//  HTMLTools
//
//  Created by George Chlipala on 5/14/06.
//  Copyright 2006 Walnut Computer Services. All rights reserved.
//
package edu.uic.orjala.cyanos.web.html;

import java.lang.String;
import java.util.Iterator;
import java.util.ListIterator;

abstract class HTMLListObj extends HTMLObj {
/**
 *	Generates HTML for the object
 */
	public String toString() {
		StringBuffer output = new StringBuffer(); 
		StringBuffer attrs = new StringBuffer();
				
		for (Iterator<String> key = this.attributes.keySet().iterator(); key.hasNext(); ) {
			Object keyObj = key.next();
			attrs.append(keyObj.toString() + "=" + '"' + this.attributes.get(keyObj) + '"'+ " ");
		}
		
		for (ListIterator<Object> i = this.data.listIterator(); i.hasNext(); ) {
				Object item = i.next();
				if ( item == null ) {
					output.append("<" + element + " " + attrs.toString() + ">" + "</" + element + ">");						
				} else {
					output.append("<" + element + " " + attrs.toString() + ">" + item.toString() + 
						"</" + element + ">");
				}
		}
		return output.toString();
	}

}
