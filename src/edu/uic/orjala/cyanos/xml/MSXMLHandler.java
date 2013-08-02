/**
 * 
 */
package edu.uic.orjala.cyanos.xml;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
public class MSXMLHandler extends DefaultHandler {

	private static final Pattern FORMAT_DECIMAL_PATTERN = Pattern.compile("([#,]*)0\\.(0+)$");
	private static final Pattern FORMAT_SCI_NOTE_PATTERN = Pattern.compile("([#,]*)0\\.(0+)E\\+0$");
	private static final Pattern FORMAT_PERCENT_PATTERN = Pattern.compile("(0\\.)?(0+)\\%$");

	protected class MSXMLStyle {	

		private String formatString;

		protected MSXMLStyle() {
		}

		protected void setFormat(String code) {
			this.formatString = code;
		}

		protected String getFormat() {
			return this.formatString;
		}

		/*
		 * General, 
		 * General Number, 
		 * General Date, 
		 * Long Date, Medium Date, Short Date, 
		 * Long Time, Medium Time, Short Time, 
		 * Currency, Euro Currency, 
		 * Fixed, Standard, Percent, Scientific, 
		 * Yes/No, True/False, or On/Off
		 */

		protected Integer getScale() {
			if ( this.formatString != null ) {
				if ( this.formatString.startsWith("General")) {
					return null;
				} else if ( this.formatString.equals("Fixed") ) {
					return null;
				} else if ( this.formatString.equals("Percent") ) {
					return 4;
				} else if ( this.formatString.equals("Scientific") ) {
					return -3;
				}
				String format = ( this.formatString.contains(";") ? this.formatString.split(";")[0] : this.formatString );
				Matcher match = FORMAT_DECIMAL_PATTERN.matcher(format);
				if ( match.matches() ) {
					return match.group(2).length();
				} 
				match = FORMAT_PERCENT_PATTERN.matcher(format);
				if ( match.matches() ) {
					return (match.group(1) == null ? 2 : match.group(2).length() + 2);
				}
				match = FORMAT_SCI_NOTE_PATTERN.matcher(format);
				if ( match.matches() ) {
					return -1 * match.group(2).length();
				}
			}
			return null;
		}

		public boolean isPercent() {
			if ( this.formatString == null ) 
				return false;
			if ( this.formatString.equals("Percent") ) 
				return true;
			if ( this.formatString.contains(";") ) {
				String[] formats = this.formatString.split(";");
				if ( formats.length == 4 ) {
					return ( formats[0].endsWith("%") || formats[1].endsWith("%") || formats[2].endsWith("%") );
				}
			} else if ( this.formatString.endsWith("%") ) 
				return true;
			return false;
		}
	}


	private static final String WORKSHEET_TAG = "Worksheet";
	private static final String TABLE_TAG = "Table";
	private static final String ROW_TAG = "Row";
	private static final String CELL_TAG = "Cell";
	private static final String DATA_TAG = "Data";
	private static final String STYLE_TAG = "Style";
	private static final String NUMBER_FORMAT_TAG = "NumberFormat";

	private static final String ID_ATTR = "ID";
	private static final String FORMAT_ATTR = "Format";
	private static final String TYPE_ATTR = "Type";
	private static final String INDEX_ATTR = "Index";
	private static final String NAME_ATTR = "Name";
	private static final String COL_COUNT_ATTR = "ExpandedColumnCount";
	private static final String ROW_COUNT_ATTR = "ExpandedRowCount";
	private static final String STYLE_ATTR = "StyleID";

	private static final String ATTR_NS = "ss";
	private static final String ATTR_URI = "urn:schemas-microsoft-com:office:spreadsheet";

	private static final String[] TYPES = { "", "Number", "DateTime", "Boolean", "String", "Error" };

	private static final int TYPE_NUMBER = 1;
	private static final int TYPE_DATETIME = 2;
	private static final int TYPE_BOOLEAN = 3;
	private static final int TYPE_STRING = 4;
	private static final int TYPE_ERROR = 5;

	private SpreadSheet myBook;
	private Sheet currSheet = null;

	private static final DateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

	//	private String name;

	private boolean inData = false;

	private int cellIndex = 0;
	private int currType = 0;
	private StringBuffer currString;
	private final Map<String,MSXMLStyle> styles = new HashMap<String,MSXMLStyle>();
	private MSXMLStyle currStyle = null;

