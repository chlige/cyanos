<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="cyanos" tagdir="/WEB-INF/tags" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.web.servlet.UploadServlet,
	edu.uic.orjala.cyanos.web.upload.IsolationUpload" %>
 <p align="center">
<input type="checkbox" name="<%= IsolationUpload.FORCE_UPLOAD %>" value="true" <%= ( request.getParameter(IsolationUpload.FORCE_UPLOAD) != null ? "checked" : "" ) %>> Force upload.<br> i.e. Overwrite existing collection information.
</p>
<table class="uploadForm">
<tr><td>Isolation ID:</td><td><cyanos:sheet-columns fieldName="<%= IsolationUpload.ISOLATION_ID %>"/></td></tr>
<tr><td>Collection ID:</td><td><cyanos:sheet-columns fieldName="<%= IsolationUpload.COLLECTION_ID %>"/></td></tr>
<tr><td>Date:</td><td><cyanos:sheet-columns fieldName="<%= IsolationUpload.DATE %>"><option value="-1">SKIP ITEM</option></cyanos:sheet-columns></td></tr>
<tr><td>Parent isolation:</td><td><cyanos:sheet-columns fieldName="<%= IsolationUpload.PARENT %>"><option value="-1">SKIP ITEM</option></cyanos:sheet-columns></td></tr>
<tr><td>Type:</td><td><cyanos:sheet-columns fieldName="<%= IsolationUpload.TYPE %>"><option value="-1">SKIP ITEM</option></cyanos:sheet-columns></td></tr>
<tr><td>Media:</td><td><cyanos:sheet-columns fieldName="<%= IsolationUpload.MEDIA %>"><option value="-1">SKIP ITEM</option></cyanos:sheet-columns></td></tr>
<tr><td>Project code:</td>
<td><cyanos:sheet-columns fieldName="<%= IsolationUpload.PROJECT_COL %>">
	<option value="-1">Use project-&gt;</option>
</cyanos:sheet-columns> <cyanos:project-popup fieldName="<%= IsolationUpload.STATIC_PROJECT %>"/></td></tr>
<tr><td>Notes:</td><td>
<cyanos:sheet-columns fieldName="<%= IsolationUpload.NOTES %>"><option value="-1">SKIP ITEM</option></cyanos:sheet-columns></td></tr>
</table>
