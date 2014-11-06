<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="cyanos" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.Inoc,
	edu.uic.orjala.cyanos.CyanosObject,edu.uic.orjala.cyanos.web.servlet.InocServlet,
	edu.uic.orjala.cyanos.web.BaseForm,
	java.text.SimpleDateFormat" %>
<%	String contextPath = request.getContextPath(); %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<cyanos:header title="Add Inoculations"/>
<script language="javascript">
function updateDefs(strainField) {
	var xmlHttp;
	
	if (window.XMLHttpRequest) {  
		xmlHttp=new XMLHttpRequest();
	} else if (window.ActiveXObject) {
		xmlHttp=new ActiveXObject("Microsoft.XMLHTTP");
  	}
	var myLoc = strainField.form.action;
	var query = "getJSON=strain&strain=" + escape(strainField.value);
	xmlHttp.open("POST", myLoc.toString(), true);
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
	
	function addRow(form, rowIndex) {
		var table = document.getElementById("formTable");
		var lastCell = table.rows[rowIndex].cells[0];
		
		var row = "01";

		for ( var i = 0; i < lastCell.childNodes.length; i++ ) {
			var field = lastCell.childNodes[i];
			if ( field.name == "row") {
				var count = parseInt(field.value);
				row = (count < 9 ? "0" : "").concat(count + 1);
				break;
			}
		}
		
		var tableRow = table.insertRow(rowIndex + 1);
		tableRow.className="banded";
		
		var newRow = document.getElementById("templateRow").cloneNode(true);

		var dateFunc = function () { showDate("cal_" + row.concat("_date"), row.concat("_date")); };

		var list = newRow.getElementsByTagName("input");
		
		for ( var i = 0; i < list.length; i++ ) {
			var field = list[i];
			if ( field.name == "row") 
				field.value = row;
			else 
				field.name = row.concat("_" + field.name);		
			if ( field.id == "strain" ) {
				field.id = row.concat("_strain");
			} else if ( field.id == "date" ) {
				field.id = row.concat("_date");
				field.onfocus = dateFunc;
			}
		}
		
		list = newRow.getElementsByTagName("select");
		
		for ( var i = 0; i < list.length; i++ ) {
			var field = list[i];
			field.name = row.concat("_" + field.name);		
		}
		
		list = newRow.getElementsByTagName("div")
		
		for ( var i = 0; i < list.length; i++ ) {
			var div = list[i];
			if ( div.id == "cal_date" ) {
				div.id = "cal_".concat(row.concat("_date"));
			} else if ( div.id = "div_strain" ) {
				div.id = "div_".concat(row.concat("_strain"));
			}
		}
		
		var dateCell = newRow.cells[2];
		
		for ( var i = 0; i < dateCell.childNodes.length; i++ ) {
			var field = dateCell.childNodes[i];
			if ( field.tagName == "A" ) {
				field.onClick = dateFunc;
			} 
		}
		
		for ( var i = 0; i < newRow.childNodes.length; i++ ) {
			tableRow.appendChild(newRow.childNodes[i]);
		}
	}
	
	function strainsearch(field) {
		var div = window.document.getElementById("div_" + field.name);
		if ( field.value.length == 0 ) {
			div.style.border = "0px";
			div.innerHTML = "";
			div.style.display = "none";
		} else {	
			div.innerHTML = "<FONT COLOR='#505050'>Loading...</FONT>";
			div.style.border = "1px solid #A5ACB2";
			div.style.display = "block";
			var query = "div=" + escape(div.id) + "&" + escape(field.name) + "=" + escape(field.value) + "&livesearch=" + escape(field.name);
			window.setTimeout(loadLS, 100, query, div.id);
		}
	}

	
	function loadTable() {
		addRow(document.getElementById('addInocs'),0); 
<% if ( request.getParameter("01_strain") != null ) { %>
		document.getElementById('addInocs').elements['01_strain'].value = "<%= request.getParameter("01_strain") %>";
<% } %>
	}
</script>
</head>
<body onLoad="loadTable()">
<cyanos:menu helpModule="<%= InocServlet.HELP_MODULE %>"/>
<div class='content'>
<h2 align="center">Add Inoculations</h2>
<hr width="75%">
<% if ( request.getParameter("addInocs") != null ) { %>
<jsp:include page="add-inocs.jsp"/>
<% } else {  %>
<form method="post" action="<%= request.getContextPath() %>/inoc" id="addInocs">
<input type="hidden" name="form" value="add">
<div id="addTable">
<table class="dashboard" id="formTable">
<tr><td></td><th class="header">Strain ID</th><th class="header">Date</th><th class="header">Parent Stock</th><th class="header">Media</th><th class="header">Volume</th><th class="header">Project</th><th class="header">Notes</th><th class="header">Stock</th></tr>
<tr><td><a onclick="addRow(document.getElementById('addInocs'),this.parentNode.parentNode.rowIndex - 1)" style="color:#89CFF0; text-decoration:none; cursor:pointer;"><b><font size="+2">+</font></b></a></td>
<td colspan="8"><a onclick="addRow(document.getElementById('addInocs'),this.parentNode.parentNode.rowIndex - 1)" style="color: gray; text-decoration:none; cursor:pointer;"><i>add line</i></a></td></tr>
</table>
</div>
<p align="center"><button type="submit" name="addInocs">Add Inoculation(s)</button></p>
</form>
<table style="display:none">
<tr id="templateRow"><td><input type="hidden" name="row"><a onclick="delRow(this)" title="Remove line" style="text-decoration:none; cursor:pointer;"><b><font color="red" size="+2">&times;</font></b></a></td>
<td><input id="strain" type="text" name="strain" autocomplete='off' onKeyUp="strainsearch(this)" style='padding-bottom: 0px' size="10" onchange="updateDefs(this)"/>
<div id="div_strain" class='livesearch'></div></td>
<td><cyanos:calendar-field fieldName="date"/></td>
<td><select name="parent"></select></td>
<td><input type="text" size="10" name="media"></td>
<td><input type="text" size="3" name="qty"> &times; <input type="text" size="5" name="vol"></td>
<td><cyanos:project-popup fieldName="project"/></td>
<td><textarea name="notes" rows="2" cols="15"></textarea></td>
<td><input type="checkbox" name="stock">
</tr>
</table>
<% } %>
</div>