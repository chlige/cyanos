//
//  Anchor.java
//  HTMLTools
//
//  Created by George Chlipala on 5/13/06.
//  Copyright 2006 Walnut Computer Services. All rights reserved.
//
package edu.uic.orjala.cyanos.web.html;

import java.lang.String;

/**
 *	Creates a HTML anchor object (&lt;A&gt;)
 */
public class Anchor extends HTMLObj {	
/**
 *	Constructs a HTML anchor object (&lt;A&gt;)
 */
	public Anchor() { this.element = new String("A"); }
	
/**
 *	Constructs a HTML anchor object (&lt;A&gt;)
 */
	public Anchor(Object[] newData) { 
		this.element = new String("A");
		this.addItem(newData); 
	}
	
/**
 *	Constructs a HTML anchor object (&lt;A&gt;)
 */
	public Anchor(Object newData) {	
		this.element = new String("A");
		this.addItem(newData); 
	}
	
/**
 *	Constructs a HTML anchor object (&lt;A&gt;)
 */
	public Anchor(String[] newAttrs, Object newData) { 
		this.element = new String("A");
		this.addItem(newData); 
		this.setAttributes(newAttrs);
	}
	
/**
 *	Constructs a HTML anchor object (&lt;A&gt;)
 */
	public Anchor(String[] newAttrs, Object[] newData) {
		this.element = new String("A");
		this.addItem(newData);
		this.setAttributes(newAttrs);
	}
	
/**
 *	Sets HREF element
 */
	public void setLink(String newHref) {
		this.setHref(newHref);
	}
	
/**
 *	Sets HREF element
 */
	public void setHref(String newHref) {
		this.attributes.put("HREF", newHref);
	}
	
/**
 *	Sets NAME element
 */
	public void setName(String newName) {
		this.attributes.put("NAME", newName);
	}
	
/**
 *	Generates &lt;A HREF="<code>href</code>"&gt;<code>text</code>&lt;A&gt;
 */
	public String quickLink(String href, String text) {
		this.attributes.remove("HREF");
		return "<A HREF=\"" + href + "\"" + this.attrString() + ">" + text + "</A>";
	}
}
