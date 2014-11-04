/**
 * 
 */
package edu.uic.orjala.cyanos.web.upload;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.List;
import java.util.ListIterator;

import javax.servlet.http.HttpServletRequest;

import edu.uic.orjala.cyanos.Assay;
import edu.uic.orjala.cyanos.Assay.AssayTemplate;
import edu.uic.orjala.cyanos.AssayPlate;
import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.Material;
import edu.uic.orjala.cyanos.Role;
import edu.uic.orjala.cyanos.Sample;
import edu.uic.orjala.cyanos.User;
import edu.uic.orjala.cyanos.sql.SQLAssay;
import edu.uic.orjala.cyanos.sql.SQLAssayTemplate;
import edu.uic.orjala.cyanos.sql.SQLMaterial;
import edu.uic.orjala.cyanos.sql.SQLSample;
import edu.uic.orjala.cyanos.web.BaseForm;
import edu.uic.orjala.cyanos.web.UploadForm;
import edu.uic.orjala.cyanos.web.html.HtmlList;

/**
 * @author George Chlipala
 *
 */
public class AssayUpload extends UploadForm {

	public static final String TITLE = "Bioassay Data";
	public static final String PROTOCOL = "assay upload";

	public static final String STRAIN_ID = "strainID";
	public static final String ASSAY_ID = "assayID";
	public static final String LOCATION = "location";
	public static final String AUTO_LOC = "autoLoc";
	public static final String VALUE = "value";
	public static final String STDEV = "stdev";
	public static final String MATERIAL = "material";
	public static final String SAMPLE = "sample";
	public static final String SAMPLE_AMT = "sample_amt";
	public static final String NAME = "name";
	public static final String ASSAY_FROM_DB = "assayFromDB";
	public static final String ASSAY_PROTOCOL = "assayProtocol";
	public static final String CONC = "conc";
	public static final String CONC_UNIT = "concUnit";

	public static final String[] templateKeys = {PARAM_HEADER, STRAIN_ID, ASSAY_ID, LOCATION, AUTO_LOC, VALUE, STDEV, MATERIAL, SAMPLE, SAMPLE_AMT, NAME, 
		ASSAY_FROM_DB, ASSAY_PROTOCOL, CONC, CONC_UNIT};
	private static final String[] templateHeader = {"Assay ID", "Strain ID", "Location", "Activity", "Material ID", "Sample ID", "Sample Amount", "Label", "Concentration"};
	private static final String[] templateType = {"Required", "Required", "Required", "Optional", "Optional", "Optional", "Optional", "Optional", "Optional"};

	public static final String JSP_FORM = "/upload/forms/assay.jsp";
	
	public AssayUpload(HttpServletRequest req) {
		super(req);
		this.accessRole = User.BIOASSAY_ROLE;
		this.permission = Role.CREATE;
	}

