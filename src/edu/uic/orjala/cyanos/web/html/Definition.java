//
//  Definition.java
//  HTMLTools
//
//  Created by George Chlipala on 5/13/06.
//  Copyright 2006 Walnut Computer Services. All rights reserved.
//
package edu.uic.orjala.cyanos.web.html;

import java.lang.String;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

/**
 *	Creates a HTML definition object (&lt;DT&gt; and &lt;DD&gt;)
 */

public class Definition extends HTMLObj {	
	
	private Map<Object,Object> defns = new HashMap<Object,Object>();

/**
 *	Constructs a HTML deinition object (&lt;DT&gt; and &lt;DD&gt;)
 */
	public Definition() { }
	
/**
 *	Constructs a HTML deinition object (&lt;DT&gt; and &lt;DD&gt;)
 */
	public Definition(Object[] newTerms, Object[] newDefns) { 
		this.addDefinition(newTerms, newDefns); 
	}
	
/**
 *	Constructs a HTML deinition object (&lt;DT&gt; and &lt;DD&gt;)
 */
	public Definition(Object newTerm, Object newDefn) {	
		this.addDefinition(newTerm, newDefn); 
	}
	
/**
 *	Constructs a HTML deinition object (&lt;DT&gt; and &lt;DD&gt;)
 */
	public Definition(Map<Object,Object> newData) { 
		this.defns = new HashMap<Object, Object>(newData); 
	}
	
/**
 *	Adds definition terms and related definitions.
 */
	public void addDefinition(Object[] newTerms, Object[] newDefns) { 
		for (int i = 0; i < newTerms.length; i++) {
			this.defns.put(newTerms[i], newDefns[i]);
		}
	}

/**
 *	Adds definition terms and related definitions.
 */
	public void addDefinition(Object newTerms, Object newDefns) { 
		this.defns.put(newTerms, newDefns);
	}
				
/**
 *	Generates HTML for the object
 */
	public String toString() {
		StringBuffer output = new StringBuffer(); 
				
		for (Iterator<Object> key = this.defns.keySet().iterator(); key.hasNext(); ) {
			Object keyObj = key.next();
			output.append("<DT>" + keyObj.toString() + "</DT><DD>" + this.defns.get(keyObj) + "</DD>");
		}
		return output.toString();
	}

}
