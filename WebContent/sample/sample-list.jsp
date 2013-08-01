<%@ page import="edu.uic.orjala.cyanos.Sample,
	edu.uic.orjala.cyanos.SampleCollection,
	edu.uic.orjala.cyanos.CyanosObject,
	edu.uic.orjala.cyanos.web.BaseForm,
	edu.uic.orjala.cyanos.web.servlet.SampleServlet,
	java.text.SimpleDateFormat" %>
<%	String contextPath = request.getContextPath();
	Sample queryResults = (Sample)request.getAttribute(SampleServlet.SEARCHRESULTS_ATTR); 
	if ( queryResults != null && queryResults.first() ) { 
		queryResults.beforeFirst(); 
		SimpleDateFormat dateFormat = (SimpleDateFormat) session.getAttribute("dateFormatter");  %>
<table  class="dashboard">
<tr><th class="header">Sample</th><th class="header" width='150'>Date</th><th class="header" width='150'>Collection</th>
<th class='header' width=50>Location</th><th class='header' width='100'>Balance</th><th class='header' width='200'>Notes</th></tr>
<% while ( queryResults.next() ) { 
	String cellClass = ( queryResults.isRemoved() ? "removed" : "none" ); %>
<tr class="banded" align='center'>
<td class='<%= cellClass %>'><a href="<%= contextPath %>/sample?id=<%= queryResults.getID() %>"><%= queryResults.getName() %></a></td>
<td class='<%= cellClass %>'><%= dateFormat.format(queryResults.getDate()) %></td>
<% SampleCollection aCol = queryResults.getCollection(); if ( aCol != null ) { %>
<td class='<%= cellClass %>'><a href="<%= contextPath %>/sample?col=<%= aCol.getID() %>"><%= aCol.getName() %></a></td>
<% } else { %>
<td class='<%= cellClass %>'><a href="<%= contextPath %>/sample?col=<%= queryResults.getCollectionID() %>"><%= queryResults.getCollectionID() %></a></td>
<% } %>
<td class='<%= cellClass %>'><%= queryResults.getLocation() %></td>
<td class='<%= cellClass %>'><%= CyanosObject.formatAmount(queryResults.accountBalance(), queryResults.getBaseUnit()) %></td>
<td class='<%= cellClass %>'><%= BaseForm.shortenString(queryResults.getNotes(), 30) %></td>
</tr>
<% } %>
</table>
<% } else { %>
<hr width="85%"/>
<p align='center'><b>No Results</b></p>
<% } %>