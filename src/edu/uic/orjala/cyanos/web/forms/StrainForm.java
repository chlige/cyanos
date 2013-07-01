/**
 * 
 */
package edu.uic.orjala.cyanos.web.forms;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.jmimemagic.Magic;
import net.sf.jmimemagic.MagicException;
import net.sf.jmimemagic.MagicMatch;
import net.sf.jmimemagic.MagicMatchNotFoundException;
import net.sf.jmimemagic.MagicParseException;

import edu.uic.orjala.cyanos.Collection;
import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.ExternalFile;
import edu.uic.orjala.cyanos.Inoc;
import edu.uic.orjala.cyanos.Isolation;
import edu.uic.orjala.cyanos.Project;
import edu.uic.orjala.cyanos.Role;
import edu.uic.orjala.cyanos.SingleFile;
import edu.uic.orjala.cyanos.Strain;
import edu.uic.orjala.cyanos.Taxon;
import edu.uic.orjala.cyanos.sql.SQLStrain;
import edu.uic.orjala.cyanos.sql.SQLTaxon;
import edu.uic.orjala.cyanos.web.BaseForm;
import edu.uic.orjala.cyanos.web.CyanosConfig;
import edu.uic.orjala.cyanos.web.CyanosWrapper;
import edu.uic.orjala.cyanos.web.FileRoot;
import edu.uic.orjala.cyanos.web.html.Anchor;
import edu.uic.orjala.cyanos.web.html.Div;
import edu.uic.orjala.cyanos.web.html.Form;
import edu.uic.orjala.cyanos.web.html.HtmlList;
import edu.uic.orjala.cyanos.web.html.Input;
import edu.uic.orjala.cyanos.web.html.Paragraph;
import edu.uic.orjala.cyanos.web.html.Popup;
import edu.uic.orjala.cyanos.web.html.StyledText;
import edu.uic.orjala.cyanos.web.html.Table;
import edu.uic.orjala.cyanos.web.html.TableCell;
import edu.uic.orjala.cyanos.web.html.TableHeader;
import edu.uic.orjala.cyanos.web.html.TableRow;

/**
 * @author George Chlipala
 *
 */
public class StrainForm extends BaseForm {

	public static final String INOC_DIV_TITLE = "Inoculations";
	public static final String INOC_DIV_ID = "strainInocs";
	public static final String QUERY_PARAM = "strainquery";
	public static final String SORT_PARAM = "sort";
	public static final String DIRECTION_PARAM = "dir";
	public static final String PHOTO_FORM = "photoForm";
	
	/**
	 * 
	 */
	public StrainForm(CyanosWrapper aWrapper) {
		super(aWrapper);
	}

	public String listSpecies() {
		StringBuffer output = new StringBuffer();

		Form queryForm = new Form();
		queryForm.setName(QUERY_PARAM);
		Input searchField = new Input("text");
		searchField.setName(QUERY_PARAM);
		String query = "";
		String sortField = "";
		String sortDirection = "";

		if ( this.hasFormValue(QUERY_PARAM) ) {
			query = this.getFormValue(QUERY_PARAM);
			searchField.setValue(this.getFormValue(QUERY_PARAM));
		} 
		if ( this.hasFormValue(SORT_PARAM) ) sortField = this.getFormValue(SORT_PARAM);
		if ( this.hasFormValue(DIRECTION_PARAM) ) sortDirection = this.getFormValue(DIRECTION_PARAM); 
		
		queryForm.addItem(searchField);
		queryForm.addSubmit("Search");
		output.append("<CENTER>" + queryForm + "</CENTER>");

		output.append("<P>" + this.listSpeciesTable(query, sortField, sortDirection) + "</P>");
		return output.toString();

	}
	
	public String queryForm() {
		StringBuffer output = new StringBuffer();

		Form queryForm = new Form();
		queryForm.setAttribute("ACTION", String.format("%s/strain", this.myWrapper.getContextPath()));
		queryForm.setName(QUERY_PARAM);
		Input searchField = new Input("text");
		searchField.setName(QUERY_PARAM);
		String query = "";
		String sortField = "";
		String sortDirection = "";

		queryForm.addItem(searchField);
		queryForm.addSubmit("Search");

		if ( this.hasFormValue(QUERY_PARAM) ) {
			query = this.getFormValue(QUERY_PARAM);
			searchField.setValue(this.getFormValue(QUERY_PARAM));
			output.append("<CENTER>" + queryForm + "</CENTER>");

			if ( this.hasFormValue(SORT_PARAM) ) sortField = this.getFormValue(SORT_PARAM);
			if ( this.hasFormValue(DIRECTION_PARAM) ) sortDirection = this.getFormValue(DIRECTION_PARAM); 
			output.append("<P>" + this.listSpeciesTable(query, sortField, sortDirection) + "</P>");
		} else {
			output.append("<CENTER>" + queryForm + "</CENTER>");
		}

		return output.toString();

	}

	public String listSpeciesTable(Strain aStrain) {

		TableCell header = new TableHeader();

		header.addItem("Strain ID");
		header.addItem("Name");
		header.addItem("Order");
		header.addItem("Notes");

		header.setAttribute("class","header");
		TableRow tableRow = new TableRow(header);
		Table myTable = new Table(tableRow);
		myTable.setAttribute("class","dashboard");
		myTable.setAttribute("align","center");
		myTable.setAttribute("width","75%");
		String curClass = "odd";
		if ( aStrain != null ) {
			try {
				aStrain.beforeFirst();
				while (aStrain.next()) {
					TableCell myCell = new TableCell();
					StringBuffer myUrl = new StringBuffer(this.myWrapper.getContextPath() + "/strain?id=");
					myUrl.append(aStrain.getID());
					Anchor myLink = new Anchor(aStrain.getID());
					myLink.setLink(myUrl.toString());
					myCell.addItem(myLink.toString());
					myCell.addItem(aStrain.getName());
					Taxon myTaxon = aStrain.getTaxon();
					if ( myTaxon != null && myTaxon.first() )
						myCell.addItem(myTaxon.getOrder());
					else 
						myCell.addItem("");
					TableRow aRow = new TableRow(myCell);
					if (aStrain.isActive()) {
						myCell.addItem(BaseForm.shortenString(aStrain.getNotes(), 25));
						aRow.setClass(curClass);
					} else {
						myCell.addItem("<FONT COLOR=#AA0000><B>REMOVED " + BaseForm.shortenString(aStrain.getNotes(), 18) +"</FONT></B>");
						aRow.setClass("dead");
					}
					aRow.setAttribute("align", "center");
					myTable.addItem(aRow);
					if ( curClass.equals("odd") ) {
						curClass = "even";
					} else {
						curClass = "odd";
					}
				}
			} catch (DataException e) {
				e.printStackTrace();
				tableRow.addItem("<TD COLSPAN='4' ALIGN='CENTER'><B><FONT COLOR='red'>ERROR:</FONT>" + e.getMessage() + "</B></TD>");
			}
		} else 
			tableRow.addItem("<TD COLSPAN='4' ALIGN='CENTER'><B>NONE</B></TD>");
		return myTable.toString();
	}

