package edu.uic.orjala.cyanos.web.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.oltu.oauth2.as.request.OAuthAuthzRequest;
import org.apache.oltu.oauth2.as.request.OAuthRequest;
import org.apache.oltu.oauth2.as.response.OAuthASResponse;
import org.apache.oltu.oauth2.as.response.OAuthASResponse.OAuthAuthorizationResponseBuilder;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;

import edu.uic.orjala.cyanos.web.AppConfig;
import edu.uic.orjala.cyanos.web.listener.AppConfigListener;

/**
 * Servlet implementation class OAuthServlet
 */
@WebServlet(description = "OAuth Servlet to allow connection of external tools.", urlPatterns = { "/oauth" })
public class OAuthServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Map<String,String> oAuthTokens = new HashMap<String,String>();
	
       
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		 String module = request.getPathInfo();
		 
		 try {
			 if ( module.startsWith("/authorize") )
				 this.processAuthz(request, response);
			 else if ( module.startsWith("/token") ) 
				 this.processToken(request, response);
		} catch (OAuthSystemException e ) {

		}
	}
	
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		 String module = request.getPathInfo();
		 
		 try {
			 if ( module.startsWith("/authorize") )
				 this.processAuthz(request, response);
			 else if ( module.startsWith("/token") ) 
				 this.processToken(request, response);
		} catch (OAuthSystemException e ) {

		}
	}

	private OAuthRequest getRequest(HttpServletRequest request) throws OAuthSystemException, OAuthProblemException {
		OAuthRequest authReq = new OAuthAuthzRequest(request);
		AppConfig config = AppConfigListener.getConfig();
		if ( config.validateOAuthClient(authReq.getClientId(), authReq.getClientSecret()) )
			return authReq;
		else 
			return null;
	}

	private void processToken(HttpServletRequest request, HttpServletResponse response) throws ServletException, OAuthSystemException, IOException {

	}
	
	private void processAuthz(HttpServletRequest request, HttpServletResponse response) throws ServletException, OAuthSystemException, IOException {
		try {
			OAuthAuthzRequest authzReq = new OAuthAuthzRequest(request);
			String redirectURI = authzReq.getRedirectURI();

			String responseType = authzReq.getResponseType();

			OAuthAuthorizationResponseBuilder respBuilder = OAuthASResponse.authorizationResponse(request, HttpServletResponse.SC_FOUND);

			if ( responseType.equalsIgnoreCase("CODE") ) {
				respBuilder.setCode("");
			} else if ( responseType.equalsIgnoreCase("TOKEN") ) {
				respBuilder.setAccessToken("");
				respBuilder.setExpiresIn(0l);
			}

			OAuthResponse resp = respBuilder.location(redirectURI).buildQueryMessage();

			response.sendRedirect(resp.getLocationUri());


		} catch (OAuthProblemException e) {
			final OAuthResponse resp = OAuthASResponse
					.errorResponse(HttpServletResponse.SC_FOUND)
					.error(e)
					.location("")
					.buildQueryMessage();
			response.sendRedirect(resp.getLocationUri());
		}

	}

}
