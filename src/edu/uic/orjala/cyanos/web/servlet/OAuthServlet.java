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
import org.apache.oltu.oauth2.as.response.OAuthASResponse;
import org.apache.oltu.oauth2.as.response.OAuthASResponse.OAuthAuthorizationResponseBuilder;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;

/**
 * Servlet implementation class OAuthServlet
 */
@WebServlet(description = "OAuth Servlet to allow connection of external tools.", urlPatterns = { "/oauth2" })
public class OAuthServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static Map<String,String> oAuthTokens = new HashMap<String,String>();
       
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
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
		} catch (OAuthSystemException e ) {

		}
		
		
		
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
