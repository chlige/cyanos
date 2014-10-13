/**
 * 
 */
package edu.uic.orjala.cyanos.web;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspWriter;

import edu.uic.orjala.cyanos.AccessException;
import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.User;
import edu.uic.orjala.cyanos.sql.SQLData;
import edu.uic.orjala.cyanos.web.html.Table;
import edu.uic.orjala.cyanos.web.html.TableCell;
import edu.uic.orjala.cyanos.web.html.TableHeader;
import edu.uic.orjala.cyanos.web.html.TableRow;
import edu.uic.orjala.cyanos.web.servlet.ServletObject;
import edu.uic.orjala.cyanos.web.servlet.UploadServlet;

/**
 * Base UploadForm.  Provided for convenience.
 * 
 * @author George Chlipala
 *
 */
public abstract class UploadForm implements Runnable, UploadModule {
	
	public final static String ERROR_TAG = "<FONT COLOR='red'><B>ERROR:</FONT></B> ";
	public final static String FAILED_TAG = "<FONT COLOR='red'><B>FAILED:</FONT></B> ";
	public final static String WARNING_TAG = "<FONT COLOR='orange'><B>WARNING:</FONT></B> ";
	public final static String SUCCESS_TAG = "<FONT COLOR='green'><B>SUCCESS:</FONT></B> ";
	public final static String FOUND_TAG = "<FONT COLOR='blue'><B>FOUND:</FONT></B> ";
	public final static String SKIP_TAG = "<FONT COLOR='purple'><B>SKIPPED:</FONT></B> ";
	public final static String NOTICE_TAG = "<FONT COLOR='gray'><B>NOTICE:</FONT></B> ";

	private static final String THREAD_LABEL = "PARSER";
	protected Thread parseThread = null;
	protected Sheet worksheet = null;
	protected Map<String,String> template = new HashMap<String,String>();
	protected int todos = 0;
	protected int done = 0;	
	protected boolean working = false;
	protected SQLData myData = null;
	protected String myURL = null;
	protected boolean hasHeaderRow = true;
	protected List<Integer> rowList = new ArrayList<Integer>();
	protected List<String> columns = null;
	
	protected int rowBehavior = ROW_BEHAVIOR_IGNORE;	
	
	protected String resultOutput = null;
	protected Sheet resultSheet = null;
	
	protected String accessRole = null;
	protected int permission = 0;
	
	protected Savepoint savepoint = null;

	public static final String PARAM_HEADER = "header";
	public static final String PARAM_ROW_BEHAVIOR = "behavior";
	public static final String PARAM_ROWS = "rows";
		
