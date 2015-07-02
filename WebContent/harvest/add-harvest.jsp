<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="cyanos" %>
<%@ page import="edu.uic.orjala.cyanos.Harvest,
	edu.uic.orjala.cyanos.User,
	edu.uic.orjala.cyanos.Project,
	edu.uic.orjala.cyanos.web.servlet.HarvestServlet,
	edu.uic.orjala.cyanos.web.servlet.InocServlet,
	java.util.List, edu.uic.orjala.cyanos.Inoc,
	edu.uic.orjala.cyanos.sql.SQLData,
	edu.uic.orjala.cyanos.sql.SQLHarvest,
	edu.uic.orjala.cyanos.DataException,
	edu.uic.orjala.cyanos.sql.SQLStrain,
	java.util.Arrays,
	java.text.DateFormat" %>
<% 	String contextPath = request.getContextPath(); 
	User userObj = HarvestServlet.getUser(request); 
	DateFormat dateFormat = (DateFormat) session.getAttribute("dateFormatter");
%>
<!DOCTYPE html>
<html>
<head>
<cyanos:header title="Add Harvest"/>
</head>
<body>
<cyanos:menu helpModule="<%= HarvestServlet.HELP_MODULE %>"/>
<div class='content'>
<p align="CENTER"><font size="+2" >Add Harvest</font></p>
<hr width="75%">
<%
	if (request.getParameter("addHarvest") != null) {
		String strainID = request.getParameter("strain");
		String projectID = request.getParameter("project");
		SQLHarvest harvest = null;
		try {
		if (projectID != null && projectID.length() > 0) {
			harvest = SQLHarvest.createInProject((SQLData) request.getAttribute(HarvestServlet.DATASOURCE), strainID, projectID);
		} else if (strainID != null) {
			harvest = SQLHarvest.create((SQLData) request.getAttribute(HarvestServlet.DATASOURCE), strainID);
		}

		if (harvest != null && harvest.first()) { 
%><p align='center'><b>Created harvest</b> <a href='<%= contextPath %>/harvest?id=<%= harvest.getID() %>'>#<%= harvest.getID() %></a><%
			harvest.setDate(request.getParameter("harvDate"));
			harvest.setColor(request.getParameter("color"));
			String[] types = request.getParameterValues("type");
			if ( types != null ) {
				StringBuffer typeJoined = new StringBuffer();
				typeJoined.append(types[0]);
				for (int i = 1; i < types.length; i++) {
					typeJoined.append(",");
					typeJoined.append(types[i]);
				}
				harvest.setType(typeJoined.toString());
			}
			harvest.setNotes(request.getParameter("notes"));
			
			if ( request.getParameter("prepDate") != null && request.getParameter("prepDate").length() > 0 ) 
					harvest.setPrepDate(request.getParameter("prepDate"));	
			
			String value = request.getParameter("cellMass");
			if ( value != null && value.length() > 0 ) {
					harvest.setCellMass(value);
			}
			
			value = request.getParameter("mediaVol");
			if ( value != null && value.length() > 0 ) {
					harvest.setMediaVolume(value);
			}			

			if ( request.getParameter("inoc") != null ) {
				Inoc queryResults = (Inoc) request.getAttribute(InocServlet.SEARCHRESULTS_ATTR);
				List<String> selected = Arrays.asList(request.getParameterValues("inoc"));
				if (queryResults != null) {
					queryResults.beforeFirst();
					while (queryResults.next()) {
						if (selected.contains(queryResults.getID())) {
							queryResults.setHarvest(harvest);
							out.print("<br>Linked inoculation (#");
							out.print(queryResults.getID());
							out.print(") - ");
							out.print(queryResults.getVolumeString());
							out.print(" on ");
							out.print(dateFormat.format(queryResults.getDate()));
						}
					}
				} 
			} else if ( request.getParameter("col") != null ) { 
				harvest.setCollectionID(request.getParameter("col")); 
				SQLStrain strain = null;
				if (projectID != null && projectID.length() > 0) {
					strain = SQLStrain.createInProject((SQLData) request.getAttribute(HarvestServlet.DATASOURCE), strainID, projectID);
				} else {
					strain = SQLStrain.create((SQLData) request.getAttribute(HarvestServlet.DATASOURCE), strainID);
				}
				strain.setSourceCollectionID(request.getParameter("col"));
				strain.setCultureSource(request.getParameter("col"));
				strain.setDate(request.getParameter("harvDate"));
				strain.setStatus(SQLStrain.FIELD_HARVEST_STATUS);
%></p><p align="center"><b>Need to add information for new strain record: </b> <a href="strain?id=<%= strain.getID() %>"><%=strain.getID() %></a>
<%
			}
%></p><%
		} else { 
%><p align="center"><b>Harvest object not created!</b></p><jsp:include page="add-harvest-form.jsp"/><% } 	
	} catch (DataException e) { %><p align="center"><b><font color="red">ERROR:</font> <%= e.getLocalizedMessage() %></b></p>
<jsp:include page="add-harvest-form.jsp"/><%
	} 
} else { %>
<jsp:include page="add-harvest-form.jsp"/>
<% } %>
</div>
</body>
</html>