<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.BasicObject,
	edu.uic.orjala.cyanos.DataFileObject,
	edu.uic.orjala.cyanos.web.servlet.DataFileServlet,	
	edu.uic.orjala.cyanos.Role,
	edu.uic.orjala.cyanos.ExternalFile,
	edu.uic.orjala.cyanos.web.BaseForm,
	edu.uic.orjala.cyanos.DataException,
	java.text.SimpleDateFormat,
	java.io.File, java.util.Map" %>
<% String contextPath = request.getContextPath();
	String div = request.getParameter("div"); %>
<div id="<%=div %>">
<%	Object anObj = request.getAttribute(DataFileServlet.DATAFILE_PARENT);
	if ( anObj != null && anObj instanceof DataFileObject ) {
	DataFileObject source = (DataFileObject) anObj;
if ( source.isAllowed(Role.WRITE) && request.getParameter("cancelBrowser") == null && request.getParameter("showBrowser") != null ) { %>
<jsp:include page="datalink.jsp"/>
<%--
<!--  
<form name="photoBrowser" method="post" enctype="multipart/form-data">
<input type="hidden" name="id" value="<%= source.getID() %>">
<input type="hidden" name="div" value="<%= div %>">
<div align="center">
<% if (request.getAttribute(DataFileServlet.ATTR_UPLOAD_MESSAGE) != null) { %>
<p><%= (String)request.getAttribute(DataFileServlet.ATTR_UPLOAD_MESSAGE) %></p>
<% } %>
<object classid="clsid:8AD9C840-044E-11D1-B3E9-00805F499D93" codebase="http://java.sun.com/out-of-proc-plugin-url-placeholder.exe#1,6,0,10" width="80%" height="300"
id="objectFM">
<param name="code" value="edu.uic.orjala.cyanos.applet.FileManager">
<param name="archive" value="<%= contextPath %>/applets/filemanager.jar">
<param name="urlPath" value="<%= contextPath %>/file/manager/<%= (String) request.getAttribute(DataFileServlet.DATAFILE_CLASS) %>/null/<%= (request.getParameter(DataFileServlet.PARAM_FILE_PATH) != null ? request.getParameter(DataFileServlet.PARAM_FILE_PATH) : "") %>">
<param name="objclass" value="<%= (String) request.getAttribute(DataFileServlet.DATAFILE_CLASS) %>">
<param name="objID" value="<%= source.getID() %>">
<comment>
<embed id="embedFM" type="application/x-java-applet;version=1.6" code="edu.uic.orjala.cyanos.applet.FileManager" 
align="center" width="80%" height="300"
archive="<%= contextPath %>/applets/filemanager.jar" 
urlPath="<%= contextPath %>/file/manager/<%= (String) request.getAttribute(DataFileServlet.DATAFILE_CLASS) %>/null/"
objclass="<%= (String) request.getAttribute(DataFileServlet.DATAFILE_CLASS) %>"
objID="<%= source.getID() %>">
<noembed>Java not available</noembed></embed>
</comment>
</object>
</div>
<div align="center">
<iframe src="<%= contextPath %>/file/upload?<%= String.format("%s=%s", DataFileServlet.DATAFILE_CLASS, request.getParameter(DataFileServlet.DATAFILE_CLASS)) %>&<%= String.format("%s=%s", DataFileServlet.PARAM_DATATYPE, request.getParameter(DataFileServlet.PARAM_DATATYPE)) %>" 
width="90%" height=70 frameborder=0 scrolling=no></iframe>
</div>
<p align="center"><button type="button" onClick="updateForm(this,'<%= div %>')" NAME='cancelBrowser'>Cancel</button></p>
</form>
-->
--%>
<% } else {	
	ExternalFile myFiles = source.getDataFiles(); 
	String objClass = (String) request.getAttribute(DataFileServlet.DATAFILE_CLASS);
	if ( myFiles.first() ) {		
		myFiles.beforeFirst(); boolean oddRow = true; 
		Map<String,String> typeMap = (Map<String,String>) request.getAttribute(DataFileServlet.DATAFILE_TYPE_MAP);  %>
<table  class="list">
<col class='datashort'><col class='datashort'><col class='datalong'>
<tr><th class="header">File</th><th class="header">Data Type</th><th class="header">Description</th></tr>
<%while ( myFiles.next() ) { 
	File aFile = myFiles.getFileObject();  
	String type = typeMap.get(myFiles.getDataType());  %>
<tr class="<%= oddRow ? "odd" : "even" %>"  valign="middle"><td><a href="<%= contextPath %>/file/get/<%= objClass %>/<%= source.getID() %>/<%= myFiles.getFilePath() %>">
<img src="<%= DataFileServlet.getIconPathForMIMEType(request, myFiles.getMimeType()) %>" border=0>
<%= aFile.getName() %></a></td>
<td><%= ( type != null ? type : myFiles.getDataType() ) %></td>
<td><%= myFiles.getDescription() %></td></tr>
<% oddRow = (! oddRow ); } %>
</table><% } else { %><p align="center"><b><i>None</i></b></p><% } if ( source.isAllowed(Role.WRITE) ) { %>
<form><input type="hidden" name="id" value="<%= source.getID() %>">
<input type="hidden" name="div" value="<%= div %>">
<p align="center"><button type="button" onClick="updateForm(this,'<%= div %>')"name="showBrowser">Manage Data Files</button></p>
</form>
<% } } } %>
</div>