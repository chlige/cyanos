<%@ page import="edu.uic.orjala.cyanos.web.servlet.AdminServlet,
	edu.uic.orjala.cyanos.User,
	edu.uic.orjala.cyanos.MutableUser,
	edu.uic.orjala.cyanos.sql.SQLMutableUser,
	edu.uic.orjala.cyanos.Role,
	java.text.SimpleDateFormat" %>

<jsp:include page="/includes/header-template.jsp">
<jsp:param value="Application Administration" name="page_title"/>
</jsp:include>
</head>
<body>
<jsp:include page="/includes/menu.jsp"/>
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