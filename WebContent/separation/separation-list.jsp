<%@ page import="edu.uic.orjala.cyanos.Separation,
	edu.uic.orjala.cyanos.CyanosObject,edu.uic.orjala.cyanos.web.servlet.SeparationServlet,
	edu.uic.orjala.cyanos.web.BaseForm,
	java.text.SimpleDateFormat" %>
<%	String contextPath = request.getContextPath();
	Separation queryResults = (Separation)request.getAttribute(SeparationServlet.SEARCHRESULTS_ATTR); 
	if ( queryResults != null ) { 
		if ( queryResults.first() ) { 
		boolean oddRow = true; queryResults.beforeFirst(); 
		SimpleDateFormat dateFormat = (SimpleDateFormat) session.getAttribute("dateFormatter");  %>
<table  class="dashboard">
<tr><th class="header">Separation</th><th class="header" width='150'>Date</th><th class="header" width='200'>Stationary Phase</th>
<th class='header' width=150>Mobile Phase</th><th class='header' width='200'>Notes</th></tr>
<% while ( queryResults.next() ) { 
	String rowFormat = ( oddRow ? "odd" : "even" ); oddRow = ! oddRow; 
	String cellClass = ( queryResults.isRemoved() ? "removed" : "none" ); %>
<tr class='<%= rowFormat %>' align='center'>
<% String name = queryResults.getTag();  if ( name == null || name.length() < 1 ) { name = queryResults.getID(); } %>
<td class='<%= cellClass %>'><a href="<%= contextPath %>/separation?id=<%= queryResults.getID() %>"><%= name %></a></td>
<td class='<%= cellClass %>'><%= dateFormat.format(queryResults.getDate()) %></td>
<td class='<%= cellClass %>'><%= BaseForm.shortenString(queryResults.getStationaryPhase(), 20) %></td>
<td class='<%= cellClass %>'><%= BaseForm.shortenString(queryResults.getMobilePhase(), 20) %></td>
<td class="<%= cellClass %>"><%= BaseForm.shortenString(queryResults.getNotes(), 30) %></td>
</tr>
<% } %>
</table>
<% } else { %>
<p align='center'><b>No Results</b></p>
<% } } %>