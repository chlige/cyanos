<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<% String divID = request.getParameter("loadingDivID"); 
	String divTitle = request.getParameter("loadingDivTitle"); 
	String contextPath = request.getContextPath();  %>
<div class="collapseSection"><a class="twist" onClick="loadDiv('<%= divID %>')" class="divTitle">
<img align="absmiddle" id="twist_<%= divID %>" src="<%= contextPath %>/images/twist-closed.png"><%= divTitle %></a>
<div class='unloaded' id='div_<%= divID %>'><%@ include file="/includes/spinner.html" %></div>
</div>
