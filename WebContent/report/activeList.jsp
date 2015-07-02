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
<h2>Assay data for targets</h2>
<hr>
<div style="margin-left: auto; margin-right:auto; text-align:center; padding-bottom:10px;">
<p>This query will return a summary of assay data for each assay target. The following columns are returned.</p>
<table class="details">
<tr><th>Assay Target</th><td>Target, e.g. 20S Proteasome or HDAC</td></tr>
<tr><th>Extract count</th><td>Count of extracts assayed</td></tr>
<tr><th>Fraction count</th><td>Count of fractions assayed</td></tr>
<tr><th>Extracts active</th><td>Count of active extracts</td></tr>
<tr><th>Fractions active</th><td>Count of active fractions</td></tr>
</table>
<form>
Assay date since: <cyanos:calendar-field fieldName="since" dateValue='<%= request.getParameter("since") != null ? request.getParameter("since") : "" %>'/>
<button type="submit">Run Report</button></form></div>
<% if ( request.isUserInRole(User.BIOASSAY_ROLE) ) {
	if ( request.getParameter("since") != null ) {		
	String sql = "SELECT ai.target, SUM(IF(e.harvest_id IS NOT NULL,1,0)) AS excnt, SUM(IF(sep.separation_id IS NOT NULL,1,0)) AS fraccnt, SUM(IF(e.harvest_id IS NOT NULL,1,0) * isActive(a.activity + a.sign, ai.active_level, ai.active_op)) AS active_ext, SUM(IF(sep.separation_id IS NOT NULL,1,0) * isActive(a.activity + a.sign, ai.active_level, ai.active_op)) AS active_frac" + 
	" FROM assay a JOIN assay_info ai USING(assay_id) LEFT OUTER JOIN extract_info e USING(material_id) LEFT OUTER JOIN separation_product sep USING(material_id) WHERE ai.date >= ? GROUP BY ai.target ORDER BY ai.target";
	Connection conn = AppConfigListener.getDBConnection();
	PreparedStatement sth = conn.prepareStatement(sql);
	sth.setString(1, request.getParameter("since"));
	ResultSet results = sth.executeQuery();
	String path = request.getContextPath();
	DateFormat dateFormat = MainServlet.DATE_FORMAT;	
%>
<table class="results">
<tr><th>Assay Target</th><th>Extract Count</th><th>Fraction Count</th><th>Extracts active</th><th>Fractions active</th></tr>
<% int extN=0; int fracN=0; int extAct=0; int fracAct=0;
while ( results.next() ) {
	extN += results.getInt(2);
	fracN += results.getInt(3);
	extAct += results.getInt(4);
	fracAct += results.getInt(5);
%><tr><td><a href="<%= path %>/assay?assaySearch&target=<%= results.getString(1) %>"><%= results.getString(1) %></a></td>
<td><%= results.getInt(2) %></td><td><%= results.getInt(3) %></td>
<td><%= results.getInt(4) %></td><td><%= results.getInt(5) %></td>
</tr>
<% } %>
<tr><th>TOTAL</th><td><%= extN %></td><td><%= fracN %></td><td><%= extAct %></td><td><%= fracAct %></td></tr>
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