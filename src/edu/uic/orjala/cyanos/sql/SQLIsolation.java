package edu.uic.orjala.cyanos.sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;

import edu.uic.orjala.cyanos.Collection;
import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.Isolation;
import edu.uic.orjala.cyanos.Notebook;
import edu.uic.orjala.cyanos.Project;
import edu.uic.orjala.cyanos.Strain;
import edu.uic.orjala.cyanos.User;


public class SQLIsolation extends SQLObject implements Isolation {
	// Setup the column names here so that changing is easier.
	public final static String ID_COLUMN = "isolation_id";
	public final static String TYPE_COLUMN = "type";
	public final static String MEDIA_COLUMN = "media";
	public final static String PARENT_COLUMN = "parent";
	public final static String COLLECTION_COLUMN = "collection_id";
	public final static String NOTES_COLUMN = "notes";
	public final static String PROJECT_COLUMN = "project_id";
	
	public final static String DATE_COLUMN = "date";
	
	public final static String NOTEBOOK_COLUMN = "notebook_id";
	public final static String NOTEBOOK_PAGE_COLUMN = "notebook_page";
	
//	public final static String REMOTE_HOST_COLUMN = "remote_host";

	private final static String[] ALL_COLUMNS = { ID_COLUMN, TYPE_COLUMN, MEDIA_COLUMN, 
		COLLECTION_COLUMN, NOTES_COLUMN, PROJECT_COLUMN, DATE_COLUMN, PARENT_COLUMN
	//	NOTEBOOK_COLUMN, NOTEBOOK_PAGE_COLUMN, 
		};	
	
	final static String SQL_BASE = sqlBase("isolation", ALL_COLUMNS);

	/*
	 * Parameter for "cleaned-up" database schema.
	 * 
	public final static String DATE_COLUMN = "added";
	 */
	
	private final static String SQL_LOAD = SQL_BASE + " WHERE isolation_id=?";
	private static final String SQL_INSERT_ISOLATION = "INSERT INTO isolation(isolation_id,collection_id,project_id) VALUES(?,?,?)";
	private static final String SQL_LOAD_KIDS = SQL_BASE + " WHERE parent=?";
	private static final String SQL_LOAD_STRAINS = SQLStrain.SQL_BASE + " WHERE isolation_id=?";
	
	private static final String SQL_LOAD_SORTABLE = SQL_BASE + " ORDER BY %s %s";
	
	private static final String SQL_LOAD_FOR_COLLECTION = SQL_BASE + " WHERE collection_id=? ORDER BY date ASC";
	private static final String SQL_LOAD_POSSIBLE_PARENTS = SQL_BASE + " WHERE collection_id=? AND isolation_id != ? ORDER BY date ASC";

	public static Isolation isolations(SQLData data, String column, String sortDirection) throws DataException {
		SQLIsolation myIsos = new SQLIsolation(data);
		String sqlString = String.format(SQL_LOAD_SORTABLE, column, sortDirection);
		myIsos.myData.loadUsingSQL(sqlString);
		return myIsos;
	}
	