	public String listSpeciesTable(String queryString, String sortField, String sortDirection) {	
		String baseURL = "?";
		if ( this.hasFormValue(QUERY_PARAM) )
			baseURL = String.format("?%s=%s&", QUERY_PARAM, this.getFormValue(QUERY_PARAM));

		if (queryString.matches("\\*") ) {
			queryString.replaceAll("\\*", "%");
		} else {
			queryString = "%" + queryString + "%";
		}

		if (sortDirection == null || sortDirection.equals("") ) {
			sortDirection = new String("ASC");
		}

		String sortString = new String("CAST(culture_id AS UNSIGNED)");
		if (sortField != null && (! sortField.equals("")) ) {
			if ( sortField.equals("name")) {
				sortString = SQLStrain.NAME_COLUMN;
			} else if ( sortField.equals("order")) {
				sortString = SQLTaxon.ORDER_COLUMN;
			}
		}

		TableCell header = new TableHeader();
		Strain myStrains = null;
		String[] columns = {SQLStrain.NAME_COLUMN, SQLStrain.ID_COLUMN};
		String[] queries = {queryString, queryString};

		try {
			if ( "id".equals(sortField) ) {
				myStrains = SQLStrain.strainsLike(this.myWrapper.getSQLDataSource(), columns, queries, sortString, sortDirection);
				if ( "ASC".equals(sortDirection) ) {
					header.addItem("<A HREF='" + baseURL + "sort=id&dir=DESC' CLASS='table'>Strain ID</A>");
					header.addItem("<A HREF='" + baseURL + "sort=name&dir=ASC' CLASS='table'>Name</A>");
					header.addItem("<A HREF='" + baseURL + "sort=order&dir=ASC' CLASS='table'>Order</A>");
				} else {
					header.addItem("<A HREF='" + baseURL + "sort=id&dir=ASC' CLASS='table'>Strain ID</A>");			
					header.addItem("<A HREF='" + baseURL + "sort=name&dir=ASC' CLASS='table'>Name</A>");
					header.addItem("<A HREF='" + baseURL + "sort=order&dir=ASC' CLASS='table'>Order</A>");
				}
			} else if ("name".equals(sortField) ) {
				myStrains = SQLStrain.strainsLike(this.myWrapper.getSQLDataSource(), columns, queries, sortString, sortDirection);
				if ( "ASC".equals(sortDirection) ) {
					header.addItem("<A HREF='" + baseURL + "sort=id&dir=ASC' CLASS='table'>Strain ID</A>");
					header.addItem("<A HREF='" + baseURL + "sort=name&dir=DESC' CLASS='table'>Name</A>");
					header.addItem("<A HREF='" + baseURL + "sort=order&dir=ASC' CLASS='table'>Order</A>");
				} else {
					header.addItem("<A HREF='" + baseURL + "sort=id&dir=ASC' CLASS='table'>Strain ID</A>");			
					header.addItem("<A HREF='" + baseURL + "sort=name&dir=ASC' CLASS='table'>Name</A>");
					header.addItem("<A HREF='" + baseURL + "sort=order&dir=ASC' CLASS='table'>Order</A>");
				}			
			} else if ("order".equals(sortField) ) {
				myStrains = SQLStrain.strainsLikeByTaxa(this.myWrapper.getSQLDataSource(), columns, queries, sortString, sortDirection);
				if ( "ASC".equals(sortDirection) ) {
					header.addItem("<A HREF='" + baseURL + "sort=id&dir=ASC' CLASS='table'>Strain ID</A>");
					header.addItem("<A HREF='" + baseURL + "sort=name&dir=ASC' CLASS='table'>Name</A>");
					header.addItem("<A HREF='" + baseURL + "sort=order&dir=DESC' CLASS='table'>Order</A>");
				} else {
					header.addItem("<A HREF='" + baseURL + "sort=id&dir=ASC' CLASS='table'>Strain ID</A>");			
					header.addItem("<A HREF='" + baseURL + "sort=name&dir=ASC' CLASS='table'>Name</A>");
					header.addItem("<A HREF='" + baseURL + "sort=order&dir=ASC' CLASS='table'>Order</A>");
				}			
			} else {
				myStrains = SQLStrain.strainsLike(this.myWrapper.getSQLDataSource(), columns, queries, sortString, sortDirection);
				header.addItem("<A HREF='" + baseURL + "sort=id&dir=ASC' CLASS='table'>Strain ID</A>");
				header.addItem("<A HREF='" + baseURL + "sort=name&dir=ASC' CLASS='table'>Name</A>");
				header.addItem("<A HREF='" + baseURL + "sort=order&dir=ASC' CLASS='table'>Order</A>");
			}
			header.addItem("Notes");

			header.setAttribute("class","header");
			TableRow tableRow = new TableRow(header);
			Table myTable = new Table(tableRow);
			myTable.setAttribute("class","dashboard");
			myTable.setAttribute("align","center");
			myTable.setAttribute("width","75%");

			myStrains.beforeFirst();
			boolean oddRow = true;
			while (myStrains.next()) {
				TableCell myCell = new TableCell();
				StringBuffer myUrl = new StringBuffer("strain?id=");
				myUrl.append(myStrains.getID());
				Anchor myLink = new Anchor(myStrains.getID());
				myLink.setLink(myUrl.toString());
				myCell.addItem(myLink.toString());
				myCell.addItem(myStrains.getName());
				Taxon myTaxon = myStrains.getTaxon();
				if ( myTaxon != null && myTaxon.first() )
					myCell.addItem(myTaxon.getOrder());
				else 
					myCell.addItem("");
				TableRow aRow = new TableRow(myCell);
				if ( myStrains.isActive() ) {
					myCell.addItem(BaseForm.shortenString(myStrains.getNotes(),25));
					if ( oddRow ) aRow.setClass("odd");
					else aRow.setClass("even");
				} else {
					myCell.addItem("<FONT COLOR=#AA0000><B>REMOVED " + BaseForm.shortenString(myStrains.getNotes(),25) +"</FONT></B>");
					aRow.setClass("dead");
				}
				aRow.setAttribute("align", "center");
				myTable.addItem(aRow);
				oddRow = (! oddRow);
			}
			return myTable.toString();
		} catch (DataException e) {
			e.printStackTrace();
			return e.getMessage();
		}
	}

	public Form addStrainForm() {
		return this.addStrainForm("");
	}
	
