<%@ page import="edu.uic.orjala.cyanos.web.AppConfig,edu.uic.orjala.cyanos.web.servlet.AdminServlet" %>
<div><h2>Configuration</h2>
<form method="post">
<p><button type="submit" name="<%= AdminServlet.PARAM_CONFIG_DOWNLOAD_FILE %>">Download configuration</button>
<button type="submit" name="<%= AdminServlet.PARAM_CONFIG_REVERT %>">Revert to saved configuration</button></p>
</form>
<form enctype="multipart/form-data" method="post" action="?">
<input type="hidden" name="form" value="savereload">
<p><b>File to upload (XML configuration):</b><input type="file" name="<%= AdminServlet.PARAM_CONFIG_UPLOAD_FILE %>" size="25">
<button type="submit">Upload XML File</button></p></form></div>