<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="cyanos" tagdir="/WEB-INF/tags" %>
<%@ page import="edu.uic.orjala.cyanos.Sample,
	edu.uic.orjala.cyanos.SampleCollection,
	edu.uic.orjala.cyanos.web.servlet.SampleServlet,
	java.text.SimpleDateFormat, java.util.List" %>
<!DOCTYPE html>
<html style="min-height:100%">
<head>
<cyanos:header title="Add new sample collection"/>
</head>
<cyanos:menu helpModule="sample"/>

<div class='content' style="padding-bottom: 60px;">
<p align="CENTER"><font size="+3" >Add a new sample collection</font></p>
<hr width="90%">
<form name='editProject' method="post">
<table class="species" align='center'>
<tr><td>Collection ID:</td><td><input type="text" name="<%= SampleServlet.PARAM_COLLECTION_ID %>"></td></tr>
<tr><td>Library:</td><td><% List<String> libraries = (List<String>) request.getAttribute(SampleServlet.ATTR_LIBRARIES); %>
<select name="<%= SampleServlet.PARAM_LIBRARY %>">
<option value="">NEW LIBRARY &rarr;</option>
<% for ( String lib : libraries ) { %><option><%= lib %></option><% } %>
</select><input name="new_library"></td></tr>
<tr><td>Label:</td><td><input type='text' name='label'></td></tr>
<tr><td>Size:</td><td><input type='text' name='box_len' size="5"> &times; <input type='text' size="5" name='width'>
<input type="checkbox" name="asList" onClick="this.form.elements['box_len'].disabled = this.checked; this.form.elements['width'].disabled = this.checked; "> Unorganized list</td></tr>
<tr><td valign=top>Notes:</td><td><textarea rows="7" cols="70" name="notes"></textarea></td></tr>
<tr><td colspan="2" align="CENTER"><button type="submit" name="addCol">Update</button><input type="RESET"></td></tr>
</table>
</form>
</div>
</body>
</html>