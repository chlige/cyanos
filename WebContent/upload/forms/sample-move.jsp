<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.web.servlet.UploadServlet,
	edu.uic.orjala.cyanos.web.upload.SampleMoveUpload,
	edu.uic.orjala.cyanos.sql.SQLData,	
	edu.uic.orjala.cyanos.sql.SQLSampleCollection,
	edu.uic.orjala.cyanos.SampleCollection,
	java.util.Map, java.util.Map.Entry, java.util.HashMap,
	java.util.List, java.util.ListIterator" %>
<% SampleMoveUpload job = (SampleMoveUpload) session.getAttribute(UploadServlet.UPLOAD_JOB); 
	SQLData datasource = (SQLData) request.getAttribute(UploadServlet.DATASOURCE); 
	
if ( job != null ) { Map<String,String> template = job.getTemplate(); %>
<table>
<tr><td>Sample ID:</td><td><select name="<%= SampleMoveUpload.SAMPLE_ID %>"><% job.genOptions(out, SampleMoveUpload.SAMPLE_ID); %></select></td></tr>
<tr><td>Collection:</td><td><select name="<%= SampleMoveUpload.DEST_COLLECTION %>">
<option value="-1">Use collection &rarr;</option>
<% job.genOptions(out, SampleMoveUpload.DEST_COLLECTION); %></select>
<select name="<%= SampleMoveUpload.COLLECTION_FROM_DB %>">
<% for ( String library : SQLSampleCollection.libraries(datasource) ) { %>
<optgroup label="<%= library %>">
<% SampleCollection collections = SQLSampleCollection.loadForLibrary(datasource, library); collections.beforeFirst(); 
	while (collections.next()) { %>
<option value="<%= collections.getID() %>"><%= collections.getName() %></option><% } %>
</optgroup><% } %>
</select>
</td></tr>
<tr><td>Location:</td><td><select name="<%= SampleMoveUpload.DEST_LOCATION %>"><% job.genOptions(out, SampleMoveUpload.DEST_LOCATION); %></select></td></tr>

</table>
<% } %>