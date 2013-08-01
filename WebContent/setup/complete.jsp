<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<%  String contextPath = request.getContextPath(); %>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<link rel="stylesheet" type="text/css" href="<%= contextPath %>/new.css"/>
<script type="text/javascript" src="<%= contextPath %>/cyanos.js"></script>
<title>Cyanos Database - Application Setup</title>
</head>
<body>
<div id="setupPanel">
<div id='contentPanel'>
<h1 align="center">Setup Complete</h1>
<p class="mainContent">The CYANOS web application is configured for use.<br>
The web application configuration can be further modified through the "Manage config" option in the "Cyanos" menu.</p>
<p><b>NOTICE:</b> To ensure that the configuration settings are properly updated, restart the web application.</p>
</div>
</div>
</body>
</html>