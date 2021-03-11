<#ftl encoding="UTF-8" output_format="HTML" />

<#--- Generates the file list -->
<#macro fileList files fileSuffix>
<nav>
    <h3>Macro Libraries</h3>
    <ul>
    <#list files as file>
        <li><a href="${file.name}${fileSuffix}">${file.name}</a></li>
    </#list>
    </ul>
</nav>
</#macro>
