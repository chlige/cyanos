<%@ tag language="java" pageEncoding="UTF-8"%>
<%@ tag import="edu.uic.orjala.cyanos.web.servlet.UploadServlet,
	edu.uic.orjala.cyanos.web.Sheet,
	edu.uic.orjala.cyanos.web.SpreadSheet,
	edu.uic.orjala.cyanos.web.UploadJob,
	edu.uic.orjala.cyanos.sql.SQLData,
	edu.uic.orjala.cyanos.sql.SQLProtocol,
	java.util.List" %>
<%@ attribute name="jspform" required="true" %>
<%@ attribute name="templateType" required="false" %>
<%
	if ( request.getParameter("clearUpload") != null) {
		UploadServlet.clearSession(session);
	} else if (request.getParameter("clearResults") != null) {
		UploadServlet.clearUploadJob(session);
	}

	Sheet worksheet = UploadServlet.getActiveWorksheet(request);
	UploadJob job = UploadServlet.getUploadJob(session);
	if (job != null && (job.isWorking() || job.resultReport() != null)) {
		if (request.getParameter(UploadServlet.SHOW_RESULTS) != null) {
			if (job.resultSheet() != null) {
				out.println("<p align='center'><a href='upload/results'>Export Results</a></p>");
			}
%><form method="post">
<p align="center"><button type="submit" name="clearUpload">Clear Uploaded Data</button>
<br><button type="submit" name="clearResults">Clear Results</button></p>
</form>
<%
		out.println(job.resultReport());  
		} else {
%>
<div align="center">
<div class="progress" style="width: 200px"><div id="progressText"></div><div id="progressBar"></div></div>
<form><button id="resultButton" name="showResults" disabled>Show Results</button></form>
</div>
<script>
	var updatePath = "<%= request.getContextPath() %>/upload/status";
	uploadStatus(updatePath, document.getElementById("resultButton"));
</script>
<%	} } else if ( worksheet != null ) { 
%><form method="post">
<div id="uploadForm">
<p align="center"><b>Select a worksheet:</b>
<select name="<%= UploadServlet.WORKSHEET_PARAM %>" onChange="this.form.submit()">
<% 	SpreadSheet aWKS = UploadServlet.getSpreadsheet(request);
	List<String> sheets = aWKS.worksheetNames();  
String selectString = request.getParameter(UploadServlet.WORKSHEET_PARAM);
int selectedSheet = 0;
if ( selectString != null ) { selectedSheet = Integer.parseInt(selectString); } 
for ( int i = 0; i < sheets.size(); i++ ) { %>
<option value="<%= i %>" <%= ( i == selectedSheet ? "selected"  : "") %>><%= sheets.get(i) %></option>
<% } %></select><br>
<button type="submit" name="clearUpload">Clear Uploaded Data</button></p>
<% SQLData datasource = UploadServlet.getSQLData(request);
	String templateType = (String) jspContext.getAttribute("templateType");
	List<String> allTemplates = SQLProtocol.listProtocols(datasource, templateType);
%><p align="center">
<input type="checkbox" name="<%=UploadServlet.PARAM_HEADER %>" value="true" onClick="this.form.submit()" <%=( request.getParameter(UploadServlet.PARAM_HEADER) != null ? "checked" : "")%>> Spreadsheet has a header row.</p>
<jsp:include page="${jspform}"/>
<p align="center">
<% String behavior = request.getParameter(UploadServlet.PARAM_ROW_BEHAVIOR);
	if ( behavior == null ) behavior = UploadServlet.ROW_BEHAVIOR_IGNORE;
%>(<input type="radio" name="<%= UploadServlet.PARAM_ROW_BEHAVIOR %>" value="<%= UploadServlet.ROW_BEHAVIOR_IGNORE %>" <%= behavior.equals(UploadServlet.ROW_BEHAVIOR_IGNORE) ? "checked" : "" %>> Ignore | 
<input type="radio" name="<%= UploadServlet.PARAM_ROW_BEHAVIOR %>" value="<%= UploadServlet.ROW_BEHAVIOR_INCLUDE %>" <%= behavior.equals(UploadServlet.ROW_BEHAVIOR_INCLUDE) ? "checked" : "" %>>Include
) selected rows.<br>
<button type="submit" name="<%=UploadServlet.PARSE_ACTION%>">Parse Upload</button></p>

</div>
<!-- <div class="collapseSection"><a class='twist' onClick='loadDiv("sheet")' class='divTitle'>
<IMG ALIGN="ABSMIDDLE" ID="twist_sheet" SRC="<%= request.getContextPath() %>/images/twist-open.png" /> SpreadSheet</a>   -->
<div class="showSection" id="spreadsheet">
<jsp:include page="/upload/sheet.jsp"/>
</div>
<!-- </div> -->
</form>
<% } else { 
%><form method="post" enctype="multipart/form-data">
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