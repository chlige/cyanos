/**
 * 
 */
package edu.uic.orjala.cyanos.web.job;

import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.Date;

import edu.uic.orjala.cyanos.Compound;
import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.sql.SQLCompound;
import edu.uic.orjala.cyanos.sql.SQLData;
import edu.uic.orjala.cyanos.web.Job;

/**
 * @author George Chlipala
 *
 */
public class RebuildCompoundGraphJob extends Job {
	
	private static final String THREAD_LABEL = "regenCompoundGraph";
	protected int todos = 0;
	protected int done = 0;	

	/**
	 * @param data
	 */
	public RebuildCompoundGraphJob(SQLData data) {
		super(data);
		this.type = "Rebuild Chemical Structure Index";
	}
	
	/**
	 * Returns current upload progress. 
	 * 
	 * @return the current progress (0 &le; N &le; 1) as a float.
	 */
	@Override
	public float getProgress() {
		Float doneFL = new Float(done);
		Float todosFL = new Float(todos);
		return doneFL / todosFL;
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.Job#run()
	 */
	@Override
	public void run() {
		if ( this.working ) return;
		this.done = 0;
		this.working = true;

		try {
			Compound compounds = SQLCompound.compoundsWithMDLData(myData);
			this.todos = compounds.count();
			compounds.beforeFirst();
			
			while ( this.working && compounds.next() ) {
				this.messages.append("Compound ");
				this.messages.append(compounds.getID());
				Savepoint savepoint = this.myData.setSavepoint("compound-update");
				((SQLCompound)compounds).updateGraph();
				this.myData.commit();
				this.myData.releaseSavepoint(savepoint);
				this.messages.append(" <b>Updated</b><br>");
				this.done++;
			}
		} catch (Exception e) {
			this.messages.append("<P ALIGN='CENTER'><B><FONT COLOR='red'>ERROR:</FONT>" + e.getMessage() + "</B></P>");
			e.printStackTrace();
			this.working = false;
		}
		this.finishJob();
	}

	/**
	 * Start paring the upload.
	 */
	public void startJob() throws DataException {
		if ( this.parseThread == null ) {
			this.create();
			this.parseThread = new Thread(this, THREAD_LABEL);
			this.parseThread.start();
		}
	}
	
	protected void finishJob() {
		try {
			this.endDate = new Date();
			if ( this.working ) { 
				this.myData.commit(); 
				this.messages.append("<P ALIGN='CENTER'><B>EXECUTION COMPLETE</B> CHANGES COMMITTED.</P>"); 
			} else { 
				this.myData.rollback(); 
				this.messages.append("<P ALIGN='CENTER'><B>EXECUTION HALTED</B> Job incomplete!</P>"); 
			}
			this.update();
			this.myData.close();
		} catch (DataException e) {
			this.messages.append("<P ALIGN='CENTER'><B><FONT COLOR='red'>ERROR:</FONT>" + e.getMessage() + "</B></P>");
			e.printStackTrace();			
		} catch (SQLException e) {
			this.messages.append("<P ALIGN='CENTER'><B><FONT COLOR='red'>ERROR:</FONT>" + e.getMessage() + "</B></P>");
			e.printStackTrace();			
		} finally {
			this.working = false;
		}
	}

	/**
	 * Stop parsing the upload.
	 */
	public void stopJob() {
		if ( this.parseThread != null ) {
			this.working = false;
			this.parseThread = null;
		}
	}

}
