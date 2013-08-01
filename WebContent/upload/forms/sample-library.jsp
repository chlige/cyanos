<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.web.servlet.UploadServlet,
	edu.uic.orjala.cyanos.web.upload.SampleLibraryUpload,
	edu.uic.orjala.cyanos.sql.SQLData,
	edu.uic.orjala.cyanos.sql.SQLSampleCollection,
	edu.uic.orjala.cyanos.SampleCollection,
	edu.uic.orjala.cyanos.sql.SQLProject,
	edu.uic.orjala.cyanos.Project,
	java.util.Map, java.util.Map.Entry, java.util.HashMap,
	java.util.List, java.util.ListIterator" %>
<% SampleLibraryUpload job = (SampleLibraryUpload) session.getAttribute(UploadServlet.UPLOAD_JOB); 
	SQLData datasource = (SQLData) request.getAttribute(UploadServlet.DATASOURCE); 
	
if ( job != null ) { Map<String,String> template = job.getTemplate(); %>
<table>
<tr><td>Source Material ID:</td><td><select name="<%= SampleLibraryUpload.SOURCE_ID %>"><% job.genOptions(out, SampleLibraryUpload.SOURCE_ID); %></select></td></tr>
<tr><th colspan="2" align="center">Destination Information</th></tr>
<tr><td>Date:</td><td><select name="<%= SampleLibraryUpload.DEST_LOCATION %>"><% job.genOptions(out, SampleLibraryUpload.DEST_LOCATION); %></select></td></tr>

<tr><td>Collection:</td><td><select name="<%= SampleLibraryUpload.DEST_COLLECTION %>">
<option value="-1">Use collection &rarr;</option>
<% job.genOptions(out, SampleLibraryUpload.DEST_COLLECTION); %></select>
<select name="<%= SampleLibraryUpload.STATIC_COLLECTION  %>">
<% for ( String library : SQLSampleCollection.libraries(datasource) ) { %>
<optgroup label="<%= library %>">
<% SampleCollection collections = SQLSampleCollection.loadForLibrary(datasource, library); collections.beforeFirst(); 
	while (collections.next()) { %>
<option value="<%= collections.getID() %>"><%= collections.getName() %></option><% } %>
</optgroup><% } %>
</select>
</td></tr>
<tr><td>Location:</td><td><select name="<%= SampleLibraryUpload.DEST_LOCATION %>"><% job.genOptions(out, SampleLibraryUpload.DEST_LOCATION); %></select></td></tr>
<tr><td>Concentration (mg/mL):</td><td><select name="<%= SampleLibraryUpload.LOAD_CONC %>"><option value="-1">Use value &rarr;</option>
<% job.genOptions(out, SampleLibraryUpload.LOAD_CONC); %></select>
<input type="text" name="<%= SampleLibraryUpload.STATIC_CONC %>" value="<c:out value="<%= template.get(SampleLibraryUpload.STATIC_CONC) %>"/>"> 
</td></tr>
<tr><td>Amount:</td><td><select name="<%= SampleLibraryUpload.LOAD_AMOUNT %>"><% job.genOptions(out, SampleLibraryUpload.LOAD_AMOUNT); %></select>
Default unit: <input type="text" size="5" name="<%= SampleLibraryUpload.LOAD_AMT_UNIT %>" value="<c:out value="<%= template.get(SampleLibraryUpload.LOAD_AMT_UNIT) %>" default="mg"/>"> 
</td></tr>
<tr><td>Label:</td><td><select name="<%= SampleLibraryUpload.DEST_LABEL %>"><option value="-1">SKIP ITEM</option><% job.genOptions(out, SampleLibraryUpload.DEST_LABEL); %></select></td></tr>
<tr><td>Notes:</td><td><select name="<%= SampleLibraryUpload.DEST_NOTES %>"><option value="-1">SKIP ITEM</option><% job.genOptions(out, SampleLibraryUpload.DEST_NOTES); %></select></td></tr>
<tr><td>Project code:</td><td><select name="<%= SampleLibraryUpload.PROJECT_COL %>"><option value="-1">Use project-&gt;</option>
<% job.genOptions(out, SampleLibraryUpload.PROJECT_COL); %></select>
<select name="<%= SampleLibraryUpload.STATIC_PROJECT %>"><option value="">NONE</option>
<% 		
	String selected = template.get(SampleLibraryUpload.STATIC_PROJECT);
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
</table>
<% } %>