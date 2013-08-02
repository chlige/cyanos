package edu.uic.orjala.cyanos.web;

import java.io.File;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.sql.DataSource;

import edu.uic.orjala.cyanos.ConfigException;
import edu.uic.orjala.cyanos.sql.SQLData;

/**
 * 
 * Database table layout:
 * 
 *   <TABLE><TR><TH>ELEMENT</TH><TH>PARAM</TH><TH>PARAM_KEY</TH><TH>VALUE</TH><TH>Notes</TH></TR>
 *   <TR><TD><B><I>parameter</I></B></TD><TD>NOT USED</TD><TD>Parameter Name</TD><TD>Parameter Value</TD><TD>Google Map Key & Administator email.</TD></TR>
 *   <TR><TD><B><I>filePath</I></B></TD><TD>class</TD><TD>type</TD><TD>path</TD></TD></TR>
 *   <TR><TD><B><I>queue</I></B></TD><TD>Queue Type</TD><TD>Source</TD><TD>Name (if static)</TD></TD><TD>Sources: Static, JDBC, Single</TD></TR>
 *   <TR><TD><B><I>module</I></B></TD><TD>Module Type</TD><TD></TD><TD>Java Class</TD><td>Modules for dereplication & upload</TD></TR>
 *   <TR><TD><B><I>urlTemplate</I></B></TD><TD>Object Class</TD><TD>Label</TD><TD>URL</TD></TD></TR>
 *   </TABLE>
 * 
 * 
 * 
 */

public class AppConfigSQL extends AppConfig {
	
		private static final String FILE_PATH_ELEMENT = "filePath";
		private final static String QUEUE_ELEMENT = "queue";
		private static final String PARAM_ELEMENT = "parameter";
		private static final String URL_TEMPLATE_ELEMENT = "urlTemplate";
		private static final String MODULE_ELEMENT = "module";
		private static final String CONFIG_ELEMENT = "config";
		private static final String MAP_ELEMENT = "map";
		private static final String DATA_TYPE_ELEMENT = "dataType";

		private static final String VALUE_COLUMN = "value";
		private static final String PARAM_COLUMN = "param";
		private static final String KEY_COLUMN = "param_key";
			
		private final static String QUEUE_SOURCE_KEY = "source";
		private final static String QUEUE_NAME_KEY = "name";
		private static final String CONFIG_VERSION_PARAM = "version";
		private static final String CONFIG_DATE_PARAM = "date";
		private static final String CONFIG_UUID_PARAM = "hostUUID";
		
		private static final String MODULE_CLASS_PARAM = "class";
		private static final String MODULE_FILE_PARAM = "file";
		private static final String OL_MAPSERVER_PARAM = "openlayers.mapserver";
		private static final String MAP_PARAMS = "parameter";
		private static final String ALL_CLASSES = "*";
		
//		private static final String CHECK_CONFIG = "SELECT COUNT(*) FROM config";

		private static final String GET_CONFIG_ELEMENT = "SELECT element,param,param_key,value FROM config WHERE element = ?";
		private static final String GET_CONFIG_PARAM = "SELECT element,param,param_key,value FROM config WHERE element = ? AND param = ?";
		private static final String GET_CONFIG_KEY = "SELECT element,param,param_key,value FROM config WHERE element = ? AND param_key = ?";
		private static final String GET_CONFIG_PARAM_KEY = "SELECT element,param,param_key,value FROM config WHERE element = ? AND param = ? AND param_key = ?";		

		private static final String INSERT_CONFIG_EPKV = "INSERT INTO config(element,param,param_key,value) VALUES (?,?,?,?)";
		private static final String INSERT_CONFIG_EKV = "INSERT INTO config(element,param_key,value) VALUES (?,?,?)";
		private static final String INSERT_CONFIG_EPV = "INSERT INTO config(element,param,value) VALUES (?,?,?)";

		private static final String UPDATE_CONFIG_V_EPK = "UPDATE config SET value=? WHERE element = ? AND param = ? AND param_key = ?";
		private static final String UPDATE_CONFIG_V_EK = "UPDATE config SET value=? WHERE element = ? AND param_key = ?";
				

/*
		
		private static final String GET_ALL_PARAMS = "SELECT DISTINCT element,param FROM config WHERE element = ?";
		private static final String GET_ALL_KEYS = "SELECT DISTINCT element,param,param_key FROM config WHERE element = ? AND param = ?";
		
		private static final String DELETE_CONFIG_EPK = "DELETE FROM config WHERE element = ? AND param = ? AND param_key = ?";
		private static final String DELETE_CONFIG_EP = "DELETE FROM config WHERE element = ? AND param = ?";
		private static final String DELETE_CONFIG_E = "DELETE FROM config WHERE element = ?";
		private static final String DELETE_CONFIG_EPV = "DELETE FROM config WHERE element = ? AND param = ? AND value = ?";;
		private static final String DELETE_CONFIG_EPKV = "DELETE FROM config WHERE element = ? AND param = ? AND param_key = ? AND value = ?";
*/
		private static final String DELETE_CONFIG = "DELETE FROM config WHERE NOT ((element = 'config' AND param_key = 'protected') OR element = 'database')";
				
		public AppConfigSQL() throws ConfigException {
			super();
			this.loadConfig();
		}
		
		public void loadConfig() throws ConfigException {
			this.loadConfig(false);
		}

