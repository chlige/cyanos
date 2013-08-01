package edu.uic.orjala.cyanos.web.servlet;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

public class InternalSheet {
	protected List<List<String>> myData;
	protected String sheetName;
	protected int maxCol = 0;
	
	protected int currRow = 0;
	protected int currCol = 0;
	
	public InternalSheet() {
		this.myData = new ArrayList<List<String>>();
	}
	
	public InternalSheet(String aName) {
		this.myData = new ArrayList<List<String>>();
		this.sheetName = new String(aName);
	}
	
	public void setName(String aName) {
		this.sheetName = new String(aName);
	}
	
	public String getName() {
		return this.sheetName;
	}
	
	public int rowCount() {
		return this.myData.size();
	}
	
	public int columnCount() {
		return this.maxCol;
	}
	
	public int rowSize() {
		if ( this.currRow > -1 && this.currRow < this.myData.size() ) {
			return this.myData.get(this.currRow).size();
		}
		return 0;
	}
	
	public void setRowSize(int aSize) {
		if ( this.currRow > -1 && this.currRow < this.myData.size() ) {
			ArrayList<String> myList = (ArrayList<String>)this.myData.get(this.currRow);
			myList.ensureCapacity(aSize);
		}
	}
	
	public String getValue(int row, int column) {
		if ( row > -1 && row < this.myData.size() ) {
			List<String> aRow = this.myData.get(row);
			if ( column > -1 && column < aRow.size() ) {
				return aRow.get(column);
			}
		}
		return null;
	}
	
	public String getValue(int column) {
		return this.getValue(this.currRow, column);
	}
	
	public String getValue() {
		return this.getValue(this.currRow, this.currCol);
	}
	
	public void beforeFirstRow() {
		this.currRow = -1;
	}
	
	public void beforeFirstColumn() {
		this.currCol = -1;
	}
	
	public void beforeFirstCell() {
		this.currCol = -1;
		this.currRow = -1;
	}
	
	public boolean firstRow() {
		if ( this.myData.size() > 0 ) {
			this.currRow = 0;
			return true;
		}
		return false;
	}
	
	public boolean firstColumn() {
		if ( this.maxCol > 0 ) {
			this.currCol = 0;
			return true;
		}
		return false;
	}
	
	public boolean firstCell() {
		return this.firstColumn() && this.firstRow();
	}
	
	public boolean nextRow() {
		this.currRow++;
		if ( this.currRow < this.myData.size() ) {
			return true;
		}
		this.currRow--;
		return false;
	}
	
	public boolean nextColumn() {
		this.currCol++;
		if ( this.currCol < this.maxCol ) {
			return true;
		}
		this.currCol--;
		return false;
	}
	
	public boolean nextCellInRow() {
		if ( this.currRow > -1 && this.currRow < this.myData.size() ) {
			List<String> aRow = this.myData.get(this.currRow);
			this.currCol++;
			if ( this.currCol < aRow.size() ) {
				return true;
			}
			this.currCol--;
		}
		return false;

	}
	
	public boolean gotoRow(int index) {
		if ( index < this.myData.size() && this.myData.get(index) != null ) {
			this.currRow = index;
			return true;
		}
		return false;
	}
		
	public void addRow() {
		List<String> aRow = new ArrayList<String>();
		this.myData.add(aRow);
		this.currRow = this.myData.size() - 1;
	}
	
	public void addRow(int index) {
		List<String> aRow = new ArrayList<String>();
		if ( index < this.myData.size() && this.myData.get(index) == null )
			this.myData.set(index, aRow);
		else if ( index > this.myData.size() )
			this.myData.add(aRow);
		else
			this.myData.add(index, aRow);
		this.currRow = index;
	}
	
	public void addCell(String aValue) {
		if ( this.currRow > -1 && this.currRow < this.myData.size() ) {
			List<String> aRow = this.myData.get(this.currRow);
			aRow.add(aValue);
			if ( aRow.size() > this.maxCol )
				this.maxCol = aRow.size();
		}
	}
		
	public void addCell(int index, String aValue) {
		if ( this.currRow > -1 && this.currRow < this.myData.size() ) {
			List<String> aRow = this.myData.get(this.currRow);
			if ( aRow.size() <= index ) {
				((ArrayList)aRow).ensureCapacity(index + 1);
			}
			aRow.add(index, aValue);
			if ( aRow.size() > this.maxCol )
				this.maxCol = aRow.size();
		}	
	}

	public String asCSV() {
		return this.toText(",");
	}
	
	public String toText(String delin) {
		this.beforeFirstRow();
		StringBuffer output = new StringBuffer();
		while ( this.nextRow() ) {
			List<String> aRow = this.myData.get(this.currRow);
			ListIterator<String> cellIter = aRow.listIterator();
			boolean nextCell = cellIter.hasNext();
			while ( nextCell ) {
				String myItem = (String)cellIter.next();
				if ( myItem != null ) {
					boolean quote = myItem.matches(delin);
					if (quote) output.append("\"");
					output.append(myItem.replaceAll("[\n\r]", " "));
					if (quote) output.append("\"");
				}
				output.append(myItem);
				if ( cellIter.hasNext() ) output.append(delin);
				else nextCell = false;
			}
			output.append("\n");
		}
		return output.toString();
	}
	
	public String[] row(int rowIndex) {
		List<String> aRow = this.myData.get(rowIndex);
		String retVal[] = {};
		if ( aRow != null ) {
			retVal = this.myData.get(rowIndex).toArray(retVal);
		} 
		return retVal;
	}
}
