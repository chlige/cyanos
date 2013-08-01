<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page import="edu.uic.orjala.cyanos.Strain,
	edu.uic.orjala.cyanos.web.servlet.StrainServlet,
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
	Strain strainObj = (Strain) request.getAttribute(StrainServlet.STRAIN_OBJECT); 	
	if ( strainObj == null ) { %>
<p align='center'><b>ERROR:</b> Object not passed</p>
<% out.flush(); return; } else if ( (! strainObj.isLoaded()) && strainObj.first() ) { %>
<p align='center'><b>ERROR:</b> Object not found</p>
<% out.flush(); return; } %>
<div style="width:90%; margin-left: 5%; margin-right: 5%" id="<%= StrainServlet.PHOTO_DIV_ID %>">
<% if ( strainObj.isAllowed(Role.WRITE) && request.getParameter("cancelBrowser") == null && request.getParameter("showBrowser") != null ) { %>
<jsp:include page="photolink.jsp"/>
<% } else { %>
<table class="species" style="width: 90%; margin-left:auto; margin-right:auto"><% int cols = 3;
	ExternalFile photos = strainObj.getPhotos();
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
			out.print("/file/preview/strain/photo/");
			out.print(filePath);
			out.print("\" border=0");
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
<% if ( strainObj.isAllowed(Role.WRITE) ) { %>
<form><input type="hidden" name="id" value="<%= strainObj.getID() %>">
<input type="hidden" name="div" value="<%= StrainServlet.PHOTO_DIV_ID %>">
<p align="center"><button type="button" onClick="updateForm(this,'<%= StrainServlet.PHOTO_DIV_ID %>')" name="showBrowser">Manage Photos</button></p>
</form>
<% } } %>
</div>
