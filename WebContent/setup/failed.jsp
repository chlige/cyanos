<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
<%  String contextPath = request.getContextPath(); %>
<meta charset="UTF-8">
<link rel="stylesheet" type="text/css" href="<%= contextPath %>/new.css"/>
<script type="text/javascript" src="<%= contextPath %>/cyanos.js"></script>
<title>Cyanos Database - Application Setup</title>
</head>
<body>
<div id="setupPanel">
<div id='contentPanel'>
<h1 align="center">Setup Failed</h1>
<p class="mainContent">The application could not be configured.</p>
<table class="buttons"><tr>
<td><button type="submit" name="prevPage">&lt; Previous</button></td></tr>
</table>
</div>
</div>
</body>
</html>