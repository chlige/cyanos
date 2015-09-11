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
	Strain strainObject = (Strain) request.getAttribute(StrainServlet.STRAIN_OBJECT);
%><!DOCTYPE html>
<html>
<head>
<cyanos:header title="Cyanos - Strains"/>
</head>
<body>
<cyanos:menu helpModule="<%= StrainServlet.HELP_MODULE %>"/>

<div class='content'>
<% if ( strainObject != null && strainObject.first() ) { %>
<p align="CENTER"><font size="+3" ><%= strainObject.getID() %> <i><c:out value="<%= strainObject.getName() %>"/></i></font>
<hr width="90%">
<div id="<%= StrainServlet.INFO_FORM_DIV_ID %>" style="width:90%">
<jsp:include page="/strain/strain-form.jsp" />
</div>

<jsp:include page="/includes/loadableDiv.jsp">
<jsp:param value="<%= StrainServlet.PHOTO_DIV_ID %>" name="loadingDivID"/>
<jsp:param value="Photos" name="loadingDivTitle"/>
</jsp:include>

<% if (request.getRemoteUser() != null )  {%>
<% 	String status = strainObject.getStatus();
if ( status != null && status.equals(Strain.FIELD_HARVEST_STATUS) ) { %>
<jsp:include page="/includes/loadableDiv.jsp">
<jsp:param value="<%= StrainServlet.COL_DIV_ID %>" name="loadingDivID"/>
<jsp:param value="Field Collections" name="loadingDivTitle"/>
</jsp:include>
<% } else { %>
<jsp:include page="/includes/loadableDiv.jsp">
<jsp:param value="<%= StrainServlet.STOCK_DIV_ID %>" name="loadingDivID"/>
<jsp:param value="Stock Cultures" name="loadingDivTitle"/>
</jsp:include>

<jsp:include page="/includes/loadableDiv.jsp">
<jsp:param value="<%= StrainServlet.INOC_DIV_ID %>" name="loadingDivID"/>
<jsp:param value="Large Scale Cultures" name="loadingDivTitle"/>
</jsp:include>

<jsp:include page="/includes/loadableDiv.jsp">
<jsp:param value="<%= StrainServlet.HARVEST_DIV_ID %>" name="loadingDivID"/>
<jsp:param value="Harvests" name="loadingDivTitle"/>
</jsp:include>

<%	Collection list = strainObject.getFieldCollections(); 
	if ( list != null && list.first() ) { %>
<jsp:include page="/includes/loadableDiv.jsp">
<jsp:param value="<%= StrainServlet.COL_DIV_ID %>" name="loadingDivID"/>
<jsp:param value="Field Collections" name="loadingDivTitle"/>
</jsp:include>
<%	} %>
<jsp:include page="/includes/loadableDiv.jsp">
<jsp:param value="<%= StrainServlet.CRYO_DIV_ID %>" name="loadingDivID"/>
<jsp:param value="Cryopreservations" name="loadingDivTitle"/>
</jsp:include>
<% } %>

<jsp:include page="/includes/loadableDiv.jsp">
<jsp:param value="<%= StrainServlet.EXTRACT_LIST_DIV_ID %>" name="loadingDivID"/>
<jsp:param value="Extracts" name="loadingDivTitle"/>
</jsp:include>

<jsp:include page="/includes/loadableDiv.jsp">
<jsp:param value="<%= StrainServlet.SEPARATION_DIV_ID %>" name="loadingDivID"/>
<jsp:param value="Separations" name="loadingDivTitle"/>
</jsp:include>

<jsp:include page="/includes/loadableDiv.jsp">
<jsp:param value="<%= StrainServlet.ASSAY_DIV_ID %>" name="loadingDivID"/>
<jsp:param value="Assays" name="loadingDivTitle"/>
</jsp:include>

<jsp:include page="/includes/loadableDiv.jsp">
<jsp:param value="<%= StrainServlet.COMPOUND_DIV_ID %>" name="loadingDivID"/>
<jsp:param value="Compounds" name="loadingDivTitle"/>
</jsp:include>

<% } } else { 
	 String queryValue = request.getParameter(StrainServlet.FIELD_QUERY); if ( queryValue == null ) { queryValue = ""; }%>
<h2 style="text-align:center">Strain Search</h2>
<hr width='85%'/>
<form name="sepquery">
<table style="border: 0px; margin-left:auto; margin-right:auto;">
<tr><td>Query:</td><td>
<input id="query" type="text" name="<%= StrainServlet.FIELD_QUERY %>" VALUE="<%= queryValue %>" >
<!-- autocomplete='off' onKeyUp="livesearch(this, 'query', 'div_query')" style='padding-bottom: 0px' -->
<div id="div_query" class='livesearch'></div></td>
<td>
<button type='SUBMIT'>Search</button>
</td></tr>
</table>
</form>
<p align="center"><a href="?query">List all strains</a></p>
<jsp:include page="/strain/strain-list.jsp">
<jsp:param value="<%= StrainServlet.SEARCH_DIV_ID %>" name="div"/>
</jsp:include>
<% } %>
</div>
</body>
</html>