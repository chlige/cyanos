/**
 * 
 */
package edu.uic.orjala.cyanos.web.forms;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.uic.orjala.cyanos.Assay;
import edu.uic.orjala.cyanos.AssayData;
import edu.uic.orjala.cyanos.AssayPlate;
import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.Project;
import edu.uic.orjala.cyanos.Role;
import edu.uic.orjala.cyanos.Sample;
import edu.uic.orjala.cyanos.SampleCollection;
import edu.uic.orjala.cyanos.Strain;
import edu.uic.orjala.cyanos.sql.SQLAssay;
import edu.uic.orjala.cyanos.sql.SQLAssayData;
import edu.uic.orjala.cyanos.sql.SQLData;
import edu.uic.orjala.cyanos.sql.SQLSampleCollection;
import edu.uic.orjala.cyanos.web.BaseForm;
import edu.uic.orjala.cyanos.web.CyanosWrapper;
import edu.uic.orjala.cyanos.web.html.Div;
import edu.uic.orjala.cyanos.web.html.Form;
import edu.uic.orjala.cyanos.web.html.Image;
import edu.uic.orjala.cyanos.web.html.Paragraph;
import edu.uic.orjala.cyanos.web.html.Popup;
import edu.uic.orjala.cyanos.web.html.Table;
import edu.uic.orjala.cyanos.web.html.TableCell;
import edu.uic.orjala.cyanos.web.html.TableHeader;
import edu.uic.orjala.cyanos.web.html.TableRow;

/**
 * @author George Chlipala
 *
 */
public class AssayForm extends BaseForm {

	public static final String ASSAY_PROTOCOL = "assay protocol";
	public static final String DIV_TITLE = "Assay Data";
	public static final String DIV_ID = "assayInfo";
	public static final String DATA_FORM = "dataForm";
	
	public static String[] PROTOCOL_KEYS = {"trgt", "active_op", "active", "unit", "size", "project"};


	/**
	 * @param callingServlet
	 */
	public AssayForm(CyanosWrapper callingServlet) {
		super(callingServlet);
	}

	public String assayListForStrain(Strain aStrain) {
		String[] headerData = {"Sample", "Assay", "Date", "Target", "Activity", "Concentration"};
		TableCell header = new TableHeader(headerData);

		header.setAttribute("class","header");
		TableRow tableRow = new TableRow(header);
		Table myTable = new Table(tableRow);
		myTable.setAttribute("class","dashboard");
		myTable.setAttribute("align","center");
		myTable.setAttribute("width","75%");

		try {
			Assay myAssays = aStrain.getAssays();
			if ( myAssays != null ) {
				myAssays.beforeFirst();
				String curClass = "odd";
				SimpleDateFormat myDate = this.dateFormat();

				while ( myAssays.next() ) {
					AssayData myData = myAssays.getAssayDataForStrain(aStrain);
					myData.beforeFirst();
					while ( myData.next() ) {
						TableCell myCell = new TableCell();
						Sample aSample = myData.getSample();
						if ( aSample != null ) {
							myCell.addItem("<A HREF='sample?id=" + aSample.getID() + "'>" + aSample.getName() + "</A>");
						} else {
							myCell.addItem("NONE");
						}
						myCell.addItem("<A HREF='assay?id=" + myAssays.getID() + "'>" + myAssays.getName() + "</A>");
						myCell.addItem(myDate.format(myAssays.getDate()));
						myCell.addItem(myAssays.getTarget());

						String activity = myData.getActivityString();
						if (activity == null) myCell.addItem("");
						else myCell.addItem(activity);
						
						BigDecimal concentration = myData.getConcentration();
						if ( concentration.compareTo(BigDecimal.ZERO) == 0 ) myCell.addItem("-");
						else myCell.addItem(SQLAssayData.autoFormatAmount(concentration, SQLAssayData.CONCENTRATION_TYPE));
						
						TableRow aRow = new TableRow(myCell);
						if ( myData.isActive() ) {
							aRow.setClass("danger" + curClass);
						} else {
							aRow.setClass(curClass);				
						}			
						aRow.setAttribute("align", "center");
						myTable.addItem(aRow);

						if ( curClass.equals("odd") ) {
							curClass = "even";
						} else {
							curClass = "odd";
						}
					}
				}	
			}
		} catch (DataException e) {
			myTable.addItem("<TR ALIGN='CENTER'><TD COLSPAN='5'>");
			myTable.addItem(this.handleException(e));
			myTable.addItem("</TD></TR>");
		}

		return myTable.toString();
	}
	
	/*
	public String assayListForSample(Sample aSample) {
		StringBuffer output = new StringBuffer();

		String[] headerData = {"Assay", "Date", "Target", "Activity", "Concentration" };
		TableCell header = new TableHeader(headerData);

		header.setAttribute("class","header");
		TableRow tableRow = new TableRow(header);
		Table myTable = new Table(tableRow);
		myTable.setAttribute("class","dashboard");
		myTable.setAttribute("align","center");
		myTable.setAttribute("width","75%");

		try {
			Assay myAssays = aSample.getAssays();
			if ( myAssays != null ) {
				myAssays.beforeFirst();
				String curClass = "odd";
				SimpleDateFormat myDate = this.dateFormat();

				while (myAssays.next()) {
					AssayData myData = myAssays.getAssayDataForSample(aSample);
					myData.beforeFirst();
					while ( myData.next() ) {
						TableCell myCell = new TableCell();
						myCell.addItem("<A HREF='assay?id=" + myAssays.getID() + "'>" + myAssays.getName() + "</A>");
						myCell.addItem(myDate.format(myAssays.getDate()));
						myCell.addItem(myAssays.getTarget());

						String activity = myData.getActivityString();
						if (activity == null) myCell.addItem("");
						else myCell.addItem(activity);
						
						float concentration = myData.getConcentration();
						if ( concentration == 0 ) myCell.addItem("-");
						else if ( concentration >= 1 ) myCell.addItem(BaseForm.formatAmount("%.0f %s", concentration, "mg/ml"));
						else myCell.addItem(BaseForm.formatAmount("%.0f %s", concentration, "ug/ml"));
						
						TableRow aRow = new TableRow(myCell);
						if ( myData.isActive() ) {
							aRow.setClass("danger" + curClass);
						} else {
							aRow.setClass(curClass);				
						}			
						aRow.setAttribute("align", "center");
						myTable.addItem(aRow);

						if ( curClass.equals("odd") ) {
							curClass = "even";
						} else {
							curClass = "odd";
						}
					}
				}	
			}
		} catch (DataException e) {
			e.printStackTrace();
			myTable.addItem("<TR ALIGN='CENTER'><TD COLSPAN='5'>");
			myTable.addItem(this.handleException(e));
			myTable.addItem("</TD></TR>");
		}

		output.append(myTable.toString());

		return output.toString();
	}
*/
	public Div assayDiv() {
		return this.loadableDiv(DIV_ID, DIV_TITLE);
	}
	
