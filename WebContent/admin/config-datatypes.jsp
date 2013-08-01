<%@ page import="edu.uic.orjala.cyanos.web.AppConfig,
	edu.uic.orjala.cyanos.web.servlet.AdminServlet,
	edu.uic.orjala.cyanos.Strain, edu.uic.orjala.cyanos.Material, edu.uic.orjala.cyanos.Assay, edu.uic.orjala.cyanos.Compound, edu.uic.orjala.cyanos.Separation,
	java.util.Map, java.util.Map.Entry,
	java.util.Iterator" %>
<div><h2>File Types</h2>
<% AppConfig appConfig = (AppConfig) session.getAttribute(AdminServlet.APP_CONFIG_ATTR); 
if ( appConfig != null ) { %>
<form method="post">
<input type="hidden" name="form" value="<%= AdminServlet.FORM_CONFIG_DATATYPES %>">
<table class="species">
<tr><th>Data Type</th><th>Description</th><th>Remove</th></tr>
<tr><td colspan=2><i>Materials</i></td></tr>
<% int row = 1; 
	Map<String,String> dataTypes = appConfig.getDataTypeMap(Material.DATA_FILE_CLASS);
	if ( dataTypes != null ) { for ( Entry<String,String> entry : dataTypes.entrySet() ) { String rowString = String.format("%d", row); %>	
<tr><td><input type="hidden" name="row" value="<%= rowString %>">
<input type="hidden" name="<%= rowString %>_class" value="<%= Material.DATA_FILE_CLASS %>">
<input type="text" name="<%= rowString %>_type" value="<%= entry.getKey() %>"></td>
<td><input type="text" name="<%=rowString %>_desc" value="<%= entry.getValue() %>"></td>
<td><input type="checkbox" name="<%= rowString %>_rm"></td></tr> 
<%  row++; } } String rowString = String.format("%d", row++); %>
<tr><td><input type="hidden" name="row" value="<%= rowString %>">
<input type="hidden" name="<%= rowString %>_class" value="<%= Material.DATA_FILE_CLASS %>">
<input type="text" name="<%= rowString %>_type"></td>
<td><input type="text" name="<%=rowString %>_desc"></td>

<tr><td colspan=2><i>Separations</i></td></tr>
<%  dataTypes = appConfig.getDataTypeMap(Separation.DATA_FILE_CLASS);
if ( dataTypes != null ) { for ( Entry<String,String> entry : dataTypes.entrySet() ) { rowString = String.format("%d", row); %>	
<tr><td><input type="hidden" name="row" value="<%= rowString %>">
<input type="hidden" name="<%= rowString %>_class" value="<%= Separation.DATA_FILE_CLASS %>">
<input type="text" name="<%= rowString %>_type" value="<%= entry.getKey() %>"></td>
<td><input type="text" name="<%=rowString %>_desc" value="<%= entry.getValue() %>"></td>
<td><input type="checkbox" name="<%= rowString %>_rm"></td></tr> 
<%  row++; } } rowString = String.format("%d", row++); %>
<tr><td><input type="hidden" name="row" value="<%= rowString %>">
<input type="hidden" name="<%= rowString %>_class" value="<%= Separation.DATA_FILE_CLASS %>">
<input type="text" name="<%= rowString %>_type"></td>
<td><input type="text" name="<%=rowString %>_desc"></td>

<tr><td colspan=2><i>Assays</i></td></tr>
<%  dataTypes = appConfig.getDataTypeMap(Assay.DATA_FILE_CLASS);
if ( dataTypes != null ) { for ( Entry<String,String> entry : dataTypes.entrySet() ) { rowString = String.format("%d", row); %>	
<tr><td><input type="hidden" name="row" value="<%= rowString %>">
<input type="hidden" name="<%= rowString %>_class" value="<%= Assay.DATA_FILE_CLASS %>">
<input type="text" name="<%= rowString %>_type" value="<%= entry.getKey() %>"></td>
<td><input type="text" name="<%=rowString %>_desc" value="<%= entry.getValue() %>"></td>
<td><input type="checkbox" name="<%= rowString %>_rm"></td></tr> 
<%  row++; } } rowString = String.format("%d", row++); %>
<tr><td><input type="hidden" name="row" value="<%= rowString %>">
<input type="hidden" name="<%= rowString %>_class" value="<%= Assay.DATA_FILE_CLASS %>">
<input type="text" name="<%= rowString %>_type"></td>
<td><input type="text" name="<%=rowString %>_desc"></td>

<tr><td colspan=2><i>Compounds</i></td></tr>
<%  dataTypes = appConfig.getDataTypeMap(Compound.DATA_FILE_CLASS);
if ( dataTypes != null ) { for ( Entry<String,String> entry : dataTypes.entrySet() ) { rowString = String.format("%d", row); %>	
<tr><td><input type="hidden" name="row" value="<%= rowString %>">
<input type="hidden" name="<%= rowString %>_class" value="<%= Compound.DATA_FILE_CLASS %>">
<input type="text" name="<%= rowString %>_type" value="<%= entry.getKey() %>"></td>
<td><input type="text" name="<%=rowString %>_desc" value="<%= entry.getValue() %>"></td>
<td><input type="checkbox" name="<%= rowString %>_rm"></td></tr> 
<%  row++; } } rowString = String.format("%d", row++); %>
<tr><td><input type="hidden" name="row" value="<%= rowString %>">
<input type="hidden" name="<%= rowString %>_class" value="<%= Compound.DATA_FILE_CLASS %>">
<input type="text" name="<%= rowString %>_type"></td>
<td><input type="text" name="<%=rowString %>_desc"></td>

<tr><td colspan=3 align="center"><button type="submit" name="<%= AdminServlet.PARAM_CONFIG_UPDATE %>">Update</button><button type="reset">Reset Values</button>
<button type="submit" name="<%= AdminServlet.PARAM_CONFIG_UPDATE %>" value="defaults">Load Defaults</button></td></tr>
</table>
</form>
<% } %>
</div>