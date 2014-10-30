<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="cyanos" tagdir="/WEB-INF/tags" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.Project,
	edu.uic.orjala.cyanos.CyanosObject,edu.uic.orjala.cyanos.web.servlet.ProjectServlet,
	edu.uic.orjala.cyanos.web.BaseForm,
	java.text.SimpleDateFormat" %>
<%  String contextPath = request.getContextPath();
	Project myObject = (Project) request.getAttribute(ProjectServlet.PROJECT_OBJECT); %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<cyanos:header title="Cyanos - Project <%= myObject != null && myObject.first() ? myObject.getID() : "List"%>"/>
</head>
<body>

<cyanos:menu helpModule="project"/>

<div class='content'>
<% if ( myObject != null && myObject.first() ) { %>
<div id="<%= ProjectServlet.INFO_FORM_DIV_ID %>">
<jsp:include page="/project/project-form.jsp" />
</div>

<div CLASS="collapseSection"><A NAME='collection' CLASS='twist' onClick='loadDiv("collection")' CLASS='divTitle'>
<img align="ABSMIDDLE" ID="twist_collection" SRC="<%= contextPath %>/images/twist-closed.png" /> Collections</A>
<div CLASS="unloaded" ID="div_collection">
</div></div>

<DIV CLASS="collapseSection"><A NAME='strain' CLASS='twist' onClick='loadDiv("strain")' CLASS='divTitle'>
<IMG ALIGN="ABSMIDDLE" ID="twist_strain" SRC="<%= contextPath %>/images/twist-closed.png" /> Strains</A>
<DIV CLASS="unloaded" ID="div_strain">
</div></div>

<DIV CLASS="collapseSection"><A NAME='material' CLASS='twist' onClick='loadDiv("material")' CLASS='divTitle'>
<IMG ALIGN="ABSMIDDLE" ID="twist_material" SRC="/cyanos/images/twist-closed.png" /> Materials</A>
<DIV CLASS="unloaded" ID="div_material"></DIV>
</DIV>

<DIV CLASS="collapseSection"><A NAME='assay' CLASS='twist' onClick='loadDiv("assay")' CLASS='divTitle'>
<IMG ALIGN="ABSMIDDLE" ID="twist_assay" SRC="/cyanos/images/twist-closed.png" /> Assays</A>
<DIV CLASS="unloaded" ID="div_assay"></DIV>
</DIV>

<DIV CLASS="collapseSection"><A NAME='compounds' CLASS='twist' onClick='loadDiv("compounds")' CLASS='divTitle'>
<IMG ALIGN="ABSMIDDLE" ID="twist_compounds" SRC="/cyanos/images/twist-closed.png" /> Compounds</A>
<DIV CLASS="unloaded" ID="div_compounds"></DIV>
</DIV>
<% } else if ( request.getParameter("addForm") != null ) { %>
<jsp:include page="/project/project-add.jsp"/>
<% } else { %>
<p align="CENTER"><font size="+3" >Project List</font>
<hr width='85%'/></p>
<center>
<form name="query">
<table border=0>
<tr><td>Query:</td><td>
<% String queryValue = request.getParameter("query"); if ( queryValue == null ) { queryValue = ""; }%>
<input id="query" type="text" name="query" VALUE="<%= queryValue %>" style='padding-bottom: 0px'/></td>
<td>
<button type='SUBMIT'>Search</button>
</td></tr>
</table>
</form>
</center>
<!-- TODO: Need to have a way to list the projects. -->
<% Project queryResults = (Project) request.getAttribute(ProjectServlet.SEARCHRESULTS_ATTR); 
if ( queryResults != null ) { 
	if ( queryResults.first() ) { 
		queryResults.beforeFirst(); 
%><table class="dashboard">
<tr><th class="header" width='100'>Project</th>
<th class="header" width='100'>Title</th>
<th class="header" width='400'>Description</th>
<th class="header" width="200">Master Server</th></tr>
<% while ( queryResults.next() ) { 
%><tr class="banded" align='center'>
<td><a href="<%= contextPath %>/project?id=<%= queryResults.getID() %>"><%= queryResults.getID() %></a></td>
<td><%= queryResults.getName() %></td>
<td><%= BaseForm.shortenString(queryResults.getNotes(), 60) %></td>
<td><% String masterURL = queryResults.getMasterURL(); 
if ( masterURL != null ) { %><a href="<%= masterURL %>"><%= masterURL %></a><% } else { %>NONE<% } %></td>
</tr><% } %>
</table>
<% } else { %>
<hr width="85%"/>
<p align='center'><b>No Results</b></p>
<% } } %>
<p align="center"><a href="?addForm">Add new project</a></p>
<% }%>
</div>
</body>
</html>