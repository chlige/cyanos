<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page import="edu.uic.orjala.cyanos.Material,edu.uic.orjala.cyanos.web.servlet.MaterialServlet,
	edu.uic.orjala.cyanos.Strain,
	java.text.SimpleDateFormat,
	edu.uic.orjala.cyanos.Separation,
	edu.uic.orjala.cyanos.web.BaseForm,
	edu.uic.orjala.cyanos.Harvest,
	edu.uic.orjala.cyanos.Project" %>
<% 	String contextPath = request.getContextPath();
	Material materialObj = (Material) request.getAttribute(MaterialServlet.MATERIAL_ATTR); 	
	SimpleDateFormat dateFormat = (SimpleDateFormat) session.getAttribute("dateFormatter");
	boolean update = request.getParameter("updateMaterial") != null;
	if ( materialObj == null ) { %>
<p align='center'><b>ERROR:</b> Object not passed</p>
<% out.flush(); return; } else if ( ! materialObj.first() ) { %>
<p align='center'><b>ERROR:</b> Object not found</p>
<% out.flush(); return; } String remoteHost = materialObj.getRemoteHostID(); 
		update = ( remoteHost != null ? false : update); %>
<div CLASS="showSection" ID="view_info">
<table class="species" align='center'>
<tr><td width='125'>Serial number:</td><td><%= materialObj.getID() %></td></tr>
<tr><td>UUID:</td><td><%= materialObj.getRemoteID() %></td>
<tr><td>Source Strain:</td><td>
<% Strain culture = materialObj.getCulture(); if ( culture != null && culture.first() ) { %>
<a href="<%= contextPath %>/strain?id=<%= culture.getID() %>"><%= culture.getID() %> <i><%=culture.getName() %></i></a>
<% } else { %>
<%= materialObj.getCultureID() %>
<% }  %>
</td></tr>
<tr 
<% if ( update ) { 
	String value = request.getParameter("label");
	if (value != null && (! value.equals(materialObj.getLabel()) ) ) {
		materialObj.setLabel(value);	
%>
class="updated"
<% } } %>
><td>Label:</td><td><%= materialObj.getLabel() %></td></tr>
<tr><td>Date:</td><td><%= dateFormat.format(materialObj.getDate()) %></td></tr>
<tr><td>Amount:</td><td><%= materialObj.displayAmount() %></td></tr>
<% if ( materialObj.isExtract() ) { Harvest parent = materialObj.getHarvestForExtract(); %>
<tr><td>Parent Harvest:</td><td><a href="<%= contextPath %>/harvest?id=<%= parent.getID() %>">Harvest #<%= parent.getID() %></a> (<%= dateFormat.format(parent.getDate()) %>)</td></tr>
<tr
<% if ( update ) { 
	String value = request.getParameter("extractType");
	if (value != null && (! value.equals(materialObj.getExtractType()) ) ) {
		materialObj.setExtractType(value);	
%>
class="updated"
<% } } %>
><td>Extract Type:</td><td><c:out value="<%= materialObj.getExtractType() %>"/></td></tr>
<tr
<% if ( update ) { 
	String value = request.getParameter("extractSolvent");
	if (value != null && (! value.equals(materialObj.getExtractSolvent()) ) ) {
		materialObj.setExtractSolvent(value);	
%>
class="updated"
<% } } %>
><td>Extract Solvent:</td><td><c:out value="<%= materialObj.getExtractSolvent()%>"/></td></tr>
<tr
<% if ( update ) { 
	String value = request.getParameter("extractMethod");
	if (value != null && (! value.equals(materialObj.getExtractMethod()) ) ) {
		materialObj.setExtractMethod(value);	
%>
class="updated"
<% } } %>
><td valign="top">Extract Method:</td><td><%= BaseForm.formatStringHTML(materialObj.getExtractMethod()) %></td></tr>
<% } %>
<% Separation source = materialObj.getParentSeparation(); 
	if ( source.first() ) { 
		String name = source.getTag();
%>
<tr><td>Parent Separation:</td><td><a href="<%= contextPath %>/separation?id=<%= source.getID() %>">
<% if ( name != null && name.length() > 0 ) { %>
<%= name %> 
<% } %>
( S/N: <%= source.getID() %> )
</a></td>
<% } %>
<tr<% if ( update ) { 
	String value = request.getParameter("project");
	if (value != null && (! value.equals(materialObj.getProjectID()) ) ) {
		materialObj.setProjectID(value); %>class="updated"<% } } %>><td>Project</td><td>
