<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="edu.uic.orjala.cyanos.web.servlet.AssayServlet,
	edu.uic.orjala.cyanos.web.servlet.UploadServlet,
	edu.uic.orjala.cyanos.User,
	edu.uic.orjala.cyanos.Role,
	edu.uic.orjala.cyanos.web.Sheet" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="cyanos"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<jsp:include page="/includes/header-template.jsp"/>
<title>Assay Data Upload</title>
</head>
<body>
<jsp:include page="/includes/menu.jsp"/>
<h1>Assay Data Upload</h1>
<div class="content">
<cyanos:upload-form jspform="/assay/upload-form.jsp">
<table align="center" class="upload">
<tr style="text-align:center"><th>Assay ID</th><th>Strain ID</th><th>Location</th><th>Activity</th><th>Material ID</th><th>Sample ID</th><th>Sample Amount</th><th>Label</th><th>Concentration</th></tr>
<tr style="text-align:center"><td>Required</td><td>Required</td><td>Required</td><td>Optional</td><td>Optional</td><td>Optional</td><td>Optional</td><td>Optional</td><td>Optional</td></tr>
</table>
</cyanos:upload-form>
</div>
</body>
</html>