//
//  Form.java
//  HTMLTools
//
//  Created by George Chlipala on 5/13/06.
//  Copyright 2006 Walnut Computer Services. All rights reserved.
//
package edu.uic.orjala.cyanos.web.html;

import java.lang.String;

/**
 *	Object to generate a HTML form object &lt;FORM&gt;
 */
public class Form extends HTMLObj {	
/**
 *	Constructs a HTML form object &lt;FORM&gt;
 */
	public Form() { this.element = new String("FORM"); }
	
/**
 *	Constructs a HTML form object &lt;FORM&gt;
 *
 * @param newData an array of objects to add to the form.
 */
	public Form(Object[] newData) { 
		this();
		this.addItem(newData); 
	}
	
/**
 *	Constructs a HTML form object &lt;FORM&gt;
 *
 * @param newData an object to add to the new form.
 */
	public Form(Object newData) {	
		this();
		this.addItem(newData); 
	}
	
/**
 *	Constructs a HTML form object &lt;FORM&gt;
 */
	public Form(String[] newAttrs, Object newData) { 
		this();
		this.addItem(newData); 
		this.setAttributes(newAttrs);
	}
	
/**
 *	Constructs a HTML form object &lt;FORM&gt;
 */
	public Form(String[] newAttrs, Object[] newData) {
		this();
		this.addItem(newData); 
		this.setAttributes(newAttrs);
	}

	/**
	 * Sets NAME element
	 * 
	 * @param newName Form name
	 */
	public void setName(String newName) {
		this.attributes.put("name", newName);
	}
	
/**
 *	Adds a submit button to the form with a label of <code>value</code>.
 *
 * @param value Button label
 */
	public void addSubmit(String value) {
		this.addItem(String.format("<BUTTON TYPE='SUBMIT'>%s</BUTTON>", value));
	}
	
	/**
	 * Adds a submit button to the form.
	 * 
	 * @param name Field name
	 * @param value Field value
	 * @param label Button label
	 */
	public void addSubmit(String name, String value, String label) {
		this.addItem(String.format("<BUTTON TYPE='SUBMIT' NAME=\"%s\" VALUE=\"%s\">%s</BUTTON>", name, value, label));
	}
	
/**
 *	Adds a reset button to the form.
 */
	public void addReset() {
		this.addItem("<BUTTON TYPE='Reset'>Reset</BUTTON>");
	}
	
	/**
	 * Adds a hidden value to the form.
	 * 
	 * @param name Field name
	 * @param value Field value
	 */
	public void addHiddenValue(String name, String value) {
		Input hiddenValue = new Input("hidden");
		hiddenValue.setName(name);
		hiddenValue.setValue(value);
		this.addItem(hiddenValue);
	}
	
	/**
	 * Set the form method to GET.
	 */
	public void setGet() {
		this.setAttribute("METHOD", "GET");
	}
	
	/**
	 * Set the form method to POST.
	 */
	public void setPost() {
		this.setAttribute("METHOD", "POST");
	}
	
	/**
	 * Setup the form to allow file uploads.
	 */
	public void setUploadForm() {
		this.setAttribute("METHOD", "POST");
		this.setAttribute("ENCTYPE", "multipart/form-data");
	}
}