<% Project aProj = materialObj.getProject(); if ( aProj != null && aProj.first() ) { %>
<a href='project?id=<%= aProj.getID() %>'><%= aProj.getName() %></a>
<% } else { %>
None
<% } %>
</td></tr>
<tr 
<% if ( update ) { 
	String value = request.getParameter("notes");
	if (value != null && (! value.equals(materialObj.getNotes()) ) ) {
		materialObj.setNotes(value);	
%>
class="updated"
<% } } %>
><td valign=top>Notes:</td><td><%= BaseForm.formatStringHTML(materialObj.getNotes()) %></td></tr>
<% if ( remoteHost != null ) { %>
<tr><td>Remote Host ID:</td><td><%= remoteHost %></td></tr></table></div>
<% } else { %>
</table>
<p align='center'><button type='button' onClick='flipDiv("info")'>Edit Values</button></p>
</div>
<div class='hideSection' id="edit_info">
<form name='editProject'>
<input type="hidden" name="id" value="<%= materialObj.getID() %>">
<table class="species" align='center'>
<tr><td width='125'>Serial number:</td><td><%= materialObj.getID() %></td></tr>
<tr><td>Source Strain:</td><td><%= materialObj.getCultureID() %></td></tr>
<tr><td>Label:</td><td><input type='text' name='label' value='<%= materialObj.getLabel() %>'></td></tr>
<tr><td>Date:</td><td><%= dateFormat.format(materialObj.getDate()) %></td></tr>
<tr><td>Amount:</td><td><%= materialObj.displayAmount() %></td></tr>
<tr><td>Project</td><td>
<jsp:include page="/includes/project-popup.jsp">
<jsp:param value="<%= materialObj.getProjectID() %>" name="project"/>
<jsp:param value="project" name="fieldName"/></jsp:include>
</td></tr>
<% if ( materialObj.isExtract() ) { Harvest parent = materialObj.getHarvestForExtract(); %>
<tr><td>Parent Harvest:</td><td><a href="<%= contextPath %>/harvest?id=<%= parent.getID() %>">Harvest #<%= parent.getID() %></a> (<%= dateFormat.format(parent.getDate()) %>)</td></tr>
<tr><td>Extract Type:</td><td><input type='text' name="extractType" value="<c:out value="<%= materialObj.getExtractType() %>"/>"></td></tr>
<tr><td>Extract Solvent:</td><td><input type='text' name="extractSolvent" value="<c:out value="<%= materialObj.getExtractSolvent() %>"/>"></td></tr>
<tr><td valign="top">Extract Method:</td><td><textarea rows="7" cols="70" name="extractMethod"><c:out value="<%= materialObj.getExtractMethod() %>" default="" /></textarea></td></tr>
<% } %>
<tr><td valign=top>Notes:</td><td><textarea rows="7" cols="70" name="notes"><c:out value="<%= materialObj.getNotes() %>" default="" /></textarea></td></tr>
<tr><td colspan="2" align="CENTER"><button type="button" name="updateMaterial" onClick="updateForm(this,'<%= MaterialServlet.INFO_FORM_DIV_ID %>')">Update</button>
<input type="RESET"></td></tr>
</table>
</form>
<p align="center"><button type='button' onClick='flipDiv("info")'>Close Form</button></p>
</div>
<% } %>
