<#ftl encoding="UTF-8" output_format="HTML" />
<#import "lib.ftl" as ftl>
<!DOCTYPE html>
<html>
<head>
    <meta charset="${.output_encoding}" />
    <link rel="stylesheet" type="text/css" href="ftldoc.css" />
    <title><#if title?has_content>${title} - </#if>Overview</title>
</head>
<body>
<@ftl.navigationBar files fileSuffix/>
<main>
<h1><#if title?has_content>${title} - </#if>Overview</h1>
${readme!}
</main>
</body>
</html>
