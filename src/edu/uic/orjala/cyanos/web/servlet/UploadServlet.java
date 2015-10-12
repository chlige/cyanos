/**
 * 
 */
package edu.uic.orjala.cyanos.web.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.web.FileUpload;
import edu.uic.orjala.cyanos.web.MultiPartRequest;
import edu.uic.orjala.cyanos.web.Sheet;
import edu.uic.orjala.cyanos.web.SpreadSheet;
import edu.uic.orjala.cyanos.web.UploadForm;
import edu.uic.orjala.cyanos.web.UploadModule;
import edu.uic.orjala.cyanos.web.listener.AppConfigListener;
import edu.uic.orjala.cyanos.web.listener.CyanosRequestListener;
import edu.uic.orjala.cyanos.web.listener.CyanosSessionListener;
import edu.uic.orjala.cyanos.web.upload.UploadJob;


/**
 * @author George Chlipala
 *
 */

public class UploadServlet extends ServletObject {
	


	private static final long serialVersionUID = 1L;

	public static final String PARAM_HEADER = "header";
	public static final String PARAM_SHOW_TYPE = "showType";
	public static final String PARAM_MODULE = "module";
	public static final String PARAM_FILE = "xmlFile";
	
	public static final String PARAM_ROW_BEHAVIOR = "behavior";
	public static final String PARAM_ROWS = "rows";
		
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
	
	public static final String ROW_BEHAVIOR_INCLUDE = "include";
	public static final String ROW_BEHAVIOR_IGNORE = "ignore";
	
	private static final String UPLOAD_JOB = "uploadJob";
	
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
			FileUpload anItem = CyanosRequestListener.getUpload(req, PARAM_FILE);
			if ( anItem != null ) {
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
		if ( request.getParameter(CLEAR_SHEET_ACTION) != null ) {
			thisSession.removeAttribute(SPREADSHEET);
			return null;
		}
		SpreadSheet worksheet = (SpreadSheet) thisSession.getAttribute(SPREADSHEET);	
		if ( worksheet == null ) {
			FileUpload anItem = getUpload(request, PARAM_FILE);	
			if ( anItem != null ) {
				worksheet = new SpreadSheet(anItem);
				thisSession.setAttribute(SPREADSHEET, worksheet);
			}

		} 
		return worksheet;
	}
	
	public static boolean hasSpreadsheet(HttpServletRequest request) throws ServletException, IOException {
		HttpSession thisSession = request.getSession();
		if ( request.getParameter(CLEAR_SHEET_ACTION) != null ) {
			thisSession.removeAttribute(SPREADSHEET);
			return false;
		}

		boolean hasWorkSheet = ( thisSession.getAttribute(SPREADSHEET) != null );
		
		if ( ! hasWorkSheet ) {
			request = MultiPartRequest.parseRequest(request);
			if ( request instanceof MultiPartRequest ) {
				hasWorkSheet = ((MultiPartRequest)request).getUploadCount(PARAM_FILE) > 0;	
			}
		}
		
		return hasWorkSheet;
	}
	
	public static void clearUploadJob(HttpSession session) {
//		session.removeAttribute(RESULTS);
		session.removeAttribute(UPLOAD_JOB);
	}
	
	public static void clearSession(HttpSession session) {
		session.removeAttribute(SPREADSHEET);
//		session.removeAttribute(RESULTS);
		session.removeAttribute(UPLOAD_JOB);
	}
	
	public static UploadJob getUploadJob(HttpSession session) {
		return (UploadJob) session.getAttribute(UPLOAD_JOB);
	} 
	
	public static void startJob(HttpServletRequest request, UploadJob job) throws DataException, SQLException, ServletException, IOException, ParserConfigurationException, SAXException {
		job.startParse(request, getActiveWorksheet(request));
		HttpSession session = request.getSession();
		CyanosSessionListener.addJob(session, job);
		session.setAttribute(UPLOAD_JOB, job);
	}
	
	private static void clearSession(HttpServletRequest req) {
		HttpSession thisSession = req.getSession();
		thisSession.removeAttribute(SPREADSHEET);
		thisSession.removeAttribute(UPLOAD_JOB);
	}
	
	public static Sheet getActiveWorksheet(HttpServletRequest req) throws ServletException, IOException, ParserConfigurationException, SAXException {
		SpreadSheet aWKS = getSpreadsheet(req);
		if ( aWKS != null ) {
			int wksTab = getSelectedWorksheetIndex(req);
			return aWKS.getSheet(wksTab);
		}
		return null;
	}
	
