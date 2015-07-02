<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="cyanos" tagdir="/WEB-INF/tags" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.Taxon,
	edu.uic.orjala.cyanos.CyanosObject,
	edu.uic.orjala.cyanos.web.servlet.TaxonServlet,
	edu.uic.orjala.cyanos.web.servlet.StrainServlet,
	edu.uic.orjala.cyanos.web.BaseForm,
	java.io.IOException,
	edu.uic.orjala.cyanos.DataException" %>
<%!private void displayTaxon(JspWriter out, Taxon taxon) throws IOException, DataException {
		out.print("<div style=\"margin-left:7mm; margin-top:2mm\">");
		if ( taxon.isLast() ) 
			TaxonServlet.printTaxon(out, taxon);
		else
			TaxonServlet.printTaxonLink(out, taxon);
		if ( taxon.next() )
			displayTaxon(out, taxon);
		else {
			taxon.previous();
			Taxon kids = taxon.getChildren();
			if ( kids.first() ) {
				out.println("<hr style=\"width: 50%; border: 1px solid gray;\" align=\"left\"><ul style=\"list-style-type: none; margin-top:2mm;\">");
				kids.beforeFirst();
				while (kids.next()) {
					out.print("<li>");
					TaxonServlet.printTaxonLink(out, kids);
					out.println("</li>");
				}
				out.println("</ul>");
			}
		}
		out.println("</div>");
	}
%><!DOCTYPE html>
<html>
<head>
<cyanos:header title="Cyanos - Taxon Broswer"/>
</head>
<body>
<cyanos:menu helpModule="<%=TaxonServlet.HELP_MODULE%>"/>
<div class='content'>
<h1>Taxon Browser</h1>
<hr width="80%">
<div style="width: 80%; margin-left:10%">
<%
	Taxon taxon = (Taxon) request.getAttribute(TaxonServlet.TAXON_OBJECT); 
	if ( taxon != null && taxon.first() ) { 
	if ( request.getParameter(TaxonServlet.PARAM_NAME) != null ) {
%>
<p align="center"><a href="?">View all taxonomic roots</a></p>
<%
	Taxon lineage = taxon.getLinage();
%><div class="taxa">
<%
	if ( lineage.first() ) { displayTaxon(out,lineage); }
%></div>
<p align="center"><a href="?export&<%=TaxonServlet.PARAM_NAME%>=<%=request.getParameter(TaxonServlet.PARAM_NAME)%>">Export taxonomic records</a></p>
<hr align="center">
<jsp:include page="/taxon/taxon-strain-list.jsp"><jsp:param value="<%=StrainServlet.SEARCH_DIV_ID%>" name="div"/></jsp:include>
<%
	} else {
%>
<div style="margin-left:7mm; margin-top:2mm">
<%
	taxon.beforeFirst();  while ( taxon.next() ) { TaxonServlet.printTaxonLink(out, taxon); out.println("<br>"); }
%>
<p align="center"><a href="?export">Export taxonomic records</a></p>
</div>
<% } } %>
</div></div>
</body>
</html>