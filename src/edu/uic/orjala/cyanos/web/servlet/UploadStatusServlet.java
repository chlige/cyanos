package edu.uic.orjala.cyanos.web.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import edu.uic.orjala.cyanos.web.UploadForm;

/**
 * Servlet implementation class UploadStatusServlet
 */
public class UploadStatusServlet extends UploadServlet {
	private static final long serialVersionUID = 1L;

    /**
     * Default constructor. 
     */
    public UploadStatusServlet() {
        // TODO Auto-generated constructor stub
    }

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

		UploadForm myForm = (UploadForm) thisSession.getAttribute(UPLOAD_FORM);

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
			if ( myForm == null ) {
				out.print("ERROR");
			} else if ( myForm.isDone() ) {
				out.print("DONE");
			} else if ( myForm.isWorking()) {
				out.print(String.format("%.0f", myForm.status() * 100));
			} else {
				out.print("STOP");
			}
		}
		out.close();
		return;	
	}

}
