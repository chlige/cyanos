/**
 * 
 */
package edu.uic.orjala.cyanos.web.forms;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import edu.uic.orjala.cyanos.Assay;
import edu.uic.orjala.cyanos.AssayData;
import edu.uic.orjala.cyanos.Compound;
import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.Harvest;
import edu.uic.orjala.cyanos.Project;
import edu.uic.orjala.cyanos.Role;
import edu.uic.orjala.cyanos.Sample;
import edu.uic.orjala.cyanos.SampleAccount;
import edu.uic.orjala.cyanos.SampleCollection;
import edu.uic.orjala.cyanos.Separation;
import edu.uic.orjala.cyanos.Strain;
import edu.uic.orjala.cyanos.sql.SQLSample;
import edu.uic.orjala.cyanos.sql.SQLSampleCollection;
import edu.uic.orjala.cyanos.sql.SQLStrain;
import edu.uic.orjala.cyanos.web.BaseForm;
import edu.uic.orjala.cyanos.web.CyanosWrapper;
import edu.uic.orjala.cyanos.web.html.Div;
import edu.uic.orjala.cyanos.web.html.Form;
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
public class SampleForm extends BaseForm {

	public static final String EXTRACT_PROTOCOL = "extract protocol";
	public static String[] EXTRACT_PROTOCOL_KEYS = {"type", "solvent"};

	/**
	 * 
	 */
	public static final String DIV_ID = "sampleinfo";
	public static final String DIV_TITLE = "Samples";
	public static final String TXN_DIV_ID = "sampleTxn";
	public static final String TXN_DIV_TITLE = "Balance Sheet";
	public static final String DATA_FORM = "dataForm";
	
	public SampleForm(CyanosWrapper aWrapper) {
		super(aWrapper);
	}
	
	public Div sampleDiv(Sample aSample, boolean filteredList) {
		return this.collapsableDiv(DIV_ID, DIV_TITLE, this.sampleListContent(aSample, filteredList));
	}
	
	public String sampleList(Sample aSample, boolean filteredList) {
		return "<P ALIGN=CENTER><FONT SIZE=+2>Samples</FONT><P>" + this.sampleListContent(aSample, filteredList);
	}
	
	public Div sampleDiv(Strain aStrain, boolean filteredList) {
		try {
			Sample aSample = aStrain.getSamples();
			return this.collapsableDiv(DIV_ID, DIV_TITLE, this.sampleListContent(aSample, filteredList));
		} catch (DataException e) {
			return this.collapsableDiv(DIV_ID, DIV_TITLE, this.handleException(e));
		}

	}
	
	public String dataForm(Sample anObject) {
		if ( anObject.isAllowed(Role.WRITE) && this.hasFormValue("showBrowser") ) {
			Form myForm = new Form(DataForm.fileManagerApplet(this.myWrapper, "sample", anObject.getID(), null, false));				
			myForm.addItem(String.format("<P ALIGN='CENTER'><BUTTON TYPE='BUTTON' onClick=\"updateForm(this,'%s')\" NAME='cancelBrowser'>Close</BUTTON>", DATA_FORM));
			myForm.setAttribute("NAME", "photoBrowser");
			myForm.addHiddenValue("id", anObject.getID());
			myForm.addHiddenValue("div", DATA_FORM);
			return myForm.toString();
		} else {
			StringBuffer output = new StringBuffer();
			DataForm dataForm = new DataForm(this.myWrapper);
			dataForm.setTypeLabel(Sample.LC_DATA_TYPE, "LC Chromatogram");
			dataForm.setTypeLabel(Sample.MS_DATA_TYPE, "Mass Spectrum");
			dataForm.setTypeLabel(Sample.NMR_DATA_TYPE, "NMR Spectrum");
			output.append(dataForm.datafileTable(anObject));
			if ( anObject.isAllowed(Role.WRITE) )
				output.append(String.format("<FORM><P ALIGN='CENTER'><INPUT TYPE=HIDDEN NAME='id' VALUE='%s'/><BUTTON TYPE='BUTTON' NAME='showBrowser' onClick=\"loadForm(this, '%s')\">Manage Data Files</BUTTON></P></FORM>", anObject.getID(), DATA_FORM));
			return output.toString();
		}
	}