	public Form addStrainForm(String newID) {
		Form myForm = new Form("<INPUT TYPE=HIDDEN NAME='action' VALUE='add'>");
		TableCell myCell;
		TableRow tableRow = new TableRow();
		Table myTable = new Table(tableRow);
		myTable.setClass("species");
		myTable.setAttribute("align", "center");
		tableRow.addItem(this.makeFormTextRow("Culture ID:", "culture_id", newID));
		tableRow.addItem(this.makeFormTextRow("Culture Source:", "culture_source"));
		tableRow.addItem(this.makeFormTextRow("Latin Binomial", "binomial"));
		tableRow.addItem(this.makeFormTextRow("Default Media:", "orig_media"));
		tableRow.addItem(this.makeFormDateRow("Date Added:", "date", "strain"));
		tableRow.addItem(this.makeFormTextAreaRow("Notes:", "notes"));
		myCell = new TableCell("Project:");
		try {
			Popup aPop = this.projectPopup();
			aPop.setName("project");
			if ( this.hasFormValue("project") )
				aPop.setDefault(this.getFormValue("project"));
			myCell.addItem(aPop.toString());
		} catch (DataException e) {
			myCell.addItem("<B><FONT COLOR='red'>ERROR:</FONT> " + e.getMessage() + "</B>"); 
		}
		tableRow.addItem(myCell);

		myCell = new TableCell("<INPUT TYPE=SUBMIT NAME='actionAddStrain' VALUE='Add Strain'/><INPUT TYPE='RESET'/>");
		myCell.setAttribute("colspan","2");
		myCell.setAttribute("align","center");
		tableRow.addItem(myCell);

		myTable.setClass("species");
		myTable.setAttribute("align", "center");
		myForm.addItem(myTable);
		myForm.setName("strain");
		myForm.setAttribute("METHOD","POST");
		return myForm;
	}

	public String addStrain() {
		try {
			if ( this.hasFormValue("actionAddStrain") ) {
				Strain aStrain = new SQLStrain(this.getSQLDataSource(), this.getFormValue("culture_id"));
				if ( aStrain.first() ) {
					Paragraph myText = new Paragraph("<P ALIGN='CENTER'><FONT COLOR='red'><B>Duplicate strain ID</FONT><BR>");
					myText.setAlign("CENTER");
					myText.addItem(aStrain.getID() + " <I>" + aStrain.getName() +
							"</I> Source: " + aStrain.getCultureSource() + "<BR>");
					String newId;

					if ( aStrain.getName().equals(this.getFormValue("binomial")) ) {
						newId = new String(aStrain.getID() + "A");
						myText.addItem("Appears to be duplicate of same strain.  Suggested strain ID is " + newId);

					} else {
						newId = new String(aStrain.getID() + "1");
						myText.addItem("Appears to new strain with duplicate number.  Suggested strain ID is " + newId);					
					}
					myText.addItem("</B>");

					Form myForm = this.addStrainForm();
					myForm.addItem("<INPUT TYPE=HIDDEN NAME='action' VALUE='add'>");
					return myText.toString() + myForm.toString();
				}


				Strain newStrain = SQLStrain.createInProject(this.getSQLDataSource(), this.getFormValue("culture_id"), this.getFormValue("project"));

				if ( newStrain.first() ) {
					newStrain.setManualRefresh();
					String genus = this.getFormValue("binomial").split(" ", 2)[0];
					newStrain.setCultureSource(this.getFormValue("culture_source"));
					newStrain.setName(this.getFormValue("binomial"));
					newStrain.setGenus(genus);
					newStrain.setDate(this.getFormValue("date"));
					newStrain.setNotes(this.getFormValue("notes"));
					newStrain.setDefaultMedia(this.getFormValue("orig_media"));
					newStrain.setStatus("Good");
					newStrain.refresh();
					newStrain.setAutoRefresh();
					Paragraph someText = new Paragraph("<FONT COLOR='green' SIZE='+2'><B>Strain Added</B></FONT>");
					someText.setAttribute("align","center");
					someText.addItem(this.showSpeciesText(newStrain));
					return someText.toString();
				} else {
					return "<FONT COLOR='red'><B>Insert Failure</B></FONT>";
				}
			} else {	
				return this.addStrainForm().toString();
			}
		} catch (DataException ex ) {
			ex.printStackTrace();
			return ("<FONT COLOR='red'><B>ERROR:</B></FONT> " + ex.getMessage());
		}	
	}

	public String showSpeciesText(Strain aStrain) {
		StringBuffer output = new StringBuffer();

		TableCell myCell;
		TableRow tableRow = new TableRow();

		try {
			if ( aStrain.wasRemoved() ) {
				SimpleDateFormat died = this.dateFormat();
				myCell = new TableCell("<B><FONT SIZE=+3 COLOR='red'>STRAIN REMOVED<BR></FONT>" +
						"<FONT SIZE=+2>" +  died.format(aStrain.getRemovedDate()) + "</B><BR>(" + 
						aStrain.getRemoveReason() + ")</FONT>");
				myCell.setAttribute("COLSPAN", "3");
				myCell.setAttribute("ALIGN", "CENTER");
				tableRow.addItem(myCell);
			}

			Isolation anIso = aStrain.getSourceIsolation();
			if ( anIso != null ) {
				Collection aCol = anIso.getCollection();
				tableRow.addItem("<TD WIDTH='100'>Culture Source:</TD><TD>" + aStrain.getCultureSource() + 
						" [<A HREF='collection?id=" + anIso.getID() + "'>View Source Isolation</A>] [<A HREF='collection?col=" + aCol.getID() + "'>View Source Collection</A>]</TD>");
			} else {
				tableRow.addItem("<TD WIDTH='100'>Culture Source:</TD><TD>" + aStrain.getCultureSource() + "</TD>");
			}

			myCell = new TableCell("Scientific Name:");
			myCell.addItem(aStrain.getName());
			tableRow.addItem(myCell);

			myCell = new TableCell("Taxonomy");
			myCell.addItem(this.buildTaxonTable(false, aStrain).toString());
			tableRow.addItem(myCell);

			SimpleDateFormat myDate = this.dateFormat();
			myCell = new TableCell("Date Added:");
			myCell.addItem(myDate.format(aStrain.getDate()));
			tableRow.addItem(myCell);
			
			String status = aStrain.getStatus();
			myCell = new TableCell("Culture Status:");	
			myCell.addItem(this.strainStatus(status));
			tableRow.addItem(myCell);

			if ( status != null && (! status.equals(Strain.FIELD_HARVEST_STATUS)) ) {
				myCell = new TableCell("Default Media:");
				myCell.addItem(aStrain.getDefaultMedia());
				tableRow.addItem(myCell);
			}

			myCell = new TableCell("Project:");
			Project myProject = aStrain.getProject();
			if ( myProject != null ) {
				myCell.addItem(myProject.getName());
			} else {
				myCell.addItem("NONE");
			}
			tableRow.addItem(myCell);
			
			String notes = aStrain.getNotes();
			if ( notes != null )
				tableRow.addItem("<TD VALIGN='TOP'>Notes:</TD><TD>" + notes.replaceAll("\n", "<BR>") + "</TD>");
			else 
				tableRow.addItem("<TD VALIGN='TOP'>Notes:</TD><TD></TD>");
				
		} catch (DataException e) {
			tableRow.addItem("<TD COLSPAN=2><B><FONT COLOR='RED'>ERROR:</FONT> " + e.getMessage() + "</B></TD>");
			e.printStackTrace();
		}

		Table myTable = new Table(tableRow);
		myTable.setClass("species");
		myTable.setAttribute("width", "80%");
		myTable.setAttribute("align", "center");
		output.append(myTable);
		return output.toString();
	}
	
