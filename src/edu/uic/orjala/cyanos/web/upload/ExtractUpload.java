/**
 * 
 */
package edu.uic.orjala.cyanos.web.upload;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import javax.servlet.http.HttpServletRequest;

import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.Harvest;
import edu.uic.orjala.cyanos.Inoc;
import edu.uic.orjala.cyanos.Material;
import edu.uic.orjala.cyanos.Material.ExtractProtocol;
import edu.uic.orjala.cyanos.Role;
import edu.uic.orjala.cyanos.SampleAccount;
import edu.uic.orjala.cyanos.User;
import edu.uic.orjala.cyanos.sql.SQLData;
import edu.uic.orjala.cyanos.sql.SQLExtractProtocol;
import edu.uic.orjala.cyanos.sql.SQLHarvest;
import edu.uic.orjala.cyanos.web.BaseForm;
import edu.uic.orjala.cyanos.web.Sheet;
import edu.uic.orjala.cyanos.web.SheetValue;
import edu.uic.orjala.cyanos.web.UploadForm;
import edu.uic.orjala.cyanos.web.html.HtmlList;

/**
 * @author George Chlipala
 * 
 */
public class ExtractUpload extends UploadJob {

	public static final String PROTOCOL = "extract upload";

	public static final String HARVEST_ID = "harvestID";
	public static final String EXTRACT_DATE = "extractDate";
	public static final String EXTRACT_LABEL = "extractLabel";
	public static final String DEFAULT_UNIT = "defaultUnit";
	public static final String EXTRACT_NOTES = "extractNotes";
	public static final String USE_PROTOCOL = "useProtocol";
	public static final String STATIC_PROTOCOL = "staticProtocol";
//	public static final String DESTINATION = "destCol";
//	public static final String DESTINATION_LOC = "destXYCol";
//	public static final String STATIC_DESTINATION = "staticDestCol";
	public static final String EXTRACT_AMOUNT = "extractAmt";
	public static final String HARVEST_CELL_MASS = "harvestCellMass";
	public static final String HARVEST_CELL_UNIT = "harvestCellUnit";
	public static final String EXTRACT_PROTOCOL = "extractProtocol";
	public static final String EXTRACT_SOLVENT = "extractSolvent";
	public static final String EXTRACT_TYPE = "extractType";
	public static final String FORCE_UPLOAD = "forceUpload";
	public static final String PROJECT_COL = "projectCol";
	public static final String STATIC_PROJECT = "staticProject";

	public static final String[] templateKeys = { HARVEST_ID,
		EXTRACT_DATE, EXTRACT_LABEL, DEFAULT_UNIT, 
		EXTRACT_NOTES,
		USE_PROTOCOL, STATIC_PROTOCOL, 
		// DESTINATION, DESTINATION_LOC, STATIC_DESTINATION,
		EXTRACT_AMOUNT, HARVEST_CELL_MASS, HARVEST_CELL_UNIT,
		EXTRACT_PROTOCOL, EXTRACT_SOLVENT, EXTRACT_TYPE, FORCE_UPLOAD,
		PROJECT_COL, STATIC_PROJECT };

	
	
	private static final String[] templateHeader = {"Source Harvest ID", "Date", 
	//	"Destination Collection", 
		"Amount", "Cell Mass", 
	//	"Location", 
		"Label", "Notes", "Project Code"};
	
	private static final String[] templateType = {"Required", "Required", 
	//	"Required or Static", 
		"Required", "Optional", 
	//	"Optional", 
		"Optional", "Optional", "Optional or Static"};

	public static final String TITLE = "Extract Data Upload";

