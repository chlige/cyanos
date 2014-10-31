package edu.uic.orjala.cyanos.web.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.uic.orjala.cyanos.web.UploadForm;
import edu.uic.orjala.cyanos.web.UploadJob;

/**
 * Servlet implementation class UploadStatusServlet
 */
public class UploadStatusServlet extends UploadServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}
	
	public void handleRequest(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		// If status request. Send the status of the current job.
		PrintWriter out = res.getWriter();
		HttpSession thisSession = req.getSession();


		if ( req.getParameter("results") != null ) {
			String results = (String)thisSession.getAttribute(RESULTS);
			if ( results != null ) {
				res.setContentType("text/plain");
				out.println(results);
				out.close();
				return;
			}
		} else if ( req.getParameter("sheet") != null ) {
			RequestDispatcher disp = this.getServletContext().getRequestDispatcher("/upload/sheet.jsp");
			disp.forward(req, res);
		} else {
			res.setContentType("text/plain");
			UploadJob job = UploadServlet.getUploadJob(thisSession);
			
			if ( job != null ) {
				out.println(getStatus(job));				
			} else {
				UploadForm myForm = (UploadForm) thisSession.getAttribute(UPLOAD_FORM);	
				out.println(getStatus(myForm));
			}
			
			
		}
		out.close();
		return;	
	}
	
	private static String getStatus(UploadForm form) {
		if ( form == null ) {
			return "ERROR";
		} else if ( form.isDone() ) {
			return "DONE";
		} else if ( form.isWorking()) {
			return String.format("%.0f", form.status() * 100);
		} else {
			return "STOP";
		}		
	}

	private static String getStatus(UploadJob job) {
		if ( job == null ) {
			return "ERROR";
		} else if ( job.isDone() ) {
			return "DONE";
		} else if ( job.isWorking()) {
			return String.format("%.0f", job.status() * 100);
		} else {
			return "STOP";
		}		
	}

}
