<%@ taglib prefix="cyanos" tagdir="/WEB-INF/tags" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.Compound,edu.uic.orjala.cyanos.web.servlet.CompoundServlet,java.text.SimpleDateFormat" %>
<% 	Compound myObject = (Compound) request.getAttribute(CompoundServlet.COMPOUND_OBJ); %>
<!DOCTYPE html>
<html>
<head>
<cyanos:header title="Cyanos - Compound">
<script type="text/javascript" src="jmol/Jmol.js"></script>
</cyanos:header>
</head>
<body>
<cyanos:menu helpModule="<%= CompoundServlet.HELP_MODULE %>"/>
<div class='content'>
<% if ( myObject != null && myObject.first() ) { %>
<p align="CENTER"><font size="+3" >Compound <%= myObject.getID() %></font>
<div id="<%= CompoundServlet.INFO_FORM_DIV_ID %>" class="main">
<jsp:include page="/compound/compound-form.jsp" />
</div>

<DIV CLASS="collapseSection"><A NAME='<%= CompoundServlet.MATERIAL_LIST_DIV_ID %>' CLASS='twist' onClick='loadDiv("<%= CompoundServlet.MATERIAL_LIST_DIV_ID %>")' CLASS='divTitle'>
<IMG ALIGN="ABSMIDDLE" ID="twist_<%= CompoundServlet.MATERIAL_LIST_DIV_ID %>" SRC="/cyanos/images/twist-closed.png" /> Materials</A>
<DIV CLASS="unloaded" ID="div_<%= CompoundServlet.MATERIAL_LIST_DIV_ID %>"></DIV>
</DIV>

<div class="collapseSection"><a name='<%= CompoundServlet.SEP_LIST_DIV_ID %>' class='twist' onClick='loadDiv("<%= CompoundServlet.SEP_LIST_DIV_ID %>")' class='divTitle'>
<IMG ALIGN="ABSMIDDLE" ID="twist_<%= CompoundServlet.SEP_LIST_DIV_ID %>" SRC="/cyanos/images/twist-closed.png" /> Separations</A>
<DIV CLASS="unloaded" ID="div_<%= CompoundServlet.SEP_LIST_DIV_ID %>"></div>
</div>

<DIV CLASS="collapseSection"><A NAME='<%= CompoundServlet.ASSAY_DIV_ID %>' CLASS='twist' onClick='loadDiv("<%= CompoundServlet.ASSAY_DIV_ID %>")' CLASS='divTitle'>
<IMG ALIGN="ABSMIDDLE" ID="twist_<%= CompoundServlet.ASSAY_DIV_ID %>" SRC="/cyanos/images/twist-closed.png" /> Assays</A>
<DIV CLASS="unloaded" ID="div_<%= CompoundServlet.ASSAY_DIV_ID %>"></DIV>
</DIV>

<DIV CLASS="collapseSection"><A NAME='<%= CompoundServlet.DATAFILE_DIV_ID %>' CLASS='twist' onClick='loadDiv("<%= CompoundServlet.DATAFILE_DIV_ID %>")' CLASS='divTitle'>
<IMG ALIGN="ABSMIDDLE" ID="twist_<%= CompoundServlet.DATAFILE_DIV_ID %>" SRC="/cyanos/images/twist-closed.png" /> Data Files</A>
<DIV CLASS="unloaded" ID="div_<%= CompoundServlet.DATAFILE_DIV_ID %>"></DIV>
</DIV>

<% } else { %>
<h2 style="text-align:center">Compound Search</h2>
<hr width='85%'/>
<form name="compoundquery">
<p style="text-align:center">Query: <input type="text" name="query" VALUE="<%= request.getParameter("query") != null ? request.getParameter("query") : "" %>">
<button type='SUBMIT'>Search</button></p>
</form>
<p align="center"><a href="?query">List all compounds</a></p>
<% if ( request.getParameter("query") != null ) { %>
<p align="center"><a href="?query=<%= request.getParameter("query") %>&export=sdf">Export Results as SDFile</a></p>
<% } %>
<jsp:include page="/compound/compound-list.jsp" />
<% } %>
</div>
</body>
</html>