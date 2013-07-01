/**
 * 
 */
package edu.uic.orjala.cyanos.web.forms;

import java.text.SimpleDateFormat;

import edu.uic.orjala.cyanos.Collection;
import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.Isolation;
import edu.uic.orjala.cyanos.Role;
import edu.uic.orjala.cyanos.sql.SQLIsolation;
import edu.uic.orjala.cyanos.web.BaseForm;
import edu.uic.orjala.cyanos.web.CyanosWrapper;
import edu.uic.orjala.cyanos.web.html.Div;
import edu.uic.orjala.cyanos.web.html.Form;
import edu.uic.orjala.cyanos.web.html.Popup;
import edu.uic.orjala.cyanos.web.html.Table;
import edu.uic.orjala.cyanos.web.html.TableCell;
import edu.uic.orjala.cyanos.web.html.TableHeader;
import edu.uic.orjala.cyanos.web.html.TableRow;

/**
 * @author George Chlipala
 *
 */
public class IsolationForm extends BaseForm {

	/**
	 * @param callingServlet
	 */
	public IsolationForm(CyanosWrapper aWrapper) {
		super(aWrapper);
	}

	public String listIsolations(Isolation isoList) {
		try {
			String headers[] = { "ID", "Type", "Media", "Date", "Notes" };
			TableCell myCell = new TableHeader(headers);
			myCell.setAttribute("class","header");
			TableRow tableRow = new TableRow(myCell);
			SimpleDateFormat myDateFormat = this.dateFormat();
			String curClass = "odd";
			Table myTable = new Table(tableRow);
			myTable.setClass("dashboard");
			myTable.setAttribute("WIDTH", "75%");
			myTable.setAttribute("align", "center");
			if (isoList != null ) {
				isoList.beforeFirst();
				while (isoList.next()) {
					myCell = new TableCell(this.isolationLink(isoList));
					myCell.addItem(isoList.getType());
					myCell.addItem(isoList.getMedia());
					myCell.addItem(myDateFormat.format(isoList.getDate()));
					myCell.addItem(shortenString(isoList.getNotes(), 75));
					TableRow aRow = new TableRow(myCell);
					aRow.setClass(curClass);							
					aRow.setAttribute("align", "center");
					myTable.addItem(aRow);
					if ( curClass.equals("odd") ) {
						curClass = "even";
					} else {
						curClass = "odd";
					}
				}
			} else {
				myTable.addItem("<TR><TD COLSPAN=5 ALIGN='CENTER'><B>No Isolations</B></TD></TR>");
			}
			return myTable.toString();
		} catch (DataException e) {
			e.printStackTrace();
			return ("<P ALIGN='CENTER'><FONT COLOR='red'><B>SQL FAILURE:</FONT> " + e.getMessage() + "</B></P>");
		}
	}
	
	public String updateIsolation(Isolation anIsolation) {
		if ( anIsolation.isAllowed(Role.WRITE) ) {
			try {
				anIsolation.setManualRefresh();
				if ( this.hasFormValue("date"))
					anIsolation.setDate(this.getFormValue("date"));
				if ( this.hasFormValue("type"))
					anIsolation.setType(this.getFormValue("type"));
				if ( this.hasFormValue("media"))
					anIsolation.setMedia(this.getFormValue("media"));
				if ( this.hasFormValue("parent"))
					anIsolation.setParentID(this.getFormValue("parent"));
				if ( this.hasFormValue("notes"))
					anIsolation.setNotes(this.getFormValue("notes"));
				anIsolation.refresh();
				anIsolation.setAutoRefresh();
				return this.message(SUCCESS_TAG, "Isolation record updated.");
				
			} catch (DataException e) {
				return this.handleException(e);
			}
		} else {
			return this.message(WARNING_TAG, "Action not allowed.");
		}
	}
	