	private String strainStatus(String status) {
		if ( status == null ) {
			return "";
		} else if ( status.equals(Strain.GOOD_STATUS) ) {
			return "Good";
		} else if ( status.equals(Strain.SLOW_GROWTH_STATUS) ) {
			return "Slow Growth";
		} else if ( status.equals(Strain.CONTAMINATED_STATUS) ) {
			return "Contaminated";
		} else if ( status.equals(Strain.REMOVED_STATUS) ) {
			return "Removed";
		} else if ( status.equals(Strain.FIELD_HARVEST_STATUS) ) {
			return "Field Collection";
		} else {
			return status;
		}
	}
	
	public String updateStrain(Strain aStrain) {
		if ( this.hasFormValue("updateAction") ) {
			try {
				aStrain.setManualRefresh();
				if ( this.hasFormValue("culture_source") && ( ! this.getFormValue("culture_source").equals(aStrain.getCultureSource())) ) 
					aStrain.setCultureSource(this.getFormValue("culture_source"));
				if ( this.hasFormValue("genus") && ( ! this.getFormValue("genus").equals(aStrain.getGenus())) )
					aStrain.setGenus(this.getFormValue("genus"));
				if ( this.hasFormValue("strainName") && ( ! this.getFormValue("strainName").equals(aStrain.getName())) )
					aStrain.setName(this.getFormValue("strainName"));
				if ( this.hasFormValue("media_name") && (! this.getFormValue("media_name").equals(aStrain.getDefaultMedia())) )
					aStrain.setDefaultMedia(this.getFormValue("media_name"));
				if ( this.hasFormValue("notes") && (! this.getFormValue("notes").equals(aStrain.getNotes())) )
					aStrain.setNotes(this.getFormValue("notes"));
				if ( this.hasFormValue("addDate") && (! this.getFormValue("addDate").equals(aStrain.getDateString())) )
					aStrain.setDate(this.getFormValue("addDate"));
				if ( this.hasFormValue("project")  )
					aStrain.setProjectID(this.getFormValue("project"));
				if ( this.hasFormValue("status") )
					aStrain.setStatus(this.getFormValue("status"));
				aStrain.refresh();
				return "<DIV CLASS='success'>Strain Updated</DIV>";
			} catch (DataException e) {
				return this.handleException(e);
			}
		}
		return "";
	}
	
	public String showSpeciesForm(Strain aStrain) {
		StringBuffer output = new StringBuffer();

		
		try {
			TableRow tableRow = new TableRow(this.makeFormTextRow("Culture Source:", "culture_source", aStrain.getCultureSource()));
			tableRow.addItem(this.makeFormTextRow("Scientific Name:", "strainName", aStrain.getName()));

			tableRow.addItem(this.makeFormTextRow("Genus:", "genus", aStrain.getGenus()));

			tableRow.addItem(this.makeFormDateRow("Date Added:", "addDate", "strain", aStrain.getDateString()));
			tableRow.addItem(this.makeFormTextRow("Default Media:", "media_name", aStrain.getDefaultMedia()));

			Popup aPop = new Popup();
			aPop.setName("status");
			aPop.addItemWithLabel(Strain.GOOD_STATUS, "Good");
			aPop.addItemWithLabel(Strain.SLOW_GROWTH_STATUS, "Slow Growth");
			aPop.addItemWithLabel(Strain.CONTAMINATED_STATUS, "Contaminated");
			aPop.addItemWithLabel(Strain.REMOVED_STATUS, "Removed");
			aPop.addItemWithLabel(Strain.FIELD_HARVEST_STATUS, "Field Collection");
			aPop.setDefault(aStrain.getStatus());
			TableCell myCell = new TableCell("Culture Status:");
			myCell.addItem(aPop);
			tableRow.addItem(myCell);

			myCell = new TableCell("Project Code:");
			try {
				aPop = this.projectPopup();
				aPop.setName("project");
				String projectID = aStrain.getProjectID();
				if ( projectID != null )
					aPop.setDefault(projectID);
				myCell.addItem(aPop.toString());
			} catch (DataException e) {
				myCell.addItem("<B><FONT COLOR='red'>ERROR:</FONT> " + e.getMessage() + "</B>"); 
				e.printStackTrace();
			}
			tableRow.addItem(myCell);

			tableRow.addItem(this.makeFormTextAreaRow("Notes:", "notes", aStrain.getNotes()));

			myCell = new TableCell("<BUTTON TYPE=SUBMIT NAME='updateAction'>Update</BUTTON><BUTTON TYPE='RESET'>Reset Form</BUTTON>");
			myCell.setAttribute("colspan","2");
			myCell.setAttribute("align","center");
			tableRow.addItem(myCell);
			Table myTable = new Table(tableRow);
			myTable.setClass("species");
			myTable.setAttribute("width", "80%");
			myTable.setAttribute("align", "center");
			Form myForm = new Form(myTable);
			myForm.setName("strain");
			myForm.setAttribute("METHOD","POST");

			output.append(myForm.toString());
		} catch (DataException e) {
			return this.handleException(e);
		}
		return output.toString();
	}

	private String buildTaxonTable(boolean asForm, Strain aStrain) {
		String headers[] = {"Kingdom", "Phylum", "Class", "Order", "Family", "Genus"};
		TableCell taxCell = new TableHeader(headers);
		TableRow taxRow = new TableRow(taxCell);
		taxCell = new TableCell();
		try {
			Taxon myTaxon = aStrain.getTaxon();
			if ( myTaxon.first() ) {
				String[] links = {"kingdom", "phylum", "class", "ord", "family" };
				String[] values = {myTaxon.getKingdom(), myTaxon.getPhylum(), myTaxon.getTaxonClass(), myTaxon.getOrder(), myTaxon.getFamily() };
				for ( int i = 0; i < links.length; i++ ) {
					if ( values[i] == null ) {
						taxCell.addItem("");
					} else {
						taxCell.addItem(String.format("<A HREF='taxabrowser?%s=%s'>%s</A>",links[i],values[i], values[i]));				
					}
				}
				String myGenus = myTaxon.getGenus();
				if ( asForm ) taxCell.addItem(String.format("<INPUT TYPE=TEXT NAME='genus' VALUE='%s'/><BR><A HREF='taxabrowser?genus=%s'>Show genus</A>", myGenus, myGenus));
				else taxCell.addItem(String.format("<A HREF='taxabrowser?genus=%s'>%s</A>", myGenus, myGenus));
				if ( myTaxon.hasSynonym() ) {
					String mySyn = myTaxon.getSynonym().getGenus();
					taxCell.addItem(String.format("<A HREF='taxabrowser?genus=%s'>%s</A>", mySyn, mySyn));
				}
			} else {
				if ( asForm) taxCell.addItem(String.format("<INPUT TYPE=TEXT NAME='genus' VALUE='%s'/><BR><B>Genus not found in taxonomic database</B>", aStrain.getGenus()));
				else taxCell.addItem(String.format("%s<BR><B>Genus not found in taxonomic database</B>", aStrain.getGenus()));
				taxCell.setAttribute("COLSPAN", "6");
			} 
		} catch ( DataException e ) {
			return this.handleException(e);
		}
		taxRow.addItem(taxCell);
		Table taxTable = new Table(taxRow);
		return taxTable.toString();
	}

