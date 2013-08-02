/**
 * 
 */
package edu.uic.orjala.cyanos.web;

import java.util.Date;

/**
 * @author George Chlipala
 *
 */
public interface SheetValue {

	boolean isNumber();
	
	boolean isDate();
	
	Number getNumber();
	
	Date getDate();

}
