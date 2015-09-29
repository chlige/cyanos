/**
 * 
 */
package edu.uic.orjala.cyanos.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.Notebook;
import edu.uic.orjala.cyanos.NotebookObject;
import edu.uic.orjala.cyanos.NotebookPage;
import edu.uic.orjala.cyanos.Project;
import edu.uic.orjala.cyanos.Role;
import edu.uic.orjala.cyanos.User;

/**
 * @author George Chlipala
 *
 */
public class SQLNotebook extends SQLObject implements Notebook {

	public static final String ID_COLUMN = "notebook_id";
	public static final String TITLE_COLUMN = "title";
	public static final String DESC_COLUMN = "description";
	public static final String USER_COLUMN = "username";
	public static final String PROJECT_COLUMN = "project_id";
	
//	private static final String SQL_LOAD_TEMPLATE = "SELECT notebook.* FROM notebook WHERE %s ORDER BY %s %s";
	private static final String SQL_INSERT_WITH_USER = "INSERT INTO notebook(notebook_id,username) VALUES(?,?)";
//	private static final String SQL_INSERT_WITH_PROJECT = "INSERT INTO notebook(notebook_id,project_id) VALUES(?,?)";
	private final static String SQL_LOAD = "SELECT notebook.* FROM notebook WHERE notebook_id=?";
	private final static String SQL_LOAD_USER = "SELECT notebook.* FROM notebook WHERE username=?";
	private final static String SQL_LOAD_FOR_PROJECT = "SELECT notebook.* FROM notebook WHERE project_id=?";

	private final static String SQL_GET_PAGE_COUNT = "SELECT COALESCE(COUNT(page),0) FROM notebook_page WHERE notebook_id=?";
	private final static String SQL_GET_FIRST_DATE = "SELECT MIN(h.date_created), MIN(p.date_updated) FROM notebook_page p LEFT OUTER JOIN notebook_page_history h ON(h.notebook_id = p.notebook_id) WHERE p.notebook_id=?";
	private final static String SQL_GET_LATEST_DATE = "SELECT MAX(p.date_updated) FROM notebook_page p WHERE p.notebook_id=?";
	
	/**
	 * Retrieve notebooks for the specified user.
	 * 
	 * @param data SQLData object
	 * @param aUser User object for specified user
	 * @return Notebook object containing the notebooks.
	 * @throws DataException
	 */
	public static Notebook notebooksForUser(SQLData data, User aUser) throws DataException, SQLException {
		SQLNotebook aNotebook = new SQLNotebook(data);
		PreparedStatement sth = aNotebook.prepareStatement(SQL_LOAD_USER);
		sth.setString(1, aUser.getUserID());
		aNotebook.loadUsingPreparedStatement(sth);
		return aNotebook;
	}
	
	/**
	 * Retrieve notebooks for the current user.
	 * 
	 * @param data SQLData object
	 * @return Notebook object containing the notebooks.
	 * @throws DataException
	 * @throws SQLException 
	 */
	public static Notebook myNotebooks(SQLData data) throws DataException, SQLException {
		return SQLNotebook.notebooksForUser(data, data.getUser());
	}

	/**
	 * Retrieve notebooks for project.
	 * 
	 * @param data SQLData object
	 * @return Notebook object containing the notebooks.
	 * @throws DataException
	 * @throws SQLException 
	 */
	public static Notebook projectNotebooks(SQLData data, String projectID) throws DataException, SQLException {
		if ( data.isAllowedForProject(Role.READ, projectID)  ) {
			SQLNotebook aNotebook = new SQLNotebook(data);
			PreparedStatement sth = aNotebook.prepareStatement(SQL_LOAD_FOR_PROJECT);
			sth.setString(1, projectID);
			aNotebook.loadUsingPreparedStatement(sth);
			return aNotebook;
		} 
		return null;
	}

	/**
	 * Creates a new notebook for the current user.
	 * 
	 * @param data SQLData object
	 * @param anID ID for the new notebook. Can be any combination of alphanumeric characters.
	 * @return Notebook object for the new notebook.
	 * @throws DataException
	 */
	public static Notebook createNotebook(SQLData data, String anID) throws DataException {
		SQLNotebook aNotebook = new SQLNotebook(data);
		try {
			PreparedStatement aSth = data.prepareStatement(SQL_INSERT_WITH_USER);
			aSth.setString(1, anID);
			aSth.setString(2, data.user.getUserID());
			if ( aSth.executeUpdate() > 0 ) {
				aNotebook.myID = anID;
				aNotebook.fetchRecord();
			} 
			aSth.close();
		} catch ( SQLException e ) {
			throw new DataException(e);
		}
		return aNotebook;
	}
	
