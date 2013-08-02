<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ page import="java.text.DateFormatSymbols" %>
<% 		String div = request.getParameter("div");
		String updateField = request.getParameter("update_field"); %><form name="cal">
<input type="hidden" name="update_field" value="<%= updateField %>">
<!-- <input type="hidden" name="cal_div" value="<%= div %>">  -->
<p align="center"><select name="month" onChange="updateDateDiv(this,'<%= updateField %>','<%= div %>')">
<% String[] monthNames = new DateFormatSymbols().getMonths(); 
for ( int i = 0; i < 12; i++ ) { %><option value="<%= String.valueOf(i) %>"><%= monthNames[i] %></option>
<% } %></select>
<input type="text" name="year" size="5" onChange="updateDateDiv(this,'<%= updateField %>','<%= div %>')">
</p>
<table class="month" style="width:250;">
<tr><th width="14%">Sun</th><th width="14%">Mon</th><th width="14%">Tues</th><th width="14%">Wed</th><th width="14%">Thurs</th><th width="14%">Fri</th><th width="14%">Sat</th></tr>
<tr><td></td><td></td><td></td><td></td><td></td><td></td><td></td></tr>
<tr><td></td><td></td><td></td><td></td><td></td><td></td><td></td></tr>
<tr><td></td><td></td><td></td><td></td><td></td><td></td><td></td></tr>
<tr><td></td><td></td><td></td><td></td><td></td><td></td><td></td></tr>
</table>
<p align="center"><button type="button" name="today" onClick="gotoToday('<%= updateField %>','<%= div %>')">Today</button>
<button type="button" name="resetDate" onClick="resetDateDiv('<%= updateField %>','<%= div %>')">Reset</button>
</p>
</form>