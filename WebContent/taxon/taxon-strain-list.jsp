<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.Strain,
	edu.uic.orjala.cyanos.sql.SQLStrain,
	edu.uic.orjala.cyanos.Taxon,
	edu.uic.orjala.cyanos.web.servlet.StrainServlet,edu.uic.orjala.cyanos.web.BaseForm,java.text.SimpleDateFormat" %>
<%
	String contextPath = request.getContextPath();
	Strain queryResults = (Strain)request.getAttribute(StrainServlet.SEARCHRESULTS_ATTR); 
	String divID = request.getParameter("div");
	if ( queryResults != null ) { 
		if ( queryResults.first() ) { 
	String sortField = request.getParameter(StrainServlet.PARAM_SORT_FIELD);
	String sortDir = request.getParameter(StrainServlet.PARAM_SORT_DIR);
	if ( sortField == null ) sortField = SQLStrain.ID_COLUMN;
	if ( sortDir == null )  sortDir = SQLStrain.ASCENDING_SORT;
	boolean sortAsc = ( sortDir.equals(SQLStrain.ASCENDING_SORT) );
	queryResults.beforeFirst();
	SimpleDateFormat dateFormat = (SimpleDateFormat) session.getAttribute(StrainServlet.SESS_ATTR_DATE_FORMAT);
%>
<div id="<%=divID%>">
<table style="width:90%; margin-left:5%"; class="dashboard">
<tr>
<th class="header" width='100'><a class="sort" onClick='sortTable("<%=divID%>","<%=SQLStrain.ID_COLUMN%>","<%=( sortField.equals(SQLStrain.ID_COLUMN) && sortAsc ? SQLStrain.DESCENDING_SORT : SQLStrain.ASCENDING_SORT)%>")'>Strain ID</a>
<%	if ( sortField.equals(SQLStrain.ID_COLUMN) ) { %><img src="<%=contextPath%>/images/<%=(sortAsc ? "sort-asc.png" : "sort-desc.png")%>"><% } %></th>
<th class="header" width='100'><a class="sort" onClick='sortTable("<%=divID%>","<%=SQLStrain.NAME_COLUMN%>","<%=( sortField.equals(SQLStrain.NAME_COLUMN) && sortAsc ? SQLStrain.DESCENDING_SORT : SQLStrain.ASCENDING_SORT)%>")'>Name</a>
<%	if ( sortField.equals(SQLStrain.NAME_COLUMN) ) { %><img src="<%=contextPath%>/images/<%=(sortAsc ? "sort-asc.png" : "sort-desc.png")%>"><% } %></th>
<th class="header" width='100'><a class="sort" onClick='sortTable("<%=divID%>","<%=SQLStrain.DATE_COLUMN%>","<%=( sortField.equals(SQLStrain.DATE_COLUMN) && sortAsc ? SQLStrain.DESCENDING_SORT : SQLStrain.ASCENDING_SORT)%>")'>Date Added</a>
<% if ( sortField.equals(SQLStrain.DATE_COLUMN) ) { %><img src="<%=contextPath%>/images/<%=(sortAsc ? "sort-asc.png" : "sort-desc.png")%>"><% } %></th>
<th class="header" width='200'>Notes</th></tr>
<%	while ( queryResults.next() ) { 
	String rowFormat = ( queryResults.isActive() ? "banded" : "dead"); %>
<tr class=<%=rowFormat%> align='center'><td><a href="<%=contextPath%>/strain?id=<%=queryResults.getID()%>"><%=queryResults.getID()%></a></td>
<td><%=queryResults.getName()%></td>
<td><%= dateFormat.format(queryResults.getDate()) %></td>
<td><% int notesLen = 50; if ( ! queryResults.isActive() ) { notesLen = notesLen - 9; %><b><font color="#AA0000">REMOVED: </font></b><% } %>
<%= BaseForm.shortenString(queryResults.getNotes(), 50) %></td></tr><% } %>
</table></div>
<% } else { %>
<p align='center'><b>No Results</b></p>
<% } } %>
