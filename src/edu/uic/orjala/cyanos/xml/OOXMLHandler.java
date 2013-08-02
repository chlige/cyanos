/**
 * 
 */
package edu.uic.orjala.cyanos.xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.MathContext;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

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
public class OOXMLHandler extends DefaultHandler {

	private static final Pattern FORMAT_DECIMAL_PATTERN = Pattern.compile("([#,]*)0\\.(0+)$");
	private static final Pattern FORMAT_SCI_NOTE_PATTERN = Pattern.compile("([#,]*)0\\.(0+)E\\+0$");
	private static final Pattern FORMAT_PERCENT_PATTERN = Pattern.compile("0\\.(0+)\\%$");

	protected class OOXMLSheet {
		private InputStream data;
		private String name;
		
		protected OOXMLSheet(String name, InputStream data) {
			this.data = data;
			this.name = name;
		}
		
		protected String getName() {
			return this.name;
		}
		
		protected InputStream getStream() {
			return this.data;
		}
	}
	
	protected class OOXMLStyle {	
		
		private int fmtID;
		private String formatCode;
		
		protected OOXMLStyle(int fmtID) {
			this.fmtID = fmtID;
		}
		
		protected int getFormatID() {
			return this.fmtID;
		}
		
		protected void setFormatCode(String code) {
			this.formatCode = code;
		}
		
		protected String getFormatCode() {
			return this.formatCode;
		}
		
		/*
	 0   General
	 1	 0
	 2	 0.00
	 3	 #,##0
	 4	 #,##0.00
	 9	 0%
	 10	 0.00%
	 11	 0.00E+00
	 12	 # ?/?
	 13	 # ??/??
	 14	 mm-dd-yy
	 15	 d-mmm-yy
	 16	 d-mmm
	 17	 mmm-yy
	 18	 h:mm AM/PM
	 19	 h:mm:ss AM/PM
	 20	 h:mm
	 21	 h:mm:ss
	 22	 m/d/yy h:mm
	 37	 #,##0 ;(#,##0)
	 38	 #,##0 ;[Red](#,##0) 
	 39	 #,##0.00 ;(#,##0.00)
	 40	 #,##0.00 ;[Red](#,##0.00 
	 45	 mm:ss
	 46	 [h]:mm:ss
	 47	 mmss.0
	 48	 ##0.0E+0
	 49	 @
		 */

		protected int getType() {
			switch ( this.fmtID ) {
			case 0:
			case 1:
			case 2:
			case 3:
			case 4:
			case 11:
			case 37:
			case 38:
			case 39:
			case 40:
			case 48:
				return TYPE_NUMBER;
			case 9:
			case 10:
				// Percents.  How to handle?
				return TYPE_NUMBER;
			case 14:
			case 15:
			case 16:
			case 17:
			case 18:
			case 19:
			case 20:
			case 21:
			case 22:
				return TYPE_DATETIME;
			}	
			if ( this.formatCode != null ) {
				String format = ( this.formatCode.contains(";") ? this.formatCode.split(";")[0] : this.formatCode );
				if ( FORMAT_DECIMAL_PATTERN.matcher(format).matches() ) {
					return TYPE_NUMBER;
				} else if ( FORMAT_PERCENT_PATTERN.matcher(format).matches()) {
					return TYPE_NUMBER;
				} else if ( FORMAT_SCI_NOTE_PATTERN.matcher(format).matches()) {
					return TYPE_NUMBER;
				}
			}
		return -1;
		}
		
		protected Integer getScale() {
			if ( this.formatCode != null ) {
				String format = ( this.formatCode.contains(";") ? this.formatCode.split(";")[0] : this.formatCode );
				Matcher match = FORMAT_DECIMAL_PATTERN.matcher(format);
				if ( match.matches() ) {
					return match.group(2).length();
				} 
				match = FORMAT_PERCENT_PATTERN.matcher(format);
				if ( match.matches() ) {
					return match.group(1).length();
				}
				match = FORMAT_SCI_NOTE_PATTERN.matcher(format);
				if ( match.matches() ) {
					return -1 * match.group(2).length();
				}
			}

			switch ( this.fmtID ) {
			case 2:
			case 4:
			case 39:
			case 40:
			case 9:
				return 2;
			case 10:
				return 4;							
			case 1:
			case 3:
			case 37:
			case 38:
				return 0;
			case 11:
				return -3;
			default:
				return null;
			}
		}
	}
	
