<%@ page import="edu.uic.orjala.cyanos.Inoc,
	edu.uic.orjala.cyanos.Role,
	edu.uic.orjala.cyanos.CyanosObject,
	edu.uic.orjala.cyanos.web.servlet.InocServlet,
	edu.uic.orjala.cyanos.web.servlet.StrainServlet,
	edu.uic.orjala.cyanos.web.BaseForm,
	edu.uic.orjala.cyanos.User,
	edu.uic.orjala.cyanos.Role,
	java.text.SimpleDateFormat" %>
<%	String contextPath = request.getContextPath();
	Inoc queryResults = (Inoc)request.getAttribute(InocServlet.SEARCHRESULTS_ATTR); 
	User user = StrainServlet.getUser(request);
	if ( request.getRemoteUser() != null ) { %>
<form method="post" action="inoc">
<input type="hidden" name="strain" value="<%= request.getParameter("id") %>">
<p align="center"><input type="checkbox" name="allInocs" <%= request.getParameter("allInocs") != null ? "checked" : ""%> onClick="updateForm(this,'<%= request.getParameter("div") %>')"> Show dead & harvested inoculations.</p>
<%		if ( queryResults != null && queryResults.first() ) { 
	queryResults.beforeFirst(); SimpleDateFormat dateFormat = (SimpleDateFormat) session.getAttribute("dateFormatter");  %>
<table  class="dashboard">
<tr><th class="header" width='100'>Inoculation</th><th class="header" width='200'>Date</th><th class="header" width='100'>Media</th><th class="header" width='100'>Volume</th><th class="header" width="100">Notes</th><th class="header" width="50">Fate</th></tr>
<% while ( queryResults.next() ) { if ( ! queryResults.isAllowed(Role.READ) ) continue; %>
<tr class="banded" align='center'><td><a href="<%= contextPath %>/inoc?id=<%= queryResults.getID() %>">Inoc. #<%= queryResults.getID() %></a></td>
<td><%= dateFormat.format(queryResults.getDate()) %></td>
<td><%= queryResults.getMedia() %></td>
<td><%= CyanosObject.autoFormatAmount(queryResults.getVolume(), Inoc.VOLUME_TYPE) %></td>
<td><%= BaseForm.shortenString(queryResults.getNotes(), 100) %></td>
<td><% String fate = queryResults.getFate(); 
	String harvestID = queryResults.getHarvestID(); 
if ( harvestID != null ) { %><a href="harvest?id=<%= harvestID %>">Harvested</a><%
} else if ( fate != null ) { 
	if ( fate.equals(Inoc.FATE_STOCK) ) { 
%><input type="checkbox" name="inoc" value="<%= queryResults.getID() %>"><%
	}
	out.print(fate.substring(0, 1).toUpperCase()); out.print(fate.substring(1).toLowerCase()); 
} else { %><input type="checkbox" name="inoc" value="<%= queryResults.getID() %>"><% } %></td></tr><% } %></table>
<p align="center"><button type="submit" name="form" value="harvest">Harvest</button><button type="submit" name="form" value="kill">Kill</button>
<button type="reset">Reset</button></p>
<% } else { %>
<p align='center'><b>No Results</b></p>
<% } %> 
<% if ( user.couldPerform(User.CULTURE_ROLE, Role.CREATE) ) { %>
<p align="center"><button name="form" value="add">Add Inoculation(s)</button></p>
<% } %>
</form>
<% } %>