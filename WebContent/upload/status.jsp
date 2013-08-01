<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
 <%@ page import="edu.uic.orjala.cyanos.web.UploadModule,
	edu.uic.orjala.cyanos.web.servlet.UploadServlet" %>   
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<%	String contextPath = request.getContextPath(); %>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<script language="JAVASCRIPT" src="<%= contextPath %>/cyanos.js"></script>
<link rel="stylesheet" type="text/css" href="<%=contextPath %>/cyanos.css"/>
<title>Spinner Test</title>
</head>
<body>

<div align="center">
<div class="progress" style="width: 200px">
<div id="progressText"></div>
<div id="progressBar"></div>
</div>

<form>
<button id="resultButton" name="showResults" disabled>Show Results</button>
</form>
</div>

<script>
	var updatePath = "<%= contextPath %>/upload/status";
	uploadStatus(updatePath, document.getElementById("resultButton"));
</script>

</body>
</html>