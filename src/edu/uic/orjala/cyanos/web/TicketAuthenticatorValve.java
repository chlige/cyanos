/**
 * 
 */
package edu.uic.orjala.cyanos.web;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.catalina.Session;
import org.apache.catalina.authenticator.Constants;
import org.apache.catalina.authenticator.FormAuthenticator;
import org.apache.catalina.connector.Request;
import org.apache.catalina.deploy.LoginConfig;
import org.apache.catalina.realm.GenericPrincipal;
import org.apache.commons.codec.binary.Base64;
import org.apache.naming.ContextBindings;

/**
 * @author George Chlipala
 *
 */
public class TicketAuthenticatorValve extends FormAuthenticator {
	
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
	private static final String COOKIE_TICKET = "NPM-Auth-Ticket";
	
	// Default duration is 30 days.
	private static final long DEFAULT_DURATION = 30 * 24 * 3600 * 1000;
	
	public static final String SESSION_REMEMBER_ME = "j_rememberMe";
	
	private static final String SQL_LOAD_KEY = "SELECT value FROM config WHERE element=? AND param_key=?";
	private final static String SQL_GET_TOMCAT_ROLES = "SELECT DISTINCT role FROM roles WHERE username=?";
	
	protected String signatureHash = "SHA1";
	protected String dataSourceName = null;
	protected String paramPrivateKey = "update_key";
	protected String paramPublicKey = "update_cert";
	protected String configElement = "parameter";
	protected int tokenSize = 256 / 8;
	protected String keyAlgorithm = "DSA";
	
	protected String publicKey = null;
	protected String privateKey = null;
	
	protected KeyPair keyPair;
	
	protected static KeyFactory keyFactory;

	
	class Ticket {
		private String id;
		private Date issued;
		private Date expires;
		private String padding;
		private String signature;
		
		Ticket(String id, long duration, PrivateKey privateKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
			this.id = id;
			this.issued = new Date();
			this.expires = new Date(this.issued.getTime() + duration);		
			byte[] tokenExtra = new byte[tokenSize];
			Random random = new Random();
			random.nextBytes(tokenExtra);	
			this.padding = Base64.encodeBase64String(tokenExtra);
			
			Signature signer = Signature.getInstance(signatureHash + "with" + privateKey.getAlgorithm());
			signer.initSign(privateKey);
			signer.update(Base64.decodeBase64(this.toString()));
			this.signature = Base64.encodeBase64String(signer.sign());
		}
		
		Ticket(String token) throws ParseException {
			String[] values = token.split("/");
			this.id = values[0];
			if ( values[1].length() > 0 ) {
				this.issued = DATE_FORMAT.parse(values[1]);
			}
			if ( values[2].length() > 0 ) {
				this.expires = DATE_FORMAT.parse(values[2]);
			}
			this.padding = values[3];
			this.signature = values[4];
		}
		
		@Override
		public String toString() {
			return id + "/" + formatDate(issued) + "/" + formatDate(expires) + "/" + padding;
		}
		
		public String ticketString() {
			return toString() + "/" + signature;
		}
		
		public boolean verify(PublicKey publicKey) throws InvalidKeyException, NoSuchAlgorithmException, SignatureException {
			Date now = new Date();
			if ( expires == null || now.after(expires) ) {
				return false;
			}
			
			Signature signer = Signature.getInstance(signatureHash + "with" + publicKey.getAlgorithm());
			signer.initVerify(publicKey);
			signer.update(Base64.decodeBase64(this.toString()));
			return signer.verify(Base64.decodeBase64(this.signature));
		}
		
		public Cookie genCookie(String cookieName) {
			Cookie cookie = new Cookie(cookieName, this.ticketString());
			Long age = new Long((this.expires.getTime() - this.issued.getTime()) / 1000);
			cookie.setMaxAge(age.intValue());
			return cookie;
		}
		 
	}
	
	
    /**
     * Return the name of the JNDI JDBC DataSource.
     *
     */
	public String getDataSourceName() {
		return dataSourceName;
	}
	/**
	 * Set the name of the JNDI JDBC DataSource.
	 *
	 * @param dataSourceName the name of the JNDI JDBC DataSource
	 */
	public void setDataSourceName(String dataSourceName) {
		this.dataSourceName = dataSourceName;
	}
   
	public String getSignatureHash() {
		return signatureHash;
	}
	
	public void setSignatureHash(String signatureHash) {
		this.signatureHash = signatureHash;
	}
	
