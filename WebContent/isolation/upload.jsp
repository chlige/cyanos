<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="cyanos" %>
<%@ page import="edu.uic.orjala.cyanos.web.servlet.UploadServlet,
	edu.uic.orjala.cyanos.web.upload.UploadJob,
	edu.uic.orjala.cyanos.web.upload.IsolationUpload" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="cyanos"%>
<!DOCTYPE html>
<html>
<head>
<cyanos:header title="Cyanos - Isolation Data Upload"/>
</head>
<body>
<cyanos:menu helpModule="isolation"/>
<h1>Isolation Data Upload</h1>
<div class="content">
<% if ( request.getParameter(UploadServlet.PARSE_ACTION) != null ) {
	UploadJob job = UploadServlet.getUploadJob(session);
	if ( job != null && job.isWorking() ) { %>
<p style="text-align: center; color: red; font-weight:bold">ERROR: Cannot start upload job.  Current upload job running.</p>		
<%	} else {
		UploadServlet.startJob(request, new IsolationUpload(UploadServlet.newSQLData(request)));
	}
} %><cyanos:upload-form jspform="/isolation/upload-form.jsp">
<table align="center" class="upload">
<tr style="text-align:center"><th>Isolation ID</th><th>Collection ID</th><th>Date</th><th>Parent</th><th>Type</th><th>Media</th><th>Notes</th><th>Project Code</th></tr>
<tr style="text-align:center"><td>Required</td><td>Required</td><td>Optional</td><td>Optional</td><td>Optional</td><td>Optional</td><td>Optional</td><td>Optional or Static</td></tr>
</table>
</cyanos:upload-form>
</div>
</body>
</html>