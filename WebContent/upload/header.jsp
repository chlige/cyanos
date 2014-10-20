<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.web.servlet.UploadServlet,
	edu.uic.orjala.cyanos.web.SpreadSheet, java.util.List" %>
<% SpreadSheet aWKS = UploadServlet.getSpreadsheet(request);
	if ( aWKS != null ) { %>
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
<button type="submit" name="<%= UploadServlet.CLEAR_SHEET_ACTION %>">Clear Uploaded Data</button>
</p>
<% } %>