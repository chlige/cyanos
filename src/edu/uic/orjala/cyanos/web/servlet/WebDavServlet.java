package edu.uic.orjala.cyanos.web.servlet;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.UUID;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import net.sf.jmimemagic.Magic;
import net.sf.jmimemagic.MagicException;
import net.sf.jmimemagic.MagicMatch;
import net.sf.jmimemagic.MagicMatchNotFoundException;
import net.sf.jmimemagic.MagicParseException;

import org.apache.commons.codec.binary.Base64;
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
import edu.uic.orjala.cyanos.Separation;
import edu.uic.orjala.cyanos.User;
import edu.uic.orjala.cyanos.sql.SQLAssay;
import edu.uic.orjala.cyanos.sql.SQLCollection;
import edu.uic.orjala.cyanos.sql.SQLCompound;
import edu.uic.orjala.cyanos.sql.SQLData;
import edu.uic.orjala.cyanos.sql.SQLMaterial;
import edu.uic.orjala.cyanos.sql.SQLSample;
import edu.uic.orjala.cyanos.sql.SQLSeparation;
import edu.uic.orjala.cyanos.sql.SQLStrain;
import edu.uic.orjala.cyanos.web.listener.CyanosRequestListener;

/**
 * Servlet implementation class WebDavServlet
 */
