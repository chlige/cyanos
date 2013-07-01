/**
 * 
 */
package edu.uic.orjala.cyanos.web;

import java.io.PrintWriter;

import edu.uic.orjala.cyanos.Harvest;
import edu.uic.orjala.cyanos.sql.SQLHarvest;
import edu.uic.orjala.cyanos.web.forms.HarvestForm;
import edu.uic.orjala.cyanos.web.html.Paragraph;
import edu.uic.orjala.cyanos.web.html.StyledText;

/**
 * @author George Chlipala
 *
 */
public class HarvestServlet extends ServletObject {
	 /**
	 * 
	 */
	private static final long serialVersionUID = -3807841657926086738L;
	private static final String HELP_MODULE = "isolation";

	public void display(CyanosWrapper aWrap) throws Exception 
	{
		PrintWriter out = aWrap.startHTMLDoc("Harvests");


		if ( aWrap.hasFormValue("id") ) {
			Harvest aHarvest = new SQLHarvest(aWrap.getSQLDataSource(), aWrap.getFormValue("id"));
			if ( aHarvest.first() ) {
				HarvestForm aForm = new HarvestForm(aWrap);
				out.println(aForm.harvestForm(aHarvest));
			} else {
				out.println("<P ALIGN='CENTER'><B><FONT COLOR='RED'>ERROR:</FONT> Harvest record does not exist.</B></P>");
			}
		} else {
			Paragraph head = new Paragraph();
			head.setAlign("CENTER");
			StyledText title = new StyledText("Harvest List");
			title.setSize("+3");
			head.addItem(title);
			head.addItem("<HR WIDTH='85%'/>");
			out.println(head);
			out.println("");
		}
		aWrap.finishHTMLDoc();
 }

	@Override
	protected String getHelpModule() {
		return HELP_MODULE;
	}
		

}
