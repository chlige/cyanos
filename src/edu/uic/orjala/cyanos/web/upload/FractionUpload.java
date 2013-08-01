/**
 * 
 */
package edu.uic.orjala.cyanos.web.upload;

import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.List;
import java.util.ListIterator;

import javax.servlet.http.HttpServletRequest;

import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.Material;
import edu.uic.orjala.cyanos.Role;
import edu.uic.orjala.cyanos.Separation;
import edu.uic.orjala.cyanos.Separation.SeparationTemplate;
import edu.uic.orjala.cyanos.User;
import edu.uic.orjala.cyanos.sql.SQLMaterial;
import edu.uic.orjala.cyanos.sql.SQLSeparation;
import edu.uic.orjala.cyanos.sql.SQLSeparationTemplate;
import edu.uic.orjala.cyanos.web.UploadForm;
import edu.uic.orjala.cyanos.web.html.HtmlList;

/**
 * @author George Chlipala
 *
 */
public class FractionUpload extends UploadForm {

	public static final String PROTOCOL = "fraction upload";

	public final static String SAMPLE_ID_KEY = "sampleID";
	public static final String FR_NUMBER_KEY = "frNumber";
	public static final String AMOUNT_KEY = "amount";
	public static final String DEFAULT_UNIT_KEY = "defaultUnit";
//	public static final String PROTOCOL_KEY = "fractionProtocol";
	public static final String DATE_KEY = "date";
	public static final String LABEL_FORMAT_KEY = "frLabelFormat";
	public static final String LABEL_KEY = "frLabel";
	public static final String NOTES_KEY = "frNotes";
	public static final String METHOD_PARAM = "sepMethod";
	public static final String METHOD_STATIONARY = "sepSPhase";
	public static final String METHOD_MOBILE = "sepMPhase";
//	public static final String DEST_KEY = "destCol";
//	public static final String DEST_LOC = "destLoc";

	public final static String[] templateKeys = { PARAM_HEADER, SAMPLE_ID_KEY, FR_NUMBER_KEY, AMOUNT_KEY, DEFAULT_UNIT_KEY, DATE_KEY, 
		// DEST_KEY, DEST_LOC, 
		LABEL_FORMAT_KEY, LABEL_KEY, NOTES_KEY,
		// PROTOCOL_KEY, 
		METHOD_PARAM, METHOD_STATIONARY, METHOD_MOBILE };
	
	public final static String TITLE = "Fraction Data";

	private final static String[] templateHeader = {"Material ID","Fraction Number","Amount", "Label", "Notes"};
	private final static String[] templateType = {"Required<BR>(for sources only)", "Required<BR/>A numbher for a fraction<i>or</i><br>S = Source material", 
		"Required<br><i>Only absolute values</i>", 
		"Optional<br>(Ignored for source materials)", "Optional<br>(Ignored for source materials)"};

	// N in the fraction number would cause the uploader to generate a new separation record.  Need to find a good way of documenting that and detailing on the upload page.
	
	public static final String JSP_FORM = "/upload/forms/fraction.jsp";

	public FractionUpload(HttpServletRequest req) {
		super(req);
		this.accessRole = User.SAMPLE_ROLE;
		this.permission = Role.CREATE;
	}

	public String worksheetTemplate() {
		StringBuffer template = new StringBuffer(this.worksheetTemplate(templateHeader, templateType));
		template.append("<P ALIGN='CENTER'><B>NOTE:</B> A spreadsheet must contain <B><I>only one</I></B> separation!</P>");
		return template.toString();
	}

	public String title() {
		return TITLE;
	}

