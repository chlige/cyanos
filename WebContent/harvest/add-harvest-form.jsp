<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.Harvest,
	edu.uic.orjala.cyanos.User,
	edu.uic.orjala.cyanos.Project,
	edu.uic.orjala.cyanos.web.servlet.HarvestServlet,
	edu.uic.orjala.cyanos.web.servlet.InocServlet,
	java.util.List, edu.uic.orjala.cyanos.Inoc,
	edu.uic.orjala.cyanos.sql.SQLData,
	edu.uic.orjala.cyanos.sql.SQLHarvest,
	java.util.Arrays,
	edu.uic.orjala.cyanos.web.BaseForm,
	java.text.SimpleDateFormat" %>
<% 	String contextPath = request.getContextPath(); 
	String strain = request.getParameter("strain"); %>
<form method="post">
<input type="hidden" value="harvest" name="form">
<table class="species" align='center'>
<% if ( strain != null ) { %>
<tr><td>Strain:</td><td><input type="hidden" name="strain" value="<%= request.getParameter("strain") %>">
<%= request.getParameter("strain") %></td></tr>
<tr><td colspan="2">
<%	Inoc queryResults = (Inoc)request.getAttribute(InocServlet.SEARCHRESULTS_ATTR); 
	List<String> selected = Arrays.asList(request.getParameterValues("inoc"));	
	if ( queryResults != null && queryResults.first() ) { 
	boolean oddRow = true; queryResults.beforeFirst(); SimpleDateFormat dateFormat = (SimpleDateFormat) session.getAttribute("dateFormatter");  %>
<table  class="dashboard">
<tr><td></td><th class="header"  width="150">Date</th><th class="header">Project</th><th class="header" width="100">Media</th><th class="header"  width="100">Volume</th><th class="header"  width="100">Notes</th></tr>
<% while ( queryResults.next() ) { %>
<tr class="banded" align='center'><td><input type="checkbox" name="inoc" value="<%= queryResults.getID() %>" <%= ( selected.contains(queryResults.getID()) ? "checked" : "") %>></td>
<td><%= dateFormat.format(queryResults.getDate()) %></td>
<td><% Project project = queryResults.getProject(); 
	if ( project != null && project.first() ) { out.print(project.getName()); } %></td>
<td><%= queryResults.getMedia() %></td>
<td><%= SQLHarvest.autoFormatAmount(queryResults.getVolume(), Inoc.VOLUME_TYPE) %></td>
<td><%= BaseForm.shortenString(queryResults.getNotes(), 100) %></td></tr><% } %>
</table>
<% } %>
</td></tr>
<% } else if ( request.getParameter("col") != null ) { %>
<tr><td>Collection ID:</td><td><input type="hidden" name="col" value="<%= request.getParameter("col") %>"><%= request.getParameter("col") %></td></tr>
<tr><td>Strain ID:</td><td><input type="text" name="strain"></td></tr>
<% } %>
<tr><td>Harvest Date:</td><td><input type="text" name="harvDate" onFocus="showDate('div_calendar','harvDate')" style='padding-bottom: 0px' id="harvDate"/>
<a onclick="showDate('div_calendar','harvDate')"><img align="MIDDLE" border="0" src="<%= contextPath %>/images/calendar.png"></a>
<div id="div_calendar" class='calendar'>
<jsp:include page="/calendar.jsp">
<jsp:param value="harvDate" name="update_field"/>
<jsp:param value="div_calendar" name="div"/>
</jsp:include>
</div></td></tr>
<tr><td>Color:</td><td><input type="text" name="color"></td></tr>
<tr><td>Type:</td><td>
<% List<String> types = SQLHarvest.types((SQLData)request.getAttribute(HarvestServlet.DATASOURCE)); 
	int count = 1;
	for (String type: types ) {
		out.print("<input type=\"checkbox\" name=\"type\" value=\"");
		out.print(type);
		out.print("\">");
		out.print(type.substring(0, 1).toUpperCase());
		out.print(type.substring(1).toLowerCase());
		count++;
		if ( count < 2 ) {
			out.print("<br>");
			count = 1;
		}
	}
%></td></tr>
<tr><td>Project</td><td>
<jsp:include page="/includes/project-popup.jsp">
<jsp:param value="project" name="fieldName"/></jsp:include>
</td></tr>
<tr><td>Prep. Date:</td><td>
<input type="text" name="prepDate" onFocus="showDate('div_calendar','prepDate')" style='padding-bottom: 0px' id="prepDate"/>
<a onclick="showDate('div_calendar','prepDate')"><img align="MIDDLE" border="0" src="<%= contextPath %>/images/calendar.png"></a>
<div id="div_calendar" class='calendar'>
<jsp:include page="/calendar.jsp">
<jsp:param value="prepDate" name="update_field"/>
<jsp:param value="div_calendar" name="div"/>
</jsp:include>
</div>	
<tr><td>Cell Mass:</td><td><input type="text" name="cellMass"></td></tr>
<tr><td>Media Volume:</td><td><input type="text" name="mediaVol"></td></tr>
<tr><td valign=top>Notes:</td><td><textarea rows="7" cols="70" name="notes"></textarea></td></tr>
<tr><td colspan="2" align="CENTER"><button type="submit" name="addHarvest">Add Harvest</button>
<input type="RESET"></td></tr>
</table>
</form>