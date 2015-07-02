<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="cyanos" %>
<%@ page import="edu.uic.orjala.cyanos.Cryo, 
	edu.uic.orjala.cyanos.Role, edu.uic.orjala.cyanos.web.servlet.CryoServlet,  java.util.List,
	java.text.SimpleDateFormat" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%	String contextPath = request.getContextPath(); %><!DOCTYPE html>
<html>
<head>
<cyanos:header title="Add Preservations"/>
</head>
<body onLoad="addRow(document.getElementById('addPreservation'),0)">
<cyanos:menu/>
<div class='content'>
<h1>Add Preservations</h1>
<h2 style="text-align:center">Results</h2>
<%	Object attr = request.getAttribute("cryoList");
if ( attr != null && attr instanceof List ) {
	List<Cryo> queryResults = (List<Cryo>) attr;
	SimpleDateFormat dateFormat = CryoServlet.DATE_FORMAT;
%><table  class="dashboard">
<tr><th class="header">Preservation #</th><th class="header" width='200'>Date</th><th class="header" width='100'>Location</th><th class="header" width="100">Notes</th></tr>
<% for (Cryo record : queryResults ) { 
	if ( ! record.isAllowed(Role.READ) ) continue;  
%><tr align='center'><td><a href="../preserve.jsp?id=<%= record.getID() %>"><%= record.getID() %></a></td>
<td><%= dateFormat.format(record.getDate()) %></td>
<td><a href="../preserve.jsp?collection=<%= record.getCollectionID() %>"><%= record.getCollectionID() %></a> (<%= record.getLocation() %>)</td>
<td><%= CryoServlet.shortenString(record.getNotes(), 100) %></td></tr><% } %>
</table><% 
} else { 
%><p align='center'><b>No Results</b></p><% 
} %></div></body></html>