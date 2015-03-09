<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.Sample,
	edu.uic.orjala.cyanos.Separation,
	edu.uic.orjala.cyanos.Harvest,
	edu.uic.orjala.cyanos.Assay,
	edu.uic.orjala.cyanos.sql.SQLSample,
	edu.uic.orjala.cyanos.SampleAccount,
	edu.uic.orjala.cyanos.Amount,
	edu.uic.orjala.cyanos.Amount.AmountUnit,
	edu.uic.orjala.cyanos.web.servlet.SampleServlet,
	java.text.SimpleDateFormat,
	java.math.BigDecimal" %>
<% 
	SimpleDateFormat dateFormat = (SimpleDateFormat) session.getAttribute("dateFormatter"); 
	Sample mySample = (Sample) request.getAttribute(SampleServlet.SAMPLE_ATTR);
	if ( mySample != null ) {
		SampleAccount txnAccount = mySample.getAccount();
		txnAccount.beforeFirst();
		BigDecimal balance = BigDecimal.ZERO;
		String unit = mySample.getBaseUnit();
		BigDecimal conc = mySample.getConcentration();
		if ( conc == null || conc.equals(BigDecimal.ZERO)) conc = BigDecimal.ONE; %>
<table class="balanceSheet" style="width:80%; margin-left:10%;">
<tr class="header"><th>Date</th><th>Description</th><th>Reference</th><th>Amount</th><th>Balance</th></tr>
<%	while (txnAccount.next()) { 
	boolean isVoid = txnAccount.isVoid();
%>
<tr<%= isVoid ? " class=\"voided\"" : "" %>><td><%= dateFormat.format(txnAccount.getDate()) %></td>
<td><% if ( isVoid ) { %><font class='voidNote'>Transaction voided on <%= dateFormat.format(txnAccount.getVoidDate()) %> by <%= txnAccount.getVoidUserID() %></font><br><% } %>
<% out.print(SampleServlet.formatStringHTML(txnAccount.getNotes())); %></td>
<td><% 	Class<?> refClass = txnAccount.getTransactionReferenceClass();
	String parentID = txnAccount.getTransactionReferenceID();
	if ( isVoid || parentID == null || refClass == null )
		out.print("");
	else if ( refClass.equals(Sample.class) ) {
		out.print(String.format("<A HREF='sample?id=%s'>Sample #%s</A>", parentID, parentID));
	} else if ( refClass.equals(Separation.class) ) {
		out.print(String.format("<A HREF='separation?id=%s'>Separation #%s</A>", parentID, parentID));
	} else if ( refClass.equals(Harvest.class) ) {
		out.print(String.format("<A HREF='harvest?id=%s'>Harvest #%s</A>", parentID, parentID));
	} else if ( refClass.equals(Assay.class) ) {
		out.print(String.format("<A HREF='assay?id=%s'>Assay %s</A>", parentID, parentID));
	} else {
			out.print("UNKNOWN");
	}
%></td>
<td>
<% if ( isVoid ) { 
	out.print("-");	
} else {	
	Amount amount = txnAccount.getAmount();
	if ( amount.getValue().signum() < 0 ) {
		out.print("<font color='red'>(");
		out.print(amount.negate().toString());
		out.print(")</font>");
	} else {
		out.print(amount.toString());	
	}
	balance = balance.add(txnAccount.getAmountMass().getValue());
}
Amount balAmt = new Amount(balance, AmountUnit.MASS);
%></td>
<td><b><%= balAmt.toString(conc) %></b></td></tr>
<% } %>
<tr><th colspan="4" align="right" class="footer">Final Balance:</th><th class="footer">
<%= mySample.accountBalance().toString() %></th></tr>
</table>
<% } else { out.println("<p align=\"center\"><b><i>NONE</i></b></p>"); } %>
