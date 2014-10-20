<%@ tag language="java" pageEncoding="UTF-8"%>
<%@ tag import="edu.uic.orjala.cyanos.web.servlet.UploadServlet,
	edu.uic.orjala.cyanos.web.Sheet,
	edu.uic.orjala.cyanos.web.UploadJob,
	edu.uic.orjala.cyanos.sql.SQLData" %>
<%@ attribute name="jspform" required="true" %>
<%@ attribute name="templateType" required="false" %>
<% 	Sheet worksheet = UploadServlet.getActiveWorksheet(request);
	UploadJob job = UploadServlet.getUploadJob(session);

	if ( job != null && ( job.isWorking() || job.resultReport() != null ) ) {		
%><div align="center">
<div class="progress" style="width: 200px"><div id="progressText"></div><div id="progressBar"></div></div>
<form><button id="resultButton" name="showResults" disabled>Show Results</button></form>
</div>
<script>
	var updatePath = "<%= request.getContextPath() %>/upload/status";
	uploadStatus(updatePath, document.getElementById("resultButton"));
</script>
<%	} else if ( worksheet != null ) { 
%><form method="post">
<div id="uploadForm">
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
<button type="submit" name="clearUpload">Clear Uploaded Data</button></p>
<% SQLData datasource = UploadServlet.getSQLData(request); %>


<jsp:include page="${jspform}"/>
</div>

<div id="spreadsheet">
<jsp:include page="/upload/sheet.jsp"/>
</div>
</form>
<% } else { %>
<form method="post" enctype="multipart/form-data">
<p align="center"><b>File to upload: </b>
<input type="file" name="<%= UploadServlet.PARAM_FILE %>" size="25"/>
<button type="submit">Upload</button></p>
</form>
<p align="center"><b>Upload Instructions</b></p>
<ul style="margin-left:50px;">
<li><b>Microsoft Excel 2007 or higher</b> - Save the file as a standard <b>Excel Workbook (*.xlsx)</b>.</li>
<li><b>Microsoft Excel 2003 or earlier</b> - Save spreadsheet as a <b>XML spreadsheet (*.xml)</b> and upload the resulting .xml file.</li>
<li><b>OpenOffice 2.0 or higher</b> - Save as a standard <b>OpenOffice spreadsheet (*.ods)</b>.</li>
</ul>
<hr width='90%'><p align="center"><font size=+1><b>Worksheet Template</b></font></p>
<jsp:doBody/>
<% } %>
