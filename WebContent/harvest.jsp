<%@ taglib prefix="cyanos" tagdir="/WEB-INF/tags" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.Harvest,edu.uic.orjala.cyanos.web.servlet.HarvestServlet,
	java.text.SimpleDateFormat" %>
<% Harvest thisObject = (Harvest) request.getAttribute("harvest"); %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<cyanos:header title="Harvest <%= (thisObject != null ? thisObject.getID() : "Search") %>"/>
</head>
<body>
<cyanos:menu helpModule="<%= HarvestServlet.HELP_MODULE %>"/>

<div class='content'>
<% if ( thisObject != null && thisObject.first() ) { %>
<p align="CENTER"><font size="+2" >Harvest Information</font>
<hr width="75%">
<div id="<%= HarvestServlet.INFO_FORM_DIV_ID %>">
<jsp:include page="harvest/harvest-form.jsp" />
</div>

<DIV CLASS="collapseSection"><A NAME='<%= HarvestServlet.EXTRACT_LIST_DIV_ID %>' CLASS='twist' onClick='loadDiv("<%= HarvestServlet.EXTRACT_LIST_DIV_ID %>")' CLASS='divTitle'>
<IMG ALIGN="ABSMIDDLE" ID="twist_<%= HarvestServlet.EXTRACT_LIST_DIV_ID %>" SRC="/cyanos/images/twist-closed.png" /> Extracts</A>
<DIV CLASS="unloaded" ID="div_<%= HarvestServlet.EXTRACT_LIST_DIV_ID %>"></DIV>
</DIV>

<% } else { %>
<p align="CENTER"><font size="+3" >Harvest Search</font>
<hr width='85%'/></p>
<center>
<form name="materialquery">
<table border=0>
<tr><td>Culture ID:</td><td>
<% String queryValue = request.getParameter("query"); if ( queryValue == null ) { queryValue = ""; }%>
<input id="query" type="text" name="query" VALUE="<%= queryValue %>" autocomplete='off' onKeyUp="livesearch(this, 'query', 'div_query')" style='padding-bottom: 0px'/>
<div id="div_query" class='livesearch'></div></td>
<td>
<button type='SUBMIT'>Search</button>
</td></tr>
</table>
</form>
</center>
<jsp:include page="/harvest/harvest-list.jsp" />
<% } %>
</div>
</body>
</html>