package edu.uic.orjala.cyanos.web.servlet;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.uic.orjala.cyanos.AssayData;
import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.Material;
import edu.uic.orjala.cyanos.sql.SQLAssay;
import edu.uic.orjala.cyanos.sql.SQLAssayData;
import edu.uic.orjala.cyanos.sql.SQLCompound;
import edu.uic.orjala.cyanos.sql.SQLMaterial;

/**
 * Servlet implementation class MaterialServlet
 */
public class MaterialServlet extends ServletObject {

	private static final long serialVersionUID = 4285574522049849257L;
	
	public static final String SEARCHRESULTS_ATTR = "searchresults";
	public static final String MATERIAL_ATTR = "material";
	
	public static final String SAMPLE_LIST = "samples";
	public static final String SAMPLE_LIST_DIV_ID = "sampleinfo";
	public static final String SEP_LIST_DIV_ID = "sepInfo";
	public static final String COMPOUND_DIV_ID = "compounds";
	public static final String INFO_FORM_DIV_ID = "infoForm";
	public static final String ASSAY_DIV_ID = "assays";
	public static final String DATAFILE_DIV_ID = "dataFiles";
	
	
	
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

	protected void handleBasicRequest(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
		try {
			if ( req.getParameter("div") != null ) {
				this.handleDivContent(req, res);
				return;
			}

			if ( req.getParameter("id") != null ) {
				Material object = SQLMaterial.load(getSQLData(req), req.getParameter("id"));
				req.setAttribute(MATERIAL_ATTR, object);
			} else if ( req.getParameter("query") != null ) {
				//			aWrap.getRequest().setAttribute("material", object);
				Material objects = SQLMaterial.find(getSQLData(req), req.getParameter("query"));
				req.setAttribute(SEARCHRESULTS_ATTR, objects);
			}
			RequestDispatcher disp = getServletContext().getRequestDispatcher("/material.jsp");
			disp.forward(req, res);
		} catch (DataException e) {
			throw new ServletException(e);
		} catch (SQLException e) {
			throw new ServletException(e);				
		}
	}    
	
	private void handleDivContent(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException, DataException, SQLException {
		String divID = req.getParameter("div");
		if ( req.getParameter("livesearch") != null ) {
			StrainServlet.printCultureIDs(getSQLData(req), req, res);
		} else if ( divID.equals(SAMPLE_LIST_DIV_ID) ) {
			Material object = SQLMaterial.load(getSQLData(req), req.getParameter("id"));
			req.setAttribute(SampleServlet.SEARCHRESULTS_ATTR, object.getSamples());
			RequestDispatcher disp = getServletContext().getRequestDispatcher("/sample/sample-list.jsp");
			disp.forward(req, res);				
		} else if ( divID.equals(SEP_LIST_DIV_ID) ) {
			Material object = SQLMaterial.load(getSQLData(req), req.getParameter("id"));
			try {
				req.setAttribute(SeparationServlet.SEARCHRESULTS_ATTR, object.getSeparations());
				RequestDispatcher disp = getServletContext().getRequestDispatcher("/separation/separation-list.jsp");
				disp.forward(req, res);		
			} catch (DataException e) {
				throw new ServletException(e);
			}
		} else if ( divID.equals(COMPOUND_DIV_ID) ) {
			Material object = SQLMaterial.load(getSQLData(req), req.getParameter("id"));
			req.setAttribute(CompoundServlet.COMPOUND_PARENT, object);
			if ( req.getParameter("showCmpdForm") != null ) 
				req.setAttribute(CompoundServlet.COMPOUND_LIST, SQLCompound.compounds(getSQLData(req), SQLCompound.ID_COLUMN, SQLCompound.ASCENDING_SORT));
			else 
				req.setAttribute(CompoundServlet.COMPOUND_RESULTS, object.getCompounds());
			RequestDispatcher disp = getServletContext().getRequestDispatcher("/material/link-material-compound.jsp");
			disp.forward(req, res);
		} else if ( divID.equals(INFO_FORM_DIV_ID) ) {
			Material object = SQLMaterial.load(getSQLData(req), req.getParameter("id"));
			req.setAttribute(MATERIAL_ATTR, object);				
			RequestDispatcher disp = getServletContext().getRequestDispatcher("/material/material-form.jsp");
			disp.forward(req, res);
		} else if ( divID.equals(ASSAY_DIV_ID) ) {
			AssayData data;
			if ( req.getParameter("target") != null && req.getParameter("target").length() > 0 )
				data = SQLAssayData.dataForMaterialID(getSQLData(req), req.getParameter("id"), req.getParameter("target"));
			else
				data = SQLAssayData.dataForMaterialID(getSQLData(req), req.getParameter("id"));
			req.setAttribute(AssayServlet.SEARCHRESULTS_ATTR, data);
			req.setAttribute(AssayServlet.TARGET_LIST, SQLAssay.targets(getSQLData(req)));
			RequestDispatcher disp = getServletContext().getRequestDispatcher("/assay/assay-data-list.jsp");
			disp.forward(req, res);
		} else if ( divID.equals(DATAFILE_DIV_ID) ) {
			Material object = SQLMaterial.load(getSQLData(req), req.getParameter("id"));
			RequestDispatcher disp = DataFileServlet.dataFileDiv(req, getServletContext(), object, Material.DATA_FILE_CLASS);
			disp.forward(req, res);			
		}
		return;
	}
}
