<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.BasicObject,
	edu.uic.orjala.cyanos.Collection,
	edu.uic.orjala.cyanos.Role,edu.uic.orjala.cyanos.web.servlet.CollectionServlet,
	edu.uic.orjala.cyanos.web.BaseForm,
	java.math.BigDecimal, java.text.SimpleDateFormat" %>
<% 	String contextPath = request.getContextPath();
	String div = request.getParameter("div");
	SimpleDateFormat dateFormat = (SimpleDateFormat) session.getAttribute("dateFormatter");
	Collection collections = (Collection) request.getAttribute(CollectionServlet.SEARCHRESULTS_ATTR);

	if ( collections != null && collections.first() ) {
			collections.beforeFirst();
			boolean oddRow = true;
%> 
<table style="width: 75%; margin-left:auto; margin-right:auto;" class="dashboard banded"><tbody>
<tr><th class="header">ID</th>
<th class="header">Date</th>
<th class="header">Location</th>
<th class="header">Coordinates</th>
<th class="header">Collector</th>
<th class="header">Notes</th>
</tr>	
<% while ( collections.next() )  { 	
%>
<tr class='banded' align='center'>
<td><a href="<%= contextPath %>/collection?col=<%= collections.getID() %>"><%= collections.getID() %></a></td>
<td><%= dateFormat.format(collections.getDate()) %></td>
<td><%= collections.getLocationName() %></td>
<td><%= collections.getLatitudeDM() %><br><%= collections.getLongitudeDM() %></td>
<td><%= collections.getCollector() %></td>
<td><%= BaseForm.shortenString(collections.getNotes(), 20) %></td>
</tr>
<% } %>
</table>
<% } else { %>
<p align='center'><b>No Results</b></p>
<% } %>

