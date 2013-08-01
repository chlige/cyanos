<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.web.AppConfig,
edu.uic.orjala.cyanos.web.servlet.MainServlet, 
edu.uic.orjala.cyanos.web.servlet.AdminServlet,
edu.uic.orjala.cyanos.ConfigException,
edu.uic.orjala.cyanos.web.listener.AppConfigListener" %>

<div class='content'>
<h2 align="center">Custom Configuration</h2>
<div class="tabset">
<% 	String thisForm = request.getParameter("form");
	String[] forms = { AdminServlet.FORM_CONFIG_FILEPATHS, AdminServlet.FORM_CONFIG_DATATYPES, AdminServlet.FORM_CONFIG_QUEUES,
			AdminServlet.FORM_CONFIG_MAPS, AdminServlet.FORM_CONFIG_MODULES, AdminServlet.FORM_CONFIG_KEYS };
	String[] titles = { "File Paths", "File Types", "Work Queues", "Maps", "Custom Modules", "Update Keys" };
	int formIndex = 0;
	for ( int i = 0; i < forms.length; i++ ) { %>
<span<% if ( ! forms[i].equals(thisForm) ) { %>><a href="?form=<%= forms[i] %>"><%= titles[i] %></a><% } else { formIndex = i; %> class="selectedTab"><b><%= titles[i] %></b><% } %></span><% } 
%></div>

<div style="height:400px; overflow:auto; border:1px solid black; margin:5pt; padding:5pt; background-color:#F0F0F0">
<% switch ( formIndex ) {
	case 0: %><jsp:include page="../admin/config-filepaths.jsp"/><% break; 
	case 1: %><jsp:include page="../admin/config-datatypes.jsp"/><% break;
	case 2: %><jsp:include page="../admin/config-queues.jsp"/><% break; 
	case 3: %><jsp:include page="../admin/config-maps.jsp"/><% break; 
	case 4: %><jsp:include page="../admin/config-modules.jsp"/><% break; 
	case 5: %><jsp:include page="../admin/config-keys.jsp"/><% break;
	} %></div>
<form method="post" action="main">
<table class="buttons"><tr>
<td><button type="submit" name="form" <%= (formIndex > 0 ? "" : "disabled") %> value="<%= (formIndex > 0 ? forms[formIndex - 1]: forms[0]) %>">&lt; Previous</button></td>
<td><button type="submit" name="form" <%= (formIndex < 5 ? "" : "disabled") %> value="<%= (formIndex < forms.length - 1 ? forms[formIndex + 1]: forms[forms.length - 1]) %>">Next &gt;</button></td>
<td><button name="page" value="3" type="submit">Return</button></td></tr>
</table></form>
</div>