<#ftl encoding="UTF-8" output_format="HTML" />
<#import "lib.ftl" as ftl>
<!DOCTYPE html>
<html>
<head>
    <meta charset="${.output_encoding}" />
    <link rel="stylesheet" type="text/css" href="ftldoc.css" />
    <title><#if title?has_content>${title} - </#if>Alphabethic Index</title>
</head>
<body>
<@ftl.navigationBar files fileSuffix/>
<main>
<#assign lastLetter = "" />
<#list macros as macro>
    <#if macro.name[0]?cap_first != lastLetter>
        <#assign lastLetter = macro.name[0]?cap_first />
        <a href="#${lastLetter}">${lastLetter}</a>
    </#if>
</#list>
<hr>
 
 
<#assign lastLetter = "" />
<#list macros as macro>
    <#if macro.name[0]?cap_first != lastLetter>
        <#assign lastLetter = macro.name[0]?cap_first />
        <a name="${lastLetter}" /><h3>${lastLetter}</h3>
    </#if>
    <b><a href="${macro.filename}.html#${macro.name}">${macro.name}</a></b>
     - ${macro.type?cap_first} in file <a href="${macro.filename}.html">${macro.filename}</a>
    <br/>
</#list>
</main>
</body>
</html>
