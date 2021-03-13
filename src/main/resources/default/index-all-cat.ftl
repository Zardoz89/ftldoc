<#ftl encoding="UTF-8" output_format="HTML" />
<#import "lib.ftl" as ftl>
<!DOCTYPE html>
<html>
<head>
    <meta charset="${.output_encoding}" />
    <link rel="stylesheet" type="text/css" href="ftldoc.css" />
    <title>ftldoc - Index by category</title>
</head>
<body>
<@ftl.navigationBar files fileSuffix/>
<main>
<#list categories?keys as category>
    <#if categories[category]?has_content>
        <#if category?has_content>
            <h3>Category ${category}</h3>
        <#else>
            <h3>no category</h3>
        </#if>
        
        <#list categories[category] as macro>
            <b><a href="${macro.filename}.html#${macro.name}">${macro.name}</a></b>
             - ${macro.type?cap_first} in file
            <a href="${macro.filename}.html">${macro.filename}</a>
            <br/>
        </#list>
        <br/>
        <#if category_has_next><hr/></#if>
    </#if>
</#list>
</main>
</body>
</html>
