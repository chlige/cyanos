<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.BasicObject,
	edu.uic.orjala.cyanos.CompoundObject,
	edu.uic.orjala.cyanos.Compound,
	edu.uic.orjala.cyanos.Role,
	edu.uic.orjala.cyanos.Material,
	edu.uic.orjala.cyanos.Separation,edu.uic.orjala.cyanos.web.servlet.CompoundServlet,
	edu.uic.orjala.cyanos.web.BaseForm,
	edu.uic.orjala.cyanos.DataException,
	java.text.SimpleDateFormat,
	java.math.BigDecimal" %>

<% String contextPath = request.getContextPath();
	String div = request.getParameter("div"); %>
<div id="<%=div %>">
<%
	Material source = (Material) request.getAttribute(CompoundServlet.COMPOUND_PARENT);
	if ( source.isAllowed(Role.WRITE) ) { %>

<form><input type="HIDDEN" name="id" value="<%= source.getID() %>">
<input type="hidden" name="div" value="<%= div %>">

<% if ( request.getParameter("showCmpdForm") != null ) { 
	Compound compoundList = (Compound) request.getAttribute(CompoundServlet.COMPOUND_LIST); 
	if ( compoundList != null && compoundList.first() ) { 
		compoundList.beforeFirst(); %>
<p align="CENTER">Compound: <select name="compoundID">
<%	while ( compoundList.next() ) { 
	String name = compoundList.getName(); 
	if ( name == null || name.length() < 1 ) { name = compoundList.getID(); } %>
<option value="<%= compoundList.getID() %>"><%= name %></option>
<% } %>
</select><br>
Retention time: <input type="text" name="retTime"size=10> min</p>
<!-- TODO: Need to create table to crosslink separations -->
<% SimpleDateFormat dateFormat = (SimpleDateFormat) session.getAttribute("dateFormatter"); 
	Separation seps = source.getSeparations(); 
	Separation parent = source.getParentSeparation();
	if ( seps.first() || parent.first() ) { %>
<p align="center"><b>Cross-link with a separation</b></p>
<table class="dashboard">
<tr><th class="header" width='200'>Separation</th><th class="header" width='150'>Date</th><th class="header" width='200'>Stationary Phase</th>
<th class='header' width=150>Mobile Phase</th><th class='header' width='200'>Notes</th></tr>
<% if ( parent.first() ) { %>
<tr class="subhead"><th colspan="5">Parent Separation</th></tr>
<tr class="odd" align='center'><td><input type="radio" name="separationID" value="<%= parent.getID() %>" checked >
<% String tag = parent.getTag(); if ( tag != null && tag.length() > 0 ) { %>
<%= tag %><br>
<% } %>
(S/N: <%= parent.getID() %>)</td>
<td><%= dateFormat.format(parent.getDate()) %></td>
<td><%= BaseForm.shortenString(parent.getStationaryPhase(), 20) %></td>
<td><%= BaseForm.shortenString(parent.getMobilePhase(), 20) %></td>
<td><%= BaseForm.shortenString(parent.getNotes(), 30) %></td>
</tr>
<% } if ( seps.first() ) { seps.beforeFirst(); %>
<tr class="subhead"><th colspan="5">Child Separations</th></tr>
<%	while ( seps.next() ) { 
		if ( seps.isRemoved() ) continue;
	%>
<tr class="banded" align='center'>
<td><input type="radio" name="separationID" value="<%= seps.getID() %>">
<% String tag = seps.getTag(); if ( tag != null && tag.length() > 0 ) { %>
<%= seps.getTag() %><br>
<% } %>
(S/N: <%= seps.getID() %>)</td>
<td><%= dateFormat.format(seps.getDate()) %></td>
<td><%= BaseForm.shortenString(seps.getStationaryPhase(), 20) %></td>
<td><%= BaseForm.shortenString(seps.getMobilePhase(), 20) %></td>
<td><%= BaseForm.shortenString(seps.getNotes(), 30) %></td>
</tr>
<% } } %>
</table>
<% } %>
<p align="center"><button type="button" onClick="updateForm(this,'<%= div %>')" name='linkCompound'>Link Compound</button>
<button type='button' onClick="updateForm(this,'<%= div %>')" name="closeForm">Close</button></p>
<% } } else if ( request.getParameter("delCmpdForm") != null ) {
	Compound compounds = source.getCompounds();
	if ( compounds != null && compounds.first() ) {
			compounds.beforeFirst();
			boolean oddRow = true;
%> 
<table style="width: 75%" class="dashboard"><tbody>
<tr><th class="header">Name</th><th class="header">Formula</th><th class="header">Notes</th></tr>	
<% while ( compounds.next() )  { 
	String rowFormat = ( oddRow ? "odd" : "even" ); oddRow = ! oddRow; 		
	String name = compounds.getName();
	if ( name == null || name.length() < 1) name = compounds.getID();
	String inchiString = compounds.getInChiKey();
	%>
<tr class='<%= rowFormat %>' align='center'>
<td><input type="checkbox" name="compoundID" value="<%= compounds.getID() %>"><%= name %></td>
<td><%= compounds.getHTMLFormula() %></td>
<td><%= BaseForm.shortenString(compounds.getNotes(), 20) %></td>
</tr>
<% } %> 
</table>
<p align="center"><button type="button" onClick="updateForm(this,'<%= div %>')" name='unlinkCompound'>Unlink Compound(s)</button>
<% } else { %>
<p align="center">
<% } %>
<button type='button' onClick="updateForm(this,'<%= div %>')" name="closeForm">Close</button></p>
<% } else { 
	if ( request.getParameter("linkCompound") != null ) { 
	String retTime = request.getParameter("retTime");
	String cmpdID = request.getParameter("compoundID");
	String[] samples = request.getParameterValues("separationID");
%>
<p align="center"><B>Linking compound ID: <%= cmpdID %> </B>
<% if ( samples.length == 0 ) { try { source.addCompoundID(cmpdID, null, retTime); %>
<FONT COLOR='green'><B>SUCCESS</B></FONT>
<% } catch (DataException e) { %>
<FONT COLOR='red'><B>ERROR</B></FONT> <%= e.getLocalizedMessage() %>	
<% e.printStackTrace(); } } else { %>
<br>
<% for ( int i = 0; i < samples.length; i++ ) { %>
With separation ID: <%=samples[i] %> 
<% try { source.addCompoundID(cmpdID, samples[i], retTime); %>
<FONT COLOR='green'><B>SUCCESS</B></FONT><br>
<% } catch (DataException e) { %>
<FONT COLOR='red'><B>ERROR</B></FONT> <%= e.getLocalizedMessage() %><br>	
<% e.printStackTrace(); } %>
<% } } %>
</p>
<% request.setAttribute(CompoundServlet.COMPOUND_RESULTS, source.getCompounds()); 
	} else if ( request.getParameter("unlinkCompound") != null ) { 
	String[] cmpdID = request.getParameterValues("compoundID"); %>
<p align="center">
<%	for ( int i = 0; i < cmpdID.length; i++ ) { %>
	Unlinking Compound ID: <%=cmpdID[i] %> 
	<% try { source.unlinkCompoundID(cmpdID[i]); %>
	<FONT COLOR='green'><B>SUCCESS</B></FONT><br>
	<% } catch (DataException e) { %>
	<FONT COLOR='red'><B>ERROR</B></FONT> <%= e.getLocalizedMessage() %><br>	
	<% e.printStackTrace(); } %>
<% } request.setAttribute(CompoundServlet.COMPOUND_RESULTS, source.getCompounds()); %>
</p>
<% } %>
<jsp:include page="/compound/compound-list.jsp"></jsp:include>
<p align="center"><button type="BUTTON" name="showCmpdForm" onclick="loadForm(this, '<%= div %>')">Link a new compound</button>
<% if ( source.isAllowed(Role.DELETE) ) { %>
<button type="BUTTON" name="delCmpdForm" onclick="loadForm(this, '<%= div %>')">Unlink a compound</button>
<% } %>
</p>
<% } %>
</form>
<% } else { %>
<jsp:include page="/compound/compound-list.jsp"></jsp:include>
<% } %>
</div>