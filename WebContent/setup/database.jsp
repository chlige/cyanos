<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.web.AppConfig,
	edu.uic.orjala.cyanos.web.AppConfigSQL,
	edu.uic.orjala.cyanos.web.servlet.MainServlet, 
	edu.uic.orjala.cyanos.ConfigException,
	edu.uic.orjala.cyanos.web.listener.AppConfigListener,
	java.util.Map,
	javax.sql.DataSource,
	java.sql.Connection,
	java.sql.SQLException,
	java.util.Set" %>
<h2 align="center">Database Validation</h2>
<% AppConfig appConfig = (AppConfig) session.getAttribute(MainServlet.APP_CONFIG_ATTR); 
	boolean dbValid = false;
	if ( appConfig != null && appConfig instanceof AppConfig ) {
		DataSource source = appConfig.getDataSourceObject();	
%>
<p class="mainContent"><b>Connecting to database</b>...
<% 	try { 
		Connection conn = source.getConnection(); 
		if ( conn != null &&  (! conn.isClosed() ) ) { %>
<font color='green'><b>SUCCESS</b></font>.</p>
<h3>Checking schema</h3>
<p class="mainContent"><b>Schema version...</b>
<% 	dbValid = true;
	int schemaVer = AppConfigSQL.getSchemaVersion(conn); 
	out.print(schemaVer); 
	dbValid = ( schemaVer == AppConfig.DATABASE_VERSION );
	if ( dbValid ) { %> (<font color='green'><b>Current</b></font>)</p>	
<% } else if ( schemaVer < AppConfig.DATABASE_VERSION ) { %> (<font color='red'><b>Need to update to v<%= AppConfig.DATABASE_VERSION %></b></font>)
<% } else { %> (<b><font color='red'>Version mismatch.</font>  Should be v<%= AppConfig.DATABASE_VERSION %></b>)
<br>Ensure that the proper version of the application WAR and schema are installed.
<% 	}
	Set<String> foundTables = AppConfigSQL.getTables(conn);
	String[] tables = appConfig.tableList(); 
%>
<table>
<tr><td><b>Table</b></td><td><b>Status</b></td><td><b>Table</b></td><td><b>Status</b></td><td><b>Table</b></td><td><b>Status</b></td></tr>
<%	int rows = (int) Math.round( Math.ceil(tables.length / 3.0) ); 
	for ( int i = 0; i < rows; i++ ) { %>
<tr>
<% for ( int m = 0; m < 3; m++ ) { int index = i + (rows * m); if ( index < tables.length ) { %>
<td><%= tables[index] %></td>
<td><% if ( foundTables.contains(tables[index])) { %><font color='green'><b>OK</b></font><% } else { dbValid = false; %><font color='red'><b>Not Found</b></font><% } %></td>
<% } } %>
</tr>
<% } %></table>
<%	Map<String,String> setupValues = (Map<String,String>) session.getAttribute(MainServlet.ATTR_SETUP_VALUES);
	if ( dbValid ) {
%><p class="mainContent"><b>Database schema valid!</b></p> 
<% if ( setupValues != null ) {
			setupValues.put(MainServlet.SETUP_DB_VALID, "true");
		}
	} else { %>
<p class="mainContent"><b><font color='red'>WARNING</font>:</b> Database schema is invalid.</p>
<%		if ( setupValues != null ) {
			setupValues.remove(MainServlet.SETUP_DB_VALID);
		}		
	} }
	} catch (SQLException e) { %>
<font color='red'><b>ERROR</b></font> : <%= e.getLocalizedMessage() %></p>		
<%	} %>
<% } %>
<table class="buttons"><tr>
<td><button type="submit" name="prevPage">&lt; Previous</button></td>
<td><button name="nextPage" type="submit" <%= ( dbValid ? "" : "disabled") %>>Next &gt;</button></td></tr>
</table>