	public static int getSelectedWorksheetIndex(HttpServletRequest request) {
		int selected = 0;
		String wksParam = request.getParameter(UploadServlet.WORKSHEET_PARAM);
		if ( wksParam != null && wksParam.length() > 0 ) {
			selected = Integer.parseInt(wksParam);
		}

		return selected;
	}
	
	public static String[] getColumnList(HttpServletRequest req) throws ServletException, IOException, ParserConfigurationException, SAXException {
		String[] columns = (String[])req.getAttribute("columnList");
		
		if ( columns == null ) {
			Sheet worksheet = getActiveWorksheet(req);
			int maxColumns = worksheet.columnCount();
			worksheet.firstRow();
			int extraCols = 0;
			List<String> headers = new ArrayList<String>();
			if ( req.getParameter(PARAM_HEADER) != null ) {
				extraCols = worksheet.rowSize();
				worksheet.beforeFirstColumn();
				while ( worksheet.nextCellInRow() ) {
					headers.add(worksheet.getStringValue());
				}
			}
			for ( int i = extraCols; i < maxColumns; i++ ) {
				headers.add(String.format("Col: %d", i + 1));
			}
			String[] empty = {};
			columns = headers.toArray(empty);
			req.setAttribute("columnList", columns);
		}
		return columns;
	}
	
	public static String genColumnSelect(HttpServletRequest req, String column, String firstOption) throws ServletException, IOException, ParserConfigurationException, SAXException {
		StringBuffer output = new StringBuffer("<select name=\"");
		output.append(column);
		output.append("\">");
		if ( firstOption != null ) {
			output.append("<option value=\"-1\">");
			output.append(firstOption);
			output.append("</option>");
		}		

		addOptions(req, column, output);
		
		output.append("</select>");
		return output.toString();

	}
	
	private static void addOptions(HttpServletRequest req, String column, StringBuffer output) throws ServletException, IOException, ParserConfigurationException, SAXException {
		int sel = -1;
		String colVal = req.getParameter(column);
		
		if ( colVal != null && colVal.length() > 0 ) {
			sel = Integer.parseInt(colVal);
		}
		
		boolean hasHeader = req.getParameter(PARAM_HEADER) != null;
		
		String[] columns = getColumnList(req);
		
		for ( int i = 0; i < columns.length; i++ ) {
			output.append("<option value=\"");
			output.append(i);
			output.append("\"");
			String thisColumn = columns[i]; 
			if ( i == sel ) {
				output.append(" selected");
			} else if ( sel < 0 && hasHeader ) {
				if ( column.equalsIgnoreCase(thisColumn) ) {
					output.append(" selected");					
				}
			}
			output.append(">");
			output.append(thisColumn);
			output.append("</option>\n");
		}	
	}
	
	public static String genOptions(HttpServletRequest req, String column) throws IOException, ServletException, ParserConfigurationException, SAXException {
		StringBuffer output = new StringBuffer();
		addOptions(req, column, output);
		return output.toString();
	}
		
	public void handleRequest(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {

		// Clear the uploaded worksheet, if requested
		if ( req.getParameter(CLEAR_SHEET_ACTION) != null ) {
			clearSession(req);
		}
		
		HttpSession thisSession = req.getSession();
		
		// Clear the uploaded worksheet, if requested
		if ( req.getParameter(PARSE_ACTION) != null ) {
			UploadModule form = (UploadModule) thisSession.getAttribute(UPLOAD_FORM);
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

		if ( thisSession.getAttribute(UPLOAD_FORM) == null && req.getParameter(PARAM_MODULE) != null ) {
			thisSession.setAttribute(UPLOAD_FORM, this.getUploadForm(req, req.getParameter(PARAM_MODULE)));
		}
		
		String path = req.getPathInfo();
		PrintWriter out = res.getWriter();
				
		if ( "/results".equals(path) ) {
			// If results request. Send the results of the job.
			UploadJob job = getUploadJob(thisSession);
			if ( job.getOutput() != null ) {
				res.setContentType("text/plain");
				out.println(job.getOutput());
				out.close();
				return;
			}
		} else if ( "/status".equals(path) ) {
			// If status request. Send the status of the current job.
			out = res.getWriter();
			UploadForm myForm = (UploadForm) thisSession.getAttribute(UPLOAD_FORM);
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
		return null;
	}

}

