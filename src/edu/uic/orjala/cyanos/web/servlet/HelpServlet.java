/**
 * 
 */
package edu.uic.orjala.cyanos.web.servlet;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopFieldDocs;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.util.Version;

import edu.uic.orjala.cyanos.web.help.HelpIndex;

/**
 * @author George Chlipala
 *
 */
public class HelpServlet extends ServletObject {

	private static final long serialVersionUID = -7522464118299655084L;
	
	public final static String HELP_PATH = "help";
	public final static String REBUILD_INDEX = "rebuild";
	public final static String SEARCH_FIELD = "search";
//	private final static String SEARCH_HELP_ACTION = "searchHelp";
//	private final static String REBUILD_BUTTON = String.format("<BUTTON TYPE='SUBMIT' NAME='%s'>Rebuild Index</BUTTON>", REBUILD_INDEX);
//	private final static String SEARCH_BUTTON = String.format("<BUTTON TYPE='SUBMIT' NAME='%s'>Search</BUTTON>", SEARCH_HELP_ACTION);
	
	public final static String SEARCH_RESULTS = "searchResults";
	public final static String SEARCH_HIGHLIGHTER = "searchHilite";

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
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.servlet.ServletObject#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		super.doGet(req, res);
		this.handleRequest(req, res);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.servlet.ServletObject#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		super.doPost(req, res);
		this.handleRequest(req, res);
	}


	private void handleRequest(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		String path = req.getServletPath();
		if ( path != null ) {
			if ( path.equals("/help/search") ) {
				res.sendRedirect("../help?search");
				return;
			} else if ( path.equals("/help/find") ) {
				res.sendRedirect("../help?find");				
				return;
			} else if ( path.equals("/help/toc") ) {
				res.sendRedirect("../help?toc");
				return;
			}
		}
		
		req.setAttribute("helpPath", this.helpDirectory);

		if ( req.getParameter("search") != null ) {	
			this.searchHelp(HelpIndex.CONTENT_FIELD, req);
		} else if ( req.getParameter("find") != null ) {
			this.searchHelp(HelpIndex.KEYWORD_FIELD, req);
		} else if ( req.getParameter("toc") != null ) {
			Query query = null;
			if ( req.getParameter("module") != null ) { 
				query = new TermQuery(new Term(HelpIndex.MODULE_FIELD, req.getParameter("module")));
			} else {
				query = new MatchAllDocsQuery(); 
			} 
			if ( query != null && this.helpDirectory != null ) { 
				Sort sort = new Sort(new SortField(HelpIndex.PATH_FIELD, SortField.STRING));
				IndexSearcher iSearch = HelpIndex.searcher(this.helpDirectory);		
				TopFieldDocs results = iSearch.search(query, 500, sort);
				if ( results.totalHits == 1 ) {
					Document aDoc = iSearch.doc(results.scoreDocs[0].doc);
					String url = String.format("help/%s", aDoc.get(HelpIndex.PATH_FIELD));
					url = res.encodeRedirectURL(url);
					res.sendRedirect(url);
					return;
				} else {
					req.setAttribute(SEARCH_RESULTS, results);
				}
			}
		}
		
		this.forwardRequest(req, res, "/help.jsp");
	}
	
	private void searchHelp(String searchField, HttpServletRequest req) throws ServletException, IOException {
		try { 
		String queryString = req.getParameter("query"); 
		if ( queryString != null ) { 
			QueryParser queryParser = new QueryParser(
					Version.LUCENE_36, searchField,
					new StandardAnalyzer(Version.LUCENE_36));

			IndexSearcher iSearch = HelpIndex.searcher(this.helpDirectory);
			Query query = queryParser.parse(queryString);
			Sort sort = new Sort(SortField.FIELD_SCORE, new SortField(HelpIndex.FILENAME_FIELD, SortField.STRING));
			TopFieldDocs results = iSearch.search(query, 50, sort);
			req.setAttribute(SEARCH_HIGHLIGHTER, new Highlighter(new SimpleHTMLFormatter(), new QueryScorer(query)));
			req.setAttribute(SEARCH_RESULTS, results);
		}
		} catch (CorruptIndexException e) {
			throw new ServletException(e);
		} catch (ParseException e) {
			throw new ServletException(e);
		}
	}

	/*
	public void display(CyanosWrapper aWrap) throws Exception {
				
		String module = aWrap.getRequest().getServletPath();
		aWrap.setContentType("text/html; charset=UTF-8");

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

			this.listFiles(aWrap);
			
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
	*/
	
	/*
	private void listFiles(CyanosWrapper aWrap) {
		Map<String,String> docMap = new HashMap<String,String>();
		String urlRoot = aWrap.getContextPath() + "/help";

		File helpDir = new File(this.helpDirectory);
		aWrap.print(listDir("", helpDir));

	}

	private static String listDir(String prefix, File dir) {
		HtmlList aList = new HtmlList();
		aList.unordered();
		File[] files = dir.listFiles(new HiddenFilenameFilter());
		for ( int i = 0; i < files.length; i++ ) {
			String location = prefix.concat(files[i].getName());
			if ( files[i].isDirectory() ) {
				StringBuffer item = new StringBuffer(files[i].getName());
				item.append(listDir(location.concat("/"), files[i]));
				aList.addItem(item);
			} else {
				String filename = files[i].getName();
				if ( filename.endsWith(".html") || filename.endsWith("htm") ) {
					
				}
				aList.addItem(String.format("<a href=\"%s\">%s</a>", location, filename));
			}
		}
		return aList.toString();
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
	*/
	
/*
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
		
*//*
		Popup hitPop = new Popup(PAGE_SIZES);
		hitPop.setName(SIZE_FIELD);

		if ( aWrap.hasFormValue(SIZE_FIELD) ) 
			hitPop.setDefault(aWrap.getFormValue(SIZE_FIELD));
		else
			hitPop.setDefault("10");
		searchForm.addItem("<BR/>Number of results per page: ");
		searchForm.addItem(hitPop.toString());
*//*
		aWrap.print(searchForm.toString());
	}
*/
/*
	private void printSearchResults(CyanosWrapper aWrap, QueryParser qParse) {
		try {
			IndexSearcher iSearch = HelpIndex.searcher(this.helpDirectory);
			Query aQuery = qParse.parse(aWrap.getFormValue(SEARCH_FIELD));
*//*
			int hitsPerPage = 25;
			if ( aWrap.hasFormValue(SIZE_FIELD) ) {
				try {
					hitsPerPage = Integer.parseInt(aWrap.getFormValue(SIZE_FIELD));
				} catch (NumberFormatException e) {
					hitsPerPage = 25;
					this.log("Could not parse the number of results per page.", e);
				}
			}
*//*

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
	*/			
}
