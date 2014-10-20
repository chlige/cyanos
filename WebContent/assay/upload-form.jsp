<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.web.servlet.UploadServlet,
	edu.uic.orjala.cyanos.web.upload.AssayUpload,
	edu.uic.orjala.cyanos.web.servlet.AssayServlet,
	edu.uic.orjala.cyanos.sql.SQLData,
	edu.uic.orjala.cyanos.sql.SQLAssay,
	edu.uic.orjala.cyanos.Assay,
	edu.uic.orjala.cyanos.sql.SQLAssayTemplate, java.util.Map, java.util.Map.Entry, java.util.HashMap,
	java.util.List, java.util.ListIterator" %>
<jsp:include page="/upload/header.jsp"/>	
<jsp:include page="/upload/template.jsp">
<jsp:param value="upload/assay" name="template_type"/>
</jsp:include>	
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
//]]>
</script>
<table><tr><td>Assay ID:</td><td>
<select onChange="setAutoLoc(this)" onLoad="setAutoLoc(this)" name="<%= AssayUpload.ASSAY_ID %>">
<option value="-1">Use Assay -&gt;</option>
<%= UploadServlet.genOptions(request, AssayUpload.ASSAY_ID) %></select>
<% 	SQLData datasource = UploadServlet.getSQLData(request);
	SQLAssay assays = SQLAssay.assays(datasource); assays.beforeFirst(); %>
<select name="<%= AssayUpload.ASSAY_FROM_DB %>">
<% String selectedID = request.getParameter(AssayUpload.ASSAY_FROM_DB);
	while ( assays.next() ) { String assayID = assays.getID(); %>
<option value="<%= assayID %>" <%= (assayID.equals(selectedID) ? "selected" : "") %>><%= assays.getName() %></option>
<% } %>
</select></td></tr>
<tr><td>Strain ID:</td><td><%= UploadServlet.genColumnSelect(request, AssayUpload.STRAIN_ID, null) %></td></tr>
<tr><td>Location:</td><td><%= UploadServlet.genColumnSelect(request, AssayUpload.LOCATION, null) %>
<input type="checkbox" name='autoLoc' onClick='this.form.location.disabled=this.checked;' onLoad="setAutoLoc(document.upload.assayID)">
Autolocation (along row: A1, A2, etc.) 
</td></tr>
<tr><td>Activity value:</td><td><%= UploadServlet.genColumnSelect(request, AssayUpload.VALUE, "SKIP ITEM") %></td></tr>
<tr><td>STD. Deviation:</td><td><%= UploadServlet.genColumnSelect(request, AssayUpload.STDEV, "SKIP ITEM") %></td></tr>
<tr><td>Assay Protocol (for new assays):</td><td><select name="<%= AssayUpload.ASSAY_PROTOCOL %>"><option value="">NONE</option><% 		
	String selected = request.getParameter(AssayUpload.ASSAY_PROTOCOL);
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
<tr><td>Source Material:</td><td><%= UploadServlet.genColumnSelect(request, AssayUpload.MATERIAL, "SKIP ITEM") %></td></tr>
<tr><td>Source Sample:</td><td><%= UploadServlet.genColumnSelect(request, AssayUpload.SAMPLE, "SKIP ITEM") %></td></tr>
<tr><td>Label:</td><td><%= UploadServlet.genColumnSelect(request, AssayUpload.NAME, "SKIP ITEM") %></td></tr>
<tr><td>Concentration:</td><td><%= UploadServlet.genColumnSelect(request, AssayUpload.CONC, "SKIP ITEM") %>
<% String unit = request.getParameter(AssayUpload.CONC_UNIT); if ( unit == null ) unit = "ug/ml"; %>
Default unit: <input type="text" name="<%= AssayUpload.CONC_UNIT %>" value="<%= unit %>"> 
</td></tr>
</table>