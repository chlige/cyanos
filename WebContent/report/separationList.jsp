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
table { margin-left: auto; margin-right: auto; }
</style>
</head>
<body>
<cyanos:menu helpModule="<%= MainServlet.HELP_MODULE %>"/>
<h1>Database Report</h1>
<h2>Separations since</h2>
<hr>
<p>This query will returns a list of harvests from strains with basic extract and fractionation information.  The columns returned are as follows:</p>
<table class="details">
<tr><th>Strain</th><td>ID and name of the strain</td></tr>
<tr><th>Date Added</th><td>Date strain was added to the collection</td></tr>
<tr><th>Culture Source</th><td>Source collection, e.g. UIC, UTEX, CCMP...</td></tr>
<tr><th>Harvest Date</th><td>Date harvested</td></tr>
<tr><th>Extract Date</th><td>Date extracted</td></tr>
<tr><th>Extract Amount</th><td>Amount of initial extract</td></tr>
<tr><th>Fractionated</th><td>Either "Yes" or "No" depending if a separation record is linked to the extract.</td></tr>
</table>

<div style="margin-left: auto; margin-right:auto; text-align:center; padding-bottom:10px;">
<form>Strain added since: <cyanos:calendar-field fieldName="since" dateValue='<%= request.getParameter("since") %>'/>
Minimum growth size: <input type="number" name="volume" value="1" min="1" max="20" step="1"> L
<button type="submit">Run Report</button></form></div>
<% if ( request.isUserInRole(User.SAMPLE_ROLE) ) {
	if ( request.getParameter("since") != null ) {
	String sql = "SELECT s.culture_id, s.name, s.date, SUBSTRING_INDEX(LTRIM(s.culture_source),' ',1), h.harvest_id, h.date, m.material_id, m.date, m.amount_value * POW(10,m.amount_scale), m.amount_scale, IF(f.separation_id > 1, 'Yes', 'No') " + 
	"FROM harvest h JOIN inoculation i USING(harvest_id) JOIN species s ON s.culture_id=i.culture_id JOIN extract_info e ON (e.harvest_id=h.harvest_id) JOIN material m ON (e.material_id = m.material_id) LEFT OUTER JOIN separation_source f ON e.material_id = f.material_id " +
	"WHERE s.date >= ? GROUP BY h.harvest_id  HAVING SUM(i.volume_value * POW(10,i.volume_scale)) > ? ORDER BY s.date";
	Connection conn = AppConfigListener.getDBConnection();
	PreparedStatement sth = conn.prepareStatement(sql);
	sth.setString(1, request.getParameter("since"));	
	sth.setString(2, request.getParameter("volume"));
	ResultSet results = sth.executeQuery();
	String path = request.getContextPath();
	DateFormat dateFormat = MainServlet.DATE_FORMAT;	
%><table style="margin-left: auto; margin-right:auto;">
<tr><th>Strain</th><th>Date Added</th><th>Culture Source</th><th>Date Harvested</th><th>Date Extracted</th><th>Extract Amount</th><th>Fractionated</th></tr>
<% while ( results.next() ) {
%><tr><td><a href="<%= path %>/strain?id=<%= results.getString(1) %>"><%= results.getString(1) %> <%= results.getString(2) %></a></td>
<td><%= results.getDate(3) %></td><td><%= results.getString(4) %></td>
<td><a href="<%= path %>/harvest?id=<%= results.getString(5) %>"><%= dateFormat.format(results.getDate(6)) %></a></td>
<td><a href="<%= path %>/material?id=<%= results.getString(7) %>"><%= dateFormat.format(results.getDate(8)) %></a></td>
<td><% 
	BigDecimal volume = results.getBigDecimal(9);
	int scale = results.getInt(10);
	volume = volume.round(new MathContext(scale * (scale < 0 ? -1 : 1)));
%><%= CyanosObject.autoFormatAmount(volume, BasicObject.MASS_TYPE) %></td>
<td><%= results.getString(11) %></td>
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