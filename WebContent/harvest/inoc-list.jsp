<%@ page import="edu.uic.orjala.cyanos.Inoc,
	edu.uic.orjala.cyanos.CyanosObject,
	edu.uic.orjala.cyanos.Project,
	edu.uic.orjala.cyanos.web.servlet.InocServlet,
	edu.uic.orjala.cyanos.web.BaseForm,
	java.text.SimpleDateFormat,
	java.util.List, java.util.Arrays" %>
<%	String contextPath = request.getContextPath();
	Inoc queryResults = (Inoc)request.getAttribute(InocServlet.SEARCHRESULTS_ATTR); 
	List<String> selected = Arrays.asList(request.getParameterValues("inoc"));	
	if ( queryResults != null ) { 
		if ( queryResults.first() ) { 
			queryResults.beforeFirst(); 
			SimpleDateFormat dateFormat = (SimpleDateFormat) session.getAttribute("dateFormatter");  
%><table class="dashboard">
<tr><td></td><th class="header" width='200'>Date</th><th class="header" width="100">Project</th><th class="header" width='100'>Media</th><th class="header" width='100'>Volume</th><th class="header" width="100">Notes</th></tr>
<% while ( queryResults.next() ) { 
%><tr class="banded" align='center'>
<td><input type="checkbox" name="inoc" value="<%= queryResults.getID() %>" <%= ( selected.contains(queryResults.getID()) ? "checked" : "") %>></td>
<td><%= dateFormat.format(queryResults.getDate()) %></td>
<td><% Project project = queryResults.getProject(); 
	if ( project != null && project.first() ) { out.print(project.getName()); } %></td>
<td><%= queryResults.getMedia() %></td>
<td><%= CyanosObject.autoFormatAmount(queryResults.getVolume(), Inoc.VOLUME_TYPE) %></td>
<td><%= BaseForm.shortenString(queryResults.getNotes(), 100) %></td></tr><% } %>
</table>
<% } else { 
%><p align='center'><b>No Results</b></p>
<% } } %>