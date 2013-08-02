<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.web.servlet.HelpServlet,
	edu.uic.orjala.cyanos.web.help.HelpIndex,
	java.util.List,
	org.apache.lucene.search.TopDocs,
	org.apache.lucene.search.ScoreDoc,
	org.apache.lucene.index.IndexReader,
	org.apache.lucene.document.Document" %>
<%	TopDocs docs = (TopDocs) request.getAttribute(HelpServlet.SEARCH_RESULTS);
	String helpPath = (String) request.getAttribute("helpPath"); 
	IndexReader reader = HelpIndex.reader(helpPath);
	
	if ( docs != null && helpPath != null ) { 
%><dl><%
		for ( ScoreDoc hit: docs.scoreDocs ) { 
			Document aDoc = reader.document(hit.doc);
			String[] keywords = aDoc.getValues(HelpIndex.KEYWORD_FIELD);
%><dt><a href="help/<%= aDoc.get(HelpIndex.PATH_FIELD) %>"><%= aDoc.get(HelpIndex.TITLE_FIELD) %></a></dt>
<dd>Keywords: <%= keywords[0] %><%
		for ( int k = 1; k < keywords.length; k++ ) {
			out.print(", ");
			out.print(keywords[k]);
		}
%></dd><%
		} 
%></dl><% 
	} %>