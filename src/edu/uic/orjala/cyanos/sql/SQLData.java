/**
 * 
 */
package edu.uic.orjala.cyanos.sql;

import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.uic.orjala.cyanos.AccessException;
import edu.uic.orjala.cyanos.ConfigException;
import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.Role;
import edu.uic.orjala.cyanos.User;
import edu.uic.orjala.cyanos.web.AppConfig;

/**
 * Interface for Cyanos SQL data access.
 * 
 * @author George Chlipala
 * @version 1.0
 *
 */
public class SQLData {
	
	class SQLConnection {
		private Connection dbc;
		private final List<Savepoint> savepoints = new ArrayList<Savepoint>();


		protected SQLConnection(Connection dbc) {
			this.dbc = dbc;
		}

		protected Connection getDBC() { 
			return this.dbc;
		}

		protected PreparedStatement prepareStatement(String sqlString, int resultSetType, int resultSetConcurrency ) throws DataException {
			try {
				return this.dbc.prepareStatement(sqlString, resultSetType, resultSetConcurrency);
			} catch (SQLException e) {
				throw new DataException(e);
			}
		}

		protected void commit() throws SQLException {
			this.dbc.commit();
			if ( this.savepoints.size() > 0 ) {
				this.dbc.releaseSavepoint(this.savepoints.get(0));
				this.savepoints.clear();
				this.setAutoCommit(true);
			}
		}

		protected boolean getAutoCommit() throws DataException {
			try {
				return this.dbc.getAutoCommit();
			} catch (SQLException e) {
				throw new DataException(e);
			}
		}

		protected void close() throws DataException {
				this.savepoints.clear();
//				System.out.format("SQLConnection: DB Connection CLOSE: %d\n", this.dbc.hashCode());
			try {
				this.dbc.close();
			} catch (SQLException e) {
				throw new DataException(e);
			}			
		}

		public Savepoint setSavepoint() throws SQLException {
			this.dbc.setAutoCommit(false);
			Savepoint spoint = this.dbc.setSavepoint();
			this.savepoints.add(spoint);
			return spoint;
		}

		public Savepoint setSavepoint(String name) throws SQLException {
			this.dbc.setAutoCommit(false);
			Savepoint spoint = this.dbc.setSavepoint(name);
			this.savepoints.add(spoint);
			return spoint;
		}

		public void setAutoCommit(boolean value) throws SQLException {
			this.dbc.setAutoCommit(value);
		}

		public void rollback() throws SQLException {
			this.dbc.rollback();
			if ( this.savepoints.size() > 0 ) {
				this.dbc.releaseSavepoint(this.savepoints.get(0));
				this.savepoints.clear();
				this.setAutoCommit(true);
			}
		}

		public void rollback(Savepoint savepoint) throws SQLException {
			this.dbc.rollback(savepoint);
			this.releaseSavepoint(savepoint);
		}

		protected void releaseSavepoint(Savepoint savepoint) throws SQLException {
			int index = this.savepoints.indexOf(savepoint);
			this.dbc.releaseSavepoint(savepoint);
			if ( index > -1 ) {
				for (int i = this.savepoints.size() - 1; i >= index; i-- ) {
					this.savepoints.remove(i);
				}
			}
			this.dbc.setAutoCommit(this.savepoints.size() > 0);		
		}
	}

	protected SQLConnection conn;
	protected AppConfig config;
	protected User user;
	protected ResultSet myData;
	protected PreparedStatement mySth;

	private boolean autoRefresh = true;

	private String currentProjectID = null;
	private String accessRole = null;
	private boolean throwAccessError = true;
	private String projectIDField = null;

	private boolean hasTrash = true;
	private String trashCatalog = "trash";
	private boolean closeOnCommit = true;
	
	public static final int ID_TYPE_SERIAL = 1;
	public static final int ID_TYPE_UUID = 2;
	
	private int idType = ID_TYPE_SERIAL;
	
	public SQLData(AppConfig config, Connection conn) throws ConfigException, SQLException {
		this.config = config;
		this.conn = new SQLConnection(conn);
	}
	
