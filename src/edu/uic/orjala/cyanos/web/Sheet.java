package edu.uic.orjala.cyanos.web;

import java.util.Arrays;

/**
 * Class for worksheets.  CYANOS servlet will create Sheet objects for uploaded worksheets.
 * 
 * @author George Chlipala
 *
 */
public class Sheet {

	private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
	
	// Matrix is indexed row, col
	private transient SheetValue[][] matrix;
	private int colSize = 0;
	private int rowSize = 0;
	
//	protected List<List<String>> myData;
	protected String sheetName;
	
	protected int increment = 10;
	
	protected int currRow = 0;
	protected int currCol = 0;
	
	public Sheet(int cols, int rows) {
		if ( cols < 0 )
			throw new IllegalArgumentException("Illegal Number of Columns: " + cols);
		if ( rows < 0 ) 
			throw new IllegalArgumentException("Illegal Number of Rows: " + rows);
		
		matrix = new SheetValue[rows][cols];
		this.colSize = cols;
		this.rowSize = rows;
	}
	
	public Sheet(int cols, int rows, String name) {
		this(cols,rows);
		this.sheetName = new String(name);
	}
	
	public Sheet() {
		this(10,10);
	}
	
	public Sheet(String aName) {
		this(10, 10, aName);
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.Sheet#setName(java.lang.String)
	 */
	public void setName(String name) {
		this.sheetName = new String(name);
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.Sheet#getName()
	 */
	public String getName() {
		return this.sheetName;
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.Sheet#rowCount()
	 */
	public int rowCount() {
		return rowSize;
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.Sheet#columnCount()
	 */
	public int columnCount() {
		return this.colSize;
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.Sheet#rowSize()
	 */
	public int rowSize() {
		return this.columnCount();
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.Sheet#setRowSize(int)
	 */
	public void setRowSize(int size) {
		if ( size > this.colSize )
			this.growRowSize(size);
		else if ( size < this.colSize ) {
			this.trimRowSize(size);
		}
	}
	
	private void trimRowSize(int size) {
		for ( int r = 0; r < matrix.length; r++ ) {
			SheetValue[] newRow = new SheetValue[size];
			for (int i = 0; i < size; i++ ) {
				newRow[i] = matrix[r][i];
			}
			matrix[r] = newRow;
		}
		this.colSize = size;		
	}
	
	private void growRowSize(int size) {
		if ( size - MAX_ARRAY_SIZE > 0 ) {
			size = hugeSize(size);
		}
		for ( int r = 0; r < matrix.length; r++ ) {
			matrix[r] = Arrays.copyOf(matrix[r], size);
		}
		this.colSize = size;
	}
	
	public void setRowCount(int count) {
		if ( count > this.rowSize )
			this.growRows(count);
		else if ( count < this.rowSize ) 
			this.trimRows(count);
	}
	
	private int hugeSize(int minSize) {
		if ( minSize < 0 )
			throw new OutOfMemoryError();
		return (minSize > MAX_ARRAY_SIZE) ? Integer.MAX_VALUE : MAX_ARRAY_SIZE;
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.Sheet#getValue(int, int)
	 */
	public SheetValue getValue(int row, int column) {
		if ( row > -1 && row < this.rowSize ) {
			if ( column > -1 && column < matrix[row].length ) {
				return matrix[row][column];
			}
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.Sheet#getValue(int)
	 */
	public SheetValue getValue(int column) {
		return this.getValue(this.currRow, column);
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.Sheet#getValue()
	 */
	public SheetValue getValue() {
		return this.getValue(this.currRow, this.currCol);
	}
	
	public String getStringValue() {
		return this.getStringValue(this.currCol);
	}

	public String getStringValue(int column) {
		return this.getStringValue(this.currRow, column);
	}
	
	public String getStringValue(int row, int column) {
		SheetValue value = this.getValue(row, column);
		if ( value != null )
			return value.toString();
		else return "";
	}
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.Sheet#beforeFirstRow()
	 */
	public void beforeFirstRow() {
		this.currRow = -1;
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.Sheet#beforeFirstColumn()
	 */
	public void beforeFirstColumn() {
		this.currCol = -1;
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.Sheet#beforeFirstCell()
	 */
	public void beforeFirstCell() {
		this.currCol = -1;
		this.currRow = -1;
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.Sheet#firstRow()
	 */
	public boolean firstRow() {
		if ( this.rowSize > 0 ) {
			this.currRow = 0;
			return true;
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.Sheet#firstColumn()
	 */
	public boolean firstColumn() {
		if ( this.colSize > 0 ) {
			this.currCol = 0;
			return true;
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.Sheet#firstCell()
	 */
	public boolean firstCell() {
		return this.firstColumn() && this.firstRow();
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.Sheet#nextRow()
	 */
	public boolean nextRow() {
		this.currRow++;
		if ( this.currRow < this.rowSize ) {
			this.currCol = 0;
			return true;
		}
		this.currRow--;
		return false;
	}
	
	public boolean nextRow(boolean add) {
		if ( this.nextRow() ) {
			return true;
		}
		if ( add ) { 
			this.growRows(currRow + 1);
			return this.nextRow(); 
		} else 
			return false;
	}
	 
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.Sheet#nextColumn()
	 */
	public boolean nextColumn() {
		this.currCol++;
		if ( this.currCol < this.colSize ) {
			return true;
		}
		this.currCol--;
		return false;
	}
	
	public boolean nextColumn(boolean add) {
		if ( this.nextColumn() ) {
			return true;
		}
		if ( add ) {
			this.setRowSize(colSize + 1);
			return nextColumn(add);
		} else
			return false;
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.Sheet#nextCellInRow()
	 */
	public boolean nextCellInRow() {
		if ( this.currRow > -1 && this.currRow < this.rowSize ) {
			this.currCol++;
			if ( this.currCol < this.colSize ) {
				return true;
			}
			this.currCol--;
		}
		return false;

	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.Sheet#gotoRow(int)
	 */
	public boolean gotoRow(int index) {
		if ( index < matrix.length ) {
			this.currRow = index;
			return true;
		}
		return false;
	}
	
	public boolean gotoRow(int index, boolean grow) {
		if ( gotoRow(index) ) {
			return true;
		}
		if ( grow ) {
			this.growRows(index + 1);
			return gotoRow(index);
		}
		return false;
	}
	
	public boolean gotoCell(int index) {
		if ( index < matrix[this.currRow].length ) {
			this.currCol = index;
			return true;
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.Sheet#addRow()
	 */
/*
	private void addRow() {
		int rows = matrix.length + 1;
		this.growRows(rows);
		matrix[this.rowSize - 1] = new SheetValue[this.colSize];	
		this.currRow = this.rowSize - 1;
	}
*/
	private void trimRows(int size) {
		SheetValue[][] newMatrix = new SheetValue[size][];
		for ( int i = 0; i < size; i++ ) {
			newMatrix[i] = matrix[i];
		}
		matrix = newMatrix;
		this.rowSize = size;
	}
	
	private void growRows(int size) {
		int oldSize = matrix.length;
		if ( size > MAX_ARRAY_SIZE  ) {
			size = hugeSize(size);
		}
		matrix = Arrays.copyOf(matrix, size);
		for (int r = oldSize; r < matrix.length; r++) {
			matrix[r] = new SheetValue[this.colSize];
		}
		this.rowSize = size;
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.Sheet#addRow(int)
	 */
/*
	private void addRow(int index) {		
		if ( index == this.rowSize ) {
			this.addRow();
		} else {
			int rows = matrix.length + 1;
			this.growRows(rows);
			for ( int r = this.rowSize - 2; r >= index; r-- ) {
				matrix[r+1] = matrix[r];
			}
			matrix[index] = new SheetValue[this.colSize];
		} 
		this.currRow = index;
	}
*/
	public void insertRow(int index) {
		if ( index >= this.rowSize ) {
			this.gotoRow(index, true);
		} else {
			int rows = matrix.length + 1;
			this.growRows(rows);
			for ( int r = this.rowSize - 2; r >= index; r-- ) {
				matrix[r+1] = matrix[r];
			}
			matrix[index] = new SheetValue[this.colSize];
		} 
		this.currRow = index;		
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.Sheet#addCell(java.lang.String)
	 */
	public void addCell(SheetValue value) {
		this.setValue(value);
		this.nextColumn(true);
	}

	public void addCell(String value) {
		this.addCell(new StringValue(value));
	}

	public void addCell(int index, String value) {
		this.addCell(index, new StringValue(value));
	}
	
	public void addCell(int index, SheetValue value) {
		this.setRowSize((index < rowSize ? rowSize + 1 : index + 1)); 
		for ( int c = (matrix[this.currRow].length - 2); c >= index; c-- ) {
			matrix[this.currRow][c+1] = matrix[this.currRow][c];
		}
		matrix[this.currRow][index] = value;
	}
	
	public void appendCell(String value) {
		this.appendCell(new StringValue(value));
	}
	
	public void appendCell(int cellIndex, String value) {
		this.appendCell(cellIndex, new StringValue(value));
	}
	
	public void appendCell(SheetValue value) {
		this.setValue(value);
		this.nextColumn();
	}
	public void appendCell(int cellIndex, SheetValue value) {
		this.setValue(cellIndex, value);
		this.nextColumn();
	}
		
	public void setValue(String value) {
		this.setValue(new StringValue(value));
	}
	
	public void setValue(SheetValue value) {
		this.matrix[this.currRow][this.currCol] = value;
	}
	
	public void setValue(int cellIndex, SheetValue value) {
		if ( this.matrix[this.currRow].length > cellIndex ) 
			this.matrix[this.currRow][cellIndex] = value;
	}
	
	public void setValue(int cellIndex, String value) {
		this.setValue(cellIndex, new StringValue(value));
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.Sheet#asCSV()
	 */
	public String asCSV() {
		return this.toText(",");
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.Sheet#toText(java.lang.String)
	 */
	public String toText(String delim) {
		this.beforeFirstRow();
		StringBuffer output = new StringBuffer();
		while ( this.nextRow() ) {
			this.beforeFirstColumn();
			while ( this.nextCellInRow() ) {
				SheetValue value = this.getValue();
				if ( value != null ) {
					String aCell = value.toString();
					boolean quote = aCell.contains(delim);
					if (quote) output.append("\"");
					output.append(aCell.replaceAll("[\n\r]", " "));
					if (quote) output.append("\"");
				} 
				if ( this.currCol < this.colSize ) 
					output.append(delim);
			}
			output.append("\n");
		}
		return output.toString();
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.Sheet#row(int)
	 */
	public SheetValue[] row(int rowIndex) {
		return matrix[rowIndex];
	}
}
