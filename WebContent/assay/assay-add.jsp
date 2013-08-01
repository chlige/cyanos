<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page import="edu.uic.orjala.cyanos.Assay,
	edu.uic.orjala.cyanos.web.servlet.AssayServlet,
	edu.uic.orjala.cyanos.User,
	edu.uic.orjala.cyanos.Role,
	java.text.SimpleDateFormat,
	java.util.Date" %>
<% 	String contextPath = request.getContextPath();
User userObj = (User) session.getAttribute(AssayServlet.SESS_ATTR_USER);  %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<script language="JAVASCRIPT" src="cyanos.js"></script>
<script language="JAVASCRIPT" src="cyanos-date.js"></script>
<link rel="stylesheet" type="text/css" href="cyanos.css"/>
<title>Cyanos Database - New Assay</title>
</head>
<body>

<jsp:include page="/includes/menu.jsp"/>
<%-- <jsp:param value="<%= AssayServlet. %>" name="module"/> 
</jsp:include> --%>

<div class="content">
<h1 align="center">New Assay</h1>
<hr width="90%">
<% if ( request.getAttribute("error_msg") != null ) { %><div class="error"><%= request.getAttribute("error_msg") %></div><% } 
	if ( userObj != null && userObj.hasGlobalPermission(User.CULTURE_ROLE, Role.CREATE) ) { %>
<form name='editStrain' method="post" action="assay">
<input type="hidden" name="action" value="add">
<table class="species" style="width:80%; margin-left:auto; margin-right:auto">
<tr><td width='125'>Assay ID:</td><td><input type='text' name='newID' value="<c:out value='<%= request.getParameter("newID") %>'/>"></td></tr>
<tr><td width='125'>Assay Name:</td><td><input type='text' name='assayName' value="<c:out value='<%= request.getParameter("assayName") %>'/>"></td></tr>
<% SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
	String today = format.format(new Date()); %>
<tr><td>Date:</td><td><input type="text" name="assayDate" onFocus="showDate('div_calendar','assayDate')"  
	value="<c:out value='<%= request.getParameter("assayDate") %>' default='<%= today %>'/>" 
	style='padding-bottom: 0px' id="assayDate"/>
<a onclick="showDate('div_calendar','assatDate')"><img align="MIDDLE" border="0" src="<%= contextPath %>/images/calendar.png"></a>
<div id="div_calendar" class='calendar'>
<jsp:include page="/calendar.jsp">
<jsp:param value="assayDate" name="update_field"/>
<jsp:param value="div_calendar" name="div"/>
</jsp:include>
</div>
</td></tr>
<tr><td>Project</td><td>
<jsp:include page="/includes/project-popup.jsp">
<jsp:param value="project" name="fieldName"/>
</jsp:include>
</td></tr>
<tr><td>Target:</td><td><% String targetValue = request.getParameter("target"); if ( targetValue == null ) { targetValue = ""; }%>
<input id="target" type="text" name="target" VALUE="<%= targetValue %>" autocomplete='off' onKeyUp="livesearch(this, 'target', 'div_target')" onBlur="window.setTimeout(closeLS, 250, 'div_target');" style='padding-bottom: 0px'/>
<div id="div_target" class='livesearch'></div></td></tr>
<tr><td>Activity Threshold:</td><td>
<% String activeOp = request.getParameter("active_op"); %>
<select name="active_op">
<% String[] operators = { Assay.OPERATOR_LESS_THAN, Assay.OPERATOR_LESS_EQUAL, Assay.OPERATOR_EQUAL, Assay.OPERATOR_NOT_EQUAL, Assay.OPERATOR_GREATER_EQUAL, Assay.OPERATOR_GREATER_THAN}; 
for ( String op : operators ) { %>
<option value="<%= op %>" <%= (op.equals(activeOp) ? "selected" : "") %>><%= AssayServlet.getOperatorText(op) %></option>
<% } %>
</select><input type="text" name="active_level" value="<c:out value='<%= request.getParameter("active_level") %>'/>"></td></tr>
<tr><td>Size:</td><td><% String size = request.getParameter("size"); %>
<select name="size">
<% 	int[] lengths = { 4, 8, 8, 16, 32 };  
	int[] widths = { 6, 6, 12, 24, 48 }; 
	for ( int i = 0; i < lengths.length; i++ ) { 
		String thisSize = String.format("%dx%d", lengths[i], widths[i]); %>
<option value="<%= thisSize %>" <%= (thisSize.equals(size) ? "selected" : "") %>><%= String.format("%d wells (%s)", lengths[i] * widths[i], thisSize) %></option>
<% } %>
</select></td></tr>
<tr><td>Significant Figures:</td><td><input type="text" name="sigFigs" value="<c:out value='<%= request.getParameter("sigFigs") %>'/>"></td></tr>
<tr><td>Unit:</td><td><input name="unit" value="<c:out value='<%= request.getParameter("unit") %>'/>"></td></tr>
<tr><td valign=top>Notes:</td><td><textarea rows="7" cols="70" name="notes"><c:out value='<%= request.getParameter("notes") %>'/></textarea></td></tr>
<tr><td colspan="2" align="CENTER"><button type="submit" name="updateAssay">Add Assay</button>
<input type="RESET"></td></tr>
</table>
</form>
<% } %>
</div>
</body>
</html>
