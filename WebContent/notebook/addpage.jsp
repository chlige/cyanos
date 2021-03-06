<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="cyanos" tagdir="/WEB-INF/tags" %>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8" %>
<%@ page import="edu.uic.orjala.cyanos.web.servlet.MainServlet,
	edu.uic.orjala.cyanos.web.listener.AppConfigListener,
	edu.uic.orjala.cyanos.CyanosObject,
	edu.uic.orjala.cyanos.BasicObject,
	edu.uic.orjala.cyanos.User,
	edu.uic.orjala.cyanos.sql.SQLNotebook,
	edu.uic.orjala.cyanos.Notebook,
	edu.uic.orjala.cyanos.NotebookPage,
	edu.uic.orjala.cyanos.Role,
	java.text.DateFormat" %>
<!DOCTYPE html>
<html>
<head>
<cyanos:header title="Cyanos Notebooks"/>
<% if ( request.getParameter("id") != null && request.getParameter("add") == null ) {  %>
<script type="text/javascript" src="<%= request.getContextPath() %>/tinymce/tinymce.js"></script>
<script type="text/javascript">
var currentCallback;

tinymce.init({
        selector: "textarea#content",
        content_css: "<%= request.getContextPath() %>/cyanos.css",
        body_class: "mceContentBody",
        height: 500,
        plugins: [
                "advlist autolink autosave link image lists charmap print preview hr anchor pagebreak spellchecker",
                "searchreplace wordcount visualblocks visualchars code fullscreen insertdatetime media nonbreaking",
                "table contextmenu directionality emoticons template textcolor paste fullpage textcolor colorpicker textpattern"
        ],

        toolbar1: "styleselect formatselect fontselect fontsizeselect | bold italic underline strikethrough | alignleft aligncenter alignright alignjustify ",
        toolbar2: "cut copy paste | searchreplace | bullist numlist | outdent indent blockquote | undo redo | link unlink anchor image media code | insertdatetime preview | forecolor backcolor",
        toolbar3: "table | hr removeformat | subscript superscript | charmap emoticons | ltr rtl | spellchecker | visualchars visualblocks nonbreaking template pagebreak restoredraft fullscreen",

        menubar: false,
        toolbar_items_size: 'small',
        file_picker_callback: function(callback, value, meta) {
            // Provide file and text for the link dialog
            if (meta.filetype == 'file') {
                callback('mypage.html', {text: 'My text'});
            }

            // Provide image and alt text for the image dialog
            if (meta.filetype == 'image') {
            	currentCallback = callback;
     			tinyMCE.activeEditor.windowManager.open({
       				title: "Select Image",
       				url: "link-file.jsp?type=image",
       				width: 600,
       				height: 400
    			});
            }

            // Provide alternative source and posted for the media dialog
            if (meta.filetype == 'media') {
                callback('movie.mp4', {source2: 'alt.ogg', poster: 'image.jpg'});
            }
        },

        convert_urls: false,
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
div#linkobj { width:25%; margin-right:0; display:block; float:right; }
div#linkobj > label { border: 1px solid #888; background-color: #ddd; margin:5px; 
	margin-top: 0px; height:20px; font-size:12pt; font-weight:bold; width:100%; display: block; padding-top: 5px; }
div#linkobj > input[type=checkbox] { display: none; }
div#linkobj > input[type=checkbox]:checked ~ iframe { display:block; animation-duration: 1s; animation-name: menudown; }
div#linkobj > iframe { margin:0px 5px; border: 1px solid gray; height:500px; width:100%; display:none; transition: all 1s;}
</style>
</head>
<body>
<cyanos:menu/>
<% if ( request.getParameter("id") != null ) { 
	String notebookid = request.getParameter("id");
	Notebook notebook = SQLNotebook.loadNotebook(MainServlet.getSQLData(request), notebookid);
	notebook.first();
%><h1>Notebook (<%= notebook.getID() %>): <%= notebook.getTitle() %></h1>
<h2>Add a page</h2>
<hr width="85%">
<% if ( notebook.isAllowed(Role.CREATE) ) { %>
<% if ( request.getParameter("add") != null ) { 
	NotebookPage aPage = notebook.addPage(Integer.parseInt(request.getParameter("page")), request.getParameter("title"), request.getParameter("content"));
	if ( aPage != null ) {
%><p align="center">Added notebook page <a href="../notebook.jsp?id=<%= request.getParameter("id") %>&page=<%= request.getParameter("page") %>"><%= request.getParameter("page") %>: <%= request.getParameter("title") %></a></p>
<%		
	} else {
%><p align="center">Unable to add notebook page!</p><%
	}
} else { %>
<form method="post">
<div style="width:80%; margin-left:auto; margin-right:auto; ">
<p><label for="page">Page:</label><input type="number" name="page" value="<%= notebook.getPageCount() + 1 %>" min="1" step="1"><br>
<label for="title">Title:</label><input type="text" name="title"></p>
<div>
<div id="linkobj">
<label for="linkView">Link Objects</label><input type="checkbox" value="linkView" id="linkView">
<iframe src="link.jsp"></iframe></div>
<div style="width:75%; margin-left:0; display:block;">
<textarea name="content" id="content"></textarea>
</div>
</div>
<p align="center">
<button type="submit" name="add">Add Notebook Page</button>
</p>
</div>
</form>
<% } } else { %>
<p align="center">ERROR: Only the owner of the notebook can create a page.</p>	
<% } } else { %>
<p align="center">ERROR: notebook ID not specified!</p>
<p align="center">Please select a <a href="../notebook.jsp">Notebook</a></p>
<% } %>
</body>
</html>