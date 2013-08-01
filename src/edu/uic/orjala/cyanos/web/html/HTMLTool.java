//
//  HTMLTool.java
//  HTMLTools
//
//  Created by George Chlipala on 5/13/06.
//  Copyright 2006 Walnut Computer Services. All rights reserved.
//
package edu.uic.orjala.cyanos.web.html;

import java.lang.String;

public class HTMLTool {	
		
	public HTMLTool() { super(); }
	
	public String StartElement(String element, String[] attrs) {
		StringBuffer attrString = new StringBuffer();				
		int i,j;
		for ( i = 0; i < attrs.length; i = i + 2) {
			j = i + 1;
			attrString.append(attrs[i].toString() + "=" + '"' + attrs[j].toString() + '"'+ " ");
		}
		return "<" + element + " " + attrString.toString() + ">";
	}
		
	public String EndElement(String element) {
		return "</" + element + ">";
	}
	
	public String StartForm(String[] attrs) {
		return this.StartElement("FORM", attrs);
	}
	
	public String EndForm() {
		return this.EndElement("FORM");
	}

	public String StartBody(String[] attrs) {
		return this.StartElement("BODY", attrs);
	}
	
	public String EndBody() {
		return this.EndElement("BODY");
	}

	public String StartHTML(String[] attrs) {
		return this.StartElement("BODY", attrs);
	}
	
	public String EndHTML() {
		return this.EndElement("BODY");
	}
	
}
