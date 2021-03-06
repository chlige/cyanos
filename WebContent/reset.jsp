<%@ page import="edu.uic.orjala.cyanos.web.servlet.ServletObject, edu.uic.orjala.cyanos.sql.SQLMutableUser, edu.uic.orjala.cyanos.DataException"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html><head><title>Login Page</title>
<meta name="viewport" content="width=device-width">
<link REL="stylesheet" TYPE="text/css" href="<%= request.getContextPath() %>/cyanos.css"/>
</head>
<body>
<div id="loginCover" style="visibility: visible;">
</div>
<div id="loginBox">
<div id="loginBoxForm">
<h2>Password reset</h2><hr>
<% boolean displayForm = true;
if ( request.getParameter("resetPWD") != null ) { 
	try {
		SQLMutableUser.resetPassword(request, request.getParameter("username"), request.getParameter("email"));
		displayForm = false;
%><p align='center'>Password reset information sent via email to <%= request.getParameter("email")%>.</p><%
	} catch (DataException e) {
%><p align='center'><font color='red'>ERROR:</font> <%= e.getLocalizedMessage() %></p><% 
	}
} else if ( request.getParameter("username") != null && request.getParameter("token") != null ) { 
	String pwd1 = request.getParameter("pwd1");
	
	if ( request.getParameter("changePWD") != null & pwd1 != null && pwd1.length() > 0 ) {	
		if ( pwd1.equals(request.getParameter("pwd2")) ) {
			try {
				SQLMutableUser.finishReset(request.getParameter("username"), request.getParameter("token"), pwd1);
				displayForm = false;
%><p align='center' style='color: green'>Password changed</p>
<p align='center'><a href="<%= request.getContextPath() %>/login.jsp">Click here</a> to login</p><%		
			} catch (DataException e) {
%><p align='center'><font color='red'>ERROR:</font> <%= e.getLocalizedMessage() %></p><% 				
			}
		} else {
%><p align='center' style='color: red'>Password mismatch</p><%			
		}
	}
	
	if ( displayForm ) { 
%><form method='post'>
<label for="username">Username: <%= request.getParameter("username") %></label>
<input type="hidden" name="username" value="<%= request.getParameter("username") %>"><br/>
<label for="token">Update Token:</label>
<input type="text" name="token" value="<%= request.getParameter("token") %>"><br/>
<label for="pwd1">New Password:</label>
<input type='password' name='pwd1'><br/>
<label for="pwd2">Confirm Password:</label>
<input type='password' name='pwd2'><br/>
<button type='submit' name="changePWD">Set Password</button>
</form><%	}
	displayForm = false;
}
if ( displayForm ) { %>
<form method='post' target='_top'>
<label for="username">Username:</label>
<input type='text' name='username' autocorrect="off" autocapitalize="none"><br/>
<label for="email">Email Address:</label>
<input type='text' name='email'><br/>
<button type='submit' name="resetPWD">Reset Password</button>
</form>
<% } %>
</div></div>
</body></html>
