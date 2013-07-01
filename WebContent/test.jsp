<%@ taglib uri="http://java.sun.com/jsp/jstl/sql" prefix="sql" %> 
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %> 

<sql:query var="rs" dataSource="jdbc/CyanosDB"> 
	select culture_source,culture_id from species
</sql:query>

<html>
<head>
<title>DB Test</title>
</head>
<body>
<h2>Results</h2>
<c:forEach var="row" items="${rs.rows}">
	${row.culture_source}${row.culture_id}<br/>
</c:forEach>
</body> </html> 
