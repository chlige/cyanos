/**
 * 
 */
package edu.uic.orjala.cyanos.web.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.uic.orjala.cyanos.AssayData;
import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.Sample;
import edu.uic.orjala.cyanos.SampleAccount;
import edu.uic.orjala.cyanos.SampleCollection;
import edu.uic.orjala.cyanos.Strain;
import edu.uic.orjala.cyanos.sql.SQLAssay;
import edu.uic.orjala.cyanos.sql.SQLAssayData;
import edu.uic.orjala.cyanos.sql.SQLCompound;
import edu.uic.orjala.cyanos.sql.SQLSample;
import edu.uic.orjala.cyanos.sql.SQLSampleCollection;
import edu.uic.orjala.cyanos.sql.SQLStrain;
import edu.uic.orjala.cyanos.web.BaseForm;
import edu.uic.orjala.cyanos.web.SheetWriter;
import edu.uic.orjala.cyanos.web.forms.SampleForm;

/**
 * @author George Chlipala
 *
 */
public class SampleServlet extends ServletObject {

	private static final long serialVersionUID = 9170635376958905064L;
	
	public static final String ACTION_INTERLACE_COLS = "interlaceCols";

	public static final String DATA_FILE_DIV_ID = "dataFiles";
	public static final String DIV_ASSAY_ID = "assayInfo";
	public static final String DIV_COMPOUND_ID = "compounds";
	public static final String DIV_INFO_FORM_ID = "sampleinfo";
	public static final String DIV_COLLECTION_INFO_FORM_ID = "sampleColinfo";
	public static final String TXN_DIV_ID = "sampleTxn";	
	public static final String INTERLACE_DIV_ID = ACTION_INTERLACE_COLS;
	
	public static final String HELP_MODULE = "sample";
	
	public static final String SEARCHRESULTS_ATTR = "searchresults";
	public static final String SAMPLE_ATTR = "sample";
	public static final String COLLECTION_ATTR = "sampleCol";
	public static final String ATTR_LIBRARIES = "sampleLibs";

	public static final String DELIM = ",";
	
