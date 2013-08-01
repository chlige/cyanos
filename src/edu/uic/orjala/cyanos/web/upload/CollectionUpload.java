/**
 * 
 */
package edu.uic.orjala.cyanos.web.upload;

import java.sql.SQLException;
import java.util.List;
import java.util.ListIterator;

import javax.servlet.http.HttpServletRequest;

import edu.uic.orjala.cyanos.Collection;
import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.Role;
import edu.uic.orjala.cyanos.User;
import edu.uic.orjala.cyanos.sql.SQLCollection;
import edu.uic.orjala.cyanos.web.SheetValue;
import edu.uic.orjala.cyanos.web.UploadForm;
import edu.uic.orjala.cyanos.web.html.HtmlList;

/**
 * @author George Chlipala
 *
 */
public class CollectionUpload extends UploadForm {

	public static final String FORCE_UPLOAD = "forceUpload";
	public static final String COLLECTION_ID = "collectionID";
	public static final String DATE = "date";
	public static final String COLLECTOR = "collector";
	public static final String LOCATION = "location";
	public static final String PRECISION = "precision";
	public static final String STATIC_PRECISION = "staticPrecision";
	public static final String LATITUDE = "latitude";
	public static final String LONGITUDE = "longitude";
	public static final String NOTES = "notes";
	public static final String PROJECT_COL = "projectCol";
	public static final String STATIC_PROJECT = "staticProject";
	
	public static final String PROTOCOL = "collection upload";

	public static final String TITLE = "Field Collection Data";

	public static final String[] templateKeys = { PARAM_HEADER, FORCE_UPLOAD, COLLECTION_ID, DATE, COLLECTOR, LOCATION, 
		PRECISION, STATIC_PRECISION, LATITUDE, LONGITUDE, NOTES, PROJECT_COL, STATIC_PROJECT };
	private static final String[] templateHeader = {"Collection ID", "Date", "Collected by", "Location Name", "Latitude", "Longitude", "Lat/Long Precision", "Notes"};
	private static final String[] templateType = {"Required", "Optional", "Optional", "Optional", "Optional", "Optional", "Optional or Static", "Optional"};

	public static final String JSP_FORM = "/upload/forms/collection.jsp";

	public CollectionUpload(HttpServletRequest req) {
		super(req);
		this.accessRole = User.CULTURE_ROLE;
		this.permission = Role.CREATE;
	}

	public String worksheetTemplate() {
		return this.worksheetTemplate(templateHeader, templateType);
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
			myCell = new TableCell("<INPUT TYPE='CHECKBOX' NAME='forceUpload' VALUE='true' CHECKED /> Force upload.<BR/> i.e. Overwrite existing collection information.");
		else 
			myCell = new TableCell("<INPUT TYPE='CHECKBOX' NAME='forceUpload' VALUE='true' /> Force upload.<BR/> i.e. Overwrite existing collection information.");
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
			String value = (String)headerList.get(i);
			ssColPop.addItemWithLabel(index, value);
			optionalPop.addItemWithLabel(index, value);
			staticPop.addItemWithLabel(index, value);
		}

		// "collectionID", "date", "collector", "location", "latitude", "longitude", "notes"

		fullRow.addItem(this.simpleTemplateRow("Collection ID:", COLLECTION_ID, ssColPop));
		//		myCell = new TableCell("ID Prefix:");
		//		String defaultUnit = new String("UIC");
		//		if ( template.containsKey("prefix")) { defaultUnit = (String)template.get("prefix"); }
		//		myCell.addItem(String.format("<INPUT TYPE='TEXT' SIZE=5 NAME='prefix' VALUE='%s'/>", defaultUnit));
		//		fullRow.addItem(myCell);

