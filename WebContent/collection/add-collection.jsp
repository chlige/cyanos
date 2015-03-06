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
<cyanos:header title="Add Collection" />
<style>
@media only screen and (min-width: 680px) {
	div.photoDrop {
		display:none;
	}
	h1 {
		text-align: left;
		margin-left: 15px;
	}
	div.content {
		width: 70%;
		margin: 0px auto;
	}
	p.message {
		font-weight: bold;
		font-size: 12pt;
	}
}
	@media only screen and (max-width: 680px) {
		#photo-details a, #photo-details a:visited, #photo-details a:hover {
			text-decoration: none;
			font-weight: bold;
			color: black;
			cursor: pointer;
		}
		div.photoDrop {
			text-align: center;
			background: #eee;
			border: 2px solid black;
			display: inline-block;
			border-radius: 5px;
			padding: 10px;
			margin-left: 125px;
		}
		div.photoDrop {
			display: block;
			margin-left: auto;
			margin-right: auto;
			width: 90%;
			background: #fdfdfd;
			background: -moz-linear-gradient(top, #fdfdfd 0%, #bebebe 100%);
			background: -webkit-gradient(linear, left top, left bottom, color-stop(0%, #fdfdfd),
				color-stop(100%, #bebebe));
			background: -webkit-linear-gradient(top, #fdfdfd 0%, #bebebe 100%);
			background: -o-linear-gradient(top, #fdfdfd 0%, #bebebe 100%);
			background: -ms-linear-gradient(top, #fdfdfd 0%, #bebebe 100%);
			background: linear-gradient(to bottom, #fdfdfd 0%, #bebebe 100%);
			border: 1px solid #bbb;
			border-radius: 10px;
		}
		p.message {
			color: green;
			font-weight: bold;
			text-align: center;
		}
		input[type=file] {
			display: none;
		}
	}
}

div.dragging {
	text-align: center;
	display: block;
	border-radius: 5px;
	padding: 10px;
	background: #ee0;
	border: 2px soild #2f2;
}
</style>
<script>
	var isMobile = {
		Android : function() {
			return navigator.userAgent.match(/Android/i);
		},
		BlackBerry : function() {
			return navigator.userAgent.match(/BlackBerry/i);
		},
		iOS : function() {
			return navigator.userAgent.match(/iPhone|iPad|iPod/i);
		},
		Opera : function() {
			return navigator.userAgent.match(/Opera Mini/i);
		},
		Windows : function() {
			return navigator.userAgent.match(/IEMobile/i);
		},
		any : function() {
			return (isMobile.Android() || isMobile.BlackBerry() || isMobile.iOS() || isMobile.Opera() || isMobile.Windows());
		}
	};
	
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

	function updatePhotos(files) {
		var imageType = /^image\//;
		var preview = document.getElementById("photo-preview");
		var button = document.getElementById("photo-button");
		button.innerHTML = "Loading...";
		preview.innerHTML = "";
		
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
			reader.onload = ( function(aImg, aButton) { aButton.innerHTML = "Change photo"; return function(e) {aImg.src = e.target.result;}; })(img, button);
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
</script>
</head>
<body>
<cyanos:menu/>
<div class='content'>
<h1>Add a Collection</h1>
<hr class="desktop" style="width:80%; margin-left: 0px; margin-bottom: 20px;">
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
				collection.setNotes(req.getParameter("notes"));		%>
<c:set var="colID" value="<%= colID %>"/>		
<p class="message">Collection record <a href="<%= contextPath %>/collection?col=${colID}">${colID}</a> added.</p>
<%				CollectionServlet.addPhotos(collection, req.getUploads("photo"));
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
<label for="colID">Collection ID:</label><input type="text" name="colID" required/><br>
<% String value = format.format(new Date());  %>
<label for="colDate">Collection Date:</label>
<cyanos:calendar-field fieldName="colDate"/><br/>
<% value = userObj.getUserName(); %>
<label for="collector">Collected by:</label><input type="text" name="collector" value="<%= value %>"><br>
<label for="loc_name">Location name:</label><input type="text" name="loc_name" size="40"><br>
<label for="pos_lat">Coordinates:</label><input id="pos_lat" type="text" name="lat" size="10">
<input  id="pos_long" type="text" name="long" size="10">
<script id="currentScript">
if ( navigator.geolocation && isMobile.any() ) {
	navigator.geolocation.getCurrentPosition(updatePosition);
} else { 
	var button = document.createElement("button");
	button.type = "button";
	button.innerHTML = "Use current location";
	button.onclick = function () { navigator.geolocation.getCurrentPosition(updatePosition);};
	var thisScript = document.getElementById("currentScript");
	if ( thisScript.nextSibling ) {
		thisScript.parentNode.insertBefore(button, thisScript.nextSibling);		
	} else {
		thisScript.parentNode.appendChild(button);
	}
}
</script><br>
<label for="project">Project:</label>
<cyanos:project-popup fieldName="project"/><br>
<label class="desktop" for="photo">Photos:</label>
<input id="photo" type="file" accept="image/*" multiple="multiple" capture="camera" name="photo" onchange="updatePhotos(this.files)"/>
<div id="photo-details" class="photoDrop" onclick="document.getElementById('photo').click();">
<a id="photo-button">Add a photo</a><br>
<div id="photo-preview"></div>
</div>
<script>
// var photodrop = document.getElementById("photo-details");
// photodrop.addEventListener("dragenter", dragEnter, false);
// photodrop.addEventListener("dragover", stopEvent, false);
// photodrop.addEventListener("drop", handleDrop, false);
// photodrop.addEventListener("dragleave", dragEnd, false);
</script><br/>
<label for="notes">Notes:</label>
<textarea rows="7" cols="70" name="notes"></textarea><br>
<p><button type="submit" name="addCollection">Add Collection</button></p>
</form>
</div>
</body>
</html>