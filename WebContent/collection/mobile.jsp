<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="cyanos" %>
<%@ page buffer="12kb" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.Collection,
	edu.uic.orjala.cyanos.User,
	edu.uic.orjala.cyanos.Project,
	edu.uic.orjala.cyanos.web.servlet.CollectionServlet,
	edu.uic.orjala.cyanos.web.MultiPartRequest,
	edu.uic.orjala.cyanos.sql.SQLCollection,
	edu.uic.orjala.cyanos.DataException,
	java.util.Date, java.util.List,
	java.text.SimpleDateFormat" %>
<% 	String contextPath = request.getContextPath(); 
	User userObj = CollectionServlet.getUser(request); 
	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
%><!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<cyanos:header title="Add Collection"/>
<meta name=viewport content="width=device-width">
<style>
body {
	background-color: #ccc;
}
input[type=text], input[type=url], input[type=email], input[type=password], input[type=tel] {
  -webkit-appearance: none; -moz-appearance: none;
  display: block;
  margin: 0;
  width: 100%; height: 30px;
  line-height: 40px; font-size: 17px;
  border: 1px solid #bbb;
}

a, a:visited, a:hover { 
  text-decoration:none;
  font-weight: bold;
  color: black;
  cursor: pointer;
}

div.photoDrop {
	text-align:center; 
	background:#fff; 
	display:block; 
	border:2px solid #222; 
	border-radius:5px; 
	padding:10px;
}

div.dragging {
	text-align:center; 
	display:block; 
	border-radius:5px; 
	padding:10px;
	background: #ee0;
	border: 2px soild #2f2;
}

textarea {
  width: 100%; height: 100px; font-size: 10px;
  display: block;
  margin: 0;
  line-height: 40px;
  border: 1px solid #bbb;
}

