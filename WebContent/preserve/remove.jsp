<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="cyanos" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.Cryo, edu.uic.orjala.cyanos.web.servlet.CryoServlet, edu.uic.orjala.cyanos.sql.SQLCryo, 
	edu.uic.orjala.cyanos.CryoCollection, edu.uic.orjala.cyanos.sql.SQLData, edu.uic.orjala.cyanos.Inoc, edu.uic.orjala.cyanos.sql.SQLInoc,
	edu.uic.orjala.cyanos.User, edu.uic.orjala.cyanos.Role,
	edu.uic.orjala.cyanos.sql.SQLCryoCollection, java.util.List, java.util.ArrayList" %>
<%	String contextPath = request.getContextPath(); 
%><!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<cyanos:header title="Cyanos - Remove Preservation Records"/>
<script language="javascript">
function toggleThaw(toggle) {
	var div = document.getElementById("thaw_" + toggle.value);
	if ( toggle.checked ) {
		div.className = "showSection";
		var media = document.getElementById("media_" + toggle.value);
		if ( media.value.length > 1 ) {
			updateDefs(toggle.value);
		}
	} else {
		div.className = "hideSection";
	}
}

function updateDefs(id) {
	var xmlHttp;
	
	if (window.XMLHttpRequest) {  
		xmlHttp=new XMLHttpRequest();
	} else if (window.ActiveXObject) {
		xmlHttp=new ActiveXObject("Microsoft.XMLHTTP");
  	}
	
	var strainField = document.getElementById("strain_" + toggle.value);
	var mediaField = document.getElementById("media_" + toggle.value);
	
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
	SQLData data = CryoServlet.getSQLData(request);
	
	String[] ids = request.getParameterValues("remove");
	
	if ( ids != null ) {
		for ( String id : ids ) {
			Cryo cryo = SQLCryo.load(data, id);
			String vol = request.getParameter("vol_".concat(id));
			if ( vol != null ) {
				Inoc culture = cryo.thaw();
			} else {
				cryo.remove();
			}
		}
	}
} else {
%> <form id='removePreservation' method="post">
<% CryoCollection collection = SQLCryoCollection.load(CryoServlet.getSQLData(request), request.getParameter("collection"));  
	Cryo queryResults = SQLCryo.loadForCollection(CryoServlet.getSQLData(request), collection.getID());
%><table  class="dashboard">
<tr><th class="header">Strain (Preservation #)</th><th class="header" width='200'>Date</th><th class="header" width='100'>Location</th><th class="header" width='300'></th></tr>
<% while ( queryResults.next() ) { 
	if ( ! queryResults.isAllowed(Role.DELETE) ) continue;  
	String id = queryResults.getID();
%><tr align='center' class="banded"><td><input type="checkbox" name="remove" value="<%= id %>" onChange="toggleThaw(this)"><%= queryResults.getCultureID() %> (<%= id %>)</td>
<td><%= CryoServlet.DATE_FORMAT.format(queryResults.getDate()) %></td>
<td><%= queryResults.getLocation() %></td>
<td><%= CryoServlet.shortenString(queryResults.getNotes(), 100) %></td>
<td><div id="thaw_<%= id %>" class="hideSection"><input type="hidden" name="strain_<%= id %>" value="<%= queryResults.getCultureID() %>">
Inoculation volume: <input type="vol_<%= id %>"> Media: <input type="media_<%= id %>"></div></td>
</tr><% } %>
</table>
</form>
<% } %>
</div>
</body>
</html>