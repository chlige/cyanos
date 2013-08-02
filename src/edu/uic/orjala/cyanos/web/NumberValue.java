/**
 * 
 */
package edu.uic.orjala.cyanos.web;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author George Chlipala
 *
 */
public class NumberValue implements SheetValue {

	private Number value;
	private int denom = 1;
	
	/**
	 * 
	 */
	public NumberValue(float value) {
		this.value = new BigDecimal(value);
	}
	
	public NumberValue(double value) {
		this.value = new BigDecimal(value);
	}
	
	public NumberValue(int value) {
		this.value = new Integer(value);
	}
	
	public NumberValue(long value) {
		this.value = new Long(value);
	}
	
	public NumberValue(Number value) {
		this.value = value;
	}

	public NumberValue(String value) {
		this.value = new BigDecimal(value);
	}
	
	public void setPercentage() {
		denom = 100;
	}
	
	public void setPPT() {
		denom = 1000;
	}
	
	public void setPPM() {
		denom = 1000000;
	}
	
	public void setNumber() {
		denom = 1;
	}
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.SheetValue#isNumber()
	 */
	
	public boolean isNumber() {
		return true;
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
		return value;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.SheetValue#getDate()
	 */
	
	public Date getDate() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public String toString() {
		switch ( denom ) {
		case 100:
			return this.asPercent();
		case 1000: 
			return this.asPPT();
		case 1000000:
			return this.asPPM();
		}
		return value.toString();
	}
	
	public String asPercent() {
		if ( value instanceof BigDecimal ) {
			return String.format("%s %%", ((BigDecimal) value).movePointRight(2).toString());
		} else if ( value instanceof Integer ) {
			return String.format("%d %%", value.intValue() * 100);
		} else if ( value instanceof Long ) { 
			return String.format("%d %%", value.longValue() * 100);
		}
		return value.toString();
	}
	
	public String asPPT() {
		if ( value instanceof BigDecimal ) {
			return String.format("%s ppt", ((BigDecimal) value).movePointRight(3).toString());
		} else if ( value instanceof Integer ) {
			return String.format("%d ppt", value.intValue() * 1000);
		} else if ( value instanceof Long ) { 
			return String.format("%d ppt", value.longValue() * 1000);
		}
		return value.toString();	
	}
	
	public String asPPM() {
		if ( value instanceof BigDecimal ) {
			return String.format("%s ppm", ((BigDecimal) value).movePointRight(6).toString());
		} else if ( value instanceof Integer ) {
			return String.format("%d ppm", value.intValue() * 1000000);
		} else if ( value instanceof Long ) { 
			return String.format("%d ppm", value.longValue() * 1000000);
		}			
		return value.toString();			
	}

	public String asPPB() {
		if ( value instanceof BigDecimal ) {
			return String.format("%s ppb", ((BigDecimal) value).movePointRight(9).toString());
		} else if ( value instanceof Integer ) {
			return String.format("%d ppb", value.intValue() * 1000000000);
		} else if ( value instanceof Long ) { 
			return String.format("%d ppb", value.longValue() * 1000000000);
		}			
		return value.toString();			
	}
}
