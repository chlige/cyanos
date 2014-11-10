<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="cyanos" %>
<%@ page import="edu.uic.orjala.cyanos.Compound,edu.uic.orjala.cyanos.web.servlet.DereplicationServlet" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<cyanos:header title="Cyanos - Compound Dereplication"/>
</head>
<body>
<cyanos:menu helpModule="derplication"/>
<% String contextPath = request.getContextPath();
	if (request.getParameter("rebuildGraph") != null ) { %>

<% } else { 
	boolean performSearch = request.getParameter("searchAction") != null;
%><h1>Compound Dereplication</h1>
<hr width="90%">
<div class="searchNav">

<div class="collapseSection"><a class='twist' onClick='loadDiv("search")'>
<img align="absmiddle" id="twist_search" src="<%= contextPath %>/images/twist-<%= performSearch ? "closed" : "open" %>.png" /> Search Form</a>
<div class="<%= performSearch ? "hide" : "show" %>Section" id="div_search"></div>
</div>
</div>
<% if ( performSearch ) { 
	StringBuffer query = DereplicationServlet.getQuery(request);
	StringBuffer join = DereplicationServlet.getJoinBuffer(request);
	
	

%>

<% } } %>
</body>
</html>