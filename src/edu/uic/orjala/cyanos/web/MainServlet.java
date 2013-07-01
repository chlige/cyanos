package edu.uic.orjala.cyanos.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.User;
import edu.uic.orjala.cyanos.sql.SQLData;
import edu.uic.orjala.cyanos.web.forms.QueueForm;
import edu.uic.orjala.cyanos.web.forms.StrainForm;
import edu.uic.orjala.cyanos.web.html.Definition;
import edu.uic.orjala.cyanos.web.html.Div;
import edu.uic.orjala.cyanos.web.html.Form;
import edu.uic.orjala.cyanos.web.html.Image;
import edu.uic.orjala.cyanos.web.html.Input;
import edu.uic.orjala.cyanos.web.html.Paragraph;
import edu.uic.orjala.cyanos.web.html.StyledText;
import edu.uic.orjala.cyanos.web.html.Table;

public class MainServlet extends ServletObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5699551419430618192L;

	public void doPost ( HttpServletRequest req, HttpServletResponse res ) throws ServletException, IOException {
		if ( this.newInstall ) {
			super.doPost(req, res);
		} else {
			throw new ServletException("Posting to main servlet not allowed");
		}
	}

	protected void display(CyanosWrapper aWrap) throws Exception {
		PrintWriter out = aWrap.startHTMLDoc("Main Page");
		
		Div sideDiv = new Div();
		sideDiv.setClass("left25");
		sideDiv.addItem("<DIV STYLE='height:100px'></DIV>");
		Div mainDiv = new Div();
		mainDiv.setClass("right75");
		
		Paragraph head = new Paragraph();
		head.setAlign("CENTER");
		StyledText title = new StyledText("Cyanos Database");
		title.setSize("+3");
		head.addItem(title);
		mainDiv.addItem(head);
		mainDiv.addItem("<HR WIDTH='85%'/>");
		
		Table newsTable = new Table("<TR><TD ALIGN='CENTER'><B><FONT SIZE='+2'>News</FONT></B></TD></TR>");
		try {
			News news = News.currentNews(aWrap.getSQLDataSource());
			Definition newsItems = new Definition();
			if ( news.first() )  {
				news.beforeFirst();
				SimpleDateFormat myFormat = aWrap.dateTimeFormat();
				while (news.next()) {
					String header = String.format("<B>%s</B> - <I>%s</I>", news.getSubject(), myFormat.format(news.getDateAdded()));
					newsItems.addDefinition(header, news.getContent().replaceAll("\n", "<BR>"));
				}
				newsTable.addItem(String.format("<TR><TD><DL WIDTH='80%%'>%s</DL></TD></TR>", newsItems.toString()));
			} else {
				newsTable.addItem("<TR><TD ALIGN='CENTER'><B>No News</B></TD></TR>");
			}
		} catch (DataException e) {
			newsTable.addItem("<TR><TD ALIGN='CENTER'><B><FONT COLOR='red'>SQL ERROR:</FONT> " + e.getMessage() + "</B></TD></TR>");
		}
		newsTable.setAttribute("ALIGN", "CENTER");
		newsTable.setAttribute("WIDTH", "80%");
		mainDiv.addItem(newsTable);
		
		StrainForm aForm = new StrainForm(aWrap);
		mainDiv.addItem("<HR WIDTH='75%'/><P ALIGN='CENTER'><B><FONT SIZE='+2'>Strain Query</FONT></B></P>");
		mainDiv.addItem(aForm.queryForm());
		
		if ( aWrap.getRemoteUser() != null ) {
			Div aModule = new Div(this.userModule(aWrap));
			aModule.setClass("sideModule");
			sideDiv.addItem(aModule);
			QueueForm qForm = new QueueForm(aWrap);
			aModule = new Div(qForm.queueModule(aWrap.getRemoteUser()));
			aModule.setClass("sideModule");
			sideDiv.addItem(aModule);
		} else {
			sideDiv.addItem("<DIV CLASS='sideModule'><IFRAME SRC='login.jsp' scrolling=NO FRAMEBORDER=0 HEIGHT=190 WIDTH='100%'></IFRAME></DIV>");
		}
		
		Div strainMod = new Div(this.strainModule(aWrap));
		strainMod.setClass("sideModule");
		sideDiv.addItem(strainMod);
		
		mainDiv.addItem("<HR WIDTH='85%'/><P ALIGN=CENTER><I>" + this.getServletInfo() + "</I></P>");		
		out.println(sideDiv.toString());
		out.println(mainDiv.toString());
		aWrap.finishHTMLDoc();
	}

	protected String userModule(CyanosWrapper aWrap) throws DataException {
		StringBuffer output = new StringBuffer();
		SQLData myData = aWrap.getSQLDataSource();
		User aUser = myData.getUser();
		output.append("<P><B>Welcome, " + aUser.getUserName() + "</B></P>");
		output.append("<P><A HREF=\"self\">Update Profile</A><BR>");
		output.append("<A HREF=\"logout.jsp\">Logout</A></P>");
		return output.toString();
	}

	protected String strainModule(CyanosWrapper aWrap) throws SQLException {
		StrainForm aForm = new StrainForm(aWrap);
		
		Div moduleDiv = new Div(aForm.summaryTable());
		moduleDiv.setClass("hideSection");
		moduleDiv.setID("div_strainModule");

		Form queryForm = new Form();
		queryForm.setName("query");
		queryForm.setAttribute("action", "strain");
		Input searchField = new Input("text");
		searchField.setName("query");
		queryForm.addItem(searchField);
		queryForm.addSubmit("Search");
		
		moduleDiv.addItem(queryForm);
		
		Image anImage = aWrap.getImage("module-twist-closed.png");
		anImage.setAttribute("ID", "twist_strainModule");
		anImage.setAttribute("ALIGN", "absmiddle");
		return "<P CLASS='moduleTitle'><A NAME='strainModule' onClick='twistModule(\"strainModule\")'>" + anImage.toString() +
			"  Strain Information</A></P>" + moduleDiv.toString();
	}
}
