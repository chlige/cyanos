<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page import="edu.uic.orjala.cyanos.Sample,edu.uic.orjala.cyanos.web.servlet.SampleServlet,
	edu.uic.orjala.cyanos.SampleCollection,
	edu.uic.orjala.cyanos.web.BaseForm,
	edu.uic.orjala.cyanos.Role,
	java.util.List" %>
<% 	String contextPath = request.getContextPath();
	SampleCollection collectionObj = (SampleCollection) request.getAttribute(SampleServlet.COLLECTION_ATTR); 
	if ( collectionObj == null ) { %>
<p align='center'><b>ERROR:</b> Object not passed</p>
<% out.flush(); return; } else if ( ! collectionObj.first() ) { %>
<p align='center'><b>ERROR:</b> Object not found</p>
<% out.flush(); return; } 
	boolean update = ( request.getParameter("updateSampleCol") != null || request.getParameter("addCol") != null ); %>
<div CLASS="showSection" ID="view_info">
<table class="species" align='center'>
<tr><td>Collection ID:</td><td><%= collectionObj.getID() %></td></tr>
<tr<% if ( update ) { 
	String value = request.getParameter("library");
	if (value != null && (! value.equals(collectionObj.getLibrary()) ) ) {
		if (value.length() < 1 )
			value = request.getParameter("new_library");
		if ( value.length() > 0 ) {
			collectionObj.setLibrary(value);	
%> class="updated"<% } } } %>><td>Library:</td><td><a href="?library=<%= collectionObj.getLibrary() %>"><%= collectionObj.getLibrary() %></a></td></tr>
<tr<% if ( update ) { 
	String value = request.getParameter("label");
	if (value != null && (! value.equals(collectionObj.getName()) ) ) {
		collectionObj.setName(value);	
%> class="updated"<% } } %>><td>Label:</td><td><%= collectionObj.getName() %></td></tr>
<tr<% if ( update ) { 
	boolean updated = false;
	int currLen = collectionObj.getLength();
	int currWid = collectionObj.getWidth();
	if ( request.getParameter("asList") != null ) {
		if ( currLen != 0 || currWid != 0 ) {
			collectionObj.setLength(0);
			collectionObj.setWidth(0);
			updated = true;
		}
	} else {
		String value = request.getParameter("box_len");
		if ( value != null && (! value.equals(String.valueOf(currLen)))) {
			updated = true;
			collectionObj.setLength(Integer.parseInt(value));
		}
		value = request.getParameter("width");
		if ( value != null && (! value.equals(String.valueOf(currWid)))) {
			updated = true;
			collectionObj.setWidth(Integer.parseInt(value));
		}		
	}
	
	if ( updated ) {%> class="updated"<% } } %>><td>Size:</td><td><%= ( collectionObj.isBox() ? String.format("%d &times; %d", collectionObj.getLength(), collectionObj.getWidth()) : "Unorganized list" ) %></td></tr>
<tr 
<% if ( update ) { 
	String value = request.getParameter("notes");
	if (value != null && (! value.equals(collectionObj.getNotes()) ) ) {
		collectionObj.setNotes(value);	
%>
class="updated"
<% } } %>
><td valign=top>Notes:</td><td><%= BaseForm.formatStringHTML(collectionObj.getNotes()) %></td></tr></table>
<% if ( collectionObj.isAllowed(Role.WRITE) ) { %>
<p align='center'><button type='button' onClick='flipDiv("info")'>Edit Values</button></p>
</div>
<div class='hideSection' id="edit_info">
<form name='editProject'>
<input type="hidden" name="col" value="<%= collectionObj.getID() %>">
<table class="species" align='center'>
<tr><td>Collection ID:</td><td><%= collectionObj.getID() %></td></tr>
<tr><td>Library:</td><td><% List<String> libraries = (List<String>) request.getAttribute(SampleServlet.ATTR_LIBRARIES); 
	String library = collectionObj.getLibrary();
%>
<select name="<%= SampleServlet.PARAM_LIBRARY %>">
<option>NEW LIBRARY &rarr;</option>
<% for ( String lib : libraries ) { %><option<%= ( lib.equals(library) ? " selected": "") %>><%= lib %></option><% } %>
</select><input name="new_library"></td></tr>
<tr><td>Label:</td><td><input type='text' name='label' value='<%= collectionObj.getName() %>'></td></tr>
<tr><td>Size:</td><td><input type='text' name='box_len' size="5" <%= collectionObj.isBox() ? String.format("value=\"%d\"", collectionObj.getLength()) : "disabled" %>> &times; <input type='text' size="5" name='width' <%= collectionObj.isBox() ? String.format("value=\"%d\"", collectionObj.getWidth()) : "disabled" %>>
<input type="checkbox" name="asList" <%= collectionObj.isList() ? "checked" : "" %> onClick="this.form.elements['box_len'].disabled = this.checked; this.form.elements['width'].disabled = this.checked; "> Unorganized list</td></tr>
<tr><td valign=top>Notes:</td><td><textarea rows="7" cols="70" name="notes"><c:out value="<%= collectionObj.getNotes() %>" default="" /></textarea></td></tr>
<tr><td colspan="2" align="CENTER"><button type="button" name="updateSampleCol" onClick="updateForm(this,'<%= SampleServlet.DIV_COLLECTION_INFO_FORM_ID %>')">Update</button>
<input type="RESET"></td></tr>
</table>
</form>
<p align="center"><button type='button' onClick='flipDiv("info")'>Close Form</button></p>
</div>
<% } %>
</div>
