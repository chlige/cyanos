package edu.uic.orjala.cyanos.web.servlet;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.uic.orjala.cyanos.Collection;
import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.DataFileObject;
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
	private static final String METHOD_MKCOL = "MKCOL";
	private static final String METHOD_COPY = "COPY";
	private static final String METHOD_MOVE = "MOVE";
	private static final String METHOD_LOCK = "LOCK";
	private static final String METHOD_UNLOCK = "UNLOCK";

    /**
     * Simple date format for the creation date ISO representation (partial).
     */
    protected static final SimpleDateFormat creationDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	static final Properties mimeProps = new Properties();
	
	/* (non-Javadoc)
	 * @see javax.servlet.GenericServlet#init()
	 */
	@Override
	public void init() throws ServletException {
		super.init();
		try {
			Magic.initialize();
		} catch (MagicParseException e) {
			this.log("Unable to load MIME Magic.", e);
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
        } else if (method.equals(METHOD_MKCOL)) {
            doMkcol(req, resp);
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
	
	private void doPropfind(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String depthStr = req.getHeader("Depth");

		int depth = INFINITY; 
		int type = FIND_ALL_PROP;
		
		if ( depthStr != null ) {
			if ( depthStr.equals("0") ) 
				depth = 0;
			else if ( depthStr.equals("1") ) 
				depth = 1;
			else if ( depthStr.equalsIgnoreCase("infinity") )
				depth = INFINITY;
		}
		
		Node propNode = null;
		
		if ( req.getInputStream().available() > 0 ) {
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
            } catch (SAXException e) {
            	throw new ServletException(e);
            }
		}
		
		if ( type == FIND_BY_PROPERTY ) {
			
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


	private static String determineMethodsAllowed(HttpServletRequest req) throws DataException, SQLException {
        StringBuffer methods = new StringBuffer();

        String module = req.getPathInfo();
        if ( module.startsWith("/") )
        	module = module.substring(1);
        
        String[] path = module.split("/", 3);    

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
