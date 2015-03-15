/**
 * 
 */
package edu.uic.orjala.cyanos.web.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.web.Job;
import edu.uic.orjala.cyanos.web.job.DereplicationQuery;
import edu.uic.orjala.cyanos.xml.XMLCompound;

/**
 * @author George Chlipala
 *
 */
public class DereplicationServlet extends ServletObject {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 25005917302237743L;
	private static final String HELP_MODULE = "dereplication";
//	private static final String QUERY_RESULTS = "dereplication_results";
	
	public static final String QUERY_ATTRIBUTE = "query";
	public static final String SEARCH_ACTION = "searchAction";

	private static final Pattern TABLE_PATTERN = Pattern.compile("(\\S+) AS (\\S+)", Pattern.CASE_INSENSITIVE);
	
	
	public static void parseGraph(HttpServletRequest request, String field, String table, String onclause, String where, String having) throws DataException, SQLException {
		DereplicationQuery job = getQueryJob(request);
		
		String value = request.getParameter(field);
		if ( value != null && value.length() > 0 ) {
			job.addTable(table, onclause);
			Matcher tableMatch = TABLE_PATTERN.matcher(table);
			String alias;
			if ( tableMatch.matches() ) {
				alias = tableMatch.group(2).concat("_having");
			} else {
				alias = table.concat("_having");
			}
			
			job.addColumn(having.concat(" AS ").concat(alias));
			if ( where != null )
				job.addQuery(where);
			if ( value.endsWith("+") ) {
				job.addHaving(alias.concat(" >= ").concat(value.substring(0, value.length() - 1)));
			} else if ( value.matches("([0-9]+)\\-([0-9]+)") ) {
				String[] vals = value.split("\\-");
				job.addHaving(alias.concat(" >= ").concat(vals[0]));
				job.addHaving(alias.concat(" <= ").concat(vals[1]));
			} else if ( value.startsWith("-") ) {
				job.addHaving(alias.concat(" <= ").concat(value.substring(1)));				
			} else if ( value.length() > 0 ) {
				job.addHaving(alias.concat(" = ").concat(value));
			}
		}
	}
	
	public String getHelpModule() {
		return HELP_MODULE;
	}
	
	public static void addQuery(HttpServletRequest request, String query) throws DataException, SQLException {
		DereplicationQuery job = getQueryJob(request);
		job.addQuery(query);
	}
	
	public static void addHaving(HttpServletRequest request, String having) throws DataException, SQLException {
		DereplicationQuery job = getQueryJob(request);
		job.addHaving(having);
	}
	
	public static void addTable(HttpServletRequest request, String table, String onclause) throws DataException, SQLException {
		DereplicationQuery job = getQueryJob(request);
		job.addTable(table, onclause);
	}
	
	public static DereplicationQuery startDereplicationQuery(HttpServletRequest request) throws DataException, SQLException {
		DereplicationQuery job = getQueryJob(request);
		job.startJob();
		getJobManager(request.getSession()).addJob(job);
		return job;
	}
	
	public static DereplicationQuery getQueryJob(HttpServletRequest request) throws DataException, SQLException {
		Object job = request.getAttribute("derep-job");
		if ( job == null ) {
			job = new DereplicationQuery(newSQLData(request));
			request.setAttribute("derep-job", job);
		}
		if ( job instanceof DereplicationQuery ) 
			return (DereplicationQuery)job;
		
		return null;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		super.doGet(req, res);
		
		if ( req.getParameter("jobid") != null ) {
			try {
				Job queryJob = Job.loadJob(getSQLData(req), req.getParameter("jobid"));
				if ( queryJob.getType().equals(DereplicationQuery.JOB_TYPE) ) {
					if ( queryJob.getOutputType().equals(DereplicationQuery.OUTPUT_TYPE) ) {
						req.setAttribute(CompoundServlet.COMPOUND_RESULTS, XMLCompound.load(new StringReader(queryJob.getOutput())));
						forwardRequest(req, res, "/compound/compound-list.jsp");
					} else {
						PrintWriter out = res.getWriter();
						out.print("<p style=\"font-weight:bold; text-align:center;\">");
						out.print(queryJob.getOutput());
						out.print("</p>");
					}
				}
			} catch (DataException e) {
				throw new ServletException(e);
			} catch (SQLException e) {
				throw new ServletException(e);
			} catch (ParserConfigurationException e) {
				throw new ServletException(e);
			} catch (SAXException e) {
				throw new ServletException(e);
			}

		}
	}
}
