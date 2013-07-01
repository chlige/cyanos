/**
 * 
 */
package edu.uic.orjala.cyanos.web;

import java.io.PrintWriter;

import edu.uic.orjala.cyanos.Project;
import edu.uic.orjala.cyanos.sql.SQLProject;
import edu.uic.orjala.cyanos.web.forms.CollectionForm;
import edu.uic.orjala.cyanos.web.forms.ProjectForm;
import edu.uic.orjala.cyanos.web.forms.SampleForm;
import edu.uic.orjala.cyanos.web.forms.StrainForm;
import edu.uic.orjala.cyanos.web.html.Paragraph;
import edu.uic.orjala.cyanos.web.html.StyledText;

/**
 * @author George Chlipala
 *
 */
public class ProjectServlet extends ServletObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5740025290717824706L;
	private static final String COL_DIV = "collection";
	private static final String STRAIN_DIV = "strain";
	private static final String SAMPLE_DIV = "sample";
	private static final String ASSAY_DIV = "assay";
	
	
	public void display(CyanosWrapper aWrap) throws Exception {

		PrintWriter out;
		ProjectForm projectForm = new ProjectForm(aWrap);
		
		if ( aWrap.hasFormValue("div") ) {
			aWrap.setContentType("text/html");
			out = aWrap.getWriter();
			String divTag = aWrap.getFormValue("div");
			if ( divTag.equals(ProjectForm.ADD_DIV_ID) ) {
				out.print(projectForm.addProject());
			} else {
				Project aProject = new SQLProject(aWrap.getSQLDataSource(), aWrap.getFormValue("id"));
				if ( aProject.first() ) {
					if ( divTag.equals(COL_DIV) ) {
						CollectionForm aForm = new CollectionForm(aWrap);
						out.print(aForm.collectionList(aProject.collections()));
					} else if ( divTag.equals(STRAIN_DIV) ) {
						StrainForm aForm = new StrainForm(aWrap);
						out.print(aForm.listSpeciesTable(aProject.strains()));
					} else if ( divTag.equals(SAMPLE_DIV) ) {
						SampleForm aForm = new SampleForm(aWrap);
						out.print(aForm.sampleListContent(aProject.samples(), false));
					} else if ( divTag.equals(ASSAY_DIV) ) {
						out.print(projectForm.listAssays(aProject));
					}
				}
			}
//			this.closeSQL();
//			out.close();
			out.flush();
			return;
		}
		
		out = aWrap.startHTMLDoc("Project Manager");
		
		
		if ( aWrap.hasFormValue("id")) {
			Project aProject = new SQLProject(aWrap.getSQLDataSource(), aWrap.getFormValue("id"));
			Paragraph head = new Paragraph();
			head.setAlign("CENTER");
			StyledText title = new StyledText("Project Details");
			title.setSize("+3");
			head.addItem(title);
			head.addItem("<HR WIDTH='85%'/>");
			out.println(head.toString());

			out.println(projectForm.viewProject(aProject));
			out.println(projectForm.loadableDiv(COL_DIV, "Collections").toString());
			out.println(projectForm.loadableDiv(STRAIN_DIV, "Strains").toString());
			out.println(projectForm.loadableDiv(SAMPLE_DIV, "Samples"));
			out.println(projectForm.loadableDiv(ASSAY_DIV, "Assays"));
		} else {
			out.println(projectForm.listProjects());
		}
		
		aWrap.finishHTMLDoc();
	}

}
