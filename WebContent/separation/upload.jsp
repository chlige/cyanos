<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="cyanos" %>
<%@ page import="edu.uic.orjala.cyanos.web.servlet.UploadServlet,
	edu.uic.orjala.cyanos.web.upload.UploadJob,
	edu.uic.orjala.cyanos.web.upload.FractionUpload,
	edu.uic.orjala.cyanos.User,
	edu.uic.orjala.cyanos.Role" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="cyanos"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<cyanos:header title="Cyanos - Fraction Data Upload"/>
</head>
<body>
<cyanos:menu helpModule="separation"/>
<h1>Fraction Data Upload</h1>
<div class="content">
<% if ( request.getParameter(UploadServlet.PARSE_ACTION) != null ) {
	UploadJob job = UploadServlet.getUploadJob(session);
	if ( job != null && job.isWorking() ) { %>
<p style="text-align: center; color: red; font-weight:bold">ERROR: Cannot start upload job.  Current upload job running.</p>		
<%	} else {
		UploadServlet.startJob(request, new FractionUpload(UploadServlet.newSQLData(request)));
	}
} %><cyanos:upload-form jspform="/separation/upload-form.jsp">
<table align="center" class="upload">
<tr style="text-align:center"><th>Material ID</th><th>Fraction Number</th><th>Amount</th><th>Label</th><th>Notes</th></tr>
<tr style="text-align:center"><td>Required<BR>(for sources only)</td>
<td>Required<BR/>A number for a fraction<i>or</i><br>S = Source material</td>
<td>Required<br><i>Only absolute mass values</i></td>
<td>Optional<br>(Ignored for source materials)</td><td>Optional<br>(Ignored for source materials)</td></tr>
</table>
<p align="center"><b>NOTE:</b> A spreadsheet can contain multiple separations.  Separate each with a row where the fraction number is "NEW".</p>
</cyanos:upload-form>
</div>
</body>
</html>