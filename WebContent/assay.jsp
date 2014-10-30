<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="cyanos" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.Assay,
	edu.uic.orjala.cyanos.CyanosObject,edu.uic.orjala.cyanos.web.servlet.AssayServlet,
	edu.uic.orjala.cyanos.web.BaseForm,
	java.math.BigDecimal,
	java.util.List,
	java.text.SimpleDateFormat" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<cyanos:header title="Cyanos - Bioassays"/>
</head>
<%  String contextPath = request.getContextPath();
	Assay myAssay = (Assay) request.getAttribute(AssayServlet.ASSAY_OBJECT); 
%><body>
<cyanos:menu helpModule="assay"/>
<div class='content'>
<% if ( myAssay != null && myAssay.first() ) { %>
<p align="CENTER"><font size="+3" ><%= myAssay.getName() %></font></p>

<p align="center"><a href='assay.csv?id=<%= myAssay.getID() %>'>Export Assay Data</a></p>

<div id="<%= AssayServlet.INFO_FORM_DIV_ID %>">
<jsp:include page="/assay/assay-form.jsp" />
</div>

<DIV CLASS="collapseSection"><A NAME='<%= AssayServlet.DIV_PLATE_GRAPHIC_ID %>' CLASS='twist' onClick='loadDiv("<%= AssayServlet.DIV_PLATE_GRAPHIC_ID %>")' CLASS='divTitle'>
<IMG ALIGN="ABSMIDDLE" ID="twist_<%= AssayServlet.DIV_PLATE_GRAPHIC_ID %>" SRC="/cyanos/images/twist-closed.png" /> Plate Graphic</A>
<DIV CLASS="unloaded" ID="div_<%= AssayServlet.DIV_PLATE_GRAPHIC_ID %>"></DIV>
</DIV>

<DIV CLASS="collapseSection"><A NAME='<%= AssayServlet.DIV_DATA_LIST_ID %>' CLASS='twist' onClick='loadDiv("<%= AssayServlet.DIV_DATA_LIST_ID %>")' CLASS='divTitle'>
<IMG ALIGN="ABSMIDDLE" ID="twist_<%= AssayServlet.DIV_DATA_LIST_ID %>" SRC="/cyanos/images/twist-closed.png" /> Assay Data</A>
<DIV CLASS="unloaded" ID="div_<%= AssayServlet.DIV_DATA_LIST_ID %>"></DIV>
</DIV>

<DIV CLASS="collapseSection"><A NAME='<%= AssayServlet.DATA_FILE_DIV_ID %>' CLASS='twist' onClick='loadDiv("<%= AssayServlet.DATA_FILE_DIV_ID %>")' CLASS='divTitle'>
<IMG ALIGN="ABSMIDDLE" ID="twist_<%= AssayServlet.DATA_FILE_DIV_ID %>" SRC="/cyanos/images/twist-closed.png" /> Data Files</A>
<DIV CLASS="unloaded" ID="div_<%= AssayServlet.DATA_FILE_DIV_ID %>"></DIV>
</DIV>

<% } else { %>
<p align="CENTER"><font size="+3" >Assay Search</font>
<hr width='85%'/></p>
<center>
<form name="sepquery">
<p align="center">Target: 
<select name="target">
<option value="">All Targets</option>
<% List<String> targets = (List<String>) request.getAttribute(AssayServlet.TARGET_LIST); String currTarget = request.getParameter("target");
for ( String target: targets ) { %>
<option <%= (target.equals(currTarget) ? "selected" : "") %>><%= target %></option>
<% } %>
</select>
<button type='SUBMIT' name="assaySearch">Search</button></p>
</form>
</center>
<p align="center"><a href='assay.csv<% if ( currTarget != null && currTarget.length() > 0 ) { out.print("?target="); out.print(currTarget); } %>'>Export Assay Data</a></p>
<jsp:include page="/assay/assay-list.jsp"/>
<% } %>
</div>
</body>
</html>