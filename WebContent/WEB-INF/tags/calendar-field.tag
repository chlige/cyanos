<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ tag import="java.text.DateFormatSymbols" %>
<%@ attribute name="fieldName" required="true" %>
<%@ attribute name="dateValue" required="false" %>
<input type="text" name="${fieldName}" onFocus="showDate('cal_${fieldName}','${fieldName}')" style='padding-bottom: 0px' id="${fieldName}" size="10"  value="<c:out value="${dateValue}"/>"/>
<a onclick="showDate('cal_${fieldName}','${fieldName}')"><img align="MIDDLE" border="0" src="<%= request.getContextPath() %>/images/calendar.png"></a>
<div id="cal_${fieldName}" class='calendar'>
<form name="cal">
<input type="hidden" name="update_field" value="${fieldName}">
<!-- <input type="hidden" name="cal_div" value="cal_${fieldName}">  -->
<p align="center"><select name="month" onChange="updateDateDiv(this,'${fieldName}','cal_${fieldName}')">
<% String[] monthNames = new DateFormatSymbols().getMonths(); 
for ( int i = 0; i < 12; i++ ) { %><option value="<%= String.valueOf(i) %>"><%= monthNames[i] %></option>
<% } %></select>
<input type="text" name="year" size="5" onKeyUp="if ( this.value.length == 4 ) { updateDateDiv(this,'${fieldName}','cal_${fieldName}') }" onChange="updateDateDiv(this,'${fieldName}','cal_${fieldName}')">
</p>
<table class="month" style="width:250; border-collapse:separate">
<tr><th width="14%">Sun</th><th width="14%">Mon</th><th width="14%">Tues</th><th width="14%">Wed</th><th width="14%">Thurs</th><th width="14%">Fri</th><th width="14%">Sat</th></tr>
<tr><td></td><td></td><td></td><td></td><td></td><td></td><td></td></tr>
<tr><td></td><td></td><td></td><td></td><td></td><td></td><td></td></tr>
<tr><td></td><td></td><td></td><td></td><td></td><td></td><td></td></tr>
<tr><td></td><td></td><td></td><td></td><td></td><td></td><td></td></tr>
</table>
<p align="center"><button type="button" name="today" onClick="gotoToday('${fieldName}','cal_${fieldName}')">Today</button>
<button type="button" name="resetDate" onClick="resetDateDiv('${fieldName}','cal_${fieldName}')">Reset</button>
</p>
</form>
</div>