/**
 * 
 */
package edu.uic.orjala.cyanos.web.job;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import edu.uic.orjala.cyanos.Compound;
import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.sql.SQLCompound;
import edu.uic.orjala.cyanos.sql.SQLData;
import edu.uic.orjala.cyanos.web.Job;

/**
 * @author George Chlipala
 *
 */
public class DereplicationQuery extends Job {


	private static final String THREAD_LABEL = "dereplication";
	private Compound compoundList;
	
	private final List<String> columns = new ArrayList<String>();
	private final StringBuffer tableBuffer = new StringBuffer();
	private final StringBuffer queryBuffer = new StringBuffer();
	private final StringBuffer havingBuffer = new StringBuffer();
	
	public DereplicationQuery(SQLData data) {
		super(data);
		super.type = "Dereplication Query";
		this.outputType = "table";
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		if ( this.working ) return;
		this.progress = 0.0f;
		this.working = true;

		try {
			if ( columns.size() > 0 ) {
				String[] cols = {};
				this.compoundList = SQLCompound.compoundQuery(this.myData, columns.toArray(cols), this.buildQuery());
			} else {
				this.compoundList = SQLCompound.compoundQuery(this.myData, this.buildQuery());
			}
			
			StringBuffer out = new StringBuffer("compound_id,name,formula,inchi_key,notes\n");
			try {
				compoundList.beforeFirst();
				
				while ( compoundList.next() ) {
					out.append(compoundList.getID());
					out.append(",");
					out.append(compoundList.getName());
					out.append(",");
					out.append(compoundList.getFormula());
					out.append(",");
					out.append(compoundList.getInChiKey());
					out.append(",\"");
					String notes = compoundList.getNotes();
					notes.replaceAll("\n", " ");
					notes.replaceAll("\"", "''");
					out.append(notes);
					out.append("\"\n");
				}
				compoundList.beforeFirst();
			} catch (DataException e) {
				this.messages.append("<P ALIGN='CENTER'><B><FONT COLOR='red'>ERROR:</FONT>" + e.getMessage() + "</B></P>");
				e.printStackTrace();			
			} finally {
				this.output = out.toString();
			}

		} catch (Exception e) {
			this.messages.append("<P ALIGN='CENTER'><B><FONT COLOR='red'>ERROR:</FONT>" + e.getMessage() + "</B></P>");
			e.printStackTrace();
			this.working = false;
		} finally {
			this.finishJob();
		}
	}

	/**
	 * Start paring the upload.
	 * @throws DataException 
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
				this.messages.append("<P ALIGN='CENTER'><B>SEARCH COMPLETE</B>.</P>"); 
			} else { 
				this.messages.append("<P ALIGN='CENTER'><B>SEARCH HALTED:</B> Job incomplete!</P>"); 
			}
			this.update();
			this.myData.close();
			this.myData.closeDBC();
		} catch (DataException e) {
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
	
	public Compound getCompounds() {
		return this.compoundList;
	}
	
	public String buildQuery() {
		StringBuffer sql = new StringBuffer(tableBuffer);
		if ( queryBuffer.length() > 0 ) {
			sql.append(" WHERE ");
			sql.append(queryBuffer);
		}
		sql.append(" GROUP BY ");
		sql.append(SQLCompound.TABLE);
		sql.append(".");
		sql.append(SQLCompound.ID_COLUMN);

		if ( havingBuffer.length() > 0 ) {
			sql.append(" HAVING ");
			sql.append(havingBuffer);
		}
		sql.append(" ORDER BY ");		
		sql.append(SQLCompound.TABLE);
		sql.append(".");
		sql.append(SQLCompound.ID_COLUMN);
		sql.append(" ASC");
		
		return sql.toString();
	}

	
	public void addQuery(String query) {
		if ( queryBuffer.length() > 0 )
			queryBuffer.append(" AND ");
		queryBuffer.append(query);
	}
	
	public void addHaving(String having) {
		if ( havingBuffer.length() > 0 )
			havingBuffer.append(" AND ");
		havingBuffer.append(having);
	}
	
	public void addTable(String table, String onclause) {
		tableBuffer.append(" JOIN ");
		tableBuffer.append(table);
		tableBuffer.append(" ON (");
		tableBuffer.append(onclause);
		tableBuffer.append(")");
	}
	
	public void addColumn(String table, String column) {
		columns.add(table.concat(".").concat(column));
	}

	public void addColumn(String column) {
		columns.add(column);
	}

}
