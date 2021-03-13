<#ftl encoding="UTF-8" output_format="HTML" />
<#import "lib.ftl" as ftl>
<!DOCTYPE html>
<html>
<head>
    <meta charset="${.output_encoding}" />
    <title>${filename}</title>
    <link rel="stylesheet" type="text/css" href="ftldoc.css" />
    <script language="javascript">
        function toggle(id) {
            elem = document.getElementById(id);
            if(elem.style.display=="block") {
                elem.style.display="none";
            } else {
                elem.style.display="block";
            }
        }
    </script>
</head>
<body>
<@ftl.navigationBar files fileSuffix/>
<main>

<#-- start prolog -->
<h1>${filename}</h1>
<#if comment.comment?has_content>
    ${comment.comment}<br/>
</#if>
<dl>
    <@ftl.printOptional comment.@author?if_exists, "Author" />
    <@ftl.printOptional comment.@version?if_exists, "Version" />
</dl>
<#-- end prolog -->

<#-- start summary -->
<table>
    <tr><td colspan="2" class="heading">Macro and Function Summary</td></tr>
        <#list categories?keys as category>
            <#if categories[category]?has_content>
                <tr><td colspan="2" class="category">
                <#if category?has_content>
                    Category ${category}
                <#else>
                    no category
                </#if>
                </td></tr>
                <#list categories[category] as macro>
                    <tr>
                        <td width="100px" valign="top">
                            <code>${macro.type}</code>
                        </td>
                        <td>
                            <dl>
                                <dt>
                                    <code>
                                        <b><a href="#${macro.name}">
                                            ${macro.name}</a>
                                        </b>
                                        <@ftl.signature macro />
                                    </code>
                                </dt>
                                <dd>
                                    ${macro.short_comment?if_exists}
                                </dd>
                            </dl>
                        </td>
                    </tr>
                </#list>
            </#if>
        </#list>
</table>

<#-- end summary -->
<br>
<#-- start details -->

<table>
    <tr><td colspan="2" class="heading">Macro and Function Detail</td></tr>
</table>
<#list macros as macro>
    <dl>
        <dt>
            <code>${macro.type} <b><a name="${macro.name}">${macro.name}</a></b>
                <@ftl.signature macro />
            </code>
        </dt>
        <dd>
            <#if macro.@deprecated??><@ftl.printDeprecated macro.@deprecated/></#if>
            <#if macro.comment?has_content>
                <p>${macro.comment!}</p>
            </#if>
            <dl>
                <@ftl.printOptional macro.category?if_exists, "Category" />
                <@ftl.printParameters macro />
                <@ftl.printOptional macro.@nested?if_exists, "Nested" />
                <@ftl.printOptional macro.@return?if_exists, "Return value" />
                <@ftl.printSourceCode macro />
            </dl>
        </dd>
    </dl>
    <#if macro_has_next><hr></#if>
</#list>

<#-- end details -->
</main>
</body>
</html>
