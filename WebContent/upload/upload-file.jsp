<form method="post" enctype="multipart/form-data">
<p align="center">
<b>File to upload: </b>
<input type="file" name="xmlFile" size="25"/>
<button type="submit">Upload</button></p></form>
<p align="center"><a href="<%= request.getContextPath() %>/upload.jsp">Return to Upload Menu</a></p>
<p align="center"><b>Upload Instructions</b></p>
<ul>
<li><b>Microsoft Excel 2007 or higher</b> - Save the file as a standard <b>Excel Workbook (*.xlsx)</b>.</li>
<li><b>Microsoft Excel 2003 or earlier</b> - Save spreadsheet as a <b>XML spreadsheet (*.xml)</b> and upload the resulting .xml file.</li>
<li><b>OpenOffice 2.0 or higher</b> - Save as a standard <b>OpenOffice spreadsheet (*.ods)</b>.</li>
</ul>
<hr width='100%'><p align="center"><font size=+1><b>Worksheet Template</b></font></p>
