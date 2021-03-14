<#ftl encoding="UTF-8" output_format="HTML" />
<#import "lib.ftl" as ftl>
<!DOCTYPE html>
<html>
<head>
    <meta charset="${.output_encoding}" />
    <title><#if title?has_content>${title} - </#if>${filename}</title>
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
<#if comment.comment?has_content && comment.comment?trim?length gt 0>
    ${comment.comment}<br/>
</#if>
<#if comment.@author?if_exists || comment.@version?if_exists >
    <dl>
        <@ftl.printOptional comment.@author?if_exists, "Author" />
        <@ftl.printOptional comment.@version?if_exists, "Version" />
    </dl>
</#if>
<#-- end prolog -->
<#-- start summary -->
<h3>Macro and Function Summary</h3>
<#list categories?keys as category>
    <#if categories[category]?has_content>
        <#if category?has_content>
            <h5>Category ${category}</h5>
        <#else>
            <h5>no category</h5>
        </#if>
        <table class="summary">
            <tbody>
                <#list categories[category] as macro>
                <tr>
                    <td class="summary__type">
                        <code>${macro.type}</code>
                    </td>
                    <td class="summary__description">
                        <dl>
                            <dt>
                                <code class="macro__signature">
                                    <a href="#${macro.name}">
                                        ${macro.name}
                                    </a>
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
            </body>
        </table>
    </#if>
</#list>
<#-- end summary -->

<#-- start details -->
<h3>Macro and Function Detail</h3>
<#list macros as macro>
    <dl class="macro">
        <dt>
            <code class="macro__signature">${macro.type} <a name="${macro.name}">${macro.name}</a>
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
    <#sep><hr/></#sep>
</#list>

<#-- end details -->
</main>
</body>
</html>
