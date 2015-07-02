<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="cyanos" tagdir="/WEB-INF/tags" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="edu.uic.orjala.cyanos.web.servlet.MainServlet,
	edu.uic.orjala.cyanos.web.listener.AppConfigListener,
	edu.uic.orjala.cyanos.Strain,
	edu.uic.orjala.cyanos.sql.SQLStrain,
	edu.uic.orjala.cyanos.Inoc,
	edu.uic.orjala.cyanos.sql.SQLInoc,
	edu.uic.orjala.cyanos.Harvest,
	edu.uic.orjala.cyanos.sql.SQLHarvest,
	edu.uic.orjala.cyanos.Material,
	edu.uic.orjala.cyanos.sql.SQLMaterial,
	edu.uic.orjala.cyanos.Assay,
	edu.uic.orjala.cyanos.sql.SQLAssay,
	edu.uic.orjala.cyanos.Separation,
	edu.uic.orjala.cyanos.sql.SQLSeparation,
	edu.uic.orjala.cyanos.Assay,
	edu.uic.orjala.cyanos.sql.SQLAssay,
	edu.uic.orjala.cyanos.User,
	java.math.BigDecimal,
	java.text.DateFormat" %>
<!DOCTYPE html>
<html>
<head>
<cyanos:header title="Cyanos Link Objects"/>
<style type="text/css">
.content { margin: 10px; }
input { background-color: white; border-radius: 0px; }
li { border: 1px solid gray; padding: 2px; cursor: copy; margin: 2px 0px;  }
li:hover { background-color: #ddd; }
ul { list-style: none; padding: 0px; }
</style>
<script type="text/javascript" src="<%= request.getContextPath() %>/tinymce/tinymce.js"></script>
<script type="text/javascript">
function addStrainLink(id, name) {
	top.tinymce.activeEditor.insertContent("<a href='<%= request.getContextPath() %>/strain?id=" + id + "' class='notelink' id='cyanos-link:strain:" + id + "'>Strain " + id + " - " + name + "</a>");
}

function addInocLink(id, strain, text) {
	top.tinymce.activeEditor.insertContent("<a href='<%= request.getContextPath() %>/inoc?id=" + id + "' class='notelink' title='Inoculation: " + id + "' id='cyanos-link:inoc:" + id + "'>" + "[Inoculation: " + id + "] " + strain + " " + text + "</a>");
}

function addHarvLink(id, strain, text) {
	top.tinymce.activeEditor.insertContent("<a href='<%= request.getContextPath() %>/harvest?id=" + id + "' class='notelink' id='cyanos-link:harvest:" + id + "'>" + strain + " Harvest: " + id + " " + text + "</a>");
}

function addMaterialLink(id, strain, text) {
	top.tinymce.activeEditor.insertContent("<a href='<%= request.getContextPath() %>/material?id=" + id + "' class='notelink' id='cyanos-link:material:" + id + "'>" + strain + " Material: " + id + " " + text + "</a>");
}

function addSepLink(id, text) {
	top.tinymce.activeEditor.insertContent("<a href='<%= request.getContextPath() %>/separation?id=" + id + "' class='notelink' id='cyanos-link:separation:" + id + "'> Separation: " + id + " " + text + "</a>");
}
</script>
</head>
<body>
<div class="content">
<% String queryType = request.getParameter("type"); String queryString = request.getParameter("query"); 
	DateFormat dateFormat = MainServlet.DATE_FORMAT;
%>
<form><p>Record type: 
<select name="type">
<option value="strain" <%= "strain".equals(queryType) ? "selected" : "" %>>Strain</option>
<option value="inoc"<%= "inoc".equals(queryType) ? "selected" : "" %>>Inoculation</option>
<option value="harvest"<%= "harvest".equals(queryType) ? "selected" : "" %>>Harvest</option>
<option value="material"<%= "material".equals(queryType) ? "selected" : "" %>>Material</option>
<option value="sep"<%= "sep".equals(queryType) ? "selected" : "" %>>Separation</option>
<option value="assay"<%= "assay".equals(queryType) ? "selected" : "" %>>Assay</option>
<option value="compound"<%= "compound".equals(queryType) ? "selected" : "" %>>Compound</option>
</select>
<br>
Query: <input type="text" name="query" size="15" value='<%= queryString != null ? queryString : "" %>'><br><button type="submit">Search</button></p>
</form>
<ul>
<% if ( queryType != null ) { 
	
	if ( queryType.equals("strain") ) {
		if (queryString.matches("\\*") ) {
			queryString.replaceAll("\\*", "%");
		} else {
			queryString = "%" + queryString + "%";
		}
		String[] columns = {SQLStrain.NAME_COLUMN, SQLStrain.ID_COLUMN};
		String[] queries = {queryString, queryString};
		Strain strainList = SQLStrain.strainsLike(MainServlet.getSQLData(request), columns, queries, SQLStrain.SORT_ID, SQLStrain.ASCENDING_SORT);	
		strainList.beforeFirst();
		while ( strainList.next() ) {
%><li onclick="addStrainLink('<%= strainList.getID() %>', '<%= strainList.getName() %>')"><%= strainList.getID() %> - <%= strainList.getName() %></li><%			
		}
	} else if ( queryType.equals("inoc") ) {
		Inoc inocList = SQLInoc.inocsForStrain(MainServlet.getSQLData(request), queryString);
		inocList.beforeFirst();
		while ( inocList.next() ) {
			String date = dateFormat.format(inocList.getDate());
%><li onclick="addInocLink('<%= inocList.getID() %>', '<%= inocList.getStrainID() %>', '<%= date %> Volume: <%= inocList.getVolumeString() %> Media: <%= inocList.getMedia() %>')">#<%= inocList.getID() %> <%= date %> Volume: <%= inocList.getVolumeString() %> Media: <%= inocList.getMedia() %></li><%						
		}
	} else if ( queryType.equals("harvest") ) {
		Harvest harvList = SQLHarvest.harvestsForStrain(MainServlet.getSQLData(request), queryString);	
		harvList.beforeFirst();
		while ( harvList.next() ) {
			String date = dateFormat.format(harvList.getDate());
%><li onclick="addHarvLink('<%= harvList.getID() %>', '<%= harvList.getStrainID() %>', '<%= date %>')">#<%= harvList.getID() %> <%= date %></li><%
		}
	} else if ( queryType.equals("material") ) {
		Material objList = SQLMaterial.findForStrain(MainServlet.getSQLData(request), queryString);
		objList.beforeFirst();
		while ( objList.next() ) {
			String date = dateFormat.format(objList.getDate());
			String amount = SQLMaterial.autoFormatAmount(objList.getAmount(), SQLMaterial.MASS_TYPE);
%><li onclick="addMaterialLink('<%= objList.getID() %>', '<%= objList.getCultureID() %>', '<%= date %> <%= amount %>')">#<%= objList.getID() %> <%= date %> <%= amount %></li><%
		}
	} else if ( queryType.equals("sep") ) {
		Separation objList = SQLSeparation.findForStrain(MainServlet.getSQLData(request), queryString);
		objList.beforeFirst();
		while ( objList.next() ) {
			String date = dateFormat.format(objList.getDate());
			String label = objList.getTag(); if ( label == null ) { label = ""; } else { label = " " + label; }
%><li onclick="addSepLink('<%= objList.getID() %>', '<%= date %><%= label %>')">#<%= objList.getID() %> <%= date %> <%= label %></li><%
		}
	} else if ( queryType.equals("assay") ) {
		Assay objList = SQLAssay.assaysForTarget(MainServlet.getSQLData(request), queryString);
		objList.beforeFirst();
		while ( objList.next() ) {
			
		}
	}
%>
<% } %>
</ul>
</div>
</body>
</html>