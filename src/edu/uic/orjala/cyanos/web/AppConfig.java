package edu.uic.orjala.cyanos.web;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import edu.uic.orjala.cyanos.ConfigException;


public abstract class AppConfig {
	
	public static final String QUEUE_STATIC = "static";
	public static final String QUEUE_JDBC = "jdbc";
	public static final String QUEUE_SINGLE = "none";

	public class QueueConfig {


		private String source;
		private String type;
		private final List<String> names = new ArrayList<String>();
		
		QueueConfig(String source, String type) {
			this.source = source;
			this.type = type;
		}
		
		public String getSource() {
			return this.source;
		}
		
		public String getType() {
			return this.type;
		}
		
		public List<String> getNames() {
			return this.names;
		}
		
		public void setSource(String source) {
			this.source = source;
			if ( ! source.equals(QUEUE_STATIC) ) 
				this.names.clear();
		}
		
		public void setStatic() {
			this.setSource(QUEUE_STATIC);
		}
		
		public void setSingle() {
			this.setSource(QUEUE_SINGLE);
		}
		
		public void setJDBC() {
			this.setSource(QUEUE_JDBC);
		}
	}

	public static final String DEREPLICATION_MODULE = "dereplication";

	public static final String UPLOAD_MODULE = "upload";

	public final static String[] TABLES = { "assay", "assay_info",
			"collection", "compound", "compound_peaks", 
			"cryo", "cryo_library", "data", "data_templates",
			"extract_info", "harvest", "inoculation", "isolation", "material",
			"news", "project", "queue", "queue_subscription", 
			"sample", "sample_acct", "sample_library", 
			"separation", "separation_product", "separation_source", 
			"species", "taxon", "taxon_paths", "update_host" };
	
	public final static String[] QUEUE_TYPES = { "user", "inoculation",
			"harvest", "extract", "cryo", "separation", "assay" };
	
	public static final String CYANOS_DB_NAME = "CyanosDB";
	
	public static final Float APP_VERSION = 1.0f;
	public static final int DATABASE_VERSION = 2;

	protected static final String PARAM_EMAIL_ROBOT_ADDRESS = "email_robot";
	public static final String PARAM_GOOGLE_MAP_KEY = "google_map_key";
	public static final String PARAM_UPDATE_KEY = "update_key";
	public static final String PARAM_UPDATE_CERT = "update_cert";
	
	private DataSource myDS = null;
	protected Float version = -1.0f;
	protected String hostUUID = null;
	protected Date lastUpdate;
	
	public static final String MAP_OSM_LAYER = "OpenLayers.OSM";
	public static final String MAP_NASA_LAYER = "OpenLayers.NASA";
	
	protected static final String FILE_PATH_ELEMENT = "filePath";
	protected static final String QUEUE_ELEMENT = "queue";
	protected static final String PARAM_ELEMENT = "parameter";
	protected static final String MAPSERVER_ELEMENT = "mapserver";
	protected static final String URL_TEMPLATE_ELEMENT = "urlTemplate";
	protected static final String MODULE_ELEMENT = "module";
	
	protected boolean updated = false;

	protected final Map<String, Map<String,String>> fileMap;
	protected final Map<String, Map<String,String>> dataTypes;
	protected final Map<String, String> dsMap;
	protected final Map<String, String> extraParams;
	protected final Map<String, String> queueType;
	protected final Map<String, List<String>> queueValues;
	protected final Map<String, Map<String,String>> urlTemplateMap;
	protected final Map<String, List<String>> modules;
	protected final Map<String, String> moduleJars;
	protected final Map<String, String> mapserverLayers;
	protected final Map<String, String> mapParams;
	protected final Map<String, String> authClients = new HashMap<String, String>();

	public abstract void loadConfig() throws ConfigException;

	public abstract boolean configExists() throws ConfigException;

	public abstract void writeConfig() throws ConfigException;
	
	public abstract boolean isUnsaved();

	public abstract boolean isSaved();
	
