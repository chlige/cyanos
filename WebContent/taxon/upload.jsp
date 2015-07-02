<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="cyanos" %>
<%@ page import="edu.uic.orjala.cyanos.web.servlet.UploadServlet,
	edu.uic.orjala.cyanos.web.upload.UploadJob,
	edu.uic.orjala.cyanos.web.upload.TaxaUpload,
	edu.uic.orjala.cyanos.User,
	edu.uic.orjala.cyanos.Role" %>
<!DOCTYPE html>
<html>
<head>
<cyanos:header title="Cyanos - Taxonomic Data Upload"/>
</head>
<body>
<cyanos:menu helpModule="sample"/>
<h1>Taxonomic Data Upload</h1>
<div class="content">
<% if ( request.getParameter(UploadServlet.PARSE_ACTION) != null ) {
	UploadJob job = UploadServlet.getUploadJob(session);
	if ( job != null && job.isWorking() ) { %>
<p style="text-align: center; color: red; font-weight:bold">ERROR: Cannot start upload job.  Current upload job running.</p>		
<%	} else {
		UploadServlet.startJob(request, new TaxaUpload(UploadServlet.newSQLData(request)));
	}
} %><cyanos:upload-form jspform="/taxon/upload-form.jsp">
<table align="center" class="upload">
<tr style="text-align:center"><th>Name</th><th>Level</th><th>Parent"</th></tr>
<tr style="text-align:center"><td>Required</td><td>Required<br>e.g. genus, family, kingdom, etc.</td><td>Required<br>Can be blank for a root taxon.</td></tr>
</table>
</cyanos:upload-form>
</div>
</body>
</html>