	public static Isolation isolationsForCollection(SQLData data, String collectionID) throws DataException {
		try {
			SQLIsolation myIsos = new SQLIsolation(data);
			PreparedStatement aPsth = myIsos.myData.prepareStatement(SQL_LOAD_FOR_COLLECTION);
			aPsth.setString(1, collectionID);
			myIsos.myData.loadUsingPreparedStatement(aPsth);
			if ( myIsos.first() )
				return myIsos;
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return null;
	}
	
	/**
	 * Retrieve a set of isolations sorted by specified column.  LIKE conditions are combined using an OR operation.
	 * 
	 * @param data SQLData object
	 * @param likeColumns columns for LIKE conditions (use statics).
	 * @param likeValues values for LIKE conditions.
	 * @param sortColumn Column to sort by (use statics).
	 * @param sortDirection Direction for sort, either {@code SQLObject.ASCENDING_SORT} or {@code SQLObject.DESCENDING_SORT}.
	 * @return Isolation object with the specified isolations.
	 * @throws DataException
	 */
	public static Isolation isolationsLike(SQLData data, String[] likeColumns, String[] likeValues, String sortColumn, String sortDirection) throws DataException {
		SQLIsolation newObj = new SQLIsolation(data);
		newObj.loadLike(SQL_BASE, likeColumns, likeValues, sortColumn, sortDirection);
		return newObj;
	}


	public static SQLIsolation load(SQLData data, String isolationID) throws DataException {
		SQLIsolation newObj = new SQLIsolation(data);
		newObj.myID = isolationID;
		newObj.fetchRecord();
		return newObj;
	}
	
	
	protected SQLIsolation(SQLData data) {
		super(data);
		this.initVals();
	}

	@Deprecated
	public SQLIsolation(SQLData data, String anID) throws DataException {
		this(data);
		this.myID = anID;
		this.fetchRecord();
	}
	
	protected void initVals() {
		this.idField = "isolation_id";
		this.myData.setProjectField(PROJECT_COLUMN);
		this.myData.setAccessRole(User.CULTURE_ROLE);
	}
	
	protected void fetchRecord() throws DataException {
		this.fetchRecord(SQL_LOAD);
	}
	
	/**
	 * Creates a new isolation record.
	 * 
	 * @param data SQLData object
	 * @param newID ID for the new isolation record.
	 * @param collectionID ID of the parent collection.
	 * @return Isolation object for the new isolation.
	 * @throws DataException
	 */
	public static Isolation create(SQLData data, String newID, String collectionID) throws DataException {
		return SQLIsolation.createInProject(data, newID, collectionID, null);
	}
	
	/**
	 * Creates a new isolation record in the specified project. 
	 * This method should be used to ensure data security of new objects.
	 * 
	 * @param data SQLData object
	 * @param newID ID for the new isolation record.
	 * @param collectionID ID of the parent collection.
	 * @param projectID ID of the project.
	 * @return Isolation object for the new isolation.
	 * @throws DataException
	 */
	public static Isolation createInProject(SQLData data, String newID, String collectionID, String projectID) throws DataException {
		SQLIsolation anIso = new SQLIsolation(data);
		try {
			PreparedStatement aSth = data.prepareStatement(SQL_INSERT_ISOLATION);
			aSth.setString(1, newID);
			aSth.setString(2, collectionID);
			aSth.setString(3, projectID);
			anIso.makeNewInProject(aSth, newID, projectID);
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return anIso;
	}

	public String getNotes() throws DataException {
		return this.myData.getString(NOTES_COLUMN);
	}
	
	public Date getDate() throws DataException {
		return this.myData.getDate(DATE_COLUMN);
	}
	
	public String getDateString() throws DataException {
		return this.myData.getString(DATE_COLUMN);
	}
	
	public String getMedia() throws DataException {
		return this.myData.getString(MEDIA_COLUMN);
	}
	
	public String getType() throws DataException {
		return this.myData.getString(TYPE_COLUMN);
	}
	
	public String getParentID() throws DataException {
		return this.myData.getString(PARENT_COLUMN);
	}
	
	public Isolation getParent() throws DataException {
		String parentID = this.getParentID();
		if ( parentID != null ) {
			Isolation anIso = new SQLIsolation(this.myData, parentID);
			return anIso;
		}
		return null;
	}
	
	public Isolation getChildren() throws DataException {
		if ( this.myID != null ) {
			SQLIsolation myKids = new SQLIsolation(this.myData);
			try {
				PreparedStatement aPsth = this.myData.prepareStatement(SQL_LOAD_KIDS);
				aPsth.setString(1, this.myID);
				myKids.loadUsingPreparedStatement(aPsth);
				if ( myKids.first() )
					return myKids;
			} catch (SQLException e) {
				throw new DataException(e);
			}
		} 
		return null;
	}
	
	public String getCollectionID() throws DataException {
		return this.myData.getString(COLLECTION_COLUMN);
	}
	
	public Collection getCollection() throws DataException {
		String colID = this.getCollectionID();
		if ( colID != null )  {
			Collection aCol = SQLCollection.load(this.myData, colID);
			return aCol;
		}
		return null;
	}
	
	public String getProjectID() throws DataException {
		String retVal = this.myData.getString(PROJECT_COLUMN);
		if ( retVal == null ) {
			Collection myCol = this.getCollection();
			if ( myCol != null )  {	retVal = myCol.getProjectID(); }
		}
		return retVal;
	}
	
	public Project getProject() throws DataException {
		String projID = this.getProjectID();
		if ( projID != null ) {
			Project aProj = SQLProject.load(this.myData, projID);
			if ( aProj.first() )
				return aProj;
		}
		return null;
	}

	public void setMedia(String newValue) throws DataException {
		this.myData.setString(MEDIA_COLUMN, newValue);
	}
	
	public void setType(String newValue) throws DataException {
		this.myData.setString(TYPE_COLUMN, newValue);
	}
	
	public void setParentID(String newValue) throws DataException {
		this.myData.setString(PARENT_COLUMN, newValue);
	}
	
	public void setParent(Isolation anIsolation) throws DataException {
		this.setParentID(anIsolation.getID());
	}
	
	public void setCollectionID(String newValue) throws DataException {
		this.myData.setString(COLLECTION_COLUMN, newValue);
	}
	
	public void setCollection(Collection aCollection) throws DataException {
		this.setCollectionID(aCollection.getID());
	}
	
	public void setDate(Date newValue) throws DataException {
		this.myData.setDate(DATE_COLUMN, newValue);
	}
	
	public void setDate(String newValue) throws DataException {
		this.myData.setString(DATE_COLUMN, newValue);
	}
	
	public void setNotes(String newNotes) throws DataException {
		this.myData.setString(NOTES_COLUMN, newNotes);
	}
	
	public void addNotes(String newNotes) throws DataException {
		StringBuffer curNotes = new StringBuffer(this.myData.getString(NOTES_COLUMN));
		curNotes.append(" ");
		curNotes.append(newNotes);
		this.myData.setString(NOTES_COLUMN, curNotes.toString());
	}
	
	public Strain getStrains() throws DataException {
		if ( this.myID != null )  {
			SQLStrain myStrains = new SQLStrain(this.myData);
			try {
				PreparedStatement aPsth = this.myData.prepareStatement(SQL_LOAD_STRAINS);
				aPsth.setString(1, this.myID);
				myStrains.loadUsingPreparedStatement(aPsth);
				if ( myStrains.first() ) 
					return myStrains;
			} catch (SQLException e) {
				throw new DataException(e);
			}
		}
		return null;
	}

	public void setProjectID(String newValue) throws DataException {
		this.myData.setStringNullBlank(PROJECT_COLUMN, newValue);
	}
	
	public void setProject(Project aProject) throws DataException {
		if ( aProject != null )
			this.setProjectID(aProject.getID());
		else 
			this.myData.setNull(PROJECT_COLUMN);
	}	
	
	public Notebook getNotebook() throws DataException {
		String notebookID = this.myData.getString(NOTEBOOK_COLUMN);
		if ( notebookID != null ) {
			Notebook aNotebook = new SQLNotebook(this.myData, notebookID);
			return aNotebook;
		}
		return null;
	}

	public String getNotebookID() throws DataException {
		return this.myData.getString(NOTEBOOK_COLUMN);
	}

	public int getNotebookPage() throws DataException {
		return this.myData.getInt(NOTEBOOK_PAGE_COLUMN);
	}

	public void setNotebook(Notebook aNotebook) throws DataException {
		if ( aNotebook != null ) 
			this.myData.setString(NOTEBOOK_COLUMN, aNotebook.getID());
		else 
			this.myData.setNull(NOTEBOOK_COLUMN);
	}

	public void setNotebook(Notebook aNotebook, int aPage) throws DataException {
		if ( aNotebook != null ) {
			this.myData.setString(NOTEBOOK_COLUMN, aNotebook.getID());
			this.myData.setInt(NOTEBOOK_PAGE_COLUMN, aPage);
		} else {
			this.myData.setNull(NOTEBOOK_COLUMN);
			this.myData.setNull(NOTEBOOK_PAGE_COLUMN);
		}
	}

	public void setNotebookID(String anID) throws DataException {
		this.myData.setStringNullBlank(NOTEBOOK_COLUMN, anID);
	}

	public void setNotebookID(String anID, int aPage) throws DataException {
		if ( anID.length() > 0 ) {
			this.myData.setString(NOTEBOOK_COLUMN, anID);
			this.myData.setInt(NOTEBOOK_PAGE_COLUMN, aPage);
		} else {
			this.myData.setNull(NOTEBOOK_COLUMN);
			this.myData.setNull(NOTEBOOK_PAGE_COLUMN);
		}
	}

	public void setNotebookPage(int aPage) throws DataException {
		this.myData.setInt(NOTEBOOK_PAGE_COLUMN, aPage);
	}

	public Isolation possibleParents() throws DataException {
		SQLIsolation myIsos = new SQLIsolation(this.myData);
		try {
			PreparedStatement aPsth = myIsos.myData.prepareStatement(SQL_LOAD_POSSIBLE_PARENTS);
			aPsth.setString(1, this.getCollectionID());
			aPsth.setString(2, this.myID);
			myIsos.myData.loadUsingPreparedStatement(aPsth);
			if ( myIsos.first() )
				return myIsos;
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return null;
		

	}

	@Override
	public String getRemoteID() throws DataException {
		return this.myID;
	}

	@Override
	public String getRemoteHostID() throws DataException {
		return this.getCollection().getRemoteHostID();
	}
}