	public Div assayDiv(Strain aStrain) {
		return this.collapsableDiv(DIV_ID, DIV_TITLE, this.assayListForStrain(aStrain));
	}
	
	public Div ajaxDiv(Strain aStrain) {
		return this.ajaxDiv(DIV_ID, this.assayListForStrain(aStrain));
	}
	
	/*
	public Div assayDiv(Sample aSample) {
		return this.collapsableDiv(DIV_ID, DIV_TITLE, this.assayListForSample(aSample));
	}
	*/
	
	public String showPlateGraphic(Assay myAssay) {
		try {
			TableCell boxHeader = new TableHeader("");
			for ( int i = 1; i <= myAssay.getWidth(); i++ ) {
				boxHeader.addItem(Integer.toString(i));
			}

			TableRow row = new TableRow(boxHeader);
			AssayPlate myData = myAssay.getAssayData();
			myData.beforeFirstRow();
			while ( myData.nextRow() ) {
				myData.beforeFirstColumn();
				TableCell thisRow = new TableCell();
				thisRow.setAttribute("ALIGN", "CENTER");
				thisRow.setAttribute("VALIGN", "TOP");
				while ( myData.nextColumn() ) {
					Image image = this.getImage("empty-flat.png");
					if ( myData.currentLocationExists() ) {
						String href = null;
						String sampleID = myData.getSampleID();
						String cultureID = myData.getStrainID();
						if ( sampleID != null ) {
							href = String.format("sample?id=%s", sampleID);
						} else if ( cultureID != null ){
							href = String.format("strain?id=%s", cultureID);
						}
							if ( myData.getActivityString() != null ) {
							if ( myData.isActive() )
								image = this.getImage("active.png");
							else 
								image = this.getImage("filled.png");
						} else {
							image = this.getImage("empty.png");						
						}
						image.setAttribute("BORDER", "0");
						if ( href != null )
							thisRow.addItem(String.format("<A HREF='%s'>%s</A>", href, image.toString()));
						else 
							thisRow.addItem(image);
					} else {
						image.setAttribute("BORDER", "0");
						thisRow.addItem(image);
					}
				}					
				row.addItem(String.format("<TH WIDTH=3>%s</TH>%s", myData.currentRowAlpha(), thisRow.toString()));
			}
			Table myTable = new Table(row);
			TableRow legendRow = new TableRow("<TH COLSPAN=6>Key</TH>");
			Table legend = new Table(legendRow);
			TableCell myCell = new TableCell(String.format("<IMG SRC='%s'>Active", this.getImagePath("active.png")));
			myCell.addItem(String.format("<IMG SRC='%s'>Inactive", this.getImagePath("filled.png")));
			myCell.addItem(String.format("<IMG SRC='%s'>No Data", this.getImagePath("empty.png")));
			myCell.addItem(String.format("<IMG SRC='%s'>No Sample", this.getImagePath("empty-flat.png")));
			legendRow.addItem(myCell);
			return myTable.toString() + legend.toString();
		} catch ( DataException e ) {
			e.printStackTrace();
			return ("<P ALIGN='CENTER'><B><FONT COLOR='red'>ERROR:</FONT> " + e.getMessage() + "</B></P>");
		}
	}

	public String showPlateData(Assay myAssay) {
		try {
			TableCell boxHeader = new TableHeader("");
			for ( int i = 1; i <= myAssay.getWidth(); i++ ) {
				boxHeader.addItem(Integer.toString(i));
			}

			TableRow row = new TableRow(boxHeader);
			AssayData myData = myAssay.getAssayData();
			myData.beforeFirst();
			while ( myData.next() ) {
				TableCell thisRow = new TableCell();
				thisRow.setAttribute("ALIGN", "CENTER");
				thisRow.setAttribute("VALIGN", "TOP");
				String activity = myData.getActivityString();
				if ( activity != null ) {
					if ( myData.isActive() )
						thisRow.addItem("<B>" + activity + "</B>");
					else 
						thisRow.addItem(activity);
				} else
					thisRow.addItem("");											
				row.addItem(String.format("<TH WIDTH=3>%s</TH>%s", myData.getLocation(), thisRow.toString()));
			}
			Table myTable = new Table(row);
			return myTable.toString();
		} catch ( DataException e ) {
			e.printStackTrace();
			return ("<P ALIGN='CENTER'><B><FONT COLOR='red'>ERROR:</FONT> " + e.getMessage() + "</B></P>");
		}
	}

	public String showPlateSamples(Assay myAssay) {
		try {
			TableCell boxHeader = new TableHeader("");
			for ( int i = 1; i <= myAssay.getWidth(); i++ ) {
				boxHeader.addItem(Integer.toString(i));
			}

			TableRow row = new TableRow(boxHeader);
			AssayData myData = myAssay.getAssayData();
			myData.beforeFirst();
			while ( myData.next() ) {
				TableCell thisRow = new TableCell();
				thisRow.setAttribute("ALIGN", "CENTER");
				thisRow.setAttribute("VALIGN", "TOP");
				Sample aSample = myData.getSample();
				if ( aSample != null )
					thisRow.addItem(String.format("<A HREF='sample?id=%s'>%s</A>", aSample.getID(), aSample.getName()));
				else
					thisRow.addItem(myData.getLabel());												
				row.addItem(String.format("<TH WIDTH=3>%s</TH>%s", myData.getLocation(), thisRow.toString()));
			}
			Table myTable = new Table(row);
			return myTable.toString();
		} catch ( DataException e ) {
			e.printStackTrace();
			return ("<P ALIGN='CENTER'><B><FONT COLOR='red'>ERROR:</FONT> " + e.getMessage() + "</B></P>");
		}
	}
	
