<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.Sample,
	edu.uic.orjala.cyanos.SampleCollection,
	edu.uic.orjala.cyanos.sql.SQLSample,
	edu.uic.orjala.cyanos.web.servlet.SampleServlet" %>
<%  String contextPath = request.getContextPath();  %>
<form>
<p style='margin-left:2cm; margin-right:2cm;'>Use this form to generate a list of samples from four source collections that are interlaced into a 
single collection, e.g. 4 x 96 well plates to a 384 well plate.</p>
<p style='margin-left:2cm; margin-right:2cm;'><b>NOTE: </b> This will NOT create a new sample collection.  It will only generate a spreadsheet that can be used to either move or create a daughter plate.</p>
<table style="margin-left:2cm;">
<tr><td>Destination ID:</td><td><input type="text" name="destID"></td></tr>
<tr><td>Sources:</td><td>
<table align="left">
<tr><td bgcolor="#FFFF90">Plate 1: <select name="source1"></select></td></tr>
<tr><td bgcolor="#90FF90">Plate 2: <select name="source2"></select></td></tr>
<tr><td bgcolor="cyan">Plate 3: <select name="source3"></select></td></tr>
<tr><td bgcolor="#FF9090">Plate 4: <select name="source4"></select></td></tr>
</table>
<img src="<%= contextPath %>/images/bywell.png" valign="middle"></td></tr>
<tr><td>Amount:</td><td><input type="text" name="amount"></td></tr>
<tr><td colspan="2" align="center"><button type="submit" name="interlaceCols">Export</button><button type="reset">Reset</button></td></tr>
</table>
</form>