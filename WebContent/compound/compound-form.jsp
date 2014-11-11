<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="cyanos"%>
<%@ page import="edu.uic.orjala.cyanos.Compound,
	edu.uic.orjala.cyanos.sql.SQLCompound,
	edu.uic.orjala.cyanos.web.servlet.CompoundServlet,
	edu.uic.orjala.cyanos.Role,
	java.text.SimpleDateFormat,
	edu.uic.orjala.cyanos.Separation,
	edu.uic.orjala.cyanos.web.BaseForm,
	edu.uic.orjala.cyanos.Harvest,
	edu.uic.orjala.cyanos.Project,
	java.math.BigDecimal,
	java.util.Set" %>
<% 	String contextPath = request.getContextPath();
	Compound compoundObj = (Compound) request.getAttribute(CompoundServlet.COMPOUND_OBJ); 	
	boolean update = request.getParameter("updateCompound") != null;	
	Set<String> updateMap = (Set<String>) request.getAttribute(CompoundServlet.ATTR_UPDATE_MAP);
	if ( compoundObj == null ) { %>
<p align='center'><b>ERROR:</b> Object not passed</p>
<% out.flush(); return; } else if ( ! compoundObj.first() ) { %>
<p align='center'><b>ERROR:</b> Object not found</p>
<% out.flush(); return; } 	boolean hasMDL = compoundObj.hasMDLData(); %>
<div CLASS="showSection" ID="view_info">
<% if ( hasMDL ) { 
	if ( request.getParameter(CompoundServlet.CLEAR_ACTION) != null ) {
		compoundObj.clearMDLData();
		hasMDL = true;
	}

	if ( request.getParameter("genGraph") != null ) {
//		try {
		((SQLCompound)compoundObj).updateGraph();
//		} catch (Exception e) {
%><%-- <p>Error building graph: <%= e.getMessage() %> --%><%
//		}
	}
%><div><div class="left70">
<% } %>
<table class="species">
<tr><td width='125'>Compound ID:</td><td><%= compoundObj.getID() %></td></tr>
<tr <% if ( updateMap != null && updateMap.contains(CompoundServlet.FIELD_NAME) ) { %> class="updated" <% } %>>
<td>Label:</td><td><%= compoundObj.getName() %></td></tr>
<tr <% if ( updateMap != null && updateMap.contains(CompoundServlet.FIELD_FORMULA) ) { %> class="updated" <% } %>>
<td>Formula:</td><td><%= compoundObj.getHTMLFormula() %></td></tr>
<tr <% if ( updateMap != null && updateMap.contains(CompoundServlet.FIELD_AVG_MASS) ) { %> class="updated" <% } %>>
<td>Formula Weight:</td><td><%= compoundObj.getAverageMass().toPlainString() %> Da</td></tr>
<tr <% if ( updateMap != null && updateMap.contains(CompoundServlet.FIELD_MONO_MASS) ) { %> class="updated" <% } %>>
<td>Monoisotopic mass:</td><td><%= compoundObj.getMonoisotopicMass().toPlainString() %> Da</td></tr>
<% String inchiKey = compoundObj.getInChiKey(); if ( inchiKey != null && inchiKey.length() > 0 ) { %>
<tr <% if ( updateMap != null && updateMap.contains(CompoundServlet.FIELD_INCHI_KEY) ) { %> class="updated" <% } %>>
<td>InChI Key:</td><td><%= inchiKey %><br>
<a class="chemspider" href="http://www.chemspider.com/Search.aspx?q=<%= inchiKey %>" target="_blank"><img src="<%= contextPath %>/images/icons/cs-icon.png" style="vertical-align:middle"> Search ChemSpider</a>
</td></tr>
<% } %>
<% String inchiString = compoundObj.getInChiString(); if ( inchiString != null ) { %>
<tr <% if ( updateMap != null && updateMap.contains(CompoundServlet.FIELD_INCHI_STRING) ) { %> class="updated" <% } %>>
<td>InChI String:</td><td>
<button type="button" onClick="showHideButton(this, 'inchi_view');">Show</button>
<div id="inchi_view" class="hideSection" style="text-align:center; position:absolute; z-index:99;">
<div class='pemData' style="background-color:#FFFFFF; width:500px; word-wrap:break-word;"><%= inchiString %></div></div></td></tr>
<% } %> 
<% String smilesString = compoundObj.getSmilesString(); if ( smilesString != null ) { %>
<tr <% if ( updateMap != null && updateMap.contains(CompoundServlet.FIELD_SMILES_STRING) ) { %> class="updated" <% } %>>
<td>SMILES String:</td><td>
<button type="button" onClick="showHideButton(this, 'smiles_view');">Show</button>
<div id="smiles_view" class="hideSection" style="text-align:center; position:absolute; z-index:99;">
<div class='pemData' style="background-color:#FFFFFF; width:500px; word-wrap:break-word;"><%= smilesString %></div></div></td></tr>
<% } %> 