	public String worksheetTemplate() {
		return this.worksheetTemplate(templateHeader, templateType);
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		if ( this.working ) return;
		StringBuffer output = new StringBuffer();
		List<Integer> rowNum = this.rowList();
		this.done = 0;
		this.todos = rowNum.size();
		this.working = true;
		// Setup the row iterator.
		ListIterator<Integer> rowIter = rowNum.listIterator();
		HtmlList resultList = new HtmlList();
		resultList.unordered();
		Savepoint savepoint = null;
		try {
			savepoint = this.myData.setSavepoint();
			/*
			 *	"strainID", "assayID", "location", "value", "sample", "name", "header", "assayFromDB", "assayProtocol";
			 */
			int strainIDCol = Integer.parseInt((String)template.get(STRAIN_ID));
			int assayIDCol = Integer.parseInt((String)this.template.get(ASSAY_ID));
			int valueCol = Integer.parseInt((String)this.template.get(VALUE));
			int materialCol = Integer.parseInt(this.template.get(MATERIAL));
			int sampleCol = Integer.parseInt((String)this.template.get(SAMPLE));
//			int sampleAmtCol = Integer.parseInt(this.template.get(SAMPLE_AMT));
			int nameCol = Integer.parseInt((String)this.template.get(NAME));
			int concCol = Integer.parseInt((String)this.template.get(CONC));
			int stdevCol = Integer.parseInt((String)this.template.get(STDEV));
			String concUnit = this.template.get(CONC_UNIT);
			boolean autoLoc = ( assayIDCol < 0 && this.template.containsKey(AUTO_LOC));
			int locationCol = -1;
			if ( ! autoLoc ) locationCol = Integer.parseInt((String)this.template.get(LOCATION));

			int maxCol = ( strainIDCol > assayIDCol ? strainIDCol: assayIDCol);
			maxCol = ( locationCol > maxCol ? locationCol : maxCol );
			maxCol = ( valueCol > maxCol ? valueCol : maxCol );
			maxCol = ( sampleCol > maxCol ? sampleCol : maxCol );
			maxCol = ( nameCol > maxCol ? nameCol : maxCol );
			maxCol = ( concCol > maxCol ? concCol : maxCol );
			maxCol = ( stdevCol > maxCol ? stdevCol : maxCol );
//			maxCol = (sampleAmtCol > maxCol ? sampleAmtCol : maxCol );
			maxCol = ( materialCol > maxCol ? materialCol : maxCol );

			boolean staticAssayID = false;

			Assay myAssay = null;
			AssayPlate myData = null;
			if ( assayIDCol == -1 ) {
				staticAssayID = true;
				myAssay = SQLAssay.load(this.myData, (String)this.template.get(ASSAY_FROM_DB));
				myData = myAssay.getAssayData();
			}

			boolean useAssayProtocol = false;
			AssayTemplate assayTemplate = null;
			if ( ! this.template.get(ASSAY_PROTOCOL).equals("") ) {
				useAssayProtocol = true;
				assayTemplate = SQLAssayTemplate.load(getSQLDataSource(), template.get(ASSAY_PROTOCOL));
			}

			if ( strainIDCol < 0 && valueCol < 0 && materialCol < 0 && sampleCol < 0 && nameCol < 0 ) {
				output.append("<P ALIGN='CENTER'><B><FONT COLOR='red'>ERROR!</FONT> No upload information specified!</B><BR/>");
				this.working = false;
				return;
			}

			if ( autoLoc && myData != null ) {
				myData.beforeFirstColumn();
				myData.firstRow();
			}

			PARSE_ROW: while (rowIter.hasNext() && this.working ) {
				Integer row = (Integer)rowIter.next();
				this.done++;
				if ( this.worksheet.gotoRow(row.intValue()) ) {
					HtmlList currResults = new HtmlList();
					currResults.unordered();

					if ( this.worksheet.rowSize() < maxCol ) {
						currResults.addItem(SKIP_TAG + "Spreadsheet row seems to be incomplete.");
						continue PARSE_ROW;
					}
					
//					Savepoint rowSave;
					try {
						if ( ! staticAssayID ) {
							String assayID = this.worksheet.getStringValue(assayIDCol);
							if ( myAssay == null || (! assayID.equals(myAssay.getID()) ) ) {
								myAssay = SQLAssay.load(this.myData, assayID);
								if ( ! myAssay.first() ) {
									myAssay = SQLAssay.create(this.myData, assayID);
									if ( myAssay.first() ) {
										currResults.addItem(SUCCESS_TAG + "Created a new assay.");
										if ( useAssayProtocol ) {
											myAssay.setTemplate(assayTemplate);
										} 
										myAssay.setNotes("");
										myAssay.setName(assayID);
									} else {
										currResults.addItem(ERROR_TAG + "Could not create a new assay.");
									}
								} else {
									currResults.addItem(FOUND_TAG + "Assay exists. Will NOT update assay information.");
								}
								myData = myAssay.getAssayData();
							}
						}
						boolean doUpdate = false;

						if ( autoLoc ) {
							doUpdate = myData.nextLocationByColumn();
						} else {
							doUpdate = myData.gotoLocation(this.worksheet.getStringValue(locationCol));
						}

						if (doUpdate) {
							Sample aSample = null;
							Material material = null;
							String strainID = null;
							if ( sampleCol > -1 && (! this.worksheet.getStringValue(sampleCol).equals("")) ) {
								aSample = new SQLSample(this.myData, this.worksheet.getStringValue(sampleCol));
								strainID = aSample.getCultureID();
							} else if ( materialCol > -1 && this.worksheet.getStringValue(materialCol).length() > 0 ) {
								material = SQLMaterial.load(this.myData, this.worksheet.getStringValue(materialCol));
								strainID = material.getCultureID();
							} else {
								strainID = this.worksheet.getStringValue(strainIDCol);
							}

							if ( ! myData.currentLocationExists() ) {
								myData.addCurrentLocation(strainID);
								myData.gotoLocation(this.worksheet.getStringValue(locationCol));
								currResults.addItem(SUCCESS_TAG + "Created new record for this location.");
							}

							if ( aSample != null ) {
								myData.setSample(aSample);	
/*								if ( sampleAmtCol > -1 && (! this.worksheet.getStringValue(sampleAmtCol).equals("")) ) {
									if ( aSample.getAmountForAssay(myAssay) != null ) {
										SampleAccount account = aSample.getAccount();
										if ( account.addTransaction() ) {
											account.withdrawAmount(this.worksheet.getStringValue(sampleAmtCol));
											account.setTransactionReference(myAssay);
											currResults.addItem(SUCCESS_TAG + "Withdrew amount from sample.");											
										} else {
											currResults.addItem(ERROR_TAG + "Unable to add transaction.");											
										}
									} else {
										currResults.addItem(SKIP_TAG + "Transaction already exists for assay. Will not withdraw amount from sample.");											
									}
								}
*/
							} else if ( material != null ) {
								myData.setMaterial(material);
							}

							if ( nameCol > -1 ) {
								myData.setLabel(this.worksheet.getStringValue(nameCol));
							} else if ( aSample != null ) {
								myData.setLabel(aSample.getName());
							}  else if ( material != null ) {
								myData.setLabel(material.getLabel());
							} else {
								myData.setLabel(strainID);
							}

							if ( valueCol > -1 ) {
								myData.setActivity(this.worksheet.getStringValue(valueCol));
							}
							if ( stdevCol > -1 ) {
								myData.setStdev(this.worksheet.getStringValue(stdevCol));
							}
							if ( nameCol > -1 ) {
								myData.setLabel(this.worksheet.getStringValue(nameCol));
							}
							if ( concCol > -1 ) {
								BigDecimal concValue = BaseForm.parseAmount(this.worksheet.getStringValue(concCol), concUnit);
								myData.setConcentration(concValue);
							}
							currResults.addItem(SUCCESS_TAG + "Information updated.");
						} else {
							currResults.addItem(FAILED_TAG + "Specified location outside size bounds of assay.");
						}
					} catch (DataException e) {
						currResults.addItem("<FONT COLOR='red'><B>ERROR</B></FONT>: " + e.getMessage());
						e.printStackTrace();
					}
					resultList.addItem(String.format("Row #:%d - %s (%s) %s", 
							row, myAssay.getID(), myData.currentLocation(), currResults.toString()));
				}
			}
		} catch (Exception e) {
			output.append("<P ALIGN='CENTER'><B><FONT COLOR='red'>ERROR:</FONT>" + e.getMessage() + "</B></P>");
			e.printStackTrace();
			this.working = false;
		}
		try {
			if ( this.working ) { 
				this.myData.commit(); 
				output.append("<P ALIGN='CENTER'><B>EXECUTION COMPLETE</B> CHANGES COMMITTED.</P>"); 
			}
			else { 
				if ( savepoint != null ) this.myData.rollback(savepoint); 
				else this.myData.rollback();
				output.append("<P ALIGN='CENTER'><B>EXECUTION HALTED</B> Upload incomplete!</P>"); }
		} catch (SQLException e) {
			output.append("<P ALIGN='CENTER'><B><FONT COLOR='red'>ERROR:</FONT>" + e.getMessage() + "</B></P>");
			e.printStackTrace();			
		}

		output.append(resultList.toString());
		this.working = false;
		this.resultOutput = output.toString();
	}

