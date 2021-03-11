<#ftl encoding="UTF-8" output_format="HTML" />
<#import "lib.ftl" as lib>
<!DOCTYPE html>
<html>
<head>
    <meta charset="${.output_encoding}" />
    <link rel="stylesheet" type="text/css" href="ftldoc.css" />
    <title>ftldoc - Overview</title>
</head>
<body>
<@lib.fileList files fileSuffix/>
<main>
<#include "nav.ftl">
<h3>Overview</h3>

</main>
</body>
</html>
