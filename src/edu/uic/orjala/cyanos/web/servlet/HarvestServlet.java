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

import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.Harvest;
import edu.uic.orjala.cyanos.Strain;
import edu.uic.orjala.cyanos.sql.SQLExtractProtocol;
import edu.uic.orjala.cyanos.sql.SQLHarvest;
import edu.uic.orjala.cyanos.sql.SQLStrain;

/**
 * @author George Chlipala
 *
 */
public class HarvestServlet extends ServletObject {
	 /**
	 * 
	 */
	private static final long serialVersionUID = -3807841657926086738L;
	public static final String HELP_MODULE = "harvest";
	public static final String SEARCHRESULTS_ATTR = "searchresults";
	public static final String INFO_FORM_DIV_ID = "infoForm";
	public static final String HARVEST_ATTR = "harvest";
	public static final String EXTRACT_LIST_DIV_ID = "extracts";
	public static final String EXTRACT_PROTOCOLS = "protocols";

	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.servlet.ServletObject#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		super.doGet(req, res);
		this.handleRequest(req, res);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.servlet.ServletObject#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		super.doPost(req, res);
		this.handleRequest(req, res);
	}



	private void handleRequest(HttpServletRequest req, HttpServletResponse res) throws ServletException,IOException {
		
		try { 
			if ( req.getParameter("div") != null ) {
				this.handleAJAX(req, res);
				return;
			}

			if ( req.getParameter("id") != null) {
				Harvest object = SQLHarvest.load(this.getSQLData(req), req.getParameter("id"));
				req.setAttribute(HARVEST_ATTR, object);
			} else if ( req.getParameter("query") != null ) {
				Harvest objects = SQLHarvest.harvestsForStrain(this.getSQLData(req), req.getParameter("query"));
				req.setAttribute(SEARCHRESULTS_ATTR, objects);
			}
			this.forwardRequest(req, res, "/harvest.jsp");
		} catch (SQLException e) {
			throw new ServletException(e);
		} catch (DataException e) {
			throw new ServletException(e);
		}
	}

	private void handleAJAX(HttpServletRequest req, HttpServletResponse res) throws DataException, SQLException, IOException, ServletException {
		res.setContentType("text/html; charset=UTF-8");
		String divID = req.getParameter("div");
		PrintWriter out = res.getWriter();

		if ( req.getParameter("livesearch") != null ) {
			this.printCultureIDs(req, res);
		} else if ( divID.equals(EXTRACT_LIST_DIV_ID) ) {
			Harvest object = SQLHarvest.load(this.getSQLData(req), req.getParameter("id"));
			req.setAttribute(HARVEST_ATTR, object);				
			if ( req.getParameter("showExtractForm") != null ) 
				req.setAttribute(EXTRACT_PROTOCOLS, SQLExtractProtocol.protocols(this.getSQLData(req)));
			else
				req.setAttribute(SampleServlet.SEARCHRESULTS_ATTR, object.getExtract());
			RequestDispatcher disp = getServletContext().getRequestDispatcher("/harvest/harvest-extract-form.jsp");
			disp.forward(req, res);				
		} else if ( divID.equals(INFO_FORM_DIV_ID) ) {
			Harvest object = SQLHarvest.load(this.getSQLData(req), req.getParameter("id"));
			req.setAttribute(HARVEST_ATTR, object);				
			RequestDispatcher disp = getServletContext().getRequestDispatcher("/harvest/harvest-form.jsp");
			disp.forward(req, res);
		} else if ( divID.startsWith("div_calendar")) {
			RequestDispatcher disp = getServletContext().getRequestDispatcher("/includes/calendar.jsp");
			disp.forward(req, res);				
		}

		out.flush();

	}
	
	private void printCultureIDs(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException, DataException, SQLException {
		PrintWriter out = res.getWriter();
		String field = req.getParameter("livesearch");
		String[] likeColumns = { SQLStrain.ID_COLUMN };
		String[] likeValues = { String.format("%s%%", req.getParameter(field)) };
		Strain strains = SQLStrain.strainsLike(this.getSQLData(req), likeColumns, likeValues, SQLStrain.ID_COLUMN, SQLStrain.ASCENDING_SORT);

		if ( strains.first() ) {
			strains.beforeFirst();
			while ( strains.next() ) {
				out.print(String.format("<A onClick='setLS(\"%s\",\"%s\",\"%s\")'>%s</A><BR>", field, strains.getID(), req.getParameter("div"), strains.getID()));
			}
		} else {
			out.print("No suggestions.");
		}
	}
	
	public String getHelpModule() {
		return HELP_MODULE;
	}
		

}
