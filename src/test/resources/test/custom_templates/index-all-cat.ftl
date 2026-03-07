<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>${title} - All Categories</title>
    <link rel="stylesheet" href="custom.css">
</head>
<body>
    <h1>All Categories</h1>
    <#list categories?keys as cat>
    <h2>${cat}</h2>
    <ul>
    <#list categories[cat] as macro>
        <li><a href="${macro.filename}.html#${macro.name}">${macro.name}</a></li>
    </#list>
    </ul>
    </#list>
</body>
</html>
