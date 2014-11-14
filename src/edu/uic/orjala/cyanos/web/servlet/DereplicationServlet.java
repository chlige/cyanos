/**
 * 
 */
package edu.uic.orjala.cyanos.web.servlet;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import edu.uic.orjala.cyanos.Compound;
import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.sql.SQLCompound;

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


	public static StringBuffer getJoinBuffer(HttpServletRequest request) {
		Object buffer = request.getAttribute("derep-join");
		if (buffer == null || ! (buffer instanceof StringBuffer) ) {
			buffer = new StringBuffer();
			request.setAttribute("derep-join", buffer);
		}
		return (StringBuffer)buffer;
	}
	
	public static void addColumn(HttpServletRequest request, String table, String column) {
		getExtraColumns(request).add(table.concat(".").concat(column));
	}
	
	public static List<String> getExtraColumns(HttpServletRequest request) {
		Object buffer = request.getAttribute("derep-cols");
		if ( buffer == null ) {
			buffer = new ArrayList<String>();
			request.setAttribute("derep-cols", buffer);
		}
		return ((List<String>)buffer);
	}
	
	public static String buildQuery(HttpServletRequest request) {
		Object sqlString = request.getAttribute("derep-sql");
		
		if ( sqlString instanceof String ) {
			return (String) sqlString;
		}
		
		StringBuffer sql = new StringBuffer(getJoinBuffer(request));
		StringBuffer buffer = getQuery(request);
		if ( buffer.length() > 0 ) {
			sql.append(" WHERE ");
			sql.append(buffer);
		}
		sql.append(" GROUP BY ");
		sql.append(SQLCompound.TABLE);
		sql.append(".");
		sql.append(SQLCompound.ID_COLUMN);
		buffer = getHaving(request);
		if ( buffer.length() > 0 ) {
			sql.append(" HAVING ");
			sql.append(buffer);
		}
		sql.append(" ORDER BY ");		
		sql.append(SQLCompound.TABLE);
		sql.append(".");
		sql.append(SQLCompound.ID_COLUMN);
		sql.append(" ASC");
		
		sqlString = sql.toString();
		request.setAttribute("derep-sql", sqlString);
		
		return (String)sqlString;
	}
	
	public static Compound getCompounds(HttpServletRequest request) throws DataException, SQLException {
		String sqlString = buildQuery(request);
		List<String> columns = getExtraColumns(request);
		if ( columns.size() > 0 ) {
			String[] cols = {};
			return SQLCompound.compoundQuery(getSQLData(request), columns.toArray(cols), sqlString);
		} else 
			return SQLCompound.compoundQuery(getSQLData(request), sqlString);
	}
	
	public static void parseGraph(HttpServletRequest request, String field, String table, String onclause, String where, String having) {
		String value = request.getParameter(field);
		if ( value != null && value.length() > 0 ) {
			addTable(request, table, onclause);
			String alias = table.concat("_having");
			addColumn(request, table, having.concat(" AS ").concat(alias));
			if ( where != null )
				addQuery(request, where);
			if ( value.endsWith("+") ) {
				addHaving(request, alias.concat(" >= ").concat(value.substring(0, value.length() - 1)));
			} else if ( value.matches("([0-9]+)\\-([0-9]+)") ) {
				String[] vals = value.split("\\-");
				addHaving(request, alias.concat(" >= ").concat(vals[0]));
				addHaving(request, alias.concat(" <= ").concat(vals[1]));
			} else if ( value.startsWith("-") ) {
				addHaving(request, alias.concat(" <= ").concat(value.substring(1)));				
			} else if ( value.length() > 0 ) {
				addHaving(request, alias.concat(" = ").concat(value));
			}
		}
	}
	
	public String getHelpModule() {
		return HELP_MODULE;
	}
	
	public static void addQuery(HttpServletRequest request, String query) {
		StringBuffer queryBuffer = getQuery(request);
		if ( queryBuffer.length() > 0 )
			queryBuffer.append(" AND ");
		queryBuffer.append(query);
	}
	
	public static void addHaving(HttpServletRequest request, String having) {
		StringBuffer buffer = getHaving(request);
		if ( buffer.length() > 0 )
			buffer.append(" AND ");
		buffer.append(having);
	}
	
	public static void addTable(HttpServletRequest request, String table, String onclause) {
		StringBuffer buffer = getJoinBuffer(request);
		buffer.append(" JOIN ");
		buffer.append(table);
		buffer.append(" ON (");
		buffer.append(onclause);
		buffer.append(")");
	}
	
	public static StringBuffer getQuery(HttpServletRequest request) {
		Object query = request.getAttribute(DereplicationServlet.QUERY_ATTRIBUTE);
		if ( query == null ) {
			query = new StringBuffer();
			request.setAttribute(QUERY_ATTRIBUTE, query);
		}
		if ( query instanceof StringBuffer ) {
			return (StringBuffer)query;
		}
		return null;
	}
	
	public static StringBuffer getHaving(HttpServletRequest request) {
		Object buffer = request.getAttribute("derep-having");
		if ( buffer == null ) {
			buffer = new StringBuffer();
			request.setAttribute("derep-having", buffer);
		}
		if ( buffer instanceof StringBuffer ) {
			return (StringBuffer)buffer;
		}
		return null;
	}

}
