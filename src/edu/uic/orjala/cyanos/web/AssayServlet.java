/**
 * 
 */
package edu.uic.orjala.cyanos.web;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import edu.uic.orjala.cyanos.Assay;
import edu.uic.orjala.cyanos.AssayData;
import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.Role;
import edu.uic.orjala.cyanos.Sample;
import edu.uic.orjala.cyanos.sql.SQLAssay;
import edu.uic.orjala.cyanos.web.forms.AssayForm;
import edu.uic.orjala.cyanos.web.forms.ProtocolForm;
import edu.uic.orjala.cyanos.web.html.Div;
import edu.uic.orjala.cyanos.web.html.Form;
import edu.uic.orjala.cyanos.web.html.Paragraph;
import edu.uic.orjala.cyanos.web.html.StyledText;

/**
 * @author George Chlipala
 *
 */
public class AssayServlet extends ServletObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3556286163243770282L;
	
	private static final String DATA_FILE_DIV_ID = "dataFiles";
	private static final String DATA_FILE_DIV_TITLE = "Data Files";
	private static final String DIV_DATA_LIST_ID = "assayList";
	private static final String DIV_DATA_LIST_TITLE = "Data List";
	private static final String DIV_PLATE_GRAPHIC_ID = "plateGraphic";
	private static final String DIV_PLATE_GRAPHIC_TITLE = "Graphic";
	private static final String DIV_PLATE_DATA_ID = "plateData";
	private static final String DIV_PLATE_DATA_TITLE = "Assay Data";
	private static final String DIV_PLATE_SAMPLE_ID = "plateSamples";
	private static final String DIV_PLATE_SAMPLE_TITLE = "Samples";


	public void display(CyanosWrapper aWrap) throws Exception {
		PrintWriter out;
		HttpServletRequest req = aWrap.getRequest();
		String module = req.getPathInfo();	
		
		if ( "/export".equals(module) ) {
			out = aWrap.getWriter();
			aWrap.setContentType("text/plain");
			if ( aWrap.hasFormValue("id") )
				out.println(this.exportAssay(aWrap));
			else
				out.println(this.exportAssayList(aWrap));
			out.flush();
//			out.close();
//			this.dbc.close();
			return;
		} else if ( aWrap.hasFormValue("div") ) {
			aWrap.setContentType("text/html");
			out = aWrap.getWriter();
			String divTag = aWrap.getFormValue("div");
			if ( "/protocol".equals(module) && divTag.equals(ProtocolForm.DIV_ID) ) {
				ProtocolForm aForm = new ProtocolForm(aWrap, AssayForm.ASSAY_PROTOCOL, AssayForm.PROTOCOL_KEYS);
				Form retForm = aForm.protocolForm();
//				AssayForm protoForm = new AssayForm(this);
//				retForm.addItem(protoForm.protocolModule(aForm.getTemplate()));
				out.print(retForm.toString());
			} else {
				Assay myAssay = new SQLAssay(aWrap.getSQLDataSource(), aWrap.getFormValue("id"));
				if ( myAssay.first() ) {
					if ( myAssay.isAllowed(Role.READ) ) {
						if ( divTag.equals(DATA_FILE_DIV_ID) ) {
							AssayForm aForm = new AssayForm(aWrap);
							Div aDiv = new Div(aForm.dataForm(myAssay));
							aDiv.setID(AssayForm.DATA_FORM);
							out.println(aDiv.toString());
						} else if ( divTag.equals(AssayForm.DATA_FORM) ) {
							AssayForm aForm = new AssayForm(aWrap);
							out.println(aForm.dataForm(myAssay));
						} else if ( divTag.equals(DIV_DATA_LIST_ID) ) {
							AssayForm aForm = new AssayForm(aWrap);
							out.println(aForm.assayList(myAssay));
						} else if ( divTag.equals(DIV_PLATE_DATA_ID)) {
							AssayForm aForm = new AssayForm(aWrap);
							out.println(aForm.showPlateData(myAssay));
						} else if ( divTag.equals(DIV_PLATE_SAMPLE_ID) ) {
							AssayForm aForm = new AssayForm(aWrap);
							out.println(aForm.showPlateSamples(myAssay));
						} else if ( divTag.equals(DIV_PLATE_GRAPHIC_ID) ) {
							AssayForm aForm = new AssayForm(aWrap);
							out.print(aForm.showPlateGraphic(myAssay));
						} else if ( divTag.equals(ProtocolForm.DIV_ID) ) {
							ProtocolForm aForm = new ProtocolForm(aWrap, AssayForm.ASSAY_PROTOCOL, AssayForm.PROTOCOL_KEYS);
							Form retForm = aForm.protocolForm();
							out.print(retForm.toString());
						}
					} else {
						out.print("ACCESS DENIED");
					}
				}
			}
			out.flush();
//			out.close();
//			this.dbc.close();
			return;

		}


		Assay thisAssay = null;
		if ( aWrap.hasFormValue("id") ) {
			thisAssay = new SQLAssay(aWrap.getSQLDataSource(), aWrap.getFormValue("id"));
			out = aWrap.startHTMLDoc(String.format("Assay ID: %s", thisAssay.getID()));
		} else {
			out = aWrap.startHTMLDoc("Assay Data");			
		}
		
		if ( module == null || module.equals("/")) {
			if ( thisAssay != null ) {
				Paragraph head = new Paragraph();
				head.setAlign("CENTER");
				StyledText title = new StyledText("Assay Data");
				title.setSize("+3");
				head.addItem(title);
				head.addItem("<HR WIDTH='85%'/>");
				out.println(head.toString());
				AssayForm aForm = new AssayForm(aWrap);
				out.println(aForm.assayInfo(thisAssay));
				out.println(aForm.loadableDiv(DIV_PLATE_GRAPHIC_ID, DIV_PLATE_GRAPHIC_TITLE));
				out.println(aForm.loadableDiv(DIV_DATA_LIST_ID, DIV_DATA_LIST_TITLE));
				out.println(aForm.loadableDiv(DATA_FILE_DIV_ID, DATA_FILE_DIV_TITLE));			
				out.println(aForm.loadableDiv(DIV_PLATE_DATA_ID, DIV_PLATE_DATA_TITLE));
				out.println(aForm.loadableDiv(DIV_PLATE_SAMPLE_ID, DIV_PLATE_SAMPLE_TITLE));
			} else {
				Paragraph head = new Paragraph();
				head.setAlign("CENTER");
				StyledText title = new StyledText("Assay List");
				title.setSize("+3");
				head.addItem(title);
				head.addItem("<HR WIDTH='85%'/>");
				aWrap.print(head.toString());
				aWrap.print(AssayForm.assayMenu(aWrap));
			}
		} else if ( module.equals("/hits") ){
			Paragraph head = new Paragraph();
			head.setAlign("CENTER");
			StyledText title = new StyledText("Assay Hit List");
			title.setSize("+3");
			head.addItem(title);
			head.addItem("<HR WIDTH='85%'/>");
			out.println(head.toString());
			out.println(AssayForm.assayMenu(aWrap));
			if ( thisAssay != null ) {
				out.println(String.format("<P ALIGN='CENTER'><A HREF='../assay?id=%s'>Return to main assay form</A></P>", thisAssay.getID()));
				out.println(AssayForm.targetHitList(aWrap, SQLAssay.assaysForTarget(aWrap.getSQLDataSource(), thisAssay.getTarget())));
			} else if ( aWrap.hasFormValue("trgt")) {
				out.println(String.format("<P ALIGN='CENTER'><A HREF='../assay?trgt=%s'>Return to main assay form</A></P>", aWrap.getFormValue("trgt")));
				out.println(AssayForm.targetHitList(aWrap, SQLAssay.assaysForTarget(aWrap.getSQLDataSource(), aWrap.getFormValue("trgt"))));
			} else {
				out.println("<P ALIGN='CENTER'><A HREF='../assay'>Return to main assay form</A></P>");
			}
		} else if ( module.equals("/protocol") ) {
			Paragraph head = new Paragraph();
			head.setAlign("CENTER");
			StyledText title = new StyledText("Assay Protocols");
			title.setSize("+3");
			head.addItem(title);
			head.addItem("<HR WIDTH='85%'/>");
			out.println(head.toString());
			
			Div formDiv = ProtocolForm.formDiv("Load an Assay Template", "Save as an Assay Template");

			AssayForm protoForm = new AssayForm(aWrap);
			Form aForm = new Form(protoForm.protocolForm(null));
			aForm.addItem(formDiv);
			
			out.println(aForm.toString());
			
		} else if ( module.equals("/add") ) {
			Paragraph head = new Paragraph();
			head.setAlign("CENTER");
			StyledText title = new StyledText("Setup a new assay");
			title.setSize("+3");
			head.addItem(title);
			head.addItem("<HR WIDTH='85%'/>");
			out.println(head.toString());
			AssayForm aForm = new AssayForm(aWrap);
			if ( aWrap.hasFormValue("addAssay"))
				out.println(aForm.addAssay());
			else 
				out.println(aForm.addForm());
		}

		aWrap.finishHTMLDoc();
	}

	private String exportAssay(CyanosWrapper aWrap) {
		try {
			Assay myAssay = new SQLAssay(aWrap.getSQLDataSource(), aWrap.getFormValue("id"));

			if ( myAssay.first() ) {
				if ( myAssay.isAllowed(Role.READ) ) {
					List<List> output = new ArrayList<List>();
					List<String> aRow = new ArrayList<String>();
					aRow.add("Assay ID");
					aRow.add("Location");
					aRow.add("Culture ID");
					aRow.add("Sample ID");
					aRow.add("Sample Label");
					aRow.add("Concentration (mg/ml)");
					aRow.add("Activity");
					output.add(aRow);
					AssayData myData = myAssay.getAssayData();
					myData.beforeFirstRow();
					myData.firstColumn();
					String assayID = myAssay.getID();
					while ( myData.nextLocationByRow() ) {
						if ( ! myData.currentLocationExists() ) continue;
						aRow = new ArrayList<String>();
						aRow.add(assayID);
						aRow.add(myData.currentLocation());
						aRow.add(myData.getStrainID());
						Sample aSample = myData.getSample();
						if ( aSample != null )
							aRow.add(aSample.getID());
						else
							aRow.add("");
						String name = myData.getLabel();
						if ( name != null ) 
							aRow.add(name.replaceAll("[\n\r]", " "));
						else
							aRow.add("");
						aRow.add(String.format("%f", myData.getConcentration()));
						aRow.add(myData.getActivityString());
						output.add(aRow);
					}
					return this.delimOutput(output, ",");
				} else {
					return "ACCESS DENIED!";
				}
			} else {
				return "ERROR: ASSAY NOT FOUND.";
			}
		} catch (DataException e) {
			e.printStackTrace();
			return "ERROR: " + e.getMessage();
		}

	}
	
	private String exportAssayList(CyanosWrapper aWrap) {
		try {
			Assay myAssay;
			
			if ( aWrap.hasFormValue("trgt")) {
				myAssay = SQLAssay.assaysForTarget(aWrap.getSQLDataSource(), aWrap.getFormValue("trgt"));
			} else {
				myAssay = SQLAssay.assays(aWrap.getSQLDataSource());
			} 
			
			if ( myAssay.first() ) {
			List<List> output = new ArrayList<List>();
			List<String> aRow = new ArrayList<String>();
			aRow.add("Assay ID");
			aRow.add("Name");
			aRow.add("Date");
			aRow.add("Target");
			aRow.add("Size");
			aRow.add("Notes");
			output.add(aRow);
			myAssay.beforeFirst();
				while ( myAssay.next() ) {
					aRow = new ArrayList<String>();
					aRow.add(myAssay.getID());
					aRow.add(myAssay.getName());
					aRow.add(myAssay.getDateString());
					aRow.add(myAssay.getTarget());
					aRow.add(String.format("%dx%d", myAssay.getLength(), myAssay.getWidth()));
					String notes = myAssay.getNotes();
					if ( notes != null ) 
						aRow.add(notes.replaceAll("[\n\r]", " "));
					else
						aRow.add("");
					output.add(aRow);
				}
				return this.delimOutput(output, ",");
			} else {
				return "ERROR: ASSAY NOT FOUND.";
			}
		} catch (DataException e) {
			e.printStackTrace();
			return "SQL ERROR: " + e.getMessage();
		}

	}
	
}
