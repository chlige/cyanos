/**
 * 
 */
package edu.uic.orjala.cyanos.web.upload;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ListIterator;

import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.sql.SQLData;
import edu.uic.orjala.cyanos.sql.SQLTaxon;
import edu.uic.orjala.cyanos.web.html.HtmlList;

/**
 * @author George Chlipala
 *
 */
public class TaxaUpload extends UploadJob {

	public final static String TITLE = "Add Taxa";

	public final static String PARAM_NAME = "taxon_name";
	public final static String PARAM_LEVEL = "taxon_level";
	public final static String PARAM_PARENT = "taxon_parent";
	
	public final static String[] templateKeys = { PARAM_NAME, PARAM_LEVEL, PARAM_PARENT };

	/**
	 * 
	 */
	public TaxaUpload(SQLData data) {
		super(data);
		this.type = TITLE;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		if ( this.working ) return;
		this.done = 0;
		this.todos = this.rowList.size();
		this.working = true;
		// Setup the row iterator.
		ListIterator<Integer> rowIter = this.rowList.listIterator();
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
			this.messages.append("<P ALIGN='CENTER'><B><FONT COLOR='red'>ERROR:</FONT>" + e.getMessage() + "</B></P>");
			e.printStackTrace();
			this.working = false;
		}
		this.messages.append(resultList.toString());
		this.finishJob();
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.UploadModule#getTemplateKeys()
	 */
	@Override
	public String[] getTemplateKeys() {
		return templateKeys;
	}

}