	public void run() {
		if ( this.working ) return;
		StringBuffer output = new StringBuffer();
		List<Integer> rowNum = this.rowList();
		// Setup the row iterator.
		this.todos = rowNum.size();
		this.done = 0;
		this.working = true;
		Savepoint mySave = null;
		
		try {
			mySave = this.myData.setSavepoint("fraction_upload");
		} catch (SQLException e) {
			output.append("<P ALIGN='CENTER'><B><FONT COLOR='red'>ERROR:</FONT>" + e.getMessage() + "</B></P>");
			e.printStackTrace();
			this.working = false;
			this.resultOutput = output.toString();
		}

		ListIterator<Integer> rowIter = rowNum.listIterator();
		HtmlList resultList = new HtmlList();
		resultList.unordered();

		String sepDate = this.template.get(DATE_KEY);		
//		if ( sepDate == null || sepDate.length() < 1 ) {
		if ( sepDate == null ) {
			output.append("<P ALIGN='CENTER'><B><FONT COLOR='red'>ERROR:</FONT> Date not valid!</B></P>");
			this.working = false;
		} else {
			try {
				int materialIDCol = Integer.parseInt(this.template.get(SAMPLE_ID_KEY));
				int frNoCol = Integer.parseInt(this.template.get(FR_NUMBER_KEY));
				int amtCol = Integer.parseInt(this.template.get(AMOUNT_KEY));
				String defaultUnit = this.template.get(DEFAULT_UNIT_KEY);

				int frLabelCol = Integer.parseInt(this.template.get(LABEL_KEY));
				int frNotesCol = Integer.parseInt(this.template.get(NOTES_KEY));
				int frLabelFormat = Integer.parseInt(this.template.get(LABEL_FORMAT_KEY));
//				int destCol = Integer.parseInt(this.template.get(DEST_KEY));
//				int destLocCol = Integer.parseInt(this.template.get(DEST_LOC));
				
				boolean useLabel = ( frLabelCol > -1 );
				boolean useNotes = ( frNotesCol > -1 );

				Separation mySep;
//				SeparationProtocol aTemplate = null;

//				if ( ! template.get(PROTOCOL_KEY).equals("") ) {
//					aTemplate = SQLSeparationProtocol.load(this.myData, template.get(PROTOCOL_KEY));
//				}
				
				String method = template.get(METHOD_PARAM);
				String mphase = template.get(METHOD_MOBILE);
				String sphase = template.get(METHOD_STATIONARY);

				String txnNote = "Separation loaded by: " + this.getSQLDataSource().getUser().getUserID();
				String frNote = "Fraction loaded by: " + this.getSQLDataSource().getUser().getUserID();
	//			String frCollection = "frLoad-" + this.myUser.getUserID();

				mySep = SQLSeparation.create(this.myData);
//				if ( aTemplate != null ) {
//					mySep.setProtocol(aTemplate);
//				}
				
				mySep.setMethod(method);
				mySep.setMobilePhase(mphase);
				mySep.setStationaryPhase(sphase);
				
				mySep.setDate(sepDate);
				mySep.setNotes(txnNote);


				while ( rowIter.hasNext() && this.working ) {
					Integer row = (Integer)rowIter.next();
					if ( this.worksheet.gotoRow(row.intValue()) ) {
						HtmlList currResults = new HtmlList();
						currResults.unordered();
						String frNumber = this.worksheet.getStringValue(frNoCol);
						String amount = this.worksheet.getStringValue(amtCol);

						if ( frNumber.matches("^[sS].*") ) {
							currResults.addItem(NOTICE_TAG + "Setting source information.");
							String materialID = this.worksheet.getStringValue(materialIDCol);
							Material aSource = SQLMaterial.load(myData, materialID);
							if ( aSource.first() ) {
								currResults.addItem(FOUND_TAG + "Material found.");
								if ( mySep.addSource(aSource, SQLMaterial.parseAmount(amount, defaultUnit)) ) {
									currResults.addItem(SUCCESS_TAG + "Connected source to separation record.");
									/*
									SampleAccount myAcct = aSource.getAccount();
									if ( myAcct.addTransaction() ) {
										myAcct.setDate(sepDate);
										myAcct.setTransactionReference(mySep);
										myAcct.withdrawAmount(amount, defaultUnit);
										myAcct.setNotes(txnNote);
										myAcct.updateTransaction();
										currResults.addItem(SUCCESS_TAG + "Updated sample information.");
									} else {
										currResults.addItem(ERROR_TAG + "Failed to add transaction to source sample.");
									}
									*/
								} else {
									currResults.addItem(ERROR_TAG + "Failed to link source material.");
								}
							} else {
								currResults.addItem(ERROR_TAG + "Failed to find source material.");
							}
						} else if ( frNumber.length() > 0 ) {
							int frInt = Integer.parseInt(frNumber);
							Material fraction = mySep.makeFraction(frInt);
							if ( fraction != null ) {
								currResults.addItem(SUCCESS_TAG + "Created a new fraction.");
								fraction.setManualRefresh();
								fraction.setDate(sepDate);
								if ( useNotes ) 
									fraction.setNotes(this.worksheet.getValue(frNotesCol) + "\n" + frNote);
								else 
									fraction.setNotes(frNote);
								if ( useLabel ) {
									fraction.setLabel(this.worksheet.getStringValue(frLabelCol));
								} 
		//						fraction.setBaseUnit(defaultUnit);
		/*						if ( destCol > -1 ) {
									fraction.setCollectionID(this.worksheet.getStringValue(destCol));
									if ( destLocCol > -1 )
										fraction.setLocation(this.worksheet.getStringValue(destLocCol));
								} else {
									fraction.setCollectionID(frCollection);
								}
		*/
								fraction.setAmount(SQLMaterial.parseAmount(amount, defaultUnit));
								fraction.refresh();
								fraction.setAutoRefresh();
	/*
								SampleAccount myAcct = fraction.getAccount();
								if ( myAcct.addTransaction() ) {
									myAcct.setDate(sepDate);
									myAcct.setTransactionReference(mySep);
									myAcct.depositAmount(amount, defaultUnit);
									myAcct.setNotes(txnNote);
									myAcct.updateTransaction();
									currResults.addItem(SUCCESS_TAG + "Updated sample information.");
								} else {
									currResults.addItem(ERROR_TAG + "Failed to add transaction to fraction.");
								}
	*/
							} else {
								currResults.addItem(ERROR_TAG + "Failed to create fraction material.");
							}
						} else if ( frNumber.matches("^[nN].*") ) {
							if ( mySep.getSources().first() ) {
								this.setFractionNames(mySep, useLabel, frLabelFormat);
								output.append("<P ALIGN='CENTER'><A HREF='../separation?id=" + mySep.getID() + "'>New Separation</A></P>");	
							}
							if ( rowIter.hasNext() ) {
								mySep = SQLSeparation.create(this.myData);
//								if ( aTemplate != null ) {
//									mySep.setProtocol(aTemplate);
//								}
								mySep.setMethod(method);
								mySep.setMobilePhase(mphase);
								mySep.setStationaryPhase(sphase);
								
								mySep.setDate(sepDate);
								mySep.setNotes(txnNote);
							}
						}
						resultList.addItem(String.format("Row #:%d %s", row, currResults.toString()));
					} else {
						resultList.addItem(String.format("Row #:%d MISSING", row));
					}
					this.done++;
				}
				if ( mySep.getSources().first() ) {
					this.setFractionNames(mySep, useLabel, frLabelFormat);
					output.append("<P ALIGN='CENTER'><A HREF='../separation?id=" + mySep.getID() + "'>New Separation</A></P>");	
				}
			} catch (Exception e) {
				output.append("<P ALIGN='CENTER'><B><FONT COLOR='red'>ERROR:</FONT>" + e.getMessage() + "</B></P>");
				e.printStackTrace();
				this.working = false;
			}
		}
		try {
			if ( this.working ) { 
				this.myData.commit(); 
				this.myData.releaseSavepoint(mySave); 
				output.append("<P ALIGN='CENTER'><B>EXECUTION COMPLETE</B> CHANGES COMMITTED.</P>"); 
			} else { 
				this.myData.rollback(mySave); 
				output.append("<P ALIGN='CENTER'><B>EXECUTION HALTED</B> Upload incomplete!</P>"); 
			}
		} catch (SQLException e) {
			output.append("<P ALIGN='CENTER'><B><FONT COLOR='red'>ERROR:</FONT>" + e.getMessage() + "</B></P>");
			e.printStackTrace();			
		}
		output.append(resultList.toString());
		this.working = false;
		this.resultOutput = output.toString();
	}
	
