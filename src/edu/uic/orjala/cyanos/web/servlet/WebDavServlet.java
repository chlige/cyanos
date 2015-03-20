package edu.uic.orjala.cyanos.web.servlet;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.uic.orjala.cyanos.Collection;
import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.DataFileObject;
import edu.uic.orjala.cyanos.ExternalFile;
import edu.uic.orjala.cyanos.Material;
import edu.uic.orjala.cyanos.Role;
import edu.uic.orjala.cyanos.sql.SQLAssay;
import edu.uic.orjala.cyanos.sql.SQLCompound;
import edu.uic.orjala.cyanos.sql.SQLData;
import edu.uic.orjala.cyanos.sql.SQLMaterial;
import edu.uic.orjala.cyanos.sql.SQLSample;
import edu.uic.orjala.cyanos.sql.SQLSeparation;
import edu.uic.orjala.cyanos.sql.SQLStrain;
import net.sf.jmimemagic.Magic;
import net.sf.jmimemagic.MagicParseException;

/**
 * Servlet implementation class WebDavServlet
 */
@WebServlet(description = "WebDAV Servlet", urlPatterns = { "/webdav/*" })
public class WebDavServlet extends ServletObject {
	private static final long serialVersionUID = 1L;
	
	private static final String METHOD_HEAD = "HEAD";
	private static final String METHOD_PROPFIND = "PROPFIND";
	private static final String METHOD_PROPPATCH = "PROPPATCH";
	private static final String METHOD_COPY = "COPY";
	private static final String METHOD_MOVE = "MOVE";
	private static final String METHOD_LOCK = "LOCK";
	private static final String METHOD_UNLOCK = "UNLOCK";

	enum LockScope {
		EXCLUSIVE("exclusive"), SHARED("shared");
		
		private String name;
		
		LockScope(String name) {
			this.name = name;
		}
		
		String getName() {
			return this.name;
		}
	}
	
	enum DavDepth {
		BASE("0", 0), SUB("1", 1), INFINITY("infinity", 2);
	
		private String name;
		private int depth = 0;
		
		DavDepth(String name, int depth) {
			this.name = name;
			this.depth = depth;
		}
	}
	
	class DavLock {
		private String lockType = "write";
		private LockScope scope;
		private DavDepth depth;
		private String owner;
		private Date timeout;
		private String locktoken;
		private String path;	
		
	}
	
	class DavLockMap {
		private final HashMap<String,DavLock> locks = new HashMap<String,DavLock>();

		DavLockMap() {
			
		}
		
		void addLock(String path, DavLock lock) {
			this.locks.put(path, lock);
		}
		
		DavLock getLock(String path) {
			Date now = new Date();
			while ( path != null ) {
				DavLock lock = this.getLock(path, now);			
				if ( lock != null ) return lock;
				int pos = path.lastIndexOf("/");
				if ( pos > 0 ) {
					path = path.substring(0, pos);
				} else {
					path = null;
				}
			}
			return null;
		}
		
		DavLock getLock(String path, Date now) {
			DavLock lock = this.locks.get(path);
			if ( lock != null ) {
				if ( lock.timeout != null ) {
					if ( lock.timeout.after(now) ) {
						return lock;
					} else {
						this.locks.remove(path);
					}
				}
				return lock;
			}
			return null;
		}
		
	}
	
	enum DavLevel {
		ROOT(0), CLASS(1), OBJECT(2), FILE(3);
		
		private int level;
		
		DavLevel(int level) {
			this.level = level;
		}
	}
	
	class PropertyWriter {
		List<String> propList = new ArrayList<String>();
		boolean values = false;
		XMLStreamWriter out;
		SQLData data;
		String baseURL;
		
		
		PropertyWriter(String baseURL, XMLStreamWriter out, SQLData data) {
			this.baseURL = ( baseURL.endsWith("/") ? baseURL : baseURL.concat("/") );
			this.data = data;
			this.out = out;
		}

		void addProperty(String property) {
			this.propList.add(property);
		}
		
		void setReturnValues(boolean values) {
			this.values = values;
		}
		
