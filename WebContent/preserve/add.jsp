<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="cyanos" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.Cryo, edu.uic.orjala.cyanos.web.servlet.CryoServlet, edu.uic.orjala.cyanos.sql.SQLCryo, 
	edu.uic.orjala.cyanos.CryoCollection,
	edu.uic.orjala.cyanos.sql.SQLCryoCollection" %>
<% 	String contextPath = request.getContextPath(); %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<script language="JAVASCRIPT" src="<%= contextPath %>/cyanos.js"></script>
<script language="JAVASCRIPT" src="<%= contextPath %>/cyanos-date.js"></script>
<script language="javascript">
function updateDefs(urlBase, strainField) {
	var xmlHttp;
	
	if (window.XMLHttpRequest) {  
		xmlHttp=new XMLHttpRequest();
	} else if (window.ActiveXObject) {
		xmlHttp=new ActiveXObject("Microsoft.XMLHTTP");
  	}
	var query = "getJSON=strain&strain=" + escape(strainField.value);
	xmlHttp.open("POST", urlBase, true);
	xmlHttp.setRequestHeader("Content-type","application/x-www-form-urlencoded");

	xmlHttp.onreadystatechange = function() {
			if (xmlHttp.readyState == 4 && xmlHttp.status == 200) {
				var JSONobj = null;
				try {
					JSONobj = JSON.parse(xmlHttp.responseText);
				} catch (err) {
				}

				var row = strainField.name.substr(0, 2);
				var form = strainField.form;
				var projectField = form.elements[row + "_project"];
				var mediaField = form.elements[row + "_media"];
				var parentField = form.elements[row + "_parent"];
				parentField.innerHTML = "";

				if (JSONobj) {
					mediaField.value = JSONobj.media;
					var options = projectField.options;
					for ( var i = 0; i < options.length; i++) {
						if (options[i].value == JSONobj.project) {
							projectField.selectedIndex = i;
							break;
						}
					}
					var opt = document.createElement("option");
					opt.text = "NONE";
					opt.value = "";
					parentField.add(opt);
					for ( var i = 0; i < JSONobj.parents.length; i++) {
						opt = document.createElement("option");
						opt.text = JSONobj.parents[i].date + " ("
								+ JSONobj.parents[i].volume + ")";
						opt.value = JSONobj.parents[i].id;
						parentField.add(opt);
					}
				} else {
					projectField.selectedIndex = 0;
					mediaField.value = "";
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
	
	function addRow(form) {
		var table = document.getElementById("formTable");
		var count = table.rows.length - 1;
		var row = (count < 10 ? "0" : "").concat(count);

		var tableRow = table.insertRow(-1);
		var cell = tableRow.insertCell(-1);

		var rowEl = document.createElement("input");
		rowEl.name = "colField";
		rowEl.setAttribute("type", "hidden");
		rowEl.value = row;
	
		cell.appendChild(rowEl);		
		
		var colEl = document.createElement("select")
		colEl.name = row.concat("_collection");
		
		var collList = form.elements['01_collection'].options;
		
		for ( var i = 0; i < collList.length; i++ ) {
			colEl.add(collList[i]);
		}
		
		cell.appendChild(colEL);
		
		cell = tableRow.insertCell(-1);
		
		var locEl = document.createElement("input");
		locEl.name = row.concat("_location");
		locEl.setAttribute("type", "text");
		locEl.setAttribute("title", "e.g. A1,B2,C3 or A1-A8");
	
		cell.appendChild(locEl);
		
	}
	
	
</script>
<link rel="stylesheet" type="text/css" href="<%= contextPath %>/cyanos.css"/>
<title>Cyanos - Add Preservations</title>
</head>
<body>
<jsp:include page="/includes/menu.jsp" />
<div class='content'>
<h1>Add Preservations</h1>
<form name='addPreservation'>
<table class="species" align='center'>
<tr><td>Source Culture:</td><td><input type="text" name="strainID"></td></tr>
<tr><td>Parent:</td><td><select name="source"></select></td></tr>
<tr><td>Preservation Date:</td><td><cyanos:calendar-field fieldName="addDate"/></td></tr>
<tr><td valign=top>Notes:</td><td><textarea rows="7" cols="70" name="notes"></textarea></td></tr>
</table>

<h3 style="text-align:center">Preservation Locations</h3>
<table class="species" align='center' id='formTable'>
<tr><th>Collection</th><th>Locations</th></tr>
<tr><td><input type="hidden" name="colField" value="01">
<select name="01_collection">
<% CryoCollection collections = SQLCryoCollection.collectionsForType(CryoServlet.getSQLData(request), SQLCryoCollection.BOX); 
	while ( collections.next() ) { %><option value="<%= collections.getID() %>"><%= collections.getName() %></option><% } %></select></td>
<td><input type="text" name="01_locations" title="e.g. A1,B2,C3 or A1-A8"></td>
<tr><td colspan=2"><a onClick="addRow(this.form)">Add Row</a></td></tr>
</table>
<p align="CENTER"><button type="submit" name="addRecord">Add</button> <button type="reset">Clear Form</button></p>
</form>
</div>
</body>
</html>
