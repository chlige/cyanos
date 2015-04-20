<?xml version="1.0" encoding="UTF-8" ?>
<%@ page language="java" contentType="application/xrds+xml; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ page import="java.net.URL, edu.uic.orjala.cyanos.web.servlet.OAuthServlet" %>
<% 	response.setHeader(OAuthServlet.XRDS_HEADER, OAuthServlet.getXRDSURL(request)); %>
<xrds:XRDS
    xmlns:xrds="xri://$xrds"
    xmlns="xri://$xrd*($v*2.0)">
  <XRD>
    <Service priority="0">
      <Type>http://specs.openid.net/auth/2.0/server</Type>
<% URL url = OAuthServlet.getURL(request, "/oauth/"); %>
      <URI><%= url.toString() %></URI>
    </Service>
  </XRD>
</xrds:XRDS>
