<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.AssayData,
	edu.uic.orjala.cyanos.web.servlet.AssayServlet,
	edu.uic.orjala.cyanos.web.BaseForm,
	edu.uic.orjala.cyanos.CyanosObject,
	edu.uic.orjala.cyanos.Role,
	edu.uic.orjala.cyanos.sql.SQLAssayData,
	java.text.SimpleDateFormat,
	java.math.BigDecimal,
	java.math.MathContext,
	java.util.List" %>
<%	String contextPath = request.getContextPath();
	AssayData queryResults = (AssayData)request.getAttribute(AssayServlet.SEARCHRESULTS_ATTR); 
	String div = request.getParameter("div");
	boolean activesOnly = request.getParameter("actives") != null;
	if ( queryResults != null && request.getRemoteUser() != null ) { %>
<form>
<input type="hidden" name="id" value="<%= request.getParameter("id") %>">
<input type="hidden" name="div" value="<%= div %>">
<p align="center">Target: 
<select name="target" onChange="refreshDiv(this,'<%= div %>')">
<option value="">All Targets</option>
<% List<String> targets = (List<String>)request.getAttribute(AssayServlet.TARGET_LIST); String currTarget = request.getParameter("target");
for ( String target: targets ) { %>
<option <%= (target.equals(currTarget) ? "selected" : "") %>><%= target %></option>
<% } %>
</select> <input type="checkbox" name="actives" value="1" <%= ( activesOnly ? "checked" : "") %> onClick="refreshDiv(this,'<%= div %>')"> Actives only.
</p>
</form>	
<% 	if ( queryResults.first() ) {
	MathContext concMC = new MathContext(5);
	queryResults.beforeFirst(); 
	SimpleDateFormat dateFormat = (SimpleDateFormat) session.getAttribute("dateFormatter");  %>
<table class="dashboard">
<tr><th class="header" width='100'>Label</th><th class="header" width='100'>Assay</th><th class="header" width='150'>Date</th>
<th class="header" width='100'>Target</th><th class="header" width='100'>Activity</th><th class="header" width='100'>Concentration</th></tr>
<% while ( queryResults.next() ) { 
	if ( ! queryResults.isAllowed(Role.READ) ) continue;
	if ( activesOnly && (! queryResults.isActive() ) ) continue; 
%><tr class=<%= queryResults.isActive() ? "active" : "banded" %> align='center'>
<td><%= queryResults.getLabel() %></td>
<td><a href="<%= contextPath %>/assay?id=<%= queryResults.getAssayID() %>"><%= queryResults.getAssayID() %></a></td>
<td><%= dateFormat.format(queryResults.getDate()) %></td>
<td><%= queryResults.getAssayTarget() %></td>
<td><%= queryResults.getActivityString() %></td>
<td><% BigDecimal conc = queryResults.getConcentration(); 
	if ( conc.compareTo(BigDecimal.ZERO) == 0 ) { 
		out.print("-");
	} else { 
		out.print(SQLAssayData.autoFormatAmount(conc.round(concMC), AssayData.CONCENTRATION_TYPE)); 
	} 
%></td></tr>
<% } %></table>
<% } else { 
%><hr width="85%"/>
<p align='center'><b>No Results</b></p>
<% } } %>