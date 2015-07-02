<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.web.AppConfig,
	edu.uic.orjala.cyanos.web.servlet.MainServlet, 
	edu.uic.orjala.cyanos.sql.SQLProject,
	java.util.Map, java.util.Iterator, 
	java.util.Map.Entry,
	java.util.List,
	edu.uic.orjala.cyanos.Material,
	edu.uic.orjala.cyanos.Assay,
	edu.uic.orjala.cyanos.Separation,
	edu.uic.orjala.cyanos.Compound,
	java.security.GeneralSecurityException" %>
<!DOCTYPE html>
<html>
<head>
<% String contextPath = request.getContextPath();  %>
<meta charset="UTF-8">
<link rel="stylesheet" type="text/css" href="<%= contextPath %>/cyanos.css"/>
<script type="text/javascript" src="<%= contextPath %>/cyanos.js"></script>
<title>Cyanos Database - Application Setup</title>
</head>
<body>
<% AppConfig appConfig = (AppConfig) request.getAttribute(MainServlet.APP_CONFIG_ATTR); 
	if ( appConfig != null ) { 	Float version = appConfig.getVersion(); %>
<p><b>Application version</b>...<%= ( version > 0 ? String.format("%.2f", version.floatValue()) : "UNDEFINED" ) %></p>
<p><b>File paths</b>...
<% 	Map<String, Map<String, String>> fileTypeMap = appConfig.getFilePathMap(); 
	if ( fileTypeMap.size() > 0 ) { %>
</p><table class="species">
<tr><th>Object Class</th><th>File Type</th><th>Directory</th></tr>	
<% Iterator<String> classIter = fileTypeMap.keySet().iterator();
	while ( classIter.hasNext() ) {
			String aClass = classIter.next();
			Map<String, String> classMap =  fileTypeMap.get(aClass);
			Iterator<String> typeIter = classMap.keySet().iterator();
			while ( typeIter.hasNext() ) {
				String aType = typeIter.next(); %>
<tr><td><%= aClass %></td>
<td><%= aType %></td>
<td><%= classMap.get(aType) %></td></tr>
<% } } %>
</table>
<% } else { %>
None configured</p>
<% } %>
<p><b>File types</b></p>
<% String[] dataTypes = { Material.DATA_FILE_CLASS, Separation.DATA_FILE_CLASS, Assay.DATA_FILE_CLASS, Compound.DATA_FILE_CLASS  };  
	String[] dataTitles = { "Materials", "Separations", "Assays", "Compounds" }; %>
<dl>
<% for ( int i = 0; i < dataTypes.length; i++ ) { %>
<dt><i><%= dataTitles[i] %></i></dt>
<dd>
<% 	Map<String,String> dataMap = appConfig.getDataTypeMap(dataTypes[i]);
	if ( dataMap != null ) { %>
<ul style="list-style-type: none;">
<%	for ( Entry<String,String> entry : dataMap.entrySet() ) { %>	
<li><%= entry.getKey() %> - <%= entry.getValue() %></li>
<%  }  %></ul> 
<% } else { %>NONE<% } %></li> <% } %>
</dl>

<p><b>Maps</b></p>
<% 	Map<String,String> layers = appConfig.getMapServerLayers();
 		
	boolean enableOSM = ("1".equals(appConfig.getMapParameter(AppConfig.MAP_OSM_LAYER)));
	boolean enableNASA = ("1".equals(appConfig.getMapParameter(AppConfig.MAP_NASA_LAYER)));
	String googleMapKey = appConfig.getGoogleMapKey(); %>
<ul style="list-style-type: none;">
<li>OpenStreet Maps...<%= (enableOSM ? "Enabled" : "Disabled") %></li>
<li>NASA BlueMarble Maps...<%= (enableNASA ? "Enabled": "Disabled") %></li>
<li>Google Maps...<% if ( googleMapKey != null && googleMapKey.length() > 0 ) { %>Enabled (<%= googleMapKey %>)<% } else { %>Disabled<% } %></li>
<li>Custom Maps...
<% if ( layers.size() > 0 ) { %>
<dl>
<% for ( Entry<String,String> layer : layers.entrySet() ) { %>
<dt><%= layer.getKey() %></dt><dd><%= layer.getValue() %></dd>
<% } %>
</dl>
<% } else { %>
None
<% }  %></li>
</ul>

<p><b>Custom Dereplication Modules</b>
<% List<String> modules = appConfig.classesForDereplicationModule(); 
	if ( modules != null && modules.size() > 0 ) { %>
</p>
<ul style="list-style-type: none;">
<% for ( String module : modules ) { %>
<li><%= module %></li>		
<% } %>
</ul>
<% } else { %>
... None</p>
<% } %>

<p><b>Custom Upload Modules</b>
<% modules = appConfig.classesForUploadModule(); 
	if ( modules != null && modules.size() > 0 ) { %>
</p>
<ul style="list-style-type: none;">
<% for ( String module : modules ) { %>
<li><%= module %></li>		
<% } %>
</ul>
<% } else { %>
... None</p>
<% } %>

<p><b>Update Key Pair</b>...
<% String pubKeyString = appConfig.getUpdateCert(); 
if ( pubKeyString != null && pubKeyString.length() > 0 && appConfig.getUpdateKey() != null ) { %>
Exists</p>
<p style="margin-left:5mm;"><code>
<% try { 
	if ( pubKeyString.startsWith("----BEGIN ") )
		out.println(pubKeyString.replace("\n", "<br>"));
	else
		out.println(SQLProject.encodePublicKey(SQLProject.decodePublicKey(pubKeyString)).replace("\n", "<br>")); 
} catch (GeneralSecurityException e) {
	out.println("ERROR: ");
	out.println(e.getLocalizedMessage());
}%>
</code></p>
<% } else { %>
None</p>
<% } %>
<% } %>
</body>
</html>