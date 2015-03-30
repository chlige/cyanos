<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="ISO-8859-1"%>
<%@ page import="edu.uic.orjala.cyanos.web.servlet.WebDavServlet,
	edu.uic.orjala.cyanos.DataFileObject, edu.uic.orjala.cyanos.ExternalFile, java.io.File, java.util.Date" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>File List</title>
<style>
table { border-collapse: collapse; }
th { border-bottom: 1px solid black; text-align: left;}
</style>
</head>
<body>
<% Object listing = request.getAttribute(WebDavServlet.ATTR_DAV_OBJECT);
String requestURI = (String)request.getAttribute("javax.servlet.forward.request_uri");
String pathInfo = (String)request.getAttribute("javax.servlet.forward.path_info");
if (pathInfo == null ) { pathInfo = "/"; }
if ( requestURI.endsWith("/") ) { requestURI = requestURI.substring(0, requestURI.length() - 1); }
%><h1>Index of <%= pathInfo %></h1>
<table>
<tr><th style="width: 150px">Name</th><th style="width: 200px;">Last Modified</th><th style="width: 50px;">Size</th><th style="width: 200px">Description</th></tr>
<% if ( listing != null ) {
	if ( listing instanceof DataFileObject ) { 
%><tr><td><a href="<%= requestURI %>/..">Parent collection</a></td></tr><%
	DataFileObject object = (DataFileObject) listing;
	object.beforeFirst();
	while ( object.next() ) {
%><tr><td><a href="<%= requestURI %>/<%= object.getID() %>/"><%= object.getID() %></a></td><td><%= object.getDate() %></td><td></td></tr>
<% } } else if ( listing instanceof ExternalFile ) { 
	%><tr><td><a href="<%= requestURI %>/..">Parent collection</a></td></tr><%
	ExternalFile object = (ExternalFile) listing;
	object.beforeFirst();
	while ( object.next() ) {
		File file = object.getFileObject();
		Date mtime = new Date(file.lastModified());
%><tr><td><a href="<%= requestURI %>/<%= file.getName() %>"><%= file.getName() %></a></td><td><%= mtime %></td><td><%= WebDavServlet.humanSize(file.length()) %></td></tr>
<% } } else if ( listing instanceof String[] ) { 
	for (String path: (String[])listing ) {
%><tr><td><a href="<%= requestURI %>/<%= path %>/"><%= path %></a></td><td></td><td></td></tr>
<% } } else { 
%><tr><td><a href="<%= requestURI %>/..">Parent collection</a></td></tr><%
} } %>
</table>
</body>
</html>