	public SQLData(AppConfig config, Connection conn, User aUser) throws ConfigException, SQLException {
		this(config, conn);
		this.user = aUser;
	}

	public SQLData(AppConfig config, Connection conn, User aUser, int idType) throws ConfigException, SQLException {
		this(config, conn, aUser);
		this.idType = idType;
	}

	public SQLData(AppConfig config, Connection conn, String aUser) throws DataException, SQLException {
		this(config, conn);
		this.user = new SQLUser(this.conn.dbc, aUser);
	}

	public SQLData(AppConfig config, Connection conn, String aUser, int idType) throws DataException, SQLException {
		this(config, conn, aUser);
		this.idType = idType;
	}

	protected SQLData(AppConfig config, SQLConnection conn) {
		this.config = config;
		this.conn = conn;
	}

	protected SQLData(AppConfig config, SQLConnection conn, User aUser) {
		this(config, conn);
		this.user = aUser;
	}

	/**
	 * Retrieves user for data access.
	 * 
	 * @return Associated user
	 */
	public User getUser() {
		return this.user;
	}
	
	public int getIDType() {
		return this.idType;
	}

	public void setAccessRole(String accessRole) {
		this.accessRole = accessRole;
	}

	protected void setProjectField(String projectField) {
		this.projectIDField = projectField;
	}

	protected boolean isAllowed(int permission) throws DataException {
		if ( this.accessRole != null ) {
			if ( this.user.isAllowed(this.accessRole, this.currentProjectID, permission) ) { 
				return true;
			} else if ( this.throwAccessError ) { 
				throw new AccessException(this.user, this.accessRole, permission); 
			}
			return false;
		} else {
			return true;
		}
	}

	protected boolean isAllowedForProject(int permission, String projectID) throws DataException {
		if ( this.accessRole != null ) {
			if ( this.user.isAllowed(this.accessRole, projectID, permission) ) { 
				return true;
			} else if ( this.throwAccessError ) { 
				throw new AccessException(this.user, this.accessRole, permission); 
			}
			return false;
		} else {
			return true;
		}
	}

	public void throwAccessExceptions() {
		this.throwAccessError = true;
	}

	public void quietAccess() {
		this.throwAccessError = false;
	}

	public boolean willThrowAccessExeceptions() {
		return this.throwAccessError;
	}

	protected String getCurrentProjectID() {
		return this.currentProjectID;
	}

	protected String getAccessRole() {
		return this.accessRole;
	}

	protected boolean checkAccess(int permission) {
		if ( this.accessRole != null ) {
			return (this.user.isAllowed(this.accessRole, this.currentProjectID, permission) );
		} else {
			return true;
		}
	}