	/*
	public String templateForm(HttpServletRequest req) {
		TableCell myCell;
		List<String> headerList = this.getHeaderList(template.containsKey(HEADER));

		if ( template.containsKey(HEADER) ) {
			myCell = new TableCell("<INPUT TYPE='CHECKBOX' NAME='header' VALUE='true' onClick='this.form.submit()' CHECKED /> Spreadsheet has a header row.<BR>");					
		} else {
			myCell = new TableCell("<INPUT TYPE='CHECKBOX' NAME='header' VALUE='true' onClick='this.form.submit()' /> Spreadsheet has a header row.<BR>");
		}


		myCell.setAttribute("COLSPAN", "2");
		myCell.setAttribute("ALIGN", "CENTER");
		TableRow fullRow = new TableRow(myCell);

		Popup ssColPop = new Popup();
		Popup optionalPop = new Popup();
		Popup assayIDPop = new Popup();
		optionalPop.addItemWithLabel("-1", "SKIP ITEM");
		assayIDPop.addItemWithLabel("-1", "Use Assay ->");
		for ( int i = 0; i < headerList.size(); i++ ) {
			String index = String.valueOf(i);
			String value = (String)headerList.get(i);
			ssColPop.addItemWithLabel(index, value);
			optionalPop.addItemWithLabel(index, value);
			assayIDPop.addItemWithLabel(index, value);
		}

		assayIDPop.setName(ASSAY_ID);
		StringBuffer assayPop = new StringBuffer();
		assayPop.append("<SCRIPT type=\"text/javascript\">\n//<![CDATA[\nfunction setAutoLoc(assay_box) {\n " +
				"if ( assay_box.selectedIndex == 0 ) {\n  assay_box.form.autoLoc.disabled = 0;\n assay_box.form.location.disabled = assay_box.form.autoLoc.checked;\n " +
				" assay_box.form.assayFromDB.disabled = 0; \n} else {\n assay_box.form.assayFromDB.disabled = 1; assay_box.form.autoLoc.disabled = 1;\n assay_box.form.location.disabled = 0;\n}\n}\n//]]>\n</SCRIPT>");

		if ( template.containsKey(ASSAY_ID)) { 
			String selAssay = template.get(ASSAY_ID);
			assayIDPop.setDefault(selAssay);
		}

		assayIDPop.setAttribute("onChange", "setAutoLoc(this)");
		assayIDPop.setAttribute("onLoad", "setAutoLoc(this)");

		try {
			Assay assays = SQLAssay.assays(this.myData);
			assays.beforeFirst();
			Popup assayList = new Popup();
			while ( assays.next() ) {
				assayList.addItemWithLabel(assays.getID(), assays.getName());
			}
			assayList.setName(ASSAY_FROM_DB);
			if ( this.template.containsKey(ASSAY_FROM_DB) ) assayList.setDefault(this.template.get(ASSAY_FROM_DB));
			assayPop.append(assayList.toString());
		} catch (DataException e) {
			assayPop.append("<B><FONT COLOR='red'>ERROR:</FONT>" + e.getMessage() + "</B>");
		}

		myCell = new TableCell("Assay ID:");
		myCell.addItem(assayIDPop.toString() + assayPop.toString() );
		fullRow.addItem(myCell);

		fullRow.addItem(this.simpleTemplateRow("Strain ID:", STRAIN_ID, ssColPop));

		myCell = new TableCell("Location:");
		ssColPop.setName(LOCATION);
		if ( template.containsKey(LOCATION) ) ssColPop.setDefault(template.get(LOCATION));
		String autoLoc;
		if ( template.containsKey(AUTO_LOC) ) autoLoc = "<INPUT TYPE='CHECKBOX' NAME='autoLoc' onClick='this.form.location.disabled=this.checked;' CHECKED />";
		else autoLoc = "<INPUT TYPE='CHECKBOX' NAME='autoLoc' onClick='this.form.location.disabled=this.checked;' />";
		myCell.addItem(ssColPop.toString() + autoLoc + "Autolocation (along row: A1, A2, etc.) <SCRIPT type=\"text/javascript\">\n//<![CDATA[\n setAutoLoc(document.upload.assayID);\n//]]>\n</SCRIPT>");

		//		fullRow.addItem(this.simpleTemplateRow("Location:", "location", ssColPop));
		fullRow.addItem(myCell);
		fullRow.addItem(this.simpleTemplateRow("Activity:", VALUE, optionalPop));
		fullRow.addItem(this.simpleTemplateRow("STD Deviation:", STDEV, optionalPop));
		fullRow.addItem(this.protocolTemplateRow("Assay Protocol:", ASSAY_PROTOCOL, PROTOCOL));
		fullRow.addItem(this.simpleTemplateRow("Material ID:", MATERIAL, optionalPop));		
		fullRow.addItem(this.simpleTemplateRow("Sample ID:", SAMPLE, optionalPop));		
		fullRow.addItem(this.simpleTemplateRow("Sample Amount:", SAMPLE_AMT, optionalPop));		
		fullRow.addItem(this.simpleTemplateRow("Label:", NAME, optionalPop));	
		fullRow.addItem(this.templateRowWithUnit("Concentration:", CONC, CONC_UNIT, optionalPop, "ug/ml"));


		Table formTable = new Table(fullRow);
		formTable.setAttribute("WIDTH", "85%");

		return formTable.toString();
	}
	 */
	
	public String title() {
		return TITLE;
	}

	public String[] getTemplateKeys() {
		return templateKeys;
	}

	public String getTemplateType() {
		return PROTOCOL;
	}

	@Override
	public String jspForm() {
		return JSP_FORM;
	}

}