		void writeRootProperties(int depth) throws XMLStreamException, DataException, SQLException {
			this.out.writeStartElement("DAV:", "response");
			this.out.writeStartElement("DAV:", "href");
			this.out.writeCharacters(baseURL);
			this.out.writeEndElement();
			
			List<String> badProps = new ArrayList<String>();
			
			if ( values ) {
				if ( propList != null ) {
					for ( String prop : propList ) {
						
					}
				} else {
					for ( String prop : CONTAINER_PROPS ) {
						this.out.writeStartElement("DAV:", prop );
						
						this.out.writeEndElement();
					}
				}
			} else {
				for ( String prop : CONTAINER_PROPS ) {
					this.out.writeEmptyElement("DAV:", prop);
				}				
			}
			this.writeStatus("HTTP/1.1 200 OK");
			this.out.writeEndElement();		
			if ( depth > 0 ) {
				depth--;
				for ( String classname : VALID_CLASSES ) {
					this.writeClassProperties(baseURL, classname, depth);
				}
			}
			
		}
		
		void writeStatus(String status) throws XMLStreamException {
			this.out.writeStartElement("DAV:", "status");
			this.out.writeCharacters(status);
			this.out.writeEndElement();
		}

		void writeClassProperties(String classname, int depth) throws XMLStreamException, DataException, SQLException {
			this.writeClassProperties(baseURL, classname, depth);
		}
		
		void writeClassProperties(String urlPrefix, String classname, int depth) throws XMLStreamException, DataException, SQLException {
			this.out.writeStartElement("DAV:", "response");
			String path = classname.concat("/");
			String urlPath = urlPrefix.concat(path);
			this.out.writeStartElement("DAV:", "href");
			this.out.writeCharacters(urlPath);
			this.out.writeEndElement();
			
			
			
			this.writeStatus("HTTP/1.1 200 OK");
			this.out.writeEndElement();		
			if ( depth > 0 ) {
				depth--;
				DataFileObject object = getAll(data, classname);
				this.writeProperties(urlPrefix, object, depth);
			}
		}
		
		void writeProperties(DataFileObject object, int depth) throws XMLStreamException, DataException {
			this.writeProperties(baseURL, object, depth);
		}
		
		void writeProperties(String urlPrefix, DataFileObject object, int depth) throws XMLStreamException, DataException {
			while ( object.next() ) {
				this.out.writeStartElement("DAV:", "response");
				String path = String.format("/%s/%s/", object.getDataFileClass(), object.getID());
				String urlPath = urlPrefix.concat(path);
				this.out.writeStartElement("DAV:", "href");
				this.out.writeCharacters(urlPath);
				this.out.writeEndElement();


				this.writeStatus("HTTP/1.1 200 OK");
				this.out.writeEndElement();
				if ( depth > 0 ) {
					depth--;
					this.writeProperties(urlPrefix, object.getDataFiles(), object, depth);
				}
				
			}
		}

		void writeProperties(ExternalFile file, DataFileObject parent, int depth) throws XMLStreamException, DataException {
			this.writeProperties(baseURL, file, parent, depth);
		}
		
		void writeProperties(String urlPrefix, ExternalFile file, DataFileObject parent, int depth) throws XMLStreamException, DataException {
			while ( file.next() ) {
				this.out.writeStartElement("DAV:", "response");
				String path = String.format("/%s/%s/%s", parent.getDataFileClass(), parent.getID(), file.getFilePath());
				String urlPath = urlPrefix.concat(path);
				this.out.writeStartElement("DAV:", "href");
				this.out.writeCharacters(urlPath);
				this.out.writeEndElement();



				this.writeStatus("HTTP/1.1 200 OK");
				this.out.writeEndElement();
			}
		}
		
		
	}
	
    /**
     * Simple date format for the creation date ISO representation (partial).
     */
    protected static final SimpleDateFormat creationDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	static final Properties mimeProps = new Properties();
	private DavLockMap lockMap;
	
