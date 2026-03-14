<#ftl encoding="UTF-8" />
<#---
    Global Variables File

    @author Inanna Panadero
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

<#-- This comment must be ignored -->
<#global myNoCommentVar = true>


