<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="edu.uic.orjala.cyanos.Collection,
	edu.uic.orjala.cyanos.web.servlet.CollectionServlet,
	java.text.SimpleDateFormat,
	java.util.Date,
	edu.uic.orjala.cyanos.Role,
	edu.uic.orjala.cyanos.web.BaseForm,
	edu.uic.orjala.cyanos.ExternalFile,
	java.io.File" %>
<%!
	private String displaySize(long bytes) {
		int unit = 1000;
		if ( bytes < unit ) return String.format("%d B", bytes);
		int exp = (int) (Math.log(bytes) / Math.log(unit));
		String prefix = "kMGTPE".substring(exp-1, exp);
		return String.format("%.1f %sB", bytes / Math.pow(unit, exp), prefix);
	}%>
<% 	String contextPath = request.getContextPath();
	Collection collection = (Collection) request.getAttribute(CollectionServlet.ATTR_COLLECTION); 	
	if ( collection == null ) { %>
<p align='center'><b>ERROR:</b> Object not passed</p>
<% out.flush(); return; } else if ( (! collection.isLoaded()) && collection.first() ) { %>
<p align='center'><b>ERROR:</b> Object not found</p>
<% out.flush(); return; } %>
<div style="width:90%; margin-left: 5%; margin-right: 5%" id="<%= CollectionServlet.PHOTO_DIV_ID %>">
<% if ( collection.isAllowed(Role.WRITE) && request.getParameter("cancelBrowser") == null && request.getParameter("showBrowser") != null ) { %>
<iframe style="width:100%; height:750px; border: 1px solid gray; background: white; overflow:hidden;" src="collection/photolink.jsp?id=<%= collection.getID() %>"></iframe>
<form><input type="hidden" name="id" value="<%= collection.getID() %>">
<input type="hidden" name="div" value="<%= CollectionServlet.PHOTO_DIV_ID %>">
<p align="center"><button type="button" id="cancelPhotos" onClick="updateForm(this,'<%= CollectionServlet.PHOTO_DIV_ID %>')" NAME='cancelBrowser'>Close</button></p>
</form>
<% } else { %>
<table class="species" style="width: 90%; margin-left:auto; margin-right:auto"><% int cols = 3;
	ExternalFile photos = collection.getPhotos();
	if ( photos.first() ) {
		int cell = 1;
		photos.beforeFirst();
		while ( photos.next() ) {
			String filePath = photos.getFilePath();
			if ( cell == 1 ) out.println("<tr>");
			out.print("<td");
			if ( cols > 1 )
				out.print(" align=\"center\"");
			out.print("><a target=\"_blank\" href=\"");
			out.print(contextPath);
			out.print("/file/get/strain/photo/");
			out.print(filePath);
			out.print("\"><img src=\"");
			out.print(contextPath);
			out.print("/file/get/strain/photo/");
			out.print(filePath);
			out.print("\" border=0 width=200");
			if ( cols > 1 )
				out.print("><br>");
			else
				out.print(" align=middle>");
			out.print(photos.getDescription());
			out.print(" (");
			File thisFile = photos.getFileObject();
			long size = thisFile.length();
			out.print(this.displaySize(size));
			out.println(")</a></td>");

			if (cell == cols) {
				cell = 1;
				out.println("</td>");
			} else {
				cell++;				
			}
		}
	} else {
		out.print("<tr><th colspan=\"");
		out.print(cols);
		out.println("\">NONE</td></tr>");
	}
%>
</table>
<% if ( collection.isAllowed(Role.WRITE) ) { %>
<form><input type="hidden" name="id" value="<%= collection.getID() %>">
<input type="hidden" name="div" value="<%= CollectionServlet.PHOTO_DIV_ID %>">
<p align="center"><button type="button" onClick="updateForm(this,'<%= CollectionServlet.PHOTO_DIV_ID %>')" name="showBrowser">Manage Photos</button></p>
</form>
<% } } %>
</div>
