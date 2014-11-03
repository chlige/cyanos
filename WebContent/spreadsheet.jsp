<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="cyanos" tagdir="/WEB-INF/tags" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.web.servlet.UploadServlet, 
	edu.uic.orjala.cyanos.web.SpreadSheet, edu.uic.orjala.cyanos.web.Sheet, java.util.List,
	edu.uic.orjala.cyanos.web.SheetValue" %>
<% 	SpreadSheet aWKS = UploadServlet.getSpreadsheet(request);  %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<cyanos:header title="Cyanos - Uploaded Spreadsheet"/>
</head>
<body>
<cyanos:menu/>
<div class="content">
<h1>Uploaded Spreadsheet</h1>
<% if ( aWKS == null ) { %>
<p align="center"><b>No spreadsheet is currently loaded</b></p>
<% } else { %>
<form>
<p align="center"><b>Select a worksheet:</b>
<select name="<%= UploadServlet.WORKSHEET_PARAM %>" onChange="this.form.submit()">
<% 	List<String> sheets = aWKS.worksheetNames();
	int sheetIndex = UploadServlet.getSelectedWorksheetIndex(request);
	
	String selectString = request.getParameter(UploadServlet.WORKSHEET_PARAM);
	int selectedSheet = 0;
	boolean showTypes = request.getParameter(UploadServlet.PARAM_SHOW_TYPE) != null;

	Sheet worksheet = UploadServlet.getActiveWorksheet(request);
	int maxLength = worksheet.rowCount();

	for ( int i = 0; i < sheets.size(); i++ ) { %>
<option value="<%= i %>" <%= ( i == sheetIndex ? "selected"  : "") %>><%= sheets.get(i) %></option>
<% } %></select><br>
<button type="submit" name="clearUpload">Clear Uploaded Data</button></p>
<p align="center"><input type="checkbox" name="<%= UploadServlet.PARAM_SHOW_TYPE %>" <%= ( showTypes ? "checked" : "") %> onclick="this.form.submit();">
	Highlight Data Types (String, <FONT COLOR='red'>Number</FONT>, <FONT COLOR='blue'>Date/Time</FONT>)</p>
<table class="spreadsheet">
<tr><th></th><% 
	int firstIndex = 0;
	int index = 0;
	for ( int i = 0; i < worksheet.columnCount(); i++ ) {
		if ( index > ('Z' - 'A') ) {
			index = 0;
			firstIndex++;
		}
	out.print("<th>");
	if ( firstIndex > 0 ) {
		out.print((char)('A' + firstIndex - 1));		
	}
	out.print((char)('A' + index));
	out.print("</th>");
	index++;
}
	out.print("</tr>");
	int rows = worksheet.rowCount();
	for ( int r = 0; r < rows; r++ ) { 
%><tr><th><%= r + 1 %></th><% 	worksheet.gotoRow(r);
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
</form>
<% } %>
</div>
</body>
</html>