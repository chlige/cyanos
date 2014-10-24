package edu.uic.orjala.cyanos.web;

import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.sql.SQLData;
import edu.uic.orjala.cyanos.web.servlet.UploadServlet;

/**
 * Interface for Upload modules.  Custom upload modules must implement this class.  
 * 
 * @author George Chlipala
 *
 */
public abstract class UploadJob implements Runnable {
	
	public final static String ERROR_TAG = "<FONT COLOR='red'><B>ERROR:</FONT></B> ";
	public final static String FAILED_TAG = "<FONT COLOR='red'><B>FAILED:</FONT></B> ";
	public final static String WARNING_TAG = "<FONT COLOR='orange'><B>WARNING:</FONT></B> ";
	public final static String SUCCESS_TAG = "<FONT COLOR='green'><B>SUCCESS:</FONT></B> ";
	public final static String FOUND_TAG = "<FONT COLOR='blue'><B>FOUND:</FONT></B> ";
	public final static String SKIP_TAG = "<FONT COLOR='purple'><B>SKIPPED:</FONT></B> ";
	public final static String NOTICE_TAG = "<FONT COLOR='gray'><B>NOTICE:</FONT></B> ";
	
	public static final int ROW_BEHAVIOR_INCLUDE = 1;
	public static final int ROW_BEHAVIOR_IGNORE = 2;
	
	private static final String THREAD_LABEL = "UPLOAD JOB";

	protected int todos = 0;
	protected int done = 0;	
	protected boolean working = false;
	protected SQLData myData = null;
	protected Thread parseThread = null;
	protected Sheet worksheet = null;
	protected final Map<String,String> template = new HashMap<String,String>();
	
	protected Savepoint savepoint = null;
	
	protected final List<Integer> rowList = new ArrayList<Integer>();
	
	protected final StringBuffer resultOutput = new StringBuffer();
	protected Sheet resultSheet = null;


	
	/**
	 * Returns current upload progress. 
	 * 
	 * @return the current progress (0 &le; N &le; 1) as a float.
	 */
	public float status() {
		Float doneFL = new Float(done);
		Float todosFL = new Float(todos);
		return doneFL / todosFL;
	}
	
	/**
	 * Return true if upload parsing is complete.
	 * 
	 * @return true if complete.
	 */
	public boolean isDone() {
		return ( todos == done ) && (! working);
	}
	
	/**
	 * Return true if the upload job is still running.
	 * 
	 * @return true if still running
	 */
	public boolean isWorking() {
		return working;
	}

	/**
	 * Start paring the upload.
	 * @throws SQLException 
	 * @throws DataException 
	 */
	public void startParse(HttpServletRequest req, Sheet worksheet) throws DataException, SQLException {
		if ( this.parseThread == null ) {
			this.myData = UploadServlet.newSQLData(req);
			
			this.worksheet = worksheet;
			this.updateTemplate(req);
			this.setupRowList(req);
			
			try {
				this.savepoint = this.myData.setSavepoint();
				this.parseThread = new Thread(this, THREAD_LABEL);
				this.parseThread.start();
			} catch (SQLException e) {
				this.resultOutput.append("<P ALIGN='CENTER'><B><FONT COLOR='red'>ERROR:</FONT>");
				this.resultOutput.append(e.getMessage());
				this.resultOutput.append("</B></P>");
				e.printStackTrace();
			}
		}
	}

	void updateTemplate(HttpServletRequest req) {
		String[] keys = getTemplateKeys();
		for (int i = 0; i < keys.length; i++ ) {
			if ( req.getParameter(keys[i]) != null ) {
				template.put(keys[i], req.getParameter(keys[i]));
			}
		}
		
		if ( this.worksheet != null ) {
		}
		
	}
	
	private void setupRowList(HttpServletRequest req) {
		this.rowList.clear();			
		boolean hasHeaderRow = req.getParameter(UploadServlet.PARAM_HEADER) != null;

		if ( ! ( req.getParameter(UploadServlet.PARAM_ROW_BEHAVIOR) == null && req.getParameter(UploadServlet.PARAM_ROWS) == null ) ) {
			String[] rows = req.getParameterValues(UploadServlet.PARAM_ROWS);
			if ( rows != null ) {
				for ( int i = 0; i < rows.length; i++ ) {
					this.rowList.add(Integer.valueOf(rows[i]));
				}
				Collections.sort(this.rowList);
			}

			if (req.getParameter(UploadServlet.PARAM_ROW_BEHAVIOR) == UploadServlet.ROW_BEHAVIOR_IGNORE ) {
				List<Integer> newRowNum = new ArrayList<Integer>();
				int startNum = ( hasHeaderRow ? 1 : 0 );
				int rowCount = this.worksheet.rowCount();
				for (int i = startNum; i < rowCount; i++ ) {
					if ( ! this.rowList.contains(i) ) {
						newRowNum.add(Integer.valueOf(i));
					}
				}
				this.rowList.clear();
				this.rowList.addAll(newRowNum);
			}
		}
	}

	
	protected abstract String[] getTemplateKeys();

	/**
	 * Stop parsing the upload.
	 */
	public void stopParse() {
		if ( this.parseThread != null ) {
			this.parseThread = null;
			this.working = false;
		}
	}

	/**
	 * Return the report of upload parsing.
	 * 
	 * @return HTML report as a String.
	 */
	public String resultReport() {
		return this.resultOutput.toString();
	}

	/**
	 * Return results as a {@link Sheet}. Can be null.
	 * 
	 * @return Results as a {@link Sheet}.
	 */
	public Sheet resultSheet() {
		return this.resultSheet;
	}

	/**
	 * Close the upload module and cleanup.
	 * 
	 * @throws DataException
	 */
	public void close() throws DataException {
		this.myData.close();
		this.myData.closeDBC();
	}
	
	public void clearResults() {
		this.resultOutput.setLength(0);
		this.resultSheet = null;
	}
	
}