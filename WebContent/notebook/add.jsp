<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="cyanos" tagdir="/WEB-INF/tags" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.web.servlet.MainServlet,
	edu.uic.orjala.cyanos.web.listener.AppConfigListener,
	edu.uic.orjala.cyanos.CyanosObject,
	edu.uic.orjala.cyanos.BasicObject,
	edu.uic.orjala.cyanos.User,
	edu.uic.orjala.cyanos.sql.SQLNotebook,
	edu.uic.orjala.cyanos.Notebook,
	java.text.DateFormat" %>
<!DOCTYPE html>
<html>
<head>
<cyanos:header title="Cyanos Notebooks"/>
<style type="text/css">
h2 { text-align:center; }
table.details { border-collapse: collapse; margin-bottom: 10px; }
table.details td, table.details th { text-align:left; }
table.results tr { border-top: 1px solid gray; border-bottom: 1px solid gray; }
table.results td, table.results th { padding-left: 2px; padding-right: 2px; }
table { margin-left: auto; margin-right:auto; }
</style>
</head>
<body>
<cyanos:menu/>
<h1>Add a Notebook</h1>
<hr width="85%">
<% if ( request.getParameter("add") != null ) { 
	Notebook notebook = SQLNotebook.createNotebook(MainServlet.getSQLData(request), request.getParameter("id"));
	if ( notebook != null ) {	
		notebook.setTitle(request.getParameter("title"));
		notebook.setDescription(request.getParameter("desc"));
%><p align="center">Added notebook <a href="../notebook.jsp?id=<%= request.getParameter("id") %>"><%= request.getParameter("id") %></a></p>
<%		
	} else {
%><p align="center">Unable to add notebook!</p><%
	}
} else { %>
<form method="post">
<p align="center">
<label for="id">Notebook ID:</label><input type="text" name="id"><br>
<label for="title">Title:</label><input type="text" name="title"><br>
<label for="desc">Description</label><br>
<textarea name="desc"></textarea>
<br>
<button type="submit" name="add">Add Notebook</button>
</p>
</form>
<% } %>
</body>
</html>