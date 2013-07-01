/**
 * 
 */
package edu.uic.orjala.cyanos.web;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;

import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.User;
import edu.uic.orjala.cyanos.sql.SQLData;
import edu.uic.orjala.cyanos.sql.SQLObject;
import edu.uic.orjala.cyanos.sql.SQLUser;

/**
 * @author gchlip2
 *
 */
public class QueueItem extends SQLObject {

	private static final String ITEM_TYPE_COLUMN = "item_type";
	private static final String ITEM_ID_COLUMN = "item_id";
	private static final String DATE_ADDED_COLUMN = "added";
	private static final String DATE_COMPLETED_COLUMN = "completed";
	private static final String REQUEST_DETAILS_COLUMN = "req_details";
	private static final String COMPLETION_DETAILS_COLUMN = "complete_details";
	private static final String REQUEST_BY_COLUMN = "added_by";
	private static final String COMPLETED_BY_COLUMN = "completed_by";

	protected static final String LOAD_ALL_SQL = "SELECT item_type,item_id,req_details,complete_details,added,completed,completed_by,added_by,queue_type,queue_name FROM queue WHERE queue_type=? AND queue_name=? ORDER BY completed DESC";
	protected static final String LOAD_COMPLETED_SQL = "SELECT item_type,item_id,req_details,complete_details,added,completed,completed_by,added_by,queue_type,queue_name FROM queue WHERE queue_type=? AND queue_name=? AND completed IS NOT NULL ORDER BY completed DESC";
	protected static final String LOAD_INCOMPLETE_SQL = "SELECT item_type,item_id,req_details,complete_details,added,completed,completed_by,added_by,queue_type,queue_name FROM queue WHERE queue_type=? AND queue_name=? AND completed IS NULL ORDER BY added ASC";
	
	private static QueueItem loadItems(SQLData data, String sqlString, String queueName, String queueType) throws DataException {
		QueueItem anItem = new QueueItem(data);
		try {
			PreparedStatement aSth = data.prepareStatement(sqlString);
			aSth.setString(1, queueType);
			aSth.setString(2, queueName);
			anItem.loadUsingPreparedStatement(aSth);
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return anItem;
	}
	
	public static QueueItem loadAll(SQLData data, Queue aQueue) throws DataException {
		return QueueItem.loadItems(data, LOAD_ALL_SQL, aQueue.getQueueName(), aQueue.getQueueType());
	}
	
	public static QueueItem loadCompleted(SQLData data, Queue aQueue) throws DataException {
		return QueueItem.loadItems(data, LOAD_COMPLETED_SQL, aQueue.getQueueName(), aQueue.getQueueType());
	}
	
	public static QueueItem loadIncomplete(SQLData data, Queue aQueue) throws DataException {
		return QueueItem.loadItems(data, LOAD_INCOMPLETE_SQL, aQueue.getQueueName(), aQueue.getQueueType());
	}

	public static QueueItem loadAll(SQLData data, String queueType, String queueName) throws DataException {
		return QueueItem.loadItems(data, LOAD_ALL_SQL, queueName, queueType);
	}
	
	public static QueueItem loadCompleted(SQLData data, String queueType, String queueName) throws DataException {
		return QueueItem.loadItems(data, LOAD_COMPLETED_SQL, queueName, queueType);
	}
	
	public static QueueItem loadIncomplete(SQLData data, String queueType, String queueName) throws DataException {
		return QueueItem.loadItems(data, LOAD_INCOMPLETE_SQL, queueName, queueType);
	}


	/**
	 * @param data
	 */
	protected QueueItem(SQLData data) {
		super(data);
		this.idField = ITEM_ID_COLUMN;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.sql.SQLObject#fetchRecord()
	 */
	@Override
	protected void fetchRecord() throws DataException {
		// TODO Auto-generated method stub
	}
	
	public String getItemType() throws DataException {
		return this.myData.getString(ITEM_TYPE_COLUMN);
	}
	
	public String getItemID() throws DataException {
		return this.myData.getString(ITEM_ID_COLUMN);
	}
	
	public Date getDateAdded() throws DataException {
		return this.myData.getDate(DATE_ADDED_COLUMN);
	}

	public Date getDateCompleted() throws DataException {
		return this.myData.getDate(DATE_COMPLETED_COLUMN);
	}
	
	public String getRequestDetails() throws DataException {
		return this.myData.getString(REQUEST_DETAILS_COLUMN);
	}
	
	public String getCompletionDetails() throws DataException {
		return this.myData.getString(COMPLETION_DETAILS_COLUMN);
	}
	
	public String getRequestedUserID() throws DataException {
		return this.myData.getString(REQUEST_BY_COLUMN);
	}
	
	public User getRequestedBy() throws DataException {
		return new SQLUser(this.myData, this.getRequestedUserID());
	}
	
	public String getCompletedUserID() throws DataException {
		return this.myData.getString(COMPLETED_BY_COLUMN);
	}
	
	public User getCompletedBy() throws DataException {
		return new SQLUser(this.myData, this.getRequestedUserID());
	}
	
	public void complete(String details) throws DataException {
		this.myData.setManualRefresh();
		this.myData.setString(COMPLETED_BY_COLUMN, this.myData.getUser().getUserID());
		this.myData.setString(COMPLETION_DETAILS_COLUMN, details);
		this.myData.setDate(DATE_COMPLETED_COLUMN, new Date());
		this.refresh();
		this.myData.setAutoRefresh();
	}
}