	private String updateAssay(Assay myAssay) {
		try {
			myAssay.setManualRefresh();
			myAssay.setName(this.getFormValue("assayName"));
			myAssay.setDate(this.getFormValue("date"));
			myAssay.setProjectID(this.getFormValue("project"));
			String assayTarget = this.getFormValue("assayTarget");
			if ( assayTarget != null && assayTarget.length() > 0 ) {
				myAssay.setTarget(this.getFormValue("assayTarget"));				
			} else if ( this.hasFormValue("newTarget") ) {
				myAssay.setTarget(this.getFormValue("newTarget"));
			}
			myAssay.setActiveOperator(this.getFormValue("active_op"));
			myAssay.setActiveLevel(this.getFormValue("active"));
			myAssay.setUnit(this.getFormValue("unit"));
			
			Pattern sizePat = Pattern.compile("(\\d+)x(\\d+)");
			Matcher match = sizePat.matcher(this.getFormValue("size"));
			if ( match.matches() ) {
				myAssay.setLength(Integer.parseInt(match.group(1)));
				myAssay.setWidth(Integer.parseInt(match.group(2)));				
			}
			myAssay.setNotes(this.getFormValue("notes"));
			myAssay.refresh();
			return this.message(SUCCESS_TAG, "Updated Assay");
		} catch (DataException e) {
			return this.handleException(e);
		}
	}
	
	private String assayForm(Assay myAssay) {
		StringBuffer output = new StringBuffer();
		try {
			TableCell myCell = new TableCell("ID:");
			myCell.addItem(myAssay.getID());
			TableRow tableRow = new TableRow(myCell);

			tableRow.addItem(this.makeFormTextRow("Assay Name:", "assayName", myAssay.getName()));
			tableRow.addItem(this.makeFormDateRow("Assay Date:", "date", "assay_info", myAssay.getDateString()));
			myCell = new TableCell("Project:");
			Popup aPop;
			try {
				aPop = this.projectPopup();
				aPop.setName("project");
				String projectID = myAssay.getProjectID();
				if ( projectID != null )
					aPop.setDefault(projectID);
				myCell.addItem(aPop.toString());
				tableRow.addItem(myCell);	
			} catch (DataException e) {
				myCell.addItem("<B><FONT COLOR='red'>ERROR:</FONT> " + e.getMessage() + "</B>"); 
			}

			myCell = new TableCell("Assay Target:");
			StringBuffer libData = new StringBuffer();
			try {
				Popup libPop = this.targets();
				libPop.setName("assayTarget");
				libPop.setDefault(myAssay.getTarget());
				libData.append(libPop.toString());
			} catch (DataException e) {
				libData.append("<FONT COLOR='red'><B>Cannot load targets</FONT><BR/>" + e.getMessage() + "</B>");
			}
			libData.append("<INPUT TYPE='TEXT' NAME='newTarget' SIZE='25'/>");		
			myCell.addItem(libData.toString());
			tableRow.addItem(myCell);

			myCell = new TableCell("Active Level:");
			aPop = this.activeOperators();
			aPop.setName("active_op");
			aPop.setDefault(myAssay.getActiveOperator());
			myCell.addItem(aPop.toString() + "<INPUT TYPE=TEXT NAME='active' SIZE=10 VALUE='" + String.valueOf(myAssay.getActiveLevel()) + "'/>");
			tableRow.addItem(myCell);

			myCell = new TableCell("Unit Format:");
			aPop = this.unitFormats();
			aPop.setName("unit");
			aPop.setDefault(myAssay.getUnit());
			myCell.addItem(aPop.toString());
			tableRow.addItem(myCell);

			myCell = new TableCell("Assay Size:");
			aPop = this.sizes();
			aPop.setName("size");
			aPop.setDefault(String.format("%dx%d", myAssay.getLength(), myAssay.getWidth()));
			myCell.addItem(aPop.toString());
			tableRow.addItem(myCell);
			
			myCell = new TableCell("Notes:");
			myCell.addItem("<TEXTAREA NAME='notes' COLS='40' ROWS='4'>" + myAssay.getNotes() + "</TEXTAREA>");
			tableRow.addItem(myCell);

			myCell = new TableCell("<INPUT TYPE=SUBMIT NAME='updateAssay' VALUE='Update'/><INPUT TYPE=RESET />");
			myCell.setAttribute("colspan","2");
			myCell.setAttribute("align","center");
			tableRow.addItem(myCell);

			Table myTable = new Table(tableRow);
			myTable.setClass("species");
			myTable.setAttribute("align", "center");
			Form myForm = new Form(myTable);
			myForm.setAttribute("METHOD", "POST");
			myForm.addItem(String.format("<INPUT TYPE='HIDDEN' NAME='id' VALUE='%s'/>", myAssay.getID()));
			myForm.setName("assay_info");
			output.append(myForm.toString());
		} catch ( DataException e ) {
			output.append("<P ALIGN='CENTER'><B><FONT COLOR='red'>ERROR:</FONT> " + e.getMessage() + "</B></P>");
		}
		return output.toString();
	}

