/**
 * 
 */
package edu.uic.orjala.cyanos.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.fileupload.FileItem;
import org.xml.sax.SAXException;

import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.web.forms.ProtocolForm;
import edu.uic.orjala.cyanos.web.html.Div;
import edu.uic.orjala.cyanos.web.html.Form;
import edu.uic.orjala.cyanos.web.html.HtmlList;
import edu.uic.orjala.cyanos.web.html.Image;
import edu.uic.orjala.cyanos.web.html.Paragraph;
import edu.uic.orjala.cyanos.web.html.Popup;
import edu.uic.orjala.cyanos.web.html.StyledText;
import edu.uic.orjala.cyanos.web.html.Table;
import edu.uic.orjala.cyanos.web.html.TableCell;
import edu.uic.orjala.cyanos.web.html.TableHeader;
import edu.uic.orjala.cyanos.web.html.TableRow;
import edu.uic.orjala.cyanos.web.upload.AssayUpload;
import edu.uic.orjala.cyanos.web.upload.CollectionUpload;
import edu.uic.orjala.cyanos.web.upload.ExtractUpload;
import edu.uic.orjala.cyanos.web.upload.FractionUpload;
import edu.uic.orjala.cyanos.web.upload.IsolationUpload;
import edu.uic.orjala.cyanos.web.upload.SampleLibraryUpload;
import edu.uic.orjala.cyanos.web.upload.SampleMoveUpload;


/**
 * @author George Chlipala
 *
 */

public class UploadServlet extends ServletObject {
	
	private static final long serialVersionUID = 1L;

//	private Sheet worksheet = null;
	private UploadModule myForm = null;
	
	private final static String RESULTS = "upload results";
	private final static String PARSE_ACTION = "parseAction";
	private final static String CLEAR_SHEET_ACTION = "clearUpload";
	private static final String SHOW_RESULTS = "showResults";
	private static final String WORKSHEET_PARAM = "worksheet";
	
	// Modules
	private final static String ASSAY_UPLOAD_MODULE = "assay";
	private final static String SAMPLE_LIBRARY_UPLOAD_MODULE = "sample/library";
	private final static String SAMPLE_MOVE_UPLOAD_MODULE = "sample/move";
	private final static String EXTRACT_UPLOAD_MODULE = "sample/extract";
	private final static String FRACTION_UPLOAD_MODULE = "fraction";
	private final static String COLLECTION_UPLOAD_MODULE = "collection";
	private final static String ISOLATION_UPLOAD_MODULE = "isolation";
	
