package edu.uic.orjala.cyanos.web.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.Inoc;
import edu.uic.orjala.cyanos.Strain;
import edu.uic.orjala.cyanos.sql.SQLInoc;
import edu.uic.orjala.cyanos.sql.SQLStrain;
import edu.uic.orjala.cyanos.web.forms.HarvestForm;
import edu.uic.orjala.cyanos.web.forms.InoculationForm;

public class InocServlet extends ServletObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 599148764116630073L;
	public static final String HELP_MODULE = "inoculation";
	public static final String RESULTS = "inocResults";
	public static final String SEARCHRESULTS_ATTR = "inocResults";
	
	public static final String ATTR_INOC_OBJECT = "inocObject";
	public static final String INFO_FORM_DIV_ID = "infoDiv";
	public static final String CHILDREN_DIV_ID = "inocKids";
	
	public static final String ATTR_INOC_LIST = "inocList";
/*
	public void display(CyanosWrapper aWrap) throws Exception {
		String module = aWrap.getRequest().getPathInfo();	
		
		if ( "/results".equals(module) ) {
			PrintWriter out = aWrap.getWriter();
			aWrap.setContentType("text/plain");
			List results = (List)aWrap.getSession().getAttribute(RESULTS);
			this.delimOutput(results, ",", out);
			aWrap.getSession().removeAttribute(RESULTS);
			out.flush();
			return;
		} else if ( aWrap.hasFormValue("div") ) {
			PrintWriter out = aWrap.getWriter();
			aWrap.setContentType("text/html; charset=UTF-8");
			String divID = aWrap.getFormValue("div");
			if ( aWrap.hasFormValue("livesearch") ) {
				InoculationForm inocForm = new InoculationForm(aWrap);
				out.println(inocForm.lsCultureID());
			} else if ( divID.equals(HarvestForm.SOURCE_DIV) ) {
				HarvestForm aForm = new HarvestForm(aWrap);
				out.print(aForm.inocForm());
			}
			out.flush();
			return;
		}
		
		PrintWriter out = aWrap.startHTMLDoc("Inoculations");
		InoculationForm inocForm = new InoculationForm(aWrap);
		
		if ( module == null ) {
			if ( aWrap.hasFormValue("id") ) {
				Paragraph head = new Paragraph();
				head.setAlign("CENTER");
				StyledText title = new StyledText("Inoculation Details");
				title.setSize("+3");
				head.addItem(title);
				head.addItem("<HR WIDTH='85%'/>");
				out.println(head);
				
				out.println(inocForm.showInoc(new SQLInoc(aWrap.getSQLDataSource(), aWrap.getFormValue("id"))));
			} else {
				Paragraph head = new Paragraph();
				head.setAlign("CENTER");
				StyledText title = new StyledText("Inoculations");
				title.setSize("+3");
				head.addItem(title);
				head.addItem("<HR WIDTH='85%'/>");
				out.println(head);
				out.println("<P ALIGN='CENTER'>This servlet must be called with an inoculation ID</P>");
			}
		} else if ( module.equals("/add") ) {
			Paragraph head = new Paragraph();
			StyledText title = new StyledText("Add Inoculations");
			title.setSize("+3");
			head.addItem(title);
			head.addItem("<HR WIDTH='85%'/>");
			head.setAlign("CENTER");
			out.println(head.toString());
			if ( aWrap.hasFormValue("addInocsAction")){
				out.println("<P ALIGN='CENTER'><A HREF='results'>Result List</A></P>");
				out.println(inocForm.addInocs());
				aWrap.getSession().setAttribute(this.RESULTS, inocForm.getResults());
			} else {
				out.println(inocForm.inocForm());
			}
		} else if ( module.equals("/harvest") ) {
			if ( aWrap.hasFormValue("addHarvest") ) {
				Paragraph head = new Paragraph();
				head.setAlign("CENTER");
				StyledText title = new StyledText("Adding Harvest");
				title.setSize("+3");
				head.addItem(title);
				head.addItem("<HR WIDTH='85%'/>");
				out.print(head.toString());
				HarvestForm aForm = new HarvestForm(aWrap);				
				out.print(aForm.addHarvest());
			} else if ( aWrap.hasFormValue("killInoc") )
				out.println(inocForm.killInoc());
			else {
				Paragraph head = new Paragraph();
				head.setAlign("CENTER");
				StyledText title = new StyledText("Add New Harvest");
				title.setSize("+3");
				head.addItem(title);
				head.addItem("<HR WIDTH='85%'/>");
				out.println(head);
				HarvestForm aForm = new HarvestForm(aWrap);
				out.println(aForm.addHarvestForm());
			}
		}
		
		aWrap.finishHTMLDoc();
	}
	
	*/
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.servlet.ServletObject#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		super.doGet(req, res);
		this.handleRequest(req, res);
		try {
			getSQLData(req).close();
		} catch (DataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.servlet.ServletObject#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		super.doPost(req, res);
		this.handleRequest(req, res);
		try {
			getSQLData(req).close();
		} catch (DataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}




	private void handleAJAX(HttpServletRequest req, HttpServletResponse res) throws DataException, SQLException, IOException, ServletException {
		res.setContentType("text/html; charset=UTF-8");
		String divID = req.getParameter("div");
		PrintWriter out = res.getWriter();

		if ( req.getParameter("livesearch") != null ) {
			this.lsCultureID(req, res);
		} else if ( divID.equals(HarvestForm.SOURCE_DIV) && req.getParameter("strain") != null ) {
			req.setAttribute(InocServlet.SEARCHRESULTS_ATTR, SQLInoc.openInocsForStrain(getSQLData(req), req.getParameter("strain")));
			this.forwardRequest(req, res, "/harvest/inoc-list.jsp");
		} else if ( divID.equals(INFO_FORM_DIV_ID) ) {
			req.setAttribute(ATTR_INOC_OBJECT, SQLInoc.load(getSQLData(req), req.getParameter("id")));
			this.forwardRequest(req, res, "/inoc/info-form.jsp");
		} else if ( divID.equals(CHILDREN_DIV_ID) ) {
			Inoc thisInoc = SQLInoc.load(getSQLData(req), req.getParameter("id"));
			req.setAttribute(SEARCHRESULTS_ATTR, thisInoc.getChildren());
			this.forwardRequest(req, res, "/inoc/inoc-list.jsp");
		} else if ( divID.equals("addTable") ) {
			if ( req.getParameter("strain") != null ) {
				req.setAttribute(StrainServlet.STRAIN_OBJECT, SQLStrain.load(getSQLData(req), req.getParameter("strain")));
				req.setAttribute(SEARCHRESULTS_ATTR, SQLInoc.viableInocsForStrain(getSQLData(req), req.getParameter("strain")));
			}
			this.forwardRequest(req, res, "/inoc/add-form-table.jsp");
		}
		out.flush();
	}
	
	public void lsCultureID(HttpServletRequest req, HttpServletResponse res) throws DataException, SQLException, IOException {
		String field = req.getParameter("livesearch");
		String[] likeColumns = { SQLStrain.ID_COLUMN };
		String[] likeValues = { String.format("%s%%", req.getParameter(field)) };
		Strain strains = SQLStrain.strainsLike(getSQLData(req), likeColumns, likeValues, SQLStrain.ID_COLUMN, SQLStrain.ASCENDING_SORT);

		PrintWriter out = res.getWriter();
		if ( strains.first() ) {
			strains.beforeFirst();
			while ( strains.next() ) {
				out.println(String.format("<A onClick='setLS(\"%s\",\"%s\",\"%s\")'>%s</A><BR>", field, strains.getID(), req.getParameter("div"), strains.getID()));
			}
		} else {
			out.print("No suggestions.");
		}
	}

	private void handleJSON(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException, DataException, SQLException {
		res.setContentType("application/json");
		String jsonType = req.getParameter("getJSON");
		PrintWriter out = res.getWriter();

		if ( jsonType.equals("strain") ) {
			Strain strain = SQLStrain.load(getSQLData(req), req.getParameter("strain"));
			if ( strain.first() ) {
			Inoc myInocs = SQLInoc.viableInocsForStrain(getSQLData(req), req.getParameter("strain"));
			out.println("{");
			out.print("\"media\": \"");
			out.print(strain.getDefaultMedia());
			out.println("\",");
			out.println("\"parents\": [");
			if ( myInocs.first() ) {
				out.print("{ \"id\": \"");
				out.print(myInocs.getID());
				out.print("\", \"date\": \"");
				out.print(DATE_FORMAT.format(myInocs.getDate()));
				out.print("\", \"volume\": \"");
				out.print(InoculationForm.autoFormatAmount(myInocs.getVolume(), SQLInoc.VOLUME_TYPE));
				out.print("\" }");
				while ( myInocs.next() ) {
					out.println(",");
					out.print("{ \"id\": \"");
					out.print(myInocs.getID());
					out.print("\", \"date\": \"");
					out.print(DATE_FORMAT.format(myInocs.getDate()));
					out.print("\", \"volume\": \"");
					out.print(InoculationForm.autoFormatAmount(myInocs.getVolume(), SQLInoc.VOLUME_TYPE));
					out.print("\" }");
				}
				out.println();
			}
			out.println("],");
			out.print("\"project\": \"");
			out.print(strain.getProjectID());
			out.print("\"");
			out.println(" }");
			}
			out.flush();
		}
	}
	
	public void handleRequest(HttpServletRequest req, HttpServletResponse res) throws ServletException,IOException {
		try {
			
			if ( req.getParameter("getJSON") != null ) {
				this.handleJSON(req, res);
				return;
			}

			if ( req.getParameter("div") != null ) {
					this.handleAJAX(req, res);
					return;
			} 
			
			if ( req.getParameter("id") != null ) 
				req.setAttribute(ATTR_INOC_OBJECT, SQLInoc.load(getSQLData(req), req.getParameter("id")));	
			else if ( req.getParameter("query") != null )
				req.setAttribute(SEARCHRESULTS_ATTR, SQLInoc.inocsForStrain(getSQLData(req), req.getParameter("query")));

			if ( req.getParameter("form") != null ) {
				String form = req.getParameter("form");
				if ( form.equals("add") ) {
					if ( req.getParameter("strain") != null ) {
						req.setAttribute(StrainServlet.STRAIN_OBJECT, SQLStrain.load(getSQLData(req), req.getParameter("strain")));
						req.setAttribute(SEARCHRESULTS_ATTR, SQLInoc.viableInocsForStrain(getSQLData(req), req.getParameter("strain")));
					}					
					this.forwardRequest(req, res, "/inoc/add.jsp");
				} else if ( form.equals("kill") ) {
					String[] ids = req.getParameterValues("inoc");
					if ( ids != null ) {
						List<Inoc> inocs = new ArrayList<Inoc>(ids.length);
						for ( String id : ids ) {
							inocs.add(SQLInoc.load(getSQLData(req), id));
						}
						req.setAttribute(ATTR_INOC_LIST, inocs); 
					}
					this.forwardRequest(req, res, "/inoc/kill-form.jsp");
				} else if ( form.equals("harvest") ) {
					req.setAttribute(SEARCHRESULTS_ATTR, SQLInoc.openInocsForStrain(getSQLData(req), req.getParameter("strain")));
					this.forwardRequest(req, res, "/harvest/add-harvest.jsp");
				}
			} else {
				this.forwardRequest(req, res, "/inoc.jsp");
			}
		} catch (DataException e) {
			throw new ServletException(e);
		} catch (SQLException e) {
			throw new ServletException(e);
		}

	}
	
	/*
	private String harvestForm() {
		StringBuffer output = new StringBuffer();

		Paragraph head = new Paragraph();
		head.setAlign("CENTER");
		StyledText title = new StyledText("Add Harvest");
		title.setSize("+3");
		head.addItem(title);
		head.addItem("<HR WIDTH='85%'/>");
		output.append(head);
		
		Form myForm = new Form("<P ALIGN='CENTER'>Strain: ");
		myForm.setName("harvest");
		myForm.setAttribute("METHOD","POST");		

		try {
			Popup strainPop = new Popup();
			try {
				strainPop = this.strainPopup();
				strainPop.setName("strain");
				strainPop.setAttribute("onChange", "this.form.submit()");

				myForm.addItem(strainPop);
			} catch (SQLException e) {
				myForm.addItem("SQL ERROR: " + e.getMessage());
			}
			myForm.addItem("</P>");

			if ( aWrap.getFormValue("strain") != null ) {
				strainPop.setDefault(aWrap.getFormValue("strain"));

				String[] headerData = {"", "Date", "Project Code", "Media", "Volume"};
				TableCell header = new TableHeader(headerData);
				header.setAttribute("class","header");
				TableRow inocRow = new TableRow(header);
				Table inocTable = new Table(inocRow);
				inocTable.setAttribute("class","dashboard");
				inocTable.setAttribute("align","center");
				inocTable.setAttribute("width","75%");


				try {
					ArrayList<String> inocList = new ArrayList<String>(java.util.Arrays.asList(aWrap.getFormValues("inoc")));
					Inoc myInocs = SQLInoc.openInocsForStrain(aWrap.getSQLDataSource(), aWrap.getFormValue("strain"));
					myInocs.beforeFirst();
					String curClass = "odd";
					SimpleDateFormat myFormat = this.dateFormat();
					Strain aStrain = new SQLStrain(aWrap.getSQLDataSource(), aWrap.getFormValue("strain"));
					while (myInocs.next()) {
						TableCell myCell = new TableCell();
						String projectID = myInocs.getProjectID();
						if ( projectID == null ) projectID = aStrain.getProjectID();
						if ( inocList.contains(myInocs.getID()) ) {
							myCell.addItem(String.format("<INPUT TYPE=CHECKBOX NAME='inoc' VALUE='%s' CHECKED onClick=\"if (this.checked) { this.form.project.value='%s';}\"/>", myInocs.getID(), projectID));									
						} else {
							myCell.addItem(String.format("<INPUT TYPE=CHECKBOX NAME='inoc' VALUE='%s' onClick=\"if (this.checked) { this.form.project.value='%s';}\"/>", myInocs.getID(), projectID));				
						}
						myCell.addItem(myFormat.format(myInocs.getDate()));
						myCell.addItem(myInocs.getProjectID());
						myCell.addItem(myInocs.getMedia());
						myCell.addItem(myInocs.getVolumeString(1.0f));
						TableRow aRow = new TableRow(myCell);
						aRow.setClass(curClass);
						aRow.setAttribute("align", "center");
						inocTable.addItem(aRow);
						if ( curClass.equals("odd") ) {
							curClass = "even";
						} else {
							curClass = "odd";
						}
					}
					myForm.addItem(inocTable);
				} catch (DataException e ) {
					myForm.addItem("<P ALIGN='CENTER'><B><FONT COLOR='red'>SQL ERROR:</FONT> " + e.getMessage() + "</B></P>");
					e.printStackTrace();
				}

				myForm.addItem("<HR WIDTH='70%'/>");
				TableRow tableRow = new TableRow(this.makeFormDateRow("Harvest Date:", "date", "harvest"));
				tableRow.addItem(this.makeFormTextRow("Color:", "color"));
				
				Strain aStrain = new SQLStrain(aWrap.getSQLDataSource(), aWrap.getFormValue("strain"));

				TableCell myCell = new TableCell("Project:");
				try {
					Popup projectPop = this.projectPopup();
					projectPop.setName("project");
					String myProject = aStrain.getProjectID();
					if ( myProject != null ) {
						projectPop.setDefault(myProject);
					}
					myCell.addItem(projectPop.toString());
				} catch (DataException e) {
					myCell.addItem("<B><FONT COLOR='red'>ERROR:</FONT> " + e.getMessage() + "</B>"); 
				}

				tableRow.addItem(myCell);
				
				myCell = new TableCell("Type");
				myCell.addItem("<INPUT TYPE=CHECKBOX NAME='type' VALUE='F'/> Filamentous<BR/>" +
						"<INPUT TYPE=CHECKBOX NAME='type' VALUE='E'/> Encrusting<BR/>" +
				"<INPUT TYPE=CHECKBOX NAME='type' VALUE='P'/> Planktonic");
				tableRow.addItem(myCell);
				
				myCell = new TableCell("Notes");
				myCell.addItem("<TEXTAREA NAME=\"notes\" COLS=40 ROWS=5></TEXTAREA>");
				myCell = new TableCell("<INPUT TYPE=SUBMIT NAME=\"addHarvest\" VALUE=\"Add Harvest\"/><INPUT TYPE=\"RESET\"/>");
				myCell.setAttribute("colspan","2");
				myCell.setAttribute("align","center");
				tableRow.addItem(myCell);
				
				Table myTable = new Table(tableRow);
				myTable.setClass("species");
				myTable.setAttribute("width", "80%");
				myTable.setAttribute("align", "center");
				myForm.addItem(myTable);
			}
		} catch (DataException e) {
			myForm.addItem("<B><FONT COLOR='red'>ERROR:</FONT> " + e.getMessage() + "</B></P>");
		}

		output.append(myForm.toString());
		return output.toString();
	}
	*/
	
	
	public String getHelpModule() {
		return HELP_MODULE;
	}
		

}
