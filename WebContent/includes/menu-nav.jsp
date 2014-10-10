<%@ page import="java.net.URLEncoder" %>
<%
String contextPath = request.getContextPath();
%>
<div id="menu">
<nav class="nav-collapse">
<ul>
<!-- APPLICATION MENU -->
<li><a href="#" onClick='toggleNavMenu("appMenu")'>CYANOS</a><br>
<div id="appMenu" class='submenu'><p>
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
%></p></div></li>
<!-- STRAIN MENU -->
<li><a href="#" onClick='toggleNavMenu("strainMenu")'>Strains</a><br>
<div class='submenu' id="strainMenu"><p>
<a href='<%= contextPath %>/strain'>Search</a><br>
<a href='<%= contextPath %>/taxabrowser'>Taxa Browser</a><br>
<% if (request.isUserInRole("culture")) { 
%><a href='<%= contextPath %>/strain?action=add'>Add New</a><br>
<a href='<%= contextPath %>/strain?photoList'>Photo Browser</a><br>
<% } %>
</p></div></li>
<!--  CULTURE MENU -->
<% if (request.isUserInRole("culture")) { %>
<li><a href="#" onClick='toggleNavMenu("cultureMenu")'>Culture</a><br>
<div class='submenu' id='cultureMenu'><p>
<a href='<%= contextPath %>/inoc?form=add'>Add Inoculations(s)</a><br>
<hr width='80%' noshade size='1'>
<a href='<%= contextPath %>/cryo'>Browse Cryopreservations(s)</a><br>
<a href='<%= contextPath %>/cryo/add'>Add Cryopreservations(s)</a><br>
</p></div>
</li>
<!-- COLLECTION/ISOLATION MENU -->
<li><a href="#" onClick='toggleNavMenu("collectionMenu")'>Collections</a><br>
<div id='collectionMenu' class='submenu'><p>
<a href='<%= contextPath %>/collection'>Search Collections</a><br>
<a href='<%= contextPath %>/collection?form=add'>Add New Collection</a><br>
<hr width='80%' noshade size='1'>
<a href='<%= contextPath %>/upload?module=collection'>Upload Collection Data</a><br>
<a href='<%= contextPath %>/upload?module=isolation'>Upload Isolation Data</a><br>
</p></div>
</li>
<% } 
	if ( request.isUserInRole("sample") ) { 
%><!-- SAMPLE MENU -->
<li><a href="#" onClick='toggleNavMenu("sampleMenu")'>Samples</a><br>
<div class='submenu' id='sampleMenu'><p>
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
</li>
<!-- SEPARATION MENU -->
<li><a href="#" onClick='toggleNavMenu("sepMenu")'>Separations</a><br>
<div class='submenu' id='sepMenu'><p>
<a href='<%= contextPath %>/separation'>Search</a><br>
<a href='<%= contextPath %>/separation/protocol'>Manage Protocols</a><br>
<a href='<%= contextPath %>/upload?module=fraction'>Upload New Data</a><br>
</p></div>
</li>
<% } 

	if ( request.isUserInRole("assay") ) { %>
<!-- ASSAY MENU -->
<li><a href="#" onClick='toggleNavMenu("assayMenu")'>Assays</a><br>
<div id='assayMenu' class='submenu'><p>
<a href='<%= contextPath %>/assay?action=add'>Add New</a><br>
<a href='<%= contextPath %>/assay'>Browse Data</a><br>
<hr width='80%' noshade size='1'>
<a href='<%= contextPath %>/assay/protocol'>Manage Protocols</a><br>
<a href='<%= contextPath %>/upload?module=assay'>Upload Data</a><br>
</p></div>
</span>
<% } 

%><!-- HELP MENU -->
<li class="helpMenu"><a href="#" onClick='toggleNavMenu("helpMenu")'>Help</a><br>
<div id='helpMenu' class='submenu'><p>
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
</li>
</ul>
</nav>
</div>
<script>
      var navigation = responsiveNav(".nav-collapse", {
        animate: true,                    // Boolean: Use CSS3 transitions, true or false
        transition: 284,                  // Integer: Speed of the transition, in milliseconds
        label: "Menu",                    // String: Label for the navigation toggle
        insert: "after",                  // String: Insert the toggle before or after the navigation
        customToggle: "",                 // Selector: Specify the ID of a custom toggle
        closeOnNavClick: false,           // Boolean: Close the navigation when one of the links are clicked
        openPos: "relative",              // String: Position of the opened nav, relative or static
        navClass: "nav-collapse",         // String: Default CSS class. If changed, you need to edit the CSS too!
        navActiveClass: "js-nav-active",  // String: Class that is added to <html> element when nav is active
        jsClass: "js",                    // String: 'JS enabled' class which is added to <html> element
        init: function(){},               // Function: Init callback
        open: function(){},               // Function: Open callback
        close: function(){}               // Function: Close callback
      });
    </script>