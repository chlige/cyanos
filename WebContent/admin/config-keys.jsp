<%@ page import="edu.uic.orjala.cyanos.web.AppConfig, 
	edu.uic.orjala.cyanos.web.servlet.AdminServlet,
	edu.uic.orjala.cyanos.sql.SQLProject,
	java.security.KeyPair,
	java.security.GeneralSecurityException,
	java.net.URL,
	org.apache.commons.codec.binary.Base64,
	edu.uic.orjala.cyanos.web.listener.ProjectUpdateListener" %>
<div><h2>Update Keys</h2>
<p>Update keys are required by CYANOS to establish a relationship between two servers for project updates. 
Normal operation of CYANOS does not requires these keys.
However, if this server will be participating in project updates with a remote server, either as master or slave, then a set of update keys are <b>required</b>.</p>
<% AppConfig appConfig = (AppConfig) session.getAttribute(AdminServlet.APP_CONFIG_ATTR); 
if ( appConfig != null ) { %>
<p><b>Host ID:</b> <%= appConfig.getHostUUID() %></p>
<% URL url = new URL(request.getScheme(), request.getServerName(), request.getLocalPort(), request.getContextPath() ); %>
<p><b>Server URL:</b> <%= url.toString() %></p>
<% String pubKeyString = appConfig.getUpdateCert(); 
	if ( pubKeyString != null && pubKeyString.length() > 0 ) { %>
<p><b>Public Key</b></p>
<div class="pemData"><code><%  if ( ! pubKeyString.startsWith("-----BEGIN ") ) {
		try {
			pubKeyString = SQLProject.encodePublicKey(SQLProject.decodePublicKey(pubKeyString)); 
		} catch (GeneralSecurityException e) {
			out.println("ERROR: ");
			out.println(e.getLocalizedMessage());
		}
	}
	out.println(pubKeyString.replace("\n", "<br>"));%></code></div>
<ul>
<li>When configuring a <b>master</b> server, supply the <i>host ID</i> and <i>public key</i> of the slave server.</li>
<li>When configuring a <b>slave</b> server, supply the <i>server URL</i> and <i>public key</i> of the master server.</li>
</ul>
<% } else { %>
<p>An set of update keys do not exists for this server.</p>
<form method="post">
<input type="hidden" name="form" value="<%= request.getParameter("form") %>">
<p><button type="submit" name="<%= AdminServlet.PARAM_CONFIG_GEN_KEY_PAIR %>">Generate Update Keys</button></p>
</form>
<% } %>
<% } %>
</div>