<%@ taglib tagdir="/WEB-INF/tags" prefix="cyanos" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="edu.uic.orjala.cyanos.web.servlet.AdminServlet,
	edu.uic.orjala.cyanos.web.News,
	edu.uic.orjala.cyanos.Role,
	edu.uic.orjala.cyanos.User,
	java.util.Date" %>
<!DOCTYPE html>
<html>
<head>
<cyanos:header title="News Administration"/>
<body>
<cyanos:menu/>
<h1>News Administration</h1>
<% if ( AdminServlet.getUser(request).isAllowed(User.ADMIN_ROLE, User.GLOBAL_PROJECT, Role.CREATE) && request.getParameter("addNews") != null ) {
		News.create(AdminServlet.getSQLData(request), request.getParameter("expires"), request.getParameter("subject"), request.getParameter("content"));
	}
	News newsList = AdminServlet.getAllNews(request);
	if ( newsList != null ) {  
		newsList.beforeFirst();
		String selected = request.getParameter("item");
		boolean update = request.getParameter("updateNews") != null;
%><form method="post">
<table style="margin-left:auto; margin-right:auto;">
<tr class="header"><td></td><th width="100">Date Added</th>
<th width="100">Expires</th><th width="200">Subject</th><th  width="400">Content</th></tr>
<%	while ( newsList.next() ) {
		Date added = newsList.getDateAdded();
		Date expires = newsList.getExpiration();
		if ( update && newsList.getID().equals(selected) ) {
			
		}
%><tr><td><input type="radio" name="item" value="<%= newsList.getID() %>"></td>
<td><fmt:formatDate type="both" dateStyle="medium" timeStyle="medium" value="${added}"/></td>
<td><fmt:formatDate type="both" dateStyle="medium" timeStyle="medium" value="${expires}"/></td>
<td><%= newsList.getSubject() %></td>
<td><%= newsList.getContent() %></td></tr>
<%	} %>
</table>
<p align="center"><button type="submit" name="updateNews">Update</button></p>
</form>
<% } %>
<% if ( AdminServlet.getUser(request).isAllowed(User.ADMIN_ROLE, User.GLOBAL_PROJECT, Role.CREATE) ) { 
%>
<form method="post">
<table class="uploadForm">
<tr><td>Subject:</td><td><input type="text" name="subject" size="30"></td></tr>
<tr><td>Expiration:</td><td><cyanos:calendar-field fieldName="expires" showTime="true"/></td></tr>
<tr><td colspan="2">Content:<br>
<textarea name="content" rows="10" cols="50"></textarea></td></tr>
</table>
</form>
<% } %>
</body>
</html>