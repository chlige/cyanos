<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="f" %>
<%@ page import="edu.uic.orjala.cyanos.web.servlet.ProjectServlet,
	java.text.SimpleDateFormat,
	edu.uic.orjala.cyanos.web.BaseForm,
	edu.uic.orjala.cyanos.Project,
	edu.uic.orjala.cyanos.Project.UpdateHost,
	edu.uic.orjala.cyanos.DataException,
	java.security.PublicKey,
	edu.uic.orjala.cyanos.sql.SQLProject,
	java.util.regex.Matcher,
	java.util.Map,
	java.util.Date,
	java.security.KeyFactory,
	java.security.spec.X509EncodedKeySpec,
	org.apache.commons.codec.binary.Base64,
	java.text.DateFormat" %>
<% 	String contextPath = request.getContextPath();
	Project myObject = (Project) request.getAttribute(ProjectServlet.PROJECT_OBJECT); 	
	boolean update = request.getParameter("updateProject") != null;
	if ( myObject == null ) { %>
<p align='center'><b>ERROR:</b> Object not passed</p>
<% out.flush(); return; } else if ( ! myObject.first() ) { %>
<p align='center'><b>ERROR:</b> Object not found</p>
<% out.flush(); return; } %>
<p align="CENTER"><font size="+3" ><c:out value="<%= myObject.getName() %>" default="<%= myObject.getID() %>"/></font>
<div CLASS="showSection" ID="view_info">
<table class="species" align='center'>
<tr><td width='125'>Project ID:</td><td><%= myObject.getID() %></td></tr>
<tr 
<% if ( update ) { 
	String value = request.getParameter("title");
	if (value != null && (! value.equals(myObject.getName()) ) ) {
		myObject.setName(value);	
%>
class="updated"
<% } } %>
><td>Title:</td><td><%= myObject.getName() %></td></tr>
<% String masterURL = myObject.getMasterURL(); 
	Map<String,Integer> updatePrefs = myObject.getUpdatePrefs(); %>
