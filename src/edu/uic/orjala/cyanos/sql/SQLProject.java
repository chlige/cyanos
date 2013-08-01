/**
 * 
 */
package edu.uic.orjala.cyanos.sql;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;

import edu.uic.orjala.cyanos.Assay;
import edu.uic.orjala.cyanos.Collection;
import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.Inoc;
import edu.uic.orjala.cyanos.Material;
import edu.uic.orjala.cyanos.Project;
import edu.uic.orjala.cyanos.Role;
import edu.uic.orjala.cyanos.Strain;
import edu.uic.orjala.cyanos.User;

/**
 * @author George Chlipala
 *
 */
public class SQLProject extends SQLObject implements Project {

	/**
	 * @author George Chlipala
	 *
	 */
	public class SQLUpdateHost extends SQLObject implements UpdateHost {

		private static final String HOSTNAME_COLUMN = "hostname";
		private static final String HOST_ID_COLUMN = "host_id";
		private static final String PUBLIC_KEY_COLUMN = "pub_key";
		
		protected SQLUpdateHost(SQLData data) {
			super(data);
			this.initVals();
		}
		
		protected void initVals() {
			this.idField = HOST_ID_COLUMN;
			this.myData.setAccessRole(User.PROJECT_MANAGER_ROLE);
			this.myData.setProjectField(SQLProject.ID_COLUMN);
		}
		
		/* (non-Javadoc)
		 * @see edu.uic.orjala.cyanos.Project.UpdateHost#getHostName()
		 */
		@Override
		public String getHostName() throws DataException {
			return this.myData.getString(HOSTNAME_COLUMN);
		}

		/* (non-Javadoc)
		 * @see edu.uic.orjala.cyanos.Project.UpdateHost#getHostID()
		 */
		@Override
		public String getHostID() throws DataException {
			return this.myData.getString(HOST_ID_COLUMN);
		}

		/* (non-Javadoc)
		 * @see edu.uic.orjala.cyanos.Project.UpdateHost#getPublicKeyString()
		 */
		@Override
		public String getPublicKeyString() throws DataException {
			return this.myData.getString(PUBLIC_KEY_COLUMN);
		}

		/* (non-Javadoc)
		 * @see edu.uic.orjala.cyanos.Project.UpdateHost#getPublicKey()
		 */
		@Override
		public PublicKey getPublicKey() throws DataException, GeneralSecurityException {
			return parsePublicKey(this.getPublicKeyString());
		}

		/* (non-Javadoc)
		 * @see edu.uic.orjala.cyanos.sql.SQLObject#fetchRecord()
		 */
		@Override
		protected void fetchRecord() throws DataException {
			// DOES NOTHING!
		}

	}

	// Setup the column names here so that changing is easier.
	public static final String ID_COLUMN = "project_id";
	public static final String NOTES_COLUMN = "notes";
	public static final String NAME_COLUMN = "name";
	public static final String URL_COLUMN = "url";
	public static final String KEY_COLUMN = "master_key";
	public static final String UPDATE_COLUMN = "update_prefs";
	public static final String LAST_UPDATE = "last_update_sent";
	public static final String LAST_UPDATE_MESSAGE = "last_update_message";
	
	private static final String INSERT_PROJECT_SQL = "INSERT INTO project(project_id) VALUES(?)";
	private static final String LOAD_COLLECTION_SQL = "SELECT collection.* FROM collection WHERE project_id = ?";
	private static final String LOAD_STRAINS_SQL = "SELECT species.* FROM species WHERE project_id = ?";
	private static final String LOAD_INOCS_SQL = "SELECT inoculation.* FROM inoculation WHERE project_id = ?";
	private static final String LOAD_MATERIAL_SQL = "SELECT material.* FROM material WHERE project_id = ?";
	private static final String LOAD_ASSAY_SQL = "SELECT assay_info.* FROM assay_info WHERE project_id = ?";
	private static final String SQL_LOAD = "SELECT project.* FROM project WHERE project_id=?";
	private static final String SQL_LOAD_ALL = "SELECT project.* FROM project ORDER BY %s %s";
	private static final String SQL_ADD_OWNER = "INSERT INTO roles(perm,username,project_id,role) VALUES(?,?,?,?)";
	
