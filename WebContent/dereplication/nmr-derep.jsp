<%@ page import="edu.uic.orjala.cyanos.web.servlet.DereplicationServlet,edu.uic.orjala.cyanos.sql.SQLCompound, java.util.List, java.util.ArrayList,	edu.uic.orjala.cyanos.User, edu.uic.orjala.cyanos.Role" %>
<%@ taglib prefix="cyanos" tagdir="/WEB-INF/tags" %>
<div class="selectSection">
<a class="twist">
<input type="checkbox" name="nmrdata" onclick="selectDiv(this)" <%= request.getParameter("nmrdata") != null ? "checked" : "" %>> NMR</a>
<%
List<String> aromSubs = new ArrayList<String>();
String[] aroms = request.getParameterValues("arom_sub");
if ( aroms != null ) {
	   for (String a : aroms ) {
			aromSubs.add(a);
	   }
}

if ( request.getParameter(DereplicationServlet.SEARCH_ACTION) != null && request.getParameter("nmrdata") != null ) {

	DereplicationServlet.parseGraph(request, "ch2_methyl", "compound_diatomic as ch2me", 
			"ch2me.compound_id = compound.compound_id", 
			"ch2me.atom1_h = 3 AND ch2me.atom1_element = 'C' AND ch2me.atom2_h=2 AND ch2me.atom2_element = 'C'", 
			"COUNT(DISTINCT ch2me.atom1_number)");
	
	DereplicationServlet.parseGraph(request, "ch_methyl", "compound_diatomic as chme", 
			"chme.compound_id = compound.compound_id", 
			"chme.atom1_h = 3 AND chme.atom1_element = 'C' AND chme.atom2_h=1 AND chme.atom2_element = 'C'", 
			"COUNT(DISTINCT chme.atom1_number)");
	
	DereplicationServlet.parseGraph(request, "c_methyl", "compound_diatomic as cme", 
			"cme.compound_id = compound.compound_id", 
			"cme.atom1_h = 3 AND cme.atom1_element = 'C' AND cme.atom2_h=0 AND cme.atom2_element = 'C'", 
			"COUNT(DISTINCT cme.atom1_number)");
	
	DereplicationServlet.parseGraph(request, "ome", "compound_diatomic as ome", 
			"ome.compound_id = compound.compound_id", 
			"ome.atom1_h = 3 AND ome.atom1_element = 'C' AND ome.atom2_element = 'O'", 
			"COUNT(DISTINCT ome.atom1_number)");
	
	DereplicationServlet.parseGraph(request, "nme", "compound_diatomic as nme", 
			"nme.compound_id = compound.compound_id", 
			"nme.atom1_h = 3 AND nme.atom1_element = 'C' AND nme.atom2_element = 'N'", 
			"COUNT(DISTINCT nme.atom1_number)");
	
	DereplicationServlet.parseGraph(request, "exo_me", "compound_diatomic as exoch2", 
			"exoch2.compound_id = compound.compound_id", 
			"exoch2.atom1_h = 2 AND exoch2.atom1_element = 'C' AND exoch2.atom2_element = 'C' AND exoch2.bond_order = 2 AND exoch2.atom2_h = 0", 
			"COUNT(DISTINCT exoch2.atom1_number)");
	
	DereplicationServlet.parseGraph(request, "ene_disub", "compound_diatomic as ene_disub", 
			"ene_disub.compound_id = compound.compound_id", 
			"ene_disub.atom1_h = 1 AND ene_disub.atom1_element = 'C' AND ene_disub.atom2_element = 'C' AND ene_disub.bond_order = 2 AND ene_disub.atom2_h = 1", 
			"COUNT(DISTINCT ene_disub.atom1_number)");
	
	DereplicationServlet.parseGraph(request, "exo_vinyl", "compound_diatomic as vinyl", 
			"vinyl.compound_id = compound.compound_id", 
			"vinyl.atom1_h = 2 AND vinyl.atom1_element = 'C' AND vinyl.atom2_element = 'C' AND vinyl.bond_order = 2 AND vinyl.atom2_h = 1", 
			"COUNT(DISTINCT vinyl.atom1_number)");

	DereplicationServlet.parseGraph(request, "aldehyde", "compound_diatomic as al", 
			"al.compound_id = compound.compound_id", 
			"al.atom1_h = 1 AND al.atom1_element = 'C' AND al.atom2_element = 'O' AND al.bond_order = 2", 
			"COUNT(DISTINCT al.atom1_number)");
	
	DereplicationServlet.parseGraph(request, "amide", "compound_triatomic as amide", 
			"amide.compound_id = compound.compound_id", 
			"amide.atom1_h = 3 AND amide.atom1_element = 'C' AND amide.atom2_element = 'N' and amide.atom3_element = 'O' AND bond2_order = 2",
			"COUNT(DISTINCT amide.atom1_number)");

	DereplicationServlet.parseGraph(request, "acetyl", "compound_triatomic as oac", 
			"oac.compound_id = compound.compound_id", 
			"oac.atom1_h = 3 AND oac.atom1_element = 'C' AND oac.atom2_element = 'C' and oac.atom3_element = 'O' AND bond2_order = 2",
			"COUNT(DISTINCT oac.atom1_number)");
	
	String value = request.getParameter("arom");
	if ( value != null && value.length() > 0 ) {
		DereplicationServlet.addTable(request, "compound_aromatic_h AS arom", "arom.compound_id = compound.compound_id");
		if ( value.endsWith("+") ) {
			DereplicationServlet.addQuery(request, "arom.aromatic_h >= ".concat(value.substring(0, value.length() - 1)));
		} else if ( value.matches("([0-9]+)\\-([0-9]+)") ) {
			String[] vals = value.split("\\-");
			DereplicationServlet.addQuery(request,"arom.aromatic_h >= ".concat(vals[0]));
			DereplicationServlet.addQuery(request, "arom.aromatic_h  <= ".concat(vals[1]));
		} else if ( value.startsWith("-") ) {
			DereplicationServlet.addQuery(request, "arom.aromatic_h  <= ".concat(value.substring(1)));				
		} else if ( value.length() > 0 ) {
			DereplicationServlet.addQuery(request, "arom.aromatic_h  = ".concat(value));
		}
	}
	
	if ( aroms != null ) {
		DereplicationServlet.addTable(request, "compound_aromatic_ring AS arom_sub", "arom_sub.compound_id = compound.compound_id");
		if ( aromSubs.contains("0") ) {
			DereplicationServlet.addQuery(request, "arom_sub.atom1_H = 0 AND arom_sub.atom2_H = 2 AND arom_sub.atom3_H = 1 AND arom_sub.atom4_H = 1 AND arom_sub.atom5_H = 1 AND arom_sub.atom6_H = 1");
		} else {
			StringBuffer query = new StringBuffer("arom_sub.atom1_H = 0");
			String[] atoms = {"2", "3", "4", "5", "6"};
			for ( String pos : atoms ) {
				query.append(String.format(" AND arom_sub.atom%s_H = %d", pos, (aromSubs.contains(pos) ? 0 : 1)));
			}
			DereplicationServlet.addQuery(request, query.toString());
		}
	}
	
} %><div class="<%= request.getParameter("nmrdata") != null ? "show" : "hide" %>Section" id="div_nmrdata">
<p align="center" style="font-size:12pt; font-style:italic;">Give number or range for signals observed, e.g. 3, 2-5, or 1+.  Blank fields will be ignored.</p>
<table class="dashboard">
<tr><th class="header">Methyl</th><td width="10px"></td><th class="header">sp2 Carbons</th></tr>
<tr><td><table class="species">
<tr align="center"><td>Triplet<br>(-CH<sub>2</sub>-<b>CH<sub>3</sub></b>)</td><td>Doublet<br>(-CH-<b>CH<sub>3</sub></b>)</td><td>Singlet<br>(-C-<b>CH<sub>3</sub></b>)</td></tr>
<tr align="center"><td><cyanos:text-field name="ch2_methyl" size="5">title="Give number or range, e.g. 3, 2-5, or 1+"</cyanos:text-field></td>
<td><cyanos:text-field name="ch_methyl" size="5">title="Give number or range, e.g. 3, 2-5, or 1+"</cyanos:text-field></td>
<td><cyanos:text-field name="c_methyl" size="5">title="Give number or range, e.g. 3, 2-5, or 1+"</cyanos:text-field></td></tr>
<tr align="center"><td>O-methyl<br>(-O-<b>CH<sub>3</sub></b>)</td><td>N-methyl<br>(-N-<b>CH<sub>3</sub></b>)</td><td>Acetyl<br>(-OOC-C<b>H<sub>3</sub></b>)</td></tr>
<tr align="center"><td><cyanos:text-field name="ome" size="5">title="Give number or range, e.g. 3, 2-5, or 1+"</cyanos:text-field></td>
	<td><cyanos:text-field name="nme" size="5">title="Give number or range, e.g. 3, 2-5, or 1+"</cyanos:text-field></td>
	<td><cyanos:text-field name="acetyl" size="5">title="Give number or range, e.g. 3, 2-5, or 1+"</cyanos:text-field></td></tr>