	/* (non-Javadoc)
	 * @see javax.servlet.GenericServlet#init(ServletConfig)
	 */
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		try {
			Magic.initialize();
		} catch (MagicParseException e) {
			this.log("Unable to load MIME Magic.", e);
		}
		ServletContext context = this.getServletContext();
		Object locks = context.getAttribute("davlocks");
		if ( locks instanceof DavLockMap ) {
			lockMap = (DavLockMap) locks;
		} else {
			lockMap = new DavLockMap();
			context.setAttribute("davlocks", lockMap);
		}
	}

	/**
	 * @see HttpServlet#service(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String method = req.getMethod();

        if (method.equals(METHOD_PROPFIND)) {
            doPropfind(req, resp);
        } else if (method.equals(METHOD_PROPPATCH)) {
            doProppatch(req, resp);
        } else if (method.equals(METHOD_COPY)) {
            doCopy(req, resp);
        } else if (method.equals(METHOD_MOVE)) {
            doMove(req, resp);
        } else if (method.equals(METHOD_LOCK)) {
            doLock(req, resp);
        } else if (method.equals(METHOD_UNLOCK)) {
            doUnlock(req, resp);
        } else {
            // DefaultServlet processing
            super.service(req, resp);
        }
	}

	private void doUnlock(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
	}

	private void doLock(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
	}

	private void doMove(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
	}

	private void doCopy(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
	}

	private void doMkcol(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
	}

	private void doProppatch(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
	}
	
	protected DocumentBuilder getDocumentBuilder() throws ServletException {
		DocumentBuilder documentBuilder = null;
		DocumentBuilderFactory documentBuilderFactory = null;
		try {
			documentBuilderFactory = DocumentBuilderFactory.newInstance();
			documentBuilderFactory.setNamespaceAware(true);
			documentBuilderFactory.setExpandEntityReferences(false);
			documentBuilder = documentBuilderFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			throw new ServletException("Failed to build document builder.");
		}
		return documentBuilder;
	}



	// PROPFIND - Specify a property mask.
    private static final int FIND_BY_PROPERTY = 0;
    // PROPFIND - Display all properties.
    private static final int FIND_ALL_PROP = 1;
    // PROPFIND - Return property names.
    private static final int FIND_PROPERTY_NAMES = 2;

	private static final int INFINITY = 3;
	
	private static String DAVPROP_CREATIONDATE = "creationdate";
	private static String DAVPROP_DISPLAYNAME = "displayname";
	private static String DAVPROP_GETCONTENTLANG = "getcontentlanguage";
	private static String DAVPROP_GETCONTENTLEN = "getcontentlength";
	private static String DAVPROP_GETCONTENTTYPE = "getcontenttype";
	private static String DAVPROP_GETETAG = "getetag";
	private static String DAVPROP_GETLASTMOD = "getlastmodified";
	private static String DAVPROP_LOCKDISCOV = "lockdiscovery";

	/*
	 * 
	 *    <D:activelock> 
            <D:locktype><D:write/></D:locktype> 
            <D:lockscope><D:exclusive/></D:lockscope> 
            <D:depth>0</D:depth> 
            <D:owner>Jane Smith</D:owner> 
            <D:timeout>Infinite</D:timeout> 
            <D:locktoken> 
              <D:href>urn:uuid:f81de2ad-7f3d-a1b2-4f3c-00a0c91a9d76</D:href>
            </D:locktoken> 
            <D:lockroot> 
              <D:href>http://www.example.com/container/</D:href> 
            </D:lockroot> 
           </D:activelock> 

	 */
	private static String DAVPROP_ACTIVELOCK = "activelock";
	
	
	private static String DAVPROP_RESOURCETYPE = "resourcetype";
	private static String DAVPROP_SUPPORTEDLOCK = "supportedlock";

	private static String[] CONTAINER_PROPS = { DAVPROP_CREATIONDATE, DAVPROP_DISPLAYNAME, DAVPROP_RESOURCETYPE, DAVPROP_SUPPORTEDLOCK };
	private static String[] FILE_PROPS = { DAVPROP_CREATIONDATE, DAVPROP_DISPLAYNAME, 
		DAVPROP_GETCONTENTLEN, DAVPROP_GETCONTENTTYPE, DAVPROP_GETETAG, DAVPROP_GETLASTMOD,
		DAVPROP_RESOURCETYPE, DAVPROP_SUPPORTEDLOCK };
	
	/*
	 *          <D:supportedlock> 
            <D:lockentry> 
              <D:lockscope><D:exclusive/></D:lockscope> 
              <D:locktype><D:write/></D:locktype> 
            </D:lockentry> 
            <D:lockentry> 
              <D:lockscope><D:shared/></D:lockscope> 
              <D:locktype><D:write/></D:locktype> 
            </D:lockentry> 
          </D:supportedlock> 

	 * 
	 */
	
	// http://www.webdav.org/specs/rfc4918.html#METHOD_PROPFIND
	
	private void doPropfind(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String depthStr = req.getHeader("Depth");
		SQLData data;
		
		try {
			if (! validPath(req) ) { 
				resp.sendError(HttpServletResponse.SC_NOT_FOUND);
				return; 
			}
			data = getSQLData(req);
		} catch (DataException | SQLException e) {
			throw new ServletException(e);
		}
		
		int depth = DavDepth.INFINITY.depth; 
		int type = FIND_ALL_PROP;
        String[] path = getPathComponents(req);		
		
		if ( depthStr != null ) {
			for ( DavDepth possible : DavDepth.values() ) {
				if ( depthStr.equals(possible.name) ) {
					depth = possible.depth;
					break;
				}
			}
		}
		

        if ( req.getInputStream().available() > 0 ) {
			Node propNode = null;
			DocumentBuilder documentBuilder = getDocumentBuilder();		
			try {
				Document document = documentBuilder.parse(req.getInputStream());
				Element root = document.getDocumentElement();
				NodeList nodes = root.getChildNodes();
				for ( int i = 0; i < nodes.getLength(); i++ ) {
					Node aNode = nodes.item(i);
					if ( aNode.getNodeType() == Node.ELEMENT_NODE ) {
						String nodeName = aNode.getNodeName();
						if ( nodeName.endsWith("prop") ) {
							type = FIND_BY_PROPERTY;
							propNode = aNode;
						} else if ( nodeName.endsWith("propname") ) {
							type = FIND_PROPERTY_NAMES;
						} else if ( nodeName.endsWith("allprop") ) {
							type = FIND_ALL_PROP;
						}
					}
				}
				
				XMLOutputFactory xof = XMLOutputFactory.newInstance();
				XMLStreamWriter xtw = xof.createXMLStreamWriter(resp.getOutputStream());
				xtw.writeStartDocument();
				xtw.writeStartElement("D", "multistatus", "DAV:");
								
				PropertyWriter propOuts = new PropertyWriter(req.getContextPath(), xtw, data);
				
				if ( type == FIND_BY_PROPERTY || type == FIND_ALL_PROP ) {
					propOuts.setReturnValues(true);
					if ( propNode != null ) {
						nodes = propNode.getChildNodes();
						for ( int i = 0; i < nodes.getLength(); i++ ) {
							Node aNode = nodes.item(i);
							if ( aNode.getNodeType() == Node.ELEMENT_NODE ) {
								propOuts.addProperty(aNode.getLocalName());
							}
						}
					}				
				} else if ( type == FIND_PROPERTY_NAMES ) {
					propOuts.setReturnValues(true);
				}

				if ( path.length == 0 ) {
					propOuts.writeRootProperties(depth);
				} else if ( path.length == 1 ) {
					propOuts.writeClassProperties(path[0], depth);
				} else if ( path.length == 2 ) {
					propOuts.writeProperties(getObject(req), depth);
				} else if ( path.length == 3 ) {
					DataFileObject object = getObject(req);
					propOuts.writeProperties(object.getDataFile(path[2]), object, depth);
				}
				
				xtw.writeEndElement();				
				xtw.writeEndDocument();
				resp.setStatus(207);
            } catch (SAXException | XMLStreamException | DataException | SQLException e) {
            	throw new ServletException(e);
            }
		}
	}
	


	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doPut(HttpServletRequest, HttpServletResponse)
	 */
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doDelete(HttpServletRequest, HttpServletResponse)
	 */
	protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doHead(HttpServletRequest, HttpServletResponse)
	 */
	protected void doHead(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doOptions(HttpServletRequest, HttpServletResponse)
	 */
	protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.addHeader("DAV", "1,2");
		try {
			String methodsAllowed = determineMethodsAllowed(req);
	        resp.addHeader("Allow", methodsAllowed);
		} catch (DataException | SQLException e) {
			throw new ServletException(e);
		}
        resp.addHeader("MS-Author-Via", "DAV");
	}

	private static String[] getPathComponents(HttpServletRequest req) {
        String module = req.getPathInfo();
        if ( module.startsWith("/") )
        	module = module.substring(1);
        
        return module.split("/", 3);    
	}

	private static String determineMethodsAllowed(HttpServletRequest req) throws DataException, SQLException {
        StringBuffer methods = new StringBuffer();

        String[] path = getPathComponents(req);

        if ( ! validPath(req) ) {
        	return null;
        }
        methods.append("OPTIONS, HEAD"); 
        
        if ( path.length == 3 ) {
        	DataFileObject object = getObject(req);
        	if ( object.isAllowed(Role.READ) )
        		methods.append(", GET, PROPFIND, LOCK, UNLOCK");
        	if ( object.isAllowed(Role.WRITE) )
        		methods.append(", PROPPATCH, COPY, MOVE, DELETE");
        } else {
        	methods.append(", GET, PROPFIND");
        }
        return methods.toString();
	}
	
	private static final String[] VALID_CLASSES = { "strain", "sample", "material", "separation", "compound", "assay", Collection.DATA_FILE_CLASS };
	
	/*
	 *   Path /<class>/<id>/<file>
	 * 
	 */
	
	private static final String ATTR_OBJECT = "dataObject";

	private static boolean validPath(HttpServletRequest req) throws DataException, SQLException {
        String module = req.getPathInfo();
        if ( module.startsWith("/") )
        	module = module.substring(1);
        
        String[] path = module.split("/", 3);    
		if ( path.length > 0 ) {
			boolean valid = validClass(path[0]);
			if ( valid && path.length > 1 ) {
				DataFileObject object = getObject(req, path[0], path[1]);
				valid = ( object != null && object.first() );
				if ( valid ) {
					req.setAttribute(ATTR_OBJECT, object);
					if ( path.length > 2 ) {
						valid = object.hasDataFile(path[2]);
					}			
				}
			}
			return valid;
		}
		return true;
	}
	
	private static DataFileObject getObject(HttpServletRequest req) throws DataException, SQLException {
		Object object = req.getAttribute(ATTR_OBJECT);
		if ( object instanceof DataFileObject ) {
			return (DataFileObject) object;
		}
		if ( validPath(req) ) {
			object = req.getAttribute(ATTR_OBJECT);
			if ( object instanceof DataFileObject ) {
				return (DataFileObject) object;
			}
		}
		return null;
	}
	
	private static DataFileObject getObject(HttpServletRequest req, String objClass, String objID) throws DataException, SQLException {
		SQLData data = getSQLData(req);
		
		if ( objClass.equals("sample") ) {
			return new SQLSample(data, objID);
		} else if (objClass.equals("strain") ) {
			return SQLStrain.load(data, objID);
		} else if (objClass.equals("separation") ) {
			return new SQLSeparation(data, objID);
		} else if (objClass.equals("compound") ) {
			return SQLCompound.load(data, objID);
		} else if ( objClass.equals("assay") ) {
			return SQLAssay.load(data, objID);
		} else if ( objClass.equals(Material.DATA_FILE_CLASS) ) {
			return SQLMaterial.load(data, objID);
		}
		return null;
	}

	private static DataFileObject getAll(SQLData data, String objClass) throws DataException, SQLException {		
		if ( objClass.equals("sample") ) {
			
		} else if (objClass.equals("strain") ) {
			return SQLStrain.strains(data);
		} else if (objClass.equals("separation") ) {
			return SQLSeparation.separations(data);
		} else if (objClass.equals("compound") ) {
			return SQLCompound.compounds(data, SQLCompound.ID_COLUMN, SQLCompound.ASCENDING_SORT);
		} else if ( objClass.equals("assay") ) {
			return SQLAssay.assays(data);
		} else if ( objClass.equals(Material.DATA_FILE_CLASS) ) {
			return SQLMaterial.find(data, "%");
		}
		return null;
	}

	private static boolean validClass(String className) {
		boolean valid = false;
		for ( String aClass : VALID_CLASSES ) {
			if ( aClass.equalsIgnoreCase(className) ) {
				valid = true;
				break;
			}
		}
		return valid;
	}
}
