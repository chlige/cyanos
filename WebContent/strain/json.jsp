<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page language="java" contentType="application/json; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.Strain,
	edu.uic.orjala.cyanos.sql.SQLStrain,edu.uic.orjala.cyanos.Role,
	edu.uic.orjala.cyanos.web.servlet.StrainServlet,edu.uic.orjala.cyanos.web.BaseForm,java.text.SimpleDateFormat" %>
<%	Strain queryResults;
	boolean fullDetails = request.getParameter("fulldetails") != null;
	if ( request.getParameter("id") != null ) {
		queryResults = SQLStrain.load(StrainServlet.getSQLData(request), request.getParameter("id"));
		fullDetails = true;
	} else if ( request.getParameter("query") != null ) {
		String query = request.getParameter("query");
		if ( query.indexOf("*") > -1 ) query = query.replaceAll("\\\\*", "%");
		else query = "%" + query + "%";
		String[] columns = {SQLStrain.NAME_COLUMN, SQLStrain.ID_COLUMN};
		String[] queries = {query, query};
		queryResults = SQLStrain.strainsLike(StrainServlet.getSQLData(request), columns, queries, SQLStrain.SORT_ID, SQLStrain.ASCENDING_SORT);
	} else {
		queryResults = SQLStrain.strains(StrainServlet.getSQLData(request));
	}
	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");;
	queryResults.beforeFirst();
%>{ "strains" : [
<%	
if ( queryResults.first() ) { if ( queryResults.isAllowed(Role.READ) ) {  %>
{ "id" : "<%= queryResults.getID() %>", "name" : "<%= queryResults.getName() %>"
<% if ( fullDetails )  { 
	String notes = queryResults.getNotes().replace("\"", "\\\"");
%>, "date_added" : "<%= dateFormat.format(queryResults.getDate()) %>", "notes" : "<%= notes %>"
<% } %>}
<% } } 
while ( queryResults.next() ) { if ( queryResults.isAllowed(Role.READ) ) {  %>
,{ "id" : "<%= queryResults.getID() %>", "name" : "<%= queryResults.getName() %>"
<% if ( fullDetails )  { 
	String notes = queryResults.getNotes().replace("\"", "\\\"");
%>, "date_added" : "<%= dateFormat.format(queryResults.getDate()) %>", "notes" : "<%= notes %>"
<% } %>}	
<% } } %> ]}