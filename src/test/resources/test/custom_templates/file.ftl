<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>${title} - ${filename}</title>
    <link rel="stylesheet" href="custom.css">
</head>
<body>
    <h1>${filename}</h1>
    <#if comment?? && comment.short_comment??>
    <p class="summary">${comment.short_comment}</p>
    </#if>
    <#if macros?has_content>
    <table>
        <thead>
            <tr>
                <th>Name</th>
                <th>Type</th>
                <th>Arguments</th>
            </tr>
        </thead>
        <tbody>
        <#list macros as macro>
            <tr>
                <td>${macro.name}</td>
                <td>${macro.type}</td>
                <td>${macro.arguments?join(", ")}</td>
            </tr>
        </#list>
        </tbody>
    </table>
    </#if>
</body>
</html>
