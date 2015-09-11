<!DOCTYPE html>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="cyanos" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.Collection,
	edu.uic.orjala.cyanos.sql.SQLCollection,
	edu.uic.orjala.cyanos.web.AppConfig,
	edu.uic.orjala.cyanos.web.servlet.CollectionServlet,
	edu.uic.orjala.cyanos.web.servlet.CollectionServlet.MapBounds,
	edu.uic.orjala.cyanos.sql.SQLData,
	java.util.Map,java.util.TreeSet, java.util.Set, java.util.Iterator" %>
<% 	String contextPath = request.getContextPath();
	String div = request.getParameter("div");
	AppConfig myConfig = (AppConfig) this.getServletContext().getAttribute(CollectionServlet.APP_CONFIG_ATTR);
	Map<String,String> layers = myConfig.getMapServerLayers();
	SQLData data = (SQLData) request.getAttribute(CollectionServlet.DATASOURCE);
	Collection myObject = (Collection) request.getAttribute(CollectionServlet.ATTR_COLLECTION);
	MapBounds bounds = (MapBounds) request.getAttribute(CollectionServlet.ATTR_MAP_BOUNDS); %>
<html>
<head>
<cyanos:header title="Cyanos - Collections"/>
<% if ( myConfig.canMap() ) { 
	request.setAttribute("canMap", Boolean.TRUE); %>
<script type="text/javascript" src="<%= contextPath %>/cyanos-map.js"></script>
<script type="text/javascript" src="<%= contextPath %>/openlayers/OpenLayers.js"></script>
<script type="text/javascript">
function setupMap(canvas) { 
	var map = setupOLMap(canvas);
 	var layer;
<% if ( myConfig.getMapParameter(AppConfig.MAP_OSM_LAYER) != null ) { %> addOSMLayers(map);<% } 
if ( myConfig.getMapParameter("mapQuest") != null ) { %> addMapQuestLayers(map);<% } 
if ( myConfig.getMapParameter(AppConfig.MAP_NASA_LAYER) != null ) { %>	addNASALayers(map);<% } 
	Set<String> keys = new TreeSet<String>(layers.keySet());
	Iterator<String> keyIter = keys.iterator();
	while ( keyIter.hasNext() ) {
		String layerName = keyIter.next();
		String layerURL = layers.get(layerName);
		if ( layerURL == null ) continue; 
%>	layer = new OpenLayers.Layer.Mapserver("<%= layerName %>", "<%= layerURL %>", {}, {gutter:15});
	layer.setIsBaseLayer(true);
	layer.setVisibility(false);
	map.addLayer(layer);
<% }
	String googleMapKey = myConfig.getGoogleMapKey();
	if ( googleMapKey != null) {
		out.println(" addGoogleLayers(map);");
	} %>	addCollectionLayer(map, "<%= CollectionServlet.getKMLURL(request) %>");
<%  if ( bounds != null ) { 
%>	var bounds = new OpenLayers.Bounds(<%= String.format("%.4f, %.4f, %.4f, %.4f", bounds.getMinLongitude(), bounds.getMinLatitude(), 
			bounds.getMaxLongitude(), bounds.getMaxLatitude()) %>);
<% } else if ( request.getParameter("lat") != null && request.getParameter("long") != null ) { 
		float lat = SQLCollection.parseCoordinate(request.getParameter("lat"));
		float lon = SQLCollection.parseCoordinate(request.getParameter("long"));	
%>	var bounds = new OpenLayers.Bounds();
	bounds.extend(new OpenLayers.LonLat(<%= lon %>, <%= lat %>));
<% } %>	setMapBounds(bounds);
}
</script>
<% } %>
<link rel="stylesheet" type="text/css" href="<%= contextPath %>/cyanos.css"/>
</head>
<body>
<cyanos:menu helpModule="<%= CollectionServlet.HELP_MODULE %>"/>
<div class='content'>
<% if ( myObject != null && myObject.first() ) { %>
<p align="CENTER"><font size="+3" >Collection <%= myObject.getID() %></font>
<hr width="85%">
<div id="<%= CollectionServlet.INFO_FORM_DIV_ID %>" class="main">
<jsp:include page="/collection/collection-form.jsp" />
</div>

<cyanos:div divID="<%= CollectionServlet.ISOLATION_DIV_ID %>" title="Isolations"/>

<cyanos:div divID="<%= CollectionServlet.STRAIN_LIST_DIV_ID %>" title="Strains"/>

<cyanos:div divID="<%= CollectionServlet.HARVEST_DIV_ID %>" title="Harvests"/>

<cyanos:div divID="<%= CollectionServlet.PHOTO_DIV_ID %>" title="Photos"/>

<% } else { %>
<h2 style="text-align:center">Collection Search</h2>
<hr width='85%'/>
<form name="collectionquery">
<% String field = request.getParameter("field"); %>
<p align="center">
<select name="field">
<option value="collection_id"<%= "collection_id".equals(field) ? " selected" : "" %>>ID</option>
<option value="date"<%= "date".equals(field) ? " selected" : "" %>>Date</option>
<option value="collector"<%= "collector".equals(field) ? " selected" : "" %>>Collector</option>
<option value="location"<%= "location".equals(field) ? " selected" : "" %>>Location</option></select>
<% String query = request.getParameter("query"); %>
<input type="text" name="query" value="<c:out value="<%= query %>"/>">
<button type='SUBMIT'>Search</button></p>
</form>
<p align="center"><a href="?query">List all collections</a></p>
<% if ( request.getAttribute(CollectionServlet.SEARCHRESULTS_ATTR) != null ) { %>
<p align="center">Export Collection [<a href="collection.csv?field=<%= field %>&query=<%= query %>">CSV</a>]
<% if ( myConfig.canMap() && request.getAttribute(CollectionServlet.ATTR_MAP_BOUNDS) != null ) { %>
 [<a href="collection.kml?field=<%= field %>&query=<%= query %>">KML</a>]</p>
<script>
function showMapCanvas(button) {
	button.innerHTML="Loading..."; 
	button.enabled=false; 
	document.getElementById("map_hide").className = "showSection"; 
	document.getElementById("map_show").className = "hideSection"; 
	window.setTimeout(setupMap, 100, document.getElementById("map_canvas"));
}
</script>
<div id="canvas_frame" style="width: 700px; margin: 0 auto; border: 1px solid gray; background-color: #FCFCFC;">
<div style="margin: 0 auto; width: 700px;" id="map_show" class="showSection">
<p align="center"><button onclick="showMapCanvas(this)" type="BUTTON">View Map</button></p></div>
<div id="map_hide" class="hideSection">
<div id="map_canvas" style="width: 700px; height: 500px; margin: 0 auto;"></div>
</div>
</div>
<% } else { %></p><% } %>
<jsp:include page="/collection/collection-list.jsp" />
<% } } %>
</div>
</body>
</html>