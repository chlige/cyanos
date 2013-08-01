<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.web.AppConfig,
	edu.uic.orjala.cyanos.web.servlet.MainServlet, 
	java.util.Map" %>
<div style="height:90%">
<div style="position:relative;">
<h2 align="center">Commit Setup</h2>
<% AppConfig appConfig = (AppConfig) session.getAttribute(MainServlet.APP_CONFIG_ATTR); 
	Map<String,String> setupValues = (Map<String,String>) session.getAttribute(MainServlet.ATTR_SETUP_VALUES);
	boolean valid = false;
	if ( appConfig != null ) { %>
<h3>Confirm and Commit Configuration</h3>
<ul>
<li>Database schema...
<% if ( setupValues.containsKey(MainServlet.SETUP_DB_VALID) ) { valid = true; %><font color='green'><b>VALID</b></font>
<% } else { %><font color='red'><b>INVALID</b></font><% } %></li>
<li>Administrator account...
<% if ( setupValues.containsKey(MainServlet.SETUP_ADMIN_ID) ) { out.print(setupValues.get(MainServlet.SETUP_ADMIN_ID)); 
	} else if (setupValues.containsKey(MainServlet.SETUP_HAS_ADMIN)) { %><b>Accounts already exists</b>
	<% } else { valid = false; %><font color='red'><b>INVALID</b></font><% } %></li>
</ul>
</div>
<div style="position:absolute; margin-bottom:10px; width:100%; height:60%">
<iframe src="?showConfig" width="90%" height="90%"></iframe>
<p align="center"><button name="customize" type="submit">Customize Configuration</button></p></div>
<% if ( valid ) { %>
<p class="mainContent">Confirm the settings for the application then click the <b>Commit</b> button to save the changes.</p>
<% } %>
<% } %>
</div>
<table class="buttons"><tr>
<td><button type="submit" name="prevPage">&lt; Previous</button></td>
<td><button name="finish" type="submit" <%= ( valid ? "" : "disabled" ) %>>Commit</button></td></tr>
</table>
