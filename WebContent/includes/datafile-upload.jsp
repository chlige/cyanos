<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.BasicObject,
	edu.uic.orjala.cyanos.DataFileObject,
	edu.uic.orjala.cyanos.web.servlet.DataFileServlet,	
	edu.uic.orjala.cyanos.Role,
	edu.uic.orjala.cyanos.ExternalFile,
	edu.uic.orjala.cyanos.web.BaseForm,
	edu.uic.orjala.cyanos.DataException,
	java.text.SimpleDateFormat,
	java.io.File, java.util.Map" %>
<% String contextPath = request.getContextPath();
	String message = (String) request.getAttribute(DataFileServlet.ATTR_UPLOAD_MESSAGE);
	%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<script language="JAVASCRIPT" src="<%= contextPath %>/cyanos.js"></script>
<link rel="stylesheet" type="text/css" href="<%= contextPath %>/cyanos.css"/>
</head>
<body style="background-color: #FFFCE6" onLoad="refreshFM('objectFM','embedFM')">
<div align="center" style="bgcolor: #FFFCE6">
<% if ( message != null ) { %>
<p align="center"><b><%= message %></b></p>
<% } %>
<form name="photoBrowser" method="post" enctype="multipart/form-data" >
<input type="hidden" name="<%= DataFileServlet.DATAFILE_CLASS %>" value="<%= request.getParameter(DataFileServlet.DATAFILE_CLASS) %>">
<input type="hidden" name="<%= DataFileServlet.PARAM_DATATYPE %>" value="<%= request.getParameter(DataFileServlet.PARAM_DATATYPE) %>">
<input type="hidden" name="filePath" value="/">
<p align="center">Upload a new file: 
<input type="file" name="newFile">
<button name="uploadFile" type="submit" onClick="getCurrentFMPath('objectFM','embedFM',this.form.filePath)">Upload</button></p>
</form>
</div>
</body>
</html>