	public String sampleListContent(Sample aSample, boolean filteredList) {
		StringBuffer output = new StringBuffer();
		if ( filteredList ) {
			Form myForm = new Form("<P ALIGN=CENTER>Show: ");
			Popup showPop = new Popup();
			showPop.setName("show");
			showPop.addItemWithLabel("nolib", "Extracts + Fractions");
			showPop.addItemWithLabel("library", "Library Samples");
			showPop.addItemWithLabel("all", "All Samples");
			if ( this.hasFormValue("show"))
				showPop.setDefault(this.getFormValue("show"));
			showPop.setAttribute("onChange", String.format("reloadDiv('%s',this)", DIV_ID));

			myForm.addItem(showPop.toString());
			myForm.addItem(String.format("<INPUT TYPE='HIDDEN' NAME='id' VALUE='%s'/>", this.getFormValue("id")));
			myForm.addItem("<INPUT TYPE='HIDDEN' NAME='tab' VALUE='sample'></P>");
			output.append(myForm.toString());
		}

		String[] headerData = {"Sample", "Date", "Collection", "Location", "Balance", "Notes"};
		TableCell header = new TableHeader(headerData);

		header.setAttribute("class","header");
		TableRow tableRow = new TableRow(header);
		Table myTable = new Table(tableRow);
		myTable.setAttribute("class","dashboard");
		myTable.setAttribute("align","center");
		myTable.setAttribute("width","75%");

		String contextPath = this.myWrapper.getContextPath();
		
		try {
			if ( aSample != null ) {
				boolean skipLib = true;
				boolean skipOther = false;
				
				if ( filteredList && this.hasFormValue("show") ) {
					if ( this.getFormValue("show").equals("library") ) {
						skipLib = false; skipOther = true;
					} else if ( this.getFormValue("show").equals("all") ) {
						skipLib = false; skipOther = false;
					}
				}
				
				aSample.beforeFirst();
				String curClass = "odd";
				SimpleDateFormat myDate = this.myWrapper.dateFormat();

				SAMPLE_TABLE: while (aSample.next()) {
					if ( aSample.isLibrarySample() ) {
						if (skipLib && filteredList) continue SAMPLE_TABLE;
					} else if ( aSample.isSelfLibrarySource() || (! aSample.isLibrarySample()) ) {
						if (skipOther && filteredList) continue SAMPLE_TABLE;
					}
					TableCell myCell = new TableCell(String.format("<A HREF='%s/sample?id=%s'>%s</A>", contextPath, aSample.getID(), aSample.getName()));
					myCell.addItem(myDate.format(aSample.getDate()));
					SampleCollection aCol = aSample.getCollection();
					if ( aCol != null ) 
						myCell.addItem(String.format("<A HREF='%s/sample?col=%s'>%s</A>", contextPath, aCol.getID(), aCol.getName()));
					else 
						myCell.addItem(String.format("<A HREF='%s/sample?col=%s'>%s</A>", contextPath, aSample.getCollectionID(), aSample.getCollectionID()));				
					myCell.addItem(aSample.getLocation());
					myCell.addItem(formatAmount("%.2f %s", aSample.accountBalance(), aSample.getBaseUnit()));
					myCell.addItem(shortenString(aSample.getNotes(), 15));
					if ( aSample.isRemoved() )
						myCell.setClass("removed");
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
			}
		} catch (DataException e) {
			e.printStackTrace();
			tableRow.addItem("<TD COLSPAN='6' ALIGN='CENTER'><FONT COLOR='red'><B>SQL FAILURE:</FONT> " + e.getMessage() + "</TD>");
		}
		output.append(myTable.toString());
		
		return output.toString();
	}

	public Div assayDiv(Sample aSample) {
		return this.collapsableDiv("sampleassayinfo", "Assay Data", this.assayContent(aSample));
	}

	public String assayTable(Sample aSample) {
		return "<P ALIGN='CENTER'><FONT SIZE='+1'><B>Assay Data</B></FONT></P>" + this.assayContent(aSample);
	}
		
	public String assayContent(Sample aSample) {
		TableRow myRow = new TableRow();
		Table myTable = new Table();
		myTable.addItem(myRow);		
		myTable.setAttribute("class","dashboard");
		myTable.setAttribute("align","center");
		myTable.setAttribute("width","75%");

		try {
			String[] headers = {"Assay", "Date", "Target", "Activity", "Concentration" };
			TableCell myCell = new TableHeader(headers);
			myCell.setAttribute("class","header");
			myRow.addItem(myCell);
			if ( aSample == null || (! aSample.first() ) ) {
				myRow.addItem("<TD ALIGN='CENTER' COLSPAN=5><B>None</B></TD>");
			} else if ( aSample.first() ) {
				aSample.beforeFirst();
				while ( aSample.next() ) {
					myTable.addItem(String.format("<TR><TD COLSPAN='5' ALIGN='CENTER' CLASS='subheader'><B>Sample: <A HREF=\"%s/sample?id=%s\">%s</A></B></TD></TR>", this.myWrapper.getContextPath(), aSample.getID(), aSample.getName()));
					myTable.addItem(this.assayRows(aSample));
				}
			} else {
				myTable.addItem(this.assayRows(aSample));
			}

		} catch ( DataException e ) {
			myTable.addItem(String.format("<TR><TD COLSPAN='5' ALIGN='CENTER'><B><FONT COLOR='red'>SQL ERROR:</FONT> %s</B></TD></TR>", e.getMessage()));
		}
		myTable.setAttribute("WIDTH", "75%");
		myTable.setAttribute("ALIGN", "CENTER");
		return myTable.toString();
	}

	protected String assayRows(Sample aSample) {
		StringBuffer output = new StringBuffer();
		try {
			Assay myAssays = aSample.getAssays();
			if ( myAssays != null ) {		
				myAssays.beforeFirst();
				boolean odd = true;
				SimpleDateFormat myDate = this.myWrapper.dateFormat();
				while ( myAssays.next() ) {
					AssayData myData = myAssays.getAssayDataForSample(aSample);
					myData.beforeFirst();
					while ( myData.next() ) {

						TableCell myCell = new TableCell("<A HREF='assay?id=" + myAssays.getID() + "'>" + myAssays.getName() + "</A>");
						myCell.addItem(myDate.format(myAssays.getDate()));
						myCell.addItem(myAssays.getTarget());

						String activity = myData.getActivityString();
						if (activity == null) myCell.addItem("");
						else myCell.addItem(activity);

						float concentration = myData.getConcentration();
						if ( concentration == 0 ) myCell.addItem("-");
						else if ( concentration >= 1 ) myCell.addItem(formatAmount("%.0f %s", concentration, "mg/ml"));
						else myCell.addItem(formatAmount("%.0f %s", concentration, "ug/ml"));

						TableRow myRow = new TableRow(myCell);
						if ( myData.isActive() ) {
							if ( odd ) {
								myRow.setClass("dangerodd");
								odd = false;
							} else {
								myRow.setClass("dangereven");
								odd = true;
							}
						} else {
							if ( odd ) {
								myRow.setClass("odd");
								odd = false;
							} else {
								myRow.setClass("even");
								odd = true;
							}
						}
						myRow.setAttribute("align", "center");
						output.append(myRow.toString());
					}
				}
			} else {
				output.append("<TR><TD ALIGN='CENTER' COLSPAN=5><B>None</B></TD></TR>");
			}
		} catch ( DataException e ) {
			output.append(String.format("<TR><TD COLSPAN='5' ALIGN='CENTER'><B><FONT COLOR='red'>SQL ERROR:</FONT> %s</B></TD></TR>", e.getMessage()));
		}
		return output.toString();
	}

	private String sampleText(Sample aSample) {
		StringBuffer output = new StringBuffer();
		TableCell myCell;
		TableRow tableRow = new TableRow();

		try {
			if ( aSample.isRemoved() ) {
				tableRow.addItem(String.format("<TD COLSPAN=2><FONT SIZE='+1'><B>Sample Removed on %s by %s.</FONT></TD>", this.formatDate(aSample.getRemovedDate()), aSample.getRemovedByID()));

			}

			myCell = new TableCell("Serial Number:");
			myCell.addItem(this.getFormValue("id"));
			tableRow.addCell(myCell);

			
			myCell = new TableCell("Sample Label:");
			myCell.addItem(this.formatStringHTML(aSample.getName()));
			tableRow.addItem(myCell);

			Strain aStrain = new SQLStrain(this.getSQLDataSource(), aSample.getCultureID());
			myCell = new TableCell("Culture ID:");
			myCell.addItem("<A HREF='strain?id=" + aStrain.getID() + 
					"'>" + aStrain.getID() + " " + aStrain.getName() + "</A>");
			tableRow.addItem(myCell);

			if ( aSample.getLocation() != null ) {
				myCell = new TableCell("Location:");
				myCell.addItem(aSample.getLocation());
				tableRow.addItem(myCell);
			}

			myCell = new TableCell("Creation Date:");
			myCell.addItem(this.formatDate(aSample.getDate()));
			tableRow.addItem(myCell);

			myCell = new TableCell("Default Unit:");
			myCell.addItem(aSample.getBaseUnit());
			tableRow.addItem(myCell);

			myCell = new TableCell("Vial Wt:");
			myCell.addItem(aSample.getVialWeight());
			tableRow.addItem(myCell);

			myCell = new TableCell("Concentration (mg/ml):");
			myCell.addItem(formatAmount("%.1f %s", aSample.getConcentration(), "mg/ml"));
			tableRow.addItem(myCell);

			myCell = new TableCell("Project:");
			Project myProject = aSample.getProject();
			if ( myProject != null ) {
				myCell.addItem(myProject.getName());
			} else {
				myCell.addItem("NONE");
			}
			tableRow.addItem(myCell);		

			myCell = new TableCell("Notes:");
			myCell.addItem(this.formatStringHTML(aSample.getNotes()));
			tableRow.addItem(myCell);

			if ( aSample.isExtract() ) {
				tableRow.addItem("<TD COLSPAN=2><B>Extract Information</B></TD>");
				myCell = new TableCell("Type:");
				myCell.addItem(aSample.getExtractType());
				tableRow.addItem(myCell);

				myCell = new TableCell("Solvent:");
				myCell.addItem(aSample.getExtractSolvent());
				tableRow.addItem(myCell);
			} 

		} catch ( DataException e ) {
			output.append(this.handleException(e));
		}

		Table myTable = new Table(tableRow);
		myTable.setClass("species");
		myTable.setAttribute("align", "center");
		output.append(myTable.toString());

		return output.toString();		
	}
	
	private String sampleForm(Sample aSample) {
		StringBuffer output = new StringBuffer();
		TableCell myCell;
		TableRow tableRow = new TableRow();

		try {

			myCell = new TableCell("Serial Number:");
			myCell.addItem(this.getFormValue("id"));
			tableRow.addCell(myCell);

			myCell = new TableCell("Sample Label:");
			myCell.addItem("<TEXTAREA NAME='label' COLS='20' ROWS='2'>" + aSample.getName() + "</TEXTAREA>");
			tableRow.addItem(myCell);

			Strain aStrain = new SQLStrain(this.getSQLDataSource(), aSample.getCultureID());
			myCell = new TableCell("Culture ID:");
			myCell.addItem("<A HREF='strain?id=" + aStrain.getID() + 
					"'>" + aStrain.getID() + " " + aStrain.getName() + "</A>");
			tableRow.addItem(myCell);

			SampleCollection myCol = aSample.getCollection();
			myCell = new TableCell("Collection:");
			if ( myCol != null )
				myCell.addItem("<A HREF='?col=" + myCol.getID() + "'>" + myCol.getName() + "</A>");
			else 
				myCell.addItem("<A HREF='?col=" + aSample.getCollectionID() + "'>" + aSample.getCollectionID() + "</A>");

			tableRow.addItem(myCell);

			if ( aSample.getLocation() != null ) {
				myCell = new TableCell("Location:");
				myCell.addItem(aSample.getLocation());
				tableRow.addItem(myCell);
			}

			myCell = new TableCell("Creation Date:");
			myCell.addItem(aSample.getDate());
			tableRow.addItem(myCell);

			tableRow.addItem(this.makeFormTextRow("Default Unit:", "unit", aSample.getBaseUnit()));
			tableRow.addItem(this.makeFormTextRow("Vial Wt:", "vial_wt", aSample.getVialWeight()));


			myCell = new TableCell("Concentration (mg/ml):");
			myCell.addItem("<INPUT TYPE='text' NAME='conc' VALUE='" + 
					formatAmount("%.1f %s", aSample.getConcentration(), "mg/ml") + "'/> (0 for neat)");
			tableRow.addItem(myCell);


			myCell = new TableCell("Project Code:");
			try {
				Popup aPop = this.projectPopup();
				aPop.setName("project");
				String projectID = aSample.getProjectID();
				if ( projectID != null )
					aPop.setDefault(projectID);
				myCell.addItem(aPop.toString());
			} catch (DataException e) {
				myCell.addItem("<B><FONT COLOR='red'>ERROR:</FONT> " + e.getMessage() + "</B>"); 
			}

			tableRow.addItem(myCell);		

			myCell = new TableCell("Notes:");
			myCell.addItem("<TEXTAREA NAME='notes' COLS='40' ROWS='4'>" + aSample.getNotes() + "</TEXTAREA>");
			tableRow.addItem(myCell);

			if ( aSample.isExtract() ) {
				tableRow.addItem("<TD COLSPAN=2><B>Extract Information</B></TD>");
				tableRow.addItem(this.makeFormTextRow("Type:", "extractType", aSample.getExtractType()));
				tableRow.addItem(this.makeFormTextAreaRow("Solvent:", "extractSolvent", aSample.getExtractSolvent()));
			} 

		} catch ( DataException e ) {
			output.append("<P ALIGN='CENTER'><B><FONT COLOR='red'>ERROR:</FONT> " + e.getMessage() + "</B></P>");
		}

		tableRow.addItem("<TD COLSPAN='2' ALIGN='CENTER'><INPUT TYPE=SUBMIT NAME='updateSample' VALUE='Update'/><INPUT TYPE=RESET /></TD>");
		
		Table myTable = new Table(tableRow);
		myTable.setClass("species");
		myTable.setAttribute("align", "center");
		Form myForm = new Form(myTable);
		myForm.setAttribute("METHOD", "POST");
		myForm.addItem(String.format("<INPUT TYPE='HIDDEN' NAME='id' VALUE='%s'/>", aSample.getID()));
		myForm.setName("sample");

		output.append(myForm.toString());
		return output.toString();
	}
	
	
	private String updateSample(Sample aSample) {
		try {
			aSample.setManualRefresh();
			if ( this.hasFormValue("label") )
				aSample.setName(this.getFormValue("label"));
			if ( this.hasFormValue("vial_wt") ) 
				aSample.setVialWeight(this.getFormValue("vial_wt"));
			if ( this.hasFormValue("notes") )
				aSample.setNotes(this.getFormValue("notes"));
			if ( this.hasFormValue("unit") )
				aSample.setBaseUnit(this.getFormValue("unit"));
			if ( this.hasFormValue("extractType") )
				aSample.setExtractType(this.getFormValue("extractType"));
			if ( this.hasFormValue("extractSolvent") )
				aSample.setExtractSolvent(this.getFormValue("extractSolvent"));
			if ( this.hasFormValue("conc")) {
				float newConc = parseAmount(this.getFormValue("conc"), "mg/ml");
				aSample.setConcentration(newConc);				}
			if ( this.hasFormValue("project") && (! this.getFormValue("project").equals(aSample.getProjectID())) )
				aSample.setProjectID(this.getFormValue("project"));
			aSample.refresh();
			aSample.setAutoRefresh();
		} catch ( DataException e ) {
			return this.handleException(e);
		}
		return "<P ALIGN='CENTER'>Updated Sample</P>";

	}

	public String showSample(Sample aSample) {
		StringBuffer output = new StringBuffer();		
		Paragraph head = new Paragraph();
		head.setAlign("CENTER");
		StyledText title = new StyledText("Sample Details");
		title.setSize("+3");
		head.addItem(title);
		head.addItem("<HR WIDTH='85%'/>");
		output.append(head);

		Div sampleDiv = new Div();
		Div viewDiv = new Div();
		viewDiv.setID("view_sampleinfo");
		viewDiv.setClass("showSection");

		sampleDiv.addItem("<FORM><INPUT TYPE='BUTTON' STYLE='display:none'/></FORM>");
		
		if ( aSample.isAllowed(Role.DELETE) && this.hasFormValue("confirmDelete") ) {
			try {
				aSample.remove();
				sampleDiv.addItem("<DIV CLASS='messages'><B>Sample Removed!</B></DIV>");
			} catch (DataException e) {
				sampleDiv.addItem(this.handleException(e));
			}
		}
		
		if ( this.hasFormValue("deleteSample") && aSample.isAllowed(Role.DELETE) ) {
			Form delForm = new Form();
			delForm.setPost();
			delForm.addItem("<B>Delete Sample?</B> <INPUT TYPE=SUBMIT NAME='confirmDelete' VALUE='Confirm'/><INPUT TYPE='BUTTON' VALUE='Cancel' onClick='hideDiv(\"delete\")' />");
			delForm.addItem(String.format("<INPUT TYPE='HIDDEN' NAME='id' VALUE='%s'/>", aSample.getID()));
			Div deleteDiv = new Div(delForm);
			deleteDiv.setClass("messages");
			deleteDiv.setID("delete");
			sampleDiv.addItem(deleteDiv);
		}

		sampleDiv.addItem(viewDiv);
		
		boolean isActive = false;
		try {
			isActive = aSample.isActive();
		} catch (DataException e) {
			this.handleException(e);
		}
		

		
		if ( isActive && aSample.isAllowed(Role.WRITE) ) {
			
			
			if ( this.hasFormValue("updateSample") ) 
				this.updateSample(aSample);
			
		
			Div editDiv = new Div(this.sampleForm(aSample));
			viewDiv.addItem(this.sampleText(aSample));
			editDiv.setID("edit_sampleinfo");
			editDiv.setClass("hideSection");
			viewDiv.addItem("<P ALIGN='CENTER'><BUTTON TYPE='BUTTON' onClick='flipDiv(\"sampleinfo\")'>Edit Values</BUTTON></P>");
			editDiv.addItem("<P ALIGN='CENTER'><BUTTON TYPE='BUTTON' onClick='flipDiv(\"sampleinfo\")'>Close Form</BUTTON></P>");
			sampleDiv.addItem(editDiv);
		} else {
			viewDiv.addItem(this.sampleText(aSample));

		}		
		output.append(sampleDiv.toString());

		try {
			if ( aSample.isCompound() ) {
				output.append(this.compoundModule(aSample));
			} else if ( isActive && (! aSample.isLibrarySample()) ) {
				output.append(String.format("<P ALIGN='CENTER'><INPUT TYPE='BUTTON' NAME='makeCompound' VALUE='Add Compound Information' onClick='compoundForm(\"%s\");'/></P>", aSample.getID()));
			} else if ( aSample.isLibrarySample() && aSample.getLibrarySource().isCompound() ) {
				output.append( this.compoundModule(aSample.getLibrarySource()) );
			}
			
			if ( aSample.isLibrarySample() && (! aSample.isSelfLibrarySource()) ) {
				Sample source = aSample.getLibrarySource();
				output.append(String.format("<DIV CLASS='pageModule'><P CLASS='moduleText'>Library Source: <A HREF='sample?id=%s'>%s</A></P></DIV>", source.getID(), source.getName()));
			} else {
				Sample kids = aSample.getLibraryChildren();
				if ( kids != null ) {
					output.append(this.libraryModule(kids));
				}
			}

			if ( isActive ) {
				Form myForm = new Form("<P ALIGN='CENTER'>");
				if ( aSample.isAllowed(Role.WRITE)) {
					myForm.addItem("<INPUT TYPE='SUBMIT' NAME='go' VALUE='Move Sample'/><BR/>");
				}
				myForm.addItem(String.format("<INPUT TYPE='BUTTON' NAME='addQueue' VALUE='Add to a Work Queue' onClick='queueForm(\"sample\", \"%s\");'/></P>", aSample.getID()));
				myForm.setName("move");
				myForm.setAttribute("METHOD", "POST");
				myForm.setAttribute("ACTION", "move");
				myForm.addItem(String.format("<INPUT TYPE='HIDDEN' NAME='id' VALUE='%s'/>", aSample.getID()));
				output.append(myForm.toString());

				if ( aSample.isAllowed(Role.DELETE) ) {
					myForm = new Form("<P ALIGN='CENTER'><INPUT TYPE=SUBMIT NAME='deleteSample' VALUE='Delete'/></P>");
					myForm.setName("deleteForm");
					myForm.setAttribute("METHOD", "POST");
					myForm.addItem(String.format("<INPUT TYPE='HIDDEN' NAME='id' VALUE='%s'/>", aSample.getID()));
					output.append(myForm.toString());
				}
			} else {
				output.append("<P></P>");
			}
		} catch ( DataException e ) {
			output.append("<P ALIGN='CENTER'><B><FONT COLOR='red'>ERROR:</FONT> " + e.getMessage() + "</B></P>");
		}

		return output.toString();
	}

	public String transactionSheet(Sample aSample) {
		try {

			String[] myHeads = {"Date", "Description", "Reference", "Amount", "Balance"};
			TableCell myCell = new TableHeader(myHeads);
			myCell.setClass("header");
			TableRow aRow = new TableRow(myCell);
			
			SimpleDateFormat myDf = this.dateFormat();
			SampleAccount txnAccount = aSample.getAccount();
			txnAccount.beforeFirst();
			float balance = 0.0f;
			String scale = aSample.getBaseUnit();
			String posFormat = "%.2f %s";
			String negFormat = "<FONT COLOR='RED'>(" + posFormat + ")</FONT>";
			while (txnAccount.next()) {
				boolean isVoid = txnAccount.isVoid();
				myCell = new TableCell(myDf.format(txnAccount.getDate()));
				if ( isVoid ) {
					myCell.addItem(String.format("<FONT CLASS='voidNote'>Transaction voided on %s by %s</FONT><BR/>%s", this.formatDate(txnAccount.getVoidDate()), txnAccount.getVoidUserID(), txnAccount.getNotes()));
				} else {
					myCell.addItem(this.formatStringHTML(txnAccount.getNotes()));
				}
				Class refClass = txnAccount.getTransactionReferenceClass();
				String parentID = txnAccount.getTransactionReferenceID();
				if ( isVoid || parentID == null || refClass == null )
					myCell.addItem("");
				else if ( refClass.equals(Sample.class) ) {
					myCell.addItem(String.format("<A HREF='sample?id=%s'>Sample #%s</A>", parentID, parentID));
				} else if ( refClass.equals(Separation.class) ) {
					myCell.addItem(String.format("<A HREF='separation?id=%s'>Separation #%s</A>", parentID, parentID));
				} else if ( refClass.equals(Harvest.class) ) {
					myCell.addItem(String.format("<A HREF='harvest?id=%s'>Harvest #%s</A>", parentID, parentID));
				} else {
					myCell.addItem("UNKNOWN");
				}
				if ( isVoid ) {
					myCell.addItem(formatAmount(posFormat, txnAccount.getAmount(), scale));	
					myCell.setClass("voided");
					myCell.addItem("");
				} else {
					if ( txnAccount.getAmount() < 0 )
						myCell.addItem(formatAmount(negFormat, txnAccount.getAmount() * -1.0f, scale));					
					else
						myCell.addItem(formatAmount(posFormat, txnAccount.getAmount(), scale));

					balance = balance + txnAccount.getAmount();
					myCell.addItem(formatAmount("<B>%.2f %s", balance, scale));
				}
				aRow.addItem(myCell);
			}
			aRow.addItem(formatAmount("<TH COLSPAN='4' ALIGN='RIGHT' CLASS='footer'>Final Balance:</TH><TH CLASS='footer'>%.2f %s</TH>", aSample.accountBalance(), scale));
			Table myTable = new Table(aRow);
			myTable.setAttribute("ALIGN", "CENTER");
			myTable.setAttribute("WIDTH", "80%");
			myTable.setClass("balanceSheet");
			return myTable.toString();
		} catch (DataException e) {
			return this.handleException(e);
		}
	}

	public String compoundModule(Sample aSample) {
		Div contentDiv = new Div();
		contentDiv.setClass("hideSection");
		contentDiv.setID("div_cmpdModule");
		Image anImage = this.getImage("module-twist-closed.png");
		anImage.setAttribute("ID", "twist_cmpdModule");
		anImage.setAttribute("ALIGN", "absmiddle");

		Div moduleDiv = new Div("<P CLASS='moduleTitle'><A NAME='cmpdnModule' onClick='twistModule(\"cmpdModule\")'>");
		moduleDiv.addItem(anImage);
		moduleDiv.addItem(" Compound Information</A></P>");
		moduleDiv.addItem(contentDiv);
		moduleDiv.setClass("pageModule");

		try {
			TableRow tableRow = new TableRow();
			Compound myCompound = aSample.getCompound();
			tableRow.addItem("<TD>Compound ID:</TD><TD>" + myCompound.getID() +  "</TD>");
			tableRow.addItem("<TD>Formula:</TD><TD>" + myCompound.getHTMLFormula() +  "</TD>");
			tableRow.addItem("<TD COLSPAN=2><A HREF=\"compound?id=" + myCompound.getID() + "\"/>View Compound</A></TD>");
			Table myTable = new Table(tableRow);
			myTable.setClass("species");
			myTable.setAttribute("align", "center");
			contentDiv.addItem(myTable);
		} catch (DataException e) {
			contentDiv.addItem(this.handleException(e));
		}		
		
		return moduleDiv.toString();
	}
	
	public String libraryModule(Sample samples) {
		Div contentDiv = new Div();
		contentDiv.setClass("hideSection");
		contentDiv.setID("div_libModule");
		Image anImage = this.getImage("module-twist-closed.png");
		anImage.setAttribute("ID", "twist_libModule");
		anImage.setAttribute("ALIGN", "absmiddle");

		Div moduleDiv = new Div("<P CLASS='moduleTitle'><A NAME='libModule' onClick='twistModule(\"libModule\")'>");
		moduleDiv.addItem(anImage);
		moduleDiv.addItem(" Library Descendents</A></P>");
		moduleDiv.addItem(contentDiv);
		moduleDiv.setClass("pageModule");

		try {
			samples.beforeFirst();
			boolean oddRow = true;
			Table kidsTable = new Table("<TR><TH>Label</TH><TH>Collection</TH><TH>Date</TH><TH>Balance</TH></TR>");
			SimpleDateFormat df = this.dateFormat();
			while ( samples.next() ) {
				TableCell kidCell = new TableCell(String.format("<A HREF='sample?id=%s'>%s</A>", samples.getID(), samples.getName()));
				SampleCollection aCol = samples.getCollection();
				if ( aCol != null ) 
					kidCell.addItem(String.format("<A HREF='%s/sample?col=%s'>%s</A>", this.myWrapper.getContextPath(), aCol.getID(), aCol.getName()));
				else 
					kidCell.addItem(String.format("<A HREF='%s/sample?col=%s'>%s</A>",  this.myWrapper.getContextPath(), samples.getCollectionID(), samples.getCollectionID()));				
				kidCell.addItem(String.format("<A HREF='sample?col=%s'>%s</A>", aCol.getID(), aCol.getName()));
				kidCell.addItem(df.format(samples.getDate()));
				kidCell.addItem(formatAmount("%.2f %s", samples.accountBalance(), samples.getBaseUnit()));
				TableRow kidRow = new TableRow(kidCell);
				kidRow.setAttribute("align", "center");
				if ( oddRow ) {
					kidRow.setClass("odd");
					oddRow = false;
				} else {
					kidRow.setClass("even");
					oddRow = true;
				}
				kidsTable.addItem(kidRow);
			}
			kidsTable.setAttribute("class","dashboard");
			kidsTable.setAttribute("align","center");
			kidsTable.setAttribute("WIDTH", "80%");
			contentDiv.addItem(kidsTable);
		} catch (DataException e) {
			contentDiv.addItem(this.handleException(e));
		}		
		return moduleDiv.toString();
	}
	
	public String extractProtocolForm(Map<String,String> myProtocol) {
		Table myTable = new Table();
		myTable.setAttribute("ALIGN","CENTER");
		TableRow myRow = new TableRow();
		if ( myProtocol != null ) {
			myRow.addItem(this.makeFormTextRow("Extract Type:", "type", myProtocol.get("type")));
			myRow.addItem(this.makeFormTextAreaRow("Solvent:", "solvent", myProtocol.get("solvent")));
		} else {
			myRow.addItem(this.makeFormTextRow("Extract Type:", "type"));
			myRow.addItem(this.makeFormTextAreaRow("Solvent:", "solvent"));
		}
		myTable.addItem(myRow);
		return myTable.toString();
	}

	public String listSamples() {
		StringBuffer output = new StringBuffer();

		Paragraph head = new Paragraph();
		head.setAlign("CENTER");
		StyledText title = new StyledText("Sample List");
		title.setSize("+3");
		head.addItem(title);
		head.addItem("<HR WIDTH='85%'/>");
		output.append(head.toString());
		output.append("<FORM>");

		head = new Paragraph();
		head.setAlign("CENTER");
		try {
			head.addItem("<B>Collection:</B> ");
			Popup aPop = this.sampleCollectionPopup();
			aPop.setAttribute("onChange", "this.form.submit()");
			aPop.setName("col");
			head.addItem(aPop);
		} catch (DataException e) {
			head.addItem(this.handleException(e));
		}
		output.append(head.toString());
		output.append("</FORM>");

		if ( this.hasFormValue("col") && (! this.getFormValue("col").equals("")) ) {
			try {
				SampleCollection myCol = new SQLSampleCollection(this.getSQLDataSource(), this.getFormValue("col"));
				output.append(this.showCollection(myCol));
			} catch (DataException e ) {
				e.printStackTrace();
				output.append("<P ALIGN='CENTER'><B><FONT COLOR='red'>SQL FAILURE:</FONT> " + e.getMessage() + "</B></P>");
			}
		} else {			
			try {
				List<String> orphans = SQLSampleCollection.orphanedCollections(this.getSQLDataSource());				
				if ( orphans.size() > 0 ) {
					output.append("<P ALIGN='CENTER'><B>Orphan Sample Collections</B></FONT>");
					TableRow tableRow = new TableRow();
					ListIterator<String> anIter = orphans.listIterator();
					while ( anIter.hasNext() ) {
						String anId = anIter.next();
						tableRow.addItem(String.format("<TD><A HREF='?col=%s'>%s</A></TD>", anId, anId));
					}
					Table myTable = new Table(tableRow);
					output.append(myTable.toString());
				}
			} catch (DataException e ) {
				this.handleException(e);
			}
			
		}
		return output.toString();
	}
	
	public String showCollection(SampleCollection myCol) {	
		try {
			if ( myCol.isLoaded()) { 
				Div mainDiv = new Div(new Div(this.sampleLibraryForm(myCol)));

				if ( myCol.isBox() ) {
					mainDiv.addItem(this.collapsableDiv("box_view", "Box View", this.showBox(myCol)));
				}
				mainDiv.addItem(this.collapsableDiv("list_view", "Sample List", this.boxList(myCol)));
				return mainDiv.toString();
			} else {
				return ("<P ALIGN='CENTER'><B><FONT COLOR='red'>ERROR:</FONT> Collection does not exist.</B><BR>Please setup this collection.</P>" + this.addBoxForm());
			}
		} catch (DataException e) {
			return this.handleException(e);
		}
	}

	private String boxList(SampleCollection myCol) {
		try {
			boolean box = ( myCol.getWidth() > 0 && myCol.getLength() > 0 );
			TableCell vialHead;
			if ( box ) {
				String[] vialHeaders = {"Name", "Strain", "Date", "Location", "Type", "Balance", "Notes"};
				vialHead = new TableHeader(vialHeaders);
			} else {
				String[] vialHeaders = {"Name", "Strain", "Date", "Type", "Balance", "Notes"};
				vialHead = new TableHeader(vialHeaders);
			}

			vialHead.setAttribute("class","header");
			TableRow vialRow = new TableRow(vialHead);
			Table vialTable = new Table(vialRow);
			vialTable.setAttribute("class","dashboard");
			vialTable.setAttribute("align","center");
			vialTable.setAttribute("width","75%");

			Sample aSample = myCol.getSamples();
			SimpleDateFormat myFormat = this.dateFormat();
			if ( aSample != null ) {
				String curClass = "odd";
				aSample.beforeFirst();
				while (aSample.next()) {			
					TableCell myCell = new TableCell();
					myCell.addItem("<A HREF='?id=" + aSample.getID() + "'>" + aSample.getName() + "</A>");	
					Strain aStrain = new SQLStrain(this.getSQLDataSource(), aSample.getCultureID());
					if ( aStrain.first() )
						myCell.addItem(String.format("<A HREF='strain?id=%s'>%s <I>%s</I></A>", aStrain.getID(), aStrain.getID(), aStrain.getName()));
					else 
						myCell.addItem("");
					myCell.addItem(myFormat.format(aSample.getDate()));
					if ( box ) 
					myCell.addItem(aSample.getLocation());
					myCell.addItem("");
					myCell.addItem(formatAmount("%.2f %s", aSample.accountBalance(), aSample.getBaseUnit()));
					myCell.addItem(shortenString(aSample.getNotes(), 15));
					TableRow aRow = new TableRow(myCell);
					aRow.setClass(curClass);
					aRow.setAttribute("align", "center");
					vialTable.addItem(aRow);
					if ( curClass.equals("odd") ) {
						curClass = "even";
					} else {
						curClass = "odd";
					}
				}
			}
			vialTable.addItem("<TR><TD COLSPAN='6' ALIGN='CENTER'><A HREF='sample/export?col=" + myCol.getID() + "'>Export to CSV</A></TD></TR>");
			return vialTable.toString();

		} catch ( DataException e ) {
			return this.handleException(e);
		}
	}

	private String showBox(SampleCollection myCol) {
		try {
			TableCell boxHeader = new TableHeader("");
			for ( int i = 1; i <= myCol.getWidth(); i++ ) {
				boxHeader.addItem(Integer.toString(i));
			}

			TableRow row = new TableRow(boxHeader);
			myCol.beforeFirstRow();
			while ( myCol.nextRow() ) {
				myCol.beforeFirstColumn();
				TableCell thisRow = new TableCell();
				thisRow.setAttribute("ALIGN", "CENTER");
				thisRow.setAttribute("VALIGN", "TOP");
				while ( myCol.nextColumn() ) {
					Sample aSample = myCol.getCurrentSample();
					if ( aSample == null ) {
						Image anImage = this.getImage("empty.png");
						anImage.setAttribute("BORDER", "0");
						thisRow.addItem(anImage);
					} else { 
						Image anImage = this.getImage("filled.png");
						anImage.setAttribute("BORDER", "0");						
						thisRow.addItem(String.format("<A HREF='sample?id=%s'>%s</A>", aSample.getID(), anImage.toString()));
					}
				}					
				row.addItem(String.format("<TH WIDTH=3>%s</TH>%s", myCol.currentRowAlpha(), thisRow.toString()));
			}
			Table myTable = new Table(row);
			return myTable.toString();
		} catch ( DataException e ) {
			return this.handleException(e);
		}
	}

	private String sampleLibraryForm(SampleCollection aCollection) {
		try {
			if ( this.hasFormValue("updateAction") ) {
				aCollection.setManualRefresh();
				if ( this.hasFormValue("library") && ( ! this.getFormValue("library").equals(aCollection.getLibrary())) ) {
					if ( this.getFormValue("library").equals("") )
						aCollection.setLibrary(this.getFormValue("newLibrary"));
					else 
						aCollection.setLibrary(this.getFormValue("library"));
				}
				if ( this.hasFormValue("name") && ( ! this.getFormValue("name").equals(aCollection.getName())) )
					aCollection.setName(this.getFormValue("name"));
				if ( this.hasFormValue("notes") && (! this.getFormValue("notes").equals(aCollection.getNotes())) )
					aCollection.setNotes(this.getFormValue("notes"));
				if ( this.hasFormValue("width") ) {
					int width = Integer.parseInt(this.getFormValue("width"));
					if ( width != aCollection.getWidth() ) aCollection.setWidth(width);
				}
				if ( this.hasFormValue("length") ) {
					int length = Integer.parseInt(this.getFormValue("length"));
					if ( length != aCollection.getLength() ) aCollection.setLength(length);
				}
				aCollection.refresh();
			}

			TableCell myCell = new TableCell("Collection ID:");
			myCell.addItem(aCollection.getID());
			TableRow tableRow = new TableRow(myCell);
			
			myCell = new TableCell("Library:");
			Popup libPop = new Popup();
			libPop.addItemWithLabel("", "NEW LIBRARY ->");
			libPop.setName("library");
			StringBuffer libData = new StringBuffer();		
			try {
				List<String> libs = SQLSampleCollection.libraries(this.getSQLDataSource());
				ListIterator<String> anIter = libs.listIterator();
				while ( anIter.hasNext() ) {
					libPop.addItem(anIter.next());
				}
				libPop.setDefault(aCollection.getLibrary());
				libData.append(libPop.toString());
			} catch (DataException e) {
				libData.append("<FONT COLOR='red'><B>Cannot load libraries</FONT><BR/>" + e.getMessage() + "</B>");
			}
			
			libData.append("<INPUT TYPE='TEXT' NAME='newLibrary' SIZE='25'/>");		
			myCell.addItem(libData.toString());
			tableRow.addItem(myCell);

			tableRow.addItem(this.makeFormTextRow("Name:", "name", aCollection.getName()));

			myCell = new TableCell("Size:");
			myCell.addItem(String.format("<INPUT TYPE=TEXT SIZE=5 NAME='length' VALUE='%d'> X <INPUT TYPE=TEXT SIZE=5 NAME='width' VALUE='%s'> (0x0 for a list)", aCollection.getLength(), aCollection.getWidth()));
			tableRow.addItem(myCell);
			tableRow.addItem(this.makeFormTextAreaRow("Notes:", "notes", aCollection.getNotes()));

			myCell = new TableCell("<INPUT TYPE=SUBMIT NAME='updateAction' VALUE='Update'/><INPUT TYPE='RESET'/>");
			myCell.setAttribute("colspan","2");
			myCell.setAttribute("align","center");
			tableRow.addItem(myCell);
			Table myTable = new Table(tableRow);
			myTable.setClass("species");
			myTable.setAttribute("width", "80%");
			myTable.setAttribute("align", "center");
			Form myForm = new Form(myTable);
			myForm.setName("sampleCollection");
			myForm.setAttribute("METHOD","POST");

			return myForm.toString();
		} catch (DataException e) {
			e.printStackTrace();
			return ("<P ALIGN='CENTER'><B><FONT COLOR='red'>ERROR:</FONT> " + e.getMessage() + "</B></P>");
		}
	}
	
	public String addBoxForm() {
		TableRow tableRow = new TableRow(this.makeFormTextRow("Collection ID:", "col"));
		TableCell myCell = new TableCell("Library:");
		Popup libPop = new Popup();
		libPop.setName("library");
		libPop.addItemWithLabel("", "NEW LIBRARY ->");
		StringBuffer libData = new StringBuffer();
		try {
			List<String> libs = SQLSampleCollection.libraries(this.getSQLDataSource());
			ListIterator<String> anIter = libs.listIterator();
			while ( anIter.hasNext() ) {
				libPop.addItem(anIter.next());
			}
			if ( this.hasFormValue("library") ) libPop.setDefault(this.getFormValue("library"));
			libData.append(libPop.toString());
		} catch (DataException e) {
			libData.append("<FONT COLOR='red'><B>Cannot load libraries</FONT><BR/>" + e.getMessage() + "</B>");
		}
		if ( this.hasFormValue("newLibrary"))
			libData.append(String.format("<INPUT TYPE='TEXT' NAME='newLibrary' SIZE='25' VALUE='%s'/>", this.getFormValue("newLibrary")));
		else 
			libData.append("<INPUT TYPE='TEXT' NAME='newLibrary' SIZE='25'/>");		
		myCell.addItem(libData.toString());
		tableRow.addItem(myCell);
		tableRow.addItem(this.makeFormTextRow("Name:", "name"));
		myCell = new TableCell("Default Type:");
		Popup aPop = new Popup();
		aPop.addItemWithLabel("extract", "Extract");
		aPop.addItemWithLabel("fraction", "Fraction");
		aPop.addItemWithLabel("compound", "Compound");
		aPop.setName("type");
		if (this.hasFormValue("type")) aPop.setDefault(this.getFormValue("type"));
		myCell.addItem(aPop);
		tableRow.addItem(myCell);
		myCell = new TableCell("Size:");
		String length = "", width = "";
		if ( this.hasFormValue("length") )
			length = this.getFormValue("length");
		if ( this.hasFormValue("width") )
			width = this.getFormValue("width");
		String checked = "";
		if ( this.hasFormValue("asList"))
			checked = "CHECKED";
		myCell.addItem(String.format("<INPUT TYPE=TEXT SIZE=5 NAME='length' VALUE='%s'> X <INPUT TYPE=TEXT SIZE=5 NAME='width' VALUE='%s'>" +
				"<INPUT TYPE='CHECKBOX' NAME='asList' onClick='this.form.length.disabled=this.checked; this.form.width.disabled=this.checked;'/>Unformatted list", 
					length, width,checked));
		tableRow.addItem(myCell);
		tableRow.addItem(this.makeFormTextAreaRow("Notes:", "notes"));

		myCell = new TableCell("<INPUT TYPE=SUBMIT NAME='addCollection' VALUE='Add'/><INPUT TYPE='RESET'/>");
		myCell.setAttribute("colspan","2");
		myCell.setAttribute("align","center");
		tableRow.addItem(myCell);
		Table myTable = new Table(tableRow);
		myTable.setClass("species");
		myTable.setAttribute("width", "80%");
		myTable.setAttribute("align", "center");
		Form myForm = new Form(myTable);
		myForm.setName("sampleCollection");
		myForm.setAttribute("METHOD","POST");
		myForm.setAttribute("ACTION", String.format("%s/sample/newCollection", this.myWrapper.getContextPath()));
		return myForm.toString();
	}

	public String addSampleForm() {
		TableCell myCell;
		TableRow tableRow = new TableRow();

		try {
			myCell = new TableCell("Collection:");
			Popup aPop = this.sampleCollectionPopup();
			aPop.setAttribute("onChange", "this.form.submit()");
			aPop.setName("col");
			myCell.addItem(aPop);
			tableRow.addItem(myCell);
		} catch (DataException e) {
			return this.handleException(e);
		}
	
		if ( this.hasFormValue("col") ) {
			try {
				SampleCollection aCol = new SQLSampleCollection(this.getSQLDataSource(), this.getFormValue("col"));
				aCol.first();
				if ( aCol.getWidth() > 0 && aCol.getLength() > 0 ) 
					tableRow.addItem(this.makeFormTextRow("Name:", "label") + "<TD ROWSPAN='9'>" + this.boxForm(aCol, "location") + "</TD>");
				else 
					tableRow.addItem(this.makeFormTextRow("Name:", "label"));

				myCell = new TableCell("Culture ID:");
				try {
					Popup aPop = new Popup();
					Strain aStrain = SQLStrain.strains(this.getSQLDataSource(), "CAST(culture_id AS UNSIGNED)", SQLStrain.ASCENDING_SORT);
					aStrain.beforeFirst();
					while (aStrain.next()) {
						String label = String.format("%s %s", aStrain.getID(), aStrain.getName());
						aPop.addItemWithLabel(aStrain.getID(), label);
					}
					aPop.setName("culture_id");
					myCell.addItem(aPop);
				} catch (DataException e) {
					e.printStackTrace();
					myCell.addItem("<B><FONT COLOR='red'>SQL Failure:</FONT>" + e.getMessage() + "</B>");
				}
				tableRow.addItem(myCell);
				tableRow.addItem(this.makeFormDateRow("Date:", "date", "sample"));
				tableRow.addItem(this.makeFormTextRow("Default unit:", "unit"));
				tableRow.addItem(this.makeFormTextRow("Vial Weight:", "vial_wt"));
				tableRow.addItem(this.makeFormTextRow("Initial Amount:", "init_amount"));

				myCell = new TableCell("Project Code:");
				try {
					Popup aPop = this.projectPopup();
					aPop.setName("project");
					myCell.addItem(aPop.toString());
				} catch (DataException e) {
					myCell.addItem("<B><FONT COLOR='red'>ERROR:</FONT> " + e.getMessage() + "</B>"); 
				}
				tableRow.addItem(myCell);

				tableRow.addItem(this.makeFormTextAreaRow("Notes:", "notes"));

				myCell = new TableCell("<INPUT TYPE=SUBMIT NAME='addSample' VALUE='Add Sample'/><INPUT TYPE='RESET'/>");
				myCell.setAttribute("colspan","2");
				myCell.setAttribute("align","center");
				tableRow.addItem(myCell);
			} catch (DataException ex) {
				tableRow.addItem("<TD><B><FONT COLOR='red'>ERROR:</FONT>" + ex.getMessage() + "</FONT></TD>");
				ex.printStackTrace();
			}

		} 

		Table myTable = new Table(tableRow);
		myTable.setClass("species");
		myTable.setAttribute("width", "80%");
		myTable.setAttribute("align", "center");
		Form myForm = new Form(myTable);
		myForm.setName("sample");
		myForm.setAttribute("METHOD","POST");

		return myForm.toString();
	}

	private String boxForm(SampleCollection myCol, String formParameter) {
		try {
			TableCell boxHeader = new TableHeader("");
			for ( int i = 1; i <= myCol.getWidth(); i++ ) {
				boxHeader.addItem(Integer.toString(i));
			}

			TableRow row = new TableRow(boxHeader);
			String tdWidth = Float.toString(100.0f / myCol.getWidth()) + "%";
			myCol.beforeFirstRow();
			while ( myCol.nextRow() ) {
				myCol.beforeFirstColumn();
				TableCell thisRow = new TableCell();
				thisRow.setAttribute("WIDTH", tdWidth);
				thisRow.setAttribute("ALIGN", "CENTER");
				thisRow.setAttribute("VALIGN", "TOP");
				while ( myCol.nextColumn() ) {
					Sample aSample = myCol.getCurrentSample();
					if ( aSample == null ) 
						thisRow.addItem(String.format("<INPUT TYPE='RADIO' NAME='%s' VALUE='%s,%s'/>",formParameter, myCol.currentRowIndex(), myCol.currentColumnIndex()));
					else 
						thisRow.addItem(String.format("<A HREF='sample?id=%s'><IMG SRC='filled.png' BORDER=0></A>", aSample.getID()));
				}					
				row.addItem(String.format("<TH WIDTH=3>%s</TH>%s", myCol.currentRowAlpha(), thisRow.toString()));
			}
			Table myTable = new Table(row);
			return myTable.toString();
		} catch ( DataException e ) {
			e.printStackTrace();
			return ("<P ALIGN='CENTER'><B><FONT COLOR='red'>ERROR:</FONT> " + e.getMessage() + "</B></P>");
		}
	}

	public String addBox() {
		if ( this.hasFormValue("addCollection")) {
			Paragraph aParagraph = new Paragraph("Adding a new sample collection...<BR/>");
			aParagraph.setAlign("CENTER");
			try {
				if ( this.hasFormValue("col")) {
					SampleCollection aCol = SQLSampleCollection.create(this.getSQLDataSource(), this.getFormValue("col"));
					aCol.first();
					aCol.setManualRefresh();
					if ( this.hasFormValue("library") && this.getFormValue("library").length() > 0 )
						aCol.setLibrary(this.getFormValue("library"));
					else 
						aCol.setLibrary(this.getFormValue("newLibrary"));
					aCol.setName(this.getFormValue("name"));
					if ( this.hasFormValue("asList") || this.getFormValue("length").equals("") || this.getFormValue("width").equals("") ) {
						aCol.setLength(0);
						aCol.setWidth(0);				
					} else {
						aCol.setLength(Integer.parseInt(this.getFormValue("length")));
						aCol.setWidth(Integer.parseInt(this.getFormValue("width")));
					}
					aCol.setNotes(this.getFormValue("notes"));
					aCol.refresh();
					aParagraph.addItem("<B><FONT COLOR='greem'>SUCCESS:</FONT> Added a new sample collection.</B><BR/>");
					aParagraph.addItem(String.format("<A HREF='../sample?col=%s'>View sample collection</A>", aCol.getID()));
				} else {
					aParagraph.addItem("<P ALIGN='CENTER'><B><FONT COLOR='red'>ERROR:</FONT> Could not create sample collection.</B></P>");
				}
			} catch (DataException e) {
				return this.handleException(e);
			}
			return aParagraph.toString();
		} else {
			return this.addBoxForm();
		}
	}
	
	public String addSample() {
		if ( this.hasFormValue("addSample")) {
			Paragraph aParagraph = new Paragraph("Adding a new sample...<BR/>");
			aParagraph.setAlign("CENTER");
			try {
				if ( this.hasFormValue("col")) {
					SampleCollection aCol = new SQLSampleCollection(this.getSQLDataSource(), this.getFormValue("col"));
					aCol.first();
					List<String> missingParms = new ArrayList<String>();
					boolean isBox = ( aCol.getWidth() > 0 && aCol.getLength() > 0 );
					aParagraph.addItem("Validating form data...");
					if ( isBox ) {
						if ( ! this.hasFormValue("location") ) missingParms.add("Location");
					}
					if ( ! this.hasFormValue("culture_id")) missingParms.add("Culture ID");
					if ( missingParms.size() > 0 ) {
						aParagraph.addItem("<B><FONT COLOR='red'>FAILED</FONT><BR/>The following parameters are missing</B><BR/>");
						ListIterator anEnum = missingParms.listIterator();
						while ( anEnum.hasNext() ) {
							aParagraph.addItem((String)anEnum.next());
							aParagraph.addItem("<BR/>");
						}
						return aParagraph.toString() + this.addSampleForm();
					}
					aParagraph.addItem("<B><FONT COLOR='green'>SUCCESS</FONT><BR/>");
					Sample aSample = SQLSample.createInProject(this.getSQLDataSource(), this.getFormValue("culture_id"), this.getFormValue("project"));
					if ( aSample.first() ) {
						aSample.setManualRefresh();
						aSample.setCollectionID(aCol.getID());
						if ( this.hasFormValue("type") ) {
						// TODO need to figure this out...
//							if ( this.getFormValue("type").equals("extract") ) 
//								aSample.makeExtract(harvestID);
						}
						aSample.setDate(this.getFormValue("date"));
						if ( this.hasFormValue("unit") && (! this.getFormValue("unit").equals("")) ) aSample.setBaseUnit(this.getFormValue("unit"));
						if ( isBox ) {
							String vals[] = this.getFormValue("location").split(",");
							aSample.setLocation(vals[0], vals[1]);
						}
						aSample.setVialWeight(this.getFormValue("vial_wt"));
						aSample.setName(this.getFormValue("label"));
						aSample.setNotes(this.getFormValue("notes"));
						aSample.refresh();
						aParagraph.addItem("<B><FONT COLOR='greem'>SUCCESS:</FONT> Added a new sample.</B>");
						if ( this.hasFormValue("init_amount") && (! this.getFormValue("init_amount").equals("")) ) {
							SampleAccount myAcct = aSample.getAccount();
							if ( myAcct.addTransaction() ) {
								myAcct.setDate(this.getFormValue("date"));
								myAcct.setNotes("Initial amount");
								myAcct.depositAmount(this.getFormValue("init_amount"), this.getFormValue("unit"));
								myAcct.updateTransaction();
								aParagraph.addItem("<B><FONT COLOR='greem'>SUCCESS:</FONT> Updated initial amount.</B>");
							}
							aParagraph.addItem("<P ALIGN='CENTER'><B><FONT COLOR='red'>ERROR:</FONT> Could not set initial amount.</B></P>");
						}
						aParagraph.addItem(String.format("New sample created. <A HREF='sample?id=%s'>View sample</A>", aSample.getID()));
					} else {
						aParagraph.addItem("<P ALIGN='CENTER'><B><FONT COLOR='red'>ERROR:</FONT> Could not create sample.</B></P>");
					}
				}
			} catch (DataException e) {
				e.printStackTrace();
				aParagraph.addItem("<P ALIGN='CENTER'><B><FONT COLOR='red'>ERROR:</FONT> " + e.getMessage() + "</B></P>");
			}
			return aParagraph.toString();
		} else {
			return this.addSampleForm();
		}
	}


}
