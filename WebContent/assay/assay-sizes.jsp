<% String size = request.getParameter("fieldValue"); 
	String name = request.getParameter("fieldName");%>
<select name="<%= name %>">
<% 	int[] lengths = { 4, 8, 8, 16, 32 };  
	int[] widths = { 6, 6, 12, 24, 48 }; 
	for ( int i = 0; i < lengths.length; i++ ) { 
	String thisSize = String.format("%dx%d", lengths[i], widths[i]); 
%><option value="<%= thisSize %>" <%= ( thisSize.equals(size) ? "selected" : "") %>><%= String.format("%d wells (%s)", lengths[i] * widths[i], thisSize) %></option>
<% } %>
</select>