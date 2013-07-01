/**
 * 
 */
package edu.uic.orjala.cyanos.web.upload;

import java.sql.SQLException;
import java.util.List;
import java.util.ListIterator;

import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.Isolation;
import edu.uic.orjala.cyanos.Role;
import edu.uic.orjala.cyanos.User;
import edu.uic.orjala.cyanos.sql.SQLIsolation;
import edu.uic.orjala.cyanos.web.CyanosWrapper;
import edu.uic.orjala.cyanos.web.Sheet;
import edu.uic.orjala.cyanos.web.UploadForm;
import edu.uic.orjala.cyanos.web.UploadModule;
import edu.uic.orjala.cyanos.web.html.HtmlList;
import edu.uic.orjala.cyanos.web.html.Popup;
import edu.uic.orjala.cyanos.web.html.Table;
import edu.uic.orjala.cyanos.web.html.TableCell;
import edu.uic.orjala.cyanos.web.html.TableRow;

/**
 * @author George Chlipala
 *
 */
public class IsolationUpload extends UploadForm {

	private static final String HEADER = "header";
	private static final String FORCE_UPLOAD = "forceUpload";
	private static final String ISOLATION_ID = "isolationID";
	private static final String COLLECTION_ID = "collectionID";
	private static final String DATE = "date";
	private static final String TYPE = "type";
	private static final String MEDIA = "media";
	private static final String NOTES = "notes";
	private static final String PARENT = "parent";
	private static final String PROJECT_COL = "projectCol";
	private static final String STATIC_PROJECT = "staticProject";

	public static final String TITLE = "Strain Isolation Data";
	
	public static final String[] templateKeys = { HEADER, FORCE_UPLOAD, ISOLATION_ID, COLLECTION_ID, DATE, TYPE, 
		MEDIA, NOTES, PARENT, PROJECT_COL, STATIC_PROJECT };
	
	private static final String[] templateHeader = {"Isolation ID", "Collection ID", "Date", "Parent", "Type", "Media", "Notes", "Project Code"};
	private static final String[] templateType = {"Required", "Required", "Optional", "Optional", "Optional", "Optional", "Optional", "Optional or Static"};

