<#ftl encoding="UTF-8" output_format="HTML" />
<#import "ftl_highlight.ftl" as ftl>
<#import "lib.ftl" as lib>
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
<@lib.fileList files fileSuffix/>
<main>
<#include "nav.ftl">

<#-- start prolog -->
<h3>${filename}</h3>
<#if comment.comment?has_content>
    ${comment.comment}<br>
</#if>
<dl>
    <@printOptional comment.@author?if_exists, "Author" />
    <@printOptional comment.@version?if_exists, "Version" />
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
                                        <@signature macro />
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
        <dt><code>${macro.type} <b><a name="${macro.name}">${macro.name}</a></b>
                <@signature macro />
        </code></dt>
        <dd>
            <br>
        <#if macro.@deprecated??>
                <@printDeprecated macro.@deprecated/>
            </#if>
            <#if macro.comment?has_content>
                ${macro.comment}<br><br>
            </#if>
            <dl>
                <@printOptional macro.category?if_exists, "Category" />
                <@printParameters macro />
                <@printOptional macro.@nested?if_exists, "Nested" />
                <@printOptional macro.@return?if_exists, "Return value" />
                <@printSourceCode macro />
            </dl>
        </dd>
    </dl>
    <#if macro_has_next><hr></#if>
</#list>

<#-- end details -->
</main>
</body>
</html>

<#macro printParameters macro>
    <#if macro.@param?has_content>
        <dt><b>Parameters</b></dt>
        <dd>
        <#if macro.@param?has_content>
        <table class="params">
            <thead>
                <tr>
                    <th>Name</th>
                    <th>Type</th>
                    <th>Description</th>
                </tr>
            </thead>
            <tbody>
            <#list macro.@param as param>
                <tr>
                    <td class="name"><code>${param.name!}</code></td>
                    <td class="type">
                        <#list param.type as type>
                            <span class="param-type">${type!}</span>
                            <#sep>|</#sep>
                        </#list>
                    </td>
                    <td class="description">
                        <#if param.optional!false><em>(Optional)</em> </#if>
                        ${param.description!}<br/>
                    </td>
                <#if param.def_val?has_content>
                Default value : ${param.def_val!}<br/>
                </#if>
                </tr>
            </#list>
            </tbody>
        </table>
            
        </#if>
        </dd>
    </#if>
</#macro>

<#macro printSourceCode macro>
    <dt><a href="#" onClick="toggle('sc_${macro.name}'); return false;">Source Code</a></dt>
    <dd>
        <code class="sourcecode" id="sc_${macro.name}">
            <@ftl.print root=macro.node/>
        </code>
    </dd>
</#macro>

<#macro printOptional value label>
    <#if value?has_content>
        <dt><b>${label}</b></dt>
        <dd>${value}</dd>
    </#if>
</#macro>

<#macro printDeprecated reason>
    <dt>&#9888; Deprecated<#if reason?? && reason?trim?length != 0>: ${reason}</#if>
    </dt><br />
</#macro>

<#macro signature macro>
    <#if macro.isfunction>
        (
        <#list macro.arguments as argument>
            ${argument}
            <#if argument_has_next>,</#if>
        </#list>
        )
    <#else>
        <#list macro.arguments as argument>
            ${argument}
        </#list>
    </#if>
</#macro>
