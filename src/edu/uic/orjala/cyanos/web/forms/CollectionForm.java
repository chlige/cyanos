/**
 * 
 */
package edu.uic.orjala.cyanos.web.forms;

import java.text.SimpleDateFormat;
import java.util.Map;

import edu.uic.orjala.cyanos.Collection;
import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.Project;
import edu.uic.orjala.cyanos.Role;
import edu.uic.orjala.cyanos.sql.SQLCollection;
import edu.uic.orjala.cyanos.web.BaseForm;
import edu.uic.orjala.cyanos.web.CyanosConfig;
import edu.uic.orjala.cyanos.web.CyanosWrapper;
import edu.uic.orjala.cyanos.web.html.Div;
import edu.uic.orjala.cyanos.web.html.Form;
import edu.uic.orjala.cyanos.web.html.Paragraph;
import edu.uic.orjala.cyanos.web.html.Popup;
import edu.uic.orjala.cyanos.web.html.StyledText;
import edu.uic.orjala.cyanos.web.html.Table;
import edu.uic.orjala.cyanos.web.html.TableCell;
import edu.uic.orjala.cyanos.web.html.TableHeader;
import edu.uic.orjala.cyanos.web.html.TableRow;

/**
 * @author gchlip2
 *
 */
public class CollectionForm extends BaseForm {

	private static final String COLLECTION_DIV_ID = "collection";
	
	private static final String COLLECTION_PROJECT_FIELD = "project";
	private static final String COLLECTION_GEO_PRECISION_FIELD = "prec";
	private static final String COLLECTION_NOTES_FIELD = "notes";
	private static final String COLLECTION_LONGITUDE_FIELD = "long";
	private static final String COLLECTION_LATITUDE_FIELD = "lat";
	private static final String COLLECTION_LOCATION_FIELD = "location";
	private static final String COLLECTION_COLLECTOR_FIELD = "collector";
	private static final String COLLECTION_DATE_FIELD = "date";
	public final static String CREATE_COLLECTION_ACTION = "makeCollection";
	private final static String CREATE_COLLECTION_BUTTONS = String.format("<INPUT TYPE=SUBMIT NAME='%s' VALUE='Create'/><INPUT TYPE=RESET />", CREATE_COLLECTION_ACTION);
	
	/**
	 * @param callingServlet
	 */
	public CollectionForm(CyanosWrapper callingServlet) {
		super(callingServlet);
	}
	
	/**
         * Generate a HTML table for the specified collections.
	 *
	 * @param colList A Collection object containing all collections to list.
	 * @return List of the collection as a HTML table.
	 */
	public String collectionList(Collection colList) {
		try {
			colList.beforeFirst();
			String headers[] = { "ID", "Date", "Location", "Coordinates", "Collector", "Notes" };
			TableCell myCell = new TableHeader(headers);
			TableRow tableRow = new TableRow(myCell);
			SimpleDateFormat myDateFormat = this.dateFormat();
			boolean oddRow = true;
			Table myTable = new Table(tableRow);
			myTable.setClass("species");
			myTable.setAttribute("align", "center");
			
			while (colList.next()) {
				myCell = new TableCell(this.fieldCollectionLink(colList));
				myCell.addItem(myDateFormat.format(colList.getDate()));
				myCell.addItem(colList.getLocationName());
				myCell.addItem(colList.getLatitudeDM() + "<BR/>" + colList.getLongitudeDM());
				myCell.addItem(colList.getCollector());
				myCell.addItem(BaseForm.shortenString(colList.getNotes(), 75));
				TableRow aRow = new TableRow(myCell);
				if ( oddRow ) {
					aRow.setClass("odd");							
				} else {
					aRow.setClass("even");							
				}
				oddRow = (! oddRow);
				aRow.setAttribute("align", "center");
				myTable.addItem(aRow);
			}
			return myTable.toString();
		} catch (DataException e) {
			return this.handleException(e);
		}
	}

