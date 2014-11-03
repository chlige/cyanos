<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="cyanos" tagdir="/WEB-INF/tags" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.Separation,
	edu.uic.orjala.cyanos.CyanosObject,
	edu.uic.orjala.cyanos.Material,edu.uic.orjala.cyanos.web.servlet.SeparationServlet,
	edu.uic.orjala.cyanos.web.BaseForm,
	java.math.BigDecimal,
	java.text.SimpleDateFormat" %>
<%  String contextPath = request.getContextPath();
	Separation sepObj = (Separation) request.getAttribute(SeparationServlet.SEP_OBJECT);
%><!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<cyanos:header title="Cyanos - Separation <%= sepObj != null && sepObj.first() ? sepObj.getID() : "Search" %>"/>
</head>
<body>
<cyanos:menu helpModule="separation"/>
<div class='content'>
<% if ( sepObj != null && sepObj.first() ) { %>
<p align="CENTER"><font size="+3" >Separation #<%= sepObj.getID() %> 
<% String tag = sepObj.getTag(); 
	if ( tag != null && tag.length() > 0 ) { %>
( <c:out value="<%= tag %>"/>)
<% } %>
</font>
<hr width="90%">
<jsp:include page="/separation/separation-form.jsp" />

<div CLASS="collapseSection"><A NAME='sep_source' CLASS='twist' onClick='loadDiv("sep_source")' CLASS='divTitle'>
<img align="ABSMIDDLE" ID="twist_sep_source" SRC="<%= contextPath %>/images/twist-open.png" /> Source Materials</A>
<div CLASS="showSection" ID="div_sep_source">
<table  class="dashboard">
<tr><th class="header" width='100'>Material</th><th class="header" width='200'>Date</th><th class="header" width='100'>Amount</th></tr>
<% Material sources = sepObj.getSources(); 
	BigDecimal total = BigDecimal.ZERO;
	if ( sources.first() ) {
		sources.beforeFirst();
		SimpleDateFormat dateFormat = (SimpleDateFormat) session.getAttribute("dateFormatter"); 
	while ( sources.next() ) { 
		BigDecimal amount = sources.getAmountForSeparation(sepObj);
		if ( amount != null ) { total = total.add(amount); }
	%>
<tr class="banded" align='center'>
<td><a href="<%= contextPath %>/material?id=<%= sources.getID() %>"><%= sources.getLabel() %></a></td>
<td><%= dateFormat.format(sources.getDate()) %></td>
<td><%= CyanosObject.autoFormatAmount(amount, Separation.MASS_TYPE) %></td>
</tr>
<% } %>
<tr><td colspan="2" align="right"><B>TOTAL:</B></td><td align="center"><B><%= CyanosObject.autoFormatAmount(total, Separation.MASS_TYPE) %></B></td></tr>
</table>
<% } else { %>
<hr width="85%"/>
<p align='center'><b>No Results</b></p>
<% } %>
</DIV></DIV>

<DIV CLASS="collapseSection"><A NAME='sep_product' CLASS='twist' onClick='loadDiv("sep_product")' CLASS='divTitle'>
<IMG ALIGN="ABSMIDDLE" ID="twist_sep_product" SRC="<%= contextPath %>/images/twist-open.png" /> Source Materials</A>
<DIV CLASS="showSection" ID="div_sep_product">
<table class="dashboard">
<tr><th class="header" width='100'>Material</th><th class="header" width='200'>Date</th><th class="header" width='100'>Amount</th><th class="header" width='100'>Yield</th></tr>
<% Material fractions = sepObj.getFractions(); 
	if ( fractions.first() ) {
		fractions.beforeFirst();
		SimpleDateFormat dateFormat = (SimpleDateFormat) session.getAttribute("dateFormatter"); 
		BigDecimal fractotal = BigDecimal.ZERO;
	while ( fractions.next() ) { 
		fractotal = fractotal.add(fractions.getAmount());
		BigDecimal yield = fractions.getAmount().divide(total, 4, BigDecimal.ROUND_HALF_UP).scaleByPowerOfTen(2);
	%>
<tr class="banded" align='center'>
<td><a href="<%= contextPath %>/material?id=<%= fractions.getID() %>"><%= fractions.getLabel() %></a></td>
<td><%= dateFormat.format(fractions.getDate()) %></td>
<td><%= fractions.displayAmount() %></td>
<td><%= yield.toPlainString() %>%</td>
</tr>
<% } BigDecimal yield = fractotal.divide(total, 4, BigDecimal.ROUND_HALF_UP).scaleByPowerOfTen(2); %>
<tr><td colspan="2" align="right"><B>TOTAL:</B></td><td align="center"><B><%= CyanosObject.autoFormatAmount(fractotal, Separation.MASS_TYPE) %></B></td>
<td align="center"><b><%= yield.toPlainString() %>%</b></td></tr>
</table>
<% } else { %>
<hr width="85%"/>
<p align='center'><b>No Results</b></p>
<% } %>
</div></div>

<DIV CLASS="collapseSection"><A NAME='dataFiles' CLASS='twist' onClick='loadDiv("dataFiles")' CLASS='divTitle'>
<IMG ALIGN="ABSMIDDLE" ID="twist_dataFiles" SRC="/cyanos/images/twist-closed.png" /> Data Files</A>
<DIV CLASS="unloaded" ID="div_dataFiles"></DIV>
</DIV>

<DIV CLASS="collapseSection"><A NAME='compounds' CLASS='twist' onClick='loadDiv("compounds")' CLASS='divTitle'>
<IMG ALIGN="ABSMIDDLE" ID="twist_compounds" SRC="/cyanos/images/twist-closed.png" /> Compounds</A>
<DIV CLASS="unloaded" ID="div_compounds"></DIV>
</DIV>

<% } else { %>
<p align="CENTER"><font size="+3" >Separation Search</font>
<hr width='85%'/></p>
<center>
<form name="sepquery">
<table border=0>
<tr><td>Strain ID:</td><td>
<% String queryValue = request.getParameter("query"); if ( queryValue == null ) { queryValue = ""; }%>
<input id="query" type="text" name="query" VALUE="<%= queryValue %>" autocomplete='off' onKeyUp="livesearch(this, 'query', 'div_query')" style='padding-bottom: 0px'/>
<div id="div_query" class='livesearch'></div></td>
<td>
<button type='SUBMIT'>Search</button>
</td></tr>
</table>
</form>
<p align="center"><a href='separation/export'>Export Separation Data</a></p>
</center>
<jsp:include page="/separation/separation-list.jsp" />
<% } %>
</div>
</body>
</html>