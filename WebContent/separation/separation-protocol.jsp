<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="cyanos" tagdir="/WEB-INF/tags" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.Separation,
	edu.uic.orjala.cyanos.CyanosObject,edu.uic.orjala.cyanos.web.servlet.SeparationServlet,edu.uic.orjala.cyanos.web.BaseForm,java.util.List,java.util.ListIterator" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<cyanos:header title="Separation Protocols"/>
</head>
<body>
<cyanos:menu helpModule="separation"/>
<div class='content'>
<h1 style="text-align:center">Separation Protocols</h1>
<hr width="85%">
<%
	Separation.SeparationTemplate myObj = (Separation.SeparationTemplate) request.getAttribute(SeparationServlet.PROTOCOL_OBJ);
%>
<% if ( myObj != null ) { 
	boolean update = request.getParameter(SeparationServlet.UPDATE_ACTION) != null; %>
<form method="post" action="protocol">
<% if ( (! update ) && request.getParameter("createProtocol") != null ) { %><input type="hidden" name="createProtocol"><% } %>
<table class="species" align='center'>
<tr><td>Name:</td><td><%= myObj.getName() %><input type="hidden" name="name" value="<%= myObj.getName() %>"></td></tr>
<tr<% if ( update ) { 
	String value = request.getParameter(SeparationServlet.PARAM_STATIONARY_PHASE);
	if (value != null && (! value.equals(myObj.getStationaryPhase() ) ) ) {
		myObj.setStationaryPhase(value);	
 %> class="updated" <% } } %>><td>Stationary Phase:</td><td><input type="text" name="<%= SeparationServlet.PARAM_STATIONARY_PHASE %>" value="<c:out value="<%= myObj.getStationaryPhase() %>"/>" size=70></td></tr>
<tr<% if ( update ) { 
	String value = request.getParameter(SeparationServlet.PARAM_MOBILE_PHASE);
	if (value != null && (! value.equals(myObj.getMobilePhase() ) ) ) {
		myObj.setMobilePhase(value);	
%> class="updated"<% } } %>><td>Mobile Phase:</td><td><input type="text" name="<%= SeparationServlet.PARAM_MOBILE_PHASE %>" value="<c:out value="<%= myObj.getMobilePhase() %>"/>" size=70></td></tr>
<tr<% if ( update ) { 
	String value = request.getParameter(SeparationServlet.PARAM_METHOD);
	if (value != null && (! value.equals(myObj.getMethod() ) ) ) {
		myObj.setMethod(value);	
 %> class="updated"<% } } %>><td valign="top">Method:</td><td><textarea rows="7" cols="70" name="<%= SeparationServlet.PARAM_METHOD %>"><c:out value="<%= myObj.getMethod() %>"/></textarea></td></tr>
<% if ( update ) { myObj.save(); } %>
<tr><td colspan="2" align="CENTER"><button type="submit" name="<%= SeparationServlet.UPDATE_ACTION %>">Save</button>
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
<% } List<String> allProtocols = (List<String>) request.getAttribute(SeparationServlet.ALL_PROTOCOLS); 
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