	private String assayText(Assay myAssay) {
		StringBuffer output = new StringBuffer();
		try {
			TableCell myCell = new TableCell("ID:");
			myCell.addItem(String.format("%s <A HREF=\"assay/export?id=%s\">Export assay data</A>", myAssay.getID(), myAssay.getID()));
			TableRow tableRow = new TableRow(myCell);

			myCell = new TableCell("Assay Name:");
			myCell.addItem(myAssay.getName());
			tableRow.addItem(myCell);
			
			myCell = new TableCell("Assay Date:");
			SimpleDateFormat myFormat = this.myWrapper.dateFormat();
			myCell.addItem(myFormat.format(myAssay.getDate()));
			tableRow.addItem(myCell);

			myCell = new TableCell("Project:");
			Project aProject = myAssay.getProject();
			if ( aProject != null ) {
				myCell.addItem(String.format("<A HREF='%s/project?id=%s'>%s</A>", this.myWrapper.getContextPath(), aProject.getID(), aProject.getName()));
			} else {
				myCell.addItem("NONE");
			}
			tableRow.addItem(myCell);
			
			myCell = new TableCell("Assay Target:");	
			myCell.addItem(myAssay.getTarget());
			tableRow.addItem(myCell);
			tableRow.addItem(String.format("<TD></TD><TD><A HREF=\"assay?trgt=%s\">Browse assays for this target</A></TD>", myAssay.getTarget()));
			tableRow.addItem(String.format("<TD></TD><TD><A HREF=\"assay/hits?id=%s\">View hit list for this target</A></TD>",myAssay.getID()));
			
			
			myCell = new TableCell("Active Level:");
			Map<String,String> opMap = operatorMap();			
			myCell.addItem(opMap.get(myAssay.getActiveOperator()) + " " + String.format(myAssay.getUnit(), myAssay.getActiveLevel()));
			tableRow.addItem(myCell);

			myCell = new TableCell("Unit Format:");
			myCell.addItem(myAssay.getUnit());
			tableRow.addItem(myCell);

			myCell = new TableCell("Assay Size:");
			myCell.addItem(String.format("%dx%d", myAssay.getLength(), myAssay.getWidth()));
			tableRow.addItem(myCell);
			
			myCell = new TableCell("Notes:");
			String notes = myAssay.getNotes();
			if ( notes != null )
				myCell.addItem(notes.replaceAll("[\n\r]", "<BR/>"));
			else 
				myCell.addItem("");				
			myCell.setAttribute("VALIGN", "TOP");
			tableRow.addItem(myCell);

			Table myTable = new Table(tableRow);
			myTable.setClass("species");
			myTable.setAttribute("align", "center");
			output.append(myTable.toString());
		} catch ( DataException e ) {
			output.append(this.handleException(e));
		}
		return output.toString();
	}

	public static Map<String,String> operatorMap() {
		Map<String,String> aMap = new HashMap<String,String>();
		aMap.put(Assay.OPERATOR_EQUAL,"=");
		aMap.put(Assay.OPERATOR_NOT_EQUAL,"!=");
		aMap.put(Assay.OPERATOR_GREATER_THAN,"&gt;");
		aMap.put(Assay.OPERATOR_GREATER_EQUAL,"&gt;=");
		aMap.put(Assay.OPERATOR_LESS_THAN,"&lt;");
		aMap.put(Assay.OPERATOR_LESS_EQUAL,"&lt;=");
		return aMap;
	}

	public String assayInfo(Assay anAssay) throws DataException {
		Div sampleDiv = new Div();
		
		if ( anAssay.isAllowed(Role.WRITE) ) {
			if ( this.hasFormValue("updateAssay")) {
				sampleDiv.addItem(this.updateAssay(anAssay));
			}
			Div viewDiv = new Div(this.assayText(anAssay));
			viewDiv.setID("view_assayinfo");
			viewDiv.setClass("showSection");
			viewDiv.addItem("<P ALIGN='CENTER'><BUTTON TYPE='BUTTON' onClick='flipDiv(\"assayinfo\")'>Edit Values</BUTTON></P>");
			sampleDiv.addItem(viewDiv);
			
			Div editDiv = new Div(this.assayForm(anAssay));
			editDiv.setID("edit_assayinfo");
			editDiv.setClass("hideSection");		
			editDiv.addItem("<P ALIGN='CENTER'><BUTTON TYPE='BUTTON' onClick='flipDiv(\"assayinfo\")'>Close Form</BUTTON></P>");
			sampleDiv.addItem(editDiv);
		} else {
			Div viewDiv = new Div(this.assayText(anAssay));
			viewDiv.setID("view_assayinfo");
			viewDiv.setClass("showSection");
			sampleDiv.addItem(viewDiv);			
		}

		return sampleDiv.toString();	
	}
	
