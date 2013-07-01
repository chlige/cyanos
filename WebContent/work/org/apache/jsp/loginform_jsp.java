package org.apache.jsp;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.jsp.*;

public final class loginform_jsp extends org.apache.jasper.runtime.HttpJspBase
    implements org.apache.jasper.runtime.JspSourceDependent {

  private static java.util.List _jspx_dependants;

  public Object getDependants() {
    return _jspx_dependants;
  }

  public void _jspService(HttpServletRequest request, HttpServletResponse response)
        throws java.io.IOException, ServletException {

    JspFactory _jspxFactory = null;
    PageContext pageContext = null;
    HttpSession session = null;
    ServletContext application = null;
    ServletConfig config = null;
    JspWriter out = null;
    Object page = this;
    JspWriter _jspx_out = null;
    PageContext _jspx_page_context = null;


    try {
      _jspxFactory = JspFactory.getDefaultFactory();
      response.setContentType("text/html");
      pageContext = _jspxFactory.getPageContext(this, request, response,
      			null, true, 8192, true);
      _jspx_page_context = pageContext;
      application = pageContext.getServletContext();
      config = pageContext.getServletConfig();
      session = pageContext.getSession();
      out = pageContext.getOut();
      _jspx_out = out;


	if (request.getHeader("REFERER") != null && session != null && session.getAttribute("url") == null ) {
		String url = new String(request.getHeader("REFERER"));
		session.setAttribute("url", url);	
	}

      out.write("\n");
      out.write("<html><head><title>Login Page</title>\n");
      out.write("<LINK REL=\"stylesheet\" TYPE=\"text/css\" HREF=\"\n");
      out.print( request.getContextPath() );
      out.write("/cyanos.css\"/>\n");
      out.write("</head>\n");
      out.write("<body>\n");
      out.write("<font size='+2'>Please Login</font><hr>\n");
      out.write("<form action='j_security_check' method='post' target='_top'>\n");
      out.write("<table>\n");
      out.write(" <tr><td>Username:</td>\n");
      out.write("   <td><input type='text' name='j_username'></td></tr>\n");
      out.write(" <tr><td>Password:</td> \n");
      out.write("   <td><input type='password' name='j_password' size='8'></td>\n");
      out.write(" </tr>\n");
      out.write("</table>\n");
      out.write("<br>\n");
      out.write("  <input type='submit' value='Login'><input type='reset' value='Reset'/>\n");
      out.write("</form><BR>\n");
      out.write("</body></html>\n");
    } catch (Throwable t) {
      if (!(t instanceof SkipPageException)){
        out = _jspx_out;
        if (out != null && out.getBufferSize() != 0)
          out.clearBuffer();
        if (_jspx_page_context != null) _jspx_page_context.handlePageException(t);
      }
    } finally {
      if (_jspxFactory != null) _jspxFactory.releasePageContext(_jspx_page_context);
    }
  }
}
