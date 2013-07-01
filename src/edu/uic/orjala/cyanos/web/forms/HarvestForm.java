/**
 * 
 */
package edu.uic.orjala.cyanos.web.forms;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import edu.uic.orjala.cyanos.Collection;
import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.Harvest;
import edu.uic.orjala.cyanos.Inoc;
import edu.uic.orjala.cyanos.Sample;
import edu.uic.orjala.cyanos.SampleAccount;
import edu.uic.orjala.cyanos.Strain;
import edu.uic.orjala.cyanos.sql.SQLCollection;
import edu.uic.orjala.cyanos.sql.SQLHarvest;
import edu.uic.orjala.cyanos.sql.SQLInoc;
import edu.uic.orjala.cyanos.sql.SQLStrain;
import edu.uic.orjala.cyanos.web.BaseForm;
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
 * @author George Chlipala
 *
 */
public class HarvestForm extends BaseForm {

	public static final String DIV_TITLE = "Harvests";
	public static final String DIV_ID = "harvestInfo";
	
	public static final String SOURCE_DIV = "sourceDiv";
	
	public static final String ADD_HARVEST_ACTION = "addHarvest";

	/**
	 * @param callingServlet
	 */
	public HarvestForm(CyanosWrapper aWrapper) {
		super(aWrapper);
	}

