<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="cyanos" tagdir="/WEB-INF/tags" %>
<%@ page import="edu.uic.orjala.cyanos.web.servlet.InocServlet,
	edu.uic.orjala.cyanos.sql.SQLData,
	edu.uic.orjala.cyanos.sql.SQLInoc,
	edu.uic.orjala.cyanos.Strain,
	edu.uic.orjala.cyanos.Inoc,
	edu.uic.orjala.cyanos.Role,
	java.text.SimpleDateFormat,
	edu.uic.orjala.cyanos.web.BaseForm,
	edu.uic.orjala.cyanos.Harvest,
	edu.uic.orjala.cyanos.CyanosObject,
	edu.uic.orjala.cyanos.Project,
	java.util.Date, java.util.List" %>
<% 	String contextPath = request.getContextPath();
	Inoc thisObject = (Inoc) request.getAttribute(InocServlet.ATTR_INOC_OBJECT); 	
	if ( thisObject == null ) { %>
<%--	String remoteHost = thisObject.getRemoteHostID(); 
		update = ( remoteHost != null ? false : update);  --%>
<p align='center'><b>ERROR:</b> Object not passed</p><% out.flush(); return; 
} else if ( ! thisObject.first() ) { %><p align='center'><b>ERROR:</b> Object not found</p><% out.flush(); return; } 
	SimpleDateFormat dateFormat = (SimpleDateFormat) session.getAttribute("dateFormatter");
	boolean update = ( thisObject.isAllowed(Role.WRITE) && request.getParameter("updateInoc") != null ); %>
