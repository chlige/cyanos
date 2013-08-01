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
	String rowString = request.getParameter("rows");
	int rows = 1;
	if ( rowString != null ) {
		rows = Integer.parseInt(rowString);	
	}
	DateFormat format = (DateFormat) session.getAttribute(InocServlet.SESS_ATTR_DATE_FORMAT);
	SQLData data = (SQLData) request.getAttribute(InocServlet.DATASOURCE);
%><table class="dashboard">
<tr><td></td><th class="header">Inoc ID</th><th class="header">Culture ID</th><th class="header">Date</th><th class="header">Parent Stock</th><th class="header">Media</th><th class="header">Volume</th><th class="header">Project</th><th class="header">Notes</th><th class="header">Stock</th></tr>
<% for ( int row = 1; row <= rows; row++) { 
	String strainName = request.getParameter(String.format("%02d_strain", row));
	if ( strainName != null ) {
		String dateString = request.getParameter(String.format("%02d_date", row));
		String parent = request.getParameter(String.format("%02d_parent", row));
		String media = request.getParameter(String.format("%02d_media", row));
		String volume = request.getParameter(String.format("%02d_vol", row));
		String project = request.getParameter(String.format("%02d_project", row));
		if ( project.length() < 1 ) { project = null; }
		String notes = request.getParameter(String.format("%02d_notes", row));
		boolean stock = request.getParameter(String.format("%02d_stock",row)) != null;
		int count = 1;
		try { 
			count = Integer.parseInt(request.getParameter(String.format("%02d_qty", row)));
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