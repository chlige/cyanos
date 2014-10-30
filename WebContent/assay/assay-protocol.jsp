<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="cyanos" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.Assay.AssayTemplate,
	edu.uic.orjala.cyanos.Assay,
	edu.uic.orjala.cyanos.CyanosObject,
	edu.uic.orjala.cyanos.web.servlet.AssayServlet,
	edu.uic.orjala.cyanos.web.BaseForm,
	java.util.List,java.util.ListIterator,
	java.math.BigDecimal" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<script language="JAVASCRIPT" src="../cyanos.js"></script>
<link rel="stylesheet" type="text/css" href="../cyanos.css"/>
<title>Assay Protocols</title>
</head>
<body>
<cyanos:menu helpModule="assay"/>
<div class='content'>
<h1 style="text-align:center">Assay Protocols</h1>
<hr width="85%">
<%
	AssayTemplate myObj = (AssayTemplate) request.getAttribute(AssayServlet.PROTOCOL_OBJ);
%>
<% if ( myObj != null ) { 
	boolean update = request.getParameter(AssayServlet.UPDATE_ACTION) != null; %>
<form method="post" action="protocol">
<% if ( (! update ) && request.getParameter("createProtocol") != null ) { %><input type="hidden" name="createProtocol"><% } %>
<table class="species" align='center'>
<tr><td>Name:</td><td><%= myObj.getName() %><input type="hidden" name="name" value="<%= myObj.getName() %>"></td></tr>
<tr<% if ( update ) { 
	String value = request.getParameter("target");
	if (value != null && (! value.equals(myObj.getTarget() ) ) ) {
		myObj.setTarget(value);	
 %> class="updated" <% } } %>><td>Target:</td><td><input type="text" name="target" value="<c:out value="<%= myObj.getTarget() %>"/>"></td></tr>
<tr<%
if ( update ) {
	String value = request.getParameter("unit");
	if (  value != null && (! value.equals(myObj.getUnitFormat())) ) {
		myObj.setUnitFormat(value);
		out.print("class=\"updated\"");
	}
	
}
%>><td>Unit Format:</td><td><input name="unit" value="<c:out value="<%= myObj.getUnitFormat() %>"/>"></td></tr>
<tr<%
if ( update ) {
	String value = request.getParameter("active_op");
	boolean updated = false;
	if ( value != null && (! value.equals(myObj.getActiveOperator())) ) {
		updated = true;
		myObj.setActiveOperator(value);
	}
	value = request.getParameter("active_level");
	BigDecimal active = myObj.getActiveLevel();
	String compareValue = ( active != null ? active.toPlainString() : null );
	if ( value != null && (! value.equals(compareValue)) ) {
		updated = true;
		myObj.setActiveLevel(new BigDecimal(value));
	}
	if ( updated ) {
		out.print("class=\"updated\"");
	}
} 
%>><tr><td>Activity Threshold:</td><td>
<% String activeOp = myObj.getActiveOperator(); 
%><select name="active_op">
<% String[] operators = { Assay.OPERATOR_LESS_THAN, Assay.OPERATOR_LESS_EQUAL, Assay.OPERATOR_EQUAL, 
		Assay.OPERATOR_NOT_EQUAL, Assay.OPERATOR_GREATER_EQUAL, Assay.OPERATOR_GREATER_THAN}; 
for ( String op : operators ) { 
%><option value="<%= op %>" <%= (op.equals(activeOp) ? "selected" : "") %>><%= AssayServlet.getOperatorText(op) %></option>
<% } 
%></select><input type="text" name="active_level" value="<c:out value="<%= myObj.getActiveLevel() %>"/>"></td></tr>
<tr<% 
if ( update ) {
	String value = request.getParameter("size");
	if ( value != null && (! value.equals(myObj.getSize())) ) {
		myObj.setSize(value);
		out.print("class=\"updated\"");	
	}
}
%>><td>Size:</td><td><% String size =  myObj.getSize(); %>
<select name="size">
<% 	int[] lengths = { 4, 8, 8, 16, 32 };  
	int[] widths = { 6, 6, 12, 24, 48 }; 
	for ( int i = 0; i < lengths.length; i++ ) { 
	String thisSize = String.format("%dx%d", lengths[i], widths[i]); 
%><option value="<%= thisSize %>" <%= ( thisSize.equals(size) ? "selected" : "") %>><%= String.format("%d wells (%s)", lengths[i] * widths[i], thisSize) %></option>
<% } %>
</select></td></tr>

<% if ( update ) { myObj.save(); } %>
<tr><td colspan="2" align="CENTER"><button type="submit" name="<%= AssayServlet.UPDATE_ACTION %>">Save</button>
<button type="reset">Reset</button></td></tr>
<tr>
<td colspan="2" align="center">
<% if ( request.getParameter("delete") != null ) { %>
<b>CONFIRM DELETION</b><br>
<button type="submit" name="confirmDelete">Confirm</button><button type="submit">Cancel</button>
<% } else { %>
<button type="submit" name="delete">Delete</button>
<% } %>
</td>
</tr>
</table>
</form>
<% } List<String> allProtocols = (List<String>) request.getAttribute(AssayServlet.ALL_PROTOCOLS); 
if ( allProtocols.size() > 0 ) { ListIterator<String> iter = allProtocols.listIterator(); %>
<p align='center'><b><u>Protocols</u></b><br>
<%	while ( iter.hasNext() ) { String proto = iter.next();  %>
<a href="?name=<%=proto%>"><%= proto %></a><br>
<% } %></p><% } %>
<form method="post" action="protocol">
<p align='center'><b>New Protocol</b><br>
Name: <input type="text" name="name"><button type="submit" name="createProtocol">Create</button></p>
</form>
</div>
</body></html>