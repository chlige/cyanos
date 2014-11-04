<%@ taglib prefix="cyanos" tagdir="/WEB-INF/tags" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.Sample,
	edu.uic.orjala.cyanos.Strain,
	edu.uic.orjala.cyanos.Material,
	edu.uic.orjala.cyanos.DataException,
	edu.uic.orjala.cyanos.SampleCollection,
	edu.uic.orjala.cyanos.sql.SQLSampleCollection,
	edu.uic.orjala.cyanos.web.servlet.SampleServlet,
	java.text.SimpleDateFormat, java.util.List" %>
<%  String contextPath = request.getContextPath();
	SimpleDateFormat dateFormat = (SimpleDateFormat) session.getAttribute("dateFormatter"); 
	SampleCollection collObj = (SampleCollection) request.getAttribute(SampleServlet.COLLECTION_ATTR);
%><!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<cyanos:header title="Cyanos - Sample Collections"/>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<script language="JAVASCRIPT" src="cyanos.js"></script>
<link rel="stylesheet" type="text/css" href="cyanos.css"/></head>
<body style="min-height:100%">
<cyanos:menu helpModule="sample"/>

<div class='content' style="padding-bottom: 60px;"><%
if ( collObj != null && collObj.first() ) { 
%><p align="CENTER"><font size="+3" >Sample Collection: <%= collObj.getID() %></font></p>
<hr width="90%">
<div id="<%= SampleServlet.DIV_COLLECTION_INFO_FORM_ID %>">
<jsp:include page="/sample/collection-form.jsp" />
</div><%
	Sample sampleList = collObj.getSamples(); 
	boolean isBox = collObj.isBox(); 
%><div CLASS="collapseSection"><A NAME='sep_sampleList' CLASS='twist' onClick='loadDiv("sampleList")' CLASS='divTitle'>
<img align="ABSMIDDLE" ID="twist_sampleList" SRC="<%= contextPath %>/images/twist-closed.png" /> Sample List</A>
<div CLASS="hideSection" ID="div_sampleList"><%
if ( sampleList.first() ) {
		sampleList.beforeFirst();
		boolean oddRow = true; 
%><table  class="dashboard" >
<tr><th class="header" width='100'>Name</th><th class="header" width='100'>Parent Material</th><th class="header" width='200'>Strain</th>
<th class="header" width='150'>Date</th><%
if ( isBox ) { %><th class="header" width="70">Location</th><% } %><th class="header" width="70">Balance</th></tr><%
while ( sampleList.next() ) { 
	try {
		String rowFormat = ( oddRow ? "odd" : "even" ); oddRow = ! oddRow;
		Material parent = sampleList.getParentMaterial();
		Strain myStrain = parent.getCulture(); 
%><tr class="<%= rowFormat %>" align='center'>
<td><a href="<%= contextPath %>/sample?id=<%= sampleList.getID() %>"><%= sampleList.getName() %></a></td>
<td><a href="<%= contextPath %>/material?id=<%= parent.getID() %>"><%= parent.getLabel() %></a></td>
<td><a href="<%= contextPath %>/strain?id=<%= myStrain.getID() %>"><%= myStrain.getID() %> <i><%= myStrain.getName() %></i></a></td>
<td><%= dateFormat.format(sampleList.getDate()) %></td><%
if ( isBox ) { %><td><%= sampleList.getLocation() %></td><% } 
%><td><%= SQLSampleCollection.autoFormatAmount(sampleList.accountBalance(), SQLSampleCollection.MASS_TYPE) %></td></tr><%
	} catch (DataException e) {
		out.print("<tr><th colspan=5>ERROR: ");
		out.print(e.getMessage());
		out.println("</th></tr>");
	}
	} 
%></table><%
} else { 
%><hr width="85%"/>
<p align='center'><b>No Results</b></p><%
} 
%></div></div><%
	if ( isBox ) { 
%><div CLASS="collapseSection"><A NAME='sep_sampleBox' CLASS='twist' onClick='loadDiv("sampleBox")' CLASS='divTitle'>
<img align="ABSMIDDLE" ID="twist_sampleBox" SRC="<%= contextPath %>/images/twist-closed.png" /> Box View</A>
<div CLASS="hideSection" ID="div_sampleBox"><%

if ( sampleList.first() ) {
		sampleList.beforeFirst();
		int width = collObj.getWidth();
		String emptyImg = contextPath.concat("/images/empty.png");
		String filledImg = contextPath.concat("/images/filled.png"); 

%><table ><tr><th></th><%
for ( int i = 1; i <= width; i++ ) { 
	out.print("<th>");
	out.print(i);
	out.print("</th>");
}

	int thisCol = width + 1;
	int thisRow = 0; 
	while ( sampleList.next() ) { 		
		int myRow = sampleList.getLocationRow();
		while ( myRow > thisRow ) {
			while ( thisCol <= width ) {
				out.print("<td><img src=\"");
				out.print(emptyImg);
				out.print("\"></td>");
				thisCol++;
			}
			out.print("</tr><tr><th>");
			thisRow++; thisCol=1;
			out.print((char)('A' + (thisRow - 1)));
			out.print("</th>");
		}
		int myCol = sampleList.getLocationCol();
		while ( thisCol < myCol ) {
			out.print("<td><img src=\"");
			out.print(emptyImg);
			out.print("\"></td>");
			thisCol++;
		}
		out.print("<td><a href=\"sample?id=");
		out.print(sampleList.getID());
		out.print("\"><img border=0 src=\"");
		out.print(filledImg);
		out.print("\"></a></td>");
		thisCol++;
		}

	int length = collObj.getLength(); 
	
	while ( thisRow <= length ) {
		while ( thisCol <= width ) {
			out.print("<td><img src=\"");
			out.print(emptyImg);
			out.print("\"></td>");
			thisCol++;
		}
		out.print("</tr><tr><th>");
		thisRow++; thisCol=1;
		if ( thisRow <= length ) {
			out.print((char)('A' + (thisRow - 1)));
			out.print("</th>");
		}
	}
}
%></tr></table></div></div><%
	} 
} else { 
%><p align="CENTER"><font size="+3" >Sample Collection Search</font></p>
<hr width='85%'/>
<center>
<form name="samplequery">
<table border=0>
<tr><td>Library:</td><td><%
	List<String> libraries = (List<String>) request.getAttribute(SampleServlet.ATTR_LIBRARIES); 
	String library = request.getParameter(SampleServlet.PARAM_LIBRARY);
%><select name="<%= SampleServlet.PARAM_LIBRARY %>" onchange="this.form.submit();"><option></option><%
	for ( String lib : libraries ) { 
%><option<%= ( lib.equals(library) ? " selected": "") %>><%= lib %></option><%
	} 
%></select>
</td></tr>
</table>
</form>
</center>
<jsp:include page="/sample/sample-collection-list.jsp" />

<div style="margin-top:10px">
<jsp:include page="/includes/loadableDiv.jsp">
<jsp:param value="<%= SampleServlet.INTERLACE_DIV_ID %>" name="loadingDivID"/>
<jsp:param value="Interlace collections" name="loadingDivTitle"/>
</jsp:include></div>
<% } %></div>
</body>
</html>