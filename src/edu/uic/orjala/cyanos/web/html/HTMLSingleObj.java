//
//  HTMLSingleObj.java
//  HTMLTools
//
//  Created by George Chlipala on 5/14/06.
//  Copyright 2006 Walnut Computer Services. All rights reserved.
//
package edu.uic.orjala.cyanos.web.html;

import java.lang.String;

abstract class HTMLSingleObj extends HTMLObj {
	
/**
 *	Generates HTML for the object
 */
	public String toString() {
		return "<" + element + this.attrString() + " />";
	}

}
