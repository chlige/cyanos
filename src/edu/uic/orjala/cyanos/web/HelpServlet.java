/**
 * 
 */
package edu.uic.orjala.cyanos.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.TextFragment;
import org.apache.lucene.search.highlight.TokenSources;
import org.apache.lucene.util.Version;

import edu.uic.orjala.cyanos.web.help.HelpIndex;
import edu.uic.orjala.cyanos.web.html.Form;
import edu.uic.orjala.cyanos.web.html.Paragraph;
import edu.uic.orjala.cyanos.web.html.StyledText;

/**
 * @author George Chlipala
 *
 */
public class HelpServlet extends ServletObject {

	private static final long serialVersionUID = -7522464118299655084L;
	
	private final static String SEARCH_HELP_ACTION = "searchHelp";
	private final static String HELP_PATH = "help";
	private final static String REBUILD_INDEX = "rebuild";
	private final static String REBUILD_BUTTON = String.format("<BUTTON TYPE='SUBMIT' NAME='%s'>Rebuild Index</BUTTON>", REBUILD_INDEX);
	private final static String SEARCH_FIELD = "search";
	private final static String SEARCH_BUTTON = String.format("<BUTTON TYPE='SUBMIT' NAME='%s'>Search</BUTTON>", SEARCH_HELP_ACTION);

// FOR pagination of search results.
/*
	private final static String SIZE_FIELD = "size";
	private final static String[] PAGE_SIZES = { "10", "25", "50", "100" };
*/	
	private String helpDirectory = null;
	
