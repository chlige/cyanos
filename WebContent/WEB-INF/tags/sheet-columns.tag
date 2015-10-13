<%@ tag language="java" pageEncoding="UTF-8"%>
<%@ tag import="edu.uic.orjala.cyanos.web.servlet.UploadServlet" %>
<%@ attribute name="fieldName" required="true" %>
<%@ attribute name="onchange" required="false" %>
<%@ attribute name="onload" required="false" %>
<%	String fieldName = (String) jspContext.getAttribute("fieldName");
	String value = request.getParameter(fieldName);
	int selIndex = -1;
	if ( value != null ) {
		try {
			selIndex = Integer.parseInt(value);
		} catch (NumberFormatException e) {
			// Nothing to do.  will use default value.
		}
	}
	String[] columnList = UploadServlet.getColumnList(request);
%><select name="${fieldName}"<% if ( onchange != null ) { %> onchange="${onchange}"<% } if ( onload != null ) { %> onload="${onload}"<% } %>>
<jsp:doBody/>
<%  
	for (int i = 0; i < columnList.length; i++ ) {
		boolean selected = false;
		if ( selIndex > -1 ) {
			selected = ( i == selIndex);
		} else {
			selected = ( columnList[i].equalsIgnoreCase(fieldName) );
		}
%><option value="<%= i %>" <%= (selected ? "selected" : "") %>><%= columnList[i] %></option>
<% } %></select>