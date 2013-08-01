/**
 * 
 */
package edu.uic.orjala.cyanos.web.forms;

import java.text.SimpleDateFormat;

import edu.uic.orjala.cyanos.Assay;
import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.Project;
import edu.uic.orjala.cyanos.Role;
import edu.uic.orjala.cyanos.sql.SQLProject;
import edu.uic.orjala.cyanos.web.BaseForm;
import edu.uic.orjala.cyanos.web.CyanosWrapper;
import edu.uic.orjala.cyanos.web.html.Div;
import edu.uic.orjala.cyanos.web.html.Form;
import edu.uic.orjala.cyanos.web.html.Paragraph;
import edu.uic.orjala.cyanos.web.html.StyledText;
import edu.uic.orjala.cyanos.web.html.Table;
import edu.uic.orjala.cyanos.web.html.TableCell;
import edu.uic.orjala.cyanos.web.html.TableHeader;
import edu.uic.orjala.cyanos.web.html.TableRow;

/**
 * @author gchlip2
 *
 */
public class ProjectForm extends BaseForm {

	public static final String DIV_ID = "projectDiv";
	public static final String ADD_DIV_ID = "addProjectDiv";
	
	/**
	 * @param callingServlet
	 */
	public ProjectForm(CyanosWrapper aWrapper) {
		super(aWrapper);
	}

	public String viewProject(Project aProject) {
		Div mainDiv = new Div();
		if ( aProject.isAllowed(Role.WRITE) ) {				
			if ( this.hasFormValue("updateProject") ) {
				mainDiv.addItem(this.updateProject(aProject));
			}
			mainDiv.addItem(this.viewDiv(DIV_ID, this.projectText(aProject)));
			mainDiv.addItem(this.editDiv(DIV_ID, this.projectForm(aProject)));
		} else {
			mainDiv.addItem(this.projectText(aProject));
		}
		return mainDiv.toString();
	}
	
	private String projectText(Project aProject) {
		try {
			TableRow tableRow = new TableRow(String.format("<TD>Project Code:</TD><TD>%s</TD>", aProject.getID()));			
			tableRow.addItem(String.format("<TD>Project Title:</TD><TD>%s</TD>", aProject.getName()));
			tableRow.addItem(String.format("<TD>Description:</TD><TD>%s</TD>", aProject.getNotes()));
			Table myTable = new Table(tableRow);
			myTable.setClass("species");
			myTable.setAttribute("align", "center");
			return myTable.toString();
		} catch ( DataException e ) {
			return this.handleException(e);
		}
	}
	
	private String updateProject(Project aProject) {
		try {
			aProject.setManualRefresh();
			if ( this.hasFormValue("name") )
				aProject.setName(this.getFormValue("name"));
			if ( this.hasFormValue("notes") )
				aProject.setNotes(this.getFormValue("notes"));
			aProject.refresh();
			return this.message(SUCCESS_TAG, "Updated project.");
		} catch ( DataException e ) {
			return this.handleException(e);
		}
	}
	
	private String projectForm(Project aProject) {
		try {

			TableCell myCell = new TableCell("Project Code:");
			myCell.addItem(aProject.getID());
			TableRow tableRow = new TableRow(myCell);

			tableRow.addItem(this.makeFormTextRow("Project Title:", "name", aProject.getName()));
			tableRow.addItem(this.makeFormTextAreaRow("Description:", "notes", aProject.getNotes()));
			
			myCell = new TableCell("<INPUT TYPE=SUBMIT NAME='updateProject' VALUE='Update'/><INPUT TYPE=RESET />");
			myCell.setAttribute("colspan","2");
			myCell.setAttribute("align","center");
			tableRow.addItem(myCell);

			Table myTable = new Table(tableRow);
			myTable.setClass("species");
			myTable.setAttribute("align", "center");
			Form myForm = new Form(myTable);
			myForm.setAttribute("METHOD", "POST");
			myForm.addItem(String.format("<INPUT TYPE='HIDDEN' NAME='id' VALUE='%s'/>", aProject.getID()));
			myForm.setName("project");
			return myForm.toString();

		} catch ( DataException e ) {
			return this.handleException(e);
		}
		
	}
		
