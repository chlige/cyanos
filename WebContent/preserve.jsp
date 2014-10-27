<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.Assay,
	edu.uic.orjala.cyanos.CyanosObject,edu.uic.orjala.cyanos.web.servlet.AssayServlet,
	edu.uic.orjala.cyanos.web.BaseForm,
	java.math.BigDecimal,
	java.util.List,
	java.text.SimpleDateFormat" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<script language="JAVASCRIPT" src="cyanos.js"></script>
<script language="JAVASCRIPT" src="cyanos-date.js"></script>
<link rel="stylesheet" type="text/css" href="cyanos.css"/>
<title>Cyanos - Preservations</title>
</head>
<body>

<jsp:include page="includes/menu.jsp" />
<div class='content'>
<% if ( request.getParameter("id") != null ) { %>
<p align="CENTER"><font size="+2" >Preservation Information</font>
<hr width="75%">
<jsp:include page="/preserve/form.jsp"/>
<% } else if ( request.getParameter("collection") != null ) {%>
<p align="CENTER"><font size="+2" >Preservation Collection</font>
<hr width="75%">
<jsp:include page="/preserve/collection.jsp"/>
<div class="collapseSection"><a name='collectionList' onClick='loadDiv("collectionList")' class='divTitle'>
<img align="ABSMIDDLE" id="twist_collectionList" src="/cyanos/images/twist-open.png" /> Preservation List</a>
<div class="showSection" id="div_collectionList">
<jsp:include page="/preserve/collection-list.jsp"/>
</div></div>
<% } else { %>
<p align="CENTER"><font size="+3" >Preservation Search</font>
<hr width='85%'/></p>
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
<jsp:include page="/preserve/list.jsp" />
<% } %>
</div>

</body>
</html>