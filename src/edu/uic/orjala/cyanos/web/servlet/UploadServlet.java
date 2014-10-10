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
import javax.servlet.http.HttpSession;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.web.MultiPartRequest;
import edu.uic.orjala.cyanos.web.MultiPartRequest.FileUpload;
import edu.uic.orjala.cyanos.web.SpreadSheet;
import edu.uic.orjala.cyanos.web.UploadForm;
import edu.uic.orjala.cyanos.web.UploadModule;
import edu.uic.orjala.cyanos.web.listener.AppConfigListener;
import edu.uic.orjala.cyanos.web.upload.AssayUpload;
import edu.uic.orjala.cyanos.web.upload.CollectionUpload;
import edu.uic.orjala.cyanos.web.upload.ExtractUpload;
import edu.uic.orjala.cyanos.web.upload.FractionUpload;
import edu.uic.orjala.cyanos.web.upload.IsolationUpload;
import edu.uic.orjala.cyanos.web.upload.SampleLibraryUpload;
import edu.uic.orjala.cyanos.web.upload.SampleMoveUpload;
import edu.uic.orjala.cyanos.web.upload.TaxaUpload;


/**
 * @author George Chlipala
 *
 */

public class UploadServlet extends ServletObject {
	

	private static final long serialVersionUID = 1L;

//	private Sheet worksheet = null;
//	private UploadModule myForm = null;
	
	public static final String PARAM_HEADER = "header";
	public static final String PARAM_SHOW_TYPE = "showType";
	public static final String PARAM_MODULE = "module";

	public final static String RESULTS = "upload results";
	public final static String PARSE_ACTION = "parseAction";
	public final static String CLEAR_SHEET_ACTION = "clearUpload";
	public static final String SHOW_RESULTS = "showResults";
	public static final String WORKSHEET_PARAM = "worksheet";
	
	public final static String REQ_PARAM_SERVLET = "uploadServlet";
	
	// Modules
	public final static String ASSAY_UPLOAD_MODULE = "assay";
	public final static String SAMPLE_LIBRARY_UPLOAD_MODULE = "sample-library";
	public final static String SAMPLE_MOVE_UPLOAD_MODULE = "sample-move";
	public final static String EXTRACT_UPLOAD_MODULE = "sample-extract";
	public final static String FRACTION_UPLOAD_MODULE = "fraction";
	public final static String COLLECTION_UPLOAD_MODULE = "collection";
	public final static String ISOLATION_UPLOAD_MODULE = "isolation";
	public final static String TAXA_UPLOAD_MODULE = "taxa";
	
	public final static String CUSTOM_UPLOAD_MODULES = "uploadModules";
	
	public final static String TEMPLATE_DIV = "templateDiv";
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.servlet.ServletObject#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		super.doGet(req, res);
		
		PrintWriter out = res.getWriter();
//		String module = req.getPathInfo();
//		HttpSession thisSession = req.getSession();

		if ( req.getParameter("div") != null ) {
			res.setContentType("text/html; charset=UTF-8");
			String divTag = req.getParameter("div");
			if ( divTag.equals(TEMPLATE_DIV) ) {
				RequestDispatcher disp = this.getServletContext().getRequestDispatcher("/upload/template.jsp");
				disp.forward(req, res);

	//			if ( this.myForm != null ) {
	//				ProtocolForm aForm = new ProtocolForm(aWrap, templateName, this.myForm.getTemplateKeys());
	//				Form retForm = aForm.protocolForm(true);
	//				out.print(retForm.toString());
	//			}
			} 

			out.flush();
			return;
		}
		this.handleRequest(req, res);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.servlet.ServletObject#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		req = MultiPartRequest.parseRequest(req);
		super.doPost(req, res);
		try {
			if ( req instanceof MultiPartRequest ) {
				FileUpload anItem = ((MultiPartRequest)req).getUpload("xmlFile");			
				SpreadSheet aWKS = new SpreadSheet(anItem);
				if ( aWKS != null  ) {
					HttpSession thisSession = req.getSession();
					thisSession.setAttribute(SPREADSHEET, aWKS);
				}
			}
		} catch (ParserConfigurationException e) {
			PrintWriter out = res.getWriter();
			out.println("<FONT COLOR='red'><B>FAILED TO PARSE UPLOADED FILE!</B></FONT><BR/>");
			out.println(e.getMessage());
			e.printStackTrace();
			out.println("<BR/>Make sure that the file being uploaded is a Microsoft XML spreadsheet (.xlsx or .xml) or an Openoffice spreadsheet (.ods)");
		} catch (SAXException e) {
			PrintWriter out = res.getWriter();
			out.println("<FONT COLOR='red'><B>FAILED TO PARSE UPLOADED FILE!</B></FONT><BR/>");
			out.println(e.getMessage());
			e.printStackTrace();
			out.println("<BR/>Make sure that the file being uploaded is either a Microsoft XML spreadsheet (.xml) or an Openoffice spreadsheet (.ods)");
		} catch (IOException e) {
			PrintWriter out = res.getWriter();
			out.println("<FONT COLOR='red'><B>FAILED TO UPLOAD FILE!</B></FONT><BR/>");
			out.println(e.getMessage());
			e.printStackTrace();
			out.println("<BR/>");
		}

