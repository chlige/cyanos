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
<h2>Extracts made since</h2>
<hr>
<div style="margin-left: auto; margin-right:auto; text-align:center; padding-bottom:10px;">
<form>Since: <cyanos:calendar-field fieldName="since" dateValue='<%= request.getParameter("since") != null ? request.getParameter("since") : "" %>'/><button type="submit">Run Report</button></form></div>
<% if ( request.isUserInRole(User.SAMPLE_ROLE) ) {
	if ( request.getParameter("since") != null ) {
	String sql = "SELECT culture_id, material.material_id, date, extract_info.solvent, extract_info.type, material.amount_value * POW(10,material.amount_scale), material.amount_scale, IF(COUNT(f.separation_id) > 1, 'Yes', 'No') " + 
	"FROM material JOIN extract_info ON(material.material_id = extract_info.material_id) LEFT OUTER JOIN separation_source f ON material.material_id = f.material_id WHERE date >= ? GROUP BY material.material_id ORDER BY date ASC";
	Connection conn = AppConfigListener.getDBConnection();
	PreparedStatement sth = conn.prepareStatement(sql);
	sth.setString(1, request.getParameter("since"));		
	ResultSet results = sth.executeQuery();
	String path = request.getContextPath();
	DateFormat dateFormat = MainServlet.DATE_FORMAT;	
%><table class="results">
<tr><th>Strain</th><th>Extract Date</th><th>Solvent</th><th>Amount</th><th>Fractionated</th></tr>
<% while ( results.next() ) {
%><tr><td><a href="<%= path %>/strain?id=<%= results.getString(1) %>"><%= results.getString(1) %></a></td>
<td><a href="<%= path %>/material?id=<%= results.getString(2) %>"><%= dateFormat.format(results.getDate(3)) %></a></td>
<td><%= results.getString(4) %> (<%= results.getString(5) %>)</td>
<td><% 
	BigDecimal volume = results.getBigDecimal(6);
	int scale = results.getInt(7);
	volume = volume.round(new MathContext(scale * (scale < 0 ? -1 : 1)));
%><%= CyanosObject.autoFormatAmount(volume, BasicObject.MASS_TYPE) %></td>
<td><%= results.getString(8) %></td>
</tr>
<% } %>
</table>
<%	results.close();
	sth.close();
	} else {
%><p align="center">Select date range</p><%
	}		
} else {
%><p align="center">Access denied</p><%
}
%>
</body>
</html>