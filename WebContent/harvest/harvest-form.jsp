<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page import="edu.uic.orjala.cyanos.Material,
	edu.uic.orjala.cyanos.web.servlet.HarvestServlet,
	edu.uic.orjala.cyanos.web.servlet.InocServlet,
	edu.uic.orjala.cyanos.web.servlet.CollectionServlet,
	edu.uic.orjala.cyanos.Strain,
	edu.uic.orjala.cyanos.Inoc,
	java.text.SimpleDateFormat,
	edu.uic.orjala.cyanos.Separation,
	edu.uic.orjala.cyanos.web.BaseForm,
	edu.uic.orjala.cyanos.Harvest,
	edu.uic.orjala.cyanos.CyanosObject,
	edu.uic.orjala.cyanos.Project,
	java.util.Date,
	java.math.BigDecimal" %>
<% 	String contextPath = request.getContextPath();
	Harvest thisObject = (Harvest) request.getAttribute(HarvestServlet.HARVEST_ATTR); 	
	SimpleDateFormat dateFormat = (SimpleDateFormat) session.getAttribute("dateFormatter");
	boolean update = request.getParameter("updateHarvest") != null;
	if ( thisObject == null ) { 
		String remoteHost = thisObject.getRemoteHostID(); 
		update = ( remoteHost != null ? false : update); %>
<p align='center'><b>ERROR:</b> Object not passed</p>
<% out.flush(); return; } else if ( ! thisObject.first() ) { %>
<p align='center'><b>ERROR:</b> Object not found</p>
<% out.flush(); return; } %>
<table class="species" align='center'>
<tr><td width='125'>Serial number:</td><td><%= thisObject.getID() %></td></tr>
<tr><td>UUID:</td><td><%= thisObject.getRemoteID() %></td>
<tr><td>Source Culture:</td><td>
<% Strain culture = thisObject.getStrain(); if ( culture != null && culture.first() ) { %>
<a href="<%= contextPath %>/strain?id=<%= culture.getID() %>"><%= culture.getID() %> <i><%=culture.getName() %></i></a>
<% } else { %>
<%= thisObject.getStrainID() %>
<% }  %>
</td></tr>
<tr><td>Harvest Date:</td><td><%= dateFormat.format(thisObject.getDate()) %></td></tr>
<tr><td>Color:</td><td><%= thisObject.getColor() %></td></tr>
<tr><td>Type:</td><td><%= thisObject.getType() %></td></tr>
</table>

<p align="center"><b>Sources</b></p>
<% if ( thisObject.isFieldHarvest() ) { request.setAttribute(CollectionServlet.SEARCHRESULTS_ATTR, thisObject.getCollection()); %>
<jsp:include page="/collection/collection-list.jsp"></jsp:include>
<% } else { request.setAttribute(InocServlet.SEARCHRESULTS_ATTR, thisObject.getInoculations()); %>
<jsp:include page="/inoc/inoc-list.jsp">
<jsp:param value="1" name="samestyle"/>
</jsp:include>
<% } %>

<P></P>
<div CLASS="showSection" ID="view_info">
<table class="species" align='center'>
<tr
<% if ( update ) { 
	String value = request.getParameter("prepDate");
	if (value != null && value.length() > 0 && (! value.equals(thisObject.getPrepDateString() ) ) ) {
		thisObject.setPrepDate(value);	
%>
class="updated"
<% } } %>
><td>Prep. Date:</td><td>
<% Date prepDate = thisObject.getPrepDate(); 
	if ( prepDate != null ) { %>
<%= dateFormat.format(thisObject.getPrepDate()) %>
<% } %>
</td></tr>
<tr
<% if ( update ) { 
	String value = request.getParameter("cellMass");
	if ( value != null && value.length() > 0 ) {
		BigDecimal amount = CyanosObject.parseAmount(value, "g"); 
		if (amount != null && (! amount.equals(thisObject.getCellMass() )) ) {	
			thisObject.setCellMass(amount);
%>	
class="updated"
<% } } } %>
><td>Cell Mass:</td><td><%= CyanosObject.autoFormatAmount(thisObject.getCellMass(), Harvest.MASS_TYPE) %></td></tr>

