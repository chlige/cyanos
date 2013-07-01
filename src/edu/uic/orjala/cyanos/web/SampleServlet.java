/**
 * 
 */
package edu.uic.orjala.cyanos.web;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.Sample;
import edu.uic.orjala.cyanos.SampleAccount;
import edu.uic.orjala.cyanos.SampleCollection;
import edu.uic.orjala.cyanos.Strain;
import edu.uic.orjala.cyanos.sql.SQLSample;
import edu.uic.orjala.cyanos.sql.SQLSampleCollection;
import edu.uic.orjala.cyanos.sql.SQLStrain;
import edu.uic.orjala.cyanos.web.forms.AssayForm;
import edu.uic.orjala.cyanos.web.forms.ProtocolForm;
import edu.uic.orjala.cyanos.web.forms.SampleForm;
import edu.uic.orjala.cyanos.web.html.Div;
import edu.uic.orjala.cyanos.web.html.Form;
import edu.uic.orjala.cyanos.web.html.Paragraph;
import edu.uic.orjala.cyanos.web.html.StyledText;

/**
 * @author George Chlipala
 *
 */
public class SampleServlet extends ServletObject {

	private static final long serialVersionUID = 9170635376958905064L;
	private static final String DATA_FILE_DIV_ID = "dataFiles";
	private static final String DATA_FILE_DIV_TITLE = "Data Files";
	private static final String DIV_ASSAY_ID = "assayInfo";
	private static final String DIV_ASSAY_TITLE = "Assay Data";
	private static final String HELP_MODULE = "sample";

	public void display(CyanosWrapper aWrap) throws Exception {

		PrintWriter out;

		String module = aWrap.getRequest().getPathInfo();	
		if ( "/export".equals(module) ) {
			aWrap.setContentType("text/plain");
			out = aWrap.getWriter();
			out.println(this.exportCollection(aWrap));
			out.flush();
			return;
		}

		if ( aWrap.hasFormValue("div") ) {
			aWrap.setContentType("text/html");
			out = aWrap.getWriter();
			String divTag = aWrap.getFormValue("div");
			if ( "/protocol".equals(module) && divTag.equals(ProtocolForm.DIV_ID) ) {
				ProtocolForm aForm = new ProtocolForm(aWrap, SampleForm.EXTRACT_PROTOCOL, SampleForm.EXTRACT_PROTOCOL_KEYS);
				Form retForm = aForm.protocolForm();
				out.print(retForm.toString());
			} else {
				Sample aSample = new SQLSample(aWrap.getSQLDataSource(), aWrap.getFormValue("id"));
				if ( aSample.first() ) {
					if ( divTag.equals(SampleForm.TXN_DIV_ID) ) {
						SampleForm aForm = new SampleForm(aWrap);
						out.print(aForm.transactionSheet(aSample));
					} else if ( divTag.equals(DATA_FILE_DIV_ID) ) {
						SampleForm aForm = new SampleForm(aWrap);
						Div aDiv = new Div(aForm.dataForm(aSample));
						aDiv.setID(SampleForm.DATA_FORM);
						out.println(aDiv.toString());
					} else if ( divTag.equals(SampleForm.DATA_FORM)) {
						SampleForm aForm = new SampleForm(aWrap);
						out.println(aForm.dataForm(aSample));
					} else if ( divTag.equals(DIV_ASSAY_ID) ) {
						AssayForm myForm = new AssayForm(aWrap);
						out.println(myForm.assayListForSample(aSample));
					}
				}
			}
			out.flush();
			return;
		}
		
		Sample aSample = null;
		if ( aWrap.hasFormValue("id") ) {
			aSample = new SQLSample(aWrap.getSQLDataSource(), aWrap.getFormValue("id"));
			out = aWrap.startHTMLDoc(String.format("Sample %s", aSample.getName()));
		} else {
			out = aWrap.startHTMLDoc("Samples", (! "/print".equals("/module")));
		}

		SampleForm aForm = new SampleForm(aWrap);
		if ( module == null ) {
			if ( aSample == null ) 
				out.println(aForm.listSamples());
			else if (aSample.first()) {
				out.println(aForm.showSample(aSample));
				out.println(aForm.loadableDiv(SampleForm.TXN_DIV_ID, SampleForm.TXN_DIV_TITLE));
				out.println(aForm.loadableDiv(DATA_FILE_DIV_ID, DATA_FILE_DIV_TITLE));
				out.println(aForm.loadableDiv(DIV_ASSAY_ID, DIV_ASSAY_TITLE));
			} else 
				out.println("<P ALIGN='CENTER'><B><FONT COLOR='red'>ERROR:</FONT> SAMPLE NOT FOUND!</B></P>");
		} else if ( module.equals("/add") ) {
			Paragraph head = new Paragraph();
			head.setAlign("CENTER");
			StyledText title = new StyledText("Add Sample Vial");
			title.setSize("+3");
			head.addItem(title);
			head.addItem("<HR WIDTH='85%'/>");
			out.println(head);
			out.println(aForm.addSample());
		} else if ( module.equals("/newCollection") ) {
			Paragraph head = new Paragraph();
			head.setAlign("CENTER");
			StyledText title = new StyledText("Add Sample Collection");
			title.setSize("+3");
			head.addItem(title);
			head.addItem("<HR WIDTH='85%'/>");
			out.println(head);
			out.println(aForm.addBox());
		} else if ( module.equals("/protocol") ) {
			Paragraph head = new Paragraph();
			head.setAlign("CENTER");
			StyledText title = new StyledText("Extract Protocols");
			title.setSize("+3");
			head.addItem(title);
			head.addItem("<HR WIDTH='85%'/>");
			out.println(head);
			
			Form protoForm = new Form(aForm.extractProtocolForm(null));
			Div tempDiv = ProtocolForm.formDiv("Load an Extraction Template", "Save as an Extraction Template");
			protoForm.addItem(tempDiv);
			out.println(protoForm.toString());
		}

		aWrap.finishHTMLDoc();

	}