button {
 -webkit-appearance: none; -moz-appearance: none;
 display: block;
 margin: 1.5em 0;
 font-size: 1em; line-height: 2.5em;
 color: #333;
 font-weight: bold;
 height: 2.5em; width: 100%;
 background: #fdfdfd; background: -moz-linear-gradient(top, #fdfdfd 0%, #bebebe 100%); 
 background: -webkit-gradient(linear, left top, left bottom, color-stop(0%,#fdfdfd), color-stop(100%,#bebebe)); 
 background: -webkit-linear-gradient(top, #fdfdfd 0%,#bebebe 100%); 
 background: -o-linear-gradient(top, #fdfdfd 0%,#bebebe 100%); 
 background: -ms-linear-gradient(top, #fdfdfd 0%,#bebebe 100%); 
 background: linear-gradient(to bottom, #fdfdfd 0%,#bebebe 100%);
 border: 1px solid #bbb;
 -webkit-border-radius: 10px; -moz-border-radius: 10px; border-radius: 10px;
}
</style>
</head>
<body>
<div class='content'>
<h1>Add Collection</h1>
<%
	MultiPartRequest req = MultiPartRequest.genRequest(request);
	if (req.getParameter("addCollection") != null) {
		String colID = req.getParameter("colID");
		String projectID = req.getParameter("project");
		Collection collection = null;
		try {
			if (projectID != null && projectID.length() > 0) {
				collection = SQLCollection.createInProject(CollectionServlet.getSQLData(req), colID, projectID);
			} else if (colID != null) {
				collection = SQLCollection.create(CollectionServlet.getSQLData(req), colID);
			}

/*			out.print("Upload count:");
			out.print(req.getUploadCount("photo"));
			out.println("<br/>");
	
			Object fileList = session.getAttribute(CollectionServlet.FILE_QUEUE);
			if ( fileList instanceof List ) {
				out.print("Queued files:");
				out.print(((List)fileList).size());
				out.println("<br/>");
			}
*/			
			
			if (collection != null && collection.first()) {
				collection.setCollector(req.getParameter("collector"));
				collection.setLocationName(req.getParameter("loc_name"));
				collection.setDate(req.getParameter("colDate"));
				collection.setLatitude(req.getParameter("lat"));
				collection.setLongitude(req.getParameter("long"));
				collection.setNotes(req.getParameter("notes"));				
				out.println("<p align='center' style='color:green; font-weight:bold'>Collection record added.</p>");
				CollectionServlet.addPhotos(collection, req.getUploads("photo"));
			}
		} catch (DataException e) {
			out.print("<p align='center'><b>Error: ");
			out.print(e.getMessage());
			out.print("</b></p>");
		}
	}
%>
<form method="post" enctype="multipart/form-data">
<input type="hidden" name="form" value="add">
<label for="colID">Collection ID</label><input type="text" name="colID" required/><br>
<% String value = format.format(new Date());  %>
<label for="colDate">Collection Date:</label><input type="date" name="colDate" value="<%= value %>" placeholder="YYYY-MM-DD"><br/>
<% value = userObj.getUserName(); %>
<label for="collector">Collected by</label><input type="text" name="collector" value="<%= value %>"><br>
<label for="loc_name">Location name</label><input type="text" name="loc_name" size="40"><br>
<label for="pos_lat">Coordinates</label><input id="pos_lat" type="text" name="lat" size="10">
<input  id="pos_long" type="text" name="long" size="10">
<script>
function updatePosition (position) { 
	var field = document.getElementById("pos_lat");
	field.value = formatPosition(position.coords.latitude, "N", "S");
	field = document.getElementById("pos_long");
	field.value = formatPosition(position.coords.longitude, "E", "W");
}

function formatPosition(position, posHemi, negHemi) {
	var hemi = ( position < 0 ? negHemi : posHemi );
	position = Math.abs(position);
	degrees = Math.floor(position);
	minutes = ( position - degrees) * 60;
	return hemi + " " + degrees.toFixed(0) + " " + minutes.toFixed(3) + "'";
}


if ( navigator.geolocation ) {
	navigator.geolocation.getCurrentPosition(updatePosition);
}

function updatePhotos(files) {
	var imageType = /^image\//;
	var preview = document.getElementById("photo-details");
	for ( var i = 0; i < files.length; i++ ) {
		if (!imageType.test(files[i].type) ) {
			continue;
		}	
		var img = document.createElement("img");
		img.classList.add("obj");
		img.file = files[i];
		img.width = 100; 
		preview.appendChild(img);	
		
		var reader = new FileReader();
		reader.onload = ( function(aImg) { return function(e) {aImg.src = e.target.result;}; })(img);
		reader.readAsDataURL(files[i]);
	}
}

function dragEnter(e) {
	e.stopPropagation();
	e.preventDefault();
	var preview = document.getElementById("photo-details");
	preview.className = "dragging";	
}

function dragEnd(e) {
	e.stopPropagation();
	e.preventDefault();
	var preview = document.getElementById("photo-details");
	preview.className = "photoDrop";	
}

function stopEvent(e) {
	e.stopPropagation();
	e.preventDefault();
}

function handleDrop(e) {
	e.stopPropagation();
	e.preventDefault();
	
	var data = e.dataTransfer;
	var files = data.files;
	
	var preview = document.getElementById("photo-details");
	preview.className = "photoDrop";		
	updatePhotos(files);	
	for ( file in files ) {
		var http = new XMLHttpRequest();
		http.open("PUT", "../collection/upload/" + file.name, true);
		http.onreadystatechange = function() {
			if ( http.readyState == 4 && xhr.status == 200) {
				// Should do something about it.
			}
		};
		http.setRequestHeader("Content-type", file.type);
		http.setRequestHeader("Content-length", file.size);
		http.send(file);
	}
}

</script><br>
<label for="project">Project</label>
<cyanos:project-popup fieldName="project"/><br>
<label for="notes">Notes</label>
<textarea rows="7" cols="70" name="notes"></textarea><br>
<input id="photo" type="file" accept="image/*" multiple="multiple" capture="camera" name="photo" style="display:none;" onchange="updatePhotos(this.files)"/>
<div id="photo-details" class="photoDrop">
<a onclick="document.getElementById('photo').click();">Add a photo</a><br>
</div>
<!--  
<script>
var photodrop = document.getElementById("photo-details");
photodrop.addEventListener("dragenter", dragEnter, false);
photodrop.addEventListener("dragover", stopEvent, false);
photodrop.addEventListener("drop", handleDrop, false);
photodrop.addEventListener("dragleave", dragEnd, false);
</script>
-->
<button type="submit" name="addCollection">Create</button>
</form>
</div>
</body>
</html>
