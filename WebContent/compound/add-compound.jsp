<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="cyanos" %>
<%@ page import="edu.uic.orjala.cyanos.web.servlet.CompoundServlet,
	edu.uic.orjala.cyanos.User,edu.uic.orjala.cyanos.Role" %>
<% 	String contextPath = request.getContextPath(); 
	User user = CompoundServlet.getUser(request);
%>
<!DOCTYPE html>
<html>
<head>
<cyanos:header title="Add new compound"/>
</head>
<body>
<cyanos:menu helpModule="<%= CompoundServlet.HELP_MODULE %>"/>

<div class='content'>		
<% if ( user.couldPerform(User.SAMPLE_ROLE, Role.CREATE) ) {  %>
<h1>Add New Compound</h1>
<form name='editProject' enctype="multipart/form-data" method="post" action="compound">
<input type="hidden" name="form" value="add">
<table class="species">
<tr><td width='125'>Compound ID:</td><td><input type="text" name="newID"  value='<c:out value='<%= request.getParameter("newID") %>'/>'></td></tr>
<tr><td>Name:</td><td><input type='text' name='<%= CompoundServlet.FIELD_NAME %>'  value='<c:out value="<%= request.getParameter(CompoundServlet.FIELD_NAME) %>"/>'></td></tr>
<tr><td>Structure:</td>
<td><select name="<%= CompoundServlet.FIELD_FILE_FORMAT %>">
<option value="<%= CompoundServlet.FORMAT_MDL %>">MDL Format</option>
<option value="<%= CompoundServlet.FORMAT_CML %>">CML Format</option>
</select>
<input type="file" name="<%= CompoundServlet.MDL_FILE %>" size="25"></td></tr>
<tr><td>Formula:</td><td><input type='text' name='<%= CompoundServlet.FIELD_FORMULA %>' value='<c:out value="<%= request.getParameter(CompoundServlet.FIELD_FORMULA) %>"/>'></td></tr>
<tr><td>Formula Weight:</td><td><input type='text' name='<%= CompoundServlet.FIELD_AVG_MASS %>' value='<c:out value="<%= request.getParameter(CompoundServlet.FIELD_AVG_MASS) %>"></c:out>'></td></tr>
<tr><td>Monoisotopic Mass:</td><td><input type='text' name='<%= CompoundServlet.FIELD_MONO_MASS %>' value='<c:out value="<%= request.getParameter(CompoundServlet.FIELD_MONO_MASS) %>"></c:out>'></td></tr>
<tr><td>SMILES String:</td><td><textarea name="<%= CompoundServlet.FIELD_SMILES_STRING %>" cols="50" rows="3"><c:out value="<%= request.getParameter(CompoundServlet.FIELD_SMILES_STRING) %>"></c:out></textarea>
</td></tr>
<tr><td>InChI Key:</td><td><input type="text" name="<%= CompoundServlet.FIELD_INCHI_KEY %>" value="<c:out value="<%= request.getParameter(CompoundServlet.FIELD_INCHI_KEY) %>"></c:out>" size="50"></td></tr>
<tr><td>InChI String:</td><td><textarea name="<%= CompoundServlet.FIELD_INCHI_STRING %>" cols="50" rows="3"><c:out value="<%= request.getParameter(CompoundServlet.FIELD_INCHI_STRING) %>"></c:out></textarea>
</td></tr>
<tr><td>Project</td><td><cyanos:project-popup fieldName="project"/></td></tr>
<tr><td valign=top>Notes:</td><td><textarea rows="7" cols="70" name="notes"><c:out value='<%= request.getParameter("notes") %>'/></textarea></td></tr>
<tr><td colspan="2" align="CENTER"><button type="submit" name="<%= CompoundServlet.UPDATE_ACTION %>">Update</button>
<input type="RESET"></td></tr>
</table>
</form>
<% } %>
</div>
