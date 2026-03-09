<#ftl encoding="UTF-8" output_format="HTML" />
<#import "lib.ftl" as ftl>
<!DOCTYPE html>
<html>
<head>
    <meta charset="${.output_encoding}" />
    <link rel="stylesheet" type="text/css" href="ftldoc.css" />
    <title><#if title?has_content>${title} - </#if>Global Variables Index</title>
</head>
<body>
<@ftl.navigationBar categorizedFiles fileSuffix/>
<main>
<#assign lastLetter = "" />
<#list variables as variable>
    <#if variable.name[0]?cap_first != lastLetter>
        <#assign lastLetter = variable.name[0]?cap_first />
        <a href="#${lastLetter}">${lastLetter}</a>
    </#if>
</#list>
<hr>
 
 
<#assign lastLetter = "" />
<#list variables as variable>
    <#if variable.name[0]?cap_first != lastLetter>
        <#assign lastLetter = variable.name[0]?cap_first />
        <a name="${lastLetter}" /><h3>${lastLetter}</h3>
    </#if>
    <b><a href="${variable.filename}.html#${variable.name}">${variable.name}</a></b>
     - Global Variable in file <a href="${variable.filename}.html">${variable.filename}</a>
    <br/>
</#list>
</main>
</body>
</html>