	public String photoList(Strain aStrain) {
		return this.photoList(aStrain, 1);
	}

	public String photoAlbum(Strain aStrain) {
		return this.photoList(aStrain, 3);
	}

	public String photoList(Strain aStrain, int cols) {
		Div aDiv = new Div(this.photoForm(aStrain, cols));
		aDiv.setID(PHOTO_FORM);
		return aDiv.toString();
	}
	
	public String photoForm(Strain aStrain, int cols) {
		StringBuffer output = new StringBuffer();
		if ( this.hasFormValue("photoBrowser") && (! this.hasFormValue("cancelBrowser")) ) {
			if ( this.hasFormValue("addPhotos") ) {
				output.append(this.addPhotos(aStrain));
				output.append(this.photoListContent(aStrain, cols));
			} else {
				return this.photoApplet(aStrain);
				// output.append(LOADING_DIV);
				// output.append(this.photoBrowser(aStrain, cols));
			}
		} else {
			output.append(this.photoListContent(aStrain, cols));
		}
		return output.toString();
	}

	private String addPhotos(Strain aStrain) {
		String[] images = this.getFormValues("image");
		StringBuffer output = new StringBuffer();
		CyanosConfig myConf = this.myWrapper.getAppConfig();
		String rootPath = myConf.getFilePath(Strain.DATA_FILE_CLASS, Strain.PHOTO_DATA_TYPE);				

		for (int i = 0; i < images.length; i++ ) {
			String aPath = this.getFormValue(String.format("%d_path", images[i]));
			String aDesc = this.getFormValue(String.format("%d_desc", images[i]));
			ExternalFile aFile = new SingleFile(rootPath, aPath);
			output.append("File: ");
			output.append(aPath);
			output.append(" -&gt; ");
			try {
				aFile.setDescription(aDesc);
				MagicMatch aMatch = Magic.getMagicMatch(aFile.getFileObject(), true);
				aFile.setMimeType(aMatch.getMimeType());
				aStrain.addPhoto(aFile);
				output.append("SUCCESS");
			} catch (DataException e) {
				output.append(String.format("ERROR: %s", e.getLocalizedMessage()));
				e.printStackTrace();
			} catch (MagicParseException e) {
				output.append(String.format("ERROR: %s", e.getLocalizedMessage()));
				e.printStackTrace();
			} catch (MagicMatchNotFoundException e) {
				output.append(String.format("ERROR: %s", e.getLocalizedMessage()));
				e.printStackTrace();
			} catch (MagicException e) {
				output.append(String.format("ERROR: %s", e.getLocalizedMessage()));
				e.printStackTrace();
			}
			output.append("<BR/>");
		}
		return this.message("", output.toString());
	}
	
	private String photoListContent(Strain aStrain, int cols) {
		StringBuffer output = new StringBuffer();
		TableRow myRow = new TableRow();
		myRow.setAttribute("align", "center");
		Table myTable = new Table(myRow);	
		myTable.setAttribute("WIDTH", "80%");
		myTable.setAttribute("ALIGN", "CENTER");
		TableCell myCell;
		try {
			ExternalFile photos = aStrain.getPhotos();
			if ( photos.first() ) {
				myCell = new TableCell();
				myRow.addItem(myCell);
				int cell = 1;
				photos.beforeFirst();
				while ( photos.next() ) {
					String picPath = String.format("%s/file/get/strain/photo/%s", this.myWrapper.getContextPath(), photos.getFilePath());
					String previewPath = String.format("%s/file/preview/strain/photo/%s", this.myWrapper.getContextPath(), photos.getFilePath());
					if ( cols > 1 )
						myCell.addItem(String.format("<A HREF='%s'><IMG SRC='%s' BORDER='0'><BR/>%s</A>", picPath, previewPath, photos.getDescription()));
					else 
						myCell.addItem(String.format("<A HREF='%s'><IMG SRC='%s' BORDER='0' ALIGN=middle>%s</A>", picPath, previewPath, photos.getDescription()));

					if (cell == cols) {
						cell = 1;
						myCell = new TableCell();
						myRow.addItem(myCell);
					} else {
						cell++;				
					}
				}
			} else {
				myRow.addItem(String.format("<TH COLSPAN='%d'>None</TH>", cols));
			}
		} catch (DataException e) {
			output.append(this.handleException(e));
		}
		output.append(myTable);

		if ( aStrain.isAllowed(Role.WRITE) )
			output.append(String.format("<FORM><P ALIGN='CENTER'><INPUT TYPE=HIDDEN NAME='id' VALUE='%s'/><BUTTON TYPE='BUTTON' NAME='photoBrowser' onClick=\"loadForm(this, '%s')\">Add Photos</BUTTON></P></FORM>", aStrain.getID(), PHOTO_FORM));
		return output.toString();
	}
	
	public String photoApplet(Strain aStrain) {
		Form myForm = new Form(DataForm.fileManagerApplet(this.myWrapper, "strain", aStrain.getID(), Strain.PHOTO_DATA_TYPE, true));				
		myForm.addItem(String.format("<P ALIGN='CENTER'><BUTTON TYPE='BUTTON' onClick=\"updateForm(this,'%s')\" NAME='cancelBrowser'>Close</BUTTON>", PHOTO_FORM));
		myForm.setAttribute("NAME", "photoBrowser");
		myForm.addHiddenValue("id", aStrain.getID());
		myForm.addHiddenValue("div", PHOTO_FORM);
		myForm.addHiddenValue("photoBrowser", "1");

		return myForm.toString();
	}
	
