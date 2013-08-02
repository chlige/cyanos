<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.Compound,edu.uic.orjala.cyanos.web.servlet.CompoundServlet,java.text.SimpleDateFormat" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<script type="text/javascript" src="cyanos.js"></script>
<script type="text/javascript" src="jmol/Jmol.js"></script>
<link rel="stylesheet" type="text/css" href="cyanos.css"/>
<% 	Compound myObject = (Compound) request.getAttribute(CompoundServlet.COMPOUND_OBJ);
if ( myObject != null && myObject.first() ) { %>
<title>Compound <%= myObject.getID() %></title>
<% } else { %>
<title>Compound Search</title>
<% } %>
</head>
<body>

<jsp:include page="includes/menu.jsp">
<jsp:param value="<%= CompoundServlet.HELP_MODULE %>" name="module"/>
</jsp:include>

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
<p align="CENTER"><font size="+3" >Compound Search</font>
<hr width='85%'/></p>
<center>
<form name="compoundquery">
<table border=0>
<tr><td>Query:</td><td>
<% String queryValue = request.getParameter("query"); if ( queryValue == null ) { queryValue = ""; }%>
<input type="text" name="query" VALUE="<%= queryValue %>"></td>
<td>
<button type='SUBMIT'>Search</button>
</td></tr>
</table>
</form>
</center>
<jsp:include page="/compound/compound-list.jsp" />
<% } %>
</div>
</body>
</html>