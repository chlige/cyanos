<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.web.servlet.UploadServlet,
	edu.uic.orjala.cyanos.web.upload.IsolationUpload,
	edu.uic.orjala.cyanos.sql.SQLData,
	edu.uic.orjala.cyanos.sql.SQLProject,
	edu.uic.orjala.cyanos.Project,
	java.util.Map, java.util.Map.Entry, java.util.HashMap,
	java.util.List, java.util.ListIterator" %>
<%
	IsolationUpload job = (IsolationUpload) session.getAttribute(UploadServlet.UPLOAD_FORM); 
	SQLData datasource = (SQLData) request.getAttribute(UploadServlet.DATASOURCE); 
	
if ( job != null ) { Map<String,String> template = job.getTemplate();
%>
 <p align="center">
<input type="checkbox" name="<%= IsolationUpload.FORCE_UPLOAD %>" value="true" <%= ( template.containsKey(IsolationUpload.FORCE_UPLOAD) ? "checked" : "" ) %>> Force upload.<br> i.e. Overwrite existing collection information.
</p>

<table>
<tr><td>Isolation ID:</td><td><select name="<%= IsolationUpload.ISOLATION_ID %>"><% job.genOptions(out, IsolationUpload.ISOLATION_ID); %></select></td></tr>
<tr><td>Collection ID:</td><td><select name="<%= IsolationUpload.COLLECTION_ID %>"><% job.genOptions(out, IsolationUpload.COLLECTION_ID); %></select></td></tr>
<tr><td>Date:</td><td><select name="<%= IsolationUpload.DATE %>"><option value="-1">SKIP ITEM</option><% job.genOptions(out, IsolationUpload.DATE); %></select></td></tr>
<tr><td>Parent isolation:</td><td><select name="<%= IsolationUpload.PARENT %>"><option value="-1">SKIP ITEM</option><% job.genOptions(out, IsolationUpload.PARENT); %></select></td></tr>

<tr><td>Type:</td><td><select name="<%= IsolationUpload.TYPE %>"><option value="-1">SKIP ITEM</option><% job.genOptions(out, IsolationUpload.TYPE); %></select></td></tr>
<tr><td>Media:</td><td><select name="<%= IsolationUpload.MEDIA %>"><option value="-1">SKIP ITEM</option><% job.genOptions(out, IsolationUpload.MEDIA); %></select></td></tr>

<tr><td>Project code:</td><td><select name="<%= IsolationUpload.PROJECT_COL %>"><option value="-1">Use project-&gt;</option>
<% job.genOptions(out, IsolationUpload.PROJECT_COL); %></select>
<select name="<%= IsolationUpload.STATIC_PROJECT %>"><option value="">NONE</option>
<% 		
	String selected = template.get(IsolationUpload.STATIC_PROJECT);
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
<tr><td>Notes:</td><td><select name="<%= IsolationUpload.NOTES %>"><option value="-1">SKIP ITEM</option><% job.genOptions(out, IsolationUpload.NOTES); %></select></td></tr>
</table>
<% } %>