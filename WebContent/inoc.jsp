<%@ taglib prefix="cyanos" tagdir="/WEB-INF/tags" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.Inoc,edu.uic.orjala.cyanos.web.servlet.InocServlet,
	java.text.SimpleDateFormat" %>
<% 	Inoc thisObject = (Inoc) request.getAttribute(InocServlet.ATTR_INOC_OBJECT); %>
<!DOCTYPE html>
<html>
<head>
<cyanos:header title="Cyanos - Inoculations"/>
</head>
<body>

<cyanos:menu helpModule="<%= InocServlet.HELP_MODULE %>"/>

<div class='content'>
<% if ( thisObject != null && thisObject.first() ) { %>
<p align="CENTER"><font size="+2" >Inoculation Information</font>
<hr width="75%">
<div id="<%= InocServlet.INFO_FORM_DIV_ID %>">
<jsp:include page="/inoc/info-form.jsp"/>
</div><% 

Inoc children = thisObject.getChildren();  
	if ( children != null && children.first() ) { 
	request.setAttribute(InocServlet.SEARCHRESULTS_ATTR, children);

%><DIV CLASS="collapseSection"><A NAME='<%= InocServlet.CHILDREN_DIV_ID %>' CLASS='twist' onClick='loadDiv("<%= InocServlet.CHILDREN_DIV_ID %>")' CLASS='divTitle'>
<IMG ALIGN="ABSMIDDLE" ID="twist_<%= InocServlet.CHILDREN_DIV_ID %>" SRC="/cyanos/images/twist-open.png" /> Child Inoculations</A>
<DIV CLASS="showSection" ID="div_<%= InocServlet.CHILDREN_DIV_ID %>">
<jsp:include page="/inoc/inoc-list.jsp"/>
</DIV></DIV><% } 
	
} else { %>
<p align="CENTER"><font size="+3" >Inoc Search</font>
<hr width='85%'/></p>
<center>
<form name="materialquery">
<table border=0>
<tr><td>Strain ID:</td><td>
<% String queryValue = request.getParameter("query"); if ( queryValue == null ) { queryValue = ""; }%>
<input id="query" type="text" name="query" VALUE="<%= queryValue %>" autocomplete='off' onKeyUp="livesearch(this, 'query', 'div_query')" style='padding-bottom: 0px'/>
<div id="div_query" class='livesearch'></div></td>
<td>
<button type='SUBMIT'>Search</button>
</td></tr>
</table>
</form>
</center>
<jsp:include page="/inoc/inoc-list.jsp" />
<% } %>
</div>
</body>
</html>