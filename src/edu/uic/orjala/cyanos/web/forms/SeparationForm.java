/**
 * 
 */
package edu.uic.orjala.cyanos.web.forms;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ListIterator;

import edu.uic.orjala.cyanos.Compound;
import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.Material;
import edu.uic.orjala.cyanos.Project;
import edu.uic.orjala.cyanos.Role;
import edu.uic.orjala.cyanos.Separation;
import edu.uic.orjala.cyanos.Separation.SeparationTemplate;
import edu.uic.orjala.cyanos.Strain;
import edu.uic.orjala.cyanos.sql.SQLCompound;
import edu.uic.orjala.cyanos.sql.SQLData;
import edu.uic.orjala.cyanos.sql.SQLSeparation;
import edu.uic.orjala.cyanos.sql.SQLStrain;
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
 * @author george
 *
 */
public class SeparationForm extends BaseForm {

	public static final String DIV_TITLE = "Separations";
	public static final String DIV_ID = "sepInfo";
	public static final String DATA_FORM = "dataForm";

	public static final String COMPOUND_FORM_ID = "compoundform";

	/**
	 * @param callingServlet
	 */
	public SeparationForm(CyanosWrapper aWrapper) {
		super(aWrapper);
	}
	
	public String sepList(Separation aSep) {
		StringBuffer output = new StringBuffer();

		String[] headerData = {"Separation", "Date", "Stationary Phase", "Mobile Phase", "Notes"};
		TableCell header = new TableHeader(headerData);
		header.setAttribute("class","header");
		TableRow tableRow = new TableRow(header);
		Table myTable = new Table(tableRow);
		myTable.setAttribute("class","dashboard");
		myTable.setAttribute("align","center");
		myTable.setAttribute("width","75%");
		
		String curClass = "odd";
		SimpleDateFormat myDate = this.dateFormat();
		
		
		try {
			aSep.beforeFirst();

			while (aSep.next()) {
				TableCell myCell = new TableCell(String.format("<A HREF='separation?id=%s'>%s</A>", aSep.getID(), aSep.getID()));
				myCell.addItem(myDate.format(aSep.getDate()));
				myCell.addItem(shortenString(aSep.getStationaryPhase(), 12));
				myCell.addItem(shortenString(aSep.getMobilePhase(), 12));
				myCell.addItem(shortenString(aSep.getNotes(),12));
				TableRow aRow = new TableRow(myCell);
				if ( aSep.isRemoved() )
					myCell.setClass("removed");
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
			myTable.addItem("<TR ALIGN='CENTER'><TD COLSPAN='5'><FONT COLOR='red'><B>SQL FAILURE:</FONT></B> ");
			myTable.addItem(e.getMessage());
			myTable.addItem("</TD></TR>");
		}

		output.append(myTable.toString());
		return output.toString();
	}

	public Div separationDiv(Separation aSep) {
		return this.collapsableDiv(DIV_ID, DIV_TITLE, this.sepList(aSep));
	}
	
	public Div separationDiv(Strain aStrain) {
		try {
			Separation aSep = SQLSeparation.separationsForStrain(this.getSQLDataSource(), aStrain);
			return this.collapsableDiv(DIV_ID, DIV_TITLE, this.sepList(aSep));
		} catch (DataException e) {
			return this.collapsableDiv(DIV_ID, DIV_TITLE, this.handleException(e));
		}
	}
	
	public String separationList(Strain aStrain) {
		try {
			Separation aSep = SQLSeparation.separationsForStrain(this.getSQLDataSource(), aStrain);
			return this.sepList(aSep);
		} catch (DataException e) {
			return this.handleException(e);
		}

	}
	
	private String separationForm(Separation thisSep) {
		try {
			Form myForm = new Form(String.format("<INPUT TYPE='HIDDEN' NAME='id' VALUE='%s'/>",thisSep.getID()));
			myForm.setAttribute("METHOD", "POST");
			myForm.setName("sepForm");
			Table editTable = new Table();
			editTable.setAttribute("ALIGN","CENTER");
			TableCell myCell = new TableCell("Serial Number:");
			myCell.addItem(thisSep.getID());
			TableRow myRow = new TableRow(myCell);
			try {
				Popup tagPop = this.tagPopup();
				tagPop.setDefault(thisSep.getTag());
				tagPop.setName("tag");
				myRow.addItem(String.format("<TD>Tag:</TD><TD>%s <INPUT TYPE='TEXT' NAME='newTag' SIZE='10'></TD>", tagPop.toString()));
			} catch (DataException e) {
				myRow.addItem(this.makeFormTextRow("Tag:", "tag", thisSep.getTag()));
			}
			myRow.addItem(this.makeFormDateRow("Date:", "date", "sepForm", thisSep.getDateString()));
			myRow.addItem(this.makeFormTextRow("Stationary Phase:", "sphase", thisSep.getStationaryPhase()));
			myRow.addItem(this.makeFormTextRow("Mobile Phase:", "mphase", thisSep.getMobilePhase()));
			myRow.addItem(this.makeFormTextAreaRow("Method:", "method", thisSep.getMethod()));
			myRow.addItem(this.makeFormTextAreaRow("Notes:", "notes", thisSep.getNotes()));
			try {
				Popup projectPop = this.projectPopup();
				projectPop.setName("project");
				projectPop.setDefault(thisSep.getProjectID());
				myRow.addItem(String.format("<TD>Project:</TD><TD>%s</TD>", projectPop.toString()));
			} catch (DataException e) {
				myRow.addItem(String.format("<TD>Project:</TD><TD>ERROR: %s</TD>", e.getLocalizedMessage()));					
			}
			myRow.addItem("<TD COLSPAN='2' ALIGN='CENTER'><INPUT TYPE='SUBMIT' NAME='updateSep' VALUE='Update'/><INPUT TYPE='RESET'/></TD>");
			editTable.addItem(myRow);
			myForm.addItem(editTable);
			return myForm.toString();
		} catch (DataException e) {
			return this.handleException(e);
		}
	}
	
	public String showSeparation(Separation thisSep) {
		Div mainDiv = new Div();
		try {


			if ( thisSep.first() ) {
				mainDiv.addItem("<FORM><INPUT TYPE='BUTTON' STYLE='display:none'/></FORM>");
				
				if ( thisSep.isAllowed(Role.DELETE) ) {
					if ( this.hasFormValue("confirmDelete") ) {
						try {
							thisSep.remove();
							mainDiv.addItem("<DIV CLASS='messages'><P><B>Separation Removed!</B></P></DIV>");
						} catch (DataException e) {
							mainDiv.addItem(this.handleException(e));
						}
					} else if ( this.hasFormValue("deleteSep") ) {
						Form delForm = new Form();
						delForm.setPost();
						delForm.addItem("<P><B>Delete Separation?</B> <INPUT TYPE=SUBMIT NAME='confirmDelete' VALUE='Confirm'/><INPUT TYPE='BUTTON' VALUE='Cancel' onClick='hideDiv(\"delete\")' /></P>");
						delForm.addItem(String.format("<INPUT TYPE='HIDDEN' NAME='id' VALUE='%s'/>", thisSep.getID()));
						Div deleteDiv = new Div(delForm);
						deleteDiv.setClass("messages");
						deleteDiv.setID("delete");
						mainDiv.addItem(deleteDiv);
					}
				}

				if ( thisSep.isAllowed(Role.WRITE) && this.hasFormValue("updateSep")  ) 
					mainDiv.addItem(this.updateSeparation(thisSep));
				
				Div viewDiv = new Div();
				viewDiv.setID("view_sepinfo");
				viewDiv.setClass("showSection");
				mainDiv.addItem(viewDiv);
				viewDiv.addItem(this.separationText(thisSep));

				if ( thisSep.isAllowed(Role.WRITE) && thisSep.isActive() ) {
					Div editDiv = new Div(this.separationForm(thisSep));
					editDiv.setID("edit_sepinfo");
					editDiv.setClass("hideSection");
					viewDiv.addItem("<P ALIGN='CENTER'><BUTTON TYPE='BUTTON' onClick='flipDiv(\"sepinfo\")'>Edit Values</BUTTON></P>");
					editDiv.addItem("<P ALIGN='CENTER'><BUTTON TYPE='BUTTON' onClick='flipDiv(\"sepinfo\")'>Close Form</BUTTON></P>");
					mainDiv.addItem(editDiv);
				}
				
				
				if ( thisSep.isActive() && thisSep.isAllowed(Role.DELETE) ) {
					Form myForm = new Form("<P ALIGN='CENTER'><INPUT TYPE='SUBMIT' NAME='deleteSep' VALUE='Delete'/></P>");
					myForm.setPost();
					myForm.addHiddenValue("id", thisSep.getID());
					mainDiv.addItem(myForm);					
				}

			} else {
				mainDiv.addItem("<FONT COLOR='red'><B>ERROR:</B></FONT> cannot find specified separation");
			}
		} catch (DataException e) {
			mainDiv.addItem(this.handleException(e));
		}
		return mainDiv.toString();
	}
	
	private String separationText(Separation thisSep) {
		try {
			Table myTable = new Table();
			myTable.setAttribute("ALIGN","CENTER");
			TableRow myRow = new TableRow();
			if ( thisSep.isRemoved() ) {
				myRow.addItem(String.format("<TD COLSPAN=2><FONT CLASS='headline'>Separation Removed on %s by %s.</FONT></TD>", this.formatDate(thisSep.getRemovedDate()), thisSep.getRemovedByID()));
			}
			TableCell myCell = new TableCell("Serial Number:");
			myCell.addItem(String.format("%s <A HREF='separation/export?id=%s'>Export Data</A>",thisSep.getID(), thisSep.getID()));
			myRow.addItem(myCell);
			
			myRow.addItem(String.format("<TD>Tag:</TD><TD>%s</TD>", thisSep.getTag()));
			Project aProject = thisSep.getProject();
			if ( aProject != null && aProject.first() ) {
				myRow.addItem(String.format("<TD>Project:</TD><TD><A HREF='%s/project?id=%s'>%s</A></TD>", this.myWrapper.getContextPath(), aProject.getID(), aProject.getName()));
			} else {
				myRow.addItem("<TD>Project:</TD><TD>NONE</TD>");
			}
			myRow.addItem(String.format("<TD>Date:</TD><TD>%s</TD>", this.formatDate(thisSep.getDate())));
			myRow.addItem(String.format("<TD>Stationary Phase:</TD><TD>%s</TD>", formatStringHTML(thisSep.getStationaryPhase())));
			myRow.addItem(String.format("<TD>Mobile Phase:</TD><TD>%s</TD>", formatStringHTML(thisSep.getMobilePhase())));
			myRow.addItem(String.format("<TD>Method:</TD><TD>%s</TD>", formatStringHTML(thisSep.getMethod())));
			myRow.addItem(String.format("<TD>Notes:</TD><TD>%s</TD>", formatStringHTML(thisSep.getNotes())));
			myTable.addItem(myRow);
			return myTable.toString();
		} catch (DataException e) {
			return this.handleException(e);
		}
	}
	
	private String updateSeparation(Separation aSep) {
		StringBuffer output = new StringBuffer("<P ALIGN='CENTER'>");
		try {
			if ( ! this.getFormValue("tag").equals(aSep.getTag()) ) {
				output.append("Updating tag.");
				if ( this.getFormValue("tag").equals("") ) {
					if ( ! this.getFormValue("newTag").equals("") ) {
						aSep.setTag(this.getFormValue("newTag"));
					}
				} else 
					aSep.setTag(this.getFormValue("tag"));
				output.append("<BR/>");
			}
			if ( ! this.getFormValue("date").equals(aSep.getDateString()) ) {
				output.append("Updating date.");
				aSep.setDate(this.getFormValue("date"));
				output.append("<BR/>");
			}
			if ( ! this.getFormValue("sphase").equals(aSep.getStationaryPhase()) ) {
				output.append("Updating stationary phase data.");
				aSep.setStationaryPhase(this.getFormValue("sphase"));
				output.append("<BR/>");
			}
			if ( ! this.getFormValue("mphase").equals(aSep.getMobilePhase()) ) {
				output.append("Updating mobile phase data.");
				aSep.setMobilePhase(this.getFormValue("mphase"));
				output.append("<BR/>");
			}
			if ( ! this.getFormValue("method").equals(aSep.getMethod()) ) {
				output.append("Updating method information.");
				aSep.setMethod(this.getFormValue("method"));
				output.append("<BR/>");
			}
			if ( ! this.getFormValue("notes").equals(aSep.getNotes()) ) {
				output.append("Updating notes.");
				aSep.setNotes(this.getFormValue("notes"));
				output.append("<BR/>");
			}
		} catch (Exception e) {
			e.printStackTrace();
			output.append("<FONT COLOR='red'><B>SQL FAILURE</B></FONT> " + e.getMessage());
		}
		output.append("</P>");
		return output.toString();
	}

	private Popup tagPopup() throws DataException {
		Popup aPop = new Popup();
		aPop.addItemWithLabel("", "Use Value ->");
		List<String> tags = SQLSeparation.tags(this.getSQLDataSource());
		ListIterator<String> anIter = tags.listIterator();
		while ( anIter.hasNext() ) {
			aPop.addItem(anIter.next());
		}
		return aPop;
	}
	
	public String sampleTable(Separation aSep, Material sources) {
		String[] headers = {"Sample", "Culture ID", "Date", "Collection", "Location", "Amount" };
		TableCell myCell = new TableHeader(headers);
		myCell.setClass("header");
		TableRow myRow = new TableRow(myCell);
		Table myTable = new Table(myRow);
		myTable.setClass("list");
		
		try {
			SQLData myData = this.myWrapper.getSQLDataSource();
			Strain aStrain = null;
//			SampleCollection aCol = null;

			if ( sources != null && sources.first() ) {
				SimpleDateFormat myDateFormat = this.dateFormat();
				sources.beforeFirst();
				boolean odd = true;
				BigDecimal total = BigDecimal.ZERO;
				while ( sources.next() ) {
					try {
						myCell = new TableCell(sources.getID());
						if ( aStrain == null || (! aStrain.getID().equals(sources.getCultureID())) ) 
								aStrain = new SQLStrain(myData, sources.getCultureID());
						myCell.addItem(this.strainLink(aStrain));
						myCell.addItem(myDateFormat.format(sources.getDate()));
	//					if ( aCol == null || (! aCol.getID().equals(sources.getCollectionID())) )
	//						aCol = sources.getCollection();
	//					myCell.addItem(this.sampleColLink(aCol));
	//					myCell.addItem(sources.getLocation());
	//					BigDecimal concentration = sources.getConcentration();
	//					if ( concentration == null || concentration.equals(BigDecimal.ZERO) ) concentration = BigDecimal.ONE;
	//					BigDecimal amount = sources.getAmountForSeparation(aSep).multiply(concentration);
						BigDecimal amount = sources.getAmountForSeparation(aSep);
						if ( amount.signum() < 0) amount = amount.negate();
						total = total.add(amount);
						myCell.addItem(SQLSeparation.autoFormatAmount(amount, SQLSeparation.MASS_TYPE));
						myRow = new TableRow(myCell);
						myRow.setAttribute("ALIGN", "CENTER");
						if ( sources.isRemoved() )
							myCell.setClass("removed");						
						if ( odd ) {
							myRow.setClass("odd");
							odd = false;
						} else {
							myRow.setClass("even");
							odd = true;
						}
						myTable.addItem(myRow);
					} catch (DataException eRow) {
						myRow.addItem(String.format("<TD COLSPAN=6>%s</TD>", this.handleException(eRow)));
					}
				}
				String unit = "mg";
				if ( total.compareTo(BigDecimal.ONE) > -1 ) unit = "g";
				myTable.addItem("<TR><TD COLSPAN=5 ALIGN='right'><B>TOTAL</B></TD><TD ALIGN='CENTER'><B>" + 
						formatAmount(total, unit) + "</B></TD></TR>");
			} else {
				myTable = new Table("<TR><TD ALIGN='center'><B>NONE</B></TD></TR>");
			}
		} catch (DataException e) {
			myTable.addItem(String.format("<TR><TD COLSPAN=6>%s</TD></TR>", this.handleException(e)));
		}
		myTable.setAttribute("ALIGN","CENTER");
		myTable.setAttribute("WIDTH", "80%");
		return myTable.toString();
	}


	public Div sourceDiv(Separation aSep) {
		String content = "CANNOT GENERATE OUTPUT";
		try {
			Material sources = aSep.getSources();
			content = this.sampleTable(aSep, sources);
		} catch (DataException e) {
			content = this.handleException(e);
		}
		return this.openDiv("sep_source", "Source Samples", content);
	}
	
	public Div fractionDiv(Separation aSep) {
		String content = "CANNOT GENERATE OUTPUT";
		try {
			Material sources = aSep.getFractions();
			content = this.sampleTable(aSep, sources);
		} catch (DataException e) {
			content = this.handleException(e);
		}
		return this.openDiv("sep_product", "Fractions", content);
	}

	public String protocolForm(SeparationTemplate myProtocol) {
		Table myTable = new Table();
		myTable.setAttribute("ALIGN","CENTER");
		TableRow myRow = new TableRow(this.makeFormTextRow("Stationary Phase:", "sphase", myProtocol.getStationaryPhase()));
		myRow.addItem(this.makeFormTextRow("Mobile Phase:", "mphase", myProtocol.getMobilePhase()));
		myRow.addItem(this.makeFormTextAreaRow("Method:", "method", myProtocol.getMethod()));
		myTable.addItem(myRow);
		return myTable.toString();
	}
	
	/*
	public String dataForm(Separation anObject) {
		if ( anObject.isAllowed(Role.WRITE) && this.hasFormValue("showBrowser") ) {
			Form myForm = new Form(DataForm.fileManagerApplet(this.myWrapper, "separation", anObject.getID(), Separation.LC_DATA_TYPE, false));				
			myForm.addItem(String.format("<P ALIGN='CENTER'><BUTTON TYPE='BUTTON' onClick=\"updateForm(this,'%s')\" NAME='cancelBrowser'>Close</BUTTON>", DATA_FORM));
			myForm.setAttribute("NAME", "dataBrowser");
			myForm.addHiddenValue("id", anObject.getID());
			myForm.addHiddenValue("div", DATA_FORM);
			return myForm.toString();
		} else {
			StringBuffer output = new StringBuffer();
			DataForm dataForm = new DataForm(this.myWrapper);
			dataForm.setTypeLabel(Separation.LC_DATA_TYPE, "LC Chromatogram");
			output.append(dataForm.datafileTable(anObject));
			if ( anObject.isAllowed(Role.WRITE) )
				output.append(String.format("<FORM><P ALIGN='CENTER'><INPUT TYPE=HIDDEN NAME='id' VALUE='%s'/><BUTTON TYPE='BUTTON' NAME='showBrowser' onClick=\"loadForm(this, '%s')\">Manage Data Files</BUTTON></P></FORM>", anObject.getID(), DATA_FORM));
			return output.toString();
		}
	}


	public String compoundForm(Separation anObject) {
		CompoundForm aForm = new CompoundForm(this.myWrapper);
		if ( anObject.isAllowed(Role.WRITE) && this.hasFormValue("showCmpdForm") ) {	
			try {
				Form myForm = new Form("<P ALIGN='CENTER'>Compound ID:");				
				myForm.setAttribute("NAME", "compoundForm");
				myForm.addHiddenValue("id", anObject.getID());
				myForm.addHiddenValue("div", COMPOUND_FORM_ID);
				Compound compoundList = SQLCompound.compounds(this.getSQLDataSource(), SQLCompound.ID_COLUMN, SQLCompound.ASCENDING_SORT);
				myForm.addItem(aForm.linkCompoundMenu("cmpdID", "newCmpdID", compoundList)); 
				myForm.addItem("</BR>Retention time: <INPUT TYPE='TEXT' NAME='retTime' SIZE=10/> min<BR/></P>");
				myForm.addItem("<H3>Samples to link:</H3>");
				myForm.addItem(this.sampleList(anObject));
				myForm.addItem(String.format("</P><P ALIGN='CENTER'><BUTTON TYPE='BUTTON' onClick=\"updateForm(this,'%s')\" NAME='linkCompound'>Link Compound</BUTTON>", COMPOUND_FORM_ID));
				myForm.addItem(String.format("<BUTTON TYPE='BUTTON' onClick=\"updateForm(this,'%s')\" NAME='closeForm'>Close</BUTTON></P>", COMPOUND_FORM_ID));
				return myForm.toString();
			} catch (DataException e) {
				e.printStackTrace();
				return (this.handleException(e));
			}
		} 
		StringBuffer output = new StringBuffer();
				
		if ( anObject.isAllowed(Role.WRITE) && this.hasFormValue("linkCompound") ) {
			String[] samples = this.getFormValues("sample");
			String retTime = this.getFormValue("retTime");
			String cmpdID = this.getFormValue("cmpdID");
			
			if ( cmpdID.length() < 1 ) {
				cmpdID = this.getFormValue("newCmpdID");
			}

			output.append(String.format("<P><B>Linking compound %s </P>", cmpdID));
			
			for ( int i = 0; i < samples.length; i++ ) {
				try {
					output.append(String.format("For sample ID: %s...", samples[i]));
					anObject.addCompoundID(cmpdID, samples[i], retTime);
					output.append("<FONT COLOR='green'><B>SUCCESS</B></FONT><BR>");
				} catch (DataException e) {
					output.append("<FONT COLOR='red'><B>ERROR</B></FONT> ");
					output.append(e.getLocalizedMessage());
					output.append("<BR>");
					e.printStackTrace();
				}
			}
		} 

		output.append(aForm.listCompounds(anObject));
		if ( anObject.isAllowed(Role.WRITE) )
			output.append(String.format("<FORM><P ALIGN='CENTER'><INPUT TYPE=HIDDEN NAME='id' VALUE='%s'/><BUTTON TYPE='BUTTON' NAME='showCmpdForm' onClick=\"loadForm(this, '%s')\">Link a new compound</BUTTON></P></FORM>", anObject.getID(), COMPOUND_FORM_ID));
		return output.toString();
	}
	

	public String sampleList(Separation aSep) {
		String[] headers = {"", "Sample", "Culture ID", "Date", "Collection", "Location"};
		TableCell myCell = new TableHeader(headers);
		myCell.setClass("header");
		TableRow myRow = new TableRow(myCell);
		Table myTable = new Table(myRow);
		myTable.setClass("list");

		try {
			myTable.addItem("<TR><TH COLSPAN=6>Source Materials</TH></TR>");
			myTable.addItem(this.loadTable(aSep.getSources(), true));
			myTable.addItem("<TR><TH COLSPAN=6>Fractions</TH></TR>");
			myTable.addItem(this.loadTable(aSep.getFractions(), false));
		} catch (DataException e) {
			myRow.addItem(String.format("<TD COLSPAN=6>%s</TD>", this.handleException(e)));
		}

		myTable.setAttribute("ALIGN","CENTER");
		myTable.setAttribute("WIDTH", "80%");
		return myTable.toString();
	}


	private String loadTable(Material material, boolean select) {
		StringBuffer output = new StringBuffer();
		try {
			SQLData myData = this.myWrapper.getSQLDataSource();
			Strain aStrain = null;
//			SampleCollection aCol = null;
			TableRow myRow = null;

			if ( material != null && material.first() ) {
				SimpleDateFormat myDateFormat = this.dateFormat();
				material.beforeFirst();
				boolean odd = true;
				while ( material.next() ) {
					try {
						TableCell myCell = new TableCell(String.format("<INPUT TYPE='CHECKBOX' NAME='sample' VALUE='%s' %s>", material.getID(), (select ? "CHECKED" : "")));
						myCell.addItem(material.getID());
						if ( aStrain == null || (! aStrain.getID().equals(material.getCultureID())) ) 
							aStrain = new SQLStrain(myData, material.getCultureID());
						myCell.addItem(this.strainLink(aStrain));
						myCell.addItem(myDateFormat.format(material.getDate()));
//						if ( aCol == null || (! aCol.getID().equals(material.getCollectionID())) )
//							aCol = material.getCollection();
//						myCell.addItem(this.sampleColLink(aCol));
//						myCell.addItem(material.getLocation());
						myRow = new TableRow(myCell);
						myRow.setAttribute("ALIGN", "CENTER");
						if ( material.isRemoved() )
							myCell.setClass("removed");						
						if ( odd ) {
							myRow.setClass("odd");
							odd = false;
						} else {
							myRow.setClass("even");
							odd = true;
						}
						output.append(myRow);
					} catch (DataException eRow) {
						myRow.addItem(String.format("<TD COLSPAN=6>%s</TD>", this.handleException(eRow)));
					}
				}
			} else {
				output.append("<TR><TD ALIGN='center' COLSPAN=6><B>NONE</B></TD></TR>");
			}
		} catch (DataException e) {
			output.append(String.format("<TR><TD COLSPAN=6>%s</TD></TR>", this.handleException(e)));
		}
		return output.toString();
	}
	*/
}
