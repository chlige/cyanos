<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="cyanos" tagdir="/WEB-INF/tags" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.web.servlet.UploadServlet,
	edu.uic.orjala.cyanos.web.BaseForm,
	edu.uic.orjala.cyanos.web.UploadModule,
	edu.uic.orjala.cyanos.web.UploadForm,
	edu.uic.orjala.cyanos.web.SpreadSheet,
	java.text.SimpleDateFormat, java.util.Map, java.util.Iterator, java.util.List" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<%  String contextPath = request.getContextPath(); %>
<head>
<cyanos:header title="Cyanos - Upload"/>
</head>
<body>
<cyanos:menu/>
<div class="content">
<h1>Upload Data</h1>
<hr width="90%">
<div style="width:90%; margin-left:auto; margin-right:auto">
<h2>Please select an upload form</h2>
<div style="margin-left:5%">
<h3>Culture Data</h3>
<ul type="none">
<li><a href="<%= contextPath %>/collection/upload.jsp">Field Collection Data</a></li>
<li><a href="<%= contextPath %>/isolation/upload.jsp">Strain Isolation Data</a></li>
<li><a href="<%= contextPath %>/taxon/upload.jsp">Taxanomic Data</a></li>
</ul>

<h3>Bioassay Data</h3>
<ul type="none">
<li><a href="<%= contextPath %>/assay/upload.jsp">Bioassay Data</a></li>
</ul>

<h3>Extract, Fraction, and Sample Data</h3>
<ul type="none">
<li><a href="<%= contextPath %>/separation/upload.jsp">Separation Data</a></li>
<li><a href="material/upload.jsp">Extract Data</a></li>
<li><a href="sample/upload.jsp">Sample Library Data</a></li>
<li><a href="smaple/move-upload.jsp">Move Samples</a></li>
</ul>
</div></div></div>
</body>
</html>