	public ExtractUpload(SQLData data) {
		super(data);
		this.type = TITLE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		if (this.working) return;
		this.done = 0;
		this.todos = this.rowList.size();
		this.working = true;
		// Setup the row iterator.
		ListIterator<Integer> rowIter = this.rowList.listIterator();
		HtmlList resultList = new HtmlList();
		resultList.unordered();
		try {

			this.resultSheet = new Sheet(this.worksheet.columnCount(), this.worksheet.rowCount());
			
			boolean useProtocol = this.template.containsKey(USE_PROTOCOL);
			boolean safeUpload = (!this.template.containsKey(FORCE_UPLOAD));
			int harvestCol = Integer.parseInt(template.get(HARVEST_ID));
			int dateCol = Integer.parseInt(template.get(EXTRACT_DATE));
			int loadAmt = Integer.parseInt(template.get(EXTRACT_AMOUNT));
			String defaultUnit = template.get(DEFAULT_UNIT);
			int labelCol = Integer.parseInt(template.get(EXTRACT_LABEL));
			int notesCol = Integer.parseInt(template.get(EXTRACT_NOTES));
	//		int libCol = Integer.parseInt(template.get(DESTINATION));
	//		int locCol = Integer.parseInt(template.get(DESTINATION_LOC));
	//		String colID = template.get(STATIC_DESTINATION);
	//		boolean wksDest = (libCol > -1);
			int cellMassCol = Integer.parseInt(template.get(HARVEST_CELL_MASS));
			String cellMassUnit = this.template.get(HARVEST_CELL_UNIT);

			String staticProject = (String) this.template.get(STATIC_PROJECT);
			int projectCol = Integer.parseInt((String) this.template
					.get(PROJECT_COL));
			boolean useProjectCol = (projectCol > -1);

			int protCol = -1, solvCol = -1, typeCol = -1;
			boolean wksProtocol = false;
			ExtractProtocol myProtocol = null;
			String protocolName = "";

			if (useProtocol) {
				protCol = Integer.parseInt(template.get(EXTRACT_PROTOCOL));
				wksProtocol = (protCol > -1);
				protocolName = template.get(STATIC_PROTOCOL);
				if (!wksProtocol) {
					try {
						myProtocol = SQLExtractProtocol.load(this.myData, protocolName);
					} catch (DataException e) {
						this.messages.append("<P ALIGN='CENTER'><B><FONT COLOR='red'>ERROR:</FONT>" + e.getMessage() + "</B></P>");
						e.printStackTrace();			
					}
				}
			} else {
				solvCol = Integer.parseInt(template.get(EXTRACT_SOLVENT));
				typeCol = Integer.parseInt(template.get(EXTRACT_TYPE));
			}

			Date myDate = new Date();
			String bulkLoadNote = String.format(
					"\nCreated via bulk load by user: %s\n %s", this.myData.getUser().getUserID(), myDate.toString());

			while (rowIter.hasNext() && this.working) {
				Integer row = (Integer) rowIter.next();
				if (this.worksheet.gotoRow(row.intValue())) {
					int myLen = this.worksheet.rowSize();
					HtmlList currResults = new HtmlList();
					currResults.unordered();
					resultSheet.gotoRow(row.intValue());
					DOROW: try {
						this.resultSheet.addCell(this.worksheet.getStringValue(harvestCol));
						Harvest myHarv = SQLHarvest.load(this.myData, this.worksheet.getStringValue(harvestCol));
						if (myHarv.first() && dateCol < this.worksheet.rowSize()
								&& loadAmt < this.worksheet.rowSize()) {
							SheetValue thisDate = this.worksheet.getValue(dateCol);
							SheetValue amount = this.worksheet.getValue(loadAmt);
							if ( thisDate == null || amount == null ) {
								currResults.addItem(SKIP_TAG + "No extract information.");
								this.resultSheet.addCell("");
								this.resultSheet.addCell("SKIPPED: No extract information.");
								break DOROW;
							}
							if (cellMassCol > -1) {
								BigDecimal cellMass = BaseForm.parseAmount(this.worksheet.getStringValue(cellMassCol), cellMassUnit);
								if (cellMass.compareTo(BigDecimal.ZERO) > 0 ) {
									myHarv.setCellMass(cellMass);
									currResults.addItem(SUCCESS_TAG + "Added cell mass information.");
								}
							}

							Material test = myHarv.getExtract();
							StringBuffer resultNotes = new StringBuffer();
							String extraID = null;
							if (test != null && test.first() ) {
								if (safeUpload) {
									currResults.addItem(FOUND_TAG + "Extract information exists.");
									currResults.addItem(SKIP_TAG + "Safe upload enabled.");
									this.resultSheet.addCell(test.getID());
									this.resultSheet.addCell("SKIPPED: Existing extract (SAFE UPLOAD).");
									break DOROW;
								} else {
									currResults.addItem(FOUND_TAG
											+ "An extract was found! Potential duplicate! Material ID:"
											+ test.getID());
									resultNotes.append("FOUND: An extracts exists for this harvest. (ID following in next column).");
									extraID = test.getID();
								}
							}

							Material extract = myHarv.createExtract();
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
							if ( thisDate.isDate() ) {
								extract.setDate(thisDate.getDate());
							} else {
								extract.setDate(thisDate.toString());
							}
							if (useProtocol) {
								if (wksProtocol && protCol < myLen ) {
									String aProtocol = this.worksheet.getStringValue(protCol);
									if (!protocolName.equals(aProtocol)) {
										protocolName = aProtocol;
										myProtocol = SQLExtractProtocol.load(this.myData, protocolName);
									}
								}
								extract.setProtocol(myProtocol);
							} else {
								if (solvCol > -1 && solvCol < myLen)
									extract.setExtractSolvent(this.worksheet.getStringValue(solvCol));
								if (typeCol > -1 && typeCol < myLen)
									extract.setExtractType(this.worksheet.getStringValue(typeCol));
							}
							String myLabel = "";
							if (labelCol > -1 && labelCol < myLen) {
								myLabel = this.worksheet.getStringValue(labelCol);
							} else {
								Inoc anInoc = myHarv.getInoculations();
								if (anInoc.first()) {
									myLabel = myHarv.getInoculations().getStrain().getID();
								} else
									myLabel = "ORPHAN";
							}
							extract.setLabel(myLabel);
	/*
							if (wksDest)
								colID = this.worksheet.getStringValue(libCol);
							extract.setCollectionID(colID);
							if ( locCol > -1 && locCol < myLen ) {
								extract.setLocation(this.worksheet.getStringValue(locCol));
							}
	*/
							if (notesCol > -1 && notesCol < myLen) {
								extract.setNotes(this.worksheet.getStringValue(notesCol));
								extract.addNotes(bulkLoadNote);
							} else {
								extract.setNotes(bulkLoadNote);
							}
	//						extract.setBaseUnit(defaultUnit);
							if (useProjectCol)
								extract.setProjectID(this.worksheet.getStringValue(projectCol));
							else
								extract.setProjectID(staticProject);
							SampleAccount myAcct = extract.getAccount();
							if (myAcct.addTransaction()) {
								myAcct.depositAmount(amount.toString(), defaultUnit);
								myAcct.setTransactionReference(myHarv);
								if ( thisDate.isDate() ) {
									myAcct.setDate(thisDate.getDate());
								} else {
									myAcct.setDate(thisDate.toString());
								}
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
		} catch (Exception e) {
			this.messages.append("<P ALIGN='CENTER'><B><FONT COLOR='red'>ERROR:</FONT>" + e.getMessage() + "</B></P>");
			e.printStackTrace();
			this.working = false;
		}
		this.resultSheet.insertRow(0);
		this.resultSheet.addCell("Harvest ID");
		this.resultSheet.addCell("Extract ID");
		this.resultSheet.addCell("Status");
		this.messages.append(resultList.toString());
		this.finishJob();
	}

	public String[] getTemplateKeys() {
		return templateKeys;
	}
}
