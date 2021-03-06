<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ tag import="java.text.DateFormatSymbols, java.text.SimpleDateFormat, java.util.Date" %>
<%@ attribute name="fieldName" required="true" %>
<%@ attribute name="dateValue" required="false" %>
<%@ attribute name="showTime" required="false" type="java.lang.Boolean" %>
<% if ( showTime == null ) {
	showTime = Boolean.FALSE;
%><c:set var="showTime" value="${false}"/><% }
if ( dateValue == null ) {
		SimpleDateFormat format = new SimpleDateFormat(showTime ? "yyyy-MM-dd hh:mm a" : "yyyy-MM-dd"); 
		dateValue = format.format(new Date());
%><c:set var="dateValue" value="<%= dateValue %>"/><%
} %>
<input id="html5_${fieldName}" type="date" name="${fieldName}" value="${dateValue}" placeholder="YYYY-MM-DD">
<input id="${fieldName}" type="text" class="dateField" name="${fieldName}" onClick="showDate('cal_' + this.name, this.name, ${showTime})"  style='padding-bottom: 0px' size="${showTime ? 15 : 10 }"  value="${dateValue}"/>
<%-- 
<a onclick="showDate('cal_${fieldName}','${fieldName}', ${showTime})"><img align="MIDDLE" border="0" src="<%= request.getContextPath() %>/images/calendar.png"></a>
--%>
<div id="cal_${fieldName}" class='calendar'>
<%-- <input type="hidden" name="update_field" value="${fieldName}"> --%>
<input id="temp_${fieldName}" type="hidden" name="temp_date" value="${dateValue} }">
<span style="float:right;"><a onclick="closeDateDiv(getParent(this,'div.calendar').id)" title="Close" style="text-decoration:none; cursor:pointer; font-size:18px; color:gray; font-weight:bold;">&times;</a></span>
<p align="center"><select id="month" onChange="updateDateDiv(this, ${showTime})">
<% String[] monthNames = new DateFormatSymbols().getMonths(); 
for ( int i = 0; i < 12; i++ ) { %><option value="<%= String.valueOf(i) %>"><%= monthNames[i] %></option>
<% } %></select>
<input type="number" id="year" size="5" min="1900" max="2100" onKeyUp="if ( this.value.length == 4 ) { updateDateDiv(this, ${showTime}) }" onChange="updateDateDiv(this, ${showTime})">
</p>
<table class="month" style="width:250; border-collapse:separate">
<tr><th width="14%">Sun</th><th width="14%">Mon</th><th width="14%">Tues</th><th width="14%">Wed</th><th width="14%">Thurs</th><th width="14%">Fri</th><th width="14%">Sat</th></tr>
<tr><td></td><td></td><td></td><td></td><td></td><td></td><td></td></tr>
<tr><td></td><td></td><td></td><td></td><td></td><td></td><td></td></tr>
<tr><td></td><td></td><td></td><td></td><td></td><td></td><td></td></tr>
<tr><td></td><td></td><td></td><td></td><td></td><td></td><td></td></tr>
</table>
<p align="center"><button type="button" name="today" onClick="gotoToday(this, ${showTime})">Today</button></p>
<c:if test="${showTime}">
<p align="center">Time: 
<input type="number" id="hour" min="1" max="12"> : <input type="number" id="minute" min="0" max="59"> <select id="meridian"><option>AM</option><option>PM</option></select><br>
<button type="button" name="updateDate" onClick="updateDateTime(this.form.parentNode.id.substring(4),this.form.parentNode.id)">Update Value</button>
</p>
</c:if>
<p align="center">
<button type="button" name="resetDate" onClick="resetDateDiv(this.form.parentNode.id.substring(4),this.form.parentNode.id)">Reset</button>
</p>
</div>
<script>
checkCalendar(document.getElementById("html5_${fieldName}"), document.getElementById("${fieldName}"), document.getElementById("cal_${fieldName}"), ${showTime});
</script>