<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="cyanos" tagdir="/WEB-INF/tags" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.CyanosObject,
	edu.uic.orjala.cyanos.web.servlet.MainServlet,
	edu.uic.orjala.cyanos.web.BaseForm,
	edu.uic.orjala.cyanos.web.News,
	edu.uic.orjala.cyanos.User,
	java.text.SimpleDateFormat,
	java.util.Set" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<cyanos:header title="Cyanos Database v1.7">
<script type="text/javascript">
	function showLogin() {
		var cover = document.getElementById("loginCover");  
		cover.style.height = window.screen.height; 
		cover.style.visibility = "visible";
		var loginBox = document.getElementById("loginBox");
		loginBox.style.display = "block";
	}
</script>
</cyanos:header>
</head>
<body>
<cyanos:menu helpModule="<%= MainServlet.HELP_MODULE %>"/>

<% if ( request.getRemoteUser() == null ) { %>
<div id="loginCover">
</div>
<div id="loginBox" style="display:none">
<div id="loginBoxForm">
<font size='+2'>CYANOS Login</font><hr>
<form action='main-login.jsp' method='post' target='_top'>
<table>
<tr><td>Username:</td><td><input type='text' name='j_username'></td></tr>
<tr><td>Password:</td><td><input type='password' name='j_password' size='8'></td></tr>
</table>
<br>
  <button type='submit'>Login</button><button type='reset'>Clear form</button>
</form>
<p><a href='<%= response.encodeURL("reset") %>'>Reset password</a></p></div></div>
<% } %>

<div class='content'>
<div class="left25">
<!-- <div style="height:100px;"> -->
<div class="sideModule">
<% if ( request.getRemoteUser() != null )  { 
	User aUser = MainServlet.getUser(request);  %>
<p><b>Welcome, <%= aUser.getUserName() %></b></p>
<p><a href="updateUser.jsp">Update user account</a><br>
<a href="logout.jsp">Logout</a></p>
<% Set<String> sites = aUser.oauthRealms(); 
	if ( sites.size() > 0 ) { %>
<p><b>External Sites</b> (OpenID connections)</br>
<% for ( String site : sites ) { %>
<a href="<%= site %>"><%= site %></a><br/>
<% } %>
</p>
<% } } else { %>
<p><b>Welcome, Guest User</b></p>
<p><a href="login.jsp">Login</a></p>
<% } %>
<!-- </div> -->
</div>
</div>

<div class="right75">
<div class="desktop">
<h1 style="text-align:center">Cyanos Database v1.7</h1>
<hr width="85%">
<% 	News news = (News) request.getAttribute(MainServlet.ATTR_NEWS); 
	SimpleDateFormat dateFormat = (SimpleDateFormat) session.getAttribute("dateFormatter");
	if ( news != null && news.first() ) { %>
<h2 align="center">News</h2>
<dl>
<% news.beforeFirst(); while ( news.next() ) { %>
<dt><b><%= news.getSubject() %></b> - <i><%= dateFormat.format(news.getDateAdded()) %></i></dt>
<dd><%=  news.getContent().replaceAll("\n", "<BR>") %></dd>
<% } %>
</dl>
<hr width='85%'/>
<% } %>
<%--
<h2 align="center">Strain Search</h2>
<center>
<form name="strainquery" action="strain">
<table border=0>
<tr><td>Query:</td><td>
<% String queryValue = request.getParameter("query"); if ( queryValue == null ) { queryValue = ""; }%>
<input type="text" name="query" VALUE="<%= queryValue %>" ></td>
<td>
<button type='submit'>Search</button>
</td></tr>
</table>
</form>
</center>
<jsp:include page="/strain/strain-list.jsp" />
</div>
 --%>
<p style="text-align: left">The CYANOS database system was developed to facilitate data management and mining for natural product drug discovery efforts. 
The following schematic displays the various object classes that CYANOS can manage and their relationship.  
Click on an object class to manage the data</p> 
<p><img src="help/intro01.jpg" width="700" style="display:block; margin-left:auto; margin-right:auto; border: 1px solid gray; padding:5px;" alt="Cyanos Workflow" usemap="#workflow_Map">
<map name="workflow_Map">
<area shape="rect" title="Manage Preservation Data" coords="50,232,183,311" href="preserve.jsp">
<area shape="rect" title="Manage Sample Library Data" coords="229,224,373,312" href="sample">
<area shape="rect" title="Manage Assay Data" coords="422,232,565,298" href="assay">
<area shape="rect" title="Manage Compound Data" coords="466,49,700,224" href="compound">
<area shape="rect" title="Manage Separation Data" coords="265,15,398,83" href="separation">
<area shape="rect" title="Manage Material Data" coords="265,113,436,202" href="material">
<area shape="rect" title="Manage Collection Data" coords="109,0,199,83" href="collection">
<area shape="rect" title="Manage Harvest Data" coords="183,113,245,202" href="harvest">
<area shape="rect" title="Manage Inoculation Data" coords="79,113,162,190" href="inoc">
<area shape="rect" title="Manage Strain Data" coords="0,113,70,191" href="strain">
<area shape="rect" title="Manage Isolation Data" coords="0,34,71,84" href="isolation.jsp">
</map>
</p>
<p><b>Original publication</b><br>
George E. Chlipala, Aleksej Krunic, Shunyan Mo, Megan Sturdy, and Jimmy Orjala. (2011) "CYANOS: A Data Management System for Natural Product Drug Discovery Efforts Using Cultured Microorganisms". 
<i>J. Chem. Inf. Model.</i> 51(1): 171-180. 
<a href="http://pubs.acs.org/doi/abs/10.1021/ci100280a">ACS Link</a> | 
<a href="http://www.ncbi.nlm.nih.gov/pubmed/21162567">Pubmed</a></p>
</div>
</div>
</div>
</body>
</html>