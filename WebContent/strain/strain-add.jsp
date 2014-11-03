<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="cyanos" tagdir="/WEB-INF/tags" %>
<%@ page import="edu.uic.orjala.cyanos.Strain,
	edu.uic.orjala.cyanos.web.servlet.StrainServlet,
	edu.uic.orjala.cyanos.User,
	edu.uic.orjala.cyanos.Role,
	java.text.SimpleDateFormat,
	java.util.Date" %>
<% 	String contextPath = request.getContextPath();
User userObj = StrainServlet.getUser(request);  %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<cyanos:header title="Add Strain"/>
</head>
<body>
<cyanos:menu helpModule="<%= StrainServlet.HELP_MODULE %>"/>
<div class="content">
<h1 align="center">New Strain</h1>
<hr width="90%">
<% if ( request.getAttribute("error_msg") != null ) { %><div class="error"><%= request.getAttribute("error_msg") %></div><% } 
	if ( userObj != null && userObj.hasGlobalPermission(User.CULTURE_ROLE, Role.CREATE) ) { %>
<form name='editStrain' method="post" action="strain">
<input type="hidden" name="action" value="add">
<table class="species" style="width:80%; margin-left:auto; margin-right:auto">
<tr><td width='125'>Strain ID:</td><td><input type='text' name='newID' value="<c:out value='<%= request.getParameter("newID") %>'/>"></td></tr>
<tr><td width='125'>Culture source:</td><td><input type='text' name='culture_source' value="<c:out value='<%= request.getParameter("culture_source") %>'/>"></td></tr>
<tr><td></td><td>
<table><tr><td>Collection:</td><td><input type="text" id="collection" name="collection"  value="<c:out value='<%= request.getParameter("collection") %>'/>" autocomplete="off" onkeyup="livesearch(this, 'collection', 'validcols')" style="padding-bottom: 0px">
<div class="livesearch" id="validcols"></div></td>
<td>Isolation:</td><td><input type="text" id="isolation" name="isolation"  value="<c:out value='<%= request.getParameter("isolation") %>'/>" autocomplete="off" onkeyup="livesearch(this, 'isolation', 'validisos')" style="padding-bottom: 0px">
<div class="livesearch" id="validisos"></div></td></tr></table></td></tr>
<tr><td>Scientific name:</td><td>
<table><tr><td><input type='text' name='sci_name'  value="<c:out value='<%= request.getParameter("sci_name") %>'/>"
	onChange="if (! this.form.elements['genus'].value ) { this.form.elements['genus'].value = this.value.split(' ')[0]; } "></td><td>
Genus:</td><td><input id="genus" type="TEXT" name="genus" value="<c:out value='<%= request.getParameter("genus") %>'/>" autocomplete="off" onkeyup="livesearch(this, 'genus', 'validgenus')" style="padding-bottom: 0px">
<div class="livesearch" id="validgenus"></div></td></tr></table></td></tr>
<tr><td>Date Added:</td><td><cyanos:calendar-field fieldName="addDate"/></td></tr>
<tr><td>Default Media:</td><td><input type="text" name="def_media" value="<c:out value='<%= request.getParameter("def_media") %>'/>"></td></tr>
<% String status = request.getParameter("culture_status"); %>
<tr><td>Culture status:</td><td><select name="culture_status">
<option value="<%= Strain.GOOD_STATUS %>" <%= ( Strain.GOOD_STATUS.equalsIgnoreCase(status) ? "selected" : "" ) %>>Good</option>
<option value="<%= Strain.SLOW_GROWTH_STATUS %>" <%= ( Strain.SLOW_GROWTH_STATUS.equalsIgnoreCase(status) ? "selected" : "") %>>Slow Growth</option>
<option value="<%= Strain.CONTAMINATED_STATUS %>" <%= ( Strain.CONTAMINATED_STATUS.equalsIgnoreCase(status) ? "selected" : "" ) %>>Contaminated</option>
<option value="<%= Strain.REMOVED_STATUS %>" <%= ( Strain.REMOVED_STATUS.equalsIgnoreCase(status) ? "selected" : "" ) %>>Removed</option>
<option value="<%= Strain.FIELD_HARVEST_STATUS %>" <%= ( Strain.FIELD_HARVEST_STATUS.equalsIgnoreCase(status) ? "selected" : "") %>>Field Collection</option>
</select></td></tr>
<tr><td>Project</td><td>
<jsp:include page="/includes/project-popup.jsp">
<jsp:param value="project" name="fieldName"/>
</jsp:include>
</td></tr>
<tr><td valign=top>Notes:</td><td><textarea rows="7" cols="70" name="notes"><c:out value='<%= request.getParameter("notes") %>'/></textarea></td></tr>
<tr><td colspan="2" align="CENTER"><button type="submit" name="updateStrain">Add Strain</button>
<input type="RESET"></td></tr>
</table>
</form>
<% } %>
</div>
</body>
</html>
