<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page import="edu.uic.orjala.cyanos.web.servlet.CryoServlet,
	edu.uic.orjala.cyanos.sql.SQLCryoCollection,
	edu.uic.orjala.cyanos.CryoCollection,
	edu.uic.orjala.cyanos.Role,
	java.text.SimpleDateFormat,
	edu.uic.orjala.cyanos.Project,
	java.util.Date, java.util.List" %>
<% 	String contextPath = request.getContextPath();
	CryoCollection thisObject = SQLCryoCollection.load(CryoServlet.getSQLData(request), request.getParameter("collection"));
	if ( thisObject == null ) { %>
<p align='center'><b>ERROR:</b> Object not passed</p><% out.flush(); return; 
} else if ( ! thisObject.first() ) { %><p align='center'><b>ERROR:</b> Object not found</p><% out.flush(); return; } 
	SimpleDateFormat dateFormat = (SimpleDateFormat) session.getAttribute("dateFormatter");
	boolean update = ( thisObject.isAllowed(Role.WRITE) && request.getParameter("updateRecord") != null ); %>
<div CLASS="showSection" ID="view_info">
<table class="species" align='center'>
<tr><td width='125'>Collection ID:</td><td><%= thisObject.getID() %></td></tr>
<tr><td>Name:</td><td><%= thisObject.getName() %></td></tr>
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
<tr><td width='125'>Collection ID:</td><td><%= thisObject.getID() %></td></tr>
<tr><td>Name:</td><td><input name="collectionName" value="<%= thisObject.getName() %>"></td></tr>
<tr><td valign=top>Notes:</td><td><textarea rows="7" cols="70" name="notes"><c:out value="<%= thisObject.getNotes() %>" default="" /></textarea></td></tr>
<tr><td colspan="2" align="CENTER"><button type="button" name="updateRecord" onClick="updateForm(this,'view_info')">Update</button>
<input type="RESET"></td></tr>
</table>
</form>
<p align="center"><button type='button' onClick='flipDiv("info")'>Close Form</button></p>
<% } %>
</div>