	/**
	 * @return the tokenSize
	 */
	public int getTokenSize() {
		return tokenSize;
	}
	/**
	 * @param tokenSize the tokenSize to set
	 */
	public void setTokenSize(int tokenSize) {
		this.tokenSize = tokenSize;
	}
	/**
	 * @return the keyAlgorithm
	 */
	public String getKeyAlgorithm() {
		return keyAlgorithm;
	}
	/**
	 * @param keyAlgorithm the keyAlgorithm to set
	 */
	public void setKeyAlgorithm(String keyAlgorithm) {
		this.keyAlgorithm = keyAlgorithm;
	}
	/**
	 * @param publicKey the publicKey to set
	 */
	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}
	/**
	 * @param privateKey the privateKey to set
	 */
	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}
	
	private static String formatDate(Date date) {
		if ( date != null )
			return DATE_FORMAT.format(date);
		else 
			return "";
	}

	@Override
	public boolean authenticate(Request req, HttpServletResponse resp, LoginConfig loginConfig) throws IOException {	
		boolean authenticated = false;
		Session session = req.getSessionInternal();
		try {
			KeyPair keys = getKeypair();

			Cookie ticketCookie = getCookie(req, COOKIE_TICKET);
			if ( ticketCookie != null ) {
				Ticket ticket = new Ticket(ticketCookie.getValue());
				authenticated = ticket.verify(keys.getPublic());
				if ( authenticated ) {
					GenericPrincipal principal = new GenericPrincipal(ticket.id, "", this.rolesForUser(ticket.id));
					session.setNote(Constants.SESS_USERNAME_NOTE, ticket.id);
					session.setNote(Constants.SESS_PASSWORD_NOTE, "");
					req.setUserPrincipal(principal);
					this.register(req, resp, principal, HttpServletRequest.FORM_AUTH, ticket.id, "");
				} else {
					Cookie cookie = new Cookie(COOKIE_TICKET, "");
					cookie.setMaxAge(0);
					addTicket(cookie, req, resp);
				}
			}
			if ( ! authenticated ) {
				authenticated = super.authenticate(req, resp, loginConfig);
				if ( authenticated ) {
					ticketCookie = getCookie(req, COOKIE_TICKET);
					if ( ticketCookie == null && req.getParameter(SESSION_REMEMBER_ME) != null ) {
						// Create a new ticket.
						Ticket ticket = new Ticket(session.getPrincipal().getName(), DEFAULT_DURATION, keys.getPrivate());
						ticketCookie = ticket.genCookie(COOKIE_TICKET);
						addTicket(ticketCookie, req, resp);
					}
				}
			}
		} catch (GeneralSecurityException | NamingException | ParseException | SQLException e) {
			resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			e.printStackTrace();
		}
		return authenticated;
	}

	private static Cookie getCookie(Request request, String cookieName) {
		Cookie cookies[] = request.getCookies();
		
		for ( int i = 0; i < cookies.length; i++ ) {
			if ( cookies[i].getName().equals(cookieName) ) 
				return cookies[i];
		}
		return null;
	}
	
	// http://www.docjar.com/html/api/org/apache/catalina/realm/DataSourceRealm.java.html
	
	protected Connection open() throws NamingException, SQLException {
		Context context = ContextBindings.getClassLoader();
		context = (Context) context.lookup("comp/env");
		DataSource datasource = (DataSource)context.lookup(dataSourceName);
		return datasource.getConnection();
	}
	
	private static void addTicket(Cookie cookie, Request req, HttpServletResponse resp) {
		cookie.setSecure(true);
		cookie.setPath(req.getContextPath());
		cookie.setDomain(req.getServerName());
		resp.addCookie(cookie);
	}
	
	private List<String> rolesForUser(String userID) throws SQLException, NamingException {
		Connection dbc = open();
		PreparedStatement sth = dbc.prepareStatement(SQL_GET_TOMCAT_ROLES);
		sth.setString(1, userID);
		List<String> roles = new ArrayList<String>();
		ResultSet results = sth.executeQuery();
		while ( results.next() ) {
			roles.add(results.getString(1));
		}
		results.close();
		sth.close();
		dbc.close();
		return roles;
	}

	private KeyFactory getKeyFactory() throws NoSuchAlgorithmException {
		if ( keyFactory == null ) {
			keyFactory = KeyFactory.getInstance(this.keyAlgorithm);
		}
		return keyFactory;
	}
	
	private KeyPair getKeypair() throws NoSuchAlgorithmException, SQLException, NamingException, InvalidKeySpecException { 
		if ( this.keyPair == null ) { 
			X509EncodedKeySpec certSpec = new X509EncodedKeySpec(Base64.decodeBase64(this.getPublicKey()));
			PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.decodeBase64(this.getPrivateKey()));
			KeyFactory kf = this.getKeyFactory();
			this.keyPair = new KeyPair(kf.generatePublic(certSpec), kf.generatePrivate(keySpec));
		}
		return this.keyPair;
	}
	
	private String getKey(String keyparam) throws SQLException, NamingException {
		String key = null;
		Connection dbc = open();
		PreparedStatement sth = dbc.prepareStatement(SQL_LOAD_KEY);
		sth.setString(1, this.configElement);
		sth.setString(2, keyparam);
		ResultSet results = sth.executeQuery();
		if ( results.first() ) {
			key = results.getString(1);
		}
		results.close();
		sth.close();
		dbc.close();
		return key;
	}
	
	public String getPublicKey() throws SQLException, NamingException {
		if ( this.publicKey == null ) {
			this.publicKey = this.getKey(this.paramPublicKey);
		}
		return this.publicKey;
	}

	public String getPrivateKey() throws SQLException, NamingException {
		if ( this.privateKey == null ) {
			this.privateKey = this.getKey(paramPrivateKey);
		}
		return this.privateKey;
	}
}