@WebServlet(description = "WebDAV Servlet", urlPatterns = { "/webdav", "/webdav/", "/webdav/*" })
public class WebDavServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	private static final String XMLNS_DAV = "DAV:";
	private static final String XMLNS_CYANOS = "urn:cyanos-schema";

	private static final String METHOD_HEAD = "HEAD";
	private static final String METHOD_PROPFIND = "PROPFIND";
	private static final String METHOD_PROPPATCH = "PROPPATCH";
	private static final String METHOD_COPY = "COPY";
	private static final String METHOD_MOVE = "MOVE";
	private static final String METHOD_LOCK = "LOCK";
	private static final String METHOD_UNLOCK = "UNLOCK";

	public static final String ATTR_DAV_OBJECT = "davObject";
	
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
		BASE("0", 0), SUB("1", 1), INFINITY("infinity", 1);
	
		private String name;
		private int depth = 0;
		
		DavDepth(String name, int depth) {
			this.name = name;
			this.depth = depth;
		}
	}
	
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
	
	class DavLock {
		private String lockType = "write";
		private LockScope scope;
		private DavDepth depth;
		private String owner;
		private Date timeout;
		private HashSet<String> tokens = new HashSet<String>();
		private String path;	

	
		void write(String xmlns, String urlBase, XMLStreamWriter xtw) throws XMLStreamException {
			xtw.writeStartElement(xmlns, DAVPROP_ACTIVELOCK);

			xtw.writeStartElement(xmlns, DAVPROP_LOCKTYPE);
			xtw.writeEmptyElement(xmlns, lockType);			
			xtw.writeEndElement();

			xtw.writeStartElement(xmlns, DAVPROP_LOCKSCOPE);
			xtw.writeEmptyElement(xmlns, scope.name);			
			xtw.writeEndElement();
			
			xtw.writeStartElement(xmlns, DAVLOCK_DEPTH);
			xtw.writeCharacters(depth.name);
			xtw.writeEndElement();
			
			xtw.writeStartElement(xmlns, DAVLOCK_OWNER);
			xtw.writeCharacters(owner);
			xtw.writeEndElement();
			
			xtw.writeStartElement(xmlns, DAVLOCK_TIMEOUT);
			xtw.writeCharacters(TIMEOUT_PREFIX);
			xtw.writeCharacters(String.format("%d", timeout.getTime() - (System.currentTimeMillis() / 1000)));
			xtw.writeEndElement();
			
			Iterator<String> tokenIter = tokens.iterator();
			
			while (tokenIter.hasNext() ) {			
				xtw.writeStartElement(xmlns, DAVLOCK_TOKEN);
				xtw.writeStartElement(xmlns, "href");
				xtw.writeCharacters("urn:uuid:");
				xtw.writeCharacters(tokenIter.next());
				xtw.writeEndElement();
				xtw.writeEndElement();
			}

			xtw.writeStartElement(xmlns, DAVLOCK_ROOT);
			xtw.writeStartElement(xmlns, "href");
			xtw.writeCharacters(urlBase);
			if ( (! urlBase.endsWith("/") ) && (! path.startsWith("/") ) ) {
				xtw.writeCharacters("/");
			}
			xtw.writeCharacters(path);
			xtw.writeEndElement();
			xtw.writeEndElement();
			
			xtw.writeEndElement();
		}

	}
	
	class DavLockMap {
		private final HashMap<String,DavLock> lockPaths = new HashMap<String,DavLock>();
		private final HashMap<String,DavLock> lockToken = new HashMap<String,DavLock>();

		DavLockMap() {
			
		}
		
		String addLock(String path, DavLock lock) {
			this.lockPaths.put(path, lock);
			String lockToken = UUID.randomUUID().toString();
			lock.tokens.add(lockToken);
			this.lockToken.put(lockToken, lock);
			return lockToken;
		}
		
		DavLock getLockForToken(String token) {
			return this.lockToken.get(token);
		}
		
		DavLock getLockForPath(String path) {
			Date now = new Date();
			int depth = 0;
			while ( path != null ) {
				DavLock lock = this.getLock(path, now);			
				if ( lock != null && (lock.depth == DavDepth.INFINITY || lock.depth.depth >= depth) ) return lock;
				int pos = path.lastIndexOf("/");
				if ( pos > 0 ) {
					path = path.substring(0, pos);
				} else {
					path = null;
				}
				depth++;
			}
			return null;
		}
		
		DavLock getLock(String path, Date now) {
			DavLock lock = this.lockPaths.get(path);
			if ( lock != null ) {
				if ( lock.timeout != null ) {
					if ( lock.timeout.after(now) ) {
						return lock;
					} else {
						this.lockPaths.remove(path);
					}
				}
				return lock;
			}
			return null;
		}
		
		void remove(String lockToken) {
			DavLock lock = this.lockToken.get(lockToken);
			if ( lock != null ) {
				this.lockToken.remove(lockToken);
				lock.tokens.remove(lockToken);
				if ( lock.tokens.size() == 0 ) {
					this.lockPaths.remove(lock.path);
				}
			}
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

		void addProperty(String prop) {
			this.propList.add(prop);
		}
		
		void setReturnValues(boolean values) {
			this.values = values;
		}
		
		void writeProperties(String url, Map<String,DavProperty> props) throws XMLStreamException {
			List<String> badProps = new ArrayList<String>();
			this.out.writeStartElement(XMLNS_DAV, "response");
			this.out.writeStartElement(XMLNS_DAV, "href");
			this.out.writeCharacters(url);
			this.out.writeEndElement();
			this.out.writeStartElement(XMLNS_DAV, "propstat");
			this.out.writeStartElement(XMLNS_DAV, "prop");

			if ( propList.size() > 0 ) {
				for ( String prop : propList ) {
					if ( props.containsKey(prop) ) {
						props.get(prop).write(prop, out);
					} else {
						badProps.add(prop);
					}
				}
			} else {
				for ( Entry<String, DavProperty> entry : props.entrySet() ) {
					if ( values ) {
						entry.getValue().write(entry.getKey(), this.out);
					} else {
						this.out.writeEmptyElement(entry.getValue().xmlns, entry.getKey());
					}
				}
			}
			this.out.writeEndElement();
			this.out.writeEndElement();
			this.writeStatus("HTTP/1.1 200 OK");
			this.out.writeEndElement();		
		}
		
		void writeStatus(String status) throws XMLStreamException {
			this.out.writeStartElement(XMLNS_DAV, "status");
			this.out.writeCharacters(status);
			this.out.writeEndElement();
		}

		void writeRootProperties(int depth) throws XMLStreamException, DataException, SQLException, IOException, MagicParseException, MagicMatchNotFoundException, MagicException {
			this.writeProperties(baseURL, rootProperties);
			if ( depth > 0 ) {
				depth--;
				for ( String classname : VALID_CLASSES ) {
					this.writeClassProperties(baseURL, classname, depth);
				}
			}
			
		}

		void writeClassProperties(String classname, int depth) throws XMLStreamException, DataException, SQLException, IOException, MagicParseException, MagicMatchNotFoundException, MagicException {
			this.writeClassProperties(baseURL, classname, depth);
		}
		
		void writeClassProperties(String urlPrefix, String classname, int depth) throws XMLStreamException, DataException, SQLException, IOException, MagicParseException, MagicMatchNotFoundException, MagicException {
			String path = classname.concat("/");
			String urlPath = urlPrefix.concat(path);
			
			HashMap<String, DavProperty> props = new HashMap<String,DavProperty>();
			props.put(DAVPROP_CREATIONDATE, new DavProperty(creationDateFormat.format(creationDate)));
			props.put(DAVPROP_DISPLAYNAME, new DavProperty(classname));
			props.put(DAVPROP_RESOURCETYPE, new DavElementProperty("collection"));
			props.put(DAVPROP_SUPPORTEDLOCK, new DavSupportedLocks());

			this.writeProperties(urlPath, props);
			if ( depth > 0 ) {
				depth--;
				DataFileObject object = getAll(data, classname);
				this.writeProperties(urlPrefix, object, depth);
			}
		}
		
		void writeProperties(DataFileObject object, int depth) throws XMLStreamException, DataException, IOException, MagicParseException, MagicMatchNotFoundException, MagicException {
			this.writeProperties(baseURL, object, depth);
		}

		void writeProperties(String urlPrefix, DataFileObject object, int depth) throws XMLStreamException, DataException, IOException, MagicParseException, MagicMatchNotFoundException, MagicException {
			if ( object != null ) {
				object.beforeFirst();
				while ( object.next() ) {
					String path = String.format("%s/%s/", object.getDataFileClass(), object.getID());
					String urlPath = urlPrefix.concat(path);
					HashMap<String, DavProperty> props = new HashMap<String,DavProperty>();
					props.put(DAVPROP_CREATIONDATE, new DavProperty(creationDateFormat.format(object.getDate())));
					if ( object instanceof Material ) {
						Material material = (Material) object;
						props.put(DAVPROP_DISPLAYNAME, new DavProperty(String.format("%s (%s)", material.getLabel(), material.getID())));			
					} else if ( object instanceof Separation ) {
						Separation sep = (Separation) object;
						props.put(DAVPROP_DISPLAYNAME, new DavProperty(String.format("%s (%s)", sep.getTag(), sep.getID())));										
					} else {
						props.put(DAVPROP_DISPLAYNAME, new DavProperty(object.getID()));										
					}
					props.put(DAVPROP_RESOURCETYPE, new DavElementProperty("collection"));
					props.put(DAVPROP_SUPPORTEDLOCK, new DavSupportedLocks());

					this.writeProperties(urlPath, props);
					if ( depth > 0 ) {
						depth--;
						this.writeProperties(urlPrefix, object.getDataFiles(), object, depth);
					}

				}
			}
		}

		void writeProperties(ExternalFile file, DataFileObject parent, int depth) throws XMLStreamException, DataException, IOException, MagicParseException, MagicMatchNotFoundException, MagicException {
			this.writeProperties(baseURL, file, parent, depth);
		}
		
		void writeProperties(String urlPrefix, ExternalFile file, DataFileObject parent, int depth) throws XMLStreamException, DataException, IOException, MagicParseException, MagicMatchNotFoundException, MagicException {
			if ( file != null ) {
				file.beforeFirst();
				while ( file.next() ) {
					String path = String.format("%s/%s/%s", parent.getDataFileClass(), parent.getID(), file.getFilePath());
					String urlPath = urlPrefix.concat(path);
					HashMap<String, DavProperty> props = new HashMap<String,DavProperty>();
					File fileObj = file.getFileObject();
					BasicFileAttributes fileAttr = Files.readAttributes(fileObj.toPath(), BasicFileAttributes.class);
					Date ctime = new Date(fileAttr.creationTime().toMillis());
					props.put(DAVPROP_CREATIONDATE, new DavProperty(creationDateFormat.format(ctime)));
					props.put(DAVPROP_DISPLAYNAME, new DavProperty(fileObj.getName()));										
					props.put(DAVPROP_RESOURCETYPE, new DavProperty());
					props.put(DAVPROP_SUPPORTEDLOCK, new DavSupportedLocks());
					props.put(DAVPROP_GETCONTENTLEN, new DavProperty(String.valueOf(fileObj.length())));
					MagicMatch aMatch = Magic.getMagicMatch(fileObj, true);
					props.put(DAVPROP_GETCONTENTTYPE, new DavProperty(aMatch.getMimeType()));
					props.put(DAVPROP_GETLASTMOD, new DavProperty(creationDateFormat.format(new Date(fileObj.lastModified()))));
//					props.put("dataType", new DavProperty(XMLNS_CYANOS, file.getDataType()));
//					props.put("description", new DavProperty(XMLNS_CYANOS, file.getDescription()));
					this.writeProperties(urlPath, props);				
				}
			}
		}		
	}
	
	class DavProperty {
		String value;
		String xmlns = XMLNS_DAV;

		DavProperty() { }
		
		DavProperty(String value) {
			this.value = value;
		}
		
		DavProperty(String xmlns, String value) {
			this(value);
			this.xmlns = xmlns;
		}
		
		void write(String property, XMLStreamWriter xtw) throws XMLStreamException {
			if ( value != null ) {
				xtw.writeStartElement(this.xmlns, property);
				xtw.writeCharacters(value);
				xtw.writeEndElement();
			} else {
				xtw.writeEmptyElement(this.xmlns, property);
			}
		}
	}
	
	class DavElementProperty extends DavProperty {
		DavElementProperty(String value) {
			super(value);
		}
		
		void write(String property, XMLStreamWriter xtw) throws XMLStreamException {
			if ( value != null ) {
				xtw.writeStartElement(this.xmlns, property);
				xtw.writeEmptyElement(this.xmlns, value);
				xtw.writeEndElement();
			} else {
				xtw.writeEmptyElement(this.xmlns, property);
			}
		}		
	}
	
	class DavSupportedLocks extends DavProperty {

		void write(String property, XMLStreamWriter xtw) throws XMLStreamException {
			this.write(xtw);
		}
		
		void write(XMLStreamWriter xtw) throws XMLStreamException {
			xtw.writeStartElement(xmlns, DAVPROP_LOCKENTRY);
			xtw.writeStartElement(xmlns, DAVPROP_LOCKSCOPE);
			xtw.writeEmptyElement(xmlns, LockScope.SHARED.name);			
			xtw.writeEndElement();
			xtw.writeStartElement(xmlns, DAVPROP_LOCKTYPE);
			xtw.writeEmptyElement(xmlns, DAVLOCK_WRITE);			
			xtw.writeEndElement();
			xtw.writeEndElement();
			xtw.writeStartElement(xmlns, DAVPROP_LOCKENTRY);
			xtw.writeStartElement(xmlns, DAVPROP_LOCKSCOPE);
			xtw.writeEmptyElement(xmlns, LockScope.EXCLUSIVE.name);			
			xtw.writeEndElement();
			xtw.writeStartElement(xmlns, DAVPROP_LOCKTYPE);
			xtw.writeEmptyElement(xmlns, DAVLOCK_WRITE);			
			xtw.writeEndElement();
			xtw.writeEndElement();
		}
	}
	
    /**
     * Simple date format for the creation date ISO representation (partial).
     */
    protected static final SimpleDateFormat creationDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
	static final Properties mimeProps = new Properties();
	private DavLockMap lockMap;
	private final HashMap<String,DavProperty> rootProperties = new HashMap<String,DavProperty>(4);
	private Date creationDate;
	
    /**
     * Default lock timeout value.
     */
    private static final int DEFAULT_TIMEOUT = 3600;

    /**
     * Maximum lock timeout.
     */
    private static final int MAX_TIMEOUT = 604800;
	
	// PROPFIND - Specify a property mask.
    private static final int FIND_BY_PROPERTY = 0;
    // PROPFIND - Display all properties.
    private static final int FIND_ALL_PROP = 1;
    // PROPFIND - Return property names.
    private static final int FIND_PROPERTY_NAMES = 2;

	private static final int INFINITY = 3;
	
	private static final String DAVPROP_CREATIONDATE = "creationdate";
	private static final String DAVPROP_DISPLAYNAME = "displayname";
	private static final String DAVPROP_GETCONTENTLANG = "getcontentlanguage";
	private static final String DAVPROP_GETCONTENTLEN = "getcontentlength";
	private static final String DAVPROP_GETCONTENTTYPE = "getcontenttype";
	private static final String DAVPROP_GETETAG = "getetag";
	private static final String DAVPROP_GETLASTMOD = "getlastmodified";
	private static final String DAVPROP_LOCKDISCOV = "lockdiscovery";

	private static final String DAVPROP_LOCKTYPE = "locktype";
	private static final String DAVPROP_LOCKSCOPE = "lockscope";
	private static final String DAVPROP_LOCKENTRY = "lockentry";
	private static final String DAVPROP_ACTIVELOCK = "activelock";

	private static final String DAVLOCK_INFO = "lockinfo";
	private static final String DAVLOCK_OWNER = "owner";
	private static final String DAVLOCK_WRITE = "write";
	private static final String DAVLOCK_DEPTH = "depth";
	private static final String DAVLOCK_TIMEOUT = "timeout";
	private static final String DAVLOCK_TOKEN = "locktoken";
	private static final String DAVLOCK_ROOT = "lockroot";
	
	private static final String DAVPROP_RESOURCETYPE = "resourcetype";
	private static final String DAVPROP_SUPPORTEDLOCK = "supportedlock";

	private String realm = "cyanos";
	
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
		creationDate = new Date();
		rootProperties.put(DAVPROP_CREATIONDATE, new DavProperty(creationDateFormat.format(creationDate)));
		rootProperties.put(DAVPROP_DISPLAYNAME, new DavProperty("CYANOS Database"));
		rootProperties.put(DAVPROP_RESOURCETYPE, new DavElementProperty("collection"));
		rootProperties.put(DAVPROP_SUPPORTEDLOCK, new DavSupportedLocks());
		
		realm = context.getContextPath();
		if ( realm.startsWith("/") ) {
			realm = realm.substring(1);
		}
		if ( realm.endsWith("/") ) {
			realm = realm.substring(0, realm.length() - 1);
		}
	}

	/**
	 * @see HttpServlet#service(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String method = req.getMethod();
        
        if ( req.getRemoteUser() == null ) {
        	String auth = req.getHeader("Authorization");
        	if ( auth != null && auth.startsWith("Basic")) {
        		auth = new String(Base64.decodeBase64(auth.substring(6)),"US-ASCII");
        		String[] authinfo = auth.split(":", 2);
        		try { 
        			req.login(authinfo[0], authinfo[1]);
        		} catch (ServletException e) {
            		resp.setHeader("WWW-Authenticate", String.format("Basic realm=\"%s\"", this.realm));
            		resp.setStatus(401);
            		return;
        		}
        	} else {
        		resp.setHeader("WWW-Authenticate", String.format("Basic realm=\"%s\"", this.realm));
        		resp.setStatus(401);
        		return;
        	}
        }
        
		try {
			if (! validPath(req) ) { 
				resp.sendError(HttpServletResponse.SC_NOT_FOUND);
				return; 
			}
		} catch (DataException | SQLException e) {
			throw new ServletException(e);
		}

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

	protected void doUnlock(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		if ( isLocked(req) ) {
			resp.sendError(SC_LOCKED, "Locked");
		}

    	if ( req.getHeader(HEADER_LOCKTOKEN) != null ) {
    		String locktoken = req.getHeader(HEADER_LOCKTOKEN).substring(10);
    		locktoken = locktoken.substring(0, locktoken.length() - 1);
    		DavLock lock = this.lockMap.getLockForToken(locktoken);
    		if ( lock != null && lock.path.equals(req.getPathInfo()) ) {   			
    			this.lockMap.remove(locktoken);
    			resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    		} else {
    			resp.setStatus(HttpServletResponse.SC_CONFLICT);
    		}
    	} else {
    		resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    	}
	}

	private static final int SC_LOCKED = 423;
	private static final int SC_MULTISTATUS = 207;
	private static final String HEADER_LOCKTOKEN = "Lock-Token";
	private static final String HEADER_TIMEOUT = "Timeout";
	private static final String HEADER_IF = "If";
	private static final String TIMEOUT_PREFIX = "Second-";
	
	protected void doLock(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		if ( isLocked(req) ) {
			resp.sendError(SC_LOCKED, "Locked");
		}
	
        try {
        	DavLock lock = null;
        	String locktoken = null;
        	
        	if ( req.getHeader(HEADER_LOCKTOKEN) != null ) {
        		locktoken = req.getHeader(HEADER_LOCKTOKEN).substring(10);
        		locktoken = locktoken.substring(0, locktoken.length() - 1);
        		lock = lockMap.getLockForToken(locktoken);
        	}
        	
        	if ( lock == null ) {
        		lock = this.parseLockInfo(req);
        	}

			String lockString = req.getHeader(HEADER_TIMEOUT);
			int lockDuration = DEFAULT_TIMEOUT;
			
			if ( lockString != null ) {
				int pos = lockString.indexOf(",");
				if ( pos > 0 ) {
					lockString = lockString.substring(0, pos);
				}
				if ( lockString.equalsIgnoreCase(DavDepth.INFINITY.name) ) {
					lockDuration = MAX_TIMEOUT;
				} else if ( lockString.startsWith(TIMEOUT_PREFIX) ) {
					lockDuration = (new Integer(lockString.substring(7))).intValue();
					if ( lockDuration == 0 ) lockDuration = DEFAULT_TIMEOUT;
					if ( lockDuration > MAX_TIMEOUT ) lockDuration = MAX_TIMEOUT;
				}
			}
			lock.timeout = new Date(System.currentTimeMillis() + (lockDuration * 1000));

			if ( locktoken == null ) {
				locktoken = this.lockMap.addLock(req.getPathInfo(), lock);
	        	resp.setHeader(HEADER_LOCKTOKEN, String.format("<urn:uuid:%s>", locktoken));
			}

			resp.setContentType("application/xml");
        	resp.setCharacterEncoding("utf-8");
        	
        	XMLOutputFactory xof = XMLOutputFactory.newInstance();
        	XMLStreamWriter xtw = xof.createXMLStreamWriter(resp.getOutputStream());
        	xtw.writeStartDocument("utf-8","1.0");
        	xtw.writeStartElement("D", "prop", XMLNS_DAV);
        	xtw.writeStartElement(XMLNS_DAV, DAVPROP_LOCKDISCOV);
        	
        	String scheme = req.getScheme();
    		StringBuffer url = new StringBuffer(scheme);
    		url.append("://");
    		url.append(req.getServerName());
    		int port = req.getServerPort();
    		if ( (scheme.equalsIgnoreCase("http") && port != 80) || (scheme.equalsIgnoreCase("https") && port != 443)  ) {
    			url.append(":");
    			url.append(port);
    		}
    		url.append(req.getContextPath());
    		url.append("webdav");
        	
        	lock.write(XMLNS_DAV, url.toString(), xtw);
    
        	xtw.writeEndElement();
        	xtw.writeEndElement();
        	xtw.writeEndDocument();
        	resp.setStatus(HttpServletResponse.SC_OK);
        } catch (SAXException | XMLStreamException e) {
        	throw new ServletException(e);
        }       
		
	}

	private DavLock parseLockInfo(HttpServletRequest req) throws ServletException, IOException, SAXException {
		DavLock lock = new DavLock();
		
		lock.depth = getDepth(req);
		
        DocumentBuilder documentBuilder = getDocumentBuilder();		
        InputStream in = req.getInputStream();
		Document document = documentBuilder.parse(in);
    	Element root = document.getDocumentElement();
    	NodeList nodes = root.getChildNodes();
    	for ( int i = 0; i < nodes.getLength(); i++ ) {
    		Node aNode = nodes.item(i);
    		if ( aNode.getNodeType() == Node.ELEMENT_NODE ) {
    			String nodeName = aNode.getNodeName();
    			if ( nodeName.equalsIgnoreCase(DAVLOCK_INFO) ) {
    				NodeList lockNodes = aNode.getChildNodes();
    				for ( int n = 0; n < nodes.getLength(); n++ ) {
    					Node locknode = lockNodes.item(n);
    					if ( locknode.getNodeType() == Node.ELEMENT_NODE ) {
    						String lockElName = locknode.getNodeName();
    						if ( lockElName.equalsIgnoreCase(DAVPROP_LOCKTYPE) ) {
    							Node type = locknode.getFirstChild();
    							lock.lockType = type.getNodeName();
    						} else if ( lockElName.equalsIgnoreCase(DAVPROP_LOCKSCOPE) ) {
    							Node scope = locknode.getFirstChild();
    							if ( scope != null && scope.getNodeName().equalsIgnoreCase(LockScope.SHARED.name) ) {
    								lock.scope = LockScope.SHARED;
    							} else {
    								lock.scope = LockScope.EXCLUSIVE;
    							}
    						} else if ( lockElName.equalsIgnoreCase(DAVLOCK_OWNER) ) {
    							Node owner = locknode.getFirstChild();
    							if ( owner != null ) {
    								lock.owner = owner.getTextContent();
    							} else {
    								lock.owner = locknode.getTextContent();
    							}
    						}
    					}
    				}
    			}
    		}
    	}
    	lock.path = req.getPathInfo();
    	return lock;
	}
	
	private boolean isLocked(HttpServletRequest req) {
		String ifHeader = req.getHeader(HEADER_IF);
		if (ifHeader == null)
			ifHeader = "";

		String lockTokenHeader = req.getHeader(HEADER_LOCKTOKEN);
		if (lockTokenHeader == null)
			lockTokenHeader = "";

		ifHeader = ifHeader + lockTokenHeader;
		
		DavLock lock = lockMap.getLockForPath(req.getPathInfo());
		
		if ( lock != null ) {
			for (String token : lock.tokens ) {
				if ( ifHeader.indexOf(token) != -1 )
					return false;
			}
			return true;
		}
		return false;
	}

	protected void doMove(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
	}

	protected void doCopy(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// TODO Auto-generated method stub
		
	}

	protected void doProppatch(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		SQLData data;
		
		try {
			data = getSQLData(req);
		} catch (DataException | SQLException e) {
			throw new ServletException(e);
		}

        String[] path = getPathComponents(req);		
        ServletInputStream in = req.getInputStream();
        DocumentBuilder documentBuilder = getDocumentBuilder();		

        try {

        	
        	
        	if ( in.available() > 0 ) {
        		Document document = documentBuilder.parse(in);
        		Element root = document.getDocumentElement();
        		NodeList nodes = root.getChildNodes();
        		for ( int i = 0; i < nodes.getLength(); i++ ) {
        			Node aNode = nodes.item(i);
        			if ( aNode.getNodeType() == Node.ELEMENT_NODE ) {
        				String nodeName = aNode.getNodeName();
        				if ( nodeName.endsWith("set") ) {

        				} else if ( nodeName.endsWith("remove") ) {

        				}
        			}
        		}
        	}

        	XMLOutputFactory xof = XMLOutputFactory.newInstance();
        	xof.setProperty("javax.xml.stream.isRepairingNamespaces", true);
        	XMLStreamWriter xtw = xof.createXMLStreamWriter(resp.getOutputStream());
        	xtw.writeStartDocument();
        	xtw.writeStartElement("D", "multistatus", XMLNS_DAV);

           	String url = req.getContextPath().concat(req.getServletPath()).concat("/");

        	int pathLen = path.length;
        	if ( path[pathLen - 1].length() == 0 ) 
        		pathLen--;

        	if ( pathLen == 0 ) {

        	} else if ( pathLen == 1 ) {

        	} else if ( pathLen == 2 ) {

        	} else if ( pathLen == 3 ) {

        	}				
        	xtw.writeEndElement();				
        	xtw.writeEndDocument();
        	resp.setStatus(SC_MULTISTATUS);
        } catch (SAXException | XMLStreamException e) {
        	throw new ServletException(e);
        }

	}
		
	// http://www.webdav.org/specs/rfc4918.html#METHOD_PROPFIND
	protected void doPropfind(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		SQLData data;
		
		try {
			data = getSQLData(req);
		} catch (DataException | SQLException e) {
			throw new ServletException(e);
		}
		
		int depth = getDepth(req).depth;
		int type = FIND_ALL_PROP;
        String[] path = getPathComponents(req);		
		
        ServletInputStream in = req.getInputStream();

        Node propNode = null;
        DocumentBuilder documentBuilder = getDocumentBuilder();		
        try {
        	if ( in.available() > 0 ) {
        		Document document = documentBuilder.parse(in);
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
        	} else {
        		type = FIND_ALL_PROP;
        	}

        	XMLOutputFactory xof = XMLOutputFactory.newInstance();
        	xof.setProperty("javax.xml.stream.isRepairingNamespaces", true);
        	XMLStreamWriter xtw = xof.createXMLStreamWriter(resp.getOutputStream());
        	xtw.writeStartDocument();
        	xtw.writeStartElement("D", "multistatus", XMLNS_DAV);

//        	URL url = new URL(req.getScheme(), req.getServerName(), req.getServerPort(), req.getContextPath().concat(req.getServletPath()).concat("/") );
           	String url = req.getContextPath().concat(req.getServletPath()).concat("/");

        	PropertyWriter propOuts = new PropertyWriter(url, xtw, data);

        	if ( type == FIND_BY_PROPERTY || type == FIND_ALL_PROP ) {
        		propOuts.setReturnValues(true);
        		if ( propNode != null ) {
        			NodeList nodes = propNode.getChildNodes();
        			for ( int i = 0; i < nodes.getLength(); i++ ) {
        				Node aNode = nodes.item(i);
        				if ( aNode.getNodeType() == Node.ELEMENT_NODE ) {
        					propOuts.addProperty(aNode.getLocalName());
        				}
        			}
        		}				
        	} else if ( type == FIND_PROPERTY_NAMES ) {
        		propOuts.setReturnValues(false);
        	}

        	int pathLen = path.length;
        	if ( path[pathLen - 1].length() == 0 ) 
        		pathLen--;

        	if ( pathLen == 0 ) {
        		propOuts.writeRootProperties(depth);
        	} else if ( pathLen == 1 ) {
        		propOuts.writeClassProperties(path[0], depth);
        	} else if ( pathLen == 2 ) {
        		propOuts.writeProperties(getObject(req), depth);
        	} else if ( pathLen == 3 ) {
        		DataFileObject object = getObject(req);
        		propOuts.writeProperties(object.getDataFile("/"+ path[2]), object, depth);
        	}				
        	xtw.writeEndElement();				
        	xtw.writeEndDocument();
        	resp.setStatus(SC_MULTISTATUS);
        } catch (SAXException | XMLStreamException | DataException | SQLException | MagicParseException | MagicMatchNotFoundException | MagicException e) {
        	throw new ServletException(e);
        }
	}



	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String[] path = getPathComponents(request);		

    	int pathLen = path.length;
    	if ( path[pathLen - 1].length() == 0 ) 
    		pathLen--;

    	try {
    		if ( pathLen == 3 ) {
    			DataFileObject object = getObject(request);
    			if ( object.isAllowed(Role.READ) )
    				this.writeFile(object.getDataFile("/"+ path[2]), response);
    			else { 
    				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    				return;
    			}
    		} else {
    			switch(pathLen) {
    			case 0: request.setAttribute(ATTR_DAV_OBJECT, VALID_CLASSES); break;
    			case 1: 
    				if ( ! isAllowed(request, path[0], Role.READ) ) {    					
    					response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    					return;
    				}
    				request.setAttribute(ATTR_DAV_OBJECT, getAll(getSQLData(request), path[0])); break;
    			case 2:	DataFileObject object = getObject(request);
    				if ( ! object.isAllowed(Role.READ) ) {
    					response.setStatus(HttpServletResponse.SC_FORBIDDEN);
    					return;
    				}
    				request.setAttribute(ATTR_DAV_OBJECT, object.getDataFiles()); break;
    			}
    			RequestDispatcher disp = getServletContext().getRequestDispatcher("/includes/file-list.jsp");
    			disp.forward(request, response);					
    		}
    	} catch (DataException | SQLException e) {
    		throw new ServletException(e);
    	}
    	response.setStatus(HttpServletResponse.SC_OK);
	}
	
	private static final int BUFFER_SIZE = 1024 * 1024; // 1 MB

	private void writeFile(ExternalFile file, HttpServletResponse resp) throws IOException, DataException {
		if ( file.first() ) {
			OutputStream out = resp.getOutputStream();
			InputStream fileIn = file.getInputStream();
			Long thisSize = new Long(file.getFileObject().length());
			resp.setContentLength(thisSize.intValue());
			byte[] buffer = new byte[BUFFER_SIZE];
			int count;
			while ( (count = fileIn.read(buffer)) > 0  ) {
				out.write(buffer, 0, count);
			}
		}
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
		if ( isLocked(request) ) {
			response.sendError(SC_LOCKED, "Locked");
		}
		
		String[] path = getPathComponents(request);		

		int pathLen = path.length;
		if ( path[pathLen - 1].length() == 0 ) 
			pathLen--;

		try { 
			if ( pathLen == 3 ) {
				DataFileObject object = getObject(request);
				if ( object.isAllowed(Role.WRITE) ) {
					String filePath = "/" + path[2];
					ExternalFile file = object.getDataFile(filePath);
					if ( ! file.first() ) {
						object.linkDataFile(filePath, "", "", request.getContentType());
						file = object.getDataFile(filePath);
					}
					File outputFile = file.getFileObject();
					boolean ready = outputFile.exists();
					if ( ready ) {
						response.setStatus(HttpServletResponse.SC_OK);
					} else {
						ready = outputFile.createNewFile();
						if ( ready )
							response.setStatus(HttpServletResponse.SC_CREATED);
					}
					if ( ready ) {
						BufferedInputStream fileData = new BufferedInputStream(request.getInputStream());
						FileOutputStream fileOut = new FileOutputStream(outputFile);
						byte[] buffer = new byte[BUFFER_SIZE];
						int count;
						while ( (count = fileData.read(buffer)) > 0  ) {
							fileOut.write(buffer, 0, count);
						}
						fileOut.close();
						fileData.close();
					}
				
				} else { 
					response.setStatus(HttpServletResponse.SC_FORBIDDEN);
					return;
				}
			}
		} catch (DataException | SQLException e) {
			throw new ServletException(e);
		}
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

	private DavDepth getDepth(HttpServletRequest req) {
		String depthStr = req.getHeader("Depth");
		if ( depthStr != null ) {
			for ( DavDepth possible : DavDepth.values() ) {
				if ( depthStr.equals(possible.name) ) {
					return possible;
				}
			}
		}
		return DavDepth.INFINITY;
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

	private static String[] getPathComponents(HttpServletRequest req) {
        String module = req.getPathInfo();
        if ( module == null ) {
        	module = "";
        } else if ( module.startsWith("/") )
        	module = module.substring(1);
        
        if ( module.endsWith("/") ) 
        	module = module.substring(0, module.length() - 1);
        
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
        String[] path = getPathComponents(req);   
		if ( path.length > 0 && path[0].length() > 0 ) {
			boolean valid = validClass(path[0]);
			if ( valid && path.length > 1 && path[1].length() > 0 ) {
				DataFileObject object = getObject(req, path[0], path[1]);
				valid = ( object != null && object.first() );
				if ( valid ) {
					req.setAttribute(ATTR_OBJECT, object);
					if ( path.length > 2 && path[2].length() > 0 ) {
						valid = object.hasDataFile("/" + path[2]);
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
	        String[] path = getPathComponents(req);		
			DataFileObject myObj = getObject(req, path[0], path[1]);
			req.setAttribute(ATTR_OBJECT, myObj);
			return myObj;
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
		} else if ( objClass.equals(Collection.DATA_FILE_CLASS) ) {
			return SQLCollection.load(data, objID);
		}
		return null;
	}

	private static boolean isAllowed(HttpServletRequest req, String objClass, int action) throws DataException, SQLException {
		SQLData data = getSQLData(req);
		User user = data.getUser();
		if ( objClass.equals("sample") ) {
			return user.couldPerform(User.SAMPLE_ROLE, action);
		} else if (objClass.equals("strain") ) {
			return user.couldPerform(User.CULTURE_ROLE, action);
		} else if (objClass.equals("separation") ) {
			return user.couldPerform(User.SAMPLE_ROLE, action);
		} else if (objClass.equals("compound") ) {
			return user.couldPerform(User.SAMPLE_ROLE, action);
		} else if ( objClass.equals("assay") ) {
			return user.couldPerform(User.BIOASSAY_ROLE, action);
		} else if ( objClass.equals(Material.DATA_FILE_CLASS) ) {
			return user.couldPerform(User.SAMPLE_ROLE, action);
		}
		return true;
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
			return SQLMaterial.find(data, "");
		} else if ( objClass.equals(Collection.DATA_FILE_CLASS ) ) {
			return SQLCollection.collections(data);
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
	
	public static SQLData getSQLData(HttpServletRequest req) throws SQLException, DataException {
		return CyanosRequestListener.getSQLData(req);
	}
	
	private static final long GIGA = 1024 * 1024 * 1024;
	private static final long MEGA = 1024 * 1024;
	private static final long KILO = 1024;
	
	public static String humanSize(long size) {
		if ( size >= GIGA ) {
			double newSize = size / GIGA;
			return String.format("%.1f GB", newSize);
		} else if ( size >= MEGA ) {
			double newSize = size / MEGA;
			return String.format("%.1f MB", newSize);
		} else if ( size >= KILO ) {
			double newSize = size / KILO;
			return String.format("%.1f kB", newSize);
		} else {
			return String.format("%d B", size);
		}
	}
}
