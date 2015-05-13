<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="cyanos" tagdir="/WEB-INF/tags" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.web.servlet.MainServlet,
	edu.uic.orjala.cyanos.web.listener.AppConfigListener,
	edu.uic.orjala.cyanos.CyanosObject,
	edu.uic.orjala.cyanos.BasicObject,
	edu.uic.orjala.cyanos.User,
	java.sql.PreparedStatement,
	java.math.BigDecimal,
	java.math.MathContext,
	java.sql.Connection,
	java.sql.ResultSet,
	java.sql.Statement,
	java.text.DateFormat" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
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
.page { margin-left: auto; margin-right:auto; width:80%; border: 1px solid gray; padding: 5px 10px; background-color: #fffffa; }
</style>
</head>
<body>
<cyanos:menu helpModule="<%= MainServlet.HELP_MODULE %>"/>
<div id="content">
<% Connection conn = AppConfigListener.getDBConnection();  
	if ( request.getParameter("id") != null ) { 
		String notebookid = request.getParameter("id");
		String sql = "SELECT n.title,n.description,COUNT(p.page),MIN(p.date_created),MAX(p.date_updated) FROM notebook n LEFT OUTER JOIN notebook_page p ON(n.notebook_id = p.notebook_id) WHERE n.username=? AND n.notebook_id=?";
		PreparedStatement sth = conn.prepareStatement(sql);
		sth.setString(1, request.getRemoteUser());
		sth.setString(2, notebookid);	
		ResultSet results = sth.executeQuery();
		results.first();
%><h1>Notebook <%= results.getString(1) %></h1><%
		if ( request.getParameter("page") != null ) {
			results.close(); sth.close();
			sql = "SELECT p.page,p.title,p.date_created,p.date_updated,p.content FROM notebook n JOIN notebook_page p ON(n.notebook_id = p.notebook_id) WHERE n.username=? AND n.notebook_id=? AND p.page=?";
			sth = conn.prepareStatement(sql);
			sth.setString(1, request.getRemoteUser());
			sth.setString(2, notebookid);
			sth.setString(3, request.getParameter("page"));
			results = sth.executeQuery();
			if ( results.first() ) { 
%><h2>Page <%= results.getInt(1) %>: <%= results.getString(2) %></h2>
<p align="center">
Created: <%= MainServlet.DATE_FORMAT.format(results.getDate(3)) %><br>
Last Updated: <%= MainServlet.DATE_FORMAT.format(results.getDate(4)) %></p>
<div class="page"><%= results.getString(5) %></div>
<%			}
			results.close(); sth.close();
		} else {
%><p align="center">
<%= results.getInt(3) %> Pages<br>
<% if ( results.getInt(3) > 0 ) { %>
Created: <%= MainServlet.DATE_FORMAT.format(results.getDate(4)) %><br>
Last Updated: <%= MainServlet.DATE_FORMAT.format(results.getDate(5)) %>
<% } %></p>
<p align="center"><%= results.getString(2) %></p>
<%	
			results.close(); sth.close();
			sql = "SELECT p.page,p.title,p.date_created,p.date_updated FROM notebook n JOIN notebook_page p ON(n.notebook_id = p.notebook_id) WHERE n.username=? AND n.notebook_id=? ORDER BY p.page ASC";
			sth = conn.prepareStatement(sql);
			sth.setString(1, request.getRemoteUser());
			sth.setString(2, notebookid);
			results = sth.executeQuery();
			if ( results.first() ) { 
				results.beforeFirst();
%><ul>
<%				while ( results.next() ) {
%><li><a href="notebook.jsp?id=<%= notebookid %>&page=<%= results.getInt(1) %>">Page <%= results.getInt(1) %>: <%= results.getString(2) %></a> Created: <%= MainServlet.DATE_FORMAT.format(results.getDate(3)) %> Last Updated: <%= MainServlet.DATE_FORMAT.format(results.getDate(4)) %></li>
<% } %></ul><%	
			} else { %>
<p align="center">No notebook pages</p>
<% } %><p align="center"><a href="notebook/addpage.jsp?id=<%= notebookid %>">Add a new notebook page</a></p>
<% 			results.close(); sth.close();
		} 
	} else { %>
<h1>Notebooks</h1>
<hr width="85%">
<% 	String sql = "SELECT n.notebook_id,n.title,COUNT(p.page),MIN(p.date_created),MAX(p.date_updated) FROM notebook n LEFT OUTER JOIN notebook_page p ON(n.notebook_id = p.notebook_id) WHERE n.username=? GROUP BY n.notebook_id ORDER BY n.notebook_id";
	PreparedStatement sth = conn.prepareStatement(sql);
	sth.setString(1, request.getRemoteUser());
	ResultSet results = sth.executeQuery();
	if ( results.first() ) { 
		results.beforeFirst();
%><ul>
<%		while ( results.next() ) {
%><li><a href="notebook.jsp?id=<%= results.getString(1) %>"><%= results.getString(2) %></a> <%= results.getInt(3) %> page(s), 
<% if ( results.getInt(3) > 0 ) {%><%= MainServlet.DATE_FORMAT.format(results.getDate(4)) %> - <%= MainServlet.DATE_FORMAT.format(results.getDate(5)) %><% } %>
</li>
<% } %></ul><%	
	} else { %>
<p align="center">No notebooks</p>
<% } %><p align="center"><a href="notebook/add.jsp">Add a new notebook</a></p>
<% results.close(); sth.close();
	} 
	conn.close();
	%>
</div>
</body>
</html>