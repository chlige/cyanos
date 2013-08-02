<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.web.servlet.HelpServlet,
	edu.uic.orjala.cyanos.web.help.HelpIndex,
	java.util.List,
	org.apache.lucene.search.highlight.Highlighter,
	org.apache.lucene.search.TopDocs,
	org.apache.lucene.search.ScoreDoc,
	org.apache.lucene.index.IndexReader,
	org.apache.lucene.document.Document,
	org.apache.lucene.search.highlight.TextFragment,
	org.apache.lucene.analysis.TokenStream,
	org.apache.lucene.search.highlight.TokenSources" %>
<div style="margin-left: 5%; margin-right:5%">
<%	TopDocs docs = (TopDocs) request.getAttribute(HelpServlet.SEARCH_RESULTS);
	String helpPath = (String) request.getAttribute("helpPath"); 
	IndexReader reader = HelpIndex.reader(helpPath);
	Highlighter highlighter = (Highlighter) request.getAttribute(HelpServlet.SEARCH_HIGHLIGHTER);
	
	if ( docs != null && helpPath != null ) { 
		
		if ( docs.totalHits > 0 ) {
%><dl><%
		for ( ScoreDoc hit: docs.scoreDocs ) { 
			Document aDoc = reader.document(hit.doc);
%><dt><a href="help/<%= aDoc.get(HelpIndex.PATH_FIELD) %>"><%= aDoc.get(HelpIndex.TITLE_FIELD) %></a></dt>
<dd><% 
			String text = aDoc.get(HelpIndex.CONTENT_FIELD);
			TokenStream tokenStream = TokenSources.getTokenStream(reader, hit.doc, HelpIndex.CONTENT_FIELD);
			for ( TextFragment fragment : highlighter.getBestTextFragments(tokenStream, text, false, 10) ) { 
				if ((fragment != null) && (fragment.getScore() > 0)) {
					out.print("...");
					out.print(fragment.toString());
					out.print("...<br>");
				}
			}	
%></dd><% 
	} 
%></dl><% 
	} else { %>
<p align="center"><b>No results</b></p>
<% } } %>
</div>