	public String addForm() throws SQLException {
		Div formDiv = ProtocolForm.formDiv("Load an Assay Template", null);
		Form myForm = new Form(formDiv);
		
		Map<String,String> myProtocol = new HashMap<String,String>();
		
		TableRow tableRow = new TableRow(this.makeFormTextRow("Assay ID:", "id"));
		tableRow.addItem(this.makeFormTextRow("Assay Name:", "assayName"));
		tableRow.addItem(this.makeFormDateRow("Assay Date:", "assayDate", "assay_info"));

		TableCell myCell = new TableCell("Project Code:");
		Popup aPop = null;
		
		try {
			aPop = this.projectPopup();
			aPop.setName("project");
			myCell.addItem(aPop.toString());
			tableRow.addItem(myCell);		
		} catch (DataException e) {
			myCell.addItem("<B><FONT COLOR='red'>ERROR:</FONT> " + e.getMessage() + "</B>"); 
		}
		
		myCell = new TableCell("Assay Target:");
		StringBuffer libData = new StringBuffer();
		try {
			Popup libPop = this.targets("trgt");
			if ( myProtocol.containsKey("trgt")) libPop.setDefault((String)myProtocol.get("trgt"));
			libData.append(libPop.toString());
		} catch (DataException e) {
			libData.append("<FONT COLOR='red'><B>Cannot load targets</FONT><BR/>" + e.getMessage() + "</B>");
		}
		libData.append("<INPUT TYPE='TEXT' NAME='newTarget' SIZE='25'/>");		
		myCell.addItem(libData.toString());
		tableRow.addItem(myCell);


		myCell = new TableCell("Active Level:");
		aPop = this.activeOperators("active_op");
		if ( myProtocol.containsKey("active_op"))
			aPop.setDefault((String)myProtocol.get("active_op"));

		String active = "";
		if ( myProtocol.containsKey("active"))
			active = (String)myProtocol.get("active");
		else if ( this.hasFormValue("active") ) 
			active = this.getFormValue("active");
		
		myCell.addItem(aPop.toString() + "<INPUT TYPE=TEXT NAME='active' SIZE=10 VALUE=\"" + active + "\"/>");
		tableRow.addItem(myCell);

		myCell = new TableCell("Unit Format:");
		aPop = this.unitFormats("unit");
		if ( myProtocol.containsKey("unit"))
			aPop.setDefault((String)myProtocol.get("unit"));
		myCell.addItem(aPop);
		tableRow.addItem(myCell);

		myCell = new TableCell("Assay Size:");
		aPop = this.sizes();
		aPop.setName("size");
		String currSize = "8x12";
		if ( myProtocol.containsKey("size"))
			currSize = myProtocol.get("size");
		else if ( this.hasFormValue("size") ) 
			currSize = this.getFormValue("8x12");
		aPop.setDefault(currSize);
		
		aPop.setAttribute("onChange", "this.form.submit()");
		myCell.addItem(aPop.toString());
		tableRow.addItem(myCell);

		try {
			Popup collectionPopup = this.sampleCollectionPopup();
			
			if ( currSize.equals("16x24") ) {
				myCell = new TableCell("<B>Copy from Source</B>");
				myCell.setAttribute("COLSPAN", "3");
				myCell.setAttribute("ALIGN", "CENTER");
				TableRow aRow = new TableRow(myCell);
				String fullCheck = "", intCheck = "CHECKED";
				if ( this.hasFormValue("loadType") && this.getFormValue("loadType").equals("full")) {
					fullCheck = "CHECKED"; intCheck = "";
				}
				myCell = new TableCell(String.format("%s<BR/><INPUT TYPE='RADIO' NAME='loadType' VALUE='interlace' %s/>Interlaced", 
						this.getImage("bywell.png").toString(), intCheck));
				myCell.addItem(String.format("%s<BR/><INPUT TYPE='RADIO' NAME='loadType' VALUE='full' %s/>Aggregated", 
						this.getImage("byplate.png").toString(), fullCheck));	

				String[] colors = { "#FFFF90", "#90FF90", "cyan", "#FF9090" };				
				Table sourceTable = new Table();
				
				for ( int i = 0; i < colors.length; i++ ) {
					String formValue = String.format("source%d", i + 1);
					if ( this.hasFormValue(formValue) ) 
						collectionPopup.setDefault(this.getFormValue(formValue));
					collectionPopup.setName(formValue);
					sourceTable.addItem(String.format("<TR><TD BGCOLOR='%s'>Plate %d: %s</TD></TR>", colors[i], i + 1, collectionPopup.toString() ));
				}
				myCell.addItem(sourceTable);
				myCell.setAttribute("ALIGN","CENTER");
				aRow.addItem(myCell);
				Table loadTable = new Table(aRow);
				tableRow.addItem("<TD COLSPAN='2'>" + loadTable.toString() + "</TD>");
			} else {
				myCell = new TableCell("Copy from Source:");
				collectionPopup.setName("source");
				if ( this.hasFormValue("source") ) {
					collectionPopup.setDefault("source");
				}
				myCell.addItem(collectionPopup);
				tableRow.addItem(myCell);
			}
		} catch (DataException e) {
			myCell = new TableCell("Copy from Source:");
			myCell.addItem(this.handleException(e));
			tableRow.addItem(myCell);
		}
		tableRow.addItem(this.makeFormTextAreaRow("Notes:", "notes"));

		myCell = new TableCell("<INPUT TYPE=SUBMIT NAME='addAssay' VALUE='Add'/><INPUT TYPE=RESET />");
		myCell.setAttribute("colspan","2");
		myCell.setAttribute("align","center");
		tableRow.addItem(myCell);

		Table myTable = new Table(tableRow);
		myTable.setClass("species");
		myTable.setAttribute("align", "center");
		
		myForm.addItem(myTable);
		myForm.setAttribute("METHOD", "POST");
		myForm.setName("assay_info");
		
		return myForm.toString();
	}

	private Popup activeOperators(String name) {
		Popup aPop = this.activeOperators();
		aPop.setName(name);
		if ( this.hasFormValue(name))
			aPop.setDefault(this.getFormValue(name));
		else
			aPop.setDefault(Assay.OPERATOR_GREATER_EQUAL);
		return aPop;
	}
	
	private Popup activeOperators() {
		Popup aPop = new Popup();
		aPop.addItemWithLabel(Assay.OPERATOR_EQUAL,"=");
		aPop.addItemWithLabel(Assay.OPERATOR_NOT_EQUAL,"!=");
		aPop.addItemWithLabel(Assay.OPERATOR_GREATER_THAN,"&gt;");
		aPop.addItemWithLabel(Assay.OPERATOR_GREATER_EQUAL,"&gt;=");
		aPop.addItemWithLabel(Assay.OPERATOR_LESS_THAN,"&lt;");
		aPop.addItemWithLabel(Assay.OPERATOR_LESS_EQUAL,"&lt;=");
		return aPop;
	}
	
	private Popup targets() throws DataException {
		Popup libPop = new Popup();
		libPop.addItemWithLabel("", "NEW TARGET ->");
		List<String> targets = SQLAssay.targets(this.getSQLDataSource());
		ListIterator<String> anIter = targets.listIterator();

		while( anIter.hasNext() ) {
			libPop.addItem(anIter.next());
		}
		return libPop;
	}
	
	private Popup targets(String name) throws DataException {
		Popup libPop = this.targets();
		libPop.setName(name);
		if ( this.hasFormValue(name) )
			libPop.setDefault(this.getFormValue(name));		
		return libPop;
	}
	
	private Popup unitFormats(String name) {
		Popup aPop =this.unitFormats();
		aPop.setName(name);
		if ( this.hasFormValue(name))
			aPop.setDefault(this.getFormValue(name));
		else 
			aPop.setDefault("%.2f %%");
		return aPop;
	}
	
	private Popup unitFormats() {
		Popup aPop = new Popup();
		aPop.addItemWithLabel("%.0f","Value");
		aPop.addItemWithLabel("%.2f %%","0.00 %");
		aPop.addItemWithLabel("%.2f ug/ml", "0.00 ug/ml");
		aPop.addItemWithLabel("%f", "Unformatted");
		return aPop;
	}
	
	private Popup sizes() {
		Popup aPop = new Popup();
		aPop.addItemWithLabel("4x6", "24 well (4x6)");
		aPop.addItemWithLabel("8x12", "96 well (8x12)");
		aPop.addItemWithLabel("16x24", "384 well (16x24)");
		return aPop;
	}
	
/*	private Popup sizes(String name) {
		Popup aPop = this.sizes();
		aPop.setName(name);
		if ( this.hasFormValue(name) )
			aPop.setDefault(this.getFormValue("size"));
		else 
			aPop.setDefault("8x12");
		return aPop;
	}
*/	
	
