<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.Isolation,edu.uic.orjala.cyanos.web.servlet.CollectionServlet,java.text.SimpleDateFormat" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<script language="JAVASCRIPT" src="cyanos.js"></script>
<link rel="stylesheet" type="text/css" href="cyanos.css"/>
<% 	Isolation isolationObj = (Isolation) request.getAttribute(CollectionServlet.ATTR_ISOLATION);
if ( isolationObj != null && isolationObj.first() ) { %>
<title>Isolation <%= isolationObj.getID() %></title>
<% } else { %>
<title>Isolation Search</title>
<% } %>
</head>
<body style="min-height:100%">

<jsp:include page="/includes/menu.jsp">
<jsp:param value="<%= CollectionServlet.HELP_MODULE %>" name="module"/>
</jsp:include>

<div class='content'>
<% if ( isolationObj != null && isolationObj.first() ) { %>
<p align="CENTER"><font size="+3" >Isolation <%= isolationObj.getID() %></font>
<hr width="85%">
<div id="<%= CollectionServlet.INFO_FORM_DIV_ID %>" class="main">
<jsp:include page="/isolation/isolation-form.jsp" />
</div>

<jsp:include page="/includes/loadableDiv.jsp">
<jsp:param value="<%= CollectionServlet.STRAIN_LIST_DIV_ID %>" name="loadingDivID"/>
<jsp:param value="Strains" name="loadingDivTitle"/>
</jsp:include>

<% } else { %>
<p align="CENTER"><font size="+3" >Isolation Search</font></p>
<hr width='85%'/>
<% } %>
</div>
</body>
</html>