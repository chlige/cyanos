<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page language="java" contentType="application/json; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.Role,
	edu.uic.orjala.cyanos.Collection,edu.uic.orjala.cyanos.sql.SQLCollection,
	edu.uic.orjala.cyanos.web.servlet.CollectionServlet,
	java.math.BigDecimal, java.text.SimpleDateFormat" %>
<%	Collection collections;
	boolean fullDetails = request.getParameter("fulldetails") != null;
	if ( request.getParameter("id") != null ) {
		collections = SQLCollection.load(CollectionServlet.getSQLData(request), request.getParameter("id"));
		fullDetails = true;
	} else if ( request.getParameter("query") != null ) {
		String query = request.getParameter("query");
		if ( query.indexOf("*") > -1 ) query = query.replaceAll("\\\\*", "%");
		else query = "%" + query + "%";
		String[] columns = { SQLCollection.ID_COLUMN, SQLCollection.LOCATION_COLUMN, SQLCollection.COLLECTOR_COLUMN, SQLCollection.DATE_COLUMN };
		String[] values = { query, query, query, query };
		collections = SQLCollection.collectionsLike(CollectionServlet.getSQLData(request), columns, values, SQLCollection.ID_COLUMN, SQLCollection.ASCENDING_SORT);
	} else {
		collections = SQLCollection.collections(CollectionServlet.getSQLData(request));
	}
	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");;
	collections.beforeFirst();	
%>{ "collections" : [
<%	while ( collections.next() ) { if ( collections.isAllowed(Role.READ) ) { %>
{ "id" : "<%= collections.getID() %>", "date" : "<%= dateFormat.format(collections.getDate()) %>", "collector" : "<%= collections.getCollector() %>",
"location" : "<%= collections.getLocationName() %>"<% 
if ( collections.getLatitudeFloat() != null && collections.getLongitudeFloat() != null ) { 
%>, "latitude" : <%= collections.getLatitudeFloat() %>, "longitude" : <%= collections.getLongitudeFloat() %>
<% }
if ( fullDetails )  { 
	String notes = collections.getNotes().replace("\"", "\\\"");
%>, "notes" : "<%= notes %>"
<% } %>},	
<% } } %> ]}