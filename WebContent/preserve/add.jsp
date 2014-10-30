<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="cyanos" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.Cryo, edu.uic.orjala.cyanos.web.servlet.CryoServlet, edu.uic.orjala.cyanos.sql.SQLCryo, 
	edu.uic.orjala.cyanos.CryoCollection, edu.uic.orjala.cyanos.sql.SQLData, edu.uic.orjala.cyanos.Inoc, edu.uic.orjala.cyanos.sql.SQLInoc,
	edu.uic.orjala.cyanos.User, edu.uic.orjala.cyanos.Role,
	edu.uic.orjala.cyanos.sql.SQLCryoCollection, java.util.List, java.util.ArrayList" %>
<%
	String contextPath = request.getContextPath(); 

	if (request.getParameter("addRecord") != null && CryoServlet.getUser(request).couldPerform(User.CULTURE_ROLE, Role.CREATE) ) {
		SQLData data = CryoServlet.getSQLData(request);
		
		String strainID = request.getParameter("strainID");
		String sourceID = request.getParameter("source");
		String date = request.getParameter("addDate");
		String notes = request.getParameter("notes");
		
		out.print("SOURCE: ");
		out.println(sourceID); 
		List<Cryo> records = new ArrayList<Cryo>();
				
		for (String row : request.getParameterValues("colField")) {
			String collectionID = request.getParameter(row.concat("_collection"));
			String locString = request.getParameter(row.concat("_location"));
			
			String[] locs = locString.split(",");
			for (String loc : locs) {
				if (loc.contains("-")) {
					String[] range = loc.split("\\-");
					if (range.length == 2) {
						out.print("(");
						int[] upper = SQLCryo.parseLocation(range[0]);
						int[] lower = SQLCryo.parseLocation(range[1]);
						for (int x = upper[0]; x <= lower[0]; x++) {
							for (int y = upper[1]; y <= lower[1]; y++) {
								Cryo cryo = SQLCryo.create(data, sourceID);
								cryo.setDate(date);
								cryo.setNotes(notes);
								cryo.setLocation(x, y);
								cryo.setCollection(collectionID);
								records.add(cryo);
							}
						}
					}
				} else if (loc.length() > 0 ){
					Cryo cryo = SQLCryo.create(data, sourceID);
					cryo.setDate(date);
					cryo.setNotes(notes);
					int[] xy = SQLCryo.parseLocation(loc);
					cryo.setCollection(collectionID);
					cryo.setLocation(xy[0], xy[1]);
					records.add(cryo);
				}
			}
		}
		if ( records.size() > 0 ) {
			Inoc source = SQLInoc.load(data, sourceID);
			source.setFate(Inoc.FATE_CRYO);
			
			request.setAttribute("cryoList", records); 
%><jsp:forward page="add-results.jsp"/><%			
		}
	}
%><!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<script language="JAVASCRIPT" src="<%= contextPath %>/cyanos.js"></script>
<script language="JAVASCRIPT" src="<%= contextPath %>/cyanos-date.js"></script>
<script language="javascript">
function updateDefs(strainField) {
	var xmlHttp;
	
	if (window.XMLHttpRequest) {  
		xmlHttp=new XMLHttpRequest();
	} else if (window.ActiveXObject) {
		xmlHttp=new ActiveXObject("Microsoft.XMLHTTP");
  	}
	
	var parentField = strainField.form.elements["source"];
	parentField.innerHTML = "";
	var opt = document.createElement("option");
	opt.text = "Loading...";
	parentField.add(opt);
	
	
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

				var form = strainField.form;
				var parentField = form.elements["source"];
				parentField.innerHTML = "";

				if (JSONobj) {
					for ( var i = 0; i < JSONobj.parents.length; i++) {
						var opt = document.createElement("option");
						opt.text = JSONobj.parents[i].date + " ("
								+ JSONobj.parents[i].volume + ")";
						opt.value = JSONobj.parents[i].id;
						parentField.add(opt);
					}
				}
			}
		}
		xmlHttp.send(query);
	}
	
	function delRow(rowLink) {
		var table = document.getElementById("formTable");
		if ( table ) {
			table.deleteRow(rowLink.parentNode.parentNode.rowIndex);
		}
	}
	
	function addRow(form, rowIndex) {
		var table = document.getElementById("formTable");
		var lastCell = table.rows[rowIndex].cells[0];
		
		var row = "01";

		for ( var i = 0; i < lastCell.childNodes.length; i++ ) {
			var field = lastCell.childNodes[i];
			if ( field.name == "colField") {
				var count = parseInt(field.value);
				row = (count < 9 ? "0" : "").concat(count + 1);
				break;
			}
		}
		
		var tableRow = table.insertRow(rowIndex + 1);
		var cell = tableRow.insertCell(-1);
		cell.innerHTML = '<a onclick="delRow(this)" title="Remove line" style="text-decoration:none; cursor:pointer;"><b><font color="red" size="+2">&times;</font></b></a>';		

		var rowEl = document.createElement("input");
		rowEl.name = "colField";
		rowEl.setAttribute("type", "hidden");
		rowEl.value = row;
	
		cell.appendChild(rowEl);		
		
		cell = tableRow.insertCell(-1);

		var colEl = document.createElement("select")
		colEl.name = row.concat("_collection");
		
		for ( var key in collectionList ) {
			var option = document.createElement("option");
			option.value = key;
			option.text = collectionList[key];
			colEl.add(option);
		}
		cell.appendChild(colEl);
		
		cell = tableRow.insertCell(-1);
		
		var locEl = document.createElement("input");
		locEl.name = row.concat("_location");
		locEl.setAttribute("type", "text");
		locEl.setAttribute("title", "e.g. A1,B2,C3 or A1-A8");
	
		cell.appendChild(locEl);
		
	}
	
	var collectionList = [];
