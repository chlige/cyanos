<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.CyanosObject,
	edu.uic.orjala.cyanos.web.servlet.MainServlet,
	edu.uic.orjala.cyanos.web.BaseForm,
	edu.uic.orjala.cyanos.web.News,
	edu.uic.orjala.cyanos.User,
	java.text.SimpleDateFormat" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<script language="JAVASCRIPT" src="cyanos.js"></script>
<script type="text/javascript">
	function showLogin() {
		var cover = document.getElementById("loginCover");  
		cover.style.height = window.screen.height; 
		cover.style.visibility = "visible";
		var loginBox = document.getElementById("loginBox");
		loginBox.style.display = "block";
	}
</script>
<link rel="stylesheet" type="text/css" href="cyanos.css"/>
<title>Cyanos Database v<%= MainServlet.versionString() %></title>
</head>
<body>

<jsp:include page="/includes/menu.jsp">
<jsp:param value="<%= MainServlet.HELP_MODULE %>" name="module"/>
</jsp:include>

<% if ( request.getRemoteUser() == null ) { %>
<div id="loginCover">
</div>
<div id="loginBox" style="display:none">
<div id="loginBoxForm">
<font size='+2'>CYANOS Login</font><hr>
<form action='main-login.jsp' method='post' target='_top'>
<table>
<tr><td>Username:</td><td><input type='text' name='j_username'></td></tr>
<tr><td>Password:</td><td><input type='password' name='j_password' size='8'></td></tr>
</table>
<br>
  <button type='submit'>Login</button><button type='reset'>Clear form</button>
</form>
<p><a href='<%= response.encodeURL("reset") %>'>Reset password</a></p></div></div>
<% } %>

<div class='content'>
<div class="left25">
<div style="height:100px;">
<div class="sideModule">
<% if ( request.getRemoteUser() != null )  { 
	User aUser = (User) session.getAttribute(MainServlet.SESS_ATTR_USER);  %>
<p><b>Welcome, <%= aUser.getUserName() %></b></p>
<p><a href="self/password">Update Password</a><br>
<a href="logout.jsp">Logout</a></p>
<% } else { %>
<p><b>Welcome, Guest User</b></p>
<p><a href="login.jsp">Login</a></p>
<% } %>
</div>
</div>
</div>

<div class="right75">
<h1 style="text-align:center">Cyanos Database v<%= MainServlet.versionString() %></h1>
<hr width="85%">
<% 	News news = (News) request.getAttribute(MainServlet.ATTR_NEWS); 
	SimpleDateFormat dateFormat = (SimpleDateFormat) session.getAttribute("dateFormatter");
	if ( news != null && news.first() ) { %>
<h2 align="center">News</h2>
<dl>
<% news.beforeFirst(); while ( news.next() ) { %>
<dt><b><%= news.getSubject() %></b> - <i><%= dateFormat.format(news.getDateAdded()) %></i></dt>
<dd><%=  news.getContent().replaceAll("\n", "<BR>") %></dd>
<% } %>
</dl>
<hr width='85%'/>
<% } %>
<h2 align="center">Strain Search</h2>
<center>
<form name="strainquery" action="strain">
<table border=0>
<tr><td>Query:</td><td>
<% String queryValue = request.getParameter("query"); if ( queryValue == null ) { queryValue = ""; }%>
<input type="text" name="query" VALUE="<%= queryValue %>" ></td>
<td>
<button type='submit'>Search</button>
</td></tr>
</table>
</form>
</center>
<jsp:include page="/strain/strain-list.jsp" />
</div>

</div>
</body>
</html>