	public String protocolForm(Map<String,String> myProtocol) {
		Table myTable = new Table();
		myTable.setAttribute("ALIGN","CENTER");
		
		boolean hasProtocol = ( myProtocol != null);
		
		TableCell myCell = new TableCell("Project:");
		try {
			Popup aPop = this.projectPopup();
			aPop.setName("project");
			if ( hasProtocol ) {
				String projectID = (String)myProtocol.get("project");
				if ( projectID != null )
					aPop.setDefault(projectID);
			}
			myCell.addItem(aPop.toString());
		} catch (DataException e) {
			myCell.addItem("<B><FONT COLOR='RED'>ERROR:</FONT> " + e.getMessage() + "</B>");
		}
		TableRow myRow = new TableRow(myCell);		

		myCell = new TableCell("Assay Target:");
		StringBuffer libData = new StringBuffer();
		try {
			Popup libPop = this.targets("trgt");
			if ( hasProtocol && myProtocol.containsKey("trgt")) libPop.setDefault((String)myProtocol.get("trgt"));
			libData.append(libPop.toString());
		} catch (DataException e) {
			libData.append("<FONT COLOR='red'><B>Cannot load targets</FONT><BR/>" + e.getMessage() + "</B>");
		}
		libData.append("<INPUT TYPE='TEXT' NAME='newTarget' SIZE='25'/>");		
		myCell.addItem(libData.toString());
		myRow.addItem(myCell);
		
		myCell = new TableCell("Active Level:");
		Popup aPop = this.activeOperators();
		aPop.setName("active_op");
		if ( hasProtocol && myProtocol.containsKey("active_op"))
			aPop.setDefault((String)myProtocol.get("active_op"));
		else 
			aPop.setDefault(Assay.OPERATOR_GREATER_EQUAL);
		String active = "";
		if ( hasProtocol && myProtocol.containsKey("active"))
			active = (String)myProtocol.get("active");
		
		myCell.addItem(aPop.toString() + "<INPUT TYPE=TEXT NAME='active' SIZE=10 VALUE=\"" + active + "\"/>");
		myRow.addItem(myCell);

		myCell = new TableCell("Unit Format:");
		aPop = this.unitFormats();
		aPop.setName("unit");
		if ( hasProtocol && myProtocol.containsKey("unit"))
			aPop.setDefault((String)myProtocol.get("unit"));
		else 
			aPop.setDefault("%.2f %%");
		myCell.addItem(aPop);
		myRow.addItem(myCell);

		myCell = new TableCell("Assay Size:");
		aPop = this.sizes();
		aPop.setName("size");
		if ( hasProtocol && myProtocol.containsKey("size"))
			aPop.setDefault((String)myProtocol.get("size"));
		else 
			aPop.setDefault("8x12");
		myCell.addItem(aPop.toString());
		myRow.addItem(myCell);
		myTable.addItem(myRow);
		return myTable.toString();
	}
	
	public String protocolModule(Map<String,String> aTemplate) {
		StringBuffer output = new StringBuffer();
		output.append(this.protocolForm(aTemplate));
		output.append("<P ALIGN='CENTER'>");
		output.append(ProtocolForm.loadButton("Load an Assay Template"));
		output.append(ProtocolForm.saveButton("Save as an Assay Template"));
		output.append("</P>");
		return output.toString();
	}

	public static String assayMenu(CyanosWrapper aWrap) {
		Popup collPop = new Popup();
		collPop.addItem("");
		collPop.setName("id");		
		collPop.setAttribute("onChange", "this.form.submit()");
		
		Popup targetPop = new Popup();
		targetPop.addItemWithLabel("", "ALL");
		targetPop.setName("trgt");		
		targetPop.setAttribute("onChange", "this.form.submit()");		
		
		try {
			Assay myAssay;
			String myTarget = null;
			if ( aWrap.hasFormValue("id") && (! aWrap.getFormValue("id").equals("")) ) {
				myAssay = new SQLAssay(aWrap.getSQLDataSource(), aWrap.getFormValue("id"));
				if ( myAssay.isAllowed(Role.READ) ) {
					collPop.setDefault(myAssay.getID());
					myTarget = myAssay.getTarget();
				}
			} else {
				myTarget = aWrap.getFormValue("trgt");
			}

			if ( myTarget != null && (! myTarget.equals("")) ) {
				targetPop.setDefault(myTarget);
				myAssay = SQLAssay.assaysForTarget(aWrap.getSQLDataSource(), myTarget);
			} else {
				myAssay = SQLAssay.assays(aWrap.getSQLDataSource());
			}

			if ( myAssay != null ) {
				myAssay.beforeFirst();
				while (myAssay.next()) {
					collPop.addItemWithLabel(myAssay.getID(),myAssay.getName());
				}
			}

			List<String> targets = SQLAssay.targets(aWrap.getSQLDataSource());
			ListIterator<String> anIter = targets.listIterator();
			while ( anIter.hasNext() ) {
				targetPop.addItem(anIter.next());
			}
			
			TableCell myCell = new TableCell();
			TableRow aRow = new TableRow(myCell);
			Table formTable = new Table(aRow);
			formTable.setAttribute("ALIGN", "CENTER");

			Form plateForm = new Form("<B>Assay Plate: </B>");
			plateForm.addItem(collPop.toString());
			myCell.addItem(plateForm);
				
			Form targetForm = new Form(" <B>Filter with target: </B>");
			targetForm.addItem(targetPop.toString());
			myCell.addItem(targetForm);
			
			return formTable.toString();
		} catch (DataException e) {
			return aWrap.handleException(e);
		}
	}
	