	/**
	 * Generate a HTML form for adding collection data.
	 *
	 * @return HTML form as String.
	 */
	public String addCollectionForm() {
		StringBuffer output = new StringBuffer();

		Paragraph head = new Paragraph();
		head.setAlign("CENTER");
		StyledText title = new StyledText("Add a New Collection");
		title.setSize("+3");
		head.addItem(title);
		head.addItem("<HR WIDTH='85%'/>");
		output.append(head);

		TableRow tableRow = new TableRow(this.makeFormTextRow("ID:", "col_id"));				
		tableRow.addItem(this.makeFormDateRow("Date:", COLLECTION_DATE_FIELD, "colForm"));

		TableCell myCell = new TableCell("Project:");
		try {
			Popup projectPop = this.projectPopup();
			projectPop.setName(COLLECTION_PROJECT_FIELD);
			if ( this.hasFormValue(COLLECTION_PROJECT_FIELD) )
				projectPop.setDefault(this.getFormValue(COLLECTION_PROJECT_FIELD));
			myCell.addItem(projectPop.toString());
		} catch (DataException e) {
			myCell.addItem("<B><FONT COLOR='RED'>ERROR: </FONT>" + e.getMessage() + "</B>");
		}
		tableRow.addItem(myCell);

		tableRow.addItem(this.makeFormTextRow("Collected by:", COLLECTION_COLLECTOR_FIELD));

		TableRow miniRow;
		miniRow = new TableRow("<TD COLSPAN='2' ALIGN='LEFT'><B><I>Location</B></I>");
		miniRow.addItem(this.makeFormTextRow("Name:", COLLECTION_LOCATION_FIELD));
		myCell = new TableCell("Coordinates:");
		String latValue = "", longValue = "";
		if ( this.hasFormValue(COLLECTION_LATITUDE_FIELD)) latValue = this.getFormValue(COLLECTION_LATITUDE_FIELD);
		if ( this.hasFormValue(COLLECTION_LONGITUDE_FIELD)) longValue = this.getFormValue(COLLECTION_LONGITUDE_FIELD);
		myCell.addItem(String.format("<INPUT TYPE='TEXT' SIZE='10' NAME='lat' VALUE=\"%s\"/> <INPUT TYPE='TEXT' SIZE='10' NAME='long' VALUE=\"%s\"/>", latValue, longValue));
		miniRow.addItem(myCell);
		miniRow.addItem(this.makeFormTextRow("Precision (m):", COLLECTION_GEO_PRECISION_FIELD));
		Table miniTable = new Table(miniRow);
		myCell = new TableCell(miniTable);
		myCell.setAttribute("COLSPAN", "2");
		tableRow.addItem(myCell);

		tableRow.addItem(this.makeFormTextAreaRow("Notes:", COLLECTION_NOTES_FIELD));
		myCell = new TableCell(CREATE_COLLECTION_BUTTONS);
		myCell.setAttribute("colspan","2");
		myCell.setAttribute("align","center");
		tableRow.addItem(myCell);

		Table myTable = new Table(tableRow);
		myTable.setClass("species");
		myTable.setAttribute("align", "center");
		Form myForm = new Form(myTable);
		myForm.setAttribute("METHOD", "POST");
		myForm.setName("colForm");
		output.append(myForm.toString());

		return output.toString();
	}

