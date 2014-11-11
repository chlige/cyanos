<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="cyanos" %>
<%@ page import="edu.uic.orjala.cyanos.Compound,
	edu.uic.orjala.cyanos.User, edu.uic.orjala.cyanos.Role,
	edu.uic.orjala.cyanos.web.servlet.DereplicationServlet,
	edu.uic.orjala.cyanos.web.servlet.CompoundServlet,
	edu.uic.orjala.cyanos.web.job.RebuildCompoundGraphJob" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<cyanos:header title="Cyanos - Compound Dereplication"/>
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
<% } else { 
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
	String sqlString = DereplicationServlet.buildQuery(request); 
%><div style="margin-left:50px; margin-right:50px;"><div class="showSection" id="showSQL">
<p align="CENTER"><a onclick="showHide('hideSQL','showSQL')">Show SQL WHERE Statement</a></p></div>
<div class="hideSection" id="hideSQL"><p align="CENTER" style="border: 1px solid gray"><code>SELECT DISTINCT compound.* FROM compound <%= sqlString 
%></code></p><p align="CENTER"><a onclick="showHide('showSQL','hideSQL')">Hide SQL WHERE Statement</a></p></div></div>
<% request.setAttribute(CompoundServlet.COMPOUND_RESULTS, DereplicationServlet.getCompounds(request)); %>
<jsp:include page="/compound/compound-list.jsp"/>
<% } } %>
</body>
</html>