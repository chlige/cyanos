<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="cyanos" %>
<%@ page import="edu.uic.orjala.cyanos.web.servlet.UploadServlet,
	edu.uic.orjala.cyanos.web.upload.UploadJob,
	edu.uic.orjala.cyanos.web.upload.FractionUpload,
	edu.uic.orjala.cyanos.User,
	edu.uic.orjala.cyanos.Role" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="cyanos"%>
<!DOCTYPE html>
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
<h3 style="text-align:center">Example Worksheet</h3>
<p style="margin-left:10%; margin-right:10%">The following example shows a worksheet that details two separations, one from material #1340 and one from material #893</p>
<table align="center" class="upload">
<tr style="text-align:center"><th>Material ID</th><th>Fraction Number</th><th>Amount</th><th>Label</th><th>Notes</th></tr>
<tr><td>1340</td><td>S</td><td>250.3 mg</td><td></td><td></td></tr>
<tr><td></td><td>1</td><td>12.5 mg</td><td>Test01 FR1</td><td></td></tr>
<tr><td></td><td>2</td><td>34.3 mg</td><td>Test01 FR2</td><td></td></tr>
<tr><td></td><td>3</td><td>48.0 mg</td><td>Test01 FR3</td><td></td></tr>
<tr><td></td><td>4</td><td>14.8 mg</td><td>Test01 FR4</td><td></td></tr>
<tr><td></td><td>NEW</td><td></td><td></td><td></td></tr>
<tr><td>893</td><td>S</td><td>374.9 mg</td><td></td><td></td></tr>
<tr><td></td><td>1</td><td>64.5 mg</td><td>Test02 FR1</td><td></td></tr>
<tr><td></td><td>2</td><td>38.8 mg</td><td>Test02 FR2</td><td></td></tr>
<tr><td></td><td>3</td><td>84.7 mg</td><td>Test02 FR3</td><td></td></tr>
<tr><td></td><td>4</td><td>9.0 mg</td><td>Test02 FR4</td><td></td></tr>
</table>
</cyanos:upload-form>
</div>
</body>
</html>