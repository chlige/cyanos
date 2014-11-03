<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page import="edu.uic.orjala.cyanos.web.servlet.CryoServlet,
	edu.uic.orjala.cyanos.sql.SQLCryo,
	edu.uic.orjala.cyanos.Cryo,
	edu.uic.orjala.cyanos.Strain,
	edu.uic.orjala.cyanos.Inoc,
	edu.uic.orjala.cyanos.Role,
	java.text.SimpleDateFormat,
	edu.uic.orjala.cyanos.Project,
	java.util.Date, java.util.List" %>
<% 	String contextPath = request.getContextPath();
	Cryo thisObject = SQLCryo.load(CryoServlet.getSQLData(request), request.getParameter("id"));
	if ( thisObject == null ) { %>
<%--	String remoteHost = thisObject.getRemoteHostID(); 
		update = ( remoteHost != null ? false : update);  --%>
<p align='center'><b>ERROR:</b> Object not passed</p><% out.flush(); return; 
} else if ( ! thisObject.first() ) { %><p align='center'><b>ERROR:</b> Object not found</p><% out.flush(); return; } 
	SimpleDateFormat dateFormat = (SimpleDateFormat) session.getAttribute("dateFormatter");
	boolean update = ( thisObject.isAllowed(Role.WRITE) && request.getParameter("updateRecord") != null ); %>
<div CLASS="showSection" ID="view_info">
<table class="species" align='center'>
<tr><td width='125'>Serial number:</td><td><%= thisObject.getID() %></td></tr>
<%--  <tr><td>UUID:</td><td><%= thisObject.getRemoteID() %></td> --%>
<tr><td>Source Strain:</td><td><% Strain culture = thisObject.getStrain(); if ( culture != null && culture.first() ) { %>
<a href="<%= contextPath %>/strain?id=<%= culture.getID() %>"><%= culture.getID() %> <i><%=culture.getName() %></i></a>
<% } else { out.print(thisObject.getCultureID()); } %></td></tr>
<tr><td>Preservation Date:</td><td><%= dateFormat.format(thisObject.getDate()) %></td></tr>
<% Inoc parent = thisObject.getSourceInoc(); if ( parent != null && parent.first() ) { %><tr><td>Parent:</td><td><a href="inoc?id=<%= parent.getID() %>">Inoc # <%= parent.getID() %> - <%= dateFormat.format(parent.getDate()) %> - <%= parent.getVolumeString() %></a></td></tr><% } %>
<tr>
<tr<% if ( update ) { 
	String value = request.getParameter("notes");
	if (value != null && (! value.equals(thisObject.getNotes()) ) ) {
		thisObject.setNotes(value);	
%> class="updated"<% } } %>><td valign=top>Notes:</td><td><%= CryoServlet.formatStringHTML(thisObject.getNotes()) %></td></tr>
</table>
<% if ( thisObject.isAllowed(Role.WRITE) ) { %>
<p align='center'><button type='button' onClick='flipDiv("info")'>Edit Values</button></p>
</div><div class='hideSection' id="edit_info">
<form name='editMaterial'>
<table class="species" align='center'>
<tr><td width='125'>Serial number:</td><td><input type="hidden" name="id" value="<%= thisObject.getID() %>"><%= thisObject.getID() %></td></tr>
<tr><td>Source Strain:</td><td><% if ( culture != null && culture.first() ) { %><%= culture.getID() %> <i><%=culture.getName() %></i><% } else { out.print(thisObject.getCultureID()); } %></td></tr>
<tr><td>Preservation Date:</td><td><%= dateFormat.format(thisObject.getDate()) %></td></tr>
<% if ( parent != null && parent.first() ) { %><tr><td>Parent:</td><td><a href="inoc?id=<%= parent.getID() %>">Inoc # <%= parent.getID() %> - <%= dateFormat.format(parent.getDate()) %> - <%= parent.getVolumeString() %></a></td></tr><% } %>
<tr><td valign=top>Notes:</td><td><textarea rows="7" cols="70" name="notes"><c:out value="<%= thisObject.getNotes() %>" default="" /></textarea></td></tr>
<tr><td colspan="2" align="CENTER"><button type="button" name="updateRecord" onClick="updateForm(this,'view_info')">Update</button>
<input type="RESET"></td></tr>
</table>
</form>
<p align="center"><button type='button' onClick='flipDiv("info")'>Close Form</button></p>
<% } %>
</div>
