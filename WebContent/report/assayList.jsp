<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="cyanos" tagdir="/WEB-INF/tags" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.web.servlet.MainServlet,
	edu.uic.orjala.cyanos.web.listener.AppConfigListener,
	edu.uic.orjala.cyanos.CyanosObject,
	edu.uic.orjala.cyanos.BasicObject,
	edu.uic.orjala.cyanos.User,
	java.sql.PreparedStatement,
	java.math.BigDecimal,
	java.math.MathContext,
	java.sql.Connection,
	java.sql.ResultSet,
	java.sql.Statement,
	java.text.DateFormat" %>
<!DOCTYPE html>
<html>
<head>
<cyanos:header title="Cyanos Queries"/>
<style type="text/css">
h2 { text-align:center; }
table.details { border-collapse: collapse; margin-bottom: 10px; }
table.details td, table.details th { text-align:left; }
table.results tr { border-top: 1px solid gray; border-bottom: 1px solid gray; }
table.results td, table.results th { padding-left: 2px; padding-right: 2px; }
table { margin-left: auto; margin-right:auto; }
</style>
</head>
<body>
<cyanos:menu helpModule="<%= MainServlet.HELP_MODULE %>"/>
<h1>Database Report</h1>
<h2>Assay data for strains added since</h2>
<hr>
<div style="margin-left: auto; margin-right:auto; text-align:center; padding-bottom:10px;">
<p>This query will return a summary of assay data for each strain grouped by assay target. The following columns are returned.</p>
<table class="details">
<tr><th>Strain</th><td>ID and name of the strain</td></tr>
<tr><th>Date Added</th><td>Date strain was added to the collection</td></tr>
<tr><th>Culture Source</th><td>Source collection, e.g. UIC, UTEX, CCMP...</td></tr>
<tr><th>Assay Target</th><td>Target, e.g. 20S Proteasome or HDAC</td></tr>
<tr><th>First Assayed</th><td>Date of the first assay for this strain in this target</td></tr>
<tr><th>Active</th><td>A material for this strain displayed activity in this target</td></tr>
</table>
<form>
<select name="field">
<% String field = request.getParameter("field"); %>
<option value="strain"<%= "strain".equals(field) ? " selected" : "" %>>Strain added</option>
<option value="assay"<%= "assay".equals(field) ? " selected" : "" %>>Assay date</option>
</select> since: <cyanos:calendar-field fieldName="since" dateValue='<%= request.getParameter("since") != null ? request.getParameter("since") : "" %>'/>
<button type="submit">Run Report</button></form></div>
<% if ( request.isUserInRole(User.CULTURE_ROLE) ) {
	if ( request.getParameter("since") != null ) {		
	String sql = "SELECT DISTINCT s.culture_id, s.name, s.date, SUBSTRING_INDEX(LTRIM(s.culture_source),' ',1), ai.target, MIN(ai.date), MAX(isActive(a.activity + a.sign, ai.active_level, ai.active_op)) " + 
	" FROM species s LEFT OUTER JOIN assay a USING(culture_id) LEFT OUTER JOIN assay_info ai USING(assay_id) WHERE s.removed IS NULL AND " + ( "assay".equals(request.getParameter("field")) ? "ai" : "s") + ".date >= ? GROUP BY s.culture_id, ai.target ORDER BY s.date";
	Connection conn = AppConfigListener.getDBConnection();
	PreparedStatement sth = conn.prepareStatement(sql);
	sth.setString(1, request.getParameter("since"));
	ResultSet results = sth.executeQuery();
	String path = request.getContextPath();
	DateFormat dateFormat = MainServlet.DATE_FORMAT;	
%>
<table class="results">
<tr><th>Strain</th><th>Date Added</th><th>Culture Source</th><th>Assay Target</th><th>Most Recent Assay</th><th>Active</th></tr>
<% while ( results.next() ) {
%><tr><td><a href="<%= path %>/strain?id=<%= results.getString(1) %>"><%= results.getString(1) %> <%= results.getString(2) %></a></td>
<td><%= dateFormat.format(results.getDate(3)) %></td><td><%= results.getString(4) %></td>
<td><%= results.getString(5) %></td><td><%= dateFormat.format(results.getDate(6)) %></td>
<td><%= results.getInt(7) > 0 ? "Yes" : "No" %></td>
</tr>
<% } %>
</table>
<%	results.close();
	sth.close();
	conn.close();
	} else {
%><p align="center">Select date range</p><%
	}	
} else {
%><p align="center">Access denied</p><%
}
%>
</body>
</html>