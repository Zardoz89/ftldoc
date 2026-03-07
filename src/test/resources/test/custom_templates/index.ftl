<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>${title}</title>
    <link rel="stylesheet" href="custom.css">
</head>
<body>
    <h1>${title}</h1>
    <p>Custom index template</p>
    <#if files?has_content>
    <ul>
    <#list files as file>
        <li><a href="${file.name}.html">${file.name}</a></li>
    </#list>
    </ul>
    </#if>
</body>
</html>
