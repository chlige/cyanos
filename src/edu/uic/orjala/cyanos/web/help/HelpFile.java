package edu.uic.orjala.cyanos.web.help;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import net.htmlparser.jericho.CharacterReference;
import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

/**
 * <P>This class is used by the {@link HelpIndex} class to index Help files.  This class in particular will parse a help file, i.e. an HTML file, 
 * and create the appropriate Lucene {@link Document} for the {@link IndexWriter}
 * 
 * @author George Chlipala
 *
 */
public class HelpFile {
	private File myFile;
	private String myPrefix = null;
	
	/**
	 * Create a new HelpFile object for the specified file.
	 * 
	 * @param someFile an HTML file to parse
	 */
	public HelpFile(File someFile) {
		this.myFile = someFile;
	}
	
	/**
	 * Create a new HelpFile object for the specified file.  
	 * 
	 * @param prefix Directory of the file.  Used for custom help files
	 * @param someFile an HTML file to parse
	 */
	public HelpFile(String prefix, File someFile) {
		this(someFile);
		this.myPrefix = prefix;
	}

	/**
	 * Return the Lucene {@link Document} for the HTML file.
	 * 
	 * @return a Lucene {@link Document}
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public Document document() throws FileNotFoundException, IOException {
		
		Document aDoc = new Document();
		if ( this.myPrefix != null )
			aDoc.add(new Field(HelpIndex.PATH_FIELD, String.format("%s/%s", this.myPrefix, myFile.getName()), Field.Store.YES, Field.Index.NO));			
		else 
			aDoc.add(new Field(HelpIndex.PATH_FIELD, myFile.getName(), Field.Store.YES, Field.Index.NO));
		aDoc.add(new Field(HelpIndex.LAST_MODIFIED_FIELD, DateTools.timeToString(myFile.lastModified(), DateTools.Resolution.MINUTE),
		        Field.Store.YES, Field.Index.NOT_ANALYZED));

		
		// The following is based on code from http://jericho.htmlparser.net/samples/console/src/ExtractText.java
		Source aSource = new Source(new FileReader(this.myFile));
		aSource.fullSequentialParse();
		
		Element titleElement = aSource.getFirstElement(HTMLElementName.TITLE);
		// TITLE element never contains other tags so just decode it collapsing whitespace		
		if (titleElement != null) 
			aDoc.add(new Field(HelpIndex.TITLE_FIELD, CharacterReference.decodeCollapseWhiteSpace(titleElement.getContent()), 
					Field.Store.YES, Field.Index.NOT_ANALYZED));
		
		List<Element> metaTags = aSource.getAllElements(HTMLElementName.META);		
		Iterator<Element> tagIter = metaTags.iterator();
		
		while ( tagIter.hasNext() ) {
			Element aTag = tagIter.next();
			String name = aTag.getAttributeValue("NAME");
			String value = aTag.getAttributeValue("CONTENT");
			if ( "keywords".equalsIgnoreCase(name) ) {
				String[] keywords = value.split(",");
				for ( int k = 0; k < keywords.length; k++ ) {
					aDoc.add(new Field(HelpIndex.KEYWORD_FIELD, keywords[k].trim(), Field.Store.YES, Field.Index.NOT_ANALYZED));			
				}
			} else if ( "module".equalsIgnoreCase(name) ) {
				aDoc.add(new Field(HelpIndex.MODULE_FIELD, value, Field.Store.YES, Field.Index.NOT_ANALYZED));
			}
		}
		
		Element bodyElement = aSource.getFirstElement(HTMLElementName.BODY);
		
		aDoc.add(new Field(HelpIndex.CONTENT_FIELD, bodyElement.getTextExtractor().setIncludeAttributes(true).toString(), 
				Field.Store.YES, Field.Index.ANALYZED));

		return aDoc;
	}

	/**
	 * Get content of an HTML file with all HTML tags removed.
	 * 
	 * @param aFile a HTML file.
	 * @return Text for the HTML file without HTML tags.
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static String getTextForHTML(File aFile) throws FileNotFoundException, IOException {
		Source aSource = new Source(new FileReader(aFile));
		aSource.fullSequentialParse();
		return aSource.getTextExtractor().setIncludeAttributes(true).toString();	
	}
}



/*
class HTMLHandler extends DefaultHandler {
	private final static String BODY_TAG = "BODY";
	private final static String BR_TAG = "BR";
	private final static String PARAGRAPH_TAG = "P";
	private final static String META_TAG = "META";
	private final static String TITLE_TAG = "TITLE";

	private boolean inBody = false;
	private boolean inTitle = false;
	private List<String> keywords = new ArrayList<String>();
	private String module = null;
	private StringBuffer title = new StringBuffer();
	private StringBuffer htmlContent = new StringBuffer();
	
	public String getContent() {
		return this.htmlContent.toString();
	}

	public String getTitle() {
		return this.title.toString();
	}
	
	public List<String> getKeywords() {
		return this.keywords;
	}

	public String getModuleName() {
		return this.module;
	}
	
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
		if ( BODY_TAG.equalsIgnoreCase(localName) ) {
			this.inBody = true;
		} else if ( TITLE_TAG.equalsIgnoreCase(localName) ) {
			this.inTitle = true;
		} else if ( BR_TAG.equalsIgnoreCase(localName) ) {
			this.htmlContent.append("\n");
		} else if ( META_TAG.equalsIgnoreCase(localName) ) {
			if ( "keywords".equalsIgnoreCase(atts.getValue(null, "NAME")) ) {
				String[] keywords = atts.getValue(null, "content").split(",");
				for ( int k = 0; k < keywords.length; k++ ) {
					this.keywords.add(keywords[k].toLowerCase());
				}
			} else if ( "module".equalsIgnoreCase(atts.getValue(null, "NAME")) ) {
				this.module = atts.getValue(null, "content");
			}
		}
	}

	public void characters(char[] ch, int start, int length) throws SAXException {
		if ( this.inBody ) {
			String newString = new String(ch, start, length);
			newString.replaceAll("\\n", "");
			this.htmlContent.append(newString);
		} else if ( this.inTitle ) {
			String newString = new String(ch, start, length);
			newString.replaceAll("\\n", "");
			this.title.append(newString);
		}
	}

	public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
		if ( BODY_TAG.equalsIgnoreCase(localName) ) {
			this.inBody = false;
		} else if ( TITLE_TAG.equalsIgnoreCase(localName) ) {
				this.inTitle = false;
		} else if ( PARAGRAPH_TAG.equalsIgnoreCase(localName) ) {
			this.htmlContent.append("\n\n");			
		}
	}
}
*/


