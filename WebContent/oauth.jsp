<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ taglib prefix="cyanos" tagdir="/WEB-INF/tags" %>
<%@ page import="org.openid4java.message.ParameterList, org.openid4java.message.Parameter, edu.uic.orjala.cyanos.User, edu.uic.orjala.cyanos.web.servlet.MainServlet" %>
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
<% ParameterList params = (ParameterList) request.getAttribute("OAuthParams"); 
	for ( Object parmObj : params.getParameters() ) {
		Parameter param = (Parameter) parmObj;
%><input type="hidden" name="<%= param.getKey() %>" value="<%= param.getValue() %>"/>
<% } %>
Access to your ID is being requested by <%= params.getParameterValue("openid.realm") %><br>
<button type='submit' name='auth_confirm' value="true">Approve</button><button type="submit" name='auth_confirm' value="false">Cancel</button>
</form></div>
</div>
</body>
</html>