	public UploadForm(HttpServletRequest req) {
		this.worksheet = this.getActiveWorksheet(req);
		if (req.getParameter(UploadServlet.WORKSHEET_PARAM) == null ) {
			this.template.put(PARAM_HEADER, "");
		}
		this.updateTemplate(req);
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.upload.UploadModule#status()
	 */
	public float status() {
		Float doneFL = new Float(done);
		Float todosFL = new Float(todos);
		return doneFL / todosFL;
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.upload.UploadModule#isDone()
	 */
	public boolean isDone() {
		return ( todos == done ) && (! working);
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.upload.UploadModule#isWorking()
	 */
	public boolean isWorking() {
		return working;
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.upload.UploadModule#worksheetTemplate()
	 */
	public abstract String worksheetTemplate();
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.upload.UploadModule#title()
	 */
	public abstract String title();
	
	/**
	 * Return the relative path of JSP file.  Path should be relative to directory of the deployed CYANOS web application.
	 * 
	 * @return String location of JSP form
	 */
	public abstract String jspForm();
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.upload.UploadModule#getTemplate()
	 */
	public Map<String,String> getTemplate() {
		return this.template;
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.upload.UploadModule#setTemplate(java.util.Map)
	 */
	public void setTemplate(Map<String,String> aTemplate) {
		this.template = aTemplate;
	}
	
	@Override
	public List<String> getHeaderList() {
		return columns;
	}
	
	public void genOptions(JspWriter out, String column) throws IOException {
		int sel = -1;
		if ( this.template.containsKey(column) ) {
			sel = Integer.parseInt(this.template.get(column));
		}
		if ( sel < 0 && this.hasHeaderRow ) {		
			for (int i = 0; i < columns.size(); i++ ) {
				if ( column.equalsIgnoreCase(columns.get(i)));
			}
		}
		
		for ( int i = 0; i < columns.size(); i++ ) {
			out.print("<option value=\"");
			out.print(i);
			out.print("\"");
			String thisColumn = columns.get(i); 
			if ( i == sel ) {
				out.print(" selected");
			} else if ( sel < 0 && this.hasHeaderRow ) {
				if ( column.equalsIgnoreCase(thisColumn) ) {
					out.print(" selected");					
				}
			}
			out.print(">");
			out.print(thisColumn);
			out.println("</option>");
		}
	}
	
	protected List<String> getHeaderList(boolean internalHeaders) {
		int maxColumns = this.worksheet.columnCount();
		this.worksheet.firstRow();
		int extraCols = 0;
		List<String> headers = new ArrayList<String>();
		if ( internalHeaders ) {
			extraCols = this.worksheet.rowSize();
			this.worksheet.beforeFirstColumn();
			while ( this.worksheet.nextCellInRow() ) {
				headers.add(this.worksheet.getStringValue());
			}
		}
		for ( int i = extraCols; i < maxColumns; i++ ) {
			headers.add(String.format("Col: %d", i + 1));
		}
		return headers;
	}
	
	/*
	protected TableCell simpleTemplateRow(String label, String name, Popup colPop) {
		TableCell myCell = new TableCell(label);
		colPop.setName(name);
		if ( this.template.containsKey(name) ) {
			colPop.setDefault((String)this.template.get(name));
		}
		myCell.addItem(colPop.toString());
		return myCell;
	}
	
	protected TableCell templateRowWithUnit(String label, String name, String unitName, Popup colPop) {
		return this.templateRowWithUnit(label,name,unitName, colPop, "mg");
	}
	
	protected TableCell templateRowWithUnit(String label, String name, String unitName, Popup colPop, String defaultUnit) {
		TableCell myCell = new TableCell(label);
		colPop.setName(name);
		if ( this.template.containsKey(name) ) {
			colPop.setDefault((String)this.template.get(name));
		}
		if ( template.containsKey(unitName)) { defaultUnit = (String)template.get(unitName); }
		myCell.addItem(String.format("%s Default unit: <INPUT TYPE='TEXT' SIZE=5 NAME='%s' VALUE='%s'/>", colPop.toString(), unitName, defaultUnit));
		return myCell;
	}
	
	protected TableCell templateRowWithStatic(String label, String name, String staticName, Popup colPop) {
		TableCell myCell = new TableCell(label);
		colPop.setName(name);
		if ( this.template.containsKey(name) ) {
			colPop.setDefault((String)this.template.get(name));
		}
		String defaultValue = "";
		if ( template.containsKey(staticName)) { defaultValue = (String)template.get(staticName); }
		myCell.addItem(String.format("%s <INPUT TYPE='TEXT' SIZE=15 NAME='%s' VALUE='%s'/>", colPop.toString(), staticName, defaultValue));
		return myCell;
	}
	
	protected TableCell protocolTemplateRow(String label, String name, String protocol ) {
		TableCell myCell = new TableCell(label);
		try {
			Popup myPop = this.protocolPopup(protocol);
			myPop.setName(name);
			if ( this.template.containsKey(name) ) myPop.setDefault((String)this.template.get(name));
			myCell.addItem(myPop);
		} catch (DataException e) {
			myCell.addItem("<B><FONT COLOR='red'>ERROR:</FONT> " + e.getMessage() + "</B>");
		}
		return myCell;
	}
	
	protected TableCell projectTemplateRow(String label, String name, Popup colPop, String staticName ) {
		TableCell myCell = new TableCell(label);
		colPop.setName(name);
		if ( this.template.containsKey(name) ) colPop.setDefault((String)this.template.get(name));
		try {
			Popup projectPop = this.projectPopup();
			projectPop.setName(staticName);
			if ( this.template.containsKey(staticName) ) projectPop.setDefault((String)this.template.get(staticName));
			myCell.addItem(colPop.toString() + " " + projectPop.toString());
		} catch (DataException e) {
			myCell.addItem("<B><FONT COLOR='red'>ERROR:</FONT> " + e.getMessage() + "</B>");
		}
		return myCell;
	}

	
	protected TableCell templateToRow(String label, String start, String end, Popup ssColPop) {
		TableCell myCell = new TableCell(label);
		ssColPop.setName(start);
		if ( this.template.containsKey(start) ) {
			ssColPop.setDefault((String)this.template.get(start));
		}
		StringBuffer content = new StringBuffer(ssColPop.toString());
		content.append(" to ");
		ssColPop.setName(end);
		if ( this.template.containsKey(end) ) {
			ssColPop.setDefault((String)this.template.get(end));
		}
		content.append(ssColPop.toString());
		myCell.addItem(content);
		return myCell;
	}
	*/
	private void setupRowList(HttpServletRequest req) { 
		if ( ! ( req.getParameter(PARAM_ROW_BEHAVIOR) == null && req.getParameter(PARAM_ROWS) == null ) ) {
			this.setupRowList(req.getParameterValues(PARAM_ROWS));
		}
	}
	
	private void setupRowList(String[] rows) {
		this.rowList.clear();

		if ( rows != null ) {
			for ( int i = 0; i < rows.length; i++ ) {
				this.rowList.add(Integer.valueOf(rows[i]));
			}
			Collections.sort(this.rowList);
		}

		if ( this.rowBehavior == ROW_BEHAVIOR_IGNORE ) {
			List<Integer> newRowNum = new ArrayList<Integer>();
			int startNum = ( this.hasHeaderRow ? 1 : 0 );
			for (int i = startNum; i < this.worksheet.rowCount(); i++ ) {
				if ( ! this.rowList.contains(i) ) {
					newRowNum.add(Integer.valueOf(i));
				}
			}
			this.rowList.clear();
			this.rowList.addAll(newRowNum);
		}
	}
	
	protected List<Integer> rowList() {
		return this.rowList;
	}

	public void updateTemplate(HttpServletRequest req) {
		String[] keys = getTemplateKeys();
		for (int i = 0; i < keys.length; i++ ) {
			if ( req.getParameter(keys[i]) != null ) {
				template.put(keys[i], req.getParameter(keys[i]));
			}
		}
		
		if (req.getParameter(UploadServlet.WORKSHEET_PARAM) != null ) {
			if ( req.getParameter(PARAM_HEADER) == null )
				this.template.remove(PARAM_HEADER);
			this.hasHeaderRow = this.template.containsKey(PARAM_HEADER);
		}
		
		this.worksheet = this.getActiveWorksheet(req);
		if ( this.worksheet != null ) {
			this.columns = this.getHeaderList(this.hasHeaderRow);
		}
		
		if ( req.getParameter(PARAM_ROW_BEHAVIOR) != null ) {
			this.rowBehavior = Integer.parseInt(req.getParameter(PARAM_ROW_BEHAVIOR));
		}
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.upload.UploadModule#startParse()
	 */
	public void startParse(HttpServletRequest req, SQLData data) {
		if ( this.parseThread == null ) {
			this.myData = data;
			this.myURL = req.getRequestURI();
			
			this.updateTemplate(req);
			this.setupRowList(req);
			
			try {
				this.savepoint = this.myData.setSavepoint();
				this.parseThread = new Thread(this, THREAD_LABEL);
				this.parseThread.start();
			} catch (SQLException e) {
				this.resultOutput = "<P ALIGN='CENTER'><B><FONT COLOR='red'>ERROR:</FONT>" + e.getMessage() + "</B></P>";
				e.printStackTrace();
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.upload.UploadModule#stopParse()
	 */
	public void stopParse() {
		if ( this.parseThread != null ) {
			this.parseThread = null;
			this.working = false;
		}
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.upload.UploadModule#resultReport()
	 */
	public String resultReport() {
		return this.resultOutput;
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.upload.UploadModule#resultSheet()
	 */
	public Sheet resultSheet() {
		return this.resultSheet;
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.upload.UploadModule#hideTemplateValues()
	 */
	public String hideTemplateValues() {
		if ( this.template != null ) {
			StringBuffer output = new StringBuffer();
			Iterator<String> anIter = this.template.keySet().iterator();
			while ( anIter.hasNext() ) {
				String aKey = (String)anIter.next();
				output.append(String.format("<INPUT TYPE='HIDDEN' NAME='%s' VALUE='%s'/>", 
						aKey, (String)template.get(aKey)));
			}
			return output.toString();
		}
		return null;
	}
	
	protected String hideTemplateValues(String[] templateKeys) {
		StringBuffer output = new StringBuffer();
		for (int i = 0; i < templateKeys.length; i++ ) {
			if ( template.containsKey(templateKeys[i]) ) {
			output.append(String.format("<INPUT TYPE='HIDDEN' NAME='%s' VALUE='%s'/>", templateKeys[i], (String)template.get(templateKeys[i]))); 
			}
		}						
		return output.toString();
	}

	protected String worksheetTemplate(String[] headers, String[] types) {
		TableCell myCell = new TableHeader(headers);
		TableRow aRow = new TableRow(myCell);
		myCell = new TableCell(types);
		aRow.addItem(myCell);
		aRow.setAttribute("align", "center");
		Table myTable = new Table(aRow);
		myTable.setAttribute("BORDER","1");
		myTable.setAttribute("ALIGN", "CENTER");
		return myTable.toString();
	}
	
	public void close() throws DataException {
		this.myData.close();
		this.myData.closeDBC();
	}
	
	protected SQLData getSQLDataSource() throws DataException {
		return this.myData;
	}

	public boolean isAllowed(HttpServletRequest req) throws DataException {
		try {
			User myUser = UploadServlet.getUser(req);		
			//		HttpSession session = req.getSession();
			//		User myUser = (User) session.getAttribute(ServletObject.SESS_ATTR_USER);
			return myUser.isAllowed(this.accessRole, User.NULL_PROJECT, this.permission);
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}
	
	@Override
	public Sheet getActiveWorksheet() {
		return this.worksheet;
	}
	
	protected Sheet getActiveWorksheet(HttpServletRequest req) {
		HttpSession session = req.getSession();
		SpreadSheet aWKS = (SpreadSheet)session.getAttribute(UploadServlet.SPREADSHEET);
		if ( aWKS != null ) {
			String wksParam = req.getParameter(UploadServlet.WORKSHEET_PARAM);
			if ( wksParam != null && (! wksParam.equals("") ) ) {
				int wksTab = Integer.parseInt(wksParam);
				return (Sheet)aWKS.getSheet(wksTab);
			} else {
				return aWKS.getSheet(0);
			}
		}
		return null;
	}

	protected String handleException(Exception e) {
		if ( AccessException.class.isAssignableFrom(e.getClass()))
			System.err.println(String.format("[%s] %s", this.getClass().getName(), e.getLocalizedMessage()));
		else
			e.printStackTrace();
		return String.format("<DIV CLASS='error'><P><B><FONT COLOR='red'>ERROR:</FONT> %s</B></P></DIV>", e.getLocalizedMessage());
	}

	public void clearResults() {
		this.resultOutput = null;
		this.resultSheet = null;
	}
	
	public boolean hasHeaderRow() {
		return this.hasHeaderRow;
	}
	
	public int getRowBehavior() {
		return this.rowBehavior;
	}
}
