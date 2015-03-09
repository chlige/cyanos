package edu.uic.orjala.cyanos.web.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.Material;
import edu.uic.orjala.cyanos.Role;
import edu.uic.orjala.cyanos.Separation;
import edu.uic.orjala.cyanos.sql.SQLCompound;
import edu.uic.orjala.cyanos.sql.SQLSeparation;
import edu.uic.orjala.cyanos.sql.SQLSeparationTemplate;
import edu.uic.orjala.cyanos.web.forms.ProtocolForm;

public class SeparationServlet extends ServletObject {


	public static final String PARAM_STATIONARY_PHASE = "sphase";
	public static final String PARAM_MOBILE_PHASE = "mphase";
	public static final String PARAM_METHOD = "method";

	/**
	 * 
	 */
	private static final long serialVersionUID = 5897244100880061390L;

	public static final String DATA_FILE_DIV_ID = "dataFiles";
	public static final String COMPOUND_DIV_ID = "compounds";
	public static final String SEARCHRESULTS_ATTR = "searchresults";
	public static final String SEP_OBJECT = "separation";
	public static final String PROTOCOL_OBJ = "sepProtocol";
	public static final String ALL_PROTOCOLS = "sepProtos";
	
	public static final String UPDATE_ACTION = "updateSep";

	public static final String DELIM = ",";

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

