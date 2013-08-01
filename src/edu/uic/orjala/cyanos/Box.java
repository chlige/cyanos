/**
 * 
 */
package edu.uic.orjala.cyanos;

/**
 * Generic interface for objects that are a box, e.g. {@link SampleCollection}, {@link AssayData}, {@link CryoCollection}.
 * The width of a box corresponds to the total number of columns and the length is the total number of rows.
 * 
 * @author George Chlipala
 *
 */
public interface Box extends BasicObject {

	/**
	 * Length, i.e. Y dimension, of the object.  This value is the total number of rows.
	 * 
	 * @return Number of possible Y values.
	 * @throws DataException
	 */
	int getLength() throws DataException;
	
	/**
	 * Width, i.e. X dimension, of the object.  This value is the total number of columns.
	 * 
	 * @return Number of possible X values
	 * @throws DataException
	 */
	int getWidth() throws DataException;
	
	/**
	 * Move internal cursor to next column, i.e. x + 1.
	 * 
	 * @return true if the location is within the bounds of the box.
	 * @throws DataException
	 */
	boolean nextColumn() throws DataException;

	/**
	 * Move internal cursor to next row, i.e. y + 1.
	 * 
	 * @return true if the location is within the bounds of the box.
	 * @throws DataException
	 */
	boolean nextRow() throws DataException;

	/**
	 * Move internal cursor to previous column, i.e. x - 1.
	 * 
	 * @return true if the location is within the bounds of the box.
	 * @throws DataException
	 */
	boolean previousColumn() throws DataException;

	/**
	 * Move internal cursor to previous row, i.e. y - 1.
	 * 
	 * @return true if the location is within the bounds of the box.
	 * @throws DataException
	 */
	boolean previousRow() throws DataException;

	/**
	 * Move the internal cursor to the first row, i.e. y = 1.
	 * 
	 * @throws DataException
	 */
	void firstRow() throws DataException;

	/**
	 * Move the internal cursor to the first column, i.e. x = 1.	
	 * 
	 * @throws DataException
	 */
	void firstColumn() throws DataException;
	
	/**
	 * Move the internal cursor to before the first row, i.e. y = 0.
	 * 
	 * @throws DataException
	 */
	void beforeFirstRow() throws DataException;

	/**
	 * Move the internal cursor to before the first column, i.e. x = 0.
	 * 
	 * @throws DataException
	 */
	void beforeFirstColumn() throws DataException;
	
	/**
	 * Move the internal cursor the next possible location by moving to the next row, i.e. y + 1.  
	 * If the current location is at the end of a column, the the cursor will move to the first row of the next column, i.e. x + 1, y = 1.
	 * 
	 * @return true if the next location exists.
	 * @throws DataException
	 */
	boolean nextLocationByRow() throws DataException;
	
	/**
	 * Move the internal cursor the next possible location by moving the next column, i.e. x + 1.  
	 * If the current location is at the end of a row, the the cursor will move to the first column of the next row, i.e. x = 1, y + 1.
	 * 
	 * @return true if the next location exists.
	 * @throws DataException
	 */
	boolean nextLocationByColumn() throws DataException;
	
	/**
	 * Return the letter of the current row, i.e. A = 1, B = 2, etc.
	 * 
	 * @return Letter of the current row.
	 */
	String currentRowAlpha();
	
	/**
	 * Return the letter of the current column, i.e. A = 1, B = 2, etc.
	 * 
	 * @return Letter of the current column.
	 */
	String currentColumnAlpha();
	
	/**
	 * Return the current row. i.e. value of y.
	 * 
	 * @return Current row number.
	 */
	int currentRowIndex();

	/**
	 * Return the current column, i.e. value of x.
	 * 
	 * @return Current column number.
	 */
	int currentColumnIndex();
	
	/**
	 * Return the current location as row letter and column number, e.g. A3 = (3, 1).
	 * 
	 * @return Current location as a string.
	 */
	String currentLocation();
	
	/**
	 * Determine if the internal cursor is within the bounds of the box.
	 * 
	 * @return true if the cursor is within the box.
	 */
	boolean inBox();
	
	/**
	 * Determine if the current object is properly defined as a box, i.e. has a length and width.
	 * 
	 * @return true if the object is a box.
	 */
	boolean isBox();
	
	/**
	 * Determine if the current object is defined as a list, i.e. only has a length.
	 * 
	 * @return true if the object is a list.
	 */
	boolean isList();
	
	/**
	 * Move the internal cursor to the specified location. 
	 * 
	 * @param row Row number, i.e. y value.
	 * @param col Column number, i.e. x value.
	 * @return true if the location is within the bounds of the box.
	 * @throws DataException
	 */
	boolean gotoLocation(int row, int col) throws DataException;
	
	/**
	 * Move the internal cursor to the specified location. Parse the string value, e.g. A3 = (3,1)
	 * 
	 * @param aLoc Location as a String.
	 * @return true if the location is within the bounds of the box.
	 * @throws DataException
	 */
	boolean gotoLocation(String aLoc) throws DataException;
	
}
