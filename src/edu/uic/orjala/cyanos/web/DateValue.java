/**
 * 
 */
package edu.uic.orjala.cyanos.web;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

/**
 * @author George Chlipala
 *
 */
public class DateValue implements SheetValue {

	private Date value;
	private boolean date = true;
	private boolean time = true;

	public DateValue(String value) throws ParseException {
		this.value = DateFormat.getDateInstance().parse(value);
	}

	public DateValue(Date value) {
		this.value = value;
	}
	
	public void setDate() {
		this.date = true;
		this.time = false;
	}
	
	public void setTime() {
		this.time = true;
		this.date = false;
	}
	
	public void setDateTime() {
		this.date = true;
		this.time = true;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.SheetValue#isNumber()
	 */
	public boolean isNumber() {
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.SheetValue#isDate()
	 */
	public boolean isDate() {
		return true;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.SheetValue#getNumber()
	 */
	public Number getNumber() {
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.SheetValue#getDate()
	 */
	public Date getDate() {
		return value;
	}

	public String toString() {
		if ( date && time ) {
			return DateFormat.getDateTimeInstance().format(value);
		} else if ( date ) {
			return DateFormat.getDateInstance().format(value);
		} else if ( time ) {
			return DateFormat.getTimeInstance().format(value);
		} 
		return "";
	}
}
