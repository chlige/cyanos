<%@ page import="edu.uic.orjala.cyanos.Harvest,
	edu.uic.orjala.cyanos.CyanosObject,edu.uic.orjala.cyanos.web.servlet.HarvestServlet,
	edu.uic.orjala.cyanos.Role,
	java.util.Date,
	java.text.SimpleDateFormat" %>
<%	String contextPath = request.getContextPath();
	Harvest queryResults = (Harvest) request.getAttribute(HarvestServlet.SEARCHRESULTS_ATTR); 
	if ( queryResults != null && request.getRemoteUser() != null ) { 
		if ( queryResults.first() ) { 
	queryResults.beforeFirst(); SimpleDateFormat dateFormat = (SimpleDateFormat) session.getAttribute("dateFormatter");  %>
<table class="dashboard">
<tr><th class="header" width='100'>Harvest</th><th class="header" width='200'>Harvest Date</th><th class="header" width='100'>Prep. Date</th><th class="header" width='100'>Cell Mass</th></tr>
<% while ( queryResults.next() ) { 
	if ( ! queryResults.isAllowed(Role.READ) ) continue;%>
<tr class="banded" align='center'><td><a href="<%= contextPath %>/harvest?id=<%= queryResults.getID() %>">Harvest #<%= queryResults.getID() %></a></td>
<td><%= dateFormat.format(queryResults.getDate()) %></td>
<td>
<% Date prepDate = queryResults.getPrepDate(); if ( prepDate != null ) { %>
<%= dateFormat.format(queryResults.getPrepDate()) %>
<% } %>
</td>
<td><%= CyanosObject.autoFormatAmount(queryResults.getCellMass(), Harvest.MASS_TYPE) %></td>
</tr>
<% } %>
</table>
<% } else { %>
<hr width="85%"/>
<p align='center'><b>No Results</b></p>
<% } } %>