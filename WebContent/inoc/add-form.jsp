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
</script>
</head>
<body>
<cyanos:menu helpModule="<%= InocServlet.HELP_MODULE %>"/>
<div class='content'>
<h2 align="center">Add Inoculations</h2>
<hr width="75%">
<% if ( request.getParameter("addInocs") != null ) { %>
<jsp:include page="add-inocs.jsp"/>
<% } else {  %>
<form method="post" action="<%= request.getContextPath() %>/inoc">
<input type="hidden" name="form" value="add">
<div id="addTable">
<jsp:include page="add-form-table.jsp"/>
</div>
<p align="center"><button type="submit" name="addInocs">Add Inoculation(s)</button></p>
</form>
<% } %>
</div>