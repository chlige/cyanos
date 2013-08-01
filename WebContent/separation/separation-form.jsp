<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page import="edu.uic.orjala.cyanos.Separation,
	edu.uic.orjala.cyanos.Project,
	edu.uic.orjala.cyanos.Role,
	edu.uic.orjala.cyanos.CyanosObject,edu.uic.orjala.cyanos.web.servlet.SeparationServlet,
	edu.uic.orjala.cyanos.web.BaseForm,
	java.text.SimpleDateFormat" %>
<% 	Separation myObj = (Separation) request.getAttribute(SeparationServlet.SEP_OBJECT);	
	SimpleDateFormat dateFormat = (SimpleDateFormat) session.getAttribute("dateFormatter");
	if ( myObj == null ) { %>
<p align='center'><b>ERROR:</b> Object not passed</p>
<% out.flush(); return; } else if ( ! myObj.first() ) { %>
<p align='center'><b>ERROR:</b> Results missing</p>
<%  out.flush(); return; }  
	boolean update = ( myObj.isAllowed(Role.WRITE) && request.getParameter("updateSep") != null ); %>
<div CLASS="showSection" ID="view_info">
<table class="species" align='center'>
<tr><td width='100'>Serial number:</td><td><%= myObj.getID() %></td></tr>
<tr><td>Tag:</td><td
<% if ( update ) { 
	String value = request.getParameter("tag");
	if (value != null && (! value.equals(myObj.getTag() ) ) ) {
		myObj.setTag(value);	
 %>
class="updated"
<% } } %>
><%= myObj.getTag() %></td></tr>
<tr><td>Date:</td><td><%= dateFormat.format(myObj.getDate()) %></td></tr>
<tr><td>Project</td><td>
<% Project aProj = myObj.getProject(); if ( aProj != null && aProj.first() ) { %>
<a href='project?id=<%= aProj.getID() %>'><%= aProj.getName() %></a>
<% } else { %>
None
<% } %>
</td></tr>
<tr><td>Stationary Phase:</td><td
<% if ( update ) { 
	String value = request.getParameter("sphase");
	if (value != null && (! value.equals(myObj.getStationaryPhase() ) ) ) {
		myObj.setStationaryPhase(value);	
 %>
class="updated"
<% } } %>
><%= myObj.getStationaryPhase() %></td></tr>
<tr><td>Mobile Phase:</td><td
<% if ( update ) { 
	String value = request.getParameter("mphase");
	if (value != null && (! value.equals(myObj.getMobilePhase() ) ) ) {
		myObj.setMobilePhase(value);	
 %>
class="updated"
<% } } %>
><%= myObj.getMobilePhase() %></td></tr>
<tr><td valign="top">Method:</td><td
<% if ( update ) { 
	String value = request.getParameter("method");
	if (value != null && (! value.equals(myObj.getMethod() ) ) ) {
		myObj.setMethod(value);	
 %>
class="updated"
<% } } %>
><%= BaseForm.formatStringHTML(myObj.getMethod()) %></td></tr>
<tr><td valign=top>Notes:</td><td
<% if ( update ) { 
	String value = request.getParameter("notes");
	if (value != null && (! value.equals(myObj.getNotes() ) ) ) {
		myObj.setNotes(value);	
 %>
class="updated"
<% } } %>
><%= BaseForm.formatStringHTML(myObj.getNotes()) %></td></tr>
</table>
<p align='center'><button type='button' onClick='flipDiv("info")'>Edit Values</button></p>
</div>
<div class='hideSection' id="edit_info">
<form name='editMaterial' method="POST">
<table class="species" align='center'>
<tr><td width='100'>Serial number:</td><td><%= myObj.getID() %></td></tr>
<tr><td>Tag:</td><td><input type="text" name="tag" value="<%= myObj.getTag() %>"></td></tr>
<tr><td>Date:</td><td><%= dateFormat.format(myObj.getDate()) %></td></tr>
<tr><td>Project</td><td>
<% if ( aProj != null && aProj.first() ) { %>
<a href='project?id=<%= aProj.getID() %>'><%= aProj.getName() %></a>
<% } else { %>
None
<% } %>
</td></tr>
<tr><td>Stationary Phase:</td><td><input type="text" name="sphase" value="<%= myObj.getStationaryPhase() %>"></td></tr>
<tr><td>Mobile Phase:</td><td><input type="text" name="mphase" value="<%= myObj.getMobilePhase() %>"></td></tr>
<tr><td>Method:</td><td><textarea rows="7" cols="70" name="method"><c:out value="<%= myObj.getMethod() %>" default="" /></textarea>
</td></tr>
<tr><td valign=top>Notes:</td><td><textarea rows="7" cols="70" name="notes"><c:out value="<%= myObj.getNotes() %>" default="" /></textarea></td></tr>
<tr><td colspan="2" align="CENTER"><input type="SUBMIT" name="updateSep" value="Update"><input type="RESET"></td></tr>
</table>
</form>
<p align='center'><button type='button' onClick='flipDiv("info")'>Close Form</button></p>
</div>
