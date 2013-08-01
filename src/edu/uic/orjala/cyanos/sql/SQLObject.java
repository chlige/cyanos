package edu.uic.orjala.cyanos.sql;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import edu.uic.orjala.cyanos.BasicObject;
import edu.uic.orjala.cyanos.CyanosObject;
import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.ExternalFile;
import edu.uic.orjala.cyanos.Project;
import edu.uic.orjala.cyanos.Role;
import edu.uic.orjala.cyanos.Sample;

public abstract class SQLObject extends CyanosObject implements BasicObject {

	protected SQLData myData = null;
	
	protected String idField = null;
	
	protected boolean autorefresh = true;
	protected boolean localDBC = false;
	
	private final static String SELECT_DATA_SQL = "SELECT data.* FROM data WHERE data.id = ? AND data.tab = ? ORDER BY data.file";
	private final static String SELECT_DATA_BY_TYPE_SQL = "SELECT data.* FROM data WHERE data.id = ? AND data.tab = ? AND data.type = ? ORDER BY data.file";
	private final static String FIND_DATA_SQL = "SELECT id,tab,file,type,description FROM data WHERE id =? AND tab = ? AND file = ?";
	private final static String DELETE_DATAFILE_SQL = "DELETE FROM data WHERE id = ? AND tab = ? AND file = ?";
//	private final static String INSERT_DATAFILE_SQL = "INSERT INTO data(id,tab,file,type,description,mime_type) VALUES(?,?,?,?,?,?)";
	protected final static String DATA_FILE_DESCRIPTION_COLUMN = "description";
	protected final static String DATA_FILE_TYPE_COLUMN = "type";
	protected final static String DATA_FILE_FILENAME_COLUMN = "file";
	protected final static String DATA_FILE_KEY_COLUMN = "tab";
	protected final static String DATA_FILE_KEYID_COLUMN = "id";
//	protected final static String DATA_FILE_MIME_TYPE = "mime_type";

	protected String LOG_TABLE = null;
	public static final String LOG_USER_COLUMN = "user";
	public static final String LOG_DATE_COLUMN = "date";
	public static final String LOG_TEXT_COLUMN = "entry";
	public static final String LOG_INSERT_SQL = "INSERT INTO %s(%s,user,date,entry) VALUES(?,?,?,?)";
	protected SQLData logEntries = null;
	
	public static final String ASCENDING_SORT = "ASC";
	public static final String DESCENDING_SORT = "DESC";
	public static final String OPERATOR_AND = "AND";
	public static final String OPERATOR_OR = "OR";
	
	private static final String SQL_GET_UUID = "SELECT UUID()";
	
	protected static String allColumns(String prefix, String[] columns) { 
		StringBuffer output = new StringBuffer();
		output.append(prefix);
		output.append(columns[0]);
		for ( int i = 1; i < columns.length; i++) {
			output.append(", ");
			output.append(prefix);
			output.append(columns[i]);
		}
		return output.toString();
	}
	
	protected static String sqlBase(String table, String[] columns) {
		return sqlBase(table, columns, false);
	}
	
	protected static String sqlBase(String table, String[] columns, boolean distinct) {
		return "SELECT " + ( distinct ? "DISTINCT ": "") + allColumns(table.concat("."), columns) + " FROM " + table + " ";
	}
	
	protected static String sqlInsert(String table, String[] columns) {
		StringBuffer cols = new StringBuffer(" (" + columns[0]);
		StringBuffer vals = new StringBuffer(" VALUES(?");
		for ( int i = 1; i < columns.length ; i++ ) {
			cols.append(", ");
			cols.append(columns[i]);
			vals.append(",?");
		}
		cols.append(")");
		vals.append(");");
		
		return "INSERT INTO " + table + cols.toString() + vals.toString();
	}
	
	public SQLObject(SQLData data) {
		this.myData = data.duplicate();
	}
	
	protected void finalize() throws Throwable {
		try {
			if ( this.myData != null ) this.myData.close();
		} finally {
			super.finalize();
		}
	}
	