	private String isolationForm(Isolation anIsolation) {
		try {
			TableCell myCell = new TableCell("ID:");
			myCell.addItem(anIsolation.getID());
			TableRow tableRow = new TableRow(myCell);

			tableRow.addItem(this.makeFormDateRow("Date:", "date", "colForm", anIsolation.getDateString()));

			Popup aPop = new Popup();
			aPop.setName("type");
			aPop.addItemWithLabel("plate", "Plate");
			aPop.addItemWithLabel("tube", "Tube");
			aPop.addItemWithLabel("direct", "Field Harvest");
			aPop.setDefault(anIsolation.getType());
			myCell = new TableCell("Type:");
			myCell.addItem(aPop);
			tableRow.addItem(myCell);

			tableRow.addItem(this.makeFormTextRow("Media:", "media", anIsolation.getMedia()));
			aPop = new Popup();
			aPop.setName("parent");
			aPop.addItemWithLabel("", "Field Collection");
			Isolation possibles = anIsolation.possibleParents();
			possibles.beforeFirst();
			while ( possibles.next() ) {
				aPop.addItem(possibles.getID());
			}
			String parent = anIsolation.getParentID();
			if ( parent == null ) parent = "";
			aPop.setDefault(parent);
			myCell = new TableCell("Parent:");
			myCell.addItem(aPop);
			tableRow.addItem(myCell);				

			tableRow.addItem(this.makeFormTextAreaRow("Notes:", "notes", anIsolation.getNotes()));
			myCell = new TableCell("<INPUT TYPE=SUBMIT NAME='updateIsolation' VALUE='Update'/><INPUT TYPE=RESET />");
			myCell.setAttribute("colspan","2");
			myCell.setAttribute("align","center");
			tableRow.addItem(myCell);

			Table myTable = new Table(tableRow);
			myTable.setClass("species");
			myTable.setAttribute("align", "center");
			Form myForm = new Form(myTable);
			myForm.setAttribute("METHOD", "POST");
			myForm.addItem(String.format("<INPUT TYPE='HIDDEN' NAME='id' VALUE='%s'/>", anIsolation.getID()));
			myForm.setName("colForm");
			return myForm.toString();
		} catch (DataException e) {
			return this.handleException(e);
		}

	}
	
	private String isolationType(String aType) {
		if ( aType == null ) {
			return "";
		} else if ( aType.equals("direct") ) {
			return "Field Harvest";
		} else if ( aType.length() > 0 ) {
			return aType.toUpperCase().substring(0, 1) + aType.substring(1);
		} 
		return "";
	}
	
	public String addIsolation() {
		StringBuffer output = new StringBuffer();

		try {

			Isolation anIsolation = SQLIsolation.createInProject(this.getSQLDataSource(), this.getFormValue("new_id"), this.getFormValue("col"), this.getFormValue("project"));
			
			if ( anIsolation.first() ) {
				output.append("<P ALIGN=CENTER><FONT COLOR='green'><B>Success</FONT></B></P>");
				anIsolation.setManualRefresh();
				anIsolation.setType(this.getFormValue("type"));
				anIsolation.setMedia(this.getFormValue("media"));
				anIsolation.setNotes(this.getFormValue("notes"));
				anIsolation.setDate(this.getFormValue("date"));
				String parent = this.getFormValue("parent");
				if ( parent != null && (! parent.equals(""))) 
					anIsolation.setParentID(parent);
				anIsolation.refresh();
				anIsolation.setAutoRefresh();
				output.append("<P ALIGN=CENTER>Added a new isolation (ID " + anIsolation.getID() + ")</P>");
			} else {
				output.append("<P ALIGN=CENTER><FONT COLOR='red'><B>Insert Failure</FONT></B></P>");
			}
		} catch (DataException e) {
			output.append(this.handleException(e));
		}
		return output.toString();
	}
	

	
	private String isolationText(Isolation anIsolation) {
		try {
			TableCell myCell = new TableCell("ID:");
			myCell.addItem(anIsolation.getID());
			TableRow tableRow = new TableRow(myCell);

			SimpleDateFormat myFormat = this.dateFormat();
			
			myCell = new TableCell("Collection:");
			myCell.addItem(String.format("<A HREF='%s/collection?col=%s'>%s</A>", this.myWrapper.getContextPath(), anIsolation.getCollectionID(), anIsolation.getCollectionID()));
			tableRow.addItem(myCell);
			
			myCell = new TableCell("Date:");
			myCell.addItem(myFormat.format(anIsolation.getDate()));
			tableRow.addItem(myCell);

			myCell = new TableCell("Type:");
			myCell.addItem(this.isolationType(anIsolation.getType()));
			tableRow.addItem(myCell);

			myCell = new TableCell("Media:");
			myCell.addItem(anIsolation.getMedia());
			tableRow.addItem(myCell);

			Isolation parent = anIsolation.getParent();
			if ( parent != null ) {
				myCell = new TableCell("Parent:");
				myCell.addItem(this.isolationLink(parent));
				tableRow.addItem(myCell);				
			}
			
			myCell = new TableCell("Notes:");
			myCell.addItem(this.formatStringHTML(anIsolation.getNotes()));
			tableRow.addItem(myCell);

			Table myTable = new Table(tableRow);
			myTable.setClass("species");
			myTable.setAttribute("align", "center");
			return myTable.toString();
		} catch (DataException e) {
			return this.handleException(e);
		}

	}

