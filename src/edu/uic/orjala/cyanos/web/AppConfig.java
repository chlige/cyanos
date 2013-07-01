package edu.uic.orjala.cyanos.web;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class AppConfig implements CyanosConfig {

		private String configFile;
		private Map<String, Map<String,String>> fileMap;
		private Map<String, String> dsMap;
		private Map<String, String> extraParams;
		private Map<String, String> queueType;
		private Map<String, List<String>> queueValues;
		private Map<String, Map<String,String>> urlTemplateMap;
		private Map<String, List<String>> modules;
		private Map<String, String> mapserverLayers;
		private boolean updated = false;
		private boolean exists = false;
		
		private static final String CONFIG_ROOT = "config";
		private static final String APP_VERSION = "1.0";
		private static final String VERSION_TAG = "version";
		
		private static final String FILE_PATH_ELEMENT = "filePath";
		private static final String FILE_PATH_CLASS_ATTR = "class";
		private static final String FILE_PATH_TYPE_ATTR = "type";
		private static final String FILE_PATH_PATH_ATTR = "path";
		
		private static final String DATASOURCE_ELEMENT ="datasource";
		private static final String DATASOURCE_NAME = "name";
		private static final String DATASOURCE_TYPE = "type";
		
		private final static String QUEUE_ELEMENT = "queue";
		private final static String QUEUE_NAME = "type";
		private final static String QUEUE_TYPE = "source";
		private final static String QUEUE_VALUE_ELEMENT = "name";
		
		private static final String PARAM_ELEMENT = "parameter";
		private static final String PARAM_NAME = "name";
		private static final String PARAM_VALUE = "value";
		private static final String PARAM_GOOGLE_MAP_TYPE = "google_map_key";
		private final static String PARAM_EMAIL_ROBOT_ADDRESS = "email_robot";

		private static final String MAPSERVER_ELEMENT = "mapserver";
		private static final String MAPSERVER_LAYER_NAME = "name";
		private static final String MAPSERVER_LAYER_URL = "URL";
		
		private static final String URL_TEMPLATE_ELEMENT = "urlTemplate";
		private static final String URL_TEMPLATE_CLASS = "class";
		private static final String URL_TEMPLATE_LABEL = "label";
		private static final String URL_TEMPLATE_CONTENT = "url";
		
		private static final String MODULE_ELEMENT = "module";
		private static final String MODULE_CLASS = "class";
		private static final String MODULE_TYPE = "type";
		
		public AppConfig(String newFile) throws ParserConfigurationException, SAXException, IOException {
			this.fileMap = new HashMap<String, Map<String,String>>();
			this.dsMap = new HashMap<String,String>();
			this.extraParams = new HashMap<String,String>();
			this.urlTemplateMap = new HashMap<String,Map<String,String>>();
			this.queueType = new HashMap<String, String>();
			this.queueValues = new HashMap<String, List<String>>();
			this.modules = new HashMap<String, List<String>>();
			this.mapserverLayers = new HashMap<String, String>();
			this.configFile = newFile;	
			this.loadConfig();
		}
		
		/* (non-Javadoc)
		 * @see edu.uic.orjala.cyanos.web.CyanosConfig#loadConfig()
		 */
		public void loadConfig() throws ParserConfigurationException, SAXException, IOException {
			if ( this.configFile != null ) {
				fileMap = new HashMap<String, Map<String,String>>();
				File xmlFile = new File(this.configFile);
				if ( xmlFile.exists() ) {
					this.exists = true;
					DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
					DocumentBuilder db = dbf.newDocumentBuilder();
					Document dom = db.parse(xmlFile);

					Element docElem = dom.getDocumentElement();
					NodeList nl = docElem.getElementsByTagName(FILE_PATH_ELEMENT);

					if (nl != null && nl.getLength() > 0 ) {
						for (int i = 0; i < nl.getLength(); i++) {
							Element pathEl = (Element)nl.item(i);
							String aClass = pathEl.getAttribute(FILE_PATH_CLASS_ATTR);
							String aType = pathEl.getAttribute(FILE_PATH_TYPE_ATTR);
							String aPath = pathEl.getAttribute(FILE_PATH_PATH_ATTR);					
							this.setFilePath(aClass, aType, aPath);
						}
					}
					
					this.dsMap = new HashMap<String,String>();
					nl = docElem.getElementsByTagName(DATASOURCE_ELEMENT);
					if (nl != null && nl.getLength() > 0 ) {
						for (int i = 0; i < nl.getLength(); i++) {
							Element pathEl = (Element)nl.item(i);
							String aType = pathEl.getAttribute(DATASOURCE_TYPE);
							String aName = pathEl.getAttribute(DATASOURCE_NAME);					
							this.dsMap.put(aType, aName);
						}
					}					
					
					this.extraParams = new HashMap<String,String>();
					nl = docElem.getElementsByTagName(PARAM_ELEMENT);
					if (nl != null && nl.getLength() > 0 ) {
						for (int i = 0; i < nl.getLength(); i++) {
							Element pathEl = (Element)nl.item(i);
							String aValue = pathEl.getAttribute(PARAM_VALUE);
							String aName = pathEl.getAttribute(PARAM_NAME);					
							this.extraParams.put(aName, aValue);
						}
					}					

					this.queueType = new HashMap<String,String>();
					this.queueValues = new HashMap<String,List<String>>();
					nl = docElem.getElementsByTagName(QUEUE_ELEMENT);
					if (nl != null && nl.getLength() > 0 ) {
						for (int i = 0; i < nl.getLength(); i++) {
							Element pathEl = (Element)nl.item(i);
							String aType = pathEl.getAttribute(QUEUE_TYPE);
							String aName = pathEl.getAttribute(QUEUE_NAME);
							this.queueType.put(aName, aType);
							if ( aType.equals("static") ) {
								NodeList valList = pathEl.getElementsByTagName(QUEUE_VALUE_ELEMENT);
								if ( valList != null && valList.getLength() > 0 ) {
									List<String> aList = new ArrayList<String>();
									for ( int x = 0; x < valList.getLength(); x++ ) {
										Element valEl = (Element)valList.item(x);
										String text = valEl.getTextContent();
										if ( text != null )
											aList.add(text);
									}
									this.queueValues.put(aName, aList);
								}
								
							}
						}
					}	
					
					this.mapserverLayers = new HashMap<String,String>();
					nl = docElem.getElementsByTagName(MAPSERVER_ELEMENT);
					if (nl != null && nl.getLength() > 0 ) {
						for (int i = 0; i < nl.getLength(); i++) {
							Element pathEl = (Element)nl.item(i);
							String aName = pathEl.getAttribute(MAPSERVER_LAYER_NAME);
							String anURL = pathEl.getAttribute(MAPSERVER_LAYER_URL);
							this.mapserverLayers.put(aName, anURL);
						}
					}

					this.urlTemplateMap = new HashMap<String,Map<String,String>>();
					nl = docElem.getElementsByTagName(URL_TEMPLATE_ELEMENT);
					if ( nl != null && nl.getLength() > 0 ) {
						for (int i = 0; i < nl.getLength(); i++) {
							Element pathEl = (Element)nl.item(i);
							String aClass = pathEl.getAttribute(URL_TEMPLATE_CLASS);
							String aLabel = pathEl.getAttribute(URL_TEMPLATE_LABEL);
							String aURL = pathEl.getAttribute(URL_TEMPLATE_CONTENT);
							Map<String,String> aClassTemplate;
							if ( this.urlTemplateMap.containsKey(aClass)) {
								aClassTemplate = this.urlTemplateMap.get(aClass);
							} else {
								aClassTemplate = new HashMap<String,String>();
								this.urlTemplateMap.put(aClass, aClassTemplate);
							}							
							aClassTemplate.put(aLabel,aURL);
						}
					}	
					
					this.modules = new HashMap<String,List<String>>();
					nl = docElem.getElementsByTagName(MODULE_ELEMENT);
					if ( nl != null && nl.getLength() > 0 ) {
						for (int i = 0; i < nl.getLength(); i++) {
							Element modEl = (Element)nl.item(i);
							String aClass = modEl.getAttribute(MODULE_CLASS);
							String aType = modEl.getAttribute(MODULE_TYPE);
							List<String> classes;
							if ( this.modules.containsKey(aType) ) {
								classes = this.modules.get(aType);
							} else {
								classes = new ArrayList<String>();
								this.modules.put(aType, classes);
							}
							classes.add(aClass);
						}
					}
					
					this.updated = false;
				} 
			}
		}
		
		/* (non-Javadoc)
		 * @see edu.uic.orjala.cyanos.web.CyanosConfig#configExists()
		 */
		public boolean configExists() {
			return this.exists;
		}
		
		/* (non-Javadoc)
		 * @see edu.uic.orjala.cyanos.web.CyanosConfig#setDataSource(java.lang.String)
		 */
		public void setDataSource(String aName) {
			this.dsMap.put("data", aName);
			this.updated = true;
		}
		
		/* (non-Javadoc)
		 * @see edu.uic.orjala.cyanos.web.CyanosConfig#setUserDB(java.lang.String)
		 */
		public void setUserDB(String aName) {
			this.dsMap.put("user", aName);
			this.updated = true;
		}
		
		/* (non-Javadoc)
		 * @see edu.uic.orjala.cyanos.web.CyanosConfig#getDataSource()
		 */
		public String getDataSource() {
			return this.dsMap.get("data");
		}
		
		/* (non-Javadoc)
		 * @see edu.uic.orjala.cyanos.web.CyanosConfig#getDataSourceObject()
		 */
		public DataSource getDataSourceObject() throws NamingException {
			if ( this.dsMap.containsKey("data")) {
				InitialContext initCtx = new InitialContext();
				DataSource aDS = (DataSource)initCtx.lookup("java:comp/env/jdbc/" + this.dsMap.get("data"));
				return aDS;
			}
			return null;
		}
		
		/* (non-Javadoc)
		 * @see edu.uic.orjala.cyanos.web.CyanosConfig#getUserDB()
		 */
		public String getUserDB() {
			return this.dsMap.get("user");
		}
		
		protected String getParameter(String paramKey) {
			return this.extraParams.get(paramKey);
		}
		
		/* (non-Javadoc)
		 * @see edu.uic.orjala.cyanos.web.CyanosConfig#getEmailAddress()
		 */
		public String getEmailAddress() {
			return this.getParameter(PARAM_EMAIL_ROBOT_ADDRESS);
		}
		
		/* (non-Javadoc)
		 * @see edu.uic.orjala.cyanos.web.CyanosConfig#setEmailAddress(java.lang.String)
		 */
		public void setEmailAddress(String newAddress) {
			this.setParameter(PARAM_EMAIL_ROBOT_ADDRESS, newAddress);
		}
		
		/* (non-Javadoc)
		 * @see edu.uic.orjala.cyanos.web.CyanosConfig#getGoogleMapKey()
		 */
		public String getGoogleMapKey() {
			return this.getParameter(PARAM_GOOGLE_MAP_TYPE);
		}
		
		protected void setParameter(String paramKey, String aValue) {
			this.extraParams.put(paramKey, aValue);
			this.updated = true;
		}
		
		/* (non-Javadoc)
		 * @see edu.uic.orjala.cyanos.web.CyanosConfig#setGoogleMapKey(java.lang.String)
		 */
		public void setGoogleMapKey(String aValue) { 
			this.setParameter(PARAM_GOOGLE_MAP_TYPE, aValue);
		}
		
		/* (non-Javadoc)
		 * @see edu.uic.orjala.cyanos.web.CyanosConfig#clearFilePaths()
		 */
		public void clearFilePaths() {
			this.fileMap.clear();
		}
		
		/* (non-Javadoc)
		 * @see edu.uic.orjala.cyanos.web.CyanosConfig#setFilePath(java.lang.String, java.lang.String, java.lang.String)
		 */
		public void setFilePath(String aClass, String aType, String aPath) {
			if ( ! fileMap.containsKey(aClass) ) {
				Map <String,String> classMap = new HashMap<String,String>();
				this.fileMap.put(aClass, classMap);
			}
			this.fileMap.get(aClass).put(aType, aPath);
			this.updated = true;
		}
		
		/* (non-Javadoc)
		 * @see edu.uic.orjala.cyanos.web.CyanosConfig#getFilePath(java.lang.String, java.lang.String)
		 */
		public String getFilePath(String aClass, String aType) {
			Map<String,String> classMap;
			if ( ! fileMap.containsKey(aClass) ) {
				if ( fileMap.containsKey("*") ) aClass = "*";
				else return null;
			} 
			
			boolean top = aClass.equals("*");
			
			classMap = this.fileMap.get(aClass);
			if ( classMap.containsKey(aType) ) {
				return classMap.get(aType);
			} else if ( classMap.containsKey("*") ) {
				return classMap.get("*");
			} else if ( ! top ) {
				return this.getFilePath("*", aType);
			} else {
				return null;
			}			
		}
		
		/* (non-Javadoc)
		 * @see edu.uic.orjala.cyanos.web.CyanosConfig#getFilePathMap()
		 */
		public Map<String, Map<String,String>> getFilePathMap() {
			Map<String, Map<String,String>> returnMap = new HashMap<String, Map<String, String>>();
			Iterator keyIter = fileMap.keySet().iterator();
			while ( keyIter.hasNext() ) {
				String aKey = (String) keyIter.next();
				Map<String,String> classMap = this.fileMap.get(aKey);
				Iterator typeIter = classMap.keySet().iterator();
				Map<String,String> rcMap = new HashMap<String,String>();
				while ( typeIter.hasNext() ) {
					String aType = (String)typeIter.next();
					rcMap.put(aType, classMap.get(aType));
				}
				returnMap.put(aKey, rcMap);
			}			
			return returnMap;
		}
		
		/* (non-Javadoc)
		 * @see edu.uic.orjala.cyanos.web.CyanosConfig#isUnsaved()
		 */
		public boolean isUnsaved() {
			return this.updated;
		}
		
		/* (non-Javadoc)
		 * @see edu.uic.orjala.cyanos.web.CyanosConfig#isSaved()
		 */
		public boolean isSaved() {
			return ! this.updated;
		}
		
		/* (non-Javadoc)
		 * @see edu.uic.orjala.cyanos.web.CyanosConfig#writeConfig()
		 */
		public void writeConfig() throws ParserConfigurationException, TransformerFactoryConfigurationError, TransformerException, IOException {
			if ( this.configFile != null ) {
				File xmlFile = new File(this.configFile);
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				Document dom = db.newDocument();
				
				Element root = dom.createElement(CONFIG_ROOT);
				dom.appendChild(root);
				root.setAttribute(VERSION_TAG, APP_VERSION);

				Iterator<String> keyIter = fileMap.keySet().iterator();
				while ( keyIter.hasNext() ) {
					String aKey = keyIter.next();
					Map<String,String> classMap = this.fileMap.get(aKey);
					Iterator typeIter = classMap.keySet().iterator();
					while ( typeIter.hasNext() ) {
						String aType = (String)typeIter.next();
						Element pathEl = dom.createElement(FILE_PATH_ELEMENT);
						pathEl.setAttribute(FILE_PATH_CLASS_ATTR, aKey);
						pathEl.setAttribute(FILE_PATH_TYPE_ATTR, aType);
						pathEl.setAttribute(FILE_PATH_PATH_ATTR, classMap.get(aType));
						root.appendChild(pathEl);
					}
				}

				keyIter = dsMap.keySet().iterator();
				while ( keyIter.hasNext() ) {
					String aKey = keyIter.next();
					String aName = this.dsMap.get(aKey);
					Element pathEl = dom.createElement(DATASOURCE_ELEMENT);
					pathEl.setAttribute(DATASOURCE_TYPE, aKey);
					pathEl.setAttribute(DATASOURCE_NAME, aName);
					root.appendChild(pathEl);
				}

				keyIter = extraParams.keySet().iterator();
				while ( keyIter.hasNext() ) {
					String aKey = keyIter.next();
					String aValue = this.extraParams.get(aKey);
					Element pathEl = dom.createElement(PARAM_ELEMENT);
					pathEl.setAttribute(PARAM_NAME, aKey);
					pathEl.setAttribute(PARAM_VALUE, aValue);
					root.appendChild(pathEl);
				}


				keyIter = this.queueType.keySet().iterator();
				while ( keyIter.hasNext() ) {
					String aKey = keyIter.next();
					String aType = this.queueType.get(aKey);
					Element pathEl = dom.createElement(QUEUE_ELEMENT);
					pathEl.setAttribute(QUEUE_NAME, aKey);
					pathEl.setAttribute(QUEUE_TYPE, aType);
					if ( aType.equals("static") ) {
						List<String> valList = this.queueValues.get(aKey);
						ListIterator<String> anIter = valList.listIterator();
						while ( anIter.hasNext() ) {
							String aValue = anIter.next();
							Element valEl = dom.createElement(QUEUE_VALUE_ELEMENT);
							valEl.setTextContent(aValue);
							pathEl.appendChild(valEl);
						}
					}
					root.appendChild(pathEl);
				}
				
				keyIter = this.urlTemplateMap.keySet().iterator();
				while ( keyIter.hasNext() ) {
					String aClass = keyIter.next();
					Map<String,String> aClassTemplate = this.urlTemplateMap.get(aClass);
					Iterator<String> labelIter = aClassTemplate.keySet().iterator();
					while ( labelIter.hasNext() ) {
						String aLabel = labelIter.next();
						Element pathEl = dom.createElement(URL_TEMPLATE_ELEMENT);
						pathEl.setAttribute(URL_TEMPLATE_CLASS, aClass);
						pathEl.setAttribute(URL_TEMPLATE_LABEL, aLabel);
						pathEl.setAttribute(URL_TEMPLATE_CONTENT, aClassTemplate.get(aLabel));
						root.appendChild(pathEl);
					}
				}

				keyIter = this.modules.keySet().iterator();
				while ( keyIter.hasNext() ) {
					String aType = keyIter.next();
					List<String> aClassTemplate = this.modules.get(aType);
					Iterator<String> labelIter = aClassTemplate.iterator();
					while ( labelIter.hasNext() ) {
						String aLabel = labelIter.next();
						Element pathEl = dom.createElement(MODULE_ELEMENT);
						pathEl.setAttribute(MODULE_CLASS, aLabel);
						pathEl.setAttribute(MODULE_TYPE, aType);
						root.appendChild(pathEl);
					}
				}
				
				keyIter = this.mapserverLayers.keySet().iterator();
				while ( keyIter.hasNext() ) {
					String aName = keyIter.next();
					String aURL = this.mapserverLayers.get(aName);
					Element pathEl = dom.createElement(MAPSERVER_ELEMENT);
					pathEl.setAttribute(MAPSERVER_LAYER_NAME, aName);
					pathEl.setAttribute(MAPSERVER_LAYER_URL, aURL);
					root.appendChild(pathEl);
				}
				
				System.setProperty("javax.xml.transform.TransformerFactory", "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl");
				
				if ( ! xmlFile.exists() ) {
					xmlFile.createNewFile();
				}
				
				Source aSrc = new DOMSource(dom);
				Result aResult = new StreamResult(xmlFile);
				Transformer xmlTrn = TransformerFactory.newInstance().newTransformer();
				xmlTrn.setOutputProperty("indent", "yes");
				xmlTrn.transform(aSrc, aResult);
				this.updated = false;
			}
		}
		
		/* (non-Javadoc)
		 * @see edu.uic.orjala.cyanos.web.CyanosConfig#isQueueStatic(java.lang.String)
		 */
		public boolean isQueueStatic(String queueType) {
			if ( this.queueType.containsKey(queueType) )
				return this.queueType.get(queueType).equals("static");
			return false;
		}
		
		/* (non-Javadoc)
		 * @see edu.uic.orjala.cyanos.web.CyanosConfig#isQueueDynamic(java.lang.String)
		 */
		public boolean isQueueDynamic(String queueType) {
			if ( this.queueType.containsKey(queueType) )
				return ! this.queueType.get(queueType).equals("jdbc");
			return false;
		}
		
		/* (non-Javadoc)
		 * @see edu.uic.orjala.cyanos.web.CyanosConfig#isQueueSingle(java.lang.String)
		 */
		public boolean isQueueSingle(String queueType) {
			if ( this.queueType.containsKey(queueType) )
				return this.queueType.get(queueType).equals("none");
			return false;
		}

		/* (non-Javadoc)
		 * @see edu.uic.orjala.cyanos.web.CyanosConfig#queueSource(java.lang.String)
		 */
		public String queueSource(String queueType) {
			if ( this.queueType.containsKey(queueType) )
				return this.queueType.get(queueType);
			return null;
		}
		
		/* (non-Javadoc)
		 * @see edu.uic.orjala.cyanos.web.CyanosConfig#setQueueSource(java.lang.String, java.lang.String)
		 */
		public void setQueueSource(String queueType, String newSource) {
			this.queueType.put(queueType, newSource);
		}
		
		/* (non-Javadoc)
		 * @see edu.uic.orjala.cyanos.web.CyanosConfig#queuesForType(java.lang.String)
		 */
		public List<String> queuesForType(String queueType) {
			if ( this.queueValues.containsKey(queueType))
				return this.queueValues.get(queueType);
			return null;
		}
		
		/* (non-Javadoc)
		 * @see edu.uic.orjala.cyanos.web.CyanosConfig#queueTypes()
		 */
		public Set<String> queueTypes() {
			return this.queueType.keySet();
		}
		
		/* (non-Javadoc)
		 * @see edu.uic.orjala.cyanos.web.CyanosConfig#addQueue(java.lang.String, java.lang.String)
		 */
		public void addQueue(String queueType, String queueName) {
			List<String> nameList;
			if ( ! this.queueType.containsKey(queueType) )
				nameList = new ArrayList<String>();
			else 
				nameList = this.queueValues.get(queueType);
			nameList.add(queueName);
			this.queueValues.put(queueType, nameList);
			this.queueType.put(queueType, "static");
			this.updated = true;
		}
		
		/* (non-Javadoc)
		 * @see edu.uic.orjala.cyanos.web.CyanosConfig#removeQueue(java.lang.String, java.lang.String)
		 */
		public void removeQueue(String queueType, String queueName) {
			if ( this.queueValues.containsKey(queueType) ) {
				List<String> nameList = this.queueValues.get(queueType);
				nameList.remove(queueName);
				this.queueValues.put(queueType, nameList);
				this.updated = true;
			}
		}
		
		/* (non-Javadoc)
		 * @see edu.uic.orjala.cyanos.web.CyanosConfig#setQueues(java.lang.String, java.lang.String[])
		 */
		public void setQueues(String queueType, String[] queues) {
			if ( queues.length > 0 ) {
				List<String> nameList = new ArrayList<String>();
				for ( int i = 0; i < queues.length; i++ ) {
					nameList.add(queues[i]);
				}
				this.queueValues.put(queueType, nameList);
				this.queueType.put(queueType, "static");
				this.updated = true;
			}
		}
		
		/* (non-Javadoc)
		 * @see edu.uic.orjala.cyanos.web.CyanosConfig#tableList()
		 */
		public String[] tableList() {
			return CyanosConfig.TABLES;
		}
		
		
		/* (non-Javadoc)
		 * @see edu.uic.orjala.cyanos.web.CyanosConfig#getURLTemplate(java.lang.String, java.lang.String)
		 */
		public String getURLTemplate(String aClass, String aLabel) {
			if ( this.urlTemplateMap.containsKey(aClass)) {
				Map<String,String> aTemplate = this.urlTemplateMap.get(aClass);
				return aTemplate.get(aLabel);
			}
			return null;
		}
		
		/* (non-Javadoc)
		 * @see edu.uic.orjala.cyanos.web.CyanosConfig#getURLTemplates(java.lang.String)
		 */
		public Map<String,String> getURLTemplates(String aClass) {
			if ( this.urlTemplateMap.containsKey(aClass) ) {
				return this.urlTemplateMap.get(aClass);
			}
			return null;
		}
		
		/* (non-Javadoc)
		 * @see edu.uic.orjala.cyanos.web.CyanosConfig#setURLTemplate(java.lang.String, java.lang.String, java.lang.String)
		 */
		public void setURLTemplate(String aClass, String aLabel, String aTemplate) {
			Map<String,String> aClassTemplate;
			if ( this.urlTemplateMap.containsKey(aClass)) {
				aClassTemplate = this.urlTemplateMap.get(aClass);
			} else {
				aClassTemplate = new HashMap<String,String>();
				this.urlTemplateMap.put(aClass, aClassTemplate);
			}							
			aClassTemplate.put(aLabel,aTemplate);
			this.updated = true;
		}

		/* (non-Javadoc)
		 * @see edu.uic.orjala.cyanos.web.CyanosConfig#canMap()
		 */
		public boolean canMap() {
			return this.extraParams.containsKey(PARAM_GOOGLE_MAP_TYPE);
		}
		
		/* (non-Javadoc)
		 * @see edu.uic.orjala.cyanos.web.CyanosConfig#classesForModuleType(java.lang.String)
		 */
		public List<String> classesForModuleType(String aType) {
			return this.modules.get(aType);
		}
		
		/* (non-Javadoc)
		 * @see edu.uic.orjala.cyanos.web.CyanosConfig#addClassForModuleType(java.lang.String, java.lang.String)
		 */
		public void addClassForModuleType(String aType, String aClass) {
			List<String> classes;
			if ( this.modules.containsKey(aType) ) {
				classes = this.modules.get(aType);
			} else {
				classes = new ArrayList<String>();
				this.modules.put(aType, classes);
			}
			if ( ! classes.contains(aClass) ) {
				classes.add(aClass);
				this.updated = true;
			}
		}
		
		/* (non-Javadoc)
		 * @see edu.uic.orjala.cyanos.web.CyanosConfig#addClassForDereplicationModule(java.lang.String)
		 */
		public void addClassForDereplicationModule(String aClass) {
			this.addClassForModuleType(DEREPLICATION_MODULE, aClass);
		}
		
		/* (non-Javadoc)
		 * @see edu.uic.orjala.cyanos.web.CyanosConfig#classesForDereplicationModule()
		 */
		public List<String> classesForDereplicationModule() {
			return this.classesForModuleType(DEREPLICATION_MODULE);
		}

		/* (non-Javadoc)
		 * @see edu.uic.orjala.cyanos.web.CyanosConfig#addClassForUploadModule(java.lang.String)
		 */
		public void addClassForUploadModule(String aClass) {
			this.addClassForModuleType(UPLOAD_MODULE, aClass);
		}
		
		/* (non-Javadoc)
		 * @see edu.uic.orjala.cyanos.web.CyanosConfig#classesForUploadModule()
		 */
		public List<String> classesForUploadModule() {
			return this.classesForModuleType(UPLOAD_MODULE);
		}
		
		/* (non-Javadoc)
		 * @see edu.uic.orjala.cyanos.web.CyanosConfig#clearModules()
		 */
		public void clearModules() {
			this.modules.clear();
			this.updated = true;
		}

		public void addMapServerLayer(String aName, String url) {
			this.mapserverLayers.put(aName, url);
		}

		public void deleteMapServerLayer(String aName) {
			this.mapserverLayers.remove(aName);
		}

		public Map<String, String> getMapServerLayers() {
			return this.mapserverLayers;
		}

}
