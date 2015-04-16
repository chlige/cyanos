<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ taglib prefix="cyanos" tagdir="/WEB-INF/tags" %>
<%@ page import="java.util.Map, java.util.Map.Entry, edu.uic.orjala.cyanos.User, edu.uic.orjala.cyanos.web.servlet.MainServlet" %>
<% User aUser = MainServlet.getUser(request); %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<cyanos:header title="Cyanos Database - OpenID"/>
</head>
<body>
<div id="loginCover">
</div>
<div id="loginBox">
<div id="loginBoxForm">
<font size='+2'>CYANOS OpenID Authorization</font><hr>
<form method='post' target='_top'>
<% Map<String,String[]> params = request.getParameterMap(); 
	for ( Entry<String,String[]> param : params.entrySet() ) {
		for (String value : param.getValue() ) {
%><input type="hidden" name="<%= param.getKey() %>" value="<%= value %>"/>
<% } } %>
<button type='submit' name='auth_confirm' value="true">Approve</button><button type="submit" name='auth_confirm' value="false">Cancel</button>
</form></div>
</div>
</body>
</html>