	protected UUID createUUID() throws DataException, SQLException {
		UUID anID;
		PreparedStatement sth = this.myData.prepareStatement(SQL_GET_UUID);
		ResultSet results = sth.executeQuery();
		if ( results.first() ) {
			anID = UUID.fromString(results.getString(1));
		} else {
			anID = UUID.randomUUID();
		}
		results.close();
		sth.close();
		return anID;
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Data#getID()
	 */
	public String getID() {
		return this.myID;
	}
	
	public boolean isAllowed(int permission) {
		return this.myData.checkAccess(permission);
	}
	
	protected boolean isAllowedException(int permission) throws DataException {
		return this.myData.isAllowed(permission);
	}


	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Data#getSamples(java.lang.String)
	 */
	public Sample[] getSamples(String sqlString) throws DataException {
		if ( this.myID == null ) {
			try {
				List<Sample> myParents = new ArrayList<Sample>();
				PreparedStatement psth = this.myData.prepareStatement(sqlString);
				psth.setString(1,this.myID);
				ResultSet mySrcs = psth.executeQuery();
				mySrcs.beforeFirst();
				while ( mySrcs.next() ) {
					Sample aSample = new SQLSample(this.myData, mySrcs.getString(1));
					myParents.add(aSample);
				}
				if (myParents.size() > 0 ) 
					return (Sample[])myParents.toArray();
			} catch (SQLException e) {
				throw new DataException(e);
			}
		} 
		return null;
	}
	
	protected void makeNewWithValue(String sqlString, String newID) throws DataException {
		if ( this.myData.isAllowedForProject(Role.CREATE, null) ) {
			try {
				PreparedStatement aSth = this.myData.prepareStatement(sqlString);
				aSth.setString(1, newID);
				if ( aSth.executeUpdate() > 0 ) {
					this.myID = newID;
					this.fetchRecord();
				} 
				aSth.close();
			} catch ( SQLException e ) {
				throw new DataException(e);
			}
		}
	}
	
	protected void makeNew(PreparedStatement aSth, String newID) throws DataException {
		this.makeNewInProject(aSth, newID, null);
	}

	protected void makeNewInProject(PreparedStatement aSth, String newID, String projectID) throws DataException {
		if ( this.myData.isAllowedForProject(Role.CREATE, projectID) ) {
			try {
				if ( aSth.executeUpdate() > 0 ) {
					this.myID = newID;
					this.fetchRecord();
				} 
				aSth.close();
			} catch ( SQLException e ) {
				throw new DataException(e);
			}
		}
	}
	
	protected void makeNewWithAutonumber(PreparedStatement aSth) throws DataException {
		this.makeNewWithAutonumber(aSth, null);
	}
	
	protected void makeNewWithAutonumber(PreparedStatement aSth, String projectID) throws DataException {
		if ( this.myData.isAllowedForProject(Role.CREATE, projectID) ) {
			try {
				//	ResultSet aResult = this.sth.executeQuery("SELECT GET_LOCK('id_insert',10)");
				//	if ( aResult.getInt(1) == 1 ) {
				if ( aSth.executeUpdate() > 0 ) {
					ResultSet aResult = aSth.executeQuery("SELECT LAST_INSERT_ID()");
					aResult.first();
					this.myID = aResult.getString(1);
					this.fetchRecord();
					//this.sth.executeQuery("SELECT RELEASE_LOCK('id_insert')");
					//		} else {
					//			return;
					//		}
					aResult.close();
					aSth.close();
				}
			} catch ( SQLException e ) {
				throw new DataException(e);
			}
		}
	}
	
	protected void makeNewWithAutonumber(String sqlString) throws DataException {
		PreparedStatement aSth = this.myData.prepareStatement(sqlString);
		this.makeNewWithAutonumber(aSth);
	}
	
	protected void makeNewInProject(String sqlString, String newID, String projectID) throws DataException {
		if ( this.myData.isAllowedForProject(Role.CREATE, projectID) ) {
			try {
				PreparedStatement aSth = this.myData.prepareStatement(sqlString);
				aSth.setString(1, newID);
				if ( projectID == null || projectID.length() < 1) 
					aSth.setNull(2, java.sql.Types.VARCHAR);
				else 
					aSth.setString(2, projectID);
				if ( aSth.executeUpdate() > 0 ) {
					this.myID = newID;
					this.fetchRecord();
				} 
				aSth.close();
			} catch ( SQLException e ) {
				throw new DataException(e);
			}
		}
	}
	
	protected void makeNewInProjectAutonumber(String sqlString, String projectID) throws DataException {
		if ( this.myData.isAllowedForProject(Role.CREATE, projectID) ) {
			try {
				PreparedStatement aSth = this.myData.prepareStatement(sqlString);
				if ( projectID == null || projectID.length() < 1) 
					aSth.setNull(1, java.sql.Types.VARCHAR);
				else 
					aSth.setString(1, projectID);
				//	ResultSet aResult = this.sth.executeQuery("SELECT GET_LOCK('id_insert',10)");
				//	if ( aResult.getInt(1) == 1 ) {
				if ( aSth.executeUpdate() > 0 ) {
					ResultSet aResult = aSth.executeQuery("SELECT LAST_INSERT_ID()");
					aResult.first();
					this.myID = aResult.getString(1);
					this.fetchRecord();
					//this.sth.executeQuery("SELECT RELEASE_LOCK('id_insert')");
					//		} else {
					//			return;
					//		}
					aResult.close();
					aSth.close();
				}
			} catch ( SQLException e ) {
				throw new DataException(e);
			}
		}
	}
	
	protected abstract void fetchRecord() throws DataException;
	
	protected void fetchRecord(String sqlString) throws DataException {
		try {
			PreparedStatement aPsth = this.myData.prepareStatement(sqlString);
			aPsth.setString(1, this.myID);
			this.fetchRecord(aPsth);
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}
	
	protected void fetchRecord(PreparedStatement aPsth) throws DataException {
		this.myData.loadUsingPreparedStatement(aPsth);
		this.myData.first();
	}

	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Data#next()
	 */
	public boolean next() throws DataException {
		this.logEntries = null;
		if ( this.myData.next() ) {
			this.setupRecord();
			return true;
		}
		return false;
	}
	
	protected void setupRecord() throws DataException {
		if ( this.idField != null )
			this.myID = this.myData.getQUIETString(this.idField);
	}
	

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Data#previous()
	 */
	public boolean previous() throws DataException {
		this.logEntries = null;
		if ( this.myData.previous() ) {
			this.setupRecord();
			return true;
		}
		return false;

	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Data#first()
	 */
	public boolean first() throws DataException {
		this.logEntries = null;
		if ( this.myData.first() ) {
			this.setupRecord();
			return true;
		}
		return false;

	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Data#last()
	 */
	public boolean last() throws DataException {
		this.logEntries = null;
		if ( this.myData.last() ) {
			this.setupRecord();
			return true;
		}
		return false;

	}
	
	public boolean isLast() throws DataException {
		try {
			return this.myData.myData.isLast();
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

	protected void unsetRecord() {
		this.myID = null;
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Data#beforeFirst()
	 */
	public void beforeFirst() throws DataException {
		this.unsetRecord();
		this.myData.beforeFirst();
		this.logEntries = null;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Data#afterLast()
	 */
	public void afterLast() throws DataException {
		this.unsetRecord();
		this.myData.afterLast();
		this.logEntries = null;
	}
	
	public boolean gotoRow(int row) throws DataException {
		this.logEntries = null;
		if ( this.myData.gotoRow(row) ) {
			this.setupRecord();
			return true;
		}
		return false;
	
	}
	
	@Deprecated
	public boolean loadUsing(PreparedStatement aSth) throws DataException {
		this.myData.loadUsingPreparedStatement(aSth);
		return this.first();
	}

	@Deprecated
	public void loadSortedBy(String sqlWhere, String column, String direction) throws DataException {
		String sqlString = String.format(sqlWhere, column, direction);
		this.myData.loadUsingSQL(sqlString);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Data#getRecordCount()
	 */
	public int getRecordCount() throws DataException {
		if ( this.myData.last() ) return this.myData.getRow();
		return 0;
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Data#setAutoRefresh()
	 */
	public void setAutoRefresh() {
		this.myData.setAutoRefresh();
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Data#setManualRefresh()
	 */
	public void setManualRefresh() {
		this.myData.setManualRefresh();
	}
	
	public void refresh() throws DataException {
		this.myData.refresh();
	}
		
	protected ExternalFile getDataFiles(String table, String id) throws DataException {
		SQLExternalFile someFiles = new SQLExternalFile(this.myData, null);
		if ( this.isAllowedException(Role.READ) ) {
			try {
				PreparedStatement aPsth = this.myData.prepareStatement(SELECT_DATA_SQL);
				aPsth.setString(1, id);
				aPsth.setString(2, table);
				someFiles.loadUsingPreparedStatement(aPsth);
			} catch (SQLException e) {
				throw new DataException(e);
			}
		}
		return someFiles;
	}

	
	protected void setDataFile(String table, String id, String dataType, ExternalFile aFile) throws DataException {
		this.setDataFile(table, id, dataType, aFile.getFilePath(), aFile.getDescription(), null);
	}

	protected void setDataFile(String table, String id, String dataType, String path, String description,
			String mimeType) throws DataException {
		if ( this.isAllowedException(Role.WRITE) ) {
			try {
				PreparedStatement insert = this.myData.prepareStatement(FIND_DATA_SQL);
				insert.setString(1, id);
				insert.setString(2, table);
				insert.setString(3, path);
				ResultSet aResult = insert.executeQuery();
				
				if ( aResult.first() ) {
					aResult.updateString(4, dataType);
					aResult.updateString(5, description);
					aResult.updateRow();
				} else {
					aResult.moveToInsertRow();
					aResult.updateString(1, id);
					aResult.updateString(2, table);
					aResult.updateString(3, path);
					aResult.updateString(4, dataType);
					aResult.updateString(5, description);
			//		aResult.updateString(6, mimeType);
					aResult.insertRow();
				}
				insert.close();
			} catch (SQLException e) {
				throw new DataException(e);
			}
		}
	}

	protected void unsetDataFile(String table, String id, ExternalFile aFile) throws DataException {
		this.unsetDataFile(table, id, aFile.getFilePath());
	}

	protected void unsetDataFile(String table, String id, String path) throws DataException {
		if ( this.isAllowedException(Role.WRITE) ) {
			try {
				PreparedStatement delete = this.myData.prepareStatement(DELETE_DATAFILE_SQL);
				delete.setString(1, id);
				delete.setString(2, table);
				delete.setString(3, path);
				delete.executeUpdate();
				delete.close();
			} catch (SQLException e) {
				throw new DataException(e);
			}
		}
	}

	protected ExternalFile getDataFilesForType(String table, String id, String dataType) throws DataException {
		SQLExternalFile someFiles = new SQLExternalFile(this.myData, this.myData.config.getFilePath(table, dataType));
		if ( this.isAllowedException(Role.READ) ) {
			try {
				PreparedStatement aPsth = this.myData.prepareStatement(SELECT_DATA_BY_TYPE_SQL);
				aPsth.setString(1, id);
				aPsth.setString(2, table);
				aPsth.setString(3, dataType);
				someFiles.loadUsingPreparedStatement(aPsth);
			} catch (SQLException e) {
				throw new DataException(e);
			}
		}
		return someFiles;
	}
	
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Data#getProjectID()
	 */
	public String getProjectID() throws DataException {
		return this.myData.getCurrentProjectID();
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Data#getProject()
	 */
	public Project getProject() throws DataException {
		String projID = this.getProjectID();
		if ( projID != null ) {
			Project aProj = SQLProject.load(this.myData, projID);
			if (aProj.first())
				return aProj;
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Data#loadLogEntries(java.lang.String, java.lang.String)
	 */
	public boolean loadLogEntries(String sortColumn, String direction) throws DataException {
		if ( this.LOG_TABLE != null && this.myID != null ) {
			try {
				if ( this.logEntries == null ) this.logEntries = this.myData.duplicate();
				String sql = String.format("SELECT * FROM %s WHERE %s=? ORDER BY %s %s", this.LOG_TABLE, this.idField, sortColumn, direction);
				PreparedStatement aPsth = this.logEntries.prepareStatement(sql);
				aPsth.setString(1, this.myID);
				this.logEntries.loadUsingPreparedStatement(aPsth);
				this.logEntries.beforeFirst();
				return true;
			} catch (SQLException e) {
				throw new DataException(e);
			}
		} 
		return false;
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Data#loadLogEntries(java.lang.String)
	 */
	public boolean loadLogEntries(String sortColumn) throws DataException {
		return this.loadLogEntries(sortColumn, ASCENDING_SORT);
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Data#loadLogEntries()
	 */
	public boolean loadLogEntries() throws DataException {
		return this.loadLogEntries(LOG_DATE_COLUMN, ASCENDING_SORT);
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Data#nextLogEntry()
	 */
	public boolean nextLogEntry() throws DataException {
		if ( this.logEntries != null ) return this.logEntries.next();
		return false;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Data#previousLogEntry()
	 */
	public boolean previousLogEntry() throws DataException {
		if ( this.logEntries != null ) return this.logEntries.previous();
		return false;
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Data#beforeFirstLogEntry()
	 */
	public void beforeFirstLogEntry() throws DataException {
		if ( this.logEntries != null ) this.logEntries.beforeFirst();
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Data#afterLastLogEntry()
	 */
	public void afterLastLogEntry() throws DataException {
		if ( this.logEntries != null ) this.logEntries.afterLast();
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Data#getLogEntry(java.lang.String)
	 */
	public String getLogEntry(String column) throws DataException {
		if ( this.logEntries != null ) {
			this.logEntries.getString(column);
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Data#getLogEntryText()
	 */
	public String getLogEntryText() throws DataException {
		return this.getLogEntry(LOG_TEXT_COLUMN);
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Data#getLogEntryUser()
	 */
	public String getLogEntryUser() throws DataException {
		return this.getLogEntry(LOG_USER_COLUMN);
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Data#getLogEntryDateString()
	 */
	public String getLogEntryDateString() throws DataException {
		return this.getLogEntry(LOG_DATE_COLUMN);
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Data#getLogEntryDate()
	 */
	public Date getLogEntryDate() throws DataException {
		if ( this.logEntries != null ) {
			return this.logEntries.getDate(LOG_DATE_COLUMN);
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Data#addLogEntry(java.lang.String, java.util.Date, java.lang.String)
	 */
	public void addLogEntry(String user, Date aDate, String text) throws DataException {
		if ( this.LOG_TABLE != null && this.myID != null ) {
			try {
				String sql = String.format(LOG_INSERT_SQL, this.LOG_TABLE, this.idField);
				PreparedStatement aPsth = this.logEntries.prepareStatement(sql);
				aPsth.setString(1, this.myID);
				aPsth.setString(2, user);
				aPsth.setDate(2, (java.sql.Date)aDate);
				aPsth.setString(4, text);
				aPsth.close();
			} catch (SQLException e) {
				throw new DataException(e);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.Data#addLogEntry(java.lang.String, java.lang.String)
	 */
	public void addLogEntry(String user, String text) throws DataException {
		Date now = new Date();
		this.addLogEntry(user, now, text);
	}
	
	public int count() throws DataException {
		return this.myData.getRecordCount();
	}

	protected void loadSQL(String sqlString) throws DataException {
		this.loadUsingSQL(sqlString);
	}

	protected void loadLike(String sqlTemplate, String[] likeColumns, String[] likeValues, String sortColumn, String sortDirection ) throws DataException {	
		try {
			PreparedStatement aSth = this.myData.prepareLikeStatement(sqlTemplate, likeColumns, likeValues, sortColumn, sortDirection);
			if ( aSth != null )
				this.loadUsingPreparedStatement(aSth);
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}
	
	protected void loadWhere(String sqlTemplate, String[] columns, String[] values, String[] operators, String sortColumn, String sortDirection ) throws DataException {	
		if ( columns.length != values.length ) return;
		String likeQuery = String.format("%s = ?", columns[0]);
		for ( int i = 1; i < columns.length; i++) {
			likeQuery = String.format("%s %s %s = ?", likeQuery, operators[i-1], columns[i]);
		}
		String sqlString = String.format(sqlTemplate, likeQuery, sortColumn, sortDirection);
		try {
			PreparedStatement aSth = this.myData.prepareStatement(sqlString);
			for ( int i = 0; i < values.length; i++) {
				aSth.setString(i + 1, values[i]);
			}
			this.loadUsingPreparedStatement(aSth);
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

	public boolean isLoaded() throws DataException {
		return(this.myData.getRow() > 0);
	}

	protected void loadUsingSQL(String sqlString) throws DataException {
		this.myData.loadUsingSQL(sqlString);
	}
	
	protected void loadUsingSQL(String sqlString, int setType, int setConcurrency) throws DataException {
		this.myData.loadUsingSQL(sqlString, setType, setConcurrency);
	}
	
	protected void loadUsingPreparedStatement(PreparedStatement aStatement) throws DataException {
		this.myData.loadUsingPreparedStatement(aStatement);
	}

	protected PreparedStatement prepareStatement(String sqlString) throws DataException {
		return this.myData.prepareStatement(sqlString);
	}

	protected PreparedStatement prepareStatement(String sqlString, int setType, int setConcurrency) throws DataException {
		return this.myData.prepareStatement(sqlString, setType, setConcurrency);
	}
	
	public Savepoint setSavepoint() throws DataException {
		try {
			return this.myData.setSavepoint();
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}
	
	public Savepoint setSavepoint(String name) throws DataException {
		try {
			return this.myData.setSavepoint(name);
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}
	
	public void setAutoCommit(boolean value) throws DataException {
		try {
			this.myData.setAutoCommit(value);
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}
	
	public void commit() throws DataException {
		try {
			this.myData.commit();
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}
	
	public void rollback() throws DataException {
		try {
			this.myData.rollback();
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}
	
	public void rollback(Savepoint savepoint) throws DataException {
		try {
			this.myData.rollback(savepoint);
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

	@Deprecated
	protected BigDecimal getDecimal(String valueColumn, String scaleColumn) throws DataException {
		BigDecimal amount = this.myData.getBigDecimal(valueColumn);
		if ( amount != null ) {
			int scale = this.myData.getInt(scaleColumn);
			return amount.setScale(scale);
		} 
		return null;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.BasicObject#getAttribute(java.lang.String)
	 */
	public String getAttribute(String attribute) throws DataException {
		return this.myData.getString(attribute);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.BasicObject#setAttribute(java.lang.String, java.lang.String)
	 */
	public void setAttribute(String attribute, String value) throws DataException {
		this.myData.setString(attribute, value);
	}
	
	public static Date getRecentUpdate(SQLData data, String sqlString, String projectID) throws SQLException, DataException {
		Date update = null;
		SQLData dataCopy = data.duplicate();
		PreparedStatement aPsth = dataCopy.prepareStatement(sqlString);
		aPsth.setString(1, projectID);
		dataCopy.loadUsingPreparedStatement(aPsth);
		if ( dataCopy.first() ) {
			update = dataCopy.getTimestamp(1);
		}
		return update;
	}

	
}
