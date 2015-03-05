/**
 * 
 */
package edu.uic.orjala.cyanos.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import edu.uic.orjala.cyanos.xml.MSXMLHandler;
import edu.uic.orjala.cyanos.xml.ODSHandler;
import edu.uic.orjala.cyanos.xml.OOXMLHandler;

/**
 * @author George Chlipala
 *
 */
public class SpreadSheet {

	/**
	 * 
	 */
	
	protected final List<Sheet> myData = new ArrayList<Sheet>();
	private final String[][] TAGS = { 
			{ "table:table", "table:table-row", "table:table-cell", "text:p", "table:name", "office:date-value", "table:number-columns-repeated", "ss:Index" },
			{ "Worksheet", "Row", "Cell", "Data", "ss:Name", "DATE_TAG_HOLDER", "table:number-columns-repeated", "ss:Index" }, 
			{ "table", "table-row", "table-cell", "table-data", "table-name" }};
	private final int TABLE_TAG = 0;
	private final int ROW_TAG = 1;
	private final int CELL_TAG = 2;
	private final int DATA_TAG = 3;
	protected final int TABLE_NAME_TAG = 4;
	protected final int DATE_VALUE_TAG = 5;
	protected final int REPEAT_TAG = 6;
	protected final int INDEX_TAG = 7;
	
	public final static int ODF_TYPE = 0;
	public final static int MS_EXCEL_TYPE = 1;
	public final static int INTERNAL_TYPE = 2;
	
	private static final String XML_CONTENT_TYPE = "application/xml";
	private static final String XML_EXT = ".xml";
	
	public final static String XLSX_CONTENT_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
	public final static String XLSX_EXT = ".xlsx";

	private static final String ODS_CONTENT_TYPE = "application/vnd.oasis.opendocument.spreadsheet";
	private static final String ODS_EXT = ".ods";

	
	public SpreadSheet(InputStream anInput, int dataType) throws ParserConfigurationException, SAXException, IOException {
		this.loadXMLFile(anInput, dataType);
	}

	public SpreadSheet(FileUpload anItem) throws ParserConfigurationException, SAXException, IOException {
		InputStream xmlData = anItem.getStream();
		String contentType = anItem.getContentType();
		String fileName = anItem.getName();
		if (contentType.equals(XML_CONTENT_TYPE) || fileName.endsWith(XML_EXT) || fileName.endsWith(".xls")) {
			this.loadXMLFile(xmlData, MS_EXCEL_TYPE);
		} else if (contentType.equals(ODS_CONTENT_TYPE) || fileName.endsWith(ODS_EXT)) {
			ZipInputStream zipStream = new ZipInputStream(xmlData);
			ZipEntry anEntry = zipStream.getNextEntry();
			boolean foundContent = false;
			while ( anEntry != null ) {
				if ( anEntry.getName().equals("content.xml") ) {
					foundContent = true;
					break;
				}
				anEntry = zipStream.getNextEntry();
			}
			if ( foundContent ) {
				this.loadXMLFile(zipStream, ODF_TYPE);
			}
			zipStream.close();
		} else if ( contentType.equals(XLSX_CONTENT_TYPE) || fileName.endsWith(XLSX_EXT) ) {
			OOXMLHandler handler = new OOXMLHandler(this);
			handler.parseFile(xmlData);
		}
	}
	
