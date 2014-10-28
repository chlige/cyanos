<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="cyanos" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="edu.uic.orjala.cyanos.Assay,
	edu.uic.orjala.cyanos.Role,edu.uic.orjala.cyanos.web.servlet.AssayServlet,edu.uic.orjala.cyanos.web.servlet.ProjectServlet,
	edu.uic.orjala.cyanos.Strain,
	edu.uic.orjala.cyanos.sql.SQLData,
	java.text.SimpleDateFormat,
	edu.uic.orjala.cyanos.web.BaseForm,
	edu.uic.orjala.cyanos.CyanosObject,
	edu.uic.orjala.cyanos.Project,
	java.util.GregorianCalendar,
	java.util.Calendar,
	java.util.Date,
	java.util.regex.Pattern,
	java.util.regex.Matcher,
	java.math.BigDecimal,
	java.math.MathContext" %>
<% 	String contextPath = request.getContextPath();
	Assay thisObject = (Assay) request.getAttribute(AssayServlet.ASSAY_OBJECT); 	
	SimpleDateFormat dateFormat = (SimpleDateFormat) session.getAttribute("dateFormatter");
	boolean update = request.getParameter("updateAssay") != null;
	if ( thisObject == null ) {  %>
<p align='center'><b>ERROR:</b> Object not passed</p>
<% out.flush(); return; } else if ( ! thisObject.first() ) { %>
<p align='center'><b>ERROR:</b> Object not found</p>
<% out.flush(); return; } 
%><div CLASS="showSection" ID="view_info">
<table class="species" align='center'>
<tr><td width='125'>Assay ID:</td><td><%= thisObject.getID() %></td></tr>
<tr<% if ( update ) { 
	String value = request.getParameter("assayName");
	if (value != null && (! value.equals(thisObject.getName()) ) ) {
		thisObject.setName(value);
%> class="updated"<% } } %>><td>Assay Name:</td><td><%= thisObject.getName() %></td></tr>
<tr<% if ( update ) { 
	String value = request.getParameter("assayDate");
	if (value != null && (! value.equals(thisObject.getDateString()) ) ) {
		thisObject.setDate(value);
%> class="updated"<% } } %>><td>Assay Date:</td><td><%= dateFormat.format(thisObject.getDate()) %></td></tr>
<tr<% if ( update ) { 
	String value = request.getParameter("project");
	if (value != null && (! value.equals(thisObject.getProjectID()) ) ) {
		thisObject.setProjectID(value); %>class="updated"<% } } %>><td>Project</td><td>
<% Project aProj = thisObject.getProject(); if ( aProj != null && aProj.first() ) { 
%><a href='project?id=<%= aProj.getID() %>'><%= aProj.getName() %></a>
<% } else { %>None
<% } %></td></tr>
<tr<% if ( update ) { 
	String value = request.getParameter("target");
	if (value != null && (! value.equals(thisObject.getTarget()) ) ) {
		thisObject.setTarget(value);	
%>
class="updated"
<% } } %>
><td>Target:</td><td><%= thisObject.getTarget() %></td></tr>
<tr<%if ( update ) {
		String value = request.getParameter("unit");
		if ( value != null && (! value.equals(thisObject.getUnit())) ) {
			thisObject.setUnit(value);
		}
	}
%>><td>Unit:</td><td><c:out value="<%= thisObject.getUnit() %>"/></td></tr>
<tr<%if ( update ) {
		String value = request.getParameter("sigFigs");
		String compareValue = Integer.toString(thisObject.getSigFigs());
		if ( value != null && (! value.equals(compareValue)) ) {
			thisObject.setSigFigs(value);
		}
	}
%>><td>Significant Figures:</td><td><c:out value="<%= thisObject.getSigFigs() %>"/></td></tr>
<tr<%if ( update ) {
	String value = request.getParameter("active_op");
	boolean updated = false;
	if ( value != null && (! value.equals(thisObject.getActiveOperator())) ) {
		updated = true;
		thisObject.setActiveOperator(value);
	}
	value = request.getParameter("active_level");
	BigDecimal active = thisObject.getActiveLevel();
	String compareValue = ( active != null ? active.toPlainString() : null);
	if ( value != null && (! value.equals(compareValue)) ) {
		updated = true;
		thisObject.setActiveLevel(value);
	}
	if ( updated ) {
		out.print("class=\"updated\"");
	}
}%>><td>Activity Threshold:</td><td><%= AssayServlet.getOperatorText(thisObject.getActiveOperator()) %>
<%
	BigDecimal active = thisObject.getActiveLevel();
	out.print(active.round(new MathContext(thisObject.getSigFigs()))); 
	if ( thisObject.getUnit() != null ) { 
		out.print(" "); out.print(thisObject.getUnit()); 
	}
