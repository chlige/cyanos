package edu.uic.orjala.cyanos.web.servlet;

import java.io.IOException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.security.RolesAllowed;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.oltu.oauth2.as.request.OAuthAuthzRequest;
import org.apache.oltu.oauth2.as.request.OAuthRequest;
import org.apache.oltu.oauth2.as.response.OAuthASResponse;
import org.apache.oltu.oauth2.as.response.OAuthASResponse.OAuthAuthorizationResponseBuilder;
import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.openid4java.association.AssociationException;
import org.openid4java.message.AuthSuccess;
import org.openid4java.message.DirectError;
import org.openid4java.message.Message;
import org.openid4java.message.MessageException;
import org.openid4java.message.MessageExtension;
import org.openid4java.message.ParameterList;
import org.openid4java.message.sreg.SRegMessage;
import org.openid4java.message.sreg.SRegRequest;
import org.openid4java.message.sreg.SRegResponse;
import org.openid4java.server.ServerException;
import org.openid4java.server.ServerManager;

import edu.uic.orjala.cyanos.DataException;
import edu.uic.orjala.cyanos.User;

/**
 * Servlet implementation class OAuthServlet
 */
@WebServlet(description = "OAuth Servlet to allow connection of external tools.", urlPatterns = { "/oauth", "/oauth/", "/oauth/user/*" })
public class OAuthServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Map<String,String> oAuthTokens = new HashMap<String,String>();
	
	public static final String XRDS_HEADER = "X-XRDS-Location";
	private static final String XRDS_PATH = "/oauth/idpXrds.jsp";
 
	public static ServerManager manager = new ServerManager();

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		 response.setHeader(XRDS_HEADER, getXRDSURL(request));
		 this.processRequest(request, response);

		 /*
		 try {
//			 this.processAuthz(request, response);
			 if ( module.startsWith("/authorize") )
				 this.processAuthz(request, response);
			 else if ( module.startsWith("/token") ) 
				 this.processToken(request, response);
			 else 
				 System.err.println("Method" + module);
		} catch (OAuthSystemException e ) {

		}
		*/
	}
	
	public static BigInteger DEFAULT_DH_P = new BigInteger("DCF93A0B883972EC0E19989AC5A2CE310E1D37717E8D9571BB7623731866E61EF75A2E27898B057F9891C2E27A639C3F29B60814581CD3B2CA3986D2683705577D45C2E7E52DC81C7A171876E5CEA74B1448BFDFAF18828EFD2519F14E45E3826634AF1949E5B535CC829A483B8A76223E5D490A257F05BDFF16F2FB22C583AB", 16);
	public static BigInteger DEFAULT_DH_G = new BigInteger("2");
	
	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		 response.setHeader(XRDS_HEADER, getXRDSURL(request));
		 this.processRequest(request, response);
	}
	
	public static void setupManager(HttpServletRequest request) throws MalformedURLException {
		manager.setOPEndpointUrl(getOPURL(request));
	}
	
	private void processRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
		ParameterList params = new ParameterList(req.getParameterMap());
		Message response;
		String responseText = null;
		
		manager.setOPEndpointUrl(getOPURL(req));

		String mode = ( params.hasParameter("openid.mode") ? params.getParameterValue("openid.mode") : "");
		
		HttpSession session = req.getSession();
		if ( mode.length() < 1 ) {
			Object parmObj = session.getAttribute("OAuthParams");
			if ( parmObj instanceof ParameterList ) {
				params = (ParameterList) parmObj;
				session.removeAttribute("OAuthParams");
				mode = ( params.hasParameter("openid.mode") ? params.getParameterValue("openid.mode") : "");
			}
		}
			
		if ( mode.equals("associate") ) {
			response = manager.associationResponse(params);
			responseText = response.keyValueFormEncoding();
		} else if ("checkid_setup".equals(mode) || "checkid_immediate".equals(mode)) {
			if ( req.getRemoteUser() == null ) {
				session.setAttribute("OAuthParams", params);
				req.authenticate(resp);
				return;
			}			
			// interact with the user and obtain data needed to continue
			String authConfirm = req.getParameter("auth_confirm");
			if ( authConfirm != null ) {
				String userID = getUserURL(req); 
				// --- process an authentication request ---
				response = manager.authResponse(params,	userID, userID, authConfirm.equalsIgnoreCase("true"), false);
				Map<String,String> userData = new HashMap<String,String>();
				try {
					
					Message reqMsg = Message.createMessage(params);
					User aUser = MainServlet.getUser(req);

					userData.put("fullname", aUser.getUserName());
					userData.put("email", aUser.getUserEmail());

					MessageExtension ext = reqMsg.getExtension(SRegMessage.OPENID_NS_SREG11);
					
					SRegResponse sregResp = SRegResponse.createSRegResponse((SRegRequest) ext, userData);
					sregResp.setTypeUri(SRegMessage.OPENID_NS_SREG11);
					response.addExtension(sregResp);		
				} catch (DataException | MessageException | SQLException e) {
					e.printStackTrace();
				}		
				
				try {
					manager.sign((AuthSuccess) response);
				} catch (ServerException | AssociationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				if (response instanceof DirectError)
					responseText = response.keyValueFormEncoding();
				else {
					responseText = response.getDestinationUrl(true);	
					resp.sendRedirect(responseText);
					return;
				}
			} else {
				req.setAttribute("OAuthParams", params);
				RequestDispatcher disp = getServletContext().getRequestDispatcher("/oauth.jsp");
				disp.forward(req, resp);					
			}
			
		} else if ("check_authentication".equals(mode)) {
				// --- processing a verification request ---
				response = manager.verify(params);
				responseText = response.keyValueFormEncoding();
		} else {
				// --- error response ---
				response = DirectError.createDirectError("Unknown request");
				responseText = response.keyValueFormEncoding();
		}
		if ( responseText != null ) {
			ServletOutputStream os = resp.getOutputStream();
			os.write(responseText.getBytes());
			os.close();
		}
	}

	private static void dumpReq(HttpServletRequest request) {
		 Enumeration<String> header = request.getHeaderNames();
		 
		 while ( header.hasMoreElements() ) {
			 String head = header.nextElement();
			 System.err.println(String.format("%s: %s", head, request.getHeader(head)));
		 }
		 
		 for ( Entry<String,String[]> param : request.getParameterMap().entrySet() ) {
			 System.err.print(param.getKey());
			 System.err.print(" => ");
			 for ( String value : param.getValue() ) {
				 System.err.print(value);
				 System.err.print(", ");
			 }
			 System.err.println();
		 }
	}
	
	private OAuthRequest getRequest(HttpServletRequest request) throws OAuthSystemException, OAuthProblemException {
		OAuthRequest authReq = new OAuthAuthzRequest(request);
		return authReq;
/*		AppConfig config = AppConfigListener.getConfig();
		if ( config.validateOAuthClient(authReq.getClientId(), authReq.getClientSecret()) )
			return authReq;
		else 
			return null;
*/
	}

	private void processToken(HttpServletRequest request, HttpServletResponse response) throws ServletException, OAuthSystemException, IOException {

	}
	
	private void processAuthz(HttpServletRequest request, HttpServletResponse response) throws ServletException, OAuthSystemException, IOException {
		String redirectURI = request.getParameter("openid.return_to");		
		try {
			OAuthAuthzRequest authzReq = new OAuthAuthzRequest(request);

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
	
	public static String getUserURL(HttpServletRequest req) throws MalformedURLException {
		URL url = new URL(req.getScheme(), req.getServerName(), req.getServerPort(), 
				req.getContextPath().concat("/oauth/user.jsp?user=").concat(req.getRemoteUser()) );
		return url.toString();
	}
	
	public static String getOPURL(HttpServletRequest req) throws MalformedURLException {
		URL url = new URL(req.getScheme(), req.getServerName(), req.getServerPort(), 
				req.getContextPath().concat("/oauth/") );
		return url.toString();
	}
	
	public static String getXRDSURL(HttpServletRequest req) throws MalformedURLException {
		URL url = new URL(req.getScheme(), req.getServerName(), req.getServerPort(), 
				req.getContextPath().concat(XRDS_PATH) );
		return url.toString();
	}	
}
