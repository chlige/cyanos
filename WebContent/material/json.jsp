<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page language="java" contentType="application/json; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.Role,
	edu.uic.orjala.cyanos.Material,edu.uic.orjala.cyanos.sql.SQLMaterial,
	edu.uic.orjala.cyanos.web.servlet.MaterialServlet,
	java.text.SimpleDateFormat" %>
<%	Material results;
	boolean fullDetails = request.getParameter("fulldetails") != null;
	if ( request.getParameter("id") != null ) {
		results = SQLMaterial.load(MaterialServlet.getSQLData(request), request.getParameter("id"));
		fullDetails = true;
	} else if ( request.getParameter("query") != null ) {
		results = SQLMaterial.find(MaterialServlet.getSQLData(request), request.getParameter("query"));
	} else {
		results = SQLMaterial.find(MaterialServlet.getSQLData(request), "");
	}
	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");;
	results.beforeFirst();	
%>{ "results" : [
<%		while ( results.next() ) { if ( results.isAllowed(Role.READ) ) { %>
{ "id" : "<%= results.getID() %>", "label" : "<%= results.getLabel() %>", "date" : "<%= dateFormat.format(results.getDate()) %>",
"strain_id": "<%= results.getCultureID() %>", "amount": "<%= results.displayAmount() %>"
<% if ( fullDetails )  { 
	String notes = results.getNotes().replace("\"", "\\\"");
%>, "notes" : "<%= notes %>"
<% } %>},	
<% } } %> ]}