<%@ taglib prefix="cyanos" tagdir="/WEB-INF/tags" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page isErrorPage="true" %>
<%@ page import="java.io.PrintWriter" %>
<!DOCTYPE html>
<html>
<cyanos:header title="Cyanos Database Error"/>
</head>
<body>
<cyanos:menu/>
<div class='content'>
<h1 style="color:red">An error has occurred</h1>
<h2 style="text-align:center; font-style:italic; font-weight:normal; color:#555"><%= exception.getMessage() %></h2>

<div class="collapseSection" style="background-color:white; border: 0px"><a name='debug' class='twist' onClick='loadDiv("debug")' class='divTitle'>
<img align="middle" id="twist_debug" src="<%= request.getContextPath() %>/images/twist-closed.png" /> Exception Report</a>
<div class="hideSection" id="div_debug">
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
</div></div></div>
</body>
</html>