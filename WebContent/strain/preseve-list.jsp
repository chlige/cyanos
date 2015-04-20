<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page import="edu.uic.orjala.cyanos.Cryo, 
	edu.uic.orjala.cyanos.Role, edu.uic.orjala.cyanos.web.servlet.CryoServlet,
	java.text.SimpleDateFormat" %>
<%	Object attr = request.getAttribute("cryoList");
if ( attr != null && attr instanceof Cryo ) {
	Cryo queryResults = (Cryo) attr;
	queryResults.beforeFirst();
	SimpleDateFormat dateFormat = CryoServlet.DATE_FORMAT;
%>
<p align="center"><input type="checkbox" onClick="toggleRows('dead_cryo',this.checked)" checked /> Show removed preservations</p>
<table  class="dashboard">
<tr><th class="header">Strain (Preservation #)</th><th class="header" width='200'>Date</th><th class="header" width='100'>Location</th><th class="header" width='100'>Remove Date</th><th class="header" width="300">Notes</th></tr>
<% while ( queryResults.next() ) { 
	if ( ! queryResults.isAllowed(Role.READ) ) continue;  
%><tr align='center' class="<%= queryResults.wasRemoved() ? "dead dead_cryo" : "banded" %>"><td><a href="preserve.jsp?id=<%= queryResults.getID() %>"><%= queryResults.getCultureID() %> (<%= queryResults.getID() %>)</a></td>
<td><%= dateFormat.format(queryResults.getDate()) %></td>
<td><a href="preserve.jsp?collection=<%= queryResults.getCollectionID() %>"><%= queryResults.getCollectionID() %></a> (<%= queryResults.getLocation() %>)</td>
<td><%= ( queryResults.wasRemoved() ? dateFormat.format(queryResults.getRemovedDate()) : "") %><%= ( queryResults.isThawed() ? "REMOVED" : "" ) %></td>
<td><%= CryoServlet.shortenString(queryResults.getNotes(), 30) %></td></tr><% } %>
</table><% 
} else { 
%><p align='center'><b>No Results</b></p><% 
} %>
