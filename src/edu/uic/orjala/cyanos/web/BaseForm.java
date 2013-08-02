/**
 * 
 */
package edu.uic.orjala.cyanos.web;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import edu.uic.orjala.cyanos.Collection;
import edu.uic.orjala.cyanos.CyanosObject;
import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.Isolation;
import edu.uic.orjala.cyanos.Project;
import edu.uic.orjala.cyanos.Sample;
import edu.uic.orjala.cyanos.SampleCollection;
import edu.uic.orjala.cyanos.Strain;
import edu.uic.orjala.cyanos.User;
import edu.uic.orjala.cyanos.sql.SQLData;
import edu.uic.orjala.cyanos.sql.SQLProject;
import edu.uic.orjala.cyanos.sql.SQLProtocol;
import edu.uic.orjala.cyanos.sql.SQLSampleCollection;
import edu.uic.orjala.cyanos.web.html.Div;
import edu.uic.orjala.cyanos.web.html.Image;
import edu.uic.orjala.cyanos.web.html.Popup;
import edu.uic.orjala.cyanos.web.html.PopupGroup;
import edu.uic.orjala.cyanos.web.html.TableCell;

/**
 * @author George Chlipala
 *
 */
public abstract class BaseForm {

	private static final String DATE_FORMAT = "yyyy-MM-dd";
	public static final String SQL_LOAD_TEMPLATE = "SELECT template FROM data_templates WHERE name=? AND data=?";
	public final static String ERROR_TAG = "<FONT COLOR='red'><B>ERROR:</FONT></B> ";
	public final static String FAILED_TAG = "<FONT COLOR='red'><B>FAILED:</FONT></B> ";
	public final static String WARNING_TAG = "<FONT COLOR='orange'><B>WARNING:</FONT></B> ";
	public final static String SUCCESS_TAG = "<FONT COLOR='green'><B>SUCCESS:</FONT></B> ";
	public final static String FOUND_TAG = "<FONT COLOR='blue'><B>FOUND:</FONT></B> ";
	public final static String SKIP_TAG = "<FONT COLOR='purple'><B>SKIPPED:</FONT></B> ";
	public final static String NOTICE_TAG = "<FONT COLOR='gray'><B>NOTICE:</FONT></B> ";
	

	public static final String TEXT_INPUT = "<INPUT TYPE='TEXT' NAME=\"%s\" VALUE=\"%s\" />";
	public static final String INPUT_LIVESEARCH = "<INPUT ID=\"%s\" TYPE='TEXT' NAME=\"%s\" VALUE=\"%s\" autocomplete='off' onKeyUp=\"livesearch(this, '%s', '%s')\" style='padding-bottom: 0px'/><DIV ID=\"%s\" CLASS='livesearch'></DIV>";
	
	public final static String LOADING_DIV = "<DIV ID='loading'></DIV>";

	protected CyanosWrapper myWrapper = null;
	
	public BaseForm(CyanosWrapper callingServlet) {
		this.myWrapper = callingServlet;
	}
	
	protected static String livesearch(String fieldID, String fieldValue, String searchID, String divID) {
		return String.format(INPUT_LIVESEARCH, fieldID, fieldID, fieldValue, searchID, divID, divID);
	}
	
	public static String getIconPathForMIMEType(CyanosWrapper aWrap, String mimeType) {
		String retval = "binary.png";
		if ( mimeType == null ) {
			retval =  "binary.png";
		} else if ( mimeType.equals("application/pdf") ||  mimeType.equals("text/html") ) {
			retval =  "layout.png";
		} else if ( mimeType.startsWith("image") ) {
			retval =  "image.png";
		} else if ( mimeType.equals("text/plain") ) {
			retval =  "text.png";
		} else if ( mimeType.startsWith("application/vnd.ms") ) {
			retval =  "quill.png";
		} else if ( mimeType.equals("application/zip") ) {
			retval =  "compressed.png";
		} 
		return String.format("%s/images/icons/%s",aWrap.getContextPath(), retval);
	}

	
	protected Image getIconForMIMEType(String mimeType) {
		Image anIcon = null;
		if ( mimeType == null ) {
			anIcon = this.getImage("icons/binary.png");
		} else if ( mimeType.equals("application/pdf") ||  mimeType.equals("text/html") ) {
			anIcon = this.getImage("icons/layout.png");
		} else if ( mimeType.startsWith("image") ) {
			anIcon = this.getImage("icons/image.png");
		} else if ( mimeType.equals("text/plain") ) {
			anIcon = this.getImage("icons/text.png");
		} else if ( mimeType.startsWith("application/vnd.ms") ) {
			anIcon = this.getImage("icons/quill.png");
		} else if ( mimeType.equals("application/zip") ) {
			anIcon = this.getImage("icons/compressed.png");
		} else {
			anIcon = this.getImage("icons/binary.png");
		}
		return anIcon;
	}
	
