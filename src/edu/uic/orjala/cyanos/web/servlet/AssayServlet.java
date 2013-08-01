/**
 * 
 */
package edu.uic.orjala.cyanos.web.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.uic.orjala.cyanos.Assay;
import edu.uic.orjala.cyanos.AssayData;
import edu.uic.orjala.cyanos.AssayPlate;
import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.Role;
import edu.uic.orjala.cyanos.Sample;
import edu.uic.orjala.cyanos.User;
import edu.uic.orjala.cyanos.sql.SQLAssay;
import edu.uic.orjala.cyanos.sql.SQLAssayTemplate;
import edu.uic.orjala.cyanos.sql.SQLData;
import edu.uic.orjala.cyanos.sql.SQLSeparationTemplate;
import edu.uic.orjala.cyanos.web.SheetWriter;
import edu.uic.orjala.cyanos.web.forms.ProtocolForm;

/**
 * @author George Chlipala
 *
 */
public class AssayServlet extends ServletObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3556286163243770282L;
	
	public static final String DATA_FILE_DIV_ID = "dataFiles";
	public static final String DATA_FILE_DIV_TITLE = "Data Files";
	public static final String DIV_DATA_LIST_ID = "assayList";
	public static final String DIV_DATA_LIST_TITLE = "Data List";
	public static final String DIV_PLATE_GRAPHIC_ID = "plateGraphic";
	public static final String DIV_PLATE_GRAPHIC_TITLE = "Graphic";
	public static final String DIV_PLATE_DATA_ID = "plateData";
	public static final String DIV_PLATE_DATA_TITLE = "Assay Data";
	public static final String DIV_PLATE_SAMPLE_ID = "plateSamples";
	public static final String DIV_PLATE_SAMPLE_TITLE = "Samples";
	public static final String INFO_FORM_DIV_ID = "assayInfo";
	
	public static final String SEARCHRESULTS_ATTR = "assays";
	public static final String TARGET_LIST = "targets";
	public static final String ASSAY_OBJECT = "assay";
	
//	public static final String ASSAY_PROTOCOL = "assay protocol";	
	public static final String PROTOCOL_OBJ = "assayProtocol";
	public static final String ALL_PROTOCOLS = "assayProtos";
	
	public static final String UPDATE_ACTION = "updateAssay";

