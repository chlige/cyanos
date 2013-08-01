<%@ page import="edu.uic.orjala.cyanos.AssayPlate,
	edu.uic.orjala.cyanos.Assay,edu.uic.orjala.cyanos.web.servlet.AssayServlet,
	edu.uic.orjala.cyanos.web.BaseForm,
	edu.uic.orjala.cyanos.CyanosObject,
	java.text.SimpleDateFormat,
	java.util.List" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%	String contextPath = request.getContextPath();
	Assay myAssay = (Assay) request.getAttribute(AssayServlet.ASSAY_OBJECT);
	String div = request.getParameter("div");
	boolean activesOnly = request.getParameter("actives") != null;
	if ( myAssay != null ) { 
		AssayPlate assayPlate = myAssay.getAssayData(); 
		if ( assayPlate.first() ) { %>
<table  class="dashboard">
<tr><td></td>
<% 	for ( int i = 1; i <= myAssay.getWidth(); i++ ) { %><th><%= String.format("%02d",i) %></th><% } %>
</tr>
<%	assayPlate.beforeFirstRow(); int row = 1;
	while ( assayPlate.nextRow() ) { %><tr><th><%= BaseForm.lettersForIndex(row) %></th><%
		row++;
		assayPlate.beforeFirstColumn();
		while ( assayPlate.nextColumn() ) { 
%><td><%
			if ( assayPlate.currentLocationExists() ) {
				String materialID = assayPlate.getMaterialID();
				String cultureID = assayPlate.getStrainID();
				if ( materialID != null && (! materialID.equals("0")) ) { 
%>Material: <a href="<%= contextPath %>/material?id=<%= materialID %>"><%= materialID %></a>
<% } else if ( cultureID != null ) { 
%>Culture: <a href="<%= contextPath %>/strain?id=<%= cultureID %>"><%= cultureID %></a>
<% } } } %></td><% } %></tr></table>
<% } } %>