	protected String getImagePath(String imageName) {
		if ( myWrapper != null ) {
			String imagePath = this.myWrapper.getContextPath() + "/images/" + imageName;
			return imagePath;
		}
		return null;
	}

	protected Image getImage(String imageName) {
		String imagePath = this.getImagePath(imageName);
		if ( imagePath != null ) {
			Image aImage = new Image(imagePath);
			return aImage;
		} 
		return null;
	}
	
	protected String getFormValue(String key) {
		if ( myWrapper != null ) {
			return this.myWrapper.getFormValue(key);
		}
		return "";
	}
	
	protected String[] getFormValues(String key) {
		if ( myWrapper != null ) {
			return this.myWrapper.getFormValues(key);
		}
		return null;
	}
	
	protected String getFormValue(String key, int index) {
		if ( myWrapper != null ) {
			return this.myWrapper.getFormValue(key, index);
		}
		return "";
	}
	
	protected boolean hasFormValue(String key) {
		if ( myWrapper != null) {
			return this.myWrapper.hasFormValue(key);
		}
		return false;
	}
	
	protected boolean hasFormValues() {
		return this.myWrapper.hasFormValues();
	}

	public static String autoFormatAmount(String format, float amount) {
		String scale = new String("");
		if ( amount < .001 ) {
			scale = "u";
			amount *= (1000*1000);
		} else if ( amount < .1 ) {
			scale = "m";
			amount *= 1000;
		} else if ( amount > 500 ) {
			scale = "k";
			amount /= 1000;
		}
		return String.format(format, amount, scale);
	}

	public static String formatAmount(String format, float amount, String scale) {
		if ( scale != null ) {
			String[] units = scale.split(" */ *");
			switch (units[0].charAt(0)) {
			case 'k':	amount /= 1000; break;
			case 'm':	amount *= 1000; break;
			case 'u':	amount *= (1000*1000); break;
			}
			if ( units.length == 2 ) {
				switch (units[1].charAt(0)) {
				case 'k':	amount *= 1000; break;
				case 'm':	amount /= 1000; break;
				case 'u':	amount /= (1000*1000); break;			
				}
			}
			return String.format(format, amount, scale);
		} else {
			return String.format(format, amount, "");
		}
	}
	
	public static String formatAmount(BigDecimal amount, String unit) {
		return CyanosObject.formatAmount(amount, unit);
	}
	
	public static BigDecimal parseAmount(String amount, String scale) {
		return CyanosObject.parseAmount(amount, scale);
	}
	
	public static BigDecimal parseAmount(String amount) {
		return CyanosObject.parseAmount(amount);
	}

	protected TableCell makeFormTextRow(String label, String name) {
		if ( this.hasFormValue(name) ) {
			return this.makeFormTextRow(label, name, this.getFormValue(name));
		} 
		return this.makeFormTextRow(label, name, "");
	}
	
	protected TableCell makeFormTextRow(String label, String name, String value ) {
		if ( value == null ) value = "";
		TableCell aCell = new TableCell(label);
		aCell.addItem(String.format(TEXT_INPUT, name, value));
		return aCell;
	}
	
	protected TableCell makeFormTextAreaRow(String label, String name) {
		if ( this.hasFormValue(name) ) {
			return this.makeFormTextAreaRow(label, name, this.getFormValue(name));
		} 
		return this.makeFormTextAreaRow(label, name, "");
	}
	
	protected TableCell makeFormTextAreaRow(String label, String name, String value ) {
		return this.makeFormTextAreaRow(label, name, value, 70, 7);
	}

