<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="edu.uic.orjala.cyanos.Isolation,
	edu.uic.orjala.cyanos.web.BaseForm,
	edu.uic.orjala.cyanos.web.servlet.CollectionServlet,
	java.text.DateFormat,
	java.util.Date,
	java.util.List,
	edu.uic.orjala.cyanos.Role,
	edu.uic.orjala.cyanos.Project" %>
<% 	String contextPath = request.getContextPath();
	Isolation myObject = (Isolation) request.getAttribute(CollectionServlet.ATTR_ISOLATION); 	
	if ( myObject == null ) { %>
<p align='center'><b>ERROR:</b> Object not passed</p>
<% out.flush(); return; } else if ( ! myObject.first() ) { %>
<p align='center'><b>ERROR:</b> Object not found</p>
<% out.flush(); return; } %>
<div CLASS="showSection" ID="view_info">
<% 	DateFormat dateFormat = (DateFormat) session.getAttribute("dateFormatter");
	boolean update = myObject.isAllowed(Role.WRITE) && request.getParameter("updateIsolation") != null;
	if ( update ) {
	}
%>
<table class="list" style="width:80%; margin-left:auto; margin-right:auto">
<tr><td width="100">ID:</td><td><%= myObject.getID() %></td></tr>
<tr><td>Collection:</td><td><a href="collection?col=<%= myObject.getCollectionID() %>"><%= myObject.getCollectionID() %></a></td></tr>
<tr<% if ( update ) {
	String value = request.getParameter("isoDate");
	if ( value != null && (! value.equals(myObject.getDateString()) )) {
		myObject.setDate(value); %> class="updated"<% } }
%>><td>Isolation Date:</td><td><%= dateFormat.format(myObject.getDate()) %></td></tr>
<tr<% if ( update ) {
	String value = request.getParameter("type");
	if ( value != null && (! value.equals(myObject.getType()) )) {
		myObject.setType(value); %> class="updated"<% } }
%>><td>Type:</td><td><%= myObject.getType() %></td></tr>
<tr<% if ( update ) {
	String value = request.getParameter("media");
	if ( value != null && (! value.equals(myObject.getMedia()) )) {
		myObject.setMedia(value); %> class="updated"<% } }
%>><td>Media:</td><td><%= myObject.getMedia() %></td></tr>
<tr<% if ( update ) {
	String value = request.getParameter("parent");
	if ( value != null && (! value.equals(myObject.getParentID()) )) {
		myObject.setParentID(value); %> class="updated"<% } }
%>><td>Parent Isolation:</td><td>
<% if (myObject.getParentID() != null ) { %><a href="collection?id=<%= myObject.getParentID() %>"><%= myObject.getParentID() %></a>
<% } else { %>NONE<% } %></td></tr>
<tr<% if ( update ) { 
	String value = request.getParameter("project");
	if (value != null && (! value.equals(myObject.getProjectID()) ) ) {
		myObject.setProjectID(value); %> class="updated"<% } } %>><td>Project:</td><td>
<% Project aProj = myObject.getProject(); if ( aProj != null && aProj.first() ) { %>
<a href='project?id=<%= aProj.getID() %>'><%= aProj.getName() %></a><% } else { %>None<% } %></td></tr>
<tr<% if ( update ) { 
	String value = request.getParameter("notes");
	if (value != null && (! value.equals(myObject.getNotes()) ) ) {
		myObject.setNotes(value);	
%> class="updated"<% } } %>><td valign=top>Notes:</td><td><%= BaseForm.formatStringHTML(myObject.getNotes()) %></td></tr>
</table>
<% if ( myObject.isAllowed(Role.WRITE) ) { %>
<p align='center'><button type='button' onClick='flipDiv("info")'>Edit Values</button></p>
</div>
<div class='hideSection' id="edit_info">
<form name='editStrain' method="post" action="collection">
<input type="hidden" name="id" value="<%= myObject.getID() %>">
<table class="species" style="width:80%; margin-left:auto; margin-right:auto">
<% Date strainDate = myObject.getDate(); %>
<tr><td>Isolation Date:</td><td><input type="text" name="isoDate" onFocus="showDate('div_calendar','isoDate')" style='padding-bottom: 0px' value='<fmt:formatDate value="<%= ( strainDate != null ? strainDate : new Date()) %>" pattern="yyyy-MM-dd"/>' id="colDate"/>
<a onclick="showDate('div_calendar','isoDate')"><img align="MIDDLE" border="0" src="<%= contextPath %>/images/calendar.png"></a>
<div id="div_calendar" class='calendar'>
<jsp:include page="/calendar.jsp">
<jsp:param value="isoDate" name="update_field"/>
<jsp:param value="div_calendar" name="div"/>
</jsp:include>
</div>
</td></tr>
<tr><td>Type:</td><td>
<select name="type"><% List<String> types = (List<String>) request.getAttribute(CollectionServlet.ATTR_TYPES); 
	String currentType = myObject.getType();
	for (String type: types) { %><option<%= (type.equals(currentType) ? " selected" : "") %>><%= type %></option><% } %>
</select></td></tr>
<tr><td>Media:</td><td><input type="text" name="media" value="<c:out value="<%= myObject.getMedia() %>"/>" size="40"></td></tr>
<tr><td>Parent:</td><td>
<% Isolation parents = myObject.possibleParents();  String currentParent = myObject.getParentID();
	if ( parents != null && parents.first() ) {
%><select name="parent"><option value="">NONE</option>
<% parents.beforeFirst(); while (parents.next()) { %><option<%=( parents.getID().equals(currentParent) ? " selected" : "" ) %>><%= parents.getID() %></option><% } %>
</select>
<% } %>
</td></tr>
<tr><td>Project:</td><td>
<jsp:include page="/includes/project-popup.jsp">
<jsp:param value="<%= myObject.getProjectID() %>" name="project"/>
<jsp:param value="project" name="fieldName"/></jsp:include>
</td></tr>
<tr><td valign=top>Notes:</td><td><textarea rows="7" cols="70" name="notes"><c:out value="<%= myObject.getNotes() %>" default="" /></textarea></td></tr>
<tr><td colspan="2" align="CENTER"><button type="button" name="updateCollection" onClick="updateForm(this,'<%= CollectionServlet.INFO_FORM_DIV_ID %>')">Update</button>
<input type="RESET"></td></tr>
</table>
</form>
<p align="center"><button type='button' onClick='flipDiv("info")'>Close Form</button></p>
<% } %>
</div>
