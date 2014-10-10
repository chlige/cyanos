package edu.uic.orjala.cyanos.web.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyAgreement;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.NullCipher;
import javax.crypto.SecretKey;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Base64InputStream;
import org.xml.sax.InputSource;
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
import edu.uic.orjala.cyanos.web.listener.AppConfigListener;
import edu.uic.orjala.cyanos.web.task.UpdateUser;
import edu.uic.orjala.cyanos.xml.ProjectUpdateXMLHandler;

/**
 * Servlet implementation class ProjectUpdateServlet
 */
public class ProjectUpdateServlet extends ServletObject {

	public static final String TASK_STATUS_ERROR = "ERROR: ";
	public static final String TASK_STATUS_COMPLETE = "COMPLETE";
	public static final String TASK_STATUS_RUNNING = "RUNNING";

	class UpdateTask extends Thread {

		private SQLData myData;
		private String hostID;
		private File xmlFile;
		private Throwable error = null;
		private boolean running = false;

		UpdateTask(File xmlFile, SQLData data, String hostID) {
			super();
			this.xmlFile = xmlFile;
			this.myData = data;
			this.hostID = hostID;
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {
			try {
				running = true;
				InputStream inputStream= new FileInputStream(xmlFile);
				Reader reader = new InputStreamReader(inputStream,"UTF-8");
				InputSource in = new InputSource(reader);
				in.setEncoding("UTF-8");
				
				SAXParserFactory factory = SAXParserFactory.newInstance();
				SAXParser saxParser = factory.newSAXParser();
				ProjectUpdateXMLHandler xmlHandler = new ProjectUpdateXMLHandler(myData, hostID);
				saxParser.parse(in, xmlHandler);	
			} catch (ParserConfigurationException e) {
				error = e;
			} catch (SAXException e) {
				error = e;
			} catch (DataException e) {
				error = e;
			} catch (IOException e) {
				error = e;
			} finally {
				running = false;
				xmlFile.delete();
				System.err.print("XML PARSING ENDED.");
			}
		}
		
		Throwable getError() {
			return error;
		}
		
		boolean isRunning() {
			return running;
		}

	}
	
	private static final long serialVersionUID = 9070569605942275345L;
	
	
	private static final String SESS_ATTR_CIPHER_TRANSFORM = "UPDATE_CIPHER_TRANSFORM";
	private static final String SESS_ATTR_CIPHER_KEY_SIZE = "UPDATE_CIPHER_KEY_SIZE";
	private static final String SESS_ATTR_TOKEN = "UPDATE_TOKEN";
	private static final String SESS_ATTR_UPDATE_TASK = "UPDATE_TASK";
	
	public static final String DEFAULT_SIGNATURE_ALGO = "SHA1with";
	public static final String KEY_ALGORITHM = "DSA";

	public static final String SIGNATURE_REQ_PARAM = "X-cyanos-update-signature";
	public static final String HOST_REQ_PARAM = "X-cyanos-update-host";
	public static final String CIPHER_IV_PARAM = "X-cyanos-update-iv";
	public static final String TOKEN_HTTP_HEADER = "X-cyanos-update-token";

	public static final String KEY_EXCHANGE_ALGO = "DH";
	
	public static final String BEGIN_DH_BLOCK = "-----BEGIN DH PUBLIC KEY-----";
	public static final String END_DH_BLOCK = "-----END DH PUBLIC KEY-----";
	
	public static final String BEGIN_SIG_FORMAT = "-----BEGIN %s SIGNATURE-----";
	public static final String END_SIG_FORMAT = "-----END %s SIGNATURE-----";
	
	public static final Pattern PATTERN_SIGNATURE = Pattern.compile(".*-----BEGIN ([A-Z]+) SIGNATURE-----(.+)-----END \\1 SIGNATURE-----.*", Pattern.MULTILINE + Pattern.DOTALL);
	
	private static final String[] ALGORITHMS = { "AES", "BLOWFISH", "ARCFOUR", "DESede", "DES", "RC2" };

	public static final String PARAM_KEY_EXCHANGE = "keyExchange";
	public static final String PARAM_REQ_UPDATE = "getUpdate";
	public static final String PARAM_FINISH = "finishUpdate";

