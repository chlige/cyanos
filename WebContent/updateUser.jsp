<%@ taglib tagdir="/WEB-INF/tags" prefix="cyanos" %>
<%@ page import="edu.uic.orjala.cyanos.web.servlet.AdminServlet,
	edu.uic.orjala.cyanos.User,
	edu.uic.orjala.cyanos.sql.SQLMutableUser,
	edu.uic.orjala.cyanos.Role,
	edu.uic.orjala.cyanos.sql.SQLProject,
	edu.uic.orjala.cyanos.Project,
	java.util.List" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<jsp:include page="/includes/header-template.jsp"/>
<title>User Administration</title>
</head>
<body>
<cyanos:menu/>
<h1>User Administration</h1>
<hr>
<% 	User user = AdminServlet.getUser(request); 
	Project projectList = SQLProject.projects(AdminServlet.getSQLData(request), SQLProject.ID_COLUMN, SQLProject.ASCENDING_SORT); %>
<form method="post">
<table class="species">
<tr><td>User ID:</td><td><%= user.getUserID() %></td></tr>
<tr><td>Full Name:</td><td><%= user.getUserName() %></td></tr>
<tr<%
	boolean update = request.getParameter("updateUser") != null;
	if ( update && (! user.getUserEmail().equals(request.getParameter("email"))) ) {
		SQLMutableUser.updateEmail(request, request.getParameter("email"));
		out.print(" style=\"updated\"");
	} %>><td>Email address:</td><td><input type="text" name="email" value="<%= user.getUserEmail() %>"></td></tr>
</table>
<h2 style="text-align:center">Change Password</h2>
<%
	if ( request.getParameter("pwd1") != null ) {
		String pwd1 = request.getParameter("pwd1");
		if ( pwd1.equals(request.getParameter("pwd2")) ) {
			SQLMutableUser.updatePassword(request, pwd1);
%><p align='center' style='color: green'>Password changed</p><%			
		} else {
%><p align='center' style='color: red'>Password mismatch</p><%			
		}
	}
%>
<table class="species">
<tr><td>New password:</td><td><input type="password" name="pwd1"></td></tr>
<tr><td>Confirm new password:</td><td><input type="password" name="pwd2"></td></tr>
</table>
<h2 style="text-align:center">Roles</h2>
<table class="dashboard">
<tr><td>GLOBAL</td><td><% for (  Role role: user.globalRoles() ) { %>
<%= role.roleName() %>(<%= role.permissionString() %>) <% } %></td></tr>
<tr><td>NULL</td><td><% for ( Role role: user.rolesForProject(User.NULL_PROJECT) ) { %>
<%= role.roleName() %>(<%= role.permissionString() %>) <% } %></td></tr>
<% projectList.beforeFirst();
while ( projectList.next() ) { 
%><tr><td><%= projectList.getID() %></td><td><% for ( Role role: user.rolesForProject(projectList.getID()) ) { %>
<%= role.roleName() %>(<%= role.permissionString() %>) <% } %></td></tr>
<% } %>
</table>
<p align="center"><button type="submit" name="updateUser">Update Information</button></p>
</form>	
</body>
</html>