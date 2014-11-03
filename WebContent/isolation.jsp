<%@ taglib prefix="cyanos" tagdir="/WEB-INF/tags" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.Isolation,edu.uic.orjala.cyanos.web.servlet.CollectionServlet,java.text.SimpleDateFormat" %>
<% 	Isolation isolationObj = (Isolation) request.getAttribute(CollectionServlet.ATTR_ISOLATION); %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<cyanos:header title="Cyanos - Isolations"/>
</head>
<body style="min-height:100%">
<cyanos:menu helpModule="<%= CollectionServlet.HELP_MODULE %>"/>
<div class='content'>
<% if ( isolationObj != null && isolationObj.first() ) { %>
<p align="CENTER"><font size="+3" >Isolation <%= isolationObj.getID() %></font>
<hr width="85%">
<div id="<%= CollectionServlet.INFO_FORM_DIV_ID %>" class="main">
<jsp:include page="/isolation/isolation-form.jsp" />
</div>
<jsp:include page="/includes/loadableDiv.jsp">
<jsp:param value="<%= CollectionServlet.STRAIN_LIST_DIV_ID %>" name="loadingDivID"/>
<jsp:param value="Strains" name="loadingDivTitle"/>
</jsp:include>
<% } else { %>
<p align="CENTER"><font size="+3" >Isolation Search</font></p>
<hr width='85%'/>
<% } %>
</div>
</body>
</html>