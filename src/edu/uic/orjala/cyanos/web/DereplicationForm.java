/**
 * 
 */
package edu.uic.orjala.cyanos.web;

import edu.uic.orjala.cyanos.Compound;
import edu.uic.orjala.cyanos.DataException;

/**
 * Interface for Dereplication Search Engines.  
 * To develop a new search engine create an object that implements this interface.
 * 
 * @author George Chlipala
 *
 */
public interface DereplicationForm {

	/**
	 * Get the search form for this search engine.
	 * 
	 * @return the HTML form as a String.
	 */
	public String form();

	/**
	 * Return compounds that this engine matches.
	 * 
	 * NOT YET IMPLEMENTED AT THE SERVLET LEVEL
	 * 
	 * @return compounds
	 * @throws DataException
	 */
	public Compound compounds() throws DataException;

	/**
	 * Get the SQL where string to find compounds.
	 * <BR/>For example, to produce the following SQL string.  {@code "SELECT compound.* FROM compound WHERE compound.name LIKE 'Mine%'"}
	 * <BR/>This method should return the following String.  {@code "compound.name LIKE 'Mine%'" }<BR/><BR/>
	 * <B>NOTE</B>: The Dereplication servlet will combine where strings using the AND operation.
	 * 
	 * @param tableAlias SQL alias of the table provided by DereplicationServlet.  Typically "".
	 * @return where statement as a String
	 */
	public String sqlWhere(String tableAlias);
	
	/**
	 * ID of the form. 
	 * 
	 * @return Short ID of the form, as a String.
	 */
	public String formID();
	
	/**
	 * Title of the search engine.
	 * 
	 * @return Title of the form as a String
	 */
	public String formTitle();
}