		public void loadConfig(boolean forceLoad) throws ConfigException {
			if ( forceLoad || this.configExists() ) {

				this.fileMap.clear();
				this.dataTypes.clear();
				this.extraParams.clear();
				this.queueType.clear();
				this.queueValues.clear();
				this.mapserverLayers.clear();
				this.urlTemplateMap.clear();
				this.modules.clear();
				this.mapParams.clear();

				loadConfig(this);							
				this.updated = false;
			}
			
		}
		
		public AppConfigSQL(boolean forceLoad) throws ConfigException {
			super();
			this.loadConfig(forceLoad);
		}

		/* (non-Javadoc)
		 * @see edu.uic.orjala.cyanos.web.CyanosConfig#loadConfig()
		 */
		public static void loadConfig(AppConfig config) throws ConfigException {
			try {
				Connection aConn = config.getDataSourceObject().getConnection();
				try {
					PreparedStatement mySth = aConn.prepareStatement(GET_CONFIG_PARAM);
					mySth.setString(1, CONFIG_ELEMENT);
					mySth.setString(2, CONFIG_VERSION_PARAM);
					ResultSet myResult = mySth.executeQuery();
					if ( myResult.first() ) {
						config.version = myResult.getFloat(VALUE_COLUMN);
						mySth.setString(2, CONFIG_DATE_PARAM);
						myResult = mySth.executeQuery();
						if ( myResult.first() ) {
							config.lastUpdate = new Date(myResult.getLong(VALUE_COLUMN));
						}
					}					
					mySth.setString(2, CONFIG_UUID_PARAM);
					myResult = mySth.executeQuery();
					if ( myResult.first() ) {
						config.hostUUID = myResult.getString(VALUE_COLUMN);
					}
					aConn.close();
				} catch (SQLException e) {
					aConn.close();
					throw new ConfigException(e);
				}
			} catch (SQLException e) {
				throw new ConfigException(e);
			}
			parseFileElements(config);
			parseDataTypes(config);
			parseParamsElements(config);
			parseQueueElements(config);
			parseMapLayers(config);
			parseMapParams(config);
			parseURLTemplates(config);
			parseModules(config);
		}
		
		private static void parseFileElements(AppConfig config) throws ConfigException {
			try {
				Connection aConn = config.getDataSourceObject().getConnection();
				try {
					PreparedStatement mySth = aConn.prepareStatement(GET_CONFIG_ELEMENT);
					mySth.setString(1, FILE_PATH_ELEMENT);
					ResultSet myResult = mySth.executeQuery();
					while ( myResult.next() ) {
						config.setFilePath(myResult.getString(2), myResult.getString(3), myResult.getString(4));
					}					
					aConn.close();
				} catch (SQLException e) {
					aConn.close();
					throw new ConfigException(e);
				}
			} catch (SQLException e) {
				throw new ConfigException(e);
			}
		}
			
		private static void parseDataTypes(AppConfig config) throws ConfigException {
			try {
				Connection aConn = config.getDataSourceObject().getConnection();
				try {
					PreparedStatement mySth = aConn.prepareStatement(GET_CONFIG_ELEMENT);
					mySth.setString(1, DATA_TYPE_ELEMENT);
					ResultSet myResult = mySth.executeQuery();
					while ( myResult.next() ) {
						config.setDataType(myResult.getString(2), myResult.getString(3), myResult.getString(4));
					}					
					aConn.close();
				} catch (SQLException e) {
					aConn.close();
					throw new ConfigException(e);
				}
			} catch (SQLException e) {
				throw new ConfigException(e);
			}
		}
			
		private static void parseParamsElements(AppConfig config) throws ConfigException {
			try {
				Connection aConn = config.getDataSourceObject().getConnection();
				try {
					PreparedStatement mySth = aConn.prepareStatement(GET_CONFIG_ELEMENT);
					mySth.setString(1, PARAM_ELEMENT);
					ResultSet myResult = mySth.executeQuery();
					while ( myResult.next() ) {
						config.setParameter(myResult.getString(3), myResult.getString(4));
					}					
					aConn.close();
				} catch (SQLException e) {
					aConn.close();
					throw new ConfigException(e);
				}
			} catch (SQLException e) {
				throw new ConfigException(e);
			}
		}
		
		private static void parseQueueElements(AppConfig config) throws ConfigException {
			try {
				Connection aConn = config.getDataSourceObject().getConnection();
				try {
					PreparedStatement mySth = aConn.prepareStatement(GET_CONFIG_ELEMENT);
					mySth.setString(1, QUEUE_ELEMENT);
					ResultSet myResult = mySth.executeQuery();
					while ( myResult.next() ) {
						String qType = myResult.getString(2);
						String qSource = myResult.getString(3);
						config.setQueueSource(qType, qSource);
						if ( qSource.equals(QUEUE_STATIC) ) {
							config.addQueue(qType, myResult.getString(4));
						}
					}
					aConn.close();
				} catch (SQLException e) {
					aConn.close();
					throw new ConfigException(e);
				}
			} catch (SQLException e) {
				throw new ConfigException(e);
			}
		}					

		private static void parseMapLayers(AppConfig config) throws ConfigException {
			try {
				Connection aConn = config.getDataSourceObject().getConnection();
				try {
					PreparedStatement mySth = aConn.prepareStatement(GET_CONFIG_PARAM);
					mySth.setString(1, MAP_ELEMENT);
					mySth.setString(2, OL_MAPSERVER_PARAM);
					ResultSet myResult = mySth.executeQuery();
					while ( myResult.next() ) {
						config.addMapServerLayer(myResult.getString(3), myResult.getString(4));
					}
					aConn.close();
				} catch (SQLException e) {
					aConn.close();
					throw new ConfigException(e);
				}
			} catch (SQLException e) {
				throw new ConfigException(e);
			}
		}

