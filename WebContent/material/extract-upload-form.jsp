<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="cyanos" tagdir="/WEB-INF/tags" %>
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
<p align="center">
<input type="checkbox" name="<%= ExtractUpload.FORCE_UPLOAD %>" value="true" <%= ( request.getParameter(ExtractUpload.FORCE_UPLOAD) != null ? "checked" : "" ) %>> Force upload.<br> i.e. Overwrite existing collection information.
</p>
<table class="uploadForm">
<tr><td>Harvest ID:</td><td><%= UploadServlet.genColumnSelect(request, ExtractUpload.HARVEST_ID, null) %></td></tr>
<tr><td>Date:</td><td><%= UploadServlet.genColumnSelect(request, ExtractUpload.EXTRACT_DATE, "SKIP ITEM") %></td></tr>
<tr><td>Cell mass:</td><td><%= UploadServlet.genColumnSelect(request, ExtractUpload.HARVEST_CELL_MASS, "SKIP ITEM") %> Default unit: <input type="text" size="5" name="<%= ExtractUpload.HARVEST_CELL_UNIT %>" value="<c:out value="<%= request.getParameter(ExtractUpload.HARVEST_CELL_UNIT) %>" default="g"/>"> 
</td></tr>
<tr><td>Extract mass:</td><td><%= UploadServlet.genColumnSelect(request, ExtractUpload.EXTRACT_AMOUNT, null) %> Default unit: <input type="text" size="5" name="<%= ExtractUpload.DEFAULT_UNIT %>" value="<c:out value="<%= request.getParameter(ExtractUpload.DEFAULT_UNIT) %>" default="mg"/>"> 
</td></tr>
<tr><td>Label:</td><td><%= UploadServlet.genColumnSelect(request, ExtractUpload.EXTRACT_DATE, "SKIP ITEM") %></td></tr>
<tr><td>Project code:</td><td><%= UploadServlet.genColumnSelect(request, ExtractUpload.PROJECT_COL, "Use project-&gt;") %>
<cyanos:project-popup fieldName="<%= ExtractUpload.STATIC_PROJECT %>"/></td></tr>
<tr><td>Notes:</td><td><%= UploadServlet.genColumnSelect(request, ExtractUpload.EXTRACT_NOTES, "SKIP ITEM") %></td></tr>
</table>

<script type="text/javascript">
//<![CDATA[
	function flipProto(checkbox) {
		if ( checkbox.checked ) { showHide('protoDiv','noProtoDiv'); } else { showHide('noProtoDiv','protoDiv'); }
	}   
//]]>
</script>
<div>
<% boolean showProto =  (request.getParameter(ExtractUpload.USE_PROTOCOL) != null ); %>
<p align="center">
<input type="checkbox" NAME='<%= ExtractUpload.USE_PROTOCOL %>' VALUE='true' onClick="flipProto(this)" <%= (showProto ? "checked" : "") %>>Use an Extraction Protocol</p>
<div id="protoDiv" class="<%= showProto ? "showSection" : "hideSection" %>">
<table class="uploadForm">
<tr><td>Extract Protocol:</td><td><cyanos:sheet-columns fieldName="<%= ExtractUpload.STATIC_PROTOCOL %>">
<option value="-1">Use protocol-&gt;</option>
</cyanos:sheet-columns>
<select name="<%= ExtractUpload.STATIC_PROTOCOL %>"><option value="">NONE</option>
<% 		
	String selected = request.getParameter(ExtractUpload.STATIC_PROTOCOL);
	for ( String proto : SQLExtractProtocol.protocolNames(UploadServlet.getSQLData(request)) ) {
		out.print("<option");
		if ( proto.equals(selected) ) {
			out.print(" selected");
		}
		out.print(">");
		out.print(proto);
		out.print("</option>");
	} %></select></td></tr></table>
</div>
<div id="noProtoDiv" class="<%= showProto ? "hideSection" : "showSection" %>">
<table class="uploadForm">
<tr><td>Extract type:</td><td><%= UploadServlet.genColumnSelect(request, ExtractUpload.EXTRACT_TYPE, "SKIP ITEM") %></td></tr>
<tr><td>Extract solvent:</td><td><%= UploadServlet.genColumnSelect(request, ExtractUpload.EXTRACT_SOLVENT, "SKIP ITEM") %></td></tr>
</table>
</div>
</div>