	public static final String SQL_UPDATABLE_PROJECTS = "SELECT project_id,url,master_key,update_prefs,last_update_sent FROM project WHERE url IS NOT NULL";
	public static final String SQL_SET_UPDATE = "UPDATE project SET last_update_sent = ? WHERE project_id = ?";
	
	public static final String SQL_GET_CERT = "SELECT pub_key FROM update_host WHERE host_id = ? AND project_id = ?";
	public static final String SQL_DEL_HOST = "DELETE FROM update_host WHERE host_id = ? AND project_id = ?";
	public static final String SQL_ADD_HOST = "REPLACE update_host(project_id,host_id,hostname,pub_key) VALUES(?,?,?,?)";
	public static final String SQL_GET_HOSTS = "SELECT host_id,hostname,pub_key,project_id FROM update_host WHERE project_id = ? ORDER BY host_id";
	public static final String SQL_GET_HOST = "SELECT host_id,hostname,pub_key,project_id FROM update_host WHERE project_id = ? AND host_id = ?";
	
	private static final String SQL_GET_NOW = "SELECT NOW()";
	
	public static final String KEY_ALGORITHM = "DSA";
	protected static KeyFactory keyFactory;
		
	/**
	 * Retrieve all projects sorted by specified column
	 * 
	 * @param data SQLData object
	 * @param column Column to sort by (use statics)
	 * @param sortDirection Direction for sort @see SQLObject.ASCENDING_SORT and SQLObject.DESCENDING_SORT
	 * @return project object with all projects.
	 * @throws DataException
	 */
	public static Project projects(SQLData data, String column, String sortDirection) throws DataException {
		SQLProject myCols = new SQLProject(data);
		String sqlString = String.format(SQL_LOAD_ALL, column, sortDirection);
		myCols.myData.loadUsingSQL(sqlString);
		return myCols;
	}

	/**
	 * Create a new Project.
	 *  
	 * @param data SQLData object.
	 * @param projectID ID of the new project.
	 * @throws DataException
	 */
	public static Project create(SQLData data, String projectID) throws DataException {
		SQLProject newProject = null;
		if ( data.getUser().hasGlobalPermission(User.PROJECT_MANAGER_ROLE, Role.CREATE) ) {
			try {
				PreparedStatement aPsth = data.prepareStatement(INSERT_PROJECT_SQL);
				aPsth.setString(1, projectID);
				if ( aPsth.executeUpdate() > 0 ) {
					aPsth.close();
					aPsth = data.prepareStatement(SQL_ADD_OWNER);
					aPsth.setInt(1, Role.READ + Role.WRITE + Role.DELETE + Role.CREATE);
					aPsth.setString(2, data.getUser().getUserID());
					aPsth.setString(3, projectID);
					aPsth.setString(4, User.ADMIN_ROLE);
					aPsth.executeUpdate();
				}
				aPsth.close();
				newProject = new SQLProject(data, projectID);
			} catch (SQLException e) {
				throw new DataException(e);
			} 
		}
		return newProject;
	}
	
	public static Project load(SQLData data, String projectID) throws DataException {
		SQLProject project = new SQLProject(data);
		project.myID = projectID;
		project.initVals();
		project.fetchRecord();
		return project;
	}
	
	public static Date SQLNow(SQLData data) throws DataException {
		SQLData dataCopy = data.duplicate();
		dataCopy.loadUsingSQL(SQL_GET_NOW);
		Date now = null;
		dataCopy.beforeFirst();
		while ( dataCopy.next() ) {
			now = dataCopy.getTimestamp(1);
		}
		return now;
	}
		
	protected SQLProject(SQLData data) {
		super(data);
		this.initVals();
	}

	@Deprecated
	public SQLProject(SQLData data, String anID) throws DataException {
		super(data);
		this.myID = anID;
		this.initVals();
		this.fetchRecord();
	}
	
	protected void initVals() {
		this.idField = ID_COLUMN;
		this.myData.setAccessRole(User.PROJECT_MANAGER_ROLE);
		this.myData.setProjectField(ID_COLUMN);
	}
	