		private static void parseMapParams(AppConfig config) throws ConfigException {
			try {
				Connection aConn = config.getDataSourceObject().getConnection();
				try {
					PreparedStatement mySth = aConn.prepareStatement(GET_CONFIG_PARAM);
					mySth.setString(1, MAP_ELEMENT);
					mySth.setString(2, MAP_PARAMS);
					ResultSet myResult = mySth.executeQuery();
					while ( myResult.next() ) {
						config.setMapParameter(myResult.getString(3), myResult.getString(4));
					}
					aConn.close();
				} catch (SQLException e) {
					aConn.close();
					throw new ConfigException(e);
				}
			} catch (SQLException e) {
				throw new ConfigException(e);
			}
		}

		private static void parseURLTemplates(AppConfig config) throws ConfigException {
			try {
				Connection aConn = config.getDataSourceObject().getConnection();
				try {
					PreparedStatement mySth = aConn.prepareStatement(GET_CONFIG_ELEMENT);
					mySth.setString(1, URL_TEMPLATE_ELEMENT);
					ResultSet myResult = mySth.executeQuery();
					while ( myResult.next() ) {
						config.setURLTemplate(myResult.getString(2), myResult.getString(3), myResult.getString(4));
					}
					aConn.close();
				} catch (SQLException e) {
					aConn.close();
					throw new ConfigException(e);
				}
			} catch (SQLException e) {
				throw new ConfigException(e);
			}
		}

		private static void parseModules(AppConfig config) throws ConfigException {
			try {
				Connection aConn = config.getDataSourceObject().getConnection();
				try {
					PreparedStatement mySth = aConn.prepareStatement(GET_CONFIG_PARAM);
					mySth.setString(1, MODULE_ELEMENT);
					mySth.setString(2, MODULE_CLASS_PARAM);
					ResultSet myResult = mySth.executeQuery();
					while ( myResult.next() ) {
						config.addClassForModuleType(myResult.getString(3), myResult.getString(4));
					}
					mySth.setString(2, MODULE_FILE_PARAM);
					myResult = mySth.executeQuery();
					while ( myResult.next() ) {
						config.addClassForJar(myResult.getString(4), myResult.getString(3));
					}
					aConn.close();
				} catch (SQLException e) {
					aConn.close();
					throw new ConfigException(e);
				}
			} catch (SQLException e) {
				throw new ConfigException(e);
			}

		}

		private String getValue(String element, String key) throws ConfigException {
			String retVal = null;
			try {
				Connection aConn = this.getDataSourceObject().getConnection();
				try {
					PreparedStatement mySth = aConn.prepareStatement(GET_CONFIG_KEY);
					mySth.setString(1, element);
					mySth.setString(2, key);
					ResultSet myResult = mySth.executeQuery();
					if ( myResult.first() ) {
						retVal = myResult.getString(VALUE_COLUMN);
					}
					aConn.close();
				} catch (SQLException e) {
					aConn.close();
					throw new ConfigException(e);
				}
			} catch (SQLException e) {
				throw new ConfigException(e);
			}
			return retVal;
		}

		private String getValue(String element, String param, String key) throws ConfigException {
			String retVal = null;
			try {
				Connection aConn = this.getDataSourceObject().getConnection();
				try {
					PreparedStatement mySth = aConn.prepareStatement(GET_CONFIG_PARAM_KEY);
					mySth.setString(1, element);
					mySth.setString(2, param);
					mySth.setString(3, key);
					ResultSet myResult = mySth.executeQuery();
					if ( myResult.first() ) {
						retVal = myResult.getString(VALUE_COLUMN);
					}
					aConn.close();
				} catch (SQLException e) {
					aConn.close();
					throw new ConfigException(e);
				}
			} catch (SQLException e) {
				throw new ConfigException(e);
			}
			return retVal;
		}

		private void addValue(String element, String param, String key, String value) throws ConfigException {
			if ( ! value.equals(this.getValue(element, param, key)) ) {
				try {
					Connection aConn = this.getDataSourceObject().getConnection();
					PreparedStatement mySth = aConn.prepareStatement(INSERT_CONFIG_EPKV);
					mySth.setString(1, element);
					mySth.setString(2, param);
					mySth.setString(3, key);
					mySth.setString(4, value);
					mySth.executeUpdate();
					aConn.close();
				} catch (SQLException e) {
					throw new ConfigException(e);
				}	
			}
		}
		
		
		private void addValue(String element, String key, String value) throws ConfigException {
			if ( ! value.equals(this.getValue(element, key)) ) {
				try {
					Connection aConn = this.getDataSourceObject().getConnection();
					PreparedStatement mySth = aConn.prepareStatement(INSERT_CONFIG_EKV);
					mySth.setString(1, element);
					mySth.setString(2, key);
					mySth.setString(3, value);
					mySth.executeUpdate();
					aConn.close();
				} catch (SQLException e) {
					throw new ConfigException(e);
				}	
			}
		}
		
		
		/* (non-Javadoc)
		 * @see edu.uic.orjala.cyanos.web.CyanosConfig#setUserDB(java.lang.String)
		 */
		public void setUserDB(String aName) {
			// DOES NOTHING RIGHT NOW.
		}
		
		/* (non-Javadoc)
		 * @see edu.uic.orjala.cyanos.web.CyanosConfig#getUserDB()
		 */
		public String getUserDB() {
			return CYANOS_DB_NAME;
		}

