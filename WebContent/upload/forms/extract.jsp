<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.web.servlet.UploadServlet,
	edu.uic.orjala.cyanos.web.upload.ExtractUpload,
	edu.uic.orjala.cyanos.web.servlet.MaterialServlet,
	edu.uic.orjala.cyanos.sql.SQLData,
	edu.uic.orjala.cyanos.sql.SQLProject,
	edu.uic.orjala.cyanos.Project,
	edu.uic.orjala.cyanos.sql.SQLExtractProtocol,
	edu.uic.orjala.cyanos.sql.SQLProtocol, java.util.Map, java.util.Map.Entry, java.util.HashMap,
	java.util.List, java.util.ListIterator" %>
<%
	ExtractUpload job = (ExtractUpload) session.getAttribute(UploadServlet.UPLOAD_FORM); 
	SQLData datasource = (SQLData) request.getAttribute(UploadServlet.DATASOURCE); 
	
if ( job != null ) { Map<String,String> template = job.getTemplate();
%>
<p align="center">
<input type="checkbox" name="<%= ExtractUpload.FORCE_UPLOAD %>" value="true" <%= ( template.containsKey(ExtractUpload.FORCE_UPLOAD) ? "checked" : "" ) %>> Force upload.<br> i.e. Overwrite existing collection information.
</p>
<table>
<tr><td>Harvest ID:</td><td><select name="<%= ExtractUpload.HARVEST_ID %>"><% job.genOptions(out, ExtractUpload.HARVEST_ID); %></select></td></tr>
<tr><td>Date:</td><td><select name="<%= ExtractUpload.EXTRACT_DATE %>"><option value="-1">SKIP ITEM</option><% job.genOptions(out, ExtractUpload.EXTRACT_DATE); %></select></td></tr>
<tr><td>Cell mass:</td><td><select name="<%= ExtractUpload.HARVEST_CELL_MASS %>"><option value="-1">SKIP ITEM</option><% job.genOptions(out, ExtractUpload.HARVEST_CELL_MASS); %></select>
Default unit: <input type="text" size="5" name="<%= ExtractUpload.HARVEST_CELL_UNIT %>" value="<c:out value="<%= template.get(ExtractUpload.HARVEST_CELL_UNIT) %>" default="g"/>"> 
</td></tr>
<tr><td>Extract mass:</td><td><select name="<%= ExtractUpload.EXTRACT_AMOUNT %>"><% job.genOptions(out, ExtractUpload.EXTRACT_AMOUNT); %></select>
Default unit: <input type="text" size="5" name="<%= ExtractUpload.DEFAULT_UNIT %>" value="<c:out value="<%= template.get(ExtractUpload.DEFAULT_UNIT) %>" default="mg"/>"> 
</td></tr>
<tr><td>Label:</td><td><select name="<%= ExtractUpload.EXTRACT_LABEL %>"><option value="-1">SKIP ITEM</option><% job.genOptions(out, ExtractUpload.EXTRACT_DATE); %></select></td></tr>
<tr><td>Project code:</td><td><select name="<%= ExtractUpload.PROJECT_COL %>"><option value="-1">Use project-&gt;</option>
<% job.genOptions(out, ExtractUpload.PROJECT_COL); %></select>
<select name="<%= ExtractUpload.STATIC_PROJECT %>"><option value="">NONE</option>
<% 		
	String selected = template.get(ExtractUpload.STATIC_PROJECT);
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
<tr><td>Notes:</td><td><select name="<%= ExtractUpload.EXTRACT_NOTES %>"><option value="-1">SKIP ITEM</option><% job.genOptions(out, ExtractUpload.EXTRACT_NOTES); %></select></td></tr>
</table>


<script type="text/javascript">
//<![CDATA[
	function flipProto(checkbox) {
		if ( checkbox.checked ) { showHide('protoDiv','nonProtoDiv'); } else { showHide('nonProtoDiv','protoDiv'); }
	}   
//]]>
</script>
<div>
<p align="center">
<input type="checkbox" NAME='useProtocol' VALUE='true' onClick="flipProto(this)" onLoad="flipProto(this)" <%= (template.containsKey(ExtractUpload.USE_PROTOCOL) ? "checked" : "") %>>Use an Extraction Protocol</p>
<div id="protoDiv">
<p align="center">Extract Protocol: 
<select name="<%= ExtractUpload.EXTRACT_PROTOCOL %>">
<option value="-1">Use protocol-&gt;</option><% job.genOptions(out, ExtractUpload.EXTRACT_PROTOCOL); %></select>
<select name="<%= ExtractUpload.STATIC_PROTOCOL %>"><option value="">NONE</option>
<% 		
	selected = template.get(ExtractUpload.STATIC_PROTOCOL);
	for ( String proto : SQLExtractProtocol.protocolNames(datasource) ) {
		out.print("<option");
		if ( proto.equals(selected) ) {
			out.print(" selected");
		}
		out.print(">");
		out.print(proto);
		out.print("</option>");
	} %></select>
</div>
<div id="noProtoDiv">
<table >
<tr><td>Extract type:</td><td><select name="<%= ExtractUpload.EXTRACT_TYPE %>"><option value="-1">SKIP ITEM</option><% job.genOptions(out, ExtractUpload.EXTRACT_TYPE); %></select></td></tr>
<tr><td>Extract solvent:</td><td><select name="<%= ExtractUpload.EXTRACT_SOLVENT %>"><option value="-1">SKIP ITEM</option><% job.genOptions(out, ExtractUpload.EXTRACT_SOLVENT); %></select></td></tr>
</table>
</div>
</div>
<% } %>