<%@ page import="edu.uic.orjala.cyanos.Material,edu.uic.orjala.cyanos.web.servlet.MaterialServlet,
	java.text.SimpleDateFormat" %>
<%	String contextPath = request.getContextPath();
	String div = request.getParameter("div");
	Material queryResults = (Material)request.getAttribute(MaterialServlet.SEARCHRESULTS_ATTR); 
	if ( queryResults != null ) { 
		if ( queryResults.first() ) { 
	queryResults.beforeFirst(); SimpleDateFormat dateFormat = (SimpleDateFormat) session.getAttribute("dateFormatter");  %>
<table  class="dashboard">
<tr><th class="header" width='100'>Material</th>
<th class="header" width='150'>Date</th>
<th class="header" width='100'>Extract Type</th>
<th class="header" width='200'>Extract Solvent</th>
<th class="header" width='100'>Amount</th></tr>
<% while ( queryResults.next() ) { 
	if ( ! queryResults.isExtract() ) continue;%>
<tr class='normal' align='center'><td><a href="<%= contextPath %>/material?id=<%= queryResults.getID() %>"><%= queryResults.getLabel() %></a></td>
<td><%= dateFormat.format(queryResults.getDate()) %></td>
<td><%= queryResults.getExtractType() %></td>
<td><%= queryResults.getExtractSolvent() %></td>
<td><%= queryResults.displayAmount() %></td>
</tr>
<% } %>
</table>
<% } else { %>
<hr width="85%"/>
<p align='center'><b>No Results</b></p>
<% } } %>