/**
 * 
 */
package edu.uic.orjala.cyanos.web.upload;

import java.sql.SQLException;
import java.util.ListIterator;

import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.Sample;
import edu.uic.orjala.cyanos.sql.SQLData;
import edu.uic.orjala.cyanos.sql.SQLSample;
import edu.uic.orjala.cyanos.web.html.HtmlList;

/**
 * @author George Chlipala
 *
 */
public class SampleMoveUpload extends UploadJob {

	public static final String DEST_COLLECTION = "destID";
	public static final String DEST_LOCATION = "destLoc";
	public static final String COLLECTION_FROM_DB = "colFromDB";
	public static final String LOAD_TYPE = "loadType";
	public static final String SOURCE_COLLECTION = "sourceCollection";
	public static final String SOURCE_LOCATION = "sourceLocation";
	public static final String SAMPLE_ID = "sampleID";

	public final static String TITLE = "Move Samples";

	public final static String[] templateKeys = { DEST_COLLECTION, DEST_LOCATION, COLLECTION_FROM_DB, 
		LOAD_TYPE, SOURCE_COLLECTION, SOURCE_LOCATION, SAMPLE_ID};

	/**
	 * 
	 */
	public SampleMoveUpload(SQLData data) {
		super(data);
		this.type = TITLE;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		if ( this.working ) return;
		this.done = 0;
		this.todos = this.rowList.size();
		this.working = true;
		// Setup the row iterator.
		ListIterator<Integer> rowIter = this.rowList.listIterator();
		HtmlList resultList = new HtmlList();
		resultList.unordered();

		try {
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
							destID = this.worksheet.getStringValue(destIDCol);
						Sample aSample = new SQLSample(this.myData, this.worksheet.getStringValue(sampleIDCol));

						String myLoc = null;
						if ( this.worksheet.rowSize() > destLocCol ) myLoc = this.worksheet.getStringValue(destLocCol);
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

		} catch (Exception e) {
			this.messages.append("<P ALIGN='CENTER'><B><FONT COLOR='red'>ERROR:</FONT>" + e.getMessage() + "</B></P>");
			e.printStackTrace();
			this.working = false;
		}
		this.messages.append(resultList.toString());
		this.finishJob();
	}

	/*
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
	*/

	public String[] getTemplateKeys() {
		return templateKeys;
	}
}
