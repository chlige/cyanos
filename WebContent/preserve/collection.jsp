<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page import="edu.uic.orjala.cyanos.web.servlet.CryoServlet,
	edu.uic.orjala.cyanos.sql.SQLCryoCollection,
	edu.uic.orjala.cyanos.CryoCollection,
	edu.uic.orjala.cyanos.Cryo,
	edu.uic.orjala.cyanos.sql.SQLCryo,
	edu.uic.orjala.cyanos.Role,
	java.text.SimpleDateFormat,
	edu.uic.orjala.cyanos.Project,
	java.util.Date, java.util.List" %>
<% 	String contextPath = request.getContextPath();
	CryoCollection thisObject = SQLCryoCollection.load(CryoServlet.getSQLData(request), request.getParameter("collection"));
	if ( thisObject == null ) { %>
<p align='center'><b>ERROR:</b> Object not passed</p><% out.flush(); return; 
} else if ( ! thisObject.first() ) { %><p align='center'><b>ERROR:</b> Object not found</p><% out.flush(); return; } 
	boolean update = ( thisObject.isAllowed(Role.WRITE) && request.getParameter("updateRecord") != null ); %>
<div CLASS="showSection" ID="view_info">
<table class="species" align='center'>
<tr><td width='125'>Collection ID:</td><td><%= thisObject.getID() %></td></tr>
<tr<% if ( update ) {
	String name = request.getParameter("collectionName");
	if ( name != null && name.compareTo(thisObject.getName()) != 0 ) {
		thisObject.setName(name);
%> class="updated"<% } } %>><td>Name:</td><td><%= thisObject.getName() %></td></tr>
<tr<% if ( update ) { 
	String rowString = request.getParameter("rowLength");
	String colString = request.getParameter("colLength");
	try {
		int row = ( rowString != null ? Integer.parseInt(rowString, 10) : 1);
		int col = ( colString != null ? Integer.parseInt(colString, 10) : 1);
		if ( row != thisObject.getWidth() || col != thisObject.getLength() ) {
			thisObject.setWidth(row); thisObject.setLength(col); %> class="updated"<%
		}
	} catch (NumberFormatException e) { %>><td colspan="2" style="color: red"><i>ERROR:</i> Please only use positive integers (&gt; 0) for the size dimensions.</td></tr><tr<%	
} } %>><td>Size:</td><td><input type="text" size="5" name="rowLength" value="<%= thisObject.getWidth() %>"> &times; <input type="text" size="5" name="colLength" value="<%= thisObject.getLength() %>"></td></tr>
<tr<% if ( update ) { 
	String value = request.getParameter("notes");
	if (value != null && (! value.equals(thisObject.getNotes()) ) ) {
		thisObject.setNotes(value);	
%> class="updated"<% } } %>><td valign=top>Notes:</td><td><%= CryoServlet.formatStringHTML(thisObject.getNotes()) %></td></tr>
</table>
<% if ( thisObject.isAllowed(Role.WRITE) ) { %>
<p align='center'><button type='button' onClick='flipDiv("info")'>Edit Values</button></p>
</div><div class='hideSection' id="edit_info">
<form name='editMaterial'>
<table class="species" align='center'>
<tr><td width='125'>Collection ID:</td><td><%= thisObject.getID() %></td></tr>
<tr><td>Name:</td><td><input name="collectionName" value="<%= thisObject.getName() %>"></td></tr>
<tr><td valign=top>Notes:</td><td><textarea rows="7" cols="70" name="notes"><c:out value="<%= thisObject.getNotes() %>" default="" /></textarea></td></tr>
<tr><td colspan="2" align="CENTER"><button type="button" name="updateRecord" onClick="updateForm(this,'view_info')">Update</button>
<input type="RESET"></td></tr>
</table>
</form>
<p align="center"><button type='button' onClick='flipDiv("info")'>Close Form</button></p>
<% } %>
</div>
<div class="collapseSection"><a onClick='loadDiv("collectionBox")' class='twist'>
<img align="absmiddle" id="twist_collectionBox" src="/cyanos/images/twist-open.png" /> Box View</a>
<div class="showSection" id="div_collectionBox">
<% 
	int rowLength = thisObject.getWidth();
	int colLength = thisObject.getLength();
	
	Cryo queryResults = SQLCryo.loadForCollection(CryoServlet.getSQLData(request), request.getParameter("collection"));
	request.setAttribute("cryoList", queryResults);
	queryResults.first();
%><table class="box"><tr><th></th><%
for ( int col = 1; col <= rowLength; col++ ) {
	%><th><%= col %></th><%	
} %></tr><%	
	int currRow = queryResults.getRowIndex();
	int currCol = queryResults.getColumnIndex();
	SimpleDateFormat dateFormat = new SimpleDateFormat("MMM-DD-YY");
	for ( int row = 1; row <= colLength; row++ ) { 
%><tr><th><%= CryoServlet.getLetterForIndex(row - 1) %></th><%
		for ( int col = 1; col <= rowLength; col++ ) {
%><td><%	if ( col == currCol && row == currRow ) {
%><a href="?id=<%= queryResults.getID() %>"><%= queryResults.getCultureID() %> (<%= queryResults.getID() %>)</a><br>
<% 				out.println(dateFormat.format(queryResults.getDate()));
				if ( queryResults.next() ) {
					currRow = queryResults.getRowIndex();
					currCol = queryResults.getColumnIndex();
				}
			} %></td><%
		} %></tr>
<% }%></table>
<p align="center"><a href="<%= request.getContextPath() %>/preserve/add.jsp">Add Preservation</a></p>
<p align="center"><a href="<%= request.getContextPath() %>/preserve/remove.jsp">Remove Preservations</a></p>
</div></div>

<div class="collapseSection"><a onClick='loadDiv("collectionList")' class='twist'>
<img align="absmiddle" id="twist_collectionList" src="/cyanos/images/twist-closed.png" /> Preservation List</a>
<div class="hideSection" id="div_collectionList">
<jsp:include page="/preserve/list.jsp"/>
<p align="center"><a href="<%= request.getContextPath()  %>/preserve/add.jsp">Add Preservation</a></p>
</div></div>