		this.handleRequest(req, res);
	}

	private void clearSession(HttpServletRequest req) {
		HttpSession thisSession = req.getSession();
		thisSession.removeAttribute(SPREADSHEET);
		thisSession.removeAttribute(RESULTS);
		thisSession.removeAttribute(UPLOAD_JOB);
	}
	
	public void handleRequest(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

		// Clear the uploaded worksheet, if requested
		if ( req.getParameter(CLEAR_SHEET_ACTION) != null ) {
			this.clearSession(req);
		}
		
		HttpSession thisSession = req.getSession();
		
		// Clear the uploaded worksheet, if requested
		if ( req.getParameter(PARSE_ACTION) != null ) {
			UploadModule form = (UploadModule) thisSession.getAttribute(UPLOAD_JOB);
			if ( form != null && form.getActiveWorksheet() != null ) {
				try {
					form.startParse(req, this.newSQLData(req, AppConfigListener.getDBConnection()));
				} catch (DataException e) {
					throw new ServletException(e);
				} catch (SQLException e) {
					throw new ServletException(e);
				}
			}
		}

		if ( thisSession.getAttribute(UPLOAD_JOB) == null && req.getParameter(PARAM_MODULE) != null ) {
			thisSession.setAttribute(UPLOAD_JOB, this.getUploadForm(req, req.getParameter(PARAM_MODULE)));
		}
		
		String path = req.getPathInfo();
		PrintWriter out = res.getWriter();
				
		if ( "/results".equals(path) ) {
			// If results request. Send the results of the job.
			String results = (String)thisSession.getAttribute(RESULTS);
			if ( results != null ) {
				res.setContentType("text/plain");
				out.println(results);
				out.close();
				return;
			}
		} else if ( "/status".equals(path) ) {
			// If status request. Send the status of the current job.
			out = res.getWriter();
			UploadForm myForm = (UploadForm) thisSession.getAttribute(UPLOAD_JOB);
			res.setContentType("text/plain");
			if ( myForm == null ) {
				out.print("ERROR");
			} else if ( myForm.isDone() ) {
				out.print("DONE");
			} else if ( myForm.isWorking()) {
				out.print(String.format("%.0f", myForm.status() * 100));
			} else {
				out.print("STOP");
			}
			out.close();
			return;
		} else if ( "/sheet".equals(path) ) {
			
			RequestDispatcher disp = this.getServletContext().getRequestDispatcher("/upload/sheet.jsp");
			disp.forward(req, res);

	/*		
			res.setContentType("text/xml");
			int length = -1;
			if ( req.getParameter("length") != null )
				length = Integer.parseInt(req.getParameter("length"));
			out.print(this.generateSpreadsheet(req, length));
			out.close();
	*/
			return;
		}
		
		RequestDispatcher disp = this.getServletContext().getRequestDispatcher("/upload.jsp");
		disp.forward(req, res);

		/*
		if ( module == null || module.equals("/") ) {
			out.println(this.blankForm());
		} else {
			String path[] = module.split("/",3);
			if ( path[1].equals("sample") ) {
				if ( path.length < 3 ) {
					out.print("<P ALIGN='CENTER'><FONT SIZE='+2'><I>Sample Upload</FONT></I></P>");
					Paragraph content = new Paragraph("Please select an upload form<BR>");
					HtmlList formList = new HtmlList();
					formList.unordered();
					formList.setAttribute("type", "none");
					formList.addItem("<LI><A HREF='sample/move'>Move Samples</A></LI>");
					formList.addItem("<LI><A HREF='sample/library'>Upload Sample Library Information</A></LI>");
					formList.addItem("<LI><A HREF='sample/extract'>Upload Extract Information</A></LI>");
					content.addItem(formList);
					out.println(content.toString());
					out.println("");					
					aWrap.finishHTMLDoc();
					return;
				} else
					this.getUploadForm(aWrap, path[1] + "/" + path[2]);
			} else if ( path[1].equals("custom") ) {
				if ( path.length < 3 ) {
					out.print("<P ALIGN='CENTER'><FONT SIZE='+2'><I>Custom Modules</FONT></I></P>");
					Paragraph content = new Paragraph("Please select an upload form<BR>");
					HtmlList formList = new HtmlList();
					formList.unordered();
					formList.setAttribute("type", "none");
					
					if ( this.uploadModules != null ) {
						Iterator<Class<UploadModule>> anIter = this.uploadModules.values().iterator();
						while ( anIter.hasNext() ) {
							Class<UploadModule> aClass = anIter.next();
							formList.addItem(String.format("<LI><A HREF='custom/%s'>%s</A>", aClass.getName(), aClass.getSimpleName()));
						}
					}
					content.addItem(formList);
					out.println(content.toString());
					out.println("");					
					aWrap.finishHTMLDoc();
					return;
				} else {
					this.setUploadModule(aWrap, path[2]);
				}
			} else {
				this.getUploadForm(aWrap, path[1]);
			}
			out.print("<P ALIGN='CENTER'><FONT SIZE='+2'><I>");
			if ( this.myForm != null ) {
				out.print(this.myForm.title());
				out.println("</FONT></I></P>");
				if ( ! this.myForm.isAllowed() ) {
					out.println("<DIV CLASS='messages'><P><B><FONT COLOR='red'>ACCESS DENIED</FONT></B></P></DIV>");
				} else if ( aWrap.getSession().getAttribute(SPREADSHEET) == null ) {
					out.println(this.uploadForm(aWrap));
					out.println("<HR WIDTH='50%' ALIGN='CENTER'/><P ALIGN='CENTER'><FONT SIZE=+1><B>Worksheet Template</B></FONT></P><P ALIGN='CENTER'>");
					out.println(this.myForm.worksheetTemplate());
					out.println("</P>");
				} else {
					if ( aWrap.hasFormValue(PARSE_ACTION) )
						out.println(this.parseUpload(aWrap));
					else if ( aWrap.hasFormValue(SHOW_RESULTS)) {
						HttpSession thisSession = aWrap.getSession();
						myForm = (UploadForm)thisSession.getAttribute(UPLOAD_JOB);
						if ( myForm == null ) {
							out.print("<P ALIGN='CENTER'><FONT SIZE='+1'><B>No results to report.</B></FONT><BR/>");
						} else {
							while ( this.myForm.isWorking() ) { Thread.sleep(1000); }
							out.print("<P ALIGN='CENTER'><FONT SIZE='+1'><B>Upload Results</B></FONT><BR/>");
							if ( this.myForm.resultSheet() != null ) {
								out.println(String.format("<A HREF='%s/upload/results'>Export Results</A></P>", aWrap.getContextPath()));
							}
							Form aForm = new Form("<P ALIGN='CENTER'><BUTTON TYPE='SUBMIT' NAME='clearUpload'>Clear Uploaded Data</BUTTON></P>");
							aForm.setPost();
							aForm.setAttribute("ACTION", aWrap.getRequestURI());
							out.println(aForm.toString());
							out.println(this.myForm.resultReport());
							Sheet resultSheet = this.myForm.resultSheet();
							if ( resultSheet != null ) 
								thisSession.setAttribute(RESULTS, this.myForm.resultSheet().asCSV());
							this.myForm.close();
							thisSession.removeAttribute(UPLOAD_JOB);
						}
					} else 
						out.println(this.displayModule(aWrap));
				}
			} else {
				out.print(this.blankForm());
			}
		}
		out.println("");
		
		aWrap.finishHTMLDoc();
		*/
		
	}

	/*
	private void setUploadModule(CyanosWrapper aWrap, String module) {
		Class<UploadModule> aModule = AppConfigListener.uploadModules.get(module);
		if ( aModule != null ) {
			Class[] classList = { CyanosWrapper.class, Sheet.class };
			Object[] args = { aWrap, this.getActiveWorksheet(aWrap) };
			try {
				Constructor aCons = aModule.getConstructor(classList);
				this.myForm = (UploadModule) aCons.newInstance(args);
			} catch (Exception e) {
				aWrap.handleException(e);
			}
		}
	}

*/
	/**
	 * @param length number of rows to display in table.
	 * @return String of a <DIV> containing the spreadsheet.
	 */
