/**
 * 
 */
package edu.uic.orjala.cyanos.web.html;

/**
 * @author George Chlipala
 *
 */
public class Div extends HTMLObj {

	private static String ELEMENT_TAG = "DIV";
	/**
	 * 
	 */
	public Div() {
		this.element = ELEMENT_TAG;
	}

	public Div(Object[] newData) { 
		this();
		this.addItem(newData); 
	}
	
	public Div(Object newData) {	
		this();
		this.addItem(newData); 
	}
	
	public Div(String[] newAttrs, Object newData) { 
		this();
		this.addItem(newData); 
		this.setAttributes(newAttrs);
	}
	
	public Div(String[] newAttrs, Object[] newData) {
		this();
		this.addItem(newData); 
		this.setAttributes(newAttrs);
	}

	public void setID(String newID) {
		this.setAttribute("ID", newID);
	}
}
