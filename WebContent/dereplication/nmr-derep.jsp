<%@ page import="edu.uic.orjala.cyanos.web.servlet.DereplicationServlet,edu.uic.orjala.cyanos.sql.SQLCompound, java.util.List, java.util.ArrayList" %>
<%@ taglib prefix="cyanos" tagdir="/WEB-INF/tags" %>
<div class="selectSection">
<a name="nmrdata" class="twist">
<input type="checkbox" name="msdata" onclick="selectDiv(this)" <%= request.getParameter("nmrdata") != null ? "checked" : "" %>> NMR</a>
<%
if ( request.getParameter(DereplicationServlet.SEARCH_ACTION) != null && request.getParameter("nmrdata") != null ) {
	StringBuffer join = DereplicationServlet.getJoinBuffer(request);
	
	
} %>
<b>Methyl</b><br>
<table class="species">
<tr align="center"><td>Triplet<br>(-CH<sub>2</sub>-<b>CH<sub>3</sub></b>)</td>
	<td>Doublet<br>(-CH-<b>CH<sub>3</sub></b>)</td>
	<td>Singlet<br>(-C-<b>CH<sub>3</sub></b>)</td>
	<td>O-methyl<br>-O-<b>CH<sub>3</sub></b></td>
	<td>N-methyl<br>-N-<b>CH<sub>3</sub></b></td></tr>
<tr align="center"><td><cyanos:text-field name="ch2_methyl" size="5"/></td>
	<td><cyanos:text-field name="ch_methyl" size="5"/></td>
	<td><cyanos:text-field name="c_methyl" size="5"/></td>
	<td><cyanos:text-field name="ome" size="5"/></td>
	<td><cyanos:text-field name="nme" size="5"/></td></tr>
</table>

<b>sp2 Carbons</b><br>

<table class="species">
<tr align="center"><td>-C=<b>CH<sub>2</sub></b></td><td>-CH=<b>CH</b></td><td>-CH=<b>CH<sub>2</sub></b></td></tr>
<tr align="center"><td><cyanos:text-field name="exo_me" size="5"/></td>
<td><cyanos:text-field name="ene_disub" size="5"/></td><td><cyanos:text-field size="5" name="exo_vinyl"/></td></tr>
</table>

<b>Aromatic Rings</b><br>
<% List<String> aromSubs = new ArrayList<String>();
   String[] aroms = request.getParameterValues("arom_sub");
   if ( aroms != null ) {
	   for (String a : aroms ) {
			aromSubs.add(a);
	   }
   }
%>
<table class="species">
<tr align="center"><td>Aromatic Protons</td><td>Substitutions</td><td rowspan="2"><img height="67" src="<%= request.getContextPath() %>/images/arom_sub.png"></td></tr>
<tr align="center"><td><cyanos:text-field name="arom" size="5"/></td>
<td><input type="checkbox" name="arom_sub" value="0" <%= aromSubs.contains("0") ? "checked" : "" %>>unsubstituted<br>
<input type="checkbox" name="arom_sub" value="2" <%= aromSubs.contains("2") ? "checked" : "" %>>2 
<input type="checkbox" name="arom_sub" value="3" <%= aromSubs.contains("3") ? "checked" : "" %>>3 
<input type="checkbox" name="arom_sub" value="4" <%= aromSubs.contains("4") ? "checked" : "" %>>4 
<input type="checkbox" name="arom_sub" value="5" <%= aromSubs.contains("5") ? "checked" : "" %>>5 
<input type="checkbox" name="arom_sub" value="6" <%= aromSubs.contains("6") ? "checked" : "" %>>6 </td></tr>
</table>

<b>Special</b><br>
<table class="species">
<tr align="center"><td>Aldehyde<br>-C-<b>CHO</b></td>
<td>Amide<br>-CO-N<b>H</b></td>
<td>Acetyl<br>-OOC-C<b>H<sub>3</sub></b></td></tr>
<tr align="center"><td><cyanos:text-field name="aldehyde" size="5"/></td>
<td><cyanos:text-field name="amide" size="5"/></td>
<td><cyanos:text-field name="acetyl" size="5"/></td></tr>
</table>

</div>