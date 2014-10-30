<%@ page buffer="12kb" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="cyanos" tagdir="/WEB-INF/tags" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.web.servlet.HelpServlet,
	edu.uic.orjala.cyanos.web.help.HelpIndex,
	java.util.List,
	org.apache.lucene.search.Query,
	org.apache.lucene.search.TermQuery,
	org.apache.lucene.search.MatchAllDocsQuery,
	org.apache.lucene.index.Term" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<cyanos:header title="Cyanos - Help"/>
</head>
<body>

<cyanos:menu/>

<div class='content'>
<%  String contextPath = request.getContextPath(); 
	String query = request.getParameter("query");
	String helpPath = (String) request.getAttribute("helpPath"); 
	if ( request.getParameter("search") != null ) { %>
<h2 align="center">Search Help Content</h2>
<hr width="80%">
<form>
<p align="center">
<input type="text" name="query" value="<c:out value="<%= query %>"/>">
<button type="submit" name="search">Search Content</button></p>
</form><%
		if ( query != null && query.length() > 0 ) { 
%><jsp:include page="/includes/help/search-results.jsp"/><%
		} 
	} else if ( request.getParameter("find") != null ) { %>
<h2 align="center">Find Help Page</h2>
<hr width="80%">
<form>
<p align="center">
<input type="text" name="query" value="<c:out value="<%= query %>"/>">
<button type="submit" name="find">Find Keywords</button></p>
</form><%
		if ( query != null ) { 
%><jsp:include page="/includes/help/search-results.jsp"/>
<% 
		}
	} else if ( helpPath != null ) { %>
<h2 align="center">Table of Contents</h2>
<hr width="80%">
<jsp:include page="/includes/help/toc.jsp"/>
<% } 
%></div>
</body>
</html>