	public static Notebook loadNotebook(SQLData data, String anID) throws DataException {
		SQLNotebook aNotebook = new SQLNotebook(data);
		aNotebook.myID = anID;
		aNotebook.fetchRecord();
		if ( aNotebook.isAllowed(Role.READ) ) 
			return aNotebook;	
		return null;
	}
	
	protected SQLNotebook(SQLData data) {
		super(data);
		this.initVals();
	}

	protected void initVals() {
		this.idField = ID_COLUMN;
		this.myData.setProjectField(PROJECT_COLUMN);
	}

	/**
	 * Retrieve notebook for an ID.
	 * 
	 * @param data SQLData object
	 * @param anID ID of desired Notebook
	 * @throws DataException
	 */
	public SQLNotebook(SQLData data, String anID) throws DataException {
		super(data);
		this.myID = anID;
		this.initVals();
		this.fetchRecord();
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.sql.SQLObject#fetchRecord()
	 */
	
	protected void fetchRecord() throws DataException {
		this.fetchRecord(SQL_LOAD);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Notebook#addPage(int, edu.uic.orjala.cyanos.NotebookObject)
	 */
	public void linkObject(int page, NotebookObject anObject) throws DataException {
		anObject.setNotebook(this, page);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Notebook#getDescription()
	 */
	public String getDescription() throws DataException {
		return this.myData.getString(DESC_COLUMN);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Notebook#getName()
	 */
	public String getTitle() throws DataException {
		return this.myData.getString(TITLE_COLUMN);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Notebook#getUser()
	 */
	public User getUser() throws DataException {
		String userID = this.getUserID();
		if ( this.myData.user.getUserID().equals(userID) ) {
			return this.getUser();
		} else if ( userID != null ) {
			User aUser = new SQLUser(this.myData.getDBC(), userID);
			return aUser;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Notebook#getUserID()
	 */
	public String getUserID() throws DataException {
		return this.myData.getString(USER_COLUMN);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Notebook#setDescription(java.lang.String)
	 */
	public void setDescription(String text) throws DataException {
		this.myData.setString(DESC_COLUMN, text);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Notebook#setName(java.lang.String)
	 */
	public void setTitle(String aName) throws DataException {
		this.myData.setString(TITLE_COLUMN, aName);
	}

	public void setProjectID(String projectID) throws DataException {
		this.myData.setString(PROJECT_COLUMN, projectID);
	}
	
	public String getProjectID() throws DataException {
		return this.myData.getString(PROJECT_COLUMN);
	}

	public void setProject(Project aProject) throws DataException {
		this.setProjectID(aProject.getID());
	}

	@Override
	public NotebookPage addPage(int page, String title, String content)	throws DataException {
		if ( this.isAllowed(Role.CREATE) ) {
			return SQLNotebookPage.addPage(this, page, title, content);
		}
		return null;
	}

	@Override
	public boolean isAllowed(int permission) {
		try {
			if ( this.myData.getUser().getUserID().equals(this.getUserID()) ) {
				return true;
			}
		} catch (DataException e) {
			// THIS IS A PAIN!!! - NPManager will fix this.
		}
		// Other users can only read if they have project manager role.
		return ( permission == Role.READ ? super.isAllowed(permission) : false );
	}

	@Override
	public NotebookPage getPages() throws DataException {
		if ( this.isAllowed(Role.READ) ) {
			return SQLNotebookPage.loadAllPages(this);
		}
		return null;
	}

	@Override
	public NotebookPage getPage(int page) throws DataException {
		if ( this.isAllowed(Role.READ) ) {
			return SQLNotebookPage.loadPage(this, page);
		}
		return null;
	}

	@Override
	public int getPageCount() throws DataException {
		int count = 0;
		try {
			PreparedStatement sth = this.myData.prepareStatement(SQL_GET_PAGE_COUNT, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			sth.setString(1, this.myID);
			ResultSet results = sth.executeQuery();
			if ( results.next() )
				count = results.getInt(1);
			results.close();
			sth.close();
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return count;
	}

	@Override
	public Date getFirstUpdate() throws DataException {
		Date date = null;
		try {
			PreparedStatement sth = this.myData.prepareStatement(SQL_GET_FIRST_DATE, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			sth.setString(1, this.myID);
			ResultSet results = sth.executeQuery();
			if ( results.next() ) {
				date = results.getDate(2);
				Date otherDate = results.getDate(1);
				if ( otherDate != null && otherDate.before(date) ) { date = otherDate; }
			}
			results.close();
			sth.close();
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return date;
	}

	@Override
	public Date getRecentUpdate() throws DataException {
		Date date = null;
		try {
			PreparedStatement sth = this.myData.prepareStatement(SQL_GET_LATEST_DATE, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			sth.setString(1, this.myID);
			ResultSet results = sth.executeQuery();
			if ( results.next() ) {
				date = results.getDate(1);
			}
			results.close();
			sth.close();
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return date;
	}
	
	
	
}
