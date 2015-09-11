/**
 * 
 */
package edu.uic.orjala.cyanos.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Source;
import edu.uic.orjala.cyanos.ContentVersion;
import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.Notebook;
import edu.uic.orjala.cyanos.NotebookObject;
import edu.uic.orjala.cyanos.NotebookPage;
import edu.uic.orjala.cyanos.Role;
import edu.uic.orjala.cyanos.User;

/**
 * @author George Chlipala
 *
 */
public class SQLNotebookPage extends SQLObject implements NotebookPage {
	
	public static final String PAGE_COLUMN = "page";
	public static final String TITLE_COLUMN = "title";
	public static final String CONTENT_COLUMN = "content";
	public static final String UPDATED_COLUMN = "date_updated";
	
	private static final String SQL_INSERT_PAGE = "INSERT INTO notebook_page(notebook_id,page,title,content) VALUES(?,?,?,?)";
	private static final String SQL_ARCHIVE_PAGE = "INSERT INTO notebook_page_history(notebook_id,page,version,content) (SELECT notebook_id,page,?,content FROM notebook_page WHERE notebook_id=? AND page=?)";
	private static final String SQL_UPDATE_PAGE = "UPDATE notebook_page SET content=? WHERE notebook_id=? AND page=?";
	
//	private static final String SQL_INSERT_WITH_PROJECT = "INSERT INTO notebook(notebook_id,project_id) VALUES(?,?)";
	private final static String SQL_LOAD_ALL = "SELECT notebook_page.* FROM notebook_page WHERE notebook_id=?";
	private final static String SQL_LOAD_PAGE = "SELECT notebook_page.* FROM notebook_page WHERE notebook_id=? AND page=?";

	private final static String SQL_CREATE_DATE = "SELECT COALESCE(MIN(h.date_created),p.date_updated) FROM notebook_page p LEFT OUTER JOIN notebook_page_history h ON(p.notebook_id = h.notebook_id AND p.page = h.page) WHERE p.notebook_id=? AND p.page=?";
	private final static String SQL_GET_NEXT_VERSION = "SELECT COALESCE(MAX(version),0)+1 FROM notebook_page_history WHERE notebook_id=? AND page=?";
	private final static String SQL_GET_VERSIONS = "SELECT version FROM notebook_page_history WHERE notebook_id=? AND page=? ORDER BY version ASC";

	private final static String SQL_GET_PREV_CONTENT = "SELECT version,content,date_created FROM notebook_page_history WHERE notebook_id=? AND page=? AND version=?";
	private final static String SQL_GET_ALL_PREV_CONTENT = "SELECT version,content,date_created FROM notebook_page_history WHERE notebook_id=? AND page=? ORDER BY version ASC";

	private final static String SQL_CLEAR_LINKS = "DELETE FROM notebook_links";
	private final static String SQL_ADD_LINK = "INSERT INTO notebook_links(notebook_id,page,object_class,object_id) VALUES(?,?,?,?)";
	
	
	private SQLNotebook notebook;
	
	protected SQLNotebookPage(SQLNotebook notebook) throws DataException {
		super(notebook.myData);
		this.notebook = notebook;
		this.myID = notebook.getID();
		this.idField = SQLNotebook.ID_COLUMN;	
		if ( ! notebook.myData.getUser().getUserID().equals(notebook.getUserID()) ) {
			this.myData.setAccessRole(User.PROJECT_MANAGER_ROLE);
		}
	}
	
