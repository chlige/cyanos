<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.sql.SQLCompound,
	edu.uic.orjala.cyanos.web.servlet.DereplicationServlet,
	java.io.BufferedReader, java.io.StringReader, edu.uic.orjala.cyanos.web.Job" %>
<% 	String contextPath = request.getContextPath(); %> 
<table width="75%" align="center" class="dashboard"><tbody>
<tr><th class="header">Name</th><th class="header">Formula</th><th class="header">Notes</th><th class="header">Links</th>
</tr>	
<% 	Object job = request.getAttribute("derep-job");
	if ( job == null && request.getParameter("jobid") != null ) {
		job = DereplicationServlet.getJobManager(session).getJob(request.getParameter("jobid"), DereplicationServlet.getSQLData(request));
	}
if ( job instanceof Job ) {
BufferedReader reader = new BufferedReader(new StringReader(((Job)job).getOutput())); 
String line = reader.readLine();
line = reader.readLine();
while ( line != null ) {
	int lineLen = line.length();
	String[] cols = line.split(",", 5);
%><tr class="banded" style="text-align:center"><td><a href="<%= contextPath %>/compound?id=<%= cols[0] %>"><%= cols[1] %></a></td>
<td><%= SQLCompound.getHTMLFormula(cols[2]) %></td>
<td><%= cols[4].substring(1, cols[4].length() - 1) %></td>
<td><% if ( cols[3].length() > 1 && (! cols[3].equals("null")) ) { %>
<a class="chemspider" href="http://www.chemspider.com/Search.aspx?q=<%= cols[3] %>" target="_blank"><img src="<%= contextPath %>/images/icons/cs-icon.png" valign="middle"> Search ChemSpider</a><% } %></td>
</tr><% line = reader.readLine(); } }%>
</table>