	protected TableCell makeFormTextAreaRow(String label, String name, String value, int cols, int rows ) {
		if ( value == null ) value = "";
		TableCell aCell = new TableCell(label);		
		aCell.addItem(String.format("<TEXTAREA NAME='%s'COLS='%d' ROWS='%d'>%s</TEXTAREA>", name, cols, rows, value));
		return aCell;
	}
	
	protected TableCell makeFormDateRow(String label, String name, String formName, Date value) {
		SimpleDateFormat aFormat = new SimpleDateFormat(DATE_FORMAT);
		return this.makeFormDateRow(label, name, formName, aFormat.format(value));
	}
	
	protected TableCell makeFormDateRow(String label, String name, String formName, String value) {
		if ( value == null ) value = "";
		TableCell aCell = new TableCell(label);
		Image myImage = this.getImage("calendar.png");
		myImage.setAttribute("BORDER", "0");
		myImage.setAttribute("ALIGN", "MIDDLE");
		aCell.addItem(String.format("<INPUT TYPE=TEXT NAME='%s' SIZE=10 VALUE='%s'/>" + 
			"<A onClick='selectDate(document.%s.%s)'>%s</A>", name, value, formName, name, myImage.toString()));
		return aCell;	
	}
	
	protected TableCell makeFormDateRow(String label, String name, String formName) {
		String value;
		if ( this.hasFormValue(name)) {
			value = this.getFormValue(name);
		} else {
			SimpleDateFormat myDate = new SimpleDateFormat(DATE_FORMAT);
			Date now = new Date();
			value = myDate.format(now);
		}
		return this.makeFormDateRow(label, name, formName, value);
	}

	public static String shortenString(String aString, int length) {
		if (aString == null ) 
			return "";
		else 
			aString = aString.split("\n")[0];
			
		if ( aString.length() < length)
			return aString;
		else 
			return aString.substring(0, length);
	}

	protected Popup protocolPopup(String protocol) throws DataException {
		List<String> protocolList = SQLProtocol.listProtocols(this.getSQLDataSource(), protocol);
		ListIterator<String> anIter = protocolList.listIterator();		
		Popup aPop = new Popup();
		aPop.addItemWithLabel("", "NONE");
		while ( anIter.hasNext()) {
			aPop.addItem(anIter.next());
		}
		return aPop;
	}
	
	protected Popup projectPopup() throws DataException  {
		Project allProjs = SQLProject.projects(this.myWrapper.getSQLDataSource(), SQLProject.ID_COLUMN, SQLProject.ASCENDING_SORT);
		allProjs.beforeFirst();
		Popup aPop = new Popup();
		aPop.addItemWithLabel("", "NONE");
		while ( allProjs.next() ) {
			aPop.addItemWithLabel(allProjs.getID(), String.format("%s (%s)", allProjs.getName(), allProjs.getID()));
		}
		return aPop;
	}

	protected Map<String,String> loadDataTemplate(String data, String name) throws DataException {
		return SQLProtocol.loadProtocol(this.getSQLDataSource(), data, name);
	}
	
	protected void saveDataTemplate(String data, String name, Map<String,String> aTemplate) throws DataException {
		SQLProtocol.saveProtocol(this.getSQLDataSource(), aTemplate, data, name);
	}

	protected SQLData getSQLDataSource() throws DataException {
		return this.myWrapper.getSQLDataSource();
	}

	protected SimpleDateFormat dateFormat() {
		return this.myWrapper.dateFormat();
	}

	protected String handleException(Exception e) {
		return this.myWrapper.handleException(e);
	}
	
	protected Div collapsableDiv(String id, String title, String content) {
		Div mainDiv = new Div();
		mainDiv.setClass("collapseSection");
		Image twist = this.getImage("twist-closed.png");
		twist.setAttribute("ID", String.format("twist_%s", id));
		twist.setAttribute("ALIGN", "ABSMIDDLE");
		mainDiv.addItem(String.format("<A NAME='%s' CLASS='twist' onClick='loadDiv(\"%s\")' CLASS='divTitle'>%s %s</A>", id, id, twist.toString(), title));
		
		Div contentDiv = new Div(content);
		contentDiv.setID(String.format("div_%s", id));
		contentDiv.setClass("hideSection");
		mainDiv.addItem(contentDiv);
		
		return mainDiv;
	}
	
