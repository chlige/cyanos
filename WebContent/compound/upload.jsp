<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="cyanos" %>
<%@ page import="edu.uic.orjala.cyanos.web.servlet.UploadServlet,
	edu.uic.orjala.cyanos.web.upload.CompoundUpload,
	edu.uic.orjala.cyanos.web.listener.CyanosRequestListener,
	edu.uic.orjala.cyanos.web.Job,
	edu.uic.orjala.cyanos.User,
	edu.uic.orjala.cyanos.Role,
	edu.uic.orjala.cyanos.web.Sheet,
	edu.uic.orjala.cyanos.web.MultiPartRequest,
	edu.uic.orjala.cyanos.web.MultiPartRequest.FileUpload" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="cyanos"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<cyanos:header title="Cyanos - Compound Data Upload"/>
<script type="text/javascript">
function jobStatus(jobID) {
	var xmlHttp = null;
	
	if (window.XMLHttpRequest) {  
		xmlHttp=new XMLHttpRequest();
	} else if (window.ActiveXObject) {
		xmlHttp=new ActiveXObject("Microsoft.XMLHTTP");
  	}
  	
	if (xmlHttp != null) {
 		xmlHttp.open("GET", "upload/status?jobid=" + jobID, false);
 		xmlHttp.send(null);
 		if (xmlHttp.readyState == 4 && xmlHttp.status == 200) {
 			var jobDIV = document.getElementById('job-' + jobID);
 			var subAreas = jobDIV.getElementsByTagName("div")
 			var progText = jobDIV.getElementsByClassName("progressText")[0];
			var response = xmlHttp.responseText;
			if ( response === "DONE" ) {
				jobDiv.innerHTML = "<p align='center'>Loading results...</p>";
				window.setTimeout(displayResults, 50, jobID);
			} else if ( response === "ERROR" ) { 
				endProgress(progressLen, resultButton);
				if ( progText != null ) {
					progText.innerHTML = "<font color='red'>ERROR!</font>";
				}						
			} else if ( response === "STOP" ) {
				endProgress(progressLen, resultButton);
				if ( progText != null ) {
					progText.innerHTML = "Stopped";
				}				
			} else {
				var length = Number(response);
				var elem = jobDIV.getElementsByClassName('progressBar')[0];
				elem.style.opacity = (0.3 *  Math.sin((count / 180) * Math.PI)) + 0.7 ;
				elem.style.width = "100%";
				progText.innerHTML = "Running...";
				if ( count == 360 ) { count = 0; }
				count += 15;
				window.setTimeout(jobStatus, 50, jobID);
			}
		} 
  	}
}

function displayResults(jobID) {
	var xmlHttp = null;
	
	if (window.XMLHttpRequest) {  
		xmlHttp=new XMLHttpRequest();
	} else if (window.ActiveXObject) {
		xmlHttp=new ActiveXObject("Microsoft.XMLHTTP");
  	}
  	
	if (xmlHttp != null) {
 		xmlHttp.open("GET", "dereplication?jobid=" + jobID, false);
 		xmlHttp.send(null);
 		if (xmlHttp.readyState == 4 && xmlHttp.status == 200) {
 			var jobDIV = document.getElementById('job-' + jobID);
			jobDiv.innerHTML = xmlHttp.responseText;
		} 
  	}	
}
</script>
<style type="text/css">
.job { margin-left:auto; margin-right:auto; margin-bottom: 10px; width: 600px; border: 2px solid gray; background-color: #eee; padding: 10px; display: block; }
.job h3 { font-size: 12pt; font-weight: bold; padding: 0px; margin: 0px; }

.progress {
  -moz-border-radius:5px;
  -webkit-border-radius:5px;
  -moz-box-shadow:black 0 0 4px;
  -webkit-box-shadow:black 0 0 4px;
  border: 2px solid #101010;
  position: relative;
  width:50px;
  height:20px;
  margin-left: auto;
  margin-right: auto;
}

.progressBar {
  position: absolute;
  top: 0;
  left: 0;
  height: 100%;
  background: #FFCC00;
}

.progressText {
  position: absolute;
  top: 0;
  left: 0;
  height: 100%;
  width: 100%;
  z-index: 10;
  text-align: center;
  font-size: 12pt;
  font-weight: bold;
}
</style>
</head>
<body>
<cyanos:menu helpModule="collection"/>
<h1>Compound Data Upload</h1>
<div class="content">
<% 	
	HttpServletRequest mpReq = MultiPartRequest.parseRequest(request);
	if ( mpReq instanceof MultiPartRequest && mpReq.getParameter(UploadServlet.PARSE_ACTION) != null ) {
	if ( session.getAttribute("upload-job") != null ) { %>
<p style="text-align: center; color: red; font-weight:bold">ERROR: Cannot start upload job.  Current upload job running.</p>		
<%	} else {
		CompoundUpload job = new CompoundUpload(UploadServlet.newSQLData(request));
		FileUpload upload = ((MultiPartRequest)mpReq).getUpload("sdfile", 0);
		job.startJob(request, upload.getStream());
		UploadServlet.addJob(session, job); 
%><div id="job-<%= job.getID()  %>">
<div class="progress" style="width: 200px"><div class="progressText"></div><div class="progressBar"></div></div>
<script>jobStatus("<%= job.getID() %>");</script>
</div>
<% } } else { 
%><form method="post" enctype="multipart/form-data">
<p align="center"><b>SDFile to upload: </b>
<input type="file" name="sdfile" size="25"/></p>
<p align="center"><b>Upload Instructions</b></p>
<ul style="margin-left:50px;">
<li>Save compound data as SDFile (v2000 or v3000)</li>
<li>Include attribute/property for compound ID.</li>
<li>Include additional attributes/properties for compound name, associated notes, or project ID.<br>  
Use the following form to designate the property name (in the SDFile) with the associated compound attribute for the CYANOS database.</li>
</ul>
<p align="center">
<input type="checkbox" name="forceUpload" value="true" <%= ( request.getParameter(CompoundUpload.FORCE_UPLOAD) != null ? "checked" : "" ) %>> Force upload.<br> i.e. Overwrite existing compound information.
</p>
<table class="uploadForm">
<tr><td>Compound ID Property:</td><td><input type="text" name="<%= CompoundUpload.COMPOUND_ID %>"></td></tr>
<tr><td>Name Property:</td><td><input type="text" name="<%= CompoundUpload.NAME_PROPERTY %>"></td></tr>
<tr><td>Notes Property:</td><td><input type="text" name="<%= CompoundUpload.NOTES_PROPERTY %>"></td></tr>
<tr><td>Project Property:</td><td><input type="text" name="<%= CompoundUpload.PROJECT_PROPERY %>"> Default Project: <cyanos:project-popup fieldName="<%= CompoundUpload.STATIC_PROJECT %>"/></td></tr>
</table>
<p align="center"><button type="submit" name="<%= UploadServlet.PARSE_ACTION %>">Upload & Parse</button><button type="reset">Clear Form</button></p>
</form>
<% } %></div>
</body>
</html>