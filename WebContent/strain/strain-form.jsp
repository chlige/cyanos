<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="edu.uic.orjala.cyanos.Strain,
	edu.uic.orjala.cyanos.web.servlet.StrainServlet,
	edu.uic.orjala.cyanos.web.servlet.TaxonServlet,
	edu.uic.orjala.cyanos.Taxon,
	java.text.SimpleDateFormat,
	java.util.Date,
	edu.uic.orjala.cyanos.Role,
	edu.uic.orjala.cyanos.Separation,
	edu.uic.orjala.cyanos.web.BaseForm,
	edu.uic.orjala.cyanos.Isolation,
	edu.uic.orjala.cyanos.Collection,
	edu.uic.orjala.cyanos.Project" %>
<% 	String contextPath = request.getContextPath();
	Strain strainObj = (Strain) request.getAttribute(StrainServlet.STRAIN_OBJECT); 	
	SimpleDateFormat dateFormat = (SimpleDateFormat) session.getAttribute("dateFormatter");
	if ( strainObj == null ) { 
%><p align='center'><b>ERROR:</b> Object not passed</p>
<%  	out.flush(); 
		return; 
	} else if ( ! strainObj.first() ) { 
%><p align='center'><b>ERROR:</b> Object not found</p>
<% 		out.flush(); 
		return; 
	} 	
	boolean update = strainObj.isAllowed(Role.WRITE) && request.getParameter("updateStrain") != null;
%><div CLASS="showSection" ID="view_info">
<table class="species" style="width:80%; margin-left:auto; margin-right:auto">
<tr<% if ( update ) { 
	String value = request.getParameter("culture_source");
	boolean setUpdate = false;
	if (value != null && (! value.equals(strainObj.getCultureSource()) ) ) {
		strainObj.setCultureSource(value); setUpdate = true;
	}
	value = request.getParameter("isolation"); 
	String colVal = request.getParameter("collection");
	if ( value != null && (! value.equals(strainObj.getSourceIsolationID())) ) {
		strainObj.setSourceIsolationID(value);  setUpdate = true;
	} 
	if ( colVal != null && (! colVal.equals(strainObj.getSourceCollectionID())) ) {
		strainObj.setSourceCollectionID(colVal); setUpdate = true;
	}

	if ( setUpdate ) { %> class="updated"<% }
} %>><td style="width: 125;">Culture source:</td><td><%= strainObj.getCultureSource() %>
<% String source = strainObj.getSourceIsolationID();
	String colsrc = strainObj.getSourceCollectionID();
	
	if ( source != null ) { %>[<a href="collection?id=<%= source %>">View Source Isolation</a>]<% } %>
<% if ( colsrc != null ) { %>[<a href="collection?col=<%= colsrc %>">View Source Collection</a>]<% } %></td></tr>
<tr<% if ( update ) { 
	String value = request.getParameter("sci_name");
	boolean setUpdate = false;
	if (value != null && (! value.equals(strainObj.getName()) ) ) {
		strainObj.setName(value); setUpdate = true; } 
	value = request.getParameter("genus");
	if ( value != null && (! value.equals(strainObj.getGenus()))) {
		strainObj.setGenus(value); setUpdate = true; }
	if ( setUpdate ) { %> class="updated"<% }
} %>
><td>Scientific Name:</td><td><%= strainObj.getName() %></td></tr>
<tr><td colspan=2>Taxonomy<br>
<% Taxon taxon = strainObj.getTaxon(); 
if ( taxon != null && taxon.first() ) { Taxon lineage = taxon.getLinage(); lineage.beforeFirst(); %>
<ul style="list-style-type: none">
<% while (lineage.next()) { %><li><%
	TaxonServlet.printTaxonLink(out, lineage);
%></li>
<% } %></ul>
<% } else { %><b>Name not valid</b><% } %></td></tr>
<tr<% if ( update ) { 
	String value = request.getParameter("addDate");
	if (value != null && (! value.equals(strainObj.getDate()) ) ) {
		strainObj.setDate(value); %> class="updated"<% } } 
%>><td>Date Added:</td><td><%= dateFormat.format(strainObj.getDate()) %></td></tr>
<tr<% if ( update ) { 
	String value = request.getParameter("def_media");
	if (value != null && (! value.equals(strainObj.getDefaultMedia()) ) ) {
		strainObj.setDefaultMedia(value); %> class="updated"<% } } 
%>><td>Default Media:</td><td><%= strainObj.getDefaultMedia() %></td></tr>
<tr<% if ( update ) { 
	String value = request.getParameter("culture_status");
	if (value != null && (! value.equals(strainObj.getStatus()) ) ) {
		strainObj.setStatus(value); %> class="updated"<% } } %>
><td>Culture status:</td><td><%= strainObj.getStatus() %></td></tr>
<tr<% if ( update ) { 
	String value = request.getParameter("project");
	if (value != null && (! value.equals(strainObj.getProjectID()) ) ) {
		strainObj.setProjectID(value); %> class="updated"<% } } %>><td>Project</td><td>
