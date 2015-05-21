 <%@ page import="edu.uic.orjala.cyanos.web.servlet.ServletObject"%>
 <%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %><%	if (request.getHeader("REFERER") != null && session != null && session.getAttribute("url") == null ) {
		String url = new String(request.getHeader("REFERER"));
		session.setAttribute("url", url);	
	}
	session.removeAttribute(ServletObject.SESS_ATTR_USER);
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html><head><title>Login Page</title>
<meta name="viewport" content="width=device-width">
<link REL="stylesheet" TYPE="text/css" href="<%= request.getContextPath() %>/cyanos.css"/>
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
<form action='j_security_check' method='post' target='_top'>
<label for="j_username">Username:</label>
<input type='text' name='j_username'  autocorrect="off" autocapitalize="none" size="20"><br/>
<label for="j_password">Password:</label>
<input type='password' name='j_password' size='20'><br/>
<button type='submit'>Login</button>
</form></div>
<div id="loginNote" style="visibility: hidden">
<h2>Login session expired</h2>
<hr>
<p align="center">Your session has expired.</p>
<p align="center"><a href="">Login again.</a></p>
</div></div>
</body></html>
