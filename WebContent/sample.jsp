<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.Sample,edu.uic.orjala.cyanos.web.servlet.SampleServlet,java.text.SimpleDateFormat" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html style="min-height:100%">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<script language="JAVASCRIPT" src="cyanos.js"></script>
<link rel="stylesheet" type="text/css" href="cyanos.css"/>
<% 	Sample sampleObj = (Sample) request.getAttribute("sample");
if ( sampleObj != null && sampleObj.first() ) { %>
<title>Sample <%= sampleObj.getID() %></title>
<% } else { %>
<title>Sample Search</title>
<% } %>
</head>

<jsp:include page="includes/menu.jsp" />

<div class='content' style="padding-bottom: 60px;">
<% if ( sampleObj != null && sampleObj.first() ) { %>
<p align="CENTER"><font size="+3" >Sample #<%= sampleObj.getID() %></font>
<div id="<%= SampleServlet.DIV_INFO_FORM_ID %>">
<jsp:include page="/sample/sample-form.jsp" />
</div>

<jsp:include page="/includes/loadableDiv.jsp">
<jsp:param value="<%= SampleServlet.TXN_DIV_ID %>" name="loadingDivID"/>
<jsp:param value="Balance Sheet" name="loadingDivTitle"/>
</jsp:include>

<jsp:include page="/includes/loadableDiv.jsp">
<jsp:param value="<%= SampleServlet.DIV_ASSAY_ID %>" name="loadingDivID"/>
<jsp:param value="Assays" name="loadingDivTitle"/>
</jsp:include>

<% } else { %>
<p align="CENTER"><font size="+3" >Sample Search</font>
<hr width='85%'/></p>
<center>
<form name="samplequery">
<table border=0>
<tr><td>Culture ID:</td><td>
<% String queryValue = request.getParameter("query"); if ( queryValue == null ) { queryValue = ""; }%>
<input id="query" type="text" name="query" VALUE="<%= queryValue %>" autocomplete='off' onKeyUp="livesearch(this, 'query', 'div_query')" style='padding-bottom: 0px'/>
<div id="div_query" class='livesearch' style="width:75px;"></div></td>
<td>
<button type='SUBMIT'>Search</button>
</td></tr>
</table>
</form>
</center>
<jsp:include page="/sample/sample-list.jsp" />
<% } %>
</div>
</body>
</html>