	protected void fetchRecord() throws DataException {
		this.fetchRecord(SQL_LOAD);
	}
	
	public String getProjectID() throws DataException {
		return this.myData.getQUIETString(ID_COLUMN);
	}
	
	public String getName() throws DataException {
		return this.myData.getQUIETString(NAME_COLUMN);
	}
	
	public String getNotes() throws DataException {
		return this.myData.getString(NOTES_COLUMN);
	}
	
	public void setName(String newValue) throws DataException {
		this.myData.setString(NAME_COLUMN, newValue);
	}
	
	public void setNotes(String newValue) throws DataException {
		this.myData.setString(NOTES_COLUMN, newValue);
	}
	
	public Collection collections() throws DataException {
		SQLCollection retVal = new SQLCollection(this.myData);
		try {
			PreparedStatement aPsth = this.myData.prepareStatement(LOAD_COLLECTION_SQL);
			aPsth.setString(1, this.myID);
			retVal.loadUsingPreparedStatement(aPsth);
			if ( retVal.first())
				return retVal;
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return null;
	}
	
	public Strain strains() throws DataException {
		SQLStrain retVal = new SQLStrain(this.myData);
		try {
			PreparedStatement aPsth = this.myData.prepareStatement(LOAD_STRAINS_SQL);
			aPsth.setString(1, this.myID);
			retVal.loadUsingPreparedStatement(aPsth);
			if ( retVal.first())
				return retVal;
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return null;
	}
	
	public Inoc inoculations() throws DataException {
		SQLInoc retVal = new SQLInoc(this.myData);
		try {
			PreparedStatement aPsth = this.myData.prepareStatement(LOAD_INOCS_SQL);
			aPsth.setString(1, this.myID);
			retVal.loadUsingPreparedStatement(aPsth);
			if ( retVal.first())
				return retVal;
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return null;
	}

	public Material materials() throws DataException {
		SQLMaterial aMaterial = new SQLMaterial(this.myData);
		try {
			PreparedStatement aPsth = this.myData.prepareStatement(LOAD_MATERIAL_SQL);
			aPsth.setString(1, this.myID);
			aMaterial.loadUsingPreparedStatement(aPsth);
			if ( aMaterial.first())
				return aMaterial;
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return null;
	}
	
	public Assay assays() throws DataException {
		SQLAssay assays = new SQLAssay(this.myData);
		try {
			PreparedStatement aPsth = this.myData.prepareStatement(LOAD_ASSAY_SQL);
			aPsth.setString(1, this.myID);
			assays.loadUsingPreparedStatement(aPsth);
			if ( assays.first())
				return assays;
		} catch (SQLException e) {
			throw new DataException(e);
		}
		return null;
	}

	
	public void setMasterURL(String url) throws DataException {
		this.myData.setStringNullBlank(URL_COLUMN, url);
	}

	
	public String getMasterURL() throws DataException {
		return this.myData.getString(URL_COLUMN);
	}

	
	public void setUpdateCert(String key) throws DataException {
		this.myData.setStringNullBlank(KEY_COLUMN, key);
	}

	
	public String getUpdateCert() throws DataException {
		return this.myData.getString(KEY_COLUMN);
	}

	
	public Map<String, Integer> getUpdatePrefs() throws DataException {
		Map<String,Integer> updatePrefs = new HashMap<String,Integer>();
		String updateValue = this.myData.getString(UPDATE_COLUMN);
		if ( updateValue == null ) return updatePrefs;
		for ( String value : updateValue.split(";") ) {
			String[] prefs = value.split("=");
			if ( prefs.length == 2 ) {
				try {
					updatePrefs.put(prefs[0], Integer.valueOf(prefs[1]));
				} catch (NumberFormatException e) {
					updatePrefs.put(prefs[0], Integer.valueOf(0));					
				}
			}
		}
		return updatePrefs;
	}

	
	public void setUpdatePrefs(String objectClass, int updateType)
			throws DataException {
		Map<String,Integer> updatePrefs = this.getUpdatePrefs();
		updatePrefs.put(objectClass, Integer.valueOf(updateType));
		StringBuffer output = new StringBuffer();
		for ( Entry<String,Integer> value : updatePrefs.entrySet() ) {
			if ( value.getValue().intValue() == 0 ) continue;
			output.append(value.getKey());
			output.append("=");
			output.append(value.getValue().toString());
			output.append(";");
		}
		this.myData.setString(UPDATE_COLUMN, output.toString());
	}

	
	public int getUpdatePrefs(String objectClass) throws DataException {
		Map<String,Integer> prefs = this.getUpdatePrefs();
		Integer value = prefs.get(objectClass);
		if ( value != null )
			return value.intValue();
		return 0;
	}

	public Date getLastUpdateSent() throws DataException {
		return this.myData.getTimestamp(LAST_UPDATE);
	}

	public void setLastUpdateSent(Date time) throws DataException {
		this.myData.setTimestamp(LAST_UPDATE, time);
	}
	
	public static Map<String,Integer> getUpdateMap(String data) {
		Map<String,Integer> updatePrefs = new HashMap<String,Integer>();
		if ( data == null ) return updatePrefs;
		for ( String value : data.split(";") ) {
			String[] prefs = value.split("=");
			if ( prefs.length == 2 ) {
				updatePrefs.put(prefs[0], Integer.getInteger(prefs[1], 0));
			}
		}
		return updatePrefs;
	}

	public String getKeyForHost(String hostID) throws DataException {
		String retVal = null;
		if ( this.myID != null ) {
			try {
				PreparedStatement pSth = this.myData.prepareStatement(SQL_GET_CERT);
				pSth.setString(1, hostID);
				pSth.setString(2, this.myID);
				ResultSet result = pSth.executeQuery();
				if ( result.first() ) {
					retVal = result.getString(1);
				}
				result.close();
				pSth.close();
			} catch (SQLException e) {
				throw new DataException(e);
			}
		}
		return retVal;
	}

	public UpdateHost getHosts() throws DataException {
		SQLUpdateHost hosts = new SQLUpdateHost(this.myData);
		if ( this.myID != null ) {
			try {
				PreparedStatement aPsth = hosts.myData.prepareStatement(SQL_GET_HOSTS);
				aPsth.setString(1, this.myID);
				hosts.loadUsingPreparedStatement(aPsth);
			} catch (SQLException e) {
				throw new DataException(e);
			}
		}
		return hosts;
	}

	public void addUpdateHost(String hostID, String hostname, String publicKey) throws DataException {
		if ( this.myID != null ) {
			String keyString = null;
			try {
				PublicKey key = parsePublicKey(publicKey);
				keyString = encodePublicKey(key);
			} catch (GeneralSecurityException e) {
				throw new DataException(e);
			}
			if ( keyString != null ) {
				PreparedStatement pSth = this.myData.prepareStatement(SQL_ADD_HOST);
				try {
					pSth.setString(1, this.myID);
					pSth.setString(2, hostID);
					pSth.setString(3, hostname);
					pSth.setString(4, keyString);
					pSth.executeUpdate();
					pSth.close();
				} catch (SQLException e) {
					throw new DataException(e);
				}
			} else {
				throw new DataException("Unable to save public key.");
			}
		}
	}

	public void removeHost(String hostID) throws DataException {
		if ( this.myData != null ) {
			PreparedStatement pSth = this.myData.prepareStatement(SQL_DEL_HOST);
			try {
				pSth.setString(1, hostID);
				pSth.setString(2, this.myID);
				pSth.executeUpdate();
				pSth.close();
			} catch (SQLException e) {
				throw new DataException(e);
			}
		}
	}
	
	public static KeyFactory getKeyFactory() throws NoSuchAlgorithmException {
		if ( keyFactory == null ) {
			keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
		}
		return keyFactory;
	}
	
	public static PublicKey decodePublicKey(String keyString) throws InvalidKeySpecException, NoSuchAlgorithmException {
		X509EncodedKeySpec certSpec = new X509EncodedKeySpec(Base64.decodeBase64(keyString));
		return getKeyFactory().generatePublic(certSpec);
	}
	
	public static PrivateKey decodePrivateKey(String keyString) throws InvalidKeySpecException, NoSuchAlgorithmException {
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.decodeBase64(keyString));
		return getKeyFactory().generatePrivate(keySpec);
	}
	
	public static KeyPair decodeKeyPair(String privateKey, String publicKey) throws InvalidKeySpecException, NoSuchAlgorithmException {
		return new KeyPair(decodePublicKey(publicKey), decodePrivateKey(privateKey));
	}
	
	public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance(KEY_ALGORITHM);
		keyGen.initialize(1024);
		return keyGen.generateKeyPair();
	}
	
	public static final Pattern PATTERN_PUB_KEY = Pattern.compile("-----BEGIN ([A-Z]+) PUBLIC KEY-----(.+)-----END \\1 PUBLIC KEY-----", Pattern.MULTILINE + Pattern.DOTALL);
	public static final Pattern PATTERN_PRIV_KEY = Pattern.compile("-----BEGIN ([A-Z]+) PRIVATE KEY-----(.+)-----END \\1 PRIVATE KEY-----", Pattern.MULTILINE + Pattern.DOTALL);

	public static PublicKey parsePublicKey(String keyString) throws GeneralSecurityException {
		Matcher match = PATTERN_PUB_KEY.matcher(keyString);
		if ( match.matches() ) {
			KeyFactory kf = KeyFactory.getInstance(match.group(1));
			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.decodeBase64(match.group(2)));
			return kf.generatePublic(keySpec);
		} else {
			return decodePublicKey(keyString);
		}
	}

	public static String encodePublicKey(PublicKey key) {
		StringBuffer output = new StringBuffer("-----BEGIN ");
		output.append(key.getAlgorithm());
		output.append(" PUBLIC KEY-----\n");
		String keyString = Base64.encodeBase64String(key.getEncoded());
		int blockLen = 64; String blockString = keyString; 
		while ( blockString.length() > blockLen ) { 
			String thisLine = blockString.substring(0, blockLen); 
			blockString = blockString.substring(blockLen);
			output.append(thisLine);
			output.append("\n");
		}
		output.append(blockString);
		output.append("\n-----END ");
		output.append(key.getAlgorithm());
		output.append(" PUBLIC KEY-----");
		return output.toString();
	}
	
	public static PrivateKey parsePrivateKey(String keyString) throws GeneralSecurityException {
		Matcher match = PATTERN_PRIV_KEY.matcher(keyString);
		if ( match.matches() ) {
			KeyFactory kf = KeyFactory.getInstance(match.group(1));
			PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.decodeBase64(match.group(2)));
			return kf.generatePrivate(keySpec);
		} else {
			return decodePrivateKey(keyString);
		}
	}

