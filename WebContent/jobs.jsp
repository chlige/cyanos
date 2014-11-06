<%@ taglib prefix="cyanos" tagdir="/WEB-INF/tags" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="edu.uic.orjala.cyanos.web.listener.CyanosSessionListener,
	edu.uic.orjala.cyanos.web.JobManager,
	edu.uic.orjala.cyanos.web.Job,
	edu.uic.orjala.cyanos.web.servlet.UploadServlet,
	java.util.Collection, java.util.Date" %>   
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<cyanos:header title="Cyanos - Job Status"/>
<script type="text/javascript">
function jobStatus(jobID) {
	var xmlHttp = null;
	
	if (window.XMLHttpRequest) {  
		xmlHttp=new XMLHttpRequest();
	} else if (window.ActiveXObject) {
		xmlHttp=new ActiveXObject("Microsoft.XMLHTTP");
  	}
  	
	if (xmlHttp != null) {
 		xmlHttp.open("GET", "status?jobid=" + jobID, false);
 		xmlHttp.send(null);
 		if (xmlHttp.readyState == 4 && xmlHttp.status == 200) {
 			var jobDIV = document.getElementById('job-' + jobID);
 			var subAreas = jobDIV.getElementsByTagName("div")
 			var progText = jobDiv.getElementsByClassName("progressText")[0];
			var response = xmlHttp.responseText;
			if ( response === "DONE" ) {
				endProgress(100, resultButton);
				if ( progText != null ) {
					progText.innerHTML = "Complete";
				}
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
				if ( length < 0 )
					elem.style.width = "100%";
				else 
					elem.style.width = length + "%";
				if ( progText != null ) {
					if ( length < 0 ) 
						progText.innerHTML = "Running...";
					else 
						progText.innerHTML =  length.toFixed(0) + "%";
				}
				if ( count == 360 ) { count = 0; }
				count += 15;
				window.setTimeout(jobStatus, 50, jobID);
			}
		} 
  	} 
}
</script>
<style type="text/css">
.job { margin-left:auto; margin-right:auto; width: 600px; border: 2px solid gray; background-color: #eee; padding: 10px; display: block; }
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

#progressBar {
  position: absolute;
  top: 0;
  left: 0;
  height: 100%;
  background: #FFCC00;
}

#progressText {
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
<cyanos:menu/>
<% JobManager manager = CyanosSessionListener.getJobManager(session); 
if ( request.getParameter("jobid") != null ) { 
	Job job = manager.getJob(request.getParameter("jobid"), UploadServlet.getSQLData(request));
	if ( job != null ) {
%><h1>Job <%= job.getID() %> - <i><%= job.getType() %></i></h1>
<p align="center">Started: <%= UploadServlet.DATETIME_FORMAT.format(job.getStartDate()) %><br>
Ended: <% Date endDate = job.getEndDate(); if ( endDate != null ) { %><%= UploadServlet.DATETIME_FORMAT.format(endDate) %><% } %><br>
<% if ( job.getOutput() != null ) { %>Show output<% } %></p>
<div class="collapseSection" style="background:white;"><a name='job_messages' class='twist' onClick='loadDiv("messages")'>
<img align="absmiddle" id="twist_messages" src="<%= request.getContextPath() %>/images/twist-closed.png" /> Messages</a>
<div class="hideSection" id="div_messages"><%= job.getMessages() %></div></div>
<%--  
<div class="collapseSection" style="background:white;"><a name='job_output' class='twist' onClick='loadDiv("output")'>
<img align="absmiddle" id="twist_output" src="<%= request.getContextPath() %>/images/twist-closed.png" /> Output</a>
<div class="hideSection" id="div_output"><%= job.getOutput() %></div></div>
--%>
<% } else { %>
<h1>Job <%= request.getParameter("jobid") %></h1>
<p align="center">Job not found</p>
<% } } else { %>
<h1>Active Jobs</h1>
<% 	Collection<Job> jobList = manager.getActiveJobs();
	if ( jobList.size() > 0  ) { 
		for ( Job job : jobList ) { 
%><div id="job-<%= job.getID()  %>" class="job"><h3>Job <%= job.getID() %> <i><%= job.getType() %></i></h3>
Started: <%= job.getStartDate().toString() %><br>
Ended: <% Date endDate = job.getEndDate(); if ( endDate != null ) {%><%= endDate.toString() %><% } %><br>
<div class="progress" style="width: 200px"><div class="progressText"></div><div class="progressBar"></div></div>
<a href="jobs.jsp?jobid=<%= job.getID() %>" style="margin-left:auto; margin-right:auto; text-align:center">Show Results</a>
<script>
	var updatePath = "<%= request.getContextPath() %>/upload/status";
	uploadStatus(updatePath, document.getElementById("resultButton"));
</script>
</div>
<% } 
	} else { %><p style="text-align:center; font-weight:bold;">No Active Jobs</p>
<% } } %></body>
</html>