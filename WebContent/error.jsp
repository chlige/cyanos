<%@ taglib prefix="cyanos" tagdir="/WEB-INF/tags" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page isErrorPage="true" %>
<%@ page import="java.io.PrintWriter" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<cyanos:header title="Cyanos Database Error"/>
</head>
<body>
<cyanos:menu/>
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