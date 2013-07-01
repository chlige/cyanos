/**
 * 
 */
package edu.uic.orjala.cyanos.web.upload;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.Harvest;
import edu.uic.orjala.cyanos.Inoc;
import edu.uic.orjala.cyanos.Role;
import edu.uic.orjala.cyanos.Sample;
import edu.uic.orjala.cyanos.SampleAccount;
import edu.uic.orjala.cyanos.SampleCollection;
import edu.uic.orjala.cyanos.User;
import edu.uic.orjala.cyanos.sql.SQLHarvest;
import edu.uic.orjala.cyanos.sql.SQLSampleCollection;
import edu.uic.orjala.cyanos.web.CyanosWrapper;
import edu.uic.orjala.cyanos.web.Sheet;
import edu.uic.orjala.cyanos.web.UploadForm;
import edu.uic.orjala.cyanos.web.UploadModule;
import edu.uic.orjala.cyanos.web.html.Div;
import edu.uic.orjala.cyanos.web.html.HtmlList;
import edu.uic.orjala.cyanos.web.html.Popup;
import edu.uic.orjala.cyanos.web.html.Table;
import edu.uic.orjala.cyanos.web.html.TableCell;
import edu.uic.orjala.cyanos.web.html.TableRow;

/**
 * @author George Chlipala
 * 
 */
public class ExtractUpload extends UploadForm {

	public static final String PROTOCOL = "extract protocol";
	
	private static final String HEADER = "header";
	private static final String HARVEST_ID = "harvestID";
	private static final String EXTRACT_DATE = "extractDate";
	private static final String EXTRACT_LABEL = "extractLabel";
	private static final String DEFAULT_UNIT = "defaultUnit";
	private static final String EXTRACT_NOTES = "extractNotes";
	private static final String USE_PROTOCOL = "useProtocol";
	private static final String STATIC_PROTOCOL = "staticProtocol";
	private static final String DESTINATION = "destCol";
	private static final String DESTINATION_LOC = "destXYCol";
	private static final String STATIC_DESTINATION = "staticDestCol";
	private static final String EXTRACT_AMOUNT = "extractAmt";
	private static final String HARVEST_CELL_MASS = "harvestCellMass";
	private static final String HARVEST_CELL_UNIT = "harvestCellUnit";
	private static final String EXTRACT_PROTOCOL = "extractProtocol";
	private static final String EXTRACT_SOLVENT = "extractSolvent";
	private static final String EXTRACT_TYPE = "extractType";
	private static final String FORCE_UPLOAD = "forceUpload";
	private static final String PROJECT_COL = "projectCol";
	private static final String STATIC_PROJECT = "staticProject";

	public static final String[] templateKeys = { HEADER, HARVEST_ID,
			EXTRACT_DATE, EXTRACT_LABEL, DEFAULT_UNIT, EXTRACT_NOTES,
			USE_PROTOCOL, STATIC_PROTOCOL, DESTINATION, DESTINATION_LOC, STATIC_DESTINATION,
			EXTRACT_AMOUNT, HARVEST_CELL_MASS, HARVEST_CELL_UNIT,
			EXTRACT_PROTOCOL, EXTRACT_SOLVENT, EXTRACT_TYPE, FORCE_UPLOAD,
			PROJECT_COL, STATIC_PROJECT };
	private static final String[] templateHeader = {"Source Harvest ID", "Date", "Destination Collection", "Amount", "Cell Mass", "Location", "Label", "Notes", "Project Code"};
	private static final String[] templateType = {"Required", "Required", "Required or Static", "Required", "Optional", "Optional", "Optional", "Optional", "Optional or Static"};

	public static final String TITLE = "Extract Data";

	/**
	 * @param servlet Calling Servlet
	 * @param aSheet Spreadsheet to parse
	 * @throws SQLException 
	 * @throws DataException 
	 */
	public ExtractUpload(CyanosWrapper aWrapper, Sheet aSheet) throws DataException {
		super(aWrapper, aSheet);
		this.template = this.buildTemplate(templateKeys);
		this.hasHeaderRow = this.template.containsKey(HEADER);
		this.accessRole = User.SAMPLE_ROLE;
		this.permission = Role.CREATE;
	}
	