<tr <% if ( updateMap != null && updateMap.contains(CompoundServlet.FIELD_PROJECT) ) { %> class="updated" <% } %>><td>Project</td><td>
<% Project aProj = compoundObj.getProject(); if ( aProj != null && aProj.first() ) { %>
<a href='project?id=<%= aProj.getID() %>'><%= aProj.getName() %></a>
<% } else { %>
None
<% } %>
</td></tr>
<tr <% if ( updateMap != null && updateMap.contains(CompoundServlet.FIELD_NOTES) ) { %> class="updated" <% } %>>
<td valign=top>Notes:</td><td><%= BaseForm.formatStringHTML(compoundObj.getNotes()) %></td></tr>
</table>
<% if ( hasMDL ) { %>
</div>
<div class="right30" align="center">
<form method="post">
<img src="<%= contextPath %>/compound?graphic=png&id=<%= compoundObj.getID() %>" height=300 width=300>
<p>Image size: <input type="text" size="5" name="imgW" value="500"> &times; <input type="text" size="5" name="imgH" value="500">
<button type="button" onclick='showCompound("<%= compoundObj.getID() %>", this.form.imgW.value, this.form.imgH.value)'>Generate Image</button></p>
<p><a href="<%= request.getContextPath() %>/compound?exportType=mol&id=<%= compoundObj.getID() %>">Export Structure (MOL)</a>
<%-- <button type="button" onclick='exportCompound("<%= compoundObj.getID() %>", ".mol")'>Export Structure</button> --%>
</p>
</form>
</div></div>
<% } if ( compoundObj.isAllowed(Role.WRITE) ) {  %>
<p align='center'><button type='button' onClick='flipDiv("info")'>Edit Values</button></p>
</div>

<div class='hideSection' id="edit_info">
<form name='editProject' enctype="multipart/form-data" method="post">
<input type="hidden" name="id" value="<%= compoundObj.getID() %>">
<table class="species">
<tr><td width='125'>Compound ID:</td><td><%= compoundObj.getID() %></td></tr>
<tr><td>Name:</td><td><input type='text' name='<%= CompoundServlet.FIELD_NAME %>' value='<%= compoundObj.getName() %>'></td></tr>
<tr><td>Structure:</td>
<% if ( hasMDL )  { %>
<td><button type="submit" name="<%= CompoundServlet.CLEAR_ACTION %>" <%-- onClick="updateForm(this,'<%= CompoundServlet.INFO_FORM_DIV_ID %>')" --%>>Clear Structure Data</button></td>
<% } else { %>
<td><select name="<%= CompoundServlet.FIELD_FILE_FORMAT %>">
<option value="<%= CompoundServlet.FORMAT_MDL %>">MDL Format</option>
<option value="<%= CompoundServlet.FORMAT_CML %>">CML Format</option>
</select>
<input type="file" name="<%= CompoundServlet.MDL_FILE %>" size="25"></td>
<% } %></tr>
<tr><td>Formula:</td><td><input type='text' name='<%= CompoundServlet.FIELD_FORMULA %>' value='<c:out value="<%= compoundObj.getFormula() %>"></c:out>'></td></tr>
<tr><td>Formula Weight:</td><td><input type='text' name='<%= CompoundServlet.FIELD_AVG_MASS %>' value='<c:out value="<%= compoundObj.getAverageMass().toPlainString() %>"></c:out>'></td></tr>
<tr><td>Monoisotopic Mass:</td><td><input type='text' name='<%= CompoundServlet.FIELD_MONO_MASS %>' value='<c:out value="<%= compoundObj.getMonoisotopicMass().toPlainString() %>"></c:out>'></td></tr>
<tr><td>SMILES String:</td><td><textarea name="<%= CompoundServlet.FIELD_SMILES_STRING %>" cols="50" rows="3"><c:out value="<%= smilesString %>"></c:out></textarea>
<% if ( hasMDL ) { %>
<br><input type="checkbox" name="genString" value="smiles" onClick="this.form.elements['<%= CompoundServlet.FIELD_SMILES_STRING %>'].disabled = this.checked;">Generate SMILES string from structure data.
<% } %>
</td></tr>
<tr><td>InChI Key:</td><td><input type="text" name="<%= CompoundServlet.FIELD_INCHI_KEY %>" value="<c:out value="<%= inchiKey %>"></c:out>" size="50"></td></tr>
<tr><td>InChI String:</td><td><textarea name="<%= CompoundServlet.FIELD_INCHI_STRING %>" cols="50" rows="3"><c:out value="<%= inchiString %>"></c:out></textarea>
<% if ( hasMDL ) { %>
<br><input type="checkbox" name="genString" value="inchi" onClick="this.form.elements['<%= CompoundServlet.FIELD_INCHI_KEY %>'].disabled = this.checked; this.form.elements['<%= CompoundServlet.FIELD_INCHI_STRING %>'].disabled = this.checked;">Generate InChI string and key from structure data.<br>
<br><input type="checkbox" name="genGraph">Regenerate Compound graph entries (for structure searches)
<% } %>
</td></tr>
<tr><td>Project</td><td>
<cyanos:project-popup fieldName="project" project="<%= compoundObj.getProjectID() %>"/>
<%-- 
<jsp:include page="/includes/project-popup.jsp">
<jsp:param value="<%=compoundObj.getProjectID() %>" name="project"/>
<jsp:param value="project" name="fieldName"/></jsp:include>
--%>
</td></tr>
<tr><td valign=top>Notes:</td><td><textarea rows="7" cols="70" name="notes"><c:out value="<%= compoundObj.getNotes() %>" default="" /></textarea></td></tr>
<tr><td colspan="2" align="CENTER"><button type="submit" name="<%= CompoundServlet.UPDATE_ACTION %>">Update</button>
<input type="RESET"></td></tr>
</table>
</form>
<p align="center"><button type='button' onClick='flipDiv("info")'>Close Form</button></p>
<% } %>
</div>
