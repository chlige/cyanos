/**
 * 
 */
package edu.uic.orjala.cyanos.xml;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import edu.uic.orjala.cyanos.web.DateValue;
import edu.uic.orjala.cyanos.web.NumberValue;
import edu.uic.orjala.cyanos.web.Sheet;
import edu.uic.orjala.cyanos.web.SheetValue;
import edu.uic.orjala.cyanos.web.SpreadSheet;
import edu.uic.orjala.cyanos.web.StringValue;

/**
 * @author George Chlipala
 *
 */
public class ODSHandler extends DefaultHandler {

	protected static final String WORKSHEET_TAG = "Worksheet";
	protected static final String TABLE_TAG = "table";
	protected static final String ROW_TAG = "table-row";
	protected static final String CELL_TAG = "table-cell";
	protected static final String P_TAG = "p";
	
	protected static final String TYPE_ATTR = "value-type";
	protected static final String NAME_ATTR = "name";
	protected static final String COL_COUNT_ATTR = "number-columns-repeated";
	protected static final String ROW_COUNT_ATTR = "number-rows-repeated";
	
	protected static final String TABLE_NS = "table";
	protected static final String TABLE_URI = "urn:oasis:names:tc:opendocument:xmlns:table:1.0";
	
	protected static final String OFFICE_NS = "office";
	protected static final String OFFICE_URI = "urn:oasis:names:tc:opendocument:xmlns:office:1.0";
	
	protected static final String STYLE_URI = "urn:oasis:names:tc:opendocument:xmlns:style:1.0";
	
	protected static final String VALUE_ATTR = "value";
	protected static final String DATE_ATTR = "date-value";
	protected static final String TIME_ATTR = "time-value";
	
	protected static final String[] OFFICE_TYPES = { "", "float", "percentage", "currency", "date", "time", "boolean", "string", "void"};
	
	protected static final int TYPE_FLOAT = 1;
	protected static final int TYPE_PERCENTAGE = 2;
	protected static final int TYPE_CURRENCY = 3;
	protected static final int TYPE_DATE = 4;
	protected static final int TYPE_TIME = 5;
	protected static final int TYPE_BOOLEAN = 6;
	protected static final int TYPE_STRING = 7;
	protected static final int TYPE_VOID = 8;
	
	private SpreadSheet myBook;
	private Sheet currSheet = null;
	
	private boolean inData = false;
	private boolean readData = false;
	
	private int colCount = 1;
	private int currType = 0;
	
	private StringBuffer currString = new StringBuffer();;

	private int cellIndex = 0;
	
	private final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
	private final DateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	private final DateFormat TIME_FORMAT = new SimpleDateFormat("'PT'HH'H'mm'M'ss'S'");

	/**
	 * 
	 */
	public ODSHandler(SpreadSheet sheet) {
		myBook = sheet;
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
	 */
	
	public void characters(char[] ch, int start, int length) throws SAXException {
		if ( this.readData && this.currSheet != null ) {
			String value = new String(ch, start, length);
			this.currString.append(value);
		}
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#endDocument()
	 */
	
	public void endDocument() throws SAXException {

	}

	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if ( localName == null || localName.length() == 0) 
			localName = qName;

		if ( localName.equalsIgnoreCase(TABLE_TAG) ) {
			this.myBook.addSheet(currSheet);
			currSheet = null;
		} else if ( this.inData && localName.equalsIgnoreCase(CELL_TAG) ) {
			this.addCell();
			this.inData = false;
			this.readData = false;
			this.currType = 0;
			this.currString.setLength(0);
		} else if ( this.readData && localName.equalsIgnoreCase(P_TAG) ) {
			this.readData = false;
		}
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#startDocument()
	 */
	
	public void startDocument() throws SAXException {
		this.currSheet = null;
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if ( localName == null || localName.length() == 0) 
			localName = qName;
		
		if ( localName.equalsIgnoreCase(TABLE_TAG) ) {
			this.currSheet = new Sheet(attributes.getValue(TABLE_URI, NAME_ATTR));
			this.currSheet.beforeFirstCell();
		} else if ( localName.equalsIgnoreCase(ROW_TAG) ) {
			this.currSheet.nextRow(true);
		} else if ( localName.equalsIgnoreCase(CELL_TAG) ) {
			this.cellIndex = 0;
			this.inData = false;
			String value = attributes.getValue(TABLE_URI, COL_COUNT_ATTR);
			if ( value != null ) {
				colCount = Integer.parseInt(value);
			} else {
				this.colCount = 1;
			}
			String type = attributes.getValue(OFFICE_URI, TYPE_ATTR);
			this.currType = 0;
			FIND_TYPE: for ( int i = 1; i < OFFICE_TYPES.length; i++ ) {
				if ( OFFICE_TYPES[i].equalsIgnoreCase(type) ) {
					this.currType = i;
					break FIND_TYPE;
				}
			}
			try {
				switch ( this.currType ) {
				case TYPE_FLOAT:
				case TYPE_PERCENTAGE:
					value = attributes.getValue(OFFICE_URI, VALUE_ATTR);
					NumberValue cellValue = new NumberValue(value);
					addCell(cellValue);
					if ( currType == TYPE_PERCENTAGE )
						cellValue.setPercentage();
					break;
				case TYPE_DATE:
					value = attributes.getValue(OFFICE_URI, DATE_ATTR);
					DateValue dValue;
					if ( value.contains("T") ) {
						dValue = new DateValue(DATETIME_FORMAT.parse(value));
						dValue.setDateTime();
					} else {
						dValue = new DateValue(DATE_FORMAT.parse(value));
						dValue.setDate();
					}
					addCell(dValue);
					break;
				case TYPE_TIME:
					value = attributes.getValue(OFFICE_URI, TIME_ATTR);
					dValue = new DateValue(TIME_FORMAT.parse(value));
					dValue.setTime();
					addCell(dValue);
					break;
				default:
					this.inData = true;
				}
			} catch (NumberFormatException e) {
				System.err.print("Could not convert value to number: ");
				System.err.println(value);
				this.inData = true;
			} catch (ParseException e) {
				System.err.print("Could not convert value to date: ");
				System.err.println(value);
				this.inData = true;
			}

		} else if ( this.inData && localName.equalsIgnoreCase(P_TAG) ) {
			this.currString = new StringBuffer();
			this.readData = true;
		}
	}

	private void addCell(SheetValue value) {
		for ( int cell = 0; cell < this.colCount ; cell++ ) {
			if ( cellIndex != 0 ) {
				this.currSheet.addCell(cellIndex, value);
			} else
				this.currSheet.addCell(value);
		}		
	}

	private void addCell(String value) {
		this.addCell(new StringValue(value));
	}

	private void addCell() {
		switch ( currType ) {
		case TYPE_FLOAT:
			try {
				NumberValue value = new NumberValue(this.currString.toString());
				addCell(value);
				break;
			} catch (NumberFormatException e) {
				System.err.print("Could not convert value to number: ");
				System.err.println(this.currString.toString());
			}
		default:
			addCell(this.currString.toString());				
		}
	}

}
