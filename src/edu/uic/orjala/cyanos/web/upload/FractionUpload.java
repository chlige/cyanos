/**
 * 
 */
package edu.uic.orjala.cyanos.web.upload;

import java.sql.SQLException;
import java.util.List;
import java.util.ListIterator;

import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.Role;
import edu.uic.orjala.cyanos.Sample;
import edu.uic.orjala.cyanos.SampleAccount;
import edu.uic.orjala.cyanos.Separation;
import edu.uic.orjala.cyanos.User;
import edu.uic.orjala.cyanos.Separation.SeparationProtocol;
import edu.uic.orjala.cyanos.sql.SQLSample;
import edu.uic.orjala.cyanos.sql.SQLSeparation;
import edu.uic.orjala.cyanos.sql.SQLSeparationProtocol;
import edu.uic.orjala.cyanos.web.CyanosWrapper;
import edu.uic.orjala.cyanos.web.Sheet;
import edu.uic.orjala.cyanos.web.UploadForm;
import edu.uic.orjala.cyanos.web.UploadModule;
import edu.uic.orjala.cyanos.web.html.HtmlList;
import edu.uic.orjala.cyanos.web.html.Popup;
import edu.uic.orjala.cyanos.web.html.Table;
import edu.uic.orjala.cyanos.web.html.TableCell;
import edu.uic.orjala.cyanos.web.html.TableRow;

/**
 * @author George Chlipala
 *
 */
public class FractionUpload extends UploadForm {

	public static final String PROTOCOL = "fraction protocol";

	private final static String HEADER_KEY = "header";
	private final static String SAMPLE_ID_KEY = "sampleID";
	private static final String FR_NUMBER_KEY = "frNumber";
	private static final String AMOUNT_KEY = "amount";
	private static final String DEFAULT_UNIT_KEY = "defaultUnit";
	private static final String PROTOCOL_KEY = "fractionProtocol";
	private static final String DATE_KEY = "date";
	private static final String LABEL_FORMAT_KEY = "frLabelFormat";
	private static final String LABEL_KEY = "frLabel";
	private static final String NOTES_KEY = "frNotes";
	private static final String DEST_KEY = "destCol";
	private static final String DEST_LOC = "destLoc";
	
	public final static String[] templateKeys = { HEADER_KEY, SAMPLE_ID_KEY, FR_NUMBER_KEY, AMOUNT_KEY, DEFAULT_UNIT_KEY, PROTOCOL_KEY, DATE_KEY, DEST_KEY, DEST_LOC, LABEL_FORMAT_KEY, LABEL_KEY, NOTES_KEY };
	public final static String TITLE = "Fraction Data";
	
	private final static String[] templateHeader = {"Sample ID","Fraction Number","Amount", "Destination", "Label", "Notes"};
	private final static String[] templateType = {"Required", "Required<BR/>S = Source", "Required", "Optional", "Optional", "Optional"};
	
	/**
	 * @param aSheet
	 * @param aTemplate
	 * @throws SQLException 
	 * @throws DataException 
	 */
	public FractionUpload(CyanosWrapper aWrapper, Sheet aSheet) throws DataException {
		super(aWrapper, aSheet);
		this.template = this.buildTemplate(templateKeys);
		this.hasHeaderRow = this.template.containsKey(HEADER_KEY);
		this.accessRole = User.SAMPLE_ROLE;
		this.permission = Role.CREATE;
	}

	public String worksheetTemplate() {
		return this.worksheetTemplate(templateHeader, templateType);
	}

	public String title() {
		return TITLE;
	}
	
