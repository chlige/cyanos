package edu.uic.orjala.cyanos.web;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.uic.orjala.cyanos.ConfigException;

public class AppConfigXML extends AppConfig {

		private String configFile;
		private DataSource myDS;
		protected boolean updated = false;

		private static final String CONFIG_ROOT = "config";
		private static final String VERSION_TAG = "version";
		private static final String DATE = "date";
		private static final String UUID_TAG = "hostUUID";
		
		private static final String FILE_PATH_CLASS_ATTR = "class";
		private static final String FILE_PATH_TYPE_ATTR = "type";
		private static final String FILE_PATH_PATH_ATTR = "path";
		
		private static final String DATA_TYPE_ELEMENT = "dataType";
		private static final String DATA_TYPE_DESC = "description";
		
		private static final String DATASOURCE_ELEMENT ="datasource";
		private static final String DATASOURCE_NAME = "name";
		private static final String DATASOURCE_TYPE = "type";
		
		private final static String QUEUE_TYPE = "type";
		private final static String QUEUE_SOURCE = "source";
		private final static String QUEUE_VALUE_ELEMENT = "name";
		
		private static final String PARAM_NAME = "name";
		private static final String PARAM_VALUE = "value";

		private static final String MAPSERVER_LAYER_NAME = "name";
		private static final String MAPSERVER_LAYER_URL = "URL";
		
		private static final String MAP_PARAM_ELEMENT = "map-param";
		private static final String MAP_PARAM_PARAM = "parameter";
		private static final String MAP_PARAM_VALUE = "value";
		
		private static final String URL_TEMPLATE_CLASS = "class";
		private static final String URL_TEMPLATE_LABEL = "label";
		private static final String URL_TEMPLATE_CONTENT = "url";
		
		private static final String MODULE_CLASS = "class";
		private static final String MODULE_TYPE = "type";
		private static final String MODULE_FILE = "file";
		
		public final static String APP_CONFIG_ATTR = "cyanosAppConfig";
		
		public AppConfigXML(String newFile) throws ConfigException {
			super();
			this.configFile = newFile;	
			this.loadConfig();
		}
		
		public AppConfigXML(AppConfig appConfig) {
			super(appConfig);
		}

		/* (non-Javadoc)
		 * @see edu.uic.orjala.cyanos.web.CyanosConfig#loadConfig()
		 */
		public void loadConfig() throws ConfigException {
			if ( this.configFile != null ) {

				this.fileMap.clear();
				this.dataTypes.clear();
				this.extraParams.clear();
				this.queueType.clear();
				this.queueValues.clear();
				this.mapserverLayers.clear();
				this.urlTemplateMap.clear();
				this.modules.clear();
				this.mapParams.clear();

				loadConfig(this, new File(this.configFile));							
				this.updated = false;
			}
		}
		
		/* (non-Javadoc)
		 * @see edu.uic.orjala.cyanos.web.CyanosConfig#writeConfig()
		 */
		public void writeConfig() throws ConfigException {
			if ( this.configFile != null ) {
				File xmlFile = new File(this.configFile);
				try {
					writeConfig(this, new FileOutputStream(xmlFile));
					this.updated = false;

				} catch (IOException e) {
					throw new ConfigException(e);
				}
			}
		}

		/* (non-Javadoc)
		 * @see edu.uic.orjala.cyanos.web.CyanosConfig#configExists()
		 */
		public boolean configExists() {
			File xmlFile = new File(this.configFile);
			return xmlFile.exists();
		}
		
		/* (non-Javadoc)
		 * @see edu.uic.orjala.cyanos.web.CyanosConfig#setDataSource(java.lang.String)
		 */
		@Deprecated
		public void setDataSource(String aName) {
			this.dsMap.put("data", aName);
			this.updated = true;
		}
		
		/* (non-Javadoc)
		 * @see edu.uic.orjala.cyanos.web.CyanosConfig#setUserDB(java.lang.String)
		 */
		@Deprecated
		public void setUserDB(String aName) {
			this.dsMap.put("user", aName);
			this.updated = true;
		}
		