<% Project aProj = strainObj.getProject(); if ( aProj != null && aProj.first() ) { %>
<a href='project?id=<%= aProj.getID() %>'><%= aProj.getName() %></a><% } else { %>None<% } %></td></tr>
<tr<% if ( update ) { 
	String value = request.getParameter("notes");
	if (value != null && (! value.equals(strainObj.getNotes()) ) ) {
		strainObj.setNotes(value);	
%> class="updated"<% } } %>><td valign=top>Notes:</td><td><%= BaseForm.formatStringHTML(strainObj.getNotes()) %></td></tr>
</table>
<% if ( strainObj.isAllowed(Role.WRITE) ) { %>
<p align='center'><button type='button' onClick='flipDiv("info")'>Edit Values</button></p>
</div>
<div class='hideSection' id="edit_info">
<form name='editStrain'>
<input type="hidden" name="id" value="<%= strainObj.getID() %>">
<table class="species" style="width:80%; margin-left:auto; margin-right:auto">
<tr><td width='125'>Culture source:</td><td><input type='text' name='culture_source' value='<%= strainObj.getCultureSource() %>'></td></tr>
<tr><td></td><td>
<table><tr><td>Collection:</td><td><input type="text" id="collection" name="collection"  value="<c:out value='<%= strainObj.getSourceCollectionID() %>'/>" autocomplete="off" onkeyup="livesearch(this, 'collection', 'validcols')" style="padding-bottom: 0px">
<div class="livesearch" id="validcols"></div></td>
<td>Isolation:</td><td><input type="text" id="isolation" name="isolation"  value="<c:out value='<%= strainObj.getSourceIsolationID() %>'/>" autocomplete="off" onkeyup="livesearch(this, 'isolation', 'validisos')" style="padding-bottom: 0px">
<div class="livesearch" id="validisos"></div></td></tr></table>
</td></tr>
<tr><td>Scientific name:</td><td><input type='text' name='sci_name' value='<%= strainObj.getName() %>'>
<br>Genus: <input id="genus" type="TEXT" name="genus" value="<%= strainObj.getGenus() %>" autocomplete="off" onkeyup="livesearch(this, 'genus', 'validgenus')" style="padding-bottom: 0px">
<div class="livesearch" id="validgenus"></div></td></tr>
<% Date strainDate = strainObj.getDate(); %>
<tr><td>Date Added:</td><td><input type="text" name="addDate" onFocus="showDate('div_calendar','addDate')" style='padding-bottom: 0px' value='<fmt:formatDate value="<%= ( strainDate != null ? strainDate : new Date()) %>" pattern="yyyy-MM-dd"/>' id="addDate"/>
<a onclick="showDate('div_calendar','addDate')"><img align="MIDDLE" border="0" src="<%= contextPath %>/images/calendar.png"></a>
<div id="div_calendar" class='calendar'>
<jsp:include page="/calendar.jsp">
<jsp:param value="addDate" name="update_field"/>
<jsp:param value="div_calendar" name="div"/>
</jsp:include>
</div>
</td></tr>
<tr><td>Default Media:</td><td><input type="text" name="def_media" value="<c:out value="<%= strainObj.getDefaultMedia() %>"/>"></td></tr>
<% String status = strainObj.getStatus(); %>
<tr><td>Culture status:</td><td><select name="culture_status">
<option value="<%= Strain.GOOD_STATUS %>" <%= ( Strain.GOOD_STATUS.equalsIgnoreCase(status) ? "selected" : "" ) %>>Good</option>
<option value="<%= Strain.SLOW_GROWTH_STATUS %>" <%= ( Strain.SLOW_GROWTH_STATUS.equalsIgnoreCase(status) ? "selected" : "") %>>Slow Growth</option>
<option value="<%= Strain.CONTAMINATED_STATUS %>" <%= ( Strain.CONTAMINATED_STATUS.equalsIgnoreCase(status) ? "selected" : "" ) %>>Contaminated</option>
<option value="<%= Strain.REMOVED_STATUS %>" <%= ( Strain.REMOVED_STATUS.equalsIgnoreCase(status) ? "selected" : "" ) %>>Removed</option>
<option value="<%= Strain.FIELD_HARVEST_STATUS %>" <%= ( Strain.FIELD_HARVEST_STATUS.equalsIgnoreCase(status) ? "selected" : "") %>>Field Collection</option>
</select></td></tr>
<tr><td>Project</td><td>
<jsp:include page="/includes/project-popup.jsp">
<jsp:param value="<%= strainObj.getProjectID() %>" name="project"/>
<jsp:param value="project" name="fieldName"/></jsp:include>
</td></tr>
<tr><td valign=top>Notes:</td><td><textarea rows="7" cols="70" name="notes"><c:out value="<%= strainObj.getNotes() %>" default="" /></textarea></td></tr>
<tr><td colspan="2" align="CENTER"><button type="button" name="updateStrain" onClick="updateForm(this,'<%= StrainServlet.INFO_FORM_DIV_ID %>')">Update</button>
<input type="RESET"></td></tr>
</table>
</form>
<p align="center"><button type='button' onClick='flipDiv("info")'>Close Form</button></p>
<% } %>
</div>