<div CLASS="showSection" ID="view_info">
<table class="species" align='center'>
<tr><td width='125'>Serial number:</td><td><%= thisObject.getID() %></td></tr>
<%--  <tr><td>UUID:</td><td><%= thisObject.getRemoteID() %></td> --%>
<tr><td>Source Strain:</td><td><% Strain culture = thisObject.getStrain(); if ( culture != null && culture.first() ) { %>
<a href="<%= contextPath %>/strain?id=<%= culture.getID() %>"><%= culture.getID() %> <i><%=culture.getName() %></i></a>
<% } else { out.print(thisObject.getStrainID()); } %></td></tr>
<tr><td>Inoculation Date:</td><td><%= dateFormat.format(thisObject.getDate()) %></td></tr>
<% Inoc parent = thisObject.getParent(); if ( parent != null && parent.first() ) { %><tr><td>Parent:</td><td><a href="inoc?id=<%= parent.getID() %>">Inoc # <%= parent.getID() %> - <%= dateFormat.format(parent.getDate()) %> - <%= CyanosObject.autoFormatAmount(parent.getVolume(), Inoc.VOLUME_TYPE) %></a></td></tr><% } %>
<tr><td>Media:</td><td><c:out value="<%= thisObject.getMedia() %>"></c:out></td></tr>
<tr><td>Volume:</td><td><%= CyanosObject.autoFormatAmount(thisObject.getVolume(), Inoc.VOLUME_TYPE) %></td></tr>
<tr<% Date removeDate = thisObject.getRemoveDate(); 
if ( update ) { 
	String value = request.getParameter("removedDate");
	if (value != null && value.length() > 1 && removeDate == null ) {
		thisObject.setRemovedDate(value);	
%> class="updated"<% } } %>><td>Removed Date:</td><td><% if ( removeDate != null ) { out.print(dateFormat.format(removeDate)); } %></td></tr>
<tr<% if ( update ) { 
	String value = request.getParameter("fate");
	if (value != null && thisObject.getFate() == null ) {
		thisObject.setFate(value); 
%> class="updated"<% } } %>><td>Fate:</td><td><c:out value="<%= thisObject.getFate() %>"></c:out></td></tr>
<% Harvest harvest = thisObject.getHarvest(); 
	if ( harvest != null && harvest.first() ) { 
%><tr><td>Harvest:</td><td><a href="harvest?id=<%= harvest.getID() %>">Harvest #<%= harvest.getID() %> - <%= dateFormat.format(harvest.getDate()) %></a></td></tr><%
} 
%><tr<% if ( update ) { 
	String value = request.getParameter("project");
	if (value != null && (! value.equals(thisObject.getProjectID()) ) ) {
		thisObject.setProjectID(value); 
%> class="updated"<% } } %>><td>Project</td><td>
<% Project aProj = thisObject.getProject(); 
if ( aProj != null && aProj.first() ) { %><a href='project?id=<%= aProj.getID() %>'><%= aProj.getName() %></a><% } else { out.print("None"); } %></td></tr>
<tr<% if ( update ) { 
	String value = request.getParameter("notes");
	if (value != null && (! value.equals(thisObject.getNotes()) ) ) {
		thisObject.setNotes(value);	
%> class="updated"<% } } %>><td valign=top>Notes:</td><td><%= BaseForm.formatStringHTML(thisObject.getNotes()) %></td></tr>
</table>
<% if ( thisObject.isAllowed(Role.WRITE) ) { %>
<p align='center'><button type='button' onClick='flipDiv("info")'>Edit Values</button></p>
</div><div class='hideSection' id="edit_info">
<form name='editMaterial'>
<table class="species" align='center'>
<tr><td width='125'>Serial number:</td><td><input type="hidden" name="id" value="<%= thisObject.getID() %>"><%= thisObject.getID() %></td></tr>
<tr><td>Source Strain:</td><td><% if ( culture != null && culture.first() ) { %><%= culture.getID() %> <i><%=culture.getName() %></i><% } else { out.print(thisObject.getStrainID()); } %></td></tr>
<tr><td>Inoculation Date:</td><td><%= dateFormat.format(thisObject.getDate()) %></td></tr>
<% if ( parent != null && parent.first() ) { %><tr><td>Parent:</td><td><a href="inoc?id=<%= parent.getID() %>">Inoc # <%= parent.getID() %> - <%= dateFormat.format(parent.getDate()) %> - <%= CyanosObject.autoFormatAmount(parent.getVolume(), Inoc.VOLUME_TYPE) %></a></td></tr><% } %>
<tr><td>Media:</td><td><%= thisObject.getMedia() %></td></tr>
<tr><td>Volume:</td><td><%= CyanosObject.autoFormatAmount(thisObject.getVolume(), Inoc.VOLUME_TYPE) %></td></tr>
<tr><td>Removed Date:</td><td><% if ( removeDate == null ) { %><cyanos:calendar-field fieldName="removedDate"/>
<% } else { out.print(dateFormat.format(removeDate)); } %></td></tr>
<% String fate = thisObject.getFate(); %>
<tr><td>Fate:</td><td>
<% if ( fate == null || fate.equals(Inoc.FATE_STOCK) ) { 
 	List<String> fates = SQLInoc.fates((SQLData)request.getAttribute(InocServlet.DATASOURCE));
%><select name="fate">
<option value="">NONE</option>
<% for ( String aFate : fates ) { %>
<option value="<%= aFate %>" <%= ( aFate.equals(fate) ? "selected" : "" ) %>><%= aFate.substring(0, 1).toUpperCase().concat(aFate.substring(1).toLowerCase()) %></option>
<% } %></select><% } else { out.print(fate); } %></td></tr>
<tr><td>Project</td><td><cyanos:project-popup fieldName="project" project="<%= thisObject.getProjectID() %>"/></td></tr>
<tr><td valign=top>Notes:</td><td><textarea rows="7" cols="70" name="notes"><c:out value="<%= thisObject.getNotes() %>" default="" /></textarea></td></tr>
<tr><td colspan="2" align="CENTER"><button type="button" name="updateInoc" onClick="updateForm(this,'<%= InocServlet.INFO_FORM_DIV_ID %>')">Update</button>
<input type="RESET"></td></tr>
</table>
</form>
<p align="center"><button type='button' onClick='flipDiv("info")'>Close Form</button></p>
<% } %>
</div>
