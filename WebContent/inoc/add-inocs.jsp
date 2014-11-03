<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.Strain,
	edu.uic.orjala.cyanos.Inoc,
	edu.uic.orjala.cyanos.CyanosObject,
	edu.uic.orjala.cyanos.sql.SQLInoc,
	edu.uic.orjala.cyanos.sql.SQLData,
	edu.uic.orjala.cyanos.web.servlet.InocServlet,
	edu.uic.orjala.cyanos.web.servlet.StrainServlet,
	edu.uic.orjala.cyanos.web.BaseForm,
	java.text.DateFormat" %>
<%	String contextPath = request.getContextPath();
	String[] rows = request.getParameterValues("row");
	DateFormat format = (DateFormat) session.getAttribute(InocServlet.SESS_ATTR_DATE_FORMAT);
	SQLData data = InocServlet.getSQLData(request);
%><table class="dashboard">
<tr><td></td><th class="header">Inoc ID</th><th class="header">Strain ID</th><th class="header">Date</th><th class="header">Parent Stock</th><th class="header">Media</th><th class="header">Volume</th><th class="header">Project</th><th class="header">Notes</th><th class="header">Stock</th></tr>
<% for ( String row: rows ) { 
	String strainName = request.getParameter(row + "_strain");
	if ( strainName != null ) {
		String dateString = request.getParameter(row + "_date");
		String parent = request.getParameter(row + "_parent");
		String media = request.getParameter(row + "_media");
		String volume = request.getParameter(row + "_vol");
		String project = request.getParameter(row + "_project");
		if ( project.length() < 1 ) { project = null; }
		String notes = request.getParameter(row + "_notes");
		boolean stock = request.getParameter(row + "_stock") != null;
		int count = 1;
		try { 
			count = Integer.parseInt(request.getParameter(row + "_qty"));
		} catch (NumberFormatException e) {
			
		}
		count = ( count < 1 ? 1 : count);
		for ( int c = 0; c < count; c++ ) {
			Inoc thisInoc;
			if ( project != null ) {
				thisInoc = SQLInoc.createInProject(data, strainName, project);
			} else {
				thisInoc = SQLInoc.create(data, strainName);
			}
			if ( thisInoc.first() ) {
				thisInoc.setManualRefresh();
				thisInoc.setDate(dateString);
				thisInoc.setVolume(volume);
				thisInoc.setMedia(media);
				if ( parent != null && parent.length() > 1)
					thisInoc.setParentID(parent);
				thisInoc.setNotes(notes);
				if ( stock ) thisInoc.setFate(Inoc.FATE_STOCK);
				thisInoc.refresh();
				thisInoc.setAutoRefresh();
			}
			
			Strain strain = thisInoc.getStrain();
			Inoc parentInoc = thisInoc.getParent();
		%>
			<tr class="banded" align='center'>
			<td><b><%= row %></b> ( <%= count %>)</td>
			<td><a href="inoc?id=<%= thisInoc.getID() %>"><%= thisInoc.getID() %></a></td>
			<td><a href="strain?id=<%= strain.getID() %>"><%= strain.getID() %> <%= strain.getName() %></a></td>
			<td><%= format.format(thisInoc.getDate()) %></td>
			<td><% if ( parentInoc != null && parentInoc.first() ) { out.print(parentInoc.getID()); out.print(" "); out.print(SQLInoc.autoFormatAmount(parentInoc.getVolume(), Inoc.VOLUME_TYPE)); } %></td>
			<td><%= thisInoc.getMedia() %></td>
			<td><%= SQLInoc.autoFormatAmount(thisInoc.getVolume(), Inoc.VOLUME_TYPE) %></td>
			<td><%= thisInoc.getProjectID() %></td>
			<td><%= thisInoc.getNotes() %></td>
			<td><% if ( stock ) { out.print("Stock"); } %></td>
			</tr><%
		}
	}
 } %>
</table>