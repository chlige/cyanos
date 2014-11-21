<%@ taglib prefix="cyanos" tagdir="/WEB-INF/tags" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="edu.uic.orjala.cyanos.web.listener.CyanosSessionListener,
	edu.uic.orjala.cyanos.web.JobManager,
	edu.uic.orjala.cyanos.web.Job,
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
 			var progText = jobDiv.querySelector("#progressText");
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
				var elem = jobDIV.querySelector('#progressBar');
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
</head>
<body>
<cyanos:menu/>
<h1>Job Status</h1>
<% JobManager manager = CyanosSessionListener.getJobManager(session); 
	Collection<Job> jobList = manager.getActiveJobs();
	if ( jobList.size() > 0  ) { 
		for ( Job job : jobList ) { %>
<div id="job-<%= job.getID()  %>">Job <%= job.getID() %> <i><%= job.getType() %></i><br>
Started: <%= job.getStartDate().toString() %><br>
Ended: <% Date endDate = job.getEndDate(); if ( endDate != null ) {%><%= endDate.toString() %><% } %><br>
<div class="progress" style="width: 200px"><div id="progressText"></div><div id="progressBar"></div></div>
<form><button id="resultButton" name="showResults" disabled>Show Results</button></form>
</div>
<script>
	var updatePath = "<%= request.getContextPath() %>/upload/status";
	uploadStatus(updatePath, document.getElementById("resultButton"));
</script>
</div>
<% } } else { %>
<p style="text-align:center; font-weight:bold;">No Active Jobs</p>
<% } %>
</body>
</html>