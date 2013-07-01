/**
 * 
 */
package edu.uic.orjala.cyanos.web.upload;

import java.sql.SQLException;
import java.util.List;
import java.util.ListIterator;

import edu.uic.orjala.cyanos.Collection;
import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.Role;
import edu.uic.orjala.cyanos.User;
import edu.uic.orjala.cyanos.sql.SQLCollection;
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
public class CollectionUpload extends UploadForm {

	private static final String HEADER = "header";
	private static final String FORCE_UPLOAD = "forceUpload";
	private static final String COLLECTION_ID = "collectionID";
	private static final String DATE = "date";
	private static final String COLLECTOR = "collector";
	private static final String LOCATION = "location";
	private static final String PRECISION = "precision";
	private static final String STATIC_PRECISION = "staticPrecision";
	private static final String LATITUDE = "latitude";
	private static final String LONGITUDE = "longitude";
	private static final String NOTES = "notes";
	private static final String PROJECT_COL = "projectCol";
	private static final String STATIC_PROJECT = "staticProject";

	public static final String TITLE = "Field Collection Data";
	
	public static final String[] templateKeys = { HEADER, FORCE_UPLOAD, COLLECTION_ID, DATE, COLLECTOR, LOCATION, 
			PRECISION, STATIC_PRECISION, LATITUDE, LONGITUDE, NOTES, PROJECT_COL, STATIC_PROJECT };
	private static final String[] templateHeader = {"Collection ID", "Date", "Collected by", "Location Name", "Latitude", "Longitude", "Lat/Long Precision", "Notes"};
	private static final String[] templateType = {"Required", "Optional", "Optional", "Optional", "Optional", "Optional", "Optional or Static", "Optional"};

	/**
	 * @param servlet
	 * @param aSheet
	 * @throws SQLException 
	 * @throws DataException 
	 */
	public CollectionUpload(CyanosWrapper aWrapper, Sheet aSheet) throws DataException {
		super(aWrapper, aSheet);
		this.accessRole = User.CULTURE_ROLE;
		this.permission = Role.CREATE;
		this.template = this.buildTemplate(templateKeys);
		this.hasHeaderRow = this.template.containsKey(HEADER);
	}

	public String worksheetTemplate() {
		return this.worksheetTemplate(templateHeader, templateType);
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
		
		/*
		 * "collectionID", "date", "collector", "location", "latitude", "longitude", "notes"
		 */
		
		fullRow.addItem(this.simpleTemplateRow("Collection ID:", COLLECTION_ID, ssColPop));
/*		myCell = new TableCell("ID Prefix:");
		String defaultUnit = new String("UIC");
		if ( template.containsKey("prefix")) { defaultUnit = (String)template.get("prefix"); }
		myCell.addItem(String.format("<INPUT TYPE='TEXT' SIZE=5 NAME='prefix' VALUE='%s'/>", defaultUnit));
		fullRow.addItem(myCell);
*/
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
		this.done = 0;
		this.todos = rowNum.size();
		this.working = true;
		// Setup the row iterator.
		ListIterator rowIter = rowNum.listIterator();
		HtmlList resultList = new HtmlList();
		resultList.unordered();

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
			Integer row = (Integer)rowIter.next();
			this.done++;
			if ( this.worksheet.gotoRow(row.intValue()) ) {
				HtmlList currResults = new HtmlList();
				currResults.unordered();
				String myID = this.worksheet.getValue(colIDCol);
				if ( myID.length() < 1 ) continue PARSE_ROW;
//				if ( prefix.length() > 0 )
//				myID = prefix + " " + myID;

				try {
					Collection aCol = new SQLCollection(this.myData, myID);
					boolean update = true;
					String myProject = staticProject;
					if ( useProjectCol ) myProject = this.worksheet.getValue(projectCol);
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
						if ( dateCol > -1 )
							aCol.setDate(this.worksheet.getValue(dateCol));
						if ( personCol > -1 )
							aCol.setCollector(this.worksheet.getValue(personCol));
						if ( locCol > -1 )
							aCol.setLocationName(this.worksheet.getValue(locCol));
						if ( latCol > -1 ) 
							aCol.setLatitude(this.worksheet.getValue(latCol));
						if ( longCol > -1 )
							aCol.setLongitude(this.worksheet.getValue(longCol));
						if ( notesCol > -1 )
							aCol.setNotes(this.worksheet.getValue(notesCol));
						if ( staticPrec ) {
							aCol.setPrecision(prec);
						} else if ( precCol > -1 ) {
							aCol.setPrecision(this.worksheet.getValue(precCol));
						}
						if ( useProjectCol ) aCol.setProjectID(this.worksheet.getValue(projectCol));
						else aCol.setProjectID(staticProject);
						aCol.refresh();
						aCol.setAutoRefresh();
						currResults.addItem(SUCCESS_TAG + "Information updated.");
					} else {
						currResults.addItem(FAILED_TAG + "Information update failed.");
					}
				} catch (DataException e) {
					currResults.addItem("<FONT COLOR='red'><B>SQL FAILURE</B></FONT> " + e.getMessage());
					e.printStackTrace();
				}
				resultList.addItem(String.format("Row #:%d %s", row, currResults.toString()));
			} else {

			}
		}
		output.append(resultList.toString());
		if ( ! this.working ) { output.append("<P ALIGN='CENTER'><B>EXECUTION HALTED</B> Upload incomplete!</P>"); }
		this.working = false;
		this.resultOutput = output.toString();
	}

	@Deprecated
	public UploadModule makeNew(CyanosWrapper aServlet, Sheet aSheet) throws DataException {
		return new CollectionUpload(aServlet, aSheet);
	}

	public String[] getTemplateKeys() {
		return templateKeys;
	}
}