	public AppConfig() {
		this.fileMap = new HashMap<String, Map<String,String>>();
		this.dataTypes = new HashMap<String, Map<String,String>>();
		this.dsMap = new HashMap<String,String>();
		this.extraParams = new HashMap<String,String>();
		this.urlTemplateMap = new HashMap<String,Map<String,String>>();
		this.queueType = new HashMap<String, String>();
		this.queueValues = new HashMap<String, List<String>>();
		this.modules = new HashMap<String, List<String>>();
		this.moduleJars = new HashMap<String, String>();
		this.mapserverLayers = new HashMap<String, String>();
		this.mapParams = new HashMap<String, String>();
	}

	public AppConfig(AppConfig config) {
		this();
		cloneSuperMap(config.fileMap, this.fileMap);
		cloneSuperMap(config.dataTypes, this.dataTypes);
		cloneMap(config.dsMap, this.dsMap);
		cloneMap(config.extraParams, this.extraParams);
		cloneSuperMap(config.urlTemplateMap, this.urlTemplateMap);
		cloneMap(config.queueType, this.queueType);
		cloneMapList(config.queueValues, this.queueValues);
		cloneMapList(config.modules, this.modules);
		cloneMap(config.mapserverLayers, this.mapserverLayers);
		cloneMap(config.mapParams, this.mapParams);
		cloneMap(config.authClients, this.authClients);
	}
	
	private void cloneSuperMap(Map<String, Map<String, String>> source, Map<String, Map<String, String>> destination) {
		Iterator<String> keyIter = source.keySet().iterator();
		while ( keyIter.hasNext() ) {
			String key = keyIter.next();
			Map<String,String> value = source.get(key);
			Map<String,String> newValue = new HashMap<String,String>();
			this.cloneMap(value, newValue);
			destination.put(new String(key), newValue);
		}		
	}	
	
	private void cloneMap(Map<String, String> source, Map<String, String> destination) {
		Iterator<String> keyIter = source.keySet().iterator();
		while ( keyIter.hasNext() ) {
			String key = keyIter.next();
			destination.put(new String(key), new String(source.get(key)));
		}
	}
	
	private void cloneMapList(Map<String, List<String>> source, Map<String, List<String>> destination ) {
		Iterator<String> keyIter = source.keySet().iterator();
		while ( keyIter.hasNext() ) {
			String key = keyIter.next();
			List<String> value = source.get(key);
			List<String> newValue = new ArrayList<String>();
			this.cloneList(value, newValue);
			destination.put(new String(key), newValue);
		}		
	}
	
