<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
 <%@ page import="edu.uic.orjala.cyanos.web.servlet.UploadServlet,
	edu.uic.orjala.cyanos.web.Sheet,
	edu.uic.orjala.cyanos.web.SheetValue,
	java.util.List" %>   
<% 
	Sheet worksheet = UploadServlet.getActiveWorksheet(request); 
	int length = 25;
	if ( request.getParameter("length") != null ) {	length = Integer.parseInt(request.getParameter("length")); } 
	int maxLength = worksheet.rowCount();
	int colSpan = worksheet.columnCount() + 1;
	boolean showTypes = request.getParameter(UploadServlet.PARAM_SHOW_TYPE) != null;
	String[] columns = UploadServlet.getColumnList(request); 
	int sheetIndex = UploadServlet.getSelectedWorksheetIndex(request);
%><div align="center">
<p><input type="checkbox" name="<%= UploadServlet.PARAM_SHOW_TYPE %>" <%= ( showTypes ? "checked" : "") %> 
	onclick="showSpreadSheet('<%= request.getContextPath() %>/upload/status?sheet','<%= sheetIndex %>',<%= length %>, this.form.elements['<%= UploadServlet.PARAM_SHOW_TYPE %>'], this.form.elements['<%=UploadServlet.PARAM_HEADER%>'])")">
	Highlight Data Types (String, <FONT COLOR='red'>Number</FONT>, <FONT COLOR='blue'>Date/Time</FONT>)</p>
<table class="spreadsheet">
<tr><th></th><% for ( String col : columns ) { %><th><%= col %></th><% } %></tr>
<% 	int startRow = 0;
	if ( request.getParameter(UploadServlet.PARAM_HEADER) != null ) startRow = 1; 
	int rows = length;
	if ( length < 0 || length > worksheet.rowCount() ) rows = worksheet.rowCount();
	for ( int r = startRow; r < rows; r++ ) { 
%><tr><th><input type="checkbox" name="<%= UploadServlet.PARAM_ROWS %>" value="<%= r %>">(<%= r + 1 %>)</th>
<% 			worksheet.gotoRow(r);
			worksheet.beforeFirstColumn();
			while ( worksheet.nextCellInRow() ) { SheetValue value = worksheet.getValue(); 
%><td><% if ( value != null ) { 
	String color="black";
	if ( showTypes ) { 
		if ( value.isNumber() ) color = "red";
		else if ( value.isDate() ) color = "blue";
	}	
%><font color="<%= color %>"><%= value.toString() %></font>		
<% } %></td><% } %></tr><% } %></table>
<% if ( length < maxLength ) { 
		int newLength = length + 25;
		if ( maxLength < newLength ) newLength = maxLength; %>
<button type="button" onclick="showSpreadSheet('<%= request.getContextPath() %>/upload/status?sheet','<%= sheetIndex %>',<%= newLength %>, this.form.elements['<%= UploadServlet.PARAM_SHOW_TYPE %>'], this.form.elements['<%=UploadServlet.PARAM_HEADER%>'])">Show rows <%= length + 1 %> to <%= newLength  %></button>
<% }  %></div>