	public void run() {
		if ( this.working ) return;
		StringBuffer output = new StringBuffer();
		List rowNum = this.rowList();
		// Setup the row iterator.
		this.todos = rowNum.size();
		this.done = 0;
		this.working = true;
		
		ListIterator rowIter = rowNum.listIterator();
		HtmlList resultList = new HtmlList();
		resultList.unordered();
	
		int sampleIDCol = Integer.parseInt(this.template.get(SAMPLE_ID_KEY));
		int frNoCol = Integer.parseInt(this.template.get(FR_NUMBER_KEY));
		int amtCol = Integer.parseInt(this.template.get(AMOUNT_KEY));
		String defaultUnit = this.template.get(DEFAULT_UNIT_KEY);
		String sepDate = this.template.get(DATE_KEY);
		int frLabelCol = Integer.parseInt(this.template.get(LABEL_KEY));
		int frNotesCol = Integer.parseInt(this.template.get(NOTES_KEY));
		int frLabelFormat = Integer.parseInt(this.template.get(LABEL_FORMAT_KEY));
		int destCol = Integer.parseInt(this.template.get(DEST_KEY));
		int destLocCol = Integer.parseInt(this.template.get(DEST_LOC));
		boolean useLabel = ( frLabelCol > -1 );
		boolean useNotes = ( frNotesCol > -1 );

		try {
			Separation mySep;
			SeparationProtocol aTemplate = null;
			
			if ( ! template.get(PROTOCOL_KEY).equals("") ) {
				aTemplate = new SQLSeparationProtocol(this.myData, template.get(PROTOCOL_KEY));
			}

			String txnNote = "Separation loaded by: " + this.myUser.getUserID();
			String frNote = "Fraction loaded by: " + this.myUser.getUserID();
			String frCollection = "frLoad-" + this.myUser.getUserID();

			mySep = SQLSeparation.create(this.myData);
			if ( aTemplate != null ) {
				mySep.setProtocol(aTemplate);
			}
			mySep.setDate(sepDate);
			mySep.setNotes(txnNote);

			output.append("<P ALIGN='CENTER'><A HREF='../separation?id=" + mySep.getID() + "'>New Separation</A></P>");

			while ( rowIter.hasNext() && this.working ) {
				Integer row = (Integer)rowIter.next();
				if ( this.worksheet.gotoRow(row.intValue()) ) {
					HtmlList currResults = new HtmlList();
					currResults.unordered();
					String frNumber = this.worksheet.getValue(frNoCol);
					String amount = this.worksheet.getValue(amtCol);
	
					if ( frNumber.matches("^[sS].*") ) {
						currResults.addItem(NOTICE_TAG + "Setting source information.");
						String sampleID = this.worksheet.getValue(sampleIDCol);
						Sample aSource = new SQLSample(this.myData, sampleID);
						if ( aSource.first() ) {
							currResults.addItem(FOUND_TAG + "Sample found.");
							if ( mySep.addSource(aSource) ) {
								currResults.addItem(SUCCESS_TAG + "Connected source to separation record.");
								SampleAccount myAcct = aSource.getAccount();
								if ( myAcct.addTransaction() ) {
									myAcct.setDate(sepDate);
									myAcct.setTransactionReference(mySep);
									myAcct.withdrawAmount(amount, defaultUnit);
									myAcct.setNotes(txnNote);
									myAcct.updateTransaction();
									currResults.addItem(SUCCESS_TAG + "Updated sample information.");
								} else {
									currResults.addItem(ERROR_TAG + "Failed to add transation to source sample.");
								}
							} else {
								currResults.addItem(ERROR_TAG + "Failed to link source sample.");
							}
						} else {
							currResults.addItem(ERROR_TAG + "Failed to find source sample.");
						}
					} else {
						int frInt = Integer.parseInt(frNumber);
						Sample fraction = mySep.makeFraction(frInt);
						if ( fraction != null ) {
							currResults.addItem(SUCCESS_TAG + "Created a new fraction.");
							fraction.setManualRefresh();
							fraction.setDate(sepDate);
							if ( useNotes ) 
								fraction.setNotes(this.worksheet.getValue(frNotesCol) + "\n" + frNote);
							else 
								fraction.setNotes(frNote);
							if ( useLabel ) {
								fraction.setName(this.worksheet.getValue(frLabelCol));
							} 
							fraction.setBaseUnit(defaultUnit);
							if ( destCol > -1 ) {
								fraction.setCollectionID(this.worksheet.getValue(destCol));
								if ( destLocCol > -1 )
									fraction.setLocation(this.worksheet.getValue(destLocCol));
							} else {
								fraction.setCollectionID(frCollection);

							}
							fraction.refresh();
							fraction.setAutoRefresh();
							SampleAccount myAcct = fraction.getAccount();
							if ( myAcct.addTransaction() ) {
								myAcct.setDate(sepDate);
								myAcct.setTransactionReference(mySep);
								myAcct.depositAmount(amount, defaultUnit);
								myAcct.setNotes(txnNote);
								myAcct.updateTransaction();
								currResults.addItem(SUCCESS_TAG + "Updated sample information.");
							} else {
								currResults.addItem(ERROR_TAG + "Failed to add transation to fraction.");
							}
						} else {
							currResults.addItem(ERROR_TAG + "Failed to create fraction sample.");
						}
					}
					resultList.addItem(String.format("Row #:%d %s", row, currResults.toString()));
				} else {
					resultList.addItem(String.format("Row #:%d MISSING", row));
				}
				this.done++;
			} 
			mySep.beforeFirstFraction();
			Sample source = mySep.getSources();

			if ( source.first() ) {
				String cultureID = source.getCultureID();
				String sourceName = source.getName();
				while ( mySep.nextFraction() ) {
					HtmlList currResults = new HtmlList();
					currResults.unordered();
					Sample aFrac = mySep.getCurrentFraction();
					if ( ! useLabel ) {
						int frNumber = mySep.getCurrentFractionNumber();
						String sampleLabel;
						if ( frLabelFormat == 2) {
							sampleLabel = String.format("%s.%d", sourceName, frNumber);
						} else {
							sampleLabel = String.format("%s FR%d", cultureID, frNumber);
						}
						aFrac.setName(sampleLabel);
					}
					aFrac.setCultureID(cultureID);
				}
			}
		} catch (DataException e) {
			output.append("<P ALIGN='CENTER'><B><FONT COLOR='red'>ERROR:</FONT>" + e.getMessage() + "</B></P>");
			e.printStackTrace();
		}
		
		output.append(resultList.toString());
		if ( ! this.working ) { output.append("<P ALIGN='CENTER'><B>EXECUTION HALTED</B> Upload incomplete!</P>"); }
		this.working = false;
		this.resultOutput = output.toString();
	}
	
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

	@Deprecated
	public UploadModule makeNew(CyanosWrapper aServlet, Sheet aSheet) throws DataException {
		return new FractionUpload(aServlet, aSheet);
	}

	public String[] getTemplateKeys() {
		return templateKeys;
	}
	
}
