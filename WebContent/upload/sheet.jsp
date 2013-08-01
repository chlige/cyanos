<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
 <%@ page import="edu.uic.orjala.cyanos.web.UploadForm,
	edu.uic.orjala.cyanos.web.servlet.UploadServlet,
	edu.uic.orjala.cyanos.web.Sheet,
	edu.uic.orjala.cyanos.web.SheetValue,
	java.util.List" %>   
<% UploadForm job = (UploadForm) session.getAttribute(UploadServlet.UPLOAD_JOB);  
	Sheet worksheet = job.getActiveWorksheet();  
	int length = 25;
	if ( request.getParameter("length") != null ) {	length = Integer.parseInt(request.getParameter("length")); } 
	int maxLength = worksheet.rowCount();
	int colSpan = worksheet.columnCount() + 1;
	boolean showTypes = request.getParameter(UploadServlet.PARAM_SHOW_TYPE) != null;
	List<String> columns = job.getHeaderList();	%>
<div align="center">
<p><input type="checkbox" name="<%= UploadServlet.PARAM_SHOW_TYPE %>" <%= ( showTypes ? "checked" : "") %> 
	onclick="showSpreadSheet('<%= request.getContextPath() %>/upload/status?sheet','<%= request.getParameter(UploadServlet.WORKSHEET_PARAM) %>',<%= length %>, this)">
	Highlight Data Types (String, <FONT COLOR='red'>Number</FONT>, <FONT COLOR='blue'>Date/Time</FONT>)</p>
<table class="spreadsheet">
<tr><th></th><% for ( String col : columns ) { %><th><%= col %></th><% } %></tr>
<% 	int startRow = 0;
	if ( job.hasHeaderRow() ) startRow = 1; 
	int rows = length;
	if ( length < 0 || length > worksheet.rowCount() ) rows = worksheet.rowCount();
	for ( int r = startRow; r < rows; r++ ) { %>
<tr><th><input type="checkbox" name="<%= UploadForm.PARAM_ROWS %>" value="<%= r %>">(<%= r + 1 %>)</th>
<% 			worksheet.gotoRow(r);
			worksheet.beforeFirstColumn();
			while ( worksheet.nextCellInRow() ) { SheetValue value = worksheet.getValue(); %><td>
<% if ( value != null ) { if ( showTypes ) { 
	if ( value.isNumber() ) { %><font color="red"><% } else if (value.isDate()) {%><font color="blue"><% } else { %><font><% } } %><%= value.toString() %><% if (showTypes ) { %></font><% } %>			
<% } %></td><% } %></tr><% } %>
</table>
<% if ( length < maxLength ) { 
		int newLength = length + 25;
		if ( maxLength < newLength ) newLength = maxLength; %>
<button type="button" onclick="showSpreadSheet('<%= request.getContextPath() %>/upload/status?sheet','<%= request.getParameter(UploadServlet.WORKSHEET_PARAM) %>',<%= newLength %>, this.form.elements['<%= UploadServlet.PARAM_SHOW_TYPE %>'], this.form.elements['<%=UploadForm.PARAM_HEADER%>'])">Show rows <%= length + 1 %> to <%= newLength  %></button>
<% }  %>
</div>
