<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page import="edu.uic.orjala.cyanos.Inoc, edu.uic.orjala.cyanos.Role,
	edu.uic.orjala.cyanos.CyanosObject,edu.uic.orjala.cyanos.web.servlet.InocServlet,
	edu.uic.orjala.cyanos.web.BaseForm,
	java.text.SimpleDateFormat" %>
<%	String contextPath = request.getContextPath();
	Inoc queryResults = (Inoc)request.getAttribute(InocServlet.SEARCHRESULTS_ATTR); 
	if ( queryResults != null && request.getRemoteUser() != null && queryResults.first() ) { 	
		queryResults.beforeFirst(); 
		SimpleDateFormat dateFormat = (SimpleDateFormat) session.getAttribute(InocServlet.SESS_ATTR_DATE_FORMAT); 
		boolean showDead = request.getParameter("samestyle") == null;
%><table  class="dashboard">
<tr><th class="header" width='100'>Inoculation</th><th class="header" width='200'>Date</th><th class="header" width='100'>Media</th><th class="header" width='100'>Volume</th><th class="header">Fate</th><th class="header" width="100">Notes</th></tr>
<% while ( queryResults.next() ) { 
	if ( ! queryResults.isAllowed(Role.READ) ) continue;  
%><tr class="<%= ( showDead && queryResults.getRemoveDate() != null ? "dead" : "normal" ) %>" align='center'><td><a href="<%= contextPath %>/inoc?id=<%= queryResults.getID() %>">Inoc. #<%= queryResults.getID() %></a></td>
<td><%= dateFormat.format(queryResults.getDate()) %></td>
<td><%= queryResults.getMedia() %></td>
<td><%= queryResults.getVolumeString() %></td>
<td><c:out value="<%= queryResults.getFate() %>"/></td>
<td><%= BaseForm.shortenString(queryResults.getNotes(), 100) %></td></tr><% } %>
</table><% 
} else { 
%><p align='center'><b>No Results</b></p><% 
} %>