	/**
	 * Create the new collection using the current CGI data.
	 *
	 * @return HTML report of collection add.
	 */
	public String addCollection() {
		StringBuffer output = new StringBuffer();

		try {

			Collection aCollection = SQLCollection.createInProject(this.getSQLDataSource(), this.getFormValue("col_id"), this.getFormValue(COLLECTION_PROJECT_FIELD));
			if ( aCollection.first() ) {
				output.append("<P ALIGN=CENTER><FONT COLOR='green'><B>Success</FONT></B></P>");
				aCollection.setManualRefresh();
				aCollection.setLocationName(this.getFormValue(COLLECTION_LOCATION_FIELD));
				if ( this.getFormValue(COLLECTION_LATITUDE_FIELD).length() > 0 )
					aCollection.setLatitude(this.getFormValue(COLLECTION_LATITUDE_FIELD));
				if ( this.getFormValue(COLLECTION_LONGITUDE_FIELD).length() > 0 )
				aCollection.setLongitude(this.getFormValue(COLLECTION_LONGITUDE_FIELD));
				if ( this.getFormValue(COLLECTION_GEO_PRECISION_FIELD).length() > 0 )
					aCollection.setPrecision(this.getFormValue(COLLECTION_GEO_PRECISION_FIELD));
				aCollection.setCollector(this.getFormValue(COLLECTION_COLLECTOR_FIELD));
				aCollection.setNotes(this.getFormValue(COLLECTION_NOTES_FIELD));
				aCollection.setDate(this.getFormValue(COLLECTION_DATE_FIELD));
				aCollection.refresh();
				aCollection.setAutoRefresh();
				output.append("<P ALIGN=CENTER>Added a new collection (ID " + aCollection.getID() + ")<BR/><A HREF='../?col=");
				output.append(aCollection.getID());
				output.append("View collection</A><BR/><A HREF='add'>Add another collection</A></P>");
			} else {
				output.append("<P ALIGN=CENTER><FONT COLOR='red'><B>Insert Failure</FONT></B></P>");
			}
		} catch (DataException e) {
			output.append("<P ALIGN='CENTER'><B><FONT COLOR='red'>SQL Error: </FONT>" + e.getMessage() + "</B></P>");
			e.printStackTrace();
		}
		return output.toString();
	}
	
	/**
	 * Update the collection using the current CGI data.
	 * 
	 * @param aCollection collection object to update.
	 * @return HTML report of update.
	 */
	private String updateCollection(Collection aCollection) {
		try {
			if ( aCollection.isAllowed(Role.WRITE) ) {
				aCollection.setManualRefresh();
				if ( this.hasFormValue(COLLECTION_DATE_FIELD))
					aCollection.setDate(this.getFormValue(COLLECTION_DATE_FIELD));
				if ( this.hasFormValue(COLLECTION_COLLECTOR_FIELD))
					aCollection.setCollector(this.getFormValue(COLLECTION_COLLECTOR_FIELD));
				if ( this.hasFormValue(COLLECTION_LOCATION_FIELD))
					aCollection.setLocationName(this.getFormValue(COLLECTION_LOCATION_FIELD));
				if ( this.hasFormValue(COLLECTION_LATITUDE_FIELD))
					aCollection.setLatitude(this.getFormValue(COLLECTION_LATITUDE_FIELD));
				if ( this.hasFormValue(COLLECTION_LONGITUDE_FIELD))
					aCollection.setLongitude(this.getFormValue(COLLECTION_LONGITUDE_FIELD));
				if ( this.hasFormValue(COLLECTION_NOTES_FIELD))
					aCollection.setNotes(this.getFormValue(COLLECTION_NOTES_FIELD));
				if ( this.hasFormValue(COLLECTION_GEO_PRECISION_FIELD))
					aCollection.setPrecision(this.getFormValue(COLLECTION_GEO_PRECISION_FIELD));
				if ( this.hasFormValue(COLLECTION_PROJECT_FIELD))
					aCollection.setProjectID(this.getFormValue(COLLECTION_PROJECT_FIELD));
				aCollection.refresh();
				aCollection.setAutoRefresh();
				return this.message(SUCCESS_TAG, "Updated collection");
			} else {
				return this.message(WARNING_TAG, "Insufficient permission");
			}
		} catch (DataException e) {
			return this.handleException(e);
		}
	}
	
