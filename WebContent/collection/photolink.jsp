<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page import="edu.uic.orjala.cyanos.BasicObject,
	edu.uic.orjala.cyanos.Collection,
	edu.uic.orjala.cyanos.DataFileObject,
	edu.uic.orjala.cyanos.web.servlet.DataFileServlet,
	edu.uic.orjala.cyanos.web.servlet.CollectionServlet,	
	edu.uic.orjala.cyanos.Role,
	edu.uic.orjala.cyanos.web.BaseForm,
	edu.uic.orjala.cyanos.DataException,
	edu.uic.orjala.cyanos.ExternalFile, java.util.HashMap,
	java.io.File, java.util.Map, java.util.Arrays, java.util.Map.Entry, java.util.List, java.util.Set, java.util.TreeSet,
	net.sf.jmimemagic.Magic,
	net.sf.jmimemagic.MagicMatch,
	net.sf.jmimemagic.MagicException,
	net.sf.jmimemagic.MagicMatchNotFoundException,
	net.sf.jmimemagic.MagicParseException" %>
<% String contextPath = request.getContextPath();
	String div = request.getParameter("div");
	Object anObj = request.getAttribute(CollectionServlet.ATTR_COLLECTION);
	if ( anObj != null && anObj instanceof DataFileObject ) {
	DataFileObject source = (DataFileObject) anObj;
	String dataFileClass = Collection.DATA_FILE_CLASS;
	File directory = (File) request.getAttribute(DataFileServlet.ATTR_CURRENT_DIR);
	String path = request.getParameter("path");
	if ( path == null ) path = "/"; else if ( ! path.endsWith("/") ) path = path.concat("/");
	String previewURL = contextPath.concat("/file/preview/").concat(dataFileClass).concat("/photo/");
if ( source.isAllowed(Role.WRITE) ) { %>
<form name="photoBrowser" method="post" enctype="multipart/form-data">
<input type="hidden" name="id" value="<%= source.getID() %>">
<input type="hidden" name="div" value="<%= div %>">
<input type="hidden" name="showBrowser" value="1">
<input type="hidden" name="path" value="<%= path %>">
<input type="hidden" name="<%= DataFileServlet.DATAFILE_CLASS %>" value="<%= dataFileClass %>">
<input type="hidden" name="<%= DataFileServlet.PARAM_DATATYPE %>" value="<%= Collection.PHOTO_DATA_TYPE %>">
<div align="center">
<% if (request.getAttribute(DataFileServlet.ATTR_UPLOAD_MESSAGE) != null) { 
	out.print("<p>");
	out.print((String)request.getAttribute(DataFileServlet.ATTR_UPLOAD_MESSAGE));
	out.println("</p>");
} %>
<table border="0" style="width: 90%; margin-left:5%;">
<tr><td colspan="2"><h3>Current path: <%= path %></h3>
<% if ( path.length() > 1 ) { String previous = path.substring(0, path.lastIndexOf("/", path.length() - 2)); %>
<a onclick="updateFileDiv('<%= div %>', '<%= previous %>', '<%= dataFileClass %>', '<%= Collection.PHOTO_DATA_TYPE %>')"><i>Parent directory</i></a><% } %></td></tr>
<% 	File[] kids = directory.listFiles();
if ( kids != null ) { %>
<tr><td style="width:15%; border-right: 1px solid black;" valign="top">
<p align="center>"><b>Subdirectories</b></p>
<ul><% Arrays.sort(kids, DataFileServlet.directoryFirstCompare());
	
	ExternalFile myFiles = source.getDataFilesForType(Collection.PHOTO_DATA_TYPE);					

	Map<String,String> fileDesc = new HashMap<String,String>();

	myFiles.beforeFirst();
	while ( myFiles.next() ) {
		String aPath = myFiles.getFilePath();
		if ( ! aPath.startsWith("/") ) aPath = "/".concat(aPath);
		fileDesc.put(aPath, myFiles.getDescription());
	}
	
	int i = 0;
	
	while ( i < kids.length ) {
		if ( kids[i].isDirectory() ) {
			String dirName = kids[i].getName();
%><li><a onclick="updateFileDiv('<%= div %>', '<%= path.concat(dirName) %>', '<%= dataFileClass %>', '<%= Collection.PHOTO_DATA_TYPE %>')"><%= dirName %></a></li><% } else { break; } i++; } %>
</ul>
</td><td><table class="files">
<% 	
	boolean update = request.getParameter("updateFiles") != null;

	Map<String,String> addedPaths = new HashMap<String,String>();
	
	if ( update && request.getParameter("selFile") != null ) {
		for ( String index: request.getParameterValues("selFile") ) {
			String newPath = request.getParameter(index.concat("_path"));
			String desc = request.getParameter(index.concat("_desc"));
			addedPaths.put(newPath, desc);
		}
	}

	int cell = 1;
	
	out.print("<tr class=\"listing\" valign=\"middle\">");
	while ( i < kids.length ) {
		String fileName = kids[i].getName(); 
		String thisPath = path.concat(fileName); 
		String myDesc = fileDesc.get(thisPath);
		
		if ( update ) {
			if ( myDesc != null && (! addedPaths.containsKey(thisPath)) ) {
				source.unlinkDataFile(thisPath);
				myDesc = null;
			} else if ( addedPaths.containsKey(thisPath) ) {
				myDesc =  addedPaths.get(thisPath);
				source.linkDataFile(thisPath, Collection.PHOTO_DATA_TYPE, myDesc, null);
			}
		}
		
		boolean isLinked = myDesc != null;
		boolean show = isLinked;
		String mimeType;

		if ( ! show ) { 
			try {
				MagicMatch aMatch = Magic.getMagicMatch(kids[i], true);
				show = aMatch.getMimeType().startsWith("image");		
			} catch (MagicMatchNotFoundException e) {
				this.log(String.format("CANNOT determine MIME type for file %s", kids[i].getAbsolutePath()));
			} catch (MagicParseException e) {
				this.log(String.format("CANNOT determine MIME type for file %s", kids[i].getAbsolutePath()));
			} catch (MagicException e) {
				this.log(String.format("CANNOT determine MIME type for file %s", kids[i].getAbsolutePath()));
			} 

		}
		
		if ( show ) {
%><td align="center">
<img src="<%= previewURL.concat(thisPath) %>"><br/>
<%= fileName %> (<%= DataFileServlet.humanReadableSize(kids[i].length()) %>)<br>
<input type="hidden" name="<%= i %>_path" value="<%= thisPath %>">
<input type="checkbox" name="selFile" value="<%= i %>" <%= (isLinked ? "checked" : "") %> 
	onclick="this.form.elements['<%= i%>_desc'].disabled = (! this.checked);">
<input type="text" style="width:95%" name="<%= i %>_desc" value="<c:out value="<%= myDesc %>"/>" <%= ( isLinked ? "" : "disabled") %>>
</td>
<% if ( cell == 3 ) { out.print("</tr><tr class=\"listing\" valign=\"middle\">"); cell = 1; } else { cell++; } } i++; }%>
</tr>
</table>
</td></tr><% } %>
</table>
</div>
<div align="center">
<!--  
<iframe src="<%= contextPath %>/file/upload?<%= String.format("%s=%s", DataFileServlet.DATAFILE_CLASS, Collection.DATA_FILE_CLASS) %>&<%= String.format("%s=%s", DataFileServlet.PARAM_DATATYPE, Collection.PHOTO_DATA_TYPE) %>" 
width="90%" height=70 frameborder=0 scrolling=no></iframe>
-->
<p>Additional files can be to the directory <%= directory.getAbsolutePath() %></p>
</div>
<p align="center"><button type="button" onClick="updateForm(this,'<%= div %>')" NAME='updateFiles'>Update</button><button type="button" onClick="updateForm(this,'<%= div %>')" NAME='cancelBrowser'>Close</button></p>
</form>
<% } } %>