	protected Div openDiv(String id, String title, String content) {
		Div mainDiv = new Div();
		mainDiv.setClass("collapseSection");
		Image twist = this.getImage("twist-open.png");
		twist.setAttribute("ID", String.format("twist_%s", id));
		twist.setAttribute("ALIGN", "ABSMIDDLE");
		mainDiv.addItem(String.format("<A NAME='%s' CLASS='twist' onClick='loadDiv(\"%s\")' CLASS='divTitle'>%s %s</A>", id, id, twist.toString(), title));
		
		Div contentDiv = new Div(content);
		contentDiv.setID(String.format("div_%s", id));
		contentDiv.setClass("showSection");
		mainDiv.addItem(contentDiv);
		
		return mainDiv;
	}
	
	protected Div selectedDiv(String id, String title, String content) {
		Div mainDiv = new Div();
		mainDiv.setClass("selectSection");
		
		Div contentDiv = new Div(content);
		contentDiv.setID(String.format("div_%s", id));
		
		if ( this.hasFormValue(id) || (! this.hasFormValues()) ) {
			contentDiv.setClass("showSection");
			mainDiv.addItem(String.format("<A NAME='%s' CLASS='twist'><INPUT TYPE='CHECKBOX' NAME='%s' onClick='selectDiv(this)' CHECKED /> %s</A>", id, id, title));
		} else {
			contentDiv.setClass("hideSection");
			mainDiv.addItem(String.format("<A NAME='%s' CLASS='twist'><INPUT TYPE='CHECKBOX' NAME='%s' onClick='selectDiv(this)' /> %s</A>", id, id, title));
			
		}
		mainDiv.addItem(contentDiv);
		
		return mainDiv;
	}
	
	
	public Div loadableDiv(String id, String title) {
		Div mainDiv = new Div();
		mainDiv.setClass("collapseSection");
		Image twist = this.getImage("twist-closed.png");
		twist.setAttribute("ID", String.format("twist_%s", id));
		twist.setAttribute("ALIGN", "ABSMIDDLE");
		mainDiv.addItem(String.format("<A NAME='%s' CLASS='twist' onClick='loadDiv(\"%s\")' CLASS='divTitle'>%s %s</A>", id, id, twist.toString(), title));
		
		Div contentDiv = new Div();
		contentDiv.setID(String.format("div_%s", id));
		contentDiv.setClass("unloaded");
		mainDiv.addItem(contentDiv);
		
		return mainDiv;
	}
	
	protected Div ajaxDiv(String id, String content) {
		Div contentDiv = new Div();
		contentDiv.setID(String.format("div_%s", id));
		contentDiv.setClass("showSection");
		return contentDiv;
	}
	
	protected User getUser() throws DataException {
		return this.myWrapper.getUser();
	}
	
	protected boolean isAllowed(String role, String projectID, int permission) throws DataException {
		User thisUser = this.getUser();
		return thisUser.isAllowed(role, projectID, permission);
	}
	
	public static String formatStringHTML(String aString) {
		if ( aString != null ) {
			return aString.replaceAll("[\n\r]+", "<BR>");
		} else {
			return "";
		}
	}
	
	protected String formatDate(Date aDate) {
		if ( aDate != null ) {
			SimpleDateFormat myFormat = this.dateFormat();
			return myFormat.format(aDate);
		}
		return "";
	}
	
	protected String strainLink(Strain aStrain) throws DataException {
		if ( aStrain == null ) { 
			return ""; 
		} else if ( aStrain.isLoaded() ) {
			return String.format("<A HREF='%s/strain?id=%s'>%s <I>%s</I></A>", this.myWrapper.getContextPath(), aStrain.getID(), aStrain.getID(), aStrain.getName());
		} else {
			return aStrain.getID();
		}
	}

	protected String sampleLink(Sample anObj) throws DataException {
		if ( anObj == null ) { 
			return ""; 
		} else if ( anObj.isLoaded() ) {
			return String.format("<A HREF='%s/sample?id=%s'>%s</A>", this.myWrapper.getContextPath(), anObj.getID(), anObj.getName());
		} else {
			return anObj.getID();
		}
	}
	
	protected String sampleColLink(SampleCollection anObj) throws DataException {
		if ( anObj == null ) {
			return "";
		} else if ( anObj.isLoaded() ) {
			return String.format("<A HREF='%s/sample?col=%s'>%s</A>", this.myWrapper.getContextPath(), anObj.getID(), anObj.getName());
		} else {
			return anObj.getID();
		}
	}

