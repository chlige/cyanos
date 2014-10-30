<%@ taglib tagdir="/WEB-INF/tags" prefix="cyanos" %>
<%@ page import="edu.uic.orjala.cyanos.web.servlet.AdminServlet,
	edu.uic.orjala.cyanos.User,
	edu.uic.orjala.cyanos.MutableUser,
	edu.uic.orjala.cyanos.sql.SQLMutableUser,
	edu.uic.orjala.cyanos.Role,
	java.text.SimpleDateFormat" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<cyanos:header title="Application Administration"/>
</head>
<body>
<cyanos:menu/>
<div class='content'>
<h1>Application Administration</h1>
<hr width="85%"/>
<ul style="list-type:none" >
<li><a href="admin/user.jsp">User Administration</a></li>
<li><a href="admin/config">Configuration Management</a></li>
<li><a href="admin/news.jsp">News/Notice Management</a></li>
</ul>
</div>
</body>
</html>