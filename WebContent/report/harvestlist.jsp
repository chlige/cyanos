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
</style>
</head>
<body>
<cyanos:menu helpModule="<%= MainServlet.HELP_MODULE %>"/>
<h1>Database Report</h1>
<h2>Harvest awaiting extraction</h2>
<hr>
<% if ( request.isUserInRole(User.CULTURE_ROLE) ) {
	String sql = "SELECT culture_id, harvest_id, date, cell_mass_value * POW(10,cell_mass_scale), cell_mass_scale FROM harvest WHERE harvest_id NOT IN (SELECT DISTINCT harvest_id FROM extract_info) ORDER BY date ASC";
	Connection conn = AppConfigListener.getDBConnection();
	Statement sth = conn.createStatement();
	ResultSet results = sth.executeQuery(sql);
	String path = request.getContextPath();
	DateFormat dateFormat = MainServlet.DATE_FORMAT;
%>
<table style="margin-left: auto; margin-right:auto;">
<tr><th>Strain</th><th>Harvest Date</th><th>Cell Mass</th></tr>
<% while ( results.next() ) {
%><tr><td><a href="<%= path %>/strain?id=<%= results.getString(1) %>"><%= results.getString(1) %></a></td>
<td><a href="<%= path %>/harvest?id=<%= results.getString(2) %>"><%= dateFormat.format(results.getDate(3)) %></a></td>
<td><% if ( results.getInt(4) > 1 ) { %><%= results.getInt(4) %> &times; <% } 
	BigDecimal mass = results.getBigDecimal(4);
	int scale = results.getInt(5);
	mass = mass.round(new MathContext(scale * (scale < 0 ? -1 : 1)));
%><%= CyanosObject.autoFormatAmount(mass, BasicObject.MASS_TYPE) %></td></tr>
<% } %>
</table>
<%	results.close();
	sth.close();
} else {
%><p align="center">Access denied</p><%
}
%>
</body>
</html>