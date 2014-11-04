<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="cyanos" tagdir="/WEB-INF/tags" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.Cryo, edu.uic.orjala.cyanos.web.servlet.CryoServlet, edu.uic.orjala.cyanos.sql.SQLCryo,
	edu.uic.orjala.cyanos.CryoCollection, edu.uic.orjala.cyanos.sql.SQLCryoCollection" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<cyanos:header title="Cyanos - Preservations"/>
</head>
<body>
<cyanos:menu helpModule="cryo"/>
<div class='content'>
<% if ( request.getParameter("id") != null ) { %>
<p align="CENTER"><font size="+2" >Preservation Information</font>
<hr width="75%">
<jsp:include page="/preserve/form.jsp"/>
<% } else if ( request.getParameter("collection") != null ) { 
	Cryo queryResults = SQLCryo.loadForCollection(CryoServlet.getSQLData(request), request.getParameter("collection"));
	request.setAttribute("cryoList", queryResults);
%><p align="CENTER"><font size="+2" >Preservation Collection</font>
<hr width="75%">
<jsp:include page="/preserve/collection.jsp"/>
<div class="collapseSection"><a name='collectionList' onClick='loadDiv("collectionList")' class='divTitle'>
<img align="absmiddle" id="twist_collectionList" src="/cyanos/images/twist-closed.png" /> Preservation List</a>
<div class="hideSection" id="div_collectionList">
<jsp:include page="/preserve/list.jsp"/>
<p align="center"><a href="<%= request.getContextPath()  %>/preserve/add.jsp">Add Preservation</a></p>
</div></div>
<% } else { %>
<p style="text-align:center; font-size:14pt;">Preservation Search</p>
<hr width='85%'/>
<center>
<form name="preservequery">
<table border=0>
<tr><td>Strain ID:</td><td>
<% String queryValue = request.getParameter("query"); if ( queryValue == null ) { queryValue = ""; }%>
<input id="query" type="text" name="query" VALUE="<%= queryValue %>"/>
<!-- autocomplete='off' onKeyUp="livesearch(this, 'query', 'div_query')" style='padding-bottom: 0px' -->
<div id="div_query" class='livesearch'></div></td>
<td>
<button type='SUBMIT'>Search</button>
</td></tr>
</table>
</form>
</center>
<%
	CryoCollection collections = SQLCryoCollection.collectionsForType(CryoServlet.getSQLData(request), CryoCollection.BOX);
	if ( collections != null ) {	
		collections.beforeFirst();
%>
<div class="collapseSection"><a name='collectionList' onClick='loadDiv("collectionList")' class='divTitle'>
<img align="absmiddle" id="twist_collectionList" src="/cyanos/images/twist-closed.png" /></a> Preservation Collections
<div class="hideSection" id="div_collectionList">
<table class="dashboard">
<tr><th class="header">Collection</th><th class="header">Notes</th></tr>
<% 
	while ( collections.next() ) { 
%><tr><td><a href="?collection=<%= collections.getID() %>"><%= collections.getName() %></a></td><td><%= CryoServlet.shortenString(collections.getNotes(), 100) %></td></tr>
<% } %></table>
</div></div>
<% }
	if ( request.getParameter("query") != null ) {
	Cryo queryResults = SQLCryo.loadForStrain(CryoServlet.getSQLData(request), request.getParameter("query"));	
	request.setAttribute("cryoList", queryResults);
%><jsp:include page="/preserve/list.jsp" /><% } } %>
</div>
</body>
</html>