%></td></tr>
<tr<%
if ( update ) {
	String value = request.getParameter("size");
	String compareValue = String.format("%dx%d", thisObject.getLength(), thisObject.getWidth());
	if ( value != null && (! value.equals(compareValue)) ) {
		Pattern pattern = Pattern.compile("(\\d+)x(\\d+)", Pattern.CASE_INSENSITIVE);
		Matcher match = pattern.matcher(value);
		if ( match.matches() ) {
			thisObject.setLength(Integer.parseInt(match.group(1)));			
			thisObject.setWidth(Integer.parseInt(match.group(2)));
		} 
		out.print(" class=\"updated\"");
	}
} %>><td>Size:</td><td><%= String.format("%dx%d", thisObject.getLength(), thisObject.getWidth()) %> (<%= thisObject.getLength() * thisObject.getWidth() %> wells) </td></tr>
<tr<% if ( update ) { 
	String value = request.getParameter("notes");
	if (value != null && (! value.equals(thisObject.getNotes()) ) ) {
		thisObject.setNotes(value);	
%> class="updated"<% } } %>><td valign=top>Notes:</td><td><%= AssayServlet.formatStringHTML(thisObject.getNotes()) %></td></tr>
</table>
<% if ( thisObject.isAllowed(Role.WRITE)) { %>
<p align='center'><button type='button' onClick='flipDiv("info")'>Edit Values</button></p>
</div>
<div class='hideSection' id="edit_info">
<form name='editMaterial'>
<input type="hidden" name="id" value="<%= thisObject.getID() %>">
<table class="species" align='center'>
<tr><td width='125'>Assay ID:</td><td><%= thisObject.getID() %></td></tr>
<tr><td>Assay Name:</td><td><input type="text" name="assayName" value="<%= thisObject.getName() %>" size=50></td></tr>
<tr><td>Assay Date:</td><td>
<% Date assayDate = thisObject.getDate();  
	Calendar assayCal = GregorianCalendar.getInstance();
	if ( assayDate != null ) { assayCal.setTime(assayDate); }
%><cyanos:calendar-field fieldName="assayDate" dateValue="<fmt:formatDate value="<%= assayCal.getTime() %>" pattern="yyyy-MM-dd"/>"/>
<%--
<input type="text" name="assayDate" onFocus="showDate('div_calendar','assayDate')" style='padding-bottom: 0px' value='' id="assayDate"/>
<a onclick="showDate('div_calendar','assayDate')"><img align="MIDDLE" border="0" src="<%= contextPath %>/images/calendar.png"></a>
<div id="div_calendar" class='calendar'>
<jsp:include page="/calendar.jsp">
<jsp:param value="assayDate" name="update_field"/>
<jsp:param value="div_calendar" name="div"/>
</jsp:include>
</div>  --%>
</td></tr>
<tr><td>Project</td><td>
<jsp:include page="/includes/project-popup.jsp">
<jsp:param value="<%= thisObject.getProjectID() %>" name="project"/>
<jsp:param value="project" name="fieldName"/></jsp:include>
</td></tr>
<tr><td>Target:</td><td><% String targetValue = thisObject.getTarget(); if ( targetValue == null ) { targetValue = ""; }%>
<input id="target" type="text" name="target" VALUE="<%= targetValue %>" autocomplete='off' onKeyUp="livesearch(this, 'target', 'div_target')" onBlur="window.setTimeout(closeLS, 250, 'div_target');" style='padding-bottom: 0px'/>
<div id="div_target" class='livesearch'></div></td></tr>
<tr><td>Activity Threshold:</td><td>
<% String activeOp = thisObject.getActiveOperator(); %><select name="active_op">
<% String[] operators = { Assay.OPERATOR_LESS_THAN, Assay.OPERATOR_LESS_EQUAL, Assay.OPERATOR_EQUAL, Assay.OPERATOR_NOT_EQUAL, Assay.OPERATOR_GREATER_EQUAL, Assay.OPERATOR_GREATER_THAN}; 
for ( String op : operators ) { 
%><option value="<%= op %>" <%= (op.equals(activeOp) ? "selected" : "") %>><%= AssayServlet.getOperatorText(op) %></option>
<% } %></select><input type="text" name="active_level" value="<c:out value="<%= thisObject.getActiveLevel() %>"/>"></td></tr>
<tr><td>Size:</td><td><% String size =  String.format("%dx%d", thisObject.getLength(), thisObject.getWidth()); %>
<select name="size">
<% 
	int[] lenghts = { 4, 8, 8, 16, 32 };  
	int[] widths = { 6, 6, 12, 24, 48 }; 
	for ( int i = 0; i < lenghts.length; i++ ) { 
		String thisSize = String.format("%dx%d", lenghts[i], widths[i]); 
%><option value="<%= thisSize %>" <%= (size.equals(thisSize) ? "selected" : "") %>><%= String.format("%d wells (%s)", lenghts[i] * widths[i], thisSize) %></option>
<% } %></select></td></tr>
<tr><td>Unit Format:</td><td><input name="unit" value="<c:out value="<%= thisObject.getUnit() %>"/>"></td></tr>
<tr><td>Significant Figures:</td><td><input name="sigFigs" value="<c:out value="<%= thisObject.getSigFigs() %>"/>"></td></td>
<tr><td valign=top>Notes:</td><td><textarea rows="7" cols="70" name="notes"><c:out value="<%= thisObject.getNotes() %>" default="" /></textarea></td></tr>
<tr><td colspan="2" align="CENTER"><button type="button" name="updateAssay" onClick="updateForm(this,'<%= AssayServlet.INFO_FORM_DIV_ID %>')">Update</button>
<input type="RESET"></td></tr>
</table>
</form>
<p align="center"><button type='button' onClick='flipDiv("info")'>Close Form</button></p>
<% } %></div>