		public boolean configExists() throws ConfigException {
			boolean exists = false;
			try {
				Connection aConn = this.getDataSourceObject().getConnection();
				try {
					PreparedStatement mySth = aConn.prepareStatement(GET_CONFIG_PARAM);
					mySth.setString(1, CONFIG_ELEMENT);
					mySth.setString(2, CONFIG_VERSION_PARAM);
					ResultSet myResult = mySth.executeQuery();
					if ( myResult.first() ) {
						exists = true;
					}		
					aConn.close();
				} catch (SQLException e) {
					aConn.close();
					throw new ConfigException(e);
				}
			} catch (SQLException e) {
				throw new ConfigException(e);
			}
			return exists;
		}
		
		public void loadXMLFile(File xmlFile) throws ConfigException {
			boolean cleared = false;
			try {
				Connection aConn = this.getDataSourceObject().getConnection();
				try {
					Statement sth = aConn.createStatement();
					sth.executeUpdate(DELETE_CONFIG);
					cleared = true;
				} catch (SQLException e) {
					throw new ConfigException(e);
				} finally {
					aConn.close();					
				}
			} catch (SQLException e) {
				throw new ConfigException(e);
			}
			if ( cleared ) AppConfigXML.loadConfig(this, xmlFile);
		}

		public void writeConfig() throws ConfigException {
			boolean cleared = false;
			try {
				Connection aConn = this.getDataSourceObject().getConnection();
				try {
					Statement sth = aConn.createStatement();
					sth.executeUpdate(DELETE_CONFIG);
					cleared = true;
				} catch (SQLException e) {
					throw new ConfigException(e);
				} finally {
					aConn.close();					
				}
			} catch (SQLException e) {
				throw new ConfigException(e);
			}

			if ( cleared ) {
				this.genParamKeysValueElement(fileMap, FILE_PATH_ELEMENT);
				this.genParamKeysValueElement(dataTypes, DATA_TYPE_ELEMENT);
				this.genParamKeysValueElement(urlTemplateMap, URL_TEMPLATE_ELEMENT);
				this.genKeyValueElement(extraParams, PARAM_ELEMENT);
				this.genKeyValueElement(mapserverLayers, MAP_ELEMENT, OL_MAPSERVER_PARAM);
				this.genKeyValueElement(mapParams, MAP_ELEMENT, MAP_PARAMS);
				this.genKeyValuesElement(modules, MODULE_ELEMENT, MODULE_CLASS_PARAM);
				this.genModuleJarElement(moduleJars);
				this.genQueueElement();
				this.setVersion();
				this.updated = false;
			}
		}
		
		private void genKeyValuesElement(Map<String, List<String>> source, String element, String param) throws ConfigException {
			try {
				Connection aConn = this.getDataSourceObject().getConnection();
				try {
					PreparedStatement sth = aConn.prepareStatement(INSERT_CONFIG_EPKV);
					sth.setString(1, element);
					sth.setString(2, param);
					for ( Entry<String, List<String>> entry : source.entrySet() ) {
						sth.setString(3, entry.getKey());
						for ( String value : entry.getValue() ) {
							sth.setString(4, value);							
							sth.execute();
						}
					}
				} catch (SQLException e) {
					throw new ConfigException(e);
				} finally {
					aConn.close();					
				}
			} catch (SQLException e) {
				throw new ConfigException(e);
			}
		}
		
		private void genModuleJarElement(Map<String, String> source) throws ConfigException {
			try {
				Connection aConn = this.getDataSourceObject().getConnection();
				try {
					PreparedStatement sth = aConn.prepareStatement(INSERT_CONFIG_EPKV);
					sth.setString(1, MODULE_ELEMENT);
					sth.setString(2, MODULE_FILE_PARAM);
					for ( Entry<String, String> entry : source.entrySet() ) {
						sth.setString(3, entry.getValue());
						sth.setString(4, entry.getKey());
						sth.execute();
					}
				} catch (SQLException e) {
					throw new ConfigException(e);
				} finally {
					aConn.close();					
				}
			} catch (SQLException e) {
				throw new ConfigException(e);
			}
		
		}
		
		private void genKeyValueElement(Map<String,String> source, String element, String param) throws ConfigException {
			try {
				Connection aConn = this.getDataSourceObject().getConnection();
				try {
					PreparedStatement sth = aConn.prepareStatement(INSERT_CONFIG_EPKV);
					sth.setString(1, element);
					sth.setString(2, param);
					for ( Entry<String, String> entry : source.entrySet() ) {
						sth.setString(3, entry.getKey());
						sth.setString(4, entry.getValue());
						sth.execute();
					}
				} catch (SQLException e) {
					throw new ConfigException(e);
				} finally {
					aConn.close();					
				}
			} catch (SQLException e) {
				throw new ConfigException(e);
			}
		}

		private void genKeyValueElement(Map<String,String> source, String element) throws ConfigException {
			try {
				Connection aConn = this.getDataSourceObject().getConnection();
				try {
					PreparedStatement sth = aConn.prepareStatement(INSERT_CONFIG_EKV);
					sth.setString(1, element);
					for ( Entry<String, String> entry : source.entrySet() ) {
						sth.setString(2, entry.getKey());
						sth.setString(3, entry.getValue());
						sth.execute();
					}
				} catch (SQLException e) {
					throw new ConfigException(e);
				} finally {
					aConn.close();					
				}
			} catch (SQLException e) {
				throw new ConfigException(e);
			}
		}

