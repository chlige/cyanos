<%@ tag language="java" pageEncoding="UTF-8"%>
<%@ tag import="edu.uic.orjala.cyanos.sql.SQLProject,
	edu.uic.orjala.cyanos.sql.SQLData,
	edu.uic.orjala.cyanos.web.servlet.ProjectServlet,
	edu.uic.orjala.cyanos.Project" %>
<%@ attribute name="project" required="false" %>
<%@ attribute name="fieldName" required="true" %>
<%	String contextPath = request.getContextPath();
	String fieldName = (String) jspContext.getAttribute("fieldName");
	String projectID = request.getParameter(fieldName);
	
	if ( projectID == null ) projectID = (String) jspContext.getAttribute("project");
	Project allProjects = null;
	Object obj = request.getAttribute("allProjects");
	if ( obj != null && obj instanceof Project ) { 
		allProjects = (Project) obj;
	} else {
		allProjects = SQLProject.projects(ProjectServlet.getSQLData(request), SQLProject.ID_COLUMN, SQLProject.ASCENDING_SORT);
		request.setAttribute("allProjects", allProjects);
	}
	if ( allProjects != null ) {
%><select name="${fieldName}">
<option value="">NONE</option>
<%  allProjects.beforeFirst();
	while ( allProjects.next()) { 
%><option value="<%= allProjects.getID() %>" <%= (allProjects.getID().equals(projectID) ? "selected" : "") %>><%= allProjects.getName() %></option>
<% } %></select><% } %>