	protected String fieldCollectionLink(Collection anObj) throws DataException {
		if ( anObj == null ) {
			return "";
		} else if ( anObj.isLoaded() ) {
			return String.format("<A HREF='%s/collection?col=%s'>%s</A>", this.myWrapper.getContextPath(), anObj.getID(), anObj.getID());
		} else {
			return anObj.getID();
		}
	}

	protected String isolationLink(Isolation anObj) throws DataException {
		if ( anObj == null ) {
			return "";
		} else if ( anObj.isLoaded() ) {
			return String.format("<A HREF='%s/collection?id=%s'>%s</A>", this.myWrapper.getContextPath(), anObj.getID(), anObj.getID());
		} else {
			return anObj.getID();
		}
	}

	protected String message(String tag, String message) {
		return this.messageDiv(tag, message).toString();
	}
	
	protected Div messageDiv(String tag, String message) {
		Div messageDiv = new Div("<P ALIGN='CENTER'>");
		messageDiv.addItem(tag);
		messageDiv.addItem(message);
		messageDiv.addItem("</P>");
		if ( tag.equals(FAILED_TAG) || tag.equals(ERROR_TAG) )
			messageDiv.setClass("error");
		else
			messageDiv.setClass("messages");
		return messageDiv;
	}
	
	protected Popup sampleCollectionPopup() throws DataException {
		Popup aPop = new Popup();
		aPop.addItem("");
		List<String> libs = SQLSampleCollection.libraries(this.getSQLDataSource());
		Iterator<String> anIter = libs.iterator();
		String aLib;

		while ( anIter.hasNext() ) {
			aLib = anIter.next();
			PopupGroup aGroup = new PopupGroup(aLib);
			SampleCollection cols = SQLSampleCollection.loadForLibrary(this.getSQLDataSource(), aLib);
			if ( cols != null ) {
				cols.beforeFirst();
				while ( cols.next() ) {
					aGroup.addItemWithLabel(cols.getID(), cols.getName());
				} 
			}
			aPop.addGroup(aGroup);
		}
		return aPop;
	}
	
	protected Div viewDiv(String divID, String viewContent) {
		Div viewDiv = new Div(viewContent);
		viewDiv.setID(String.format("view_%s", divID));
		viewDiv.setClass("showSection");
		viewDiv.addItem(String.format("<P ALIGN='CENTER'><BUTTON TYPE='BUTTON' onClick='flipDiv(\"%s\")'>Edit Values</BUTTON></P>", divID));
		return viewDiv;	
	}
	
	protected Div editDiv(String divID, String editContent) {
		Div editDiv = new Div(editContent);
		editDiv.setID(String.format("edit_%s", divID));
		editDiv.setClass("hideSection");
		editDiv.addItem(String.format("<P ALIGN='CENTER'><BUTTON TYPE='BUTTON' onClick='flipDiv(\"%s\")'>Close Form</BUTTON></P>", divID));
		return editDiv;
	}
	
	public static String autoFormatAmount(BigDecimal amount, int type) {
		return CyanosObject.autoFormatAmount(amount, type);		
	}
	
	public static String autoFormatAmount(float amount, int sigfigs, int type) {
		return CyanosObject.autoFormatAmount(new BigDecimal(amount, new MathContext(sigfigs)), type);
	}
	
	public static String autoFormatAmountHTML(BigDecimal amount, int type) {
		return CyanosObject.autoFormatAmount(amount, type, CyanosObject.KILO_PREFIX, CyanosObject.MILLI_PREFIX, "&micro;");
	}

	
	// TODO need to fix to accommodate values larger than 26 (Z).  26+1 (AA) .. 26+26 (AZ) 
	public static String lettersForIndex(int index) {
		if ( index < 1 ) return "";
		double temp = Math.log(index) / Math.log(26);
		temp = Math.floor(temp) + 1;
		int length = (int) temp;
		char[] letters = new char[length];
		int i = length - 1;
		while ( index > 26 ) {
			int currI = ( index % 26 );
			index = index / 26;
			if ( currI == 0 ) { currI = 26; index--; }
			letters[i] = (char)(currI + 'A' - 1);
			i--;
		}
		letters[i] = (char)(index + 'A' - 1); 
		return String.valueOf(letters);
	}
	
}
