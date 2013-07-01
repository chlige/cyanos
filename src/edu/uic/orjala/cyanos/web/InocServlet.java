package edu.uic.orjala.cyanos.web;

import java.io.PrintWriter;
import java.util.List;

import edu.uic.orjala.cyanos.sql.SQLInoc;
import edu.uic.orjala.cyanos.web.forms.HarvestForm;
import edu.uic.orjala.cyanos.web.forms.InoculationForm;
import edu.uic.orjala.cyanos.web.html.Paragraph;
import edu.uic.orjala.cyanos.web.html.StyledText;

public class InocServlet extends ServletObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 599148764116630073L;
	private static final String HELP_MODULE = "inoculation";
	private final String RESULTS = "inocResults";
	
	public void display(CyanosWrapper aWrap) throws Exception {
		String module = aWrap.getRequest().getPathInfo();	
		
		if ( "/results".equals(module) ) {
			PrintWriter out = aWrap.getWriter();
			aWrap.setContentType("text/plain");
			List results = (List)aWrap.getSession().getAttribute(this.RESULTS);
			out.print(this.delimOutput(results, ","));
			aWrap.getSession().removeAttribute(this.RESULTS);
			out.flush();
			return;
		} else if ( aWrap.hasFormValue("div") ) {
			PrintWriter out = aWrap.getWriter();
			aWrap.setContentType("text/html");
			String divID = aWrap.getFormValue("div");
			if ( divID.equals(HarvestForm.SOURCE_DIV) ) {
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
	
	@Override
	protected String getHelpModule() {
		return HELP_MODULE;
	}
		

}
