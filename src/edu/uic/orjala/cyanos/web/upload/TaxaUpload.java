/**
 * 
 */
package edu.uic.orjala.cyanos.web.upload;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.ListIterator;

import javax.servlet.http.HttpServletRequest;

import edu.uic.orjala.cyanos.Role;
import edu.uic.orjala.cyanos.User;
import edu.uic.orjala.cyanos.sql.SQLTaxon;
import edu.uic.orjala.cyanos.web.UploadForm;
import edu.uic.orjala.cyanos.web.html.HtmlList;

/**
 * @author George Chlipala
 *
 */
public class TaxaUpload extends UploadForm {

	public final static String TITLE = "Add Taxa";

	public final static String PARAM_NAME = "taxon_name";
	public final static String PARAM_LEVEL = "taxon_level";
	public final static String PARAM_PARENT = "taxon_parent";
	
	public final static String[] templateKeys = { PARAM_HEADER, PARAM_NAME, PARAM_LEVEL, PARAM_PARENT };
	private final static String[] templateHeader = {"Name","Level",	"Parent" };
	private final static String[] templateType = {"Required", "Required<br>e.g. genus, family, kingdom, etc.", "Required<br>Can be blank for a root taxon."};

	public static final String JSP_FORM = "/upload/forms/taxa.jsp";

	/**
	 * 
	 */
	public TaxaUpload(HttpServletRequest req) {
		super(req);
		this.accessRole = User.CULTURE_ROLE;
		this.permission = Role.WRITE;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		if ( this.working ) return;
		StringBuffer output = new StringBuffer();
		List<Integer> rowNum = this.rowList();
		this.done = 0;
		this.todos = rowNum.size();
		this.working = true;
		// Setup the row iterator.
		ListIterator<Integer> rowIter = rowNum.listIterator();
		HtmlList resultList = new HtmlList();
		resultList.unordered();

		try {
			int nameCol = Integer.parseInt(template.get(PARAM_NAME));
			int levelCol = Integer.parseInt(template.get(PARAM_LEVEL));
			int parentCol = Integer.parseInt(template.get(PARAM_PARENT));
			
			PreparedStatement addTaxon = myData.prepareStatement(SQLTaxon.SQL_INSERT_TAXON);
			PreparedStatement updateNodes = myData.prepareStatement(SQLTaxon.SQL_UPDATE_NODES);

			while (rowIter.hasNext() && this.working) {
				Integer row = (Integer)rowIter.next();
				if ( this.worksheet.gotoRow(row.intValue()) ) {
					HtmlList currResults = new HtmlList();
					currResults.unordered();

					try {
						String name = this.worksheet.getStringValue(nameCol);

						if ( name != null && name.length() > 0 ) {
							addTaxon.setString(1, name);
							addTaxon.setString(2, this.worksheet.getStringValue(levelCol));
							
							if ( addTaxon.executeUpdate() > 0 ) {
								String parent = this.worksheet.getStringValue(parentCol);
								updateNodes.setString(1, name);
								updateNodes.setString(2, parent);
								updateNodes.setString(3, name);
								updateNodes.setString(4, name);
								if ( updateNodes.executeUpdate() > 0 )
									currResults.addItem(SUCCESS_TAG + "Information updated.");
							}
						}
					} catch (SQLException e) {
						currResults.addItem("<FONT COLOR='red'><B>ERROR:</B></FONT> " + e.getMessage());
						e.printStackTrace();
					}
					resultList.addItem(String.format("Row #:%d %s", row, currResults.toString()));
				}
				this.done++;
			}
			addTaxon.close();
			updateNodes.close();
		} catch (Exception e) {
			output.append("<P ALIGN='CENTER'><B><FONT COLOR='red'>ERROR:</FONT>" + e.getMessage() + "</B></P>");
			e.printStackTrace();
			this.working = false;
		}
		try {
			if ( this.working ) { this.myData.commit(); output.append("<P ALIGN='CENTER'><B>EXECUTION COMPLETE</B> CHANGES COMMITTED.</P>"); }
			else { this.myData.rollback(); output.append("<P ALIGN='CENTER'><B>EXECUTION HALTED</B> Upload incomplete!</P>"); }
		} catch (SQLException e) {
			output.append("<P ALIGN='CENTER'><B><FONT COLOR='red'>ERROR:</FONT>" + e.getMessage() + "</B></P>");
			e.printStackTrace();			
		}
		output.append(resultList.toString());
		this.working = false;
		this.resultOutput = output.toString();	
		this.parseThread = null;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.UploadModule#getTemplateKeys()
	 */
	@Override
	public String[] getTemplateKeys() {
		return templateKeys;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.UploadForm#worksheetTemplate()
	 */
	@Override
	public String worksheetTemplate() {
		return this.worksheetTemplate(templateHeader, templateType);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.UploadForm#title()
	 */
	@Override
	public String title() {
		return TITLE;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.UploadForm#jspForm()
	 */
	@Override
	public String jspForm() {
		return JSP_FORM;
	}

}
