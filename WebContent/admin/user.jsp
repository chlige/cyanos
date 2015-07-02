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
<hr>
<% User thisUser = AdminServlet.getUser(request);
if ( thisUser.isAllowed(User.ADMIN_ROLE, User.GLOBAL_PROJECT, Role.WRITE) ) {
	Project projectList = SQLProject.projects(AdminServlet.getSQLData(request), SQLProject.ID_COLUMN, SQLProject.ASCENDING_SORT);
	String userID = request.getParameter("userID");
	if ( userID != null ) {
		MutableUser user = SQLMutableUser.load(AdminServlet.getSQLData(request), userID); %>
<form method="post">
<table class="species">
<tr><td>User ID:</td><td><%= user.getUserID() %></td></tr>
<tr<%
	boolean update = (request.getParameter("updateUser") != null);
	if ( update && (! user.getUserName().equals(request.getParameter("fullname"))) ) {
		user.setUserName(request.getParameter("fullname"));
		out.print(" style=\"updated\"");
	} %>><td>Full Name:</td><td><input type="text" name="fullname" value="<%= user.getUserName() %>"></td></tr>
<tr<%
	if ( update && (! user.getUserEmail().equals(request.getParameter("email"))) ) {
		user.setUserEmail(request.getParameter("email"));
		out.print(" style=\"updated\"");
	} %>><td>Email address:</td><td><input type="text" name="email" value="<%= user.getUserEmail() %>"></td></tr>
</table>
<h2 style="text-align:center">Roles</h2>
<table class="dashboard">
<tr><td></td><th colspan="<%= User.ROLES.length %>">Permissions</th></tr>
<tr><th>Project</th><% for ( String role : User.ROLES ) { %><th><%= role %></th><% } %></tr>
<tr><td><i>GLOBAL</i></td>
<% int[] perms = {Role.READ, Role.WRITE, Role.CREATE, Role.DELETE};
 	Map<String,Role> roleMap = user.globalRoleMap();
	for ( String role : User.ROLES ) {  
		Role currentRole = roleMap.get(role);
		String inputName = "globalrole_".concat(role);
		if ( update ) {
			String[] newPerms = request.getParameterValues(inputName);
			if ( newPerms != null && newPerms.length > 0 ) {
				int newBits = 0;
				for ( String bit: newPerms ) {
					newBits = newBits + Integer.parseInt(bit);
				}
				if ( currentRole == null || currentRole.permissions() != newBits ) {
					user.grantGlobalPermission(role, newBits);
					currentRole = roleMap.get(role);
				}
			} else if ( currentRole != null ) {
				currentRole = null;
				user.removeGlobalRole(role);
			}
		}
%><td style="border-left: solid 1px black; border-right: solid 1px black;"><%
 for ( int perm : perms ) { %>
<input type="checkbox" name="<%= inputName %>" value="<%= String.format("%d", perm) %>" <%= (currentRole != null && currentRole.hasPermission(perm) ? "checked" : "" ) %>><%= Role.charForBit(perm) %>
<% } } %></tr>
<tr><td><i>NULL</i></td>
<%	roleMap = user.roleMapForProject(User.NULL_PROJECT);
	for (String role : User.ROLES ) {  Role currentRole = roleMap.get(role);  
	String inputName = "nullrole_".concat(role);
	if ( update ) {
		String[] newPerms = request.getParameterValues(inputName);
		if (  newPerms != null && newPerms.length > 0 ) {
			int newBits = 0;
			for ( String bit: newPerms ) {
				newBits = newBits + Integer.parseInt(bit);
			}
			if ( currentRole == null || currentRole.permissions() != newBits ) {
				user.grantPermissionForProject(User.NULL_PROJECT, role, newBits);
				currentRole = roleMap.get(role);
			}
		} else if ( currentRole != null ) {
			currentRole = null;
			user.removeFromProject(User.NULL_PROJECT, role);
		}
	}
%><td style="border-left: solid 1px black; border-right: solid 1px black;"><%
for ( int perm : perms ) { %>
<input type="checkbox" name="<%= inputName %>" value="<%= String.format("%d", perm) %>" <%= (currentRole != null && currentRole.hasPermission(perm) ? "checked" : "" ) %>><%= Role.charForBit(perm) %>
<% } out.print("</td>"); } %></tr>
<% projectList.beforeFirst();
	while ( projectList.next() ) { 
%><tr><td><%= projectList.getID() %></td>
<%	roleMap = user.roleMapForProject(User.NULL_PROJECT);
	for (String role : User.ROLES ) {  Role currentRole = roleMap.get(role); 
	
	String inputName = "projectrole_".concat(role).concat("_").concat(projectList.getID());
	if ( update ) {
		String[] newPerms = request.getParameterValues(inputName);
		if ( newPerms != null && newPerms.length > 0 ) {
			int newBits = 0;
			for ( String bit: newPerms ) {
				newBits = newBits + Integer.parseInt(bit);
			}
			if ( currentRole == null || currentRole.permissions() != newBits ) {
				user.grantPermissionForProject(projectList.getID(), role, newBits);
				currentRole = roleMap.get(role);
			}
		} else if ( currentRole != null ) {
			currentRole = null;
			user.removeFromProject(projectList.getID(), role);
		}
	}
%><td style="border-left: solid 1px black; border-right: solid 1px black;"><%
	for ( int perm : perms ) { %>
<input type="checkbox" name="<%= inputName %>" value="<%= String.format("%d", perm) %>" <%= (currentRole != null && currentRole.hasPermission(perm) ? "checked" : "" ) %>><%= Role.charForBit(perm) %>
<% } out.print("</td>"); } %></tr><% } %>
</table>
<p align="center"><button type="submit" name="updateUser">Update User</button> <button type="reset">Reset Values</button></p>
</form>	
<p align="center"><a href="user.jsp">Return to User List</a></p>
<%		
	} else {
if ( thisUser.isAllowed(User.ADMIN_ROLE, User.GLOBAL_PROJECT, Role.CREATE) )  {%>
<p align="center"><a href="user-add.jsp">Add a new user</a></p>
<% }  MutableUser userList = SQLMutableUser.users(AdminServlet.getSQLData(request));
userList.beforeFirst(); %>
<table class="dashboard">
<tr><th>Username</th><th>Full Name</th><th>Email address</th><th>Roles</th></tr>
<% while ( userList.next() ) { %>
<tr><td><a href="?userID=<%= userList.getUserID() %>"><%= userList.getUserID() %></a></td><td><%= userList.getUserName() %></td><td><%= userList.getUserEmail() %></td>
<td>GLOBAL: <% for (  Role role: userList.globalRoles() ) { %>
<%= role.roleName() %>(<%= role.permissionString() %>) <% } %><br>
NULL: <% for ( Role role: userList.rolesForProject(User.NULL_PROJECT) ) { %>
<%= role.roleName() %>(<%= role.permissionString() %>) <% } %><br>
<% projectList.beforeFirst();
while ( projectList.next() ) { 
%><%= projectList.getID() %>: <% for ( Role role: userList.rolesForProject(projectList.getID()) ) { %>
<%= role.roleName() %>(<%= role.permissionString() %>) <% } %><br>
<% } %>
</td></tr>
<% } %>
</table>
<% } } else { %>
<h2>ACCESS DENIED</h2>
<% } %>
</body>
</html>