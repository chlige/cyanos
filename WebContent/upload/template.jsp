<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.web.servlet.UploadServlet,
	edu.uic.orjala.cyanos.web.UploadForm,
	edu.uic.orjala.cyanos.sql.SQLData,
	edu.uic.orjala.cyanos.sql.SQLProtocol,
	edu.uic.orjala.cyanos.web.UploadForm, java.util.Map, java.util.Map.Entry, java.util.HashMap,
	java.util.List, java.util.ListIterator" %>
<% SQLData datasource = UploadServlet.getSQLData(request);
	UploadForm form = (UploadForm) session.getAttribute(UploadServlet.UPLOAD_JOB);
	String dataType = request.getParameter("template_type");
	String jspFile = form.jspForm(); %>
<p align='center'>
<% if ( jspFile != null ) { 
	if ( request.getParameter("loadTemplateForm") != null ) { 
	if ( request.getParameter("template") != null ) { 
		Map<String,String> template = SQLProtocol.loadProtocol(datasource, dataType, request.getParameter("template")); 
		if ( template != null ) { out.println("<b>Worksheet template loaded!</b>"); 
			for ( Entry<String,String> item : template.entrySet() ) {  %>
<input type="hidden" name="<%= item.getKey() %>" value="<%= item.getValue() %>">
<% } %>
<br><button type="submit" name="useTemplate">Use template</button>
<button type="button" onClick="updateForm(this,'<%= UploadServlet.TEMPLATE_DIV %>')">Cancel</button>
<% } else { out.println("<b>ERROR:</b> Unable to load worksheet template."); } 
  } else {
	List<String> allProtocols = SQLProtocol.listProtocols(datasource, dataType);
if ( allProtocols.size() > 0 ) { ListIterator<String> iter = allProtocols.listIterator(); %>
Select a template: <select name="template">
<%	while ( iter.hasNext() ) { %><option><%= iter.next() %></option><% } %>
</select><br>
<button type="button" name="loadTemplateForm" onClick="updateForm(this,'<%= UploadServlet.TEMPLATE_DIV %>')">Load</button>
<% } %>
<button type="button" onClick="updateForm(this,'<%= UploadServlet.TEMPLATE_DIV %>')">Cancel</button>
<% } } else if ( request.getParameter("saveTemplateForm") != null ) { 
	if ( request.getParameter("template") != null ) { 
		String templateName = request.getParameter("template");
		if ( templateName.equals("") ) { templateName = request.getParameter("newTemplate"); } 
if ( templateName.length() > 0 ) {
		SQLProtocol.saveProtocol(datasource, form.getTemplate(), dataType, templateName); %>
<b>Template saved as <%= templateName %></b><br>
<button type="button" name="loadTemplateForm" onClick="updateForm(this,'<%= UploadServlet.TEMPLATE_DIV %>')">Load a worksheet template</button>
<button type="button" name="saveTemplateForm" onClick="updateForm(this,'<%= UploadServlet.TEMPLATE_DIV %>')">Save as a worksheet template</button>
<div id="uploadForm" align="center">
<p align="center">
<input type="checkbox" name="<%=UploadForm.PARAM_HEADER%>" value="true" onClick="this.form.submit()" <%=( form.hasHeaderRow() ? "checked" : "")%>> Spreadsheet has a header row.</p>
<jsp:include page="<%=jspFile%>" />
<p align="center">
<select name="<%= UploadForm.PARAM_ROW_BEHAVIOR %>">
<option value="<%= UploadForm.ROW_BEHAVIOR_IGNORE  %>" <%= ( form.getRowBehavior() == UploadForm.ROW_BEHAVIOR_IGNORE ? "selected" : "") %>>Ignore</option>
<option value="<%= UploadForm.ROW_BEHAVIOR_INCLUDE  %>" <%= ( form.getRowBehavior() == UploadForm.ROW_BEHAVIOR_INCLUDE ? "selected" : "") %>>Include</option>
</select>
<b>selected rows.</b><br><button type="submit" name="<%=UploadServlet.PARSE_ACTION%>">Parse Upload</button></p>
</div>
<%
	} } else { form.updateTemplate(request);
%>
Save template as: <select name="template">
<option value="">A New Protocol -&gt;</option>
<%
	List<String> allProtocols = SQLProtocol.listProtocols(datasource, dataType); 
	ListIterator<String> iter = allProtocols.listIterator(); 
while ( iter.hasNext() ) {
%><option><%=iter.next()%></option><%
	}
%>
</select><input type="text" name="newTemplate"><br>
<button type="submit" name="saveTemplateForm">Save template</button>
<button type="button" onClick="updateForm(this,'<%=UploadServlet.TEMPLATE_DIV%>')">Cancel</button>
<div id="uploadForm" align="center">
<input type="checkbox" name="<%= UploadForm.PARAM_HEADER %>" value="true" onClick="this.form.submit()" <%=( form.hasHeaderRow()  ? "checked" : "")%>> Spreadsheet has a header row.</p>
<jsp:include page="<%=jspFile%>" />
</div>
<%
	} } else {
%>
<button type="button" name="loadTemplateForm" onClick="updateForm(this,'<%=UploadServlet.TEMPLATE_DIV%>')">Load a worksheet template</button>
<button type="button" name="saveTemplateForm" onClick="updateForm(this,'<%=UploadServlet.TEMPLATE_DIV%>')">Save as a worksheet template</button>
<div id="uploadForm" align="center">
<p align="center">
<input type="checkbox" name="<%=UploadForm.PARAM_HEADER%>" value="true" onClick="this.form.submit()" <%= ( form.hasHeaderRow()  ? "checked" : "") %>> Spreadsheet has a header row.</p>
<jsp:include page="<%= jspFile %>" />
<p align="center">
<select name="<%= UploadForm.PARAM_ROW_BEHAVIOR %>">
<option value="<%= UploadForm.ROW_BEHAVIOR_IGNORE  %>" <%= ( form.getRowBehavior() == UploadForm.ROW_BEHAVIOR_IGNORE ? "selected" : "") %>>Ignore</option>
<option value="<%= UploadForm.ROW_BEHAVIOR_INCLUDE  %>" <%= ( form.getRowBehavior() == UploadForm.ROW_BEHAVIOR_INCLUDE ? "selected" : "") %>>Include</option>
</select>
<b>selected rows.</b><br>
<% if ( form.resultReport() != null ) { %>
<br><button id="resultButton" name="showResults">Show Previous Results</button>
<% } else {%>
<button type="submit" name="<%= UploadServlet.PARSE_ACTION %>">Parse Upload</button>
<% } %>
</p>
</div>
<% } } else { %>
<b>NO FORM SPECIFIED</b>
<% } %>
</p>
