<#ftl encoding="UTF-8" output_format="HTML" strip_text="true" />

<#include "/ftl_highlight.ftl" />
<#---@begin Navigation related macros -->

<#--- Generates the navigation bar
    @param {Sequence<File>} files List of files
    @param {String} fileSuffix Suffix of file name (ie. extension)
    -->
<#macro navigationBar files fileSuffix>
<nav>
    <ul>
        <li><a href="index.html">Overview</a></li>
        <li><a href="index-all-cat.html">Index (categorical)</a></li>
        <li><a href="index-all-alpha.html">Index (alphabetical)</a></li>
    </ul>
    <@_fileList files fileSuffix />
</nav>
</#macro>

<#--- Internal macro that generates the file list
    @param {Sequence<File>} files List of files
    @param {String} fileSuffix Suffix of file name (ie. extension)
    -->
<#macro _fileList files fileSuffix>
    <h6>Macro Libraries</h6>
    <ul>
    <#list files as file>
        <li><a href="${file.name}${fileSuffix}">${file.name}</a></li>
    </#list>
    </ul>
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
                        ${param.description!}
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
        <dd>${value}</dd>
    </#if>
</#macro>

<#macro printDeprecated reason>
<#compress>
    <span class="deprecated">&#9888; Deprecated</span><#t/>
    <#if reason?has_content><span class="deprecated__reason">${reason}</span></#if><#t/>
</#compress>
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

<#---@end -->
