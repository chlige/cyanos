<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.Material,edu.uic.orjala.cyanos.web.servlet.MaterialServlet,java.text.SimpleDateFormat" %>
<%@ taglib prefix="cyanos" tagdir="/WEB-INF/tags" %>
<% 	Material materialObj = (Material) request.getAttribute("material"); %>
<!DOCTYPE html>
<html>
<head>
<cyanos:header title="Cyanos - Materials"/>
</head>
<body style="min-height:100%">

<cyanos:menu helpModule="material"/>

<div class='content' style="padding-bottom: 60px;">
<% if ( materialObj != null && materialObj.first() ) { %>
<p align="CENTER"><font size="+3" >Material #<%= materialObj.getID() %></font>
<hr width="90%">
<div id="<%= MaterialServlet.INFO_FORM_DIV_ID %>">
<jsp:include page="/material/material-form.jsp" />
</div>

<jsp:include page="/includes/loadableDiv.jsp">
<jsp:param value="<%= MaterialServlet.SAMPLE_LIST_DIV_ID %>" name="loadingDivID"/>
<jsp:param value="Samples" name="loadingDivTitle"/>
</jsp:include>

<jsp:include page="/includes/loadableDiv.jsp">
<jsp:param value="<%= MaterialServlet.SEP_LIST_DIV_ID %>" name="loadingDivID"/>
<jsp:param value="Separations" name="loadingDivTitle"/>
</jsp:include>

<jsp:include page="/includes/loadableDiv.jsp">
<jsp:param value="<%= MaterialServlet.DATAFILE_DIV_ID %>" name="loadingDivID"/>
<jsp:param value="Data Files" name="loadingDivTitle"/>
</jsp:include>

<jsp:include page="/includes/loadableDiv.jsp">
<jsp:param value="<%= MaterialServlet.COMPOUND_DIV_ID %>" name="loadingDivID"/>
<jsp:param value="Compounds" name="loadingDivTitle"/>
</jsp:include>

<jsp:include page="/includes/loadableDiv.jsp">
<jsp:param value="<%= MaterialServlet.ASSAY_DIV_ID %>" name="loadingDivID"/>
<jsp:param value="Assays" name="loadingDivTitle"/>
</jsp:include>

<% } else { %>
<p align="CENTER"><font size="+3" >Material Search</font></p>
<hr width='85%'/>
<center>
<form name="materialquery">
<table border=0>
<tr><td>Strain ID:</td><td>
<% String queryValue = request.getParameter("query"); if ( queryValue == null ) { queryValue = ""; }%>
<input id="query" type="text" name="query" VALUE="<%= queryValue %>" autocomplete='off' onKeyUp="livesearch(this, 'query', 'div_query')" style='padding-bottom: 0px'/>
<div id="div_query" class='livesearch' style="width:75px;"></div></td>
<td>
<button type='SUBMIT'>Search</button>
</td></tr>
</table>
</form>
</center>
<jsp:include page="/material/material-list.jsp" />
<% } %>
</div>
</body>
</html>