	protected final static String SHEET_TAG = "sheet";
	protected final static String ROW_TAG = "row";
	protected final static String CELL_TAG = "c";
	protected final static String VALUE_TAG = "v";
	protected final static String TEXT_TAG = "t";
	protected final static String STRING_ITEM_TAG = "si";
	protected final static String INLINE_STRING_TAG = "is";
	private final static String DIMENSION_TAG = "dimension";
	private static final String FORMULA_TAG = "f";
	private final static String NUMBER_FORMAT_TAG = "numFmt";
	
	private static final String CELLXFS_TAG = "cellXfs";
	private static final String XF_TAG = "xf";
	
	protected final static String TYPE_ATTR = "t";
	protected final static String REFERENCE_ATTR = "r";
	protected final static String NAME_ATTR = "name";
	private static final String SHEET_ID_ATTR = "sheetId";
	private static final String REF_ATTR = "ref";
	private static final String STYLE_ATTR = "s";

	private static final String FORMAT_CODE_ATTR = "formatCode";
	private static final String NUMBER_FORMAT_ID = "numFmtId";
	private static final String COUNT_ATTR = "count";
	
	protected final static String ATTR_NS = "ss";
	protected final static String ATTR_URI = "urn:schemas-microsoft-com:office:spreadsheet";
	
	private final static Pattern LOC_PATTERN = Pattern.compile("([A-Z]+)([0-9]+)", Pattern.CASE_INSENSITIVE);
	private final static Pattern DIM_PATTERN = Pattern.compile("([A-Z]+)([0-9]+):([A-Z]+)([0-9]+)", Pattern.CASE_INSENSITIVE);
	
	
	private final static BigDecimal MS_IN_DAY = new BigDecimal(24 * 3600 * 1000);
	
	protected final String[] TYPES = { "", "n", "d", "b", "s", "e", "str", "inlineStr" };
	
	protected int inFile = 0;
	
	protected final static int SHAREDSTRINGS_FILE = 1;
	protected final static String SHAREDSTRINGS_FILENAME = "xl/sharedStrings.xml";
	protected final static int WORKBOOK_FILE = 2;
	protected final static String WORKBOOK_FILENAME = "xl/workbook.xml";
	protected final static int WORKSHEET_FILE = 4;
	
	protected final static String STYLE_FILENAME = "xl/styles.xml";
	protected final static int STYLE_FILE = 3;
	
	protected final Map<String, InputStream> xmlData = new HashMap<String,InputStream>();
	protected final List<StringValue> sharedStrings = new ArrayList<StringValue>();
	private final List<OOXMLSheet> sheets = new ArrayList<OOXMLSheet>();
	private final List<OOXMLStyle> styles = new ArrayList<OOXMLStyle>();
	private final Map<String, String> numFormats = new HashMap<String,String>();
	
	protected final static int TYPE_NUMBER = 1;
	protected final static int TYPE_DATETIME = 2;
	protected final static int TYPE_BOOLEAN = 3;
	protected final static int TYPE_SHARED_STRING = 4;
	protected final static int TYPE_ERROR = 5;
	protected final static int TYPE_STRING = 6;
	protected final static int TYPE_INLINE_STRING = 7;
	
	private OOXMLStyle currStyle = null;
	
	private StringBuffer inlineString;
	
	private SpreadSheet myBook = null;
	private Sheet currSheet = null;
	
	private boolean inData = false;
	private boolean inCellXFS = false;
	
	private int cellIndex = -1;
	private int currType = 0;

	/**
	 * 
	 */
	public OOXMLHandler(SpreadSheet sheet) {
		this.myBook = sheet;
	}
	
