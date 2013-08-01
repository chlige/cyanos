<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.AssayData,
	edu.uic.orjala.cyanos.web.servlet.AssayServlet,
	edu.uic.orjala.cyanos.web.BaseForm,
	edu.uic.orjala.cyanos.CyanosObject,
	java.text.SimpleDateFormat,
	java.math.BigDecimal,
	java.math.MathContext,
	java.util.List" %>
<%	String contextPath = request.getContextPath();
	AssayData queryResults = (AssayData)request.getAttribute(AssayServlet.SEARCHRESULTS_ATTR); 
	String div = request.getParameter("div");
	boolean activesOnly = request.getParameter("actives") != null;
	if ( queryResults != null ) { %>
<form>
<input type="hidden" name="id" value="<%= request.getParameter("id") %>">
<input type="hidden" name="div" value="<%= div %>">
<p align="center">
<input type="checkbox" name="actives" value="1" <%= ( activesOnly ? "checked" : "") %> onClick="refreshDiv(this,'<%= div %>')"> Actives only.
</p>
</form>	
<% 	if ( queryResults.first() ) {
	queryResults.beforeFirst(); 
	SimpleDateFormat dateFormat = (SimpleDateFormat) session.getAttribute("dateFormatter");  %>
<table  class="dashboard">
<tr><th class="header" width='100'>Label</th><th class="header" width='100'>Location</th><th class="header" width='150'>Date</th>
<th class="header" width='100'>Culture ID</th><th class="header" width='100'>Material ID</th><th class="header" width='100'>Sample ID</th>
<th class="header" width='100'>Activity</th><th class="header" width='100'>Concentration</th></tr>
<% while ( queryResults.next() ) { 
	MathContext concMC = new MathContext(5);
	if ( activesOnly && (! queryResults.isActive() ) ) continue; 
%><tr class="<%= queryResults.isActive() ? "active" : "banded" %>" align='center'>
<td><%= queryResults.getLabel() %></td>
<td><%= queryResults.getLocation() %></td>
<td><%= dateFormat.format(queryResults.getDate()) %></td>
<td><a href="<%= contextPath %>/strain?id=<%= queryResults.getStrainID() %>"><%= queryResults.getStrainID() %></a>
<td><% String materialID = queryResults.getMaterialID(); if ( materialID != null && (! materialID.equals("0")) ) { %>
<a href="<%= contextPath %>/material?id=<%= materialID %>"><%= materialID %></a>
<% } else { %>-<% } %></td>
<td><% String sampleID = queryResults.getSampleID(); if ( sampleID != null && sampleID.length() > 0 ) { %>
<a href="<%= contextPath %>/sample?id=<%= sampleID %>"><%= sampleID %></a>
<% }  { %>-<% } %></td>
<td><%= queryResults.getActivityString() %></td>
<td><% BigDecimal conc = queryResults.getConcentration(); 
	if ( conc.compareTo(BigDecimal.ZERO) == 0 ) { 
		out.print("-");
	} else { 
		out.print(BaseForm.autoFormatAmount(conc.round(concMC), AssayData.CONCENTRATION_TYPE)); 
	} 
%></td>
</tr>
<% } %></table>
<% } else { 
%><hr width="85%"/>
<p align='center'><b>No Results</b></p>
<% } } %>