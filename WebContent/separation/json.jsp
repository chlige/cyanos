<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.Role,
	edu.uic.orjala.cyanos.Separation,edu.uic.orjala.cyanos.sql.SQLSeparation,
	edu.uic.orjala.cyanos.web.servlet.SeparationServlet,
	java.text.SimpleDateFormat" %>
<%	Separation results;
	boolean fullDetails = request.getParameter("fulldetails") != null;
	if ( request.getParameter("id") != null ) {
		results = SQLSeparation.load(SeparationServlet.getSQLData(request), request.getParameter("id"));
		fullDetails = true;
	} else if ( request.getParameter("query") != null ) {
		results = SQLSeparation.findForStrain(SeparationServlet.getSQLData(request), request.getParameter("query"));
	} else {
		results = SQLSeparation.separations(SeparationServlet.getSQLData(request));
	}
	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");;
	results.beforeFirst();	
%>{ "results" : [
<%		while ( results.next() ) { if ( results.isAllowed(Role.READ) ) { %>
{ "id" : "<%= results.getID() %>", "label" : "<%= results.getTag() %>", "date" : "<%= dateFormat.format(results.getDate()) %>"
<% if ( fullDetails )  { 
	String notes = results.getNotes().replace("\"", "\\\"");
	String method = results.getMethod().replace("\"", "\\\"");
%>, "mobile_phase" : "<%= results.getMobilePhase() %>", "stationary_phase": "<%= results.getStationaryPhase() %>", 
"method": "<%= method %>", "notes" : "<%= notes %>"
<% } %>},	
<% } } %> ]}