	@SuppressWarnings("unchecked")
	@Deprecated
	public String photoBrowser(Strain aStrain, int cols) {
		TableRow myRow = new TableRow();
		myRow.setAttribute("align", "center");
		Table myTable = new Table(myRow);	
		myTable.setAttribute("WIDTH", "80%");
		myTable.setAttribute("ALIGN", "CENTER");
		TableCell myCell;
		Map<String,String> photoDesc = new HashMap<String,String>();

		try {
			ExternalFile photos = aStrain.getPhotos();
			if ( photos.first() ) {
				photos.beforeFirst();
				while ( photos.next() ) {
					photoDesc.put(photos.getFilePath(), photos.getDescription());
				}
			}
		} catch (DataException e) {
			return this.handleException(e);
		}
			
		File currentPath;
		CyanosConfig myConf = this.myWrapper.getAppConfig();
		String rootPath = myConf.getFilePath(Strain.DATA_FILE_CLASS, Strain.PHOTO_DATA_TYPE);				
		FileRoot thisRoot = new FileRoot(rootPath);
		
		if ( this.hasFormValue("path") ) {
			currentPath = new File(rootPath, this.getFormValue("path"));
		} else {
			currentPath = new File(rootPath);
		}

		File[] kids = currentPath.listFiles();

		Arrays.sort(kids, DataForm.directoryFirstCompare());
		String contextPath = this.myWrapper.getContextPath();

		int imageIndex = 1;
		int cell = 1;
		if ( ! thisRoot.isRoot(currentPath) ) {
			myRow.addItem(String.format("<TD COLSPAN='%d' ALIGN='CENTER'><IMG SRC='%s/images/folder.png' HEIGHT=32 VALIGN='middle' /> <B>%s</B><HR/></TD>", cols, contextPath, currentPath.getName()));
			myCell = new TableCell(String.format("<A NAME='aPath' onClick=\"refreshForm('%s','path','%s', document.forms['photoBrowser'])\"><IMG SRC='%s/images/up-folder2.png' BORDER=0/><BR/>Parent Folder", PHOTO_FORM, thisRoot.chrootPath(currentPath.getParent()), contextPath ));	
			if (cell == cols) {
				cell = 1;
				myCell = new TableCell();
				myRow.addItem(myCell);
			} else {
				cell++;				
			}
		} else {
			myCell = new TableCell();			
		}
			
		myRow.addItem(myCell);

		for ( int i = 0; i < kids.length; i++ ) {
			if ( kids[i].isDirectory() ) {
				myCell.addItem(String.format("<A NAME='aPath' onClick=\"refreshForm('%s','path','%s', document.forms['photoBrowser'])\"><IMG SRC='%s/images/folder.png' BORDER=0/><BR/>%s", PHOTO_FORM, thisRoot.chrootFile(kids[i]), contextPath, kids[i].getName()));
			} else {
				try {
					String relPath = thisRoot.chrootFile(kids[i]);
					String fileName = relPath.replaceFirst("^/+", "");
					String picPath = String.format("%s/file/preview/strain/photo/%s", contextPath, fileName);
					if ( photoDesc.containsKey(relPath) ) {
						myCell.addItem(String.format("<IMG SRC='%s' BORDER='0'><BR/>%s", picPath, photoDesc.get(relPath)));								
					} else {
						MagicMatch aMatch = Magic.getMagicMatch(kids[i], true);
						String mimeType = aMatch.getMimeType();
						if ( mimeType.startsWith("image") ) {
							String title = String.format("%s (%s)", kids[i].getName(), DataForm.humanReadableSize(kids[i].length()));
							myCell.addItem(String.format("<IMG SRC='%s' BORDER='0'><BR/>%s<BR/>Add <INPUT TYPE='CHECKBOX' NAME='image' VALUE='%d' onClick=\"if ( this.checked ) { this.form.elements['%d_desc'].disabled = false; } else { this.form.elements['%d_desc'].disabled = true; }\"><INPUT TYPE='HIDDEN' NAME='%d_path' VALUE=\"%s\"><INPUT TYPE='TEXT' NAME='%d_desc' disabled='true'>", picPath, title, imageIndex, imageIndex, imageIndex, imageIndex, relPath, imageIndex));
							imageIndex++;
						} else {
							cell--;
						}
					}
				} catch (MagicParseException e) {
					myCell.addItem(this.handleException(e));
				} catch (MagicMatchNotFoundException e) {
					myCell.addItem(this.handleException(e));
				} catch (MagicException e) {
					myCell.addItem(this.handleException(e));
				}
			}
			if (cell == cols) {
				cell = 1;
				myCell = new TableCell();
				myRow.addItem(myCell);
			} else {
				cell++;				
			}
		}
				
		Form myForm = new Form(String.format("<P ALIGN='CENTER'><BUTTON TYPE='BUTTON' onClick=\"updateForm(this,'%s')\" NAME='cancelBrowser'>Cancel</BUTTON>", PHOTO_FORM));

		if ( imageIndex > 1 ) {
			myForm.addItem(String.format("<BUTTON TYPE='BUTTON' onClick=\"updateForm(this,'%s')\" NAME='addPhotos'>Add Photos</BUTTON></P>", PHOTO_FORM));
		} else {
			myForm.addItem("</P>");
		}
		myForm.addItem(myTable);
		myForm.setAttribute("NAME", "photoBrowser");
		myForm.addHiddenValue("id", aStrain.getID());
		myForm.addHiddenValue("div", PHOTO_FORM);
		myForm.addHiddenValue("photoBrowser", "1");

		return myForm.toString();
	}

	public String urlList(Strain aStrain) {
		StringBuffer output = new StringBuffer("<P ALIGN='CENTER'>");
		try {
			ExternalFile urls = aStrain.getURLs();
			if ( urls.first() ) {
				urls.beforeFirst();
				while ( urls.next() ) {
					output.append(String.format("<A HREF=\"%s\" TARGET='_blank'>%s</A><BR/>", urls.getFilePath(), urls.getDescription()));
				}
			} else {
				output.append("NONE");
			}
		} catch (DataException e) {
			output.append(this.handleException(e));
		}
		output.append(String.format("</P><P ALIGN='CENTER'><A onClick=\"setLink('strain','%s')\">Add Link</A></P>",Strain.URL_DATA_TYPE));
		return output.toString();
	}

	public Div strainViewDiv(Strain aStrain) {
		Div strainDiv = new Div();
		Div viewDiv = new Div(this.showSpeciesText(aStrain));
		viewDiv.setID("view_straininfo");
		Div editDiv = new Div(this.showSpeciesForm(aStrain));
		editDiv.setID("edit_straininfo");
		viewDiv.setClass("showSection");
		editDiv.setClass("hideSection");
		viewDiv.addItem("<P ALIGN='CENTER'><BUTTON TYPE='BUTTON' onClick='flipDiv(\"straininfo\")'>Edit Values</BUTTON></P>");
		editDiv.addItem("<P ALIGN='CENTER'><BUTTON TYPE='BUTTON' onClick='flipDiv(\"straininfo\")'>Close Form</BUTTON></P>");
		strainDiv.addItem(viewDiv);
		strainDiv.addItem(editDiv);

		return strainDiv;
	}
	
	public Div inocDiv(Strain aStrain) {
		return this.collapsableDiv(INOC_DIV_ID, INOC_DIV_TITLE, this.sourceList(aStrain));
	}
	
	public String sourceList(Strain aStrain) {
		StringBuffer output = new StringBuffer();

		try {
			Form buttonForm = new Form("<INPUT TYPE='HIDDEN' NAME='id' VALUE='" + aStrain.getID() + "'/>");
			buttonForm.addItem("<P ALIGN=CENTER>Show: ");
			Popup showPop = new Popup();
			showPop.setName("show");
			showPop.addItemWithLabel("inocs", "Inoculations");
			showPop.addItemWithLabel("field", "Field Collections");

			String showType = this.getFormValue("show");
			if ( ! this.hasFormValue("show") ) {
				if ( "Field Collection".equals(aStrain.getStatus()))
					showType = "field";
				else showType = "inoc";
			}
			showPop.setDefault(showType);
			showPop.setAttribute("onChange", String.format("reloadDiv('%s',this)", INOC_DIV_ID));
			buttonForm.addItem(showPop.toString());
			output.append(buttonForm.toString());
			
				
			if ( showType.equals("field") ) {
				output.append(this.collectionList(aStrain));
			} else {
				output.append(this.inoculationList(aStrain));
			}
		} catch (DataException e) {
			output.append(this.handleException(e));
		}
		
		return output.toString();

	}

