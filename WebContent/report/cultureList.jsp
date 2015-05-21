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
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<cyanos:header title="Cyanos Queries"/>
<style type="text/css">
h2 { text-align:center; }
table.details { border-collapse: collapse; }
table.details td, table.details th { border-top: 1px solid black; border-bottom: 1px solid black; }
table.results tr { border-top: 1px solid gray; border-bottom: 1px solid gray; }
table.results td, table.results th { padding-left: 2px; padding-right: 2px; }
table { margin-left: auto; margin-right:auto; }
</style>
</head>
<body>
<cyanos:menu helpModule="<%= MainServlet.HELP_MODULE %>"/>
<h1>Database Report</h1>
<h2>Status of strains since</h2>
<hr>
<div style="margin-left: auto; margin-right:auto; text-align:center; padding-bottom:10px">
<form>Since: <cyanos:calendar-field fieldName="since" dateValue='<%= request.getParameter("since") != null ? request.getParameter("since") : "" %>'/>
<button type="submit">Run Report</button></form></div>
<% if ( request.isUserInRole(User.CULTURE_ROLE) ) {
	boolean haveDate = request.getParameter("since") != null;	
	String sql = "SELECT s.culture_id, s.date, name, culture_status, s.removed, MIN(c.date), MAX(c.date) FROM species s LEFT OUTER JOIN cryo c ON(s.culture_id = c.culture_id) WHERE c.removed IS NULL " + 
			(haveDate ? "AND s.date >= ? " : "") + "GROUP BY s.culture_id ORDER BY s.date ASC";
	Connection conn = AppConfigListener.getDBConnection();
	PreparedStatement sth = conn.prepareStatement(sql);
	if ( haveDate )
	sth.setString(1, request.getParameter("since"));

	ResultSet results = sth.executeQuery();
	String path = request.getContextPath();
	DateFormat dateFormat = MainServlet.DATE_FORMAT;	
%>
<table class="results">
<tr><th>Strain</th><th>Date Added</th><th>Name</th><th>Status</th><th>Remove date</th><th>First cryo</th><th>Last cryo</th></tr>
<% while ( results.next() ) {
%><tr><td><a href="<%= path %>/strain?id=<%= results.getString(1) %>"><%= results.getString(1) %></a></td>
<td><%= dateFormat.format(results.getDate(2)) %></td>
<td><i><%= results.getString(3) %></i></td>
<td><%= results.getString(4) %></td>
<td><%= (results.getString(5) != null ? dateFormat.format(results.getDate(5)) : "-") %></td>
<td><%= (results.getString(6) != null ? dateFormat.format(results.getDate(6)) : "-") %></td>
<td><%= (results.getString(7) != null ? dateFormat.format(results.getDate(7)) : "-") %></td>
</tr>
<% } %>
</table>
<%	results.close();
	sth.close();
	conn.close();
} else {
%><p align="center">Access denied</p><%
}
%>
</body>
</html>