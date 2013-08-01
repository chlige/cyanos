<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.web.AppConfig,
	edu.uic.orjala.cyanos.web.servlet.MainServlet, 
	edu.uic.orjala.cyanos.web.MultiPartRequest,
	java.util.Map, java.util.Date, java.text.DateFormat" %>
<h2 align="center">Restore Saved Configuration</h2>
<% AppConfig appConfig = (AppConfig) session.getAttribute(MainServlet.APP_CONFIG_ATTR); 
	Map<String,String> setupValues = (Map<String,String>) session.getAttribute(MainServlet.ATTR_SETUP_VALUES);
	boolean complete = false;
	if ( request.getAttribute(MainServlet.ATTR_FILE_COMPLETE) != null ) {
		complete = (Boolean)request.getAttribute(MainServlet.ATTR_FILE_COMPLETE);
	}
	AppConfig newConfig = (AppConfig) session.getAttribute(MainServlet.SESS_ATTR_UPLOAD_CONFIG);
	if ( appConfig != null ) { %>
<form method="post" enctype="multipart/form-data">
<input type="hidden" name="uploadPage">
<p class="mainContent">Use a saved configuration from a previous installation of CYANOS you wish to restore or clone.</p>
<p><b>NOTE:</b> If you are restoring a installation of CYANOS the preferred procedure is to restore the MySQL database then reinstall the application, if needed.
The backup of the MySQL database should contain the configuration information for the CYANOS application.</p>
<% if (newConfig != null ) { 
	Date currDate = appConfig.getDate(); 
	Date newDate = newConfig.getDate(); 
	DateFormat format = DateFormat.getDateInstance(DateFormat.MEDIUM);  %>
<table >
<tr><td></td><th>Existing configuration</th><th>Uploaded configuration</th></tr>
<tr align="center"><th>Version</th>
<td><%= (appConfig.getVersion() > 0 ? String.format("%.2f", appConfig.getVersion()) : "UNDEFINED") %></td>
<td><%= (newConfig.getVersion() > 0 ? String.format("%.2f", newConfig.getVersion()) : "UNDEFINED") %></td></tr>
<tr align="center"><th>Date</th>
<td><%= (currDate != null ? format.format(currDate) : "UNDEFINED" ) %></td>
<td><%= (newDate != null ? format.format(newDate) : "UNDEFINED" ) %></td></tr>
<tr align="center"><td></td>
<td><button type="submit" name="useConfig" value="sql">Use existing configuration</button></td>
<td><button type="submit" name="useConfig" value="xml">Use uploaded configuration</button></td>
</table>
<% if ( appConfig.getVersion() > newConfig.getVersion() )  { %>
<p align="center"><b><font color='red'>WARNING</font>: The uploaded configuration is a lower version than the existing configuration.</b><br>
It is suggested that you use the existing configuration.</p>
<% } else if ( currDate != null && newDate != null && ( currDate.compareTo(newDate) > 0 ) )  { %>
<p align="center"><b><font color='red'>WARNING</font>: The uploaded configuration is older that the existing configuration.</b><br>
It is suggested that you use the existing configuration.</p>
<% } } else if ( request.getParameter("useConfig") != null  ) { 	
	Date currDate = appConfig.getDate(); 
	DateFormat format = DateFormat.getDateInstance(DateFormat.MEDIUM); %>
<p>Selected configuration: version <%= (appConfig.getVersion() > 0 ? String.format("%.2f", appConfig.getVersion()) : "UNDEFINED") %> (<%= (currDate != null ? format.format(currDate) : "DATE UNDEFINED" ) %>)</p>
<% } else { if (setupValues.containsKey(MainServlet.SETUP_XML_CONFIG) ) {%>
<p>An existing XML configuration file exits (<%= setupValues.get(MainServlet.SETUP_XML_CONFIG) %>). <button type="submit" name="<%= MainServlet.SETUP_ACTION_PARSE  %>">Load</button></p>
<% } %>
<p><b>File to upload (XML configuration):</b><input type="file" name="<%= MainServlet.SETUP_UPLOAD %>" size="25">
<button type="submit" name="<%= MainServlet.SETUP_ACTION_UPLOAD %>">Upload XML File</button></p>
<% } %></form><% } %>
<form method="post">
<table class="buttons"><tr>
<td><button type="submit" name="back">&lt; Previous</button></td>
<td><button name="nextPage" type="submit" <%= ( complete ? "" : "disabled" ) %>>Next &gt;</button></td></tr>
</table></form>