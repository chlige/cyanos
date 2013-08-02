<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page isErrorPage="true" %>
<%@ page import="java.io.PrintWriter" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<%  String contextPath = request.getContextPath(); %>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<script language="JAVASCRIPT" src="<%= contextPath %>/cyanos.js"></script>
<link rel="stylesheet" type="text/css" href="<%= contextPath %>/cyanos.css"/>
<title>Cyanos Database Error</title>
</head>
<body>
<jsp:include page="includes/menu.jsp" />
<div class='content'>
<h2>An error has been generated</h2>
<dl>
<dt><b>Exception: </b> <%= exception.getClass().getCanonicalName() %></dt>
<dd>
<pre>
<% PrintWriter writer = new PrintWriter(out);
	exception.printStackTrace(writer);%>
</pre></dd>
<% 	Throwable cause = exception.getCause();
	if ( cause == null && exception instanceof ServletException )
		cause = ((ServletException) exception).getRootCause();
	while ( cause != null ) { %>
<dt><b>Caused by: </b> <%= cause.getClass().getCanonicalName() %></dt>
<dd><pre><% cause.printStackTrace(writer); cause = cause.getCause(); if ( cause == null && cause instanceof ServletException ) { cause = ((ServletException)cause).getRootCause(); } %></pre></dd>
<% } %>
<!-- <c:out value="<%= exception.toString() %>"></c:out> -->
</dl>
</div>
</body>
</html>