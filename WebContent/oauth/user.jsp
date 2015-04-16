<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="java.net.URL, edu.uic.orjala.cyanos.web.servlet.OAuthServlet" %>  
<% URL url = new URL(request.getScheme(), request.getServerName(), request.getServerPort(), 
		request.getContextPath().concat("/oauth/") ); 
String userXRDS = url.toString().concat("userXrds.jsp?user=").concat(request.getParameter("user"));
response.setHeader(OAuthServlet.XRDS_HEADER, userXRDS);		
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<link rel="openid2.provider openid.server" href="<%= url.toString() %>"/>
<meta http-equiv="X-XRDS-Location" content="<%= userXRDS %>" />
</head>
<body>
This is the identity page for users of this server.
</body>
</html>