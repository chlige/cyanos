<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page import="edu.uic.orjala.cyanos.Sample,edu.uic.orjala.cyanos.web.servlet.SampleServlet,
	edu.uic.orjala.cyanos.sql.SQLSample,
	edu.uic.orjala.cyanos.Strain,
	edu.uic.orjala.cyanos.SampleCollection,
	edu.uic.orjala.cyanos.Material,
	java.text.SimpleDateFormat,
	edu.uic.orjala.cyanos.web.BaseForm,
	edu.uic.orjala.cyanos.Project,
	edu.uic.orjala.cyanos.Role,
	java.math.BigDecimal, java.util.Date" %>
<% 	String contextPath = request.getContextPath();
	Sample sampleObj = (Sample) request.getAttribute(SampleServlet.SAMPLE_ATTR); 
	SimpleDateFormat dateFormat = (SimpleDateFormat) session.getAttribute("dateFormatter");

	if ( sampleObj == null ) { %>
<p align='center'><b>ERROR:</b> Object not passed</p>
<% out.flush(); return; } else if ( ! sampleObj.first() ) { %>
<p align='center'><b>ERROR:</b> Object not found</p>
<% out.flush(); return; } 
	Material parentMaterial = sampleObj.getParentMaterial();  	
	Date removedDate = sampleObj.getRemovedDate();
	boolean update = ( request.getParameter("updateSample") != null && removedDate == null ); 
	if ( removedDate != null ) { %><h3 align="center">Sample removed on <%= dateFormat.format(removedDate) %> by <%= sampleObj.getRemovedByID() %></h3><% } %>
<div CLASS="showSection" ID="view_info">
<table class="species" align='center'>
<tr><td>Serial number:</td><td><%= sampleObj.getID() %></td></tr>
<% SampleCollection myCol = sampleObj.getCollection(); %>
<tr><td>Collection:</td><td><a href="?col=<%= myCol.getID() %>"><%= myCol.getName() %></a></td></tr>
<tr><td>Parent Material:</td><td><a href="material?id=<%= parentMaterial.getID() %>"><%= parentMaterial.getLabel() %></a></td></tr>
<tr 
<% if ( update ) { 
	String value = request.getParameter("label");
	if (value != null && (! value.equals(sampleObj.getName()) ) ) {
		sampleObj.setName(value);	
%>
class="updated"
<% } } %>
><td>Label:</td><td><%= sampleObj.getName() %></td></tr>
<tr><td>Creation Date:</td><td><%= dateFormat.format(sampleObj.getDate()) %></td></tr>
<tr 
<% if ( update ) { 
	String value = request.getParameter("unit");
	if (value != null && (! value.equals(sampleObj.getBaseUnit()) ) ) {
		sampleObj.setBaseUnit(value);	
%>
class="updated"
<% } } %>
><td>Default unit:</td><td><%= sampleObj.getBaseUnit() %></td></tr>
<tr 
<% if ( update ) { 
	String value = request.getParameter("vial_wt");
	if (value != null && (! value.equals(sampleObj.getVialWeight()) ) ) {
		sampleObj.setVialWeight(value);	
%>
class="updated"
<% } } %>
><td>Vial weight:</td><td><%= sampleObj.getVialWeight()  %></td></tr>
<% BigDecimal conc = sampleObj.getConcentration();  %>
<tr><td>Concentration (mg/mL):</td><td><%= conc.compareTo(BigDecimal.ZERO) == 0 ? "Neat" :  SQLSample.autoFormatAmount(conc, SQLSample.CONCENTRATION_TYPE) %></td></tr>
<tr<% if ( update ) { 
	String value = request.getParameter("project");
	if (value != null && (! value.equals(sampleObj.getProjectID()) ) ) {
		sampleObj.setProjectID(value); %>class="updated"<% } } %>><td>Project</td><td>
<% Project aProj = sampleObj.getProject(); if ( aProj != null && aProj.first() ) { %>
<a href='project?id=<%= aProj.getID() %>'><%= aProj.getName() %></a>
<% } else { %>
None
<% } %>
</td></tr>
<tr 
<% if ( update ) { 
	String value = request.getParameter("notes");
	if (value != null && (! value.equals(sampleObj.getNotes()) ) ) {
		sampleObj.setNotes(value);	
%>
class="updated"
<% } } %>
><td valign=top>Notes:</td><td><%= BaseForm.formatStringHTML(sampleObj.getNotes()) %></td></tr></table>
<% if ( sampleObj.isAllowed(Role.WRITE) && removedDate == null ) { %>
<p align='center'><button type='button' onClick='flipDiv("info")'>Edit Values</button></p>
</div>
<div class='hideSection' id="edit_info">
<form name='editProject'>
<input type="hidden" name="id" value="<%= sampleObj.getID() %>">
<table class="species" align='center'>
<tr><td>Serial number:</td><td><%= sampleObj.getID() %></td></tr>
<tr><td>Collection:</td><td><a href="?col=<%= myCol.getID() %>"><%= myCol.getName() %></a></td></tr>
<tr><td>Parent Material:</td><td><%= parentMaterial.getLabel() %></td></tr>
<tr><td>Label:</td><td><input type='text' name='label' value='<%= sampleObj.getName() %>'></td></tr>
<tr><td>Date:</td><td><%= dateFormat.format(sampleObj.getDate()) %></td></tr>
<tr><td>Default unit:</td><td><input type='text' name='unit' value="<%= sampleObj.getBaseUnit() %>"></td></tr>
<tr><td>Vial weight:</td><td><input type='text' name='vial_wt' value="<%= sampleObj.getVialWeight() %>"></td></tr>
<tr><td>Concentration (mg/mL):</td><td><%= conc.compareTo(BigDecimal.ZERO) == 0 ? "Neat" :  SQLSample.autoFormatAmount(conc, SQLSample.CONCENTRATION_TYPE) %></td></tr>
<tr><td>Project</td><td>
<jsp:include page="/includes/project-popup.jsp">
<jsp:param value="<%= sampleObj.getProjectID() %>" name="project"/>
<jsp:param value="project" name="fieldName"/></jsp:include>
</td></tr>
<tr><td valign=top>Notes:</td><td><textarea rows="7" cols="70" name="notes"><c:out value="<%= sampleObj.getNotes() %>" default="" /></textarea></td></tr>
<tr><td colspan="2" align="CENTER"><button type="button" name="updateSample" onClick="updateForm(this,'<%= SampleServlet.DIV_INFO_FORM_ID %>')">Update</button>
<input type="RESET"></td></tr>
</table>
</form>
<p align="center"><button type='button' onClick='flipDiv("info")'>Close Form</button></p>
</div>
<% } %>
</div>
