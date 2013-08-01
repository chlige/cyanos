//
//  DefinitionList.java.java
//  HTMLTools
//
//  Created by George Chlipala on 5/13/06.
//  Copyright 2006 Walnut Computer Services. All rights reserved.
//
package edu.uic.orjala.cyanos.web.html;

import java.lang.String;

/**
 *	Creates a HTML definition list (&lt;DL&gt;)
 */
public class DefinitionList extends HTMLObj {	
/**
 *	Constructs a HTML deinition object (&lt;DL&gt;)
 */
	public DefinitionList() { this.element = new String("DL"); }
	
/**
 *	Constructs a HTML deinition object (&lt;DL&gt;)
 */
	public DefinitionList(Object[] newData) { 
		this();
		this.addItem(newData); 
	}
	
/**
 *	Constructs a HTML deinition object (&lt;DL&gt;)
 */
	public DefinitionList(Object newData) {	
		this();
		this.addItem(newData); 
	}
	
/**
 *	Constructs a HTML deinition object (&lt;DL&gt;)
 */
	public DefinitionList(String[] newAttrs, Object newData) { 
		this();
		this.addItem(newData); 
		this.setAttributes(newAttrs);
	}
	
/**
 *	Constructs a HTML deinition object (&lt;DL&gt;)
 */
	public DefinitionList(String[] newAttrs, Object[] newData) {
		this();
		this.addItem(newData); 
		this.setAttributes(newAttrs);
	}
}