	private String exportCollection(CyanosWrapper aWrap) {
		try {
			SampleCollection aCol = new SQLSampleCollection(aWrap.getSQLDataSource(), aWrap.getFormValue("col"));
			List<List> output = new ArrayList<List>();
			List<String> aRow = new ArrayList<String>();
			boolean isBox = false;
			Sample mySamples = null;
			if ( aWrap.hasFormValue("col") && (! aWrap.getFormValue("col").equals("")) ) {
				if ( aCol.first() ) 
					mySamples = aCol.getSamples();
				else
					return "ERROR: Cannot find sample collection.";
			} else if ( aWrap.hasFormValue("strain") && (! aWrap.getFormValue("strain").equals("")) ) {
				Strain aStrain = new SQLStrain(aWrap.getSQLDataSource(), aWrap.getFormValue("strain"));
				if ( aStrain.first() )
					mySamples = aStrain.getSamples();
				else 
					return "ERROR: Cannot find strain";
			}
			if ( aCol.getLength() > 0 && aCol.getWidth() > 0 ) isBox = true;
			aRow.add("Sample ID");
			aRow.add("Sample Label");
			aRow.add("Culture ID");
			aRow.add("Collection ID");
			if ( isBox ) aRow.add("Location");
			aRow.add("Date Added");
			aRow.add("Last Activity");
			aRow.add("Current Balance");
			aRow.add("Notes");
			output.add(aRow);
			if ( mySamples != null ) {
				mySamples.beforeFirst();
				while ( mySamples.next() ) {
					aRow = new ArrayList<String>();
					aRow.add(mySamples.getID());
					String name = mySamples.getName();
					String notes = mySamples.getNotes();
					if ( name != null ) 
						aRow.add(name.replaceAll("[\n\r]", " "));
					else
						aRow.add("");
					aRow.add(mySamples.getCultureID());
					aRow.add(mySamples.getCollectionID());
					if ( isBox ) 
						aRow.add(mySamples.getLocation());
					aRow.add(mySamples.getDateString());
					SampleAccount txnAcct = mySamples.getAccount();
					if ( txnAcct.last() ) {
						aRow.add(txnAcct.getDateString());
						aRow.add(BaseForm.formatAmount("%.2f %s", mySamples.accountBalance(), mySamples.getBaseUnit()));
					} else {
						aRow.add(""); aRow.add("");
					}
					if ( notes != null )
						aRow.add(notes.replaceAll("[\n\r]", " "));
					else 
						aRow.add("");
					output.add(aRow);
				}
				return this.delimOutput(output, ",");
			} else {
				return "ERROR: NO SAMPLES FOUND FOR THIS QUERY";
			}
		} catch (DataException e) {
			e.printStackTrace();
			return "ERROR: " + e.getMessage();
		}
	}
	
/*
	private String withdrawalForm(Sample aSample) {
		Paragraph head = new Paragraph();
		head.setAlign("CENTER");
		StyledText title = new StyledText("Withdraw from sample");
		title.setSize("+1");
		head.addItem(title);
		Form myForm = new Form(head);
		myForm.setAttribute("METHOD", "POST");
		myForm.setName("balance");
		myForm.addItem("<INPUT TYPE=HIDDEN NAME='id' VALUE='" + req.getParameter("id") + "'/>");
		myForm.addItem("<INPUT TYPE=HIDDEN NAME='withdrawalForm' VALUE='YES'/>");

		TableRow tableRow = new TableRow(this.makeFormDateRow("Date:", "date", "balance"));

		TableCell myCell = new TableCell("Destination:");
		Popup aPop = new Popup();
		aPop.setName("dest");
		aPop.addItemWithLabel("sample", "New Sample Vial");		
		aPop.addItemWithLabel("tie", "Link to Existing Sample");		
		aPop.addItemWithLabel("assay", "Bioassay");
		aPop.addItemWithLabel("", "Other (e.g. chemisty)");
		myCell.addItem(aPop.toString());
		if ( aWrap.hasFormValue("dest")) 
			aPop.setDefault(aWrap.getFormValue("dest"));
		tableRow.addItem(myCell);
		
		tableRow.addItem(this.makeFormTextRow("Amount:", "amount"));
		tableRow.addItem(this.makeFormTextRow("Description:", "notes"));

		myCell = new TableCell("<INPUT TYPE=SUBMIT NAME='withdrawAction' VALUE='Withdraw'/><INPUT TYPE='RESET'/><BR/><INPUT TYPE='SUBMIT' NAME='resetForm' VALUE='Return'/>");
		myCell.setAttribute("colspan","2");
		myCell.setAttribute("align","center");
		tableRow.addItem(myCell);

		Table myTable = new Table(tableRow);
		myTable.setClass("species");
		myTable.setAttribute("align", "center");
		myForm.addItem(myTable.toString());
		
		return myForm.toString();
	}

	private String depositForm(Sample aSample) {
		Paragraph head = new Paragraph();
		head.setAlign("CENTER");
		StyledText title = new StyledText("Deposit into sample");
		title.setSize("+1");
		head.addItem(title);
		Form myForm = new Form(head);
		myForm.setAttribute("METHOD", "POST");
		myForm.setName("balance");
		myForm.addItem("<INPUT TYPE=HIDDEN NAME='id' VALUE='" + req.getParameter("id") + "'/>");
		myForm.addItem("<INPUT TYPE=HIDDEN NAME='depositForm' VALUE='YES'/>");

		TableRow tableRow = new TableRow(this.makeFormDateRow("Date:", "date", "balance"));

		TableCell myCell = new TableCell("Source Harvest:");
		
		try {
			Popup aPop = new Popup();
			aPop.setName("source");
			aPop.addItemWithLabel("NONE", "-- NO SOURCE --");
			Harvest aHarv = SQLHarvest.harvestsForStrain(aWrap.getSQLDataSource(), aSample.getCultureID());
			aHarv.beforeFirst();
			SimpleDateFormat myDF = this.dateFormat();
			while (aHarv.next()) {
				aPop.addItemWithLabel(aHarv.getID(), myDF.format(aHarv.getDate()) + " " + 
						this.formatAmount("%.2f %s", aHarv.getCellMass(),"g"));
			}
			myCell.addItem(aPop.toString());
		} catch (DataException e) {
			e.printStackTrace();
			myCell.addItem("<P ALIGN='CENTER'><B><FONT COLOR='red'>SQL FAILURE:</FONT> " + e.getMessage() + "</B></P>");
		}
		tableRow.addItem(myCell);
		
		tableRow.addItem(this.makeFormTextRow("Amount:", "amount"));
		tableRow.addItem(this.makeFormTextRow("Description:", "notes"));

		myCell = new TableCell("<INPUT TYPE=SUBMIT NAME='depositAction' VALUE='Deposit'/><INPUT TYPE='RESET'/><BR/><INPUT TYPE='SUBMIT' NAME='resetForm' VALUE='Return'/>");
		myCell.setAttribute("colspan","2");
		myCell.setAttribute("align","center");
		tableRow.addItem(myCell);

		Table myTable = new Table(tableRow);
		myTable.setClass("species");
		myTable.setAttribute("align", "center");
		myForm.addItem(myTable.toString());
		
		return myForm.toString();
	}


	private String withdrawFromSample(Sample aSample) {	
		StringBuffer output = new StringBuffer();
		try {
			SampleAccount myAcct = aSample.getAccount();
			if ( myAcct.addTransaction() ) {
				myAcct.setDate(aWrap.getFormValue("date"));
				myAcct.setNotes(aWrap.getFormValue("notes"));
				myAcct.withdrawAmount(aWrap.getFormValue("amount"), aSample.getBaseUnit());
				myAcct.updateTransaction();
				output.append("<P ALIGN='CENTER'><B><FONT COLOR='green'>SUCCESS:</FONT> Transaction added</B></P>");
			} else {
				output.append("<P ALIGN='CENTER'><B><FONT COLOR='red'>ERROR:</FONT> Could not add a transaction!</B></P>");
				output.append(this.withdrawalForm(aSample));
			}
		} catch (DataException e) {
			e.printStackTrace();
			output.append("<P ALIGN='CENTER'><B><FONT COLOR='red'>SQL FAILURE:</FONT> " + e.getMessage() + "</B></P>");
		}

		return output.toString();
	}
	
	private String depositIntoSample(Sample aSample) {	
		StringBuffer output = new StringBuffer();
		try {
			SampleAccount myAcct = aSample.getAccount();
			if ( myAcct.addTransaction() ) {
				myAcct.setDate(aWrap.getFormValue("date"));
				myAcct.setNotes(aWrap.getFormValue("notes"));
				myAcct.depositAmount(aWrap.getFormValue("amount"), aSample.getBaseUnit());
				if ( ! aWrap.getFormValue("source").equals("NONE") ) {
					Harvest aHarvest = new SQLHarvest(aWrap.getSQLDataSource(), aWrap.getFormValue("source"));
					if ( aHarvest.first() ) {
						myAcct.setTransactionReference(aHarvest);
						aSample.makeExtract(aHarvest.getID());
					} else 
						output.append("<P ALIGN='CENTER'><B><FONT COLOR='red'>ERROR:</FONT> Could not find source harvest!</B></P>");
				}
				myAcct.updateTransaction();
				output.append("<P ALIGN='CENTER'><B><FONT COLOR='green'>SUCCESS:</FONT> Transaction added</B></P>");
			} else {
				output.append("<P ALIGN='CENTER'><B><FONT COLOR='red'>ERROR:</FONT> Could not add a transaction!</B></P>");
				output.append(this.depositForm(aSample));
			}
		} catch (DataException e) {
			e.printStackTrace();
			output.append("<P ALIGN='CENTER'><B><FONT COLOR='red'>SQL FAILURE:</FONT> " + e.getMessage() + "</B></P>");
		}

		return output.toString();
	}
	
	
	private String fractionForm(Sample aSample) {
		Paragraph head = new Paragraph();
		head.setAlign("CENTER");
		StyledText title = new StyledText("Fractionate sample");
		title.setSize("+1");
		head.addItem(title);
		Form myForm = new Form(head);
		myForm.setAttribute("METHOD", "POST");
		myForm.setName("balance");
		myForm.addItem("<INPUT TYPE=HIDDEN NAME='id' VALUE='" + req.getParameter("id") + "'/>");
		myForm.addItem("<INPUT TYPE=HIDDEN NAME='fractionForm' VALUE='YES'/>");

		TableRow tableRow = new TableRow(this.makeFormDateRow("Date:", "date", "balance"));
		tableRow.addItem(this.makeFormTextRow("Amount:", "amount"));
		tableRow.addItem(this.makeFormTextRow("Description:", "notes"));
		
		Table myTable = new Table(tableRow);
		myTable.setClass("species");
		myTable.setAttribute("align", "center");
		myTable.setAttribute("WIDTH", "50%");
		myForm.addItem(myTable.toString());
		myForm.addItem("<CENTER><HR WIDTH='75%'/>");
		myForm.addItem("<P ALIGN='CENTER'>Destination:");
		myForm.addItem(this.collectionPopup("collection", false));
		myForm.addItem("</P>");

		Integer rowNumber = new Integer(8);
		if ( aWrap.hasFormValue("rows")) {
			rowNumber = new Integer(aWrap.getFormValue("rows"));
		}
		Popup numberPop = new Popup();
		for ( int i = 1; i <= 15; i++ ) {
			numberPop.addItem(String.valueOf(i));
		}
		numberPop.setName("rows");
		numberPop.setDefault(rowNumber.toString());
		numberPop.setAttribute("onChange", "this.form.submit()");
		
		myForm.addItem("<P ALIGN='CENTER'>Number of fractions: ");
		myForm.addItem(numberPop.toString());
		myForm.addItem("</P>");

		String[] tableHeaders = { "", "Vial Wt", "Amount", "Notes" };
		TableCell header = new TableHeader(tableHeaders);
		tableRow = new TableRow(header);
		myTable = new Table(tableRow);
		myTable.setClass("species");
		myTable.setAttribute("width", "90%");
		myTable.setAttribute("align", "center");
		String curClass = "odd";

		for ( int i = 1; i <= rowNumber.intValue(); i++ ) {
			Integer thisRow = new Integer(i);
			TableCell myCell = new TableCell("<B>FR " + thisRow.toString() + "</B>");
			String vialWtParam = String.format("vial_wt-%d", thisRow.intValue());
			String amountParam = String.format("amount-%d", thisRow.intValue());
			String notesParam = String.format("notes-%d", thisRow.intValue());
			myCell.addItem(String.format("<INPUT TYPE='TEXT' SIZE=10 NAME='%s' VALUE='%s'/>", vialWtParam, aWrap.getFormValue(vialWtParam)));
			myCell.addItem(String.format("<INPUT TYPE='TEXT' SIZE=10 NAME='%s' VALUE='%s'/>", amountParam, aWrap.getFormValue(amountParam)));
			myCell.addItem(String.format("<TEXTAREA ROWS=2 COLS=15 NAME='%s'>%s</TEXTAREA>", notesParam, aWrap.getFormValue(notesParam)));
			TableRow aRow = new TableRow(myCell);
			aRow.setClass(curClass);
			aRow.setAttribute("align", "center");
			myTable.addItem(aRow);
			if ( curClass.equals("odd") ) {
				curClass = "even";
			} else {
				curClass = "odd";
			}
		}
		myForm.addItem(myTable.toString());
		myForm.addItem("<P ALIGN=CENTER><INPUT TYPE=SUBMIT NAME='action' VALUE='Add Fractions'/><INPUT TYPE='RESET'/></P>");
		return myForm.toString();
	}
*/
	
	@Override
	protected String getHelpModule() {
		return HELP_MODULE;
	}
		
}
