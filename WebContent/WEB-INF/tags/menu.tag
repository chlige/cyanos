<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ tag import="java.net.URLEncoder,
	edu.uic.orjala.cyanos.web.servlet.UploadServlet" %>
<%@ attribute name="helpModule" required="false" %>
<% String contextPath = request.getContextPath(); %>
<div style="width:100%; margin-bottom:20px">
<nav>
<div class="mobile">
<label for="show-menu" id="show-menu-label" class="show-menu">CYANOS</label>
<input type="checkbox" id="show-menu" role="button">
<ul id="mobile-menu" class="mobile-menu">
<li><a href='<%= contextPath %>/main'>Main Page</a></li>
<li><a href="<%= contextPath %>/collection/add-collection.jsp">Add Collection Data</a></li>
<li><a href="<%= contextPath %>/strain">Strain List</a></li>
<c:if test="${helpModule} != null">
<li class='helpmenu'><a href='<%= contextPath %>/help?toc&module=${helpModule}'>Quick Help</a></li>
</c:if>
<li class='helpmenu'><a href='<%= contextPath %>/help?search'>Search Help</a></li>
</ul>
</div>
<div class="desktop">
<input type="checkbox" id="show-fullmenu" role="button">
<label for="show-fullmenu" class="show-menu">&equiv;</label>
<ul id="menu" class="menu">
<li><a>Cyanos</a>
<ul class='submenu'>
<li><a href='<%= contextPath %>/main'>Main Page</a></li>
<% if ( request.isUserInRole("project") ) { %>
<li><a href='<%= contextPath %>/project'>Manage Projects</a></li>
<% } %>

<% 	if ( request.getRemoteUser() != null ) { 
%><li><a href='<%= contextPath %>/report'>Reports</a></li>
<li><a href='<%= contextPath %>/notebook.jsp'>Notebooks</a></li>
<li><a href='<%= contextPath %>/upload.jsp'>Upload Data</a></li>
<li><a href="<%= contextPath %>/jobs.jsp">Job Status</a></li>
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
%><li><a href='<%= contextPath %>/login.jsp?url=<%= URLEncoder.encode(loginURL, "UTF-8") %>'>Login</a></li>
<%	} 
%>
</ul></li>

<% if ( request.isUserInRole("culture") ) { %>
<!-- COLLECTION/ISOLATION MENU -->
<li><a>Collections</a>
<ul class='submenu'>
<li><a href='<%= contextPath %>/collection'>Search Collections</a></li>
<li><a href='<%= contextPath %>/collection?form=add'>Add New Collection</a></li>
<li class="break"><a href='<%= contextPath %>/collection/upload.jsp'>Upload Collection Data</a></li>
<li><a href='<%= contextPath %>/isolation/upload.jsp'>Upload Isolation Data</a></li>
</ul></li>

<li><a>Cultures</a>
<ul class='submenu'>
<li><a href='<%= contextPath %>/strain'>Search Strains</a></li>
<li><a href='<%= contextPath %>/taxabrowser'>Taxa Browser</a></li>
<li><a href='<%= contextPath %>/strain?action=add'>Add New Strain</a></li>
<li><a href='<%= contextPath %>/strain?photoList'>Photo Browser</a></li>
<li class="break"><a href='<%= contextPath %>/inoc?form=add'>Add Inoculation(s)</a></li>
<li><a href='<%= contextPath %>/harvest'>Search Harvests</a></li>
<li><a href='<%= contextPath %>/preserve.jsp'>Browse Preservations</a></li>
<li><a href='<%= contextPath %>/preserve/add.jsp'>Add Preservation(s)</a></li>
</ul></li>
<% } 
	if ( request.isUserInRole("sample") ) { 
%><!-- SAMPLE MENU -->
<li><a>Materials</a>
<ul class='submenu'>
<li><a href='<%= contextPath %>/material'>Search Materials</a></li>
<li><a href="<%= contextPath %>/material/upload.jsp">Upload Extract Data</a></li>
<!--  <li><a href='<%= contextPath %>/material?protocolMgr'>Manage Extract Protocols</a></li>  -->
<li class="break"><a href='<%= contextPath %>/separation'>Search Separations</a></li>
<li><a href='<%= contextPath %>/separation/protocol'>Manage Separation Templates</a></li>
<li><a href='<%= contextPath %>/separation/upload.jsp'>Upload New Separation</a></li>
<li class="break"><a href='<%= contextPath %>/sample?newCollection'>Add Sample Collection</a></li>
<li><a href='<%= contextPath %>/sample'>Browse Samples</a></li>
<li><a href='<%= contextPath %>/sample/upload.jsp'>Upload Sample Data</a></li>
<li><a href="<%= contextPath %>/sample/move-upload.jsp">Move Samples (upload)</a></li>
<li class="break"><a href='<%= contextPath %>/compound'>Search Compounds</a></li>
<li><a href='<%= contextPath %>/compound?form=add'>Add Compound</a></li>
<li><a href='<%= contextPath %>/compound/upload.jsp'>Upload Compounds</a></li>
<li><a href='<%= contextPath %>/dereplication.jsp'>Dereplication</a></li>
</ul></li>
<% } 
	if ( request.isUserInRole("assay") ) { %>
<!-- ASSAY MENU -->
<li><a>Assays</a>
<ul class='submenu'>
<li><a href='<%= contextPath %>/assay?action=add'>Add New</a></li>
<li><a href='<%= contextPath %>/assay'>Browse Data</a></li>
<li class="break"><a href='<%= contextPath %>/assay/protocol'>Manage Templates</a></li>
<li><a href='<%= contextPath %>/assay/upload.jsp'>Upload Data</a></li>
</ul></li>
<% } %>
<!-- HELP MENU -->
<li class='helpmenu' id='helpMenu'><a>Help</a>
<ul class='submenu'><li><%
	if ( jspContext.getAttribute("helpModule") != null ) {
%><a href='<%= contextPath %>/help?toc&module=${helpModule}'>Quick Help</a></li>
<li class="break"><% } %><a href='<%= contextPath %>/help?toc'>Contents</a></li>
<li><a href='<%= contextPath %>/help?find'>Find a Topic</a></li>
<li><a href='<%= contextPath %>/help?search'>Search</a></li>
</ul></li>
<li style="float:right; border: 0px; padding-top:5px; padding-right:10px" class="notablet">
<% if (UploadServlet.hasSpreadsheet(request) ) { %>
<a href="<%= contextPath %>/spreadsheet.jsp" style="width:20px; display:inline" class="ignore"><img title="View loaded spreadsheet" src="<%= contextPath %>/images/icons/spreadsheet.png" height="20px"></a>
<% }
	if ( UploadServlet.hasActiveJobs(session) ) { 
%><a href="<%= contextPath %>/jobs.jsp" style="width:20px; display:inline" class="ignore"><img title="A job is running" src="<%= contextPath %>/images/icons/job-running.png" height="20px"></a>
<% } %>
</li>
</ul>
</div>
</nav>
</div>