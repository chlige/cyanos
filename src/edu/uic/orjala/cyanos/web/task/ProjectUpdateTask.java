package edu.uic.orjala.cyanos.web.task;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.Random;
import java.util.TimerTask;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.net.ssl.HttpsURLConnection;
import javax.sql.DataSource;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.codec.binary.Base64OutputStream;
import org.xml.sax.SAXException;

import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.Project;
import edu.uic.orjala.cyanos.sql.SQLAssay;
import edu.uic.orjala.cyanos.sql.SQLCollection;
import edu.uic.orjala.cyanos.sql.SQLData;
import edu.uic.orjala.cyanos.sql.SQLHarvest;
import edu.uic.orjala.cyanos.sql.SQLMaterial;
import edu.uic.orjala.cyanos.sql.SQLProject;
import edu.uic.orjala.cyanos.sql.SQLStrain;
import edu.uic.orjala.cyanos.web.AppConfig;
import edu.uic.orjala.cyanos.web.AppConfigSQL;
import edu.uic.orjala.cyanos.web.servlet.ProjectServlet;
import edu.uic.orjala.cyanos.web.servlet.ProjectUpdateServlet;
import edu.uic.orjala.cyanos.xml.ProjectUpdateXMLHandler;

public class ProjectUpdateTask extends TimerTask {
	
	public static int KEY_SIZE = 1024;

	protected PrivateKey privKey = null;
	protected AppConfig config = null;
	protected final CookieManager cookies = new CookieManager();
	protected Cipher cipher = null;
	protected PublicKey masterPubKey = null;
	protected URL masterURL = null;
	protected SQLData myData = null;
	protected Project myProject = null;
	protected boolean encryptPayload = true;
	protected byte[] token = null;
	protected String transform = null;
	protected int keyLength = 0;
		
	protected Throwable error = null;
	protected boolean running = false;
	
	protected String projectID = null;
	
	protected ProjectUpdateTask() {
		// NOTHING TO DO.
	}

	public static ProjectUpdateTask createTask(String projectID) {
		ProjectUpdateTask task = new ProjectUpdateTask();
		task.projectID = projectID;
		return task;
	}
	
