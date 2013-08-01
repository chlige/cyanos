/**
 * 
 */
package edu.uic.orjala.cyanos.sql;

import edu.uic.orjala.cyanos.Box;
import edu.uic.orjala.cyanos.DataException;

/**
 * Abstract class to contain common code to handle Box objects.
 * 
 * @author George Chlipala
 *
 */
public abstract class SQLBoxObject extends SQLObject implements Box {
	// Setup counters for row/colum iteration.
	protected int currRow = 0;
	protected int currCol = 0;
	// Setup rows to be alphabetic and columns to be numeric.
	protected boolean rowAlpha = true;
	protected boolean colAlpha = false;
	protected int maxLength = 0;
	protected int maxWidth = 0;
	
	protected String lengthCol = null;
	protected String widthCol = null;
		
	public SQLBoxObject(SQLData data) {
		super(data);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.sql.SQLObject#next()
	 */
	
	public boolean next() throws DataException {
		if ( super.next() ) {
			this.setSize();
			return true;
		}
		return false;
	}
	
	protected void setSize() throws DataException {
		if ( this.lengthCol != null )
			this.maxLength = this.myData.getInt(lengthCol);
		if ( this.widthCol != null ) 
			this.maxWidth = this.myData.getInt(widthCol);		
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.sql.SQLObject#previous()
	 */
	
	public boolean previous() throws DataException {
		if ( super.previous() ) {
			this.setSize();
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.sql.SQLObject#first()
	 */
	
	public boolean first() throws DataException {
		if ( super.first() ) {
			this.setSize();
			return true;
		} 
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.sql.SQLObject#last()
	 */
	
	public boolean last() throws DataException {
		if ( super.last() ) {
			this.setSize();
			return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.sql.SQLObject#gotoRow(int)
	 */
	
	public boolean gotoRow(int row) throws DataException {
		if ( super.gotoRow(row) ) {
			this.setSize();
			return true;
		}
		return false;
	}
	
	protected void fetchRecord(String sqlString, String lengthCol, String widthCol) throws DataException {
		this.lengthCol = lengthCol;
		this.widthCol = widthCol;
		this.fetchRecord(sqlString);
		if ( this.myData.first() ) {
			this.setSize();
		}
	}

	public boolean nextColumn() throws DataException {
		if (this.currCol >= maxWidth ) this.currCol = maxWidth;
		else { this.currCol++; return true; }
		return false;
	}

	public boolean nextRow() throws DataException {
		if (this.currRow >= maxLength) this.currRow = maxLength;
		else { this.currRow++; return true; }
		return false;
	}

	public boolean previousColumn() throws DataException  {
		if (this.currCol <= 1) this.currCol = 1;
		else { this.currCol--; return true; }
		return false;
	}

	public boolean previousRow() throws DataException  {
		if (this.currRow <= 1) this.currRow = 1;
		else { this.currRow--; return true; }
		return false;
	}

	public void firstRow() throws DataException  {
		this.currRow = 1;
	}

	public void firstColumn() throws DataException {
		this.currCol = 1;
	}
	
	public void beforeFirstRow() throws DataException {
		this.currRow = 0;

	}

	public void beforeFirstColumn() throws DataException {
		this.currCol = 0;
	}
	
	public boolean nextLocationByRow() throws DataException {
		if ( this.nextRow() ) return true;
		this.firstRow();
		return this.nextColumn();
	}
	
	public boolean nextLocationByColumn() throws DataException {
		if ( this.nextColumn() ) return true;
		this.firstColumn();
		return this.nextRow();
	}
	
	public String currentRowAlpha() {
		return ALPHABET[currRow];
	}
	
	public String currentColumnAlpha() {
		return ALPHABET[currCol];
	}
	
	public int currentRowIndex() {
		return this.currRow;
	}

	public int currentColumnIndex() {
		return this.currCol;
	}
	
	public String currentLocation() {
		return this.stringLocation(this.currRow, this.currCol);
	}
	
	public boolean inBox() {
		return ( this.currCol > 0 && this.currRow > 0 );
	}
	
	public boolean isBox() {
		return ( this.maxLength > 0 && this.maxWidth > 0 );
	}
	
	public boolean isList() {
		return (! this.isBox() );
	}
	
	protected void setSize(int length, int width) {
		this.maxLength = length;
		this.maxWidth = width;
	}
		
	protected String stringLocation(int row, int col) {
		String rowS = ( this.rowAlpha ? ALPHABET[row] : String.valueOf(row) );
		String colS = ( this.colAlpha ? ALPHABET[col] : String.valueOf(col) );
		return rowS.concat(colS);
	}
	
	public boolean gotoLocation(int row, int col) throws DataException {
		if ( row <= this.maxLength && col <= this.maxWidth && row > 0 && col > 0) { 
			this.currRow = row;
			this.currCol = col;
			return true;
		}
		return false;
	}
	
	public boolean gotoLocation(String aLoc) throws DataException {
		int[] vals = this.parseLocation(aLoc);
		return this.gotoLocation(vals[0], vals[1]);
	}
	
}