	/*
	
	public static String targetHitList(CyanosWrapper aWrap, Assay myAssay) {	
		try {
			TableRow myRow = new TableRow("<TD COLSPAN=4 ALIGN='CENTER'><B><FONT SIZE='+2'>Target Hit List</FONT></B></TD>");
			myRow.addItem("<TH>Sample</TH><TH>Assay</TH><TH>Location</TH><TH>Concentration</TH><TH>Activity</TH>");
			Table myTable = new Table(myRow);
			myTable.setAttribute("class","dashboard");
			myTable.setAttribute("align","center");
			myTable.setAttribute("width","75%");
			String currClass = "odd";
			if ( myAssay != null ) {
				myAssay.beforeFirst();
				while ( myAssay.next() ) {
					AssayData myData = myAssay.getActiveData();
					if ( myData.first() ) {
						myData.beforeFirst();
						String myID = myAssay.getID();
						String myName = myAssay.getName();
						while ( myData.next() ) {
							String sampleName;
							Sample aSample = myData.getSample();
							if ( aSample != null ) 
								sampleName = String.format("<A HREF='%s/sample?id=%s'>%s</A>", aWrap.getContextPath(), aSample.getID(), myData.getLabel());
							else 
								sampleName = String.format("<A HREF='%s/strain?id=%s'>%s</A>", aWrap.getContextPath(), myData.getStrainID(), myData.getLabel());							
							TableCell myCell = new TableCell(sampleName);
							String assayName = String.format("<A HREF='%s/assay?id=%s'>%s</A>", aWrap.getContextPath(), myID, myName);
							myCell.addItem(assayName);
							myCell.addItem(myData.getLocation());
							float conc = myData.getConcentration();
							if ( conc != 0 ) {
								myCell.addItem(BaseForm.formatAmount("%.0f %s", conc, "ug/ml"));
							} else
								myCell.addItem("-");
							myCell.addItem(myData.getActivityString());
							TableRow aRow = new TableRow(myCell);
							aRow.setClass(currClass);				
							aRow.setAttribute("align", "center");
							myTable.addItem(aRow);
							if ( currClass.equals("odd") ) {
								currClass = "even";
							} else {
								currClass = "odd";
							}						
						}
					}
				}	
				return myTable.toString();
			} else {
				return "";
			}
		} catch ( DataException e ) {
			return aWrap.handleException(e);
		}
	}
	*/

	/*
	public String assayList(Assay myAssay) {
		String[] vialHeaders = {"Strain", "Sample Name", "Concentration", "Location", "Activity"};
		TableCell headers = new TableHeader(vialHeaders);
		TableRow tableRow = new TableRow(headers);
		Table vialTable = new Table(tableRow);
		headers.setAttribute("class", "header");
		vialTable.setAttribute("class","dashboard");
		vialTable.setAttribute("align","center");
		vialTable.setAttribute("width","95%");
		try {
			AssayData myData = myAssay.getAssayData();
			myData.beforeFirst();
			String curClass = "odd";
			while ( myData.next() ) {
				try {
					TableCell myCell = new TableCell();
					Strain aStrain = SQLStrain.load(this.getSQLDataSource(), myData.getStrainID());
					if ( aStrain.first() )
						myCell.addItem(this.strainLink(aStrain));
					else myCell.addItem(myData.getStrainID());
					myCell.addItem(myData.getLabel());
					float conc = myData.getConcentration();
					if ( conc > 0 ) {
						myCell.addItem(BaseForm.formatAmount("%.0f %s", conc, "ug/ml"));						
					} else {
						myCell.addItem("");
					}
					myCell.addItem(myData.getLocation());
					myCell.addItem(myData.getActivityString());
					TableRow aRow = new TableRow(myCell);
					aRow.setAttribute("align", "center");
					vialTable.addItem(aRow);
					if (myData.isActive()) {
						aRow.setClass("danger" + curClass);
					} else {
						aRow.setClass(curClass);				
					}
					if ( curClass.equals("odd") ) {
						curClass = "even";
					} else {
						curClass = "odd";
					}
				} catch (DataException e ) {
					e.printStackTrace();
					vialTable.addItem("<TR><TD ALIGN='CENTER' COLSPAN='4'><B><FONT COLOR='red'>ERROR:</FONT> " + e.getMessage() + "</B></TD><TR>");
				}
			}
		} catch ( DataException e ) {
			e.printStackTrace();
			vialTable.addItem("<TR><TD ALIGN='CENTER' COLSPAN='4'><B><FONT COLOR='red'>SQL FAILURE:</FONT> " + e.getMessage() + "</B></TD><TR>");
		}
		return vialTable.toString();
	}
	*/
	
	public String addAssay() {
		return AssayForm.addAssay(this.myWrapper);
	}
	
