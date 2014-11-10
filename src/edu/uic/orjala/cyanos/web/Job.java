/**
 * 
 */
package edu.uic.orjala.cyanos.web;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.Role;
import edu.uic.orjala.cyanos.User;
import edu.uic.orjala.cyanos.sql.SQLData;

/**
 * @author George Chlipala
 *
 */
public class Job implements Runnable {

	protected String id;
	protected String owner;
	protected final StringBuffer messages = new StringBuffer();
	protected String output;
	protected String outputType = "text";
	protected Date startDate;
	protected Date endDate;
	protected String type;
	protected float progress = 0.0f;
	
	protected SQLData myData = null;
	protected Thread parseThread = null;
	
	protected boolean working = false;
	
	/**
	 * 
	 */
	public Job(SQLData data) {
		this.myData = data;
	}

	private final static String SQL_SELECT = "SELECT job_id,owner,job_type,messages,output,progress,startDate,endDate,output_type FROM jobs WHERE job_id=?";
	
	private Job(SQLData data, ResultSet results) throws SQLException {
		this(data);
		this.id = results.getString(1);
		this.owner = results.getString(2);
		this.type = results.getString(3);
		this.messages.append(results.getString(4));
		this.output = results.getString(5);
		this.progress = results.getFloat(6);
		this.startDate = results.getTimestamp(7);
		this.endDate = results.getTimestamp(8);
		this.outputType = results.getString(9);
	}
	
	public static Job loadJob(SQLData data, String jobID) throws DataException {
		try {
			PreparedStatement sth = data.prepareStatement(SQL_SELECT);
			sth.setString(1, jobID);
			ResultSet results = sth.executeQuery();
			results.first();
			String owner = results.getString(2);
			if ( data.getUser().isAllowed(User.ADMIN_ROLE, User.GLOBAL_PROJECT, Role.READ) || data.getUser().getUserID().equals(owner) ) {
				Job job = new Job(data, results);
				results.close();
				sth.close();
				return job;
			} else {
				return null;
			}
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}
	
	private final static String SQL_OLD_JOBS = "SELECT job_id,owner,job_type,messages,output,progress,startDate,endDate,output_type FROM jobs WHERE owner=? AND endDate IS NOT NULL";

	public static List<Job> oldJobs(SQLData data) throws DataException {
		List<Job> jobList = new ArrayList<Job>();
		try {
			PreparedStatement sth = data.prepareStatement(SQL_OLD_JOBS);
			sth.setString(1, data.getUser().getUserID() );
			ResultSet results = sth.executeQuery();
			while ( results.next() ) {
				jobList.add(new Job(data, results));
			}
			results.close();
			sth.close();
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return jobList;
	}
	
	
	private final static String SQL_INSERT_JOB = "INSERT INTO jobs(job_type,owner) VALUES(?,?)";
	
	
	protected void create() throws DataException {
		try {
			PreparedStatement sth = myData.prepareStatement(SQL_INSERT_JOB, true);
			sth.setString(1, this.getType());
			sth.setString(2, this.myData.getUser().getUserID());
			if ( sth.executeUpdate() == 1 ) {
				ResultSet results = sth.getGeneratedKeys();
				results.first();
				this.id = results.getString(1);
				results.close();
				sth.close();
				sth = myData.prepareStatement(SQL_SELECT);
				sth.setString(1, this.id);
				results = sth.executeQuery();
				if ( results.first() ) {
					this.startDate = results.getDate(7);
				}
			}
			sth.close();
		} catch (SQLException e) {
			throw new DataException(e);
		}
	}
	
	private final static String SQL_UPDATE = "UPDATE jobs SET messages=?, output=?, output_type=?, endDate=? WHERE job_id=?";
	
	protected void update() throws DataException {
		if ( this.id != null ) {
			try {
				PreparedStatement sth = myData.prepareStatement(SQL_UPDATE);
				sth.setString(1, this.messages.toString());
				sth.setString(2, this.output);
				sth.setString(3, outputType);
				Timestamp endValue = ( this.endDate != null ? new Timestamp(this.endDate.getTime()) : null);
				sth.setTimestamp(4, endValue);
				sth.setString(5, id);
				sth.executeUpdate();
				sth.close();
			} catch (SQLException e) {
				throw new DataException(e);
			}	
		}		
	}

	public String getID() {
		return this.id;
	}

	public String getOwner() {
		return this.owner;
	}

	public String getType() {
		return this.type;
	}
	
	public Date getStartDate() {
		return this.startDate;
	}
	
	public Date getEndDate() {
		return this.endDate;
	}
	
	public String getMessages() {
		return messages.toString();
	}
	
	public String getOutput() {
		return this.output;
	}
	
	public String getOutputType() {
		return this.outputType;
	}
	
	/**
	 * Returns current job progress. 
	 * 
	 * @return the current progress (0 &le; N &le; 1) as a float.  If indeterminate return -1
	 */

	public float getProgress() {
		return this.progress;
	}
	
	/**
	 * Return true if the job is still running.
	 * 
	 * @return true if still running
	 */
	public boolean isWorking() {
		return working;
	}
	
	/**
	 * Return true if upload parsing is complete.
	 * 
	 * @return true if complete.
	 */
	public boolean isDone() {
		return ( getProgress() == 1.0f ) && (! working);
	}
	
	/**
	 * Close the job and cleanup.
	 * 
	 * @throws DataException
	 */
	public void close() throws DataException {
		this.myData.close();
		this.myData.closeDBC();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
}