	static SQLNotebookPage loadPage(SQLNotebook notebook, int page) throws DataException {
		SQLNotebookPage pageObj = new SQLNotebookPage(notebook);
		try { 
			PreparedStatement sth = pageObj.prepareStatement(SQL_LOAD_PAGE);
			sth.setString(1, notebook.getID());
			sth.setInt(2, page);
			pageObj.myData.loadUsingPreparedStatement(sth);
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return pageObj;
	}
	
	static SQLNotebookPage loadAllPages(SQLNotebook notebook) throws DataException {
		SQLNotebookPage pageObj = new SQLNotebookPage(notebook);
		try { 
			PreparedStatement sth = pageObj.prepareStatement(SQL_LOAD_ALL);
			sth.setString(1, notebook.getID());
			pageObj.myData.loadUsingPreparedStatement(sth);
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return pageObj;
	}
	
	protected static SQLNotebookPage addPage(SQLNotebook notebook, int page, String title, String content) throws DataException {
		SQLNotebookPage pageObj = null;
		try {
			PreparedStatement sth = notebook.prepareStatement(SQL_INSERT_PAGE, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			sth.setString(1, notebook.getID());
			sth.setInt(2, page);
			sth.setString(3, title);
			sth.setString(4, content);
			if ( sth.executeUpdate() == 1) {
				pageObj = SQLNotebookPage.loadPage(notebook, page);
				pageObj.updateLinks(content);
			}
			sth.close();
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return pageObj;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.NotebookPage#getNotebook()
	 */
	@Override
	public Notebook getNotebook() throws DataException {
		return notebook;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.NotebookPage#getPage()
	 */
	@Override
	public int getPage() throws DataException {
		return this.myData.getInt(PAGE_COLUMN);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.NotebookPage#getContent()
	 */
	@Override
	public String getContent() throws DataException {
		return this.myData.getString(CONTENT_COLUMN);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.NotebookPage#getTitle()
	 */
	@Override
	public String getTitle() throws DataException {
		return this.myData.getString(TITLE_COLUMN);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.NotebookPage#getCreationDate()
	 */
	@Override
	public Date getCreationDate() throws DataException {
		Date createDate = null;
		try {
			PreparedStatement sth = this.myData.prepareStatement(SQL_CREATE_DATE, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			sth.setString(1, this.notebook.getID());
			sth.setInt(2, this.getPage());
			ResultSet results = sth.executeQuery();
			if ( results.first() ) {
				createDate = results.getDate(1);
			}
			results.close();
			sth.close();
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return createDate;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.NotebookPage#getLastModifiedDate()
	 */
	@Override
	public Date getLastModifiedDate() throws DataException {
		return this.myData.getDate(UPDATED_COLUMN);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.NotebookPage#linkObject(edu.uic.orjala.cyanos.NotebookObject)
	 */
	@Override
	public void linkObject(NotebookObject object) throws DataException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.NotebookPage#getLinks()
	 */
	@Override
	public NotebookObject getLinks() throws DataException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.NotebookPage#getVersions()
	 */
	@Override
	public Integer[] getVersionNumbers() throws DataException {
		List<Integer> versions = new ArrayList<Integer>();
		try {
			PreparedStatement sth = this.myData.prepareStatement(SQL_GET_VERSIONS, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			sth.setString(1, this.notebook.getID());
			sth.setInt(2, this.getPage());
			ResultSet results = sth.executeQuery();
			while ( results.next() ) {
				versions.add(results.getInt(1));
			}
			results.close();
			sth.close();
		} catch (SQLException e) {
			throw new DataException(e);
		}
		Integer[] retarray = {};
		return versions.toArray(retarray);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.NotebookPage#updateContent(java.lang.String)
	 */
	@Override
	public void updateContent(String content) throws DataException {
		if ( this.notebook.isAllowed(Role.WRITE) ) {
			try {
				PreparedStatement sth = this.myData.prepareStatement(SQL_GET_NEXT_VERSION, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
				sth.setString(1, this.notebook.getID());
				sth.setInt(2, this.getPage());
				ResultSet results = sth.executeQuery();
				results.next();
				int version = results.getInt(1);
				results.close(); sth.close();
				sth = this.myData.prepareStatement(SQL_ARCHIVE_PAGE, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
				sth.setString(1, this.notebook.getID());
				sth.setInt(1, this.getPage());
				sth.setInt(2, version);
				if ( sth.executeUpdate() == 1) {
					sth.close();
					sth = this.myData.prepareStatement(SQL_UPDATE_PAGE, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
					sth.setString(1, content);
					sth.setString(2, this.notebook.getID());
					sth.setInt(3, this.getPage());
					if ( sth.executeUpdate() == 1 ) 
						this.updateLinks(content);
						this.myData.refresh();
				}
				sth.close();
			} catch (SQLException e) {
				throw new DataException(e);
			}
		}
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.sql.SQLObject#fetchRecord()
	 */
	@Override
	protected void fetchRecord() throws DataException {
		// TODO Auto-generated method stub

	}

	@Override
	public ContentVersion[] getVersions() throws DataException {
		List<ContentVersion> versions = new ArrayList<ContentVersion>();
		try {
			PreparedStatement sth = this.myData.prepareStatement(SQL_GET_ALL_PREV_CONTENT, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			sth.setString(1, notebook.getID());
			sth.setInt(2, this.getPage());
			ResultSet results = sth.executeQuery();
			while ( results.next() ) {
				versions.add(new ContentVersion(results.getInt(1), results.getDate(3), results.getString(2)));
			}
			results.close();
			sth.close();
		} catch (SQLException e) {
			throw new DataException(e);
		}
		ContentVersion[] retArray = {};
		return versions.toArray(retArray);
	}

	@Override
	public ContentVersion getVersion(int version) throws DataException {
		ContentVersion retVal = null;
		try {
			PreparedStatement sth = this.myData.prepareStatement(SQL_GET_PREV_CONTENT, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			sth.setString(1, notebook.getID());
			sth.setInt(2, this.getPage());
			sth.setInt(3, version);
			ResultSet results = sth.executeQuery();
			if ( results.next() ) {
				retVal = new ContentVersion(results.getInt(1), results.getDate(3), results.getString(2));
			}
			results.close();
			sth.close();
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return retVal;
	}
	
	private void updateLinks(String content) throws SQLException, DataException {
		int page = this.getPage();
		PreparedStatement sth = this.prepareStatement(SQL_CLEAR_LINKS, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		sth.executeUpdate();
		sth.close();
		sth = this.prepareStatement(SQL_ADD_LINK, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		Source source = new Source(content);
		List<Element> elements = source.getAllElementsByClass("notelink");
		for ( Element elem : elements ) {
			// id = cyanos-link:<class>:<id>
			String[] idvals = elem.getAttributeValue("id").split(":", 3);
			sth.setString(1, this.notebook.getID());
			sth.setInt(2, page);
			sth.setString(3, idvals[1]);
			sth.setString(4, idvals[2]);
			sth.addBatch();
		}
		sth.executeBatch();
		sth.close();
	}

	// THIS IS A STUB METHOD.
	public static List<String> parseLinks(String content) {
		List<String> links = new ArrayList<String>();
		Source source = new Source(content);
		List<Element> elements = source.getAllElementsByClass("notelink");
		for ( Element elem : elements ) {
			links.add(elem.getAttributeValue("id"));
		}
		return links;
	}
	
}