	public void parseFile(InputStream xlsxFile) throws IOException, ParserConfigurationException, SAXException {		
		ZipInputStream zipStream = new ZipInputStream(xlsxFile);
		ZipEntry anEntry = zipStream.getNextEntry();
		while ( anEntry != null ) {
			String name = anEntry.getName();
			if ( name.equals(SHAREDSTRINGS_FILENAME) || name.equals(WORKBOOK_FILENAME) || name.equals(STYLE_FILENAME) || name.startsWith("xl/worksheets/sheet") ) {
				this.loadFile(anEntry, zipStream);
			}
			anEntry = zipStream.getNextEntry();
		}
		zipStream.close();
		
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setNamespaceAware(true);
		SAXParser saxParser = factory.newSAXParser();

		this.inFile = SHAREDSTRINGS_FILE;
		saxParser.parse(xmlData.get(SHAREDSTRINGS_FILENAME), this);
		
		this.inFile = STYLE_FILE;
		this.styles.clear();
		this.numFormats.clear();
		saxParser.parse(xmlData.get(STYLE_FILENAME), this);
		
		this.sheets.clear();
		this.inFile = WORKBOOK_FILE;
		saxParser.parse(xmlData.get(WORKBOOK_FILENAME), this);
		
		ListIterator<OOXMLSheet> iter = this.sheets.listIterator();		
		while ( iter.hasNext() ) {
			OOXMLSheet aSheet = iter.next();
			this.currSheet = new Sheet(aSheet.getName());
			this.inFile = WORKSHEET_FILE;
			saxParser.parse(aSheet.getStream(), this);
			this.myBook.addSheet(currSheet);
		}
	}
	
	
	private void loadFile(ZipEntry entry, ZipInputStream stream) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int count;
        while ((count = stream.read(buffer)) != -1) {
            baos.write(buffer, 0, count);
        }
        baos.flush();
        this.xmlData.put(entry.getName(), new ByteArrayInputStream(baos.toByteArray()));
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
	 */
	