	/**
	 * 
	 */
	public MSXMLHandler(SpreadSheet sheet) {
		myBook = sheet;
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
	 */
	
	public void characters(char[] ch, int start, int length) throws SAXException {
		if ( this.currSheet != null && inData ) {
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

		if ( localName.equalsIgnoreCase(WORKSHEET_TAG) ) {
			this.myBook.addSheet(currSheet);
			currSheet = null;
		} else if ( localName.equalsIgnoreCase(CELL_TAG) ) {
			this.cellIndex = 0;
			this.addCell();
			this.currType = 0;
			this.currStyle = null;
		} else if ( localName.equalsIgnoreCase(DATA_TAG) ) {
			this.inData = false;
		} else if ( localName.equalsIgnoreCase(STYLE_TAG) ) {
			this.currStyle = null;
		}		
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#startDocument()
	 */
	
	public void startDocument() throws SAXException {
		// TODO Should clear existing spreadsheet.
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if ( localName == null || localName.length() == 0) 
			localName = qName;

		if ( localName.equalsIgnoreCase(WORKSHEET_TAG) ) {
			this.currSheet = new Sheet(attributes.getValue(ATTR_URI, NAME_ATTR));
		} else if ( localName.equalsIgnoreCase(TABLE_TAG) ) {
			String colCount = attributes.getValue(ATTR_URI, COL_COUNT_ATTR);
			String rowCount = attributes.getValue(ATTR_URI, ROW_COUNT_ATTR);
			this.currSheet.setRowCount(Integer.parseInt(rowCount));
			this.currSheet.setRowSize(Integer.parseInt(colCount));
			this.currSheet.beforeFirstCell();
		} else if ( localName.equalsIgnoreCase(ROW_TAG) ) {
			this.currSheet.nextRow(true);
		} else if ( localName.equalsIgnoreCase(CELL_TAG) ) {
			String value = attributes.getValue(ATTR_URI, INDEX_ATTR);
			if ( value != null ) {
				cellIndex = Integer.parseInt(value);
			} else {
				this.cellIndex = 0;
			}
			value = attributes.getValue(ATTR_URI, STYLE_ATTR);
			if ( value != null ) {
				this.currStyle = this.styles.get(value);
			}
		} else if ( localName.equalsIgnoreCase(DATA_TAG) ) {
			String type = attributes.getValue(ATTR_URI, TYPE_ATTR);
			this.currType = 0;
			this.inData = true;
			this.currString = new StringBuffer();
			FIND_TYPE: for ( int i = 1; i < TYPES.length; i++ ) {
				if ( TYPES[i].equalsIgnoreCase(type) ) {
					this.currType = i;
					break FIND_TYPE;
				}
			}
		} else if ( localName.equalsIgnoreCase(STYLE_TAG) ) {
			this.currStyle = new MSXMLStyle();
			this.styles.put(attributes.getValue(ATTR_URI, ID_ATTR), this.currStyle);
		} else if ( this.currStyle != null && localName.equalsIgnoreCase(NUMBER_FORMAT_TAG) ) {
			this.currStyle.setFormat(attributes.getValue(ATTR_URI, FORMAT_ATTR));
		}
	}

	private void addCell(SheetValue value) {
		if ( cellIndex != 0 )
			currSheet.addCell(cellIndex, value);
		else
			currSheet.addCell(value);		
	}

	private void addCell() {
		String cellValue = this.currString.toString();
		switch (this.currType) {
		case TYPE_NUMBER:
			BigDecimal dValue = new BigDecimal(cellValue);
			if ( this.currStyle != null ) {
				NumberValue nValue;
				Integer scale = this.currStyle.getScale();
				if ( scale == null ) {
					nValue = new NumberValue(dValue);
				} else if ( scale < 0 ) {
					nValue = new NumberValue(dValue.round(new MathContext(scale * -1)));
				} else {
					nValue = new NumberValue(dValue.setScale(scale, BigDecimal.ROUND_HALF_UP));
				}
				if ( this.currStyle.isPercent() ) {
					nValue.setPercentage();
				}
				this.addCell(nValue);
			} else {
				this.addCell(new NumberValue(dValue));
			}
			break;
		case TYPE_DATETIME:
			try {
				Date value = DATETIME_FORMAT.parse(cellValue);
				DateValue sheetValue = new DateValue(value);
				String format = this.currStyle.getFormat();
				if ( format.endsWith(" Time") ) {
					sheetValue.setTime();
				} else if ( format.endsWith(" Date") && (! format.startsWith("General")) ) {
					sheetValue.setDate();
				}
				addCell(sheetValue);
				break;
			} catch (ParseException e) {
				e.printStackTrace();
			}
		default:
			addCell(new StringValue(cellValue));
		}
		this.currString.setLength(0);
	}
}
