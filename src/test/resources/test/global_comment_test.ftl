<#ftl encoding="UTF-8" />
<#---
    This is a global comment for the global_comment_test.ftl file.
    It should appear in the documentation as the file description.
    @author Global Author
    @version 1.0
-->
<#---
    A simple greeting macro.
    @param {String} name The name to greet
-->
<#macro greet name>
    Hello ${name}!
</#macro>