<tr
<% if ( update ) { 
	String value = request.getParameter("mediaVol");
	if ( value != null && value.length() > 0 ) {
		BigDecimal amount = CyanosObject.parseAmount(value, "L"); 
		if (amount != null && (! amount.equals(thisObject.getMediaVolume() )) ) {	
			thisObject.setMediaVolume(amount);
%>	
class="updated"
<% } } } %>
><td>Media Volume:</td><td><%= CyanosObject.autoFormatAmount(thisObject.getMediaVolume(), Harvest.VOLUME_TYPE) %></td></tr>
<tr<% if ( update ) { 
	String value = request.getParameter("project");
	if (value != null && (! value.equals(thisObject.getProjectID()) ) ) {
		thisObject.setProjectID(value); %>class="updated"<% } } %>><td>Project</td><td>
<% Project aProj = thisObject.getProject(); if ( aProj != null && aProj.first() ) { %>
<a href='project?id=<%= aProj.getID() %>'><%= aProj.getName() %></a>
<% } else { %>
None
<% } %>
</td></tr>
<tr 
<% if ( update ) { 
	String value = request.getParameter("notes");
	if (value != null && (! value.equals(thisObject.getNotes()) ) ) {
		thisObject.setNotes(value);	
%>
class="updated"
<% } } %>
><td valign=top>Notes:</td><td><%= BaseForm.formatStringHTML(thisObject.getNotes()) %></td></tr>
<% String remoteHost = thisObject.getRemoteHostID(); 
if ( remoteHost != null ) { %>
<tr><td>Remote Host ID:</td><td><%= remoteHost %></td></tr></table></div>
<% } else { %>
</table>
<p align='center'><button type='button' onClick='flipDiv("info")'>Edit Values</button></p>
</div>
<div class='hideSection' id="edit_info">
<form name='editMaterial'>
<input type="hidden" name="id" value="<%= thisObject.getID() %>">
<table class="species" align='center'>
<tr><td>Prep. Date:</td><td>
<% 	if ( prepDate == null ) { %>
<input type="text" name="prepDate" onFocus="showDate('div_calendar','prepDate')" style='padding-bottom: 0px' id="prepDate"/>
<a onclick="showDate('div_calendar','prepDate')"><img align="MIDDLE" border="0" src="<%= contextPath %>/images/calendar.png"></a>
<div id="div_calendar" class='calendar'>
<jsp:include page="/calendar.jsp">
<jsp:param value="prepDate" name="update_field"/>
<jsp:param value="div_calendar" name="div"/>
</jsp:include>
</div>	
<% } else { %>
<%= dateFormat.format(prepDate) %>
<% } %>
</td></tr>
<tr><td>Cell Mass:</td><td>
<% BigDecimal massAmount = thisObject.getCellMass();
	if ( massAmount == null || massAmount.compareTo(BigDecimal.ZERO) == 0 ) { %>
<input type="text" name="cellMass">
<% } else { %>
<%= CyanosObject.autoFormatAmount(massAmount, Harvest.MASS_TYPE) %>
<% } %>
</td></tr>
<tr><td>Media Volume:</td><td>
<% BigDecimal volAmount = thisObject.getMediaVolume();
	if ( volAmount == null || volAmount.compareTo(BigDecimal.ZERO) == 0 ) { %>
<input type="text" name="mediaVol">
<% } else { %>
<%= CyanosObject.autoFormatAmount(volAmount, Harvest.MASS_TYPE) %>
<% } %>
</td></tr>
<tr><td>Project</td><td>
<jsp:include page="/includes/project-popup.jsp">
<jsp:param value="<%= thisObject.getProjectID() %>" name="project"/>
<jsp:param value="project" name="fieldName"/></jsp:include>
</td></tr>
<tr><td valign=top>Notes:</td><td><textarea rows="7" cols="70" name="notes"><c:out value="<%= thisObject.getNotes() %>" default="" /></textarea></td></tr>
<tr><td colspan="2" align="CENTER"><button type="button" name="updateHarvest" onClick="updateForm(this,'<%= HarvestServlet.INFO_FORM_DIV_ID %>')">Update</button>
<input type="RESET"></td></tr>
</table>
</form>
<p align="center"><button type='button' onClick='flipDiv("info")'>Close Form</button></p>
</div>
<% } %>