	public static String addAssay(CyanosWrapper aWrap) {
		Paragraph aParagraph = new Paragraph("Adding a new assay...<BR/>");
		aParagraph.setAlign("CENTER");
		try {
			SQLData data = aWrap.getSQLDataSource();
			if ( aWrap.hasFormValue("id")) {
				Assay newAssay =SQLAssay.createInProject(data, aWrap.getFormValue("id"), aWrap.getFormValue("project"));
				newAssay.first();
				newAssay.setManualRefresh();
				String aTarget = aWrap.getFormValue("trgt");
				if ( aTarget.equals("") ) aTarget = aWrap.getFormValue("newTarget");
				newAssay.setTarget(aTarget);
				newAssay.setName(aWrap.getFormValue("assayName"));
				newAssay.setDate(aWrap.getFormValue("assayDate"));
				newAssay.setActiveLevel(aWrap.getFormValue("active"));
				newAssay.setActiveOperator(aWrap.getFormValue("active_op"));
				String dimS[] = aWrap.getFormValue("size").split("x",2);
				newAssay.setLength(Integer.parseInt(dimS[0]));
				newAssay.setWidth(Integer.parseInt(dimS[1]));
				newAssay.setUnit(aWrap.getFormValue("unit"));
				newAssay.setNotes(aWrap.getFormValue("notes"));
				newAssay.refresh();
				aParagraph.addItem("<B><FONT COLOR='greem'>SUCCESS:</FONT> Added a new assay.</B><BR/>");
				aParagraph.addItem(String.format("<A HREF='../assay?id=%s'>View assay</A>", newAssay.getID()));
				int size = newAssay.getLength() * newAssay.getWidth();
				if ( size >= 384 && (! aWrap.getFormValue("source1").equals("")) ) {
					aParagraph.addItem("<BR/><BR/>");
					SampleCollection source1 = SQLSampleCollection.load(data, aWrap.getFormValue("source1"));
					SampleCollection source2 = SQLSampleCollection.load(data, aWrap.getFormValue("source2"));
					SampleCollection source3 = SQLSampleCollection.load(data, aWrap.getFormValue("source3"));
					SampleCollection source4 = SQLSampleCollection.load(data, aWrap.getFormValue("source4"));
					if ( aWrap.hasFormValue("loadType") && aWrap.getFormValue("loadType").equals("full") ) {
						int midCol = newAssay.getWidth() / 2;
						int midRow = newAssay.getLength() / 2;
						aParagraph.addItem(AssayForm.addSamplesToAssay(source1, newAssay, 0, 0));
						aParagraph.addItem(AssayForm.addSamplesToAssay(source2, newAssay, 0, midCol));
						aParagraph.addItem(AssayForm.addSamplesToAssay(source3, newAssay, midRow, 0));
						aParagraph.addItem(AssayForm.addSamplesToAssay(source4, newAssay, midRow, midCol));
					} else {
						boolean oddCol = true;
						AssayPlate myData = newAssay.getAssayData();
						myData.beforeFirstColumn();
						source1.beforeFirstColumn();
						source2.beforeFirstColumn();
						source3.beforeFirstColumn();
						source4.beforeFirstColumn();
						while ( myData.nextColumn() ) {
							myData.beforeFirstRow();
							SampleCollection sourceA, sourceB;
							if ( oddCol ) {
								// Plate 1 & 3
								sourceA = source1;
								sourceB = source3;
							} else {
								// Plate 2 & 4
								sourceA = source2;
								sourceB = source4;
							}
							sourceA.beforeFirstRow();
							sourceA.nextColumn();
							sourceB.beforeFirstRow();
							sourceB.nextColumn();
							boolean oddRow = true;
							while ( myData.nextRow() ) {
								Sample aSample = null;
								if ( oddRow ) {
									sourceA.nextRow();
									aSample = sourceA.getCurrentSample();
								} else {
									sourceB.nextRow();
									aSample = sourceB.getCurrentSample();
								}
								if ( aSample != null ) {
						/*			aSample.firstTransaction();
									Class aClass = aSample.getTransactionReferenceClass();
									while ( aClass != null && aClass.equals(Sample.class) ) {
										aSample = (Sample)aSample.getTransactionReference();
									} */
									if (! myData.currentLocationExists() ) myData.addCurrentLocation(aSample.getCultureID());
									myData.setSample(aSample);
									myData.setLabel(aSample.getName());
								}
								oddRow = (! oddRow);
							}
							oddCol = (! oddCol);
						}
						aParagraph.addItem("Adding sample plates in interlace mode.<BR/>");
						aParagraph.addItem(String.format("Added samples from sample collection <A HREF='../../sample?col=%s'>%s</A><BR/>", 
								source1.getID(), source1.getName()));
						aParagraph.addItem(String.format("Added samples from sample collection <A HREF='../../sample?col=%s'>%s</A><BR/>", 
								source2.getID(), source2.getName()));
						aParagraph.addItem(String.format("Added samples from sample collection <A HREF='../../sample?col=%s'>%s</A><BR/>", 
								source3.getID(), source3.getName()));
						aParagraph.addItem(String.format("Added samples from sample collection <A HREF='../../sample?col=%s'>%s</A><BR/>", 
								source4.getID(), source4.getName()));
					}
				} else if ( ! aWrap.getFormValue("source").equals("") ) {
					aParagraph.addItem("<BR/><BR/>");
					SampleCollection aCol = SQLSampleCollection.load(aWrap.getSQLDataSource(), aWrap.getFormValue("source"));
					try {
						aParagraph.addItem(AssayForm.addSamplesToAssay(aCol, newAssay));
					} catch (DataException e) {
						aParagraph.addItem(aWrap.handleException(e));
					}
				}
				
			} else {
				aParagraph.addItem("<P ALIGN='CENTER'><B><FONT COLOR='red'>ERROR:</FONT> Could not create an assay record.</B></P>");
			}
		} catch (DataException e) {
			e.printStackTrace();
			aParagraph.addItem("<P ALIGN='CENTER'><B><FONT COLOR='red'>ERROR:</FONT> " + e.getMessage() + "</B></P>");
		}
		return aParagraph.toString();
	}
	
	public static String addSamplesToAssay(SampleCollection aCol, Assay anAssay) throws DataException {
		return AssayForm.addSamplesToAssay(aCol, anAssay, 0, 0);
	}
	
	public static String addSamplesToAssay(SampleCollection aCol, Assay anAssay, int rowOffset, int colOffset) throws DataException {
		if ( aCol.first() ) {
			aCol.beforeFirstRow();
			aCol.firstColumn();
			AssayPlate myData = anAssay.getAssayData();
			while ( aCol.nextLocationByRow() ) {
				Sample aSample = aCol.getCurrentSample();
				myData.gotoLocation(aCol.currentRowIndex() + rowOffset, aCol.currentColumnIndex() + colOffset);
				if ( ! myData.currentLocationExists() ) myData.addCurrentLocation(aSample.getCultureID());
				/*	aSample.firstTransaction();
					Class aClass = aSample.getTransactionReferenceClass();
					while ( aClass != null && aClass.equals(Sample.class) ) {
						aSample = (Sample)aSample.getTransactionReference();
					} */
				myData.setSample(aSample);
				myData.setLabel(aSample.getName());
			}
			return(String.format("Added samples from sample collection <A HREF='../../sample?col=%s'>%s</A><BR/>", 
					aCol.getID(), aCol.getName()));
		} else {
			return("<B><FONT COLOR='red'>ERROR: </FONT> Could not find sample collection.</B>");
		}			
	}

	public String dataForm(Assay anObject) {
		if ( anObject.isAllowed(Role.WRITE) && this.hasFormValue("showBrowser") ) {
			Form myForm = new Form(DataForm.fileManagerApplet(this.myWrapper, "assay", anObject.getID(), null, false));				
			myForm.addItem(String.format("<P ALIGN='CENTER'><BUTTON TYPE='BUTTON' onClick=\"updateForm(this,'%s')\" NAME='cancelBrowser'>Close</BUTTON>", DATA_FORM));
			myForm.setAttribute("NAME", "dataBrowser");
			myForm.addHiddenValue("id", anObject.getID());
			myForm.addHiddenValue("div", DATA_FORM);
			return myForm.toString();
		} else {
			StringBuffer output = new StringBuffer();
			DataForm dataForm = new DataForm(this.myWrapper);
			dataForm.setTypeLabel(Assay.RAW_DATA_TYPE, "Raw Data");
			dataForm.setTypeLabel(Assay.REPORT_DATA_TYPE, "Report");
			output.append(dataForm.datafileTable(anObject));
			if ( anObject.isAllowed(Role.WRITE) )
				output.append(String.format("<FORM><P ALIGN='CENTER'><INPUT TYPE=HIDDEN NAME='id' VALUE='%s'/><BUTTON TYPE='BUTTON' NAME='showBrowser' onClick=\"loadForm(this, '%s')\">Manage Data Files</BUTTON></P></FORM>", anObject.getID(), DATA_FORM));
			return output.toString();
		}
	}

	
}