		private void genKeyValuesElement(Map<String, List<String>> source, String element) throws ConfigException {
			try {
				Connection aConn = this.getDataSourceObject().getConnection();
				try {
					PreparedStatement sth = aConn.prepareStatement(INSERT_CONFIG_EKV);
					sth.setString(1, element);
					for ( Entry<String, List<String>> entry : source.entrySet() ) {
						sth.setString(2, entry.getKey());
						for ( String value : entry.getValue() ) {
							sth.setString(3, value);							
							sth.execute();
						}
					}
				} catch (SQLException e) {
					throw new ConfigException(e);
				} finally {
					aConn.close();					
				}
			} catch (SQLException e) {
				throw new ConfigException(e);
			}
		}

		private void genParamKeysValueElement(Map<String, Map<String, String>> source, String element) throws ConfigException {
			try {
				Connection aConn = this.getDataSourceObject().getConnection();
				try {
					PreparedStatement sth = aConn.prepareStatement(INSERT_CONFIG_EPKV);
					sth.setString(1, element);
					for ( Entry<String, Map<String, String>> e : source.entrySet() ) {
						sth.setString(2, e.getKey());
						for ( Entry<String, String> subE : e.getValue().entrySet() ) {
							sth.setString(3, subE.getKey());
							sth.setString(4, subE.getValue());
							sth.execute();
						}
					}
				} catch (SQLException e) {
					throw new ConfigException(e);
				} finally {
					aConn.close();					
				}
			} catch (SQLException e) {
				throw new ConfigException(e);
			}
		}

		
		private void genQueueElement() throws ConfigException {
			try {
				Connection aConn = this.getDataSourceObject().getConnection();
				try {
					PreparedStatement sth = aConn.prepareStatement(INSERT_CONFIG_EPKV);
					sth.setString(1, QUEUE_ELEMENT);

					Iterator<String> keyIter = this.queueType.keySet().iterator();
					while ( keyIter.hasNext() ) {
						String aKey = keyIter.next();
						String aSource = this.queueType.get(aKey);
						sth.setString(2, aKey);
						sth.setString(3, aSource);
						if ( aSource.equals(QUEUE_STATIC) ) {
							List<String> valList = this.queueValues.get(aKey);
							ListIterator<String> anIter = valList.listIterator();
							while ( anIter.hasNext() ) {
								sth.setString(4, anIter.next());
								sth.execute();
							}
						} else {
							sth.setString(4, "");
							sth.execute();
						}
					}
				} catch (SQLException e) {
					throw new ConfigException(e);
				} finally {
					aConn.close();					
				}
			} catch (SQLException e) {
				throw new ConfigException(e);
			}
		}
		
		private void setVersion() throws ConfigException {
			try {
				Connection aConn = this.getDataSourceObject().getConnection();
				try {
					PreparedStatement mySth = aConn.prepareStatement(INSERT_CONFIG_EPV);
					mySth.setString(1, CONFIG_ELEMENT);
					mySth.setString(2, CONFIG_VERSION_PARAM);
					mySth.setFloat(3, APP_VERSION);
					mySth.execute();
					
					mySth.setString(2, CONFIG_DATE_PARAM);
					mySth.setLong(3, Calendar.getInstance().getTimeInMillis());
					mySth.execute();
					
	//				mySth.setString(2, CONFIG_UUID_PARAM);
	//				mySth.setString(3, this.hostUUID);
	//				aConn.close();
				} catch (SQLException e) {
					aConn.close();
					throw new ConfigException(e);
				}
			} catch (SQLException e) {
				throw new ConfigException(e);
			}

		}

		public boolean isSaved() {
			return ! this.updated;
		}

		public boolean isUnsaved() {
			return this.updated;
		}
		
		private static final String SQL_GET_SCHEMA_PARAM = "SELECT value FROM config WHERE element = 'database' AND param = ?";

		private static final String SCHEMA_ID_TYPE_PARAM = "id_type";
		private static final String SCHEMA_VERSION_PARAM = "version";
				
		public static int getSchemaVersion(DataSource aDS) throws SQLException {
			Connection conn = aDS.getConnection();
			try { 
				return getSchemaVersion(conn);
			} finally {
				conn.close();
			}
		}

		public static int getSchemaVersion(Connection connection) throws SQLException {
			int version = 0;
			PreparedStatement psth = connection.prepareStatement(SQL_GET_SCHEMA_PARAM);
			psth.setString(1, SCHEMA_VERSION_PARAM);
			ResultSet result = psth.executeQuery();
			if ( result.first() ) {
				version = result.getInt(1);
			}
			result.close();
			psth.close();
			return version;
		}
		
		public static int getSchemaIDType(DataSource aDS) throws SQLException {
			Connection conn = aDS.getConnection();
			try { 
				return getSchemaIDType(conn);
			} finally {
				conn.close();
			}
		}

		public static int getSchemaIDType(Connection connection) throws SQLException {
			int version = SQLData.ID_TYPE_SERIAL;
			PreparedStatement psth = connection.prepareStatement(SQL_GET_SCHEMA_PARAM);
			psth.setString(1, SCHEMA_ID_TYPE_PARAM);
			ResultSet result = psth.executeQuery();
			if ( result.first() ) {
				version = result.getInt(1);
			}
			result.close();
			psth.close();
			return version;
		}
		
		public static Set<String> getTables(Connection connection) throws SQLException { 
			DatabaseMetaData dbMeta = connection.getMetaData();
			String[] types = {"TABLE"};
			ResultSet aResult = dbMeta.getTables(null, null, null, types);
			Set<String> foundTables = new HashSet<String>();
			aResult.beforeFirst();
			while ( aResult.next() ) {
				foundTables.add(aResult.getString("TABLE_NAME"));
			}
			aResult.close();
			return foundTables;
		}
}

