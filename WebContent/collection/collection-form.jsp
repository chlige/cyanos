<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="cyanos" tagdir="/WEB-INF/tags" %>
<%@ page import="edu.uic.orjala.cyanos.Collection,
	edu.uic.orjala.cyanos.web.BaseForm,
	edu.uic.orjala.cyanos.web.servlet.CollectionServlet,
	edu.uic.orjala.cyanos.web.servlet.CollectionServlet.MapBounds,
	edu.uic.orjala.cyanos.sql.SQLCollection,
	java.text.DateFormat,
	java.util.Date,
	edu.uic.orjala.cyanos.Role,
	edu.uic.orjala.cyanos.Project" %>
<% 	String contextPath = request.getContextPath();
	Collection myObject = (Collection) request.getAttribute(CollectionServlet.ATTR_COLLECTION); 	
	if ( myObject == null ) { %>
<p align='center'><b>ERROR:</b> Object not passed</p>
<% out.flush(); return; } else if ( ! myObject.first() ) { %>
<p align='center'><b>ERROR:</b> Object not found</p>
<% out.flush(); return; } %>
<div CLASS="showSection" ID="view_info">
<% 	boolean updateLoc = false;
	DateFormat dateFormat = (DateFormat) session.getAttribute("dateFormatter");
	boolean update = myObject.isAllowed(Role.WRITE) && request.getParameter("updateCollection") != null;
	boolean canMap = request.getAttribute("canMap") != null && (Boolean) request.getAttribute("canMap");
	if ( update ) {
		String value = request.getParameter("lat");
		if ( value != null ) {
			Float locVal = new Float(SQLCollection.parseCoordinate(value));
			if ( locVal != null && (! locVal.equals(myObject.getLatitudeFloat())) ) {
				myObject.setLatitude(locVal);
				updateLoc = true;
			}
		}
		value = request.getParameter("long");
		if ( value != null ) {
			Float locVal = new Float(SQLCollection.parseCoordinate(value));
			if ( locVal != null && (! locVal.equals(myObject.getLongitudeFloat())) ) {
				myObject.setLongitude(locVal);
				updateLoc = true;
			}
		}	
		myObject.refresh();
	}
	canMap = (canMap && myObject.getLatitudeFloat() != null && myObject.getLongitudeFloat() != null );
if ( canMap ) { %>
<script type="text/javascript">
function openMap(button) {
	button.innerHTML="Loading..."; 
	button.enable=false; 
	window.setTimeout(setupMap, 100, document.getElementById("map_canvas"));
}
</script>
<table style="margin-bottom:10px; width: 90%; margin-left:auto; margin-right:auto">
<tr><td valign="top">
<% } %>
<table class="list" style="width:80%; margin-left:auto; margin-right:auto">
<tr><td width="100">ID:</td><td><%= myObject.getID() %></td></tr>
<tr<% if ( update ) {
	String value = request.getParameter("colDate");
	if ( value != null && (! value.equals(myObject.getDateString()) )) {
		myObject.setDate(value); %> class="updated"<% } }
%>><td>Collection Date:</td><td><%= dateFormat.format(myObject.getDate()) %></td></tr>
<tr<% if ( update ) {
	String value = request.getParameter("collector");
	if ( value != null && (! value.equals(myObject.getCollector()) )) {
		myObject.setCollector(value); %> class="updated"<% } }
%>><td>Collection By:</td><td><%= myObject.getCollector() %></td></tr>
<tr<% if ( updateLoc ) { %> class="updated"<% } %>><td colspan="2"><b><i>Location</i></b><br>
<div style="margin-left:10px"><% 
boolean closeI = false;
if ( update ) {
	String value = request.getParameter("loc_name");
	if ( value != null && (! value.equals(myObject.getLocationName()) )) {
		myObject.setLocationName(value); out.print("<i>"); closeI = true; } } %>
<%= myObject.getLocationName() %><% if ( closeI ) { out.print("</i>"); } %><br>
Coordinates: <% if ( updateLoc ) {	out.print("<i>"); }
	out.print(myObject.getLatitudeDM());
	out.print(", ");
	out.print(myObject.getLongitudeDM()); if ( updateLoc ) { out.print("</i>"); } %>
