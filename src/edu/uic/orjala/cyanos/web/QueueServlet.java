/**
 * 
 */
package edu.uic.orjala.cyanos.web;

import java.io.PrintWriter;

import edu.uic.orjala.cyanos.web.forms.QueueForm;
import edu.uic.orjala.cyanos.web.html.Paragraph;
import edu.uic.orjala.cyanos.web.html.StyledText;

/**
 * @author George Chlipala
 *
 */
public class QueueServlet extends ServletObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 811651536165835064L;
	
	public void display(CyanosWrapper aWrap) throws Exception {

		String module = aWrap.getRequest().getPathInfo();
		PrintWriter out;
		
		aWrap.setContentType("text/html");
		
		QueueForm myForms = new QueueForm(aWrap);
		
		if ( module != null ) {
			if ( module.startsWith("/admin") ) {
				out = aWrap.startHTMLDoc("Queue Manager");

			} else if ( module.startsWith("/add") ) {
				out = aWrap.startHTMLDoc("Queue Manager", false);
				Paragraph head = new Paragraph();
				head.setAlign("CENTER");
				StyledText title = new StyledText("Add to a queue");
				title.setSize("+3");
				head.addItem(title);
				head.addItem("<HR WIDTH='85%'/>");
				out.println(head);
				if ( aWrap.hasFormValue("addAction") ) {
					out.println(myForms.addToQueue());
				} else 
					out.println(myForms.addForm());
			} else {
				out = aWrap.startHTMLDoc("Queue Manager");

				String[] details = module.split("/");
				if ( details.length == 2 ) {
					Paragraph head = new Paragraph();
					head.setAlign("CENTER");
					StyledText title = new StyledText("Queue List");
					title.setSize("+3");
					head.addItem(title);
					head.addItem("<HR WIDTH='85%'/>");
					out.println(String.format("<P ALIGN='CENTER'><A HREF='../user/%s/'>My Work Queue</A></P>", aWrap.getRemoteUser()));
					out.println(head.toString());
					out.println(myForms.listQueues(details[1]));
				} else if ( details.length > 0 ){
					Paragraph head = new Paragraph();
					head.setAlign("CENTER");
					StyledText title = new StyledText("Queue Detail");
					title.setSize("+3");
					head.addItem(title);
					head.addItem("<HR WIDTH='85%'/>");
					out.println(head.toString());
					out.println(myForms.queueDetails(details[1], details[2]));
				} else {
					Paragraph head = new Paragraph();
					head.setAlign("CENTER");
					StyledText title = new StyledText("Queue List");
					title.setSize("+3");
					head.addItem(title);
					head.addItem("<HR WIDTH='85%'/>");
					out.println(head.toString());
					out.println(String.format("<P ALIGN='CENTER'><A HREF='./queue/user/%s/'>My Work Queue</A></P>", aWrap.getRemoteUser()));
					out.println(myForms.listAllQueues());
				}
			}
		} else {
			out = aWrap.startHTMLDoc("Queue Manager");
			Paragraph head = new Paragraph();
			head.setAlign("CENTER");
			StyledText title = new StyledText("Queue List");
			title.setSize("+3");
			head.addItem(title);
			head.addItem("<HR WIDTH='85%'/>");
			out.println(head.toString());
			out.println(String.format("<P ALIGN='CENTER'><A HREF='./queue/user/%s/'>My Work Queue</A></P>", aWrap.getRemoteUser()));
			out.println(myForms.listAllQueues());
		}
		
		aWrap.finishHTMLDoc();
	}
		
}