	/**
	 * Generate the HTML DIV for the map display for the specified collection. The map will include nearby collection data.
	 * 
	 * @param aCollection collection to map.
	 * @return HTML code for the map display as a DIV object.
	 */
	private Div mapContent(Collection aCollection) {
		try {
			Float latFloat = aCollection.getLatitudeFloat();
			Float longFloat = aCollection.getLongitudeFloat();
			boolean hasLocation = ( (latFloat != null) && (longFloat != null) );
			if ( hasLocation ) {
				float thisLat = latFloat.floatValue();
				float thisLong = longFloat.floatValue();
				float latRange = 180.0f / 1024.0f;
				float longRange = 360.0f / 1024.0f;
				float maxLat = thisLat + latRange; float minLat = thisLat - latRange;
				float maxLong = thisLong + longRange; float minLong = thisLong - longRange;
				Collection nearList = SQLCollection.collectionsLoacted(this.getSQLDataSource(), minLat, maxLat, minLong, maxLong);
				
				CyanosMap aMap = this.getMap(nearList);
				aMap.setDIVSize(400, 300);
				aMap.setMapCenter(thisLat, thisLong);
				aMap.setMapZoom(10);				
				return aMap.mapDiv();
			}
			return new Div();
		} catch (DataException e) {
			return new Div(this.handleException(e));
		}
		
	}
	
	/**
	 * Generate HTML report for a Collection.
	 *
	 * @param aCollection a Collection object
	 * @return HTML report as a String.
	 */
	private String collectionText(Collection aCollection) {
		try {
			TableRow tableRow = new TableRow(String.format("<TD>ID:</TD><TD>%s</TD>", aCollection.getID()));
			tableRow.addItem(String.format("<TD>Date:</TD><TD>%s</TD>", this.formatDate(aCollection.getDate())));
	
			TableCell myCell = new TableCell("Project:");
			Project aProject = aCollection.getProject();
			if ( aProject != null ) {
				myCell.addItem(aProject.getName());
			} else {
				myCell.addItem("");
			}
			tableRow.addItem(myCell);
			
			tableRow.addItem(String.format("<TD>Collected by:</TD><TD>%s</TD>", aCollection.getCollector()));
			Div coords = new Div(aCollection.getLocationName());
			coords.addItem(String.format("<BR/>Coordinates: %s, %s<BR/>", aCollection.getLatitudeDM(), aCollection.getLongitudeDM()));
			coords.addItem(String.format("Precision (m): %d m<BR/>", aCollection.getPrecision()));
			coords.setAttribute("STYLE", "margin-left: 10px;");
			tableRow.addItem("<TD COLSPAN='2' ALIGN='LEFT'><B><I>Location</B></I><BR/>" + coords.toString());
			tableRow.addItem(new TableCell(new String[]{"Notes:", this.formatStringHTML(aCollection.getNotes())}));
			
			Table myTable = new Table(tableRow);
			Div textDiv = new Div(myTable);
			myTable.setAttribute("class","list");
			myTable.setAttribute("align","center");
	
			CyanosConfig myConf = this.myWrapper.getAppConfig();

			if ( myConf.canMap() ) {
				myCell = new TableCell(textDiv);
				myCell.setAttribute("VALIGN", "TOP");
				Div mapDiv = this.mapContent(aCollection);
				mapDiv.setAttribute("STYLE", "width: 400; float: right; display: inline;");
				myCell.addItem(mapDiv);
				Table fullTable = new Table(new TableRow(myCell));
				fullTable.setAttribute("STYLE", "margin-bottom: 10px; width: 90%; align: center;");
				return fullTable.toString();
			} else {
				myTable.setAttribute("width","75%");
				return textDiv.toString();
			}
		} catch (DataException e) {
			return this.handleException(e);
		}

	}
	
