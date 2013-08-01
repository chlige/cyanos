/**
 * 
 */
package edu.uic.orjala.cyanos.web;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.User;
import edu.uic.orjala.cyanos.sql.SQLData;
import edu.uic.orjala.cyanos.sql.SQLObject;

/**
 * @author George Chlipala
 *
 */
public class News extends SQLObject {

	private static final String CONTENT_COLUMN = "content";
	private static final String SUBJECT_COLUMN = "subject";
	private static final String DATE_ADDED_COLUMN = "date_added";
	private static final String DATE_EXPIRES_COLUMN = "expires";
	
	private static final String SQL_CURRENT_NEWS = "SELECT * FROM news WHERE expires > CURRENT_TIMESTAMP ORDER BY date_added DESC";
	private static final String SQL_ALL_NEWS = "SELECT * FROM news ORDER BY date_added DESC";
	private static final String SQL_LOAD_NEWS = "SELECT * FROM news WHERE date_added = ?";
	private static final String SQL_INSERT_NEWS = "INSERT INTO news(date_added,expires,subject,content) VALUES(?,?,?,?)";
	
	protected final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-DD HH:mm:ss");
	
	public static News create(SQLData data, Date expires, String subject, String content) throws DataException {
		SimpleDateFormat aFormat = new SimpleDateFormat("yyyy-MM-DD HH:mm:ss");
		return News.create(data, aFormat.format(expires), subject, content);
	}

	public static News create(SQLData data, String expires, String subject, String content) throws DataException {
		News retVal = new News(data);
		Date dateAdded = new Date();
		try {
			PreparedStatement aPsth = retVal.prepareStatement(SQL_INSERT_NEWS);
			aPsth.setDate(1, new java.sql.Date(dateAdded.getTime()));			
			aPsth.setString(2, expires);
			aPsth.setString(3, subject);
			aPsth.setString(4, content);
			if ( aPsth.executeUpdate() > 0 ) {
				retVal.setID(dateAdded);
				retVal.fetchRecord();
			}
			aPsth.close();
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return retVal;
	}

	public static News currentNews(SQLData data) throws DataException {
		News retVal = new News(data);
		retVal.loadUsingSQL(SQL_CURRENT_NEWS);			
		return retVal;
	}
	
	public static News allNews(SQLData data) throws DataException {
		News retVal = new News(data);
		retVal.loadUsingSQL(SQL_ALL_NEWS);			
		return retVal;	
	}
	
	/**
	 * @param data SQLData object
	 */
	protected News(SQLData data) {
		super(data);
		this.myData.setAccessRole(User.ADMIN_ROLE);
		this.idField = DATE_ADDED_COLUMN;
	}

	protected News(SQLData data, String dateAdded) throws DataException {
		this(data);
		this.myID = dateAdded;
		this.fetchRecord();
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.sql.SQLObject#fetchRecord()
	 */
	
	protected void fetchRecord() throws DataException {
		this.fetchRecord(SQL_LOAD_NEWS);
	}
	
	protected void setID(Date dateID) {
		this.myID = this.dateFormat.format(dateID);
	}
	
	public String getContent() throws DataException {
		return this.myData.getString(CONTENT_COLUMN);
	}
	
	public void setContent(String newValue) throws DataException {
		this.myData.setString(CONTENT_COLUMN, newValue);
	}
	
	public Date getDateAdded() throws DataException {
		return this.myData.getTimestamp(DATE_ADDED_COLUMN);
	}
	
	public void setDateAdded(Date newValue) throws DataException {
		this.myData.setTimestamp(DATE_ADDED_COLUMN, newValue);
	}
	
	public void setExpiration(Date newValue) throws DataException {
		this.myData.setTimestamp(DATE_EXPIRES_COLUMN, newValue);
	}
	
	public void setExpiration(String newValue) throws DataException {
		this.myData.setString(DATE_EXPIRES_COLUMN, newValue);
	}
	
	public String getExpirationString() throws DataException {
		return this.dateFormat.format(this.getExpiration());
	}
	
	public Date getExpiration() throws DataException {
		return this.myData.getTimestamp(DATE_EXPIRES_COLUMN);
	}
	
	public String getSubject() throws DataException {
		return this.myData.getString(SUBJECT_COLUMN);
	}
	
	public void setSubject(String newValue) throws DataException {
		this.myData.setString(SUBJECT_COLUMN, newValue);
	}
	
}
