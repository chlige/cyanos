<%
	if (request.getHeader("REFERER") != null && session != null && session.getAttribute("url") == null ) {
		String url = new String(request.getHeader("REFERER"));
		session.setAttribute("url", url);	
	}
%>
<html><head><title>Login Page</title>
<LINK REL="stylesheet" TYPE="text/css" HREF="
<%= request.getContextPath() %>/cyanos.css"/>
</head>
<body>
<font size='+2'>Please Login</font><hr>
<form action='j_security_check' method='post' target='_top'>
<table>
 <tr><td>Username:</td>
   <td><input type='text' name='j_username'></td></tr>
 <tr><td>Password:</td> 
   <td><input type='password' name='j_password' size='8'></td>
 </tr>
</table>
<br>
  <input type='submit' value='Login'><input type='reset' value='Reset'/>
</form><BR>
</body></html>
