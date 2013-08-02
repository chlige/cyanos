<%
	if ( request.getParameter("url") != null) {
		String url = (String)request.getParameter("url");
		response.sendRedirect(url);
	} else {
		response.sendRedirect("main");
	}
%>
