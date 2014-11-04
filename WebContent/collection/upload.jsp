<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="cyanos" %>
<%@ page import="edu.uic.orjala.cyanos.web.servlet.CollectionServlet,
	edu.uic.orjala.cyanos.web.servlet.UploadServlet,edu.uic.orjala.cyanos.web.upload.UploadJob,
	edu.uic.orjala.cyanos.web.upload.CollectionUpload,
	edu.uic.orjala.cyanos.User,
	edu.uic.orjala.cyanos.Role,
	edu.uic.orjala.cyanos.web.Sheet" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="cyanos"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<cyanos:header title="Cyanos - Collection Data Upload"/>
</head>
<body>
<cyanos:menu helpModule="collection"/>
<h1>Collection Data Upload</h1>
<div class="content">
<% if ( request.getParameter(UploadServlet.PARSE_ACTION) != null ) {
	UploadJob job = UploadServlet.getUploadJob(session);
	if ( job != null && job.isWorking() ) { %>
<p style="text-align: center; color: red; font-weight:bold">ERROR: Cannot start upload job.  Current upload job running.</p>		
<%	} else {
		UploadServlet.startJob(request, new CollectionUpload());
	}
} %><cyanos:upload-form jspform="/collection/upload-form.jsp">
<table align="center" class="upload">
<tr style="text-align:center"><th>Collection ID</th><th>Date</th><th>Collected by</th><th>Location Name</th><th>Latitude</th><th>Longitude</th><th>Lat/Long Precision</th><th>Notes</th></tr>
<tr style="text-align:center"><td>Required</td><td>Optional</td><td>Optional</td><td>Optional</td><td>Optional</td><td>Optional</td><td>Optional or Static</td><td>Optional</td></tr>
</table>
</cyanos:upload-form>
</div>
</body>
</html>