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
import edu.uic.orjala.cyanos.SampleCollection;
import edu.uic.orjala.cyanos.User;
import edu.uic.orjala.cyanos.sql.SQLSample;
import edu.uic.orjala.cyanos.sql.SQLSampleCollection;
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
public class SampleMoveUpload extends UploadForm {

	private static final String HEADER = "header";
	private static final String DEST_COLLECTION = "destID";
	private static final String DEST_LOCATION = "destLoc";
	private static final String COLLECTION_FROM_DB = "colFromDB";
	private static final String LOAD_TYPE = "loadType";
	private static final String SOURCE_COLLECTION = "sourceCollection";
	private static final String SOURCE_LOCATION = "sourceLocation";
	private static final String SAMPLE_ID = "sampleID";

	public final static String TITLE = "Move Samples";
	
	public final static String[] templateKeys = { HEADER, DEST_COLLECTION, DEST_LOCATION, COLLECTION_FROM_DB, 
		 LOAD_TYPE, SOURCE_COLLECTION, SOURCE_LOCATION, SAMPLE_ID};
	private final static String[] templateHeader = {"Sample ID","Destination ID","Location",};
	private final static String[] templateType = {"Required", "Required", "Required"};

	/**
	 * @param servlet
	 * @param aSheet
	 * @throws SQLException 
	 * @throws DataException 
	 */
	public SampleMoveUpload(CyanosWrapper aWrapper, Sheet aSheet) throws DataException {
		super(aWrapper, aSheet);
		this.template = this.buildTemplate(templateKeys);
		this.hasHeaderRow = this.template.containsKey(HEADER);
		this.accessRole = User.SAMPLE_ROLE;
		this.permission = Role.WRITE;
	}
	
	public String title() {
		return TITLE;
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
		List rowNum = this.rowList();
		this.done = 0;
		this.todos = rowNum.size();
		this.working = true;
		// Setup the row iterator.
		ListIterator rowIter = rowNum.listIterator();
		HtmlList resultList = new HtmlList();
		resultList.unordered();

		/*
		 *	"header", "destID", "destLoc", "colFromDB", "sampleID",
		 *  FUTURE -> "loadType", "sourceCollection", "sourceLocation"
		 */
		int sampleIDCol = Integer.parseInt(template.get(SAMPLE_ID));
		int destIDCol = Integer.parseInt(this.template.get(DEST_COLLECTION));
		int destLocCol = Integer.parseInt(this.template.get(DEST_LOCATION));

		boolean staticID = false;
		String destID = "";
		if ( destIDCol == -1 ) {
			staticID = true;
			destID = this.template.get(COLLECTION_FROM_DB);
		}

		while (rowIter.hasNext() && this.working) {
			Integer row = (Integer)rowIter.next();
			if ( this.worksheet.gotoRow(row.intValue()) ) {
				HtmlList currResults = new HtmlList();
				currResults.unordered();

				try {
					if ( ! staticID )
						destID = this.worksheet.getValue(destIDCol);
					Sample aSample = new SQLSample(this.myData, this.worksheet.getValue(sampleIDCol));

					String myLoc = null;
					if ( this.worksheet.rowSize() > destLocCol ) myLoc = this.worksheet.getValue(destLocCol);
					if ( myLoc == null ) myLoc = "";

					if ( aSample.first() && (! destID.equals("")) ) {
						aSample.setCollectionID(destID);
						if ( ! myLoc.equals("") )
							aSample.setLocation(myLoc);
						else 
							aSample.setLocation(0, 0);
						currResults.addItem(SUCCESS_TAG + "Information updated.");
					} else {
						currResults.addItem(FAILED_TAG + "Sample not found.");
					}
				} catch (DataException e) {
					currResults.addItem("<FONT COLOR='red'><B>ERROR:</B></FONT> " + e.getMessage());
					e.printStackTrace();
				}
				resultList.addItem(String.format("Row #:%d %s", row, currResults.toString()));
			}
			this.done++;
		}
		output.append(resultList.toString());
		if ( ! this.working ) { output.append("<P ALIGN='CENTER'><B>EXECUTION HALTED</B> Upload incomplete!</P>"); }
		this.working = false;
		this.resultOutput = output.toString();
	}

	public String templateForm() {
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
		assayIDPop.addItemWithLabel("-1", "Use Collection ->");
		for ( int i = 0; i < headerList.size(); i++ ) {
			String index = String.valueOf(i);
			String value = (String)headerList.get(i);
			ssColPop.addItemWithLabel(index, value);
			optionalPop.addItemWithLabel(index, value);
			assayIDPop.addItemWithLabel(index, value);
		}

		fullRow.addItem(this.simpleTemplateRow("Sample ID:", SAMPLE_ID, ssColPop));		
		
		assayIDPop.setName(DEST_COLLECTION);
		if ( template.containsKey(DEST_COLLECTION)) {
			assayIDPop.setDefault((String)template.get(DEST_COLLECTION));
		}
		String colPop = "";
		try {
			SampleCollection myCols = SQLSampleCollection.sampleCollections(this.myData, SQLSampleCollection.ID_COLUMN, SQLSampleCollection.ASCENDING_SORT);
			myCols.beforeFirst();
			Popup colList = new Popup();
			while ( myCols.next() ) {
				colList.addItemWithLabel(myCols.getID(), myCols.getName());
			}
			colList.setName(COLLECTION_FROM_DB);
			colPop = colList.toString();
		} catch (DataException e) {
			colPop = "<B><FONT COLOR='red'>ERROR:</FONT>" + e.getMessage() + "</B>";
		}
		
		myCell = new TableCell("Destination ID:");
		myCell.addItem(assayIDPop.toString() + colPop );
		fullRow.addItem(myCell);
		
		fullRow.addItem(this.simpleTemplateRow("Destination Location:", DEST_LOCATION, ssColPop));

		Table formTable = new Table(fullRow);
		formTable.setAttribute("WIDTH", "85%");
		
		return formTable.toString();
	}

	@Deprecated
	public UploadModule makeNew(CyanosWrapper aWrapper, Sheet aSheet) throws DataException {
		return new SampleMoveUpload(aWrapper, aSheet);
	}
	
	public String[] getTemplateKeys() {
		return templateKeys;
	}
}
