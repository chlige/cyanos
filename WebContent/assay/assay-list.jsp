<%@ page import="edu.uic.orjala.cyanos.CyanosObject,edu.uic.orjala.cyanos.web.servlet.AssayServlet,
	edu.uic.orjala.cyanos.Assay,
	edu.uic.orjala.cyanos.web.BaseForm,
	java.util.Date,
	java.text.SimpleDateFormat" %>
<%	String contextPath = request.getContextPath();
	Assay queryResults = (Assay) request.getAttribute(AssayServlet.SEARCHRESULTS_ATTR); 
if ( queryResults != null ) { if ( queryResults.first() ) { 
queryResults.beforeFirst(); 
SimpleDateFormat dateFormat = (SimpleDateFormat) session.getAttribute("dateFormatter"); %>
<table  class="dashboard">
<tr><th class="header" width='125'>Assay</th><th class="header" width='125'>Date</th><th class="header" width="75">Size</th><th class="header" width='400'>Notes</th></tr>
<% while ( queryResults.next() ) { %>
<tr class='normal' align='center'>
<td><a href="<%= contextPath %>/assay?id=<%= queryResults.getID() %>"><%= queryResults.getID() %></a></td>
<td><%= dateFormat.format(queryResults.getDate()) %></td>
<td><%= String.format("%d wells", queryResults.getLength() * queryResults.getWidth()) %></td>
<td><%= BaseForm.shortenString(queryResults.getNotes(), 60) %></td>
</tr><% } %>
</table>
<% } else { %>
<hr width="85%"/>
<p align='center'><b>No Results</b></p>
<% } } %>