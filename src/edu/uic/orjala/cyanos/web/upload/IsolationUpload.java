/**
 * 
 */
package edu.uic.orjala.cyanos.web.upload;

import java.sql.SQLException;
import java.util.List;
import java.util.ListIterator;

import javax.servlet.http.HttpServletRequest;

import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.Isolation;
import edu.uic.orjala.cyanos.Role;
import edu.uic.orjala.cyanos.User;
import edu.uic.orjala.cyanos.sql.SQLIsolation;
import edu.uic.orjala.cyanos.web.SheetValue;
import edu.uic.orjala.cyanos.web.UploadForm;
import edu.uic.orjala.cyanos.web.html.HtmlList;

/**
 * @author George Chlipala
 *
 */
public class IsolationUpload extends UploadForm {
	
	public static final String PROTOCOL = "isolation upload";

	public static final String FORCE_UPLOAD = "forceUpload";
	public static final String ISOLATION_ID = "isolationID";
	public static final String COLLECTION_ID = "collectionID";
	public static final String DATE = "date";
	public static final String TYPE = "type";
	public static final String MEDIA = "media";
	public static final String NOTES = "notes";
	public static final String PARENT = "parent";
	public static final String PROJECT_COL = "projectCol";
	public static final String STATIC_PROJECT = "staticProject";

	public static final String TITLE = "Strain Isolation Data";

	public static final String[] templateKeys = { PARAM_HEADER, FORCE_UPLOAD, ISOLATION_ID, COLLECTION_ID, DATE, TYPE, 
		MEDIA, NOTES, PARENT, PROJECT_COL, STATIC_PROJECT };

	private static final String[] templateHeader = {"Isolation ID", "Collection ID", "Date", "Parent", "Type", "Media", "Notes", "Project Code"};
	private static final String[] templateType = {"Required", "Required", "Optional", "Optional", "Optional", "Optional", "Optional", "Optional or Static"};

	public static final String JSP_FORM = "/upload/forms/isolation.jsp";
	
	public IsolationUpload(HttpServletRequest req) {
		super(req);
		this.accessRole = User.CULTURE_ROLE;
		this.permission = Role.CREATE;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.upload.UploadForm#templateForm()
	 */
	/*
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
	/*
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
 */
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
		List<Integer> rowNum = this.rowList();
		// Setup the row iterator.
		this.done = 0;
		this.todos = rowNum.size();
		this.working = true;
		ListIterator<Integer> rowIter = rowNum.listIterator();
		HtmlList resultList = new HtmlList();
		resultList.unordered();

		try {
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
				try {
					Integer row = (Integer)rowIter.next();
					if ( this.worksheet.gotoRow(row.intValue()) ) {
						HtmlList currResults = new HtmlList();
						currResults.unordered();
						String isoID = this.worksheet.getStringValue(isoIDCol);
						if ( isoID.equals("") ) continue;
						//			if ( usePrefix )
						//				isoID = prefix + " " + isoID;
						try {
							Isolation anIso = new SQLIsolation(this.myData, isoID);

							boolean update = true;
							String myProject = null;
							if ( useProjectCol ) myProject = this.worksheet.getStringValue(projectCol);
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
								anIso = SQLIsolation.createInProject(this.myData, isoID, this.worksheet.getStringValue(colIDCol), myProject);
								currResults.addItem(SUCCESS_TAG + "Created new isolation record.");
							}

							if ( anIso.first() && update ) {
								anIso.setManualRefresh();
								if ( dateCol > -1 ) {
									SheetValue value = this.worksheet.getValue(dateCol);
									if ( value.isDate() ) {
										anIso.setDate(value.getDate());
									} else {
										anIso.setDate(value.toString());
									}
								}
								if ( typeCol > -1 )
									anIso.setType(this.worksheet.getStringValue(typeCol));
								if ( mediaCol > -1 )
									anIso.setMedia(this.worksheet.getStringValue(mediaCol));
								if ( notesCol > -1 )
									anIso.setNotes(this.worksheet.getStringValue(notesCol));
								if ( parentIDCol > -1 ) {
									//						if ( usePrefix )
									//							anIso.setParentID(prefix + " " + thisRow.get(parentIDCol));
									//						else 
									anIso.setParentID(this.worksheet.getStringValue(parentIDCol));
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
				} catch (Exception e) {
					output.append("<P ALIGN='CENTER'><B><FONT COLOR='red'>ERROR:</FONT>" + e.getMessage() + "</B></P>");
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			output.append("<P ALIGN='CENTER'><B><FONT COLOR='red'>ERROR:</FONT>" + e.getMessage() + "</B></P>");
			e.printStackTrace();
			this.working = false;
		}
		try {
			if ( this.working ) { this.myData.commit(); output.append("<P ALIGN='CENTER'><B>EXECUTION COMPLETE</B> CHANGES COMMITTED.</P>"); }
			else { this.myData.rollback(); output.append("<P ALIGN='CENTER'><B>EXECUTION HALTED</B> Upload incomplete!</P>"); }
		} catch (SQLException e) {
			output.append("<P ALIGN='CENTER'><B><FONT COLOR='red'>ERROR:</FONT>" + e.getMessage() + "</B></P>");
			e.printStackTrace();			
		}
		output.append(resultList.toString());
		this.working = false;
		this.resultOutput = output.toString();
	}

	public String worksheetTemplate() {
		return this.worksheetTemplate(templateHeader, templateType);
	}

	public String[] getTemplateKeys() {
		return templateKeys;
	}

	@Override
	public String jspForm() {
		return JSP_FORM;
	}

	public String getTemplateType() {
		return PROTOCOL;
	}


}
