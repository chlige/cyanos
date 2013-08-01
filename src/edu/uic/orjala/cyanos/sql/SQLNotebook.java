/**
 * 
 */
package edu.uic.orjala.cyanos.sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.Notebook;
import edu.uic.orjala.cyanos.NotebookObject;
import edu.uic.orjala.cyanos.Project;
import edu.uic.orjala.cyanos.Role;
import edu.uic.orjala.cyanos.User;

/**
 * @author George Chlipala
 *
 */
public class SQLNotebook extends SQLObject implements Notebook {

	public static final String ID_COLUMN = "notebook_id";
	public static final String NAME_COLUMN = "name";
	public static final String DESC_COLUMN = "description";
	public static final String USER_COLUMN = "username";
	public static final String PROJECT_COLUMN = "project_id";
	
	private static final String SQL_LOAD_TEMPLATE = "SELECT notebook.* FROM notebook WHERE %s ORDER BY %s %s";
	private static final String SQL_INSERT_WITH_USER = "INSERT INTO notebook(notebook_id,username) VALUES(?,?)";
//	private static final String SQL_INSERT_WITH_PROJECT = "INSERT INTO notebook(notebook_id,project_id) VALUES(?,?)";
	private final static String SQL_LOAD = "SELECT notebook.* FROM notebook WHERE notebook_id=?";

	/**
	 * Retrieve notebooks for the specified user.
	 * 
	 * @param data SQLData object
	 * @param aUser User object for specified user
	 * @return Notebook object containing the notebooks.
	 * @throws DataException
	 */
	public static Notebook notebooksForUser(SQLData data, User aUser) throws DataException {
		SQLNotebook aNotebook = new SQLNotebook(data);
		String[] columns = { USER_COLUMN };
		String[] values = { aUser.getUserID() };
		String[] operators = {};
		aNotebook.loadWhere(SQL_LOAD_TEMPLATE, columns, values, operators, ID_COLUMN, SQLObject.ASCENDING_SORT);
		return aNotebook;
	}
	
	/**
	 * Retrieve notebooks for the current user.
	 * 
	 * @param data SQLData object
	 * @return Notebook object containing the notebooks.
	 * @throws DataException
	 */
	public static Notebook myNotebooks(SQLData data) throws DataException {
		SQLNotebook aNotebook = new SQLNotebook(data);
		String[] columns = { USER_COLUMN };
		String[] values = { data.getUser().getUserID() };
		String[] operators = {};
		aNotebook.loadWhere(SQL_LOAD_TEMPLATE, columns, values, operators, ID_COLUMN, SQLObject.ASCENDING_SORT);
		return aNotebook;
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
		if ( aNotebook.isAllowedException(Role.CREATE) ) {
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
		}
		return aNotebook;
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
	public void addPage(int page, NotebookObject anObject) throws DataException {
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
	public String getName() throws DataException {
		return this.myData.getString(NAME_COLUMN);
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
	public void setName(String aName) throws DataException {
		this.myData.setString(NAME_COLUMN, aName);
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
	
}