	@Override
	public void run() {
		Connection aConn = null;
		this.running = true;
		try {
			config = new AppConfigSQL();
			this.setupKeypair();

			if ( this.privKey != null ) {
				DataSource myDS = config.getDataSourceObject();
				aConn = myDS.getConnection();
				CookieHandler.setDefault(cookies);
				this.updateProject(aConn, this.projectID);

			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if ( aConn != null && ! aConn.isClosed() )
					aConn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		this.config = null;
		this.privKey = null;
		this.running = false;
	}
	
	public Throwable getError() {
		return this.error;
	}
	
	public boolean isRunning() { 
		return this.running;
	}
	
	protected void clearFields() {
		cipher = null;
		masterPubKey = null;
		masterURL = null;
		myData = null;
		myProject = null;
		encryptPayload = true;
		token = null;
		transform = null;
		keyLength = 0;
		
		cookies.getCookieStore().removeAll();		
	}
	
	protected void updateProject(Connection dsConn, String projectID) {
		this.clearFields();
		
		try {
			myData = new SQLData(config, dsConn, new UpdateUser(projectID));
			myProject = SQLProject.load(myData, projectID);						
			masterURL = new URL(myProject.getMasterURL().concat("/update/").concat(URLEncoder.encode(projectID, "US-ASCII")));
			masterPubKey = SQLProject.parsePublicKey(myProject.getUpdateCert()); 
			this.updateProject();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void setupKeypair() throws GeneralSecurityException {
		String keyString = config.getUpdateKey();
		
		if ( keyString != null ) {
			privKey = SQLProject.parsePrivateKey(keyString);

			/*
			String certString = config.getUpdateCert();
			X509EncodedKeySpec certSpec = new X509EncodedKeySpec(Base64.decodeBase64(certString));
			pubKey = keyFactory.generatePublic(certSpec);
			*/
		}
	}
	
	private void updateProject() throws IOException {

		try { 
			if ( ! this.getUpdateToken() ) {
				System.err.println("Unable to get update token for project update to: " + masterURL.toString());
				return;			
			}

			if ( this.encryptPayload ) {
				if ( ! this.setupCipher() ) {
					System.err.println("Unable to setup encryption for project update to: " + masterURL.toString());
					return;							
				}
			}

			this.sendUpdate();
		} catch (Exception e) {
			this.error = e;
		} finally {
			HttpURLConnection connection = (HttpURLConnection) masterURL.openConnection();		
			connection.addRequestProperty(ProjectUpdateServlet.HOST_REQ_PARAM, config.getHostUUID());

			connection.setUseCaches(false);
			connection.setDoOutput(true);

			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			connection.setRequestProperty("Content-Length", Integer.toString(ProjectUpdateServlet.PARAM_FINISH.getBytes().length));
			connection.connect();

			DataOutputStream out = new DataOutputStream(connection.getOutputStream());
			out.writeBytes(ProjectUpdateServlet.PARAM_FINISH);
			out.flush();
			out.close();
			
			connection.getContent();
			
			connection.disconnect();
		}		
	}

	private boolean setupCipher() throws InvalidKeyException, NoSuchAlgorithmException, SignatureException, UnsupportedEncodingException, IOException {
		HttpURLConnection connection = this.sendRequest(this.generateCipherRequest());
		
		InputStream in = connection.getInputStream();
		BufferedReader read = new BufferedReader(new InputStreamReader(in));
		String line;
		while ( (line = read.readLine()) != null ) {
			if ( line.startsWith("TRANSFORM:") )
				transform = line.split(":", 2)[1];
			
			if ( line.startsWith("KEY LENGTH:")) {
				String lenString = line.split(":", 2)[1];
				keyLength = Integer.parseInt(lenString);
			}
		}
		read.close();
		in.close();
		
		return ( transform != null && keyLength > 0 );
	}

	private boolean sendUpdate() throws DataException, XMLStreamException, SQLException, IOException, GeneralSecurityException, ParserConfigurationException, SAXException, EncoderException {
		boolean success = false;
		
		// Get the current data according to the SQL server.  
		// This will be used as the current update timestamp so that if users modify objects while this update is occurring those 
		// update will be sent in the next update.
		
		Date updateDate = SQLProject.SQLNow(myData);
		
		// Setup the HTTP connection to the master server.
		HttpURLConnection masterConn = this.getConnection();
		masterConn.setRequestMethod("PUT");
		masterConn.setRequestProperty("Content-type", "application/xml");

		OutputStream out;
		
		// If the cipherKey is set (i.e. not HTTPS), encrypt the payload.
		if ( encryptPayload ) {
			cipher = Cipher.getInstance(transform);
			cipher.init(Cipher.ENCRYPT_MODE, this.getCipherKey());

			// If the cipher uses an IV send it along.
			byte[] iv = cipher.getIV();
			if ( iv != null && iv.length > 0 )
				masterConn.setRequestProperty(ProjectUpdateServlet.CIPHER_IV_PARAM, Base64.encodeBase64String(iv));	
			
			out = new Base64OutputStream(masterConn.getOutputStream());
			out = new CipherOutputStream(out, cipher);
		} else {
			out = new Base64OutputStream(masterConn.getOutputStream());
		}
		
		writeXML(myData, myProject, config.getHostUUID(), out);
		
		out.flush();
		out.close();

		int status = masterConn.getResponseCode();

		if ( status == HttpURLConnection.HTTP_CREATED ) {
			String updateRequest = this.generateUpdateRequest();
			myProject.setLastUpdateSent(updateDate);
			myProject.setLastUpdateMessage("COMPLETE - SYNC");
			success = true;
			this.receiveUpdate(updateRequest);
		} else if ( status == HttpURLConnection.HTTP_ACCEPTED ) {
			String updateStatus = this.getUpdateStatus();
			
			while ( updateStatus.startsWith(ProjectUpdateServlet.TASK_STATUS_RUNNING) ) {
				try {
					Thread.sleep(10 * 1000);
				} catch (InterruptedException e) {
					// DO NOT DO ANYTHING.  Just try again.
				} 
				updateStatus = this.getUpdateStatus();
			}

			myProject.setLastUpdateMessage(updateStatus);
			
			if ( updateStatus.startsWith(ProjectUpdateServlet.TASK_STATUS_ERROR) ) {
				System.err.println(updateStatus);
			} else if (updateStatus.startsWith(ProjectUpdateServlet.TASK_STATUS_COMPLETE) ) {
				String updateRequest = this.generateUpdateRequest();
				myProject.setLastUpdateSent(updateDate);
				success = true;
				this.receiveUpdate(updateRequest);				
			}
		}

		return success;
	}
	private String getUpdateStatus() throws InvalidKeyException, NoSuchAlgorithmException, SignatureException, IOException {
		HttpURLConnection conn = this.getConnection();
		conn.setDoOutput(false);
		conn.setRequestMethod("GET");
		return this.readInput(conn);
	}
	
	private boolean receiveUpdate(String updateRequest) throws IOException, DataException, InvalidAlgorithmParameterException, EncoderException, GeneralSecurityException, ParserConfigurationException, SAXException {
		if ( updateRequest != null ) {
			HttpURLConnection conn = this.sendRequest(updateRequest);	
			InputStream in = conn.getInputStream();
			
			if ( this.verifyConnection(conn) ) {
				
				// If the payload was encrypted, decrypt					
				if ( encryptPayload ) {
					String ivString = conn.getRequestProperty(ProjectUpdateServlet.CIPHER_IV_PARAM);
					if ( ivString != null ) {
						byte[] iv = Base64.decodeBase64(ivString);				
						cipher.init(Cipher.DECRYPT_MODE, this.getCipherKey(), new IvParameterSpec(iv));
					} else
						cipher.init(Cipher.DECRYPT_MODE, this.getCipherKey());
					in = new CipherInputStream(new Base64InputStream(in), cipher);		
				} 

				SAXParserFactory factory = SAXParserFactory.newInstance();
				SAXParser saxParser = factory.newSAXParser();
				ProjectUpdateXMLHandler xmlHandler = new ProjectUpdateXMLHandler(myData);
				saxParser.parse(in, xmlHandler);
				return true;
				// TODO should report the results.
			} else {
				// TODO Should complain that the connection is invalid!!
			}
		}
		return false;
	}
	
	/**
	 * Verify the update token of the incoming connection.  
	 * The "token" sent is composed of two parts, extra bytes and a signature.
	 * 
	 * @param conn an HttpURLConnection
	 * @return true if the connection has a valid update token.
	 * @throws NoSuchAlgorithmException
	 * @throws SignatureException
	 */
	private boolean verifyConnection(HttpURLConnection conn) throws NoSuchAlgorithmException, SignatureException {
		String signature = conn.getRequestProperty(ProjectUpdateServlet.SIGNATURE_REQ_PARAM);
		String tokenExtra = conn.getRequestProperty(ProjectUpdateServlet.TOKEN_HTTP_HEADER);

		if ( signature != null && tokenExtra != null ) {
			Signature signer = Signature.getInstance(ProjectUpdateServlet.DEFAULT_SIGNATURE_ALGO + masterPubKey.getAlgorithm());

			signer.update(token);
			signer.update(Base64.decodeBase64(tokenExtra));

			return signer.verify(Base64.decodeBase64(signature));
		}
		return false;
	}

	private String generateUpdateRequest() throws DataException, UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		StringBuffer formParams = new StringBuffer(ProjectUpdateServlet.PARAM_REQ_UPDATE);

		String[] updateClasses = { ProjectServlet.UPDATE_CLASS_COLLECTION, 
				ProjectServlet.UPDATE_CLASS_STRAIN, ProjectServlet.UPDATE_CLASS_MATERIAL, ProjectServlet.UPDATE_CLASS_ASSAY};	
		
		boolean hasUpdate = false;
		
		for ( String updateClass : updateClasses ) {
			int updatePref = myProject.getUpdatePrefs(updateClass);
			if ( hasUpdatePref(updatePref, Project.UPDATE_RECEIVE) ) {
				hasUpdate = true;
				formParams.append("&" + ProjectUpdateServlet.PARAM_UPDATE_CLASS + "=");
				formParams.append(updateClass);
				if ( updatePref > Project.UPDATE_RECEIVE_LOCAL_ONLY ) {
					formParams.append("&" + ProjectUpdateServlet.PARAM_LOCAL_UPDATE + "=");
					formParams.append(updateClass);
				}
			} 
		}
		
		if ( hasUpdate ) {
			formParams.append("&" + ProjectUpdateServlet.PARAM_HOST_ID + "=");
			formParams.append(URLEncoder.encode(config.getHostUUID(),"UTF-8"));
			
			formParams.append("&" + ProjectUpdateServlet.PARAM_LAST_UPDATE + "=");
			formParams.append(URLEncoder.encode(ProjectUpdateServlet.REQ_DATE_FORMAT.format(myProject.getLastUpdateSent()), "UTF-8"));
			
			return formParams.toString();
		} else {
			return null;
		}
	}
	
	private KeyPair getDHKeys() throws NoSuchAlgorithmException {
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ProjectUpdateServlet.KEY_EXCHANGE_ALGO);
		keyGen.initialize(KEY_SIZE);
		return keyGen.generateKeyPair();
	}
	
	private String generateCipherRequest() throws UnsupportedEncodingException, NoSuchAlgorithmException {
		// Generate the HTTP request for setting up a cipher.
		StringBuffer formParams = new StringBuffer(ProjectUpdateServlet.PARAM_GET_CIPHER);

		// Send the available ciphers to the remote server.
		for ( String algo : Security.getAlgorithms("Cipher") ) {
			formParams.append("&" + ProjectUpdateServlet.PARAM_CIPHER_ALGOS + "=");
			formParams.append(URLEncoder.encode(algo, "UTF-8"));
			
			// If there is a defined max key length, include that information in the request.
			int size = Cipher.getMaxAllowedKeyLength(algo);
			if ( size < Integer.MAX_VALUE ) {
				formParams.append("&" + ProjectUpdateServlet.PARAM_KEY_SIZE);
				formParams.append(":" + algo + "=");
				formParams.append(URLEncoder.encode(Integer.toString(size), "UTF-8"));
			}
		}
		
		return formParams.toString();
	}
	
	private String generateDHRequest(KeyPair dhKey) throws NoSuchAlgorithmException, UnsupportedEncodingException, SignatureException, InvalidKeyException {
		// Sign the DH public key
		String signAlgo = ProjectUpdateServlet.DEFAULT_SIGNATURE_ALGO + privKey.getAlgorithm();
		Signature signer = Signature.getInstance(signAlgo);
		signer.initSign(privKey);
		signer.update(dhKey.getPublic().getEncoded());			
		
		// Generate the HTTP request for the key exchange.
		StringBuffer formParams = new StringBuffer(ProjectUpdateServlet.PARAM_KEY_EXCHANGE);
		formParams.append("&" + ProjectUpdateServlet.PARAM_HOST_ID + "=");
		formParams.append(URLEncoder.encode(config.getHostUUID(),"UTF-8"));
		
		// Send the DH public key 
		formParams.append("&" + ProjectUpdateServlet.PARAM_DH_KEY + "=");
		formParams.append(Base64.encodeBase64URLSafeString(dhKey.getPublic().getEncoded()));

		// Send the signature for the DH public key
		formParams.append("&" + ProjectUpdateServlet.PARAM_SIGNATURE + "=");
		formParams.append(URLEncoder.encode(Base64.encodeBase64String(signer.sign()), "UTF-8"));

		// Send the algorigthm used to sign the public key
		formParams.append("&" + ProjectUpdateServlet.PARAM_SIGNATURE_ALGO + "=");
		formParams.append(URLEncoder.encode(signAlgo, "UTF-8"));
		
//		formParams.append("&exchangeAlgo=");
//		formParams.append(URLEncoder.encode(ProjectUpdateServlet.KEY_EXCHANGE_ALGO, "UTF-8"));
		
		return formParams.toString();

	}
	
	private String readInput(HttpURLConnection conn) throws IOException {
		InputStream in = conn.getInputStream();
		BufferedReader read = new BufferedReader(new InputStreamReader(in));
		String line;
		StringBuffer response = new StringBuffer();
		while ( (line = read.readLine()) != null ) {
			response.append(line);
			response.append('\r');
		}
		read.close();
		return response.toString();
	}
	
	/**
	 * Get a HTTP connection to the master server
	 * 
	 * @return
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws SignatureException
	 */
	private HttpURLConnection getConnection() throws IOException, NoSuchAlgorithmException, InvalidKeyException, SignatureException {
		HttpURLConnection connection = (HttpURLConnection) masterURL.openConnection();		
		if ( token != null && token.length > 0 ) {			
			byte[] tokenExtra = new byte[ProjectUpdateServlet.TOKEN_SIZE];
			
			Random random = new Random();
			random.nextBytes(tokenExtra);	

			Signature signer = Signature.getInstance(ProjectUpdateServlet.DEFAULT_SIGNATURE_ALGO + privKey.getAlgorithm());
			signer.initSign(privKey); 
			signer.update(token);
			signer.update(tokenExtra);
			
			connection.addRequestProperty(ProjectUpdateServlet.SIGNATURE_REQ_PARAM, Base64.encodeBase64String(signer.sign()));
			connection.addRequestProperty(ProjectUpdateServlet.TOKEN_HTTP_HEADER, Base64.encodeBase64String(tokenExtra));
		}
		connection.addRequestProperty(ProjectUpdateServlet.HOST_REQ_PARAM, config.getHostUUID());

		connection.setUseCaches(false);
		connection.setDoInput(true);
		connection.setDoOutput(true);
		return connection;
	}
	
	/**
	 * Send a request to the master server and return the resulting HttpURLConnection.
	 * 
	 * @param request
	 * @return
	 * @throws IOException
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws SignatureException
	 */
	private HttpURLConnection sendRequest(String request) throws IOException, InvalidKeyException, NoSuchAlgorithmException, SignatureException {
		HttpURLConnection connection = this.getConnection();
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		connection.setRequestProperty("Content-Length", Integer.toString(request.getBytes().length));
		
		DataOutputStream out = new DataOutputStream(connection.getOutputStream());
		out.writeBytes(request);
		out.flush();
		out.close();

		return connection;
	}
	
	/**
	 * Parse the DH public key from the HTTP response
	 * 
	 * @param response
	 * @return
	 */
	private String parseDHKey(String response) {
		// Parse out the DH key
		int start = response.indexOf(ProjectUpdateServlet.BEGIN_DH_BLOCK);
		int stop = response.indexOf(ProjectUpdateServlet.END_DH_BLOCK);

		return response.substring(start + ProjectUpdateServlet.BEGIN_DH_BLOCK.length(), stop);
	}
	
	/**
	 * Generate a cipher key from the update token
	 * 
	 * @return
	 * @throws IOException
	 * @throws EncoderException
	 * @throws GeneralSecurityException
	 */
	private SecretKey getCipherKey() throws IOException, EncoderException, GeneralSecurityException {
		if ( token == null || token.length < 1 )
			this.getUpdateToken();
		
		// Get the secret key from the security token
		return ProjectUpdateServlet.getSecretKey(token, transform, keyLength);
	}
		
	
	/**
	 * Get an update token from the master server.  This token is generated using a Diffie-Hellman key exchange. 
	 * The resulting DH public key is signed using the local server public key and the DH public key from the master server is verified.
	 * The resulting shared secret from the DH key exchange is used to generate the update token as well as the cipher key.
	 * 
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws UnsupportedEncodingException
	 * @throws SignatureException
	 * @throws IOException
	 * @throws InvalidKeySpecException
	 */
	private boolean getUpdateToken() throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException, SignatureException, IOException, InvalidKeySpecException {
		// Generate DiffieHellman keys
		KeyPair dhKey = this.getDHKeys();
		
		HttpURLConnection connection = this.sendRequest(this.generateDHRequest(dhKey));
		
		// If the connection is a HTTPS connection do not encrypt the payload.
		if ( connection instanceof HttpsURLConnection )
			encryptPayload = false;
		
		// read the response from the master server.
		String response = this.readInput(connection);		
		
		// Parse out the DH public key
		String certString = this.parseDHKey(response);
		
		// Parse out the signature
		String sigString = ProjectUpdateServlet.parseSignature(response.toString(), masterPubKey.getAlgorithm());
		
		// Verify the signature
		Signature signer = Signature.getInstance(ProjectUpdateServlet.DEFAULT_SIGNATURE_ALGO + masterPubKey.getAlgorithm());
		signer.initVerify(masterPubKey);
		signer.update(Base64.decodeBase64(certString));
		
		// If the signature is invalid return without generating the security token.
		if ( ! signer.verify(Base64.decodeBase64(sigString)) ) {
			return false;
		}

		// Generate the DH public key from the encoded key in the response.
		KeyFactory keyfactory = KeyFactory.getInstance(ProjectUpdateServlet.KEY_EXCHANGE_ALGO);
		X509EncodedKeySpec certSpec = new X509EncodedKeySpec(Base64.decodeBase64(certString));
		PublicKey passedKey = keyfactory.generatePublic(certSpec);

		// Generate DiffieHellman Key Agreement 
		KeyAgreement ka = KeyAgreement.getInstance(ProjectUpdateServlet.KEY_EXCHANGE_ALGO);
		ka.init(dhKey.getPrivate());
		ka.doPhase(passedKey, true);

		// Set the security token to the shared secret
		this.token = ka.generateSecret();
		return true;
	}

	protected static boolean hasUpdatePref(int bits, int desired) {
		int myBit = bits & desired;
		return (myBit == desired);
	}
	
	static ByteArrayOutputStream generateOutput(SQLData myData, Project project) throws DataException, XMLStreamException, SQLException, IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		writeXML(myData, project, null, out);
		return out;
	}
	
	/**
	 * Write an update XML to the specified OutputStream.
	 * 
	 * @param myData an SQLData source
	 * @param project a Project to generate the update.
	 * @param hostID the host ID of the host generating the update.
	 * @param out OutputStream to write the XML data to.
	 * @throws DataException
	 * @throws XMLStreamException
	 * @throws SQLException
	 * @throws IOException
	 */
	public static void writeXML(SQLData myData, Project project, String hostID, OutputStream out) throws DataException, XMLStreamException, SQLException, IOException {
		Date lastUpdate = project.getLastUpdateSent();
		
		XMLOutputFactory xof = XMLOutputFactory.newInstance();
		XMLStreamWriter xtw = xof.createXMLStreamWriter(out, "UTF-8");

		xtw.writeStartDocument("UTF-8", "1.0");
		xtw.writeStartElement(ProjectUpdateXMLHandler.ELEMENT_PROJECT);
		xtw.writeAttribute(SQLProject.ID_COLUMN, project.getID());
		if ( hostID != null )
			xtw.writeAttribute(ProjectUpdateXMLHandler.ATTR_HOST_ID, hostID);
		
		if ( hasUpdatePref(project.getUpdatePrefs(ProjectServlet.UPDATE_CLASS_COLLECTION), Project.UPDATE_SEND) )
			ProjectUpdateXMLHandler.writeCollections(SQLCollection.loadForProjectLastUpdated(myData, project.getID(), lastUpdate), xtw);
		
		if ( hasUpdatePref(project.getUpdatePrefs(ProjectServlet.UPDATE_CLASS_STRAIN), Project.UPDATE_SEND) )
			ProjectUpdateXMLHandler.writeStrains(SQLStrain.loadForProjectLastUpdated(myData, project.getID(), lastUpdate), xtw);

		if ( hasUpdatePref(project.getUpdatePrefs(ProjectServlet.UPDATE_CLASS_MATERIAL), Project.UPDATE_SEND) ) {
			ProjectUpdateXMLHandler.writeHarvests(SQLHarvest.loadForProjectLastUpdated(myData, project.getID(), lastUpdate), xtw);
			ProjectUpdateXMLHandler.writeMaterials(SQLMaterial.loadForProjectLastUpdated(myData, project.getID(), lastUpdate), xtw);				
		}

		if ( hasUpdatePref(project.getUpdatePrefs(ProjectServlet.UPDATE_CLASS_ASSAY), Project.UPDATE_SEND) )
			ProjectUpdateXMLHandler.writeAssays(SQLAssay.loadForProjectLastUpdated(myData, project.getID(), lastUpdate), xtw);
		
		xtw.writeEndElement();
		xtw.writeEndDocument();

		out.flush();
	}
}