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
<h2>Large scale inoculations since</h2>
<hr>
<div style="margin-left: auto; margin-right:auto; text-align:center; padding-bottom:10px;">
<% boolean excludeHarvested = request.getParameter("excludeHarvest") != null; %>
<form><table><tr><th>Since</th><th>Minimum growth size<br>(liters)</th><th>Exclude<br>harvested records</th></tr>
<tr><td><cyanos:calendar-field fieldName="since" dateValue='<%= request.getParameter("since") != null ? request.getParameter("since") : "" %>'/></td>
<td><input type="number" name="volume" value="1" min="1" max="20" step="1"></td>
<td><input type="checkbox" name="excludeHarvest" value="1" <%= excludeHarvested ? "checked" : "" %>></td>
<td><button type="submit">Run Report</button></td></tr></table></form></div>
<% if ( request.isUserInRole(User.CULTURE_ROLE) ) {
	if ( request.getParameter("since") != null ) {		
	String sql = "SELECT culture_id, date, media, COUNT(inoculation_id),volume_value * POW(10,volume_scale), MAX(volume_scale), IF(harvest_id > 0, 'Yes', 'No') FROM inoculation WHERE date >= ? AND volume_value * POW(10,volume_scale) >= ?" + 
			(excludeHarvested ? "AND harvest_id IS NULL " : "") + "GROUP BY culture_id,media,volume_value,volume_scale,date ORDER BY date ASC";
	Connection conn = AppConfigListener.getDBConnection();
	PreparedStatement sth = conn.prepareStatement(sql);
	sth.setString(1, request.getParameter("since"));
	sth.setString(2, request.getParameter("volume"));
	ResultSet results = sth.executeQuery();
	String path = request.getContextPath();
	DateFormat dateFormat = MainServlet.DATE_FORMAT;	
%>
<table class="results">
<tr><th>Strain</th><th>Inoc. Date</th><th>Volume</th><th>Media</th><% if ( ! excludeHarvested ) { %><th>Harvested</th><% } %></tr>
<% while ( results.next() ) {
%><tr><td><a href="<%= path %>/strain?id=<%= results.getString(1) %>"><%= results.getString(1) %></a></td>
<td><%= dateFormat.format(results.getDate(2)) %></td>
<td><% if ( results.getInt(4) > 1 ) { %><%= results.getInt(4) %> &times; <% } 
	BigDecimal volume = results.getBigDecimal(5);
	int scale = results.getInt(6);
	volume = volume.round(new MathContext(scale * (scale < 0 ? -1 : 1)));
%><%= CyanosObject.autoFormatAmount(volume, BasicObject.VOLUME_TYPE) %></td>
<td><%= results.getString(3) %></td>
<% if ( ! excludeHarvested ) { %><td><%= results.getString(7) %></td><% } %>
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