/*
	private String generateSpreadsheet(HttpServletRequest req, int length) {
		StringBuffer output = new StringBuffer("<P>");
		boolean header = req.getParameter(PARAM_HEADER) != null;
		Table spreadSheet = this.makeSheetTable(req, header, length);
		String jsFormat;
		if ( length > 0 ) {
			Sheet aSheet = this.getActiveWorksheet(req);
			int maxLength = aSheet.rowCount();
			int colSpan = aSheet.columnCount() + 1;
			if ( length <= maxLength ) {
				int newLength = 100;
				if ( length < 100 ) newLength = length * 2;
				int left = maxLength - length;
				if ( left < newLength ) newLength = length + left;
				jsFormat = String.format("loadTable('%s/upload/sheet?%slength=%d&worksheet=%s', this.form.%s)", req.getContextPath(), 
						( header ? "header&" : ""), length + newLength, req.getParameter(WORKSHEET_PARAM), PARAM_SHOW_TYPE);
				spreadSheet.addItem(String.format("<TR><TH COLSPAN='%d'><BUTTON TYPE='BUTTON' onClick=\"%s\">View Next %d Rows</BUTTON></TH></TR>", colSpan, jsFormat, newLength));
			}
			jsFormat = String.format("loadTable('%s/upload/sheet?%sworksheet=%s', this.form.%s)", 
					req.getContextPath(), ( header ? "header&" : ""), req.getParameter(WORKSHEET_PARAM), PARAM_SHOW_TYPE);			
		} else {
			jsFormat = String.format("loadTable('%s/upload/sheet?%sworksheet=%s', this.form.%s)", 
					req.getContextPath(), ( header ? "header&" : ""), req.getParameter(WORKSHEET_PARAM), PARAM_SHOW_TYPE);			
		}
		output.append(String.format("<INPUT TYPE=CHECKBOX NAME='%s' onClick=\"%s\"", PARAM_SHOW_TYPE, jsFormat));
		if ( req.getParameter(PARAM_SHOW_TYPE) != null ) {
			output.append(" CHECKED");
		}
		output.append("/> Highlight Data Types (String, <FONT COLOR='red'>Number</FONT>, <FONT COLOR='blue'>Date/Time</FONT>)</P>");
		output.append(spreadSheet.toString());
		return output.toString();
	}
*/
	/*
	private String parseUpload(CyanosWrapper aWrap) {
		this.myForm.startParse();
		HttpSession thisSession = aWrap.getSession();
		thisSession.setAttribute(UPLOAD_JOB, this.myForm);
		
		return "<P ALIGN='CENTER'>Parsing upload</P>" + this.getProgressAJAX(aWrap);

	}
	*/
	
	/*
	private String getProgressAJAX(CyanosWrapper aWrap) {
		String contextPath = aWrap.getContextPath();
		String progressApplet = "<div align='center'><div class='progress' style='width: 200px'><div id='progressText'></div>" +
				"<div id='progressBar'></div></div>" +
				"<form><p><button id='resultButton' name='showResults' disabled>Show Results</button></p></form></div>" +
				String.format("<script> var updatePath = '%s/upload/status';",contextPath) +
				"uploadStatus(updatePath, document.getElementById('resultButton'));</script>";
		return progressApplet;
	}
	*/
	
