<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="cyanos" tagdir="/WEB-INF/tags" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.web.servlet.UploadServlet,
	edu.uic.orjala.cyanos.web.upload.FractionUpload" %>
<table class="uploadForm">
<tr><td>Date:</td><td><cyanos:calendar-field fieldName="<%= FractionUpload.DATE_KEY %>"/></td></tr>
<tr><td>Source Material ID(s):</td><td><cyanos:sheet-columns fieldName="<%= FractionUpload.SAMPLE_ID_KEY %>"><option value="-1">Use Label Column</option></cyanos:sheet-columns></td></tr>
<tr><td>Fraction #:</td><td><cyanos:sheet-columns fieldName="<%= FractionUpload.FR_NUMBER_KEY %>"/></td></tr>
<tr><td>Amount:</td><td><cyanos:sheet-columns fieldName="<%= FractionUpload.AMOUNT_KEY %>"/>
Default unit: <input type="text" name="<%= FractionUpload.DEFAULT_UNIT_KEY %>" value="<c:out value="<%= request.getParameter(FractionUpload.DEFAULT_UNIT_KEY) %>" default="mg"/>"> </td></tr>
<tr><td>Stationary Phase:</td><td><input type="text" name="<%= FractionUpload.METHOD_STATIONARY %>" value="<c:out value='<%= request.getParameter(FractionUpload.METHOD_STATIONARY) %>'/>" size="25"></td></tr>
<tr><td>Mobile Phase:</td><td><input type="text" name="<%= FractionUpload.METHOD_MOBILE %>" value="<c:out value='<%= request.getParameter(FractionUpload.METHOD_MOBILE) %>'/>" size="25"></td></tr>
<tr><td>Method:</td><td><textarea name="<%= FractionUpload.METHOD_PARAM %>" rows="5" cols="25"><c:out value='<%= request.getParameter(FractionUpload.METHOD_PARAM ) %>'/></textarea></td></tr>
<%--  
<tr><td>Separation Protocol:</td><td><select name="<%= FractionUpload.PROTOCOL_KEY %>"><option value="">NONE</option><% 		
	String selected = request.getParameter(FractionUpload.PROTOCOL_KEY);
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
<cyanos:sheet-columns fieldName="<%= FractionUpload.LABEL_KEY %>"><option value="-1">Use Format -&gt;</option></cyanos:sheet-columns>
<% 
	String selected = request.getParameter(FractionUpload.LABEL_FORMAT_KEY); if ( selected == null ) selected = "0"; 
%><select name="<%= FractionUpload.LABEL_FORMAT_KEY %>">
<option value="1" <%= (selected.equals("1") ? "selected" : "") %>>CultureID FR#</option>
<option value="2" <%= (selected.equals("2") ? "selected" : "") %>>SourceLabel.#</option>
</select>
</td></tr>
<tr><td>Project:</td><td><cyanos:project-popup fieldName="<%= FractionUpload.PROJECT_KEY %>"/></td></td>
<tr><td>Notes:</td><td><cyanos:sheet-columns fieldName="<%= FractionUpload.NOTES_KEY %>"><option value="-1">SKIP ITEM</option></cyanos:sheet-columns></td></tr>
</table>