	public PreparedStatement prepareStatement(String sqlString) throws DataException {
		return this.prepareStatement(sqlString, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
	}
	
	PreparedStatement prepareLikeStatement(String sqlTemplate, String[] likeColumns, String[] likeValues, String sortColumn, String sortDirection ) throws SQLException, DataException {	
		if ( likeColumns.length != likeValues.length ) return null;
		
		String likeQuery = String.format("%s LIKE ?", likeColumns[0]);
		
		for ( int i = 1; i < likeColumns.length; i++) {
			likeQuery = likeQuery.concat(String.format(" OR %s LIKE ?", likeColumns[i]));
		}
		StringBuffer sqlString = new StringBuffer(sqlTemplate);
		sqlString.append(" WHERE ");
		sqlString.append(likeQuery);
		sqlString.append(" ORDER BY ");
		sqlString.append(sortColumn);
		sqlString.append(" ");
		sqlString.append(sortDirection);

		PreparedStatement aSth = this.prepareStatement(sqlString.toString());
		for ( int i = 0; i < likeValues.length; i++) {
			aSth.setString(i + 1, likeValues[i]);
		}
		return aSth;
	}


	protected PreparedStatement prepareStatement(String sqlString, int setType, int setConcurrency) throws DataException {
		return this.conn.prepareStatement(sqlString, setType, setConcurrency);
	}

	protected void loadUsingPreparedStatement(PreparedStatement aStatement) throws DataException {
		try {
			this.myData = aStatement.executeQuery();
		} catch (SQLException e) {
			throw new DataException(e);
		}
		this.mySth = aStatement;
	}

	protected void reload() throws DataException {
		try {
			this.myData = this.mySth.executeQuery();
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

	protected void loadUsingSQL(String sqlString) throws DataException {
		try {
			if ( this.conn.getDBC() != null ) {
				this.mySth = this.prepareStatement(sqlString);
				this.myData = this.mySth.executeQuery();
			}
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

	protected void loadUsingSQL(String sqlString, int setType, int setConcurrency) throws DataException {
		try {
			if ( this.conn.getDBC() != null ) {
				this.mySth = this.prepareStatement(sqlString, setType, setConcurrency);
				this.myData = this.mySth.executeQuery();
			}
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

	private boolean canUse() throws SQLException, DataException {
		if ( this.projectIDField != null )
			this.currentProjectID = this.myData.getString(this.projectIDField);
		else 
			this.currentProjectID = null;
		if ( this.accessRole != null ) {
			if ( this.user.isAllowed(this.accessRole, this.currentProjectID, Role.READ) ) { 
				return true;
			}
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Moves to before the first retrieved entry
	 * 
	 * @throws DataException
	 */
	protected void beforeFirst() throws DataException {
		try {
			this.currentProjectID = null;
			if ( this.myData != null ) this.myData.beforeFirst();
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

	/**
	 * Moves to the first retrieved entry
	 * 
	 * @return true if there is an entry
	 * @throws DataException
	 */
	protected boolean first() throws DataException {
		try {
			if ( this.myData != null ) {
				if ( this.myData.first() ) {
					if ( this.canUse() ) {
						return true;
					} else {
						return this.next();
					}
				}
			}
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return false;
	}

	/**
	 * Moves to the previous entry of the object.
	 * 
	 * @return true if there is a previous entry
	 * @throws DataException
	 */
	protected boolean previous() throws DataException {
		try {
			if ( this.myData != null ) {
				while ( this.myData.previous() ) {
					if ( this.canUse() )
						return true;
				}
			}
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return false;
	}

	/**
	 * Moves to the next entry of the object.
	 * 
	 * @return true if there is another entry
	 * @throws DataException
	 */
	protected boolean next() throws DataException {
		try {
			if ( this.myData != null ) {
				while ( this.myData.next() ) {
					if ( this.canUse() )
						return true;
				}
			}
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return false;
	}

	/**
	 * Moves to the last retrieved entry
	 * 
	 * @return true if there is an entry
	 * @throws DataException
	 */
	protected boolean last() throws DataException {
		try {
			if ( this.myData != null ) {
				if ( this.myData.last() ) {
					if ( this.canUse() ) {
						return true;
					} else {
						return this.previous();
					}
				}
				return false;
			}
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return false;
	}

	/**
	 * Moves to after the last retrieved entry
	 * 
	 * @throws DataException
	 */
	protected void afterLast() throws DataException {
		this.currentProjectID = null;
		try {
			if ( this.myData != null ) this.myData.afterLast();
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

	public void close() throws DataException {
		this.close(this.closeOnCommit);
	}

	public void close(boolean commit) throws DataException {
		try {
			if ( commit && ( ! this.conn.getAutoCommit() ) ) {
				this.conn.commit();
			}
			if ( this.myData != null ) this.myData.close();
			this.myData = null;
			if ( this.mySth != null ) this.mySth.close();
			this.mySth = null;
		} catch (SQLException e) {
			throw new DataException(e);
		}		
	}

	public void closeDBC() throws DataException {
		try {
			if ( this.conn.getDBC() != null & ( ! this.conn.getDBC().isClosed() ) )
				this.conn.close();
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}
	/* (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	@Override
	protected void finalize() throws Throwable {
		this.close();
		this.closeDBC();
		super.finalize();
	}

	/**
	 * Retieves float value for parameter
	 * 
	 * @param param Parameter to retrieve
	 * @return float value
	 * @throws DataException
	 */
	public float getFloat(String param) throws DataException {
		if ( this.myData != null && this.isAllowed(Role.READ) ) {
			try {
				return this.myData.getFloat(param);
			} catch (SQLException e) {
				throw new DataException(e);
			}
		}
		return 0;
	}

	/**
	 * Retieves double value for parameter
	 * 
	 * @param param Parameter to retrieve
	 * @return double value
	 * @throws DataException
	 */
	public double getDouble(String param) throws DataException {
		if ( this.myData != null && this.isAllowed(Role.READ) ) {
			try {
				return this.myData.getDouble(param);
			} catch (SQLException e) {
				throw new DataException(e);
			}
		}
		return 0;
	}

	/**
	 * Retieves integer value for parameter
	 * 
	 * @param param Parameter to retrieve
	 * @return integer value
	 * @throws DataException
	 */
	public int getInt(String param) throws DataException {
		if ( this.myData != null && this.isAllowed(Role.READ) ) {
			try {
				return this.myData.getInt(param);
			} catch (SQLException e) {
				throw new DataException(e);
			}
		}
		return 0;
	}
	
	public BigDecimal getBigDecimal(String param) throws DataException {
		if ( this.myData != null && this.isAllowed(Role.READ) ) {
			try {
				return this.myData.getBigDecimal(param);
			} catch (SQLException e) {
				throw new DataException(e);
			}
		}
		return null;

	}

	/**
	 * Get the number of retrieved entries.
	 * 
	 * @return Number of entries
	 * @throws DataException
	 */
	protected int getRecordCount() throws DataException {
		if ( this.last() ) {
			try {
				return this.myData.getRow();
			} catch (SQLException e) {
				throw new DataException(e);
			}
		}
		return 0;
	}

	protected int getRow() throws DataException {
		if ( this.myData != null && this.isAllowed(Role.READ) ) {
			try {
				return this.myData.getRow();
			} catch (SQLException e) {
				throw new DataException(e);
			}
		}
		return 0;
	}
	
	protected boolean gotoRow(int row) throws DataException {
		if ( this.myData != null ) {
			try {
				return this.myData.absolute(row);
			} catch (SQLException e) {
				throw new DataException(e);
			}
		}
		return false;
	}

	protected void insertRow() throws DataException {
		if ( this.isAllowed(Role.CREATE) ) {
			try {
				this.myData.insertRow();
			} catch (SQLException e) {
				throw new DataException(e);
			}
		}
	}

	protected void moveToInsertRow() throws DataException {
		if ( this.isAllowed(Role.CREATE) ) {
			try {
				this.myData.moveToInsertRow();
			} catch (SQLException e) {
				throw new DataException(e);
			}
		}
	}

	/**
	 * Retieves java.util.Date object for parameter
	 * 
	 * @param param Parameter to retrieve
	 * @return Date object
	 * @throws DataException
	 */
	public Date getDate(String param) throws DataException {
		if ( this.myData != null && this.isAllowed(Role.READ) ) {
			try {
				return this.myData.getDate(param);
			} catch (SQLException e) {
				throw new DataException(e);
			}
		}
		return null;
	}

	/**
	 * Retieves java.util.Data object for parameter as a Timestamp
	 * 
	 * @param param Parameter to retrieve
	 * @return Date object
	 * @throws DataException
	 */
	public Date getTimestamp(String param) throws DataException {
		if ( this.myData != null && this.isAllowed(Role.READ) ) {
			try {
				return this.myData.getTimestamp(param);
			} catch (SQLException e) {
				throw new DataException(e);
			}
		}
		return null;
	}

	/**
	 * Retieves java.util.Data object for parameter as a Timestamp
	 * 
	 * @param param param index
	 * @return Date object
	 * @throws DataException
	 */
	public Date getTimestamp(int param) throws DataException {
		if ( this.myData != null && this.isAllowed(Role.READ) ) {
			try {
				return this.myData.getTimestamp(param);
			} catch (SQLException e) {
				throw new DataException(e);
			}
		}
		return null;
	}

	/**
	 * Retrieves string for parameter.
	 * 
	 * @param param Parameter to retrieve
	 * @return String value for parameter
	 * @throws DataException
	 */
	public String getString(String param) throws DataException {
		if ( this.myData != null && this.isAllowed(Role.READ) ) {
			try {
				return this.myData.getString(param);
			} catch (SQLException e) {
				throw new DataException(e);
			}
		}
		return null;
	}

	/**
	 * Retrieves string for parameter without checking access permissions.
	 * 
	 * @param param Parameter to retrieve
	 * @return String value for parameter
	 * @throws DataException
	 */
	protected String getQUIETString(String param) throws DataException {
		if ( this.myData != null ) {
			try {
				return this.myData.getString(param);
			} catch (SQLException e) {
				throw new DataException(e);
			}
		}
		return null;
	}

	/**
	 * Set the project ID.
	 * 
	 * @param param Column for the project ID.
	 * @throws DataException
	 */
	public void setProject(String param) throws DataException {
		this.currentProjectID = null;
		if ( this.myData != null && param != null ) {
			try {
				this.currentProjectID = this.myData.getString(param);
			} catch (SQLException e) {
				throw new DataException(e);
			}
		}
	}

	/**
	 * Retrieves a byte array for parameter.
	 * 
	 * @param param Parameter to retrieve
	 * @return byte array of parameter
	 * @throws DataException
	 */
	public byte[] getByteArray(String param) throws DataException {
		if ( this.myData != null && this.isAllowed(Role.READ) ) {
			try {
				return this.myData.getBytes(param);
			} catch (SQLException e) {
				throw new DataException(e);
			}
		}
		return null;
	}

	/**
	 * Determines if specified parameter is <CODE>NULL</CODE>
	 * 
	 * @param param Parameter to check
	 * @return true if parameter is <CODE>NULL</CODE>
	 * @throws DataException
	 */
	public boolean isNull(String param) throws DataException {
		if ( this.myData != null && this.isAllowed(Role.READ) ) {
			try {
				this.myData.getString(param);
				return this.lastWasNull();
			} catch (SQLException e) {
				throw new DataException(e);
			}
		}
		return false;
	}

	/**
	 * Determines if the last accessed parameter is <CODE>NULL</CODE>
	 * 
	 * @return true if parameter is <CODE>NULL</CODE>
	 * @throws DataException 
	 */
	protected boolean lastWasNull() throws DataException {
		if ( this.myData != null && this.isAllowed(Role.READ) ) {
			try {
				return this.myData.wasNull();
			} catch (SQLException e) {
				throw new DataException(e);
			}

		}
		return false;
	}

	/**
	 * Retrieve an InputStream for the specified parameter.
	 * 
	 * @param param Parameter to retrieve
	 * @return InputStream for specified parameter
	 * @throws DataException
	 */
	public InputStream getBinaryStream(String param) throws DataException {
		if ( this.myData != null && this.isAllowed(Role.READ) ) {
			try {
				return this.myData.getBinaryStream(param);
			} catch (SQLException e) {
				throw new DataException(e);
			}
		}
		return null;

	}

	/**
	 * Update values to data store.
	 * 
	 * @throws DataException
	 */
	protected void refresh() throws DataException {
		if ( this.myData != null && this.isAllowed(Role.WRITE) ) {
			try {
				this.myData.updateRow();
				this.myData.refreshRow();
			} catch (SQLException e) {
				throw new DataException(e);
			}
		}
	}

	/**
	 * Sets java.util.Date object for parameter
	 * 
	 * @param param Parameter to set
	 * @param newValue Date object to set
	 * @throws DataException
	 */
	public void setDate(String param, Date newValue) throws DataException {
		try {
			if ( this.myData != null && this.myData.getRow() > 0 && this.isAllowed(Role.WRITE) )  {
				this.myData.updateDate(param, new java.sql.Date(newValue.getTime()));
				if ( this.autoRefresh ) this.refresh();
			}
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

	/**
	 * Sets java.util.Date object for parameter as a Timestamp
	 * 
	 * @param param Parameter to set
	 * @param newValue Date object to set
	 * @throws DataException
	 */
	public void setTimestamp(String param, Date newValue) throws DataException {
		try {
			if ( this.myData != null && this.myData.getRow() > 0 && this.isAllowed(Role.WRITE) )  {
				this.myData.updateTimestamp(param, new java.sql.Timestamp(newValue.getTime()));
				if ( this.autoRefresh ) this.refresh();
			}
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

	/**
	 * Sets float for parameter.
	 * 
	 * @param param Parameter to set
	 * @param newValue float value to set
	 * @throws DataException
	 */
	public void setFloat(String param, float newValue) throws DataException {
		try {
			if ( this.myData != null && this.myData.getRow() > 0 && this.isAllowed(Role.WRITE) )  {
				this.myData.updateFloat(param, newValue);
				if ( this.autoRefresh ) this.refresh();
			}
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

	/**
	 * Sets double for parameter.
	 * 
	 * @param param Parameter to set
	 * @param newValue double value to set
	 * @throws DataException
	 */
	public void setDouble(String param, double newValue) throws DataException {
		try {
			if ( this.myData != null && this.myData.getRow() > 0 && this.isAllowed(Role.WRITE) )  {
				this.myData.updateDouble(param, newValue);
				if ( this.autoRefresh ) this.refresh();
			}
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

	/**
	 * Sets integer for parameter.
	 * 
	 * @param param Parameter to set
	 * @param newValue int value to set
	 * @throws DataException
	 */
	public void setInt(String param, int newValue) throws DataException {
		try {
			if ( this.myData != null && this.myData.getRow() > 0 && this.isAllowed(Role.WRITE) )  {
				this.myData.updateInt(param, newValue);
				if ( this.autoRefresh ) this.refresh();
			}
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

	
	public void setBigDecimal(String param, BigDecimal value) throws DataException {
		try {
			if ( this.myData != null && this.myData.getRow() > 0 && this.isAllowed(Role.WRITE) )  {
				this.myData.updateBigDecimal(param, value);
				if ( this.autoRefresh ) this.refresh();
			}
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}
	
	public void setDecimal(String valueColumn, String scaleColumn, BigDecimal value) throws DataException {
		try {
			if ( this.myData != null && this.myData.getRow() > 0 && this.isAllowed(Role.WRITE) )  {
				if ( value == null ) {
					this.myData.updateNull(valueColumn);
					this.myData.updateNull(scaleColumn);
				} else {
					BigInteger unscaledValue = value.unscaledValue();
					this.myData.updateLong(valueColumn, unscaledValue.longValue());
					this.myData.updateInt(scaleColumn, -1 * value.scale());
				}		
				if ( this.autoRefresh ) this.refresh();
			}
		} catch (SQLException e) {
			throw new DataException(e);
		}		
	}
	
	protected BigDecimal getDecimal(String valueColumn, String scaleColumn) throws DataException {
		try {
			long amount = this.myData.getLong(valueColumn);
			if ( this.myData.wasNull() ) {
				return null;
			}
			int scale = this.myData.getInt(scaleColumn);
			return BigDecimal.valueOf(amount, -1 * scale);
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

	
	/**
	 * Sets a byte array (byte[]) for a parameter.
	 * 
	 * @param param Parameter to set
	 * @param newValue byte[] to set.
	 * @throws DataException
	 */
	public void setByteArray(String param, byte[] newValue) throws DataException {
		try {
			if ( this.myData != null && this.myData.getRow() > 0 && this.isAllowed(Role.WRITE) )  {
				this.myData.updateBytes(param, newValue);
				if ( this.autoRefresh ) this.refresh();
			}
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}


	/**
	 * Returns true if set to automatically update values.
	 */
	public boolean autoRefresh() {
		return this.autoRefresh;
	}

	/**
	 * Set the object automatically update values for each set*() method call.
	 */
	public void setAutoRefresh() {		
		this.autoRefresh = true;
	}

	/**
	 * Set the object to update values only when <CODE>anObject.refresh()</CODE> is invoked. 
	 */
	public void setManualRefresh() {
		this.autoRefresh = false;
	}

	/**
	 * Sets parameter to <CODE>NULL</CODE>.
	 * 
	 * @param param Parameter to set
	 * @throws DataException
	 */
	public void setNull(String param) throws DataException {
		try {
			if ( this.myData != null && this.myData.getRow() > 0 && this.isAllowed(Role.WRITE) ) 
				this.myData.updateNull(param);
			if ( autoRefresh ) this.refresh();
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

	/**
	 * Sets string for parameter.
	 * 
	 * @param param Parameter to set
	 * @param newValue
	 * @throws DataException
	 */
	public void setString(String param, String newValue) throws DataException {
		try {
			if ( this.myData != null && this.myData.getRow() > 0 && this.isAllowed(Role.WRITE) )  {
				this.myData.updateString(param, newValue);
				if ( this.autoRefresh ) this.refresh();
			}
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}

	/**
	 * Sets string for parameter.  Will set value to blank if String length is < 1.
	 * 
	 * @param param Parameter to set
	 * @param newValue String value to set
	 * @throws DataException
	 */
	public void setStringNullBlank(String param, String newValue) throws DataException {
		if ( newValue == null || newValue.trim().length() < 1 ) 
			this.setNull(param);
		else 
			this.setString(param, newValue);
	}

	/**
	 * Create a duplicate of this SQLData object
	 * 
	 * @return new SQLData object
	 */
	public SQLData duplicate() {
		SQLData newData = new SQLData(this.config, this.conn, this.user);
		newData.hasTrash = this.hasTrash;
		newData.trashCatalog = this.trashCatalog;
		newData.closeOnCommit = (this.conn.savepoints.size() < 1);
		newData.accessRole = null;
		newData.currentProjectID = null;
		return newData;
	}

	/**
	 * Get the database Connection object for this data source
	 * 
	 * @return database Connection object
	 */
	protected Connection getDBC() {
		return this.conn.getDBC();
	}

	public void useTrash() {
		this.hasTrash = true;
	}

	public void noTrash() {
		this.hasTrash = false;
	}

	public boolean hasTrash() {
		return this.hasTrash;
	}

	public String getTrashCatalog() {
		return this.trashCatalog;
	}

	public void setTrashCatalog(String newCatalog) {
		this.trashCatalog = newCatalog;
	}

	public Savepoint setSavepoint() throws SQLException {
		return this.conn.setSavepoint();
	}

	public Savepoint setSavepoint(String name) throws SQLException {
		return this.conn.setSavepoint(name);
	}

	public void setAutoCommit(boolean value) throws SQLException {
		this.conn.setAutoCommit(value);
	}

	public void commit() throws SQLException {
		this.conn.commit();
	}

	public void rollback() throws SQLException {
		this.conn.rollback();
	}

	public void rollback(Savepoint savepoint) throws SQLException {
		this.conn.rollback(savepoint);
	}

	public void releaseSavepoint(Savepoint savepoint) throws SQLException {
		this.conn.releaseSavepoint(savepoint);
	}
	
	List<String> getPossibles(String sqlString) throws DataException, SQLException {
		List<String> fates = new ArrayList<String>();
		PreparedStatement sth = this.prepareStatement(sqlString);
		ResultSet columnInfo =  sth.executeQuery();
		columnInfo.first();
		String type = columnInfo.getString(2);
		if ( type.startsWith("enum(") || type.startsWith("set(") ) {
			int pos = 0;
			while ( (pos = type.indexOf("'", pos)) > 0 ) {
				int end = type.indexOf("'", pos + 1);
				fates.add(type.substring(pos + 1, end));
				pos = end + 1;
			}
		}
		columnInfo.close();
		sth.close();
		return fates;
	}
	

}
