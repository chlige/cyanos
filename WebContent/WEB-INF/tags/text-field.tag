<%@ tag language="java" pageEncoding="UTF-8"%>
<%@ attribute name="name" required="true" %>
<%@ attribute name="value" required="false" %>
<%@ attribute name="size" required="false" %>
<% if ( jspContext.getAttribute("value") == null ) {
	String value = request.getParameter((String) jspContext.getAttribute("name"));
	if ( value == null ) { value = "";	}
	jspContext.setAttribute("value", value);	
} 
	String size = (String) jspContext.getAttribute("size");
%>
<input type="text" <%= (size != null ? String.format("size=\"%s\"", size) : "") %> name="${name}" value="${value}" <jsp:doBody/> >