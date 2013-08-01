<%@ page import="edu.uic.orjala.cyanos.web.AppConfig,
	edu.uic.orjala.cyanos.web.servlet.AdminServlet,
	edu.uic.orjala.cyanos.web.CustomJarFilter,
	java.util.List, java.io.File, java.io.IOException,
	edu.uic.orjala.cyanos.web.UploadForm,
	edu.uic.orjala.cyanos.web.DereplicationForm, 
	java.util.jar.JarFile, java.util.Enumeration, java.util.jar.JarEntry, java.io.FileFilter" %>
<div><h2>Custom Modules</h2>
<% AppConfig appConfig = (AppConfig) session.getAttribute(AdminServlet.APP_CONFIG_ATTR); 
if ( appConfig != null ) { %>
<p>Classes specified should be fully qualified, e.g. <code>java.lang.String</code> not <code>String</code>, and implement the appropriate interface.<br>
In addition, the compiled class files should be located in the CLASSPATH of the Tomcat Application Server</p>
<% 
List<String> derepModules = appConfig.classesForDereplicationModule();
List<String> uploadModules = appConfig.classesForUploadModule();
if ( ( derepModules != null && derepModules.size() > 0 ) || ( uploadModules != null && uploadModules.size() > 0 ) ) {
%>
<form method="post">
<input type="hidden" name="form" value="<%= request.getParameter("form") %>">
<table class="species">
<tr><th>Module Type</th><th>Java Class</th><th>Remove</th></tr>
<%	if ( derepModules != null ) { for ( String module : derepModules ) { %>
<tr><td>Dereplication</td><td><%= module %></td><td><input type="checkbox" name="delDerepClass" value="<%= module %>"></td></tr>		
<% } } %>
<%	if ( uploadModules != null ) { for ( String module : uploadModules ) { %>
<tr><td>Upload Forms</td><td><%= module %></td><td><input type="checkbox" name="delUploadClass" value="<%= module %>"></td></tr>		
<% } } %>
</table>
<p><button type="submit" name="<%= AdminServlet.PARAM_CONFIG_UPDATE %>">Update</button><button type="reset">Reset Values</button></p>
</form>
<% } %>
<% String libPath = application.getRealPath("/WEB-INF/lib"); 
File libDir = new File(libPath);
if ( request.getParameter("jar") != null )  { %>
<form method="post">
<input type="hidden" name="form" value="<%= request.getParameter("form") %>">
<input type="hidden" name="lib-jar" value="<%= request.getParameter("jar") %>">
<h3>New Modules</h3>
<p>Select modules to add to CYANOS.  You only need to select the Java class for the module and not any of the supporting classes that may be present in the JAR file.<br>
The supporting classes will load automatically with the module class.</p>
<table>
<tr><td></td><th>Module Type</th><th>Java Class</th></tr>
<% try { 
	JarFile jar = new JarFile(new File(libDir, request.getParameter("jar")));
	ClassLoader classLoad = page.getClass().getClassLoader();
	for ( Enumeration<JarEntry> anEnum = jar.entries(); anEnum.hasMoreElements(); ) {
		JarEntry entry = anEnum.nextElement();
		if ( entry.getName().endsWith(".class") ) {
			String name = entry.getName().replaceAll("/", "\\.");
			name = name.substring(0, name.length() - 6); 
			boolean isDerep = false;
			boolean isUpload = false;
			try {
				Class aClass = classLoad.loadClass(name);
				if ( aClass != null ) {
					isUpload = UploadForm.class.isAssignableFrom(aClass);
					isDerep = DereplicationForm.class.isAssignableFrom(aClass);
				}
			} catch (Throwable e) {
				continue;
			}	
			if ( isDerep || isUpload ) { %>
<tr><td><input type="hidden" name="new_class" value="<%= name %>">
<input type="checkbox" name="class:<%= name %> value="<%= ( isDerep ? AppConfig.DEREPLICATION_MODULE : AppConfig.UPLOAD_MODULE ) %>"></td>
<td><%= (isDerep ? "Dereplication Module" : "Upload Form") %></td><td><%= name %></td></tr>	
<%		} }
	} 
%><tr><td colspan=2 align="center"><button type="submit" name="<%= AdminServlet.PARAM_CONFIG_UPDATE  %>">Update</button><button type="reset">Reset</button></td></tr> <%
} catch (IOException e) { %>
<tr><td colspan="2">ERROR: <%= e.getLocalizedMessage() %></td></tr>
<% } %>
</table>
</form>

<% } 
File[] files = libDir.listFiles(new FileFilter() { public boolean accept(File pathname) { return( pathname.isFile() && pathname.getName().endsWith(".jar")); }});
%> 
<h3>JAR Files</h3>
<p>JAR files with custom Java classes should be placed in the folder</p>
<p style='margin-left: 10px'><code><%= libPath %></code></p>
<p>on the Web Application Server.</p>
<% if ( files.length > 0 ) { %>
<ul>
<% for ( File file : files ) { %>
<li><a href="?form=<%= request.getParameter("form") %>&jar=<%= file.getName() %>"><%= file.getName() %></a></li>
<% } %>
</ul>
<% } } %>
</div>