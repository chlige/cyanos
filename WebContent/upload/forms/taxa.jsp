<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.web.servlet.UploadServlet,
	edu.uic.orjala.cyanos.web.upload.TaxaUpload,
	java.util.Map, java.util.List" %>
<%
	TaxaUpload job = (TaxaUpload) session.getAttribute(UploadServlet.UPLOAD_FORM); 	
if ( job != null ) { 
	Map<String,String> template = job.getTemplate();
%>
<table>
<tr><td>Name:</td><td><select name="<%= TaxaUpload.PARAM_NAME %>"><% job.genOptions(out, TaxaUpload.PARAM_NAME); %></select></td></tr>
<tr><td>Level:</td><td><select name="<%= TaxaUpload.PARAM_LEVEL %>"><% job.genOptions(out, TaxaUpload.PARAM_LEVEL); %></select></td></tr>
<tr><td>Parent:</td><td><select name="<%= TaxaUpload.PARAM_PARENT %>"><% job.genOptions(out, TaxaUpload.PARAM_PARENT); %></select></td></tr>
</table>
<% } %>