</table></td><td></td>
<td><table class="species">
<tr align="center"><td>-C=<b>CH<sub>2</sub></b></td><td>-CH=<b>CH</b></td><td>-CH=<b>CH<sub>2</sub></b></td></tr>
<tr align="center"><td><cyanos:text-field name="exo_me" size="5">title="Give number or range, e.g. 3, 2-5, or 1+"</cyanos:text-field></td>
<td><cyanos:text-field name="ene_disub" size="5">title="Give number or range, e.g. 3, 2-5, or 1+"</cyanos:text-field></td><td><cyanos:text-field size="5" name="exo_vinyl"/></td></tr>
</table></td></tr>
<tr><th class="header">Aromatic Rings</th><td width="10px"></td><th class="header">Miscellaneous</th></tr>
<tr><td><table class="species">
<tr align="center"><td>Aromatic Protons</td><td rowspan="2"><img src="<%= request.getContextPath() %>/images/arom_sub.png"></td><td>Substitutions</td></tr>
<tr align="center"><td><cyanos:text-field name="arom" size="5">title="Give number or range, e.g. 3, 2-5, or 1+"</cyanos:text-field></td>
<td><input type="checkbox" name="arom_sub" value="0" <%= aromSubs.contains("0") ? "checked" : "" %>>unsubstituted<br>
<input type="checkbox" name="arom_sub" value="2" <%= aromSubs.contains("2") ? "checked" : "" %>>2
<input type="checkbox" name="arom_sub" value="3" <%= aromSubs.contains("3") ? "checked" : "" %>>3 
<input type="checkbox" name="arom_sub" value="4" <%= aromSubs.contains("4") ? "checked" : "" %>>4 
<input type="checkbox" name="arom_sub" value="5" <%= aromSubs.contains("5") ? "checked" : "" %>>5 
<input type="checkbox" name="arom_sub" value="6" <%= aromSubs.contains("6") ? "checked" : "" %>>6 </td></tr>
</table></td><td></td>
<td><table class="species">
<tr align="center"><td>Aldehyde<br>-C-<b>CHO</b></td>
<td>Amide<br>-CO-N<b>H</b></td>
<tr align="center"><td><cyanos:text-field name="aldehyde" size="5">title="Give number or range, e.g. 3, 2-5, or 1+"</cyanos:text-field></td>
<td><cyanos:text-field name="amide" size="5">title="Give number or range, e.g. 3, 2-5, or 1+"</cyanos:text-field></td>
</tr>
</table></td></tr>
</table>
<% if ( DereplicationServlet.getUser(request).isAllowed(User.SAMPLE_ROLE, User.GLOBAL_PROJECT, Role.CREATE)  )  { %>
<p align="center"><a href="?rebuildGraph">Rebuild chemical structure index</a><br>(Run only in cases where structure data is inconsistent)</p>
<% } %>
</div></div>