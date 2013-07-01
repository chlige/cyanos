package edu.uic.orjala.cyanos.web;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
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
import edu.uic.orjala.cyanos.Role;
import edu.uic.orjala.cyanos.Sample;
import edu.uic.orjala.cyanos.Separation;
import edu.uic.orjala.cyanos.SingleFile;
import edu.uic.orjala.cyanos.Strain;
import edu.uic.orjala.cyanos.User;
import edu.uic.orjala.cyanos.sql.SQLAssay;
import edu.uic.orjala.cyanos.sql.SQLCompound;
import edu.uic.orjala.cyanos.sql.SQLExternalFile;
import edu.uic.orjala.cyanos.sql.SQLSample;
import edu.uic.orjala.cyanos.sql.SQLSeparation;
import edu.uic.orjala.cyanos.sql.SQLStrain;
import edu.uic.orjala.cyanos.web.forms.DataForm;
import edu.uic.orjala.cyanos.web.html.Paragraph;
import edu.uic.orjala.cyanos.web.html.StyledText;


public class DataFileServlet extends ServletObject {
	
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


	@Override
	public void doPut ( HttpServletRequest req, HttpServletResponse res ) throws ServletException, IOException {
		CyanosWrapper aWrap = new ServletWrapper(this, this.dbh, req, res);		
		if ( this.newInstall ) {
			return;
		}
		String module = req.getPathInfo();
		if ( module != null && module.startsWith("/manager") ) {
			String[] details = module.split("/", 5);
			
			String requiredRole = this.getRole(details[2]);
			try {
				if ( aWrap.getUser().isAllowed(requiredRole, User.NULL_PROJECT, Role.WRITE) ) { 

					CyanosConfig myConf = aWrap.getAppConfig();
					String path = myConf.getFilePath(details[2], details[3]);

					SingleFile aFile = new SingleFile(path, details[4]);

					File outputFile;
					outputFile = aFile.getFileObject();
					if ( (! outputFile.exists()) && outputFile.createNewFile() ) {
						BufferedInputStream fileData = new BufferedInputStream(req.getInputStream());
						FileOutputStream fileOut = new FileOutputStream(outputFile);
						int d = fileData.read();
						int count = 1;
						while ( d != -1 ) {
							fileOut.write(d);
							d = fileData.read();
							count++;
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
			}
		}
	}
	
	public void display(CyanosWrapper aWrap) throws Exception {
		
		String module = aWrap.getRequest().getPathInfo();	
		if ( module != null && ( module.startsWith("/get") || module.startsWith("/preview") ) ) {
			try {
				CyanosConfig myConf = aWrap.getAppConfig();
				
				String[] details = module.split("/", 5);
				String path = myConf.getFilePath(details[2], details[3]);
				String requiredRole = this.getRole(details[2]);
				if ( ! aWrap.getUser().isAllowed(requiredRole, User.NULL_PROJECT, Role.READ) ) { 
					aWrap.getResponse().setStatus(HttpServletResponse.SC_FORBIDDEN);
					return;
				}
				
				ExternalFile aFile = SQLExternalFile.load(aWrap.getSQLDataSource(), path, details[4]);
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
					ServletOutputStream binaryOut = aWrap.getOutputStream();
					aWrap.setContentType(contentType);
					
					if ( contentType.startsWith("image") && (module.startsWith("/preview") || aWrap.hasFormValue("height") || aWrap.hasFormValue("width")) ) {
						BufferedImage source = ImageIO.read(aFile.getFileObject());
						int width = -1, height = -1;
						double widthScale = 0, heightScale = 0;
						
						if ( module.startsWith("/preview") ) {
							height = 200;
							heightScale = (double) height / source.getHeight();							
						} else {
							if ( aWrap.hasFormValue("height") ) {
								height = Integer.parseInt(aWrap.getFormValue("height"));
								heightScale = (double) height / source.getHeight();
							}
							if ( aWrap.hasFormValue("width") ) {
								width = Integer.parseInt(aWrap.getFormValue("width"));
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
							aWrap.getResponse().setContentLength(byteOut.size());
							binaryOut.write(byteOut.toByteArray());
						} else {
							InputStream fileIn = aFile.getInputStream();
							Long thisSize = new Long(aFile.getFileObject().length());
							aWrap.getResponse().setContentLength(thisSize.intValue());
							int d = fileIn.read();
							while ( d != -1 ) {
								binaryOut.write(d);
								d = fileIn.read();
							}
							while ( d != -1 ) {
								binaryOut.write(d);
								d = fileIn.read();
							}
						}
					} else {
						InputStream fileIn = aFile.getInputStream();
						Long thisSize = new Long(aFile.getFileObject().length());
						aWrap.getResponse().setContentLength(thisSize.intValue());
						int d = fileIn.read();
						while ( d != -1 ) {
							binaryOut.write(d);
							d = fileIn.read();
						}
						while ( d != -1 ) {
							binaryOut.write(d);
							d = fileIn.read();
						}
					}
					binaryOut.flush();
					binaryOut.close();
					aWrap.getResponse().flushBuffer();
				} else {
					aWrap.setContentType("text/plain");
					PrintWriter out = aWrap.getWriter();
					out.println("File Not Found.");
				}
			} catch ( IOException ex ) {
				aWrap.setContentType("text/plain");
				aWrap.getResponse().resetBuffer();
				PrintWriter out = aWrap.getWriter();
				out.println("IO Error:");
				out.println(ex.getMessage());
				ex.printStackTrace();
			} catch (MagicParseException ex) {
				aWrap.setContentType("text/plain");
				aWrap.getResponse().resetBuffer();
				PrintWriter out = aWrap.getWriter();
				out.println(ex.getMessage());
				ex.printStackTrace();
			} catch (MagicMatchNotFoundException ex) {
				aWrap.setContentType("text/plain");
				aWrap.getResponse().resetBuffer();
				PrintWriter out = aWrap.getWriter();
				out.println(ex.getMessage());
				ex.printStackTrace();
			} catch (MagicException ex) {
				aWrap.setContentType("text/plain");
				aWrap.getResponse().resetBuffer();
				PrintWriter out = aWrap.getWriter();
				out.println(ex.getMessage());
				ex.printStackTrace();
			}

/*			try {
				this.closeSQL();
			} catch ( SQLException ex ) {
				this.log("FAILURE TO CLOSE SQL CONNECTION.");
				ex.printStackTrace();
			}
*/
			return;		
		} else if ( module != null && module.startsWith("/manager") ) {
			String[] details = module.split("/", 5);
			String requiredRole = this.getRole(details[2]);			
			CyanosConfig myConf = aWrap.getAppConfig();
			aWrap.setContentType("text/plain");
			String path = myConf.getFilePath(details[2], details[3]);

			if ( aWrap.hasFormValue("getdirs") ) {
				if ( aWrap.getUser().isAllowed(requiredRole, User.NULL_PROJECT, Role.READ) ) {
					File aDir = new File(path, aWrap.getFormValue("path"));
					File[] kids = aDir.listFiles();
					Arrays.sort(kids, DataForm.directoryFirstCompare());

					PrintWriter writer = aWrap.getWriter();

					for ( int i = 0; i < kids.length; i++ ) {
						if ( kids[i].isDirectory() ) {
							writer.println(kids[i].getName());
						}
					}
					writer.close();
				} else 
					aWrap.getResponse().setStatus(HttpServletResponse.SC_FORBIDDEN);
			} else if ( aWrap.hasFormValue("getfiles") ) {
				if ( aWrap.getUser().isAllowed(requiredRole, User.NULL_PROJECT, Role.READ) ) {
					File aDir = new File(path, aWrap.getFormValue("path"));
					File[] kids = aDir.listFiles();
					Arrays.sort(kids, DataForm.directoryFirstCompare());
					String previewURL = String.format("%s/file/preview/%s/%s", aWrap.getContextPath(), details[2], details[3]);

					String filePath = aWrap.getFormValue("path");
					filePath = filePath.replaceAll("/+", "/");
					if ( filePath.startsWith("/") ) {
						filePath = filePath.substring(1);
					}

					DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
					DocumentBuilder db = dbf.newDocumentBuilder();
					Document dom = db.newDocument();

					DataFileObject myObject = this.getObject(aWrap, aWrap.getFormValue("objectClass"), aWrap.getFormValue("objectID"));
					ExternalFile myFiles;
					if ( aWrap.hasFormValue("dataType") ) {
						myFiles = myObject.getDataFilesForType(aWrap.getFormValue("dataType"));					
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
					for ( int i = 0; i < kids.length; i++ ) {
						if ( ! kids[i].isDirectory() && (! kids[i].getName().startsWith(".")) ) {
							Element pathEl = dom.createElement(FILE_ELEMENT);
							pathEl.setAttribute(FILENAME_ATTR, kids[i].getName());
							pathEl.setAttribute(SIZE_ATTR, DataForm.humanReadableSize(kids[i].length()));
							String mimeType = "";
							String aPath = String.format("%s%s", filePath, kids[i].getName());
							if ( fileDesc.containsKey(aPath) ) {
								pathEl.setAttribute(DESC_ATTR, fileDesc.get(aPath));
								mimeType = fileMime.get(aPath);
								pathEl.setAttribute(TYPE_ATTR, fileType.get(aPath));
							} else {
								try {
									MagicMatch aMatch = Magic.getMagicMatch(kids[i], true);
									mimeType = aMatch.getMimeType();		
								} catch (MagicMatchNotFoundException e) {
									this.log(String.format("CANNOT determine MIME type for file %s", kids[i].getAbsolutePath()));
								}
							}
							pathEl.setAttribute(ICON_ATTR, DataForm.getIconPathForMIMEType(aWrap, mimeType));
							if ( mimeType.startsWith("image") )
								pathEl.setAttribute(URL_ATTR, String.format("%s/%s", previewURL, aPath));
							root.appendChild(pathEl);
						}
					}

					System.setProperty("javax.xml.transform.TransformerFactory", "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl");
					Source aSrc = new DOMSource(dom);
					Result aResult = new StreamResult(aWrap.getOutputStream());
					Transformer xmlTrn = TransformerFactory.newInstance().newTransformer();
					xmlTrn.setOutputProperty("indent", "yes");
					xmlTrn.transform(aSrc, aResult);
					aWrap.setContentType("text/xml");	
				} else 
					aWrap.getResponse().setStatus(HttpServletResponse.SC_FORBIDDEN);
			} else if ( aWrap.hasFormValue("updateFile") ) {
				if ( aWrap.getUser().isAllowed(requiredRole, User.NULL_PROJECT, Role.WRITE) ) {
					String filePath = aWrap.getFormValue("path");
					filePath = filePath.replaceAll("/+", "/");
					if ( filePath.startsWith("/") ) {
						filePath = filePath.substring(1);
					}
					SingleFile aFile = new SingleFile(path, filePath);
					DataFileObject myObject = this.getObject(aWrap, aWrap.getFormValue("objectClass"), aWrap.getFormValue("objectID"));
					if ( aWrap.hasFormValue("delete") ) {
						myObject.unlinkDataFile(aFile);
					} else {
						aFile.setDescription(aWrap.getFormValue("desc"));
						try {
							MagicMatch aMatch = Magic.getMagicMatch(aFile.getFileObject(), true);
							aFile.setMimeType(aMatch.getMimeType());		
						} catch (MagicMatchNotFoundException e) {
							this.log(String.format("CANNOT determine MIME type for file %s", aFile.getFilePath()));
						}
						if ( myObject != null ) {
							myObject.linkDataFile(aFile, aWrap.getFormValue("dataType"));
						} else {
							aWrap.getResponse().setStatus(HttpServletResponse.SC_NOT_FOUND);
							aWrap.print("Object class not valid.");
						}
					}
				} else 
					aWrap.getResponse().setStatus(HttpServletResponse.SC_FORBIDDEN);
			} else if ( aWrap.hasFormValue("getTypes") ) {
				if ( aWrap.getUser().isAllowed(requiredRole, User.NULL_PROJECT, Role.READ) ) {
					String objClass = aWrap.getFormValue("objectClass");
					PrintWriter writer = aWrap.getWriter();
					if ( objClass.equals("sample") ) {
						writer.println(String.format("%s:%s", Sample.LC_DATA_TYPE, "LC Chromatogram"));		
						writer.println(String.format("%s:%s", Sample.NMR_DATA_TYPE, "NMR Spectrum"));		
						writer.println(String.format("%s:%s", Sample.MS_DATA_TYPE, "Mass Spectrum"));
					} else if (objClass.equals("strain") ) {
						writer.println(String.format("%s:%s", Strain.PHOTO_DATA_TYPE, "Photo"));
					} else if (objClass.equals("separation") ) {
						writer.println(String.format("%s:%s", Separation.LC_DATA_TYPE, "LC Chromatogram"));			
					} else if (objClass.equals("compound") ) {
						writer.println(String.format("%s:%s", Compound.NMR_DATA_TYPE, "NMR Spectrum"));			
						writer.println(String.format("%s:%s", Compound.MS_DATA_TYPE, "Mass Spectrum"));			
						writer.println(String.format("%s:%s", Compound.IR_DATA_TYPE, "IR Spectrum"));			
						writer.println(String.format("%s:%s", Compound.UV_DATA_TYPE, "UV Spectrum"));			
					} else if ( objClass.equals("assay") ) {
						writer.println(String.format("%s:%s", Assay.RAW_DATA_TYPE, "Raw Data"));			
						writer.println(String.format("%s:%s", Assay.REPORT_DATA_TYPE, "Report"));			
					}
					writer.close();
				} else 
					aWrap.getResponse().setStatus(HttpServletResponse.SC_FORBIDDEN);
			}

			return;
		}
		
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
	}

	private DataFileObject getObject(CyanosWrapper aWrap, String objClass, String objID) throws DataException {
		if ( objClass.equals("sample") ) {
			return new SQLSample(aWrap.getSQLDataSource(), objID);
		} else if (objClass.equals("strain") ) {
			return new SQLStrain(aWrap.getSQLDataSource(), objID);
		} else if (objClass.equals("separation") ) {
			return new SQLSeparation(aWrap.getSQLDataSource(), objID);
		} else if (objClass.equals("compound") ) {
			return new SQLCompound(aWrap.getSQLDataSource(), objID);
		} else if ( objClass.equals("assay") ) {
			return new SQLAssay(aWrap.getSQLDataSource(), objID);
		}
		return null;
	}

	private String getRole(String objClass) {
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
			CyanosConfig myConf = this.getAppConfig();

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
		
		Iterator writers = ImageIO.getImageWritersByMIMEType(mimeType);
		while ( writers.hasNext() ) {
			ImageWriter w = (ImageWriter) writers.next();
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


}