	private Map<String, Class> uploadModules = null;
	
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		if ( ! this.newInstall ) {
			CyanosConfig myConfig = this.getAppConfig();
			List<String> addOns = myConfig.classesForUploadModule();
			this.addModules(addOns);
		}
	}
	
	private void addModules(List<String> newModules) {
		if ( newModules != null && newModules.size() > 0 ) {
			this.uploadModules = new HashMap<String, Class>(newModules.size());
			ListIterator<String> anIter = newModules.listIterator();
			while ( anIter.hasNext() ) {
				String className = anIter.next();

				try {
					Class aClass = Class.forName(className, true, this.getClass().getClassLoader());
					if ( aClass != null ) {
						if ( UploadModule.class.isAssignableFrom(aClass) ) {
							this.uploadModules.put(aClass.getName(), aClass);
							this.log(String.format("LOADED upload module: %s", className));
						} else {
							this.log(String.format("Will NOT load module: %s. Does NOT implement UploadModule interface!", className));
						}
					}
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
					this.log(String.format("Could not load upload module: %s via class loader", className));
					this.log(e.getLocalizedMessage());
				}
			}
		}

	}
	
	public void display(CyanosWrapper aWrap) throws Exception {
		// Clear existing data
		this.myForm = null;

		// Clear the uploaded worksheet, if requested
		if ( aWrap.hasFormValue(CLEAR_SHEET_ACTION) ) {
			HttpSession thisSession = aWrap.getSession();
			thisSession.setAttribute(SPREADSHEET, null);
			thisSession.removeAttribute(SPREADSHEET);
			thisSession.removeAttribute(RESULTS);
			thisSession.removeAttribute(UPLOAD_JOB);
		}

		PrintWriter out;
		String module = aWrap.getRequest().getPathInfo();

		if ( "/results".equals(module) ) {
			HttpSession thisSession = aWrap.getSession();
			String results = (String)thisSession.getAttribute(RESULTS);
			if ( results != null ) {
				aWrap.setContentType("text/plain");
				out = aWrap.getWriter();
				out.println(results);
				out.close();
				return;
			}
		} else if ( "/status".equals(module) ) {
			out = aWrap.getWriter();
			HttpSession thisSession = aWrap.getSession();
			this.myForm = (UploadForm)thisSession.getAttribute(UPLOAD_JOB);
			aWrap.setContentType("text/plain");
			if ( this.myForm == null ) {
				out.print("ERROR");
			} else if ( this.myForm.isDone() ) {
				out.print("DONE");
			} else if ( this.myForm.isWorking()) {
				out.print(String.format("%.0f", this.myForm.status() * 100));
			} else {
				out.print("STOP");
			}
			out.close();
			return;
		} else if ( "/sheet".equals(module) ) {
			out = aWrap.getWriter();
			aWrap.setContentType("text/xml");
			int length = -1;
			if ( aWrap.hasFormValue("length") )
				length = Integer.parseInt(aWrap.getFormValue("length"));
			out.print(this.generateSpreadsheet(aWrap, length));
			out.close();
			return;
		} else if ( aWrap.hasFormValue("div") ) {
			aWrap.setContentType("text/html");
			out = aWrap.getWriter();
			String divTag = aWrap.getFormValue("div");
			if ( divTag.equals(ProtocolForm.DIV_ID) ) {
				String templateName = "";
				if ( module != null && (! module.equals("/")) ) {
					String path[] = module.split("/",3);
					if ( path[1].equals("sample") && path.length == 3 ) {
						this.setUploadForm(aWrap, path[1] + "/" + path[2]);
						templateName = String.format("upload/sample/%s", path[2]);
					} else if ( path[1].equals("custom") && path.length == 3) {
						this.setUploadModule(aWrap, path[2]);
						templateName = String.format("upload/custom/%s", path[2]);
					} else {
						this.setUploadForm(aWrap, path[1]);
						templateName = String.format("upload/%s", path[1]);
					}
				}
				
				if ( this.myForm != null ) {
					ProtocolForm aForm = new ProtocolForm(aWrap, templateName, this.myForm.getTemplateKeys());
					Form retForm = aForm.protocolForm(true);
					out.print(retForm.toString());
				}
			} 
			out.flush();
			return;
		}
		
		out = aWrap.startHTMLDoc("Upload Data");
	
		try {
			if ( aWrap.hasUpload("xmlFile") ) {
				FileItem anItem = aWrap.getUpload("xmlFile");			
				SpreadSheet aWKS = new SpreadSheet(anItem);
				if ( aWKS != null  ) {
					HttpSession thisSession = aWrap.getSession();
					thisSession.setAttribute(SPREADSHEET, aWKS);
				}
			}
		} catch (ParserConfigurationException e) {
			out.println("<FONT COLOR='red'><B>FAILED TO PARSE UPLOADED FILE!</B></FONT><BR/>");
			out.println(e.getMessage());
			e.printStackTrace();
			out.println("<BR/>Make sure that the file being uploaded is either a Microsoft XML spreadsheet (.xml) or an Openoffice spreadsheet (.ods)");
		} catch (SAXException e) {
			out.println("<FONT COLOR='red'><B>FAILED TO PARSE UPLOADED FILE!</B></FONT><BR/>");
			out.println(e.getMessage());
			e.printStackTrace();
			out.println("<BR/>Make sure that the file being uploaded is either a Microsoft XML spreadsheet (.xml) or an Openoffice spreadsheet (.ods)");
		} catch (IOException e) {
			out.println("<FONT COLOR='red'><B>FAILED TO UPLOAD FILE!</B></FONT><BR/>");
			out.println(e.getMessage());
			e.printStackTrace();
			out.println("<BR/>");
		}
		
		Paragraph head = new Paragraph();
		head.setAlign("CENTER");		
		
		// Start to create the upload form.
		StyledText sTitle = new StyledText("Upload Data");
		sTitle.setSize("+3");
		head.addItem(sTitle);
		head.addItem("<HR WIDTH='85%'/>");
		out.println(head.toString());
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
					this.setUploadForm(aWrap, path[1] + "/" + path[2]);
			} else if ( path[1].equals("custom") ) {
				if ( path.length < 3 ) {
					out.print("<P ALIGN='CENTER'><FONT SIZE='+2'><I>Custom Modules</FONT></I></P>");
					Paragraph content = new Paragraph("Please select an upload form<BR>");
					HtmlList formList = new HtmlList();
					formList.unordered();
					formList.setAttribute("type", "none");
					
					if ( this.uploadModules != null ) {
						Iterator<Class> anIter = this.uploadModules.values().iterator();
						while ( anIter.hasNext() ) {
							Class aClass = anIter.next();
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
				this.setUploadForm(aWrap, path[1]);
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
						this.myForm = (UploadForm)thisSession.getAttribute(UPLOAD_JOB);
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
					} else 
						out.println(this.displayModule(aWrap));
				}
			} else {
				out.print(this.blankForm());
			}
		}
		out.println("");
		
		aWrap.finishHTMLDoc();
		
	}

	private void setUploadModule(CyanosWrapper aWrap, String module) {
		Class aModule = this.uploadModules.get(module);
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

	/**
	 * @param length number of rows to display in table.
	 * @return String of a <DIV> containing the spreadsheet.
	 */
	private String generateSpreadsheet(CyanosWrapper aWrap, int length) {
		Table spreadSheet = this.makeSheetTable(aWrap, aWrap.hasFormValue("header"), length);
		if ( length > 0 ) {
			Sheet aSheet = this.getActiveWorksheet(aWrap);
			int maxLength = aSheet.rowCount();
			int colSpan = aSheet.columnCount() + 1;
			if ( length <= maxLength ) {
				int newLength = 100;
				if ( length < 100 ) newLength = length * 2;
				int left = maxLength - length;
				if ( left < newLength ) newLength = length + left;
				String urlFormat;
				if ( aWrap.hasFormValue("header") ) {
					urlFormat = "<TR><TH COLSPAN='%d'><BUTTON TYPE='BUTTON' onClick=\"loadTable('%s/upload/sheet?header&length=%d&worksheet=%s')\">View Next %d Rows</BUTTON></TH></TR>";
				} else {
					urlFormat = "<TR><TH COLSPAN='%d'><BUTTON TYPE='BUTTON' onClick=\"loadTable('%s/upload/sheet?length=%d&worksheet=%s')\">View Next %d Rows</BUTTON></TH></TR>";
				}
				spreadSheet.addItem(String.format(urlFormat, colSpan, aWrap.getContextPath(), length + newLength, aWrap.getFormValue(WORKSHEET_PARAM), newLength));
			}
		}
		return spreadSheet.toString();
	}
	
	private String parseUpload(CyanosWrapper aWrap) {
		this.myForm.startParse();
		HttpSession thisSession = aWrap.getSession();
		thisSession.setAttribute(UPLOAD_JOB, this.myForm);	
		Image progressImg = aWrap.getImage("progress.png");
		progressImg.setAttribute("NAME", "progressBar");
		return "<P ALIGN='CENTER'>Parsing upload</P>" + this.getProgressApplet(aWrap);

	}
	
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

	private Sheet getActiveWorksheet(CyanosWrapper aWrap) {
		if ( aWrap.hasFormValue(WORKSHEET_PARAM) && (! aWrap.getFormValue(WORKSHEET_PARAM).equals("") ) ) {
			HttpSession thisSession = aWrap.getSession();
			SpreadSheet aWKS = (SpreadSheet)thisSession.getAttribute(SPREADSHEET);
			if ( aWKS != null ) {
				int wksTab = Integer.parseInt(aWrap.getFormValue(WORKSHEET_PARAM));
				return (Sheet)aWKS.getSheet(wksTab);
			}
		}
		return null;
	}
	
	private void setUploadForm(CyanosWrapper aWrap, String module) throws DataException, SQLException {
		if ( module.equals(ASSAY_UPLOAD_MODULE) )
			this.myForm = new AssayUpload(aWrap, this.getActiveWorksheet(aWrap));
		else if ( module.equals(SAMPLE_MOVE_UPLOAD_MODULE) )
			this.myForm = new SampleMoveUpload(aWrap, this.getActiveWorksheet(aWrap));
		else if ( module.equals(SAMPLE_LIBRARY_UPLOAD_MODULE) )
			this.myForm = new SampleLibraryUpload(aWrap, this.getActiveWorksheet(aWrap));
		else if ( module.equals(EXTRACT_UPLOAD_MODULE) )
			this.myForm = new ExtractUpload(aWrap, this.getActiveWorksheet(aWrap));
		else if ( module.equals(ISOLATION_UPLOAD_MODULE) )
			this.myForm = new IsolationUpload(aWrap, this.getActiveWorksheet(aWrap));
		else if ( module.equals(COLLECTION_UPLOAD_MODULE) )
			this.myForm = new CollectionUpload(aWrap, this.getActiveWorksheet(aWrap));
		else if ( module.equals(FRACTION_UPLOAD_MODULE) )
			this.myForm = new FractionUpload(aWrap, this.getActiveWorksheet(aWrap));
	}

	private String displayModule(CyanosWrapper aWrap) {
		Form myForm = this.basicForm(aWrap);
		if ( this.getActiveWorksheet(aWrap) != null ) {
			myForm.addItem(this.fullForm(aWrap));
		}
		return myForm.toString();		
	}
	
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
			Iterator<Class> anIter = this.uploadModules.values().iterator();
			while ( anIter.hasNext() ) {
				Class aClass = anIter.next();
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
		uploadForm.addItem("<BR/>For Microsoft Excel spreadsheets save the file as an XML spreadsheet (.xml) and upload the resulting .xml file.<BR/>");
		uploadForm.addItem("For an OpenOffice spreadsheet (.ods), be sure to utilize version 2.0 or higher of the OpenOffice.org suite.</P>");		
		return uploadForm.toString();
	}
	
	// The basic header of the form. i.e. sheet selector.
	@SuppressWarnings("unchecked")
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

	@Deprecated
	private Table makeSheetTable (CyanosWrapper aWrap, boolean internalHeaders, int length) {
		TableCell headerCell = new TableHeader("");
		List<String> headerList = this.getHeaderList(aWrap, internalHeaders);
		// headerCell.addItem(headerList.toArray());
		ListIterator<String> anIter = headerList.listIterator();
		while ( anIter.hasNext() ) {
			headerCell.addItem(anIter.next());
		}
		TableRow ssRow = new TableRow(headerCell);
		// Build the spreadsheet table.
		int startRow = 0;
		if ( internalHeaders ) startRow = 1;

		Sheet worksheet = this.getActiveWorksheet(aWrap);
		
		int rows = length;
		if ( length < 0 || length > worksheet.rowCount() ) rows = worksheet.rowCount();
		for ( int r = startRow; r < rows; r++ ) {
			TableCell ssCell = new TableCell("<INPUT TYPE='CHECKBOX' NAME='rows' VALUE='" + String.valueOf(r) + "'/>");
			ssCell.addItem(worksheet.row(r));
			ssRow.addItem(ssCell);
		}
		Table ssTable = new Table(ssRow);
		ssTable.setAttribute("CLASS", "plate");
		ssTable.setAttribute("ALIGN", "CENTER");
		return ssTable;
	}

	@Deprecated
	private List<String> getHeaderList(CyanosWrapper aWrap, boolean internalHeaders) {
		Sheet worksheet = this.getActiveWorksheet(aWrap);
		int maxColumns = worksheet.columnCount();
		worksheet.firstRow();
		int extraCols = 0;
		List<String> headers = new ArrayList<String>();
		if ( internalHeaders ) {
			extraCols = worksheet.rowSize();
			worksheet.beforeFirstColumn();
			while ( worksheet.nextCellInRow() ) {
				headers.add(worksheet.getValue());
			}
		}
		for ( int i = extraCols; i < maxColumns; i++ ) {
			headers.add(String.format("Col: %d", i + 1));
		}
		return headers;
	}
}

