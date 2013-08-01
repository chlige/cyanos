/**
 * 
 */
package edu.uic.orjala.cyanos.web.forms;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.uic.orjala.cyanos.Cryo;
import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.Harvest;
import edu.uic.orjala.cyanos.Inoc;
import edu.uic.orjala.cyanos.Strain;
import edu.uic.orjala.cyanos.sql.SQLInoc;
import edu.uic.orjala.cyanos.sql.SQLStrain;
import edu.uic.orjala.cyanos.web.BaseForm;
import edu.uic.orjala.cyanos.web.CyanosWrapper;
import edu.uic.orjala.cyanos.web.html.Form;
import edu.uic.orjala.cyanos.web.html.HtmlList;
import edu.uic.orjala.cyanos.web.html.Image;
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
public class InoculationForm extends BaseForm {

	public static final String LS_CULTURE_ID = "cultureid";
	private List<List<String>> myResults = null;
	
	/**
	 * @param callingServlet
	 */
	public InoculationForm(CyanosWrapper aWrapper) {
		super(aWrapper);
	}

	public String lsCultureID() {
		try {
			String field = this.getFormValue("livesearch");
			String[] likeColumns = { SQLStrain.ID_COLUMN };
			String[] likeValues = { String.format("%s%%", this.getFormValue(field)) };
			Strain strains = SQLStrain.strainsLike(getSQLDataSource(), likeColumns, likeValues, SQLStrain.ID_COLUMN, SQLStrain.ASCENDING_SORT);
			
			StringBuffer output = new StringBuffer();
			if ( strains.first() ) {
				strains.beforeFirst();
				while ( strains.next() ) {
					output.append(String.format("<A onClick='setLS(\"%s\",\"%s\",\"%s\")'>%s</A><BR>", field, strains.getID(), this.getFormValue("div"), strains.getID()));
				}
				return output.toString();
			} else {
				return "No suggestions.";
			}
		} catch (DataException e) {
			return this.handleException(e);
		}

	}
	
