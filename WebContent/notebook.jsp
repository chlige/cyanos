<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="cyanos" tagdir="/WEB-INF/tags" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.web.servlet.MainServlet,
	edu.uic.orjala.cyanos.web.listener.AppConfigListener,
	edu.uic.orjala.cyanos.sql.SQLData,
	edu.uic.orjala.cyanos.CyanosObject,
	edu.uic.orjala.cyanos.BasicObject,
	edu.uic.orjala.cyanos.User,
	edu.uic.orjala.cyanos.Role,
	edu.uic.orjala.cyanos.Notebook,
	edu.uic.orjala.cyanos.sql.SQLNotebook,
	edu.uic.orjala.cyanos.sql.SQLProject,
	edu.uic.orjala.cyanos.NotebookPage,
	java.text.DateFormat, java.util.List" %>
<!DOCTYPE html>
<html>
<head>
<cyanos:header title="Cyanos Notebooks"/>
<style type="text/css">
h2 { text-align:center; }
table.details { border-collapse: collapse; margin-bottom: 10px; }
table.details td, table.details th { text-align:left; }
table.results tr { border-top: 1px solid gray; border-bottom: 1px solid gray; }
table.results td, table.results th { padding-left: 2px; padding-right: 2px; }
table { margin-left: auto; margin-right:auto; }
.page { margin-left: auto; margin-right:auto; width:80%; border: 1px solid gray; padding: 5px 10px; background-color: #fffffa; }
</style>
</head>
<body>
<cyanos:menu helpModule="<%= MainServlet.HELP_MODULE %>"/>
<div id="content">
<% SQLData data = MainServlet.getSQLData(request);
if ( request.getParameter("id") != null ) { 
		String notebookid = request.getParameter("id");
		Notebook notebook = SQLNotebook.loadNotebook(data, notebookid);
		
//		String sql = "SELECT n.title,n.description,COUNT(p.page),MIN(p.date_created),MAX(p.date_updated) FROM notebook n LEFT OUTER JOIN notebook_page p ON(n.notebook_id = p.notebook_id) WHERE n.username=? AND n.notebook_id=?";
		notebook.first();
%><h1>Notebook <%= notebook.getID() %></h1><%
		if ( request.getParameter("page") != null ) {
			NotebookPage notebookpage = notebook.getPage(Integer.parseInt(request.getParameter("page")));
			if ( notebookpage.first() ) { 
%><h2>Page <%= notebookpage.getPage() %>: <%= notebookpage.getTitle() %></h2>
<p align="center">
Created: <%= MainServlet.DATE_FORMAT.format(notebookpage.getCreationDate()) %><br>
Last Updated: <%= MainServlet.DATE_FORMAT.format(notebookpage.getLastModifiedDate()) %></p>
<div class="page"><%= notebookpage.getContent() %></div>
<%			}
		} else {
%><p align="center">
<% int pageCount = notebook.getPageCount(); %>
<%= pageCount %> Pages<br>
<% if ( pageCount > 0 ) { %>
Created: <%= MainServlet.DATE_FORMAT.format(notebook.getFirstUpdate()) %><br>
Last Updated: <%= MainServlet.DATE_FORMAT.format(notebook.getRecentUpdate()) %>
<% } %></p>
<p align="center"><%= notebook.getDescription() %></p>
<%	NotebookPage pages = notebook.getPages();
	if ( pages != null ) { 
%><ul>
<%				while ( pages.next() ) {
%><li><a href="notebook.jsp?id=<%= notebookid %>&page=<%= pages.getPage() %>">Page <%= pages.getPage() %>: <%= pages.getTitle() %></a> Last Updated: <%= MainServlet.DATE_FORMAT.format(pages.getLastModifiedDate()) %></li>
<% } %></ul><%	
			} else { %>
<p align="center">No notebook pages</p>
<% } %><p align="center"><a href="notebook/addpage.jsp?id=<%= notebookid %>">Add a new notebook page</a></p>
<% 		} 
	} else { %>
<h1>Notebooks</h1>
<hr width="85%">
<h2>Your Notebooks</h2>
<% 	
	Notebook notebooks = SQLNotebook.myNotebooks(MainServlet.getSQLData(request));
	if ( notebooks != null && notebooks.first() ) { 
%><ul>
<%		while ( notebooks.next() ) {
			int pageCount = notebooks.getPageCount();
%><li><a href="notebook.jsp?id=<%= notebooks.getID() %>"><%= notebooks.getTitle() %> (ID: <%= notebooks.getID() %>)</a> <%= pageCount %> page(s), 
<% if ( pageCount > 0 ) {%><%= MainServlet.DATE_FORMAT.format(notebooks.getFirstUpdate()) %> - <%= MainServlet.DATE_FORMAT.format(notebooks.getRecentUpdate()) %><% } %>
</li>
<% } %></ul><%	
	} else { %>
<p align="center">No notebooks</p>
<% } %><p align="center"><a href="notebook/add.jsp">Add a new notebook</a></p>
<% User user = MainServlet.getUser(request); 
	List<String> projects = SQLProject.listProjects(data);
	for ( String project : projects ) {
		if ( user.isAllowed(User.PROJECT_MANAGER_ROLE, project, Role.READ) )  {
%><h3><%= project %></h3><table style="border: 0px">
<tr><th>Notebook</th><th>Owner</th><th>Pages</th><th>First Update</th><th>Last Update</th></tr>
<%			notebooks = SQLNotebook.projectNotebooks(data, project);
while ( notebooks.next() ) {
	int pageCount = notebooks.getPageCount();
%><tr><td><a href="notebook.jsp?id=<%= notebooks.getID() %>"><%= notebooks.getTitle() %> (ID: <%= notebooks.getID() %>)</a></td><td><%= notebooks.getUser().getUserName() %></td><td><%= pageCount %></td>
<% if ( pageCount > 0 ) {%><td><%= MainServlet.DATE_FORMAT.format(notebooks.getFirstUpdate()) %></td><td><%= MainServlet.DATE_FORMAT.format(notebooks.getRecentUpdate()) %></td><% } else { %><td></td><td></td><% } %>
</tr>
<% } %></table><%
		}
	}
%>
<% } %>
</div>
</body>
</html>