		fullRow.addItem(this.simpleTemplateRow("Date:", DATE, optionalPop));	
		fullRow.addItem(this.simpleTemplateRow("Collected By:", COLLECTOR, optionalPop));
		fullRow.addItem(this.simpleTemplateRow("Location Name:", LOCATION, optionalPop));
		fullRow.addItem(this.simpleTemplateRow("Latitude:", LATITUDE, optionalPop));
		fullRow.addItem(this.simpleTemplateRow("Longitude:", LONGITUDE, optionalPop));
		fullRow.addItem(this.templateRowWithStatic("Lat/Long Precision (m):", PRECISION, STATIC_PRECISION, staticPop));
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
		this.done = 0;
		this.todos = rowNum.size();
		this.working = true;
		// Setup the row iterator.
		ListIterator<Integer> rowIter = rowNum.listIterator();
		HtmlList resultList = new HtmlList();
		resultList.unordered();
		try {

			/*
			 * "collectionID", "date", "collector", "location", "latitude", "longitude", "notes"
			 */
			int colIDCol = Integer.parseInt((String)this.template.get(COLLECTION_ID));
			int dateCol = Integer.parseInt((String)this.template.get(DATE));
			int personCol = Integer.parseInt((String)this.template.get(COLLECTOR));
			int locCol = Integer.parseInt((String)this.template.get(LOCATION));
			int latCol = Integer.parseInt((String)this.template.get(LATITUDE));
			int longCol = Integer.parseInt((String)this.template.get(LONGITUDE));
			int notesCol = Integer.parseInt((String)this.template.get(NOTES));
			boolean safeUpload = (! this.template.containsKey(FORCE_UPLOAD));
			//		String prefix = (String)this.template.get("prefix");
			String prec = (String)this.template.get(STATIC_PRECISION);
			int precCol = Integer.parseInt((String)this.template.get(PRECISION));
			boolean staticPrec = ( precCol < 0 && prec.length() > 0);

			int projectCol = Integer.parseInt((String)this.template.get(PROJECT_COL));
			String staticProject = (String)this.template.get(STATIC_PROJECT);
			boolean useProjectCol = (projectCol > -1);

			PARSE_ROW: while ( rowIter.hasNext() && this.working ) {
				try {

					Integer row = (Integer)rowIter.next();
					this.done++;
					if ( this.worksheet.gotoRow(row.intValue()) ) {
						HtmlList currResults = new HtmlList();
						currResults.unordered();
						String myID = this.worksheet.getStringValue(colIDCol);
						if ( myID.length() < 1 ) continue PARSE_ROW;
						//				if ( prefix.length() > 0 )
						//				myID = prefix + " " + myID;

						try {
							Collection aCol = SQLCollection.load(this.myData, myID);
							boolean update = true;
							String myProject = staticProject;
							if ( useProjectCol ) myProject = this.worksheet.getStringValue(projectCol);
							if ( aCol.first() ) {
								currResults.addItem(FOUND_TAG + "Collection found.");
								if ( safeUpload ) {
									currResults.addItem(SKIP_TAG + "Information skipped: SAFE UPLOAD");
									update = false;
								} else {
									aCol.setProjectID(myProject);
								}
							} else {
								aCol = SQLCollection.createInProject(this.myData, myID, myProject);
								currResults.addItem(SUCCESS_TAG + "Created new collection record.");
							}

							if ( aCol.first() && update ) {
								aCol.setManualRefresh();
								if ( dateCol > -1 ) {
									SheetValue value = this.worksheet.getValue(dateCol);
									if ( value.isDate() ) {
										aCol.setDate(value.getDate());
									} else {
										aCol.setDate(value.toString());
									}
								}
								if ( personCol > -1 )
									aCol.setCollector(this.worksheet.getStringValue(personCol));
								if ( locCol > -1 )
									aCol.setLocationName(this.worksheet.getStringValue(locCol));
								if ( latCol > -1 ) 
									aCol.setLatitude(this.worksheet.getStringValue(latCol));
								if ( longCol > -1 )
									aCol.setLongitude(this.worksheet.getStringValue(longCol));
								if ( notesCol > -1 )
									aCol.setNotes(this.worksheet.getStringValue(notesCol));
								if ( staticPrec ) {
									aCol.setPrecision(prec);
								} else if ( precCol > -1 ) {
									aCol.setPrecision(this.worksheet.getStringValue(precCol));
								}
								if ( useProjectCol ) aCol.setProjectID(this.worksheet.getStringValue(projectCol));
								else aCol.setProjectID(staticProject);
								aCol.refresh();
								aCol.setAutoRefresh();
								currResults.addItem(SUCCESS_TAG + "Information updated.");
							} else {
								currResults.addItem(FAILED_TAG + "Information update failed.");
							}
						} catch (DataException e) {
							currResults.addItem("<FONT COLOR='red'><B>ERROR:</B></FONT> " + e.getMessage());
							e.printStackTrace();
						}
						resultList.addItem(String.format("Row #:%d %s", row, currResults.toString()));
					} else {
						//TODO If there is no row???
					}
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