	public static final String PARAM_SIGNATURE = "signature";
	public static final String PARAM_SIGNATURE_ALGO = "signAlgo";
	public static final String PARAM_DH_KEY = "dhkey";
	public static final String PARAM_HOST_ID = "hostid";
	public static final String PARAM_UPDATE_CLASS = "updateClass";
	public static final String PARAM_LOCAL_UPDATE = "localUpdate";
	public static final String PARAM_TOKEN = "token";
	public static final String PARAM_LAST_UPDATE = "lastUpdated";
	public static final String PARAM_CIPHER_TRANSFORM = "transform";
	
	// Parameters for a "GET CIPHER" request.
	public static final String PARAM_GET_CIPHER = "getCipher";
	public static final String PARAM_CIPHER_ALGOS = "cipher";
	public static final String PARAM_KEY_SIZE = "keySize";
	
	private static final String CIPHER_TRANSFORM = "/CBC/PKCS5Padding";
	
	// The max inactive interval for update sessions.  5 minutes.
	private static final int MAX_INACTIVE_INTERVAL = 5 * 60;
	
	// The extra token is 256 bits in length.
	public static final int TOKEN_SIZE = 256 / 8;
	
	// Maximum key length (in bits) for the cipher key.
	public static final int MAX_KEY_LENGTH = 256;
	
	
	public static final SimpleDateFormat REQ_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");



	
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if ( AppConfigListener.isUpgradeInstall() ) 
			return;

		String projectID = request.getPathInfo();
		Project project = null;
		SQLData myData = null;

		if ( projectID != null && projectID.length() > 1 ) {
			projectID = projectID.substring(1);
			try {
				myData = new SQLData(this.getAppConfig(), AppConfigListener.getDBConnection(), new UpdateUser(projectID));
				project = SQLProject.load(myData, projectID);
			} catch (SQLException e) {
				throw new ServletException(e);
			} catch (DataException e) {
				throw new ServletException(e);
			}
		}
			
		if ( request.getSession().getAttribute(SESS_ATTR_UPDATE_TASK) != null ) {
			response.setContentType("text/plain");
			UpdateTask task = (UpdateTask) request.getSession().getAttribute(SESS_ATTR_UPDATE_TASK);
			PrintWriter out = response.getWriter();
			if ( task.isRunning() ) {
				out.println(TASK_STATUS_RUNNING);
			} else if ( task.getError() != null ) {
				request.getSession().removeAttribute(SESS_ATTR_UPDATE_TASK);
				Throwable error = task.getError();
				out.print(TASK_STATUS_ERROR);
				out.println(error.getLocalizedMessage());
				error.printStackTrace();
			} else {
				request.getSession().removeAttribute(SESS_ATTR_UPDATE_TASK);
				out.println(TASK_STATUS_COMPLETE);
			}
		}
	}
	
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if ( AppConfigListener.isUpgradeInstall() ) 
			return;

		String projectID = request.getPathInfo();
		Project project = null;
		SQLData myData = null;

		if ( projectID != null && projectID.length() > 1 ) {
			projectID = projectID.substring(1);
			try {
				myData = new SQLData(this.getAppConfig(), AppConfigListener.getDBConnection(), new UpdateUser(projectID));
				project = SQLProject.load(myData, projectID);
			} catch (SQLException e) {
				new ServletException(e);
			} catch (DataException e) {
				new ServletException(e);
			}
		}

		response.setContentType("text/plain");

