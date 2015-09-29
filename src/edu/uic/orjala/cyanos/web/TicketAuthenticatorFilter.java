/**
 * 
 */
package edu.uic.orjala.cyanos.web;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;

import org.apache.commons.codec.binary.Base64;

import edu.uic.orjala.cyanos.web.servlet.ProjectUpdateServlet;

/**
 * @author George Chlipala
 *
 */
public class TicketAuthenticatorFilter implements Filter {

	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
	private static final String COOKIE_TICKET = "NPM-Auth-Ticket";
	
	// Default duration is 30 days.
	private static final long DEFAULT_DURATION = 30 * 24 * 3600 * 1000;
	
	public static final String SESSION_REMEMBER_ME = "j_rememberMe";
	
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
			byte[] tokenExtra = new byte[ProjectUpdateServlet.TOKEN_SIZE];
			Random random = new Random();
			random.nextBytes(tokenExtra);	
			this.padding = Base64.encodeBase64String(tokenExtra);
			
			Signature signer = Signature.getInstance(ProjectUpdateServlet.DEFAULT_SIGNATURE_ALGO + privateKey.getAlgorithm());
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
			
			Signature signer = Signature.getInstance(ProjectUpdateServlet.DEFAULT_SIGNATURE_ALGO + publicKey.getAlgorithm());
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
	
	private static String formatDate(Date date) {
		if ( date != null )
			return DATE_FORMAT.format(date);
		else 
			return "";
	}

	/* (non-Javadoc)
	 * @see javax.servlet.Filter#destroy()
	 */
	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
		
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	@Override
	public void init(FilterConfig config) throws ServletException {
		// TODO Auto-generated method stub

	}

}
