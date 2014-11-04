<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="cyanos" %>
<%@ page import="edu.uic.orjala.cyanos.web.servlet.UploadServlet,
	edu.uic.orjala.cyanos.web.upload.UploadJob,
	edu.uic.orjala.cyanos.web.upload.SampleMoveUpload,
	edu.uic.orjala.cyanos.User,
	edu.uic.orjala.cyanos.Role" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<cyanos:header title="Cyanos - Sample Move Upload"/>
</head>
<body>
<cyanos:menu helpModule="sample"/>
<h1>Sample Move Upload</h1>
<div class="content">
<% if ( request.getParameter(UploadServlet.PARSE_ACTION) != null ) {
	UploadJob job = UploadServlet.getUploadJob(session);
	if ( job != null && job.isWorking() ) { %>
<p style="text-align: center; color: red; font-weight:bold">ERROR: Cannot start upload job.  Current upload job running.</p>		
<%	} else {
		UploadServlet.startJob(request, new SampleMoveUpload());
	}
} %><cyanos:upload-form jspform="/sample/move-upload-form.jsp">
<table align="center" class="upload">
<tr style="text-align:center"><th>Sample ID</th><th>Destination ID</th><th>Location</th></tr>
<tr style="text-align:center"><td>Required</td><td>Required</td><td>Required</td></tr>
</table>
</cyanos:upload-form>
</div>
</body>
</html>