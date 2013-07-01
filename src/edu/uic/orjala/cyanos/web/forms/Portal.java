/**
 * 
 */
package edu.uic.orjala.cyanos.web.forms;

import java.text.SimpleDateFormat;

import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.web.BaseForm;
import edu.uic.orjala.cyanos.web.CyanosWrapper;
import edu.uic.orjala.cyanos.web.News;
import edu.uic.orjala.cyanos.web.html.Definition;
import edu.uic.orjala.cyanos.web.html.Table;

/**
 * @author George Chlipala
 *
 */
@Deprecated
public class Portal extends BaseForm {

	/**
	 * 
	 */
	public final static String STRAIN_SUMMARY = "strain_summary";
	public final static String STRAIN_LIST = "strain_list";
	public final static String NEWS = "news";
	public final static String QUEUES = "queues";
	
	public final static String[] MODULES = { STRAIN_SUMMARY, STRAIN_LIST, NEWS, QUEUES };
	
	public Portal(CyanosWrapper aWrapper) {
		super(aWrapper);
	}

	public String moduleTitle(String moduleKey) {
		if ( moduleKey == null ) 
			return null;
		if ( moduleKey.equals(STRAIN_SUMMARY)) {
			return "Strain Summary";
		} else if ( moduleKey.equals(STRAIN_LIST)) {
			return "Strain Listing";
		} else if ( moduleKey.equals(NEWS)) {
			return "News";
		} else if ( moduleKey.equals(QUEUES)) {
			return "Queue Information";
		}
		return null;
	}
	
	public String module(String moduleKey) {
		if ( moduleKey == null ) 
			return null;
		if ( moduleKey.equals(STRAIN_SUMMARY)) {
//			return this.strainModule();
		} else if ( moduleKey.equals(STRAIN_LIST)) {
			
		} else if ( moduleKey.equals(NEWS)) {
			return this.newsModule();
		} else if ( moduleKey.equals(QUEUES)) {
			QueueForm qForm = new QueueForm(this.myWrapper);
			return qForm.queueModule(this.myWrapper.getRemoteUser());
		}
		return null;

	}
	
	public String newsModule() {
		Table newsTable = new Table("<TR><TD ALIGN='CENTER'></FONT SIZE='+1'>News</FONT></TD></TR>");
		
		try {
			News news = News.currentNews(this.getSQLDataSource());			
			Definition newsItems = new Definition();
			if ( news.first() )  {
				news.beforeFirst();
				SimpleDateFormat myFormat = this.myWrapper.dateTimeFormat();
				while ( news.next()) {
					String header = String.format("<B>%s</B> - <I>%s</I>", news.getSubject(), myFormat.format(news.getDateAdded()));
					newsItems.addDefinition(header, this.formatStringHTML(news.getSubject()));
				}
				newsTable.addItem(String.format("<TR><TD><DL WIDTH='80%%'>%s</DL></TD></TR>", newsItems.toString()));
			} else {
				newsTable.addItem("<TR><TD ALIGN='CENTER'><B>No News</B></TD></TR>");
			}
		} catch (DataException e) {
			newsTable.addItem("<TR><TD ALIGN='CENTER'><B><FONT COLOR='red'>ERROR:</FONT> " + e.getMessage() + "</B></TD></TR>");
		}
		newsTable.setAttribute("ALIGN", "CENTER");
		newsTable.setAttribute("WIDTH", "80%");

/*		try {
			dbc.close();
		} catch (SQLException e) {
			newsTable.addItem("<TR><TD><B><FONT COLOR='RED'>ERROR: </FONT>" + e.getMessage() + "</B></TD></TR>");
			e.printStackTrace();
		}
*/		
		return newsTable.toString();
	}
/*	
	public String strainModule() {
		TableHeader myHeader = new TableHeader("<P ALIGN='CENTER'><FONT SIZE='+1'>Strain Information</FONT></P>");
		TableRow myRow = new TableRow(myHeader);
		Table myTable = new Table(myRow);
		myTable.setAttribute("align", "center");
		TableCell myCell;

		try {
			Connection dbc = this.myServlet.dbh.getConnection();
			Statement sth = dbc.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

			ResultSet results = sth.executeQuery("SELECT COUNT(DISTINCT culture_id) FROM species WHERE removed IS NULL");
			results.last();
			myCell = new TableCell("Number of strains:");
			myCell.addItem(results.getString(1));
			myRow.addItem(myCell);

			results = sth.executeQuery("SELECT COUNT(DISTINCT t.ord) FROM species s JOIN taxonomic t ON t.genus = s.genus WHERE s.removed IS NULL");
			results.last();
			myCell = new TableCell("Number of taxonomic orders:");
			myCell.addItem(results.getString(1));
			myRow.addItem(myCell);
			
			results = sth.executeQuery("SELECT t.ord,COUNT(*) FROM species s JOIN taxonomic t ON t.genus = s.genus WHERE s.removed IS NULL GROUP BY t.ord");
			results.beforeFirst();
			HtmlList myList = new HtmlList();
			myList.unordered();
			myCell = new TableCell(myList);
			myCell.setAttribute("colspan", "0");
			myRow.addItem(myCell);
			
			while (results.next()) {
				myList.addItem(results.getString(1) + ": " + results.getString(2));
			}
		} catch (SQLException e) {
			myRow.addItem("<TD COLSPAN=2><B><FONT COLOR='RED'>ERROR: </FONT>" + e.getMessage() + "</B></TD>");
			e.printStackTrace();
		}

		Form queryForm = new Form();
		queryForm.setName("query");
		queryForm.setAttribute("action", "strain");
		Input searchField = new Input("text");
		searchField.setName("query");
		queryForm.addItem(searchField);
		queryForm.addSubmit("Search");
		myCell = new TableCell(queryForm);
		myCell.setAttribute("colspan", "0");
		myCell.setAttribute("align", "center");
		myRow.addItem(myCell);

		myCell = new TableCell("<A HREF='strain'>View strain list</A>");
		myCell.setAttribute("colspan", "0");
		myCell.setAttribute("align", "center");
*/
/*		
		try {
			dbc.close();
		} catch (SQLException e) {
			myRow.addItem("<TD COLSPAN=2><B><FONT COLOR='RED'>ERROR: </FONT>" + e.getMessage() + "</B></TD>");
			e.printStackTrace();
		}
*/
/*
		myRow.addItem(myCell);
		return myTable.toString();
	}
*/
}
