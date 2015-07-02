<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="cyanos" %>
<%@ page import="edu.uic.orjala.cyanos.web.servlet.UploadServlet,
	edu.uic.orjala.cyanos.web.upload.UploadJob,
	edu.uic.orjala.cyanos.web.upload.ExtractUpload,
	edu.uic.orjala.cyanos.User,
	edu.uic.orjala.cyanos.Role,
	edu.uic.orjala.cyanos.web.Sheet" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="cyanos"%>
<!DOCTYPE html>
<html>
<head>
<cyanos:header title="Cyanos - Extract Data Upload"/>
</head>
<body>
<cyanos:menu helpModule="material"/>
<h1>Extract Data Upload</h1>
<div class="content">
<% if ( request.getParameter(UploadServlet.PARSE_ACTION) != null ) {
	UploadJob job = UploadServlet.getUploadJob(session);
	if ( job != null && job.isWorking() ) { %>
<p style="text-align: center; color: red; font-weight:bold">ERROR: Cannot start upload job.  Current upload job running.</p>		
<%	} else {
		UploadServlet.startJob(request, new ExtractUpload(UploadServlet.newSQLData(request)));
	}
} %><cyanos:upload-form jspform="/material/extract-upload-form.jsp">
<table align="center" class="upload">
<tr style="text-align:center"><th>Source Harvest ID</th><th>Date</th><th>Amount</th><th>Cell Mass</th><th>Label</th><th>Notes</th><th>Project Code</th></tr>
<tr style="text-align:center"><td>Required</td><td>Required</td><td>Required</td><td>Optional</td><td>Optional</td><td>Optional</td><td>Optional or Static</td></tr>
</table>
</cyanos:upload-form>
</div>
</body>
</html>