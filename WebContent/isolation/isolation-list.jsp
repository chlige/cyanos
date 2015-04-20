<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.BasicObject,
	edu.uic.orjala.cyanos.Isolation,
	edu.uic.orjala.cyanos.Role,edu.uic.orjala.cyanos.web.servlet.CollectionServlet,
	edu.uic.orjala.cyanos.web.BaseForm,
	java.math.BigDecimal, java.text.SimpleDateFormat" %>
<% 	String contextPath = request.getContextPath();
	String div = request.getParameter("div");
	SimpleDateFormat dateFormat = (SimpleDateFormat) session.getAttribute("dateFormatter");
	Isolation isolations = (Isolation) request.getAttribute(CollectionServlet.SEARCHRESULTS_ATTR);

	if ( isolations != null && isolations.first() ) {
			isolations.beforeFirst();
			boolean oddRow = true;
%> 
<table width="75%" align="center" class="dashboard"><tbody>
<tr><th class="header">ID</th>
<th class="header">Date</th>
<th class="header">Type</th>
<th class="header">Media</th>
<th class="header">Notes</th>
</tr>	
<% while ( isolations.next() )  { %>
<tr class='banded' align='center'>
<td><a href="<%= contextPath %>/collection?id=<%= isolations.getID() %>"><%= isolations.getID() %></a></td>
<td><%= dateFormat.format(isolations.getDate()) %></td>
<td><%= isolations.getType() %></td>
<td><%= isolations.getMedia() %></td>
<td><%= BaseForm.shortenString(isolations.getNotes(), 50) %></td>
</tr>
<% } %>
</table>
<% } else { %>
<p align='center'><b>No Results</b></p>
<% } %>