//	public static String[] PROTOCOL_KEYS = {"trgt", "active_op", "active", "unit", "size", "project"};
	
	public static final String LS_TARGET = "target";
	
	private static final Map<String,String> operatorMap = new HashMap<String,String>(6);

	private static Map<String,String> dataTypeMap; 

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
			
			String servletPath = req.getServletPath();
			
			if ( servletPath.endsWith("export") || servletPath.endsWith(".csv") ) {
					res.setContentType("text/plain");
					if ( req.getParameter("id") != null )
						this.exportAssay(req, res);
					else
						this.exportAssayList(req, res);
					return;
			} else if ( servletPath.endsWith("protocol") ) {
				if ( req.getParameter("name") != null ) {
					String protoName = req.getParameter("name");
					if ( req.getParameter("createProtocol") != null ) {
						SQLAssayTemplate proto = SQLAssayTemplate.create(this.getSQLData(req), protoName);
						req.setAttribute(PROTOCOL_OBJ, proto);
						if ( req.getParameter(UPDATE_ACTION) != null )
							proto.save();
					} else if ( req.getParameter("confirmDelete") != null ) {
						SQLAssayTemplate.delete(this.getSQLData(req), protoName);
					} else
						req.setAttribute(PROTOCOL_OBJ, SQLSeparationTemplate.load(this.getSQLData(req), protoName));
				} 
				req.setAttribute(ALL_PROTOCOLS, SQLSeparationTemplate.listProtocols(this.getSQLData(req)));
				RequestDispatcher disp = getServletContext().getRequestDispatcher("/assay/assay-protocol.jsp");
				disp.forward(req, res);		
				return;
			} else if ( "add".equals(req.getParameter("action")) ) {
				if ( this.getUser(req).isAllowed(User.BIOASSAY_ROLE, User.NULL_PROJECT, Role.CREATE) ) {
					if ( req.getParameter(UPDATE_ACTION) != null ) {
						try {
							Assay assayObj = SQLAssay.create(this.getSQLData(req), req.getParameter("newID"));
							req.setAttribute(ASSAY_OBJECT, assayObj);	
							this.forwardRequest(req, res, "/assay.jsp");
						} catch (DataException e) {
							req.setAttribute("error_msg", e.getLocalizedMessage());
							this.forwardRequest(req, res, "/assay/assay-add.jsp");							
						}
					} else 
						this.forwardRequest(req, res, "/assay/assay-add.jsp");
				} else {
					res.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
				}
				return;
			} else if ( req.getParameter("id") != null ) {
				Assay thisAssay = SQLAssay.load(this.getSQLData(req), req.getParameter("id"));
				req.setAttribute(AssayServlet.ASSAY_OBJECT, thisAssay);
			} else {
				if ( req.getParameter("assaySearch") != null ) {
					String target = req.getParameter("target");
					if ( target.length() == 0 ) 
						req.setAttribute(SEARCHRESULTS_ATTR, SQLAssay.assays(this.getSQLData(req)));
					else
						req.setAttribute(SEARCHRESULTS_ATTR, SQLAssay.assaysForTarget(this.getSQLData(req), req.getParameter("target")));
				}
				req.setAttribute(TARGET_LIST, SQLAssay.targets(this.getSQLData(req)));
			}
			
		 this.forwardRequest(req, res, "/assay.jsp");
			
		} catch (DataException e) {
			throw new ServletException(e);
		} catch (SQLException e) {
			throw new ServletException(e);				
		}		
	}
	
	private void handleAJAX(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException, DataException, SQLException {
		res.setContentType("text/html; charset=UTF-8");
		PrintWriter out = res.getWriter();
		String divTag = req.getParameter("div");

		
//		if ( "/protocol".equals(module) && divTag.equals(ProtocolForm.DIV_ID) ) {
//			ProtocolForm aForm = new ProtocolForm(aWrap, AssayForm.ASSAY_PROTOCOL, AssayForm.PROTOCOL_KEYS);
//			Form retForm = aForm.protocolForm();
//			AssayForm protoForm = new AssayForm(this);
//			retForm.addItem(protoForm.protocolModule(aForm.getTemplate()));
//			out.print(retForm.toString());
//		} else {
			if ( req.getParameter("livesearch") != null ) {
				String searchTag = req.getParameter("livesearch");
				if ( searchTag.equals(LS_TARGET) ) {
					out.println(this.livesearchQuery(this.getSQLData(req), divTag, searchTag, req.getParameter(searchTag)));
				}
			}
			Assay myAssay = SQLAssay.load(this.getSQLData(req), req.getParameter("id"));
			if ( myAssay.first() ) {
				if ( myAssay.isAllowed(Role.READ) ) {
					if ( divTag.equals(DATA_FILE_DIV_ID) ) {
						RequestDispatcher disp = DataFileServlet.dataFileDiv(req, getServletContext(), myAssay, Assay.DATA_FILE_CLASS);
						disp.forward(req, res);			
					} else if ( divTag.equals(DIV_DATA_LIST_ID) ) {
						AssayData data = myAssay.getAssayData();
						req.setAttribute(AssayServlet.SEARCHRESULTS_ATTR, data);
						this.forwardRequest(req, res, "/assay/assay-data.jsp");
					} else if ( divTag.equals(DIV_PLATE_DATA_ID)) {
						req.setAttribute(AssayServlet.ASSAY_OBJECT, myAssay);
						this.forwardRequest(req, res, "/assay/assay-plate-data.jsp");
					} else if ( divTag.equals(DIV_PLATE_SAMPLE_ID) ) {
						req.setAttribute(AssayServlet.ASSAY_OBJECT, myAssay);
						this.forwardRequest(req, res, "/assay/assay-plate-material.jsp");
					} else if ( divTag.equals(DIV_PLATE_GRAPHIC_ID) ) {
						req.setAttribute(AssayServlet.ASSAY_OBJECT, myAssay);
						this.forwardRequest(req, res, "/assay/assay-plate.jsp");
					} else if ( divTag.equals(ProtocolForm.DIV_ID) ) {
//						ProtocolForm aForm = new ProtocolForm(aWrap, AssayForm.ASSAY_PROTOCOL, AssayForm.PROTOCOL_KEYS);
//						Form retForm = aForm.protocolForm();
//						out.print(retForm.toString());
					} else if ( divTag.equals(INFO_FORM_DIV_ID) ) {
						req.setAttribute(AssayServlet.ASSAY_OBJECT, myAssay);
						this.forwardRequest(req, res, "/assay/assay-form.jsp");
					}
				} else {
					out.print("ACCESS DENIED");
				}
			}
//		}
		out.flush();
//		out.close();
//		this.dbc.close();
		return;

	}
	
	/*
	
	public void display(CyanosWrapper aWrap) throws Exception {
		PrintWriter out;
		HttpServletRequest req = aWrap.getRequest();
		String module = req.getPathInfo();	
		
		if ( aWrap.getSession().getAttribute("dateFormatter") == null )
			aWrap.getSession().setAttribute("dateFormatter", aWrap.dateFormat());
		
		
		if ( "/export".equals(module) ) {
			out = aWrap.getWriter();
			aWrap.setContentType("text/plain");
			if ( aWrap.hasFormValue("id") )
				out.println(this.exportAssay(aWrap));
			else
				out.println(this.exportAssayList(aWrap));
			out.flush();
//			out.close();
//			this.dbc.close();
			return;
		} else if ( aWrap.hasFormValue("div") ) {
			aWrap.setContentType("text/html; charset=UTF-8");
			out = aWrap.getWriter();
			String divTag = aWrap.getFormValue("div");
			if ( "/protocol".equals(module) && divTag.equals(ProtocolForm.DIV_ID) ) {
				ProtocolForm aForm = new ProtocolForm(aWrap, AssayForm.ASSAY_PROTOCOL, AssayForm.PROTOCOL_KEYS);
				Form retForm = aForm.protocolForm();
//				AssayForm protoForm = new AssayForm(this);
//				retForm.addItem(protoForm.protocolModule(aForm.getTemplate()));
				out.print(retForm.toString());
			} else {
				if ( aWrap.hasFormValue("livesearch") ) {
					String searchTag = aWrap.getFormValue("livesearch");
					if ( searchTag.equals(LS_TARGET) ) {
						out.println(this.livesearchQuery(aWrap.getSQLDataSource(), divTag, searchTag, aWrap.getFormValue(searchTag)));
					}
				}
				Assay myAssay = SQLAssay.load(aWrap.getSQLDataSource(), aWrap.getFormValue("id"));
				if ( myAssay.first() ) {
					if ( myAssay.isAllowed(Role.READ) ) {
						if ( divTag.equals(DATA_FILE_DIV_ID) ) {
							RequestDispatcher disp = DataFileServlet.dataFileDiv(aWrap.getRequest(), getServletContext(), myAssay, Assay.DATA_FILE_CLASS);
							disp.forward(aWrap.getRequest(), aWrap.getResponse());			
//						} else if ( divTag.equals(AssayForm.DATA_FORM) ) {
//							AssayForm aForm = new AssayForm(aWrap);
//							out.println(aForm.dataForm(myAssay));
						} else if ( divTag.equals(DIV_DATA_LIST_ID) ) {
							AssayData data = myAssay.getAssayData();
							aWrap.getRequest().setAttribute(AssayServlet.SEARCHRESULTS_ATTR, data);
							RequestDispatcher disp = getServletContext().getRequestDispatcher("/assay/assay-data.jsp");
							disp.forward(aWrap.getRequest(), aWrap.getResponse());
//							AssayForm aForm = new AssayForm(aWrap);
//							out.println(aForm.assayList(myAssay));
						} else if ( divTag.equals(DIV_PLATE_DATA_ID)) {
							AssayForm aForm = new AssayForm(aWrap);
							out.println(aForm.showPlateData(myAssay));
						} else if ( divTag.equals(DIV_PLATE_SAMPLE_ID) ) {
							AssayForm aForm = new AssayForm(aWrap);
							out.println(aForm.showPlateSamples(myAssay));
						} else if ( divTag.equals(DIV_PLATE_GRAPHIC_ID) ) {
							aWrap.getRequest().setAttribute(AssayServlet.ASSAY_OBJECT, myAssay);
							RequestDispatcher disp = getServletContext().getRequestDispatcher("/assay/assay-plate.jsp");
							disp.forward(aWrap.getRequest(), aWrap.getResponse());
//							AssayForm aForm = new AssayForm(aWrap);
//							out.print(aForm.showPlateGraphic(myAssay));
						} else if ( divTag.equals(ProtocolForm.DIV_ID) ) {
							ProtocolForm aForm = new ProtocolForm(aWrap, AssayForm.ASSAY_PROTOCOL, AssayForm.PROTOCOL_KEYS);
							Form retForm = aForm.protocolForm();
							out.print(retForm.toString());
						} else if ( divTag.equals(INFO_FORM_DIV_ID) ) {
							aWrap.getRequest().setAttribute(AssayServlet.ASSAY_OBJECT, myAssay);
							RequestDispatcher disp = getServletContext().getRequestDispatcher("/assay/assay-form.jsp");
							disp.forward(aWrap.getRequest(), aWrap.getResponse());
						}
					} else {
						out.print("ACCESS DENIED");
					}
				}
			}
			out.flush();
//			out.close();
//			this.dbc.close();
			return;

		}


		Assay thisAssay = null;
		if ( aWrap.hasFormValue("id") ) {
			thisAssay = SQLAssay.load(aWrap.getSQLDataSource(), aWrap.getFormValue("id"));
			aWrap.getRequest().setAttribute(AssayServlet.ASSAY_OBJECT, thisAssay);
		} else {
			if ( aWrap.hasFormValue("assaySearch") ) {
				String target = aWrap.getFormValue("target");
				if ( target.length() == 0 ) 
					aWrap.getRequest().setAttribute(SEARCHRESULTS_ATTR, SQLAssay.assays(aWrap.getSQLDataSource()));
				else
					aWrap.getRequest().setAttribute(SEARCHRESULTS_ATTR, SQLAssay.assaysForTarget(aWrap.getSQLDataSource(), aWrap.getFormValue("target")));
			}
			aWrap.getRequest().setAttribute(TARGET_LIST, SQLAssay.targets(aWrap.getSQLDataSource()));
		}
		
	 if ( module.equals("/protocol") ) {
		out = aWrap.startHTMLDoc("Assay Data");
		Paragraph head = new Paragraph();
		head.setAlign("CENTER");
		StyledText title = new StyledText("Assay Protocols");
		title.setSize("+3");
		head.addItem(title);
		head.addItem("<HR WIDTH='85%'/>");
		out.println(head.toString());
		
		Div formDiv = ProtocolForm.formDiv("Load an Assay Template", "Save as an Assay Template");

		AssayForm protoForm = new AssayForm(aWrap);
		Form aForm = new Form(protoForm.protocolForm(null));
		aForm.addItem(formDiv);
		
		out.println(aForm.toString());
		
	} else if ( module.equals("/add") ) {
		out = aWrap.startHTMLDoc("Assay Data");
		Paragraph head = new Paragraph();
		head.setAlign("CENTER");
		StyledText title = new StyledText("Setup a new assay");
		title.setSize("+3");
		head.addItem(title);
		head.addItem("<HR WIDTH='85%'/>");
		out.println(head.toString());
		AssayForm aForm = new AssayForm(aWrap);
		if ( aWrap.hasFormValue("addAssay"))
			out.println(aForm.addAssay());
		else 
			out.println(aForm.addForm());
	}

		
//		if ( module == null || module.equals("/")) {
/*
			if ( thisAssay != null ) {
				Paragraph head = new Paragraph();
				head.setAlign("CENTER");
				StyledText title = new StyledText("Assay Data");
				title.setSize("+3");
				head.addItem(title);
				head.addItem("<HR WIDTH='85%'/>");
				out.println(head.toString());
				AssayForm aForm = new AssayForm(aWrap);
				out.println(aForm.assayInfo(thisAssay));
				out.println(aForm.loadableDiv(DIV_PLATE_GRAPHIC_ID, DIV_PLATE_GRAPHIC_TITLE));
				out.println(aForm.loadableDiv(DIV_DATA_LIST_ID, DIV_DATA_LIST_TITLE));
				out.println(aForm.loadableDiv(DATA_FILE_DIV_ID, DATA_FILE_DIV_TITLE));			
				out.println(aForm.loadableDiv(DIV_PLATE_DATA_ID, DIV_PLATE_DATA_TITLE));
				out.println(aForm.loadableDiv(DIV_PLATE_SAMPLE_ID, DIV_PLATE_SAMPLE_TITLE));
			} else {
				Paragraph head = new Paragraph();
				head.setAlign("CENTER");
				StyledText title = new StyledText("Assay List");
				title.setSize("+3");
				head.addItem(title);
				head.addItem("<HR WIDTH='85%'/>");
				aWrap.print(head.toString());
				aWrap.print(AssayForm.assayMenu(aWrap));
			}
	*/
/*
		RequestDispatcher disp = getServletContext().getRequestDispatcher("/assay.jsp");
		disp.forward(aWrap.getRequest(), aWrap.getResponse());
*/		
/*		} else if ( module.equals("/hits") ){
			Paragraph head = new Paragraph();
			head.setAlign("CENTER");
			StyledText title = new StyledText("Assay Hit List");
			title.setSize("+3");
			head.addItem(title);
			head.addItem("<HR WIDTH='85%'/>");
			out.println(head.toString());
			out.println(AssayForm.assayMenu(aWrap));
			if ( thisAssay != null ) {
				out.println(String.format("<P ALIGN='CENTER'><A HREF='../assay?id=%s'>Return to main assay form</A></P>", thisAssay.getID()));
				out.println(AssayForm.targetHitList(aWrap, SQLAssay.assaysForTarget(aWrap.getSQLDataSource(), thisAssay.getTarget())));
			} else if ( aWrap.hasFormValue("trgt")) {
				out.println(String.format("<P ALIGN='CENTER'><A HREF='../assay?trgt=%s'>Return to main assay form</A></P>", aWrap.getFormValue("trgt")));
				out.println(AssayForm.targetHitList(aWrap, SQLAssay.assaysForTarget(aWrap.getSQLDataSource(), aWrap.getFormValue("trgt"))));
			} else {
				out.println("<P ALIGN='CENTER'><A HREF='../assay'>Return to main assay form</A></P>");
			}
*/
/*
		aWrap.finishHTMLDoc();
	}
*/
	
	private void exportAssay(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
		PrintWriter out = res.getWriter();
		res.setContentType("text/plain");
		
		try { 
			Assay myAssay = SQLAssay.load(this.getSQLData(req), req.getParameter("id"));

			if ( myAssay.first() ) {
				if ( myAssay.isAllowed(Role.READ) ) {
					SheetWriter sheetOut = new SheetWriter(out);
					sheetOut.print("Assay ID");
					sheetOut.print("Location");
					sheetOut.print("Culture ID");
					sheetOut.print("Sample ID");
					sheetOut.print("Sample Label");
					sheetOut.print("Concentration (mg/ml)");
					sheetOut.println("Activity");
					
					AssayPlate myData = myAssay.getAssayData();
					myData.beforeFirstRow();
					String assayID = myAssay.getID();
					myData.firstColumn();
					while ( myData.nextLocationByRow() ) {
						if ( ! myData.currentLocationExists() ) continue;
						sheetOut.print(assayID);
						sheetOut.print(myData.currentLocation());
						sheetOut.print(myData.getStrainID());
						Sample aSample = myData.getSample();
						if ( aSample != null )
							sheetOut.print(aSample.getID());
						else
							sheetOut.print("");
						String name = myData.getLabel();
						if ( name != null ) 
							sheetOut.print(name.replaceAll("[\n\r]", " "));
						else
							sheetOut.print("");
						sheetOut.print(String.format("%f", myData.getConcentration()));
						sheetOut.println(myData.getActivityString());
					}
				}
			} else {
				out.println("ERROR: ASSAY NOT FOUND");
			}
		} catch (DataException e) {
			throw new ServletException(e);
		} catch (SQLException e) {
			throw new ServletException(e);
		}
		out.flush();
	
	}
	
	
	private void exportAssayList(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
		PrintWriter out = res.getWriter();
		res.setContentType("text/plain");
		try {
			Assay myAssay;
			
			if ( req.getParameter("target") != null ) {
				myAssay = SQLAssay.assaysForTarget(this.getSQLData(req), req.getParameter("target"));
			} else {
				myAssay = SQLAssay.assays(this.getSQLData(req));
			} 
			
			if ( myAssay.first() ) {
				SheetWriter sheetOut = new SheetWriter(out);

				sheetOut.print("Assay ID");
				sheetOut.print("Name");
				sheetOut.print("Date");
				sheetOut.print("Target");
				sheetOut.print("Size");
				sheetOut.println("Notes");

				myAssay.beforeFirst();
				while ( myAssay.next() ) {
					sheetOut.print(myAssay.getID());
					sheetOut.print(myAssay.getName());
					sheetOut.print(myAssay.getDateString());
					sheetOut.print(myAssay.getTarget());
					sheetOut.print(String.format("%dx%d", myAssay.getLength(), myAssay.getWidth()));
					String notes = myAssay.getNotes();
					if ( notes != null ) 
						sheetOut.println(notes.replaceAll("[\n\r]", " "));
					else
						sheetOut.println("");
				}
			} else {
				out.println("ERROR: ASSAY NOT FOUND.");
			}
		} catch (DataException e) {
			throw new ServletException(e);
		} catch (SQLException e) {
			throw new ServletException(e);
		}
	}
	
	public static Map<String,String> operatorMap() {
		if ( operatorMap.size() == 0 ) {
			operatorMap.put(Assay.OPERATOR_EQUAL,"=");
			operatorMap.put(Assay.OPERATOR_NOT_EQUAL,"!=");
			operatorMap.put(Assay.OPERATOR_GREATER_THAN,"&gt;");
			operatorMap.put(Assay.OPERATOR_GREATER_EQUAL,"&gt;=");
			operatorMap.put(Assay.OPERATOR_LESS_THAN,"&lt;");
			operatorMap.put(Assay.OPERATOR_LESS_EQUAL,"&lt;=");
		}
		return operatorMap;
	}
	
	public static String getOperatorText(String operator) {
		return operatorMap().get(operator);
	}


	public static List<String> targets(SQLData data) throws DataException {
		return SQLAssay.targets(data);
	}
	
	private String livesearchQuery(SQLData data, String divID, String parameter, String value) throws DataException, SQLException {
		String lval = String.format("%s%%", value);
		String lvalSpace = String.format("%% %s%%", value);
		
		String[] likeColumns = { SQLAssay.TARGET_COLUMN, SQLAssay.TARGET_COLUMN };
		String[] likeValues = { lval, lvalSpace };
		List<String> targets = SQLAssay.targetsLike(data, likeColumns, likeValues);

		if ( targets.size() > 0 ) {
			StringBuffer output = new StringBuffer();
//			output.append(String.format("<p align='right'><a onclick='closeLS(\"%s\")' class='closeBox'><b>X</b></a></p>", divID));
			for ( String target : targets ) {
				output.append(String.format("<A onMouseDown='closeLSdiv=false' onMouseUp='setLS(\"%s\",\"%s\",\"%s\")'>%s</A><BR>", parameter, target, divID, target));			
			}
			return output.toString();
		} else {
//			return String.format("<p align='right'><a class='closeBox' onclick='closeLS(\"%s\")'><b>X</b></a></p>No suggestions.", divID);
			return "No suggestions.";
		}
	}
	
	public static Map<String,String> getDataTypeMap() {
		if ( dataTypeMap == null ) {
			dataTypeMap = new HashMap<String,String>();
			dataTypeMap.put(Assay.REPORT_DATA_TYPE, "Assay Report");
			dataTypeMap.put(Assay.RAW_DATA_TYPE, "Raw Data");
		}
		return dataTypeMap;
	}

}
