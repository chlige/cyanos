/**
 * 
 */
package edu.uic.orjala.cyanos.web;

import java.util.Date;

/**
 * @author gchlip2
 *
 */
public class StringValue implements SheetValue {

	private String value;
	
	
	public StringValue(String value) {
		this.value = value;
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.SheetValue#isNumber()
	 */
	
	public boolean isNumber() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.SheetValue#isDate()
	 */
	
	public boolean isDate() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.SheetValue#getNumber()
	 */
	
	public Number getNumber() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.SheetValue#getDate()
	 */
	
	public Date getDate() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public String toString() {
		return value;
	}

}
