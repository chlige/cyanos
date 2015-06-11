<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="cyanos" tagdir="/WEB-INF/tags" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="edu.uic.orjala.cyanos.web.servlet.MainServlet,
	edu.uic.orjala.cyanos.web.listener.AppConfigListener,
	java.sql.Connection,
	java.sql.ResultSet,
	java.sql.Statement,
	java.sql.PreparedStatement,
	edu.uic.orjala.cyanos.User,
	java.math.BigDecimal,
	java.text.DateFormat" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<cyanos:header title="Cyanos Link Objects"/>
<style type="text/css">
.content { margin: 10px; }
input { background-color: white; border-radius: 0px; }
li { border: 1px solid gray; padding: 2px; cursor: copy; margin: 2px 0px;  }
li:hover { background-color: #ddd; }
ul { list-style: none; padding: 0px; }

div.dragging {
	text-align: center;
	display: block;
	border-radius: 5px;
	padding: 10px;
	margin: 10px;
	background: #cc0;
	border: 2px solid #ff0;
}

div.fileDrop {
	text-align: center;
	display: block;
	border-radius: 5px;
	padding: 10px;
	margin: 10px;
	background: #ddd;
	border: 2px solid #aaa;
}

div.fileStatus {
	text-align: center;
	display: block;
	border-radius: 5px;
	padding: 10px;
	margin: 10px;
	background: #fff;
	border: 2px solid #aaa;
}

#file-button {
	font-weight: bold;
	margin-bottom: 20px;
}

</style><% String contextPath = request.getContextPath(); %>
<script type="text/javascript" src="<%= contextPath %>/tinymce/tinymce.js"></script>
<script type="text/javascript">
function addImage(url, name) {
	top.currentCallback(url, {alt: name});
	top.tinymce.activeEditor.windowManager.close();
}

function dragEnter(e) {
	e.stopPropagation();
	e.preventDefault();
	var preview = document.getElementById("file-details");
	preview.className = "dragging";	
}

function dragEnd(e) {
	e.stopPropagation();
	e.preventDefault();
	var preview = document.getElementById("file-details");
	preview.className = "fileDrop";	
}

function stopEvent(e) {
	e.stopPropagation();
	e.preventDefault();
}

function handleDrop(e) {
	e.stopPropagation();
	e.preventDefault();
	var preview = document.getElementById("file-details");
	var details = document.getElementById("file-info");
	var files = e.dataTransfer.files;

	processFile(files[0]);
	preview.className = "fileDrop";		
}

function processFile(file) {
	var button = document.getElementById("file-button");
	button.innerHTML = "Loading...";
	
	var currstatus = document.getElementById("file-info");
	var http = new XMLHttpRequest();
	http.open("PUT", "<%= contextPath %>/file/upload/" + file.name, true);
	http.onreadystatechange = function() {
		if ( this.readyState == 4 ) {
			if ( this.status == 200) {
				var fileid = this.responseText;
				var url = "<%= contextPath %>/file/uuid/" + fileid;
				addImage(url, file.name);
			} else {
				currstatus.innerHTML = "ERROR(" + this.status + "): " + this.statusText + this.responseText;				
			}
		}
	};
	http.upload.addEventListener("progress", function(e) {
		if (e.lengthComputable) {
			var percentage = Math.round((e.loaded * 100) / e.total);
			currstatus.innerHTML = "Uploading..." + percentage + "%";
		}
	}, false);
	http.send(file);
}

</script>
</head>
<body>
<div class="content">
<% Connection conn = AppConfigListener.getDBConnection(); 
	
	ResultSet results;
	String type = request.getParameter("type");
	boolean image = false;
	if ( type != null ) {
		PreparedStatement sth = conn.prepareStatement("SELECT file,type,description,id,tab,mime_type,CONCAT_WS('/',tab,id,file) FROM data WHERE mime_type LIKE ? ORDER BY file");
		if ( type.equals("image")) {
			image = true;
			sth.setString(1, "image/%");
		}
	
		results = sth.executeQuery();
	} else {
		Statement sth = conn.createStatement();
		results = sth.executeQuery("SELECT file,type,description,id,tab,mime_type FROM data ORDER BY file");
	}
%>
<form method="post" enctype="multipart/form-data">
<input id="files" type="file" name="files" onchange="sendFiles(this.files)" style="display:none"/>
<div id="file-details" class="fileDrop" style="width:80%; margin-left:auto; margin-right:auto;">
<a id="file-button" style="color:black">Upload file</a><br>
Drag and drop file here or 
<a style="color:blue; font-weight:bold;" onclick="document.getElementById('files').click();">Click here</a>
<div id="file-info" style="width=70%; margin-left:auto; margin-right:auto;"></div>
</div>
<script>
var filedrop = document.getElementById("file-details");
filedrop.addEventListener("dragenter", dragEnter, false);
filedrop.addEventListener("dragover", stopEvent, false);
filedrop.addEventListener("drop", handleDrop, false);
filedrop.addEventListener("dragleave", dragEnd, false);
</script>
</form>

<ul>
<% while ( results.next() ) { 
	String url = contextPath.concat("/file/get/").concat(results.getString(7));
	if ( image ) {
		String preview = contextPath.concat("/file/preview/").concat(results.getString(7));
%><li onclick="addImage('<%= url %>','<%= results.getString(3) %>')"><img src="<%= preview %>">
<%= results.getString(1) %>(<%= results.getString(5) %>: <%= results.getString(4) %>)</li>
<%	} else {
%><li onclick="addImage('<%= url %>','<%= results.getString(3) %>')"><%= results.getString(1) %>(<%= results.getString(5) %>: <%= results.getString(4) %>)</li>	
<%	} } %>
</ul>
</div>
</body>
</html>