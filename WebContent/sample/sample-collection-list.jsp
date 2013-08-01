<%@ page import="edu.uic.orjala.cyanos.Sample,
	edu.uic.orjala.cyanos.SampleCollection,
	edu.uic.orjala.cyanos.CyanosObject,
	edu.uic.orjala.cyanos.web.BaseForm,
	edu.uic.orjala.cyanos.web.servlet.SampleServlet,
	java.text.SimpleDateFormat" %>
<%	String contextPath = request.getContextPath();
	SampleCollection queryResults = (SampleCollection) request.getAttribute(SampleServlet.SEARCHRESULTS_ATTR); 
	if ( queryResults != null && queryResults.first() ) { 
		queryResults.beforeFirst(); 
		SimpleDateFormat dateFormat = (SimpleDateFormat) session.getAttribute("dateFormatter");  
%><table class="dashboard">
<tr><th class="header">Collection</th><th class="header" width='150'>Library</th><th class="header" width='150'>Size</th>
<th class='header' width='100'>Sample count</th><th class='header' width='200'>Notes</th></tr>
<% while ( queryResults.next() ) { 
%><tr class="banded" align='center'>
<td><a href="<%= contextPath %>/sample?col=<%= queryResults.getID() %>"><%= queryResults.getName() %></a></td>
<td><%= queryResults.getLibrary() %></td>
<td><%= ( queryResults.isBox() ? String.format("%d &times; %d", queryResults.getLength(), queryResults.getWidth()) : "list" ) %></td>
<td><%= queryResults.getSamples().count() %></td>
<td><%= BaseForm.shortenString(queryResults.getNotes(), 30) %></td>
</tr>
<% } %>
</table>
<% } else { %>
<hr width="85%"/>
<p align='center'><b>No Results</b></p>
<% } %>