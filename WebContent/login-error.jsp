
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html><head><title>Login Page</title>
<link REL="stylesheet" TYPE="text/css" href="<%= request.getContextPath() %>/cyanos.css"/>
<script type="text/javascript">
	function showLogin() {
		var cover = document.getElementById("loginCover");  
		cover.style.height = window.screen.height; 
		cover.style.visibility = "visible";
	}
</script>
</head>
<body onLoad="showLogin()">
<div id="loginCover">
</div>
<div id="loginBox">
<div id="loginBoxForm">
<div id="loginForm"  style="visibility: visible">
<font size='+2'>CYANOS Login</font><hr>
<p style="color:red">Invalid username and/or password, please try again.</p>
<form action='j_security_check' method='post' target='_top'>
<table>
 <tr><td>Username:</td>
   <td><input type='text' name='j_username'></td></tr>
 <tr><td>Password:</td> 
   <td><input type='password' name='j_password' size='8'></td>
 </tr>
</table>
<br>
  <button type='submit'>Login</button><button type='reset'>Clear form</button>
</form>
<p><a href='<%= response.encodeURL("reset") %>'>Reset password</a></p></div>
<div id="loginNote" style="visibility: hidden">
<h2>Login error</h2>
<hr>
<p align="center">Your session has expired.</p>
<p align="center"><a href="">Login again.</a></p>

</div>
</div></div>
</body></html>