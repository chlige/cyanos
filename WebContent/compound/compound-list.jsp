<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.BasicObject,
	edu.uic.orjala.cyanos.CompoundObject,
	edu.uic.orjala.cyanos.Compound,
	edu.uic.orjala.cyanos.Role,edu.uic.orjala.cyanos.web.servlet.CompoundServlet,
	edu.uic.orjala.cyanos.web.BaseForm,
	java.math.BigDecimal" %>
<% 	String contextPath = request.getContextPath();
	String div = request.getParameter("div");
	Compound compounds = (Compound) request.getAttribute(CompoundServlet.COMPOUND_RESULTS);
	CompoundObject source = (CompoundObject) request.getAttribute(CompoundServlet.COMPOUND_PARENT);
	boolean displayStructure = (request.getParameter("showStructure") != null );
	if ( compounds != null ) { 
		if ( compounds.first() ) {
			compounds.beforeFirst();
			boolean oddRow = true;
%> 
<table width="75%" align="center" class="dashboard"><tbody>
<tr><th class="header">Name</th><th class="header">Formula</th><th class="header">Notes</th><th class="header">Links</th>
<% if ( source != null )  { %>
<th class="header">Retention Time</th>
<% } %>
</tr>	
<% while ( compounds.next() )  { 
	String rowFormat = ( oddRow ? "odd" : "even" ); oddRow = ! oddRow; 		
	String name = compounds.getName();
	if ( name == null || name.length() < 1) name = compounds.getID();
	String inchiString = compounds.getInChiKey();
	%>
<tr class='<%= rowFormat %>' align='center'>
<td><a href="<%= contextPath %>/compound?id=<%= compounds.getID() %>"><%= name %></a></td>
<td><%= compounds.getHTMLFormula() %></td>
<td><%= BaseForm.shortenString(compounds.getNotes(), 20) %></td>
<td>
<% if ( inchiString != null )  { %>
<a class="chemspider" href="http://www.chemspider.com/Search.aspx?q=<%= inchiString %>" target="_blank"><img src="<%= contextPath %>/images/icons/cs-icon.png" valign="middle"> Search ChemSpider</a>
<% } %>
</td>
<% if ( source != null )  {  
	BigDecimal rt = compounds.getRetentionTime(source); 
	if ( rt == null || rt.compareTo(BigDecimal.ZERO) == 0 ) {
%>
<td>-</td>
<% } else { %>
<td><%= String.format("%.2f", rt.doubleValue()) %> min</td>
<% } %>
</tr>
<% } } %>
</table>
<% } else { %>
<p align='center'><b>No Results</b></p>
<% } } %>



