<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="cyanos" %>
<%@ page import="edu.uic.orjala.cyanos.BasicObject,
	edu.uic.orjala.cyanos.Collection,
	edu.uic.orjala.cyanos.DataFileObject,
	edu.uic.orjala.cyanos.web.servlet.DataFileServlet,
	edu.uic.orjala.cyanos.web.servlet.CollectionServlet,
	edu.uic.orjala.cyanos.sql.SQLCollection,	
	edu.uic.orjala.cyanos.Role,
	edu.uic.orjala.cyanos.web.BaseForm,
	edu.uic.orjala.cyanos.DataException,
	edu.uic.orjala.cyanos.ExternalFile, java.util.HashMap,
	edu.uic.orjala.cyanos.web.listener.AppConfigListener,
	java.io.File, java.util.Map, java.util.Arrays, java.util.Map.Entry, java.util.List, java.util.Set, java.util.TreeSet,
	net.sf.jmimemagic.Magic,
	net.sf.jmimemagic.MagicMatch,
	net.sf.jmimemagic.MagicException,
	net.sf.jmimemagic.MagicMatchNotFoundException,
	net.sf.jmimemagic.MagicParseException" %>
<html>
<head>
<cyanos:header title="Collection photos"/>
<style>
div#dropoverlay {
	width:100%; height: 100%; margin: 0px; 
	visibility: hidden;
	background-color: gray; opacity: 0; 
	position:absolute; z-index:100; color: white; 
	font-size: 16pt; font-weight:bold; line-height:600px;
}

div#dropoverlay > span {
	display:inline-block; vertical-align:middle;
}

div.folders ul {
	list-style: none; padding:0px; margin-top:10px; margin-left:10px;
}

</style>
<script>
	var fileQueue = new Array();

	function dragEnter(e) {
		var preview = document.getElementById("dropoverlay");
		preview.style.opacity = 0.6;
		preview.style.visibility = "visible";
		e.preventDefault();
	}

	function dragStart(e) {
		var preview = document.getElementById("dropoverlay");
		preview.style.opacity = 0.3;
		preview.style.visibility = "visible";
		e.preventDefault();
	}

	function dragEnd(e) {
		var preview = document.getElementById("dropoverlay");
		e.preventDefault();
		preview.style.opacity = 0;
		preview.style.visibility = "hidden";
	}

	function stopEvent(e) {
		e.preventDefault();
	}

	function handleDrop(e) {
		e.preventDefault();

		var data = e.dataTransfer;
		var files = data.files;

		var overlay = document.getElementById("dropoverlay");
		preview.style.opacity = 0;

		var preview = document.getElementById("fileList");
		preview.className = "files";

		for (var i = 0; i < files.length; i++) {
			fileQueue.push(files[i]);
		}

		processQueue();
	}

	function processQueue() {
		if (fileQueue.length > 0) {
			var file = fileQueue.shift();
			var http = new XMLHttpRequest();
			http.open("HEAD", "../file/get" + filePath + file.name, true);
			http.onreadystatechange = function() {
				if (this.readyState == 4) {
					if (this.status == 404) {
						uploadFile(file, file.name);
					} else if (this.status == 200) {
						alert("File already exists!");
					}
				}
			};
			http.send();
		}
	}

	function humanFileSize(bytes, si) {
		var thresh = si ? 1000 : 1024;
		if (Math.abs(bytes) < thresh) {
			return bytes + ' B';
		}
		var units = si ? [ 'kB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB' ] : [
				'KiB', 'MiB', 'GiB', 'TiB', 'PiB', 'EiB', 'ZiB', 'YiB' ];
		var u = -1;
		do {
			bytes /= thresh;
			++u;
		} while (Math.abs(bytes) >= thresh && u < units.length - 1);
		return bytes.toFixed(1) + ' ' + units[u];
	}

	function uploadFile(file, filename) {
		var fullPath = filePath + file.name;

		var preview = document.getElementById("fileList");
		var rows = preview.rows;
		var index = 0;
		var rowIndex = -1;

		for (var i = 0; i < rows.length; i++) {
			var cell = rows[i].cells[rows[i].cells.length - 1];
			var elems = cell.getElementsByTagName("input");

			for (var i = 0; i < elems.length; i++) {
				var elemName = elems[i].name.substr(-4, 4);
				if (elemName = "path") {
					if (elems[i].value > fullPath)
						rowIndex = i;
					break;
				}
			}
			if (rowIndex != -1)
				break;
		}

		var row = rows[rows.length - 1];
		var elems = row.getElementsByTagName("input");

		for (var i = 0; i < elems.length; i++) {
			if (elems[i].name = "selFile") {
				index = elems[i].value;
				break;
			}
		}

		row = preview.insertRow(rowIndex);
		cell = row.insertCell(-1);
		cell.innerHTML = "Loading...";

		var http = new XMLHttpRequest();

		http.open("PUT", "../file/upload" + filePath + filename, true);
		http.onreadystatechange = function() {
			if (this.readyState == 4) {
				if (this.status == 201 || this.status == 200) {
					var img = document.createElement("img");
					img.src = "../file/preview" + filePath + file.name;
					cell.innerHTML = "";
					cell.appendChild(img);
					cell.appendChild(document.createElement("br"));
					cell.appendChild(document.createTextNode(file.name + " ("
							+ humanFileSize(file.size, false) + ")"));

					cell = row.insertCell(-1);
					var input = document.createElement("input");
					input.type = "hidden";
					input.name = index + "_path";
					input.value = fullPath;
					cell.appendChild(input);

					input = document.createElement("input");
					input.type = "checkbox";
					input.name = "selFile";
					input.value = index;

					cell.appendChild(input);
					cell.appendChild(document.createElement("br"));

					input = document.createElement("input");
					input.name = index + "_desc";
					input.type = "text";
					input.style = "width:95%";
					// NEED TO SET EVENT HANDLER TO DISABLE TEXT FIELD
					cell.appendChild(input);
				} else {
					cell.innerHTML = "ERROR" + this.status + "<br>"
							+ this.responseText;
					this.responseText;
				}
				processQueue();
			}
		};

		http.upload.addEventListener("progress", function(e) {
			if (e.lengthComputable) {
				var percentage = Math.round((e.loaded * 100) / e.total);
				cell.innerHTML = file.name + " (" + percentage + "%)";
			}
		}, false);

		http.setRequestHeader("Content-type", file.type);
		http.setRequestHeader("Content-length", file.size);
		http.send(file);
	}
