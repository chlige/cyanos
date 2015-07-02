<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="cyanos" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.Cryo, edu.uic.orjala.cyanos.web.servlet.CryoServlet, edu.uic.orjala.cyanos.sql.SQLCryo, 
	edu.uic.orjala.cyanos.CryoCollection, edu.uic.orjala.cyanos.sql.SQLData, edu.uic.orjala.cyanos.Inoc, edu.uic.orjala.cyanos.sql.SQLInoc,
	edu.uic.orjala.cyanos.User, edu.uic.orjala.cyanos.Role,
	edu.uic.orjala.cyanos.sql.SQLCryoCollection, java.util.List, java.util.ArrayList" %>
<%	String contextPath = request.getContextPath(); 
%><!DOCTYPE html>
<html>
<head>
<cyanos:header title="Cyanos - Remove Preservation Records"/>
<script language="javascript">
function toggleThaw(toggle) {
	var form = toggle.form;
	var div = document.getElementById("thaw_" + toggle.value);
	if ( toggle.checked ) {
		div.style.display = "inline";
		var media = form.elements["media_" + toggle.value];
		if ( media.value.length < 1 ) {
			updateDefs(toggle.value);
		}
	} else {
		div.style.display = "none";
	}
}

function updateDefs(id) {
	var xmlHttp;
	
	if (window.XMLHttpRequest) {  
		xmlHttp=new XMLHttpRequest();
	} else if (window.ActiveXObject) {
		xmlHttp=new ActiveXObject("Microsoft.XMLHTTP");
  	}
	
	
	var form = document.getElementById("removePreservation");
	
	var strainField = form.elements["strain_" + id];
	var mediaField = form.elements["media_" + id];
	
	var query = "getJSON=strain&strain=" + escape(strainField.value);
	xmlHttp.open("POST", "<%= contextPath %>/inoc", true);
	xmlHttp.setRequestHeader("Content-type","application/x-www-form-urlencoded");

	xmlHttp.onreadystatechange = function() {
			if (xmlHttp.readyState == 4 && xmlHttp.status == 200) {
				var JSONobj = null;
				try {
					JSONobj = JSON.parse(xmlHttp.responseText);
				} catch (err) {
				}

				if (JSONobj) {
					mediaField.value = JSONobj.media;
				}
			}
		}
		xmlHttp.send(query);
	}
</script>
</head>
<body>
<cyanos:menu/>
<div class='content'>
<h1>Remove Preservations</h1>
<% 	if ( request.getParameter("removeRecords") != null && CryoServlet.getUser(request).couldPerform(User.CULTURE_ROLE, Role.DELETE) ) { 
%><h2>Results</h2>
<p align="center"><a href="../preserve.jsp?collection=<%= request.getParameter("collection") %>">Return to Preservation Collection</a></p>
<table align="center">
<tr><th class="header" width="150">Strain (Preservation #)</th><th class="header" width='200'>Preservation Date</th><th class="header" width='100'>Location</th><th class="header" width='400'></th></tr>
<%
	SQLData data = CryoServlet.getSQLData(request);
	String[] ids = request.getParameterValues("remove");
	if ( ids != null ) {
		for ( String id : ids ) {
			Cryo cryo = SQLCryo.load(data, id);
%><tr class="banded"><td><%= cryo.getCultureID() %> (<%= id %>)</td><td><%= CryoServlet.DATE_FORMAT.format(cryo.getDate()) %></td><td><%= cryo.getLocation() %></td><%
			String vol = request.getParameter("vol_".concat(id));
			if ( vol != null && vol.length() > 0 ) {
				Inoc culture = cryo.thaw(vol, request.getParameter("media_".concat(id)));
%><td>Created new inoculation - <a href="<%= contextPath %>/inoc?id=<%= culture.getID() %>"><%= culture.getVolumeString() %> (ID: <%= culture.getID() %>)</a></td><%
			} else {
				cryo.remove();
%><td>Marked removed - <%= CryoServlet.DATE_FORMAT.format(cryo.getRemovedDate()) %></td><%
			}
%></tr><%
		}
	} 
%></table><%
} else {
%> <form id='removePreservation' method="post">
<% CryoCollection collection = SQLCryoCollection.load(CryoServlet.getSQLData(request), request.getParameter("collection"));  
	Cryo queryResults = SQLCryo.loadForCollection(CryoServlet.getSQLData(request), collection.getID());
%>
<p align="center"><button type="submit" name="removeRecords">Remove Preservations</button><button type="reset">Clear Form</button></p>
<table  class="dashboard">
<tr><th class="header" width="150">Strain (Preservation #)</th><th class="header" width='200'>Date</th><th class="header" width='100'>Location</th><th class="header" width='400'></th></tr>
<% while ( queryResults.next() ) { 
	if ( ! queryResults.isAllowed(Role.DELETE) ) continue;  
	String id = queryResults.getID();
%><tr align='center' class="banded"><td><input type="checkbox" name="remove" value="<%= id %>" onChange="toggleThaw(this)"><%= queryResults.getCultureID() %> (<%= id %>)</td>
<td><%= CryoServlet.DATE_FORMAT.format(queryResults.getDate()) %></td>
<td><%= queryResults.getLocation() %></td>
<td><div id="thaw_<%= id %>" style="display:none"><input type="hidden" name="strain_<%= id %>" value="<%= queryResults.getCultureID() %>">
Inoculation volume: <input type="text" name="vol_<%= id %>" size="15"> Media: <input type="text" name="media_<%= id %>" size="10"></div></td>
</tr><% } %>
</table>
</form>
<% } %>
</div>
</body>
</html>