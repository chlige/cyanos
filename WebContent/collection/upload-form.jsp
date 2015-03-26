<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="cyanos" tagdir="/WEB-INF/tags" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.web.servlet.UploadServlet,
	edu.uic.orjala.cyanos.web.upload.CollectionUpload,
	edu.uic.orjala.cyanos.sql.SQLData,
	edu.uic.orjala.cyanos.sql.SQLCollection,
	edu.uic.orjala.cyanos.sql.SQLProject,
	edu.uic.orjala.cyanos.Project,
	java.util.Map, java.util.List" %>
<p align="center">
<input type="checkbox" name="forceUpload" value="true" <%= ( request.getParameter(CollectionUpload.FORCE_UPLOAD) != null ? "checked" : "" ) %>> Force upload. (i.e. Overwrite existing collection information.)
</p>
<table class="uploadForm">
<tr><td>Collection ID:</td><td><%= UploadServlet.genColumnSelect(request, CollectionUpload.COLLECTION_ID, null) %></td></tr>
<tr><td>Date:</td><td><%= UploadServlet.genColumnSelect(request, CollectionUpload.DATE, "SKIP ITEM") %></td></tr>
<tr><td>Collected by:</td><td><%= UploadServlet.genColumnSelect(request, CollectionUpload.COLLECTOR, "SKIP ITEM") %></td></tr>
<tr><td>Location name:</td><td><%= UploadServlet.genColumnSelect(request, CollectionUpload.LOCATION, "SKIP ITEM") %></td></tr>
<tr><td>Latitude:</td><td><%= UploadServlet.genColumnSelect(request, CollectionUpload.LATITUDE, "SKIP ITEM") %></td></tr>
<tr><td>Longitude:</td><td><%= UploadServlet.genColumnSelect(request, CollectionUpload.LONGITUDE, "SKIP ITEM") %></td></tr>
<tr><td>Lat/Long Precision (m):</td><td><%= UploadServlet.genColumnSelect(request, CollectionUpload.PRECISION, "Use value -&gt;") %>
<input type="text" name="<%= CollectionUpload.STATIC_PRECISION %>" value="<c:out value="<%= request.getParameter(CollectionUpload.STATIC_PRECISION) %>" default=""/>"> 
</td></tr>
<tr><td>Project code:</td><td><%= UploadServlet.genColumnSelect(request, CollectionUpload.PROJECT_COL, "Use project-&gt;") %>
<cyanos:project-popup fieldName="<%= CollectionUpload.STATIC_PROJECT %>"/></td></tr>
<tr><td>Notes:</td><td><%= UploadServlet.genColumnSelect(request, CollectionUpload.NOTES, "SKIP ITEM") %></td></tr>
</table>