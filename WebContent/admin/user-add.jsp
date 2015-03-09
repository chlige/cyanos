<%@ taglib tagdir="/WEB-INF/tags" prefix="cyanos" %>
<%@ page import="edu.uic.orjala.cyanos.web.servlet.AdminServlet,
	edu.uic.orjala.cyanos.User,
	edu.uic.orjala.cyanos.MutableUser,
	edu.uic.orjala.cyanos.sql.SQLMutableUser,
	edu.uic.orjala.cyanos.Role,
	edu.uic.orjala.cyanos.sql.SQLProject,
	edu.uic.orjala.cyanos.Project,
	java.util.List,
	java.util.Map,
	java.text.SimpleDateFormat" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<cyanos:header title="User Administration"/>
</head>
<body>
<cyanos:menu/>
<h1>User Administration</h1>
<hr width="80%">
<% User thisUser = AdminServlet.getUser(request);
if ( thisUser.isAllowed(User.ADMIN_ROLE, User.GLOBAL_PROJECT, Role.CREATE) ) {

	if ( request.getParameter("addUser") != null ) {

	} else {
	
	Project projectList = SQLProject.projects(AdminServlet.getSQLData(request), SQLProject.ID_COLUMN, SQLProject.ASCENDING_SORT);
%><form method="post">
<table class="species">
<tr><td>User ID:</td><td><input type="text" name="userid"></td></tr>
<tr><td>Full Name:</td><td><input type="text" name="fullname"></td></tr>
<tr><td>Email address:</td><td><input type="text" name="email"></td></tr>
</table>
<h2 style="text-align:center">Roles</h2>
<table class="dashboard">
<tr><td></td><th colspan="<%= User.ROLES.length %>">Permissions</th></tr>
<tr><th>Project</th><% for ( String role : User.ROLES ) { %><th><%= role %></th><% } %></tr>
<tr><td><i>GLOBAL</i></td>
<% int[] perms = {Role.READ, Role.WRITE, Role.CREATE, Role.DELETE};
	for ( String role : User.ROLES ) {  
		String inputName = "globalrole_".concat(role);
%><td style="border-left: solid 1px black; border-right: solid 1px black;"><%
 for ( int perm : perms ) { %>
<input type="checkbox" name="<%= inputName %>" value="<%= String.format("%d", perm) %>"><%= Role.charForBit(perm) %>
<% } } %></tr>
<tr><td><i>NULL</i></td>
<%	for (String role : User.ROLES ) { 
	String inputName = "nullrole_".concat(role);
%><td style="border-left: solid 1px black; border-right: solid 1px black;"><%
for ( int perm : perms ) { %>
<input type="checkbox" name="<%= inputName %>" value="<%= String.format("%d", perm) %>"><%= Role.charForBit(perm) %>
<% } out.print("</td>"); } %></tr>
<% projectList.beforeFirst();
	while ( projectList.next() ) { 
%><tr><td><%= projectList.getID() %></td>
<%	for (String role : User.ROLES ) { 
	String inputName = "projectrole_".concat(role).concat("_").concat(projectList.getID());
%><td style="border-left: solid 1px black; border-right: solid 1px black;"><%
	for ( int perm : perms ) { %>
<input type="checkbox" name="<%= inputName %>" value="<%= String.format("%d", perm) %>"><%= Role.charForBit(perm) %>
<% } out.print("</td>"); } %></tr><% } %>
</table>
<p align="center"><button type="submit" name="addUser">Add User</button> <button type="reset">Reset Values</button></p>
</form>	
<% } } else { %>
<h2>ACCESS DENIED</h2>
<% } %>
</body>
</html>