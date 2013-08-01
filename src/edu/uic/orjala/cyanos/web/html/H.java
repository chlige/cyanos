//
//  H.java
//  HTMLTools
//
//  Created by George Chlipala on 5/13/06.
//  Copyright 2006 Walnut Computer Services. All rights reserved.
//
package edu.uic.orjala.cyanos.web.html;

import java.lang.String;

/**
 *	Class to generate a HTML header text objects, e.g. &lt;H1&gt;
 */
public class H extends HTMLObj {	
/**
 *	Constructor for HTML header text objects, e.g. &lt;H1&gt;
 */
	public H() { this.element = new String("H1"); }
	
/**
 *	Constructor for HTML header text objects, e.g. &lt;H1&gt;
 */
	public H(Object[] newData) {
		this.setLevel(1);
		this.addItem(newData); 
	}

/**
 *	Constructor for HTML header text objects, e.g. &lt;H1&gt;
 */
	public H(Object newData) {
		this.setLevel(1);
		this.addItem(newData); 
	}

/**
 *	Constructor for HTML header text objects, e.g. &lt;H1&gt;
 */
	public H(int newLevel, Object newData) { 
		this.setLevel(newLevel);
		this.addItem(newData); 
	}
	
/**
 *	Constructor for HTML header text objects, e.g. &lt;H1&gt;
 */
	public H(int newLevel, Object[] newData) {
		this.setLevel(newLevel);
		this.addItem(newData); 
	}

/**
 *	Set header level (1-5).
 */
	public void setLevel(int newLevel) {
		this.element = new String("H" + newLevel);
	}
}
