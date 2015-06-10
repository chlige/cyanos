<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="cyanos" tagdir="/WEB-INF/tags" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="edu.uic.orjala.cyanos.web.servlet.MainServlet,
	edu.uic.orjala.cyanos.web.listener.AppConfigListener,
	edu.uic.orjala.cyanos.User,
	java.math.BigDecimal,
	java.text.DateFormat" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<cyanos:header title="Cyanos Link Objects"/>
<style type="text/css">
.content { margin: 10px; }
input { background-color: white; border-radius: 0px; }
li { border: 1px solid gray; padding: 2px; cursor: copy; margin: 2px 0px;  }
li:hover { background-color: #ddd; }
ul { list-style: none; padding: 0px; }
</style>
<script type="text/javascript" src="<%= request.getContextPath() %>/tinymce/tinymce.js"></script>
<script type="text/javascript">

</script>
</head>
<body>
<div class="content">


</div>
</body>
</html>