<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="cyanos" tagdir="/WEB-INF/tags" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="edu.uic.orjala.cyanos.web.servlet.MainServlet,
	edu.uic.orjala.cyanos.web.listener.AppConfigListener,
	edu.uic.orjala.cyanos.Strain,
	edu.uic.orjala.cyanos.sql.SQLStrain,
	edu.uic.orjala.cyanos.User,
	java.text.DateFormat" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<cyanos:header title="Cyanos Link Objects"/>
<style type="text/css">
.content { margin: 10px; }
input { background-color: white; border-radius: 0px; }
</style>
</head>
<body>
<div class="content">
<% String queryType = request.getParameter("type"); 	String queryString = request.getParameter("query"); %>
<form><p>Record type: 
<select name="type">
<option value="strain" <%= "strain".equals(queryType) ? "selected" : "" %>>Strain</option>
<option value="inoc"<%= "inoc".equals(queryType) ? "selected" : "" %>>Inoculation</option>
<option value="harvest"<%= "harvest".equals(queryType) ? "selected" : "" %>>Harvest</option>
<option value="material"<%= "material".equals(queryType) ? "selected" : "" %>>Material</option>
<option value="sep"<%= "sep".equals(queryType) ? "selected" : "" %>>Separation</option>
<option value="assay"<%= "assay".equals(queryType) ? "selected" : "" %>>Assay</option>
<option value="compound"<%= "compound".equals(queryType) ? "selected" : "" %>>Compound</option>
</select>
</p>
<p>Query: <input type="text" name="query" size="15" value='<%= queryString != null ? queryString : "" %>'> <button type="submit">Search</button></p>
</form>
<ul>
<% if ( queryType != null ) { 
	if (queryString.matches("\\*") ) {
		queryString.replaceAll("\\*", "%");
	} else {
		queryString = "%" + queryString + "%";
	}
	
	if ( queryType.equals("strain") ) {
		String[] columns = {SQLStrain.NAME_COLUMN, SQLStrain.ID_COLUMN};
		String[] queries = {queryString, queryString};
		Strain strainList = SQLStrain.strainsLike(MainServlet.getSQLData(request), columns, queries, SQLStrain.SORT_ID, SQLStrain.ASCENDING_SORT);	
		strainList.beforeFirst();
		while ( strainList.next() ) {
%><li><%= strainList.getID() %></li><%			
		}
	}
%>
<% } %>
</ul>
</div>
</body>
</html>