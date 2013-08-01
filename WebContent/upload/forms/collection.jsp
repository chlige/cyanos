<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.web.servlet.UploadServlet,
	edu.uic.orjala.cyanos.web.upload.CollectionUpload,
	edu.uic.orjala.cyanos.sql.SQLData,
	edu.uic.orjala.cyanos.sql.SQLCollection,
	edu.uic.orjala.cyanos.sql.SQLProject,
	edu.uic.orjala.cyanos.Project,
	java.util.Map, java.util.List" %>
<% CollectionUpload job = (CollectionUpload) session.getAttribute(UploadServlet.UPLOAD_JOB); 
	SQLData datasource = (SQLData) request.getAttribute(UploadServlet.DATASOURCE); 
if ( job != null ) { Map<String,String> template = job.getTemplate(); %>
<p align="center">
<input type="checkbox" name="forceUpload" value="true" <%= ( template.containsKey(CollectionUpload.FORCE_UPLOAD) ? "checked" : "" ) %>> Force upload.<br> i.e. Overwrite existing collection information.
</p>
<table>
<tr><td>Collection ID:</td><td><select name="<%= CollectionUpload.COLLECTION_ID %>"><% job.genOptions(out, CollectionUpload.COLLECTION_ID); %></select></td></tr>
<tr><td>Date:</td><td><select name="<%= CollectionUpload.DATE %>"><option value="-1">SKIP ITEM</option><% job.genOptions(out, CollectionUpload.DATE); %></select></td></tr>
<tr><td>Collected by:</td><td><select name="<%= CollectionUpload.COLLECTOR %>"><option value="-1">SKIP ITEM</option><% job.genOptions(out, CollectionUpload.COLLECTOR); %></select></td></tr>
<tr><td>Location name:</td><td><select name="<%= CollectionUpload.LOCATION %>"><option value="-1">SKIP ITEM</option><% job.genOptions(out, CollectionUpload.LOCATION); %></select></td></tr>
<tr><td>Latitude:</td><td><select name="<%= CollectionUpload.LATITUDE %>"><option value="-1">SKIP ITEM</option><% job.genOptions(out, CollectionUpload.LATITUDE); %></select></td></tr>
<tr><td>Longitude:</td><td><select name="<%= CollectionUpload.LONGITUDE %>"><option value="-1">SKIP ITEM</option><% job.genOptions(out, CollectionUpload.LONGITUDE); %></select></td></tr>
<tr><td>Lat/Long Precision (m):</td><td><select name="<%= CollectionUpload.PRECISION %>"><option value="-1">Use value -&gt;</option><% job.genOptions(out, CollectionUpload.PRECISION); %></select>
<input type="text" name="<%= CollectionUpload.STATIC_PRECISION %>" value="<c:out value="<%= template.get(CollectionUpload.STATIC_PRECISION) %>" default=""/>"> 
</td></tr>
<tr><td>Project code:</td><td><select name="<%= CollectionUpload.PROJECT_COL %>"><option value="-1">Use project-&gt;</option>
<% job.genOptions(out, CollectionUpload.PROJECT_COL); %></select>
<select name="<%= CollectionUpload.STATIC_PROJECT %>"><option value="">NONE</option>
<% 		
	String selected = template.get(CollectionUpload.STATIC_PROJECT);
	Project project = SQLProject.projects(datasource, SQLProject.ID_COLUMN, SQLProject.ASCENDING_SORT);
	project.beforeFirst();
	while ( project.next() ) {
		out.print("<option value=\"");
		out.print(project.getID());
		out.print("\"");
		if ( project.getID().equals(selected) ) {
			out.print(" selected");
		}
		out.print(">");
		out.print(project.getName());
		out.print("</option>");
	}
 %></select></td></tr>
<tr><td>Notes:</td><td><select name="<%= CollectionUpload.NOTES %>"><option value="-1">SKIP ITEM</option><% job.genOptions(out, CollectionUpload.NOTES); %></select></td></tr>
</table>
<% } %>