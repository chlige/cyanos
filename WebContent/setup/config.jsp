<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.web.AppConfig,
	edu.uic.orjala.cyanos.web.servlet.MainServlet, 
	edu.uic.orjala.cyanos.web.servlet.AdminServlet,
	java.util.Map,java.util.Map.Entry" %>
<h2 align="center">Basic Configuration</h2>
<% AppConfig appConfig = (AppConfig) session.getAttribute(MainServlet.APP_CONFIG_ATTR); 
	Map<String,String> setupValues = (Map<String,String>) session.getAttribute(MainServlet.ATTR_SETUP_VALUES);
	boolean valid = false;
	if ( appConfig != null ) { %>
<p class="mainContent">Setup the basic configuration of this application.</p>
<% Object error = request.getAttribute("update_error");
if ( error != null && error instanceof Throwable ) { %>
<div class="error"><b>ERROR:</b> <%= ((Throwable)error).getLocalizedMessage() %></div>
<% } %>
<% String defaultPath =  setupValues.get(MainServlet.SETUP_DEFAULT_PATH); 
	if ( defaultPath == null ) defaultPath = appConfig.getFilePath("*", "*");  %>
<p><b>Default File Path</b>: <input type="text" name="<%= MainServlet.SETUP_DEFAULT_PATH %>" value="<c:out value="<%= defaultPath %>"/>" size=50><br>
This specifies the default location, on the server, to store externally linked files, e.g. strain photos, assay data spreadsheets, chromatograms, and spectral data.
More detailed configuration of file paths, i.e. different paths for different types of files, can be setup via the "Advanced Configuration..." or 
via the "CYANOS &gt; Manage Config" menu item after the initial setup.
</p>
<%  boolean enableOSM = ("1".equals(appConfig.getMapParameter(AppConfig.MAP_OSM_LAYER)));
	boolean enableNASA = ("1".equals(appConfig.getMapParameter(AppConfig.MAP_NASA_LAYER)));
	String googleMapKey = appConfig.getGoogleMapKey();
	
	if ( ! appConfig.configExists() )
		enableOSM = true;
%>
<h3>Map setup</h3>
<input type="hidden" name="setup-map" value="1">
<p><input type="checkbox" name="enableOSM" value="1" <%= ( enableOSM ? "checked" : "") %>> Enable OpenStreet Terrain map.</p>
<p><input type="checkbox" name="enableNASA" value="1" <%= ( enableNASA ? "checked" : "") %>> Enable NASA BlueMarble map.</p>
<p>Google Maps API Key: <input type="text" name="" value="<%= ( googleMapKey != null ? googleMapKey : "") %>" size="100"><br>
You can acquire a Map API key at <a href='http://code.google.com/apis/maps/signup.html'>http://code.google.com/apis/maps/signup.html</a></p>
<%-- boolean hasKey = ( appConfig.getUpdateCert() != null && appConfig.getUpdateKey() != null );
boolean genKeys = ( hasKey || setupValues.containsKey("genKeys") ); --%>
<!-- <p><input type="checkbox" name="genKeys" value="1" <%--= ( genKeys ? "checked" : "") %>" <%= (hasKey ? "disabled" : "") %>> <font color="<%=(hasKey ? "grey" : "black") %>"><b>Generate project update key pair</b></font><%= ( hasKey ? " (<b>Key pair exists</b>)" : "") --%><br>
A update key pair is required by CYANOS to establish a relationship between two servers for project updates. 
Normal operation of CYANOS does not requires an update key pair.
However, if this server will be participating in project updates with a remote server, either as master or slave, then an update key pair is <b>required</b>.
<br><b>NOTE: </b> an update key pair can be generated at a later time using the configuration management page (CYANOS %gt; Manage Config).</p>
<p align="center"><button name="customize" type="submit">Advanced Configuration...</button></p> -->
<% } %><table class="buttons"><tr>
<td><button type="submit" name="prevPage">&lt; Previous</button></td>
<td><button name="nextPage" type="submit">Next &gt;</button></td></tr>
</table>
