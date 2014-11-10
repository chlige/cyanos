package edu.uic.orjala.cyanos.web.upload;

import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.sql.SQLData;
import edu.uic.orjala.cyanos.web.Job;
import edu.uic.orjala.cyanos.web.Sheet;
import edu.uic.orjala.cyanos.web.servlet.UploadServlet;

/**
 * Interface for Upload modules.  Custom upload modules must implement this class.  
 * 
 * @author George Chlipala
 *
 */
public abstract class UploadJob extends Job {
	
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
	protected Sheet worksheet = null;
	protected final Map<String,String> template = new HashMap<String,String>();
	
	protected Savepoint savepoint = null;
	
	protected final ArrayList<Integer> rowList = new ArrayList<Integer>();
	
	protected Sheet resultSheet = null;
	
	public static final String OUTPUT_TYPE = "table";

	public UploadJob(SQLData data) {
		super(data);
		this.outputType = OUTPUT_TYPE;
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
	
	/**
	 * Start paring the upload.
	 * @throws SQLException 
	 * @throws DataException 
	 */
	public void startParse(HttpServletRequest req, Sheet worksheet) throws DataException, SQLException {
		if ( this.parseThread == null ) {
//			this.myData = UploadServlet.newSQLData(req);
			this.create();
			
			this.worksheet = worksheet;
			this.updateTemplate(req);
			this.setupRowList(req);
			
			try {
				this.savepoint = this.myData.setSavepoint();
				this.parseThread = new Thread(this, THREAD_LABEL);
				this.parseThread.start();
			} catch (SQLException e) {
				this.messages.append("<P ALIGN='CENTER'><B><FONT COLOR='red'>ERROR:</FONT>");
				this.messages.append(e.getMessage());
				this.messages.append("</B></P>");
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

		if ( req.getParameter(UploadServlet.PARAM_ROWS) != null ) {
			String[] rows = req.getParameterValues(UploadServlet.PARAM_ROWS);
			if ( rows != null ) {
				for ( int i = 0; i < rows.length; i++ ) {
					this.rowList.add(Integer.valueOf(rows[i]));
				}
				Collections.sort(this.rowList);
			}

			if ( UploadServlet.ROW_BEHAVIOR_IGNORE.equals(req.getParameter(UploadServlet.PARAM_ROW_BEHAVIOR)) ) {
				System.err.println("DOING FLIP");
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
		} else if ( UploadServlet.ROW_BEHAVIOR_IGNORE.equals(req.getParameter(UploadServlet.PARAM_ROW_BEHAVIOR)) ) {
			int startNum = ( hasHeaderRow ? 1 : 0 );
			int rowCount = this.worksheet.rowCount();
			this.rowList.ensureCapacity(rowCount - startNum);
			for (int i = startNum; i < rowCount; i++ ) {
				this.rowList.add(Integer.valueOf(i));
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
		return this.messages.toString();
	}

	/**
	 * Return results as a {@link Sheet}. Can be null.
	 * 
	 * @return Results as a {@link Sheet}.
	 */
	public Sheet resultSheet() {
		return this.resultSheet;
	}

	public void clearResults() {
		this.messages.setLength(0);
		this.resultSheet = null;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.Job#update()
	 */
	@Override
	protected void update() throws DataException {
		if ( this.resultSheet != null ) {
			this.output = this.resultSheet.asCSV();
		}
		super.update();
	}	
	
	protected void finishJob() {
		try {
			this.endDate = new Date();
			if ( this.working ) { 
				this.myData.commit(); 
				this.messages.append("<P ALIGN='CENTER'><B>EXECUTION COMPLETE</B> CHANGES COMMITTED.</P>"); 
			} else { 
				this.myData.rollback(); 
				this.messages.append("<P ALIGN='CENTER'><B>EXECUTION HALTED</B> Upload incomplete!</P>"); 
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

}