	public String harvestList(Harvest harvests) {
		StringBuffer output = new StringBuffer();
		String[] headerData = {"Date", "Color", "Type", "Cell Mass", "Volume", "Notes",""};
		TableCell header = new TableHeader(headerData);
		header.setAttribute("class","header");
		TableRow tableRow = new TableRow(header);
		Table myTable = new Table(tableRow);
		myTable.setAttribute("class","dashboard");
		myTable.setAttribute("align","center");
		myTable.setAttribute("width","75%");
		
		try {
			if ( harvests != null && harvests.first() ) {
				harvests.beforeFirst();
				String curClass = "odd";
				SimpleDateFormat myDate = this.dateFormat();
				while (harvests.next()) {
					TableCell myCell = new TableCell();
					myCell.addItem(myDate.format(harvests.getDate()));
					myCell.addItem(harvests.getColor());
					myCell.addItem(harvests.getType());
					Float cellMass = harvests.getCellMass();
					myCell.addItem((cellMass != null ? 
							BaseForm.formatAmount("%.2f %s", cellMass.floatValue(), "g")	: ""));
					Float mediaVolume = harvests.getMediaVolume();
					myCell.addItem((mediaVolume != null ?
							BaseForm.formatAmount("%.1f %s", mediaVolume.floatValue(), "L") : ""));
					myCell.addItem(harvests.getNotes());
					myCell.addItem("<A HREF=harvest?id=" + harvests.getID() + ">View Harvest</A>");
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
				myTable.addItem("<TR ALIGN='CENTER'><TH COLSPAN='5'>None</TH></TR>");
			}
		} catch (DataException e) {
			myTable.addItem("<TR ALIGN='CENTER'><TD COLSPAN='5'>");
			myTable.addItem(this.handleException(e));
			myTable.addItem("</TD></TR>");
		}

		Form myForm = new Form(myTable);
		myForm.setName("harvest");
		myForm.setAttribute("METHOD","POST");
		myForm.setAttribute("ACTION","harvest");
		output.append(myForm.toString());
		return output.toString();
	}

	public Div harvestDiv(Harvest harvests) {
		return this.collapsableDiv(DIV_ID, DIV_TITLE, this.harvestList(harvests));
	}
	
	public Div harvestDiv(Strain aStrain) {
		try {
			Harvest harvests = SQLHarvest.harvestsForStrain(this.getSQLDataSource(), aStrain);
			return this.collapsableDiv(DIV_ID, DIV_TITLE, this.harvestList(harvests));
		} catch (DataException e) {
			return this.collapsableDiv(DIV_ID, DIV_TITLE, this.handleException(e));
		}
	}
	
	public String harvestList(Strain aStrain) {
		try {
			Harvest harvests = SQLHarvest.harvestsForStrain(this.getSQLDataSource(), aStrain);
			return this.harvestList(harvests);
		} catch (DataException e) {
			return this.handleException(e);
		}
	}
	
	public Div harvestDiv() {
		return this.loadableDiv(DIV_ID, DIV_TITLE);
	}

	public String addHarvest(Collection aCollection) {
		try {
			Harvest aHarvest = aCollection.createHarvest(this.getFormValue("strain"));
			if ( aHarvest.first() ) {
				aHarvest.setManualRefresh();
				aHarvest.setColor(this.getFormValue("color"));
				aHarvest.setType(this.getFormValue("type"));
				aHarvest.setNotes(this.getFormValue("notes"));
				aHarvest.setCellMass(this.getFormValue("cell_mass"));
				aHarvest.setDate(this.getFormValue("date"));
				aHarvest.setProjectID(this.getFormValue("project"));
				aHarvest.refresh();
				aHarvest.setAutoRefresh();				
				return this.message(SUCCESS_TAG, String.format("Added a new harvest (serial # %s)", aHarvest.getID()));
			} else {
				return this.message(FAILED_TAG, "Could not create harvest record!");
			}
		} catch (DataException e) {
			return this.handleException(e);
		}
	}
	
	private TableRow addCollectionHarvestForm() {
		Div sourceDiv = new Div("<P ALIGN='CENTER'><B>Source Collection</B></P>");
		sourceDiv.setID("sourceDiv");

		try {
			sourceDiv.addItem(String.format("<INPUT TYPE=HIDDEN NAME='col' VALUE='%s'/>", this.getFormValue("col")));
			sourceDiv.addItem(this.collectionList(new SQLCollection(this.getSQLDataSource(), this.getFormValue("col"))));
		} catch (DataException e) {
			sourceDiv.addItem(this.handleException(e));
		}

		TableCell myCell = new TableCell(sourceDiv);
		myCell.setAttribute("COLSPAN", "2");
		TableRow tableRow = new TableRow(myCell);

		myCell = new TableCell("Strain:");
		myCell.addItem(String.format("<INPUT TYPE='TEXT' NAME='strain'  onChange='setValue(this.form.project, strainProject(this));' VALUE='%s'/>", this.getFormValue("strain")));
		tableRow.addItem(myCell);

		myCell = new TableCell("Project:");
		try {
			Popup projectPop = this.projectPopup();
			projectPop.setName("project");
			String projectID = null;
			if ( this.hasFormValue("col") ) {
				Collection aCol = new SQLCollection(this.getSQLDataSource(), this.getFormValue("col"));
				projectID = aCol.getProjectID();
			} 
			if ( projectID != null ) 
				projectPop.setDefault(projectID);
			myCell.addItem(projectPop.toString());
		} catch (DataException e) {
			myCell.addItem("<B><FONT COLOR='red'>ERROR:</FONT> " + e.getMessage() + "</B></P>");
		}
		tableRow.addItem(myCell);
		return tableRow;
		

	}

	private TableRow addInocHarvestForm() {
		TableCell myCell = new TableCell("Strain:");
		myCell.addItem(String.format("<INPUT TYPE='TEXT' NAME='strain'  onChange='checkStrain(this); window.document.getElementById(\"sourceDiv\").innerHTML = \"\";  this.form.getSource.disabled = 0; setValue(this.form.project, strainProject(this));' VALUE='%s'/>", this.getFormValue("strain")));
		TableRow tableRow = new TableRow(myCell);
		
		myCell = new TableCell("<BUTTON TYPE='BUTTON' NAME='getSource' onClick=\"loadForm(this, 'sourceDiv')\">Get Inoculations</BUTTON>");
		myCell.setAttribute("COLSPAN", "2");
		myCell.setAttribute("ALIGN", "CENTER");
		tableRow.addItem(myCell);		
		
		Div sourceDiv = new Div();
		sourceDiv.setID("sourceDiv");

		try {
			if ( this.hasFormValue("strain") )
				sourceDiv.addItem(this.inocForm(SQLInoc.openInocsForStrain(this.getSQLDataSource(), this.getFormValue("strain"))));
			sourceDiv.addItem("");
		} catch (DataException e) {
			sourceDiv.addItem(this.handleException(e));
		}
		
		myCell = new TableCell(sourceDiv);
		myCell.setAttribute("COLSPAN", "2");
		tableRow.addItem(myCell);

		myCell = new TableCell("Project:");
		try {
			Popup projectPop = this.projectPopup();
			projectPop.setName("project");
			String projectID = null;
			if ( this.hasFormValue("strain") ) {
				Strain aStrain = new SQLStrain(this.getSQLDataSource(), this.getFormValue("strain"));
				projectID = aStrain.getProjectID();
			}
			if ( projectID != null ) 
				projectPop.setDefault(projectID);
			myCell.addItem(projectPop.toString());
		} catch (DataException e) {
			myCell.addItem("<B><FONT COLOR='red'>ERROR:</FONT> " + e.getMessage() + "</B></P>");
		}
		tableRow.addItem(myCell);
		return tableRow;

	}
		
	public Form addHarvestForm() {
		Table myTable = new Table();

		if ( this.hasFormValue("col") ) {
			myTable.addItem(this.addCollectionHarvestForm());
		} else {
			myTable.addItem(this.addInocHarvestForm());
		}
		myTable.addItem(this.addFormCommon());
		
		myTable.setClass("species");
		myTable.setAttribute("align", "center");
		myTable.setAttribute("WIDTH", "80%");

		StrainForm aForm = new StrainForm(this.myWrapper);
		
		Form myForm = new Form("<SCRIPT LANGUAGE='Javascript'>\n//<!--\n");
		myForm.addItem(aForm.strainJScript());
		myForm.addItem("\n//--></SCRIPT>\n");
		myForm.addItem(myTable);
		myForm.setName("harvest");
		myForm.setAttribute("METHOD", "POST");
		
		return myForm;
	}

	private TableRow addFormCommon() {


		TableRow tableRow = new TableRow(this.makeFormDateRow("Harvest Date:", "date", "harvest"));
		tableRow.addItem(this.makeFormTextRow("Color:", "color"));

		TableCell myCell = new TableCell("Type");
		myCell.addItem(this.types());
		tableRow.addItem(myCell);
		
		tableRow.addItem(this.makeFormTextRow("Cell Mass:", "cell_mass"));

		myCell = new TableCell("Notes");
		myCell.addItem("<TEXTAREA NAME=\"notes\" COLS=40 ROWS=5></TEXTAREA>");
		tableRow.addItem(myCell);
		
		myCell = new TableCell(String.format("<BUTTON TYPE=SUBMIT NAME='%s'>Add Harvest</BUTTON><BUTTON TYPE=RESET>Reset Form</BUTTON><BR/><BUTTON TYPE=SUBMIT NAME='return'>Cancel</BUTTON>", ADD_HARVEST_ACTION));
		myCell.setAttribute("colspan","2");
		myCell.setAttribute("align","center");
		tableRow.addItem(myCell);

		return tableRow;
	}

	private String types() {
		return "<INPUT TYPE=CHECKBOX NAME='type' VALUE='F'/> Filamentous<BR/>" +
		"<INPUT TYPE=CHECKBOX NAME='type' VALUE='E'/> Encrusting<BR/>" +
		"<INPUT TYPE=CHECKBOX NAME='type' VALUE='P'/> Planktonic";
	}

	public String makeExtract(Harvest myHarvest) {
		if ( this.getFormValue("makeExtract").equals("Create New Extract") ) {
			try {
				Sample newExtract = myHarvest.createExtract();
				if ( newExtract != null && newExtract.first() ) {
					newExtract.setManualRefresh();
					newExtract.setCollectionID(this.getFormValue("collection"));
					newExtract.setName(this.getFormValue("name"));
					newExtract.setNotes(this.getFormValue("sampleNotes"));
					newExtract.setVialWeight(this.getFormValue("vial_wt"));
					newExtract.setDate(this.getFormValue("date"));
					newExtract.setProjectID(this.getFormValue("project"));
					String vals[] = this.getFormValue("amount").split(" ");
					if ( vals.length == 2 )
						newExtract.setBaseUnit(vals[1]);
					else
						newExtract.setBaseUnit("mg");
					String myProtocol = this.getFormValue("protocol");
					if ( myProtocol != null && (! myProtocol.equals("")) ) {
						Map protocol = this.loadDataTemplate("extract protocol", myProtocol);
						newExtract.setExtractSolvent((String)protocol.get("solvent"));
						newExtract.setExtractType((String)protocol.get("type"));
					}
					newExtract.refresh();
					SampleAccount txnAccount = newExtract.getAccount();
					txnAccount.addTransaction();
					txnAccount.setDate(this.getFormValue("date"));
					txnAccount.setNotes(this.getFormValue("txnNotes"));
					txnAccount.depositAmount(this.getFormValue("amount"));
					txnAccount.setTransactionReference(myHarvest);
					txnAccount.updateTransaction();
					String location = newExtract.getLocation();
					if ( location != null ) location = "Location: " + location;
					else location = "";
					return String.format("<B>New Extract</B>: <A HREF='sample?id=%s'>%s</A> %s (%s) Collection: %s %s",newExtract.getID(), newExtract.getName(), 
							BaseForm.formatAmount("%.2f %s", newExtract.accountBalance(), newExtract.getBaseUnit()), 
							this.dateFormat().format(newExtract.getDate()), newExtract.getCollectionID(), location);	
				} else 
					return "<FONT COLOR='red'><B>Extract creation failed!</B></FONT><BR/>" + this.makeExtractForm(myHarvest);
			} catch (DataException e) {
				return this.handleException(e);
			}
		} else {
			return this.makeExtractForm(myHarvest);
		}
	}
	
	public String makeExtractForm(Harvest myHarvest) {
		try {
			Paragraph head = new Paragraph();
			head.setAlign("CENTER");
			StyledText title = new StyledText("Make a new extract");
			title.setSize("+1");
			head.addItem(title);
			Form myForm = new Form(head);
			myForm.setAttribute("METHOD", "POST");
			myForm.setName("makeExtract");
			myForm.addItem("<INPUT TYPE=HIDDEN NAME='id' VALUE='" + myHarvest.getID() + "'/>");

			TableRow tableRow = new TableRow(this.makeFormDateRow("Date:", "date", "makeExtract"));

			TableCell myCell = new TableCell("Collection:");
			try {
				Popup aPop = this.sampleCollectionPopup();
				aPop.setName("collection");
				if ( this.hasFormValue("collection") ) aPop.setDefault(this.getFormValue("collection"));
				myCell.addItem(aPop);
			} catch (DataException e) {
				myCell.addItem(this.handleException(e));
			}

			tableRow.addItem(myCell);
			if ( ! this.hasFormValue("name") ) 
				tableRow.addItem(this.makeFormTextRow("Sample Label:", "name", myHarvest.getStrainID()));
			else 
				tableRow.addItem(this.makeFormTextRow("Sample Label:", "name"));
			tableRow.addItem(this.makeFormTextRow("Vial weight:", "vial_wt"));
			tableRow.addItem(this.makeFormTextRow("Amount:", "amount"));
			
			myCell = new TableCell("Project:");
			try {
				Popup projectPop = this.projectPopup();
				projectPop.setName("project");
				String myProject = myHarvest.getProjectID();
				if ( myProject != null ) {
					projectPop.setDefault(myProject);
				}
				myCell.addItem(projectPop.toString());
			} catch (DataException e) {
				myCell.addItem("<B><FONT COLOR='red'>ERROR:</FONT> " + e.getMessage() + "</B>"); 
			}
			tableRow.addItem(myCell);
			
			myCell = new TableCell("Extraction protocol:");
			try {
				Popup aPop = this.protocolPopup("extract protocol");
				aPop.setName("protocol");
				if ( this.hasFormValue("protocol") ) aPop.setDefault(this.getFormValue("protocol"));
				myCell.addItem(aPop);
			} catch (DataException e) {
				myCell.addItem("<B><FONT COLOR='red'>ERROR:</FONT> " + e.getMessage() + "</B>"); 
			}

			tableRow.addItem(myCell);
			tableRow.addItem(this.makeFormTextRow("Sample notes:", "sampleNotes"));
			tableRow.addItem(this.makeFormTextRow("Transaction notes:", "txnNotes"));

			myCell = new TableCell("<INPUT TYPE=SUBMIT NAME='makeExtract' VALUE='Create New Extract'/><INPUT TYPE='RESET'/><BR/><INPUT TYPE='SUBMIT' NAME='resetForm' VALUE='Return'/>");
			myCell.setAttribute("colspan","2");
			myCell.setAttribute("align","center");
			tableRow.addItem(myCell);

			Table myTable = new Table(tableRow);
			myTable.setClass("species");
			myTable.setAttribute("align", "center");
			myForm.addItem(myTable.toString());

			return myForm.toString();
		} catch (DataException e) {
			e.printStackTrace();
			return ("<P ALIGN='CENTER'><B><FONT COLOR='red'>ERROR:</FONT> " + e.getMessage() + "</B></P>");		
		}
	}

	public String inocList(Inoc myInocs) {
		String[] headerData = {"Date", "Project Code", "Media", "Volume", "Notes"};
		TableCell header = new TableHeader(headerData);
		header.setAttribute("class","header");
		TableRow tableRow = new TableRow(header);
		Table myTable = new Table(tableRow);
		myTable.setAttribute("class","dashboard");
		myTable.setAttribute("align","center");
		myTable.setAttribute("width","75%");
		SimpleDateFormat myDate = this.dateFormat();
		
		try {
			myInocs.beforeFirst();
			String curClass = "odd";
			while (myInocs.next()) {
				TableCell myCell = new TableCell();
				myCell.addItem(myDate.format(myInocs.getDate()));
				myCell.addItem(myInocs.getProjectID());
				myCell.addItem(myInocs.getMedia());
				myCell.addItem(BaseForm.formatAmount("%.0f %s", myInocs.getVolume(), "mL"));
				myCell.addItem(BaseForm.shortenString(myInocs.getNotes(), 25));
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
		} catch (DataException ex) {
			myTable.addItem("<TR><TD colspan=4><FONT COLOR='red'><B>ERROR:</FONT> " + ex.getMessage() + "</B></TD></TR>");
			ex.printStackTrace();
		}
		return myTable.toString();	
	}	
	
	public String collectionList(Collection colList) {
		try {
			colList.beforeFirst();
			String headers[] = { "ID", "Date", "Location", "Coordinates", "Collector", "Notes" };
			TableCell myCell = new TableHeader(headers);
			TableRow tableRow = new TableRow(myCell);
			SimpleDateFormat myDateFormat = this.dateFormat();
			String curClass = "odd";
			Table myTable = new Table(tableRow);
			myTable.setClass("species");
			myTable.setAttribute("align", "center");
			
			String urlBase = this.myWrapper.getContextPath();
			
			while (colList.next()) {
				myCell = new TableCell(String.format("<A HREF=\"%s/collection?col=%s\">%s</A>", urlBase, colList.getID(),colList.getID()));
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
			return myTable.toString();
		} catch (DataException e) {
			e.printStackTrace();
			return ("<P ALIGN='CENTER'><FONT COLOR='red'><B>ERROR:</FONT> " + e.getMessage() + "</B></P>");
		}
	}
	
	public String harvestForm(Harvest aHarvest) {
		StringBuffer output = new StringBuffer();
		Map updateMap = new HashMap();
		try {
			if ( this.hasFormValue("updateHarvest") ) {
				aHarvest.setManualRefresh();
				if ( this.hasFormValue("cell_mass") && (! this.getFormValue("cell_mass").equals("")) )
					aHarvest.setCellMass(BaseForm.parseAmount(this.getFormValue("cell_mass"), "g"));
				if ( this.hasFormValue("media_volume") && (! this.getFormValue("media_volume").equals("")))
					aHarvest.setMediaVolume(BaseForm.parseAmount(this.getFormValue("media_volume"), "L"));
				if ( this.hasFormValue("notes") )
					aHarvest.setNotes(this.getFormValue("notes"));
				if ( this.hasFormValue("prepDate") && (! this.getFormValue("prepDate").equals("")) )
					aHarvest.setPrepDate(this.getFormValue("prepDate"));
				if ( this.hasFormValue("project"))
					aHarvest.setProjectID(this.getFormValue("project"));
				aHarvest.refresh();
				aHarvest.setAutoRefresh();
			} else if ( this.hasFormValue("inheritInoc") ) {
				if ( aHarvest.isFieldHarvest() ) {
					Collection parent = aHarvest.getCollection();
					aHarvest.setProjectID(parent.getProjectID());
				} else {
					Inoc parent = aHarvest.getInoculations();
					aHarvest.setProjectID(parent.getProjectID());
				}
			}
		} catch (DataException e) {
			output.append(this.handleException(e));
		}

		try {
			Paragraph head = new Paragraph();
			head.setAlign("CENTER");
			StyledText title = new StyledText("Harvest Details");
			title.setSize("+3");
			head.addItem(title);
			head.addItem("<HR WIDTH='85%'/>");
			output.append(head);
			
			TableCell myCell = new TableCell("Harvest Serial Number:");
			myCell.addItem(aHarvest.getID());
			TableRow tableRow = new TableRow(myCell);
			
			myCell = new TableCell("Strain");
			Strain myStrain = aHarvest.getStrain();
			myCell.addItem(String.format("<A HREF='strain?id=%s'>%s <I>%s</I></A>", myStrain.getID(), myStrain.getID(), myStrain.getName()));
			tableRow.addItem(myCell);
			SimpleDateFormat myDate = this.dateFormat();

			myCell = new TableCell("Harvest Date");
			myCell.addItem(myDate.format(aHarvest.getDate()));
			tableRow.addItem(myCell);
			
			myCell = new TableCell("Project:");
			try {
				Popup projectPop = this.projectPopup();
				projectPop.setName("project");
				String myProject = aHarvest.getProjectID();
				if ( myProject != null )
					projectPop.setDefault(myProject);
				myCell.addItem(projectPop.toString() + " <INPUT TYPE='SUBMIT' NAME='inheritInoc' VALUE='Inherit From Source'/>");
			} catch (DataException e) {
				myCell.addItem("<B><FONT COLOR='red'>ERROR:</FONT> " + e.getMessage() + "</B>"); 
			}
			tableRow.addItem(myCell);

			myCell = new TableCell("Color");
			myCell.addItem(aHarvest.getColor());		
			tableRow.addItem(myCell);
			myCell = new TableCell("Type");
			myCell.addItem(aHarvest.getType());
			tableRow.addItem(myCell);
			
			Table myTable = new Table(tableRow);
			myTable.setClass("species");
			myTable.setAttribute("width", "80%");
			myTable.setAttribute("align", "center");
			Form myForm = new Form(myTable);
			myForm.setName("harvest");
			myForm.setAttribute("METHOD","POST");		

			if ( aHarvest.isFieldHarvest() ) {
				myForm.addItem("<P ALIGN='CENTER'><FONT SIZE='+1'><B>Source Collection</B></FONT></P>");
				myForm.addItem(this.collectionList(aHarvest.getCollection()));
			} else {
				myForm.addItem("<P ALIGN='CENTER'><FONT SIZE='+1'><B>Source Inoculation(s)</B></FONT></P>");
				myForm.addItem(this.inocList(aHarvest.getInoculations()));
			}

			tableRow = new TableRow();
			tableRow.addItem(this.makeFormDateRow("PrepDate:", "prepDate", "harvest", aHarvest.getPrepDateString()));

			myCell = new TableCell("Cell Mass:");
			Float cellMass = aHarvest.getCellMass();
			if ( cellMass == null ) {
				myCell.addItem("<INPUT TYPE=TEXT NAME='cell_mass' />");
			} else {
				myCell.addItem("<INPUT TYPE=TEXT NAME='cell_mass' VALUE='" + 
					BaseForm.formatAmount("%.2f %s", cellMass.floatValue(),"g") + "'>");
			}
			if (updateMap.containsKey("cell_mass")) 
				myCell.setAttribute("bgcolor", "yellow");		
			tableRow.addItem(myCell);
			
			
			myCell = new TableCell("Extract:");
			Sample extract = aHarvest.getExtract();
			if ( this.hasFormValue("makeExtract") && (! this.hasFormValue("resetForm")) ) {
				myCell = new TableCell(this.makeExtract(aHarvest));
				myCell.setAttribute("COLSPAN", "2");
			} else if ( extract != null  ) {
				StringBuffer extractInfo = new StringBuffer();
				extract.beforeFirst();
				Table extractTable = new Table();
				boolean oddRow = true;
				while ( extract.next() ) {
					TableCell extCell = new TableCell(String.format("<A HREF='sample?id=%s'>%s</A>", extract.getID(), extract.getName()));
					extCell.addItem(String.format("Extract Amount:%s (%s)", BaseForm.formatAmount("%.2f %s", extract.getAmountForHarvest(aHarvest), extract.getBaseUnit()), 
							myDate.format(extract.getDate())));
					extCell.addItem(String.format("Current Balance: %s", BaseForm.formatAmount("%.2f %s", extract.accountBalance(), extract.getBaseUnit())));
					
					if ( extract.isRemoved() )
						extCell.setClass("removed");
					TableRow extRow = new TableRow(extCell);
					if ( oddRow )
						extRow.setClass("odd");
					else 
						extRow.setClass("even");
					oddRow = (! oddRow);
					extractTable.addItem(extRow);
				}
				extractInfo.append(extractTable.toString());
				extractInfo.append("<INPUT TYPE='SUBMIT' NAME='makeExtract' VALUE='Make an Extract'/>");
				extractInfo.append(String.format("<INPUT TYPE='BUTTON' NAME='addQueue' VALUE='Add to an Extraction Queue' onClick='queueForm(\"harvest\", \"%s\");'/>", aHarvest.getID()));
				myCell.addItem(extractInfo.toString());	
			} else {
				myCell.addItem("No extract information available<BR/><INPUT TYPE='SUBMIT' NAME='makeExtract' VALUE='Make an Extract'/>" +
						String.format("<INPUT TYPE='BUTTON' NAME='addQueue' VALUE='Add to an Extraction Queue' onClick='queueForm(\"harvest\", \"%s\");'/>", aHarvest.getID()));			
			}
			tableRow.addItem(myCell);

			myCell = new TableCell("Media Volume");
			Float mediaVol = aHarvest.getMediaVolume();
			if ( mediaVol == null ) {
				myCell.addItem("<INPUT TYPE='TEXT' NAME='media_volume' />");
			} else {
			myCell.addItem("<INPUT TYPE=TEXT NAME='media_volume' VALUE='" + 
					BaseForm.formatAmount("%.1f %s", mediaVol.floatValue(),"L") + "'/>");
			}
			if (updateMap.containsKey("media_volume")) {
				myCell.setAttribute("bgcolor", "yellow");
			}		
			tableRow.addItem(myCell);			
			
			myCell = this.makeFormTextAreaRow("Notes:", "notes", aHarvest.getNotes());
			if (updateMap.containsKey("notes")) {
				myCell.setAttribute("bgcolor", "yellow");
			}		
			tableRow.addItem(myCell);
			
			myCell = new TableCell("<INPUT TYPE=SUBMIT NAME='updateHarvest' VALUE='Update Harvest'/><INPUT TYPE='RESET'/>");
			myCell.setAttribute("colspan","2");
			myCell.setAttribute("align","center");
			tableRow.addItem(myCell);
			
			myTable = new Table(tableRow);
			myTable.setClass("species");
			myTable.setAttribute("width", "80%");
			myTable.setAttribute("align", "center");

			myForm.addItem(myTable);		
			output.append(myForm);

		} catch (DataException ex) {
			output.append("<P ALIGN='CENTER'><B><FONT COLOR='red'>SQLException:</FONT> " + ex.getMessage() + "</B></P>");
			ex.printStackTrace();
		}
		return output.toString();
	}	

	public String inocForm() {
		try {
			if ( this.hasFormValue("strain") )
				return this.inocForm(SQLInoc.openInocsForStrain(this.getSQLDataSource(), this.getFormValue("strain")));
		} catch (DataException e) {
			return this.handleException(e);
		}
		return "";
	}
	
	
	private String inocForm(Inoc myInocs) {
		String[] headerData = {"", "Date", "Project Code", "Media", "Volume"};
		TableCell header = new TableHeader(headerData);
		header.setAttribute("class","header");
		TableRow inocRow = new TableRow(header);
		Table inocTable = new Table(inocRow);
		inocTable.setAttribute("class","dashboard");
		inocTable.setAttribute("align","center");
		inocTable.setAttribute("width","75%");

		if ( myInocs != null ) {
			try {
				ArrayList<String> inocList = null;		
				if ( this.hasFormValue("inoc") ) {
					inocList = new ArrayList<String>(java.util.Arrays.asList(this.getFormValues("inoc")));
				} 
				myInocs.beforeFirst();
				boolean oddRow = true;
				while (myInocs.next()) {
					TableCell myCell = new TableCell();
					if ( inocList != null && inocList.contains(myInocs.getID()) ) {
						myCell.addItem(String.format("<INPUT TYPE=CHECKBOX NAME='inoc' VALUE='%s' CHECKED />", myInocs.getID()));									
					} else {
						myCell.addItem(String.format("<INPUT TYPE=CHECKBOX NAME='inoc' VALUE='%s' />", myInocs.getID()));				
					}
					myCell.addItem(this.formatDate(myInocs.getDate()));
					myCell.addItem(myInocs.getProjectID());
					myCell.addItem(myInocs.getMedia());
					myCell.addItem(myInocs.getVolumeString(1.0f));
					TableRow aRow = new TableRow(myCell);
					if ( oddRow ) 
						aRow.setClass("odd");
					else 
						aRow.setClass("even");			
					aRow.setAttribute("align", "center");
					inocTable.addItem(aRow);
					oddRow = (! oddRow);
				}
			} catch (DataException e ) {
				return this.handleException(e);
			}
		} else {
			inocTable.addItem("<TR><TH COLSPAN='5'>None</TH></TR>");
		}
		
		return inocTable.toString();
	}
	
	public String addHarvest() {
		try {
			if ( this.hasFormValue("col") ) {
				return this.addHarvest(new SQLCollection(this.getSQLDataSource(), this.getFormValue("col")));
			} else {
				return this.addHarvestInoc();
			}
		} catch (DataException e) {
			return this.handleException(e);
		}
	}
	
	private String addHarvestInoc() throws DataException {
		StringBuffer output = new StringBuffer();
		Harvest aHarvest = SQLHarvest.createInProject(this.getSQLDataSource(), this.getFormValue("strain"), this.getFormValue("project"));
		if ( aHarvest.first() ) {
			output.append("<P ALIGN=CENTER><FONT COLOR='green'><B>Success</FONT></B></P>");
			aHarvest.setManualRefresh();
			aHarvest.setColor(this.getFormValue("color"));
			aHarvest.setType(this.getFormValue("type"));
			aHarvest.setNotes(this.getFormValue("notes"));
			aHarvest.setDate(this.getFormValue("date"));
			aHarvest.refresh();
			aHarvest.setAutoRefresh();
			TableRow myRow = new TableRow();
			Table myTable = new Table(myRow);
			output.append("<P ALIGN=CENTER>Added harvest serial # " + aHarvest.getID() + "</P>");
			String[] inocs = this.getFormValues("inoc");
			for (int i = 0; i < inocs.length; i++ ) {
				Inoc anInoc = new SQLInoc(this.getSQLDataSource(), inocs[i]);
				anInoc.setHarvest(aHarvest);
				TableCell myCell = new TableCell("Updating Inoculation: " + inocs[i]);
				myCell.addItem("<FONT COLOR='green'><B>Updated</B></FONT>");
				myRow.addItem(myCell);
			}
			output.append("<P ALIGN=CENTER>" + myTable.toString() + "</P>");
		} else {
			output.append("<P ALIGN=CENTER><FONT COLOR='red'><B>Insert Failure</FONT></B></P>");
		}
		return output.toString();
	}

}
