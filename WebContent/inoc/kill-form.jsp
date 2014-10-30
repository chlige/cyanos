<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="cyanos" tagdir="/WEB-INF/tags" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.Inoc,
	edu.uic.orjala.cyanos.Strain,
	edu.uic.orjala.cyanos.CyanosObject,
	edu.uic.orjala.cyanos.web.servlet.InocServlet,
	edu.uic.orjala.cyanos.web.servlet.StrainServlet,
	edu.uic.orjala.cyanos.web.BaseForm,
	java.util.List, java.util.Iterator,
	java.util.Date,
	java.text.SimpleDateFormat" %>
<%	String contextPath = request.getContextPath();
	int row = 1; %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<cyanos:header title="Kill Inoculations"/>
</head>
<body>
<cyanos:menu helpModule="<%= InocServlet.HELP_MODULE %>"/>
<div class='content'>
<h2 align="center">Kill Inoculations</h2>
<hr width="75%">

<%	List queryResults = (List)request.getAttribute(InocServlet.ATTR_INOC_LIST); 
	if ( queryResults != null && queryResults.size() > 0 ) { 
	SimpleDateFormat dateFormat = (SimpleDateFormat) session.getAttribute("dateFormatter");  
	boolean confirm = "kill".equals(request.getParameter("action"));
%>
<form method="post" action="inoc">
<input type="hidden" name="form" value="<%= request.getParameter("form") %>">
<table  class="dashboard">
<tr><th class="header">Inoculation</th><th class="header">Strain</th><th class="header" width='150'>Inoc. Date</th><th class="header" width='100'>Media</th><th class="header" width='100'>Volume</th><th class="header" width="100">Notes</th><th class="header">Fate</th>
<th class="header" width='200'>Kill Date</th></tr>
<% Iterator anIter = queryResults.iterator();
Date killDate = new Date();
while ( anIter.hasNext() ) { 
	Object item = anIter.next();
	if ( item instanceof Inoc ) {
		Inoc thisInoc = (Inoc) item;
		String fieldName = String.format("%s_date", thisInoc.getID()); 
%><tr class="banded" align='center'>
<td>
<% if ( confirm ) { 
	thisInoc.setRemovedDate(request.getParameter(fieldName)); out.print("<font color='red'><b>KILLED</b></font> "); } else {  %>
<input type="checkbox" name="inoc" value="<%= thisInoc.getID() %>" checked><% } %> Inoc. #<%= thisInoc.getID() %></td>
<td><% Strain strain = thisInoc.getStrain(); out.print(strain.getID()); out.print(" <i>"); out.print(strain.getName()); out.print("</i>");  %></td>
<td><%= dateFormat.format(thisInoc.getDate()) %></td>
<td><%= thisInoc.getMedia() %></td>
<td><%= CyanosObject.autoFormatAmount(thisInoc.getVolume(), Inoc.VOLUME_TYPE) %></td>
<td><%= BaseForm.shortenString(thisInoc.getNotes(), 100) %></td>
<td><% String fate = thisInoc.getFate(); 
if ( thisInoc.getHarvestID() != null ) { out.print("Harvested");
} else if ( fate != null ) { out.print(fate.substring(0, 1).toUpperCase()); out.print(fate.substring(1).toLowerCase());  } %></td>
<td>
<% if ( confirm ) {
	out.println(dateFormat.format(thisInoc.getRemoveDate())); 
} else {  %>
<input type="text" name="<%= fieldName %>" onFocus="showDate('cal_<%= fieldName %>','<%= fieldName %>')" style='padding-bottom: 0px' id="<%= fieldName %>" value='<fmt:formatDate value="<%= killDate %>" pattern="yyyy-MM-dd"/>' size="10"/>
<a onclick="showDate('cal_<%= fieldName %>','<%= fieldName %>')"><img align="MIDDLE" border="0" src="<%= contextPath %>/images/calendar.png"></a>
<div id="cal_<%= fieldName %>" class='calendar'>
<jsp:include page="/calendar.jsp">
<jsp:param value="<%= fieldName %>" name="update_field"/>
<jsp:param value="cal_<%= fieldName %>" name="div"/>
</jsp:include>
</div><% } %></td>
</tr><% } } %></table>
<% if ( ! confirm ) { %>
<p align="center"><button type="submit" name="action" value="kill">Confirm Selected</button><button type="reset">Reset</button></p>
<% } %>
</form>
<% } %>
</div></body></html>