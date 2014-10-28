<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="cyanos" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.Strain,
	edu.uic.orjala.cyanos.Inoc,
	edu.uic.orjala.cyanos.CyanosObject,
	edu.uic.orjala.cyanos.sql.SQLInoc,
	edu.uic.orjala.cyanos.sql.SQLData,
	edu.uic.orjala.cyanos.web.servlet.InocServlet,
	edu.uic.orjala.cyanos.web.servlet.StrainServlet,
	edu.uic.orjala.cyanos.web.BaseForm,
	java.text.DateFormat" %>
<%	String contextPath = request.getContextPath();
	String rowString = request.getParameter("rows");
	int rows = 1;
	if ( rowString != null ) {
		rows = Integer.parseInt(rowString);	
	}
	Strain strain = (Strain) request.getAttribute(StrainServlet.STRAIN_OBJECT);
	Inoc parents = (Inoc) request.getAttribute(InocServlet.SEARCHRESULTS_ATTR);
	DateFormat format = (DateFormat) session.getAttribute(InocServlet.SESS_ATTR_DATE_FORMAT);
%>
<table  class="dashboard" id="formTable">
<tr><td></td><th class="header">Culture ID</th><th class="header">Date</th><th class="header">Parent Stock</th><th class="header">Media</th><th class="header">Volume</th><th class="header">Project</th><th class="header">Notes</th><th class="header">Stock</th></tr>
<% for ( int row = 1; row <= rows; row++) { %>
<tr class="banded" align='center'><td>
<input type="hidden" name="row" value="<%= String.format("%02d", row) %>"><b><%= row %></b></td>
<td><% String fieldName = String.format("%02d_strain", row); 
	String strainName = request.getParameter(fieldName);
	boolean thisStrain = ( strain != null && ( strainName == null || strain.getID().equals(strainName) ) );
	if ( strainName == null && strain != null ) { strainName = strain.getID(); } %>
<input id="<%= fieldName  %>" type="text" name="<%= fieldName %>" value="<c:out value="<%= strainName %>"/>" autocomplete='off' onKeyUp="livesearch(this, '<%= fieldName %>', 'div_<%= fieldName %>')" style='padding-bottom: 0px' size="10" onchange="updateDefs(this)"/>
<div id="div_<%= fieldName %>" class='livesearch'></div></td>
<% fieldName = String.format("%02d_date", row); %>
<td><cyanos:calendar-field fieldName="<%= fieldName %>" dateValue="<%= request.getParameter(fieldName) %>"/></td>
<% fieldName = String.format("%02d_parent", row); %>
<td><select name="<%= fieldName %>">
<% 
	Inoc theseInocs = null;

	if ( thisStrain ) {
		theseInocs = parents;
	} else if ( strainName != null ) {
		theseInocs = SQLInoc.openInocsForStrain((SQLData)request.getAttribute(InocServlet.DATASOURCE), strainName);
	}

if ( theseInocs != null && theseInocs.first() )  { 
	theseInocs.beforeFirst();
	out.println("<option value=\"\">NONE</option>");
	String selParent = request.getParameter(fieldName);
	while ( theseInocs.next() ) {
		out.print("<option value=\"");
		out.print(theseInocs.getID());
		out.print("\"");
		if ( theseInocs.getID().equals(selParent) ) { 
			out.print(" selected");
		}
		out.print(">");
		out.print(format.format(theseInocs.getDate()));
		out.print(" (");
		out.print(BaseForm.autoFormatAmount(theseInocs.getVolume(), Inoc.VOLUME_TYPE));
		out.println(")</option>");
	}
} %>
</select></td>
<% fieldName = String.format("%02d_media", row);  String media = request.getParameter(fieldName); if ( thisStrain && media == null ) { media = strain.getDefaultMedia(); } %>
<td><input type="text" size="10" name="<%= fieldName %>" value="<c:out value="<%= media %>"/>"></td>
<% fieldName = String.format("%02d_qty", row); %>
<td><input type="text" size="3" name="<%= fieldName %>" value="<c:out value="<%= request.getParameter(fieldName) %>"/>"> &times; 
<% fieldName = String.format("%02d_vol", row); %>
<input type="text" size="5" name="<%= fieldName %>" value="<c:out value="<%= request.getParameter(fieldName) %>"/>"></td>
<% fieldName = String.format("%02d_project", row); String projectID = request.getParameter(fieldName); if ( thisStrain && projectID == null ) { projectID = strain.getProjectID(); } %>
<td><jsp:include page="/includes/project-popup.jsp">
<jsp:param value="<%= fieldName %>" name="fieldName"/>
<jsp:param value="<%= projectID %>" name="project"/>
</jsp:include></td>
<% fieldName = String.format("%02d_notes", row); %>
<td><textarea name="<%= fieldName %>" rows="2" cols="15"><c:out value="<%= request.getParameter(fieldName) %>"/></textarea></td>
<td><input type="checkbox" name="<%= String.format("%02d_stock", row) %>" <%= request.getParameter(String.format("%02d_stock", row)) != null ? "checked" : "" %>>
<td><a onclick="delRow(this)"><b><font color="red" size="+1">X</font></b></a></td>
</tr>
<% } %>
</table>
<% if ( request.getParameter("strain") != null) { %><input type="hidden" name="strain" value="<%= request.getParameter("strain") %>"><% } %>
<p align="center"><button type="button" onclick="this.value = document.getElementById('formTable').rows.length; updateForm(this,'addTable')" name='rows' value="<%= String.format("%d",rows + 1) %>">Add Row</button></p>