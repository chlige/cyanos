<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.web.servlet.UploadServlet,
	edu.uic.orjala.cyanos.web.BaseForm,
	edu.uic.orjala.cyanos.web.UploadModule,
	edu.uic.orjala.cyanos.web.UploadForm,
	edu.uic.orjala.cyanos.web.SpreadSheet,
	java.text.SimpleDateFormat, java.util.Map, java.util.Iterator, java.util.List" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<%  String contextPath = request.getContextPath(); %>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<script language="JAVASCRIPT" src="<%= contextPath %>/cyanos.js"></script>
<script language="JAVASCRIPT" src="<%= contextPath %>/cyanos-date.js"></script>
<link rel="stylesheet" type="text/css" href="<%= contextPath %>/cyanos.css"/>
<title>Cyanos Database - Upload</title>
</head>
<body>

<jsp:include page="/includes/menu.jsp"/>


<div class="content">
<h1>Upload Data</h1>
<hr width="90%">
<% UploadModule form = (UploadModule) session.getAttribute(UploadServlet.UPLOAD_JOB); 
	if ( form != null ) { %>
<div style="width:90%; margin-left:auto; margin-right:auto">
<h2 style="text-align:center"><%= form.title() %></h2>
<% if ( form.isAllowed(request) ) { 
	if ( request.getParameter("clearResults") != null ) {
		form.clearResults();
	}
	if (request.getParameter(UploadServlet.SHOW_RESULTS) != null ) { 
		if ( form.resultSheet() != null ) {
				out.println("<p align='center'><a href='upload/results'>Export Results</a></p>");
		} %>
<form method="post" action="<%= request.getContextPath() %>/upload">
<p align="center"><button type="submit" name="clearUpload">Clear Uploaded Data</button>
<br><button type="submit" name="clearResults">Clear Results</button></p>
</form>
<% out.println(form.resultReport()); 
} else if ( form.isWorking() || form.resultReport() != null || request.getParameter(UploadServlet.PARSE_ACTION) != null ) { %>
<div align="center">
<div class="progress" style="width: 200px"><div id="progressText"></div><div id="progressBar"></div></div>
<form><button id="resultButton" name="showResults" disabled>Show Results</button></form>
</div>
<script>
	var updatePath = "<%= contextPath %>/upload/status";
	uploadStatus(updatePath, document.getElementById("resultButton"));
</script>
<% } else { 
	SpreadSheet aWKS = (SpreadSheet) session.getAttribute(UploadServlet.SPREADSHEET);
	if ( aWKS != null ) {  String jspFile = form.jspForm(); 
	if ( jspFile != null ) { form.updateTemplate(request); %>
<form method="post" action="<%= request.getContextPath() %>/upload">	
<p align="center"><b>Select a worksheet:</b>
<select name="<%= UploadServlet.WORKSHEET_PARAM %>" onChange="this.form.submit()">
<% List<String> sheets = aWKS.worksheetNames();  
String selectString = request.getParameter(UploadServlet.WORKSHEET_PARAM);
int selectedSheet = 0;
if ( selectString != null ) { selectedSheet = Integer.parseInt(selectString); } 
for ( int i = 0; i < sheets.size(); i++ ) { %>
<option value="<%= i %>" <%= ( i == selectedSheet ? "selected"  : "") %>><%= sheets.get(i) %></option>
<% } %>
</select><br>
<button type="submit" name="clearUpload">Clear Uploaded Data</button>
</p>
<% if ( form.getActiveWorksheet() != null ) { %>	
<div id="<%= UploadServlet.TEMPLATE_DIV %>">
<jsp:include page="/upload/template.jsp"/>
</div>
<div id="spreadsheet">
<jsp:include page="/upload/sheet.jsp"/>
</div>
</form>
<% } } else { %><p align="center"><b>NO FORM SPECIFIED</b></p>
<% } } else { %><form method="post" enctype="multipart/form-data" action="<%= request.getContextPath() %>/upload">
<p align="center">
<b>File to upload: </b>
<input type="file" name="xmlFile" size="25"/>
<button type="submit">Upload</button></p></form>
<form method="post" action="<%= request.getContextPath() %>/upload">
<p align="center"><button type="submit" name="clearUpload">Return to Upload Menu</button></p>
</form>
<p align="center"><b>Upload Instructions</b></p>
<ul>
<li><b>Microsoft Excel 2007 or higher</b> - Save the file as a standard <b>Excel Workbook (*.xlsx)</b>.</li>
<li><b>Microsoft Excel 2003 or earlier</b> - Save spreadsheet as a <b>XML spreadsheet (*.xml)</b> and upload the resulting .xml file.</li>
<li><b>OpenOffice 2.0 or higher</b> - Save as a standard <b>OpenOffice spreadsheet (*.ods)</b>.</li>
</ul>
<hr width='100%'><p align="center"><font size=+1><b>Worksheet Template</b></font></p>
<%= form.worksheetTemplate() %>
<% } } } else { session.removeAttribute(UploadServlet.UPLOAD_JOB);  %><h1 style="color: red;">ACCESS DENIED</h1><% } %></div><% } else { %>		
<% String module = request.getRequestURI(); %>
<div style="width:90%; margin-left:auto; margin-right:auto">
<h2>Please select an upload form</h2>
<div style="margin-left:5%">
<h3>Culture Data</h3>
<ul type="none">
<li><a href="?module=<%= UploadServlet.COLLECTION_UPLOAD_MODULE %>">Field Collection Data</a></li>
<li><a href="?module=<%= UploadServlet.ISOLATION_UPLOAD_MODULE %>">Strain Isolation Data</a></li>
<li><a href="?module=<%= UploadServlet.TAXA_UPLOAD_MODULE %>">Taxanomic Data</a></li>
</ul>

<h3>Bioassay Data</h3>
<ul type="none">
<li><a href="?module=<%= UploadServlet.ASSAY_UPLOAD_MODULE %>">Bioassay Data</a></li>
</ul>

<h3>Extract, Fraction, and Sample Data</h3>
<ul type="none">
<li><a href="?module=<%= UploadServlet.FRACTION_UPLOAD_MODULE %>">Separation Data</a></li>
<li><a href="?module=<%= UploadServlet.EXTRACT_UPLOAD_MODULE %>">Extract Data</a></li>
<li><a href="?module=<%= UploadServlet.SAMPLE_LIBRARY_UPLOAD_MODULE %>">Sample Library Data</a></li>
<li><a href="?module=<%= UploadServlet.SAMPLE_MOVE_UPLOAD_MODULE %>">Move Samples</a></li>
</ul>
<% Map<String,Class<UploadModule>> uploadModules = (Map<String,Class<UploadModule>>) application.getAttribute(UploadServlet.CUSTOM_UPLOAD_MODULES); 
	if ( uploadModules != null && uploadModules.size() > 0 ) {  %>
<h3>Custom Upload Modules</h3>
<ul type="none">
<% Iterator<Class<UploadModule>> anIter = uploadModules.values().iterator();
	while ( anIter.hasNext() ) {
		Class<UploadModule> aClass = anIter.next(); %>
<li><a href="?module=custom-<%= aClass.getName() %>"><%= aClass.getSimpleName() %></a></li>
<% } %>
</ul></div></div>
<% } } %>
</div>
</body>
</html>