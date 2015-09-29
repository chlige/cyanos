<%@ page import="edu.uic.orjala.cyanos.web.servlet.ServletObject"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="npm" tagdir="/WEB-INF/tags" %>
<!DOCTYPE html>
<html><head>
<npm:header title="Login Page"/>
<meta name="viewport" content="width=device-width">
<script type="text/javascript">
	function showLogin() {
		var cover = document.getElementById("loginCover");  
		cover.style.height = window.screen.height; 
		cover.style.visibility = "visible";
	}
</script>
</head>
<body onLoad="showLogin()">
<div id="loginCover">
</div>
<div id="loginBox">
<div id="loginBoxForm">
<h1>CYANOS Login</h1><hr>
<p style="color:red">Invalid username and/or password, please try again.</p>
<form action='j_security_check' method='post' target='_top'>
<label for="j_username">Username:</label>
<input type='text' name='j_username' autocorrect="off" autocapitalize="none" size="20"><br/>
<label for="j_password">Password:</label>
<input type='password' name='j_password' size='20'><br/>
<button type='submit'>Login</button>
</form>
<p><a href='<%= response.encodeURL("reset.jsp") %>'>Reset password</a></p></div>
<div id="loginNote" style="visibility: hidden">
<h2>Login error</h2>
<hr>
<p align="center">Your session has expired.</p>
<p align="center"><a href="">Login again.</a></p>
</div></div>
</body></html>
