<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.web.servlet.MainServlet, 
	edu.uic.orjala.cyanos.web.listener.AppConfigListener,
	edu.uic.orjala.cyanos.web.AppConfig,
	java.sql.DriverManager,
	java.sql.Driver,
	java.util.Enumeration,
	javax.naming.InitialContext" %>
<!DOCTYPE html>
<html>
<head>
<%  String contextPath = request.getContextPath(); 
	InitialContext initCtx = new InitialContext();
	boolean jdbcSetup = ( initCtx.lookup("java:comp/env/jdbc/" + AppConfig.CYANOS_DB_NAME) == null );
	boolean mailSetup = ( MainServlet.getMailSession() == null );
%>
<meta charset="UTF-8">
<link rel="stylesheet" type="text/css" href="<%= contextPath %>/new.css"/>
<script type="text/javascript" src="<%= contextPath %>/cyanos.js"></script>
<script>
	function confirmPwds(form) {
		var pwd1 = form.elements["jdbcPwd1"];
		if ( pwd1 ) {
			var pwd2 = form.elements["jdbcPwd2"];
			if ( pwd1.value != pwd2.value ) {
				alert("Password values for JDBC login do not match!");
				return false;
			}			
		}
		
		pwd1 = form.elements["smtpPwd1"];
		if ( pwd1 ) {
			var pwd2 = form.elements["smtpPwd2"];
			if ( pwd1.value != pwd2.value ) {
				alert("Password values for SMTP login do not match!");
				return false;
			}			
		}
		return true;
	}


</script>
<title>Cyanos Database - Application Setup</title>
</head>
<body>
<div id="setupPanel">
<div id="sideNav">
</div>

<div id="mainPanel">
<div id='contentPanel'>
<h2 align="center">CYANOS Context Setup</h2>
<p class="mainContent">This appears to be a new installation of CYANOS.  You need to setup the following web application resources before CYANOS itself can be setup.</p>
<h3>Database Connection</h3>
<form method="post" style="height:90%" action="main">
<p class="mainContent">
<% if ( jdbcSetup ) { %>
Database Driver: <select name="jdbcDriver">
<% 	String selDriver = request.getParameter("jdcbDriver");
	Enumeration<Driver> driverList = DriverManager.getDrivers();
	while ( driverList.hasMoreElements() ) {
		Driver driver = driverList.nextElement();
		String name = driver.getClass().getCanonicalName();  
%><option value="<%= name %>" <%= (name.equals(selDriver) ? "selected" : "") %>><%= name %> (<%= driver.getMajorVersion() %>.<%= driver.getMinorVersion() %>)</option>
<% } %></select><br>
Database URL: <input type="text" name="jdbcURL" size="25" value="<c:out value='<%= request.getParameter("jdbcURL") %>'/>"><br>
Database Login: <input type="text" name="jdbcUser" value="<c:out value='<%= request.getParameter("jdbcUser") %>'/>"><br>
<% if ( request.getParameter("jdbcPwd1") != null ) {
	String password = request.getParameter("jdbcPwd1");
	String passConf = request.getParameter("jdbcPwd2");	
	if ( password.equals(passConf) ) { 
%><input type="hidden" name="jdbcPwdConf" value="<%= password %>">
<% } } %>Database Password: <input type="password" name="jdbcPwd1"><br>
Confirm Password: <input type="password" name="jdbcPwd2">
<% } else { 
%><font color='green'><b>ALREADY CONFIGURED</b></font>
<% } 
%></p>
<h3>Outgoing Mail Server</h3>
<p class="mainContent">
<% if ( mailSetup ) { %>
SMTP Server: <input type="text" name="smtpHost" value="<c:out value='<%= request.getParameter("smtpHost") %>'/>"><br>
Enable SSL: <input type="checkbox" name="smtpSSL" value="<%= ( request.getParameter("smtpHost") != null && request.getParameter("smtpSSL") == null ? "false" : "true" ) %>"><br>
SMTP Login: <input type="text" name="smtpUser" value="<c:out value='<%= request.getParameter("smtpUser") %>'/>"><br>
<% if ( request.getParameter("smtpPwd1") != null ) {
	String password = request.getParameter("smtpPwd1");
	String passConf = request.getParameter("smtpPwd2");	
	if ( password.equals(passConf) ) { 
%><input type="hidden" name="smtpPwdConf" value="<%= password %>">
<% } } %>SMTP Password: <input type="password" name="smtpPwd1"><br>
Confirm Password: <input type="password" name="smtpPwd2">
<% } else { 
%><font color='green'><b>ALREADY CONFIGURED</b></font>
<% } 
%></p>
<p  class="mainContent"><button type="button" name="submit" onclick="if ( confirmPwds(this.form) ) { this.form.submit(); }">Submit</button></p> 
</form>
</div>
</div>
</div>
</body>
</html>