	protected void loadXMLFile(InputStream xmlData, int dataType) throws ParserConfigurationException, SAXException, IOException {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setNamespaceAware(true);
		SAXParser saxParser = factory.newSAXParser();
		DefaultHandler handler = null;
		switch (dataType) {
		case ODF_TYPE:
			handler = new ODSHandler(this);
			break;
		case MS_EXCEL_TYPE:
			handler = new MSXMLHandler(this);
			break;
		}
		saxParser.parse(xmlData, handler);

		/*
		String[] theseTags = this.TAGS[dataType];
		if ( xmlData != null ) {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document dom = db.parse(xmlData);	

			Element docElem = dom.getDocumentElement();
			NodeList nl = docElem.getElementsByTagName(theseTags[this.TABLE_TAG]);

			if (nl != null && nl.getLength() > 0 ) {
				for (int i = 0; i < nl.getLength(); i++) {			
					Element wkst = (Element)nl.item(i);
					Sheet aSheet = new Sheet(wkst.getAttribute(theseTags[this.TABLE_NAME_TAG]));
					NodeList rows = wkst.getElementsByTagName(theseTags[this.ROW_TAG]);
					if (rows != null && rows.getLength() > 0 ) {
						for ( int r = 0; r < rows.getLength(); r++ ) {
							aSheet.addRow();
							Element thisRowEl = (Element)rows.item(r);
							NodeList cells = thisRowEl.getElementsByTagName(theseTags[this.CELL_TAG]);
							if ( cells != null && cells.getLength() > 0 ) {
								for ( int c = 0; c < cells.getLength(); c++ ) {
									Element cellEl = (Element)cells.item(c);
									String myValue = "";
									int reps = 1;
									if ( cellEl.hasAttribute(theseTags[this.REPEAT_TAG]) ) {
										reps = Integer.parseInt(cellEl.getAttribute(theseTags[this.REPEAT_TAG]));
									}
									if ( cellEl.hasAttribute(theseTags[this.DATE_VALUE_TAG]) ) {
										myValue = cellEl.getAttribute(theseTags[this.DATE_VALUE_TAG]);
									} else {
										NodeList data = cellEl.getElementsByTagName(theseTags[this.DATA_TAG]);
										if ( data != null && data.getLength() > 0) {
											Element dataEl = (Element)data.item(0);
											if (dataEl.getFirstChild() != null ) {
												myValue = dataEl.getFirstChild().getNodeValue();
											} 
										} 
									}
									int anIndex = -1;
									if ( cellEl.hasAttribute(theseTags[this.INDEX_TAG]) ) {
										anIndex = Integer.parseInt(cellEl.getAttribute(theseTags[this.INDEX_TAG])) - 1;
									}
									for ( int crep = 0; crep < reps ; crep++ ) {
										if ( anIndex > 0 ) 
											aSheet.addCell(anIndex, myValue);
										else 
											aSheet.addCell(myValue);
									}
								}
							}
						}
					}
					this.myData.add(aSheet);
				}
			}
		}
		*/
	}

	public List<String> worksheetNames() {
		List<String> names = new ArrayList<String>();
		ListIterator<Sheet> anIter = this.myData.listIterator();
		while ( anIter.hasNext() ) {
			Sheet aSheet = anIter.next();
			names.add(aSheet.getName());
		}
		return names;
	}

	
	public Sheet getSheet(int index) {
		return this.myData.get(index);
	}
	
	public void addSheet() {
		Sheet aSheet = new Sheet();
		this.addSheet(aSheet);
	}
	
	public void addSheet(Sheet aSheet) { 
		this.myData.add(aSheet);
	}
	
	public void toXML(OutputStream aStream) throws ParserConfigurationException, TransformerFactoryConfigurationError, TransformerException {
		Result xmlResult = new StreamResult(aStream);
		this.output(xmlResult);
	}
	
	public void toXML(Writer aWriter) throws ParserConfigurationException, TransformerFactoryConfigurationError, TransformerException {
		Result xmlResult = new StreamResult(aWriter);
		this.output(xmlResult);
	}
	
	private void output(Result xmlResult) throws ParserConfigurationException, TransformerException {
		Source aSrc = new DOMSource(this.xmlDocument());
		Transformer xmlTrn = TransformerFactory.newInstance().newTransformer();
		xmlTrn.transform(aSrc, xmlResult);
	}
	
	private Document xmlDocument() throws ParserConfigurationException {
		String[] theseTags = this.TAGS[INTERNAL_TYPE];
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document dom = db.newDocument();
		Element myBook = dom.createElement("workbook");
		dom.appendChild(myBook);
		
		ListIterator<Sheet> sheetIter = this.myData.listIterator();
		while ( sheetIter.hasNext() ) {
			Element aWks = dom.createElement(theseTags[TABLE_TAG]);
			myBook.appendChild(aWks);
//			dom.appendChild(aWks);
			Sheet aSheet = sheetIter.next();
			aWks.setAttribute(theseTags[TABLE_NAME_TAG], aSheet.getName());
			for ( int r = 0; r < aSheet.rowCount(); r++ ) {
				Element aRow = dom.createElement(theseTags[ROW_TAG]);
				aSheet.gotoRow(r);
				aWks.appendChild(aRow);
				for ( int c = 0; c < aSheet.rowSize(); c++ ) {
					Element aCell = dom.createElement(theseTags[CELL_TAG]);
					Element dataEl = dom.createElement(theseTags[DATA_TAG]);
					dataEl.setNodeValue(aSheet.getStringValue(c));
					aCell.appendChild(dataEl);
					aRow.appendChild(aCell);
				}
			}
		}
		return dom;
	}
}
