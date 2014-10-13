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
	
	public static SpreadSheet getSpreadsheet(HttpServletRequest request) throws ServletException, IOException, ParserConfigurationException, SAXException {
		HttpSession thisSession = request.getSession();
		SpreadSheet worksheet = (SpreadSheet) thisSession.getAttribute(SPREADSHEET);
		if ( worksheet == null ) {
			request = MultiPartRequest.parseRequest(request);
			if ( request instanceof MultiPartRequest ) {
				FileUpload anItem = ((MultiPartRequest)request).getUpload("xmlFile");			
				worksheet = new SpreadSheet(anItem);
				thisSession.setAttribute(SPREADSHEET, worksheet);
			}
		}
		return worksheet;
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
					form.startParse(req, newSQLData(req, AppConfigListener.getDBConnection()));
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
			return;
		}
		
		RequestDispatcher disp = this.getServletContext().getRequestDispatcher("/upload.jsp");
		disp.forward(req, res);
	}
	
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

}

