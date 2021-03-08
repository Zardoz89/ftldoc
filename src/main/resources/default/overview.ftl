<html>
<head>
    <meta charset="${.output_encoding}" />
    <link rel="stylesheet" type="text/css" href="ftldoc.css" />
    <script language="javascript">
    function setTitle() {
        parent.document.title="ftldoc - Overview";
    }
    </script>
</head>
<body onLoad="setTitle();">
<#include "nav.ftl">
<h3>Overview</h3>

<table>
    <tr><td colspan="2" class="heading">Library Summary</td></tr>
    <#list files as file>
        <tr>
            <td width="160px"><a href="${file.filename}.html"><b>${file.filename}</b></a></td>
            <td>${file.comment.short_comment?if_exists}&nbsp;</td>
        </tr>
    </#list>
</table>
</body>
</html>
