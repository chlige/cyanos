<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ attribute name="divID" required="true" %>
<%@ attribute name="title" required="true" %>
<%@ attribute name="ajax" required="false" type="java.lang.Boolean" %>
<c:set var="ajax" value="${ajax == null ? false : ajax}"/>
<div class="collapseSection">
<a class='twist' onclick='loadDiv("${divID}")' class='divTitle'>
<img align="absmiddle" id="twist_${divID}" src="/cyanos/images/twist-closed.png" /> ${title}</A>
<div class="unloaded" id="div_${divID}"></div>
</div>