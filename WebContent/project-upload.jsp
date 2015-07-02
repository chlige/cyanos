<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="cyanos" tagdir="/WEB-INF/tags" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.Project,
	edu.uic.orjala.cyanos.web.servlet.ProjectServlet,
	edu.uic.orjala.cyanos.web.task.RecvUpdateJob,
	edu.uic.orjala.cyanos.xml.ProjectUpdateXMLHandler,
	java.text.SimpleDateFormat,
	java.io.PrintWriter" %>
<%	String contextPath = request.getContextPath();
	Project myObject = (Project) request.getAttribute(ProjectServlet.PROJECT_OBJECT); 
%><!DOCTYPE html>
<html>
<head>
<cyanos:header title="Cyanos - Update Project"/>
</head>
<body>
<cyanos:menu/>

<div class='content'>
<%
	if ( request.getParameter("clearJob") != null ) {
	session.removeAttribute(ProjectServlet.SESS_ATTR_UPDATE_JOB);	
}
	if ( session.getAttribute(ProjectServlet.SESS_ATTR_UPDATE_JOB) != null ) { 
	RecvUpdateJob job = (RecvUpdateJob) session.getAttribute(ProjectServlet.SESS_ATTR_UPDATE_JOB); 
	ProjectUpdateXMLHandler handler = job.getXMLHandler();
%>
<table>
<tr><th>Strains</th><td><%= handler.getStrainCount() %></td></tr>
<tr><th>Materials</th><td><%= handler.getMaterialCount() %></td></tr>
<tr><th>Harvests</th><td><%= handler.getHarvestCount() %></td></tr>
<tr><th>Collection</th><td><%= handler.getCollectionCount() %></td></tr>
<tr><th>Assays</th><td><%= handler.getAssayCount() %></td></tr>
</table>
<%= (job.isRunning() ? "RUNNING" : "STOPPED") %>
<form>
<button type="submit">Refresh</button>
<% if ( ! job.isRunning() ) { %>
<button type="submit" name="clearJob">Clear Job</button>
<% } %>
</form>
<% if ( job.hasException() ) { 
	out.println("<pre>");
	Throwable execp = job.getException();
	PrintWriter writer = new PrintWriter(out); 
	while ( execp != null ) {
		out.print(execp.getLocalizedMessage()); 
		execp.printStackTrace(writer);
		execp = execp.getCause();
	}
	out.println("</pre>"); } %>
<% } else if ( myObject != null && myObject.first() ) { %>
<form enctype="multipart/form-data" method="post">
<input type="hidden" name="id" value="<%= myObject.getID() %>">
Update file: <input type="file" name="xmlFile"><button type="submit" name="runJob">Upload</button>
</form>
<% } %>
</div>
</body>
</html>