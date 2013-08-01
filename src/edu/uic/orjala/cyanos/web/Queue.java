/**
 * 
 */
package edu.uic.orjala.cyanos.web;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.Harvest;
import edu.uic.orjala.cyanos.Inoc;
import edu.uic.orjala.cyanos.Sample;
import edu.uic.orjala.cyanos.Strain;
import edu.uic.orjala.cyanos.sql.SQLData;
import edu.uic.orjala.cyanos.sql.SQLObject;

/**
 * @author gchlip2
 *
 */
public class Queue extends SQLObject {

	private static final String QUEUE_LIST_SQL = "SELECT DISTINCT queue_name,queue_type FROM queue WHERE queue_type=? ORDER BY queue_name ASC";
	private static final String SUBSCRIBED_LIST_SQL = "SELECT queue_name,queue_type FROM queue_subscription WHERE username=? ORDER BY queue_type,queue_name ASC";
	private static final String ALL_QUEUES_SQL = "SELECT DISTINCT queue_name,queue_type FROM queue ORDER BY queue_type,queue_name ASC";
	
	private static final String ADD_ITEM_SQL = "INSERT INTO queue(queue_name,queue_type,item_type,item_id,added,added_by,req_details) VALUES(?,?,?,?,CURRENT_DATE,?,?)";
	private static final String ADD_SUBSCRIPTION_SQL = "INSERT INTO queue_subscription(queue_name,queue_type,username) VALUES(?,?,?)";
	
	private static final String COUNT_ALL_SQL = "SELECT COUNT(added) FROM queue WHERE queue_name=? AND queue_type=?";
	private static final String COUNT_COMPLETED_SQL = "SELECT COUNT(added) FROM queue WHERE queue_name=? AND queue_type=? AND completed IS NOT NULL";
	private static final String COUNT_INCOMPLETE_SQL = "SELECT COUNT(added) FROM queue WHERE queue_name=? AND queue_type=? AND completed IS NULL";
	
	public static final String INOCULATION_QUEUE = "inoculation";
	public static final String CRYO_QUEUE = "cryo";
	public static final String BIOASSAY_QUEUE = "assay";
	public static final String HARVEST_QUEUE = "harvest";
	public static final String EXTRACTION_QUEUE = "extract";
	public static final String FRACTION_QUEUE = "separation";

	public static final String[] SPECIES_QUEUES = { INOCULATION_QUEUE, CRYO_QUEUE, BIOASSAY_QUEUE };
	public static final String[] INOC_QUEUES = { HARVEST_QUEUE, CRYO_QUEUE };
	public static final String[] HARVEST_QUEUES = { EXTRACTION_QUEUE };
	public static final String[] SAMPLE_QUEUES = { FRACTION_QUEUE, BIOASSAY_QUEUE };
	
	private static final String NAME_COLUMN = "queue_name";
	private static final String TYPE_COLUMN = "queue_type";
	
	private static final String STRAIN_TYPE = "strain";
	private static final String SAMPLE_TYPE = "sample";
	private static final String INOC_TYPE = "inoc";
	private static final String HARVEST_TYPE = "harvest";

	private static final String QUEUE_USER_SQL = "SELECT DISTINCT queue_name,queue_type FROM queue WHERE queue_type='user' AND queue_name = ?";

	
	public static void addItem(SQLData data, String queueType, String queueName, Strain aStrain, String requestDetails) throws DataException {
		Queue.addItem(data, queueType, queueName, STRAIN_TYPE, aStrain.getID(), requestDetails);
	}

	public static void addItem(SQLData data, String queueType, String queueName, Sample aSample, String requestDetails) throws DataException {
		Queue.addItem(data, queueType, queueName, SAMPLE_TYPE, aSample.getID(), requestDetails);
	}

	public static void addItem(SQLData data, String queueType, String queueName, Inoc anInoc, String requestDetails) throws DataException {
		Queue.addItem(data, queueType, queueName, INOC_TYPE, anInoc.getID(), requestDetails);
	}
	
	public static void addItem(SQLData data, String queueType, String queueName, Harvest aHarv, String requestDetails) throws DataException {
		Queue.addItem(data, queueType, queueName, HARVEST_TYPE, aHarv.getID(), requestDetails);
	}
	
	public static void addItem(SQLData data, String queueType, String queueName, String itemType, String itemID, String requestDetails) throws DataException {
		PreparedStatement aSth = null;
		try {
			aSth = data.prepareStatement(ADD_ITEM_SQL);
			aSth.setString(1, queueType);
			aSth.setString(2, queueName);
			aSth.setString(3, itemType);
			aSth.setString(4, itemID);
			aSth.setString(5, data.getUser().getUserID());
			aSth.setString(6, requestDetails);
			aSth.executeUpdate();
			aSth.close();
		} catch (DataException e) {
			throw e;
		} catch (SQLException e) {
			throw new DataException(e);
		} finally {
			if ( aSth != null ) {
				try {
					aSth.close();
				} catch (SQLException e) {

				}
			}
		}
	}
	
