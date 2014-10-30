<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="cyanos" tagdir="/WEB-INF/tags" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.web.servlet.UploadServlet,
	edu.uic.orjala.cyanos.web.upload.FractionUpload,
	edu.uic.orjala.cyanos.sql.SQLData,edu.uic.orjala.cyanos.sql.SQLSeparationTemplate,
	java.util.Map, java.util.Map.Entry, java.util.HashMap,
	java.util.List, java.util.ListIterator" %>
<%
	FractionUpload job = (FractionUpload) session.getAttribute(UploadServlet.UPLOAD_FORM); 
	SQLData datasource = (SQLData) request.getAttribute(UploadServlet.DATASOURCE); 
String contextPath = request.getContextPath();
if ( job != null ) { Map<String,String> template = job.getTemplate();
%>
<table>
<tr><td>Date:</td><td><cyanos:calendar-field fieldName="<%= FractionUpload.DATE_KEY %>"/></td></tr>
<tr><td>Material ID:</td><td><select name="<%= FractionUpload.SAMPLE_ID_KEY %>"><% job.genOptions(out, FractionUpload.SAMPLE_ID_KEY); %></select></td></tr>
<tr><td>Fraction #:</td><td><select name="<%= FractionUpload.FR_NUMBER_KEY %>"><% job.genOptions(out, FractionUpload.FR_NUMBER_KEY); %></select></td></tr>
<tr><td>Amount:</td><td><select name="<%= FractionUpload.AMOUNT_KEY %>"><% job.genOptions(out, FractionUpload.AMOUNT_KEY); %></select>
Default unit: <input type="text" name="<%= FractionUpload.DEFAULT_UNIT_KEY %>" value="<c:out value="<%= template.get(FractionUpload.DEFAULT_UNIT_KEY) %>" default="mg"/>"> 
</td></tr>
<% String selected = template.get(FractionUpload.METHOD_STATIONARY); %>
<tr><td>Stationary Phase:</td><td><input type="text" name="<%= FractionUpload.METHOD_STATIONARY %>" value="<c:out value='<%= selected %>'/>" size="25"></td></tr>
<% selected = template.get(FractionUpload.METHOD_MOBILE); %>
<tr><td>Mobile Phase:</td><td><input type="text" name="<%= FractionUpload.METHOD_MOBILE %>" value="<c:out value='<%= selected %>'/>" size="25"></td></tr>
<% selected = template.get(FractionUpload.METHOD_PARAM ); %>
<tr><td>Method:</td><td><textarea name="<%= FractionUpload.METHOD_PARAM %>" rows="5" cols="25"><c:out value='<%= selected %>'/></textarea></td></tr>
<%--  
<tr><td>Separation Protocol:</td><td><select name="<%= FractionUpload.PROTOCOL_KEY %>"><option value="">NONE</option><% 		
	String selected = template.get(FractionUpload.PROTOCOL_KEY);
	for ( String proto : SQLSeparationProtocol.listProtocols(datasource) ) {
		out.print("<option");
		if ( proto.equals(selected) ) {
			out.print(" selected");
		}
		out.print(">");
		out.print(proto);
		out.print("</option>");
	}
 %></select></td></tr>
 --%>
<tr><td>Label:</td><td>
<select name="<%= FractionUpload.LABEL_KEY %>"><option value="-1">Use Format -&gt;</option><% job.genOptions(out, FractionUpload.LABEL_KEY); %></select>
<% selected = template.get(FractionUpload.LABEL_FORMAT_KEY); if ( selected == null ) selected = "0"; %>
<select name="<%= FractionUpload.LABEL_FORMAT_KEY %>">
<option value="1" <%= (selected.equals("1") ? "selected" : "") %>>CultureID FR#</option>
<option value="2" <%= (selected.equals("2") ? "selected" : "") %>>SourceLabel.#</option>
</select>
</td></tr>
<tr><td>Project:</td><td><cyanos:project-popup fieldName="<%= FractionUpload.PROJECT_KEY %>"/></td></td>
<tr><td>Notes:</td><td><select name="<%= FractionUpload.NOTES_KEY %>"><option value="-1">SKIP ITEM</option><% job.genOptions(out, FractionUpload.NOTES_KEY); %></select></td></tr>
</table>
<% } %>