	public static final String PARAM_LIBRARY = "library";
	public static final String PARAM_COLLECTION_ID = "col";
	public static final String PARAM_SAMPLE_ID = "id";


	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.servlet.ServletObject#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		super.doGet(req, res);
		this.handleBasicRequest(req, res);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.servlet.ServletObject#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		super.doPost(req, res);
		this.handleBasicRequest(req, res);
	}

	void handleBasicRequest(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

		try {
			String module = req.getPathInfo();	
			if ( "/export".equals(module) ) {
				res.setContentType("text/plain");
				this.exportCollection(req, res);
				return;
			}

			if ( req.getParameter("div") != null ) {
				this.handleAJAXRequest(req, res);
				return;
			}

			PrintWriter out = res.getWriter();

			Sample aSample = null;
			if ( req.getParameter(PARAM_SAMPLE_ID) != null ) {
				aSample = new SQLSample(this.getSQLData(req), req.getParameter(PARAM_SAMPLE_ID));
			}

			//		SampleForm aForm = new SampleForm(aWrap);
			if ( aSample == null ) {
				req.setAttribute(ATTR_LIBRARIES, SQLSampleCollection.libraries(this.getSQLData(req)));
				if ( req.getParameter(SampleForm.ACTION_INTERLACE_COLS) != null ) { 
					this.exportInterlace(req, res);
					return;
				} 
				if ( req.getParameter(PARAM_COLLECTION_ID) != null ) {
					String collID = req.getParameter(PARAM_COLLECTION_ID);
					if ( req.getParameter("addCol") != null && collID.length() > 0 ) {
						req.setAttribute(COLLECTION_ATTR, SQLSampleCollection.create(this.getSQLData(req), collID));
					} else {
						req.setAttribute(COLLECTION_ATTR, SQLSampleCollection.load(this.getSQLData(req), collID));
					}
				} else {		
					if ( req.getParameter("newCollection") != null ) {
						RequestDispatcher disp = getServletContext().getRequestDispatcher("/sample/add-collection.jsp");
						disp.forward(req, res);
						return;
					} else if ( req.getParameter(PARAM_LIBRARY) != null && req.getParameter(PARAM_LIBRARY).length() > 0 ) {
						req.setAttribute(SEARCHRESULTS_ATTR, SQLSampleCollection.loadForLibrary(this.getSQLData(req), req.getParameter(PARAM_LIBRARY)));
					} else {
						req.setAttribute(SEARCHRESULTS_ATTR, SQLSampleCollection.sampleCollections(this.getSQLData(req)));
					}
				}
				RequestDispatcher disp = getServletContext().getRequestDispatcher("/sample-collection.jsp");
				disp.forward(req, res);
			} else if (aSample.first()) {
				req.setAttribute(SAMPLE_ATTR, aSample);
				RequestDispatcher disp = getServletContext().getRequestDispatcher("/sample.jsp");
				disp.forward(req, res);			
			} else 
				out.println("<P ALIGN='CENTER'><B><FONT COLOR='red'>ERROR:</FONT> SAMPLE NOT FOUND!</B></P>");

		} catch (DataException e) {
			throw new ServletException(e);
		} catch (SQLException e) {
			throw new ServletException(e);				
		}

	/*
		} else if ( module.equals("/add") ) {
			Paragraph head = new Paragraph();
			head.setAlign("CENTER");
			StyledText title = new StyledText("Add Sample Vial");
			title.setSize("+3");
			head.addItem(title);
			head.addItem("<HR WIDTH='85%'/>");
			out.println(head);
			out.println(aForm.addSample());
*/
/*
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
*/
//		aWrap.finishHTMLDoc();

	}
	
	private void handleAJAXRequest(HttpServletRequest req, HttpServletResponse res) throws DataException, SQLException, IOException, ServletException {
		res.setContentType("text/html; charset=UTF-8");
		PrintWriter out = res.getWriter();
		String divTag = req.getParameter("div");
		//			if ( "/protocol".equals(module) && divTag.equals(ProtocolForm.DIV_ID) ) {
		//				ProtocolForm aForm = new ProtocolForm(aWrap, SampleForm.EXTRACT_PROTOCOL, SampleForm.EXTRACT_PROTOCOL_KEYS);
		//				Form retForm = aForm.protocolForm();
		//				out.print(retForm.toString());
		//			} else {
		Sample aSample = new SQLSample(this.getSQLData(req), req.getParameter("id"));
		if ( aSample.first() ) {
			if ( divTag.equals(TXN_DIV_ID) ) {
				req.setAttribute(SAMPLE_ATTR, aSample);
				RequestDispatcher disp = getServletContext().getRequestDispatcher("/sample/sample-acct.jsp");
				disp.forward(req, res);			
			} else if ( divTag.equals(DATA_FILE_DIV_ID) ) {
				req.setAttribute(SAMPLE_ATTR, aSample);
				RequestDispatcher disp = DataFileServlet.dataFileDiv(req, getServletContext(), aSample, Sample.DATA_FILE_CLASS);
				disp.forward(req, res);			
			} else if ( divTag.equals(DIV_ASSAY_ID) ) {
				AssayData data;
				if ( req.getParameter("target") != null && req.getParameter("target").length() > 0 )
					data = SQLAssayData.dataForSampleID(this.getSQLData(req), req.getParameter("id"), req.getParameter("target"));
				else
					data = SQLAssayData.dataForSampleID(this.getSQLData(req), req.getParameter("id"));
				req.setAttribute(AssayServlet.SEARCHRESULTS_ATTR, data);
				req.setAttribute(AssayServlet.TARGET_LIST, SQLAssay.targets(this.getSQLData(req)));
				RequestDispatcher disp = getServletContext().getRequestDispatcher("/assay/assay-data-list.jsp");
				disp.forward(req, res);
			} else if ( divTag.equals(DIV_COMPOUND_ID) ) {
				req.setAttribute(CompoundServlet.COMPOUND_PARENT, aSample);
				if ( req.getParameter("showCmpdForm") != null ) 
					req.setAttribute(CompoundServlet.COMPOUND_LIST, SQLCompound.compounds(this.getSQLData(req), SQLCompound.ID_COLUMN, SQLCompound.ASCENDING_SORT));
				else 
					req.setAttribute(CompoundServlet.COMPOUND_RESULTS, aSample.getCompounds());
				RequestDispatcher disp = getServletContext().getRequestDispatcher("/material/link-material-compound.jsp");
				disp.forward(req, res);
			}
		} else if ( divTag.equals(INTERLACE_DIV_ID) ) {
			RequestDispatcher disp = getServletContext().getRequestDispatcher("/sample/sample-interlace.jsp");
			disp.forward(req, res);				
		} else if ( divTag.equals(DIV_COLLECTION_INFO_FORM_ID) ) {
			req.setAttribute(COLLECTION_ATTR, SQLSampleCollection.load(this.getSQLData(req), req.getParameter("col")));
			req.setAttribute(ATTR_LIBRARIES, SQLSampleCollection.libraries(this.getSQLData(req)));
			RequestDispatcher disp = getServletContext().getRequestDispatcher("/sample/collection-form.jsp");
			disp.forward(req, res);							
		}
		//			}
		out.flush();
		return;
	}

	private void exportCollection(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
		PrintWriter out = res.getWriter();
		res.setContentType("text/plain");
		Sample mySamples = null;
		boolean isBox = false;
		
		try {
			if ( req.getParameter("col") != null) {
				SampleCollection aCol = SQLSampleCollection.load(this.getSQLData(req), req.getParameter("col"));
				if ( aCol.first() ) {
					mySamples = aCol.getSamples();
					isBox = ( aCol.getLength() > 0 && aCol.getWidth() > 0 );
				} else {
					out.println("ERROR: Cannot find sample collection.");
					return;
				}
			} else if ( req.getParameter("strain")  != null ) {
				Strain aStrain = SQLStrain.load(this.getSQLData(req), req.getParameter("strain"));
				if ( aStrain.first() )
					mySamples = aStrain.getSamples();
				else {
					out.println("ERROR: Cannot find strain.");
					return;					
				}
			}

			SheetWriter sheetOut = new SheetWriter(out);
			
			sheetOut.print("Sample ID");
			sheetOut.print("Sample Label");
			sheetOut.print("Culture ID");
			sheetOut.print("Collection ID");
			if ( isBox ) {
				sheetOut.print("Location");
			}
			sheetOut.print("Date Added");
			sheetOut.print("Last Activity");
			sheetOut.print("Current Balance");
			sheetOut.println("Notes");

			if ( mySamples != null ) {
				mySamples.beforeFirst();
				while ( mySamples.next() ) {
					sheetOut.print(mySamples.getID());
					sheetOut.print(mySamples.getName());

					sheetOut.print(mySamples.getCultureID());
					sheetOut.print(mySamples.getCollectionID());
					if ( isBox ) 
						sheetOut.print(mySamples.getLocation());
					sheetOut.print(mySamples.getDateString());
					SampleAccount txnAcct = mySamples.getAccount();
					if ( txnAcct.last() ) {
						sheetOut.print(txnAcct.getDateString());
						sheetOut.print(BaseForm.formatAmount(mySamples.accountBalance(), mySamples.getBaseUnit()));
					} else {
						sheetOut.print(""); sheetOut.print("");
					}
					sheetOut.println(mySamples.getNotes());
				}
			} else {
				sheetOut.println("ERROR: NO SAMPLES FOUND FOR THIS QUERY");
			}
		} catch (DataException e) {
			throw new ServletException(e);
		} catch (SQLException e) {
			throw new ServletException(e);
		}
		out.flush();
	}

