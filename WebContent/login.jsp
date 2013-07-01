<%
	if (session.getAttribute("url") != null) {
		String url = (String)session.getAttribute("url");
		session.removeAttribute("url");
		response.sendRedirect(url);
	} else {
		response.sendRedirect("main");
	}
%>
