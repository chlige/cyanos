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
<!DOCTYPE html>
<html>
<head>
<cyanos:header title="User Administration"/>
</head>
<body>
<cyanos:menu/>
<h1>User Administration</h1>
<hr width="80%">
<div class="content">
<% User thisUser = AdminServlet.getUser(request);
Project projectList = SQLProject.projects(AdminServlet.getSQLData(request), SQLProject.ID_COLUMN, SQLProject.ASCENDING_SORT);

if ( thisUser.isAllowed(User.ADMIN_ROLE, User.GLOBAL_PROJECT, Role.CREATE) ) {

	if ( request.getParameter("addUser") != null ) {
		MutableUser user = SQLMutableUser.createUser(AdminServlet.getSQLData(request), request.getParameter("userid"));
		user.setUserEmail(request.getParameter("email"));
		user.setUserName(request.getParameter("fullname"));
%><p>Creating user: <%= user.getUserID() %><br><%
		int[] perms = {Role.READ, Role.WRITE, Role.CREATE, Role.DELETE};
		for ( String role: User.ROLES ) {
			String inputName = "globalrole_".concat(role);
			String[] newPerms = request.getParameterValues(inputName);
			if ( newPerms != null && newPerms.length > 0 ) {
				int newBits = 0;
				for ( String bit: newPerms ) {
					newBits = newBits + Integer.parseInt(bit);
				}
				user.grantGlobalPermission(role, newBits);
			}
			inputName = "nullrole_".concat(role);
			newPerms = request.getParameterValues(inputName);
			if ( newPerms != null && newPerms.length > 0 ) {
				int newBits = 0;
				for ( String bit: newPerms ) {
					newBits = newBits + Integer.parseInt(bit);
				}
				user.grantPermissionForProject(User.NULL_PROJECT, role, newBits);
			}
			projectList.beforeFirst();
			while ( projectList.next() ) {
				inputName = "projectrole_".concat(role).concat("_").concat(projectList.getID());
				newPerms = request.getParameterValues(inputName);
				if ( newPerms != null && newPerms.length > 0 ) {
					int newBits = 0;
					for ( String bit: newPerms ) {
						newBits = newBits + Integer.parseInt(bit);
					}
					user.grantPermissionForProject(projectList.getID(), role, newBits);
				}
			}
		}
%>Roles:<br>
GLOBAL: <% for (  Role role: user.globalRoles() ) { %>
<%= role.roleName() %>(<%= role.permissionString() %>) <% } %><br>
NULL: <% for ( Role role: user.rolesForProject(User.NULL_PROJECT) ) { %>
<%= role.roleName() %>(<%= role.permissionString() %>) <% } %><br>
<% projectList.beforeFirst();
while ( projectList.next() ) { 
%><%= projectList.getID() %>: <% for ( Role role: user.rolesForProject(projectList.getID()) ) { %>
<%= role.roleName() %>(<%= role.permissionString() %>) <% } %><br>
<% } 
		SQLMutableUser.newPassword(request, user.getUserID(), user.getUserEmail());
%><p>An email has been sent to the user (<%= user.getUserEmail()  %>) notifying of the account creation and informing them of how to create their password</p><%
	} else {
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
</div>
</body>
</html>