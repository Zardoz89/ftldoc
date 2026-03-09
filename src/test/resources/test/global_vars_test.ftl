<#ftl encoding="UTF-8" />
<#---
    Global Variables Test File
    @author Test Author
    @version 1.0
-->

<#--- Global variable with type and description
    @type {String}
-->
<#global myStringVar = "hello">

<#--- Global variable with complex type
    @type {Hash}
-->
<#global myHashVar = {"key": "value"}>

<#--- Global variable without type annotation
-->
<#global myUntypedVar = 123>

<#--- Global variable without any comment
-->
<#global myNoCommentVar = true>

<#--- A macro to test that #assign is not captured
    @param {String} name The name
-->
<#macro greet name>
    Hello ${name}!
</#macro>

<#assign regularAssignVar = "not global">
