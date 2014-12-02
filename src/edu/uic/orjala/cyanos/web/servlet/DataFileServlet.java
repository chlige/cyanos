package edu.uic.orjala.cyanos.web.servlet;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.sf.jmimemagic.Magic;
import net.sf.jmimemagic.MagicException;
import net.sf.jmimemagic.MagicMatch;
import net.sf.jmimemagic.MagicMatchNotFoundException;
import net.sf.jmimemagic.MagicParseException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.uic.orjala.cyanos.Assay;
import edu.uic.orjala.cyanos.Compound;
import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.DataFileObject;
import edu.uic.orjala.cyanos.ExternalFile;
import edu.uic.orjala.cyanos.Material;
import edu.uic.orjala.cyanos.Role;
import edu.uic.orjala.cyanos.Separation;
import edu.uic.orjala.cyanos.SingleFile;
import edu.uic.orjala.cyanos.Strain;
import edu.uic.orjala.cyanos.User;
import edu.uic.orjala.cyanos.sql.SQLAssay;
import edu.uic.orjala.cyanos.sql.SQLCompound;
import edu.uic.orjala.cyanos.sql.SQLData;
import edu.uic.orjala.cyanos.sql.SQLExternalFile;
import edu.uic.orjala.cyanos.sql.SQLMaterial;
import edu.uic.orjala.cyanos.sql.SQLSample;
import edu.uic.orjala.cyanos.sql.SQLSeparation;
import edu.uic.orjala.cyanos.sql.SQLStrain;
import edu.uic.orjala.cyanos.web.AppConfig;
import edu.uic.orjala.cyanos.web.MultiPartRequest;
import edu.uic.orjala.cyanos.web.MultiPartRequest.FileUpload;
import edu.uic.orjala.cyanos.web.forms.DataForm;
import edu.uic.orjala.cyanos.web.listener.AppConfigListener;
import edu.uic.orjala.cyanos.web.listener.CyanosRequestListener;


public class DataFileServlet extends ServletObject {
	
	public static final String ACTION_UPDATE_FILE = "updateFile";
	public static final String ACTION_GET_FILES = "getfiles";
	public static final String ACTION_GET_DIRS = "getdirs";
	public static final String PARAM_OBJECT_ID = "objectID";
	public static final String PARAM_OBJECT_CLASS = "objectClass";
	public static final String ATTR_UPLOAD_MESSAGE = "uploadMessage";

	/**
	 * 
	 */
	private static final long serialVersionUID = -6573916173406892684L;
	
	private static final String FILE_ELEMENT = "file";
	
	private static final String FILENAME_ATTR = "filename";
	private static final String SIZE_ATTR = "size";
	private static final String DESC_ATTR = "description";
	private static final String ICON_ATTR = "icon";
	private static final String TYPE_ATTR = "type";
	private static final String URL_ATTR = "url";
	
	public static final String DATAFILE_PARENT = "parentObject";
	public static final String DATAFILE_TYPE_MAP = "dataFileMap";
	public static final String DATAFILE_LIST = "searchResults";
	public static final String DATAFILE_CLASS = "dataClass";

	public static final String PARAM_NEW_FILE = "newFile";
	public static final String PARAM_FILE_PATH = "filePath";
	public static final String PARAM_DATATYPE = "dataType";
	public static final String ACTION_UPLOAD = "uploadFile";
	public static final String ACTION_SHOW_BROWSER = "showBrowser";
	
	public static final String ATTR_CURRENT_DIR = "currentDir";
	public static final String ATTR_ROOT_DIR = "rootDir";
	
	public static final String[] DATA_CLASSES = { Strain.DATA_FILE_CLASS, Material.DATA_FILE_CLASS, Assay.DATA_FILE_CLASS, Compound.DATA_FILE_CLASS, Separation.DATA_FILE_CLASS };
	
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
		
