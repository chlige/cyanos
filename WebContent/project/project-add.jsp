<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page import="edu.uic.orjala.cyanos.web.servlet.ProjectServlet,
	edu.uic.orjala.cyanos.User,
	edu.uic.orjala.cyanos.Role,
	edu.uic.orjala.cyanos.Project" %>
<% 	String contextPath = request.getContextPath(); 
	User userObj = ProjectServlet.getUser(request);  %>
<h2 align="center">New Project</h2>
<div class="content">
<% if ( userObj != null && userObj.hasGlobalPermission(User.PROJECT_MANAGER_ROLE, Role.CREATE) ) { %>
<form name='editProject' method="post" action="project">
<table class="species" align='center'>
<tr><td width='150'>Project ID:</td><td><input type="text" name="newID"></td></tr>
<tr><td>Title:</td><td><input type='text' name='label' size="75"></td></tr>
<tr><td valign=top>Notes:</td><td><textarea rows="7" cols="70" name="notes"></textarea></td></tr>
<tr><td colspan=2><hr width="50%"><h3 align="center">Master server</h3></td></tr>
<tr><td>Master Server URL:</td><td><input type='text' name='masterURL' size="75"></td></tr> 
<tr><td valign="top">Master Server Public Key:</td><td><textarea rows="12" cols="65" name='masterKey'></textarea></td></tr>
<tr><td>Update Preferences:</td><td>
<table class="species">

<% String[] options = { "No Update", "Send Only", "Receive Only", "Send/Receive"}; 
int[] optBits = { 0, Project.UPDATE_SEND, Project.UPDATE_RECEIVE, Project.UPDATE_SEND_RECIEVE}; %>
<tr><td>Collections:</td><td><select name="pref_collection">
<% for (int i = 0; i < optBits.length; i++) { %>
<option value="<%= optBits[i] %>"><%= options[i] %></option>
<% } %>
</select></td></tr>
<tr><td>Strains:</td><td><select name="pref_strain">
<% for (int i = 0; i < optBits.length; i++) { %>
<option value="<%= optBits[i] %>"><%= options[i] %></option>
<% } %>
</select></td></tr>
<tr><td>Materials:</td><td><select name="pref_material">
<% for (int i = 0; i < optBits.length; i++) { %>
<option value="<%= optBits[i] %>"><%= options[i] %></option>
<% } %>
</select> 
<input type="checkbox" value="<%= Project.UPDATE_RECEIVE_LOCAL_ONLY  %>" name="pref_material"> Only receive data for local strains.
</td></tr>
<tr><td>Assays:</td><td><select name="pref_assay">
<% for (int i = 0; i < optBits.length; i++) { %>
<option value="<%= optBits[i] %>"><%= options[i] %></option>
<% } %>
</select> 
<input type="checkbox" value="<%= Project.UPDATE_RECEIVE_LOCAL_ONLY  %>" name="pref_assay"> Only receive data for local strains.
</td></tr>
</table>
</td></tr>
<tr><td colspan=2><hr width="50%"><h3 align="center">Slave systems</h3></td></tr>	
<tr><td colspan=2 align="center"><b>New Slave</b></td></tr>
<tr><td>Hostname:</td><td><input type="text" name="new_hostname" size=50></td>
<tr><td>Host UUID:</td><td><input type="text" name="new_hostid" size=75></td>
<tr><td valign="top">Host Public Key:</td><td><textarea rows="12" cols="65" name='new_hostkey'></textarea></td></tr>	
</table>
<!--  <p align="CENTER"><button type="button" name="updateProject" onClick="updateForm(this,'<%= ProjectServlet.INFO_FORM_DIV_ID %>')">Update</button> -->
<p align="CENTER"><button type="submit" name="addProject">Create</button>
<input type="RESET"></p>
</form>
<% } else { %>
<p><b>Forbidden:</b> user has insufficient permission to create a new project.</p>
<% } %>
</div>
