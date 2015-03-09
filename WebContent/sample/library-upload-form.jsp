<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="cyanos" tagdir="/WEB-INF/tags" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.web.servlet.UploadServlet,
	edu.uic.orjala.cyanos.web.upload.SampleLibraryUpload,
	edu.uic.orjala.cyanos.sql.SQLSampleCollection,
	edu.uic.orjala.cyanos.SampleCollection,
	java.util.Map, java.util.Map.Entry, java.util.HashMap,
	java.util.List, java.util.ListIterator" %>
<table class="uploadForm">
<tr><td>Source Material ID:</td><td><cyanos:sheet-columns fieldName="<%= SampleLibraryUpload.SOURCE_ID %>"/></td></tr>
<tr><td>Date:</td><td><cyanos:sheet-columns fieldName="<%= SampleLibraryUpload.LOAD_DATE %>"/></td></tr>
<tr><th colspan="2" align="center">Destination Information</th></tr>
<tr><td>Collection:</td><td>
<cyanos:sheet-columns fieldName="<%= SampleLibraryUpload.DEST_COLLECTION %>">
<option value="-1">Use collection &rarr;</option></cyanos:sheet-columns>
<select name="<%= SampleLibraryUpload.STATIC_COLLECTION  %>">
<% for ( String library : SQLSampleCollection.libraries(UploadServlet.getSQLData(request)) ) { %>
<optgroup label="<%= library %>">
<% SampleCollection collections = SQLSampleCollection.loadForLibrary(UploadServlet.getSQLData(request), library); collections.beforeFirst(); 
	while (collections.next()) { %>
<option value="<%= collections.getID() %>"><%= collections.getName() %></option><% } %>
</optgroup><% } %>
</select>
</td></tr>
<tr><td>Location:</td><td>
<cyanos:sheet-columns fieldName="<%= SampleLibraryUpload.DEST_LOCATION %>"/></td></tr>
<tr><td>Concentration (mg/mL):</td><td><cyanos:sheet-columns fieldName="<%= SampleLibraryUpload.LOAD_CONC %>"><option value="-1">Use value &rarr;</option></cyanos:sheet-columns>
<input type="text" name="<%= SampleLibraryUpload.STATIC_CONC %>" value="<c:out value="<%= request.getParameter(SampleLibraryUpload.STATIC_CONC) %>"/>"> </td></tr>
<tr><td>Amount:</td><td>
<cyanos:sheet-columns fieldName="<%= SampleLibraryUpload.LOAD_AMOUNT %>"/>
Default unit: <input type="text" size="5" name="<%= SampleLibraryUpload.LOAD_AMT_UNIT %>" value="<c:out value="<%= request.getParameter(SampleLibraryUpload.LOAD_AMT_UNIT) %>" default="mg"/>"> </td></tr>
<tr><td>Label:</td><td><cyanos:sheet-columns fieldName="<%= SampleLibraryUpload.DEST_LABEL %>"><option value="-1">SKIP ITEM</option></cyanos:sheet-columns></td></tr>
<tr><td>Project code:</td><td><cyanos:sheet-columns fieldName="<%= SampleLibraryUpload.PROJECT_COL %>"><option value="-1">Use project-&gt;</option></cyanos:sheet-columns>
<cyanos:project-popup fieldName="<%= SampleLibraryUpload.STATIC_PROJECT %>"/></td></tr>
<tr><td>Notes:</td><td><cyanos:sheet-columns fieldName="<%= SampleLibraryUpload.DEST_NOTES %>"><option value="-1">SKIP ITEM</option></cyanos:sheet-columns></td></tr>
</table>