	private void handleRequest(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {

		try {
			if ( req.getParameter("div") != null ) {
				this.handleDivContent(req, res);
				return;
			}


			if ( req.getServletPath().endsWith("/export") ) {
				res.setHeader("Content-Disposition", "attachement; filename=\"export.csv\"");
				res.setContentType("text/csv");
				if ( req.getParameter("id") != null )
					this.exportSeparation(req, res);
				else 
					this.exportSepList(req, res);
				return;
			} else if ( req.getServletPath().endsWith("/protocol") ) {
				if ( req.getParameter("name") != null ) {
					String protoName = req.getParameter("name");
					if ( req.getParameter("createProtocol") != null ) {
						SQLSeparationTemplate proto = SQLSeparationTemplate.create(getSQLData(req), protoName);
						req.setAttribute(PROTOCOL_OBJ, proto);
						if ( req.getParameter(UPDATE_ACTION) != null )
							proto.save();
					} else if ( req.getParameter("confirmDelete") != null ) {
						SQLSeparationTemplate.delete(getSQLData(req), protoName);
					} else
						req.setAttribute(PROTOCOL_OBJ, SQLSeparationTemplate.load(getSQLData(req), protoName));
				} 
				req.setAttribute(ALL_PROTOCOLS, SQLSeparationTemplate.listProtocols(getSQLData(req)));
				RequestDispatcher disp = getServletContext().getRequestDispatcher("/separation/separation-protocol.jsp");
				disp.forward(req, res);
			} else {
				if ( req.getParameter("id") != null ) {
					Separation thisSep = new SQLSeparation(getSQLData(req), req.getParameter("id"));
					req.setAttribute(SEP_OBJECT, thisSep);
				} else if ( req.getParameter("query") != null ) {
					Separation objects = SQLSeparation.findForStrain(getSQLData(req), req.getParameter("query"));
					req.setAttribute(SEARCHRESULTS_ATTR, objects);
				}
				RequestDispatcher disp = getServletContext().getRequestDispatcher("/separation.jsp");
				disp.forward(req, res);
			}

		} catch (DataException e) {
			throw new ServletException(e);
		} catch (SQLException e) {
			throw new ServletException(e);				
		}

	}
	private void handleDivContent(HttpServletRequest request, HttpServletResponse response) throws DataException, IOException, SQLException, ServletException {
		response.setContentType("text/html; charset=UTF-8");
		String divTag = request.getParameter("div");
		String module = request.getPathInfo();
		
		if ( "/protocol".equals(module) && divTag.equals(ProtocolForm.DIV_ID) ) {
//			ProtocolForm aForm = new ProtocolForm(aWrap, AssayForm.ASSAY_PROTOCOL, AssayForm.PROTOCOL_KEYS);
//			Form retForm = aForm.protocolForm();
//			out.print(retForm.toString());
		} else if ( request.getParameter("livesearch") != null ) {
			StrainServlet.printCultureIDs(getSQLData(request), request, response);
		} else if ( request.getParameter("id") != null ) {
			Separation thisSep = new SQLSeparation(getSQLData(request), request.getParameter("id"));
			if ( thisSep.first() ) {
				if ( divTag.equals(DATA_FILE_DIV_ID) ) {
					RequestDispatcher disp = DataFileServlet.dataFileDiv(request, getServletContext(), thisSep, Separation.DATA_FILE_CLASS);
					disp.forward(request, response);									
				} else if ( divTag.equals(COMPOUND_DIV_ID) ) {
					request.setAttribute(CompoundServlet.COMPOUND_PARENT, thisSep);
					if ( thisSep.isAllowed(Role.WRITE) && request.getParameter("linkCompound") != null ) {
						String retTime = request.getParameter("retTime");
						String cmpdID = request.getParameter("cmpdID");
						String[] samples = request.getParameterValues("materialID");
						
					}
					if ( request.getParameter("showCmpdForm") != null ) 
						request.setAttribute(CompoundServlet.COMPOUND_LIST, SQLCompound.compounds(getSQLData(request), 
								SQLCompound.ID_COLUMN, SQLCompound.ASCENDING_SORT));
					else 
						request.setAttribute(CompoundServlet.COMPOUND_RESULTS, thisSep.getCompounds());
					RequestDispatcher disp = getServletContext().getRequestDispatcher("/includes/link-sep-compound.jsp");
					disp.forward(request, response);
/*					} else if ( divTag.equals("compoundForm") ) {
					request.setAttribute(CompoundServlet.COMPOUND_PARENT, thisSep);
					request.setAttribute(CompoundServlet.COMPOUND_LIST, SQLCompound.compounds(aWrap.getSQLDataSource(), SQLCompound.ID_COLUMN, SQLCompound.ASCENDING_SORT));
					RequestDispatcher disp = getServletContext().getRequestDispatcher("/includes/link-sep-compound.jsp");
					disp.forward(request, response);						
				} else if ( divTag.equals(SeparationForm.COMPOUND_FORM_ID) ) {
					out.println(myForm.compoundForm(thisSep));
*/
				}
			}
		}
		return;
	}

	private void exportSepList(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		PrintWriter out = res.getWriter();
		res.setContentType("text/plain");
		try {
			Separation thisSep = SQLSeparation.separations(getSQLData(req));
			if ( thisSep != null && thisSep.first() ) {
				out.print("Separation ID");
				out.print(DELIM);
				out.print("Strain ID");
				out.print(DELIM);
				out.print("Stationary Phase");
				out.print(DELIM);
				out.print("Mobile Phase");
				out.print(DELIM);
				out.print("Method");
				out.print(DELIM);
				out.print("Date");
				out.print(DELIM);
				out.println("Notes");

				while ( thisSep.next() ) {
					out.print(quoteString(thisSep.getID(), DELIM));
					out.print(DELIM);
					Material parents = thisSep.getSources();
					if ( parents != null && parents.first() ) 
						out.print(quoteString(parents.getCultureID(),DELIM));
					else
						out.print(DELIM);
					out.print(quoteString(thisSep.getStationaryPhase(),DELIM));
					out.print(DELIM);
					out.print(quoteString(thisSep.getMobilePhase(),DELIM));
					out.print(DELIM);
					out.print(quoteString(thisSep.getMethod(),DELIM));
					out.print(DELIM);
					out.print(quoteString(thisSep.getDateString(),DELIM));
					out.print(DELIM);
					out.print(quoteString(thisSep.getNotes(),DELIM));
					out.println();
				}
			} else {
				out.println("ERROR: SEPARATION NOT FOUND.");
			}
		} catch (DataException e) {
			throw new ServletException(e);
		} catch (SQLException e) {
			throw new ServletException(e);
		}
	}

	/*
	private String protocolModule(HttpServletRequest req) {
		Paragraph head = new Paragraph();
		head.setAlign("CENTER");
		StyledText title = new StyledText("Separation Protocols");
		title.setSize("+3");
		head.addItem(title);
		head.addItem("<HR WIDTH='85%'/>");
		Form uploadForm = new Form(head);
		uploadForm.setAttribute("METHOD", "POST");
		uploadForm.addItem("<CENTER>");
		
		SeparationProtocol myProtocol = null;
		try {
			myProtocol = this.buildProtocol(req);
		} catch (DataException e) {
			return aWrap.handleException(e);
		}
		
		if ( aWrap.hasFormValue("loadform") ) {
			uploadForm.addItem(this.loadProtocolTemplate(aWrap, myProtocol));
		} else if ( aWrap.hasFormValue("saveform") ) {
			uploadForm.addItem(this.saveProtocolTemplate(aWrap, myProtocol));
		} else {
			uploadForm.addItem("<INPUT TYPE='SUBMIT' NAME='loadform' VALUE='Load a protocol template'/>");
			uploadForm.addItem("<INPUT TYPE='SUBMIT' NAME='saveform' VALUE='Save as a protocol template'/><BR/>");
			uploadForm.addItem(this.protocolForm(aWrap, myProtocol));
		}
		uploadForm.addItem("</CENTER>");
		return uploadForm.toString();
	}
	*/

	private void exportSeparation(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		PrintWriter out = res.getWriter();
		try {
			Separation thisSep = new SQLSeparation(getSQLData(req), req.getParameter("id"));
			if ( thisSep.first() ) {
				out.print("Separation ID");
				out.print(DELIM);
				out.print("FR Number");
				out.print(DELIM);
				out.print("Sample ID");
				out.print(DELIM);
				out.print("Strain ID");
				out.print(DELIM);
				out.print("Sample Label");
				out.print(DELIM);
				out.print("Amount");
				out.println();
				String myID = thisSep.getID();
				Material parents = thisSep.getSources();
				if ( parents != null ) {
					parents.beforeFirst();
					while ( parents.next() ) {
						out.print(quoteString(myID,DELIM));
						out.print(DELIM);
						out.print(quoteString("SOURCE",DELIM));
						out.print(DELIM);
						out.print(quoteString(parents.getID(),DELIM));
						out.print(DELIM);
						out.print(quoteString(parents.getCultureID(),DELIM));
						out.print(DELIM);
						out.print(quoteString(parents.getLabel(),DELIM));
						out.print(DELIM);
						out.print(quoteString(SQLSeparation.autoFormatAmount(parents.getAmountForSeparationID(myID), SQLSeparation.MASS_TYPE),DELIM));
						out.println();
					}
				}
				thisSep.beforeFirstFraction();
				while ( thisSep.nextFraction() ) {
					Material aFrac = thisSep.getCurrentFraction();
					out.print(quoteString(myID,DELIM));
					out.print(DELIM);
					out.print(quoteString(String.valueOf(thisSep.getCurrentFractionNumber()),DELIM));
					out.print(DELIM);
					out.print(quoteString(aFrac.getID(),DELIM));
					out.print(DELIM);
					out.print(quoteString(aFrac.getCultureID(),DELIM));
					out.print(DELIM);
					out.print(quoteString(aFrac.getLabel(),DELIM));
					out.print(DELIM);
					out.print(quoteString(aFrac.displayAmount(),DELIM));
					out.println();
				}
			} else {
				out.println("ERROR: SEPARATION NOT FOUND.");
			}
		} catch (DataException e) {
			throw new ServletException(e);
		} catch (SQLException e) {
			throw new ServletException(e);
		}
	}
}
