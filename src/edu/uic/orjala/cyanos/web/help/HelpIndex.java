/**
 * 
 */
package edu.uic.orjala.cyanos.web.help;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.util.Version;

/**
 * <P>This class has convience methods to create, maintain, and use a Lucene index for a help file directory.
 * The help files for CYANOS are contained in the help subdirectory of the application's directory, e.g. /usr/shared/tomcat5/webapps/cyanos/help.
 * Please refer to the details of your particular Tomcat installation to determine the proper location of the webapps directory.</P>
 * 
 * <P>The CYANOS Help system can also accomodate custom help files.  These help files should be exist in the {@link #CUSTOM_HELP_DIR} subdirectory of the help directory.
 * This directory can be created as a symbolic link, via ln -s, to allow users to easily add custom help pages.  Custom help files should be written in HTML and have an extension of either .html or .htm.  
 * Supporting files, e.g. images, can also be included with the custom directory, however these files will not be indexed for searching.</P>
 * 
 * <P>Help files should have the following header elements.<BR>
 * <CODE>
 * &lt;HEAD&gt;<BR>
 * &lt;TITLE&gt;My Title&lt;/TITLE&gt;<BR>
 * &lt;META NAME="KEYWORDS" CONTENT="keyword1,keyword2,etc."&gt;<BR>
 * &lt;META NAME="MODULE" CONTENT="some module"&gt;<BR>
 * &lt;/HEAD&gt;<BR>
 * </CODE></P>
 *  
 * <P><B>NOTE</B>: Updating the CYANOS web application, i.e. a new .war file, will typically result in a loss of the custom help directory.  
 * When installating a new version of the CYANOS web application, be sure to back the custom help files, if the custom directory does NOT exist as a symbolic link.
 * After completing the installation of the new version of the CYANOS web application, one can either copy the saved directory to the {@link #CUSTOM_HELP_DIR} subdirectory or recreate the symbolic link.</P>
 * 
 * @author George Chlipala
 *
 */
public class HelpIndex {

	/**
	 * The Lucene index field to store keywords.  
	 * These are denoted in the help field via the following META tag syntax.<BR>
	 * 
	 * <CODE>&lt;META NAME=&quot;keyword&quot; CONTENT=&quot;aword,another,etc.&quot;&gt;</CODE>
	 */
	public final static String KEYWORD_FIELD = "keyword";
	/**
	 * The Lucene index field used to store the content of the help file, sans HTML tags. 
	 */
	public final static String CONTENT_FIELD = "content";
	/**
	 * The Lucene index field used to store the title of the help file. 
	 * The title is determined using the standard &lt;TITLE&gt;&lt;/TITLE&gt; HTML tags.
	 */
	public final static String TITLE_FIELD = "title";
	/**
	 * The Lucene field used to store the relative path of the help file.
	 */
	public final static String PATH_FIELD = "path";
	/**
	 * The Lucene field used to store the associated CYANOS servelet module of the help file.  
	 * The module can be denoted using the following META tag syntax.<BR>
	 * 
	 * <CODE>&lt;META NAME=&quot;module&quot; CONTENT=&quot;some module&quot;&gt;</CODE>
	 */
	public final static String MODULE_FIELD = "module";
	public final static String LAST_MODIFIED_FIELD = "modified";

	/**
	 * Name of the subdirectory that contains the Lucene index files. 
	 */
	public final static String HELP_INDEX = ".index";
	/**
	 * Name of the subdirectory that contains custom help files.  In this version it is "custom".
	 */
	public final static String CUSTOM_HELP_DIR = "custom";
	
	/**
	 * Return an IndexSearcher for the help file directory
	 * 
	 * @param aPath Help file directory
	 * @return IndexSearcher for the directory
	 * @throws CorruptIndexException
	 * @throws IOException
	 */
	public static IndexSearcher searcher(File aPath) throws CorruptIndexException, IOException {
		return new IndexSearcher(openIndex(aPath), true);
	}

	/**
	 * Return an IndexSearcher for the help file directory
	 * 
	 * @param aPath Help file directory string
	 * @return IndexSearcher for the directory
	 * @throws CorruptIndexException
	 * @throws IOException
	 */
	public static IndexSearcher searcher(String aPath) throws CorruptIndexException, IOException {
		return new IndexSearcher(openIndex(aPath), true);
	}