		try {
			if ( request.getParameter(PARAM_KEY_EXCHANGE) != null ) {
				this.doKeyExchange(request, response, project);
			} else if ( request.getParameter(PARAM_REQ_UPDATE) != null ) {
				this.sendUpdate(request, response, myData, project);
			} else if ( request.getParameter(PARAM_GET_CIPHER) != null ) {
				this.setupCipher(request, response, project);
			} else if ( request.getParameter(PARAM_FINISH) != null ) {
				request.getSession().invalidate();
			}
		} catch (Exception e) {
			this.handleException(response, e);
		}
	}

	private void handleException(HttpServletResponse response, Exception e) throws IOException {
		response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getLocalizedMessage());
		PrintWriter out = response.getWriter();
		e.printStackTrace(out);
	}

	/**
	 * @see HttpServlet#doPut(HttpServletRequest, HttpServletResponse)
	 */
	protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if ( AppConfigListener.isUpgradeInstall() ) 
			return;
		
		String projectID = request.getPathInfo();
		Project project = null;
		SQLData myData = null;

		if ( projectID != null && projectID.length() > 1 ) {
			projectID = projectID.substring(1);
			try {
				myData = new SQLData(this.getAppConfig(), AppConfigListener.getDBConnection(), new UpdateUser(projectID));
				project = SQLProject.load(myData, projectID);
			} catch (SQLException e) {
				throw new ServletException(e);
			} catch (DataException e) {
				throw new ServletException(e);
			}
		}
		String hostID = request.getHeader(HOST_REQ_PARAM); 
		
		try {
			if ( ! this.verifyToken(request, project) ) {
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
				System.err.println("Failure to verify security token of update from " + hostID);
				return;
			}
		} catch (GeneralSecurityException e) {
			this.handleException(response, e);
			return;
		} catch (DataException e) {
			this.handleException(response, e);
			return;
		}
		
		InputStream in = request.getInputStream();

		try {
			// Setup the decryption stream.
			in = new CipherInputStream(new Base64InputStream(in), this.getDecryptCipher(request));
			this.log("Project update recieved from " + hostID);
			
			File xmlFile = File.createTempFile(String.format("cyanos-update-%s-%s-", projectID, hostID), ".xml");
			FileOutputStream fileOut = new FileOutputStream(xmlFile);
			
			this.log("XML File: " + xmlFile.getAbsolutePath());
			
			byte[] buffer = new byte[4096];
			int bytesRead;
			while ( (bytesRead = in.read(buffer)) != -1 ) {
				fileOut.write(buffer, 0, bytesRead);
			}
			fileOut.flush();
			fileOut.close();

			UpdateTask task = new UpdateTask(xmlFile, myData, hostID);
			request.getSession().setAttribute(SESS_ATTR_UPDATE_TASK, task);
			task.start();
			response.setStatus(HttpServletResponse.SC_ACCEPTED);
			
		} catch (GeneralSecurityException e) {
			this.handleException(response, e);
		} finally {
			in.close();
		}
	}

	/**
	 * @param project
	 * @param hostID
	 * @return
	 * @throws DataException
	 * @throws GeneralSecurityException
	 */
	private PublicKey getPublicKey(Project project, String hostID) throws DataException, GeneralSecurityException {
		String keyString = project.getKeyForHost(hostID);
		return SQLProject.parsePublicKey(keyString);
	}

	/**
	 * @return
	 * @throws GeneralSecurityException
	 */
	private KeyPair getKeypair() throws GeneralSecurityException {
		AppConfig config = this.getAppConfig();
		String keyString = config.getUpdateKey();

		if ( keyString != null ) {
			String certString = config.getUpdateCert();
			return SQLProject.decodeKeyPair(keyString, certString);
		}
		return null;
	}

	/**
	 * @param request
	 * @param response
	 * @param myData
	 * @param project
	 * @throws IOException
	 * @throws SQLException 
	 * @throws DataException 
	 * @throws XMLStreamException 
	 * @throws GeneralSecurityException 
	 */
	private void sendUpdate(HttpServletRequest request, HttpServletResponse response, SQLData myData, Project project) throws IOException, XMLStreamException, DataException, SQLException, GeneralSecurityException {

		// Verify if the security token is valid.  If not set the HTTP status code to FORBIDDEN (505) and return.
		if ( ! this.verifyToken(request, project) ) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return;
		}
		
		Cipher cipher = this.getEncryptCipher(request);
		response.setContentType("application/other");
		OutputStream out = new CipherOutputStream(response.getOutputStream(), cipher);
		this.writeUpdateXML(request, myData, project, out);

		// Send the signature of the security token.  May not be necessary but, a good idea?
		this.signToken(request, response);

		byte[] iv = cipher.getIV();

		// Send the IV if the cipher has one.
		if ( iv != null && iv.length > 0 )
			response.addHeader(CIPHER_IV_PARAM, Base64.encodeBase64String(iv));

		out.flush();
		out.close();
	}

	/**
	 * Verify if the update token is correct.  The slave server will send a set of random bytes in the TOKEN_HTTP_HEADER and sign a combination of the 
	 * generated secret key and the random bytes.  The resulting signature will be send as the SIGNATURE_REQ_PARAM header.
	 * 
	 * @param request
	 * @param project
	 * @return
	 * @throws GeneralSecurityException 
	 * @throws DataException 
	 */
	private boolean verifyToken(HttpServletRequest request, Project project) throws DataException, GeneralSecurityException {
		String tokenSignature = request.getHeader(SIGNATURE_REQ_PARAM);
		String hostID = request.getHeader(HOST_REQ_PARAM);

		PublicKey publicKey = this.getPublicKey(project, hostID);
		Signature signer = Signature.getInstance(DEFAULT_SIGNATURE_ALGO + publicKey.getAlgorithm());
		signer.initVerify(publicKey);

		String token = (String) request.getSession().getAttribute(SESS_ATTR_TOKEN);
		String tokenExtra = request.getHeader(TOKEN_HTTP_HEADER);

		if ( token != null ) {
			signer.update(Base64.decodeBase64(token));
			signer.update(Base64.decodeBase64(tokenExtra));
			return signer.verify(Base64.decodeBase64(tokenSignature));
		}			

		return false;
	}
	
	/**
	 * Sign the security token and add to the response.
	 * 
	 * @param request
	 * @param response
	 * @throws NoSuchAlgorithmException
	 * @throws SignatureException
	 * @throws InvalidKeyException
	 * @throws InvalidKeySpecException
	 */
	private void signToken(HttpServletRequest request, HttpServletResponse response) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException, InvalidKeySpecException {
		PrivateKey privateKey = this.getPrivateKey();
		Signature signer = Signature.getInstance(DEFAULT_SIGNATURE_ALGO + privateKey.getAlgorithm());
		signer.initSign(privateKey);

		String token = (String) request.getSession().getAttribute(SESS_ATTR_TOKEN);

		byte[] tokenExtra = new byte[TOKEN_SIZE];
		
		Random random = new Random();
		random.nextBytes(tokenExtra);	
		
		if ( token != null ) {
			signer.update(Base64.decodeBase64(token));
			signer.update(tokenExtra);
			
			response.addHeader(SIGNATURE_REQ_PARAM, Base64.encodeBase64String(signer.sign()));
			response.addHeader(TOKEN_HTTP_HEADER, Base64.encodeBase64String(tokenExtra));
		}	
	}

	/**
	 * @param request
	 * @param myData
	 * @param project
	 * @param out
	 * @throws XMLStreamException
	 * @throws DataException
	 * @throws SQLException
	 * @throws IOException
	 */
	private void writeUpdateXML(HttpServletRequest request, SQLData myData, Project project, OutputStream out) throws XMLStreamException, DataException, SQLException, IOException {
		String hostID = request.getParameter(PARAM_HOST_ID);		
		String lastUpdateString = request.getParameter(PARAM_LAST_UPDATE);
		Calendar epoch = Calendar.getInstance();
		epoch.setTimeInMillis(0);
		Date lastUpdate = epoch.getTime();
		try {
			lastUpdate = REQ_DATE_FORMAT.parse(lastUpdateString);
		} catch (ParseException e) {
			// DO nothing right now.  Just print the error and use Jan 1, 1970 as the lastUpdate.
			e.printStackTrace();
		}
		
		XMLOutputFactory xof = XMLOutputFactory.newInstance();
		XMLStreamWriter xtw = xof.createXMLStreamWriter(out);

		xtw.writeStartDocument();
		xtw.writeStartElement(ProjectUpdateXMLHandler.ELEMENT_PROJECT);
		xtw.writeAttribute(SQLProject.ID_COLUMN, project.getID());
		
		String myID = this.getLocalUUID();
		if ( myID != null )
			xtw.writeAttribute(ProjectUpdateXMLHandler.ATTR_HOST_ID, myID);
		
		String[] updates = request.getParameterValues(PARAM_UPDATE_CLASS);
		Arrays.sort(updates);
		
		String[] localUpdates = request.getParameterValues(PARAM_LOCAL_UPDATE);
		Arrays.sort(localUpdates);
		
		if ( Arrays.binarySearch(updates, ProjectServlet.UPDATE_CLASS_COLLECTION) > -1 ) {
			ProjectUpdateXMLHandler.writeCollections(SQLCollection.loadForProjectLastUpdated(myData, project.getID(), lastUpdate, hostID), xtw);
		}
		
		if ( Arrays.binarySearch(updates, ProjectServlet.UPDATE_CLASS_STRAIN) > -1 )
			ProjectUpdateXMLHandler.writeStrains(SQLStrain.loadForProjectLastUpdated(myData, project.getID(), lastUpdate, hostID), xtw);

		if ( Arrays.binarySearch(updates, ProjectServlet.UPDATE_CLASS_MATERIAL) > -1 ) {
			ProjectUpdateXMLHandler.writeHarvests(SQLHarvest.loadForProjectLastUpdated(myData, project.getID(), lastUpdate, hostID), xtw);
			ProjectUpdateXMLHandler.writeMaterials(SQLMaterial.loadForProjectLastUpdated(myData, project.getID(), lastUpdate, hostID), xtw);				
		}

		if ( Arrays.binarySearch(updates, ProjectServlet.UPDATE_CLASS_ASSAY) > -1 )
			ProjectUpdateXMLHandler.writeAssays(SQLAssay.assaysForProjectLastUpdated(myData, project.getID(), lastUpdate, hostID), xtw);
		
		xtw.writeEndElement();
		xtw.writeEndDocument();
	}
	
	/**
	 * This method performs a key exchange with the requesting slave system.  
	 * Typically the key exchange will utilize a Diffie-Hellman exchange protocol and the DH public keys sent will be signed by each system using the 
	 * DSA key pair of the system.
	 * The resulting key will be used for encryption, if the connection does not use SSL, as well as a session token.
	 * In order for the session token to be accepted, the slave system must sign the token and append the signature with each request for the session.
	 * 
	 * @param request
	 * @param response
	 * @param project
	 * @throws GeneralSecurityException 
	 * @throws IOException 
	 * @throws DataException 
	 */
	private void doKeyExchange(HttpServletRequest request, HttpServletResponse response, Project project) throws GeneralSecurityException, IOException, DataException {
		// Get the key pair for the local server.
		KeyPair keypair = this.getKeypair();

		// If the key pair does not exists, return with a failure code.  
		// Will not be able to perform a key exchange.
		if ( keypair == null ) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Cannot load keypair");
			return;
		}

		// Get public key for host
		String hostID = request.getParameter(PARAM_HOST_ID);
		PublicKey hostKey = this.getPublicKey(project, hostID);

		// Read incoming DH public key & signature
		String certString = request.getParameter(PARAM_DH_KEY);
		byte[] certBytes = Base64.decodeBase64(certString);

		String signString = request.getParameter(PARAM_SIGNATURE);
		byte[] signBytes = Base64.decodeBase64(signString);

		// Get the signature algorithm, e.g. SHA1withDSA, MD5withRSA, SHA1withECDSA etc.
		// If the algorithm information was not sent, assume the default SHA1with<key algorithm>
		String signatureAlgo = request.getParameter(PARAM_SIGNATURE_ALGO);
		if ( signatureAlgo == null )
			signatureAlgo = DEFAULT_SIGNATURE_ALGO + hostKey.getAlgorithm();

		// Verify the signature of the incoming DH public key
		Signature signer = Signature.getInstance(signatureAlgo);
		signer.initVerify(hostKey);
		signer.update(certBytes);
		if ( ! signer.verify(signBytes) ) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		// Set the key factory to perform the DH key exchange and decode the DH public key sent in the request.
		KeyFactory keyFactory = KeyFactory.getInstance(KEY_EXCHANGE_ALGO);				
		X509EncodedKeySpec certSpec = new X509EncodedKeySpec(certBytes);
		PublicKey passedKey = keyFactory.generatePublic(certSpec);

		// Setup the key pair generator to make the local DH key pair.
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance(KEY_EXCHANGE_ALGO);

		if ( passedKey instanceof DHPublicKey ) {
			DHParameterSpec specs = ((DHPublicKey)passedKey).getParams();
			keyGen.initialize(specs);
		} else {
			keyGen.initialize(1024);
		}

		// Generate DH key pair.
		KeyPair dhKey = keyGen.generateKeyPair();

		// Setup to sign the local DH public key to be sent to the slave system.
		signer = Signature.getInstance(DEFAULT_SIGNATURE_ALGO + keypair.getPrivate().getAlgorithm());
		signer.initSign(keypair.getPrivate());
		signer.update(dhKey.getPublic().getEncoded());	

		// Setup the KeyAgreement object to perform the DH key exchange
		KeyAgreement ka = KeyAgreement.getInstance(KEY_EXCHANGE_ALGO);
		ka.init(dhKey.getPrivate());
		ka.doPhase(passedKey, true);

		// Send the local DH public key and associated signature to the slave system.
		response.setContentType("text/plain");
		PrintWriter out = response.getWriter();
		out.println(BEGIN_DH_BLOCK);
		out.println(Base64.encodeBase64String(dhKey.getPublic().getEncoded()));
		out.println(END_DH_BLOCK);
		out.println(String.format(BEGIN_SIG_FORMAT, keypair.getPublic().getAlgorithm()));
		out.println(Base64.encodeBase64String(signer.sign()));
		out.println(String.format(END_SIG_FORMAT, keypair.getPublic().getAlgorithm()));

		// Get the cipher transform and store in the session.
		String transform = request.getParameter(PARAM_CIPHER_TRANSFORM);
		HttpSession session = request.getSession();

		// Set the session inactive timeout to 5 minutes.  May be too long.
		session.setMaxInactiveInterval(MAX_INACTIVE_INTERVAL);
		session.setAttribute(SESS_ATTR_CIPHER_TRANSFORM, transform);

		// Setup symmetric key
		byte[] secret = ka.generateSecret();

		// Store the secret key (update token and cipher key)
		session.setAttribute(SESS_ATTR_TOKEN, Base64.encodeBase64String(secret));
	}
	
	private void setupCipher(HttpServletRequest request, HttpServletResponse response, Project project) throws IOException, DataException, GeneralSecurityException {
		if ( ! this.verifyToken(request, project) )
			return;
		
		String[] algos = request.getParameterValues(PARAM_CIPHER_ALGOS);
		Arrays.sort(algos);
		
		String transform = null;
		int keylen = MAX_KEY_LENGTH;
		
		for ( String selected: ALGORITHMS ) {
			if ( Arrays.binarySearch(algos, selected) > -1 ) {
				String aCipher = selected + CIPHER_TRANSFORM;
				try { 
					int thisLen = Cipher.getMaxAllowedKeyLength(aCipher);
					String lenString = request.getParameter(PARAM_KEY_SIZE + ":" + selected);
					if ( lenString != null && lenString.length() > 0 ) {
						int keySize = Integer.parseInt(lenString);
						if ( keySize > 0 && keySize < thisLen && keySize < keylen ) {
							keylen = keySize;
						} 
					} 
					if ( thisLen < keylen ) {
						keylen = thisLen;
					}
					transform = aCipher;
					break;
				} catch (NoSuchAlgorithmException e) {
					// Print the error and try the next one.
					e.printStackTrace();
				} catch (NumberFormatException e) {
					// Print the error and try the next one.
					e.printStackTrace();					
				}
			}
		}
		
		if ( transform != null ) {
			request.getSession().setAttribute(SESS_ATTR_CIPHER_TRANSFORM, transform);
			request.getSession().setAttribute(SESS_ATTR_CIPHER_KEY_SIZE, new Integer(keylen));

			response.setContentType("text/plain");
			PrintWriter out = response.getWriter();
			
			out.print("TRANSFORM:");
			out.println(transform);
			out.print("KEY LENGTH:");
			out.println(keylen);

			response.setStatus(HttpServletResponse.SC_OK);
			
			out.flush();
			out.close();
			
		} else {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Cannot setup cipher");
		}
	}


	/**
	 * @param session
	 * @param transform
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	private SecretKey getSecretKey(HttpSession session) throws NoSuchAlgorithmException {
		String encodeSecret = (String) session.getAttribute(SESS_ATTR_TOKEN);	
		String transform = (String) session.getAttribute(SESS_ATTR_CIPHER_TRANSFORM);
		Integer keySize = (Integer) session.getAttribute(SESS_ATTR_CIPHER_KEY_SIZE);
		
		if ( encodeSecret != null ) 
			return getSecretKey(Base64.decodeBase64(encodeSecret), transform, keySize);
		return null;
	}
	
	
	public static SecretKey getSecretKey(byte[] secret, String transform, int size) throws NoSuchAlgorithmException {
		if ( transform == null )
			return null;

		String cipherAlgo = transform.split("/",2)[0];
		
		return new SecretKeySpec(secret, 0, size / 8, cipherAlgo);
	}
	
	/**
	 * Get a decryption cipher for the associated HttpServletRequest
	 * 
	 * @param request
	 * @return
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidAlgorithmParameterException
	 */
	private Cipher getDecryptCipher(HttpServletRequest request) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException {
		return this.getCipher(request, Cipher.DECRYPT_MODE, request.getHeader(CIPHER_IV_PARAM));
	}
	
	/**
	 * Get an encryption cipher for the associated HttpServletRequest
	 * 
	 * @param request
	 * @return
	 * @throws InvalidKeyException
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidAlgorithmParameterException
	 */
	private Cipher getEncryptCipher(HttpServletRequest request) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidAlgorithmParameterException {
		return this.getCipher(request, Cipher.ENCRYPT_MODE, null);
	}

	/**
	 * Get the cipher for the associated HttpServletRequest
	 * 
	 * @param request
	 * @param cipherMode
	 * @param ivString
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws InvalidAlgorithmParameterException
	 */
	private Cipher getCipher(HttpServletRequest request, int cipherMode, String ivString) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
		HttpSession session = request.getSession();
		String transform = (String) session.getAttribute(SESS_ATTR_CIPHER_TRANSFORM);
		
		SecretKey key = this.getSecretKey(session);
		
		if ( transform != null ) {
			Cipher cipher = Cipher.getInstance(transform);
			if ( ivString != null )
				cipher.init(cipherMode, key, new IvParameterSpec(Base64.decodeBase64(ivString)));
			else 
				cipher.init(cipherMode, key);
			return cipher;
		} else { 
			return new NullCipher();
		}
	}
	
	/**
	 * Get the UUID of the local CYANOS server
	 * 
	 * @return
	 */
	private String getLocalUUID() {
		AppConfig config = this.getAppConfig();
		if ( config != null ) {
			return config.getHostUUID();
		}
		return null;
	}
	
	/**
	 * Get the private key of the local server.
	 * 
	 * @return
	 * @throws InvalidKeySpecException
	 * @throws NoSuchAlgorithmException
	 */
	private PrivateKey getPrivateKey() throws InvalidKeySpecException, NoSuchAlgorithmException {
		AppConfig config = this.getAppConfig();
		if ( config != null ) {
			String privKey = config.getUpdateKey();
			return SQLProject.decodePrivateKey(privKey);
		}
		return null;
		
	}
	
	/* (non-Javadoc)
	 * @see edu.uic.orjala.cyanos.web.servlet.ServletObject#getAppConfig()
	 */
	protected AppConfig getAppConfig() {
		ServletContext aCtx = this.getServletContext();
		Object config = aCtx.getAttribute(APP_CONFIG_ATTR);
		if ( config instanceof AppConfig ) 
			return (AppConfig) config;
		return null;
	}
	
	/**
	 * Parse the signaure sent.
	 * 
	 * @param sigString
	 * @param algorithm
	 * @return
	 */
	public static String parseSignature(String sigString, String algorithm) {
		// Parse out the signature
		Matcher match = PATTERN_SIGNATURE.matcher(sigString);
		if ( match.matches() ) {
			if ( algorithm != null ) {
				if ( algorithm.equalsIgnoreCase(match.group(1)) ) 
					return match.group(2);
			} else {
				return match.group(2);
			}
		}
		return null;
	}



}