	/**
	 * @param aWrapper
	 * @param aSheet
	 * @throws SQLException 
	 * @throws DataException 
	 */
	public IsolationUpload(CyanosWrapper aWrapper, Sheet aSheet) throws DataException {
		super(aWrapper, aSheet);
		this.template = this.buildTemplate(templateKeys);
		this.hasHeaderRow = this.template.containsKey(HEADER);
		this.accessRole = User.CULTURE_ROLE;
		this.permission = Role.CREATE;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.upload.UploadForm#templateForm()
	 */
	public String templateForm() {
		TableCell myCell;
		List<String> headerList = this.getHeaderList(template.containsKey(HEADER));
		
		if ( template.containsKey(HEADER) ) {
			myCell = new TableCell("<INPUT TYPE='CHECKBOX' NAME='header' VALUE='true' onClick='this.form.submit()' CHECKED /> Spreadsheet has a header row.");					
		} else {
			myCell = new TableCell("<INPUT TYPE='CHECKBOX' NAME='header' VALUE='true' onClick='this.form.submit()' /> Spreadsheet has a header row.");
		}
		myCell.setAttribute("COLSPAN", "2");
		myCell.setAttribute("ALIGN", "CENTER");
		TableRow fullRow = new TableRow(myCell);
		
		if ( template.containsKey(FORCE_UPLOAD) )
			myCell = new TableCell("<INPUT TYPE='CHECKBOX' NAME='forceUpload' VALUE='true' CHECKED /> Force upload.<BR/> i.e. Overwrite existing isolation information.");
		else 
			myCell = new TableCell("<INPUT TYPE='CHECKBOX' NAME='forceUpload' VALUE='true' /> Force upload.<BR/> i.e. Overwrite existing isolation information.");
		myCell.setAttribute("COLSPAN", "2");
		myCell.setAttribute("ALIGN", "CENTER");
		fullRow.addItem(myCell);

		Popup ssColPop = new Popup();
		Popup optionalPop = new Popup();
		Popup staticPop = new Popup();
		optionalPop.addItemWithLabel("-1", "SKIP ITEM");
		staticPop.addItemWithLabel("-1", "Use Value ->");
		for ( int i = 0; i < headerList.size(); i++ ) {
			String index = String.valueOf(i);
			String value = headerList.get(i);
			ssColPop.addItemWithLabel(index, value);
			optionalPop.addItemWithLabel(index, value);
			staticPop.addItemWithLabel(index, value);
		}
		
		/*
		 * "isolationID", "date", "type", "media", "notes", "parent"
		 */
		
/*		myCell = new TableCell("ID Prefix:");
		String defaultUnit = new String("UIC");
		if ( template.containsKey("prefix")) { defaultUnit = template.get("prefix"); }
		myCell.addItem(String.format("<INPUT TYPE='TEXT' SIZE=5 NAME='prefix' VALUE='%s'/>", defaultUnit));
		fullRow.addItem(myCell);
*/
		fullRow.addItem(this.simpleTemplateRow("Isolation ID:", ISOLATION_ID, ssColPop));	
		fullRow.addItem(this.simpleTemplateRow("Collection ID:", COLLECTION_ID, ssColPop));
		fullRow.addItem(this.simpleTemplateRow("Parent ID:", PARENT, optionalPop));

		fullRow.addItem(this.simpleTemplateRow("Date:", DATE, optionalPop));	
		fullRow.addItem(this.simpleTemplateRow("Type:", TYPE, optionalPop));
		fullRow.addItem(this.simpleTemplateRow("Media:", MEDIA, optionalPop));
		fullRow.addItem(this.projectTemplateRow("Project Code:", PROJECT_COL, staticPop, STATIC_PROJECT));
		fullRow.addItem(this.simpleTemplateRow("Notes:", NOTES, optionalPop));
		
		Table formTable = new Table(fullRow);
		formTable.setAttribute("WIDTH", "85%");
		
		return formTable.toString();
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.upload.UploadForm#title()
	 */
	public String title() {
		return TITLE;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		if ( this.working ) return;
		StringBuffer output = new StringBuffer();
		List rowNum = this.rowList();
		// Setup the row iterator.
		this.done = 0;
		this.todos = rowNum.size();
		this.working = true;
		ListIterator rowIter = rowNum.listIterator();
		HtmlList resultList = new HtmlList();
		resultList.unordered();

		/*
		 *	"isolationID", "date", "type", "media", "notes", "parent"
		 */
		int isoIDCol = Integer.parseInt(template.get(ISOLATION_ID));
		int colIDCol = Integer.parseInt(this.template.get(COLLECTION_ID));
		int dateCol = Integer.parseInt(this.template.get(DATE));
		int typeCol = Integer.parseInt(this.template.get(TYPE));
		int mediaCol = Integer.parseInt(this.template.get(MEDIA));
		int notesCol = Integer.parseInt(this.template.get(NOTES));
		int parentIDCol = Integer.parseInt(this.template.get(PARENT));
		boolean safeUpload = (! this.template.containsKey(FORCE_UPLOAD));
//		String prefix = this.template.get("prefix");
//		boolean usePrefix = ( prefix.length() > 0 ); 
		
		int projectCol = Integer.parseInt(this.template.get(PROJECT_COL));
		String staticProject = this.template.get(STATIC_PROJECT);
		boolean useProjectCol = ( projectCol > -1 );

		while (rowIter.hasNext() && this.working) {
			Integer row = (Integer)rowIter.next();
			if ( this.worksheet.gotoRow(row.intValue()) ) {
			HtmlList currResults = new HtmlList();
			currResults.unordered();
			String isoID = this.worksheet.getValue(isoIDCol);
			if ( isoID.equals("") ) continue;
//			if ( usePrefix )
//				isoID = prefix + " " + isoID;
			try {
				Isolation anIso = new SQLIsolation(this.myData, isoID);
				
				boolean update = true;
				String myProject = null;
				if ( useProjectCol ) myProject = this.worksheet.getValue(projectCol);
				else myProject = staticProject;

				if ( anIso.first() ) {
					currResults.addItem(FOUND_TAG + "Isolation found.");
					if ( safeUpload ) {
						currResults.addItem(SKIP_TAG + "Information skipped: SAFE UPLOAD");
						update = false;
					} else {
						anIso.setProjectID(myProject);
					}
				} else {
					anIso = SQLIsolation.createInProject(this.myData, isoID, this.worksheet.getValue(colIDCol), myProject);
					currResults.addItem(SUCCESS_TAG + "Created new isolation record.");
				}
				
				if ( anIso.first() && update ) {
					anIso.setManualRefresh();
					if ( dateCol > -1 )
						anIso.setDate(this.worksheet.getValue(dateCol));
					if ( typeCol > -1 )
						anIso.setType(this.worksheet.getValue(typeCol));
					if ( mediaCol > -1 )
						anIso.setMedia(this.worksheet.getValue(mediaCol));
					if ( notesCol > -1 )
						anIso.setNotes(this.worksheet.getValue(notesCol));
					if ( parentIDCol > -1 ) {
//						if ( usePrefix )
//							anIso.setParentID(prefix + " " + thisRow.get(parentIDCol));
//						else 
							anIso.setParentID(this.worksheet.getValue(parentIDCol));
					}
					anIso.refresh();
					anIso.setAutoRefresh();
					currResults.addItem(SUCCESS_TAG + "Information updated.");
				} else {
					currResults.addItem(FAILED_TAG + "Information update failed.");
				}
			} catch (DataException e) {
				currResults.addItem("<FONT COLOR='red'><B>SQL FAILURE</B></FONT> " + e.getMessage());
				e.printStackTrace();
			}
			resultList.addItem(String.format("Row #:%d %s", row, currResults.toString()));
			}
			this.done++;
		}
		output.append(resultList.toString());
		
	}

	public String worksheetTemplate() {
		return this.worksheetTemplate(templateHeader, templateType);
	}

	@Deprecated
	public UploadModule makeNew(CyanosWrapper aServlet, Sheet aSheet) throws DataException {
		return new IsolationUpload(aServlet, aSheet);
	}

	public String[] getTemplateKeys() {
		return templateKeys;
	}
	
}
