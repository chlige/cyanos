<?xml version="1.0" encoding="UTF-8" ?>
<%@ page language="java" contentType="application/xrds+xml; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="java.net.URL" %>
<% URL url = new URL(request.getScheme(), request.getServerName(), request.getContextPath().concat("/oauth/") ); %>
<xrds:XRDS
    xmlns:xrds="xri://$xrds"
    xmlns="xri://$xrd*($v*2.0)">
  <XRD>
    <Service priority="0">
      <Type>http://specs.openid.net/auth/2.0/signon</Type>
      <Type>http://openid.net/signon/1.1</Type>
      <URI><%= url.toString() %></URI>
    </Service>
  </XRD>
</xrds:XRDS>
