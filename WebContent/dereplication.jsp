<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="cyanos" %>
<%@ page import="edu.uic.orjala.cyanos.Compound,
	edu.uic.orjala.cyanos.User, edu.uic.orjala.cyanos.Role,
	edu.uic.orjala.cyanos.web.servlet.DereplicationServlet,
	edu.uic.orjala.cyanos.web.servlet.CompoundServlet,
	edu.uic.orjala.cyanos.web.job.RebuildCompoundGraphJob,
	edu.uic.orjala.cyanos.web.job.DereplicationQuery,
	edu.uic.orjala.cyanos.web.Job,
	edu.uic.orjala.cyanos.xml.XMLCompound,
	java.io.StringReader,
	java.util.Collection" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<cyanos:header title="Cyanos - Compound Dereplication"/>
<script type="text/javascript">
var count = 0;

	function jobStatus(jobID) {
		var xmlHttp = null;

		if (window.XMLHttpRequest) {
			xmlHttp = new XMLHttpRequest();
		} else if (window.ActiveXObject) {
			xmlHttp = new ActiveXObject("Microsoft.XMLHTTP");
		}

		if (xmlHttp != null) {
			xmlHttp.open("GET", "upload/status?jobid=" + jobID, true);
			xmlHttp.onreadystatechange = function() {
				if (xmlHttp.readyState == 4 && xmlHttp.status == 200) {
					var jobDIV = document.getElementById('job-' + jobID);
					var subAreas = jobDIV.getElementsByTagName("div")
					var progText = jobDIV.getElementsByClassName("progressText")[0];
					var response = xmlHttp.responseText;
					if (response === "DONE") {
						jobDIV.innerHTML = "<p align='center'>Loading results...</p>";
						displayResults(jobID);
					} else if (response === "ERROR") {
						endProgress(progressLen, resultButton);
						if (progText != null) {
							progText.innerHTML = "<font color='red'>ERROR!</font>";
						}
					} else if (response === "STOP") {
						displayResults(jobID);
						if (progText != null) {
							progText.innerHTML = "Stopped";
						}
					} else {
						var length = Number(response);
						var elem = jobDIV.getElementsByClassName('progressBar')[0];
						elem.style.opacity = (0.3 * Math.sin((count / 180) * Math.PI)) + 0.7;
						elem.style.width = "100%";
						progText.innerHTML = "Running...";
						if (count == 360) {
							count = 0;
						}
						count += 15;
						window.setTimeout(jobStatus, 50, jobID);
					}
				}
			};

			xmlHttp.send(null);

		}
	}


	function displayResults(jobID) {
		var xmlHttp = null;

		if (window.XMLHttpRequest) {
			xmlHttp = new XMLHttpRequest();
		} else if (window.ActiveXObject) {
			xmlHttp = new ActiveXObject("Microsoft.XMLHTTP");
		}

		if (xmlHttp != null) {

			xmlHttp.open("GET", "dereplication?jobid=" + jobID, true);
			xmlHttp.onreadystatechange = function() {
				if (xmlHttp.readyState == 4 && xmlHttp.status == 200) {
					var jobDIV = document.getElementById('job-' + jobID);
					jobDIV.innerHTML = xmlHttp.responseText;
				}
			};
			xmlHttp.send(null);
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
<cyanos:menu helpModule="derplication"/>
<h1>Compound Dereplication</h1>
<hr width="90%">
<% String contextPath = request.getContextPath();
	if (request.getParameter("rebuildGraph") != null && DereplicationServlet.getUser(request).isAllowed(User.SAMPLE_ROLE, User.GLOBAL_PROJECT, Role.CREATE) ) { 
		RebuildCompoundGraphJob job = new RebuildCompoundGraphJob(DereplicationServlet.newSQLData(request));
		job.startJob(); 
		DereplicationServlet.addJob(session, job);
%><p align="center"><i>Rebuilding chemical structure index</i><br><a href="<%= request.getContextPath() %>/jobs.jsp">View status</a></p>
<% } else if (request.getParameter("listPrevJobs") != null ) {
		Collection<Job> jobList = DereplicationQuery.previousQueries(DereplicationServlet.getSQLData(request));
%><h2 style="text-align:center">Previous Dereplication Queries</h2><ul>
<% for (Job job : jobList ) { %>
<li><a href="?jobid=<%= job.getID() %>">Job #<%= job.getID() %></a> - <%= DereplicationServlet.DATETIME_FORMAT.format(job.getStartDate()) %></li>
<% } %></ul>
<% } else if (request.getParameter("jobid") != null ) {
   		Job queryJob = Job.loadJob(DereplicationServlet.getSQLData(request), request.getParameter("jobid"));
%><h2 style="text-align:center">Previous Dereplication Query</h2>
<p align="center">Job #<%= queryJob.getID() %> - <%= DereplicationServlet.DATETIME_FORMAT.format(queryJob.getStartDate()) %></p> <%
   		if ( queryJob.getOutputType().equals("compound-xml") ) { 
   			request.setAttribute(CompoundServlet.COMPOUND_RESULTS, XMLCompound.load(new StringReader(queryJob.getOutput())));
%><jsp:include page="/compound/compound-list.jsp" /><% 
   		} else {
 %><p style="font-weight:bold; text-align:center;"><%= queryJob.getOutput() %></p><%  			
   		}
  } else {
	boolean performSearch = request.getParameter(DereplicationServlet.SEARCH_ACTION) != null;
%><div class="searchNav"><a class='twist' onClick='loadDiv("search")'>
<form method="post">
<img align="absmiddle" id="twist_search" src="<%= contextPath %>/images/twist-<%= performSearch ? "closed" : "open" %>.png" /> Search Form</a>
<div class="<%= performSearch ? "hide" : "show" %>Section" id="div_search">
<jsp:include page="/dereplication/ms-derep.jsp"/>
<jsp:include page="/dereplication/nmr-derep.jsp"/>
</div>
<p align="center"><button type="submit" name="<%= DereplicationServlet.SEARCH_ACTION %>">Perform Search</button><button type="reset">Reset Form</button></p>
</form>
</div>
<% if ( performSearch ) { 
	DereplicationQuery search = DereplicationServlet.startDereplicationQuery(request);
%><div style="margin-left:50px; margin-right:50px;"><div class="showSection" id="showSQL">
<p align="CENTER"><a onclick="showHide('hideSQL','showSQL')">Show SQL WHERE Statement</a></p></div>
<div class="hideSection" id="hideSQL"><p align="CENTER" style="border: 1px solid gray">
<code>SELECT DISTINCT compound.* FROM compound <%= search.buildQuery() %></code></p>
<p align="CENTER"><a onclick="showHide('showSQL','hideSQL')">Hide SQL WHERE Statement</a></p></div></div>
<div id="job-<%= search.getID()  %>">
<div class="progress" style="width: 200px"><div class="progressText"></div><div class="progressBar"></div></div>
<script>jobStatus("<%= search.getID() %>");</script>
</div>
<% } else { %><p align="center"><a href="?listPrevJobs">List previous dereplication jobs</a></p><% } } %>
</body>
</html>