	public static String encodePrivateKey(PrivateKey key) {
		StringBuffer output = new StringBuffer("-----BEGIN ");
		output.append(key.getAlgorithm());
		output.append(" PRIVATE KEY-----\n");
		String keyString = Base64.encodeBase64String(key.getEncoded());
		int blockLen = 64; String blockString = keyString; 
		while ( blockString.length() > blockLen ) { 
			String thisLine = blockString.substring(0, blockLen); 
			blockString = blockString.substring(blockLen);
			output.append(thisLine);
			output.append("\n");
		}
		output.append(blockString);
		output.append("\n-----END ");
		output.append(key.getAlgorithm());
		output.append(" PRIVATE KEY-----");
		return output.toString();
	}

	@Override
	public UpdateHost getUpdateHost(String hostID) throws DataException {
		SQLUpdateHost hosts = new SQLUpdateHost(this.myData);
		if ( this.myID != null ) {
			try {
				PreparedStatement aPsth = hosts.myData.prepareStatement(SQL_GET_HOST);
				aPsth.setString(1, this.myID);
				aPsth.setString(2, hostID);
				hosts.loadUsingPreparedStatement(aPsth);
			} catch (SQLException e) {
				throw new DataException(e);
			}
		}
		return hosts;
	}

	@Override
	public String getLastUpdateMessage() throws DataException {
		return this.myData.getString(LAST_UPDATE_MESSAGE);
	}

	@Override
	public void setLastUpdateMessage(String message) throws DataException {
		this.myData.setStringNullBlank(LAST_UPDATE_MESSAGE, message);
	}
}