		/* (non-Javadoc)
		 * @see edu.uic.orjala.cyanos.web.CyanosConfig#getDataSource()
		 */
		@Deprecated
		public String getDataSource() {
			return this.dsMap.get("data");
		}
		
		/* (non-Javadoc)
		 * @see edu.uic.orjala.cyanos.web.CyanosConfig#getDataSourceObject()
		 */
		@Deprecated
		public DataSource getDataSourceObject() throws ConfigException {
			if ( this.myDS == null ) {
				try {
					InitialContext initCtx = new InitialContext();
					this.myDS = (DataSource)initCtx.lookup("java:comp/env/jdbc/" + CYANOS_DB_NAME);
				} catch (NamingException e) {
					throw new ConfigException(e);
				}
			}
			return this.myDS;
		}
		
		/* (non-Javadoc)
		 * @see edu.uic.orjala.cyanos.web.CyanosConfig#getUserDB()
		 */
		@Deprecated
		public String getUserDB() {
			return this.dsMap.get("user");
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

		public static void writeConfig(AppConfig config, OutputStream stream) throws ConfigException {
			try {
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				Document dom = db.newDocument();

				Element root = dom.createElement(CONFIG_ROOT);
				dom.appendChild(root);
				root.setAttribute(VERSION_TAG, String.format("%.02f", APP_VERSION));
				root.setAttribute(DATE, String.format("%d",config.lastUpdate.getTime()));
				root.setAttribute(UUID_TAG, config.hostUUID);

				genFileMap(config, root);
				genParamsElement(config, root);
				genQueueElement(config, root);
				genURLTemplateElement(config, root);
				genModuleElements(config, root);
				genURLTemplateElement(config, root);
				genMapLayerElements(config, root);
				genDataTypeElements(config, root);
				
				System.setProperty("javax.xml.transform.TransformerFactory", 
						"com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl");

				Source aSrc = new DOMSource(dom);
				Result aResult = new StreamResult(stream);
				Transformer xmlTrn = TransformerFactory.newInstance().newTransformer();
				xmlTrn.setOutputProperty("indent", "yes");
				xmlTrn.transform(aSrc, aResult);

			} catch (ParserConfigurationException e) {
				throw new ConfigException(e);
			} catch (TransformerConfigurationException e) {
				throw new ConfigException(e);
			} catch (TransformerFactoryConfigurationError e) {
				throw new ConfigException(e);
			} catch (TransformerException e) {
				throw new ConfigException(e);
			}
		}

		private static void genParamsElement(AppConfig config, Element root) throws ConfigException {
			Document dom = root.getOwnerDocument();
			Iterator<String> keyIter = config.extraParams.keySet().iterator();
			while ( keyIter.hasNext() ) {
				String key = keyIter.next();
				Element pathEl = dom.createElement(PARAM_ELEMENT);
				pathEl.setAttribute(PARAM_NAME, key);
				if ( key.equals(PARAM_UPDATE_KEY) || key.equals(PARAM_UPDATE_CERT) ) {
					pathEl.setTextContent(config.extraParams.get(key));
				} else 			
					pathEl.setAttribute(PARAM_VALUE, config.extraParams.get(key));
				root.appendChild(pathEl);
			}
		}


		private static void genQueueElement(AppConfig config, Element root) {
			Document dom = root.getOwnerDocument();
			Iterator<String> keyIter = config.queueType.keySet().iterator();
			while ( keyIter.hasNext() ) {
				String aKey = keyIter.next();
				String aType = config.queueType.get(aKey);
				Element pathEl = dom.createElement(QUEUE_ELEMENT);
				pathEl.setAttribute(QUEUE_TYPE, aKey);
				pathEl.setAttribute(QUEUE_SOURCE, aType);
				if ( aType.equals("static") ) {
					List<String> valList = config.queueValues.get(aKey);
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
		}
		
		private static void genURLTemplateElement(AppConfig config, Element root) {
			Document dom = root.getOwnerDocument();
			Iterator<String> keyIter = config.urlTemplateMap.keySet().iterator();
			while ( keyIter.hasNext() ) {
				String aClass = keyIter.next();
				Map<String,String> aClassTemplate = config.urlTemplateMap.get(aClass);
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
		}

		private static void genModuleElements(AppConfig config, Element root) {
			Document dom = root.getOwnerDocument();
			Iterator<String> keyIter = config.modules.keySet().iterator();
			while ( keyIter.hasNext() ) {
				String aType = keyIter.next();
				List<String> aClassTemplate = config.modules.get(aType);
				Iterator<String> labelIter = aClassTemplate.iterator();
				while ( labelIter.hasNext() ) {
					String aLabel = labelIter.next();
					Element pathEl = dom.createElement(MODULE_ELEMENT);
					pathEl.setAttribute(MODULE_CLASS, aLabel);
					pathEl.setAttribute(MODULE_TYPE, aType);
					pathEl.setAttribute(MODULE_FILE, config.moduleJars.get(aLabel));
					root.appendChild(pathEl);
				}
			}
		}
				
		private static void genMapLayerElements(AppConfig config, Element root) {
			Document dom = root.getOwnerDocument();
			Iterator<String> keyIter = config.mapserverLayers.keySet().iterator();
			while ( keyIter.hasNext() ) {
				String aName = keyIter.next();
				String aURL = config.mapserverLayers.get(aName);
				Element pathEl = dom.createElement(MAPSERVER_ELEMENT);
				pathEl.setAttribute(MAPSERVER_LAYER_NAME, aName);
				pathEl.setAttribute(MAPSERVER_LAYER_URL, aURL);
				root.appendChild(pathEl);
			}
		}
		
		private static void genFileMap(AppConfig config, Element root) {
			Iterator<String> keyIter = config.fileMap.keySet().iterator();
			Document dom = root.getOwnerDocument();
			while ( keyIter.hasNext() ) {
				String aKey = keyIter.next();
				Map<String,String> classMap = config.fileMap.get(aKey);
				Iterator<String> typeIter = classMap.keySet().iterator();
				while ( typeIter.hasNext() ) {
					String aType = (String)typeIter.next();
					Element pathEl = dom.createElement(FILE_PATH_ELEMENT);
					pathEl.setAttribute(FILE_PATH_CLASS_ATTR, aKey);
					pathEl.setAttribute(FILE_PATH_TYPE_ATTR, aType);
					pathEl.setAttribute(FILE_PATH_PATH_ATTR, classMap.get(aType));
					root.appendChild(pathEl);
				}
			}
		}

		private static void genDataTypeElements(AppConfig config, Element root) {
			Iterator<String> keyIter = config.dataTypes.keySet().iterator();
			Document dom = root.getOwnerDocument();
			while ( keyIter.hasNext() ) {
				String aKey = keyIter.next();
				Map<String,String> subMap = config.dataTypes.get(aKey);
				Iterator<String> typeIter = subMap.keySet().iterator();
				while ( typeIter.hasNext() ) {
					String aType = (String)typeIter.next();
					Element pathEl = dom.createElement(DATA_TYPE_ELEMENT);
					pathEl.setAttribute(FILE_PATH_CLASS_ATTR, aKey);
					pathEl.setAttribute(FILE_PATH_TYPE_ATTR, aType);
					pathEl.setAttribute(DATA_TYPE_DESC, subMap.get(aType));
					root.appendChild(pathEl);
				}
			}
		}

		
		private static void genDataSourceElements(AppConfig config, Element root) {
			Iterator<String> keyIter = config.dsMap.keySet().iterator();
			Document dom = root.getOwnerDocument();
			while ( keyIter.hasNext() ) {
				String aKey = keyIter.next();
				String aName = config.dsMap.get(aKey);
				Element pathEl = dom.createElement(DATASOURCE_ELEMENT);
				pathEl.setAttribute(DATASOURCE_TYPE, aKey);
				pathEl.setAttribute(DATASOURCE_NAME, aName);
				root.appendChild(pathEl);
			}
		}

		public static void loadConfig(AppConfig config, File xmlFile) throws ConfigException {
			if ( xmlFile.exists() ) {
				try {
					DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
					DocumentBuilder db = dbf.newDocumentBuilder();
					loadConfig(config, db.parse(xmlFile));
				} catch (ParserConfigurationException e) {
					throw new ConfigException(e);
				} catch (SAXException e) {
					throw new ConfigException(e);
				} catch (IOException e) {
					throw new ConfigException(e);
				}					
			}
		}
		
		public static void loadConfig(AppConfig config, InputStream xmlStream) throws ConfigException {
			try {
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				loadConfig(config, db.parse(xmlStream));
				
			} catch (ParserConfigurationException e) {
				throw new ConfigException(e);
			} catch (SAXException e) {
				throw new ConfigException(e);
			} catch (IOException e) {
				throw new ConfigException(e);
			}					
		}
		
		private static void loadConfig(AppConfig config, Document dom) throws ConfigException {
			Element docElem = dom.getDocumentElement();
			if ( docElem.hasAttribute(VERSION_TAG) ) {
				String verString = docElem.getAttribute(VERSION_TAG);
				try {
					config.setVersion(Float.parseFloat(verString));
				} catch ( NumberFormatException e) {
					e.printStackTrace();
				}
			}
			
			if ( docElem.hasAttribute(DATE) ) {
				String dateString = docElem.getAttribute(DATE);
				try {
					config.lastUpdate = new Date(Long.parseLong(dateString));
				} catch ( NumberFormatException e) {
					e.printStackTrace();
				}
				
			}
			
			if ( docElem.hasAttribute(UUID_TAG)) {
				config.hostUUID = docElem.getAttribute(UUID_TAG);
			}
			
			parseFileElements(config, docElem);
			parseDataTypeElements(config, docElem);
			parseParamsElements(config, docElem);
			parseModules(config, docElem);
			parseQueueElements(config, docElem);
			parseURLTemplates(config, docElem);		
			parseMapLayers(config, docElem);		
			parseMapParams(config, docElem);
		}

		private static void parseFileElements(AppConfig config, Element docElem) throws ConfigException {
			NodeList nl = docElem.getElementsByTagName(FILE_PATH_ELEMENT);
			if (nl != null && nl.getLength() > 0 ) {
				for (int i = 0; i < nl.getLength(); i++) {
					Element pathEl = (Element)nl.item(i);
					String aClass = pathEl.getAttribute(FILE_PATH_CLASS_ATTR);
					String aType = pathEl.getAttribute(FILE_PATH_TYPE_ATTR);
					String aPath = pathEl.getAttribute(FILE_PATH_PATH_ATTR);					
					config.setFilePath(aClass, aType, aPath);
				}
			}
		}
			
		private static void parseDataTypeElements(AppConfig config, Element docElem) throws ConfigException {
			NodeList nl = docElem.getElementsByTagName(DATA_TYPE_ELEMENT);
			if (nl != null && nl.getLength() > 0 ) {
				for (int i = 0; i < nl.getLength(); i++) {
					Element pathEl = (Element)nl.item(i);
					String aClass = pathEl.getAttribute(FILE_PATH_CLASS_ATTR);
					String aType = pathEl.getAttribute(FILE_PATH_TYPE_ATTR);
					String aDesc = pathEl.getAttribute(DATA_TYPE_DESC);					
					config.setDataType(aClass, aType, aDesc);
				}
			}
		}
			
		private void parseDataSourceElements(Element docElem) {
			this.dsMap.clear();
			NodeList nl = docElem.getElementsByTagName(DATASOURCE_ELEMENT);
			if (nl != null && nl.getLength() > 0 ) {
				for (int i = 0; i < nl.getLength(); i++) {
					Element pathEl = (Element)nl.item(i);
					String aType = pathEl.getAttribute(DATASOURCE_TYPE);
					String aName = pathEl.getAttribute(DATASOURCE_NAME);					
					this.dsMap.put(aType, aName);
				}
			}					
		}
		
		private static void parseParamsElements(AppConfig config, Element docElem) throws ConfigException {
			NodeList nl = docElem.getElementsByTagName(PARAM_ELEMENT);
			if (nl != null && nl.getLength() > 0 ) {
				for (int i = 0; i < nl.getLength(); i++) {
					Element pathEl = (Element)nl.item(i);
					String name = pathEl.getAttribute(PARAM_NAME);
					if ( name.equals(PARAM_UPDATE_KEY) || name.equals(PARAM_UPDATE_CERT) )
						config.setParameter(name, pathEl.getTextContent());
					else 
						config.setParameter(name, pathEl.getAttribute(PARAM_VALUE));
				}
			}					
		}
		
		private static void parseQueueElements(AppConfig config, Element docElem) throws ConfigException {
			NodeList nl = docElem.getElementsByTagName(QUEUE_ELEMENT);
			if (nl != null && nl.getLength() > 0 ) {
				for (int i = 0; i < nl.getLength(); i++) {
					Element pathEl = (Element)nl.item(i);
					String qSource = pathEl.getAttribute(QUEUE_SOURCE);
					String qType = pathEl.getAttribute(QUEUE_TYPE);
					config.setQueueSource(qType, qSource);
					if ( qSource.equals("static") ) {
						NodeList valList = pathEl.getElementsByTagName(QUEUE_VALUE_ELEMENT);
						if ( valList != null && valList.getLength() > 0 ) {
//							List<String> aList = new ArrayList<String>();
							for ( int x = 0; x < valList.getLength(); x++ ) {
								Element valEl = (Element)valList.item(x);
								String text = valEl.getTextContent();
								if ( text != null )
									config.addQueue(qType, text);
								//	aList.add(text);
							}
							// this.queueValues.put(aName, aList);
						}
					}
				}
			}	
		}					
		
		private static void parseMapLayers(AppConfig config, Element docElem) throws ConfigException {
			NodeList nl = docElem.getElementsByTagName(MAPSERVER_ELEMENT);
			if (nl != null && nl.getLength() > 0 ) {
				for (int i = 0; i < nl.getLength(); i++) {
					Element pathEl = (Element)nl.item(i);
					config.addMapServerLayer(pathEl.getAttribute(MAPSERVER_LAYER_NAME), pathEl.getAttribute(MAPSERVER_LAYER_URL));
				}
			}
		}

		private static void parseMapParams(AppConfig config, Element docElem) throws ConfigException {
			NodeList nl = docElem.getElementsByTagName(MAP_PARAM_ELEMENT);
			if (nl != null && nl.getLength() > 0 ) {
				for (int i = 0; i < nl.getLength(); i++) {
					Element pathEl = (Element)nl.item(i);
					config.addMapServerLayer(pathEl.getAttribute(MAP_PARAM_PARAM), pathEl.getAttribute(MAP_PARAM_VALUE));
				}
			}
		}

		private static void parseURLTemplates(AppConfig config, Element docElem) throws ConfigException {
			NodeList nl = docElem.getElementsByTagName(URL_TEMPLATE_ELEMENT);
			if ( nl != null && nl.getLength() > 0 ) {
				for (int i = 0; i < nl.getLength(); i++) {
					Element pathEl = (Element)nl.item(i);
					String aClass = pathEl.getAttribute(URL_TEMPLATE_CLASS);
					String aLabel = pathEl.getAttribute(URL_TEMPLATE_LABEL);
					String aURL = pathEl.getAttribute(URL_TEMPLATE_CONTENT);
					config.setURLTemplate(aClass, aLabel, aURL);
/*					
					Map<String,String> aClassTemplate;
					if ( this.urlTemplateMap.containsKey(aClass)) {
						aClassTemplate = this.urlTemplateMap.get(aClass);
					} else {
						aClassTemplate = new HashMap<String,String>();
						this.urlTemplateMap.put(aClass, aClassTemplate);
					}							
					aClassTemplate.put(aLabel,aURL);
*/
				}
			}	
		}
		
		private static void parseModules(AppConfig config, Element docElem) throws ConfigException {
			NodeList nl = docElem.getElementsByTagName(MODULE_ELEMENT);
			if ( nl != null && nl.getLength() > 0 ) {
				for (int i = 0; i < nl.getLength(); i++) {
					Element modEl = (Element)nl.item(i);
					config.addClassForModuleType(modEl.getAttribute(MODULE_TYPE), modEl.getAttribute(MODULE_CLASS));
					config.addClassForJar(modEl.getAttribute(MODULE_CLASS), modEl.getAttribute(MODULE_FILE));

					/*
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
					*/
				}
			}
		}

}