<br>Precision: <% 
if ( update ) {
	String value = request.getParameter("loc_prec");
	if ( value != null ) {
		myObject.setPrecision(value); } } 
	out.print(myObject.getPrecision()); %> m
</div></td>
<tr<% if ( update ) { 
	String value = request.getParameter("project");
	if (value != null && (! value.equals(myObject.getProjectID()) ) ) {
		myObject.setProjectID(value); %> class="updated"<% } } %>><td>Project:</td><td>
<% Project aProj = myObject.getProject(); if ( aProj != null && aProj.first() ) { %>
<a href='project?id=<%= aProj.getID() %>'><%= aProj.getName() %></a><% } else { %>None<% } %></td></tr>
<tr<% if ( update ) { 
	String value = request.getParameter("notes");
	if (value != null && (! value.equals(myObject.getNotes()) ) ) {
		myObject.setNotes(value);	
%> class="updated"<% } } %>><td valign=top>Notes:</td><td><%= BaseForm.formatStringHTML(myObject.getNotes()) %></td></tr>
</table>
<% if ( canMap ) { %>
</td><td valign="top">
<div style="width: 400px; float: right; display: inline;">
<div style="width: 400px; height: 300px; margin: 0 auto; border: 1px solid gray; background-color: #FCFCFC;" id="map_canvas">
<p align="CENTER">
<button style="margin-top:30%" onclick="openMap(this)" type="button">View Map</button></p></div></div>
</td></tr></table>
<% } if ( myObject.isAllowed(Role.WRITE) ) { %>
<p align='center'><button type='button' onClick='flipDiv("info")'>Edit Values</button></p>
</div>
<div class='hideSection' id="edit_info">
<form name='editStrain' method="post" action="collection">
<input type="hidden" name="col" value="<%= myObject.getID() %>">
<table class="species" style="width:80%; margin-left:auto; margin-right:auto">
<% Date strainDate = myObject.getDate();
   String dateString = ( strainDate != null ? CollectionServlet.CALFIELD_FORMAT.format(strainDate) : null );
%><tr><td>Collection Date:</td><td>
<cyanos:calendar-field fieldName="colDate" dateValue="<%= dateString %>"/></td></tr>
<tr><td>Collected by:</td><td><input type="text" name="collector" value="<c:out value="<%= myObject.getCollector() %>"/>"></td></tr>
<tr><td>Location name:</td><td><input type="text" name="loc_name" value="<c:out value="<%= myObject.getLocationName() %>"/>" size="40"></td></tr>
<tr><td>Coordinates:</td><td id="coords"><input type="text" id="pos_lat" name="lat" size="10" value="<%= myObject.getLatitudeFloat() %>"> 
<input type="text" id="pos_long" name="long" size="10" value="<%= myObject.getLongitudeFloat() %>"></td></tr>
<tr><td>Accuracy (m):</td><td><input id="pos_acc" type="text" name="loc_prec" size="10" value="<c:out value="<%= myObject.getPrecision() %>"/>"></td></tr>
<tr><td>Project:</td><td><cyanos:project-popup fieldName="project" project="<%= myObject.getProjectID() %>"/></td></tr>
<tr><td valign=top>Notes:</td><td><textarea rows="7" cols="70" name="notes"><c:out value="<%= myObject.getNotes() %>" default="" /></textarea></td></tr>
<tr><td colspan="2" align="CENTER"><button type="button" name="updateCollection" onClick="updateForm(this,'<%= CollectionServlet.INFO_FORM_DIV_ID %>')">Update</button>
<input type="RESET"></td></tr>
</table>
</form>
<script>
function updatePosition (position) { 
	var field = document.getElementById("pos_lat");
	field.value = position.coords.latitude;
	field = document.getElementById("pos_long");
	field.value = position.coords.longitude;
	field = document.getElementById("pos_acc");
	field.value = position.coords.accuracy.toFixed(0) + " m";
}

if ( navigator.geolocation ) {
	var button = document.createElement("button");
	button.type = "button";
	button.innerHTML = "Use current location";
	button.onclick = function () { navigator.geolocation.getCurrentPosition(updatePosition);};
	var cell = document.getElementById("coords");
	cell.appendChild(button);
}
</script>
<p align="center"><button type='button' onClick='flipDiv("info")'>Close Form</button></p>
<% } %>
</div>
