<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="cyanos" tagdir="/WEB-INF/tags" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.web.servlet.MainServlet,
	edu.uic.orjala.cyanos.web.listener.AppConfigListener,
	edu.uic.orjala.cyanos.CyanosObject,
	edu.uic.orjala.cyanos.BasicObject,
	edu.uic.orjala.cyanos.User,
	java.sql.PreparedStatement,
	java.math.BigDecimal,
	java.math.MathContext,
	java.sql.Connection,
	java.sql.ResultSet,
	java.sql.Statement,
	java.text.DateFormat" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<cyanos:header title="Cyanos Notebooks"/>
<% if ( request.getParameter("id") != null && request.getParameter("add") == null ) {  %>
<script type="text/javascript" src="<%= request.getContextPath() %>/tinymce/tinymce.min.js"></script>
<script type="text/javascript">
tinymce.init({
        selector: "textarea#content",
        height: 500,
        plugins: [
                "advlist autolink autosave link image lists charmap print preview hr anchor pagebreak spellchecker",
                "searchreplace wordcount visualblocks visualchars code fullscreen insertdatetime media nonbreaking",
                "table contextmenu directionality emoticons template textcolor paste fullpage textcolor colorpicker textpattern"
        ],

        toolbar1: "fullpage | bold italic underline strikethrough | alignleft aligncenter alignright alignjustify | styleselect formatselect fontselect fontsizeselect",
        toolbar2: "cut copy paste | searchreplace | bullist numlist | outdent indent blockquote | undo redo | link unlink anchor image media code | insertdatetime preview | forecolor backcolor",
        toolbar3: "table | hr removeformat | subscript superscript | charmap emoticons | print fullscreen | ltr rtl | spellchecker | visualchars visualblocks nonbreaking template pagebreak restoredraft",

        menubar: false,
        toolbar_items_size: 'small',

        style_formats: [
                {title: 'Bold text', inline: 'b'},
                {title: 'Red text', inline: 'span', styles: {color: '#ff0000'}},
                {title: 'Red header', block: 'h1', styles: {color: '#ff0000'}},
                {title: 'Example 1', inline: 'span', classes: 'example1'},
                {title: 'Example 2', inline: 'span', classes: 'example2'},
                {title: 'Table styles'},
                {title: 'Table row 1', selector: 'tr', classes: 'tablerow1'}
//        ],

//        templates: [
//                {title: 'Test template 1', content: 'Test 1'},
//                {title: 'Test template 2', content: 'Test 2'}
        ]
});</script>
<% } %>
<style type="text/css">
h2 { text-align:center; }
table.details { border-collapse: collapse; margin-bottom: 10px; }
table.details td, table.details th { text-align:left; }
table.results tr { border-top: 1px solid gray; border-bottom: 1px solid gray; }
table.results td, table.results th { padding-left: 2px; padding-right: 2px; }
table { margin-left: auto; margin-right:auto; }
</style>
</head>
<body>
<cyanos:menu/>
<% Connection conn = AppConfigListener.getDBConnection();
if ( request.getParameter("id") != null ) { 
	String notebookid = request.getParameter("id");
	String sql = "SELECT n.title,COUNT(p.page),COALESCE(MAX(p.page) + 1,1) FROM notebook n LEFT OUTER JOIN notebook_page p ON(n.notebook_id = p.notebook_id) WHERE n.username=? AND n.notebook_id=?";
	PreparedStatement sth = conn.prepareStatement(sql);
	sth.setString(1, request.getRemoteUser());
	sth.setString(2, notebookid);	
	ResultSet results = sth.executeQuery();
	results.first();
%><h1>Notebook <%= results.getString(1) %> (<%= results.getInt(2) %> Pages)</h1>
<h2>Add a page</h2>
<hr width="85%">
<% if ( request.getParameter("add") != null ) { 
	results.close(); sth.close();
	sql = "INSERT INTO notebook_page(notebook_id,page,title,content,date_created) VALUES(?,?,?,?,CURDATE())";
	sth = conn.prepareStatement(sql);
	sth.setString(1, request.getParameter("id"));
	sth.setString(2, request.getParameter("page"));
	sth.setString(3, request.getParameter("title"));
	sth.setString(4, request.getParameter("content"));
	if ( sth.executeUpdate() == 1) {
%><p align="center">Added notebook page <a href="../notebook.jsp?id=<%= request.getParameter("id") %>&page=<%= request.getParameter("page") %>"><%= request.getParameter("page") %>: <%= request.getParameter("title") %></a></p>
<%		
	} else {
%><p align="center">Unable to add notebook page!</p><%
	}
	sth.close();
	conn.close();
} else { %>
<form method="post">
<div style="width:70%; margin-left:auto; margin-right:auto; ">
<p><label for="page">Page:</label><input type="number" name="page" value="<%= results.getInt(3) %>" min="1" step="1"><br>
<label for="title">Title:</label><input type="text" name="title"></p>
<textarea name="content" id="content"></textarea>
<p align="center">
<button type="submit" name="add">Add Notebook Page</button>
</p>
</div>
</form>
<% } } else { %>
<p align="center">ERROR: notebook ID not specified!</p>
<p align="center">Please select a <a href="../notebook.jsp">Notebook</a></p>
<% }  %>
</body>
</html>