	public String listAssays(Project aProject) {
		String[] headers = { "Name", "Date", "Target" };
		TableCell aCell = new TableHeader(headers);
		TableRow aRow = new TableRow(aCell);
		Table aTable = new Table(aRow);

		try {
			Assay assays = aProject.assays();
			if ( assays != null ) {
				boolean oddRow = true;
				assays.beforeFirst();
				SimpleDateFormat myFormat = this.dateFormat();
				while (assays.next()) {
					aCell = new TableCell(String.format("<A HREF='assay?id=%s'>%s</A>", assays.getID(), assays.getName()));
					aCell.addItem(myFormat.format(assays.getDate()));
					aCell.addItem(assays.getTarget());
					aRow = new TableRow(aCell);
					if ( oddRow ) 
						aRow.setClass("odd");
					else
						aRow.setClass("even");
					oddRow = (! oddRow);
					aTable.addItem(aRow);
				}
			} else {
				aTable.addItem("<TR><TD ALIGN='CENTER' COLSPAN='3'><B>NONE</B></TD></TD>");
			}
		} catch (DataException e) {
			aTable.addItem("<TR><TD ALIGN='CENTER' COLSPAN='3'>");
			aTable.addItem(this.handleException(e));
			aTable.addItem("</TD></TR>");
		}
		return aTable.toString();
	}

	public String listProjects() {
		StringBuffer output = new StringBuffer();
		Paragraph head = new Paragraph();
		head.setAlign("CENTER");
		StyledText title = new StyledText("Project List");
		title.setSize("+3");
		head.addItem(title);
		head.addItem("<HR WIDTH='85%'/>");
		output.append(head.toString());

		String[] headers = { "Project Code", "Title", "Description"};
		TableCell aCell = new TableHeader(headers);
		TableRow aRow = new TableRow(aCell);
		Table aTable = new Table(aRow);
	
		try {
			Project allProjs = SQLProject.projects(this.getSQLDataSource(), SQLProject.ID_COLUMN, SQLProject.ASCENDING_SORT);
			allProjs.beforeFirst();
			while ( allProjs.next() ) {
				aCell = new TableCell(String.format("<A HREF='?id=%s'>%s</A>", allProjs.getID(), allProjs.getID()));
				aCell.addItem(allProjs.getName());
				String notes = allProjs.getNotes();
				if ( notes != null ) {
					aCell.addItem(notes.replaceAll("\n", "<BR>"));
				} else {
					aCell.addItem("");
				}
				aRow.addItem(aCell);
			}
		} catch (DataException e) {
			aTable.addItem("<TR><TD ALIGN='CENTER' COLSPAN='4'><B><FONT COLOR='red'>Error:</FONT> " + e.getMessage() + "</B></TD></TR>");
		}

		output.append("<P ALIGN='CENTER'>");
		output.append(aTable);
		output.append("</P>");
		
		output.append("<DIV ID='addProjectDiv'><P ALIGN='CENTER'><BUTTON TYPE='BUTTON' NAME='addForm' onClick=\"loadForm(this,'addProjectDiv');\">Create'</BUTTON></P></DIV>");
		return output.toString();

	}

	public String addProjectForm() {
		if ( this.hasFormValue("addNew")) {
			return this.addProject();
		} else {
			Form aForm = new Form("<P ALIGN='CENTER'><FONT SIZE='+1'><B>Create a new project</B></FONT></BR>");
			aForm.setAttribute("METHOD", "POST");
			TableCell aCell = new TableCell("Project Code:");
			aCell.addItem("<INPUT TYPE='TEXT' NAME='code'/>");
			TableRow aRow = new TableRow(this.makeFormTextRow("Project Code:", "code"));
			aRow.addItem(this.makeFormTextRow("Title:", "title"));
			aRow.addItem(this.makeFormTextAreaRow("Description:", "desc"));
			aRow.addItem("<TD COLSPAN='2' ALIGN='CENTER'><BUTTON TYPE='BUTTON' NAME='addNew' onClick=\"updateForm(this, 'addProjectDiv')\">Create</BUTTON><BUTTON TYPE='BUTTON' onClick=\"closeForm('addProjectDiv');\">Return</BUTTON></TD>");			
			
			Table aTable = new Table(aRow);
			aForm.addItem(aTable);
			aForm.addItem("</P>");
			return aForm.toString();
		}
	}
	
	public String addProject() {
		try {
			Project newProj = SQLProject.create(this.getSQLDataSource(), this.getFormValue("code"));
			newProj.setManualRefresh();
			newProj.setName(this.getFormValue("title"));
			newProj.setNotes(this.getFormValue("desc"));
			newProj.refresh();
			Div messageDiv = this.messageDiv(SUCCESS_TAG, String.format("Added project %s", this.getFormValue("code")));
			messageDiv.addItem("<BUTTON TYPE='BUTTON' onClick=\"closeForm('addProjectDiv');\">Close</BUTTON>");
			return messageDiv.toString();
		} catch (DataException e) {
			return this.handleException(e);
		}
	}

}