<tr
<% if ( update ) { 
	String value = request.getParameter("masterURL");
	if (value != null && (! value.equals(masterURL) ) ) {
		myObject.setMasterURL(value);	
		masterURL = myObject.getMasterURL();
%>
class="updated"
<% } } %>
><td>Master Server:</td><td><a href="<%= masterURL %>"><c:out value="<%= masterURL %>"/></a></td></tr>
<% if ( update ) { 
	String value = request.getParameter("masterKey");
	String updateCert = myObject.getUpdateCert();
	if ( value != null && (! (value.length() == 0 && updateCert == null) ) && (! value.equals(updateCert)) ) {
		myObject.setUpdateCert(value);
		%>
<tr class="updated"><td colspan="2">Updated Master Server Certificate</td></tr>
<% } 
	String[] params = {"pref_collection", "pref_strain", "pref_material", "pref_assay" };
	String[] classes = {ProjectServlet.UPDATE_CLASS_COLLECTION, ProjectServlet.UPDATE_CLASS_STRAIN, ProjectServlet.UPDATE_CLASS_MATERIAL, ProjectServlet.UPDATE_CLASS_ASSAY}; 
	
	for ( int i = 0; i < params.length; i++ ) {
		String[] values = request.getParameterValues(params[i]);
		if ( values != null && values.length > 0 ) {
			int bits= 0;
			try {
				for ( String bit : values ) {
					bits = bits + Integer.parseInt(bit);
				}
				if ( myObject.getUpdatePrefs(classes[i]) != bits ) {
					myObject.setUpdatePrefs(classes[i], bits); 
					out.print("<tr class='updated'><td colspan='2'>Updated update preferences (");
					out.print(classes[i]);
					out.println(")</td></tr>");
				}
			} catch (NumberFormatException e) {
				out.print("<tr class='updated'><td colspan='2'>ERROR updating preferences (");
				out.print(classes[i]);
				out.print(") ");
				out.print(e.getLocalizedMessage());
				out.println("</td></tr>");
			}
		}
	}	
} %>
<% if ( masterURL != null ) { Date lastUpdate = myObject.getLastUpdateSent();  if ( lastUpdate != null ) { %>
<tr><td>Last Update Sent:</td><td><% if ( lastUpdate.getTime() > 0 ) { 
		out.print(DateFormat.getDateTimeInstance().format(lastUpdate)); 
		out.println("<br>");
		out.print(myObject.getLastUpdateMessage());
	} else { out.print("NEVER"); } %></td></tr>
<% } } %>
<tr <% if ( update ) { 
	String value = request.getParameter("notes");
	if (value != null && (! value.equals(myObject.getNotes()) ) ) {
		myObject.setNotes(value);	
%>
class="updated"
<% } } %>><td valign=top>Description:</td><td><%= BaseForm.formatStringHTML(myObject.getNotes()) %></td></tr>
</table>
<% if ( update && masterURL == null ) { 
		String[] hostIDs = request.getParameterValues("del_host");
		if ( hostIDs != null ) {
		for ( String hostID : hostIDs ) {
			myObject.removeHost(hostID);	
		} }
		String newHostID = request.getParameter("new_hostid");
		String keyString = request.getParameter("new_hostkey");
		if ( newHostID != null && newHostID.length() > 0 ) {
			try {
				myObject.addUpdateHost(newHostID, request.getParameter("new_hostname"), request.getParameter("new_hostkey"));	
			} catch (DataException e) { %> 
<p align="center"><b>ERROR:</b> Unable to add a new slave entry.<br>
<%= e.getLocalizedMessage() %></p>
<%			}
		}
	}
	UpdateHost hosts = myObject.getHosts(); 
	if ( hosts.first() ) { hosts.beforeFirst(); %>
<hr width="50%">
<h3 align="center">Slave systems</h3>
<table >
<tr><th>Hostname</th><th>Host ID</th><td width=100></td></tr>
<% while ( hosts.next() ) { %>
<tr><td><%= hosts.getHostName() %></td><td><%= hosts.getHostID() %></td>
<td><button type="button" onClick="showHideButton(this, 'key_<%= hosts.getHostID() %>');">Show</button>
<div id="key_<%= hosts.getHostID() %>" class="hideSection" style="text-align:center; position:absolute; z-index:99;">
<div class='pemData' style="background-color:#FFFFFF;"><code><%= hosts.getPublicKeyString().replace("\n", "<br>") %></code></div></div></td></tr>
<% } %>
</table>
<% } %>
<p align='center'><button type='button' onClick='flipDiv("info")'>Edit Values</button></p>
<%--
<% SimpleDateFormat tsFormat = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss"); %>
<form method="get" action="project">
<input type="hidden" name="id" value="<%= myObject.getID() %>"> 
<p align='center'><b>Project Update</b><br>
Updates since: <input type="text" name="since" value="<%= tsFormat.format(myObject.getLastUpdateSent()) %>"><button type="submit" name="<%= ProjectServlet.ACTION_GEN_XML %>">Download</button></p></form>
--%>
</div>
<div class='hideSection' id="edit_info">
<form name='editProject' method="post" action="project">
<input type="hidden" name="id" value="<%= myObject.getID() %>">
<table class="species" align='center'>
<tr><td width='150'>Project ID:</td><td><%= myObject.getID() %></td></tr>
<tr><td>Title:</td><td><input type='text' name='label' value='<%= myObject.getName() %>' size="75"></td></tr>
<tr><td valign=top>Notes:</td><td><textarea rows="7" cols="70" name="notes"><c:out value="<%= myObject.getNotes() %>" default="" /></textarea></td></tr>
<% if ( ! hosts.first() ) { %>
<tr><td colspan=2><hr width="50%"><h3 align="center">Master server</h3></td></tr>
<tr><td>Master Server URL:</td><td><input type='text' name='masterURL' value='<c:out value="<%= masterURL %>" default=""/>' size="75"></td></tr> 
<tr><td valign="top">Master Server Public Key:</td><td><textarea rows="12" cols="65" name='masterKey'><c:out value="<%= myObject.getUpdateCert() %>" default=""/></textarea></td></tr>
<tr><td>Update Preferences:</td><td>
<table class="species">

<% String[] options = {"No Update", "Send Only", "Receive Only", "Send/Receive"}; 
int[] optBits = { 0, Project.UPDATE_SEND, Project.UPDATE_RECEIVE, Project.UPDATE_SEND_RECIEVE}; 
int thisBit = myObject.getUpdatePrefs(ProjectServlet.UPDATE_CLASS_COLLECTION); %>
<tr><td>Collections:</td><td><select name="pref_collection">
<% for (int i = 0; i < optBits.length; i++) { %>
<option value="<%= optBits[i] %>" <%= ( optBits[i] == thisBit ? "SELECTED" : "") %>><%= options[i] %></option>
<% } %>
</select></td></tr>
<% thisBit = myObject.getUpdatePrefs(ProjectServlet.UPDATE_CLASS_STRAIN); %>
<tr><td>Strains:</td><td><select name="pref_strain">
<% for (int i = 0; i < optBits.length; i++) { %>
<option value="<%= optBits[i] %>" <%= ( optBits[i] == thisBit ? "SELECTED" : "") %>><%= options[i] %></option>
<% } %>
</select></td></tr>
<% thisBit = myObject.getUpdatePrefs(ProjectServlet.UPDATE_CLASS_MATERIAL); 
	boolean localOnly = ( thisBit > Project.UPDATE_RECEIVE_LOCAL_ONLY );
	if ( localOnly ) thisBit -= Project.UPDATE_RECEIVE_LOCAL_ONLY;  %>
<tr><td>Materials:</td><td><select name="pref_material">
<% for (int i = 0; i < optBits.length; i++) { %>
<option value="<%= optBits[i] %>" <%= ( optBits[i] == thisBit ? "SELECTED" : "") %>><%= options[i] %></option>
<% } %>
</select> 
<input type="checkbox" value="<%= Project.UPDATE_RECEIVE_LOCAL_ONLY  %>" name="pref_material" <%= (localOnly ? "CHECKED" :"") %>> Only receive data for local strains.
</td></tr>
<% thisBit = myObject.getUpdatePrefs(ProjectServlet.UPDATE_CLASS_ASSAY); 
	localOnly = ( thisBit > Project.UPDATE_RECEIVE_LOCAL_ONLY );
	if ( localOnly ) thisBit -= Project.UPDATE_RECEIVE_LOCAL_ONLY;  %>
<tr><td>Assays:</td><td><select name="pref_assay">
<% for (int i = 0; i < optBits.length; i++) { %>
<option value="<%= optBits[i] %>" <%= ( optBits[i] == thisBit ? "SELECTED" : "") %>><%= options[i] %></option>
<% } %>
</select> 
<input type="checkbox" value="<%= Project.UPDATE_RECEIVE_LOCAL_ONLY  %>" name="pref_assay" <%= (localOnly ? "CHECKED" :"") %>> Only receive data for local strains.
</td></tr>
</table>
</td></tr>
<% }	
	if ( masterURL == null ) { %>
<tr><td colspan=2><hr width="50%"><h3 align="center">Slave systems</h3></td></tr>	
<% if ( hosts.first() ) { hosts.beforeFirst(); %>
<tr><td colspan="2" align="center"><table>
<tr><th>Hostname</th><th>Host ID</th><th>Remove</th></tr>
<% while ( hosts.next() ) { %>
<tr><td><%= hosts.getHostName() %></td><td><%= hosts.getHostID() %></td>
<td><input type="checkbox" name="del_host" value="<%= hosts.getHostID() %>"></tr>
<% } %>
</table></td></tr>
<% } %> 
<tr><td colspan=2 align="center"><b>New Slave</b></td></tr>
<tr><td>Hostname:</td><td><input type="text" name="new_hostname" size=50></td>
<tr><td>Host UUID:</td><td><input type="text" name="new_hostid" size=75></td>
<tr><td valign="top">Host Public Key:</td><td><textarea rows="12" cols="65" name='new_hostkey'></textarea></td></tr>	
<% } %>
</table>
<!--  <p align="CENTER"><button type="button" name="updateProject" onClick="updateForm(this,'<%= ProjectServlet.INFO_FORM_DIV_ID %>')">Update</button> -->
<p align="CENTER"><button type="submit" name="updateProject">Update</button>
<input type="RESET"></p>
</form>
<p align="center"><button type='button' onClick='flipDiv("info")'>Close Form</button></p>
</div>