</script>
</head>
<body>
<% String contextPath = request.getContextPath();
	String div = request.getParameter("div");
	Object anObj = SQLCollection.load(DataFileServlet.getSQLData(request), request.getParameter("id"));
	if ( anObj != null && anObj instanceof DataFileObject ) {
	DataFileObject source = (DataFileObject) anObj;
	String dataFileClass = Collection.DATA_FILE_CLASS;
	
	String rootPath = AppConfigListener.getConfig().getFilePath(Collection.DATA_FILE_CLASS, Collection.PHOTO_DATA_TYPE);
	String path = request.getParameter("path");

	File directory = ( path != null ? new File(rootPath, path) : new File(rootPath) );
	if ( path == null ) path = "/"; else if ( ! path.endsWith("/") ) path = path.concat("/");
	String previewURL = contextPath.concat("/file/preview/").concat(dataFileClass).concat("/photo/");
if ( source.isAllowed(Role.WRITE) ) { 
	String colId = source.getID();
%>
<form name="photoBrowser" method="post">
<input type="hidden" name="id" value="<%= source.getID() %>">
<input type="hidden" name="div" value="<%= div %>">
<input type="hidden" name="path" value="<%= path %>">
<script type="text/javascript">
var filePath = "/<%= dataFileClass %>/<%= Collection.PHOTO_DATA_TYPE%>/<%= path %>/";
</script>
<div align="center">
<div style="border: 1px solid gray; background-color: #ddd; text-align:left; margin:5px; margin-bottom: 0px; padding: 5px 0px; width: 98%;"><b style="margin-left: 5px">File Path:</b> 
<% if ( path.length() > 1 ) { String[] paths = path.split("/"); String thisPath = "";%>
<a href="?id=<%= colId %>">ROOT</a>
<% for ( int i = 1; i < paths.length - 1; i++ ) { thisPath = thisPath.concat("/").concat(paths[i]); %>/ <a href="?id=<%= colId %>&path=<%= thisPath %>"><%= paths[i] %></a>
<% } %>/ <%= paths[paths.length - 1] %>
<% } else { %>
ROOT
<% } %></div> 
<div style="height:600px">
<% 	File[] kids = directory.listFiles();
if ( kids != null ) { %>
<div class="folders" style="width:18%; height:600px; margin-left:1%; display: block; overflow:auto; border: 1px solid gray; float:left; position:absolute; text-align:left;">
<ul>
<% int count = 1;
if ( path.length() > 1 ) { String[] paths = path.split("/"); String thisPath = ""; count = paths.length; %>
<li><a href="?id=<%= colId %>">ROOT</a><ul>
<% for ( int i = 1; i < paths.length - 1; i++ ) { thisPath = thisPath.concat("/").concat(paths[i]); %>
<li><a href="?id=<%= colId %>&path=<%= thisPath %>"><%= paths[i] %></a><ul>
<% } %><li><%= paths[paths.length - 1] %>
<% } else { %>
<li>ROOT</li>
<% } %><ul>
<% Arrays.sort(kids, DataFileServlet.directoryFirstCompare());
	
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
%><li><img src="../images/folder.png" height="24px" valign="middle"> <a href="?id=<%= source.getID() %>&path=<%= path.concat(dirName) %>"><%= dirName %></a></li><% 
		} else { break; } i++; } %>
</ul>
<% for ( int c = 0; c < count; c++) { %>
</li></ul>
<% } %>
</div>
<div id="filedrop" style="width:80%; margin-left:19%; height:600px; display: block; overflow:auto; border: 1px solid gray; float:right;position:absolute;">
<div id="dropoverlay">
<span>Drop files to upload</span></div>
<table id="fileList" class="files">
<% 	
	boolean update = request.getParameter("updateFiles") != null;

	Map<String,String> addedPaths = new HashMap<String,String>();
	
	if ( update && request.getParameter("selFile") != null ) {
		for ( String index : request.getParameterValues("selFile") ) {
			String newPath = request.getParameter(index.concat("_path"));
			String desc = request.getParameter(index.concat("_desc"));
			addedPaths.put(newPath, desc);
		}
	}

	while ( i < kids.length ) {
		String fileName = kids[i].getName(); 
		String thisPath = path.concat(fileName); 
		String myDesc = fileDesc.get(thisPath);
		
		if ( update ) {
			boolean linked = addedPaths.containsKey(thisPath);
			if ( myDesc != null && (! linked) ) {
				source.unlinkDataFile(thisPath);
				myDesc = null;
			} else if ( linked ) {
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
%><tr valign="middle"><td style="width:270px; text-align:center; padding:5px 0px;"><img src="<%= previewURL.concat(thisPath) %>"><br>
<%= fileName %> (<%= DataFileServlet.humanReadableSize(kids[i].length()) %>)</td>
<td style="width:10px">
<input type="hidden" name="<%= i %>_path" value="<%= thisPath %>">
<input type="checkbox" name="selFile" value="<%= i %>" <%= (isLinked ? "checked" : "") %> 
	onclick="this.form.elements['<%= i%>_desc'].disabled = (! this.checked);"></td>
<td>Description: <br/>
<input type="text" style="width:90%" name="<%= i %>_desc" value="<c:out value="<%= myDesc %>"/>" <%= ( isLinked ? "" : "disabled") %>>
</td></tr>
<% } i++; } %>
</table></div><% } %>
</div>
</div>
<p align="center"><button type="submit" NAME='updateFiles'>Update</button></p>
<div align="center">
<p>Additional files can be to the directory <%= directory.getAbsolutePath() %></p>
</div>
</form>
<script type="text/javascript">
<!--
var fileDrop = document.getElementById("filedrop");
fileDrop.addEventListener("drag", stopEvent, false);
fileDrop.addEventListener("dragstart", dragStart, false);
fileDrop.addEventListener("dragenter", dragEnter, false);
fileDrop.addEventListener("dragover", stopEvent, false);
fileDrop.addEventListener("drop", handleDrop, false);
fileDrop.addEventListener("dragleave", dragEnd, false);

//-->
</script>
<% } } %>
</body></html>