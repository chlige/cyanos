<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page import="edu.uic.orjala.cyanos.BasicObject,
	edu.uic.orjala.cyanos.DataFileObject,
	edu.uic.orjala.cyanos.web.servlet.DataFileServlet,	
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
<% 	String contextPath = request.getContextPath();
	String div = request.getParameter("div");
	Object anObj = request.getAttribute(DataFileServlet.DATAFILE_PARENT);
	if ( anObj != null && anObj instanceof DataFileObject ) {
	DataFileObject source = (DataFileObject) anObj;
	String dataFileClass = (String) request.getAttribute(DataFileServlet.DATAFILE_CLASS);
	File directory = (File) request.getAttribute(DataFileServlet.ATTR_CURRENT_DIR);
	String path = request.getParameter("path");
	boolean staticType = request.getParameter(DataFileServlet.PARAM_DATATYPE) != null;
	if ( path == null ) path = "/"; else if ( ! path.endsWith("/") ) path = path.concat("/");
	String previewURL = contextPath.concat("/file/preview/").concat(dataFileClass).concat("/").concat(staticType ? request.getParameter(DataFileServlet.PARAM_DATATYPE) : "null").concat("/");
if ( source.isAllowed(Role.WRITE) ) { %>
<form name="photoBrowser" method="post" enctype="multipart/form-data">
<input type="hidden" name="id" value="<%= source.getID() %>">
<input type="hidden" name="div" value="<%= div %>">
<input type="hidden" name="showBrowser" value="1">
<input type="hidden" name="path" value="<%= path %>">
<input type="hidden" name="<%= DataFileServlet.DATAFILE_CLASS %>" value="<%= dataFileClass %>">
<% if ( staticType ) { %>
<input type="hidden" name="<%= DataFileServlet.PARAM_DATATYPE %>" value="<%= request.getParameter(DataFileServlet.PARAM_DATATYPE) %>">
<% } %>
<div align="center">
<% if (request.getAttribute(DataFileServlet.ATTR_UPLOAD_MESSAGE) != null) { 
	out.print("<p>");
	out.print((String)request.getAttribute(DataFileServlet.ATTR_UPLOAD_MESSAGE));
	out.println("</p>");
} %>
<table border="0" style="width: 90%; margin-left:5%;">
<tr><td colspan="2"><h3>Current path: <%= path %></h3>
<% if ( path.length() > 1 ) { String previous = path.substring(0, path.lastIndexOf("/", path.length() - 2)); %>
<a onclick="updateFileDiv('<%= div %>', '<%= previous %>', '<%= dataFileClass %>', '<%= staticType ? request.getParameter(DataFileServlet.PARAM_DATATYPE) : "" %>')"><i>Parent directory</i></a><% } %></td></tr>
<% 	File[] kids = directory.listFiles();
if ( kids != null ) { %>
<tr><td style="width:15%; border-right: 1px solid black;" valign="top">
<p align="center>"><b>Subdirectories</b></p>
<ul><% Arrays.sort(kids, DataFileServlet.directoryFirstCompare());
	
	Map<String,String> typeMap = (Map<String,String>) request.getAttribute(DataFileServlet.DATAFILE_TYPE_MAP);  
	ExternalFile myFiles;
	if ( staticType ) {
		myFiles = source.getDataFilesForType(request.getParameter(DataFileServlet.PARAM_DATATYPE));					
	} else {
		myFiles = source.getDataFiles();
	}

	Map<String,String> fileDesc = new HashMap<String,String>();;
	Map<String,String> fileType = new HashMap<String,String>();

	myFiles.beforeFirst();
	while ( myFiles.next() ) {
		String aPath = myFiles.getFilePath();
		if ( ! aPath.startsWith("/") ) aPath = "/".concat(aPath);
		fileDesc.put(aPath, myFiles.getDescription());
		fileType.put(aPath, myFiles.getDataType());
	}
	
	int i = 0;
	
	while ( i < kids.length ) {
		if ( kids[i].isDirectory() ) {
			String dirName = kids[i].getName();
%><li><a onclick="updateFileDiv('<%= div %>', '<%= path.concat(dirName) %>', '<%= dataFileClass %>', '<%= staticType ? request.getParameter(DataFileServlet.PARAM_DATATYPE) : "" %>')"><%= dirName %></a></li><% } else { break; } i++; } %>
</ul>
</td><td><table class="files">
<tr><td></td><th>File</th><th>Size</th><th>Description</th><% if ( ! staticType ) { %><th>Type</th><% } %></tr>
<% 	
	boolean update = request.getParameter("updateFiles") != null;
	Map<String,String> addedPaths = new HashMap<String,String>();
	Map<String,String> addedTypes = new HashMap<String,String>();

	if ( update && request.getParameter("selFile") != null ) {
		for ( String index: request.getParameterValues("selFile") ) {
			String newPath = request.getParameter(index.concat("_path"));
			String desc = request.getParameter(index.concat("_desc"));
			addedPaths.put(newPath, desc);
			if ( ! staticType ) {
				String type = request.getParameter(index.concat("_type"));
				addedTypes.put(newPath, type);
			}
		}
	}

	
	while ( i < kids.length ) {
		String fileName = kids[i].getName(); 
		String thisPath = path.concat(fileName); 
		String myDesc = fileDesc.get(thisPath);
		String myType = fileType.get(thisPath);
		
		if ( update ) {
			if ( myDesc != null && (! addedPaths.containsKey(thisPath)) ) {
				source.unlinkDataFile(thisPath);
				myDesc = null;
				myType = null;
			} else if ( addedPaths.containsKey(thisPath) ) {
				myDesc = addedPaths.get(thisPath);
				myType = (staticType ? request.getParameter(DataFileServlet.PARAM_DATATYPE) : addedTypes.get(thisPath));
				source.linkDataFile(thisPath, myType, myDesc, null);
			}
		}

		boolean isLinked = myDesc != null;

		String mimeType = null;
		try {
			MagicMatch aMatch = Magic.getMagicMatch(kids[i], true);
			mimeType = aMatch.getMimeType();		
		} catch (MagicMatchNotFoundException e) {
			this.log(String.format("CANNOT determine MIME type for file %s", kids[i].getAbsolutePath()));
		} catch (MagicParseException e) {
			this.log(String.format("CANNOT determine MIME type for file %s", kids[i].getAbsolutePath()));
		} catch (MagicException e) {
			this.log(String.format("CANNOT determine MIME type for file %s", kids[i].getAbsolutePath()));
		} 
		
%><tr class="listing" valign="middle"><th><input type="checkbox" name="selFile" value="<%= i %>" <%= (isLinked ? "checked" : "") %> 
	onclick="this.form.elements['<%= i%>_desc'].disabled = (! this.checked); this.form.elements['<%= i%>_type'].disabled = (! this.checked);">
<% if ( isLinked ) { %><input type="hidden" name="wasSelected" value="<%= thisPath %>"><% } %>
<input type="hidden" name="<%= i %>_path" value="<%= thisPath %>">
</th><td><img src="<%= DataFileServlet.getIconPathForMIMEType(request, mimeType) %>"><%= fileName %></td><td><%= DataFileServlet.humanReadableSize(kids[i].length()) %></td><td style="width: 50%">
<input type="text" style="width:95%" name="<%= i %>_desc" value="<c:out value="<%= myDesc %>"/>" <%= ( isLinked ? "" : "disabled") %>>
<% if ( ! staticType ) {
	out.print("</td><td><select name=\"");
	out.print(i);
	out.print("_type\" ");
	if ( ! isLinked ) 
		out.print("disabled");
	out.print(">");
	for ( Entry<String,String> entry : typeMap.entrySet() ) {
		out.print("<option value=\"");
		out.print(entry.getKey());
		out.print("\"");
		if ( entry.getKey().equals(myType) ) {
			out.print(" selected");
		}
		out.print(">");
		out.print(entry.getValue());
		out.print("</option>");
	}
} %></td></tr>
<% i++; }%>
</table>
</td></tr><% } %>
</table>
</div>
<div align="center">
<!--  
<iframe src="<%= contextPath %>/file/upload?<%= String.format("%s=%s", DataFileServlet.DATAFILE_CLASS, request.getParameter(DataFileServlet.DATAFILE_CLASS)) %>&<%= String.format("%s=%s", DataFileServlet.PARAM_DATATYPE, request.getParameter(DataFileServlet.PARAM_DATATYPE)) %>" 
width="90%" height=70 frameborder=0 scrolling=no></iframe>
-->
<p>Additional files can be to the directory <%= directory.getAbsolutePath() %></p>
</div>
<p align="center"><button type="button" onClick="updateForm(this,'<%= div %>')" NAME='updateFiles'>Update</button><button type="button" onClick="updateForm(this,'<%= div %>')" NAME='cancelBrowser'>Close</button></p>
</form>
<% } } %>