/*
	private String getProgressApplet(CyanosWrapper aWrap) {
		String contextPath = aWrap.getContextPath();
		String progressApplet = String.format("<DIV ALIGN='CENTER'>\n<OBJECT classid=\"clsid:8AD9C840-044E-11D1-B3E9-00805F499D93\" ALIGN='CENTER' " +
				"CODEBASE=\"http://java.sun.com/products/plugin/autodl/jinstall-1_5_0-windows-i586.cab#Version=1,5,0,0\" HEIGHT=100 WIDTH=200 >\n" +
				"<PARAM NAME=\"CODE\" VALUE=\"edu.uic.orjala.progressbar.ProgressBar\">\n" +
				"<PARAM NAME=\"ARCHIVE\" VALUE='%s/applets/progress.jar'>\n" +
				"<PARAM NAME=\"STATUS\" VALUE=\"%s/upload/status\">\n<COMMENT>\n" +
				"<EMBED TYPE='application/x-java-applet;version=1.5' " +
				"CODE='edu.uic.orjala.progressbar.ProgressBar' ARCHIVE='%s/applets/progress.jar' " +
				"ALIGN='CENTER' HEIGHT=100 WIDTH=200 " +
				"STATUS='%s/upload/status'>\n<NOEMBED>Java not available</NOEMBED>\n</EMBED>\n</COMMENT>\n</OBJECT>\n</DIV>", 
				contextPath, contextPath, contextPath, contextPath);
		return progressApplet;
	}
*/
	
	private UploadForm getUploadForm(HttpServletRequest req, String module) {
		if ( module != null ) {
			if ( module.equals(ASSAY_UPLOAD_MODULE) )
				return new AssayUpload(req);
			else if ( module.equals(SAMPLE_MOVE_UPLOAD_MODULE) )
				return new SampleMoveUpload(req);
			else if ( module.equals(SAMPLE_LIBRARY_UPLOAD_MODULE) )
				return new SampleLibraryUpload(req);
			else if ( module.equals(EXTRACT_UPLOAD_MODULE) )
				return new ExtractUpload(req);
			else if ( module.equals(ISOLATION_UPLOAD_MODULE) )
				return new IsolationUpload(req);
			else if ( module.equals(COLLECTION_UPLOAD_MODULE) )
				return new CollectionUpload(req);
			else if ( module.equals(FRACTION_UPLOAD_MODULE) )
				return new FractionUpload(req);
			else if ( module.equals(TAXA_UPLOAD_MODULE) ) 
				return new TaxaUpload(req);
		}
		return null;
	}

	/*
	private String displayModule(CyanosWrapper aWrap) {
		Form myForm = this.basicForm(aWrap);
		if ( this.getActiveWorksheet(aWrap) != null ) {
			myForm.addItem(this.fullForm(aWrap));
		}
		return myForm.toString();		
	}
	*/
	
	/*
	private String fullForm(CyanosWrapper aWrap) {
		StringBuffer output = new StringBuffer();
		Div formDiv = new Div();
		formDiv.setID(ProtocolForm.DIV_ID);
		formDiv.addItem("<P ALIGN='CENTER'>");
		formDiv.addItem(ProtocolForm.loadButton("Load a worksheet template"));
		formDiv.addItem(ProtocolForm.saveButton("Save as a worksheet template"));
		formDiv.addItem("</P>");
		output.append(formDiv.toString());
		
		if ( this.getActiveWorksheet(aWrap) != null ) {
			output.append(this.myForm.templateForm());
			output.append("<P ALIGN='CENTER'>");
			Popup inOutPopup = new Popup();
			inOutPopup.addItemWithLabel("ignore", "Ignore");
			inOutPopup.addItemWithLabel("include", "Parse");
			inOutPopup.setName("behavior");
			output.append(inOutPopup.toString());
			output.append("<B> selected rows.</B><BR/><INPUT TYPE='SUBMIT' NAME='parseAction' VALUE='Parse Data'/><INPUT TYPE='reset'/></P>");
			output.append("<HR WIDTH='80%'/>");
//			output.append("\n<EMBED TYPE='application/x-java-applet;version=1.5' CODE='edu.uic.orjala.ssView.SpreadSheetViewer' ARCHIVE='/cyanos/applets/spreadsheet-viewer.jar'>\n");
//			output.append("<PARAM NAME='spreadsheet' VALUE='/cyanos/upload/sheet' />\n");
//			output.append("<NOEMBED>Java not available</NOEMBED>\n");
//			output.append("</EMBED>\n");
			Div aDiv = new Div(this.generateSpreadsheet(aWrap, 25));
			aDiv.setID("spreadsheet");
			output.append(aDiv.toString());
		} else {
			output.append("<P ALIGN='CENTER'><B><FONT COLOR='red'>Error:</FONT> Worksheet not defined!</B></P>");
		}
		return output.toString();
	}
	*/

	/*
	private String blankForm() {
		Paragraph content = new Paragraph("<FONT SIZE='+1'><B>Please select an upload form</B></FONT><BR>");
		HtmlList formList = new HtmlList();
		formList.unordered();
		formList.setAttribute("type", "none");
		formList.addItem(String.format("<LI><A HREF='upload/%s'>Upload %s</A></LI>", ASSAY_UPLOAD_MODULE, AssayUpload.TITLE));
		formList.addItem(String.format("<LI><A HREF='upload/%s'>Upload %s</A></LI>", FRACTION_UPLOAD_MODULE, FractionUpload.TITLE));
		formList.addItem(String.format("<LI><A HREF='upload/%s'>Upload %s</A></LI>", ISOLATION_UPLOAD_MODULE, IsolationUpload.TITLE));
		formList.addItem(String.format("<LI><A HREF='upload/%s'>Upload %s</A></LI>", COLLECTION_UPLOAD_MODULE, CollectionUpload.TITLE));
				
		HtmlList subList = new HtmlList();
		subList.unordered();
		subList.setAttribute("type", "none");

		subList.addItem("<LI><A HREF='upload/sample/move'>Move Samples</A></LI>");
		subList.addItem("<LI><A HREF='upload/sample/library'>Upload Sample Library Information</A></LI>");
		subList.addItem("<LI><A HREF='upload/sample/extract'>Upload Extract Information</A></LI>");

		formList.addItem(String.format("<LI><B>Samples</B> %s</LI>", subList.toString()));
		
		subList = new HtmlList();
		subList.unordered();
		subList.setAttribute("type", "none");
		
		if ( this.uploadModules != null ) {
			Iterator<Class<UploadModule>> anIter = this.uploadModules.values().iterator();
			while ( anIter.hasNext() ) {
				Class<UploadModule> aClass = anIter.next();
				subList.addItem(String.format("<LI><A HREF='upload/custom/%s'>%s</A>", aClass.getName(), aClass.getSimpleName()));
			}
		}
		formList.addItem(String.format("<LI><B>Custom Modules</B> %s</LI>", subList.toString()));

		content.addItem(formList);
		return content.toString();
	}

	private String uploadForm(CyanosWrapper aWrap) {
		Form uploadForm = new Form("<P ALIGN='CENTER'>");
		uploadForm.setAttribute("METHOD", "POST");
		uploadForm.setAttribute("ACTION", aWrap.getRequestURI());
		uploadForm.setAttribute("ENCTYPE", "multipart/form-data");
		uploadForm.addItem("<BR/><B>File to upload:</B> ");
		uploadForm.addItem("<INPUT TYPE='FILE' NAME='xmlFile' SIZE=25/>");
		uploadForm.addItem("<INPUT TYPE='SUBMIT' VALUE='Upload'/>");
		uploadForm.addItem("</P>");
		uploadForm.addItem("<P ALIGN='CENTER'><B>Upload Instructions</B>");
		uploadForm.addItem("<UL>");
		uploadForm.addItem("<LI><B>Microsoft Excel 2007 or higher</B> - Save the files as a standard <B>Excel Workbook (*.xlsx)</B>.</LI>");
		uploadForm.addItem("<LI><B>Microsoft Excel 2003 or earlier</B> - Save spreadsheets as an <B>XML spreadsheet (*.xml)</B> and upload the resulting .xml file.</LI>");
		uploadForm.addItem("<LI><B>OpenOffice 2.0 or higher</B> - Save as a standard <B>OpenOffice spreadsheet (*.ods)</B></LI></UL></P>");		
		return uploadForm.toString();
	}
	
	*/
	
	// The basic header of the form. i.e. sheet selector.
	/*
	private Form basicForm(CyanosWrapper aWrap) {
		Form uploadForm = new Form("<CENTER>");
		uploadForm.setAttribute("METHOD", "POST");
		uploadForm.setAttribute("ACTION", aWrap.getRequestURI());
		uploadForm.setName("upload");

		HttpSession thisSession = aWrap.getSession();
		uploadForm.addItem("<BR/><B>Select a worksheet:</B> ");
		Popup collPop = new Popup();
		collPop.addItem("");
		collPop.setName(WORKSHEET_PARAM);		
		collPop.setAttribute("onChange", "this.form.submit()");

		SpreadSheet aWKS = (SpreadSheet)thisSession.getAttribute(SPREADSHEET);
		List<String> sheets = aWKS.worksheetNames();
		for ( int i = 0; i < sheets.size(); i++ ) {
			collPop.addItemWithLabel(String.format("%d", i), sheets.get(i));
		}
		uploadForm.addItem(collPop);
		uploadForm.addItem("<BR/><INPUT TYPE='SUBMIT' NAME='clearUpload' VALUE='Clear Uploaded Data'/><BR/>");
		if ( aWrap.hasFormValue(WORKSHEET_PARAM) && (! aWrap.getFormValue(WORKSHEET_PARAM).equals("")) ) {
			collPop.setDefault(aWrap.getFormValue(WORKSHEET_PARAM));
		}
		return uploadForm;
	}
	*/
	
	/*
 	private String saveDataTemplateForm(String module) {
		StringBuffer output = new StringBuffer(this.myForm.hideTemplateValues());
		String dataName = module + " upload";
		
		if ( aWrap.hasFormValue("saveAction") ) {
			String fileName = aWrap.getFormValue("template");
			if ( fileName == null || fileName.equals("") ) 
				fileName = aWrap.getFormValue("newName");
			output.append(this.saveDataTemplate(dataName, fileName));
			output.append("<P ALIGN='CENTER'><INPUT TYPE='SUBMIT' NAME='cancelForm' VALUE='Return to upload form'/></P>");
			return output.toString();
		}
		try {			
			Statement sth = dbc.createStatement();
			ResultSet results = sth.executeQuery(String.format("SELECT name FROM data_templates WHERE data='%s'",dataName));
			results.beforeFirst();
			Popup aPop = new Popup();
			aPop.addItemWithLabel("", "A New File ->");
			aPop.setName("template");
			while (results.next()) {
				aPop.addItem(results.getString(1));
			}
			output.append("<INPUT TYPE='HIDDEN' NAME='subform' VALUE='Save as a worksheet template'/>");
			output.append("<P><B>Save template to:</B>");
			output.append(aPop.toString());
			output.append("<INPUT TYPE='TEXT' NAME='newName'/>");
			output.append("</P><P><INPUT TYPE='SUBMIT' NAME='saveAction' VALUE='Save worksheet template'/><INPUT TYPE='SUBMIT' NAME='cancelForm' VALUE='Cancel'/></P>");
		} catch (SQLException e) {
			output.append("<P ALIGN='CENTER'><FONT COLOR='red'><B>SQL failure in saving data template!</FONT><BR/>");
			output.append(e.toString());
			output.append("</B></P>");
			this.log("SQL failure in saving data template", e);
		}
				
		return output.toString();
	}
		
	protected String saveDataTemplate(String data, String name) {
		return this.saveDataTemplate(data, name, this.myForm.getTemplate());
	}
	*/
	
	/*
	private String loadDataTemplate(String module) {
		StringBuffer output = new StringBuffer();
		String dataName = module + " upload";
		if ( formValues.containsKey("action") ) {
			if ( aWrap.getFormValue("action").equals("Load worksheet template") ) {
				output.append(this.loadWorksheetTemplate(dataName, aWrap.getFormValue("template")));
				output.append(this.myForm.hideTemplateValues());
				output.append("<P ALIGN='CENTER'><INPUT TYPE='SUBMIT' NAME='cancelForm' VALUE='Return to upload form'/></P>");
			}
			return output.toString();
		}
		output.append(this.loadDataTemplateForm(dataName));
		return output.toString();
	}

	private String loadDataTemplateForm(String dataName) {
		StringBuffer output = new StringBuffer(this.myForm.hideTemplateValues());
		try {
			Statement sth = dbc.createStatement();
			ResultSet results = sth.executeQuery(String.format("SELECT name FROM data_templates WHERE data='%s'",dataName));
			results.beforeFirst();
			Popup aPop = new Popup();
			aPop.addItemWithLabel("", "---SELECT A TEMPLATE---");
			aPop.setName("template");
			while (results.next()) {
				aPop.addItem(results.getString(1));
			}
			output.append("<INPUT TYPE='HIDDEN' NAME='subform' VALUE='Load a worksheet template'/>");
			output.append("<P><B>Select a template:</B>");
			output.append(aPop.toString());
			output.append("</P><P><INPUT TYPE='SUBMIT' NAME='action' VALUE='Load worksheet template'/><INPUT TYPE='SUBMIT' NAME='cancelForm' VALUE='Cancel'/></P>");
		} catch (SQLException e) {
			output.append("<P ALIGN='CENTER'><FONT COLOR='red'><B>SQL failure in retrieving templates!</FONT><BR/>");
			output.append(e.toString());
			output.append("</B></P>");
		}
		return output.toString();
	}
	
	protected String loadWorksheetTemplate(String data, String name) {
		try {
			Map<String,String> aTemplate = super.loadDataTemplate(data, name);
			if ( aTemplate != null ) {
				this.myForm.setTemplate(aTemplate);
				return "<P ALIGN='CENTER'><FONT COLOR='green'><B>Data template loaded.</B></FONT></P>";		
			} else
				return "<P ALIGN='CENTER'><FONT COLOR='red'><B>Could not retrieve data template!</B></FONT></P>";
		} catch (SQLException e) {
			return "<P ALIGN='CENTER'><FONT COLOR='red'><B>SQL Failure in retrieving data template!</B></FONT><BR/>" + e.getMessage() + "</B></P>";
		} catch (IOException e) {
			return "<P ALIGN='CENTER'><FONT COLOR='red'><B>Java IO Failure in retrieving data template!</B></FONT><BR/>" + e.getMessage() + "</B></P>";
		} catch (ClassNotFoundException e) {
			return "<P ALIGN='CENTER'><FONT COLOR='red'><B>Java class not defined!</B></FONT><BR/>" + e.getMessage() + "</B></P>";
		}
	}
*/
	
	/*

	@Deprecated
	private Table makeSheetTable (HttpServletRequest req, boolean internalHeaders, int length) {
		TableCell headerCell = new TableHeader("");
		List<String> headerList = this.getHeaderList(req, internalHeaders);
		// headerCell.addItem(headerList.toArray());
		ListIterator<String> anIter = headerList.listIterator();
		while ( anIter.hasNext() ) {
			headerCell.addItem(anIter.next());
		}
		TableRow ssRow = new TableRow(headerCell);
		// Build the spreadsheet table.
		int startRow = 0;
		if ( internalHeaders ) startRow = 1;

		Sheet worksheet = this.getActiveWorksheet(req);
		boolean showTypes = req.getParameter(PARAM_SHOW_TYPE) != null;
		
		int rows = length;
		if ( length < 0 || length > worksheet.rowCount() ) rows = worksheet.rowCount();
		for ( int r = startRow; r < rows; r++ ) {
			TableCell ssCell = new TableCell("<INPUT TYPE='CHECKBOX' NAME='rows' VALUE='" + String.valueOf(r) + "'/>");
			worksheet.gotoRow(r);
			worksheet.beforeFirstColumn();
			while ( worksheet.nextCellInRow() ) {
				SheetValue value = worksheet.getValue();
				if ( value == null ) {
					ssCell.addItem("");
				} else if ( showTypes ) {
					if ( value.isNumber() ) {
						ssCell.addItem(String.format("<FONT COLOR='red'>%s</FONT>", value.toString()));
					} else if ( value.isDate() ) {
						ssCell.addItem(String.format("<FONT COLOR='blue'>%s</FONT>", value.toString()));
					} else {
						ssCell.addItem(value.toString());
					}
				} else {
					ssCell.addItem(value.toString());
				}
			}
			ssRow.addItem(ssCell);
		}
		Table ssTable = new Table(ssRow);
		ssTable.setAttribute("CLASS", "plate");
		ssTable.setAttribute("ALIGN", "CENTER");
		return ssTable;
	}
*/
	
	/*
	@Deprecated
	private List<String> getHeaderList(HttpServletRequest req, boolean internalHeaders) {
		Sheet worksheet = this.getActiveWorksheet(req);
		int maxColumns = worksheet.columnCount();
		worksheet.firstRow();
		int extraCols = 0;
		List<String> headers = new ArrayList<String>();
		if ( internalHeaders ) {
			extraCols = worksheet.rowSize();
			worksheet.beforeFirstColumn();
			while ( worksheet.nextCellInRow() ) {
				headers.add(worksheet.getStringValue());
			}
		}
		for ( int i = extraCols; i < maxColumns; i++ ) {
			headers.add(String.format("Col: %d", i + 1));
		}
		return headers;
	}
	
	*/
}