	public static Queue myQueues(SQLData data) throws DataException {
		Queue aQueue = new Queue(data);
		try {
			PreparedStatement aSth = data.prepareStatement(SUBSCRIBED_LIST_SQL);
			aSth.setString(1, data.getUser().getUserID());
			aQueue.loadUsingPreparedStatement(aSth);
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return aQueue;
	}
	
	public static Queue userQueue(SQLData data) throws DataException {
		Queue aQueue = new Queue(data);
		try {
			PreparedStatement aSth = data.prepareStatement(QUEUE_USER_SQL);
			aSth.setString(1, data.getUser().getUserID());
			aQueue.loadUsingPreparedStatement(aSth);
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return aQueue;		
	}
	
	public static Queue allQueues(SQLData data) throws DataException {
		Queue aQueue = new Queue(data);
		aQueue.loadSQL(ALL_QUEUES_SQL);
		return aQueue;
	}

	public static Queue queuesForType(SQLData data, String queueType) throws DataException {
		Queue aQueue = new Queue(data);
		try {
			PreparedStatement aSth = data.prepareStatement(QUEUE_LIST_SQL);
			aSth.setString(1, queueType);
			aQueue.loadUsingPreparedStatement(aSth);
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return aQueue;
	}
	
	public static void subscribe(SQLData data, String queueType, String queueName) throws DataException {
		PreparedStatement aPsth = null;
		DataException excep = null;
		try {
			aPsth = data.prepareStatement(ADD_SUBSCRIPTION_SQL);
			aPsth.setString(1, queueName);
			aPsth.setString(2, queueType);
			aPsth.setString(3, data.getUser().getUserID());
			aPsth.executeUpdate();
		} catch (SQLException e) {
			excep = new DataException(e);
		}
		if ( aPsth != null ) {
			try {
				aPsth.close();
			} catch (SQLException e) {
				// DO NOTHING.
			}
		}
		if ( excep != null )
			throw excep;
	}
	
	/**
	 * @param data
	 */
	protected Queue(SQLData data) {
		super(data);
		this.idField = NAME_COLUMN;
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.sql.SQLObject#fetchRecord()
	 */
	
	protected void fetchRecord() throws DataException {
		// TODO Auto-generated method stub
	}
	
	public String getQueueName() throws DataException {
		return this.myData.getString(NAME_COLUMN);
	}
	
	public String getQueueType() throws DataException {
		return this.myData.getString(TYPE_COLUMN);
	}

	public void subscribe() throws DataException {
		PreparedStatement aPsth = null;
		DataException excep = null;
		try {
			aPsth = this.prepareStatement(ADD_SUBSCRIPTION_SQL);
			aPsth.setString(1, this.getQueueName());
			aPsth.setString(2, this.getQueueType());
			aPsth.setString(3, this.myData.getUser().getUserID());
			aPsth.executeUpdate();
		} catch (SQLException e) {
			excep = new DataException(e);
		}
		if ( aPsth != null ) {
			try {
				aPsth.close();
			} catch (SQLException e) {
				// DO NOTHING.
			}
		}
		if ( excep != null )
			throw excep;
	}
	
	
	private int numberForSQL(String sqlString) throws DataException {
		PreparedStatement aPsth = null;
		DataException excep = null;
		int number = 0;
		try {
			aPsth = this.prepareStatement(sqlString);
			aPsth.setString(1, this.getQueueName());
			aPsth.setString(2, this.getQueueType());
			ResultSet aResult = aPsth.executeQuery();
			if ( aResult.first() ) {
				number = aResult.getInt(1);
			}
			aResult.close();
		} catch (SQLException e) {
			excep = new DataException(e);
		}
		if ( aPsth != null ) {
			try {
				aPsth.close();
			} catch (SQLException e) {

			}
		}
		if ( excep != null )
			throw excep;
		return number;
	}

	public int numberOfItems() throws DataException {
		return this.numberForSQL(COUNT_ALL_SQL);
	}

	public int numberComplete() throws DataException {
		return this.numberForSQL(COUNT_COMPLETED_SQL);
	}
	
	public int numberIncomplete() throws DataException {
		return this.numberForSQL(COUNT_INCOMPLETE_SQL);
	}
	
	public QueueItem allItems() throws DataException {
		return QueueItem.loadAll(this.myData, this);
	}
	
	public QueueItem completedItems() throws DataException {
		return QueueItem.loadCompleted(this.myData, this);
	}
	
	public QueueItem incompleteItems() throws DataException {
		return QueueItem.loadIncomplete(this.myData, this);
	}
}
