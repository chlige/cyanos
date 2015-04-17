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
<% ParameterList params = (ParameterList) request.getAttribute("OAuthParams"); 
if ( params != null ) { %>
<form method='post' target='_top'>
<% 	for ( Object parmObj : params.getParameters() ) {
		Parameter param = (Parameter) parmObj;
%><input type="hidden" name="<%= param.getKey() %>" value="<%= param.getValue() %>"/>
<% } %>
<p>Access to your ID is being requested by <br/> <code><%= params.getParameterValue("openid.realm") %></code></p>
<% if ( request.getRemoteUser() != null ) { %>
<p>Current logged in as: <%= request.getRemoteUser() %></p>
<% } else { %>
<p>Please provide your username and password.</p>
<p>
<label for="j_username">Username:</label>
<input type='text' name='j_username'  autocorrect="off" autocapitalize="none"><br/>
<label for="j_password">Password:</label>
<input type='password' name='j_password' size='8'><br/>
</p>
<% } %>
<p><button type='submit' name='auth_confirm' value="true">Approve</button><button type="submit" name='auth_confirm' value="false">Cancel</button></p>
<% } %>
</form>
</div>
</div>
<script type="text/javascript">
	var cover = document.getElementById("loginCover");  
	cover.style.height = window.screen.height; 
	cover.style.visibility = "visible";
</script>
</body>
</html>