/*
	private String exportCollection(CyanosWrapper aWrap) {
		try {
			SampleCollection aCol = new SQLSampleCollection(aWrap.getSQLDataSource(), aWrap.getFormValue("col"));
			List<List<String>> output = new ArrayList<List<String>>();
			List<String> aRow = new ArrayList<String>();
			boolean isBox = false;
			Sample mySamples = null;
			if ( aWrap.hasFormValue("col") && (! aWrap.getFormValue("col").equals("")) ) {
				if ( aCol.first() ) 
					mySamples = aCol.getSamples();
				else
					return "ERROR: Cannot find sample collection.";
			} else if ( aWrap.hasFormValue("strain") && (! aWrap.getFormValue("strain").equals("")) ) {
				Strain aStrain = SQLStrain.load(aWrap.getSQLDataSource(), aWrap.getFormValue("strain"));
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
						aRow.add(BaseForm.formatAmount(mySamples.accountBalance(), mySamples.getBaseUnit()));
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
*/
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
	private void exportInterlace(HttpServletRequest req, HttpServletResponse res) throws IOException {
		SheetWriter out = new SheetWriter(res.getWriter());
		res.setContentType("text/plain");
		
		try {
			
			String[] sources = {
				req.getParameter("source1"), 
				req.getParameter("source2"), 
				req.getParameter("source3"), 
				req.getParameter("source4") 
			};		
			
			/*
			 * Source, Source location, Strain ID, Sample ID, label, dest ID, dest loc, amount
			 */
			
			out.print("Source");
			out.print("Source location");
			out.print("Strain ID");
			out.print("Sample ID");
			out.print("Label");
			out.print("Destination");
			out.print("Dest. location");
			out.println("Amount");
			
			String amount = req.getParameter("amount");
			String destID = req.getParameter("destID");

			/*
			 * 1 | 2
			 * --+--
			 * 3 | 4
			 */
			
			int rowShift = 1;
			int colShift = 1;
			
			for ( String source: sources ) {
				SampleCollection collection = SQLSampleCollection.load(this.getSQLData(req), source);
				if ( collection.first() ) {
					Sample samples = collection.getSamples();
					samples.beforeFirst();
					while ( samples.next() ) {
						out.print(source);
						out.print(samples.getLocation());
						out.print(samples.getCultureID());
						out.print(samples.getID());
						out.print(samples.getName());
						out.print(destID);
						
						int row = ((samples.getLocationRow() - 1 ) * 2) + rowShift;
						int col = ((samples.getLocationCol() - 1) * 2) + colShift;
						out.print(getLocation(row, col));
						out.println(amount);
					}
					
				}
				colShift++;
				if ( colShift > 2 ) { colShift = 1; rowShift = 2; }
			}
		} catch (DataException e) {
			out.println("ERROR: ".concat(e.getMessage()));
		} catch (SQLException e) {
			out.println("ERROR: ".concat(e.getMessage()));
		}
	}
		
	private static String getLocation(int row, int col) {
		char rowAlpha = (char) ('A' + (row - 1));
		return String.format("%c%02d",rowAlpha, col);
	}

	public String getHelpModule() {
		return HELP_MODULE;
	}
		
}
