<jsp:directive.page contentType="text/html"/>
<jsp:directive.page import="java.util.Date, java.util.Locale"/>
<jsp:directive.page import="java.text.*"/>
<jsp:directive.page import="java.io.*"/>
<jsp:directive.page import="java.sql.*"/>
<jsp:directive.page import="javax.sql.*"/>
<jsp:directive.page import="javax.naming.*"/>
<jsp:directive.page import="org.apache.commons.dbcp.*"/>

<jsp:declaration>
<![CDATA[

	public void printRoot(JspWriter out, HttpServletRequest req) 
		throws Exception 
	{
		Context ctx = null;
		try {
			ctx = new InitialContext();
		} catch (NamingException e) {
			out.println("Couldn't build an initial context : " + e);
		}
		out.println("InitialContext: " + ctx);
		out.println("<ul><li>");
		printContext(req, out, ctx, "java:/comp/env/jdbc");
		out.println("</li></ul>");
	}

	public void printContext(HttpServletRequest req, JspWriter out, Context ctx, String name) throws Exception {
		try {
			out.println("Context: " + name);

			NamingEnumeration enum = ctx.listBindings(name);
			out.print("<ul>");
			while (enum.hasMoreElements()) {
				out.print("<li>");
				Object o = enum.nextElement();
				if (o instanceof Binding) {
					Binding b = (Binding) o;
					String childname = b.getName();
					Object childvalue = b.getObject();
					String fullname = name + (name.endsWith("/") ? "" : "/") + childname;
					if (childvalue instanceof Context) {
						printContext(req, out, ctx, fullname);
					}
					else {
								out.println(childname + "&nbsp;=>&nbsp;" + childvalue);
						if (childvalue instanceof Reference) {
							try {
								out.print("<b>Reference</b><br/>");
								out.print("type: " + ((Reference) childvalue).getClassName() + "<br/>");
								Object refvalue = ctx.lookup(fullname);
								printValue(req, out, refvalue);
							}
							catch (NamingException e) {}
						}
						else {
							printValue(req, out, childvalue);
						}
					}
				}
				else {
							out.print(o.getClass().getName() + ": ");
							out.println(o.toString());
					printValue(req, out, o);
				}
				out.print("</li>");
			}
			out.print("</ul>");
		
		} catch (NamingException e) {
			out.println(e);
		}
	}

	public void printValue(HttpServletRequest req, JspWriter out, Object o) throws Exception {
		if (o instanceof BasicDataSource) {
			BasicDataSource ds = (BasicDataSource) o;
			if ( "Close Connections".equals(req.getParameter(ds.toString())) ) {
				out.println("<BR><FONT COLOR='red'><B>Closing all connections</B></FONT>");
				ds.close();
			}
			out.println("<table border=\"1\">");
			out.println("<tr><td>Url</td><td>" + ds.getUrl() + "</td></tr>");
			out.println("<tr><td>ValidationQuery</td><td>" + ds.getValidationQuery() + "</td></tr>");
			out.println("<tr><td>NumActive</td><td>" + ds.getNumActive() + "</td></tr>");
			out.println("<tr><td>NumIdle</td><td>" + ds.getNumIdle() + "</td></tr>");
			out.println("<tr><TD COLSPAN=2 ALIGN=CENTER><FORM METHOD=POST><INPUT TYPE=SUBMIT NAME='" + ds.toString() + "' VALUE='Close Connections'></FORM></td></tr>");
			out.println("</table>");
		}
		else if (o == null) {
			out.println("object == null");
		}
		else {
			out.print(o.getClass().getName() + ": ");
			out.println(o.toString());
		}
	}

]]>
</jsp:declaration>

<html>
<head>
  <title>Show Datasources</title>
</head>
<body>

<h1>Show JDBC status</h1>
<hr/>
<jsp:scriptlet>
	printRoot(out, request);
</jsp:scriptlet>
<A HREF="">Refresh</A>
<hr/>

</body>
</html>