	private void cloneList(List<String> source, List<String> destination ) {
		Iterator<String> iter = source.listIterator();
		while ( iter.hasNext() ) {
			destination.add(new String(iter.next()));
		}
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
		return this.getMapParameter(PARAM_GOOGLE_MAP_KEY);
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.CyanosConfig#setGoogleMapKey(java.lang.String)
	 */
	public void setGoogleMapKey(String aValue) { 
		this.setMapParameter(PARAM_GOOGLE_MAP_KEY, aValue);
	}	
	
	public String getUpdateKey() {
		return this.getParameter(PARAM_UPDATE_KEY);
	}
	
	public void setUpdateKey(String key) {
		this.setParameter(PARAM_UPDATE_KEY, key);
	}
	
	public String getUpdateCert() {
		return this.getParameter(PARAM_UPDATE_CERT);
	}
	
	public void setUpdateCert(String cert) {
		this.setParameter(PARAM_UPDATE_CERT, cert);
	}
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.CyanosConfig#canMap()
	 */
	public boolean canMap() {
		return true;
	}

	protected void setParameter(String paramKey, String aValue) {
		this.extraParams.put(paramKey, aValue);
		this.updated = true;
	}

	protected String getParameter(String paramKey) {
		return this.extraParams.get(paramKey);
	}
	
	public String getMapParameter(String paramKey) {
		return this.mapParams.get(paramKey);
	}
	
	public void setMapParameter(String paramKey, String value) {
		this.mapParams.put(paramKey, value);
		this.updated = true;
	}
	
	public void removeMapParameter(String paramKey) {
		this.mapParams.remove(paramKey);
		this.updated = true;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.CyanosConfig#tableList()
	 */
	public String[] tableList() {
		return TABLES;
	}

	public DataSource getDataSourceObject() throws ConfigException {
		if ( this.myDS == null ) {
			try {
				InitialContext initCtx = new InitialContext();
				this.myDS  = (DataSource)initCtx.lookup("java:comp/env/jdbc/" + CYANOS_DB_NAME);
			} catch (NamingException e) {
				throw new ConfigException(e);
			}
		}
		return this.myDS;
	}
	
	public Float getVersion() {
		return this.version;
	}
	
	public String getHostUUID() { 
		return this.hostUUID;
	}
	
	protected void setVersion(float parseFloat) throws ConfigException {
		this.version = parseFloat;
	}

	public void clearFilePaths() {
		this.fileMap.clear();
		this.updated = true;
	}
	
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


	public void setFilePath(String aClass, String aType, String aPath) {
		if ( ! fileMap.containsKey(aClass) ) {
			Map <String,String> classMap = new HashMap<String,String>();
			this.fileMap.put(aClass, classMap);
		}
		this.fileMap.get(aClass).put(aType, aPath);
		this.updated = true;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.CyanosConfig#getFilePathMap()
	 */
	public Map<String, Map<String,String>> getFilePathMap() {
		Map<String, Map<String,String>> returnMap = new HashMap<String, Map<String, String>>();
		Iterator<String> keyIter = fileMap.keySet().iterator();
		while ( keyIter.hasNext() ) {
			String aKey = (String) keyIter.next();
			Map<String,String> classMap = this.fileMap.get(aKey);
			Iterator<String> typeIter = classMap.keySet().iterator();
			Map<String,String> rcMap = new HashMap<String,String>();
			while ( typeIter.hasNext() ) {
				String aType = (String)typeIter.next();
				rcMap.put(aType, classMap.get(aType));
			}
			returnMap.put(aKey, rcMap);
		}			
		return returnMap;
	}

	public void clearDataTypes() {
		this.dataTypes.clear();
		this.updated = true;
	}
	
	public String getDataTypeDescription(String aClass, String description) {
		Map<String,String> classMap = this.getDataTypeMap(aClass);
		if ( classMap.containsKey(description) ) {
			return classMap.get(description);
		} else {
			return null;
		}			
	}
	
	public void addOAuthClient(String clientID, String secret) {
		this.authClients.put(clientID, secret);
		this.updated = true;
	}

	public boolean validateOAuthClient(String clientID, String secret) {
		String realSecret = this.authClients.get(clientID);
		return ( realSecret != null && realSecret.equals(secret) );
	}
	
	public boolean existsOAuthClient(String clientID) {
		return this.authClients.containsKey(clientID);
	}
	
	public void setDataType(String aClass, String type, String description) {
		this.getDataTypeMap(aClass).put(type, description);
		this.updated = true;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.CyanosConfig#getDataTypeMap()
	 */
	public Map<String,String> getDataTypeMap(String dataClass) {
		Map<String,String> map = this.dataTypes.get(dataClass);
		if ( map != null )
			return map;

		map = new TreeMap<String,String>();
		this.dataTypes.put(dataClass, map);
		return map;
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
		if ( ! this.queueValues.containsKey(queueType) )
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
	
	public void removeClassForModuleType(String aType, String aClass) {
		if ( this.modules.containsKey(aType) ) {
			this.modules.get(aType).remove(aClass);
			boolean present = false;
			for ( Entry<String, List<String>> entry : this.modules.entrySet() ) {
				present = entry.getValue().contains(aClass);
			}
			if ( ! present ) {
				this.moduleJars.remove(aClass);
			}
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
	
	public void addClassForJar(String aClass, String jarFile) {
		this.moduleJars.put(aClass, jarFile);
		this.updated = true;
	}

	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.CyanosConfig#clearModules()
	 */
	public void clearModules() {
		this.modules.clear();
		this.moduleJars.clear();
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
	
	public void loadXML(InputStream in) throws ConfigException {
		this.fileMap.clear();
		this.extraParams.clear();
		this.queueType.clear();
		this.queueValues.clear();
		this.mapserverLayers.clear();
		this.urlTemplateMap.clear();
		this.modules.clear();

		AppConfigXML.loadConfig(this, in);							
		this.updated = true;
	}
	
	public Date getDate() {
		return this.lastUpdate;
	}

}
