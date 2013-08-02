<%@ page import="java.net.URLEncoder" %>
<%
String contextPath = request.getContextPath();
%><div class='menubar'>
<span class='menu' id='cyanosMenu'><a name='cyanos' onClick='toggleMenu("cyanosMenu")'>Cyanos</a>
<div class='submenu'><p>
<a href='<%= contextPath %>/main'>Main Page</a><br>
<% if ( request.isUserInRole("project") ) { %>
	<a href='<%= contextPath %>/project'>Manage Projects</a><br>
<% } %>

<% 	if ( request.getRemoteUser() != null ) { 
%><a href='<%= contextPath %>/upload'>Upload Data</a><br>
<a href='<%= contextPath %>/logout.jsp'>Logout</a><br>
<%		if ( request.isUserInRole("admin") ) { 
%><hr width='80%' noshade size='1'>
<a href='<%= contextPath %>/admin/user'>Manage Users</a><br>
<a href='<%= contextPath %>/admin/news'>Manage News</a><br>
<a href='<%= contextPath %>/admin/config'>Manage Config</a><br>
<% 		} 
	} else { 
	String loginURL = (String) request.getAttribute("javax.servlet.forward.request_uri");
	if ( loginURL == null ) { loginURL = request.getRequestURI(); }
	if ( request.getQueryString() != null ) {
		loginURL = loginURL.concat("?").concat(request.getQueryString());
	}
%><a href='<%= contextPath %>/login.jsp?url=<%= URLEncoder.encode(loginURL) %>'>Login</a><br>
<%	} 
%></p></div>
</span>

<span class='menu' id='strainMenu'><a name='strains' onClick='toggleMenu("strainMenu")'>Strains</a><br>
<div class='submenu'><p>
<a href='<%= contextPath %>/strain'>Search</a><br>
<a href='<%= contextPath %>/taxabrowser'>Taxa Browser</a><br>
<% if (request.isUserInRole("culture")) { 
%><a href='<%= contextPath %>/strain?action=add'>Add New</a><br>
<a href='<%= contextPath %>/strain?photoList'>Photo Browser</a><br>
</p></div>
</span>
<!--  CULTURE MENU -->
<span class='menu' id='cultureMenu'><a name='cultures' onClick='toggleMenu("cultureMenu")'>Culture</a><br>
<div class='submenu'><p>
<a href='<%= contextPath %>/inoc?form=add'>Add Inoculations(s)</a><br>
<hr width='80%' noshade size='1'>
<a href='<%= contextPath %>/cryo'>Browse Cryopreservations(s)</a><br>
<a href='<%= contextPath %>/cryo/add'>Add Cryopreservations(s)</a><br>
</p></div>
</span>
<!-- COLLECTION/ISOLATION MENU -->
<span class='menu' id='collectionMenu'><a name='collections' onClick='toggleMenu("collectionMenu")'>Collections</a><br>
<div class='submenu'><p>
<a href='<%= contextPath %>/collection'>Search Collections</a><br>
<a href='<%= contextPath %>/collection?form=add'>Add New Collection</a><br>
<hr width='80%' noshade size='1'>
<a href='<%= contextPath %>/upload?module=collection'>Upload Collection Data</a><br>
<a href='<%= contextPath %>/upload?module=isolation'>Upload Isolation Data</a><br>
</p></div>
</span>
<% } else { 
%></p></div>
</span>
<% } 

	if ( request.isUserInRole("sample") ) { 
%><!-- SAMPLE MENU -->
<span class='menu' id='sampleMenu'><a name='samples' onClick='toggleMenu("sampleMenu")'>Samples</a><br>
<div class='submenu'><p>
<a href='<%= contextPath %>/material'>Search Materials</a><br>
<!--  <a href='<%= contextPath %>/material?protocolMgr'>Manage Extract Protocols</a><br>  -->
<hr width='80%' noshade size='1'>
<a href='<%= contextPath %>/sample?newCollection'>Add Sample Collection</a><br>
<a href='<%= contextPath %>/sample'>Browse Samples</a><br>
<a href='<%= contextPath %>/upload/sample'>Upload Sample Data</a><br>
<hr width='80%' noshade size='1'>
<a href='<%= contextPath %>/compound'>Search Compounds</a><br>
<a href='<%= contextPath %>/compound?form=add'>Add Compound</a><br>
<a href='<%= contextPath %>/dereplication'>Dereplication</a><br>
</p></div>
</span>

<!-- SEPARATION MENU -->
<span class='menu' id='sepMenu'><a name='seps' onClick='toggleMenu("sepMenu")'>Separations</a><br>
<div class='submenu'><p>
<a href='<%= contextPath %>/separation'>Search</a><br>
<a href='<%= contextPath %>/separation/protocol'>Manage Protocols</a><br>
<a href='<%= contextPath %>/upload?module=fraction'>Upload New Data</a><br>
</p></div>
</span>
<% } 

	if ( request.isUserInRole("assay") ) { %>
<!-- ASSAY MENU -->
<span class='menu' id='assayMenu'><a name='assays' onClick='toggleMenu("assayMenu")'>Assays</a><br>
<div class='submenu'><p>
<a href='<%= contextPath %>/assay?action=add'>Add New</a><br>
<a href='<%= contextPath %>/assay'>Browse Data</a><br>
<hr width='80%' noshade size='1'>
<a href='<%= contextPath %>/assay/protocol'>Manage Protocols</a><br>
<a href='<%= contextPath %>/upload?module=assay'>Upload Data</a><br>
</p></div>
</span>
<% } 

%><!-- HELP MENU -->
<span class='helpmenu' id='helpMenu'><a name='help' onClick='toggleMenu("helpMenu")'>Help</a><br>
<div class='submenu'><p>
<%
	String module = request.getParameter("module");
	if ( module != null ) {
%><a href='<%= contextPath %>/help?toc&module=<%= module %>'>Quick Help</a><br>
<hr width='80%' noshade size='1'>
<% } 
%><a href='<%= contextPath %>/help?toc'>Contents</a><br>
<a href='<%= contextPath %>/help?find'>Find a Topic</a><br>
<a href='<%= contextPath %>/help?search'>Search</a><br>
</p></div>
</span>
</div>
<div style='height:20px'></div>