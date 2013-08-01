<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.web.AppConfig,
	edu.uic.orjala.cyanos.User,
	edu.uic.orjala.cyanos.web.servlet.MainServlet, 
	java.util.Map, java.util.Map.Entry,
	java.util.Set, java.util.HashSet" %>
<h2 align="center">Setup Administrator Account</h2>
<% AppConfig appConfig = (AppConfig) session.getAttribute(MainServlet.APP_CONFIG_ATTR); 
	Map<String,String> setupValues = (Map<String,String>) session.getAttribute(MainServlet.ATTR_SETUP_VALUES);
	boolean valid = false;
	if ( appConfig != null ) { %>
<% if ( setupValues.containsKey(MainServlet.SETUP_HAS_ADMIN) ) { valid = true; %>
<p class="mainContent">Administrator account(s) already exist for this application.</p>
<ul>
<% Map<String,String> admins = (Map<String,String>) session.getAttribute(MainServlet.SETUP_HAS_ADMIN);
	for (Entry<String,String> admin : admins.entrySet() )  { %>
<li><%= admin.getValue() %> (<%= admin.getKey() %>)</li>
	<% } %>
</ul>
<% } else { %>
<p class="mainContent">Setup an administrator account for the management of this application.</p>
<h3>Create a new user account</h3>
<p><b>NOTE:</b> By default this user will only be given right to change the configuration of the application and will not be granted access rights to the data.</p>
<table>
<tr><td>Login:</td><td><input type="text" name="<%= MainServlet.SETUP_ADMIN_ID %>" value="<c:out value="<%= setupValues.get(MainServlet.SETUP_ADMIN_ID) %>"/>"></td></tr>
<tr><td>Name:</td><td><input type="text" name="<%= MainServlet.SETUP_ADMIN_NAME %>" value="<c:out value="<%= setupValues.get(MainServlet.SETUP_ADMIN_NAME) %>"/>"></td></tr>
<tr><td>E-mail:</td><td><input type="text" name="<%= MainServlet.SETUP_ADMIN_EMAIL %>" value="<c:out value="<%= setupValues.get(MainServlet.SETUP_ADMIN_EMAIL) %>"/>"></td></tr>
<tr><td>Password:</td><td><input type="password" name="<%= MainServlet.SETUP_ADMIN_PWD %>" 
	onChange="validatePassword(this.value, this.form.elements['<%= MainServlet.SETUP_ADMIN_PWD %>-confirm'].value, this.form.elements['nextPage'], 'passwordMsg')"></td></tr>
<tr><td>Confirm Password:</td><td><input type="password" name="<%= MainServlet.SETUP_ADMIN_PWD %>-confirm"
	onChange="validatePassword(this.value, this.form.elements['<%= MainServlet.SETUP_ADMIN_PWD %>'].value, this.form.elements['nextPage'], 'passwordMsg')"></td>
	<td id="passwordMsg"></td></tr>
<tr><td>Additional Roles:</td><td>
<% 	String roleString = setupValues.get(MainServlet.SETUP_ADMIN_ROLES); 
	Set<String> roles = new HashSet<String>(5);
	if ( roleString != null ) {
		for ( String role : roleString.split("\\s*,\\s*") ) {
			roles.add(role);
		}
	}
%><b>Note:</b> User will be granted read (R), write (W), create (C), and delete (D) permissions for selected roles.<br>
<input type="checkbox" name="<%= MainServlet.SETUP_ADMIN_ROLES %>" value="<%= User.BIOASSAY_ROLE %>"<%= ( roles.contains(User.BIOASSAY_ROLE) ? " checked" : "") %>> <%= User.BIOASSAY_ROLE %><br>
<input type="checkbox" name="<%= MainServlet.SETUP_ADMIN_ROLES %>" value="<%= User.CULTURE_ROLE %>"<%= ( roles.contains(User.CULTURE_ROLE) ? " checked" : "") %>> <%= User.CULTURE_ROLE %><br>
<input type="checkbox" name="<%= MainServlet.SETUP_ADMIN_ROLES %>" value="<%= User.PROJECT_MANAGER_ROLE %>"<%= ( roles.contains(User.PROJECT_MANAGER_ROLE) ? " checked" : "") %>> <%= User.PROJECT_MANAGER_ROLE %><br>
<input type="checkbox" name="<%= MainServlet.SETUP_ADMIN_ROLES %>" value="<%= User.SAMPLE_ROLE %>"<%= ( roles.contains(User.SAMPLE_ROLE) ? " checked" : "") %>> <%= User.SAMPLE_ROLE %></td></tr>
</table>
<% valid = setupValues.containsKey(MainServlet.SETUP_ADMIN_ID); } } %>
<table class="buttons"><tr>
<td><button type="submit" name="prevPage">&lt; Previous</button></td>
<td><button name="nextPage" type="submit" <%= ( valid ? "" : "disabled" ) %>>Next &gt;</button></td></tr>
</table>
