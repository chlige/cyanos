<%@ tag language="java" pageEncoding="UTF-8"%>
<%@ tag import="edu.uic.orjala.cyanos.DataFileObject,
	edu.uic.orjala.cyanos.web.servlet.DataFileServlet,
	edu.uic.orjala.cyanos.Role,
	edu.uic.orjala.cyanos.DataException,
	edu.uic.orjala.cyanos.ExternalFile, java.util.HashMap,
	edu.uic.orjala.cyanos.web.listener.AppConfigListener,
	java.io.File, java.util.Map, java.util.Arrays, java.util.Map.Entry, java.util.List, java.util.Set, java.util.TreeSet,
	net.sf.jmimemagic.Magic,
	net.sf.jmimemagic.MagicMatch,
	net.sf.jmimemagic.MagicException,
	net.sf.jmimemagic.MagicMatchNotFoundException,
	net.sf.jmimemagic.MagicParseException" %>
<%@ attribute name="object" required="true" type="edu.uic.orjala.cyanos.DataFileObject" %>
<%@ attribute name="type" required="false" %>