	/**
	 * Return an IndexReader for the help file directory
	 * 
	 * @param aPath Help file directory
	 * @return IndexReader for the directory
	 * @throws CorruptIndexException
	 * @throws IOException
	 */
	public static IndexReader reader(File aPath) throws CorruptIndexException, IOException {
		return IndexReader.open(openIndex(aPath), true);
	}

	/**
	 * Return an IndexReader for the help file directory
	 * 
	 * @param aPath Help file directory string
	 * @return IndexReader for the directory
	 * @throws CorruptIndexException
	 * @throws IOException
	 */
	public static IndexReader reader(String aPath) throws CorruptIndexException, IOException {
		return IndexReader.open(openIndex(aPath), true);
	}
	
	/**
	 * Build a new index file if it does NOT exist.  The index will be stored in the subdirectory &quot;.index&quot;.
	 * 
	 * @param aPath Help file directory 
	 * @throws CorruptIndexException
	 * @throws LockObtainFailedException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	public static void buildIndex(File aPath) throws CorruptIndexException, LockObtainFailedException, IOException {
		if ( ! hasIndex(aPath) ) {
			File customDir = new File(aPath, CUSTOM_HELP_DIR);
			if ( ! customDir.exists() ) {
				customDir.mkdir();
			} 
			rebuildIndex(aPath);
			
		}
	}
	
	/**
	 * Build a new index file if it does NOT exist. The index will be stored in the subdirectory &quot;.index&quot;.
	 * 
	 * @param aPath Help file directory string
	 * @throws CorruptIndexException
	 * @throws LockObtainFailedException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	public static void buildIndex(String aPath) throws CorruptIndexException, LockObtainFailedException, IOException {
		buildIndex(new File(aPath));	
	}

	
	/**
	 * Rebuild the index file.  It will overwrite the previous index.  The index will be stored in the subdirectory &quot;.index&quot;.
	 * 
	 * @param aPath Help file directory
	 * @throws CorruptIndexException
	 * @throws LockObtainFailedException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	public static void rebuildIndex(String aPath) throws CorruptIndexException, LockObtainFailedException, IOException {
		rebuildIndex(new File(aPath));
	}
	
	/**
	 * Rebuild the index file.  It will overwrite the previous index.  The index will be stored in the subdirectory &quot;.index&quot;.
	 * 
	 * @param aPath Help file directory string
	 * @throws CorruptIndexException
	 * @throws LockObtainFailedException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws SAXException
	 */
	public static void rebuildIndex(File aPath) throws CorruptIndexException, LockObtainFailedException, IOException {

		IndexWriter aWriter = new IndexWriter(openIndex(aPath), new StandardAnalyzer(Version.LUCENE_CURRENT), true,
                new IndexWriter.MaxFieldLength(1000000));
		indexDirectory(aPath, null, aWriter);
		File customDir = new File(aPath, CUSTOM_HELP_DIR);
		indexDirectory(customDir, CUSTOM_HELP_DIR, aWriter);
		aWriter.optimize();
		aWriter.close();
	}
	
	protected static void indexDirectory(File aPath, String prefix, IndexWriter aWriter) throws CorruptIndexException, IOException {
		File[] fileList = aPath.listFiles();
		
		for ( int i = 0; i < fileList.length; i++ ) {
			String fileName = fileList[i].getName();
			if ( fileName.endsWith(".html") || fileName.endsWith(".htm") ) {
				HelpFile aFile = new HelpFile(prefix, fileList[i]);
				aWriter.addDocument(aFile.document());
			}
		}		
	}
	
	protected static Directory openIndex(String aPath) throws IOException {
		return FSDirectory.open(new File(aPath, HELP_INDEX));
	}

	protected static Directory openIndex(File aPath) throws IOException {
		return FSDirectory.open(new File(aPath, HELP_INDEX));
	}
	
	/**
	 * Determine if an index exists for the help file directory
	 * 
	 * @param aPath Help file directory
	 * @return true if the directory has an index for searching.
	 * @throws IOException
	 */
	public static boolean hasIndex(File aPath) throws IOException {
		File indexPath = new File(aPath, HELP_INDEX);
		return indexPath.exists();
	}

	/**
	 * Determine if an index exists for the help file directory
	 * 
	 * @param aPath Help file directory string
	 * @return true if the directory has an index for searching.
	 * @throws IOException
	 */
	public static boolean hasIndex(String aPath) throws IOException {
		return hasIndex(new File(aPath));
	}
}
