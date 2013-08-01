<%@ page import="edu.uic.orjala.cyanos.web.AppConfig,
	edu.uic.orjala.cyanos.web.servlet.AdminServlet,
	edu.uic.orjala.cyanos.Strain, edu.uic.orjala.cyanos.Material, edu.uic.orjala.cyanos.Assay, edu.uic.orjala.cyanos.Compound, edu.uic.orjala.cyanos.Separation,
	java.util.Map, java.util.Map.Entry,
	java.util.Iterator" %>
<div><h2>File Paths</h2>
<% AppConfig appConfig = (AppConfig) session.getAttribute(AdminServlet.APP_CONFIG_ATTR); 
if ( appConfig != null ) { %>
<form method="post">
<script type="text/javascript">
var dataTypeMap = { "*": {"*": "ALL FILES"} };
<% for ( String someClass : AdminServlet.FILE_PATH_CLASSES ) { 
	if ( someClass.equals(Strain.DATA_FILE_CLASS) ) { %>
dataTypeMap["<%= Strain.DATA_FILE_CLASS  %>"] = { "*": "ALL FILES", <%= Strain.PHOTO_DATA_TYPE %> : "Photos"}; 		
<% } else { Map<String, String> typeMap = appConfig.getDataTypeMap(someClass); 
		if ( typeMap != null ) { %>
dataTypeMap["<%=someClass%>"] = { "*": "ALL FILES"
		<%  for ( Entry<String,String> dataType : typeMap.entrySet() ) { %>
		, <%= dataType.getKey() %>: "<%= dataType.getValue() %>"
		<% } %> };
		<% } } } %>
</script>
<input type="hidden" name="form" value="<%= AdminServlet.FORM_CONFIG_FILEPATHS %>">
<table class="species">
<tr><th>Object Class</th><th>File Type</th><th>Directory</th><th>Remove</th></tr>
<% 	Map<String, Map<String, String>> fileTypeMap = appConfig.getFilePathMap();
	Iterator<String> classIter = fileTypeMap.keySet().iterator();
	int row = 1; 		
	while ( classIter.hasNext() ) {
			String aClass = classIter.next();
			Map<String, String> classMap =  fileTypeMap.get(aClass);
			Iterator<String> typeIter = classMap.keySet().iterator();
			Map<String, String> typeMap = appConfig.getDataTypeMap(aClass);
			while ( typeIter.hasNext() ) {
				String aType = typeIter.next(); 
				String rowString = String.format("%d", row); %>
<tr><td><input type="hidden" name="row" value="<%= rowString %>"><input type="hidden" name="<%= rowString %>_class" value="<%= aClass %>"><%= aClass %></td>
<td><% 	if ( aClass.equals("*") ) { %><input type="hidden" name="<%= rowString %>_type" value="<%= aType %>">ALL FILES<% } else { %>
<select name="<%= rowString %>_type">
<option value="*" <%= (aType.equals("*") ? "selected" : "") %>>ALL FILES</option>
<% if ( typeMap != null ) { for ( Entry<String,String> dataType : typeMap.entrySet() ) { %>
<option value="<%= dataType.getKey() %>" <%= (dataType.getKey().equals(aType) ? "selected" : "") %>><%= dataType.getValue() %></option>
<% } } else if ( aClass.equals(Strain.DATA_FILE_CLASS) ) { %>
<option value="<%= Strain.PHOTO_DATA_TYPE %>" <%= (aType.equals(Strain.PHOTO_DATA_TYPE) ? "selected" : "") %>>Photos</option>
<%} %></select><% } %></td>
<td><input type="text" name="<%= rowString %>_path" value="<%= classMap.get(aType) %>" size=50></td>
<td><input type="checkbox" name="<%= rowString %>_rm"></td></tr>
<% row++; } %>
<tr><td colspan="3"><hr></td></tr>
<% } %>
<tr><td colspan="3" align="center"><b>New Mapping</b></td></tr>
<tr><td><select name="new_class" onchange="var thisClass = this.options[this.selectedIndex];
var types = dataTypeMap[this.options[this.selectedIndex].value]; 
var typeSelect = this.form.elements['new_type'];
while ( typeSelect.options.length > 0 ) { typeSelect.options.remove(0); }
var newValues = Object.keys(types);
for ( var i = 0; i < newValues.length; i++ ) { var option = document.createElement('option'); 
	option.text=types[newValues[i]]; 
	option.value=newValues[i]; 
	typeSelect.add(option); } ">
<% for ( String someClass : AdminServlet.FILE_PATH_CLASSES ) { %><option><%= someClass %></option><% } %></select></td>
<td><select name="new_type"><option value="*">ALL FILES</option></select></td>
<td><input type="text" name="new_path"  size=50></td></tr>
<tr><td colspan=3 align="center"><button type="submit" name="<%= AdminServlet.PARAM_CONFIG_UPDATE %>">Update</button><button type="reset">Reset Values</button></td></tr>
</table>
</form>
<% } %>
</div>