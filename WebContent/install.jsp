<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.web.AppConfig,
	edu.uic.orjala.cyanos.sql.SQLProject,
	edu.uic.orjala.cyanos.web.servlet.MainServlet, 
	edu.uic.orjala.cyanos.web.servlet.AdminServlet,
	edu.uic.orjala.cyanos.ConfigException,
	edu.uic.orjala.cyanos.web.listener.AppConfigListener,
	org.apache.commons.codec.binary.Base64,
	java.security.GeneralSecurityException,
	java.util.Map,
	java.security.KeyPair" %>
<!DOCTYPE html>
<html>
<head>
<%  String contextPath = request.getContextPath(); 
	String pageString = request.getParameter("page");
	Map<String,String> setupValues = (Map<String,String>) session.getAttribute(MainServlet.ATTR_SETUP_VALUES);
	AppConfig appConfig = (AppConfig) session.getAttribute(MainServlet.APP_CONFIG_ATTR);
	
	int pageNo = 0;
	try { 
		if ( pageString != null )
			pageNo = Integer.parseInt(pageString);
	} catch (NumberFormatException e) { 
		pageNo = 0;
	}
	if ( pageNo == 2 ) {
		String pass1 = request.getParameter(MainServlet.SETUP_ADMIN_PWD);
		String pass2 = request.getParameter(MainServlet.SETUP_ADMIN_PWD + "-confirm");
		if ( pass1 != null && pass1.equals(pass2) && pass1.length() > 0 ) {
			setupValues.put(MainServlet.SETUP_ADMIN_ID, request.getParameter(MainServlet.SETUP_ADMIN_ID));
			setupValues.put(MainServlet.SETUP_ADMIN_PWD, pass1);
			setupValues.put(MainServlet.SETUP_ADMIN_NAME, request.getParameter(MainServlet.SETUP_ADMIN_NAME));
			setupValues.put(MainServlet.SETUP_ADMIN_EMAIL, request.getParameter(MainServlet.SETUP_ADMIN_EMAIL));
			String[] values = request.getParameterValues(MainServlet.SETUP_ADMIN_ROLES);
			if ( values != null && values.length > 0 ) {
				StringBuffer roles = new StringBuffer();
				roles.append(values[0]);
				for ( int i = 1; i < values.length; i++ ) {
					roles.append(",");
					roles.append(values[i]);
				}
				setupValues.put(MainServlet.SETUP_ADMIN_ROLES, roles.toString());
			}
		}
	} else if ( pageNo == 3 ) {
		if ( request.getParameter(MainServlet.SETUP_DEFAULT_PATH) != null ) 
			appConfig.setFilePath("*", "*", request.getParameter(MainServlet.SETUP_DEFAULT_PATH) );
		if ( request.getParameter("genKeys") != null && appConfig.getUpdateCert() == null ) {
			try { 
				KeyPair keyPair = SQLProject.generateKeyPair();
				appConfig.setUpdateKey(SQLProject.encodePrivateKey(keyPair.getPrivate()));
				appConfig.setUpdateCert(SQLProject.encodePublicKey(keyPair.getPublic()));
			} catch (GeneralSecurityException e) {
				request.setAttribute("update_error", e);
			}
		}
	}
	
	if ( request.getAttribute("update_error") == null && request.getParameter("nextPage") != null ) {
		switch (pageNo) {
		case 1: 
			if ( setupValues.containsKey(MainServlet.SETUP_DB_VALID) ) { pageNo++; } break;
		case 2:
			if ( setupValues.containsKey(MainServlet.SETUP_ADMIN_ID) || setupValues.containsKey(MainServlet.SETUP_HAS_ADMIN) ) { pageNo++; } break;
		default: 
			pageNo++;
		}
	} else if ( request.getAttribute("update_error") == null && request.getParameter("prevPage") != null )
		pageNo--;
	
%>
<meta charset="UTF-8">
<link rel="stylesheet" type="text/css" href="<%= contextPath %>/new.css"/>
<script type="text/javascript" src="<%= contextPath %>/cyanos.js"></script>
<title>Cyanos Database - Application Setup</title>
</head>
<body>
<div id="setupPanel">
<div id="sideNav">
<ol><% 
	String[] SETUP_TITLES = { "Welcome", "Validate Database", "Setup Administrator", "Basic Configuration", "Finish" };
	for ( int i = 0; i < SETUP_TITLES.length ; i++ ) { %>
<li><% if ( i == pageNo ) { %><b><%= SETUP_TITLES[i] %></b><% } else { %><a href="?page=<%= i %>"><%= SETUP_TITLES[i] %></a><% } %></li><% } %>
</ol>
</div>

<div id="mainPanel">
<div id='contentPanel'>
<% if ( request.getParameter("uploadPage") != null ) { %>
<jsp:include page="/setup/upload.jsp"/>
<% } else if ( request.getParameter("customize") != null || request.getParameter("form") != null ) { %>
<jsp:include page="/setup/custom.jsp"/>
<% } else { %>
<form method="post" style="height:90%" action="main">
<input type="hidden" name="page" value="<%= pageNo %>">
<% if ( pageNo == 0 ) {%>
<jsp:include page="/setup/welcome.jsp"/>
<% } else if ( pageNo == 1 ) { %>
<jsp:include page="/setup/database.jsp"/>
<% } else if ( pageNo == 2 ) { %>
<jsp:include page="/setup/admin.jsp"/>
<% } else if ( pageNo == 3 ) { %>
<jsp:include page="/setup/config.jsp"/>
<% } else if ( pageNo == 4 ) { %>
<jsp:include page="/setup/finish.jsp"/>
<% } %>
</form>
<% } %>
</div>
</div>
</div>
</body>
</html>