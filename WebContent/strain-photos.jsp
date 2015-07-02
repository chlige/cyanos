<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="cyanos" tagdir="/WEB-INF/tags" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.Strain,
	edu.uic.orjala.cyanos.Collection,
	edu.uic.orjala.cyanos.CyanosObject,edu.uic.orjala.cyanos.web.servlet.StrainServlet,
	edu.uic.orjala.cyanos.Material,
	edu.uic.orjala.cyanos.web.BaseForm,
	java.math.BigDecimal,
	java.text.SimpleDateFormat" %>
<%  String contextPath = request.getContextPath();
	Strain strainObject = (Strain) request.getAttribute(StrainServlet.STRAIN_OBJECT); %>
<!DOCTYPE html>
<html>
<cyanos:header title="Cyanos - Photo Browswer"/>
</head>
<body>
<cyanos:menu helpModule="<%= StrainServlet.HELP_MODULE %>"/>
<div class='content'>
<h1>Photo Browser</h1>
<hr width="90%">
<% if ( strainObject != null && strainObject.first() ) { %>
<% strainObject.beforeFirst(); 
	while ( strainObject.next() ) { String strainID = strainObject.getID(); %>
<div class="collapseSection" style="background-color:transparent;">
<a name='strain_<%= strainID %>' class='twist' onClick='loadDiv("strain_<%= strainID %>")'>
<img align="middle" id="twist_strain_<%= strainID %>" src="<%= contextPath %>/images/twist-closed.png" /></a>
<a href="strain?id=<%= strainObject.getID() %>" class="divTitle" style="font-size:16px;"> <%= strainObject.getID() %> <i><%= strainObject.getName() %></i></a>
<div class="hideSection" id="div_strain_<%= strainID %>">
<jsp:include page="/strain/strain-photos.jsp"/>
</div></div>
<% } %>
<% } %>
</div>
</body>
</html>