	public void init(ServletConfig config) throws ServletException {
		try {
			super.init(config);
			this.helpDirectory = config.getServletContext().getRealPath(HELP_PATH);			
			if ( ! HelpIndex.hasIndex(this.helpDirectory) ) {
				this.log("Building the help index.");
				HelpIndex.buildIndex(this.helpDirectory);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void display(CyanosWrapper aWrap) throws Exception {
				
		String module = aWrap.getRequest().getServletPath();
		aWrap.setContentType("text/html");

		if ( "/help/search".equals(module) ) {
			aWrap.startHTMLDoc("CYANOS Help - Search Help Pages");
			Paragraph head = new Paragraph();
			head.setAlign("CENTER");
			StyledText title = new StyledText("CYANOS Help");
			title.setSize("+3");
			head.addItem(title);		
			aWrap.print(head.toString());
			aWrap.print("<H2 ALIGN='CENTER'>Search Help Pages</H2>");
			aWrap.print("<HR WIDTH='85%'/>");
			this.doSearch(aWrap, HelpIndex.CONTENT_FIELD);
		} else if ( "/help/find".equals(module) ) {
			aWrap.startHTMLDoc("CYANOS Help - Find Help Topics");
			Paragraph head = new Paragraph();
			head.setAlign("CENTER");
			StyledText title = new StyledText("CYANOS Help");
			title.setSize("+3");
			head.addItem(title);		
			aWrap.print(head.toString());
			aWrap.print("<H2 ALIGN='CENTER'>Find Help Topics</H2>");
			aWrap.print("<HR WIDTH='85%'/>");
			this.doSearch(aWrap, HelpIndex.KEYWORD_FIELD);
		} else if ( "/help/toc".equals(module) ) {
			PrintWriter out = aWrap.startHTMLDoc("CYANOS Help - Table of Contents");			
			Paragraph head = new Paragraph();
			head.setAlign("CENTER");
			StyledText title = new StyledText("CYANOS Help");
			title.setSize("+3");
			head.addItem(title);
			head.addItem("<H2>Table of Contents</H2>");
			head.addItem("<HR WIDTH='85%'/>");
			out.println(head);
			this.listTOC(aWrap);
		} else if ( "/help/admin".equals(module) ) {
			PrintWriter out = aWrap.startHTMLDoc("CYANOS Help - Administration");			
			Paragraph head = new Paragraph();
			head.setAlign("CENTER");
			StyledText title = new StyledText("CYANOS Help");
			title.setSize("+3");
			head.addItem(title);
			head.addItem("<H2>Administration</H2>");
			head.addItem("<HR WIDTH='85%'/>");
			out.println(head);

			Form searchForm = new Form();
			if ( aWrap.hasFormValue(REBUILD_INDEX) ) {
				HelpIndex.rebuildIndex(this.helpDirectory);
			}
			searchForm.setPost();
			searchForm.addItem(REBUILD_BUTTON);
			out.println(searchForm.toString());
		}
		
		aWrap.finishHTMLDoc();
	}
	
	private void listTOC(CyanosWrapper aWrap) {
		try {
			Map<String,String> docMap = new HashMap<String,String>();
			String urlRoot = aWrap.getContextPath() + "/help";

			if ( aWrap.hasFormValue("module") ) {
				Query aQuery = new TermQuery(new Term(HelpIndex.MODULE_FIELD, aWrap.getFormValue("module")));
				TopScoreDocCollector collector = TopScoreDocCollector.create(500, false);
				IndexSearcher iSearch = HelpIndex.searcher(this.helpDirectory);
				iSearch.search(aQuery, collector);
				ScoreDoc[] hits = collector.topDocs().scoreDocs;
				if ( hits.length == 1 ) {
					Document aDoc = iSearch.doc(hits[0].doc);
					String url = String.format("%s/%s", urlRoot, aDoc.get(HelpIndex.PATH_FIELD));
					HttpServletResponse res = aWrap.getResponse();
					url = res.encodeRedirectURL(url);
					res.sendRedirect(url);
					return;
				}
				for ( int i = 0; i < hits.length; i++ ) {
					Document aDoc = iSearch.doc(hits[i].doc);
					String[] keywords = aDoc.getValues(HelpIndex.KEYWORD_FIELD);
					StringBuffer keywds = new StringBuffer(keywords[0]);
					for ( int k = 1; k < keywords.length; k++ ) {
						keywds.append(", ");
						keywds.append(keywords[k]);
					}
					String content = String.format("<DT><A HREF='%s/%s'>%s</A></DT><DD>Keywords: %s</DD>", urlRoot, aDoc.get(HelpIndex.PATH_FIELD), aDoc.get(HelpIndex.TITLE_FIELD), keywds.toString());
					docMap.put(aDoc.get("path"), content);
				}				
			} else {
				IndexReader aReader = HelpIndex.reader(this.helpDirectory);	
				for (int i = 0; i < aReader.numDocs(); i++) {
					Document aDoc = aReader.document(i);
					String[] keywords = aDoc.getValues(HelpIndex.KEYWORD_FIELD);
					StringBuffer keywds = new StringBuffer(keywords[0]);
					for ( int k = 1; k < keywords.length; k++ ) {
						keywds.append(", ");
						keywds.append(keywords[k]);
					}
					String content = String.format("<DT><A HREF='%s/%s'>%s</A></DT><DD>Keywords: %s</DD>", urlRoot, aDoc.get(HelpIndex.PATH_FIELD), aDoc.get(HelpIndex.TITLE_FIELD), keywds.toString());
					docMap.put(aDoc.get("path"), content);
				}
				
			}
			
			Set<String> docs = new TreeSet<String>(docMap.keySet());
			Iterator<String> anIter = docs.iterator();
			aWrap.print("<DL>");
			
			while ( anIter.hasNext() ) {
				aWrap.print(docMap.get(anIter.next()));
			}
			aWrap.print("</DL>");
		
		} catch (CorruptIndexException e) {
			aWrap.print(aWrap.handleException(e));
		} catch (IOException e) {
			aWrap.print(aWrap.handleException(e));
		}		
	}
	
	private void doSearch(CyanosWrapper aWrap, String defaultField) {
		this.printSearchForm(aWrap);
		if ( aWrap.hasFormValue(SEARCH_HELP_ACTION) ) {
			this.printSearchResults(aWrap, new QueryParser(Version.LUCENE_CURRENT, defaultField, new StandardAnalyzer(Version.LUCENE_CURRENT)));
		}		
	}
	
	private void printSearchForm(CyanosWrapper aWrap) {
		Form searchForm = new Form("<P ALIGN='CENTER'>Find: ");
		if ( aWrap.hasFormValue(SEARCH_FIELD) ) {
			String searchValue = aWrap.getFormValue(SEARCH_FIELD);
			searchValue.replaceAll("\"", "\\\"");
			searchForm.addItem(String.format("<INPUT TYPE='TEXT' NAME='%s' VALUE=\"%s\"/> ", SEARCH_FIELD, searchValue));
		} else { 
			searchForm.addItem("<INPUT TYPE='TEXT' NAME='search'/> ");
		}
		searchForm.addItem(SEARCH_BUTTON);
		searchForm.addItem("</P>");
		
/*
		Popup hitPop = new Popup(PAGE_SIZES);
		hitPop.setName(SIZE_FIELD);

		if ( aWrap.hasFormValue(SIZE_FIELD) ) 
			hitPop.setDefault(aWrap.getFormValue(SIZE_FIELD));
		else
			hitPop.setDefault("10");
		searchForm.addItem("<BR/>Number of results per page: ");
		searchForm.addItem(hitPop.toString());
*/
		aWrap.print(searchForm.toString());
	}
	
	private void printSearchResults(CyanosWrapper aWrap, QueryParser qParse) {
		try {
			IndexSearcher iSearch = HelpIndex.searcher(this.helpDirectory);
			Query aQuery = qParse.parse(aWrap.getFormValue(SEARCH_FIELD));
			/*
			int hitsPerPage = 25;
			if ( aWrap.hasFormValue(SIZE_FIELD) ) {
				try {
					hitsPerPage = Integer.parseInt(aWrap.getFormValue(SIZE_FIELD));
				} catch (NumberFormatException e) {
					hitsPerPage = 25;
					this.log("Could not parse the number of results per page.", e);
				}
			}
			*/

			TopScoreDocCollector collector = TopScoreDocCollector.create(50, false);
			String urlRoot = aWrap.getContextPath() + "/help";
			iSearch.search(aQuery, collector);
			ScoreDoc[] hits = collector.topDocs().scoreDocs;

			SimpleHTMLFormatter htmlFormatter = new SimpleHTMLFormatter();
			Highlighter highlighter = new Highlighter(htmlFormatter, new QueryScorer(aQuery));

			aWrap.print("<DL>");
			
			for ( int i = 0; i < hits.length; i++ ) {
				Document aDoc = iSearch.doc(hits[i].doc);
				aWrap.print(String.format("<DT><A HREF='%s/%s'>%s</A></DT>", urlRoot, aDoc.get(HelpIndex.PATH_FIELD), aDoc.get(HelpIndex.TITLE_FIELD)));
				String text = aDoc.get(HelpIndex.CONTENT_FIELD);
				TokenStream tokenStream = TokenSources.getTokenStream(aDoc, HelpIndex.CONTENT_FIELD, new StandardAnalyzer(Version.LUCENE_CURRENT));
				TextFragment[] frag = highlighter.getBestTextFragments(tokenStream, text, false, 10);
				//highlighter.getBestFragments(tokenStream, text, 3, "...");

				aWrap.print("<DD>");
				for (int j = 0; j < frag.length; j++) {
					if ((frag[j] != null) && (frag[j].getScore() > 0)) {
						aWrap.print(frag[j].toString());
						aWrap.print("<BR/>");
					}
				}
				aWrap.print("</DD>");
			}
			aWrap.print("</DL>");
			
		} catch (ParseException e) {
			aWrap.print(aWrap.handleException(e));
		} catch (IOException e) {
			aWrap.print(aWrap.handleException(e));
		} catch (InvalidTokenOffsetsException e) {
			aWrap.print(aWrap.handleException(e));
		}		
	}			
}