/*
 * 
 * OLD CODE




protected String getParameter(String paramKey) throws ConfigException {
	return this.getValue(PARAM_ELEMENT, paramKey);
}
		
protected void setParameter(String paramKey, String aValue) throws ConfigException {
	this.setValue(PARAM_ELEMENT, paramKey, aValue);
}


public void clearFilePaths() throws ConfigException {
	this.clearElement(FILE_PATH_ELEMENT);
}


public void setFilePath(String aClass, String aType, String aPath) throws ConfigException {
	this.setValue(FILE_PATH_ELEMENT, aClass, aType, aPath);
}


public String getFilePath(String aClass, String aType) throws ConfigException {
	String retVal = this.getValue(FILE_PATH_ELEMENT, aClass, aType);
	if ( retVal == null ) {
		retVal = this.getValue(FILE_PATH_ELEMENT, aClass, ALL_CLASSES);
		if ( retVal == null ) {
			retVal = this.getValue(FILE_PATH_ELEMENT, ALL_CLASSES, aType);
			if ( retVal == null ) {
				retVal = this.getValue(FILE_PATH_ELEMENT, ALL_CLASSES, ALL_CLASSES);
			}
		}
	}
	return retVal;
}


public Map<String, Map<String,String>> getFilePathMap() throws ConfigException {
	Map<String, Map<String,String>> returnMap = new HashMap<String, Map<String, String>>();
	try {
		Connection aConn = this.getDataSourceObject().getConnection();
		PreparedStatement mySth = aConn.prepareStatement(GET_CONFIG_ELEMENT);
		mySth.setString(1, FILE_PATH_ELEMENT);
		ResultSet myResults = mySth.executeQuery();	
		while ( myResults.next() ) {
			String fileClass = myResults.getString(2);
			String fileType = myResults.getString(3);
			String fileLoc = myResults.getString(4);
			if ( ! returnMap.containsKey(fileClass) ) 
				returnMap.put(fileClass, new HashMap<String,String>());
			returnMap.get(fileClass).put(fileType, fileLoc);
		}
	} catch (SQLException e) {
		throw new ConfigException(e);
	}
	return returnMap;
}


public boolean isUnsaved() {
	return false;
}


public boolean isSaved() {
	return true;
}


public void writeConfig() throws ConfigException {
	// NOTHING TO DO!
}


private List<String> getValues(String element, String param) throws ConfigException {
	List<String> retVal = new ArrayList<String>();
	try {
		Connection aConn = this.getDataSourceObject().getConnection();
		try {
			PreparedStatement mySth = aConn.prepareStatement(GET_CONFIG_PARAM);
			mySth.setString(1, element);
			mySth.setString(2, param);
			ResultSet myResult = mySth.executeQuery();
			while ( myResult.next() ) {
				retVal.add(myResult.getString(VALUE_COLUMN));
			}
			aConn.close();
		} catch (SQLException e) {
			aConn.close();
			throw new ConfigException(e);
		}
	} catch (SQLException e) {
		throw new ConfigException(e);
	}
	return retVal;
}

private List<String> getValues(String element, String param, String key) throws ConfigException {
	List<String> retVal = new ArrayList<String>();
	try {
		Connection aConn = this.getDataSourceObject().getConnection();
		try {
			PreparedStatement mySth = aConn.prepareStatement(GET_CONFIG_PARAM_KEY);
			mySth.setString(1, element);
			mySth.setString(2, param);
			mySth.setString(3, key);
			ResultSet myResult = mySth.executeQuery();
			while ( myResult.next() ) {
				retVal.add(myResult.getString(VALUE_COLUMN));
			}
			aConn.close();
		} catch (SQLException e) {
			aConn.close();
			throw new ConfigException(e);
		}
	} catch (SQLException e) {
		throw new ConfigException(e);
	}
	return retVal;
}

private List<String> getParams(String element) throws ConfigException {
	List<String> retVal = new ArrayList<String>();
	try {
		Connection aConn = this.getDataSourceObject().getConnection();
		try {
			PreparedStatement mySth = aConn.prepareStatement(GET_ALL_PARAMS);
			mySth.setString(1, element);
			ResultSet myResult = mySth.executeQuery();
			while ( myResult.next() ) {
				retVal.add(myResult.getString(PARAM_COLUMN));
			}
			aConn.close();
		} catch (SQLException e) {
			aConn.close();
			throw new ConfigException(e);
		}
	} catch (SQLException e) {
		throw new ConfigException(e);
	}
	return retVal;
}

private List<String> getKeys(String element, String param) throws ConfigException {
	List<String> retVal = new ArrayList<String>();
	try {
		Connection aConn = this.getDataSourceObject().getConnection();
		try {
			PreparedStatement mySth = aConn.prepareStatement(GET_ALL_KEYS);
			mySth.setString(1, element);
			mySth.setString(2, param);
			ResultSet myResult = mySth.executeQuery();
			while ( myResult.next() ) {
				retVal.add(myResult.getString(KEY_COLUMN));
			}
			aConn.close();
		} catch (SQLException e) {
			aConn.close();
			throw new ConfigException(e);
		}
	} catch (SQLException e) {
		throw new ConfigException(e);
	}
	return retVal;
}

private Map<String, String> getKeyMap(String element, String param) throws ConfigException {
	Map<String, String> retVal = new HashMap<String,String>();
	try {
		Connection aConn = this.getDataSourceObject().getConnection();
		try {
			PreparedStatement mySth = aConn.prepareStatement(GET_CONFIG_PARAM);
			mySth.setString(1, element);
			mySth.setString(2, param);
			ResultSet myResult = mySth.executeQuery();
			while ( myResult.next() ) {
				retVal.put(myResult.getString(KEY_COLUMN), myResult.getString(VALUE_COLUMN));
			}
			aConn.close();
		} catch (SQLException e) {
			aConn.close();
			throw new ConfigException(e);
		}
	} catch (SQLException e) {
		throw new ConfigException(e);
	}
	return retVal;
}

private void setValue(String element, String param, String value) throws ConfigException {
	try {
		Connection aConn = this.getDataSourceObject().getConnection();
		if ( this.getParameter(param) != null ) {
			PreparedStatement mySth = aConn.prepareStatement(UPDATE_CONFIG_V_EP);
			mySth.setString(2, element);
			mySth.setString(3, param);
			mySth.setString(1, value);
			mySth.executeUpdate();				
		} else {
			PreparedStatement mySth = aConn.prepareStatement(INSERT_CONFIG_EPV);
			mySth.setString(1, element);
			mySth.setString(2, param);
			mySth.setString(3, value);
			mySth.executeUpdate();
		}
		aConn.close();
	} catch (SQLException e) {
		throw new ConfigException(e);
	}				
}

private void setValue(String element, String param, float value) throws ConfigException {
	try {
		Connection aConn = this.getDataSourceObject().getConnection();
		if ( this.getParameter(param) != null ) {
			PreparedStatement mySth = aConn.prepareStatement(UPDATE_CONFIG_V_EP);
			mySth.setString(2, element);
			mySth.setString(3, param);
			mySth.setFloat(1, value);
			mySth.executeUpdate();				
		} else {
			PreparedStatement mySth = aConn.prepareStatement(INSERT_CONFIG_EPV);
			mySth.setString(1, element);
			mySth.setString(2, param);
			mySth.setFloat(3, value);
			mySth.executeUpdate();
		}
		aConn.close();
	} catch (SQLException e) {
		throw new ConfigException(e);
	}				
}

private void setValue(String element, String param, String key, String value) throws ConfigException {
	try {
		Connection aConn = this.getDataSourceObject().getConnection();
		if ( this.getParameter(param) != null ) {
			PreparedStatement mySth = aConn.prepareStatement(UPDATE_CONFIG_V_EPK);
			mySth.setString(1, value);
			mySth.setString(2, element);
			mySth.setString(3, param);
			mySth.setString(4, key);
			mySth.executeUpdate();				
		} else {
			PreparedStatement mySth = aConn.prepareStatement(INSERT_CONFIG_EPKV);
			mySth.setString(1, element);
			mySth.setString(2, param);
			mySth.setString(3, key);
			mySth.setString(4, value);
			mySth.executeUpdate();
		}
		aConn.close();
	} catch (SQLException e) {
		throw new ConfigException(e);
	}				
}

private void addValue(String element, String param, String key, String value) throws ConfigException {
	if ( ! value.equals(this.getValue(element, param, key)) ) {
		try {
			Connection aConn = this.getDataSourceObject().getConnection();
			PreparedStatement mySth = aConn.prepareStatement(INSERT_CONFIG_EPKV);
			mySth.setString(1, element);
			mySth.setString(2, param);
			mySth.setString(3, key);
			mySth.setString(4, value);
			mySth.executeUpdate();
			aConn.close();
		} catch (SQLException e) {
			throw new ConfigException(e);
		}	
	}
}

private void clearElement(String element) throws ConfigException {
	try {
		Connection aConn = this.getDataSourceObject().getConnection();
		try {
			PreparedStatement mySth = aConn.prepareStatement(DELETE_CONFIG_E);
			mySth.setString(1, element);
			mySth.executeUpdate();
			aConn.close();
		} catch (SQLException e) {
			aConn.close();
			throw new ConfigException(e);
		}
	} catch (SQLException e) {
		throw new ConfigException(e);
	}
}

private void deleteParam(String element, String param) throws ConfigException {
	try {
		Connection aConn = this.getDataSourceObject().getConnection();
		try {
			PreparedStatement mySth = aConn.prepareStatement(DELETE_CONFIG_EP);
			mySth.setString(1, element);
			mySth.setString(2, param);
			mySth.executeUpdate();
			aConn.close();
		} catch (SQLException e) {
			aConn.close();
			throw new ConfigException(e);
		}
	} catch (SQLException e) {
		throw new ConfigException(e);
	}	
}

private void deleteParam(String element, String param, String key) throws ConfigException {
	try {
		Connection aConn = this.getDataSourceObject().getConnection();
		try {
			PreparedStatement mySth = aConn.prepareStatement(DELETE_CONFIG_EPK);
			mySth.setString(1, element);
			mySth.setString(2, param);
			mySth.setString(3, key);
			mySth.executeUpdate();
			aConn.close();
		} catch (SQLException e) {
			aConn.close();
			throw new ConfigException(e);
		}
	} catch (SQLException e) {
		throw new ConfigException(e);
	}
}

private void deleteValue(String element, String param, String value) throws ConfigException {
	try {
		Connection aConn = this.getDataSourceObject().getConnection();
		try {
			PreparedStatement mySth = aConn.prepareStatement(DELETE_CONFIG_EPV);
			mySth.setString(1, element);
			mySth.setString(2, param);
			mySth.setString(3, value);
			mySth.executeUpdate();
			aConn.close();
		} catch (SQLException e) {
			aConn.close();
			throw new ConfigException(e);
		}
	} catch (SQLException e) {
		throw new ConfigException(e);
	}
}

private void deleteValue(String element, String param, String key, String value) throws ConfigException {
	try {
		Connection aConn = this.getDataSourceObject().getConnection();
		try {
			PreparedStatement mySth = aConn.prepareStatement(DELETE_CONFIG_EPKV);
			mySth.setString(1, element);
			mySth.setString(2, param);
			mySth.setString(3, key);
			mySth.setString(4, value);
			mySth.executeUpdate();
			aConn.close();
		} catch (SQLException e) {
			aConn.close();
			throw new ConfigException(e);
		}
	} catch (SQLException e) {
		throw new ConfigException(e);
	}
}



public boolean isQueueStatic(String queueType) throws ConfigException {
	String source = queueSource(queueType);
	return (source != null && source.equalsIgnoreCase(QUEUE_STATIC));
}


public boolean isQueueDynamic(String queueType) throws ConfigException {
	String source = queueSource(queueType);
	return (source != null && source.equalsIgnoreCase(QUEUE_JDBC));
}


public boolean isQueueSingle(String queueType) throws ConfigException {
	String source = queueSource(queueType);
	return (source != null && source.equalsIgnoreCase(QUEUE_SINGLE));
}


public String queueSource(String queueType) throws ConfigException {
	return this.getValue(QUEUE_ELEMENT, queueType, QUEUE_SOURCE_KEY);
}


public void setQueueSource(String queueType, String newSource) throws ConfigException {
	if ( QUEUE_STATIC.equals(this.queueSource(queueType)) ) {
		this.deleteParam(QUEUE_ELEMENT, queueType, QUEUE_NAME_KEY);
	}
	this.setValue(QUEUE_ELEMENT, queueType, QUEUE_SOURCE_KEY, newSource);
}


public List<String> queuesForType(String queueType) throws ConfigException {
	return this.getValues(QUEUE_ELEMENT, queueType, QUEUE_NAME_KEY);
}


public Set<String> queueTypes() throws ConfigException {
	return new HashSet<String>(this.getParams(QUEUE_ELEMENT));
}


public void addQueue(String queueSource, String queueName) throws ConfigException {
	this.setValue(QUEUE_ELEMENT, queueSource, QUEUE_SOURCE_KEY, QUEUE_STATIC);
	this.addValue(QUEUE_ELEMENT, queueSource, QUEUE_NAME_KEY, queueName);
}


public void removeQueue(String queueSource, String queueName) throws ConfigException {
	this.deleteValue(QUEUE_ELEMENT, queueSource, QUEUE_NAME_KEY, queueName);
}


public void setQueues(String queueSource, String[] queues) throws ConfigException {
	this.deleteParam(QUEUE_ELEMENT, queueSource, QUEUE_NAME_KEY);
	for ( int i = 0; i < queues.length; i++ ) {
		this.addQueue(queueSource, queues[i]);
	}
}


public String[] tableList() {
	return CyanosConfig.TABLES;
}



public String getURLTemplate(String aClass, String aLabel) throws ConfigException {
	return this.getValue(URL_TEMPLATE_ELEMENT, aClass, aLabel);
}


public Map<String,String> getURLTemplates(String aClass) throws ConfigException {
	return this.getKeyMap(URL_TEMPLATE_ELEMENT, aClass);
}


public void setURLTemplate(String aClass, String aLabel, String aTemplate) throws ConfigException {
	this.setValue(URL_TEMPLATE_ELEMENT, aClass, aLabel, aTemplate);
}


public List<String> classesForModuleType(String aType) throws ConfigException {
	return this.getValues(MODULE_ELEMENT, aType);
}


public void addClassForModuleType(String aType, String aClass) throws ConfigException {
	this.addValue(MODULE_ELEMENT, aType, "", aClass);
}


public void addClassForDereplicationModule(String aClass) throws ConfigException {
	this.addClassForModuleType(DEREPLICATION_MODULE, aClass);
}


public List<String> classesForDereplicationModule() throws ConfigException {
	return this.classesForModuleType(DEREPLICATION_MODULE);
}


public void addClassForUploadModule(String aClass) throws ConfigException {
	this.addClassForModuleType(UPLOAD_MODULE, aClass);
}


public List<String> classesForUploadModule() throws ConfigException {
	return this.classesForModuleType(UPLOAD_MODULE);
}


public void clearModules() throws ConfigException {
	this.clearElement(MODULE_ELEMENT);
}

public void addMapServerLayer(String aName, String url) throws ConfigException {
	this.setValue(MAPSERVER_ELEMENT, aName, url);
}

public void deleteMapServerLayer(String aName) throws ConfigException {
	this.deleteParam(MAPSERVER_ELEMENT, aName);
}

public Map<String, String> getMapServerLayers() throws ConfigException {
	Map<String, String> retVal = new HashMap<String,String>();
	try {
		Connection aConn = this.getDataSourceObject().getConnection();
		try {
			PreparedStatement mySth = aConn.prepareStatement(GET_CONFIG_ELEMENT);
			mySth.setString(1, MAPSERVER_ELEMENT);
			ResultSet myResult = mySth.executeQuery();
			while ( myResult.next() ) {
				retVal.put(myResult.getString(PARAM_COLUMN), myResult.getString(VALUE_COLUMN));
			}
		} catch (SQLException e) {
			throw new ConfigException(e);
		} finally {
			aConn.close();					
		}
	} catch (SQLException e) {
		throw new ConfigException(e);
	}
	return retVal;
}

*/
