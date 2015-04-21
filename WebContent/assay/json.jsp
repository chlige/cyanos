<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page language="java" contentType="application/json; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.Role,
	edu.uic.orjala.cyanos.Assay,edu.uic.orjala.cyanos.sql.SQLAssay,
	edu.uic.orjala.cyanos.web.servlet.AssayServlet,
	java.text.SimpleDateFormat" %>
<%	Assay results;
	boolean fullDetails = request.getParameter("fulldetails") != null;
	if ( request.getParameter("id") != null ) {
		results = SQLAssay.load(AssayServlet.getSQLData(request), request.getParameter("id"));
		fullDetails = true;
	} else if ( request.getParameter("query") != null ) {
		results = SQLAssay.assaysForTarget(AssayServlet.getSQLData(request), request.getParameter("query"));
	} else {
		results = SQLAssay.assays(AssayServlet.getSQLData(request));
	}
	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");;
	results.beforeFirst();	
%>{ "results" : [
<%		while ( results.next() ) { if ( results.isAllowed(Role.READ) ) { %>
{ "id" : "<%= results.getID() %>", "name" : "<%= results.getName() %>", "date" : "<%= dateFormat.format(results.getDate()) %>",
"target": "<%= results.getTarget() %>"
<% if ( fullDetails )  { 
	String notes = results.getNotes().replace("\"", "\\\"");
%>, "notes" : "<%= notes %>"
<% } %>},	
<% } } %> ] }