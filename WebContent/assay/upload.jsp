<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="cyanos" %>
<%@ page import="edu.uic.orjala.cyanos.web.servlet.AssayServlet,
	edu.uic.orjala.cyanos.web.servlet.UploadServlet,edu.uic.orjala.cyanos.web.upload.UploadJob,
	edu.uic.orjala.cyanos.web.upload.AssayUploadJob,
	edu.uic.orjala.cyanos.User,
	edu.uic.orjala.cyanos.Role,
	edu.uic.orjala.cyanos.web.Sheet" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="cyanos"%>
<!DOCTYPE html>
<html>
<head>
<cyanos:header title="Assay Data Upload"/>
</head>
<body>
<cyanos:menu helpModule="assay"/>
<h1>Assay Data Upload</h1>
<div class="content">
<% if ( request.getParameter(UploadServlet.PARSE_ACTION) != null ) {
	UploadJob job = UploadServlet.getUploadJob(session);
	if ( job != null && job.isWorking() ) { %>
<p style="text-align: center; color: red; font-weight:bold">ERROR: Cannot start upload job.  Current upload job running.</p>		
<%	} else {
		UploadServlet.startJob(request, new AssayUploadJob(UploadServlet.newSQLData(request)));
	}
} %><cyanos:upload-form jspform="/assay/upload-form.jsp">
<table align="center" class="upload">
<tr style="text-align:center"><th>Assay ID</th><th>Strain ID</th><th>Location</th><th>Activity</th><th>Material ID</th><th>Sample ID</th><th>Sample Amount</th><th>Label</th><th>Concentration</th></tr>
<tr style="text-align:center"><td>Required</td><td>Required</td><td>Required</td><td>Optional</td><td>Optional</td><td>Optional</td><td>Optional</td><td>Optional</td><td>Optional</td></tr>
</table>
</cyanos:upload-form>
</div>
</body>
</html>