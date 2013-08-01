/**
 * 
 */
package edu.uic.orjala.cyanos.web.servlet;

import java.io.PrintWriter;

import edu.uic.orjala.cyanos.Cryo;
import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.SampleCollection;
import edu.uic.orjala.cyanos.sql.SQLCryo;
import edu.uic.orjala.cyanos.sql.SQLSampleCollection;
import edu.uic.orjala.cyanos.web.CyanosWrapper;
import edu.uic.orjala.cyanos.web.forms.CryoForm;
import edu.uic.orjala.cyanos.web.html.Paragraph;
import edu.uic.orjala.cyanos.web.html.StyledText;

/**
 * @author George Chlipala
 *
 */
public class CryoServlet extends ServletObject {

	private static final long serialVersionUID = 3327782999474628574L;

	public void display(CyanosWrapper aWrap) throws Exception {
		
		 String module = aWrap.getRequest().getPathInfo();	

		 Cryo aCryo = null;
		 if ( aWrap.hasFormValue("id") ) {
			 aCryo = new SQLCryo(aWrap.getSQLDataSource(), aWrap.getFormValue("id"));
		 }
		 
		 PrintWriter out = aWrap.startHTMLDoc("Cryopreservations", (! "/print".equals(module)));
		 CryoForm aForm = new CryoForm(aWrap);		 
		 
		 if ( module == null ) {
			 if ( aCryo == null ) {
				Paragraph head = new Paragraph();
				head.setAlign("CENTER");
				StyledText title = new StyledText("Cryopreservation List");
				title.setSize("+3");
				head.addItem(title);
				head.addItem("<HR WIDTH=\"85%\"/>");
				out.println(head);
				out.println(aForm.listCollections());
			 } else if (aCryo.first()) {
				 Paragraph head = new Paragraph();
				 head.setAlign("CENTER");
				 StyledText title = new StyledText("Cryopreservation Details");
				 title.setSize("+3");
				 head.addItem(title);
				 head.addItem("<HR WIDTH='85%'/>");
				 out.println(head);
				 out.println(aForm.showCryo(aCryo));
			 } else 
				 out.println("<P ALIGN='CENTER'><B><FONT COLOR='red'>ERROR:</FONT> SAMPLE NOT FOUND!</B></P>");
		 } else if ( module.equals("/report") ) {
			// out.println(this.cryoReport());
		 } else if ( module.equals("/print") ) {
			 Paragraph head = new Paragraph();
			 head.setAlign("CENTER");
			 try {
				 SampleCollection aCol = SQLSampleCollection.load(aWrap.getSQLDataSource(), aWrap.getFormValue("col"));		
				 if ( aCol.first() ) {
					 StyledText title = new StyledText("Collection: " + aWrap.getFormValue("col"));
					 title.setSize("+3");
					 head.addItem(title);
					 head.addItem("<HR WIDTH='85%'/>");
					 out.println(head.toString());
//					 out.println(this.printBox(aCol));
				 } else {
					 head.addItem("<FONT COLOR='red'><B>Collection not found!</B></FONT>");
					 out.println(head.toString());
				 }
			 } catch (DataException e) {
				 head.addItem("<B><FONT COLOR='red'>ERROR: </FONT>");
				 head.addItem(e.getMessage());
				 head.addItem("</B>");
				 out.println(head.toString());
			 }
			 out.println("");
		 } else if ( module.equals("/add") ) {
			 Paragraph head = new Paragraph();
			 head.setAlign("CENTER");
			 StyledText title = new StyledText("Add Cryo Vial");
			 title.setSize("+3");
			 head.addItem(title);
			 head.addItem("<HR WIDTH='85%'/>");
			 out.println(head);
			 if ( aWrap.hasFormValue("addVials"))
				 out.println(aForm.addCryo());
			 else
				 out.println(aForm.listCollections(true));
		 } else if ( module.equals("/newCollection") ) {
			 Paragraph head = new Paragraph();
			 head.setAlign("CENTER");
			 StyledText title = new StyledText("Add Cryo Collection");
			 title.setSize("+3");
			 head.addItem(title);
			 head.addItem("<HR WIDTH='85%'/>");
			 out.println(head);
//			 out.println(this.addCryoBox());
		 } 

		aWrap.finishHTMLDoc();

	 }
	
}
