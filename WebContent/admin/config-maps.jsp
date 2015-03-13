<%@ page import="edu.uic.orjala.cyanos.web.AppConfig,
	edu.uic.orjala.cyanos.web.servlet.AdminServlet,
	java.util.Map,java.util.TreeSet,
	java.util.Iterator" %>
<div><h2>Mapping Configuration</h2>
<% AppConfig appConfig = (AppConfig) session.getAttribute(AdminServlet.APP_CONFIG_ATTR); 
	if ( appConfig != null ) { 
%><h3>OpenLayers Mapping</h3>
<p>CYANOS utilizes the OpenLayers mapping system to display maps (<a href='http://openlayers.org'>openlayers.org</a>).<br>
This mapping system allows one to customize the maps that can be displayed, also called layers.<br>
By default, CYANOS will enable the OpenStree maps terrain layer.</p>
<form method="post">
<input type="hidden" name="form" value="<%= request.getParameter("form") %>">
<% 	Map<String,String> layers = appConfig.getMapServerLayers();
 		
	boolean enableOSM = ("1".equals(appConfig.getMapParameter(AppConfig.MAP_OSM_LAYER)));
	boolean enableNASA = ("1".equals(appConfig.getMapParameter(AppConfig.MAP_NASA_LAYER)));
	boolean enableMQ = ("1".equals(appConfig.getMapParameter("mapQuest")));
	String googleMapKey = appConfig.getGoogleMapKey();
	
	if ( ! appConfig.configExists() )
		enableOSM = true;
%><p><input type="checkbox" name="enableOSM" value="1" <%= ( enableOSM ? "checked" : "") %>> Enable OpenStreet Terrain map.</p>
<p><input type="checkbox" name="enableMQ" value="1" <%= ( enableMQ ? "checked" : "") %>> Enable MapQuest layers.</p>
<p><input type="checkbox" name="enableNASA" value="1" <%= ( enableNASA ? "checked" : "") %>> Enable NASA BlueMarble map.</p>
<p>Google Maps API Key: <input type="text" name="" value="<%= ( googleMapKey != null ? googleMapKey : "") %>" size="100"><br>
You can acquire a Map API key at <a href='http://code.google.com/apis/maps/signup.html'>http://code.google.com/apis/maps/signup.html</a></p>
<p>Link to an additional MapServer services.  MapServer source code/binaries can be down-loaded from <a href='http://mapserver.org/'>http://mapserver.org/</a><br>
New Layer: (Name)<input type='text' name='mapName' size=25 /> (URL)<input type='text' name='mapURL' size=75 /><br>
<% if ( layers.size() > 0 ) { 
%><b>Existing Layers</b></p>
<table class="species">
<tr><th>Layer Name</th><th>URL</th><th>Delete</th></tr>
<%	for ( String key : new TreeSet<String>(layers.keySet()) ) { %>
<tr><td><%= key %></td><td><%= layers.get(key) %></td><td><input type="checkbox" name="delLayer" value="<%= key %>"></td></tr>		
<% } 
%></table>
<% } 
%><p><button type="submit" name="<%= AdminServlet.PARAM_CONFIG_UPDATE %>">Update</button><button type="reset">Reset Values</button></p>
</form>
<% } 
%></div>