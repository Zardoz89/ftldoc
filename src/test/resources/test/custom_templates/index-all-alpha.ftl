<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>${title} - Alphabetical</title>
    <link rel="stylesheet" href="custom.css">
</head>
<body>
    <h1>All Macros (Alphabetical)</h1>
    <ul>
    <#list macros as macro>
        <li><a href="${macro.filename}.html#${macro.name}">${macro.name}</a> (${macro.filename})</li>
    </#list>
    </ul>
</body>
</html>
