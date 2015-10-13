<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="cyanos" tagdir="/WEB-INF/tags" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.web.servlet.UploadServlet,
	edu.uic.orjala.cyanos.web.upload.AssayUploadJob,
	edu.uic.orjala.cyanos.web.servlet.AssayServlet,
	edu.uic.orjala.cyanos.sql.SQLData,
	edu.uic.orjala.cyanos.sql.SQLAssay,
	edu.uic.orjala.cyanos.Assay,
	edu.uic.orjala.cyanos.sql.SQLAssayTemplate, java.util.Map, java.util.Map.Entry, java.util.HashMap,
	java.util.List, java.util.ListIterator" %>
<script type="text/javascript">
//<![CDATA[ 
function setAutoLoc(assay_box) {
	if ( assay_box.selectedIndex == 0 ) {
		assay_box.form.autoLoc.disabled = 0;
		assay_box.form.location.disabled = assay_box.form.autoLoc.checked;
		assay_box.form.assayFromDB.disabled = 0;
	} else {
		assay_box.form.assayFromDB.disabled = 1; assay_box.form.autoLoc.disabled = 1;
		assay_box.form.location.disabled = 0;
	}
}

function checkEnabled(field, otherList) {
	var disabled = (field.options[field.selectedIndex].value !== "-1");
	
	for ( var o in otherList) {
		var item = field.form.elements.namedItem(otherList[o]);
		if ( item ) {
			item.disabled = disabled;
		}
	}
}
//]]>
</script>
<table class="uploadForm"><tr><td>Assay ID:</td><td>
<select onChange="setAutoLoc(this)" onLoad="setAutoLoc(this)" name="<%= AssayUploadJob.ASSAY_ID %>">
<option value="-1">Use Assay -&gt;</option>
<%= UploadServlet.genOptions(request, AssayUploadJob.ASSAY_ID) %></select>
<% 	SQLData datasource = UploadServlet.getSQLData(request);
	SQLAssay assays = SQLAssay.assays(datasource); assays.beforeFirst(); %>
<select name="<%= AssayUploadJob.ASSAY_FROM_DB %>">
<% String selectedID = request.getParameter(AssayUploadJob.ASSAY_FROM_DB);
	while ( assays.next() ) { String assayID = assays.getID(); %>
<option value="<%= assayID %>" <%= (assayID.equals(selectedID) ? "selected" : "") %>><%= assays.getName() %></option>
<% } %>
</select></td></tr>
<tr><td>Assay Protocol (for new assays):</td><td><select name="<%= AssayUploadJob.ASSAY_PROTOCOL %>"><option value="">NONE</option><% 		
	String selected = request.getParameter(AssayUploadJob.ASSAY_PROTOCOL);
	for ( String proto : SQLAssayTemplate.listProtocols(datasource) ) {
		out.print("<option");
		if ( proto.equals(selected) ) {
			out.print(" selected");
		}
		out.print(">");
		out.print(proto);
		out.print("</option>");
	}
 %></select></td></tr>
<tr><td>Location:</td><td><cyanos:sheet-columns fieldName="<%= AssayUploadJob.LOCATION %>"/>
<input type="checkbox" name='autoLoc' onClick='this.form.location.disabled=this.checked;' onLoad="setAutoLoc(document.upload.assayID)">
Autolocation (along row: A1, A2, etc.) </td></tr>
<tr><td>Label:</td>
<td><cyanos:sheet-columns fieldName="<%= AssayUploadJob.NAME %>"><option value="-1">SKIP ITEM</option></cyanos:sheet-columns></td></tr>
<tr><th colspan=2 style="text-align:center;">Source</th></tr>
<tr><td>Source Material:</td>
<td><cyanos:sheet-columns fieldName="<%= AssayUploadJob.MATERIAL %>" onchange="checkEnabled(this, ['strainID'])" onload="checkEnabled(this, ['strainID'])"><option value="-1">NOT USED</option></cyanos:sheet-columns>
<input type="checkbox" name="<%= AssayUploadJob.MATERIAL_BY_LABEL %>" <%= request.getParameter(AssayUploadJob.MATERIAL_BY_LABEL) != null ? "checked" : "" %>/>Find material by label</td></tr>
<tr><td>Source Sample:</td>
<td><cyanos:sheet-columns fieldName="<%= AssayUploadJob.SAMPLE %>" onchange="checkEnabled(this, ['strainID'])" onload="checkEnabled(this, ['strainID'])"><option value="-1">SKIP ITEM</option></cyanos:sheet-columns></td></tr>
<tr><td>Strain ID:</td>
<td><cyanos:sheet-columns fieldName="<%= AssayUploadJob.STRAIN_ID %>" onchange="checkEnabled(this, ['material','sample','materialLabel'])" onload="checkEnabled(this, ['material','sample','materialLabel'])"><option value="-1">NOT USED</option></cyanos:sheet-columns></td></tr>
<tr><th colspan=2 style="text-align:center;">Data information</th></tr>
<tr><td>Activity value:</td>
<td><cyanos:sheet-columns fieldName="<%= AssayUploadJob.VALUE %>"><option value="-1">SKIP ITEM</option></cyanos:sheet-columns></td></tr>
<tr><td>STD. Deviation:</td>
<td><cyanos:sheet-columns fieldName="<%= AssayUploadJob.STDEV %>"><option value="-1">SKIP ITEM</option></cyanos:sheet-columns></td></tr>
<tr><td>Concentration:</td>
<td><cyanos:sheet-columns fieldName="<%= AssayUploadJob.CONC %>"><option value="-1">SKIP ITEM</option></cyanos:sheet-columns>
<% String unit = request.getParameter(AssayUploadJob.CONC_UNIT); if ( unit == null ) unit = "ug/ml"; %>
Default unit: <input type="text" name="<%= AssayUploadJob.CONC_UNIT %>" value="<%= unit %>"> 
</td></tr>
</table>