		try {
			mimeProps.load(new FileInputStream(getServletContext().getRealPath(getInitParameter("mimetype-icons"))));
		} catch (FileNotFoundException e) {
			this.log("Could not find mimetype-icons properties file.", e);
		} catch (IOException e) {
			this.log("Could load mimetype-icons properties file.", e);
		}
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.servlet.ServletObject#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		super.doGet(req, res);
		String module = req.getPathInfo();	
		if ( module != null && ( module.startsWith("/get") || module.startsWith("/preview") ) ) {
			this.getFile(req, res);
			return;		
		}
		this.handleRequest(req, res);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.servlet.ServletObject#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
		req = MultiPartRequest.parseRequest(req);
		super.doPost(req, res);
		this.handleRequest(req, res);
	}

	public void doPut ( HttpServletRequest req, HttpServletResponse res ) throws ServletException, IOException {
		if ( AppConfigListener.isUpgradeInstall() ) {
			return;
		}
		
		String module = req.getPathInfo();
		if ( module != null && module.startsWith("/manager") ) {
			String[] details = module.split("/", 5);
			
			String requiredRole = getRole(details[2]);
			try {
				if ( getUser(req).isAllowed(requiredRole, User.NULL_PROJECT, Role.WRITE) ) { 

					AppConfig myConf = this.getAppConfig();
					String path = myConf.getFilePath(details[2], details[3]);

					SingleFile aFile = new SingleFile(path, details[4]);

					File outputFile;
					outputFile = aFile.getFileObject();
					if ( (! outputFile.exists()) && outputFile.createNewFile() ) {
						BufferedInputStream fileData = new BufferedInputStream(req.getInputStream());
						FileOutputStream fileOut = new FileOutputStream(outputFile);
						int d = fileData.read();
		//				int count = 1;
						while ( d != -1 ) {
							fileOut.write(d);
							d = fileData.read();
		//					count++;
						}
						fileOut.close();
						fileData.close();
						res.setStatus(HttpServletResponse.SC_CREATED);
					} else {
						res.setStatus(HttpServletResponse.SC_OK);
					}
				} else {
					res.setStatus(HttpServletResponse.SC_FORBIDDEN);
				}
			} catch (DataException e) {
				e.printStackTrace();
				res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			} catch (SQLException e) {
				e.printStackTrace();
				res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			}
		}
	}
	
	public void uploadFile(HttpServletRequest req, HttpServletResponse res, String dataFileClass, String dataFileType) throws IOException, ServletException {
		FileUpload newFile = getUpload(req, PARAM_NEW_FILE);
		if ( newFile != null ) {
			try {
				String fileName = req.getParameter(PARAM_FILE_PATH) + newFile.getName();
				String requiredRole = getRole(dataFileClass);
				if ( getUser(req).isAllowed(requiredRole, User.NULL_PROJECT, Role.WRITE) ) { 

					AppConfig myConf = this.getAppConfig();
					String path = myConf.getFilePath(dataFileClass, dataFileType);

					SingleFile aFile = new SingleFile(path, fileName);

					File outputFile;
					outputFile = aFile.getFileObject();
					if ( (! outputFile.exists()) && outputFile.createNewFile() ) {
						BufferedInputStream fileData = new BufferedInputStream(newFile.getStream());
						FileOutputStream fileOut = new FileOutputStream(outputFile);
						int d = fileData.read();
						while ( d != -1 ) {
							fileOut.write(d);
							d = fileData.read();
						}
						fileOut.close();
						fileData.close();
						req.setAttribute(ATTR_UPLOAD_MESSAGE, "File uploaded successfully.");
					} else {
						req.setAttribute(ATTR_UPLOAD_MESSAGE, "File already exists.");
					}
				} else {
					req.setAttribute(ATTR_UPLOAD_MESSAGE, "Permission denied.");
				}
			} catch (DataException e) {
				res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getLocalizedMessage());
				e.printStackTrace();
			} catch (SQLException e) {
				res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getLocalizedMessage());
				e.printStackTrace();
			}
		}
	}

	private void handleRequest(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
		String module = req.getPathInfo();	
		if ( module != null && module.startsWith("/manager") ) {
			this.handleManagerReq(req, res);
		} else if ( module != null && module.startsWith("/upload") ) {
			if ( CyanosRequestListener.getUploadCount(req, PARAM_NEW_FILE) > 0  && req.getParameter(ACTION_UPLOAD) != null ) {
				this.uploadFile(req, res, req.getParameter(DATAFILE_CLASS), req.getParameter(PARAM_DATATYPE));
			}
			RequestDispatcher disp = getServletContext().getRequestDispatcher("/includes/datafile-upload.jsp");
			disp.forward(req, res);			
		} else if ( module != null && module.startsWith("/list") ) {
			if ( CyanosRequestListener.getUploadCount(req, PARAM_NEW_FILE) > 0 && req.getParameter(ACTION_UPLOAD) != null ) {
				this.uploadFile(req, res, req.getParameter(DATAFILE_CLASS), req.getParameter(PARAM_DATATYPE));
			} else if ( req.getParameter(ACTION_SHOW_BROWSER) != null ) {
				AppConfig myConf = this.getAppConfig();
				String path = myConf.getFilePath(req.getParameter(DATAFILE_CLASS), req.getParameter(PARAM_DATATYPE));
				req.setAttribute(ATTR_ROOT_DIR, path);
				if ( req.getParameter("path") != null ) {
					req.setAttribute(ATTR_CURRENT_DIR, new File(path, req.getParameter("path")));					
				} else {
					req.setAttribute(ATTR_CURRENT_DIR, new File(path));						
				}
			}
			RequestDispatcher disp = getServletContext().getRequestDispatcher("/includes/datafiles.jsp");
			disp.forward(req, res);				
		}
		
		
		/*
		PrintWriter out = aWrap.startHTMLDoc("Data Files", false);
		DataForm aForm = new DataForm(aWrap);
		try {
			Paragraph head = new Paragraph();
			head.setAlign("CENTER");
			StyledText title = new StyledText("<B>Add Data Files</B>");
			title.setSize("+1");
			head.addItem(title);
			out.println(head);

			if ( aWrap.hasUpload("datafile") ) {
				HttpSession aSession = aWrap.getSession();
				aSession.setAttribute(DataForm.DATAFILE, aWrap.getUpload("datafile"));
			} 
			
			if ( aWrap.hasFormValue("forceUpload") ) {
				// out.println(this.forceUpload());
			} else if ( aWrap.hasFormValue("addDataAction")) {
				out.println(aForm.finishUpload());
			} else {
				out.println(aForm.fileForm());
			}
		} catch (Exception e) {
			out.println(aWrap.handleException(e));
		}
		aWrap.finishHTMLDoc();
		*/
	}

	private void handleManagerReq(HttpServletRequest req, HttpServletResponse res) throws IOException {
		String module = req.getPathInfo();	
		String[] details = module.split("/", 5);
		String requiredRole = getRole(details[2]);			
		AppConfig myConf = this.getAppConfig();
		res.setContentType("text/plain");
		String path = myConf.getFilePath(details[2], details[3]);

		try {
			if ( req.getParameter(ACTION_GET_DIRS) != null ) {
				if ( getUser(req).isAllowed(requiredRole, User.NULL_PROJECT, Role.READ) ) {
					File aDir = new File(path, req.getParameter("path"));
					File[] kids = aDir.listFiles();
					Arrays.sort(kids, DataForm.directoryFirstCompare());

					PrintWriter writer = res.getWriter();

					for ( int i = 0; i < kids.length; i++ ) {
						if ( kids[i].isDirectory() ) {
							writer.println(kids[i].getName());
						}
					}
					writer.close();
				} else 
					res.setStatus(HttpServletResponse.SC_FORBIDDEN);
			} else if ( req.getParameter(ACTION_GET_FILES) != null ) {
				if ( getUser(req).isAllowed(requiredRole, User.NULL_PROJECT, Role.READ) ) {
					try {
						this.getFileList(req, res);
					} catch (ParserConfigurationException e) {
						res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getLocalizedMessage());
						e.printStackTrace();
					} catch (DataException e) {
						res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getLocalizedMessage());
						e.printStackTrace();
					} catch (SQLException e) {
						res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getLocalizedMessage());
						e.printStackTrace();
					} catch (TransformerFactoryConfigurationError e) {
						res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getLocalizedMessage());
						e.printStackTrace();
					} catch (TransformerException e) {
						res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getLocalizedMessage());
						e.printStackTrace();
					}			
				} else 
					res.setStatus(HttpServletResponse.SC_FORBIDDEN);
			} else if ( req.getParameter(ACTION_UPDATE_FILE) != null ) {
				if ( getUser(req).isAllowed(requiredRole, User.NULL_PROJECT, Role.WRITE) ) {
					String filePath = req.getParameter("path");
					filePath = filePath.replaceAll("/+", "/");
					if ( filePath.startsWith("/") ) {
						filePath = filePath.substring(1);
					}
					SingleFile aFile = new SingleFile(path, filePath);
					DataFileObject myObject = getObject(req);
					if ( req.getParameter("delete") != null ) {
						myObject.unlinkDataFile(aFile);
					} else {
						aFile.setDescription(req.getParameter("desc"));
						aFile.setMimeType(this.getMimeType(aFile.getFileObject()));
						if ( myObject != null ) {
							myObject.linkDataFile(aFile, req.getParameter("dataType"));
						} else {
							res.setStatus(HttpServletResponse.SC_NOT_FOUND);
							PrintWriter out = res.getWriter();
							out.println("Object class not valid.");
						}
					}
				} else 
					res.setStatus(HttpServletResponse.SC_FORBIDDEN);
			} else if ( req.getParameter("getTypes") != null ) {
				if ( getUser(req).isAllowed(requiredRole, User.NULL_PROJECT, Role.READ) ) {
					String objClass = req.getParameter(PARAM_OBJECT_CLASS);
					PrintWriter writer = res.getWriter();
					Map<String,String> dataTypes = this.getAppConfig().getDataTypeMap(objClass);
					for ( Entry<String,String> entry : dataTypes.entrySet() ) {
						writer.format("%s:%s\n", entry.getKey(), entry.getValue());
					}
					writer.close();
				} else 
					res.setStatus(HttpServletResponse.SC_FORBIDDEN);
			}
		} catch (DataException e) {
			res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getLocalizedMessage());
			e.printStackTrace();
		} catch (SQLException e) {
			res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getLocalizedMessage());
			e.printStackTrace();
		}
	}
	
	private void getFileList(HttpServletRequest req, HttpServletResponse res) throws ParserConfigurationException, DataException, SQLException, IOException, TransformerFactoryConfigurationError, TransformerException {
		String module = req.getPathInfo();	
		String[] details = module.split("/", 5);
		AppConfig myConf = this.getAppConfig();
		String path = myConf.getFilePath(details[2], details[3]);

		File aDir = new File(path, req.getParameter("path"));
		File[] kids = aDir.listFiles();
		Arrays.sort(kids, DataForm.directoryFirstCompare());
		String previewURL = String.format("%s/file/preview/%s/%s", req.getContextPath(), details[2], details[3]);

		String filePath = req.getParameter("path");
		filePath = filePath.replaceAll("/+", "/");
		if ( filePath.startsWith("/") ) {
			filePath = filePath.substring(1);
		}

		
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document dom = db.newDocument();

		DataFileObject myObject = getObject(req);
		ExternalFile myFiles;
		if ( req.getParameter("dataType") != null ) {
			myFiles = myObject.getDataFilesForType(req.getParameter("dataType"));					
		} else {
			myFiles = myObject.getDataFiles();
		}

		Map<String,String> fileDesc = new HashMap<String,String>();
		Map<String,String> fileMime = new HashMap<String,String>();
		Map<String,String> fileType = new HashMap<String,String>();

		myFiles.beforeFirst();
		while ( myFiles.next() ) {
			String aPath = myFiles.getFilePath();
			fileDesc.put(aPath, myFiles.getDescription());
			fileMime.put(aPath, myFiles.getMimeType());
			fileType.put(aPath, myFiles.getDataType());
		}

		Element root = dom.createElement("directory");
		dom.appendChild(root);
		for ( File file : kids ) {
			if ( ! file.isDirectory() && (! file.getName().startsWith(".")) ) {
				Element pathEl = dom.createElement(FILE_ELEMENT);
				pathEl.setAttribute(FILENAME_ATTR, file.getName());
				pathEl.setAttribute(SIZE_ATTR, DataForm.humanReadableSize(file.length()));
				String mimeType = "";
				String aPath = String.format("%s%s", filePath, file.getName());
				if ( fileDesc.containsKey(aPath) ) {
					pathEl.setAttribute(DESC_ATTR, fileDesc.get(aPath));
					mimeType = fileMime.get(aPath);
					pathEl.setAttribute(TYPE_ATTR, fileType.get(aPath));
				} else {
					mimeType = this.getMimeType(file);
				}
				pathEl.setAttribute(ICON_ATTR, getIconPathForMIMEType(req, mimeType));
				if ( mimeType.startsWith("image") )
					pathEl.setAttribute(URL_ATTR, String.format("%s/%s", previewURL, aPath));
				root.appendChild(pathEl);
			}
		}

		System.setProperty("javax.xml.transform.TransformerFactory", "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl");
		Source aSrc = new DOMSource(dom);
		Result aResult = new StreamResult(res.getOutputStream());
		Transformer xmlTrn = TransformerFactory.newInstance().newTransformer();
		xmlTrn.setOutputProperty("indent", "yes");
		xmlTrn.transform(aSrc, aResult);
		res.setContentType("text/xml");	
	}

	private String getMimeType(File file) {
		try {
			MagicMatch aMatch = Magic.getMagicMatch(file, true);
			return aMatch.getMimeType();		
		} catch (MagicMatchNotFoundException e) {
			this.log(String.format("CANNOT determine MIME type for file %s", file.getAbsolutePath()));
		} catch (MagicParseException e) {
			this.log(String.format("CANNOT determine MIME type for file %s", file.getAbsolutePath()));
		} catch (MagicException e) {
			this.log(String.format("CANNOT determine MIME type for file %s", file.getAbsolutePath()));
		} 
		return null;
	}
	
	private DataFileObject getObject(HttpServletRequest req) throws DataException, SQLException {
		SQLData data = this.getSQLData(req);
		String objClass = req.getParameter(PARAM_OBJECT_CLASS);
		String objID = req.getParameter(PARAM_OBJECT_ID);
		
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

	
	private void getFile(HttpServletRequest req, HttpServletResponse res) throws IOException {
		String module = req.getPathInfo();	
		ServletOutputStream out = res.getOutputStream();
		String[] details = module.split("/", 5);
		
		try {
			AppConfig myConf = this.getAppConfig();
			
			String path = myConf.getFilePath(details[2], details[3]);
			String requiredRole = getRole(details[2]);
			if ( ! getUser(req).isAllowed(requiredRole, User.NULL_PROJECT, Role.READ) ) { 
				res.setStatus(HttpServletResponse.SC_FORBIDDEN);
				return;
			}
			
			ExternalFile aFile = SQLExternalFile.load(this.getSQLData(req), path, details[4]);
			String contentType = null;
			
			if ( aFile.first() ) {
				contentType = aFile.getMimeType();
			} else {					
				aFile = new SingleFile(path, details[4]);
				File fileObj = aFile.getFileObject();
				if ( fileObj.exists() ) {
					MagicMatch aMatch = Magic.getMagicMatch(aFile.getFileObject(), true);
					contentType = aMatch.getMimeType();
				}
			}

			if ( contentType != null) {
				res.setContentType(contentType);
				
				if ( contentType.startsWith("image") && (module.startsWith("/preview") || req.getParameter("height") != null || req.getParameter("width") != null ) ) {
					BufferedImage source = ImageIO.read(aFile.getFileObject());
					int width = -1, height = -1;
					double widthScale = 0, heightScale = 0;
					
					if ( module.startsWith("/preview") ) {
						height = 200;
						heightScale = (double) height / source.getHeight();							
					} else {
						if ( req.getParameter("height") != null ) {
							height = Integer.parseInt(req.getParameter("height"));
							heightScale = (double) height / source.getHeight();
						}
						if ( req.getParameter("width") != null ) {
							width = Integer.parseInt(req.getParameter("width"));
							widthScale = (double) width / source.getWidth();
						}
					}
					
					
					if ( width < 0 ) {
						widthScale = heightScale;
						width = Double.valueOf(source.getWidth() * widthScale).intValue();
					}
					if ( height < 0 ) {
						heightScale = widthScale;
						height = Double.valueOf(source.getHeight() * heightScale).intValue();
					}
					
					if ( (heightScale + widthScale > 0 ) && source.getType() != BufferedImage.TYPE_CUSTOM ) {
						BufferedImage dest = new BufferedImage(width, height, source.getType());
						Graphics2D g = dest.createGraphics();
						AffineTransform at = AffineTransform.getScaleInstance(widthScale, heightScale);
						g.drawRenderedImage(source, at);
						ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
						try {
							if ( ! this.writeImageForMIME(dest, contentType, byteOut) )
								this.log("FAILURE TO WRITE IMAGE.");
						} catch ( Exception ex ) {
							this.log(String.format("ERROR: %s", ex.getLocalizedMessage()));
							ex.printStackTrace();
						}
						g.dispose();
						byteOut.flush();
						res.setContentLength(byteOut.size());
						out.write(byteOut.toByteArray());
					} else {
						InputStream fileIn = aFile.getInputStream();
						Long thisSize = new Long(aFile.getFileObject().length());
						res.setContentLength(thisSize.intValue());
						int d = fileIn.read();
						while ( d != -1 ) {
							out.write(d);
							d = fileIn.read();
						}
						while ( d != -1 ) {
							out.write(d);
							d = fileIn.read();
						}
					}
				} else {
					InputStream fileIn = aFile.getInputStream();
					Long thisSize = new Long(aFile.getFileObject().length());
					res.setContentLength(thisSize.intValue());
					int d = fileIn.read();
					while ( d != -1 ) {
						out.write(d);
						d = fileIn.read();
					}
					while ( d != -1 ) {
						out.write(d);
						d = fileIn.read();
					}
				}
				out.flush();
				out.close();
				res.flushBuffer();
			} else {
				res.setContentType("text/plain");
				out.print("File Not Found: ");
				out.println(details[4]);
			}
		} catch (FileNotFoundException ex ) {
			res.setContentType("text/plain");
			res.resetBuffer();
			ex.printStackTrace();
			out.print("File Not Found: ");
			out.println(details[4]);
		} catch (IOException ex ) {
			res.setContentType("text/plain");
			res.resetBuffer();
			ex.printStackTrace();
			out.println("IO Error:");
			out.println(ex.getMessage());
		} catch (MagicParseException ex) {
			res.setContentType("text/plain");
			res.resetBuffer();
			ex.printStackTrace();
			out.println(ex.getMessage());
		} catch (MagicMatchNotFoundException ex) {
			res.setContentType("text/plain");
			res.resetBuffer();
			ex.printStackTrace();
			out.println(ex.getMessage());
		} catch (MagicException ex) {
			res.setContentType("text/plain");
			res.resetBuffer();
			ex.printStackTrace();
			out.println(ex.getMessage());
		} catch (DataException e) {
			res.setContentType("text/plain");
			res.resetBuffer();
			e.printStackTrace();
			out.println(e.getMessage());
		} catch (SQLException e) {
			res.setContentType("text/plain");
			res.resetBuffer();
			e.printStackTrace();
			out.println(e.getMessage());
		}
	}
	
	
	private static String getRole(String objClass) {
		if ( objClass.equals("sample") ) {
			return User.SAMPLE_ROLE;
		} else if (objClass.equals("strain") ) {
			return User.CULTURE_ROLE;
		} else if (objClass.equals("separation") ) {
			return User.SAMPLE_ROLE;
		} else if (objClass.equals("compound") ) {
			return User.SAMPLE_ROLE;
		} else if ( objClass.equals("assay") ) {
			return User.BIOASSAY_ROLE;
		}
		return null;
	}
	
	protected long getLastModified(HttpServletRequest req) {
		String module = req.getPathInfo();	
		if ( module != null && ( module.startsWith("/get") || module.startsWith("/preview") ) ) {
			AppConfig myConf = this.getAppConfig();

			String[] details = module.split("/", 5);
			String path = myConf.getFilePath(details[2], details[3]);
			File aFile = new File(path, details[4]);
			if ( aFile.exists() )
				return aFile.lastModified();

		}  
		return -1;
	}
	
	private boolean writeImageForMIME(RenderedImage anImage, String mimeType, OutputStream output) throws IOException {
		ImageOutputStream imageOut = new MemoryCacheImageOutputStream(output);
		
		Iterator<ImageWriter> writers = ImageIO.getImageWritersByMIMEType(mimeType);
		while ( writers.hasNext() ) {
			ImageWriter w = writers.next();
			try {
				w.setOutput(imageOut);
			} catch (IllegalArgumentException e) {
				continue;
			}
			w.write(anImage);
			imageOut.close();
			return true;
		}
		return false;
	}

/*	protected String loadFile(String fieldName, String sessionParam) {		
		StringBuffer output = new StringBuffer();
		this.formValues = new Hashtable<String, String[]>();
		FileItemFactory factory = new DiskFileItemFactory();
		ServletFileUpload upload = new ServletFileUpload(factory);
		try {
			List items = upload.parseRequest(req);
			Iterator iter = items.iterator();
			while (iter.hasNext() ) {
				FileItem anItem = (FileItem) iter.next();
				String thisField = anItem.getFieldName();
				if ( anItem.isFormField()) {
					if ( formValues.containsKey(thisField) ) {
						String[] vals = (String[])formValues.get(thisField);
						int next = vals.length;
						vals[next] = anItem.getString();
						formValues.put(thisField, vals);
					} else {
						String[] vals = { anItem.getString() };
						formValues.put(thisField, vals);
					}
				} else {
					if ( anItem.getFieldName().equals(fieldName) ) {
						if ( anItem.getSize() > 0 ) {
							HttpSession aSession = this.getSession();
							aSession.setAttribute(this.DATAFILE, anItem);
						}
					}
				}
			}
		} catch (FileUploadException e) {
			output.append("<P ALIGN='CENTER'><B><FONT COLOR='RED'>ERROR:</FONT> " + e.getMessage() + "</B></P>");
			e.printStackTrace();
		} 

		output.append(this.fileForm());
		return output.toString();
	}
*/

	public static String fileManagerApplet(String contextPath, String objectClass, String objectID, String dataType, boolean allowPreview) {
		StringBuffer progressApplet = new StringBuffer("<DIV ALIGN='CENTER'>\n<OBJECT classid=\"clsid:8AD9C840-044E-11D1-B3E9-00805F499D93\" ALIGN='CENTER' ");
		progressApplet.append("CODEBASE=\"http://java.sun.com/out-of-proc-plugin-url-placeholder.exe#1,6,0,10\" HEIGHT=300 WIDTH='80%' >\n");
		progressApplet.append("<PARAM NAME=\"CODE\" VALUE=\"edu.uic.orjala.cyanos.applet.FileManager\">\n");
		progressApplet.append(String.format("<PARAM NAME=\"ARCHIVE\" VALUE='%s/applets/filemanager.jar'>\n", contextPath));
		progressApplet.append(String.format("<PARAM NAME=\"urlPath\" VALUE=\"%s/file/manager/%s/%s/\">\n", contextPath, objectClass, dataType));
		progressApplet.append(String.format("<PARAM NAME=\"objclass\" VALUE=\"%s\">\n", objectClass));
		progressApplet.append(String.format("<PARAM NAME=\"objID\" VALUE=\"%s\">\n", objectID));
		if ( dataType != null )
		progressApplet.append(String.format("<PARAM NAME=\"dataType\" VALUE=\"%s\">\n", dataType));
		if ( allowPreview )
			progressApplet.append("<PARAM NAME=\"preview\" VALUE=\"true\">\n");
		
		
		progressApplet.append("<COMMENT>\n<EMBED TYPE='application/x-java-applet;version=1.5' CODE='edu.uic.orjala.cyanos.applet.FileManager' ALIGN='CENTER' HEIGHT=300 WIDTH='80%'");
		progressApplet.append(String.format("ARCHIVE='%s/applets/filemanager.jar' ", contextPath));
		progressApplet.append(String.format("urlPath='%s/file/manager/%s/%s/' ", contextPath, objectClass, dataType));
		if ( dataType != null )
			progressApplet.append(String.format(" dataType=\"%s\" ", dataType));
		if ( allowPreview )
			progressApplet.append(" preview=\"true\" ");

		progressApplet.append(String.format("objclass='%s' objID='%s' >", objectClass, objectID));

		progressApplet.append("<NOEMBED>Java not available</NOEMBED>\n</EMBED>\n</COMMENT>\n</OBJECT>\n</DIV>");
		return progressApplet.toString();
	}

	public static String getIconForMIMEType(String mimeType) {
		return "/images/icons/filetypes/".concat(mimeProps.getProperty(mimeType, "_blank.png"));
	}	
	
	public static String getIconPathForMIMEType(HttpServletRequest req, String mimeType) {
		return req.getContextPath().concat(getIconForMIMEType(mimeType));
	}
		

	public static RequestDispatcher dataFileDiv(HttpServletRequest request, ServletContext context, DataFileObject source, String dataClass) {
		request.setAttribute(DATAFILE_PARENT, source);
		request.setAttribute(DATAFILE_CLASS, dataClass);
		Object obj = context.getAttribute(APP_CONFIG_ATTR);
		if ( obj != null & obj instanceof AppConfig ) {
			request.setAttribute(DATAFILE_TYPE_MAP, ((AppConfig)obj).getDataTypeMap(dataClass));
		} 
		
		return context.getRequestDispatcher("/file/list");		
	}

	public static File[] filesForPath(File somePath) {
		File[] fileList = somePath.listFiles();
		Arrays.sort(fileList, DataForm.fileCompare());
		return fileList;
	}
	
	public static Comparator<File> fileCompare() {
		return new Comparator<File>()
		    {
		      public int compare(final File o1, final File o2) {
		        return o1.getName().compareToIgnoreCase(o2.getName());
		      }
		    };
	}
	
	public static Comparator<File> directoryFirstCompare() {
		return new Comparator<File>()
		    {
		      public int compare(final File o1, final File o2) {
		    	  boolean isDir1 = o1.isDirectory();
		    	  boolean isDir2 = o2.isDirectory();
		    	  if ( isDir1 == isDir2 ) {
		    		  return ( o1.getName().compareToIgnoreCase(o2.getName()) );
		    	  } else if ( isDir1 ) {
		    		  return -1;
		    	  } else if ( isDir2 ) {
		    		  return 1;
		    	  } else {
		    		  return ( o1.getName().compareToIgnoreCase(o2.getName()) );		    		  
		    	  }
		      }
		    };
	}
	
	public static String humanReadableSize(Long aSize) {
		double exponent = Math.log10(aSize.doubleValue());
		if ( exponent >= 9.0 ) {
			return String.format("%.1f GB", (aSize.doubleValue() / Math.pow(10, 9)));
		} else if ( exponent >= 6.0 ) {
			return String.format("%.1f MB", (aSize.doubleValue() / Math.pow(10, 6)));			
		} else if ( exponent >= 3.0 ) {
			return String.format("%.1f kB", (aSize.doubleValue() / Math.pow(10, 3)));			
		} else {
			return String.format("%d B", aSize.longValue());						
		}
	}	
}
