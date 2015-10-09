<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="cyanos" tagdir="/WEB-INF/tags" %>
<%@ page import="edu.uic.orjala.cyanos.BasicObject,
	edu.uic.orjala.cyanos.Harvest,
	edu.uic.orjala.cyanos.Role,
	edu.uic.orjala.cyanos.Material,
	edu.uic.orjala.cyanos.Material.ExtractProtocol,
	edu.uic.orjala.cyanos.Separation,edu.uic.orjala.cyanos.web.servlet.HarvestServlet,edu.uic.orjala.cyanos.web.servlet.MaterialServlet,
	edu.uic.orjala.cyanos.web.BaseForm,
	edu.uic.orjala.cyanos.CyanosObject,
	edu.uic.orjala.cyanos.DataException,
	java.text.SimpleDateFormat,
	java.util.List,
	java.math.BigDecimal" %>

<% String contextPath = request.getContextPath();
	String div = request.getParameter("div"); %>
<div id="<%=div %>">
<%
	Harvest source = (Harvest) request.getAttribute(HarvestServlet.HARVEST_ATTR);
	if ( source.isAllowed(Role.WRITE) ) { %>

<form>
<input type="hidden" name="id" value="<%= source.getID() %>">
<input type="hidden" name="div" value="<%= div %>">
<% if ( request.getParameter("showExtractForm") != null ) { %>
<table class="species" align="center">
<tr><td>Label:</td><td><input type="text" name="label" value="<%= source.getStrainID() %>"></td></tr>
<tr><td>Date:</td><td><cyanos:calendar-field fieldName="extractDate"/></td></tr>
<tr><td>Amount:</td><td><input type="text" name="amount"></td></tr>
<% List<ExtractProtocol> protocols = (List<ExtractProtocol>) request.getAttribute("protocols"); 
if ( protocols != null && protocols.size() > 0 ) { %>
<tr><td>Protocol:</td><td><select onChange="if ( this.selectedIndex > 0 ) {
			var protocolValues = [ [] ];
<% int i = 1; for ( ExtractProtocol item : protocols ) { %>
protocolValues[<%= i %>] = ['<c:out value="<%= item.getExtractType() %>" default="" />', 
	'<c:out value="<%= item.getExtractSolvent() %>" default="" />', 
	'<c:out value="<%= item.getExtractMethod() %>" default="" />' ];
<% i++; } %>          	
		var form = this.form;
		var settings = protocolValues[this.selectedIndex];
		form.elements['extractType'].value = settings[0];
		form.elements['extractSolvent'].value = settings[1];
		form.elements['extractMethod'].value = settings[2]; 
}
">
<option selected>None</option>
<% for ( ExtractProtocol item : protocols ) { %>
<option><%= item.getName() %></option>
<% } %>
</select>

<% } %>
<tr><td>Extract Type:</td><td><input type='text' name="extractType"></td></tr>
<tr><td>Extract Solvent:</td><td><input type='text' name="extractSolvent" size="50"></td></tr>
<tr><td valign="top">Extract Method:</td><td><textarea rows="7" cols="70" name="extractMethod"></textarea></td></tr>
<tr><td valign=top>Notes:</td><td><textarea rows="7" cols="70" name="notes"></textarea></td></tr>
</table>
<p align="center"><button type="button" onClick="updateForm(this,'<%= div %>')" name='createExtract'>Create Extract</button>
<button type='button' onClick="updateForm(this,'<%= div %>')" name="closeForm">Close</button></p>
<% } else { 
	if ( request.getParameter("createExtract") != null ) { 
	String amountString = request.getParameter("amount");
	String dateString = request.getParameter("extractDate");
	String extractType = request.getParameter("extractType");
	String extractSolvent = request.getParameter("extractSolvent");
	String extractMethod = request.getParameter("extractMethod");
	String notesString = request.getParameter("notes"); 
	String label = request.getParameter("label"); %>
<p align="center">Creating new extract...
<%	try {
	Material extract = source.createExtract(label);
	extract.setManualRefresh();
	extract.setDate(dateString);
	extract.setExtractType(extractType);
	extract.setExtractSolvent(extractSolvent);
	extract.setExtractMethod(extractMethod);
	extract.setNotes(notesString);
	extract.setAmount(CyanosObject.parseAmount(amountString, "g"));
	extract.refresh();
	extract.setAutoRefresh();
%>
<FONT COLOR='green'><B>SUCCESS</B></FONT>
<% } catch (DataException e) { %>
<FONT COLOR='red'><B>ERROR</B></FONT> <%= e.getLocalizedMessage() %>	
<% e.printStackTrace(); } %>
</p>	
<% request.setAttribute(MaterialServlet.SEARCHRESULTS_ATTR, source.getExtract()); }  %>
<jsp:include page="/material/extract-list.jsp"></jsp:include>
<p align="center"><button type="BUTTON" name="showExtractForm" onclick="loadForm(this, '<%= div %>')">Create a new extract</button></p>
<% } } else { %>
</form>
<jsp:include page="/material/extract-list.jsp"></jsp:include>
<% } %>
</div>