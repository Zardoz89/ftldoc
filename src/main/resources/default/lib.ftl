<#ftl encoding="UTF-8" output_format="HTML" strip_text="true" />

<#include "/ftl_highlight.ftl" />
<#---@begin Navigation related macros -->

<#--- Generates the navigation bar
    @param {Hash<File, Sequence<File>>} files List of files
    @param {String} fileSuffix Suffix of file name (ie. extension)
    -->
<#macro navigationBar categorizedFiles fileSuffix>
<nav>
    <ul>
        <li><a href="index.html">Overview</a></li>
        <li><a href="index-all-cat.html">Index (categorical)</a></li>
        <li><a href="index-all-alpha.html">Index (alphabetical)</a></li>
    </ul>
    <@_fileList categorizedFiles fileSuffix />
</nav>
</#macro>

<#--- Internal macro that generates the file list
    @param {Hash<File, Sequence<File>>} files List of files
    @param {String} fileSuffix Suffix of file name (ie. extension)
    -->
<#macro _fileList categorizedFiles fileSuffix>
    <h6>Macro Libraries</h6>
    <#list categorizedFiles as category, files>
        <#if categorizedFiles?keys?size gt 1>
            <h7>${category.getName()}</h7>
        </#if>
        <ul>
        <#list files as file>
            <li><a href="${file.name}${fileSuffix}">${file.name}</a></li>
        </#list>
        </ul>
    </#list>
</#macro>

<#---@end -->
<#---@begin Printing macro information -->

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
                        ${(param.description!)?no_esc}
                        <#if param.def_val?has_content>
                        <br/>
                        Default value : ${param.def_val!}
                        </#if>
                    </td>
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
            <@print root=macro.node/>
        </code>
    </dd>
</#macro>

<#macro printOptional value label>
    <#if value?has_content>
        <dt><b>${label}</b></dt>
        <dd>${value?no_esc}</dd>
    </#if>
</#macro>

<#macro printDeprecated reason>
<#compress>
    <span class="deprecated">&#9888; Deprecated</span><#t/>
    <#if reason?has_content><span class="deprecated__reason">${reason?no_esc}</span></#if><#t/>
</#compress>
</#macro>

<#macro signature macro>
    <#if macro.isfunction>
        (
        <#list macro.arguments as argument>
            <span class="macro__signature-argument">${argument!}</span><#sep> , </#sep>
        </#list>
        )
    <#else>
        <#list macro.arguments as argument>
            <span class="macro__signature-argument">${argument!}</span>
        </#list>
    </#if>
</#macro>

<#---@end -->
