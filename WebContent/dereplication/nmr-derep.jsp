<%@ page import="edu.uic.orjala.cyanos.web.servlet.DereplicationServlet,edu.uic.orjala.cyanos.sql.SQLCompound" %>
<div class="selectSection">
<a name="nmrdata" class="twist">
<input type="checkbox" name="msdata" onclick="selectDiv(this)" <%= request.getParameter("nmrdata") != null ? "checked" : "" %>> NMR</a>
<%
if ( request.getParameter(DereplicationServlet.SEARCH_ACTION) != null && request.getParameter("nmrdata") != null ) {
	StringBuffer query = (StringBuffer)request.getAttribute(DereplicationServlet.QUERY_ATTRIBUTE);
	
} %>

</div>