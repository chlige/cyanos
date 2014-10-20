package edu.uic.orjala.cyanos.web;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.sql.SQLData;

/**
 * Interface for Upload modules.  Custom upload modules must implement this class.  
 * Custom modules can optionally extend {@link UploadForm}.
 * 
 * <P>Upload modules should implement a constructor that accepts {@link CyanosWrapper} and {@link Sheet} 
 * as the arguments.  The {@link CyanosWrapper} will be the Upload Servlet and {@link Sheet} will be
 * the currently selected worksheet of the uploaded spreadsheet file.</P>
 * 
 * @author George Chlipala
 *
 */
public interface UploadJob {
	
	public static final int ROW_BEHAVIOR_INCLUDE = 1;
	public static final int ROW_BEHAVIOR_IGNORE = 2;
	
	/**
	 * Returns current upload progress. 
	 * 
	 * @return the current progress (0 &le; N &le; 1) as a float.
	 */
	float status();

	/**
	 * Return true if upload parsing is complete.
	 * 
	 * @return true if complete.
	 */
	boolean isDone();

	/**
	 * Return true if the upload job is still running.
	 * 
	 * @return true if still running
	 */
	boolean isWorking();

	/**
	 * Start paring the upload.
	 */
	void startParse(HttpServletRequest request, SQLData data);

	/**
	 * Stop parsing the upload.
	 */
	void stopParse();

	/**
	 * Return the report of upload parsing.
	 * 
	 * @return HTML report as a String.
	 */
	String resultReport();

	/**
	 * Return results as a {@link Sheet}. Can be null.
	 * 
	 * @return Results as a {@link Sheet}.
	 */
	Sheet resultSheet();

	/**
	 * Close the upload module and cleanup.
	 * 
	 * @throws DataException
	 */
	void close() throws DataException;
	
	void clearResults();

	List<String> getHeaderList();
	
	boolean hasHeaderRow();
	
	int getRowBehavior();
	
}