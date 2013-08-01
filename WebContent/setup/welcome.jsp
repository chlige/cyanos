<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.web.servlet.MainServlet, java.util.Map" %>
<% Map<String,String> setupValues = (Map<String,String>) session.getAttribute(MainServlet.ATTR_SETUP_VALUES); %>
<h2 align="center">Welcome!</h2>
<p class="mainContent">Welcome to Cyanos, a natural product drug discovery information management system.  
This system allows one to store and manage data related to:
<ul>
<li>Taxonomic data</li>
<li>Culture information, e.g. inoculations, harvest, and cryopreservations.</li>
<li>Extraction and fractionation data.</li>
<li>Sample libraries</li>
<li>Bioassay data</li>
<li>Datafile management, e.g. LC-UV, MS, and NMR data.</li>
</ul>
<p align="center"><button type="submit" name="uploadPage">Restore Saved Configuration</button></p>
<h3>NOTICE</h3>
<p class="mainContent">If this is a new installation of CYANOS, be sure to complete the following tasks before 
you configure the web application (i.e., these configuration pages).  See the <a href="setup/install.html">Installation Instructions</a> for details.</p>

<table class="buttons"><tr>
<td><button name="<%= ( setupValues.containsKey(MainServlet.SETUP_XML_CONFIG) ? "uploadPage" : "nextPage" )%>" type="submit">Next &gt;</button></td></tr>
</table>