	public String worksheetTemplate() {
		return this.worksheetTemplate(templateHeader, templateType);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.uic.orjala.cyanos.web.upload.UploadForm#templateForm()
	 */
	public String templateForm() {
		TableCell myCell;
		List<String> headerList = this.getHeaderList(template.containsKey(HEADER));
		
		if ( template.containsKey(HEADER) ) {
			myCell = new TableCell("<INPUT TYPE='CHECKBOX' NAME='header' VALUE='true' onClick='this.form.submit()' CHECKED /> Spreadsheet has a header row.");					
		} else {
			myCell = new TableCell("<INPUT TYPE='CHECKBOX' NAME='header' VALUE='true' onClick='this.form.submit()' /> Spreadsheet has a header row.");
		}
		myCell.setAttribute("COLSPAN", "2");
		myCell.setAttribute("ALIGN", "CENTER");
		TableRow fullRow = new TableRow(myCell);
		
		if ( template.containsKey(FORCE_UPLOAD) )
			myCell = new TableCell("<INPUT TYPE='CHECKBOX' NAME='forceUpload' VALUE='true' CHECKED /> Force upload.<BR/> i.e. Overwrite existing extract information.");
		else 
			myCell = new TableCell("<INPUT TYPE='CHECKBOX' NAME='forceUpload' VALUE='true' /> Force upload.<BR/> i.e. Overwrite existing extract information.");
		myCell.setAttribute("COLSPAN", "2");
		myCell.setAttribute("ALIGN", "CENTER");
		fullRow.addItem(myCell);

		Popup ssColPop = new Popup();
		Popup optionalPop = new Popup();
		Popup colPop = new Popup();
		optionalPop.addItemWithLabel("-1", "SKIP ITEM");
		colPop.addItemWithLabel("-1", "Use Value ->");
		for ( int i = 0; i < headerList.size(); i++ ) {
			String index = String.valueOf(i);
			String value = (String)headerList.get(i);
			ssColPop.addItemWithLabel(index, value);
			optionalPop.addItemWithLabel(index, value);
			colPop.addItemWithLabel(index, value);
		}

		/*
		 * "harvestID", "extractDate", "extractLabel", "defaultUnit", 
						"useProtocol", "staticProtocol", "destCol", "staticDestCol",
						"extractProtocol", "extractSolvent", "extractType"
		 */
		
		fullRow.addItem(this.simpleTemplateRow("Harvest ID:", HARVEST_ID, ssColPop));

		colPop.setName(DESTINATION);
		if ( template.containsKey(DESTINATION)) {
			colPop.setDefault((String)template.get(DESTINATION));
		}
		String collections = "";
		try {
			SampleCollection myCols = SQLSampleCollection.sampleCollections(this.myData, SQLSampleCollection.ID_COLUMN, SQLSampleCollection.ASCENDING_SORT);
			myCols.beforeFirst();
			Popup colList = new Popup();
			while ( myCols.next() ) {
				colList.addItemWithLabel(myCols.getID(), myCols.getName());
			}
			colList.setName(STATIC_DESTINATION);
			if ( this.template.containsKey(STATIC_DESTINATION))
				colList.setDefault(this.template.get(STATIC_DESTINATION));
			collections = colList.toString();
		} catch (DataException e) {
			collections = "<B><FONT COLOR='red'>ERROR:</FONT>" + e.getMessage() + "</B>";
		}
		
		myCell = new TableCell("Collection:");
		myCell.addItem(colPop.toString() + collections );
		fullRow.addItem(myCell);
		
		fullRow.addItem(this.simpleTemplateRow("Location:", DESTINATION_LOC, optionalPop));
		fullRow.addItem(this.simpleTemplateRow("Date:", EXTRACT_DATE, ssColPop));	
		fullRow.addItem(this.templateRowWithUnit("Cell Mass:", HARVEST_CELL_MASS, HARVEST_CELL_UNIT, optionalPop));
		fullRow.addItem(this.simpleTemplateRow("Amount:", EXTRACT_AMOUNT, ssColPop));
		myCell = new TableCell("Default Unit:");
		String defaultUnit = new String("mg");
		if ( template.containsKey(DEFAULT_UNIT)) { defaultUnit = (String)template.get(DEFAULT_UNIT); }
		myCell.addItem(String.format("<INPUT TYPE='TEXT' SIZE=5 NAME='%s' VALUE='%s'/>", DEFAULT_UNIT, defaultUnit));
		fullRow.addItem(myCell);
		fullRow.addItem(this.simpleTemplateRow("Sample Label:", EXTRACT_LABEL, optionalPop));
		fullRow.addItem(this.simpleTemplateRow("Notes:", EXTRACT_NOTES, optionalPop));
		fullRow.addItem(this.projectTemplateRow("Project Code:", PROJECT_COL, colPop, STATIC_PROJECT));
		
		Div protoDiv = new Div("<TABLE><TR><TD>Protocol:</TD><TD>");
		String protocols = "";
		colPop.setName(EXTRACT_PROTOCOL);
		if ( template.containsKey(EXTRACT_PROTOCOL) ) {
			colPop.setDefault(template.get(EXTRACT_PROTOCOL));
		}
		protoDiv.addItem(colPop.toString());
		try {
			Popup protocolPop = this.protocolPopup(PROTOCOL);
			if ( this.template.containsKey(STATIC_PROTOCOL) )
				protocolPop.setDefault((String)this.template.get(STATIC_PROTOCOL));
			protocolPop.setName(STATIC_PROTOCOL);
			protocols = protocolPop.toString();
		} catch (DataException e) {
			protocols = "<B><FONT COLOR='red'>ERROR:</FONT>" + e.getMessage() + "</B>";
		}
		protoDiv.addItem(protocols);
		protoDiv.addItem("</TD></TR></TABLE>");
		protoDiv.setID("protoDiv");
		
		Div noProtoDiv = new Div("<TABLE>");
		TableRow protoRow = new TableRow(this.simpleTemplateRow("Extract type:", EXTRACT_TYPE, optionalPop));
		protoRow.addItem(this.simpleTemplateRow("Extract solvent:", EXTRACT_SOLVENT, optionalPop));
		noProtoDiv.addItem(protoRow.toString());
		noProtoDiv.addItem("</TABLE>");
		noProtoDiv.setID("nonProtoDiv");
		
		if ( this.template.containsKey(USE_PROTOCOL)) {
			fullRow.addItem("<TH COLSPAN=4 ALIGN='LEFT'><INPUT TYPE='CHECKBOX' NAME='useProtocol' VALUE='true' onClick=\"if ( this.checked ) { showHide('protoDiv','nonProtoDiv'); } else { showHide('nonProtoDiv','protoDiv'); }\" CHECKED/>Use an Extraction Protocol</TH>");
			protoDiv.setClass("showSection");
			noProtoDiv.setClass("hideSection");
			
		} else {
			fullRow.addItem("<TH COLSPAN=4 ALIGN='LEFT'><INPUT TYPE='CHECKBOX' NAME='useProtocol' VALUE='true' onClick=\"if ( this.checked ) { showHide('protoDiv','nonProtoDiv'); } else { showHide('nonProtoDiv','protoDiv'); }\"/>Use an Extraction Protocol</TH>");
			noProtoDiv.setClass("showSection");
			protoDiv.setClass("hideSection");
		}
		StringBuffer thisRow = new StringBuffer("<TD COLSPAN=4 ALIGN='LEFT'>");
		thisRow.append(protoDiv.toString());
		thisRow.append(noProtoDiv.toString());
		thisRow.append("</TD>");
		fullRow.addItem(thisRow.toString());
		
		Table formTable = new Table(fullRow);
		formTable.setAttribute("WIDTH", "85%");
		
		return formTable.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.uic.orjala.cyanos.web.upload.UploadForm#title()
	 */
	public String title() {
		return TITLE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		if (this.working) return;
		StringBuffer output = new StringBuffer();
		List rowNum = this.rowList();
		// Setup the row iterator.
		this.done = 0;
		this.todos = rowNum.size();
		this.working = true;
		ListIterator rowIter = rowNum.listIterator();
		HtmlList resultList = new HtmlList();
		resultList.unordered();

		this.resultSheet = new Sheet();

		boolean useProtocol = this.template.containsKey(USE_PROTOCOL);
		boolean safeUpload = (!this.template.containsKey(FORCE_UPLOAD));
		int harvestCol = Integer.parseInt(template.get(HARVEST_ID));
		int dateCol = Integer.parseInt(template.get(EXTRACT_DATE));
		int loadAmt = Integer.parseInt(template.get(EXTRACT_AMOUNT));
		String defaultUnit = template.get(DEFAULT_UNIT);
		int labelCol = Integer.parseInt(template.get(EXTRACT_LABEL));
		int notesCol = Integer.parseInt(template.get(EXTRACT_NOTES));
		int libCol = Integer.parseInt(template.get(DESTINATION));
		int locCol = Integer.parseInt(template.get(DESTINATION_LOC));
		String colID = template.get(STATIC_DESTINATION);
		boolean wksDest = (libCol > -1);
		int cellMassCol = Integer.parseInt(template.get(HARVEST_CELL_MASS));
		String cellMassUnit = this.template.get(HARVEST_CELL_UNIT);

		String staticProject = (String) this.template.get(STATIC_PROJECT);
		int projectCol = Integer.parseInt((String) this.template
				.get(PROJECT_COL));
		boolean useProjectCol = (projectCol > -1);

		int protCol = -1, solvCol = -1, typeCol = -1;
		boolean wksProtocol = false;
		Map myProtocol = null;
		String protocolName = "";

		if (useProtocol) {
			protCol = Integer.parseInt(template.get(EXTRACT_PROTOCOL));
			wksProtocol = (protCol > -1);
			protocolName = template.get(STATIC_PROTOCOL);
			if (!wksProtocol) {
				try {
					myProtocol = this.loadDataTemplate(PROTOCOL, protocolName);
				} catch (DataException e) {
					output.append(this.handleException(e));
				}
			}
		} else {
			solvCol = Integer.parseInt(template.get(EXTRACT_SOLVENT));
			typeCol = Integer.parseInt(template.get(EXTRACT_TYPE));
		}

		Date myDate = new Date();
		String bulkLoadNote = String.format(
				"\nCreated via bulk load by user: %s\n %s", this.myUser.getUserID(), myDate.toString());

		while (rowIter.hasNext() && this.working) {
			Integer row = (Integer) rowIter.next();
			if (this.worksheet.gotoRow(row.intValue())) {
				int myLen = this.worksheet.rowSize();
				HtmlList currResults = new HtmlList();
				currResults.unordered();
				resultSheet.addRow(row.intValue());
				DOROW: try {
					this.resultSheet.addCell(this.worksheet.getValue(harvestCol));
					Harvest myHarv = new SQLHarvest(this.myData, this.worksheet.getValue(harvestCol));
					if (myHarv.first() && dateCol < this.worksheet.rowSize()
							&& loadAmt < this.worksheet.rowSize()) {
						String thisDate = this.worksheet.getValue(dateCol);
						String amtString = this.worksheet.getValue(loadAmt);
						if (thisDate.equals("") || amtString.equals("")) {
							currResults.addItem(SKIP_TAG + "No extract information.");
							this.resultSheet.addCell("");
							this.resultSheet.addCell("SKIPPED: No extract information.");
							break DOROW;
						}
						if (cellMassCol > -1) {
							float cellMass = parseAmount(this.worksheet.getValue(cellMassCol), cellMassUnit);
							if (cellMass > 0) {
								myHarv.setCellMass(cellMass);
								currResults.addItem(SUCCESS_TAG + "Added cell mass information.");
							}
						}
						float amount = parseAmount(amtString, defaultUnit);
						Sample test = myHarv.getExtract();
						StringBuffer resultNotes = new StringBuffer();
						String extraID = null;
						if (test != null) {
							if (safeUpload) {
								currResults.addItem(FOUND_TAG + "Extract information exists.");
								currResults.addItem(SKIP_TAG + "Safe upload enabled.");
								this.resultSheet.addCell(test.getID());
								this.resultSheet.addCell("SKIPPED: Existing extract (SAFE UPLOAD).");
								break DOROW;
							} else {
								currResults.addItem(FOUND_TAG
												+ "An extract was found! Potential duplicate! Sample ID:"
												+ test.getID());
								resultNotes.append("FOUND: An extracts exists for this harvest. (ID following in next column).");
								extraID = test.getID();
							}
						}

						Sample extract = myHarv.createExtract();
						if (extract == null) {
							currResults.addItem(ERROR_TAG + "Could not create an extract record.");
							this.resultSheet.addCell("");
							resultNotes.append("ERROR: Could not create a new extract record.");
							this.resultSheet.addCell(resultNotes.toString());
							if (extraID != null)
								this.resultSheet.addCell(extraID);
							break DOROW;
						}
						this.resultSheet.addCell(extract.getID());
						currResults.addItem(SUCCESS_TAG + "Created a new extract.");
						extract.setDate(thisDate);
						if (useProtocol) {
							String aProtocol = this.worksheet.getValue(protCol);
							if (wksProtocol && protCol < myLen
									&& (!protocolName.equals(aProtocol))) {
								protocolName = aProtocol;
								myProtocol = this.loadDataTemplate(PROTOCOL, protocolName);
							}
							extract.setExtractSolvent((String) myProtocol.get("solvent"));
							extract.setExtractType((String) myProtocol.get("type"));
						} else {
							if (solvCol > -1 && solvCol < myLen)
								extract.setExtractSolvent(this.worksheet.getValue(solvCol));
							if (typeCol > -1 && typeCol < myLen)
								extract.setExtractType(this.worksheet.getValue(typeCol));
						}
						String myLabel = "";
						if (labelCol > -1 && labelCol < myLen) {
							myLabel = this.worksheet.getValue(labelCol);
						} else {
							Inoc anInoc = myHarv.getInoculations();
							if (anInoc.first()) {
								myLabel = myHarv.getInoculations().getStrain().getID();
							} else
								myLabel = "ORPHAN";
						}
						extract.setName(myLabel);
						if (wksDest)
							colID = this.worksheet.getValue(libCol);
						extract.setCollectionID(colID);
						if ( locCol > -1 && locCol < myLen ) {
							extract.setLocation(this.worksheet.getValue(locCol));
						}
						
						if (notesCol > -1 && notesCol < myLen) {
							extract.setNotes(this.worksheet.getValue(notesCol));
							extract.addNotes(bulkLoadNote);
						} else {
							extract.setNotes(bulkLoadNote);
						}
						extract.setBaseUnit(defaultUnit);
						if (useProjectCol)
							extract.setProjectID(this.worksheet.getValue(projectCol));
						else
							extract.setProjectID(staticProject);
						SampleAccount myAcct = extract.getAccount();
						if (myAcct.addTransaction()) {
							myAcct.depositAmount(amount);
							myAcct.setTransactionReference(myHarv);
							myAcct.setDate(thisDate);
							myAcct.setNotes("Initial Amount" + bulkLoadNote);
							myAcct.updateTransaction();
							currResults.addItem(SUCCESS_TAG + "Amount deposited into destination.");
							resultNotes.append("SUCCESS: Extract record created.");
						} else {
							resultNotes.append("ERROR: Could not add initial amount.");
							currResults.addItem(ERROR_TAG + "Could not create a transaction for the destination sample.");
						}
						this.resultSheet.addCell(resultNotes.toString());
						if (extraID != null)
							this.resultSheet.addCell(extraID);
					} else {
						currResults.addItem(ERROR_TAG + "Could not load source harvest.");
						this.resultSheet.addCell("");
						this.resultSheet.addCell("ERROR: Could not load harvest record.");
					}
				} catch (DataException e) {
					currResults.addItem(ERROR_TAG + e.getMessage());
					e.printStackTrace();
				} catch (IndexOutOfBoundsException e) {
					currResults.addItem(ERROR_TAG + "Column Index out of bounds. " + e.getMessage());
					e.printStackTrace();
				}
				resultList.addItem(String.format("Row #:%d %s", row + 1, currResults.toString()));
			}
			this.done++;
		}
		this.resultSheet.addRow(0);
		this.resultSheet.addCell("Harvest ID");
		this.resultSheet.addCell("Extract ID");
		this.resultSheet.addCell("Status");

		output.append(resultList.toString());
		if ( ! this.working ) { output.append("<P ALIGN='CENTER'><B>EXECUTION HALTED</B> Upload incomplete!</P>"); }
		this.working = false;
		this.resultOutput = output.toString();
	}

	@Deprecated
	public UploadModule makeNew(CyanosWrapper aServlet, Sheet aSheet) throws DataException {
		return new ExtractUpload(aServlet, aSheet);
	}

	public String[] getTemplateKeys() {
		return templateKeys;
	}
	public String getProtocolType() {
		return PROTOCOL;
	}
}
