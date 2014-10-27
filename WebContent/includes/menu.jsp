<%@ page import="java.net.URLEncoder,
	edu.uic.orjala.cyanos.web.servlet.UploadServlet,
	edu.uic.orjala.cyanos.web.UploadJob" %>
<%
String contextPath = request.getContextPath();
%>
<nav>
<label for="show-menu" class="show-menu">Cyanos</label>
<input type="checkbox" id="show-menu" role="button">
<ul class="menu">
<li><a onClick='toggleMenu("cyanosMenu")'>Cyanos</a>
<ul class='submenu'>
<li><a href='<%= contextPath %>/main'>Main Page</a></li>
<% if ( request.isUserInRole("project") ) { %>
<li><a href='<%= contextPath %>/project'>Manage Projects</a></li>
<% } %>

<% 	if ( request.getRemoteUser() != null ) { 
%><li><a href='<%= contextPath %>/upload'>Upload Data</a></li>
<li><a href='<%= contextPath %>/logout.jsp'>Logout</a></li>
<%		if ( request.isUserInRole("admin") ) { 
%>
<li class="break"><a href='<%= contextPath %>/admin/user.jsp'>Manage Users</a></li>
<li><a href='<%= contextPath %>/admin/news.jsp'>Manage News</a></li>
<li><a href='<%= contextPath %>/admin/config'>Manage Config</a></li>
<% 		} 
	} else { 
	String loginURL = (String) request.getAttribute("javax.servlet.forward.request_uri");
	if ( loginURL == null ) { loginURL = request.getRequestURI(); }
	if ( request.getQueryString() != null ) {
		loginURL = loginURL.concat("?").concat(request.getQueryString());
	}
%><li><a href='<%= contextPath %>/login.jsp?url=<%= URLEncoder.encode(loginURL) %>'>Login</a></li>
<%	} 
%>
</ul></li>

<% if ( request.isUserInRole("culture") ) { %>
<!-- COLLECTION/ISOLATION MENU -->
<li class='menu' id='collectionMenu'><a onClick='toggleMenu("collectionMenu")'>Collections</a>
<ul class='submenu'>
<li><a href='<%= contextPath %>/collection'>Search Collections</a></li>
<li><a href='<%= contextPath %>/collection?form=add'>Add New Collection</a></li>
<li class="break"><a href='<%= contextPath %>/upload?module=collection'>Upload Collection Data</a></li>
<li><a href='<%= contextPath %>/upload?module=isolation'>Upload Isolation Data</a></li>
</ul></li>

<li><a onClick='toggleMenu("strainMenu")'>Culture</a>
<ul class='submenu'>
<li><a href='<%= contextPath %>/strain'>Search Strains</a></li>
<li><a href='<%= contextPath %>/taxabrowser'>Taxa Browser</a></li>
<li><a href='<%= contextPath %>/strain?action=add'>Add New Strain</a></li>
<li><a href='<%= contextPath %>/strain?photoList'>Photo Browser</a></li>
<li class="break"><a href='<%= contextPath %>/inoc?form=add'>Add Inoculation(s)</a></li>
<li><a href='<%= contextPath %>/harvest'>Search Harvests</a></li>
<li><a href='<%= contextPath %>/cryo'>Browse Preservations</a></li>
<li><a href='<%= contextPath %>/cryo/add'>Add Preservation(s)</a></li>
</ul></li>
<% } 
	if ( request.isUserInRole("sample") ) { 
%><!-- SAMPLE MENU -->
<li class='menu' id='sampleMenu'><a onClick='toggleMenu("sampleMenu")'>Materials</a>
<ul class='submenu'>
<li><a href='<%= contextPath %>/material'>Search Materials</a></li>
<!--  <li><a href='<%= contextPath %>/material?protocolMgr'>Manage Extract Protocols</a></li>  -->
<li class="break"><a href='<%= contextPath %>/separation'>Search Separations</a></li>
<li><a href='<%= contextPath %>/separation/protocol'>Manage Separation Templates</a></li>
<li><a href='<%= contextPath %>/upload?module=fraction'>Upload New Separation</a></li>
<li class="break"><a href='<%= contextPath %>/sample?newCollection'>Add Sample Collection</a></li>
<li><a href='<%= contextPath %>/sample'>Browse Samples</a></li>
<li><a href='<%= contextPath %>/upload/sample'>Upload Sample Data</a></li>
<li class="break"><a href='<%= contextPath %>/compound'>Search Compounds</a></li>
<li><a href='<%= contextPath %>/compound?form=add'>Add Compound</a></li>
<li><a href='<%= contextPath %>/dereplication'>Dereplication</a></li>
</ul></li>
<% } 
	if ( request.isUserInRole("assay") ) { %>
<!-- ASSAY MENU -->
<li class='menu' id='assayMenu'><a onClick='toggleMenu("assayMenu")'>Assays</a>
<ul class='submenu'>
<li><a href='<%= contextPath %>/assay?action=add'>Add New</a></li>
<li><a href='<%= contextPath %>/assay'>Browse Data</a></li>
<li class="break"><a href='<%= contextPath %>/assay/protocol'>Manage Templates</a></li>
<li><a href='<%= contextPath %>/upload?module=assay'>Upload Data</a></li>
</ul></li>
<% } %>
<!-- HELP MENU -->
<li class='helpmenu' id='helpMenu'><a onClick='toggleMenu("helpMenu")'>Help</a>
<ul class='submenu'><li><%
	String module = request.getParameter("module");
	if ( module != null ) {
%><a href='<%= contextPath %>/help?toc&module=<%= module %>'>Quick Help</a></li>
<li class="break"><% } %><a href='<%= contextPath %>/help?toc'>Contents</a></li>
<li><a href='<%= contextPath %>/help?find'>Find a Topic</a></li>
<li><a href='<%= contextPath %>/help?search'>Search</a></li>
</ul></li>
</ul>
<div style="float:right;margin-right: 30px;padding: 4px;"><%= (UploadServlet.hasSpreadsheet(request) ? "SHEET" : "") %><%
	UploadJob job = UploadServlet.getUploadJob(session);
	if ( job != null ) { %>JOB<% } %></div>
</nav>
<div style='height:20px'></div>