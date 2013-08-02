<%@ page import="edu.uic.orjala.cyanos.sql.SQLProject,
	edu.uic.orjala.cyanos.sql.SQLData,
	edu.uic.orjala.cyanos.web.servlet.ProjectServlet,
	edu.uic.orjala.cyanos.Project" %>
<%
	String contextPath = request.getContextPath();
	String projectID = request.getParameter(request.getParameter("fieldName")); 
	if ( projectID == null ) projectID = request.getParameter("project");
	Project allProjects = null;
	Object obj = request.getAttribute("allProjects");
	if ( obj != null && obj instanceof Project ) { 
		allProjects = (Project) obj;
	} else {
		obj = request.getAttribute(ProjectServlet.DATASOURCE);
		if (obj != null && obj instanceof SQLData) {
			allProjects = SQLProject.projects((SQLData) obj, SQLProject.ID_COLUMN, SQLProject.ASCENDING_SORT);
			request.setAttribute("allProjects", allProjects);
		}
	}
	if ( allProjects != null ) {
%>
<select name="<%= request.getParameter("fieldName") %>">
<option value="">NONE</option>
<%    
	allProjects.beforeFirst();
	while ( allProjects.next()) { %>
<option value="<%= allProjects.getID() %>" <%= (allProjects.getID().equals(projectID) ? "selected" : "") %>><%= allProjects.getName() %></option>
<% } %>
</select>
<% } %>