	public void characters(char[] ch, int start, int length) throws SAXException {
		if ( inData ) {
			String value = new String(ch, start, length);
			this.inlineString.append(value);
		}
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#endDocument()
	 */
	
	public void endDocument() throws SAXException {
		this.inFile = 0;
		this.inData = false;
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if ( localName == null || localName.length() == 0) 
			localName = qName;

		switch ( this.inFile ) {
		case SHAREDSTRINGS_FILE:
			if ( localName.equalsIgnoreCase(TEXT_TAG) ) {
				this.sharedStrings.add(new StringValue(this.inlineString.toString()));
				inData = false;
			}		
			break;
		case STYLE_FILE:
			if ( localName.equalsIgnoreCase(CELLXFS_TAG) ) {
				this.inCellXFS = false;
			}
			break;
		case WORKSHEET_FILE:
			if ( localName.equalsIgnoreCase(CELL_TAG) ) {	
				if ( this.currType > -1 && this.inlineString.length() > 0 ) {
					switch ( currType ) {
					case TYPE_SHARED_STRING: 
						this.addCell(this.sharedStrings.get(Integer.parseInt(this.inlineString.toString())));
						break;
					case TYPE_NUMBER:
					case 0:
						try {
							BigDecimal dValue = new BigDecimal(this.inlineString.toString());
							if ( this.currStyle != null ) {
								NumberValue cellValue;
								Integer scale = this.currStyle.getScale();
								if ( scale == null ) {
									cellValue = new NumberValue(dValue);
								} else if ( scale < 0 ) {
									cellValue = new NumberValue(dValue.round(new MathContext(scale * -1)));
								} else {
									cellValue = new NumberValue(dValue.setScale(scale, BigDecimal.ROUND_HALF_UP));
								}
								if ( this.currStyle.getFormatID() == 9 || this.currStyle.getFormatID() == 10 ) {
									cellValue.setPercentage();
								}
								this.addCell(cellValue);
							} else {
								this.addCell(new NumberValue(dValue));
							}
						} catch (NumberFormatException e) {
							System.err.print("Could not convert value to number: ");
							System.err.println(this.inlineString.toString());
							this.addCell(this.inlineString.toString());
						}
						break;	
					case TYPE_DATETIME:
						try {
							int formatID = ( this.currStyle != null ? this.currStyle.getFormatID() : -1 );
							Calendar cal = new GregorianCalendar(1900, 0, 1);
							DateValue value = null;
							switch ( formatID ) {
							case 14:
							case 15:
							case 16:
							case 17:
								int days = Integer.parseInt(this.inlineString.toString());
								cal.add(Calendar.DATE, days);
								value = new DateValue(cal.getTime());
								value.setDate();
								break;
							case 18:
							case 19:
							case 20:
							case 21:
								BigDecimal number = new BigDecimal(this.inlineString.toString());
								cal.set(1970, 0, 1);
								cal.add(Calendar.MILLISECOND, number.multiply(MS_IN_DAY).setScale(0,BigDecimal.ROUND_HALF_UP).intValue());
								value = new DateValue(cal.getTime());
								value.setTime();
								break;
							case 22:
								number = new BigDecimal(this.inlineString.toString());
								BigDecimal dateVal = number.setScale(0, BigDecimal.ROUND_HALF_UP);
								cal.add(Calendar.DATE, dateVal.intValue());
								BigDecimal timeVal = number.subtract(dateVal);
								cal.add(Calendar.MILLISECOND, timeVal.multiply(MS_IN_DAY).setScale(0,BigDecimal.ROUND_HALF_UP).intValue());
								value = new DateValue(cal.getTime());
								value.setDateTime();
								break;
							default:
								value = new DateValue(this.inlineString.toString());
							}
							this.addCell(value);
						} catch (NumberFormatException e) {
							System.err.print("Could not convert value to date: ");
							System.err.println(this.inlineString.toString());
							this.addCell(this.inlineString.toString());
						} catch (ParseException e) {
							System.err.print("Could not convert value to date: ");
							System.err.println(this.inlineString.toString());
							this.addCell(this.inlineString.toString());
						}
						break;							
					default:
						this.addCell(this.inlineString.toString());	
					}
				}
				this.currType = -1;
				this.currStyle = null;
				this.inlineString.setLength(0);
			} else if ( currType !=  TYPE_INLINE_STRING && localName.equalsIgnoreCase(VALUE_TAG) ) {
				this.inData = false;
			} else if ( currType ==  TYPE_INLINE_STRING && localName.equalsIgnoreCase(INLINE_STRING_TAG) ) {
				this.inData = false;
			}	
		}
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#startDocument()
	 */
	
	public void startDocument() throws SAXException {
		this.inlineString = new StringBuffer();
		this.currType = 0;
		this.cellIndex = -1;
		// TODO Should clear existing spreadsheet.
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
	 */
	
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if ( localName == null || localName.length() == 0) 
			localName = qName;
		
		switch ( this.inFile ) {
		case SHAREDSTRINGS_FILE:
			if ( localName.equalsIgnoreCase(TEXT_TAG) ) {
				inData = true;
				this.inlineString = new StringBuffer();
			}		
			break;
		case WORKBOOK_FILE:
			if ( localName.equalsIgnoreCase(SHEET_TAG) ) {
				String filename = String.format("xl/worksheets/sheet%s.xml", attributes.getValue(SHEET_ID_ATTR));
				this.sheets.add(new OOXMLSheet(attributes.getValue(NAME_ATTR), this.xmlData.get(filename)));
			}
			break;
			
		case STYLE_FILE:
			if ( localName.equalsIgnoreCase(CELLXFS_TAG) ) {
				String count = attributes.getValue(COUNT_ATTR);
				try {
					int countNo = Integer.parseInt(count);
					((ArrayList<OOXMLStyle>)this.styles).ensureCapacity(countNo);	
					this.inCellXFS = true;
				} catch (NumberFormatException e) {
					System.err.print("FAILED: Trying to parse value into integer ");
					System.err.println(count);
				}
			} else if ( this.inCellXFS && localName.equalsIgnoreCase(XF_TAG) ) {
				String formatID = attributes.getValue(NUMBER_FORMAT_ID);	
				try {
					int formatNo = Integer.parseInt(formatID);
					OOXMLStyle aStyle = new OOXMLStyle(formatNo);
					this.styles.add(aStyle);
					if ( this.numFormats.containsKey(formatID)) {
						aStyle.setFormatCode(this.numFormats.get(formatID));
					}
				} catch (NumberFormatException e) {
					System.err.print("FAILED: Trying to parse value into integer ");
					System.err.println(formatID);
				}
			} else if ( localName.equals(NUMBER_FORMAT_TAG) ) {
				String formatID = attributes.getValue(NUMBER_FORMAT_ID);
				String formatCode = attributes.getValue(FORMAT_CODE_ATTR);
				this.numFormats.put(formatID, formatCode);
			}
			break;
		case WORKSHEET_FILE:
			if ( localName.equalsIgnoreCase(DIMENSION_TAG)) { 	
				String ref = attributes.getValue(REF_ATTR);
				Matcher locMatch = DIM_PATTERN.matcher(ref);
				if ( locMatch.matches() ) {
					int rowStart = Integer.parseInt(locMatch.group(2));
					int colStart = getIndex(locMatch.group(1));
					int rowEnd = Integer.parseInt(locMatch.group(4));
					int colEnd = getIndex(locMatch.group(3));
					this.currSheet.setRowCount(rowEnd - rowStart + 1);
					this.currSheet.setRowSize(colEnd - colStart + 1);
				}
			} else if ( localName.equalsIgnoreCase(ROW_TAG) ) {
				String rowIndex = attributes.getValue(REFERENCE_ATTR);
				this.currSheet.gotoRow(Integer.parseInt(rowIndex) - 1);
			} else if ( localName.equalsIgnoreCase(CELL_TAG) ) {
				this.currType = 0;
				String type = attributes.getValue(TYPE_ATTR);
				FIND_TYPE: for ( int i = 1; i < TYPES.length; i++ ) {
					if ( TYPES[i].equalsIgnoreCase(type) ) {
						this.currType = i;
						break FIND_TYPE;
					}
				}
				String loc = attributes.getValue(REFERENCE_ATTR);
				Matcher locMatch = LOC_PATTERN.matcher(loc);
				if ( locMatch.matches() ) {
					int colStart = getIndex(locMatch.group(1));
					this.cellIndex = colStart - 1;
				} else {
					this.cellIndex = -1;
				}

				String style = attributes.getValue(STYLE_ATTR);
				if ( style != null ) {
					try {
						int styleNo = Integer.parseInt(style);
						
						OOXMLStyle aStyle = this.styles.get(styleNo);
						this.currStyle = aStyle;
						if ( this.currType < 1 ) {
							this.currType = aStyle.getType();
						}
					} catch (NumberFormatException e) {
						System.err.print("FAILED: Trying to parse value into integer ");
						System.err.println(style);
					}
				}

			} else if ( currType !=  TYPE_INLINE_STRING && localName.equalsIgnoreCase(VALUE_TAG) ) {
				this.inData = true;
				this.inlineString = new StringBuffer();
			} else if ( currType ==  TYPE_INLINE_STRING ) {
				if ( localName.equalsIgnoreCase(INLINE_STRING_TAG) )
					this.inlineString = new StringBuffer();
				else if ( localName.equalsIgnoreCase(TEXT_TAG))
					this.inData = true;
			} else if ( localName.equalsIgnoreCase(FORMULA_TAG) && this.currType == 0 ) {
				this.currType = TYPE_NUMBER;
			}
			
			break;
		}
	}
	
	private static int getIndex(String col) {
		int value = 0;
		char[] chars = col.toCharArray();
		int m = 0;
		BigDecimal base = new BigDecimal(26);
		for ( int c = chars.length - 1; c >= 0; c-- ) {
			value = value + ( letterToNumber(chars[c]) * base.pow(m).intValue() );
			m++;
		}
		return value;
	}
	
	private static int letterToNumber(char letter) {
		char L = Character.toUpperCase(letter);
		return (int) L - 'A' + 1;
	}
	
	private void addCell(String value) {
		if ( cellIndex > -1 )
			currSheet.setValue(cellIndex, value);
		else
			currSheet.appendCell(value);
	}
	
	private void addCell(SheetValue value) {
		if ( cellIndex > -1 )
			currSheet.setValue(cellIndex, value);
		else
			currSheet.appendCell(value);		
	}

}
