<%@ page import="edu.uic.orjala.cyanos.web.AppConfig,
	edu.uic.orjala.cyanos.web.servlet.AdminServlet,
	java.util.Map,java.util.List,java.util.ListIterator,
	java.util.Iterator" %>
<div><h2>Work Queues</h2>
<% AppConfig appConfig = (AppConfig) session.getAttribute(AdminServlet.APP_CONFIG_ATTR); 
if ( appConfig != null ) { %>
<form method="post">
<input type="hidden" name="form" value="<%= request.getParameter("form") %>">
<table class="species">
<tr><th>Type</th><th>Source</th><td></td></tr>
<% 		
	String[] queueTypes = { AppConfig.QUEUE_SINGLE, AppConfig.QUEUE_STATIC, AppConfig.QUEUE_JDBC };
	String[] queueTitles =  { "Single Queue", "Static List", "Database List" };
	for ( String queue : AppConfig.QUEUE_TYPES ) {
		String queueSource = appConfig.queueSource(queue);
		if ( queueSource == null ) {
			if (queue.equals("user") ) 
				queueSource = AppConfig.QUEUE_JDBC;
			else 
				queueSource = AppConfig.QUEUE_STATIC;
		} %>
<tr><td><%= queue %></td><td><select name="queueType:<%= queue %>">
<% for ( int i = 0; i < queueTypes.length; i++ ) { %>
<option<%= (queueTypes[i].equals(queueSource) ? " selected " : "") %>><%= queueTitles[i] %></option>
<% } %>
</select></td>
<% if ( queueSource.equals(AppConfig.QUEUE_STATIC) ) { 				
	List<String> queueNames = appConfig.queuesForType(queue);
				StringBuffer nameVal = new StringBuffer();
				if ( queueNames != null && (queueNames.size() > 0 )) {
					ListIterator<String> nameIter = queueNames.listIterator();
						nameVal.append(nameIter.next());
						while ( nameIter.hasNext() ) {
							nameVal.append(", ");
							nameVal.append(nameIter.next());
						}
				}
%>
<td><input type="text" name="queueList:<%= queue %>" value="<%= nameVal.toString() %>" size=75></td>
<% } %></tr>		
<% } %>
<tr><td colspan=3 align="center"><button type="submit" name="<%= AdminServlet.PARAM_CONFIG_UPDATE %>">Update</button><button type="reset">Reset Values</button></td></tr>
</table>
</form>
<% } %>
</div>