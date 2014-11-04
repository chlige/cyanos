/**
 * 
 */
package edu.uic.orjala.cyanos.web.upload;

import java.sql.SQLException;
import java.util.ListIterator;

import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.Isolation;
import edu.uic.orjala.cyanos.sql.SQLData;
import edu.uic.orjala.cyanos.sql.SQLIsolation;
import edu.uic.orjala.cyanos.web.SheetValue;
import edu.uic.orjala.cyanos.web.html.HtmlList;

/**
 * @author George Chlipala
 *
 */
public class IsolationUpload extends UploadJob {
	
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

	public static final String[] templateKeys = { FORCE_UPLOAD, ISOLATION_ID, COLLECTION_ID, DATE, TYPE, 
		MEDIA, NOTES, PARENT, PROJECT_COL, STATIC_PROJECT };
	
	public IsolationUpload(SQLData data) {
		super(data);
		this.type = TITLE;
	}


	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		if ( this.working ) return;
		// Setup the row iterator.
		this.done = 0;
		this.todos = this.rowList.size();
		this.working = true;
		ListIterator<Integer> rowIter = this.rowList.listIterator();
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
							Isolation anIso = SQLIsolation.load(this.myData, isoID);

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
					this.messages.append("<P ALIGN='CENTER'><B><FONT COLOR='red'>ERROR:</FONT>" + e.getMessage() + "</B></P>");
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			this.messages.append("<P ALIGN='CENTER'><B><FONT COLOR='red'>ERROR:</FONT>" + e.getMessage() + "</B></P>");
			e.printStackTrace();
			this.working = false;
		}
		try {
			if ( this.working ) { this.myData.commit(); this.messages.append("<P ALIGN='CENTER'><B>EXECUTION COMPLETE</B> CHANGES COMMITTED.</P>"); }
			else { this.myData.rollback(); this.messages.append("<P ALIGN='CENTER'><B>EXECUTION HALTED</B> Upload incomplete!</P>"); }
			this.myData.close();
		} catch (DataException e) {
			this.messages.append("<P ALIGN='CENTER'><B><FONT COLOR='red'>ERROR:</FONT>" + e.getMessage() + "</B></P>");
			e.printStackTrace();			
		} catch (SQLException e) {
			this.messages.append("<P ALIGN='CENTER'><B><FONT COLOR='red'>ERROR:</FONT>" + e.getMessage() + "</B></P>");
			e.printStackTrace();			
		}
		this.messages.append(resultList.toString());
		this.working = false;
	}

	public String[] getTemplateKeys() {
		return templateKeys;
	}
}