	private void setFractionNames(Separation mySep, boolean useLabel, int frLabelFormat) throws DataException {
		mySep.beforeFirstFraction();
		Material source = mySep.getSources();

		if ( source.first() ) {
			String cultureID = source.getCultureID();
			String sourceName = source.getLabel();
			while ( mySep.nextFraction() ) {
				HtmlList currResults = new HtmlList();
				currResults.unordered();
				Material aFrac = mySep.getCurrentFraction();
				if ( ! useLabel ) {
					int frNumber = mySep.getCurrentFractionNumber();
					String sampleLabel;
					if ( frLabelFormat == 2) {
						sampleLabel = String.format("%s.%d", sourceName, frNumber);
					} else {
						sampleLabel = String.format("%s FR%d", cultureID, frNumber);
					}
					aFrac.setLabel(sampleLabel);
				}
				aFrac.setCultureID(cultureID);
			}
		}
	}

	/*
	public String templateForm() {
		TableCell myCell;
		List<String> headerList = this.getHeaderList(template.containsKey(HEADER_KEY));

		if ( template.containsKey("header") ) {
			myCell = new TableCell("<INPUT TYPE='CHECKBOX' NAME='header' VALUE='true' onClick='this.form.submit()' CHECKED /> Spreadsheet has a header row.");					

		} else {
			myCell = new TableCell("<INPUT TYPE='CHECKBOX' NAME='header' VALUE='true' onClick='this.form.submit()' /> Spreadsheet has a header row.");
		}
		myCell.setAttribute("COLSPAN", "4");
		myCell.setAttribute("ALIGN", "CENTER");
		TableRow fullRow = new TableRow(myCell);

		Popup ssColPop = new Popup();
		Popup optionalPop = new Popup();
		Popup staticPop = new Popup();
		optionalPop.addItemWithLabel("-1", "SKIP ITEM");
		staticPop.addItemWithLabel("-1", "Use Format ->");

		for ( int i = 0; i < headerList.size(); i++ ) {
			String index = String.valueOf(i);
			String value = (String)headerList.get(i);
			ssColPop.addItemWithLabel(index, value);
			optionalPop.addItemWithLabel(index, value);
			staticPop.addItemWithLabel(index, value);
		}

		/*
		 * "sampleID", "frNumber", "amount", "defaultUnit", "fractionProtocol"
		 */
/*
		Popup formatPop = new Popup();
		formatPop.addItemWithLabel("1", "CultureID FR#");
		formatPop.addItemWithLabel("2", "SourceLabel.#");

		fullRow.addItem(this.simpleTemplateRow("Sample ID:", SAMPLE_ID_KEY, ssColPop));

		fullRow.addItem(this.simpleTemplateRow("FR Number:", FR_NUMBER_KEY, ssColPop));	
		fullRow.addItem(this.templateRowWithUnit("Amount:", AMOUNT_KEY, DEFAULT_UNIT_KEY, ssColPop));
		fullRow.addItem(this.simpleTemplateRow("Destination Collection ID:", DEST_KEY, optionalPop));
		fullRow.addItem(this.simpleTemplateRow("Destination Location:", DEST_LOC, optionalPop));
		fullRow.addItem(this.protocolTemplateRow("Fractionation Protocol:", PROTOCOL_KEY, PROTOCOL));
		fullRow.addItem(this.makeFormDateRow("Date:", DATE_KEY, "upload", this.template.get(DATE_KEY)));
		myCell = new TableCell("Label:");
		if ( this.template.containsKey(LABEL_KEY) )
			staticPop.setDefault(this.template.get(LABEL_KEY));
		if ( this.template.containsKey(LABEL_FORMAT_KEY) )
			formatPop.setDefault(this.template.get(LABEL_FORMAT_KEY));
		staticPop.setName(LABEL_KEY);
		formatPop.setName(LABEL_FORMAT_KEY);
		myCell.addItem(staticPop.toString() + formatPop.toString() );
		fullRow.addItem(myCell);
		fullRow.addItem(this.simpleTemplateRow("Notes:", NOTES_KEY, optionalPop));

		Table formTable = new Table(fullRow);
		formTable.setAttribute("WIDTH", "85%");

		return formTable.toString();

	}
	*/

	public String[] getTemplateKeys() {
		return templateKeys;
	}

	@Override
	public String jspForm() {
		return JSP_FORM;
	}
	
	public String getTemplateType() {
		return PROTOCOL;
	}

}
