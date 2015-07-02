<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ attribute name="title" required="true" %>
<% String contextPath = request.getContextPath(); %>
<meta charset="UTF-8">
<link rel="stylesheet" type="text/css" href="<%= contextPath %>/cyanos.css"/>
<script type="text/javascript" src="<%= contextPath %>/cyanos.js"></script>
<script type="text/javascript" src="<%= contextPath %>/cyanos-date.js"></script>
<title>${title}</title>
<meta name="viewport" content="width=device-width">
<jsp:doBody/>