	/**
	 * Create a HTML form for the collection, to allow updates.
	 *
	 * @param aCollection a Collection object
	 * @return HTML form as a String.
	 */
	private String collectionForm(Collection aCollection) {
		try {
			TableCell myCell = new TableCell("ID:");
			myCell.addItem(aCollection.getID());
			TableRow tableRow = new TableRow(myCell);

			tableRow.addItem(this.makeFormDateRow("Date:", COLLECTION_DATE_FIELD, "colForm", aCollection.getDateString()));

			myCell = new TableCell("Project:");
			try {
				Popup projectPop = this.projectPopup();
				projectPop.setName(COLLECTION_PROJECT_FIELD);
				String myProject = aCollection.getProjectID();
				if ( myProject != null )
					projectPop.setDefault(myProject);
				myCell.addItem(projectPop.toString());
				tableRow.addItem(myCell);
			} catch (DataException e) {
				myCell.addItem("<B><FONT COLOR='red'>ERROR:</FONT> " + e.getMessage() + "</B>"); 
			}

			tableRow.addItem(this.makeFormTextRow("Collected by:", COLLECTION_COLLECTOR_FIELD, aCollection.getCollector()));

			tableRow.addItem(this.makeFormTextRow("Location:", COLLECTION_LOCATION_FIELD, aCollection.getLocationName()));
			myCell = new TableCell("Coordinates:");
			myCell.addItem(String.format("<INPUT TYPE='TEXT' SIZE='10' NAME='lat' VALUE=\"%s\"/> <INPUT TYPE='TEXT' SIZE='10' NAME='long' VALUE=\"%s\"/>", aCollection.getLatitudeDM(), aCollection.getLongitudeDM()));
			tableRow.addItem(myCell);
			myCell = new TableCell("Precision (m):");
			myCell.addItem(String.format("<INPUT TYPE='TEXT' SIZE='10' NAME='prec' VALUE=\"%d m\"/>", aCollection.getPrecision()));
			tableRow.addItem(myCell);
			
			tableRow.addItem(this.makeFormTextAreaRow("Notes:", COLLECTION_NOTES_FIELD, aCollection.getNotes()));
			myCell = new TableCell("<INPUT TYPE=SUBMIT NAME='updateCollection' VALUE='Update'/><INPUT TYPE=RESET />");
			myCell.setAttribute("colspan","2");
			myCell.setAttribute("align","center");
			tableRow.addItem(myCell);

			Table myTable = new Table(tableRow);
			myTable.setClass("species");
			myTable.setAttribute("align", "center");
			Form myForm = new Form(myTable);
			myForm.setAttribute("METHOD", "POST");
			myForm.addItem(String.format("<INPUT TYPE='HIDDEN' NAME='col' VALUE='%s'/>", aCollection.getID()));
			myForm.setName("colForm");
			return myForm.toString();
		} catch (DataException e) {
			return this.handleException(e);
		}
		
	}

	/**
	 * Display the Collection.  This is generate the text and form, as needed.
	 *
	 * @param aCollection Collection to display.
	 * @return HTML code as a String.
	 */
	public String showCollection(Collection aCollection) {
		try {
			if ( aCollection.isLoaded() ) {
				Div mainDiv = new Div();
				if ( aCollection.isAllowed(Role.WRITE) ) {				
					if ( this.hasFormValue("updateCollection") ) {
						mainDiv.addItem(this.updateCollection(aCollection));
					}
					mainDiv.addItem(this.viewDiv(COLLECTION_DIV_ID, this.collectionText(aCollection)));
					mainDiv.addItem(this.editDiv(COLLECTION_DIV_ID, this.collectionForm(aCollection)));
				} else {
					mainDiv.addItem(this.collectionText(aCollection));
				}
				return mainDiv.toString();
			} else {
				return ("<P ALIGN='CENTER'><B><FONT COLOR='red'>ERROR:</FONT> Collection information not found</B></P>");
			}
		} catch ( DataException e ) {
			return this.handleException(e);
		}
	}

	public String collectionMap(Collection aCol, int width, int height) {
		try {
			if ( aCol.first() ) {
				CyanosMap aMap = this.getMap(aCol);
				aMap.setDIVSize(width, height);				
				Div mapDiv = aMap.hiddenDiv();
				return mapDiv.toString();
			} else {
				return "";
			}
		} catch ( DataException e ) {
			return this.handleException(e);
		}
	}
	
	private boolean showOLMap() {
		CyanosConfig myConf = this.myWrapper.getAppConfig();
		Map<String,String> layers = myConf.getMapServerLayers();
		return (layers.size() > 0);
	}

	private CyanosMap getMap(Collection aCol) {
		if ( this.showOLMap() ) {
			return new OpenLayersMap(this.myWrapper, aCol);
		} else {
			return new GoogleMap(this.myWrapper, aCol);
		}		
	}
	
}
