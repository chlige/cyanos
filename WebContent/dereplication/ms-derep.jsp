<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page import="edu.uic.orjala.cyanos.web.servlet.DereplicationServlet,
edu.uic.orjala.cyanos.sql.SQLCompound,
org.openscience.cdk.Molecule,
org.openscience.cdk.tools.MFAnalyser, java.util.List, java.util.ArrayList" %>
<div class="selectSection" >
<a class="twist">
<input type="checkbox" name="msdata" onclick="selectDiv(this)" <%= request.getParameter("msdata") != null ? "checked" : "" %>> Mass Spec</a>
<% if ( request.getParameter(DereplicationServlet.SEARCH_ACTION) != null && request.getParameter("msdata") != null ) {
	
	StringBuffer query = new StringBuffer();
	String sign = request.getParameter("msMode");
	String column = SQLCompound.TABLE + "." + SQLCompound.MONOISOTOPIC_MASS_COLUMN;
	
	double mass = Double.parseDouble(request.getParameter("mass"));
	
	double diff = 0.0f;
	
	if ( request.getParameter("diffUnit").equals("Da") ) {
		diff = Double.parseDouble(request.getParameter("diff"));
	} else if ( request.getParameter("diff").length() > 0 ) {
		diff = ( Double.parseDouble(request.getParameter("diff")) * mass ) / 1000000;
	} else {
		diff = ( 10 * mass ) / 1000000;
	}

	query.append(" ");
	query.append(column);
	query.append(" IS NOT NULL AND (");
	
	String[] adducts = request.getParameterValues("adduct");
	
	for ( int i = 0; i < adducts.length; i++ ) {
		double addMass = 0.0d;
		if ( adducts[i].equals("H") ) {
			addMass = 1.007825d;
		} else if ( adducts[i].equals("Na") ) {
			addMass = 22.989769d;
		} else if ( adducts[i].equals("NH4") ) {
			addMass = 18.0344d;
		} else if ( adducts[i].equals("K") ) {
			addMass = 38.9637d;
		}
		query.append(String.format("ABS(%s - (%s %s %.5f)) < %.5f", mass, column, sign, addMass, diff));
		if ( i < (adducts.length - 1) ) 
			query.append(" OR ");
	}
	
	String customAdduct = request.getParameter("customAdduct");
	
	if ( customAdduct != null && customAdduct.length() > 0 ) {
		if ( adducts.length > 0 ) query.append(" OR ");
		Molecule aMolecule = new Molecule();
		MFAnalyser anAnalyzer = new MFAnalyser(customAdduct, aMolecule);
		if ( anAnalyzer != null ) {
			query.append(String.format("ABS(%s - (%s %s %.5f)) < %.5f", mass, column, sign, anAnalyzer.getMass(), diff));
		}
	}
	query.append(")");
	
	DereplicationServlet.addQuery(request, query.toString());
	
} %><div class="<%= request.getParameter("msdata") != null ? "show" : "hide" %>Section" id="div_msdata">
<p align="CENTER">
<% String msmode = request.getParameter("msMode"); %>
<select name="msMode">
<option value="+" <%= "+".equals(msmode) ? "selected" : ""%>>Positive Mode</option>
<option value="-" <%= "-".equals(msmode) ? "selected" : ""%>>Negative Mode</option>
</select><i>m/z</i> <input type="text" name="mass" value="<c:out value='<%= request.getParameter("mass") %>'/>"> 
± <input type="text" size="5" name="diff" value="<%= request.getParameter("diff") != null ? request.getParameter("diff") : "10" %>">
<% String diffUnit = request.getParameter("diffUnit"); %>
<select name="diffUnit">
<option value="ppm" <%= "ppm".equals(diffUnit) ? "selected" : ""%>>ppm</option>
<option value="Da" <%= "Da".equals(diffUnit) ? "selected" : ""%>>Da</option>
</select></p>

<h3 style="text-align:center">Adducts</h3>
<% String[] adducts = request.getParameterValues("adduct");  
	List<String> addList = new ArrayList<String>(5);
	
	if ( adducts != null ) {
		for ( String adduct : adducts) {
			addList.add(adduct);	
		}
	} else {
		addList.add("M");
		addList.add("H");
	}
%>
<table align="center">
<tbody>
<tr align="center"><td>M<input type="checkbox" name="adduct" value="M" <%= ( addList.contains("M") ? "checked" : "") %>></td>
<td>&plusmn;H<input type="checkbox" name="adduct" value="H" <%= ( addList.contains("H") ? "checked" : "") %>></td>
<td>Na<input type="checkbox" name="adduct" value="Na" <%= ( addList.contains("Na") ? "checked" : "") %>></td>
<td>NH<sub>4</sub><input type="checkbox" name="adduct" value="NH4" <%= ( addList.contains("NH4") ? "checked" : "") %>></td>
<td>K<input type="checkbox" name="adduct" value="K" <%= ( addList.contains("K") ? "checked" : "") %>></td>
<td>Custom: <input type="text" name="customAdduct"></td></tr></tbody></table>
</div></div>