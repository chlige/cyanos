<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page import="edu.uic.orjala.cyanos.Cryo, 
	edu.uic.orjala.cyanos.Role, edu.uic.orjala.cyanos.web.servlet.CryoServlet,
	java.text.SimpleDateFormat" %>
<%	Object attr = request.getAttribute("cryoList");
if ( attr != null && attr instanceof Cryo ) {
	Cryo queryResults = (Cryo) attr;
	SimpleDateFormat dateFormat = CryoServlet.DATE_FORMAT;
%><table  class="dashboard">
<tr><th class="header">Strain (Preservation #)</th><th class="header" width='200'>Date</th><th class="header" width='100'>Location</th><th class="header" width='100'>Remove Date</th><th class="header" width="100">Notes</th></tr>
<% while ( queryResults.next() ) { 
	if ( ! queryResults.isAllowed(Role.READ) ) continue;  
%><tr align='center' class="banded"><td><a href="?id=<%= queryResults.getID() %>"><%= queryResults.getCultureID() %> (<%= queryResults.getID() %>)</a></td>
<td><%= dateFormat.format(queryResults.getDate()) %></td>
<td><a href="?collection=<%= queryResults.getCollectionID() %>"><%= queryResults.getCollectionID() %></a> (<%= queryResults.getLocation() %>)</td>
<td><%= ( queryResults.isThawed() ? "REMOVED" : "" ) %></td>
<td><%= CryoServlet.shortenString(queryResults.getNotes(), 100) %></td></tr><% } %>
</table><% 
} else { 
%><p align='center'><b>No Results</b></p><% 
} %>