<% CryoCollection collections = SQLCryoCollection.collectionsForType(CryoServlet.getSQLData(request), SQLCryoCollection.BOX); 
	while ( collections.next() ) { 
%> collectionList["<%= collections.getID() %>"] = "<%= collections.getName() %>";
<% } 
%></script>
<link rel="stylesheet" type="text/css" href="<%= contextPath %>/cyanos.css"/>
<title>Cyanos - Add Preservations</title>
</head>
<body onLoad="addRow(document.getElementById('addPreservation'),0)">
<cyanos:menu/>
<div class='content'>
<h1>Add Preservations</h1>
<form id='addPreservation' method="post">
<table class="species" align='center'>
<tr><td>Source Culture:</td><td><input type="text" name="strainID" onChange="updateDefs(this)"></td></tr>
<tr><td>Parent:</td><td><select name="source"></select></td></tr>
<tr><td>Preservation Date:</td><td><cyanos:calendar-field fieldName="addDate"/></td></tr>
<tr><td valign=top>Notes:</td><td><textarea rows="7" cols="70" name="notes"></textarea></td></tr>
</table>

<h3 style="text-align:center">Preservation Locations</h3>
<table class="species" align='center' id='formTable'>
<tr><td></td><th>Collection</th><th>Locations</th></tr>
<tr><td><a onclick="addRow(document.getElementById('addPreservation'),this.parentNode.parentNode.rowIndex - 1)" style="color:#89CFF0; text-decoration:none; cursor:pointer;"><b><font size="+2">+</font></b></a></td>
<td colspan="2"><a onclick="addRow(document.getElementById('addPreservation'),this.parentNode.parentNode.rowIndex - 1)" style="color: gray; text-decoration:none; cursor:pointer;"><i>add line</i></a></td></tr>
</table>
<p align="center"><button type="submit" name="addRecord">Add Record</button> <button type="reset">Clear Form</button></p>
</form>
<% 	if (request.getParameter("addRecord") != null) {
	String strainID = request.getParameter("strainID");
	String sourceID = request.getParameter("source");
	String date = request.getParameter("addDate");
	String notes = request.getParameter("notes");
	
	
	for (String row : request.getParameterValues("colField")) {
		String collectionID = request.getParameter(row.concat("_collection"));
		String locString = request.getParameter(row.concat("_location"));
		
		String[] locs = locString.split(",");
		for (String loc : locs) {
			if (loc.contains("-")) {
				String[] range = loc.split("\\-");
				if (range.length == 2) {
					out.print("(");
					int[] upper = SQLCryo.parseLocation(range[0]);
					int[] lower = SQLCryo.parseLocation(range[1]);
					for (int x = upper[0]; x <= lower[0]; x++) {
						for (int y = upper[1]; y <= lower[1]; y++) {
							out.print("create: ");
							out.print((char) ('A' + x - 1));
							out.print(y);
							out.print(" ");
						}
					}
					out.print(")");
				}
			} else if (loc.length() > 0 ){
				out.print("create: ");
				out.print(loc);
				out.print(" ");
			}
		}
		out.println("<br>");
	}
} %>
</div>
</body>
</html>