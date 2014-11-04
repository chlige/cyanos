<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="cyanos" tagdir="/WEB-INF/tags" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.web.servlet.UploadServlet,
	edu.uic.orjala.cyanos.web.upload.SampleMoveUpload,
	edu.uic.orjala.cyanos.sql.SQLSampleCollection,
	edu.uic.orjala.cyanos.SampleCollection" %>
<table class="uploadForm">
<tr><td>Sample ID:</td><td><cyanos:sheet-columns fieldName="<%= SampleMoveUpload.SAMPLE_ID %>"/></td></tr>
<tr><td>Collection:</td><td><cyanos:sheet-columns fieldName="<%= SampleMoveUpload.DEST_COLLECTION %>"><option value="-1">Use collection &rarr;</option></cyanos:sheet-columns>
<select name="<%= SampleMoveUpload.COLLECTION_FROM_DB %>">
<% for ( String library : SQLSampleCollection.libraries(UploadServlet.getSQLData(request)) ) { %>
<optgroup label="<%= library %>">
<% SampleCollection collections = SQLSampleCollection.loadForLibrary(UploadServlet.getSQLData(request), library); collections.beforeFirst(); 
	while (collections.next()) { %>
<option value="<%= collections.getID() %>"><%= collections.getName() %></option><% } %>
</optgroup><% } %>
</select>
</td></tr>
<tr><td>Location:</td><td><cyanos:sheet-columns fieldName="<%= SampleMoveUpload.DEST_LOCATION %>"/></td></tr>
</table>