	public String inoculationList(Inoc anInoc) {
		StringBuffer output = new StringBuffer();
		output.append("<P ALIGN=CENTER><FONT SIZE=+2>Inoculations</FONT><P>");

		String[] headerData = {"Date", "Media", "Volume", "Fate", "Notes"};
		TableCell header = new TableHeader(headerData);

		header.setAttribute("class","header");
		TableRow tableRow = new TableRow(header);
		Table myTable = new Table(tableRow);
		myTable.setAttribute("class","dashboard");
		myTable.setAttribute("align","center");
		myTable.setAttribute("width","75%");

		try {
			anInoc.beforeFirst();
			String curClass = "odd";
			SimpleDateFormat myDate = this.dateFormat();
			while (anInoc.next()) {
				TableCell myCell = new TableCell();
				myCell.addItem(myDate.format(anInoc.getDate()));
				myCell.addItem(anInoc.getMedia());
				myCell.addItem(anInoc.getVolumeString(5.0f));
				String myFate = anInoc.getFate();
				if ( myFate == null ) {
					myCell.addItem("");
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
					myCell.addItem("Harvested");
				} else {
					myCell.addItem(myFate);
				}
				myCell.addItem(anInoc.getNotes());
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
		} catch (DataException e) {
			tableRow.addItem("<TD COLSPAN=5><B><FONT COLOR='red'>ERROR:</FONT> " + e.getLocalizedMessage() + "</B></TD>");
			e.printStackTrace();
		}
		output.append(myTable.toString());
		return output.toString();
	}

	/**
	 * Create a list of supplied inoculations.
	 * 
	 * @param anInoc Inoculations to list
	 * @param selectable set to <CODE>true</CODE> to create a selection box.
	 * @return List of inoculations as a {@link java.lang.String}.
	 */
	public String inoculationList(Inoc anInoc, boolean selectable) {
		StringBuffer output = new StringBuffer();
		output.append("<P ALIGN=CENTER><FONT SIZE=+2>Inoculations</FONT><P>");

		String[] headerData = {"Date", "Media", "Volume", "Fate", "Notes"};
		TableCell header = new TableHeader(headerData);

		header.setAttribute("class","header");
		TableRow tableRow = new TableRow(header);
		Table myTable = new Table(tableRow);
		myTable.setAttribute("class","dashboard");
		myTable.setAttribute("align","center");
		myTable.setAttribute("width","75%");

		try {
			anInoc.beforeFirst();
			String curClass = "odd";
			SimpleDateFormat myDate = this.dateFormat();
			while (anInoc.next()) {
				TableCell myCell = new TableCell();
				myCell.addItem(myDate.format(anInoc.getDate()));
				myCell.addItem(anInoc.getMedia());
				myCell.addItem(anInoc.getVolumeString(5.0f));
				String myFate = anInoc.getFate();
				if ( myFate == null ) {
					if (selectable)	myCell.addItem("<INPUT TYPE=CHECKBOX NAME='inoc' VALUE='" + anInoc.getID() + "'/>");
					else myCell.addItem("");
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
					myCell.addItem("Harvested");
				} else {
					myCell.addItem(myFate);
				}
				myCell.addItem(anInoc.getNotes());
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
		} catch (DataException e) {
			tableRow.addItem("<TD COLSPAN=5><B><FONT COLOR='red'>ERROR:</FONT> " + e.getLocalizedMessage() + "</B></TD>");
			e.printStackTrace();
		}
		output.append(myTable.toString());
		return output.toString();
	}

	public String showInoc(Inoc anInoc) {
		StringBuffer output = new StringBuffer();
		TableCell myCell;
		TableRow tableRow = new TableRow();

		try {

			if ( this.hasFormValue("updateInoc") ) {
				anInoc.setManualRefresh();
				if ( this.hasFormValue("fate") ) 
					anInoc.setFate(this.getFormValue("fate"));
				if ( this.hasFormValue("rmdate") ) 
					anInoc.setRemovedDate(this.getFormValue("rmdate"));
				if ( this.hasFormValue("notes"))
					anInoc.setNotes(this.getFormValue("notes"));
				if ( this.hasFormValue("project") )
					anInoc.setProjectID(this.getFormValue("project"));
				anInoc.setAutoRefresh();
				anInoc.refresh();
			} else if ( this.hasFormValue("inheritStrain") ) {
				Strain myStrain = anInoc.getStrain();
				anInoc.setProjectID(myStrain.getProjectID());
			} else if ( this.hasFormValue("inheritInoc") ) {
				Inoc parent = anInoc.getParent();
				anInoc.setProjectID(parent.getProjectID());
			}
			
			myCell = new TableCell("Serial number:");
			myCell.addItem(anInoc.getID());
			tableRow.addItem(myCell);
			
			Strain aStrain = anInoc.getStrain();
			myCell = new TableCell("Strain:");
			myCell.addItem(String.format("<A HREF='strain?id=%s'>%s <I>%s</I></A>", aStrain.getID(), aStrain.getID(), aStrain.getName()));
			tableRow.addItem(myCell);

			SimpleDateFormat myFormat = this.dateFormat();
			myCell = new TableCell("Date:");
			myCell.addItem(myFormat.format(anInoc.getDate()));
			tableRow.addItem(myCell);

			myCell = new TableCell("Media:");
			myCell.addItem(anInoc.getMedia());
			tableRow.addItem(myCell);
			
			myCell = new TableCell("Project:");
			try {
				Popup projectPop = this.projectPopup();
				projectPop.setName("project");
				String myProject = anInoc.getProjectID();
				if ( myProject != null )
					projectPop.setDefault(myProject);
				myCell.addItem(projectPop);
			} catch (DataException e) {
				myCell.addItem("<B><FONT COLOR='red'>ERROR:</FONT> " + e.getMessage() + "</B>"); 
				e.printStackTrace();
			}

			tableRow.addItem(myCell);
			
			tableRow.addItem("<TD COLSPAN='2'><INPUT TYPE='SUBMIT' NAME='inheritStrain' VALUE='Inherit From Strain'/><INPUT TYPE='SUBMIT' NAME='inheritInoc' VALUE='Inherit From Parent Inoc'/></TD>");
			
			myCell = new TableCell("Fate:");
			String myFate = anInoc.getFate();
			if ( myFate == null ) myFate = "";
			Popup aPop = new Popup();
			aPop.setName("fate");
			aPop.addItemWithLabel("", "UNDEFINED");
			aPop.addItemWithLabel(Inoc.FATE_STOCK,"Stock Culture");
			aPop.addItemWithLabel(Inoc.FATE_HARVEST,"Harvested");
			aPop.addItemWithLabel(Inoc.FATE_CRYO,"Cryopreserved");
			aPop.addItemWithLabel(Inoc.FATE_DEAD,"Died");
			aPop.setDefault(myFate);
			if ( myFate.equals("stock") ) 
				aPop.setAttribute("onChange", "if ( this.value == 'dead' ) { alert('Do not change the fate when removing a stock culture.') }");
			myCell.addItem(aPop);
			tableRow.addItem(myCell);

			if ( anInoc.getRemoveDate() != null ) {
				tableRow.addItem(this.makeFormDateRow("Date Removed:", "rmdate", "inoc", anInoc.getRemoveDate()));
			} else if ( anInoc.getFate() != null ) {
				tableRow.addItem(this.makeFormDateRow("Date Removed:", "rmdate", "inoc", ""));
			}
			
			if ( "harvest".equals(anInoc.getFate()) ) {
				myCell = new TableCell();
				myCell.setAttribute("COLSPAN", "2");
				Harvest aHarvest = anInoc.getHarvest();
				if ( aHarvest != null && aHarvest.first() ) {
					myCell.addItem("<A HREF='harvest?id=" + aHarvest.getID() + "'>View Harvest</A>");
				} else {
					myCell.addItem("<A HREF='?id=" + anInoc.getID() + "&action=set_harvest'>Set Harvest</A>");					
				}
				tableRow.addItem(myCell);
			}

			if ( this.hasFormValue("linkInoc") ) {
				myCell = new TableCell();			
				myCell.setAttribute("COLSPAN","2");	
				anInoc.setParentID(this.getFormValue("parent"));
				myCell.addItem("<FONT COLOR='green'>Linked to inoculation # " + this.getFormValue("parent") + "</FONT>");
				tableRow.addItem(myCell);
			}
			
			myCell = new TableCell();			
			myCell.setAttribute("COLSPAN","2");	
			Inoc myParent = anInoc.getParent();
			if ( myParent != null && myParent.first() ) {
				myCell.addItem(String.format("<A HREF='?id=%s'>View Parent Culture</A>", myParent.getID()));
			} else {
				Cryo cryoParent = anInoc.getCryoParent();
				if ( cryoParent != null ) {
					myCell.addItem("<A HREF='cryo?id=" + cryoParent.getID() + "'>View Cryo Parent</A>");
				} else if ( this.hasFormValue("linkParentForm") ) {
					Form parentForm = new Form("Parent inoculation: ");
					Strain myStrain = anInoc.getStrain();
					Inoc possibles = myStrain.getInoculations();
					possibles.beforeFirst();
					Popup posPop = new Popup();
					posPop.setName("parent");
					posPop.addItemWithLabel("", "--NONE--");
					String thisInoc = anInoc.getID();
					SimpleDateFormat myDf = this.dateFormat();
					while ( possibles.next() ) {
						if ( possibles.getID().equals(thisInoc) ) continue;
						String date = myDf.format(possibles.getDate());
						posPop.addItemWithLabel(possibles.getID(), String.format("%s (%s)", date, BaseForm.formatAmount(possibles.getVolume(), "mL")));
					}
					parentForm.addItem(posPop);
					parentForm.addItem("<INPUT TYPE='SUBMIT' NAME='linkInoc' VALUE='Link Parent'/><INPUT TYPE='RESET'/><INPUT TYPE='SUBMIT' NAME='cancel' VALUE='Cancel'/>");
				} else {
					myCell.addItem("NONE<BR/><INPUT TYPE='submit' NAME='linkParentForm' VALUE='Link Parent'/>");
				}
			}
			tableRow.addItem(myCell);

			myCell = new TableCell("Children:");
			myCell.addItem(this.listKids(anInoc));
			myCell.setAttribute("VALIGN", "TOP");
			tableRow.addItem(myCell);

			myCell = new TableCell("Notes:");
			myCell.addItem("<TEXTAREA NAME='notes' COLS=40 ROWS=5>" + anInoc.getNotes() + "</TEXTAREA>");
			tableRow.addItem(myCell);

			myCell = new TableCell("<INPUT TYPE=SUBMIT NAME='updateInoc' VALUE='Update'/><INPUT TYPE='RESET'/>");
			myCell.setAttribute("colspan","2");
			myCell.setAttribute("align","center");
			tableRow.addItem(myCell);

			Table myTable = new Table(tableRow);
			myTable.setClass("species");
			myTable.setAttribute("width", "80%");
			myTable.setAttribute("align", "center");
			Form myForm = new Form(myTable);
			myForm.setName("inoc");
			myForm.setAttribute("METHOD","POST");
			output.append("<P ALIGN=CENTER>");
			output.append(myForm.toString());
			output.append("</P>");
		} catch (DataException e) {
			e.printStackTrace();
			return "<P ALIGN='CENTER'><B><FONT COLOR='red'>Error:</FONT> " + e.getMessage() + "</B></P>";
		}
		return output.toString();
	}

	private String listKids(Inoc anInoc) {
		String output = "";
		try {
			Inoc kids = anInoc.getChildren();
			if ( kids != null ) {
				HtmlList aList = new HtmlList();
				aList.unordered();
				SimpleDateFormat myFormat = this.dateFormat();
				kids.beforeFirst();
				while ( kids.next() ) {
					aList.addItem("<A HREF='?id=" + kids.getID() + "'>" +
							myFormat.format(kids.getDate()) + " (" + formatAmount(kids.getVolume(), "mL") + 
							" " + kids.getMedia() + ")</A>" + this.listKids(kids));
				}
				output = aList.toString(); 
			}
			return output;
		} catch (DataException e) {
			return "<B><FONT COLOR='red'>Error:</FONT> " + e.getMessage() + "</B>";
		}
	}

	public String inocForm() {
		StringBuffer output = new StringBuffer();
		Integer rowNumber;

		Popup numberPop = new Popup();
		for ( int i = 1; i <= 15; i++ ) {
			numberPop.addItem(String.valueOf(i));
		}
		numberPop.setName("rows");
		if ( this.hasFormValue("rows") ) {
			numberPop.setDefault(this.getFormValue("rows"));
			rowNumber = new Integer(this.getFormValue("rows"));
		} else {
			numberPop.setDefault("5");
			rowNumber = new Integer("5");
		}
		numberPop.setAttribute("onChange", "this.form.submit()");
		Paragraph head = new Paragraph();
		head.addItem("<BR><FORM name='inoc' METHOD='POST'><CENTER>Number of inoculations: ");
		head.addItem(numberPop);
		head.addItem("<BR><INPUT TYPE=SUBMIT VALUE='Retrieve Culture Information'>");
		head.addItem("</CENTER>");
		head.setAlign("CENTER");
		output.append(head.toString());

		StrainForm aForm = new StrainForm(this.myWrapper);
		output.append("<SCRIPT TYPE='text/javascript'>\n//<![CDATA[\n");
		output.append(aForm.strainJScript());
		output.append("\n//]]>\n</SCRIPT>\n");

		String[] tableHeaders = { "", "Culture ID", "Date", "Project", "Parent Stock", "Volume", "Media", "Notes", "Stock" };
		TableCell header = new TableHeader(tableHeaders);
		TableRow tableRow = new TableRow(header);
		Table myTable = new Table(tableRow);
		myTable.setClass("species");
		myTable.setAttribute("width", "90%");
		myTable.setAttribute("align", "center");
		String curClass = "odd";

		Popup projectPop = new Popup();
		try {
			projectPop = this.projectPopup();
		} catch (DataException e) {
			projectPop.addItemWithLabel("", "ERROR");
			e.printStackTrace();
		}
		
		for ( int i = 1; i <= rowNumber.intValue(); i++ ) {
			Integer thisRow = new Integer(i);
			TableCell myCell = new TableCell("<B>" + thisRow.toString() + "</B>");
			// strainPop.setName("cultureid" + thisRow.toString());
			Popup parentPop = new Popup();
			parentPop.addItem("");
			parentPop.setAttribute("WIDTH", "10");
			parentPop.setName("parent" + thisRow.toString());

			projectPop.setDefault("");
			projectPop.setName("project" + thisRow.toString());

			String defaultMedia = new String();
			String cidField = String.format("cultureid%d", thisRow);
			
			if ( this.hasFormValue(cidField) ) {
				String cultureID = this.getFormValue(cidField);
				myCell.addItem(InoculationForm.livesearch(cidField, cultureID, cidField, String.format("div_cultureid%d", thisRow)));
				try {
					Inoc possibleParents = SQLInoc.viableInocsForStrain(this.getSQLDataSource(), cultureID);
					if ( possibleParents != null ){
						possibleParents.beforeFirst();
						SimpleDateFormat myFormat = this.dateFormat();
						while ( possibleParents.next() ) {
							StringBuffer myLabel = new StringBuffer(myFormat.format(possibleParents.getDate()));
							myLabel.append(" (");
							myLabel.append(formatAmount(possibleParents.getVolume(), "mL"));
							myLabel.append(")");
							parentPop.addItemWithLabel(possibleParents.getID(), myLabel.toString());
						}
					}
				} catch (DataException e) {
					e.printStackTrace();
					parentPop.addItemWithLabel("", "SQL ERROR");
				}
				try {
					Strain aStrain = SQLStrain.load(this.getSQLDataSource(), cultureID);
					if ( aStrain.first() ) {
						defaultMedia = aStrain.getDefaultMedia();
						String defaultProject = "";
						String parent = this.getFormValue("parent" + thisRow.toString());
						if ( parent != null && (! parent.equals("")) ) {
							Inoc myParent = SQLInoc.load(this.getSQLDataSource(), this.getFormValue("parent" + thisRow.toString()));
							defaultProject = myParent.getProjectID();
						} else {
							defaultProject = aStrain.getProjectID();
						}
						if ( defaultProject != null )
							projectPop.setDefault(defaultProject);
					}
				} catch (DataException e) {
					e.printStackTrace();
				}
			} else {
				defaultMedia = new String();
				myCell.addItem(InoculationForm.livesearch(cidField, "", cidField, String.format("div_cultureid%d", thisRow)));
			}
			// myCell.addItem(strainPop.toString());
			Image calImage = this.getImage("calendar.png");
			calImage.setAttribute("BORDER", "0");
			calImage.setAttribute("ALIGN", "MIDDLE");
			if ( this.hasFormValue("date" + thisRow.toString()) ) {
				myCell.addItem("<INPUT TYPE=TEXT NAME='date" + thisRow.toString() + "' SIZE=10 VALUE='"+
						this.getFormValue("date" + thisRow.toString()) + "' />" + 
						"<A onClick='selectDate(document.inoc.date" + thisRow.toString() + 
				")'>" + calImage.toString() + "</A>");
			} else {
				myCell.addItem("<INPUT TYPE=TEXT NAME='date" + thisRow.toString() + "' SIZE=10 />" + 
						"<A onClick='selectDate(document.inoc.date" + thisRow.toString() + 
				")'>" + calImage.toString() + "</A>");			
			}
			
			String radioCheck = "CHECKED";

			if ( this.hasFormValue("parent" + thisRow.toString()) ) {
				parentPop.setDefault(this.getFormValue("parent" + thisRow.toString()));
				if ( ! this.hasFormValue("inheritProj" + thisRow.toString()) ) {
					radioCheck = "";
					projectPop.setAttribute("onLoad", "");
				}
			} else {
				parentPop.setDefault("");
				
			}
			
			if ( radioCheck.equals("CHECKED") ) 
				projectPop.setAttribute("DISABLED", "TRUE");
			
			myCell.addItem(projectPop.toString() + 
					String.format("<BR><INPUT TYPE='CHECKBOX' NAME='inheritProj%d' VALUE='true' %s onClick='this.form.project%d.disabled=this.checked'/> Inherit", thisRow, radioCheck, thisRow));
			myCell.addItem(parentPop.toString());			

			if ( this.hasFormValue("volume" + thisRow.toString()) ) {
				myCell.addItem("<INPUT TYPE='TEXT' SIZE=2 NAME='qty" + thisRow.toString() + 
						"' VALUE='"+ this.getFormValue("qty" + thisRow.toString()) + 
						"' /> X <INPUT TYPE='TEXT' SIZE=10 NAME='volume" + thisRow.toString() + "' VALUE='"+ 
						this.getFormValue("volume" + thisRow.toString()) + "' />");
			} else {
				myCell.addItem("<INPUT TYPE='TEXT' SIZE=2 VALUE='1' NAME='qty" + thisRow.toString() + 
						"' /> X <INPUT TYPE='TEXT' SIZE=10 NAME='volume" + thisRow.toString() + "' />");
			}
			if ( this.hasFormValue("media" + thisRow.toString()) && (this.getFormValue("media" + thisRow.toString()).length() > 0) ) {
				myCell.addItem("<INPUT TYPE='TEXT' SIZE=10 NAME='media" + thisRow.toString() + "' VALUE='"+ 
						this.getFormValue("media" + thisRow.toString()) + "' />");
			} else {
				myCell.addItem("<INPUT TYPE='TEXT' SIZE=10 NAME='media" + thisRow.toString() + "' VALUE='"+ 
						defaultMedia + "' />");
			}
			if ( this.hasFormValue("notes" + thisRow.toString()) ) {
				myCell.addItem("<TEXTAREA ROWS=2 COLS=15 NAME='notes" + thisRow.toString() + "'>" +
						this.getFormValue("notes" + thisRow.toString()) + "</TEXTAREA>");
			} else {
				myCell.addItem("<TEXTAREA ROWS=2 COLS=15 NAME='notes" + thisRow.toString() + "'></TEXTAREA>");			
			}
			if ( this.hasFormValue("stock" + thisRow.toString()) ) {
				myCell.addItem("<INPUT TYPE=CHECKBOX NAME='stock" + thisRow.toString() + "' VALUE='yes' CHECKED/>");
			} else {
				myCell.addItem("<INPUT TYPE=CHECKBOX NAME='stock" + thisRow.toString() + "' VALUE='yes' />");			
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
		myTable.addItem("<TR><TD COLSPAN=7 ALIGN=CENTER>");
		myTable.addItem("<INPUT TYPE=SUBMIT NAME='addInocsAction' VALUE='Add Inoculations'>");
		myTable.addItem("<INPUT TYPE=RESET VALUE='Reset'></TD></TR>");
		output.append(myTable);
		output.append("</FORM>");
		return output.toString();
	}

	
	public String killInoc() {
		StringBuffer output = new StringBuffer();

		Paragraph head = new Paragraph();
		head.setAlign("CENTER");
		StyledText title = new StyledText("Removing Inoculations");
		title.setSize("+3");
		head.addItem(title);
		head.addItem("<HR WIDTH='85%'/>");
		output.append(head);
		String rmDate;
		if ( this.hasFormValue("date") ) 
			rmDate = this.getFormValue("date");
		else {
			SimpleDateFormat aFormat = new SimpleDateFormat("yyyy-MM-dd");
			Date aDate = new Date();
			rmDate = aFormat.format(aDate);
		}
		TableRow myRow = new TableRow("<TD COLSPAN=2>Setting removal date " + rmDate + "</TH>");
		Table myTable = new Table(myRow);
		
		String[] inocs = this.getFormValues("inoc");
		for (int i = 0; i < inocs.length; i++ ) {
			TableCell myCell = new TableCell("Updating Inoculation: <A HREF='../inoc?id=" + inocs[i] + "'>" + inocs[i] + "</A>");
			try {
				Inoc myInoc = SQLInoc.load(this.getSQLDataSource(), inocs[i]);
				myInoc.setFate(Inoc.FATE_DEAD);
				myInoc.setRemovedDate(rmDate);
				myCell.addItem("<FONT COLOR='green'><B>Updated</B></FONT>");
			} catch (DataException e) {
				myCell.addItem("<B><FONT COLOR='red'>SQL Error:</FONT>" + e.getMessage() + "</B>");
				e.printStackTrace();
			}
			myRow.addItem(myCell);
		}
		output.append("<P ALIGN=CENTER>" + myTable.toString() + "</P>");
		return output.toString();
	}

	public String addInocs() {
		StringBuffer output = new StringBuffer();
		Integer rowNumber = new Integer(this.getFormValue("rows"));

		String[] tableHeaders = { "Inoc ID", "Culture ID", "Parent ID", "Date", "Volume", "Media", "Notes" };
		TableCell header = new TableHeader(tableHeaders);
		TableRow tableRow = new TableRow(header);
		Table myTable = new Table(tableRow);
		myTable.setClass("species");
		myTable.setAttribute("width", "80%");
		myTable.setAttribute("align", "center");
		String curClass = "odd";

		this.myResults = new ArrayList<List<String>>();
		List<String> resultRow = new ArrayList<String>();
		resultRow.add("Inoculation ID");
		resultRow.add("Culture ID");
		resultRow.add("Parent ID");
		resultRow.add("Inoculation Date");
		resultRow.add("Volume");
		resultRow.add("Media");
		resultRow.add("Project Code");
		resultRow.add("Notes");
		resultRow.add("Stock");
		myResults.add(resultRow);
		
		for ( int i = 1; i <= rowNumber.intValue(); i++ ) {
			Integer thisRow = new Integer(i);
			if ( this.getFormValue("cultureid" + thisRow.toString()).equals("") )
				continue;

			Integer qty = new Integer(this.getFormValue("qty" + thisRow.toString()));
			for ( int q = 1; q <= qty.intValue(); q++ ) {	
				String status = new String();
				TableCell myCell;
				resultRow = new ArrayList<String>();
				String cultureID = this.getFormValue("cultureid" + thisRow.toString());
				String parentID = this.getFormValue("parent" + thisRow.toString());
				String date = this.getFormValue("date" + thisRow.toString());
				String media = this.getFormValue("media" + thisRow.toString());
				BigDecimal volume = parseAmount(this.getFormValue("volume" + thisRow.toString()));
				String notes = this.getFormValue("notes" + thisRow.toString());
				String projectCode = this.getFormValue("project" + thisRow.toString());
				boolean stock = "yes".equals(this.getFormValue("stock" + thisRow.toString()));
				try {
					if ( this.hasFormValue("inheritProj" + thisRow.toString()) ) {
						if ( parentID != null && (! parentID.equals("")) ) {
							Inoc myParent = SQLInoc.load(this.getSQLDataSource(), parentID);
							projectCode = myParent.getProjectID();
						} else {
							Strain aStrain = SQLStrain.load(this.getSQLDataSource(), cultureID);
							projectCode = aStrain.getProjectID();
						}
					}
					Inoc anInoc = SQLInoc.createInProject(this.getSQLDataSource(), cultureID, projectCode);
					if ( anInoc.first() ) {
						status = "<FONT COLOR='green'>Success</FONT>";
						anInoc.setManualRefresh();
						if ( parentID != null && (! parentID.equals("")) )
							anInoc.setParentID(parentID);
						anInoc.setDate(date);
						anInoc.setVolume(volume);
						anInoc.setMedia(media);
						anInoc.setNotes(notes);
						if ( stock ) 
							anInoc.setFate(Inoc.FATE_STOCK);
						// Update values
						anInoc.refresh();
						anInoc.setAutoRefresh();
						resultRow.add(anInoc.getID());
						resultRow.add(cultureID);
						resultRow.add(parentID);
						resultRow.add(date);
						resultRow.add(SQLInoc.autoFormatAmount(anInoc.getVolume(), SQLInoc.VOLUME_TYPE));
						resultRow.add(anInoc.getMedia());
						resultRow.add(anInoc.getProjectID());
						resultRow.add(anInoc.getNotes());
						myCell = new TableCell(anInoc.getID());
					} else {
						status = "<FONT COLOR='red'>Insert Failure</FONT>";
						myCell = new TableCell("");
					}
				} catch (DataException e) {
					status = "<FONT COLOR='red'>ERROR:</FONT> " + e.getMessage();
					myCell = new TableCell("");
				}

				myCell.addItem(String.format("<A HREF='%s/strain?id=%s'>%s</A>", this.myWrapper.getContextPath(), cultureID, cultureID));
				myCell.addItem(parentID);
				myCell.addItem(date);
				myCell.addItem(this.getFormValue("volume" + thisRow.toString()));
				myCell.addItem(media);
				if ( this.hasFormValue("inheritProj" + thisRow.toString()) ) 
					myCell.addItem("INHERITED");
				else
					myCell.addItem(projectCode);
				myCell.addItem(notes);
				if ( stock ) {
					myCell.addItem("Stock");
					resultRow.add("X");
				} else {
					myCell.addItem("");
				}
				myCell.addItem(status);
				TableRow aRow = new TableRow(myCell);
				aRow.setClass(curClass);
				aRow.setAttribute("align", "center");
				myTable.addItem(aRow);
				if ( curClass.equals("odd") ) {
					curClass = "even";
				} else {
					curClass = "odd";
				}
				myResults.add(resultRow);
			}
		}
		output.append(myTable);
		return output.toString();
	}

	public List<List<String>> getResults() {
		return this.myResults;
	}
	
}
