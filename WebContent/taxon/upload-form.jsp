<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="cyanos" tagdir="/WEB-INF/tags" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.web.servlet.UploadServlet,
	edu.uic.orjala.cyanos.web.upload.TaxaUpload" %>
<table class="uploadForm">
<tr><td>Name:</td><td><cyanos:sheet-columns fieldName="<%= TaxaUpload.PARAM_NAME %>"/></td></tr>
<tr><td>Level:</td><td><cyanos:sheet-columns fieldName="<%= TaxaUpload.PARAM_LEVEL %>"/></td></tr>
<tr><td>Parent:</td><td><cyanos:sheet-columns fieldName="<%= TaxaUpload.PARAM_PARENT %>"/></td></tr>
</table>