	public String collectionList(Strain aStrain) {
		StringBuffer output = new StringBuffer();

		try {			
			Collection colList = aStrain.getFieldCollections();
			String headers[] = { "ID", "Date", "Location", "Coordinates", "Collector", "Notes" };
			TableCell myCell = new TableHeader(headers);
			myCell.setClass("header");
			TableRow tableRow = new TableRow(myCell);
			SimpleDateFormat myDateFormat = this.dateFormat();
			String curClass = "odd";
			Table myTable = new Table(tableRow);
			myTable.setAttribute("class","dashboard");
			myTable.setAttribute("align","center");
			myTable.setAttribute("width","75%");
			if ( colList != null ) {
				colList.beforeFirst();

				while (colList.next()) {
					myCell = new TableCell(String.format("<A HREF=\"collection?col=%s\">%s</A>",colList.getID(),colList.getID()));
					myCell.addItem(myDateFormat.format(colList.getDate()));
					myCell.addItem(colList.getLocationName());
					myCell.addItem(colList.getLatitudeDM() + "<BR/>" + colList.getLongitudeDM());
					myCell.addItem(colList.getCollector());
					myCell.addItem(BaseForm.shortenString(colList.getNotes(), 75));
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
				myTable.addItem("<TR ALIGN='CENTER'><TD COLSPAN='6'><B>No field collections for this strain.</B></TD></TR>");
			}
			myTable.addItem("<TR ALIGN='CENTER'><TD COLSPAN=6><INPUT TYPE=SUBMIT NAME='action' VALUE='Harvest'/><INPUT TYPE='RESET'/></TD></TR>");
			Form myForm = new Form("<INPUT TYPE='HIDDEN' NAME='strain' VALUE='" + aStrain.getID() + "'/>");
			myForm.addItem(myTable);
			myForm.setName("strain");
			myForm.setAttribute("METHOD","POST");
			myForm.setAttribute("ACTION","harvest/collection");
			output.append(myForm.toString());

		} catch (DataException e) {
			e.printStackTrace();
			output.append("<FONT COLOR='red'><B>SQL FAILURE</B></FONT> " + e.getMessage());
		}
		
		return output.toString();

	}
	
	public String inoculationList(Strain aStrain) {
		StringBuffer output = new StringBuffer();

		try {			
			if (aStrain.isActive() ) {
				Form addForm = new Form("<INPUT TYPE='HIDDEN' NAME='cultureid1' VALUE='" + aStrain.getID() + "'/>");
				addForm.addItem("<INPUT TYPE='HIDDEN' NAME='rows' VALUE='1'/>");
				addForm.addItem("<P ALIGN='CENTER'><INPUT TYPE=SUBMIT NAME='button' VALUE='Add New Inoculations'/></P>");
				addForm.setName("addInoc");
				addForm.setAttribute("METHOD","POST");
				addForm.setAttribute("ACTION","inoc/add");
				output.append(addForm.toString());
			}

			String[] headerData = {"", "Date", "Media", "Volume", "Notes", ""};
			TableCell header = new TableHeader(headerData);

			Form aForm = new Form("<INPUT TYPE='HIDDEN' NAME='id' VALUE='" + aStrain.getID() + "'/>");
			aForm.addItem("<INPUT TYPE='HIDDEN' NAME='tab' VALUE='inoc'/>");
			aForm.setAttribute("METHOD","POST");
			aForm.addItem("<P ALIGN='CENTER'>");

			boolean hideRemoved = ! this.hasFormValue("showRemoved");
			if ( hideRemoved ) {
				aForm.addItem(String.format("<INPUT TYPE='CHECKBOX' NAME='showRemoved' VALUE='showRemoved' onClick=\"reloadDiv('%s', this)\" />", INOC_DIV_ID));
			} else {
				aForm.addItem(String.format("<INPUT TYPE='CHECKBOX' NAME='showRemoved' VALUE='showRemoved' CHECKED onClick=\"reloadDiv('%s', this)\" />", INOC_DIV_ID));
			}
			aForm.addItem(" Show removed/killed inoculations</P>");

			output.append(aForm.toString());

			/* String sqlString = new String("SELECT * FROM inoculation WHERE culture_id=\"" + 
			aStrain.getId() + "\" AND (fate IS NULL OR fate != 'harvest') ORDER BY date"); */

			header.setAttribute("class","header");
			TableRow tableRow = new TableRow(header);
			Table myTable = new Table(tableRow);
			myTable.setAttribute("class","dashboard");
			myTable.setAttribute("align","center");
			myTable.setAttribute("width","75%");

			Inoc anInoc = aStrain.getInoculations();
			if ( anInoc != null ) {
				anInoc.beforeFirst();
				String curClass = "odd";
				SimpleDateFormat myDate = this.dateFormat();
				while (anInoc.next()) {
					if ( hideRemoved && anInoc.getRemoveDate() != null) continue;
					TableCell myCell = new TableCell();
					myCell.addItem("<A HREF='inoc?id=" + anInoc.getID() + "'>View</A>");
					myCell.addItem(myDate.format(anInoc.getDate()));
					myCell.addItem(anInoc.getMedia());
					myCell.addItem(anInoc.getVolumeString(5.0f));
					myCell.addItem(BaseForm.shortenString(anInoc.getNotes(), 12));
					String myFate = anInoc.getFate();
					if ( myFate == null ) {
						myCell.addItem("<INPUT TYPE=CHECKBOX NAME='inoc' VALUE='" + anInoc.getID() + "'/>");
					} else if ( myFate.equals("stock") ) {
						Date remDate = anInoc.getRemoveDate();
						if ( remDate == null ) {
							myCell.addItem("Stock");
						} else {
							myCell.addItem("Removed");
						}
					} else if ( myFate.equals("cryo") ) {
						myCell.addItem("Cryopreservation");
					} else if ( myFate.equals("harvest") ) {
						myCell.addItem("<A HREF='harvest?id=" + anInoc.getHarvest().getID() + "'>Harvested</A>");
					} else if ( myFate.equals("dead") ){
						myCell.addItem("Removed (" + myDate.format(anInoc.getRemoveDate()) + ")");
					} else {
						myCell.addItem(myFate);
					}

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

				if ( aStrain.isActive() ) {
					myTable.addItem("<TR ALIGN='CENTER'><TD COLSPAN=5><INPUT TYPE=SUBMIT NAME='action' VALUE='Harvest'/><INPUT TYPE='RESET'/></TD><TD><INPUT TYPE=SUBMIT NAME='killInoc' VALUE='Kill'/></TD></TR>");
					Form myForm = new Form("<INPUT TYPE='HIDDEN' NAME='strain' VALUE='" + aStrain.getID() + "'/>");
					myForm.addItem(myTable);
					myForm.setName("strain");
					myForm.setAttribute("METHOD","POST");
					myForm.setAttribute("ACTION","inoc/harvest");
					output.append(myForm.toString());
				} else {
					output.append(myTable.toString());
				}
			} else {
				myTable.addItem("<TR ALIGN='CENTER'><TD COLSPAN='6'><B>No Inocluations for Strain.</B> ");
				myTable.addItem("</TD></TR>");
				output.append(myTable.toString());
			}
		} catch (DataException e) {
			output.append(this.handleException(e));
		}
		
		return output.toString();
	}

	public String strainJScript() {
		StringBuffer script = new StringBuffer("var strains = new Array();\n");
		try {
			Strain results = SQLStrain.strains(this.getSQLDataSource(), "CAST(culture_id as UNSIGNED)", SQLStrain.ASCENDING_SORT);

			results.beforeFirst();
			while (results.next()) {
				String projectID = results.getProjectID();
				if ( projectID == null ) projectID = "";
				script.append(String.format("strains[\"%s\".toLowerCase()]=\"%s\";\n", results.getID(), projectID));
			}
			script.append("function checkStrain(field) {\n if ( field.value != '' && field.value != null && strains[field.value.toLowerCase()] == null ) { " +
					"field.value=prompt('Strain not found!\\nPlease enter a valid strain ID.',field.value); \n" +
			"checkStrain(field);\n} \n}\n\n");
			script.append("function strainProject(field) {\n return strains[field.value.toLowerCase()];\n} \n\n");
		} catch (DataException e) {
			script.append(String.format("function checkStrain(field) {\n alert(\"Could not load strain list\n%s\") \n}\n\n", e.getLocalizedMessage()));
			script.append(String.format("function strainProject(field) {\n alert(\"Could not load strain list\n%s\") \n}\n\n", e.getLocalizedMessage()));
			e.printStackTrace();
		}
		return script.toString();	
	}

	public String killStrainForm(Strain aStrain) {
		TableRow tableRow = new TableRow();

		tableRow.addItem(this.makeFormDateRow("Date Removed:", "date", "strain"));
		tableRow.addItem(this.makeFormTextRow("Reason:", "reason"));
		tableRow.addItem("<TD COLSPAN='2' ALIGN='center'><INPUT TYPE=SUBMIT NAME='actionRemove' VALUE='Remove Strain'/><INPUT TYPE='RESET'/></TD>");

		Table myTable = new Table(tableRow);
		myTable.setClass("species");
		myTable.setAttribute("align", "center");
		Form myForm = new Form(myTable);
		myForm.setName("strain");
		myForm.setAttribute("METHOD","POST");
		return myForm.toString();
	}
	
	public String killStrain() {
		try {
			Strain aStrain = new SQLStrain(this.getSQLDataSource(), this.getFormValue("id"));
			if ( this.hasFormValue("actionRemove") ) {
				StyledText subtitle = new StyledText("<CENTER>");
				subtitle.setSize("+2");
				try {
					if ( aStrain.first() )  {
						aStrain.setManualRefresh();
						aStrain.setRemoveDate(this.getFormValue("date"));
						aStrain.setRemoveReason(this.getFormValue("reason"));
						aStrain.refresh();
						subtitle.setColor("#00AA00");
						subtitle.addBoldString("Strain Removed");
					} else {
						subtitle.setColor("red");
						subtitle.addBoldString("Strain NOT found!");
					}
				} catch (DataException e) {
					e.printStackTrace();
					subtitle.setColor("red");
					subtitle.addBoldString("Update Failure: ");
					subtitle.addString(e.getMessage());
				}

				subtitle.addString("</CENTER>");		
				return subtitle.toString() + 
				"<P ALIGN='CENTER'><A HREF='strain?id=" + this.getFormValue("id") + "'>View Strain</A></P>";
			} else {
				return this.killStrainForm(aStrain);
			}
		} catch (DataException e) {
			e.printStackTrace();
			return ("<FONT COLOR='red'><B>ERROR:</B></FONT> " + e.getMessage());
		}
	}

	public String summaryTable() {
		TableHeader myHeader = new TableHeader("<P ALIGN='CENTER'><FONT SIZE='+1'>Strain Information</FONT></P>");
		TableRow myRow = new TableRow(myHeader);
		Table myTable = new Table(myRow);
		myTable.setAttribute("align", "center");
		TableCell myCell;

		try {
			myRow.addItem(String.format("<TD>Number of strains:</TD><TD>%d</TD>", SQLStrain.numberOfStrains(this.getSQLDataSource())));
			myRow.addItem(String.format("<TD>Number of taxonomic orders:</TD><TD>%d</TD>", SQLStrain.numberOfTaxa(this.getSQLDataSource(), SQLTaxon.ORDER)));

			HtmlList myList = new HtmlList();
			myList.unordered();
			Map<String, Integer> taxa = SQLStrain.countForTaxa(this.getSQLDataSource(), SQLTaxon.ORDER);
			Iterator<Entry<String,Integer>> anIter = taxa.entrySet().iterator();
			while ( anIter.hasNext() ) {
				Entry<String,Integer> anEntry = anIter.next();
				myList.addItem(String.format("%s: %d", anEntry.getKey(), anEntry.getValue()));
			}
			myCell = new TableCell(myList);
			myCell.setAttribute("colspan", "0");
			myRow.addItem(myCell);

		} catch (DataException e) {
			myRow.addItem("<TD COLSPAN='2' ALIGN='CENTER'><FONT COLOR='red'><B>ERROR:</FONT>" + e.getMessage() + "</B></TD>");
			e.printStackTrace();
		}
		Form queryForm = new Form();
		queryForm.setName("query");
		queryForm.setAttribute("action", "strain");
		Input searchField = new Input("text");
		searchField.setName("query");
		queryForm.addItem(searchField);
		queryForm.addSubmit("Search");
		myCell = new TableCell(queryForm);
		myCell.setAttribute("colspan", "0");
		myCell.setAttribute("align", "center");
		myRow.addItem(myCell);
		
		myCell = new TableCell("<A HREF='strain'>View strain list</A>");
		myCell.setAttribute("colspan", "0");
		myCell.setAttribute("align", "center");
		myRow.addItem(myCell);
		
		return myTable.toString();
	}

	public static Popup strainPopup(Strain strainList) throws DataException {
		Popup strainPop = new Popup();
		strainPop.addItem("");
		if ( strainList != null ) {
			strainList.beforeFirst();
			strainList.beforeFirst();
			while ( strainList.next() ) {
				strainPop.addItemWithLabel(strainList.getID(), String.format("%s <I>%s</I>", strainList.getID(), strainList.getName()));
			}
		}
		return strainPop;	
	}
}
