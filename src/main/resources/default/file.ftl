<#ftl encoding="UTF-8" output_format="HTML" />
<#--- @ftlvariable name="title" type="java.lang.String" -->
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
<@ftl.navigationBar categorizedFiles fileSuffix filename/>
<main>

<#-- start prolog -->
<h1>${filename}</h1>
<#if comment.comment?has_content && comment.comment?trim?length gt 0>
    ${comment.comment?no_esc}<br/>
</#if>
<#if comment.@author?? || comment.@version?? || comment.@copyright?? >
    <dl>
        <#if comment.@author?? ><#t/>
            <#list comment.@author as author><#t/>
                <@ftl.printOptional author "Author" />
            </#list>
        </#if>
        <#if comment.@version?? ><#t/>
            <#list comment.@version as version><#t/>
                <@ftl.printOptional version "Version" />
            </#list>
        </#if>
        <#if comment.@copyright?? ><#t/>
            <#list comment.@copyright as copyright><#t/>
                <@ftl.printOptional copyright "Copyright" />
            </#list>
        </#if>
    </dl>
</#if>
<#if comment.@ftlroot?? >
    <dl>
        <dt>FTL Root:</dt>
        <dd><code>${comment.@ftlroot}</code></dd>
    </dl>
</#if>
<#-- end prolog -->
<#if variables?has_content && variables?size gt 0>
<#-- start global variables summary -->
<h3>Global Variables Summary</h3>
<table class="summary">
    <tbody>
        <#list variables as variable>
        <tr>
            <td class="summary__type">
                <code>${variable.type}</code>
            </td>
            <td class="summary__description">
                <dl>
                    <dt>
                        <code class="variable__signature">
                            <a href="#${variable.name}">
                                ${variable.name}
                            </a>
                            <#if variable.commentText.@type??>
                                <#list variable.commentText.@type as typeInfo>
                                    : ${typeInfo}
                                </#list>
                            </#if>
                        </code>
                    </dt>
                    <dd>
                        ${variable.commentText.short_comment?if_exists}
                    </dd>
                </dl>
            </td>
        </tr>
        </#list>
    </tbody>
</table>
<#-- end global variables summary -->
<#-- start global variables detail -->
<h3>Global Variables Detail</h3>
<#list variables as variable>
    <dl class="variable">
        <dt>
            <code class="variable__signature"><a name="${variable.name}">${variable.name}</a>
                <#if variable.commentText.@type??>
                    <#list variable.commentText.@type as typeInfo>
                        : ${typeInfo}
                    </#list>
                </#if>
            </code>
        </dt>
        <dd>
            <#if variable.commentText.@deprecated??><@ftl.printDeprecated variable.@deprecated/></#if>
            <#if variable.commentText.comment?has_content>
                <p>${variable.comment!}</p>
            </#if>
        </dd>
    </dl>
    <#sep><hr/></#sep>
</#list>
<#-- end global variables detail -->
</#if>
<#if externalVariables?has_content && externalVariables?size gt 0>
<#-- start external variables summary -->
<h3>External Variables Summary</h3>
<p>These variables are defined externally (e.g., by Java code) and available in this template.</p>
<table class="summary">
    <tbody>
        <#list externalVariables as variable>
        <tr>
            <td class="summary__type">
                <code>${variable.type}</code>
            </td>
            <td class="summary__description">
                <dl>
                    <dt>
                        <code class="variable__signature">
                            <#if variable.file??>
                                <span title="File: ${variable.file}">${variable.name}</span>
                            <#else>
                                ${variable.name}
                            </#if>
                        </code>
                    </dt>
                </dl>
            </td>
        </tr>
        </#list>
    </tbody>
</table>
<#-- end external variables summary -->
</#if>
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
