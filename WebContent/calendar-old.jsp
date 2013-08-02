<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ page import="java.util.Calendar, java.util.GregorianCalendar" %>
<% 		String div = request.getParameter("div");
		Calendar selectedCal = (Calendar) request.getAttribute("calValue");
		Calendar today = GregorianCalendar.getInstance();
		boolean selectedDate = ( selectedCal != null );
		if ( ! selectedDate ) selectedCal = today;
		Calendar myCal;
		Integer month;
		Integer year;
		if ( request.getParameter("month") != null && request.getParameter("year") != null) {
			month = new Integer(request.getParameter("month"));
			year = new Integer(request.getParameter("year"));
			selectedDate = true;
		} else {
			month = selectedCal.get(Calendar.MONTH);
			year = selectedCal.get(Calendar.YEAR);
		}
		myCal = new GregorianCalendar(year, month, 1);
		boolean thisMonth = (myCal.get(Calendar.YEAR) == selectedCal.get(Calendar.YEAR) && myCal.get(Calendar.MONTH) == selectedCal.get(Calendar.MONTH));		
		boolean currentMonth = (myCal.get(Calendar.YEAR) == today.get(Calendar.YEAR) && myCal.get(Calendar.MONTH) == today.get(Calendar.MONTH));
%>
<table class="month" style="width:250;">
<tr><th width="14%">Sun</th><th width="14%">Mon</th><th width="14%">Tues</th><th width="14%">Wed</th><th width="14%">Thurs</th><th width="14%">Fri</th><th width="14%">Sat</th></tr>
<tr>
<% for ( int i = 1; i < myCal.get(Calendar.DAY_OF_WEEK); i++ ) { %>
<td></td>
<% } %>

<% while ( myCal.get(Calendar.MONTH) == month ) { %>
<td class="<%= (thisMonth && myCal.get(Calendar.DATE) == selectedCal.get(Calendar.DATE) ? (selectedDate ? "selDay" : "today") : (currentMonth && myCal.get(Calendar.DATE) == today.get(Calendar.DATE) ? "today" : "day") ) %>">
<a onClick="setDateDiv('<%= request.getParameter("update_field") %>','<%= String.format("%04d-%02d-%02d", year, month+1, myCal.get(Calendar.DAY_OF_MONTH))%>','<%= div %>')">
<%= myCal.get(Calendar.DAY_OF_MONTH) %></a></td>
<% 	if ( myCal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY ) { %>
</tr><tr>
<% } myCal.add(Calendar.DATE, 1); } %>
<tr>
</table>