	public String showIsolation(Isolation anIsolation) {
		try {
			if ( anIsolation.isLoaded() ) {
				Div mainDiv = new Div();
				
				if ( anIsolation.isAllowed(Role.WRITE) ) {
					if ( this.hasFormValue("updateIsolation") ) {
						mainDiv.addItem(this.updateIsolation(anIsolation));
					}
					mainDiv.addItem(this.viewDiv("isolation", this.isolationText(anIsolation)));
					mainDiv.addItem(this.editDiv("isolation", this.isolationForm(anIsolation)));
				} else {
					mainDiv.addItem(this.isolationText(anIsolation));
				}
				return mainDiv.toString();
			} else {
				return "<P ALIGN='CENTER'><B><FONT COLOR='red'>ERROR:</FONT> Collection information not found</B></P>";
			}
		} catch ( DataException e ) {
			return this.handleException(e);
		}
	}
	
	public String addIsolationForm(Collection aCollection) {
		TableRow tableRow;
		tableRow = new TableRow(this.makeFormTextRow("ID:", "new_id", aCollection.getID()));
		
		tableRow.addItem(this.makeFormDateRow("Date:", "date", "colForm"));

		Popup aPop = new Popup();
		aPop.setName("type");
		aPop.addItemWithLabel("plate", "Plate");
		aPop.addItemWithLabel("tube", "Tube");
		aPop.addItemWithLabel("direct", "Field Harvest");
		if ( this.hasFormValue("type"));
			aPop.setDefault(this.getFormValue("type"));
		TableCell myCell = new TableCell("Type:");
		myCell.addItem(aPop);
		tableRow.addItem(myCell);

		tableRow.addItem(this.makeFormTextRow("Media:", "media"));
		aPop = new Popup();
		aPop.setName("parent");
		aPop.addItemWithLabel("", "Field Collection");

		myCell = new TableCell("Parent:");
		try {
			Isolation possibles = SQLIsolation.isolationsForCollection(this.getSQLDataSource(), this.getFormValue("col"));
			possibles.beforeFirst();
			while ( possibles.next() ) {
				aPop.addItem(possibles.getID());
			}
			String parent = this.getFormValue("parent");
			if ( parent == null ) parent = "";
			aPop.setDefault(parent);
			myCell.addItem(aPop);
		} catch (DataException e) {
			myCell.addItem("<B><FONT COLOR='red'>SQL ERROR:</FONT> " + e.getMessage() + "</B></P>");
		}
		tableRow.addItem(myCell);	
		
		myCell = new TableCell("Project:");
		try {
			Popup projectPop = this.projectPopup();
			projectPop.setName("project");
			if ( this.hasFormValue("project") ) {
				projectPop.setDefault(this.getFormValue("project"));
			} else if ( aCollection.getProjectID() != null ){
				projectPop.setDefault(aCollection.getProjectID());
			}
			myCell.addItem(projectPop.toString());
		} catch (DataException e) {
			myCell.addItem("<B><FONT COLOR='red'>ERROR:</FONT> " + e.getMessage() + "</B></P>");
		}
		tableRow.addItem(myCell);		
		
		tableRow.addItem(this.makeFormTextAreaRow("Notes:", "notes"));
		myCell = new TableCell("<INPUT TYPE=SUBMIT NAME='addIsolation' VALUE='Add'/><INPUT TYPE=RESET />");
		myCell.setAttribute("colspan","2");
		myCell.setAttribute("align","center");
		tableRow.addItem(myCell);

		Table myTable = new Table(tableRow);
		myTable.setClass("species");
		myTable.setAttribute("align", "center");
		Form myForm = new Form(myTable);
		myForm.setAttribute("METHOD", "POST");
		myForm.setName("colForm");
		myForm.addItem(String.format("<INPUT TYPE='HIDDEN' NAME='col' VALUE='%s'/>", this.getFormValue("col")));
		return myForm.toString();

	}

}
