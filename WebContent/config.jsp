<%@ taglib prefix="cyanos" tagdir="/WEB-INF/tags" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.web.AppConfig,
edu.uic.orjala.cyanos.web.servlet.AdminServlet, 
edu.uic.orjala.cyanos.ConfigException,edu.uic.orjala.cyanos.web.listener.AppConfigListener" %>
<%  String contextPath = request.getContextPath(); %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<cyanos:header title="Cyanos - Administration"/>
</head>
<body>
<cyanos:menu/>
<div class='content'>
<h1>Application Configuration</h1>
<% AppConfig appConfig = (AppConfig) session.getAttribute(AdminServlet.APP_CONFIG_ATTR);
if ( appConfig.isUnsaved() ) { 
if ( request.getParameter(AdminServlet.PARAM_CONFIG_REVERT) != null ) {
	appConfig.loadConfig(); %>
	<div class='messages'><p><b>Configuration reloaded!</b></p></div>
<% } else if ( request.getParameter(AdminServlet.PARAM_CONFIG_SAVE) != null ) {
	try { 
	appConfig.writeConfig(); 
	AppConfigListener.reloadConfig();
	%>
	<div class='messages'><p><b>Configuration saved!</b></p></div>
<%  } catch (ConfigException e) { %>
	<div class="messages"><p><b>ERROR:</b><%= e.getLocalizedMessage() %></p></div>
<%	} } else { %>
<div class='messages'><form method='post'><p><b>Changes not saved to configuration file!</b> <button type="submit" name='<%= AdminServlet.PARAM_CONFIG_SAVE %>'>Save</button>
<button type="submit" name="<%= AdminServlet.PARAM_CONFIG_REVERT %>">Revert</button></p></form></div>
<% } } %>

<div class="tabset">
<% 	String thisForm = request.getParameter("form");
	for ( int i = 0; i < AdminServlet.CONFIG_FORMS.length; i++ ) { %>
<span<% if ( ! AdminServlet.CONFIG_FORMS[i].equals(thisForm) ) { %>><a href="?form=<%= AdminServlet.CONFIG_FORMS[i] %>"><%= AdminServlet.CONFIG_TITLES[i] %></a><% } else { %> class="selectedTab"><b><%= AdminServlet.CONFIG_TITLES[i] %></b><% } %></span>
<% } %></div>

<% if ( thisForm != null )  {%>
<% if ( thisForm.equalsIgnoreCase(AdminServlet.FORM_CONFIG_XML) ) { %>
<jsp:include page="/admin/config-load.jsp"/>
<% } else if ( thisForm.equalsIgnoreCase(AdminServlet.FORM_CONFIG_FILEPATHS) ) { %>
<jsp:include page="/admin/config-filepaths.jsp"/>
<% } else if ( thisForm.equalsIgnoreCase(AdminServlet.FORM_CONFIG_DATATYPES) ) { %>
<jsp:include page="/admin/config-datatypes.jsp"/>
<% } else if ( thisForm.equalsIgnoreCase(AdminServlet.FORM_CONFIG_QUEUES) ) { %>
<jsp:include page="/admin/config-queues.jsp"/>
<% } else if ( thisForm.equalsIgnoreCase(AdminServlet.FORM_CONFIG_MAPS) ) { %>
<jsp:include page="/admin/config-maps.jsp"/>
<% } else if ( thisForm.equalsIgnoreCase(AdminServlet.FORM_CONFIG_MODULES) ) { %>
<jsp:include page="/admin/config-modules.jsp"/>
<% } else if ( thisForm.equalsIgnoreCase(AdminServlet.FORM_CONFIG_KEYS) ) { %>
<jsp:include page="/admin/config-keys.jsp"/>
<% } %>
<% } else { %>
<jsp:include page="/admin/config-load.jsp"/>
<% } %>

</div>

</body>
</html>